<template>
  <div class="chat-panel-container">
    <!-- L2: 会话列表 (280px) -->
    <ChatSessionList />

    <!-- 垂直分割线 -->
    <div class="resizer-v" />

    <!-- L3: 聊天主窗口 -->
    <ChatWindow />
  </div>
</template>

<script setup lang="js">
import ChatSessionList from '@/components/im/ChatSessionList.vue'
import ChatWindow from '@/components/im/ChatWindow.vue'
</script>

<style scoped lang="scss">
.chat-panel-container {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.resizer-v {
  width: 1px;
  height: 100%;
  background: rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}
</style>
