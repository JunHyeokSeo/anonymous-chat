# 시퀀스 다이어그램

이 문서는 시스템의 주요 기능에 대한 시퀀스 다이어그램을 제공합니다.

## 1. 사용자 회원가입 및 로그인

OAuth2 소셜 로그인을 통해 사용자가 시스템에 처음 접근하여 회원가입하고, JWT를 발급받는 과정입니다.

```mermaid
sequenceDiagram
    participant Client as 클라이언트
    participant SpringSecurity as Spring Security
    participant OAuth2SuccessHandler as OAuth2SuccessHandler
    participant AuthUseCase as AuthUseCase
    participant UserService as UserService
    participant AuthService as AuthService
    participant JwtTokenProvider as JWT Provider
    participant UserRepository as UserRepository

    Client->>SpringSecurity: GET /oauth2/authorization/{provider}
    SpringSecurity->>Client: Redirect to OAuth Provider

    Client->>OAuth Provider: 로그인 및 동의
    OAuth Provider->>Client: Redirect to /auth/callback?code=...

    Client->>SpringSecurity: GET /auth/callback?code=...
    SpringSecurity->>OAuth2SuccessHandler: onAuthenticationSuccess()
    OAuth2SuccessHandler->>AuthUseCase: login(provider, providerId)
    AuthUseCase->>UserService: findByProviderAndProviderId()
    UserService->>UserRepository: findByProviderAndProviderId()
    alt 기존 사용자 없음
        UserRepository-->>UserService: Optional.empty()
        UserService->>UserRepository: save(Guest User)
        UserRepository-->>UserService: Guest User
        UserService-->>AuthUseCase: Guest User
    else 기존 사용자 있음
        UserRepository-->>UserService: User
        UserService-->>AuthUseCase: User
    end

    AuthUseCase->>AuthService: issueTokensForUser(user)
    AuthService->>JwtTokenProvider: createAccessToken(userId, role)
    JwtTokenProvider-->>AuthService: accessToken
    AuthService->>JwtTokenProvider: createRefreshToken(userId, role)
    JwtTokenProvider-->>AuthService: refreshToken
    AuthService-->>AuthUseCase: AuthData(tokens)
    AuthUseCase-->>OAuth2SuccessHandler: AuthData

    OAuth2SuccessHandler->>AuthUseCase: storeOAuthTempData(authData)
    AuthUseCase->>AuthService: storeOAuthTempData()
    AuthService-->>AuthUseCase: tempCode
    OAuth2SuccessHandler-->>Client: Redirect to /auth/callback?code={tempCode}

    Client->>AuthCallbackController: GET /auth/callback?code={tempCode}
    AuthCallbackController->>AuthUseCase: handleOAuthCallback(tempCode)
    AuthUseCase->>AuthService: consumeOAuthTempData(tempCode)
    AuthService-->>AuthUseCase: AuthTempData
    AuthUseCase->>AuthService: issueTokensForUser(user)
    AuthService-->>AuthUseCase: AuthData(accessToken, refreshToken)
    AuthUseCase-->>AuthCallbackController: AuthData
    AuthCallbackController-->>Client: HTML with tokens
```

## 2. 실시간 채팅 메시지 전송

WebSocket을 통해 클라이언트가 메시지를 보내고, 서버가 이를 처리하여 다른 클라이언트에게 브로드캐스트하는 과정입니다.

### 추상화 된 버전
```mermaid
sequenceDiagram
    participant C1 as Client A
    participant WS as WebSocket Handler
    participant SM as Session Manager
    participant MS as Message Service
    participant DB as Database
    participant C2 as Client B

    C1->>WS: Send Message
    WS->>MS: Process & Save Message
    MS->>DB: Store Message
    DB-->>MS: Message ID
    MS->>SM: Find Target Sessions
    SM-->>MS: Active Sessions
    
    par Broadcast to sender
        MS->>C1: Message Delivered
    and Broadcast to recipient
        MS->>C2: New Message Received
    end
    
    Note over MS: 비동기로 푸시 알림 전송
    MS->>MS: Trigger Push Event
```

### 클래스 간 상호작용 명확한 버전
```mermaid
sequenceDiagram
    participant ClientA as 클라이언트 A
    participant WebSocketHandler as ChatWebSocketHandler
    participant ChatMessageDispatcher as Dispatcher
    participant ChatMessageHandler as Handler
    participant MessageBroadcaster as Broadcaster
    participant MessageUseCase as MessageUseCase
    participant MessageService as MessageService
    participant ClientB as 클라이언트 B

    ClientA->>WebSocketHandler: Send(TextMessage: {type: 'CHAT', roomId: 1, content: '...'}) 
    WebSocketHandler->>ChatMessageDispatcher: dispatch(session, inboundMsg)
    ChatMessageDispatcher->>ChatMessageHandler: handle(session, inboundMsg)

    Handler->>MessageBroadcaster: broadcastExcept(roomId, outboundMsg, senderId)
    MessageBroadcaster->>ClientB: Send(TextMessage)

    par 비동기 처리
        Handler->>MessageUseCase: sendMessageAsync(request)
        MessageUseCase->>MessageService: saveMessage(chatRoom, sender, content)
        MessageService->>MessageRepository: save(message)
        MessageRepository-->>MessageService: Saved Message
        MessageService-->>MessageUseCase: Message ID

        alt 온라인 수신자 없음
            MessageUseCase->>ApplicationEventPublisher: publishEvent(PushNotificationRequired)
        end
    end
```

## 3. JWT 토큰 갱신
```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant TokenService
    participant Redis
    
    Client->>AuthController: POST /api/v1/auth/refresh
    AuthController->>TokenService: Refresh Token
    TokenService->>Redis: Validate Refresh Token
    Redis-->>TokenService: Token Valid
    TokenService->>TokenService: Generate New Access Token
    TokenService->>Redis: Update Token Set
    TokenService-->>AuthController: New Access Token
    AuthController-->>Client: {accessToken: "..."}
```

