# anonymous-chat-server

익명 기반 1:1 채팅 애플리케이션의 백엔드 서버입니다.  
OAuth2 로그인, JWT 인증, 조건 기반 유저 검색, 채팅 기능 및 차단 기능 등을 제공합니다.

---

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA
- **Security**: Spring Security + OAuth2 + JWT
- **Database**: MySQL
- **Build Tool**: Gradle
- **Documentation**: Swagger or Spring REST Docs
- **Testing**: JUnit 5 + Mockito

---

## 주요 기능

### 회원가입 및 인증
- OAuth2 로그인 (Google, Apple 등)
- JWT 기반 인증/인가 (Access + Refresh 토큰 전략)
- 최초 로그인 시 닉네임, 성별, 나이, 지역, 자기소개 입력
- 프로필 사진 최대 3장 업로드

### 유저 검색
- 성별 / 나이 / 지역 필터 기반 검색
- 차단 유저는 노출 제외

### 1:1 채팅
- 채팅방 생성 및 메시지 전송
- 서버에 메시지 저장
- 채팅방에서 한 명이 나가면 상대방에게 “상대방이 나갔습니다” 메시지 표시
- 두 명 모두 나가면 채팅방 soft-delete

### 차단
- 유저 차단 및 해제
- 차단된 유저는 유저 리스트, 채팅에서 제외됨

---

## 아키텍처 및 구조

###  패키지 구조 (DDD-Lite)

```text
com.example.anonymouschat
├── domain          // 핵심 도메인(Entity, Repository 등)
├── application     // 유스케이스, 서비스
├── interface       // Controller, DTO
├── infra           // 외부 연동, 설정, 보안
```

### ️ 공통 설계
- 전역 예외 처리 (`@ControllerAdvice`)
- 공통 응답 포맷 (`ApiResponse<T>`)
- Validation 어노테이션 사용 (`@NotBlank`, `@Min`, `@Valid` 등)
- 환경 분리 (`application.yml` → dev, prod)

---

##  테스트 및 문서화

- 단위 테스트: JUnit5 + Mockito
- API 문서화: Swagger (or Spring REST Docs)
- Postman 테스트 컬렉션 제공 예정

---