# 前端UI优化计划 — 对标钉钉/野火IM

## 背景

上一轮已完成基础样式修正（品牌色 `#3296FA`、去渐变、去装饰动画、统一气泡圆角6px、去左边框激活指示器）。本轮聚焦**功能性UI优化**，目标是从"形似"升级到"神似"——在交互体验、信息层级、空状态、加载态、响应式适配等方面对齐钉钉/野火IM水准。

### 当前优势（无需改动）
- 三栏布局 60px+280px+flex ✅
- 会话项72px高度、48px头像、6px圆角 ✅
- 设计Token系统完整（2300+行）✅
- 深色模式Token就绪 ✅
- 消息合并/时间分割线/虚拟滚动 ✅
- 富媒体消息（图片/文件/视频/语音/位置）✅

### 主要优化方向
1. 消息气泡分组圆角（连续消息视觉合并）
2. 会话列表精细化（分割线、置顶区分、输入指示器升级）
3. 空状态 & 加载态打磨
4. 输入框体验增强
5. 联系人面板优化
6. 残留硬编码色值清理（`#0089FF` → `#3296FA`）

---

## Phase 1: 消息气泡分组圆角

**目标**: 连续同一发送者的消息，在视觉上形成"气泡组"（钉钉/微信/Telegram风格）。

### 1.1 数据层 — MessageList.vue

**文件**: `src/components/Chat/MessageList.vue`

在渲染消息列表时，为每条消息计算分组位置 prop：

```
groupPosition: 'single' | 'first' | 'middle' | 'last'
```

判断逻辑：
- 同一发送者 + 时间间隔 < 2分钟 = 同组
- 如果是时间分割线后的第一条 = 新组开始
- 传递给 `MessageItemRefactored` → `MessageBubbleRefactored`

### 1.2 样式层 — message-bubble.scss

**文件**: `src/components/Chat/message-bubble/styles/message-bubble.scss`

```scss
// 左侧（接收）气泡分组圆角
.bubble-left {
  &.group-single  { border-radius: 6px; }
  &.group-first   { border-radius: 6px 6px 6px 2px; }
  &.group-middle  { border-radius: 2px 6px 6px 2px; }
  &.group-last    { border-radius: 2px 6px 6px 6px; }
}

// 右侧（发送）气泡分组圆角
.bubble-right {
  &.group-single  { border-radius: 6px; }
  &.group-first   { border-radius: 6px 6px 2px 6px; }
  &.group-middle  { border-radius: 6px 2px 2px 6px; }
  &.group-last    { border-radius: 6px 2px 6px 6px; }
}

// 分组内消息间距缩小
.message-item.is-merged {
  margin-top: 2px; // 从默认 8px 缩小
}
```

### 1.3 头像显示逻辑

- `group-first` 和 `group-single`: 显示头像
- `group-middle` 和 `group-last`: 隐藏头像，保留占位宽度

**涉及文件**:
- `src/components/Chat/MessageList.vue` — 计算 groupPosition
- `src/components/Chat/MessageItemRefactored.vue` — 传递 groupPosition
- `src/components/Chat/MessageBubbleRefactored.vue` — 应用 class
- `src/components/Chat/message-bubble/styles/message-bubble.scss` — 圆角样式

---

## Phase 2: 会话列表精细化

### 2.1 添加会话分割线

**文件**: `src/views/SessionPanel.vue`

在会话项之间添加细分割线（仅在非置顶区域），增强视觉分层：

```scss
.session-item + .session-item {
  border-top: 1px solid var(--dt-border-lighter); // rgba(0,0,0,0.04)
}

// 置顶区域底部加粗分割线
.session-group-pinned + .session-group-default {
  border-top: 1px solid var(--dt-border-light);
}
```

### 2.2 置顶会话视觉区分

当前置顶背景 `rgba(0,0,0,0.02)` 太淡。增强：

```scss
.session-item.is-pinned {
  background: var(--dt-bg-pinned); // rgba(0,0,0,0.03)

  // 置顶图标 - 右上角小钉子
  &::after {
    content: '';
    position: absolute;
    top: 8px;
    right: 8px;
    width: 6px;
    height: 6px;
    background: var(--dt-text-quaternary);
    clip-path: polygon(50% 0%, 100% 100%, 0% 100%); // 三角形
  }
}
```

### 2.3 输入指示器升级为三点动画

当前是纯文字"正在输入..."，改为钉钉风格三点跳动动画：

**文件**: `src/views/SessionPanel.vue` — 模板 + 样式

