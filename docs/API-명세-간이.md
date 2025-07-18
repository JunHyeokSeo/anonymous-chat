# API 명세서 - 익명 1:1 채팅 앱 백엔드

본 문서는 도메인별 기능 정의를 기반으로 한 REST API 명세서입니다. 각 API는 목적, 경로, 메서드, 요청/응답 예시를 포함하여 실질적인 구현 및 테스트에 바로 적용될 수 있도록 작성되었습니다.

---

## 1. 사용자(User)

### 🔹 회원 가입/정보 등록 (최초 로그인 이후)

* **POST** `/api/v1/users`
* **요청 Body**

```json
{
  "nickname": "밍글유저",
  "gender": "FEMALE",
  "age": 25,
  "region": "SEOUL",
  "bio": "안녕하세요."
}
```

* **응답** `201 Created`

```json
{
  "code": "SUCCESS",
  "message": "사용자 등록 완료",
  "data": { "userId": 1 }
}
```

---

### 🔹 사용자 정보 조회 (마이페이지)

* **GET** `/api/v1/users/me`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "사용자 정보",
  "data": {
    "id": 1,
    "nickname": "밍글유저",
    "gender": "FEMALE",
    "age": 25,
    "region": "SEOUL",
    "bio": "안녕하세요.",
    "profileImages": ["https://.../1.jpg"]
  }
}
```

---

### 🔹 사용자 검색

* **GET** `/api/v1/users/search?gender=FEMALE&region=SEOUL&minAge=20&maxAge=30`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "사용자 목록",
  "data": [
    { "id": 2, "nickname": "상대유저", "age": 27, "region": "SEOUL" }
  ]
}
```

---

## 2. 프로필 이미지

### 🔹 이미지 업로드

* **POST** `/api/v1/users/me/images`
* **요청 타입**: multipart/form-data
* **응답** `201 Created`

```json
{
  "code": "SUCCESS",
  "message": "이미지 업로드 완료",
  "data": {
    "imageId": 10,
    "imageUrl": "https://..."
  }
}
```

### 🔹 이미지 삭제

* **DELETE** `/api/v1/users/me/images/{imageId}`
* **응답** `204 No Content`

---

## 3. 채팅방(ChatRoom)

### 🔹 채팅방 생성

* **POST** `/api/v1/chatrooms`
* **요청 Body**

```json
{
  "targetUserId": 2
}
```

* **응답** `201 Created`

```json
{
  "code": "SUCCESS",
  "message": "채팅방 생성",
  "data": { "chatRoomId": 100 }
}
```

### 🔹 채팅방 목록 조회

* **GET** `/api/v1/chatrooms`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "채팅방 목록",
  "data": [
    {
      "chatRoomId": 100,
      "partner": { "id": 2, "nickname": "상대유저" },
      "lastMessage": "안녕하세요",
      "partnerExited": false
    }
  ]
}
```

### 🔹 채팅방 나가기

* **PATCH** `/api/v1/chatrooms/{chatRoomId}/exit`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "채팅방 나가기 완료"
}
```

---

## 4. 메시지(Message)

### 🔹 메시지 전송

* **POST** `/api/v1/chatrooms/{chatRoomId}/messages`
* **요청 Body**

```json
{
  "content": "안녕하세요!"
}
```

* **응답** `201 Created`

```json
{
  "code": "SUCCESS",
  "message": "메시지 전송 완료",
  "data": { "messageId": 999 }
}
```

### 🔹 메시지 조회

* **GET** `/api/v1/chatrooms/{chatRoomId}/messages?page=0&size=20`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "메시지 목록",
  "data": [
    {
      "messageId": 999,
      "senderId": 1,
      "content": "안녕하세요!",
      "sentAt": "2025-07-18T14:00:00"
    }
  ]
}
```

---

## 5. 차단(Block)

### 🔹 사용자 차단

* **POST** `/api/v1/blocks`
* **요청 Body**

```json
{
  "blockedUserId": 2
}
```

* **응답** `201 Created`

### 🔹 차단 해제

* **DELETE** `/api/v1/blocks/{blockedUserId}`
* **응답** `204 No Content`

### 🔹 차단 목록 조회

* **GET** `/api/v1/blocks`
* **응답** `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "차단 목록",
  "data": [
    { "id": 2, "nickname": "상대유저" }
  ]
}
```

---

> 모든 응답은 공통 포맷을 따르며, `401`, `403`, `404`, `500` 에 대한 에러 응답도 일관된 구조로 처리됩니다.
> 이 API 명세는 Swagger 및 REST Docs를 통해 자동 문서화될 예정입니다.
