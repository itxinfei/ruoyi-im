<template>
  <div class="chat-header">
    <!-- 左侧：头像+标题+状态 -->
    <div
      class="header-left"
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
          >({{ realMemberCount }}人)</span>
        </h2>
        <p
          class="user-online-status"
          @click="clickConversationDesc"
        >
          {{ targetUserOnlineStateDesc }}
        </p>
      </div>
    </div>

    <!-- 右侧:操作区 -->
    <div class="header-actions">
      <!-- 搜索按钮 -->
      <el-tooltip
        content="搜索聊天内容"
        placement="bottom"
      >
        <button
          class="action-btn"
          @click="$emit('search')"
        >
          <span class="material-icons-outlined">search</span>
        </button>
      </el-tooltip>

      <!-- 语音/视频通话（仅单聊） -->
      <template v-if="isPrivateChat">
        <el-tooltip
          content="语音通话"
          placement="bottom"
        >
          <button
            class="action-btn"
            @click="$emit('voice-call')"
          >
            <span class="material-icons-outlined">call</span>
          </button>
        </el-tooltip>

        <el-tooltip
          content="视频通话"
          placement="bottom"
        >
          <button
            class="action-btn"
            @click="$emit('video-call')"
          >
            <span class="material-icons-outlined">videocam</span>
          </button>
        </el-tooltip>
      </template>

      <div class="header-divider" />

      <!-- 会话设置/详情 -->
      <el-tooltip
        :content="session?.type === 'GROUP' ? '群管理' : '聊天设置'"
        placement="bottom"
      >
        <button
          class="action-btn setting-btn"
          :class="{ active: showConversationInfo }"
          @click="toggleConversationInfo"
        >
          <span class="material-icons-outlined">more_horiz</span>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: value => {
      if (value === null) { return true }
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  },
  typingUsers: {
    type: Array,
    default: () => []
  },
  pinnedCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'show-detail',
  'show-user',
  'toggle-sidebar',
  'search',
  'toggle-pinned',
  'voice-call',
  'video-call'
])

const showConversationInfo = ref(false)

// 获取在线状态
const store = useStore()
const isOnline = computed(() => {
  if (props.session?.type === 'GROUP') { return false }

  const userId = props.session?.targetId
  if (userId && store.state.im.contact.userStatus[userId]) {
    return store.state.im.contact.userStatus[userId] === 'online'
  }

  return props.session?.peerOnline ?? false
})

// 获取真实群成员数量
const realMemberCount = computed(() => {
  if (props.session?.type !== 'GROUP') {return 0}

  // 优先从 store 获取最新的群组信息（数据更准确）
  const group = store.getters['im/contact/groupById'](props.session?.targetId)
  if (group && group.memberCount !== undefined) {
    return group.memberCount
  }

  // 降级使用会话中的数据
  return props.session?.memberCount || 0
})

// 是否有用户正在输入
const isTyping = computed(() => {
  return props.typingUsers && props.typingUsers.length > 0
})

const isPrivateChat = computed(() => props.session?.type !== 'GROUP')

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
  height: 60px; // 野火IM标准:60px高度
  padding: 0 20px; // 野火IM:左右20px padding
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
  transition: all var(--dt-transition-base);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
  }
}

// ============================================================================
// 左侧区域
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: 12px; // 野火IM:头像与文字间距12px
  flex: 1;
  min-width: 0;
  cursor: pointer;
  padding: 8px 0;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }

  .dark &:hover {
    background: rgba(255, 255, 255, 0.05);
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
  width: 40px;
  height: 40px;
  border-radius: 4px; // 野火IM:4px圆角
  flex-shrink: 0;
  position: relative;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  img {
    width: 100%;
    height: 100%;
    border-radius: 4px; // 野火IM:4px圆角
    object-fit: cover;
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
  color: var(--dt-text-primary);
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
  color: var(--dt-text-tertiary);
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
  gap: 8px; // 野火IM:按钮间距8px
  flex-shrink: 0;
}

.action-btn {
  width: 32px; // 野火IM:32px按钮
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #666666; // 野火IM:灰色图标
  cursor: pointer;
  border-radius: 4px;
  transition: all var(--dt-transition-fast);
  font-size: 18px; // 图标大小

  .material-icons-outlined {
    font-size: 18px;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.05);
    color: #4168e0; // hover时变为野火IM蓝色
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
</style>