模板：
```html
<span v-if="isTyping" class="typing-dots">
  <span class="dot" /><span class="dot" /><span class="dot" />
</span>
```

样式：
```scss
.typing-dots {
  display: inline-flex;
  gap: 3px;
  align-items: center;

  .dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: var(--dt-brand-color);
    animation: typingBounce 1.4s infinite;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}
```

### 2.4 未读数样式优化

- 数字 `1-9`：圆形 18x18px
- 数字 `10-99`：药丸形 padding: 0 5px
- `99+`：固定宽度药丸形
- 免打扰会话：灰色小圆点代替红色数字

**涉及文件**:
- `src/views/SessionPanel.vue` — 分割线、置顶样式、输入指示器、未读数

---

## Phase 3: 空状态 & 加载态

### 3.1 空状态组件增强

**文件**: `src/components/Common/EmptyState.vue`

当前空状态只有图标+文字，缺少引导操作。增加：

| 场景 | 图标 | 标题 | 描述 | 操作按钮 |
|------|------|------|------|---------|
| 无会话 | chat-bubble | 暂无消息 | 开始你的第一个对话 | 发起聊天 |
| 无联系人 | contacts | 暂无联系人 | 搜索并添加新朋友 | 添加联系人 |
| 无搜索结果 | search | 没有找到结果 | 尝试其他关键词 | — |
| 聊天未选择 | select | 选择一个会话 | 从左侧选择或发起新聊天 | — |

### 3.2 骨架屏优化

**文件**: `src/components/Common/SkeletonLoader.vue`

当前骨架屏有基本shimmer效果。优化方向：
- 添加会话列表专用骨架布局（圆角矩形头像 + 两行文本）
- 添加消息列表专用骨架（左右交替气泡形状）
- shimmer 方向从左到右，使用 `var(--dt-bg-tertiary)` 和 `var(--dt-bg-hover)` 颜色

### 3.3 消息加载状态

在MessageList拉取历史消息时，顶部显示加载指示器（三点跳动，而非旋转spinner）。

**涉及文件**:
- `src/components/Common/EmptyState.vue` — 增加 action slot/prop
- `src/components/Common/SkeletonLoader.vue` — 新增会话/消息骨架变体
- `src/components/Chat/MessageList.vue` — 顶部加载指示器

---

## Phase 4: 输入框体验增强

### 4.1 @提及自动补全面板

**文件**: `src/components/Chat/MessageInputRefactored.vue`

当用户输入 `@` 时，弹出成员列表选择面板（群聊场景）：

- 位置：输入框上方浮层
- 内容：群成员列表，支持搜索过滤
- 样式：`max-height: 240px`，圆角8px，阴影 `var(--dt-shadow-lg)`
- 键盘导航：↑↓选择，Enter确认，Esc关闭

### 4.2 回复消息预览条

当用户回复某条消息时，输入框上方显示被回复消息的预览条：

```
┌──────────────────────────────────────┐
│ ↩ 回复 张三                    [×]   │
│ 这是被回复的消息内容...               │
├──────────────────────────────────────┤
│ 输入消息...                          │
└──────────────────────────────────────┘
```

样式：
- 背景：`var(--dt-bg-tertiary)`
- 左边框：3px solid `var(--dt-brand-color)`
- 高度：40px，单行截断
- 关闭按钮右上角

### 4.3 文件拖拽上传预览

当用户拖拽文件到聊天区域时，显示半透明覆盖层：

```scss
.drag-overlay {
  position: absolute;
  inset: 0;
  background: rgba(50, 150, 250, 0.08);
  border: 2px dashed var(--dt-brand-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
```

**涉及文件**:
- `src/components/Chat/MessageInputRefactored.vue` — @提及面板、回复预览
- `src/views/ChatPanel.vue` — 拖拽覆盖层

---

## Phase 5: 联系人面板优化

### 5.1 分组折叠增强

**文件**: `src/components/Contacts/ContactList.vue`

- 分组标题右侧显示成员计数
- 折叠/展开动画：`max-height` transition 0.2s
- 折叠时显示 `▸`，展开时显示 `▾`

### 5.2 联系人搜索增强

- 搜索框常驻顶部（sticky）
- 搜索结果高亮匹配文字
- 无结果时显示空状态

### 5.3 好友在线状态

在联系人头像右下角显示在线状态点：
- 在线：绿色 `#00C853`
- 离线：不显示（或灰色 `#A0A8B8`）

**涉及文件**:
- `src/components/Contacts/ContactList.vue` — 分组折叠、搜索
- `src/views/ContactsPanel.vue` — 搜索框 sticky

