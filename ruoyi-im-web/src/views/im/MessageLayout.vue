<template>
  <div class="message-layout">
    <!-- 一级导航栏 (固定 68px) -->
    <aside class="primary-sidebar">
      <div class="nav-icons">
        <div class="nav-item active">
          <i class="icon-message"></i>
        </div>
      </div>
    </aside>

    <!-- 二级会话栏 (240px) -->
    <ChatSessionList />

    <!-- 聊天主窗体 -->
    <main class="chat-main-container">
      <ChatWindow v-if="currentSession" />
      <div v-else class="empty-placeholder">
        <p>未选择会话</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
/**
 * MessageLayout.vue (Vuex 修复版)
 */
import { computed } from 'vue';
import { useStore } from 'vuex';
import ChatSessionList from '@/components/im/ChatSessionList.vue';
import ChatWindow from '@/components/im/ChatWindow.vue';

const store = useStore();
const currentSession = computed(() => store.state.imSession.currentSession);
</script>

<style scoped>
.message-layout { display: flex; width: 100vw; height: 100vh; overflow: hidden; }
.primary-sidebar { width: 68px; height: 100%; background-color: var(--dt-sb-bg); display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
.chat-main-container { flex: 1; height: 100%; min-width: 0; background-color: var(--dt-bg-chat); }
.empty-placeholder { display: flex; align-items: center; justify-content: center; height: 100%; color: #999; }
</style>
