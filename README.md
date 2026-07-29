# AI Interview Coach

An AI-powered Interview Preparation Platform built with Java, Spring Boot, PostgreSQL, and AI.

## Tech Stack

- Java 21
- Spring Boot
- PostgreSQL
- Spring Data JPA
- Maven

## Features

- User Authentication (Coming Soon)
- AI Interview Generator
- AI Answer Evaluation
- Resume Analyzer
- Personalized Study Planner

## Status

✅ Day 1 Completed
## Progress

### Day 1
- Spring Boot project setup
- PostgreSQL configuration
- Health Check API

### Day 2
- User Entity
- JPA Repository
- Service Layer
- REST Controller
- CRUD APIs
- PostgreSQL Integration
- Tested APIs using Postman
- ## Day 3
- Added DTOs (UserRequest & UserResponse)
- Added Bean Validation
- Added @Valid request validation
- Added Global Exception Handler
- Tested validation with Postman
## Day 4 - Authentication with Spring Security

### Features Implemented
- Added Spring Security dependency
- Configured BCrypt Password Encoder
- Passwords are stored in encrypted format
- Implemented Login API
- Created LoginRequest and LoginResponse DTOs
- Added custom exceptions:
    - UserNotFoundException
    - InvalidPasswordException
- Improved authentication flow
- Tested registration and login using Postman

### Endpoints
POST /api/v1/users/register
POST /api/v1/users/login

### Tech Used
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- BCrypt
- Postman

## 📅 Day 5 Progress (JWT Authentication)

### ✅ Completed
- Added JJWT dependency
- Created `JwtService` for JWT generation
- Configured JWT secret key in `application.properties`
- Generated JWT token after successful login
- Updated `LoginResponse` to include JWT token
- Integrated `JwtService` with `UserService`
- Successfully tested login and JWT generation using Postman

### 🧪 Login API Response

```json
{
  "message": "Login Successful",
  "name": "Vaibhav",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 🔐 Current Authentication Flow

```
User Registration
        │
        ▼
Password encrypted using BCrypt
        │
        ▼
User Login
        │
        ▼
Password Verification
        │
        ▼
JWT Token Generation
        │
        ▼
JWT Returned to Client
```

### 🚀 Next Step
- Validate JWT tokens
- Implement JWT Authentication Filter
- Protect APIs using Spring Security


- ## 📅 Day 6 Progress (JWT Filter & Route Protection)

### ✅ Completed
- Added JwtAuthenticationFilter
- Registered JWT filter in SecurityConfig
- Configured Spring Security filter chain
- Protected all endpoints except:
  - POST /api/v1/users/register
  - POST /api/v1/users/login
- Implemented Authorization header extraction
- Extracted Bearer JWT token from incoming requests
- Verified JWT token interception using Postman
- Successfully generated JWT after login

### 📅 Day 7 Progress (JWT Authentication)

✅ Implemented CustomUserDetailsService

✅ Completed JwtAuthenticationFilter

✅ Extracted email from JWT

✅ Validated JWT token

✅ Authenticated users using SecurityContextHolder

✅ Protected endpoints using Spring Security

✅ Successfully accessed protected APIs with Bearer Token in Postman

🔐 Authentication Flow:
Login → Receive JWT → Send Bearer Token → JWT Filter Validates Token → SecurityContext Updated → Protected Endpoint Access Granted
## 📅 Day 8 Progress (Role-Based Authorization)

✅ Enabled Method Security using `@EnableMethodSecurity`

✅ Added role-based authorization with `@PreAuthorize`

✅ Created protected `/profile` endpoint (USER & ADMIN)

✅ Created protected `/admin` endpoint (ADMIN only)

✅ Successfully tested Bearer Token authentication

✅ Verified USER receives 403 Forbidden when accessing ADMIN endpoint

🔐 Authorization Flow:
Login → Receive JWT → Send Bearer Token → JWT Authentication Filter → Security Context → Role Check → Endpoint Access Granted/Denied
## Day 10 – Spring AI & OpenRouter Integration

### ✅ Completed
- Integrated Spring AI into the project.
- Created dedicated AI module:
  - `AiController`
  - `AiService`
  - `PromptRequest`
  - `PromptResponse`
- Added `/api/ai/chat` REST endpoint.
- Configured Spring Security to allow public access to AI endpoints.
- Implemented AI request/response flow using Spring AI `ChatClient`.
- Migrated from Google Gemini to OpenRouter due to Google Cloud billing restrictions.
- Successfully connected the backend with the OpenRouter API.
- Verified that requests travel through the complete pipeline:
  ```
  Client → Spring Boot → AI Service → OpenRouter
  ```

### 🐛 Issues Resolved
- Fixed Spring AI dependency configuration.
- Resolved Spring Boot startup issues caused by AI configuration.
- Fixed `403 Forbidden` by updating Spring Security configuration.
- Verified JWT filter compatibility with public AI endpoints.
- Confirmed AI endpoint receives and processes requests correctly.

### ⚠️ Current Blocker
- OpenRouter account has **$0.00 credits**, resulting in:
  ```
  HTTP 402 - Payment Required
  ```
- Backend integration is complete; only API credits are required to receive AI-generated responses.

### 📌 Next Steps (Day 11)
- Add OpenRouter credits.
- Verify successful AI responses.
- Design Interview Prompt Engine.
- Create Interview Modes:
  - HR
  - Java
  - Spring Boot
  - SQL
  - DSA
- Begin AI Interview Session workflow.
- ## 📅 Day 11
- Built the AI Interview module (Controller, Service, DTOs, Prompt Builder)
- Integrated Spring AI with OpenRouter
- Configured environment variables and security
- Added Interview start API and prompt generation
- Debugged Spring AI ↔ OpenRouter integration (ongoing)
- ## Day 12 - Interview Module Foundation

### Completed
- Created modular interview package structure.
- Added `Interview` entity with JPA annotations.
- Added interview enums:
  - `InterviewType`
  - `InterviewStatus`
  - `Difficulty`
- Created `InterviewRepository`.
- Organized the project using a feature-based architecture.
- Verified the application builds and runs successfully.

### Current Progress
- ✅ Authentication & JWT
- ✅ AI Question Generation
- ✅ Interview Module Foundation
- 🚧 Interview Persistence (In Progress)

### Next Steps
- Associate interviews with authenticated users.
- Save interviews in MySQL.
- Create interview sessions and questions.
- Implement answer evaluation and interview history.
- ## Progress

- [x] User Authentication (JWT)
- [x] Spring Security
- [x] AI Integration (Spring AI)
- [x] Prompt Builder
- [x] Interview Module Structure
- [x] Interview Entity & Repository
- [ ] Interview Persistence
- [ ] Interview Session Management
- [ ] AI Evaluation
- [ ] Interview Analytics