# RuoYi-IM 即时通讯系统

## 项目简介

RuoYi-IM 是基于 RuoYi 框架开发的企业级即时通讯系统，采用领域驱动设计(DDD)架构，提供安全、高效的实时通讯解决方案。

## 核心功能

- 🔐 **用户管理**：注册登录、资料管理、在线状态
- 💬 **即时消息**：私聊、群聊、多媒体消息、消息撤回
- 👥 **好友系统**：好友添加、分组管理、黑名单
- 🏢 **群组管理**：群组创建、成员管理、权限控制
- 📁 **文件共享**：文件上传下载、在线预览、权限控制
- 🎥 **音视频通话**：WebRTC实现的语音视频通话
- 📊 **系统监控**：性能监控、日志审计、告警系统

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 2.5.15 + RuoYi 3.8.7
- **架构**: 领域驱动设计(DDD)四层架构
- **实时通讯**: WebSocket + STOMP + Netty
- **数据库**: MySQL 5.7+ + Redis 5.0+
- **消息队列**: RabbitMQ
- **安全**: JWT认证 + Spring Security

### 前端技术栈
- **框架**: Vue 3.3.11 + Vite 5.0.7
- **UI组件**: Element Plus 2.4.4
- **状态管理**: Vuex 4.1.0
- **实时通讯**: WebSocket + SockJS + STOMP

### 开发环境
- **操作系统**: Windows 11
- **开发工具**: VSCode
- **Java版本**: JDK 8
- **代码规范**: 阿里巴巴Java开发规范

## 项目结构

```
ruoyi-im-system/
├── docs/                           # 项目文档
│   ├── server/                     # 服务端文档
│   ├── web/                        # 前端文档
│   └── archive/                    # 历史文档
├── ruoyi-im-server/               # 后端服务
│   ├── ruoyi-admin/               # 管理后台
│   ├── ruoyi-common/              # 公共模块
│   ├── ruoyi-framework/           # 框架核心
│   ├── ruoyi-system/              # 系统功能
│   ├── ruoyi-im/                  # IM核心模块(DDD架构)
│   │   └── src/main/java/com/ruoyi/im/
│   │       ├── interfaces/        # 用户接口层
│   │       ├── application/       # 应用服务层
│   │       ├── domain/            # 领域层
│   │       └── infrastructure/    # 基础设施层
│   └── sql/                       # 数据库脚本
└── ruoyi-im-web/                  # 前端应用
    ├── src/
    │   ├── api/im/                # IM接口
    │   ├── views/im/              # IM页面
    │   ├── components/Chat/       # 聊天组件
    │   ├── store/modules/         # 状态管理
    │   └── utils/socket/          # WebSocket工具
    └── public/                    # 静态资源
```

## 快速开始

### 环境要求
- JDK 8+
- MySQL 5.7+
- Redis 5.0+
- Node.js 12+

### 启动步骤

1. **数据库初始化**
   ```bash
   # 导入数据库脚本
   mysql -u root -p < ruoyi-im-server/sql/ry_im_20240101.sql
   ```

2. **启动后端服务**
   ```bash
   cd ruoyi-im-server
   mvn spring-boot:run
   ```

3. **启动前端应用**
   ```bash
   cd ruoyi-im-web
   npm install
   npm run dev
   ```

4. **访问应用**
   - 前端地址：http://localhost:3000
   - 后端API：http://localhost:8080

## 开发指南

- 📖 **详细文档**: 查看 `docs/` 目录
- 🔧 **开发规范**: 严格遵循阿里巴巴Java开发规范
- 🧪 **测试要求**: 单元测试覆盖率 > 80%
- 📝 **代码提交**: 使用规范的commit message

## 许可证

本项目基于 MIT 许可证开源，详见 [LICENSE](LICENSE) 文件。