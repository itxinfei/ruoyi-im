<template>
  <div class="chat-header">
    <!-- 左侧：头像+标题+状态 -->
    <div
      class="header-left"
      role="button"
      tabindex="0"
      @click="handleShowDetail"
    >
      <div class="header-avatar-wrapper">
        <!-- 群组使用图标，单聊使用钉钉风格头像 -->
        <div
          v-if="session?.type === 'GROUP'"
          class="header-avatar group-avatar"
        >
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId"
          :size="40"
          shape="square"
          custom-class="header-avatar"
        />
        <span
          v-if="session?.type !== 'GROUP' && isOnline"
          class="online-indicator"
        />
        <span
          v-if="session?.type !== 'GROUP' && isOnline"
          class="online-pulse"
        />
      </div>
      <div class="header-info">
        <h2 class="header-name">
          {{ session?.name }}
          <span
            v-if="session?.type === 'GROUP'"
            class="member-count"
          >({{ session?.memberCount || 0 }}人)</span>
        </h2>
        <p
          class="user-online-status"
          @click="clickConversationDesc"
        >
          {{ targetUserOnlineStateDesc }}
        </p>
      </div>
    </div>

    <!-- 右侧：设置按钮 -->
    <div class="header-actions">
      <button
        class="action-btn setting-btn"
        :class="{ active: showConversationInfo }"
        @click="toggleConversationInfo"
      >
        <span class="material-icons-outlined">settings</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: value => {
      if (value === null) {return true}
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  },
  typingUsers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['show-detail', 'show-user', 'toggle-sidebar'])

const showConversationInfo = ref(false)

// 获取在线状态
const store = useStore()
const isOnline = computed(() => {
  if (props.session?.type === 'GROUP') {return false}

  const userId = props.session?.targetId
  if (userId && store.state.im.contact.userStatus[userId]) {
    return store.state.im.contact.userStatus[userId] === 'online'
  }

  return props.session?.peerOnline ?? false
})

// 是否有用户正在输入
const isTyping = computed(() => {
  return props.typingUsers && props.typingUsers.length > 0
})

// 在线状态描述
const targetUserOnlineStateDesc = computed(() => {
  if (props.session?.type === 'GROUP') {
    return ''
  }

  if (!isOnline.value) {
    return '离线'
  }

  if (isTyping.value) {
    return '正在输入...'
  }

  return '在线'
})

// 显示详情 - 触发全局弹窗
const handleShowDetail = () => {
  // 单聊时触发用户详情弹窗
  if (props.session?.type !== 'GROUP') {
    const userId = props.session?.targetId
    if (userId) {
      emit('show-user', userId)
      return
    }
  }
  // 群聊或无targetId时触发详情面板
  emit('show-detail', props.session)
}

// 点击状态描述区域 - 触发用户详情弹窗
const clickConversationDesc = () => {
  // 单聊时触发用户详情弹窗
  if (props.session?.type !== 'GROUP') {
    const userId = props.session?.targetId
    if (userId) {
      emit('show-user', userId)
      return
    }
  }
  emit('show-detail', props.session)
}

// 切换会话信息面板
const toggleConversationInfo = () => {
  showConversationInfo.value = !showConversationInfo.value
  emit('toggle-sidebar', showConversationInfo.value ? 'info' : null)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 容器
// ============================================================================
.chat-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
  z-index: 10;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

// ============================================================================
// 左侧区域
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px 8px 8px;
  margin: -8px -12px -8px -8px;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-base);

  &:hover {
    background: rgba(0, 0, 0, 0.04);

    .header-avatar-wrapper {
      transform: scale(1.02);
    }
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &:active {
    transform: scale(0.98);
  }
}

.header-avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s var(--dt-ease-out);
  flex-shrink: 0;
}

.header-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-2);

  :deep(.dingtalk-avatar) {
    border-radius: var(--dt-radius-md) !important;
  }
}

.group-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  background: linear-gradient(135deg, #0089FF 0%, #006ECC 100%);
  color: #fff;

  .material-icons-outlined {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }
}

// 在线状态指示器
.online-indicator {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 12px;
  height: 12px;
  background: var(--dt-success-color);
  border: 2px solid #f5f5f5;
  border-radius: var(--dt-radius-full);
  z-index: 2;

  .dark & {
    border-color: var(--dt-bg-card-dark);
  }
}

// 在线脉冲动画
.online-pulse {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 12px;
  height: 12px;
  background: var(--dt-success-color);
  border-radius: var(--dt-radius-full);
  z-index: 1;
  animation: onlinePulse 2s ease-out infinite;
}

@keyframes onlinePulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.5);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.header-name {
  font-size: 16px;
  font-weight: normal;
  color: #181818;
  margin: 0;
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 4px;

  .member-count {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    font-weight: normal;
  }

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.user-online-status {
  color: gray;
  font-size: 12px;
  margin: 0;
  cursor: pointer;

  &:hover {
    color: var(--dt-brand-color);
  }

  .dark & {
    color: var(--dt-text-tertiary-dark);
  }
}

// ============================================================================
// 右侧操作区
// ============================================================================
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 4px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  padding: 0;
  color: #181818;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--dt-ease-out);

  .material-icons-outlined {
    font-size: 20px;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.06);
    color: #3f64e4;
  }

  &:active {
    transform: scale(0.95);
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &.active {
    color: #3f64e4;
  }

  .dark & {
    color: var(--dt-text-secondary-dark);

    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }
  }
}

// ============================================================================
// 响应式设计
// ============================================================================
@media (max-width: 768px) {
  .chat-header {
    padding: 0 12px;

    .header-name {
      font-size: 15px;

      .member-count {
        font-size: 11px;
      }
    }

    .user-online-status {
      font-size: 11px;
    }
  }

  .header-left {
    gap: 8px;
    padding: 6px 8px 6px 6px;
  }

  .header-avatar,
  .group-avatar {
    width: 36px;
    height: 36px;

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .action-btn {
    width: 34px;
    height: 34px;

    .material-icons-outlined {
      font-size: 18px;
    }
  }
}

@media (max-width: 480px) {
  .chat-header {
    padding: 0 8px;
  }

  .header-name {
    font-size: 14px;
  }

  .action-btn {
    width: 32px;
    height: 32px;
  }
}
</style>