---

## Phase 6: 硬编码色值清理

### 6.1 批量替换 `#0089FF` → `#3296FA`

以下文件中存在旧品牌色硬编码，需统一替换为 `#3296FA`（或改用 `var(--dt-brand-color)`）：

| 文件 | 出现次数 | 类型 |
|------|---------|------|
| `views/WorkbenchPanel.vue` | 5处 | 色值+渐变 |
| `views/admin/DataBackup.vue` | 1处 | 渐变 |
| `views/admin/DepartmentManagement.vue` | 2处 | 模板+JS |
| `views/admin/OperationLog.vue` | 1处 | 渐变 |
| `views/admin/SystemSettings.vue` | 1处 | 内联样式 |
| `views/admin/RoleManagement.vue` | 1处 | JS常量 |
| `views/CalendarPanel.vue` | 3处 | JS常量 |
| `views/ApprovalPanel.vue` | 1处 | JS数组 |
| `views/MailPanel.vue` | 1处 | JS数组 |
| `views/GroupFilePanel.vue` | 2处 | CSS |
| `views/auth/LoginPage.vue` | 1处 | CSS border |
| `components/Chat/BotMessageBubble.vue` | 7处 | CSS fallback |
| `components/Chat/ChatSidebar.vue` | 2处 | 渐变+色值 |
| `components/Chat/CombineMessagePreview.vue` | 2处 | CSS fallback |
| `components/FileUpload/index.vue` | 1处 | CSS (小写) |
| `components/Common/ThemeCustomizer.vue` | 2处 | JS常量 |
| `components/MailDetailDialog/index.vue` | 2处 | JS常量 |
| `views/ContactsPanel.vue` | 1处 | rgba(0,137,255) |

### 6.2 替换规则

- CSS中的 `#0089FF` / `#0089ff` → `var(--dt-brand-color)` 或 `#3296FA`
- JS中的 `'#0089FF'` → `'#3296FA'`
- `rgba(0, 137, 255, x)` → `rgba(50, 150, 250, x)`
- 渐变 `linear-gradient(135deg, #0089FF ..., #00xxxx ...)` → 纯色 `#3296FA`

---

## 验证方式

### 每个Phase验证：

1. **Phase 1**: 发送3条连续消息，确认气泡形成视觉组（首条圆角大，中间圆角小，末条圆角大）
2. **Phase 2**: 检查会话列表有细分割线、置顶区域可区分、输入指示器为三点动画
3. **Phase 3**: 清空会话列表确认空状态有引导按钮、首次加载显示骨架屏
4. **Phase 4**: 在群聊输入@确认弹出成员面板、回复消息确认有预览条
5. **Phase 5**: 联系人分组可折叠展开、搜索有高亮
6. **Phase 6**: 全局搜索 `#0089FF` 和 `rgba(0, 137, 255` 确认为0结果

### 最终视觉验证：
- 连续消息气泡形成"组"，间距紧凑（2px），圆角渐变
- 会话列表层次分明：置顶区 vs 普通区，有细分割线
- 所有空状态有引导文案和操作按钮
- 输入框支持@提及和回复预览
- 全局品牌色统一为 `#3296FA`，无旧色残留

---

## 影响文件汇总

| 阶段 | 文件 | 改动类型 |
|------|------|----------|
| P1 | `src/components/Chat/MessageList.vue` | 计算 groupPosition |
| P1 | `src/components/Chat/MessageItemRefactored.vue` | 传递 groupPosition |
| P1 | `src/components/Chat/MessageBubbleRefactored.vue` | 应用分组class |
| P1 | `src/components/Chat/message-bubble/styles/message-bubble.scss` | 分组圆角样式 |
| P2 | `src/views/SessionPanel.vue` | 分割线+置顶+输入指示器+未读数 |
| P3 | `src/components/Common/EmptyState.vue` | 增加操作按钮 |
| P3 | `src/components/Common/SkeletonLoader.vue` | 新增骨架变体 |
| P3 | `src/components/Chat/MessageList.vue` | 顶部加载指示器 |
| P4 | `src/components/Chat/MessageInputRefactored.vue` | @提及+回复预览 |
| P4 | `src/views/ChatPanel.vue` | 拖拽覆盖层 |
| P5 | `src/components/Contacts/ContactList.vue` | 折叠+搜索+状态 |
| P5 | `src/views/ContactsPanel.vue` | 搜索框sticky |
| P6 | 18个文件 | 色值替换 `#0089FF` → `#3296FA` |
