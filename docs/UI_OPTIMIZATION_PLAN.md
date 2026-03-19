# RuoYi-IM 钉钉风格UI优化方案

## 📋 文档信息

| 项目 | 内容 |
|------|------|
| 版本 | v1.0 |
| 日期 | 2026-03-18 |
| 目标 | 全面优化UI界面，对标钉钉 8.2 企业级通讯体验 |

---

## 一、设计规范文档

### 1.1 色彩系统 (Color System)

#### 主色调 (Brand Colors)
```scss
--dt-brand-color: #1677ff;        // 钉钉蓝 - 主品牌色
--dt-brand-hover: #4096ff;        // 悬停态
--dt-brand-active: #0958d9;       // 点击态
--dt-brand-light: #91caff;        // 浅色辅助
--dt-brand-lighter: #e6f4ff;      // 极浅色背景
--dt-brand-bg: #f0f7ff;           // 背景蓝
```

#### 状态色 (Status Colors)
```scss
--dt-success-color: #52c41a;      // 成功绿
--dt-warning-color: #faad14;      // 警告黄
--dt-error-color: #f54a45;       // 错误红
--dt-info-color: #1677ff;         // 信息蓝
--dt-unread-color: #f54a45;       // 未读红
```

#### 背景色 (Background Colors)
```scss
--dt-bg-body: #f4f6f8;           // 页面背景（呼吸感灰）
--dt-bg-card: #ffffff;            // 卡片背景
--dt-bg-chat: #f4f6f8;            // 聊天区域背景
--dt-bg-session-list: #ffffff;    // 会话列表背景
--dt-bg-session-hover: #f5f6f7;   // 悬停背景
--dt-bg-session-active: #e8f4ff;  // 激活态背景
--dt-bg-input: #f9fafb;           // 输入框背景
--dt-bg-mask: rgba(0, 0, 0, 0.45); // 遮罩层
```

#### 文字色 (Text Colors)
```scss
--dt-text-primary: #1f2329;       // 主文字（标题）
--dt-text-secondary: #646a73;     // 次要文字（正文）
--dt-text-tertiary: #8f959e;      // 辅助文字（说明）
--dt-text-quaternary: #bbbfc4;    // 禁用/占位文字
--dt-text-link: #1677ff;          // 链接色
--dt-text-white: #ffffff;         // 反白文字
```

#### 边框色 (Border Colors)
```scss
--dt-border-light: rgba(31, 35, 41, 0.08);   // 极细边框
--dt-border-color: rgba(31, 35, 41, 0.15);   // 标准边框
--dt-border-dark: rgba(31, 35, 41, 0.25);    // 强调边框
--dt-border-lighter: rgba(31, 35, 41, 0.04); // 微弱边框
```

### 1.2 阴影系统 (Shadow System)

```scss
--dt-shadow-1: 0 1px 2px rgba(0, 0, 0, 0.04);    // 微弱阴影（卡片静止）
--dt-shadow-2: 0 2px 8px rgba(0, 0, 0, 0.08);    // 轻微阴影（按钮悬停）
--dt-shadow-3: 0 4px 16px rgba(0, 0, 0, 0.12);   // 标准阴影（卡片悬停）
--dt-shadow-4: 0 8px 32px rgba(0, 0, 0, 0.16);   // 强调阴影（模态框）
--dt-shadow-float: 0 6px 24px rgba(0, 0, 0, 0.15); // 浮动阴影（悬浮按钮）
--dt-shadow-modal: 0 12px 48px rgba(0, 0, 0, 0.2); // 模态框阴影
```

### 1.3 圆角系统 (Radius System)

```scss
--dt-radius-xs: 2px;   // 极小组件（标签、徽章）
--dt-radius-sm: 4px;   // 小型组件（头像、按钮）
--dt-radius-md: 6px;   // 中型组件（输入框、小卡片）
--dt-radius-lg: 8px;   // 标准组件（卡片、气泡）
--dt-radius-xl: 12px;  // 大组件（对话框、面板）
--dt-radius-2xl: 16px; // 超大组件（欢迎页）
--dt-radius-full: 9999px; // 完全圆形
--dt-bubble-radius: 8px;  // 消息气泡专用
```

### 1.4 间距系统 (8px Grid System)

