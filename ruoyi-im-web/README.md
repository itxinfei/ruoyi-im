
# RuoYi IM Web 前端项目

基于 Vue 3 + Element Plus 的现代化即时通讯系统前端项目，采用钉钉式布局设计。

## 🚀 项目特性

### 核心功能
- **实时聊天**: 支持文本、图片、文件、语音、视频、位置、投票、代码片段等多种消息类型
- **钉钉式布局**: 左侧导航栏 + 三栏聊天界面（会话列表/消息区/信息侧栏）
- **群组管理**: 创建群组、管理成员、群组设置
- **联系人管理**: 添加好友、分组管理、在线状态显示
- **文件传输**: 支持大文件上传下载、进度显示
- **音视频通话**: 支持语音通话和视频通话
- **消息搜索**: 全文搜索、历史消息查询
- **消息归档**: 自动归档、手动归档、归档规则设置

### 用户体验
- **响应式设计**: 完美适配桌面端和移动端
- **主题切换**: 支持浅色/深色主题切换
- **多语言支持**: 国际化支持
- **快捷键支持**: 丰富的键盘快捷键
- **消息提醒**: 桌面通知、声音提醒
- **离线消息**: 离线消息同步

### 安全特性
- **端到端加密**: 消息加密传输
- **两步验证**: 增强账户安全性
- **登录设备管理**: 管理多设备登录
- **隐私设置**: 灵活的隐私控制选项

## 🛠️ 技术栈

- **前端框架**: Vue 3.3.11
- **UI组件库**: Element Plus 2.4.4
- **状态管理**: Vuex 4.1.0
- **路由管理**: Vue Router 4.2.5
- **构建工具**: Vite 5.0.7
- **开发语言**: TypeScript 5.3.3
- **样式预处理**: Sass 1.69.5
- **代码规范**: ESLint + Prettier
- **测试框架**: Vitest 1.0.4
- **HTTP客户端**: Axios
- **日期处理**: Day.js
- **虚拟滚动**: Vue Virtual Scroller

## 📦 项目结构

```
ruoyi-im-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── im/           # IM相关接口
│   │   ├── login.js      # 登录接口
│   │   └── system/       # 系统接口
│   ├── assets/           # 资源文件
│   │   ├── icons/        # 图标
│   │   ├── images/       # 图片
│   │   └── styles/       # 样式文件
│   ├── components/       # 公共组件
│   │   ├── Chat/         # 聊天相关组件
│   │   │   ├── MessageBubble.vue    # 消息气泡
│   │   │   ├── ChatInput.vue        # 聊天输入
│   │   │   ├── EmojiPicker.vue      # 表情选择器
│   │   │   ├── SessionList.vue      # 会话列表
│   │   │   ├── MessageList.vue      # 消息列表
│   │   │   └── MessagePanel.vue     # 消息面板
│   │   ├── DictTag/      # 字典标签
│   │   ├── Editor/       # 编辑器
│   │   ├── FilePreview/  # 文件预览
│   │   ├── ImageUpload/  # 图片上传
│   │   ├── Pagination/   # 分页组件
│   │   ├── RightToolbar/ # 右侧工具栏
│   │   ├── SvgIcon/      # SVG图标
│   │   ├── ThemeSwitch/  # 主题切换
│   │   └── Upload/       # 上传组件
│   ├── directive/        # 自定义指令
│   ├── layout/           # 布局组件
│   │   ├── index.vue     # 主布局（钉钉式侧栏）
│   │   └── components/   # 布局子组件
│   ├── router/           # 路由配置
│   │   ├── index.js      # 主路由
│   │   └── modules/      # 路由模块
│   │       └── im.js     # IM路由模块
│   ├── store/            # 状态管理
│   │   ├── index.js      # Store入口
│   │   ├── chat.js       # 聊天状态
│   │   ├── im.js         # IM状态
│   │   └── user.js       # 用户状态
│   ├── styles/           # 全局样式
│   ├── utils/            # 工具函数
│   │   ├── socket/       # WebSocket工具
│   │   ├── auth.js       # 认证工具
│   │   ├── request.js    # 请求工具
│   │   └── validate.js   # 验证工具
│   └── views/            # 页面组件
│       ├── im/           # IM功能页面
│       │   ├── chat/     # 聊天页面
│       │   │   ├── index.vue        # 聊天页面入口
│       │   │   └── ChatContainer.vue # 三栏聊天容器
│       │   ├── contacts/ # 联系人页面
│       │   ├── group/    # 群组页面
│       │   ├── collect/  # 收藏页面
│       │   ├── archive/  # 归档页面
│       │   ├── tag/      # 标签页面
│       │   └── file/     # 文件页面
│       ├── login/        # 登录页面
│       ├── settings/     # 设置页面
│       └── error/        # 错误页面
├── .eslintrc.js          # ESLint配置
├── .prettierrc           # Prettier配置
├── index.html            # 入口HTML
├── package.json          # 项目依赖
├── vite.config.js        # Vite配置
└── tsconfig.json         # TypeScript配置
```

