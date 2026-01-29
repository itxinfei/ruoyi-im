# 消息模块重构迁移进度

## Phase 1: 消息气泡组件重构 ✅

### 1. 消息气泡组件重构
- [x] 主组件 MessageBubbleRefactored.vue
- [x] Composables 分离
  - [x] useMessageBubble.js - 交互逻辑
  - [x] useMessageStatus.js - 状态管理
  - [x] useMessageReaction.js - 表情回应
- [x] 消息类型子组件
  - [x] TextBubble.vue - 文本消息
  - [x] ImageBubble.vue - 图片消息
  - [x] FileBubble.vue - 文件消息
  - [x] VoiceBubble.vue - 语音消息
  - [x] VideoBubble.vue - 视频消息
  - [x] LocationBubble.vue - 位置消息
  - [x] SystemBubble.vue - 系统消息
  - [x] RecalledBubble.vue - 撤回消息
- [x] 零件组件
  - [x] MessageReplyRef.vue - 引用块
  - [x] MessageStatus.vue - 状态指示器
  - [x] MessageReactions.vue - 表情聚合
  - [x] CodeBlock.vue - 代码块
  - [x] MessageMarkers.vue - 消息标记
  - [x] LinkCardList.vue - 链接卡片
- [x] 统一样式文件 message-bubble.scss
- [x] 组件索引 index.js
- [x] 使用文档 README.md
- [x] MessageList.vue 适配（更新导入路径）

## Phase 2: 全局 Composables 抽离 🔄

### 2. 创建全局可复用 Composables
- [x] useFileUpload.js - 统一文件上传逻辑
- [x] useTypingIndicator.js - 输入状态管理
- [x] useMessageTransformation.js - 消息数据转换
- [x] useChatPanel.js - ChatPanel 状态管理
- [x] 更新 composables/index.js 导出

### 3. ChatPanel.vue 重构
- [ ] 使用 useChatPanel 替换状态管理
- [ ] 使用 useFileUpload 替换上传逻辑
- [ ] 使用 useTypingIndicator 替换输入状态逻辑
- [ ] 提取 MultiSelectToolbar 子组件
- [ ] 提取 DialogManager 统一管理弹窗

### 4. MessageInput.vue 重构 ✅
- [x] 创建 useInputDraft.js - 草稿管理 Composable
- [x] 创建 useInputResize.js - 高度调整 Composable
- [x] 创建 useInputCommand.js - 快捷命令 Composable
- [x] 创建 useVoicePreview.js - 语音预览 Composable
- [x] 创建 MessageInputRefactored.vue (从 2000 行降至 ~700 行)
- [x] 更新 composables/index.js 导出

### 5. 子组件进一步模块化 ✅
- [x] 创建 MultiSelectToolbar.vue - 多选工具栏
- [x] 创建 InputToolbar.vue - 输入工具栏
- [x] 创建 ResizeHandle.vue - 高度调整手柄
- [x] 创建 ReplyPreview.vue - 引用回复预览
- [x] 创建 EditPreview.vue - 编辑消息预览
- [x] 创建 VoicePreviewPanel.vue - 语音预览面板
- [x] 更新 MessageInputRefactored.vue 使用新子组件（降至 ~500 行）
- [ ] 更新 ChatPanel.vue 使用重构后的 MessageInput
- [ ] 测试输入功能

### 重构效果
| 指标 | 原始 | 重构后 | 改善 |
|------|------|--------|------|
| 主组件行数 | ~2000 | ~700 | ↓ 65% |
| 状态变量 | 30+ ref | 集中管理 | 更清晰 |
| 逻辑复用 | 0 | 4 Composables | 可复用 |

## 待开始 📋

### 5. ChatPanel.vue 重构
- [ ] ContactItem.vue 优化（已完成，验证中）
- [ ] MessageItem.vue 简化
- [ ] ChatHeader.vue 组件化
- [ ] 空状态组件统一

### 6. 全局优化
- [ ] 暗色模式全面适配
- [ ] 响应式断点统一
- [ ] 动画性能优化
- [ ] 无障碍支持（ARIA）

### 7. 测试验证
- [ ] 全面测试各消息类型
- [ ] 验证已读回执功能
- [ ] 验证表情回应功能
- [ ] 性能对比测试
- [ ] 单元测试更新

## 迁移步骤

1. **备份原有组件**
   ```bash
   cp MessageBubble.vue MessageBubble.vue.backup
   ```

2. **更新引用路径**
   ```vue
   <!-- MessageList.vue -->
   <script setup>
   - import MessageBubble from './MessageBubble.vue'
   + import MessageBubble from './message-bubble/MessageBubbleRefactored.vue'
   </script>
   ```

3. **测试验证**
   - 发送各类型消息
   - 测试右键菜单
   - 测试表情回应
   - 测试已读回执
   - 测试撤回功能

4. **清理旧文件**（验证通过后）
   ```bash
   rm MessageBubble.vue.backup
   ```

## API 兼容性

重构后的组件 API 保持完全兼容：

```vue
<!-- 无需修改调用方式 -->
<MessageBubble
  :message="message"
  :session-type="sessionType"
  @command="handleCommand"
  @preview="handlePreview"
  @download="handleDownload"
  @retry="handleRetry"
  @add-reaction="handleAddReaction"
  @re-edit="handleReEdit"
  @at="handleAt"
  @scroll-to="handleScrollTo"
/>
```
