# Design Tokens 更新草案（对齐钉钉风格）

目的：将钉钉风格要点以设计令牌的形式固化，便于全站统一切换和后续扩展。

新增/调整 Tokens 说明
- dt-group-avatar-gap: 1px
- dt-group-avatar-size: 20px 28px 32px (响应式设计，可以通过相关组件变量选择)
- dt-group-grid-template: auto-fit / minmax(…)（用于网格自适应）
- dt-group-avatar-max-visible: 9
- dt-message-status-color: #1677ff
- dt-message-status-read-color: #4CAF50
- dt-message-status-icon-size: 12px
- dt-message-status-gap: 4px
- dt-text-link-color: var(--dt-brand-color)
- dt-text-link-hover-color: #0b63e6
- dt-bubble-status-position: bottom-right
- dt-bubble-compact-padding: 6px

说明与使用
- 这些 Tokens 将被引入到 MessageBubble.vue、DingtalkAvatar.vue、ChatHeader.vue 等核心组件。
- 如需主题切换，可在全局主题中扩展 dark 变量，确保暗色模式下仍然保持对比度与可读性。

验收准则
- 令牌生效后，UI 在不同主题下保持一致性。
- 针对群组头像与消息状态的改动，视觉对齐达到钉钉 8.2 的可感知水平。
