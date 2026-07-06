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
- 📅 Day 5 Progress (JWT Authentication)
  ✅ Completed
  Added JJWT dependency
  Created JwtService for JWT generation
  Configured JWT secret key in application.properties
  Generated JWT token after successful login
  Updated LoginResponse to include JWT token
  Integrated JwtService with UserService
  Successfully tested login and JWT generation using Postman
  🧪 Login API Response
  {
  "message": "Login Successful",
  "name": "Vaibhav",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  🔐 Current Authentication Flow
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