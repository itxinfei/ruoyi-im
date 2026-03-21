# 01 消息模块 Vue 组件化与性能优化规范

> **定位**：规范即时通信（IM）中消息模块 Vue 3 前端代码的组件化架构。本规范旨在确保大规模消息渲染的性能，以及核心聊天窗口代码的可维护性与高内聚。

---

## 1. 消息模块组件拆分架构与分发机制

为了实现解耦与职责分明，消息页面不得将所有逻辑堆砌在长篇的 `Vue` 单文件组件中。必须划分为以下子组件体系：

### 1.1 `MessageLayout.vue`（布局骨架层）
核心功能：承载左中右三段式容器划分。
- `<ChatSessionList />`（左侧栏）
- `<ChatWindow />`（中部容器区域，通过 `v-if/v-show` 决定当前是否选中聊天记录）
- `<ChatDetailDrawer />`（右侧弹出区抽屉，例如：群设置/详情）

### 1.2 `ChatWindow.vue`（中部会话区域）
负责协调数据的流转（订阅消息与下发给气泡）。
- **`ChatHeader.vue`**：显示当前对方/群组名称、状态及通用操作项。
- **`ChatMessageList.vue`**：承接所有消息并渲染气泡集合，处理历史记录分页拉取事件。
- **`ChatInputBox.vue`**：富文本输入区域、工具栏（表情、图片、文件、待办）以及发送事件派发。

### 1.3 `ChatMessageBubble.vue`（消息体呈现）
- 依据 `msg.type`（文字、图片、语音、文件、系统通知）动态渲染组件。
- 处理“状态透出”：已送达/发送中（Loading 图标）、已读/未读（水滴标识）、失败（重试按钮）。
- 包括交互层：鼠标长按 / 右键展示操作条（引用、撤回、收藏、转发）。

---

## 2. 消息长列表的性能方案与兼容性

### 2.1 假性滚动与动态高度 (虚拟长列表)
当某个会话的历史消息积累超过数万条时，原生的 `v-for` 会导致 DOM 节点爆炸，严重引发卡顿：
1. **策略**：必须采用 `Virtual-Scroller` 或 `@vueuse` 提供的 `useVirtualList`，使得 DOM 树内仅保留可视窗口及其上下预留区域（缓存约 30-50 个节点）。
2. **重排适配**：由于图片消息渲染前高度未知（必须保留纵横比以占位），需通过 `IntersectionObserver` 进行懒加载并在图片 `load` 事件后重计算条目高度，避免因突然加载完成导致的视区滚动抖动。
3. **触顶加载**：绑定向上滚动事件，到达阈值（如距顶部 `100px`）触发历史分页接口，向数据源 `unshift`，并主动调节滚动条维持原坐标体验以仿冒平滑衔接。

### 2.2 本地防抖与队列处理
- **防抖机制**：当 WebSocket 短时间内收到大量同一群的广播数据帧时（例如：批量进群消息），对 Pinia 会话库需采用 `debounce` 或 `throttle` 推迟批量更新 Vue 响应式状态，避免瞬时海量页面重绘。

### 2.3 网络适配与优雅降级
考虑到气密内网可能产生波动：
1. **重发队列**：发送中的消息赋予临时 `clientMsgId` 并置为 `PENDING` 状态。`5000ms` 无返回将状态设为红色警告图标 `FAILED`，用户可手动点击重新发送。
2. **断网缓存**：配合 `@vueuse/core` 中的 `useNetwork` 指令。当 `isOnline == false` 时，关闭 `WebSocket` 请求并将消息临时存入 `indexedDB`，在网终恢复后统一消费提交（后台任务化）。

---

## 3. Vue 3 组合式 API (Composition API) 全局管理规范

### 3.1 会话管理 (Pinia `useSessionStore`)
必须剥离组件直接管理状态行为，集中使用 Pinia 保存活跃态数据：
```javascript
export const useSessionStore = defineStore('session', () => {
    // 按 chatId 归档的映射结构
    const messageMap = reactive(new Map());
    const activeChatId = ref('');

    function appendMessage(chatId, messageVO) {
        if (!messageMap.has(chatId)) {
            messageMap.set(chatId, []);
        }
        messageMap.get(chatId).push(messageVO);
    }
    
    return { messageMap, activeChatId, appendMessage };
})
```

---

> **结语**：遵循前沿的大范围 SPA 推送应用性能守则，通过强隔离的组件设计和优良的数据状态（Pinia）管理，这是对齐顶级办公即时通讯产品（如钉钉、飞书）所必须打好的技术地基。
