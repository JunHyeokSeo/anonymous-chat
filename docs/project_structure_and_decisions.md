# 프로젝트 패키지 구조 및 계층별 설계 결정 요약

## 설계 목적

- 유지보수성과 실무 유사성을 고려한 백엔드 구조 설계
- 계층별 책임 명확화 및 DTO 책임 분리
- 단방향 의존과 역할 기반 네이밍으로 결합도 최소화

---

## 패키지 구조

```
com.anonymouschat
├── domain
│   └── [도메인별 엔티티, 리포지토리, 도메인 예외]
├── application
│   └── [서비스 로직, Command/Result DTO]
├── facade
│   └── [유즈케이스 조립, Criteria/Result DTO]
├── interface
│   └── api
│       └── [Controller, Request/Response DTO]
├── infra
│   └── [보안, 외부 연동]
├── common
    └── [예외, 응답 포맷, 유틸]
```

## 1차 작업 기준(변경 가능)
```

com.anonymouschat
├── AnonymousChatApplication.java
├── domain
│   ├── user
│   │   ├── User.java
│   │   ├── Gender.java
│   │   ├── UserRepository.java
│   │   └── exception
│   │       └── UserNotFoundException.java
│   ├── chatroom
│   │   ├── ChatRoom.java
│   │   ├── ChatRoomStatus.java
│   │   ├── ChatRoomRepository.java
│   │   └── ChatRoomExit.java

├── application
│   ├── user
│   │   ├── UserService.java
│   │   └── dto
│   │       ├── RegisterUserCommand.java
│   │       ├── UserProfileResult.java
│   │       └── UserSearchCondition.java
│   ├── chatroom
│   │   ├── ChatRoomService.java
│   │   └── dto
│   │       ├── CreateChatRoomCommand.java
│   │       └── ChatRoomInfoResult.java

├── facade
│   ├── user
│   │   ├── UserFacade.java
│   │   └── dto
│   │       ├── RegisterUserCriteria.java
│   │       ├── RegisterUserResult.java
│   │       └── UserSearchCriteria.java
│   ├── chatroom
│   │   ├── ChatRoomFacade.java
│   │   └── dto
│   │       ├── CreateChatRoomCriteria.java
│   │       ├── CreateChatRoomResult.java

├── interface
│   └── api
│       ├── user
│       │   ├── UserController.java
│       │   └── dto
│       │       ├── RegisterUserRequest.java
│       │       ├── RegisterUserResponse.java
│       │       └── SearchUserRequest.java
│       ├── chatroom
│       │   ├── ChatRoomController.java
│       │   └── dto
│       │       ├── CreateChatRoomRequest.java
│       │       └── ChatRoomResponse.java

├── infra
│   └── security
│       ├── SecurityConfig.java
│       ├── JwtAuthenticationFilter.java
│       └── JwtTokenProvider.java

├── common
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorCode.java
│   │   └── BaseException.java
│   ├── response
│   │   ├── ApiResponse.java
│   │   └── ApiResponseCode.java
│   └── util
│       └── TimeUtils.java
```

---

## 계층별 DTO 네이밍 및 책임

| 계층 | DTO 타입 | 네이밍 예 | 설명 |
|------|----------|------------|------|
| Controller | Request / Response | `RegisterUserRequest`, `UserProfileResponse` | 외부 API 요청/응답 전용 |
| Facade     | Criteria / Result  | `RegisterUserCriteria`, `RegisterUserResult` | 유즈케이스 조립용 입력/출력 |
| Service    | Command / Result   | `RegisterUserCommand`, `UserProfileResult`   | 도메인 처리용 입력/출력 |

---

## DTO 변환 전략

- 변환은 항상 **상위 계층에서 하위 계층으로만** 발생 (단방향)
- `toXxx()` / `fromXxx()` 메서드 또는 전용 Mapper 사용
- DTO 간 결합은 가급적 최소화

---

## 책임/분리 기준

- DTO는 **계층별로 무조건 분리**
- **필드가 같아도 책임이 다르면 분리**
- **DTO 변환은 해당 계층의 책임**
- 단순하면 DTO 내부 변환 메서드, 복잡하면 전용 Mapper

---

## 예시 흐름

```
UserController
    ↓ uses
RegisterUserRequest → RegisterUserCriteria
    ↓ uses
UserFacade
    ↓ uses
RegisterUserCommand
    ↓ uses
UserService
    ↓ returns
UserProfileResult
    ↓ maps to
RegisterUserResult
    ↓ maps to
RegisterUserResponse
```

---