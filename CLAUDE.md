# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供项目指导，所有对话使用中文。

## 项目概述

RuoYi-IM 是一个全栈企业级即时通讯系统：
- **后端**: Java 8, Spring Boot 2.7, WebSocket, MyBatis Plus, MySQL, Redis
- **前端**: Vue 3 (Composition API), Vite, Element Plus, Vuex, Vue Router
- **管理后台**: RuoYi v4.8.0，使用 Thymeleaf 和 Shiro

## 项目结构

```
im/
├── ruoyi-im-api/          # 核心API服务 (Spring Boot, 端口 8080)
│   └── src/main/java/com/ruoyi/im/
│       ├── controller/    # REST API 接口
│       ├── service/       # 业务逻辑
│       ├── mapper/        # MyBatis-Plus 数据访问层
│       ├── domain/        # 实体类
│       ├── config/        # Spring 配置 (WebSocket, Security, Redis)
│       └── ImApplication.java  # 主入口
├── ruoyi-im-web/          # 前端聊天界面 (Vue 3 + Vite, 端口 3000)
│   └── src/
│       ├── api/           # API 客户端调用
│       ├── components/    # Vue 组件
│       ├── views/         # 页面视图
│       ├── store/         # Vuex 状态管理
│       ├── router/        # Vue Router 配置
│       └── main.js        # Vue 应用入口
├── ruoyi-im-admin/        # 管理后台 (RuoYi 框架, 端口 8081)
├── sql/                   # 数据库结构文件
├── docs/                  # 项目文档
└── pom.xml               # 父级 Maven POM
```

## 开发命令

### 前端 (ruoyi-im-web)
```bash
cd ruoyi-im-web
npm install              # 安装依赖
npm run dev             # 启动开发服务器 (端口 3000)
npm run build           # 生产环境构建
npm run lint            # ESLint 自动修复
npm run format          # Prettier 格式化
npm run type-check      # TypeScript 类型检查
npm run test            # 运行 Vitest 测试
npm run test:coverage   # 运行测试并生成覆盖率报告
```

### 后端 (ruoyi-im-api)
```bash
cd ruoyi-im-api
mvn clean package       # 构建 JAR 文件
mvn test               # 运行测试
# 运行 ImApplication.java 的 main 方法启动服务器 (端口 8080)
```

### 管理后台 (ruoyi-im-admin)
```bash
cd ruoyi-im-admin/ruoyi-admin
mvn clean package
# 运行主应用启动管理服务器 (端口 8081)
```

## 架构模式

### 后端分层架构
- **Controller 层**: RESTful API 接口，Spring Security 安全认证
- **Service 层**: 业务逻辑，事务管理
- **Mapper 层**: MyBatis-Plus 数据库操作
- **WebSocket**: 实时消息推送 (端点: `/ws`)

### 前端架构
- **Vue 3 Composition API** 使用 `<script setup>` 语法
- **Vuex** 集中式状态管理 (用户、消息、会话)
- **Vue Router** 路由守卫和路由管理
- **Element Plus** 组件通过 unplugin 自动导入
- **Axios** HTTP 请求，JWT 认证拦截器
- **WebSocket Client** 实时消息更新

### WebSocket 通信
- 后端端点: `ws://localhost:8080/ws`
- 前端通过 JWT token 认证连接 WebSocket
- 30秒心跳间隔保持连接健康
- Redis 发布订阅实现多实例消息分发

### 状态管理 (Vuex)
- `user`: 用户资料、认证状态、在线状态
- `chat`: 当前会话、消息历史、输入状态
- `contact`: 好友列表、群组列表
- `websocket`: WebSocket 连接状态

### API 代理配置
Vite 开发服务器代理 API 和 WebSocket 请求:
- `/api/*` → `http://localhost:8080` (REST API)
- `/system/*` → `http://localhost:8080` (系统 API)
- `/ws/*` → `ws://localhost:8080` (WebSocket)

## 核心依赖

### 后端
- Spring Boot 2.7.18
- Spring Security (JWT 认证)
- Spring WebSocket + STOMP
- MyBatis Plus 3.5.3
- Redis (缓存、发布订阅、会话存储)
- MySQL 8.0.33
- Hutool 5.8.18 (工具类库)
- Lombok (代码生成)
- SpringDoc OpenAPI (API 文档 `/swagger-ui.html`)

### 前端
- Vue 3.3.11
- Vite 5.0.7
- Element Plus 2.4.4 (UI 组件库)
- Vuex 4.1.0
- Vue Router 4.2.5
- Axios 1.6.2
- dayjs (日期处理)
- Quill (富文本编辑器)
- Cropper.js (图片裁剪)
- vue-i18n (国际化)

## 数据库
- MySQL 数据表: `im_user`, `im_message`, `im_friend`, `im_group`, `im_group_member`, `im_conversation`, `im_file_asset`
- SQL 结构文件位于 `sql/` 目录
- 默认管理员账号: admin/123456

## 构建与部署
- 后端 JAR 包含嵌入式 Tomcat
- 前端构建到 `dist/` 目录，支持 gzip/brotli 压缩
- 生产环境推荐使用 Nginx 反向代理和 WebSocket 支持

## 重要约束

### 数据库字段变更规则
⚠️ **数据库结构是单一事实来源，任何字段变更必须先确认数据库表结构**

1. 修改表结构前，必须先查看 `docs/DATABASE-SCHEMA.md` 确认当前字段定义
2. 变更数据库后，需要同步更新:
   - Entity 实体类 (`domain/*.java`)
   - Mapper XML (`resources/mapper/*.xml`)
   - API 接口返回字段
3. 所有字段命名遵循: 数据库 `snake_case` ↔ Java `camelCase` 自动转换

### 代码规范
- Entity 类使用 Lombok 注解，字段名与数据库列名自动映射
- Mapper XML 中明确指定所有字段映射关系
- 避免使用 `select *`，明确列出所需字段
