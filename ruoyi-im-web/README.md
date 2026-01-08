# RuoYi IM Web 前端项目

基于 Vue 3 + Vite + Element Plus 构建的即时通讯系统前端，采用钉钉6.5.x风格UI设计。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 (Composition API) |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.x | UI组件库 |
| Vuex | 4.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | - | HTTP客户端 |
| SCSS | - | CSS预处理器 |
| WebSocket | - | 实时通信 |

## UI设计风格

本项目采用**钉钉6.5.x风格**设计，包含完整的样式库：

### 颜色系统
```scss
--im-primary-color: #1677ff;    // 钉钉蓝
--im-text-primary: #262626;     // 主要文字
--im-text-secondary: #595959;   // 次要文字
--im-bg-base: #f0f2f5;          // 基础背景
--im-bg-white: #ffffff;         // 白色背景
--im-border-color: #f0f0f0;     // 边框颜色
```

### 样式文件结构

```
ruoyi-im-web/src/styles/
├── dingtalk-index.scss      # 主入口文件
├── dingtalk-theme.scss      # 主题变量和混合宏
├── dingtalk-navigation.scss # 导航侧边栏样式
├── dingtalk-message.scss    # 消息气泡样式
├── dingtalk-components.scss # 通用组件样式
├── dingtalk-chat-input.scss # 聊天输入框样式
├── dingtalk-emoji.scss      # 表情选择器样式
├── dingtalk-contacts.scss   # 通讯录样式
├── dingtalk-notification.scss # 通知/Toast样式
├── dingtalk-modal.scss      # 模态框/抽屉样式
├── dingtalk-utils.scss      # 工具类
└── im-theme.scss            # IM主题变量
```

### 使用钉钉样式

在 `main.js` 中引入：

```javascript
import '@/styles/dingtalk-index.scss'
```

或在组件中按需引入：

```javascript
import '@/styles/dingtalk-message.scss'
```

### 工具类示例

```html
<!-- 布局工具类 -->
<div class="dd-flex dd-items-center dd-justify-between">

<!-- 间距工具类 -->
<div class="dd-p-4 dd-m-2">

<!-- 文字工具类 -->
<span class="dd-text-primary dd-font-medium">

<!-- 背景工具类 -->
<div class="dd-bg-white dd-rounded-lg dd-shadow-md">

<!-- 动画工具类 -->
<div class="dd-animate-fade-in">
```

## 项目结构

```
ruoyi-im-web/
├── public/                    # 静态资源
├── src/
│   ├── api/                   # API接口模块
│   │   ├── im/               # IM业务接口
│   │   │   ├── contact.js    # 联系人API
│   │   │   ├── file.js       # 文件API
│   │   │   ├── group.js      # 群组API
│   │   │   ├── message.js    # 消息API
│   │   │   ├── session.js    # 会话API
│   │   │   └── config.js     # 配置API
│   │   ├── system/           # 系统管理接口
│   │   │   └── user.js       # 用户管理API
│   │   └── login.js          # 登录认证API
│   │
│   ├── assets/               # 静态资源
│   │   ├── icons/           # 图标
│   │   ├── images/          # 图片
│   │   └── styles/          # 全局样式
│   │       ├── variables.scss  # SCSS变量定义
│   │       ├── mixin.scss      # SCSS混入
│   │       └── index.scss      # 样式入口
│   │
│   ├── components/           # 公共组件
│   │   └── Chat/            # 聊天组件库
│   │       ├── ChatInput.vue         # 聊天输入框
│   │       ├── MessageList.vue       # 消息列表
│   │       ├── MessageBubble.vue     # 消息气泡
│   │       ├── SessionList.vue       # 会话列表
│   │       ├── EmojiPicker.vue       # 表情选择器
│   │       ├── ImagePreview.vue      # 图片预览
│   │       ├── MentionSelector.vue   # @提及选择器
│   │       ├── LocationPicker.vue    # 位置选择器
│   │       ├── VoteDialog.vue        # 投票对话框
│   │       ├── CodeSnippetDialog.vue # 代码片段对话框
│   │       ├── VirtualMessageList.vue # 虚拟列表消息
│   │       └── index.js              # 组件导出
│   │
│   ├── layout/              # 布局组件
│   │   └── index.vue        # 主布局 (钉钉式侧栏)
│   │
│   ├── router/              # 路由配置
│   │   ├── index.js         # 路由入口
│   │   └── modules/
│   │       └── im.js        # IM模块路由
│   │
│   ├── store/               # Vuex状态管理
│   │   ├── index.js         # Store入口
│   │   └── modules/
│   │       ├── im.js        # IM业务状态
│   │       └── user.js      # 用户认证状态
│   │
│   ├── utils/               # 工具函数
│   │   ├── request.js       # Axios封装
│   │   ├── auth.js          # Token认证
│   │   ├── common.js        # 通用工具
│   │   ├── message.js       # 消息处理
│   │   ├── persist.js       # 持久化存储
│   │   ├── socket/          # WebSocket模块
│   │   │   ├── nativeSocket.js    # WebSocket封装
│   │   │   └── useImSocket.js     # Socket Composable
│   │   ├── voice/           # 语音功能
│   │   │   ├── voiceRecorder.js   # 录音器封装
│   │   │   └── useVoiceRecorder.js # 录音Composable
│   │   └── virtualList/     # 虚拟列表
│   │       └── useVirtualList.js  # 虚拟列表Composable
│   │
│   ├── views/               # 页面视图
│   │   ├── login/           # 登录页
│   │   │   └── index.vue
│   │   ├── error/           # 错误页
│   │   │   └── 404.vue
│   │   └── im/              # IM功能模块
│   │       ├── chat/        # 聊天
│   │       │   └── ChatContainer.vue
│   │       ├── contacts/    # 联系人
│   │       │   └── index.vue
│   │       ├── group/       # 群组管理
│   │       │   ├── index.vue
│   │       │   ├── form.vue
│   │       │   ├── manage.vue
│   │       │   └── settings.vue
│   │       ├── file/        # 文件管理
│   │       │   └── index.vue
│   │       └── settings/    # 系统设置
│   │           └── index.vue
│   │
│   ├── App.vue              # 根组件
│   └── main.js              # 应用入口
│
├── env.config.js            # 环境变量配置
├── vite.config.js           # Vite构建配置
├── package.json             # 依赖配置
└── README.md                # 项目文档
```

