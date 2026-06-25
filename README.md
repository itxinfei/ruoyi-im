# RuoYi-IM

<p align="center">
  <strong>企业级即时通讯与协同办公平台</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License">
  <img src="https://img.shields.io/badge/java-1.8+-orange.svg" alt="Java 1.8+">
  <img src="https://img.shields.io/badge/vue-3.x-brightgreen.svg" alt="Vue 3.x">
  <img src="https://img.shields.io/badge/springboot-2.7.x-blue.svg" alt="Spring Boot 2.7.x">
  <img src="https://img.shields.io/badge/mysql-5.7+-blue.svg" alt="MySQL 5.7+">
  <img src="https://img.shields.io/badge/redis-6.x+-red.svg" alt="Redis 6.x+">
</p>

<p align="center">
  🌐 <a href="#">在线演示</a> · 📖 <a href="docs/00-文档索引总纲.md">完整文档</a> · 🐛 <a href="#">提交 Issue</a> · 💡 <a href="#">功能建议</a>
</p>

---

## ✨ 项目简介

RuoYi-IM 是一款面向企业内网场景的**私有化即时通讯与协同办公平台**，产品形态深度复刻钉钉 Windows 8.2，支持私有化部署，数据完全自主可控。

### 核心特性

| 模块 | 功能 |
|------|------|
| 💬 **即时通讯** | 私聊、群聊、消息撤回/编辑、已读回执、@提及、表情回应、消息转发/收藏 |
| 🏢 **协同办公** | 组织架构、云盘管理、审批流程、待办任务、日历日程、工作日志 |
| 📎 **多媒体消息** | 图片预览、大文件分片上传、语音消息、视频消息、位置分享、名片推送 |
| 📞 **音视频通话** | 基于 WebRTC 的 P2P 语音/视频通话、群组通话 |
| 🤖 **AI 助手** | 智能问答、长对话总结、多模型切换、对话历史漫游 |
| 🔍 **全局搜索** | 跨模块搜索：联系人、群组、消息、文件、待办、日程 |
| 📧 **企业邮箱** | 邮件收发、文件夹管理、未读统计 |
| 🔔 **DING 消息** | 必达消息、已读/未读追踪、确认回执 |

### 界面预览

> 💡 请将截图放在 `docs/screenshots/` 目录下，以下为占位示意

| 聊天界面 | 通讯录 | 工作台 |
|---------|--------|--------|
| ![聊天](docs/screenshots/chat.png) | ![通讯录](docs/screenshots/contacts.png) | ![工作台](docs/screenshots/workbench.png) |

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 | 检查命令 |
|------|------|---------|
| Node.js | 18+ | `node -v` |
| Java | 1.8 | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 5.7+ | `mysql --version` |
| Redis | 6.x+ | `redis-cli ping` |

### 1. 克隆项目

```bash
git clone https://gitee.com/your-username/ruoyi-im.git
cd ruoyi-im
```

### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u root -p im < ruoyi-im-api/sql/im_db.sql
```

### 3. 启动后端

```bash
cd ruoyi-im-api

# 修改数据库配置
# 编辑 src/main/resources/application.yml 中的数据库连接信息

