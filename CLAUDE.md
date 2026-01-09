# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RuoYi-IM is a full-stack enterprise instant messaging system built with:
- **Backend**: Java 8, Spring Boot 2.7, WebSocket, MyBatis Plus, MySQL, Redis
- **Frontend**: Vue 3 (Composition API), Vite, Element Plus, Vuex, Vue Router
- **Admin**: RuoYi v4.8.0 admin panel with Thymeleaf and Shiro

## Project Structure

```
im/
├── ruoyi-im-api/          # Core API service (Spring Boot, port 8080)
│   └── src/main/java/com/ruoyi/im/
│       ├── controller/    # REST API endpoints
│       ├── service/       # Business logic
│       ├── mapper/        # MyBatis-Plus database access
│       ├── domain/        # Entity models
│       ├── config/        # Spring configuration (WebSocket, Security, Redis)
│       └── ImApplication.java  # Main entry point
├── ruoyi-im-web/          # Frontend chat interface (Vue 3 + Vite, port 3000)
│   └── src/
│       ├── api/           # API client calls
│       ├── components/    # Vue components
│       ├── views/         # Page views
│       ├── store/         # Vuex state management
│       ├── router/        # Vue Router configuration
│       └── main.js        # Vue app entry point
├── ruoyi-im-admin/        # Admin management panel (RuoYi framework, port 8081)
├── sql/                   # Database schema files
└── pom.xml               # Parent Maven POM
```

## Development Commands

### Frontend (ruoyi-im-web)
```bash
cd ruoyi-im-web
npm install              # Install dependencies
npm run dev             # Start dev server (port 3000)
npm run build           # Production build
npm run lint            # ESLint with auto-fix
npm run format          # Prettier format
npm run type-check      # TypeScript type checking
npm run test            # Run Vitest tests
npm run test:coverage   # Run tests with coverage
```

### Backend (ruoyi-im-api)
```bash
cd ruoyi-im-api
mvn clean package       # Build JAR file
mvn test               # Run tests
# Run ImApplication.java main method to start server (port 8080)
```

### Admin Panel (ruoyi-im-admin)
```bash
cd ruoyi-im-admin/ruoyi-admin
mvn clean package
# Run main application to start admin server (port 8081)
```

## Architecture Patterns

### Backend Layered Architecture
- **Controller Layer**: RESTful API endpoints with Spring Security
- **Service Layer**: Business logic with transaction management
- **Mapper Layer**: MyBatis-Plus for database operations
- **WebSocket**: Real-time messaging via Spring WebSocket (endpoint: `/ws`)

### Frontend Architecture
- **Vue 3 Composition API** with `<script setup>` syntax
- **Vuex** for centralized state management (user, messages, conversations)
- **Vue Router** for navigation guards and route management
- **Element Plus** components auto-imported via unplugin
- **Axios** for HTTP requests with interceptors for JWT auth
- **WebSocket Client** for real-time message updates

### WebSocket Communication
- Backend endpoint: `ws://localhost:8080/ws`
- Frontend connects via WebSocket with JWT token authentication
- 30-second heartbeat interval for connection health
- Redis pub/sub for message distribution across instances

### State Management (Vuex)
- `user`: User profile, authentication status, online status
- `chat`: Current conversation, message history, typing status
- `contact`: Friends list, group list
- `websocket`: WebSocket connection state

### API Proxy Configuration
The Vite dev server proxies API and WebSocket requests:
- `/api/*` → `http://localhost:8080` (REST API)
- `/system/*` → `http://localhost:8080` (System API)
- `/ws/*` → `ws://localhost:8080` (WebSocket)

## Key Dependencies

### Backend
- Spring Boot 2.7.18
- Spring Security (JWT-based auth)
- Spring WebSocket + STOMP
- MyBatis Plus 3.5.3
- Redis (caching, pub/sub, session storage)
- MySQL 8.0.33
- Hutool 5.8.18 (utility library)
- Lombok (code generation)
- SpringDoc OpenAPI (API documentation at `/swagger-ui.html`)

### Frontend
- Vue 3.3.11
- Vite 5.0.7
- Element Plus 2.4.4 (UI component library)
- Vuex 4.1.0
- Vue Router 4.2.5
- Axios 1.6.2
- dayjs (date manipulation)
- Quill (rich text editor)
- Cropper.js (image cropping)
- vue-i18n (internationalization)

## Database
- MySQL with tables: `im_user`, `im_message`, `im_friend`, `im_group`, `im_group_member`, `im_conversation`, `im_file_asset`
- SQL schema files in `sql/` directory
- Default admin credentials: admin/123456

## Build & Deployment
- Backend JAR includes embedded Tomcat
- Frontend builds to `dist/` directory with gzip/brotli compression
- Nginx recommended for reverse proxy and WebSocket support in production
