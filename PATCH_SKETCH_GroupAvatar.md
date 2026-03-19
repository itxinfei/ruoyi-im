# 补丁设计草案：核心差异 1 - 群组头像网格

目标：将群组聚合头像网格改造为更可扩展、可控的网格布局，统一走 Design Tokens，支持溢出提示。

变更要点（实现思路，非实际代码）：
- 在 DingtalkAvatar.vue 中引入以下 token：dt-group-avatar-gap、dt-group-avatar-size、dt-group-grid-template、dt-group-avatar-max-visible。
- displayMembers 的计算从 slice(0, 9) 改为 slice(0, maxVisible)，并在末尾增加溢出处理逻辑（如末尾额外单元显示 +N）。
- 网格 CSS 使用 dt-group-grid-template 作为网格模板，避免大量硬编码 grid- N 类。
- 新增一个简单的溢出指示单元（如一个统一风格的圆角区域，显示 +N），该单元颜色与 dt-brand 相关变量对齐。

测试/验收要点：
- 3、9、12 名成员场景下网格应对齐且美观，且超出 maxVisible 时显示正确的 +N 提示。
- 暗色模式下对比度仍然清晰，网格单元背景色遵循 dt-theme。
- 以不同设备宽度查看，网格自适应合理，无布局抖动。
