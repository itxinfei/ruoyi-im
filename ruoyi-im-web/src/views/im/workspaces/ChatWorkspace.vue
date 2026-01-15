<template>
  <div class="chat-workspace" :class="{ collapsed: collapsed }">
    <!-- 会话面板 -->
    <SessionPanel 
      :width="sessionWidth"
      :collapsed="collapsed"
      :nav-width="navWidth"
      @update:width="updateSessionWidth"
      @select-session="selectSession"
    />

    <!-- 聊天面板 -->
    <ChatPanel 
      :current-session="currentSession"
      :collapsed="collapsed"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import SessionPanel from './SessionPanel.vue'
import ChatPanel from './ChatPanel.vue'

const store = useStore()

// 响应式状态
const collapsed = ref(false)
const navWidth = ref(60)

// 会话面板宽度
const sessionWidth = ref(parseInt(localStorage.getItem('sessionWidth')) || 320)

// 当前会话
const currentSession = computed(() => store.state.im.currentSession)

// 更新会话面板宽度
const updateSessionWidth = (width) => {
  sessionWidth.value = width
  localStorage.setItem('sessionWidth', width)
}

// 选择会话
const selectSession = (session) => {
  store.commit('im/SET_CURRENT_SESSION', session)
}
</script>

<style scoped lang="scss">
.chat-workspace {
  display: flex;
  width: 100%;
  height: 100%;
  transition: all 0.3s ease;
  
  &.collapsed {
    .session-panel {
      width: 0 !important;
      padding: 0;
      overflow: hidden;
    }
  }
}
</style>