```scss
--dt-spacing-xs: 4px;   // 极紧凑
--dt-spacing-sm: 8px;   // 紧凑
--dt-spacing-md: 12px;  // 标准
--dt-spacing-lg: 16px;  // 宽松
--dt-spacing-xl: 24px;  // 大间距
--dt-spacing-2xl: 32px; // 超大间距
```

### 1.5 布局尺寸 (Layout Dimensions)

```scss
--dt-nav-sidebar-width: 64px;       // 左侧导航宽度
--dt-session-panel-width: 240px;    // 会话列表宽度
--dt-contact-panel-width: 240px;    // 联系人面板宽度
--dt-chat-min-width: 400px;         // 聊天区域最小宽度
--dt-header-height: 56px;           // 头部高度
--dt-chat-header-height: 60px;      // 聊天头部高度
--dt-chat-input-height: 160px;      // 输入框高度
```

### 1.6 字体系统 (Typography)

```scss
// 字号
--dt-font-size-xs: 11px;    // 辅助信息、时间戳
--dt-font-size-sm: 12px;    // 次要文字、标签
--dt-font-size-base: 14px;  // 正文标准
--dt-font-size-md: 15px;    // 稍大正文
--dt-font-size-lg: 16px;    // 小标题
--dt-font-size-xl: 18px;    // 中标题
--dt-font-size-2xl: 24px;   // 大标题

// 字重
--dt-font-weight-light: 300;
--dt-font-weight-normal: 400;
--dt-font-weight-medium: 500;
--dt-font-weight-semibold: 600;
--dt-font-weight-bold: 700;

// 字体族
--dt-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

### 1.7 过渡动画 (Transitions)

```scss
--dt-transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
--dt-transition-base: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
--dt-transition-slow: 0.4s cubic-bezier(0.4, 0, 0.2, 1);
```

### 1.8 层级系统 (Z-Index)

```scss
--dt-z-deep: -1;      // 背景层
--dt-z-base: 1;       // 基础层
--dt-z-sticky: 100;   // 粘性定位
--dt-z-fixed: 200;    // 固定定位
--dt-z-drawer: 500;   // 抽屉
--dt-z-modal: 1000;   // 模态框
--dt-z-popover: 1500; // 弹出层
--dt-z-max: 9999;     // 最高层
```

### 1.9 暗色模式 (Dark Mode)

```scss
.dark {
  --dt-text-primary: #f1f5f9;
  --dt-text-secondary: #94a3b8;
  --dt-text-tertiary: #64748b;
  --dt-text-quaternary: #475569;
  --dt-bg-body: #0f172a;
  --dt-bg-card: #1e293b;
  --dt-bg-hover: #1e293b;
  --dt-bg-active: #334155;
  --dt-border-input-hover: #3b82f6;
}
```

---

## 二、组件改造清单

### 2.1 优先级矩阵

| 优先级 | 组件数量 | 描述 |
|--------|----------|------|
| P0 - 核心组件 | 8个 | 直接影响主界面视觉，需立即改造 |
| P1 - 高频组件 | 15个 | 每日频繁交互，需优先改造 |
| P2 - 功能组件 | 30个 | 功能页面使用，分批改造 |
| P3 - 辅助组件 | 27个 | 次要功能，按需改造 |

### 2.2 P0 核心组件 (已完成)

| 组件 | 文件路径 | 状态 | 主要改动 |
|------|----------|------|----------|
| MessageBubble | `components/Chat/MessageBubble.vue` | ✅ | 圆角12px、颜色token化 |
| DingMessageBubble | `components/Chat/DingMessageBubble.vue` | ✅ | 圆角统一、渐变优化 |
| ChatSidebar | `components/Chat/ChatSidebar.vue` | ✅ | 高度56px、间距24px、min-width:0 |
| DingtalkAvatar | `components/Common/DingtalkAvatar.vue` | ✅ | 圆角4px、颜色token化 |
| SessionPanel | `views/SessionPanel.vue` | ✅ | 未读角标颜色、背景色 |
| ChatPanel | `views/ChatPanel.vue` | ✅ | 背景色、输入区边框 |
| MainPage | `views/MainPage.vue` | ✅ | 布局结构、过渡动画 |
| LoginPage | `views/auth/LoginPage.vue` | ✅ | 暗色模式、卡片阴影 |

### 2.3 P1 高频组件 (已完成)

| 组件 | 文件路径 | 状态 | 主要改动 |
|------|----------|------|----------|
| WorkbenchPanel | `views/WorkbenchPanel.vue` | ✅ | 背景、图标、角标颜色 |
| ContactsPanel | `views/ContactsPanel.vue` | ✅ | 侧边栏背景、图标颜色 |
| ApprovalPanel | `views/ApprovalPanel.vue` | ✅ | 按钮颜色、计数badge |
| Dashboard | `views/admin/Dashboard.vue` | ✅ | 卡片边框、指标颜色、图表 |

### 2.4 P2 功能组件 (待改造)

| 组件 | 文件路径 | 状态 | 改造要点 |
|------|----------|------|----------|
| AdminLayout | `views/admin/AdminLayout.vue` | 📝 | 导航栏背景、边框色 |
| AssistantPanel | `views/AssistantPanel.vue` | 📝 | 卡片背景、按钮样式 |
| CalendarPanel | `views/CalendarPanel.vue` | 📝 | 日期选中色、事件标签 |
| DocumentsPanel | `views/DocumentsPanel.vue` | 📝 | 文件图标色、列表悬停 |
| MailPanel | `views/MailPanel.vue` | 📝 | 邮件列表背景、未读标记 |
| ProfilePanel | `views/ProfilePanel.vue` | 📝 | 表单输入框、按钮样式 |
| SearchPanel | `views/SearchPanel.vue` | 📝 | 搜索结果高亮、筛选标签 |
| SettingsPanel | `views/SettingsPanel.vue` | 📝 | 设置项布局、开关样式 |
| TodoPanel | `views/TodoPanel.vue` | 📝 | 任务状态色、优先级标签 |
| ChatHeader | `components/Chat/ChatHeader.vue` | 📝 | 高度60px、阴影层级 |
| MessageList | `components/Chat/MessageList.vue` | 📝 | 滚动条样式、加载动画 |
| MessageInput | `components/Chat/MessageInput.vue` | 📝 | 输入框高度160px、工具栏 |
| UserProfileDialog | `components/Contacts/UserProfileDialog.vue` | 📝 | 对话框阴影、卡片圆角 |
| GroupProfileDialog | `components/Contacts/GroupProfileDialog.vue` | 📝 | 成员列表样式、按钮 |
| ForwardDialog | `components/ForwardDialog/index.vue` | 📝 | 搜索框样式、联系人列表 |

### 2.5 P3 辅助组件 (按需改造)

| 类别 | 组件列表 | 改造要点 |
|------|----------|----------|
| 消息类型 | VoiceMessage, FileMessage, ImageMessage | 气泡内边距、图标颜色 |
| 弹窗类 | CreateGroupDialog, ImagePreviewDialog, CallDialog | 阴影、圆角、动画 |
| 表单类 | Input, Select, DatePicker, Switch | 边框色、聚焦态、错误态 |
| 反馈类 | Toast, Loading, Empty, Skeleton | 颜色、动画时长 |
| 导航类 | Breadcrumb, Tabs, Pagination | 选中态、悬停态 |
| 数据类 | Table, Tree, Collapse | 斑马纹、悬停行、展开图标 |

---

## 三、实现步骤

### 阶段一：基础层完善 (Week 1)

#### Day 1-2: 设计令牌审计
- [ ] 检查 `design-tokens.scss` 完整性
- [ ] 补充缺失的token（如：深色模式变量、组件专用变量）
- [ ] 验证token命名一致性

#### Day 3-4: 全局样式强化
- [ ] 优化 `global.scss` 中Element Plus覆盖样式
- [ ] 完善自定义滚动条样式
- [ ] 添加常用工具类（margin、padding、flex等）

#### Day 5-7: 混合宏扩展
- [ ] 扩展 `_mixins.scss` 中的实用混合宏
- [ ] 添加卡片样式混合宏
- [ ] 添加按钮样式混合宏

### 阶段二：核心组件改造 (Week 2)

#### Day 8-10: 聊天模块精修
- [ ] 优化 ChatHeader 阴影和高度
- [ ] 统一 MessageList 滚动体验
- [ ] 重构 MessageInput 工具栏布局

#### Day 11-12: 侧边栏统一
- [ ] 优化 SessionPanel 列表项样式
- [ ] 统一 ContactsPanel 选中态

#### Day 13-14: 弹窗体系优化
- [ ] 统一 UserProfileDialog 样式
- [ ] 优化 GroupProfileDialog 布局

### 阶段三：功能面板改造 (Week 3)

#### Day 15-17: 办公模块
- [ ] WorkbenchPanel 卡片网格优化
- [ ] ApprovalPanel 审批流程样式
- [ ] CalendarPanel 日历视图美化

#### Day 18-19: 协作模块
- [ ] DocumentsPanel 文件管理界面
- [ ] TodoPanel 任务列表优化

#### Day 20-21: 系统模块
- [ ] SettingsPanel 设置页面重构
- [ ] ProfilePanel 个人资料页优化

### 阶段四：细节打磨 (Week 4)

#### Day 22-24: 动画与交互
- [ ] 添加页面过渡动画
- [ ] 优化按钮点击反馈
- [ ] 完善加载状态样式

#### Day 25-26: 暗色模式
- [ ] 验证所有组件暗色模式表现
- [ ] 修复暗色模式下的对比度问题

#### Day 27-28: 响应式适配
- [ ] 测试各断点布局表现
- [ ] 修复移动端适配问题

---

## 四、验收标准

### 4.1 视觉验收标准

#### 色彩一致性 (Color Consistency)
- [ ] 所有组件颜色100%使用Design Tokens
- [ ] 无硬编码色值（除第三方品牌色外）
- [ ] 暗色模式切换无闪烁
- [ ] 对比度符合WCAG AA标准

#### 间距一致性 (Spacing Consistency)
- [ ] 遵循8px栅格系统
- [ ] 组件间距使用 `--dt-spacing-*` 变量
- [ ] 布局容器均有 `min-width: 0`

#### 圆角一致性 (Radius Consistency)
- [ ] 头像：4px (`--dt-radius-sm`)
- [ ] 按钮：4px (`--dt-radius-sm`)
- [ ] 卡片：12px (`--dt-radius-xl`)
- [ ] 气泡：8px (`--dt-radius-lg`)
- [ ] 输入框：6px (`--dt-radius-md`)

#### 阴影一致性 (Shadow Consistency)
- [ ] 静止卡片：`--dt-shadow-1`
- [ ] 悬停卡片：`--dt-shadow-2`
- [ ] 模态框：`--dt-shadow-modal`
- [ ] 浮动元素：`--dt-shadow-float`

### 4.2 交互验收标准

#### 响应速度
- [ ] 按钮点击反馈 < 100ms
- [ ] 页面切换动画 < 300ms
- [ ] 列表滚动帧率 > 50fps

#### 悬停状态
- [ ] 所有可交互元素均有悬停态
- [ ] 悬停过渡使用 `--dt-transition-fast`
- [ ] 悬停态视觉反馈清晰（透明度/背景色变化）

#### 聚焦状态
- [ ] 输入框聚焦边框色：`--dt-brand-color`
- [ ] 按钮聚焦可见（outline或阴影）
- [ ] Tab导航聚焦顺序合理

### 4.3 代码验收标准

#### SCSS规范
- [ ] 使用CSS变量而非硬编码值
- [ ] 嵌套层级不超过3层
- [ ] 混合宏命名语义化

#### Vue组件规范
- [ ] style scoped 使用 lang="scss"
- [ ] 复杂样式抽离到独立scss文件
- [ ] 避免行内样式

#### 可维护性
- [ ] 样式注释完整
- [ ] 无重复代码
- [ ] 组件样式独立不污染

### 4.4 性能验收标准

#### 首屏加载
- [ ] 首屏CSS < 50KB (gzipped)
- [ ] 首屏渲染时间 < 1.5s

#### 运行时性能
- [ ] 滚动无卡顿
- [ ] 动画帧率稳定
- [ ] 内存无泄漏

### 4.5 验收检查清单

```markdown
## 组件验收检查表

