# Chat Application - System Architecture

## 1. Overview

Chat Application là hệ thống nhắn tin thời gian thực (Real-time Chat System), cho phép người dùng đăng ký tài khoản, kết bạn, tạo cuộc trò chuyện cá nhân hoặc nhóm, gửi tin nhắn văn bản, hình ảnh và nhận thông báo theo thời gian thực.

Hệ thống được xây dựng theo kiến trúc tách biệt Frontend và Backend:

* Frontend: Next.js + TypeScript
* Backend: Spring Boot
* Database: MySQL
* Cache / Session / Online Status: Redis
* Real-time Communication: WebSocket
* Containerization: Docker

---

## 2. Goals

### Functional Goals

* Đăng ký / Đăng nhập
* Quản lý hồ sơ người dùng
* Kết bạn
* Tạo cuộc trò chuyện cá nhân
* Tạo nhóm chat
* Gửi và nhận tin nhắn thời gian thực
* Hiển thị trạng thái Online/Offline
* Hiển thị trạng thái đã xem (Seen)
* Gửi hình ảnh và file
* Thông báo (Notification)

### Non-functional Goals

* Hiệu năng tốt
* Hỗ trợ nhiều người dùng đồng thời
* Dễ bảo trì
* Dễ mở rộng
* Tách biệt Frontend và Backend
* Dễ triển khai bằng Docker

---

## 3. Technology Stack

| Layer            | Technology               |
| ---------------- | ------------------------ |
| Frontend         | Next.js 16 + TypeScript  |
| UI               | Tailwind CSS + Shadcn UI |
| State Management | React Query              |
| Backend          | Spring Boot              |
| Authentication   | JWT                      |
| Database         | MySQL                    |
| Cache            | Redis                    |
| Realtime         | WebSocket                |
| ORM              | Spring Data JPA          |
| Build Tool       | Maven                    |
| Container        | Docker                   |
| Version Control  | Git + GitHub             |

---

## 4. System Architecture

```text
                    ┌───────────────────┐
                    │      Client       │
                    │ Browser / Mobile  │
                    └─────────┬─────────┘
                              │
                  HTTP / WebSocket
                              │
                              ▼
                ┌────────────────────────┐
                │      Next.js UI        │
                │      Frontend          │
                └──────────┬─────────────┘
                           │
                    REST API / WS
                           │
                           ▼
                ┌────────────────────────┐
                │      Spring Boot       │
                │       Backend          │
                └───────┬───────┬────────┘
                        │       │
                        │       │
                        ▼       ▼
                   MySQL      Redis
```

---

## 5. Backend Architecture

Backend áp dụng kiến trúc Module / Feature-Based.

```text
backend
│
├── auth
├── user
├── friend
├── conversation
├── message
├── notification
├── websocket
├── common
└── config
```

Mỗi module:

```text
module
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── validator
```

Ví dụ:

```text
message
│
├── controller
├── service
├── repository
├── entity
├── dto
└── mapper
```

---

## 6. Frontend Architecture

```text
frontend
│
├── app
├── components
├── features
├── hooks
├── lib
├── providers
├── services
└── types
```

Feature structure:

```text
features
│
├── auth
├── user
├── friend
├── conversation
├── message
└── notification
```

---

## 7. Database Architecture

Core Entities:

```text
User
│
├── Friend
│
├── Conversation
│      │
│      └── Message
│              │
│              └── Attachment
│
└── Notification
```

Tables:

```text
users
friendships
conversations
conversation_members
messages
attachments
notifications
```

---

## 8. Authentication & Authorization

Authentication sử dụng JWT.

Flow:

```text
Login
   │
   ▼
Backend
   │
Generate JWT
   │
   ▼
Frontend lưu Access Token
   │
   ▼
Request API
   │
Authorization: Bearer Token
```

---

## 9. Real-time Communication

Sử dụng WebSocket.

Flow:

```text
User A
   │
Send Message
   │
WebSocket
   │
Backend
   │
Save DB
   │
Publish Event
   │
WebSocket
   │
User B
```

---

## 10. Redis Strategy

Redis được sử dụng cho:

* Online Status
* Session
* Cache
* Temporary Data

Ví dụ:

```text
online:user:1
online:user:2

user:profile:1

conversation:15:last-message
```

---

## 11. Message Processing Flow

```text
Frontend
   │
Send Message
   │
WebSocket
   │
Controller
   │
Service
   │
Repository
   │
MySQL
   │
Redis Update
   │
Push Notification
   │
Receiver
```

---

## 12. File Upload

Giai đoạn đầu:

```text
Frontend
   │
Backend
   │
Local Storage
```

Giai đoạn mở rộng:

```text
AWS S3
Cloud Storage
MinIO
```

---

## 13. Docker Architecture

```text
Docker
│
├── frontend
├── backend
├── mysql
└── redis
```

docker-compose:

```yaml
frontend
backend
mysql
redis
```

---

## 14. Security

Áp dụng:

* JWT Authentication
* Password Hashing (BCrypt)
* CORS
* Input Validation
* SQL Injection Prevention
* XSS Protection
* Rate Limiting

---

## 15. Scalability

Có thể mở rộng:

```text
Nginx
   │
Load Balancer
   │
Backend 1
Backend 2
Backend 3
   │
Redis
   │
MySQL
```

---

## 16. Future Improvements

* Group Chat
* Video Call
* Voice Message
* Message Search
* AI Chatbot
* Push Notification
* Kubernetes Deployment

---

## 17. Development Principles

* Clean Code
* SOLID
* Feature-Based Architecture
* API First
* Documentation First
* Git Flow
* Code Review
* CI/CD

---

## 18. Repository Structure

```text
chat-application
│
├── backend
├── frontend
├── docs
│   ├── architecture
│   ├── api
│   ├── database
│   └── development
│
├── docker
├── scripts
└── README.md
```
