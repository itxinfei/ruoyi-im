<template>
  <div class="dt-main-layout">
    <!-- 1. 左侧一级导航 (64px) -->
    <ImSideNavNew
      :active-module="activeModule"
      @switch-module="handleSwitchModule"
      @open-edit-profile="profileVisible = true"
      @open-system-settings="settingsVisible = true"
    />

    <!-- 2. 主功能区：流式切换与状态保持 -->
    <main class="dt-main-view">
      <transition :name="transitionName" mode="out-in">
        <keep-alive>
          <component
            :is="activeViewComponent"
            :key="activeModule"
            @switch-module="handleSwitchModule"
          />
        </keep-alive>
      </transition>
    </main>

    <!-- 3. 全局辅助组件 -->
    <EditProfileDialog v-model="profileVisible" />
    <SystemSettingsDialog v-model="settingsVisible" />
    <GlobalSearch v-model:visible="showGlobalSearch" @select="handleSearchSelect" />
  </div>
</template>

<script setup lang="js">
import { ref, computed, defineAsyncComponent, onMounted, onUnmounted, watch } from 'vue'
import ImSideNavNew from '@/components/ImSideNavNew/index.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'
import SystemSettingsDialog from '@/components/Common/SystemSettingsDialog.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'

const views = {
  chat: defineAsyncComponent(() => import('@/views/ChatPanel.vue')),
  contacts: defineAsyncComponent(() => import('@/views/ContactsPanel.vue')),
  workbench: defineAsyncComponent(() => import('@/views/WorkbenchPanel.vue')),
  todo: defineAsyncComponent(() => import('@/views/TodoPanel.vue')),
  calendar: defineAsyncComponent(() => import('@/views/CalendarPanel.vue')),
  documents: defineAsyncComponent(() => import('@/views/DocumentsPanel.vue')),
  approval: defineAsyncComponent(() => import('@/views/ApprovalPanel.vue')),
  mail: defineAsyncComponent(() => import('@/views/MailPanel.vue')),
  ding: defineAsyncComponent(() => import('@/views/DingPanel.vue')),
  call: defineAsyncComponent(() => import('@/components/Chat/CallHistoryPanel.vue'))
}

const moduleOrder = ['chat', 'contacts', 'workbench', 'todo', 'calendar', 'documents', 'approval', 'mail', 'ding']

const activeModule = ref('chat')
const previousModule = ref('chat')
const profileVisible = ref(false)
const settingsVisible = ref(false)
const showGlobalSearch = ref(false)

const activeViewComponent = computed(() => views[activeModule.value] || views.chat)

const transitionName = computed(() => {
  const prevIdx = moduleOrder.indexOf(previousModule.value)
  const currIdx = moduleOrder.indexOf(activeModule.value)
  return currIdx > prevIdx ? 'view-slide-left' : 'view-slide-right'
})

const handleSwitchModule = (id) => {
  if (id === 'search') {
    showGlobalSearch.value = true
    return
  }
  if (id !== activeModule.value) {
    previousModule.value = activeModule.value
    activeModule.value = id
  }
}

const handleSearchSelect = (item) => { /* 全局跳转逻辑 */ }

const handleGlobalKeydown = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
    e.preventDefault()
    showGlobalSearch.value = true
  }
}

onMounted(() => window.addEventListener('keydown', handleGlobalKeydown))
onUnmounted(() => window.removeEventListener('keydown', handleGlobalKeydown))
</script>

<style scoped lang="scss">
// ============================================================================
// 主布局 - 三栏结构
// ============================================================================

.dt-main-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--dt-bg-body);
}

.dt-main-view {
  flex: 1;
  min-width: 0;
  height: 100%;
  position: relative;
  overflow: hidden;
}

// ============================================================================
// 视图切换动效 - 左右滑动
// ============================================================================

.view-slide-left-enter-active,
.view-slide-left-leave-active,
.view-slide-right-enter-active,
.view-slide-right-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.view-slide-left-enter-from {
  opacity: 0;
  transform: translateX(12px);
}
.view-slide-left-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

.view-slide-right-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}
.view-slide-right-leave-to {
  opacity: 0;
  transform: translateX(12px);
}
</style>