### 基础信息
- 组件名称：
- 文件路径：
- 验收日期：
- 验收人：

### 视觉检查
- [ ] 颜色使用Design Tokens
- [ ] 间距符合8px栅格
- [ ] 圆角符合规范
- [ ] 阴影使用正确
- [ ] 字体大小/字重规范

### 交互检查
- [ ] 悬停状态正常
- [ ] 点击反馈正常
- [ ] 聚焦状态可见
- [ ] 过渡动画平滑

### 响应式检查
- [ ] 桌面端显示正常
- [ ] 平板端适配良好
- [ ] 暗色模式正常

### 代码检查
- [ ] 无硬编码值
- [ ] 注释完整
- [ ] 无重复样式
- [ ] scoped样式

### 验收结果
- [ ] 通过
- [ ] 不通过（问题描述：）
```

---

## 五、常见问题与解决方案

### 5.1 布局问题

#### 问题：Flex容器内容撑破布局
**解决方案：**
```scss
.flex-container {
  display: flex;
  min-width: 0; // 关键：允许子元素收缩

  .flex-item {
    min-width: 0; // 关键：防止内容撑开
    overflow: hidden;
  }
}
```

#### 问题：侧边栏高度计算
**解决方案：**
```scss
.sidebar {
  height: 100vh;
  display: flex;
  flex-direction: column;

  .header {
    height: var(--dt-header-height);
    flex-shrink: 0;
  }

  .content {
    flex: 1;
    overflow-y: auto;
  }
}
```

### 5.2 样式问题

#### 问题：Element Plus覆盖不生效
**解决方案：**
```scss
// 使用更具体的选择器
.dingtalk-app {
  .el-button {
    border-radius: var(--dt-radius-sm);
  }
}

