# 补丁设计草案：核心差异 2 - 消息状态指示

目标：在消息气泡内显式呈现发送/送达/已读等状态，提升与钉钉风格的一致性。

实现思路（不写具体代码）：
- 在 MessageBubble.vue 内新增一个小型状态区域，渲染条件基于 message.status，优先级：read > delivered > sent。
- 新增 Tokens：dt-message-status-color、dt-message-status-read-color、dt-message-status-icon-size、dt-message-status-gap、dt-bubble-status-position。
- 状态区域放置在气泡的右下角或底部区域，避免遮挡文本，确保气泡在缩放时仍保持对齐。
- 发送成功后，服务端/发送逻辑应返回 status 值（如 delivered/read），或在本地更新状态以触发 UI 更新。

验收要点：
- 当 message.status 为 sent/delivered/read 时，气泡显示相应状态标识，且不影响文本/图片的布局。
- 在不同主题（light/dark）下状态颜色保持对比度。
- 无障碍：为状态控件添加 aria-label。
