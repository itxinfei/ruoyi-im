<template>
  <div class="message-layout">
    <!-- 一级导航栏 (对齐钉钉 64px) -->
    <ImSideNavNew 
      :active-module="'chat'" 
      @switch-module="handleSwitchModule"
    />

    <!-- 二级会话栏 (250px) -->
    <ChatSessionList />

    <!-- 聊天主窗体 -->
    <main class="chat-main-container">
      <ChatWindow v-if="currentSession" />
      
      <!-- 完善的空状态：Logo + 欢迎语 -->
      <div v-else class="empty-placeholder">
        <div class="welcome-card">
          <div class="welcome-logo">
            <el-icon :size="48" color="var(--dt-text-white)"><ChatDotRound /></el-icon>
          </div>
          <h2 class="welcome-title">欢迎使用 IM</h2>
          <p class="welcome-subtitle">选择左侧会话，开始高效协同</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
/**
 * MessageLayout.vue (对齐钉钉 Windows 布局规范)
 */
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ChatDotRound } from '@element-plus/icons-vue';
import ImSideNavNew from '@/components/ImSideNavNew/index.vue';
import ChatSessionList from '@/components/im/ChatSessionList.vue';
import ChatWindow from '@/components/im/ChatWindow.vue';

const store = useStore();
const router = useRouter();

// 对齐 Vuex 路径: im/session/currentSession
const currentSession = computed(() => store.state.im?.session?.currentSession);

const handleSwitchModule = (moduleId) => {
  if (moduleId === 'chat') return;
  router.push(`/${moduleId}`);
};
</script>

<style scoped>
.message-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: var(--dt-bg-body);
}

/* 左侧导航 64px + 会话列表 250px 固定宽度 */
:deep(.side-nav) {
  width: 64px;
  flex-shrink: 0;
}

.chat-main-container {
  flex: 1;
  height: 100%;
  min-width: 480px;
  background-color: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: var(--dt-bg-body);
}

.welcome-card {
  text-align: center;
  animation: fadeIn 0.5s ease-out;
}

.welcome-logo {
  width: 80px;
  height: 80px;
  background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
  border-radius: var(--dt-radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 24px var(--dt-brand-bg-dark);
}

.welcome-title {
  font-size: var(--dt-font-size-2xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-tertiary);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