// 或使用 :deep() 在scoped样式中
:deep(.el-input__inner) {
  background-color: var(--dt-bg-input);
}
```

#### 问题：暗色模式切换闪烁
**解决方案：**
```vue
<template>
  <div :class="['dingtalk-app', { dark: isDark }]" v-cloak>
    <!-- 内容 -->
  </div>
</template>

<style>
[v-cloak] { display: none; }

// 使用CSS变量而非类名切换
.dark {
  --dt-bg-body: #0f172a;
  --dt-bg-card: #1e293b;
}
</style>
```

### 5.3 性能问题

#### 问题：大量消息滚动卡顿
**解决方案：**
```vue
<script setup>
import { useVirtualList } from '@vueuse/core'

const { list, containerProps, wrapperProps } = useVirtualList(
  messages,
  { itemHeight: 60 }
)
</script>

<template>
  <div v-bind="containerProps" class="message-list">
    <div v-bind="wrapperProps">
      <div v-for="item in list" :key="item.index">
        <MessageBubble :message="item.data" />
      </div>
    </div>
  </div>
</template>
```

---

## 六、参考资源

### 6.1 设计参考
- 钉钉设计规范：[钉钉设计](https://www.dingtalk.com)
- Ant Design 设计系统
- Element Plus 官方文档

### 6.2 技术资源
- CSS变量指南：[MDN CSS Variables](https://developer.mozilla.org/zh-CN/docs/Web/CSS/--*)
- SCSS文档：[Sass Guide](https://sass-lang.com/guide)
- Vue3样式指南：[Vue Style Guide](https://vuejs.org/style-guide/)

### 6.3 工具推荐
- Chrome DevTools - 元素检查、性能分析
- Figma - UI设计稿比对
- Lighthouse - 性能审计

---

## 七、附录

### 7.1 组件改造速查表

```scss
// 快速应用钉钉风格

// 1. 卡片样式
.card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: var(--dt-shadow-1);
  padding: var(--dt-spacing-lg);

  &:hover {
    box-shadow: var(--dt-shadow-2);
  }
}

// 2. 按钮样式
.btn-primary {
  background: var(--dt-brand-color);
  color: var(--dt-text-white);
  border-radius: var(--dt-radius-sm);
  height: 36px;
  padding: 0 var(--dt-spacing-lg);
  font-weight: var(--dt-font-weight-medium);

  &:hover {
    background: var(--dt-brand-hover);
  }

  &:active {
    background: var(--dt-brand-active);
  }
}

// 3. 输入框样式
.input {
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  height: 40px;
  padding: 0 var(--dt-spacing-md);

  &:focus {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-lighter);
  }
}

// 4. 列表项样式
.list-item {
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-bg-session-active);
  }
}
```

### 7.2 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| CSS变量 | --dt-{category}-{name} | --dt-bg-body, --dt-text-primary |
| SCSS变量 | $category-name | $breakpoint-lg, $spacing-md |
| Mixins | {action}{Target} | flexCenter, textEllipsis |
| 类名 | kebab-case | .chat-panel, .message-bubble |

---

**文档结束**

*最后更新：2026-03-18*