## 路由配置

| 路径 | 组件 | 说明 |
|------|------|------|
| `/login` | login/index.vue | 用户登录 |
| `/im/chat` | chat/ChatContainer.vue | 聊天主页 |
| `/im/contacts` | contacts/index.vue | 联系人管理 |
| `/im/group` | group/index.vue | 群组管理 |
| `/im/file/list` | file/index.vue | 文件管理 |
| `/im/settings` | settings/index.vue | 系统设置 |

## 核心功能

### 聊天功能
- 单聊/群聊消息
- 多媒体消息 (文字、图片、文件、语音)
- 表情选择器
- @提及功能
- 消息搜索
- 位置分享
- 投票功能
- 代码片段

### 联系人管理
- 联系人列表与搜索
- 在线状态显示
- 好友分组管理
- 联系人详情

### 群组管理
- 群组CRUD操作
- 成员管理
- 群组设置
- 群公告

### 文件管理
- 文件上传/下载
- 文件预览
- 批量操作
- 搜索过滤

### 系统设置
- 个人资料
- 主题切换 (浅色/深色)
- 通知设置
- 隐私设置

## 快速开始

### 环境要求
- Node.js >= 16.0
- npm >= 8.0

### 安装依赖
```bash
npm install
```

### 开发运行
```bash
npm run dev
```
访问 http://localhost:3000

### 生产构建
```bash
npm run build
```

### 预览构建
```bash
npm run preview
```

## 环境配置

### 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端开发服务器 | 3000 | Vue前端页面访问地址 |
| IM接口服务 | 8080 | 即时通讯业务接口（聊天、联系人、群组、文件等） |
| 后台管理服务 | 8081 | 系统管理接口（用户、角色、权限等） |
| MySQL数据库 | 3306 | 数据存储 |
| Redis缓存 | 6379 | 缓存和会话存储 |

编辑 `env.config.js`:

```javascript
module.exports = {
  VITE_BASE_API: '/api',           // API基础路径
  VITE_WS_URL: 'ws://localhost:8080/ws'  // WebSocket地址（IM服务）
}
```

### Vite代理配置

前端通过Vite代理将请求转发到不同的后端服务：

```javascript
// vite.config.js
proxy: {
  '/api/im': {
    target: 'http://localhost:8080',  // IM接口服务
    changeOrigin: true,
  },
  '/system': {
    target: 'http://localhost:8081',  // 后台管理服务
    changeOrigin: true,
  },
  '/ws': {
    target: 'ws://localhost:8080',  // WebSocket（IM服务）
    ws: true,
    changeOrigin: true,
  },
}
```

## API模块说明

| 模块 | 文件 | 功能 |
|------|------|------|
| 认证 | `login.js` | 登录、登出、获取用户信息 |
| 用户 | `system/user.js` | 用户管理、资料更新 |
| 消息 | `im/message.js` | 消息发送、历史查询 |
| 会话 | `im/session.js` | 会话列表、会话操作 |
| 联系人 | `im/contact.js` | 好友管理、分组 |
| 群组 | `im/group.js` | 群组CRUD、成员管理 |
| 文件 | `im/file.js` | 文件上传、下载、管理 |

## 状态管理

### user模块
- `token` - 认证Token
- `name` - 用户名
- `avatar` - 头像
- `roles` - 角色列表

### im模块
- `sessions` - 会话列表
- `currentSession` - 当前会话
- `messageList` - 消息列表 (按会话ID索引)
- `onlineStatus` - 在线状态
- `wsConnected` - WebSocket连接状态

## 开发规范

### 组件开发
- 使用 Vue 3 Composition API
- 使用 `<script setup>` 语法糖
- 组件样式使用 `scoped`

### 命名规范
- 组件文件: PascalCase (如 `ChatInput.vue`)
- 工具文件: camelCase (如 `useVoiceRecorder.js`)
- 样式变量: kebab-case (如 `$primary-color`)

### 样式规范
- 全局变量定义在 `assets/styles/variables.scss`
- 已通过Vite配置自动注入到所有组件
- 组件内样式使用SCSS

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT License
