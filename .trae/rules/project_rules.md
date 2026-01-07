遵循《阿里巴巴 Java 开发手册》，使用 JDK 1.8。
仅允许生成 Java + Vue 工程代码。

【强制要求】
- 中文回答，先查后改，避免重复创建，先查是否有已有的代码，有则直接修改，没有则创建新的代码。
- 严格分层（controller / service / mapper），禁止跨层调用
- 禁止使用 Java 9+ 语法或激进写法
- 所有类、public 方法、核心业务逻辑必须有中文注释
- 命名必须清晰，禁止 data、info、temp、obj、handle、process
- 禁止炫技式 Stream / lambda，复杂逻辑必须可断点调试
- common 包为受控区域，禁止堆放业务代码
- 代码必须可维护、可二次修改

【失败条件】
如出现分层违例、命名不规范、缺少注释，
本次代码视为不合格，必须重写。


## 3.1 后端技术栈

- 核心框架：Spring Boot 2.7

- ORM 框架：MyBatis Plus（简化数据库操作）

- 通信技术：WebSocket（实时消息推送）、Netty（高性能网络通信）

- 缓存中间件：Redis 3.0+（用户状态缓存、消息队列、在线状态存储）

- 数据库：MySQL 5.7.44（数据持久化）

- 安全框架：Spring Security（复用 RuoYi 权限认证）

- 文件存储：本地文件系统（基础版）

- 日志框架：SLF4J + Logback（日志记录与审计）

- 工具：hutool（日期时间处理、加密解密、文件操作等）Lombok 

## 3.2 前端技术栈

- 核心框架：Vue 3

- 构建工具：Vite（快速构建与热更新）

- UI 组件库：Element Plus

- 状态管理：Vuex（全局状态管理）

- 网络请求：Axios（HTTP 请求）、WebSocket API（实时消息）

- 文件上传：vue-upload-component（大文件分片上传）

- 富文本编辑：wangeditor（支持消息格式化）

## 3.3 中间件与依赖

- Redis：用于缓存用户 Token、在线状态、未读消息计数、消息队列（异步消息推送）

- Nginx：前端静态资源部署、反向代理后端接口、WebSocket 连接转发