## 🎯 主要功能模块

### 1. 聊天模块 (`/src/views/im/chat/`)
- **三栏布局**: 会话列表 + 消息区 + 信息侧栏
- **消息列表**: 虚拟滚动、消息分组、时间显示
- **消息输入**: 富文本编辑、表情选择、文件上传、语音录制
- **消息类型**: 文本、图片、文件、语音、视频、位置、投票、代码片段
- **消息操作**: 回复、转发、删除、撤回、收藏
- **会话管理**: 会话列表、搜索、置顶、免打扰

### 2. 联系人模块 (`/src/views/im/contacts/`)
- **联系人列表**: 分组显示、在线状态、搜索功能
- **好友管理**: 添加好友、删除好友、好友分组
- **个人资料**: 头像、昵称、签名、在线状态
- **快捷操作**: 发送消息、语音通话、视频通话

### 3. 群组模块 (`/src/views/im/group/`)
- **群组创建**: 群组信息设置、成员邀请
- **群组管理**: 成员管理、权限设置、群组公告
- **群组聊天**: 群组消息、@提醒、群组文件
- **群组设置**: 群组头像、群组名称、群组描述

### 4. 设置模块 (`/src/views/settings/`)
- **个人信息**: 头像、昵称、签名、联系方式
- **隐私设置**: 在线状态、已读回执、添加好友
- **通知设置**: 消息提醒、声音设置、免打扰
- **安全设置**: 密码修改、两步验证、设备管理
- **主题设置**: 主题切换、颜色选择、字体设置

### 5. 文件管理 (`/src/views/im/file/`)
- **文件上传**: 拖拽上传、进度显示、断点续传
- **文件预览**: 图片预览、文档预览、视频播放
- **文件管理**: 文件分类、搜索、删除、分享
- **存储管理**: 存储空间、文件统计

## 🔧 开发指南

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 代码检查
```bash
npm run lint
```

### 代码格式化
```bash
npm run format
```

### 类型检查
```bash
npm run type-check
```

### 运行测试
```bash
npm run test
```

## 🌟 核心组件

### 聊天组件
- `MessageBubble.vue`: 消息气泡组件，支持多种消息类型和状态显示
- `ChatInput.vue`: 聊天输入组件，支持富文本、文件上传、语音录制
- `EmojiPicker.vue`: 表情选择器，支持分类、搜索和最近使用
- `MessageList.vue`: 消息列表组件，支持虚拟滚动和消息操作
- `SessionList.vue`: 会话列表组件，支持搜索、置顶和分组
- `ChatContainer.vue`: 三栏聊天容器，整合会话列表、消息区和信息侧栏

### 布局组件
- `Layout/index.vue`: 主布局组件，钉钉式侧栏导航
- `AppMain.vue`: 主内容区域组件
- `Sidebar.vue`: 侧边栏组件

### 文件组件
- `FileUploader.vue`: 文件上传组件，支持拖拽和进度
- `FilePreview.vue`: 文件预览组件，支持多种格式
- `ImageUpload.vue`: 图片上传组件，支持裁剪和压缩

### 通话组件
- `VoiceCall.vue`: 语音通话组件
- `VideoCall.vue`: 视频通话组件
- `CallWindow.vue`: 通话窗口组件

## 📱 响应式设计

项目采用移动优先的响应式设计，完美适配各种设备：

- **桌面端**: 完整三栏布局，钉钉式侧栏导航
- **平板端**: 自适应布局，触摸优化
- **移动端**: 单列布局，手势支持

## 🎨 主题系统

支持多种主题模式：

- **浅色主题**: 默认主题，适合日间使用
- **深色主题**: 护眼主题，适合夜间使用
- **自动主题**: 根据系统设置自动切换
- **自定义主题**: 支持自定义颜色和字体

## 🔒 安全特性

- **HTTPS**: 全站HTTPS加密
- **Token认证**: JWT Token认证机制
- **XSS防护**: 输入输出过滤
- **CSRF防护**: 跨站请求伪造防护
- **内容安全策略**: CSP策略配置

## 📊 性能优化

- **代码分割**: 按路由和组件分割
- **懒加载**: 图片和组件懒加载
- **虚拟滚动**: 长列表虚拟滚动
- **缓存策略**: 静态资源缓存
- **压缩优化**: Gzip压缩

## 🧪 测试覆盖

- **单元测试**: 组件和工具函数测试
- **集成测试**: API接口测试
- **E2E测试**: 端到端测试

## 📈 监控分析

- **错误监控**: 错误收集和分析
- **性能监控**: 页面性能监控
- **用户行为**: 用户行为分析

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

## 📞 联系我们

- 项目主页: [GitHub Repository](https://github.com/your-repo/ruoyi-im-web)
- 问题反馈: [Issues](https://github.com/your-repo/ruoyi-im-web/issues)
- 邮箱: support@ruoyi-im.com

---

**RuoYi IM Web** - 让沟通更简单，让连接更紧密！ 🚀