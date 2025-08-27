# API 명세서

이 문서는 익명 채팅 애플리케이션의 API를 설명합니다.

## 인증

모든 API 요청은 `Authorization` 헤더에 Bearer 토큰을 포함해야 합니다.

`Authorization: Bearer {JWT_TOKEN}`

## API Endpoints

---

### User

#### `GET /api/v1/users/me`

- **내 프로필 조회**
- **설명**: 로그인한 사용자의 프로필을 조회합니다. (User, Admin 허용)
- **응답**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

#### `PUT /api/v1/users/me`

- **내 정보 수정**
- **설명**: 내 정보를 수정합니다. (User, Admin 허용)
- **요청 본문**: `UpdateRequest` DTO와 이미지 파일(multipart/form-data)을 포함합니다.
- **응답**:
  - `200 OK`: 수정 성공
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패

#### `DELETE /api/v1/users/me`

- **회원 탈퇴**
- **설명**: 회원 탈퇴 처리합니다. (User, Admin 허용)
- **응답**:
  - `200 OK`: 탈퇴 성공
  - `401 Unauthorized`: 인증 실패

#### `GET /api/v1/users`

- **유저 목록 검색**
- **설명**: 검색 조건에 맞는 유저 목록을 Slice 페이징으로 조회합니다. (User, Admin 허용)
- **쿼리 파라미터**: `SearchConditionRequest`, `Pageable`
- **응답**:
  - `200 OK`: 검색 성공
  - `401 Unauthorized`: 인증 실패

#### `POST /api/v1/users`

- **회원가입**
- **설명**: 신규 사용자가 회원가입합니다. (Guest만 허용)
- **요청 본문**: `RegisterRequest` DTO와 이미지 파일(multipart/form-data)을 포함합니다.
- **응답**:
  - `201 Created`: 회원가입 성공
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패

#### `GET /api/v1/users/{userId}`

- **사용자 프로필 조회**
- **설명**: 특정 사용자의 프로필을 조회합니다. (User, Admin 허용)
- **경로 변수**: `userId` (사용자 ID)
- **응답**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

---

### Message

#### `POST /api/v1/messages/read`

- **메시지 읽음 처리**
- **설명**: 특정 채팅방의 메시지들을 읽음 처리합니다. (USER, ADMIN 권한 필요)
- **요청 본문**: `MarkMessagesAsReadRequest` DTO
- **응답**:
  - `200 OK`: 읽음 처리된 메시지 개수 반환
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패

#### `GET /api/v1/messages`

- **채팅방 메시지 조회**
- **설명**: 특정 채팅방의 메시지를 페이징 조회합니다. (USER, ADMIN 권한 필요)
- **쿼리 파라미터**: `GetMessagesRequest`
- **응답**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

#### `GET /api/v1/messages/last-read`

- **상대방의 마지막 읽은 메시지 ID 조회**
- **설명**: 특정 채팅방에서 상대방이 마지막으로 읽은 메시지 ID를 조회합니다. (USER, ADMIN 권한 필요)
- **쿼리 파라미터**: `GetLastReadMessageRequest`
- **응답**:
  - `200 OK`: 상대방의 마지막 읽은 메시지 ID 반환
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 채팅방 또는 메시지 없음

---

### ChatRoom

#### `GET /api/v1/chat-rooms`

- **참여 중인 채팅방 목록 조회**
- **설명**: 현재 로그인한 사용자가 참여 중인 모든 채팅방을 조회합니다. (USER, ADMIN 권한 필요)
- **응답**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

#### `POST /api/v1/chat-rooms`

- **채팅방 생성 또는 조회**
- **설명**: 상대방과의 채팅방을 새로 생성하거나, 이미 존재하면 해당 채팅방을 반환합니다. (USER, ADMIN 권한 필요)
- **요청 본문**: `CreateRequest` DTO
- **응답**:
  - `201 Created`: 채팅방 생성 성공
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패

#### `GET /api/v1/chat-rooms/{roomId}`

- **채팅방 단건 조회**
- **설명**: roomId를 사용하여 채팅방을 조회합니다. (USER, ADMIN 권한 필요)
- **경로 변수**: `roomId` (채팅방 ID)
- **응답**:
  - `200 OK`: 채팅방 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 해당 채팅방 없음

#### `DELETE /api/v1/chat-rooms/{roomId}`

- **채팅방 나가기**
- **설명**: 현재 로그인한 사용자가 특정 채팅방을 나갑니다. (USER, ADMIN 권한 필요)
- **경로 변수**: `roomId` (채팅방 ID)
- **응답**:
  - `204 No Content`: 채팅방 나가기 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 해당 채팅방 없음

---

### Block

#### `POST /api/v1/blocks`

- **유저 차단**
- **설명**: 현재 로그인된 사용자가 다른 사용자를 차단합니다. (USER, ADMIN 권한 필요)
- **요청 본문**: `BlockRequest` DTO
- **응답**:
  - `201 Created`: 차단 성공
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패

#### `DELETE /api/v1/blocks`

- **유저 차단 해제**
- **설명**: 현재 로그인된 사용자가 이전에 차단한 사용자를 차단 해제합니다. (USER, ADMIN 권한 필요)
- **요청 본문**: `UnblockRequest` DTO
- **응답**:
  - `204 No Content`: 차단 해제 성공
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 차단 정보 없음

---

### Auth

#### `POST /api/v1/auth/refresh`

- **토큰 재발급**
- **설명**: 현재 사용자의 저장된 Refresh Token을 사용하여 Access Token을 재발급합니다.
- **응답**:
  - `200 OK`: 재발급 성공
  - `401 Unauthorized`: 인증 실패 또는 Refresh Token 없음

#### `POST /api/v1/auth/logout`

- **로그아웃**
- **설명**: 현재 로그인된 사용자의 Refresh Token을 무효화합니다. (USER, ADMIN 권한 필요)
- **응답**:
  - `200 OK`: 로그아웃 성공
  - `401 Unauthorized`: 인증 실패

---

### Auth Callback

#### `GET /auth/callback`

- **OAuth 인증 콜백 처리**
- **설명**: OAuth 로그인 성공 후 임시 토큰을 실제 토큰으로 교환하고 인증 처리를 완료합니다.
- **쿼리 파라미터**: `code` (임시 인증 코드)
- **응답**:
  - `200 OK`: 인증 콜백 처리 성공 (auth-callback.html 페이지로 이동)
  - `302 Found`: 리다이렉트 (로그인 페이지로 이동)
