<template>
  <div class="message-layout">
    <!-- 一级导航栏 (Doc-21 §4): 72px, 深色背景 -->
    <aside class="primary-sidebar">
      <div class="nav-icons">
        <div class="nav-item active">
          <i class="icon-message"></i>
        </div>
        <div class="nav-item">
          <i class="icon-contact"></i>
        </div>
        <div class="nav-item">
          <i class="icon-workbench"></i>
        </div>
      </div>
      <div class="user-avatar-bottom">
        <img src="/avatars/me.png" class="avatar" alt="me" />
      </div>
    </aside>

    <!-- 二级会话栏 (Doc-21 §5): 240px -->
    <ChatSessionList />

    <!-- 聊天主窗体 (Doc-21 §3.1) -->
    <main class="chat-main-container">
      <ChatWindow v-if="activeConversationId" />
      <div v-else class="empty-placeholder">
        <img src="/images/empty-chat.png" alt="empty" />
        <p>未选择会话</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
/**
 * MessageLayout.vue (主骨架层)
 * 严格对齐 Doc-21 三栏结构
 */
import { computed } from 'vue';
import { useSessionStore } from '@/stores/session';
import ChatSessionList from './ChatSessionList.vue';
import ChatWindow from './ChatWindow.vue';

const sessionStore = useSessionStore();
const activeConversationId = computed(() => sessionStore.activeConversationId);
</script>

<style scoped>
.message-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: var(--dt-bg-body);
}

/* 一级导航 (Doc-21 §4) */
.primary-sidebar {
  width: 72px;
  height: 100%;
  background-color: var(--dt-sb-bg); /* #2B2D30 */
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  flex-shrink: 0;
}

.nav-icons {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.nav-item {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--dt-sb-icon);
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item.active {
  color: var(--dt-sb-icon-active);
  background-color: rgba(255, 255, 255, 0.1);
}

.user-avatar-bottom {
  margin-top: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px; /* 钉钉标准圆角 */
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 主内容区 */
.chat-main-container {
  flex: 1;
  height: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-chat);
}

.empty-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-desc);
}

.empty-placeholder img {
  width: 160px;
  opacity: 0.5;
  margin-bottom: 16px;
}
</style>
