<template>
  <div class="chat-main-v4">
    <!-- 1. 核心会话区 (Header + List + Input) -->
    <div class="chat-container">
      <ChatWindowHeader
        :session="currentSession"
        :is-sidebar-open="isDetailOpen"
        @toggle-sidebar="isDetailOpen = !isDetailOpen"
        @toggle-selection="toggleSelectionMode"
        @open-search="showGlobalSearch = true"
        @voice-call="handleVoiceCall"
        @video-call="handleVideoCall"
      />

      <!-- 消息列表区 -->
      <div class="message-area-scroller custom-scrollbar" ref="messageListRef">
        <ChatMessageList
          :messages="messages"
          :is-selection-mode="isSelectionMode"
          :selected-messages="selectedMessages"
          @select-message="handleMessageClick"
        />
      </div>

      <!-- 底部输入区 (对齐钉钉：通铺纯白) -->
      <ChatInputArea
        v-model="currentDraft"
        @send="processSendMessage"
      />

      <!-- 多选操作条 (悬浮覆盖) -->
      <SelectionActionBar
        v-if="isSelectionMode"
        :selected-count="selectedMessages.size"
        @cancel="isSelectionMode = false"
      />
    </div>

    <!-- 2. 侧边详情区 (去虚构：Push 布局而非覆盖) -->
    <transition name="push-sidebar">
      <aside v-if="isDetailOpen" class="chat-right-sidebar">
        <ChatDetailDrawer :session="currentSession" @close="isDetailOpen = false" />
      </aside>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import ChatWindowHeader from './ChatWindow/ChatWindowHeader.vue'
import ChatMessageList from './ChatWindow/ChatMessageList.vue'
import ChatInputArea from './ChatInputArea.vue'
import SelectionActionBar from './ChatWindow/SelectionActionBar.vue'
import ChatDetailDrawer from '@/components/Chat/ChatDetailDrawer.vue'

const store = useStore()
const isDetailOpen = ref(false)
const isSelectionMode = ref(false)
const selectedMessages = ref(new Set())
const currentDraft = ref('')

const currentSession = computed(() => store.state.im.session.currentSession)
const messages = computed(() => store.state.im.message.messages)

// 模拟发送
const processSendMessage = (payload) => {
  store.dispatch('im/message/sendMessage', { ...payload, sessionId: currentSession.value.id })
}
</script>

<style scoped lang="scss">
.chat-main-v4 {
  flex: 1;
  display: flex;
  height: 100%;
  overflow: hidden;
  background-color: #ffffff; // Slack 纯白背景
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 400px;
  height: 100%;
  position: relative;
}

.message-area-scroller {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 右侧侧边栏 (钉钉 Push 布局) */
.chat-right-sidebar {
  width: 320px;
  background-color: #ffffff;
  border-left: 1px solid rgba(0, 0, 0, 0.05);
  height: 100%;
  flex-shrink: 0;
  z-index: 50;
}

// 侧边栏滑出动效 (挤压感)
.push-sidebar-enter-active, .push-sidebar-leave-active {
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.push-sidebar-enter-from, .push-sidebar-leave-to {
  width: 0;
}
</style>
