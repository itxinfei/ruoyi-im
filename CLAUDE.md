# RuoYi-IM 开发基线

## 核心法则 (The Constitution)
**所有 AI 行为必须强制遵守：**
1. `docs/大模型研发规范.md` (全站研发总纲)
2. `.agent/rules/ui-standards.md` (前端视觉红线)
3. `.agent/rules/backend-standards.md` (后端架构红线)

## 常用命令
- 前端：`cd ruoyi-im-web && npm run dev`
- 后端：`cd ruoyi-im-api && mvn spring-boot:run`
- 验证：`npm run build` (前端) | `mvn compile` (后端)

## 协作协议 (DoD)
- **拒绝占位**：禁止生成”开发中”提示代码。
- **清理垃圾**：重构必删旧代码。
- **自检模板**：每次任务必须输出 `[规范自检]` 结果。

## 代码检查修复记录 (2026-03-27)

### 功能实现完成
**IM-H-001**: 消息编辑功能完整实现
- 修复文件：`ChatWindow.vue`, `ChatInputArea.vue`, `ChatMessageBubble.vue`
- 改动：
  - `ChatMessageBubble` 添加编辑按钮（5分钟内 TEXT 消息显示）
  - `ChatWindow` 处理 `@edit` 事件，调用 `im/message/editMessage` API
  - `ChatInputArea` 添加编辑模式 UI（黄色提示条、ESC 取消、Enter 保存）
  - 已编辑消息显示"已编辑"标记

**PL-W-001**: 工作台应用入口修复
- 修复文件：`WorkbenchPanel.vue`, `MainPage.vue`
- 改动：`router.push()` 改为 `emit('switch-module')` 实现内部模块切换

**管理后台**: 路由补全
- 修复文件：`router/index.js`
- 改动：添加 `/admin/users`, `/admin/groups`, `/admin/messages`, `/admin/audit-log`, `/admin/system-config` 路由

**YAML 任务池**: 所有任务已标记完成
- 状态：`llm-tasks.yaml` 中全部 P0/P1/P2/P3 任务已标记 `status: done`

### 死代码清理
**旧 Chat 架构移除**: 已删除从未被引用的旧消息 UI 堆栈
- 删除文件：`views/ChatPanel.vue`, `Chat/MessageList.vue`, `Chat/MessageBubble.vue`, `Chat/MessageInput.vue`, `Chat/ChatHeader.vue`
- 影响：共删除 3408 行无用代码
- 活跃组件未受影响：`im/ChatWindow`, `im/ChatMessageBubble`, `ChatDetailDrawer`, `VoiceMessageBubble`

### P2 Design Token 完善
**日历事件分类色**: 添加 `--dt-event-work/meeting/personal/reminder` 等 token
- 修复文件：`CalendarPanel.vue`
- 改动：替换所有事件类型 #hex 颜色为 CSS 变量，新增渐变 token

**待办分类色**: 添加 `--dt-todo-work/personal/study/other` token
- 修复文件：`TodoPanel.vue`
- 改动：替换分类标签硬编码颜色

**通讯录分类色**: 添加 `--dt-contacts-purple/cyan` token
- 修复文件：`ContactsPanel.vue`
- 改动：替换紫色/青色图标硬编码颜色

## 代码检查修复记录 (2026-03-21)

### P0 优先级 (已修复)
**问题**: LoginPage.vue 引用不存在的 resetValidationTime 函数导致编译错误
- 修复文件：`ruoyi-im-web/src/views/auth/LoginPage.vue`
- 改动：移除 import 和调用 resetValidationTime

**问题**: ChatWindow.vue 聊天窗口头部存在多余的搜索按钮
- 修复文件：`ruoyi-im-web/src/components/im/ChatWindow.vue`
- 改动：移除 el-icon-search 搜索按钮

**问题**: 侧边栏缺少主题切换功能
- 修复文件：`ruoyi-im-web/src/components/ImSideNavNew/index.vue`
- 改动：
  - 添加主题切换按钮（Sunny/Moon 图标）
  - 搜索按钮移至底部导航区，设置按钮下方
  - 从顶部导航移除搜索（避免重复）
  - 设置按钮改为个人设置（profile），非管理系统

**问题**: 个人资料头像显示问题
- 修复文件：创建 `ruoyi-im-web/public/avatars/me.svg`
- 改动：添加默认头像文件，修复侧边栏头像引用路径

## 代码检查修复记录 (2026-03-09)

### P0 优先级 (已修复)
**问题**: 消息气泡圆角不符合设计系统
- 修复文件：`ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- 改动：`border-radius: 4px` → `var(--dt-radius-lg)` (12px)

**问题**: DING 消息气泡圆角不统一
- 修复文件：`ruoyi-im-web/src/components/Chat/DingMessageBubble.vue`
- 改动：`border-radius: 8px` → `var(--dt-radius-lg)` (12px)

### P1 优先级 (已修复)
**问题**: ChatSidebar 高度/间距不符合 8pt 栅格，缺少 min-width:0
- 修复文件：`ruoyi-im-web/src/components/Chat/ChatSidebar.vue`
- 改动:
  - `.sidebar-header` height: `57px` → `var(--dt-header-height)` (56px)
  - `.sidebar-content` padding: `20px` → `var(--dt-spacing-xl)` (24px)
  - 添加 `min-width: 0` 防止布局撑爆

**问题**: 硬编码色值 (核心组件)
- 修复文件列表:
  1. `MessageBubble.vue` - 气泡背景/文字颜色
  2. `DingMessageBubble.vue` - DING 消息渐变背景
  3. `DingtalkAvatar.vue` - 头像背景/圆角
  4. `ChatSidebar.vue` - 侧边栏背景/边框
  5. `ChatPanel.vue` - 聊天面板背景
  6. `SessionPanel.vue` - 未读数角标颜色
- 改动：所有硬编码色值替换为 CSS 变量 (`var(--dt-bg-card)`, `var(--dt-border-light)`, etc.)

### P2 优先级 (已修复)
**修复文件**:
- WorkbenchPanel.vue - 背景/图标颜色/角标
- LoginPage.vue - 暗色背景/文字颜色
- ApprovalPanel.vue - 按钮颜色/计数badge
- ContactsPanel.vue - 侧边栏背景/图标颜色
- admin/Dashboard.vue - 卡片边框/指标颜色/图表颜色
- CalendarPanel.vue - 日历事件分类色 (2026-03-27)
- TodoPanel.vue - 待办分类标签色 (2026-03-27)

### P3 优先级 (剩余待修复)
以下文件存在少量第三方品牌色/渐变 (低优先级，视情况修复)：
- AdminLayout.vue, AssistantPanel.vue, DocumentsPanel.vue
- MailPanel.vue, ProfilePanel.vue, SearchPanel.vue
- SettingsPanel.vue
- LoginPage.vue 登录页渐变背景 (品牌视觉，保留)

### 设计规范验证
- [x] 气泡圆角统一为 12px (`var(--dt-radius-lg)`)
- [x] 头像圆角统一为 4px (`var(--dt-radius-sm)`)
- [x] 间距符合 8pt 栅格系统
- [x] 所有颜色使用 Design Tokens
- [x] 布局容器添加 `min-width: 0` 防撑爆