mvn spring-boot:run
```

后端启动后访问 http://localhost:8080

### 4. 启动前端

```bash
cd ruoyi-im-web
npm install
npm run dev
```

前端启动后访问 http://localhost:5173

### 5. 登录测试

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 测试用户 | user1 | 123456 |

---

## 📁 项目结构

```
ruoyi-im/
├── ruoyi-im-api/                    # 后端 (Spring Boot)
│   ├── src/main/java/com/ruoyi/im/
│   │   ├── controller/              # REST 接口
│   │   ├── service/                 # 业务逻辑
│   │   ├── mapper/                  # 数据访问层
│   │   ├── domain/                  # 实体类
│   │   ├── config/                  # 配置类
│   │   ├── websocket/               # WebSocket 处理
│   │   └── util/                    # 工具类
│   └── src/main/resources/
│       ├── mapper/                  # MyBatis XML
│       └── application.yml          # 应用配置
│
├── ruoyi-im-web/                    # 前端 (Vue 3)
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   ├── components/              # 通用组件
│   │   ├── store/                   # 状态管理 (Vuex)
│   │   ├── api/                     # API 接口
│   │   ├── router/                  # 路由配置
│   │   ├── styles/                  # 全局样式 & Design Tokens
│   │   └── utils/                   # 工具函数
│   └── public/                      # 静态资源
│
├── docs/                            # 项目文档
│   ├── START.md                     # 快速上手指南
│   ├── 30-技术架构与约束.md          # 技术架构
│   ├── 33-API接口设计.md             # API 文档
│   └── 35-前端全局样式与主题配置.md   # Design Tokens
│
└── README.md                        # 本文件
```

---

## 🛠️ 技术栈

### 后端

| 技术 | 用途 |
|------|------|
| Spring Boot 2.7 | 应用框架 |
| MyBatis-Plus 3.5 | ORM 框架 |
| MySQL 8.0 | 主数据库 |
| Redis 6.x | 缓存 & 消息推送 |
| WebSocket (JSR-356) | 实时通讯 |
| JWT | 身份认证 |
| Druid | 数据库连接池 |

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 (Composition API) | UI 框架 |
| Vite | 构建工具 |
| Element Plus | 组件库 |
| Vuex 4 | 状态管理 |
| SCSS | 样式预处理器 |
| ECharts | 数据可视化 |

---

## 📖 文档导航

| 分类 | 文档 | 说明 |
|------|------|------|
| 🚀 快速开始 | [START.md](docs/START.md) | 5 分钟跑通环境 |
| 🏗️ 技术架构 | [30-技术架构与约束.md](docs/30-技术架构与约束.md) | 系统架构与技术选型 |
| 📡 API 文档 | [33-API接口设计.md](docs/33-API接口设计.md) | RESTful API 设计 |
| 🔌 WebSocket | [34-WebSocket协议.md](docs/34-WebSocket协议.md) | 实时消息协议 |
| 🎨 Design Tokens | [35-前端全局样式与主题配置.md](docs/35-前端全局样式与主题配置.md) | CSS 变量体系 |
| 📐 UI 规范 | [27-PC版钉钉UI设计蓝图.md](docs/27-PC版钉钉UI设计蓝图.md) | 钉钉 8.2 设计规范 |
| 📊 数据库 | [32-数据库设计.md](docs/32-数据库设计.md) | 表结构设计 |
| 🚢 部署指南 | [50-部署指南.md](docs/50-部署指南.md) | 生产环境部署 |

完整文档索引 → [00-文档索引总纲.md](docs/00-文档索引总纲.md)

---

## 🤝 参与贡献

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork** 本仓库
2. 创建你的特性分支：`git checkout -b feature/amazing-feature`
3. 提交你的修改：`git commit -m 'feat: add amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 提交 **Pull Request**

### Commit 规范

本项目遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>: <description>

类型：feat, fix, refactor, docs, test, chore, perf, ci
```

示例：
```
feat: 添加消息撤回功能
fix: 修复会话列表未读数显示错误
docs: 更新 API 接口文档
```

### 开发规范

- **后端**：遵循 [Java 编码规范](docs/30-技术架构与约束.md)，使用 LambdaQueryWrapper，禁止魔法值
- **前端**：遵循 [Design Token 体系](docs/35-前端全局样式与主题配置.md)，禁止硬编码颜色/尺寸
- **UI**：严格对齐 [钉钉 8.2 设计规范](docs/27-PC版钉钉UI设计蓝图.md)

---

## 📋 版本路线

- [x] v1.0 - 核心即时通讯（私聊、群聊、消息类型）
- [x] v1.1 - 协同办公（通讯录、审批、待办、日历）
- [x] v1.2 - 多媒体与云盘（文件上传、云盘管理）
- [x] v1.3 - AI 助手集成（多模型、对话历史）
- [ ] v1.4 - 音视频通话优化
- [ ] v1.5 - 移动端适配
- [ ] v2.0 - 插件化架构

---

## 📄 开源许可

本项目基于 [MIT License](LICENSE) 开源。

---

<p align="center">
  如果这个项目对你有帮助，请给一个 ⭐ Star 支持一下！
</p>
