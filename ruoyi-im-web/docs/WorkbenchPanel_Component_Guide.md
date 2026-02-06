# WorkbenchPanel 组件文档

## 1. 组件概述

**文件路径**: `src/views/WorkbenchPanel.vue`

`WorkbenchPanel` 是系统的**工作台/仪表盘**首页组件。它为用户提供了一个中心化的视图，用于快速查看待办事项、审批请求、日程安排、未读邮件以及公司公告等关键信息，并提供常用功能的快捷入口。

## 2. 视觉结构

组件采用栅格布局，主要包含以下几个区域：

1.  **顶部欢迎区 (`.workbench-header`)**
    - 显示基于时间的问候语（如"早上好"）和当前登录用户昵称。
    - 显示当前日期（月/日/星期）。
    - 提供全局数据刷新按钮。

2.  **统计概览区 (`.stats-overview-section`)**
    - 包含4个核心指标卡片：
        - **待办事项**: 待处理任务数量。
        - **待审批**: 需要审批的申请数量。
        - **未读邮件**: 未读邮件数量。
        - **在线同事**: 当前在线的用户数量。
    - 点击卡片可跳转至相应的功能模块。

3.  **主要内容网格 (`.main-content-grid`)**
    - **左侧栏**:
        - **我的待办**: 显示优先级排序的待办列表，支持快速勾选完成。
        - **待我审批**: 显示待审批项，支持快捷"通过"或"拒绝"操作。
    - **中间栏**:
        - **今日日程**: 时间轴形式展示今日会议或日程。
        - **快捷操作**: 4个常用功能入口（新建待办、新建日程、发起会议、写邮件）。
    - **右侧栏**:
        - **公告通知**: 显示最新的系统公告。
        - **近期会议**: 显示即将开始的会议信息。

## 3. 功能特性详解

### 3.1 数据加载与刷新
- 组件挂载时 (`onMounted`) 会自动加载所有模块数据。
- 顶部刷新按钮调用 `refreshAllData` 方法，并行刷新所有子模块数据。
- 加载过程中显示 `v-loading` 遮罩。

### 3.2 待办事项管理 (Todo)
- **展示**: 列表展示前5条待办，按完成状态和优先级排序。
- **交互**:
    - 点击 Checkbox 可直接切换完成状态 (`toggleTodoComplete`)。
    - 点击"新建待办"快捷方式或列表头部的"+"号可打开新建弹窗。
    - 优先级通过颜色区分（红/橙/蓝）。
- **新建**: 内置 `el-dialog` 弹窗，支持设置标题、优先级和截止时间。

### 3.3 审批流 (Approval)
- **展示**: 显示待审批列表。
- **快捷操作**: 直接在卡片上提供"通过"和"拒绝"按钮，调用 `quickApprove` / `quickReject`。

### 3.4 日程管理 (Schedule)
- **展示**: 时间轴样式展示今日日程。
- **新建**: 内置 `el-dialog` 弹窗，支持设置标题、起止时间和地点。

### 3.5 导航跳转
- 使用 `navigateTo(type)` 方法统一处理页面跳转，基于 `emit('switch-panel', type)` 通知父组件切换视图。

## 4. API 依赖

组件依赖以下 API 模块：

- **`@/api/im/workbench`**: 获取概览统计数据 (`getOverview`, `getStatistics`).
- **`@/api/im/todo`**: 待办列表与操作 (`getTodoList`, `createTodo`, `completeTodo`).
- **`@/api/im/approval`**: 审批数据与操作 (`getPendingApprovals`, `handleApproval`).
- **`@/api/im/schedule`**: 日程管理 (`getEventsByTimeRange`, `createEvent`).
- **`@/api/im/announcement`**: 公告获取 (`getLatestAnnouncements`).
- **`@/api/im/mail`**: 邮件统计 (`getUnreadCount`).
- **`@/api/im/meeting`**: 会议列表 (`getMeetingList`).

## 5. 状态管理 (Vuex)

- **`user/currentUser`**: 获取当前登录用户信息（昵称）。
- **`contact/onlineUsers`**: 获取在线用户列表以计算在线人数。

## 6. 组件交互 (Events)

| 事件名 | 说明 | 参数 |
| :--- | :--- | :--- |
| `switch-panel` | 请求切换主面板视图 | `panelName` (e.g., 'todo', 'mail', 'calendar') |

## 7. 样式设计

- 使用 SCSS 编写样式。
- 采用了 Flexbox 和 Grid 布局实现响应式设计。
- 卡片悬停效果 (`hover`) 增强交互感。
- 使用 `design-tokens.scss` 中的变量保持设计一致性。
