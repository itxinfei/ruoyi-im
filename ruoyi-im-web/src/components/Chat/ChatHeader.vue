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

      <!-- 钉钉风格通话按钮布局 -->
      <template v-if="isPrivateChat">
        <div class="call-buttons">
          <el-tooltip
            content="语音通话"
            placement="bottom"
          >
            <button
              class="action-btn call-btn voice-btn"
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
              class="action-btn call-btn video-btn"
              @click="$emit('video-call')"
            >
              <span class="material-icons-outlined">videocam</span>
            </button>
          </el-tooltip>
        </div>
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
          <span class="material-icons-outlined">more_vert</span>
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

.chat-header {
  height: 60px;
  padding: 0 20px;
  background: #FFFFFF;
  border-bottom: 1px solid #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  z-index: 10;

  .dark & {
    background: #171A1D;
    border-bottom-color: #2E3238;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  margin-left: -6px;
  transition: background 0.2s;

  &:hover {
    background: #F7F8F9;
  }
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 4px; // 钉钉标准方圆角
  overflow: hidden;
  flex-shrink: 0;

  :deep(.dingtalk-avatar) {
    border-radius: 4px !important;
  }
}

.online-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #00B42A;
  border: 2px solid #fff;
  border-radius: 50%;

  &::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    border: 1px solid #00B42A;
    animation: status-pulse 2s infinite;
  }
}

@keyframes status-pulse {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}

.header-name {
  font-size: 17px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;

  .member-count {
    font-size: 14px;
    color: #86909C;
    font-weight: 400;
  }
}

.user-online-status {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}

.action-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #4E5969;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F2F3F5;
    color: #165DFF;
  }
}

.call-buttons {
  display: flex;
  gap: 4px;
}

.call-btn {
  &:hover {
    background: #F2F3F5;
    color: #165DFF;
  }

  &.voice-btn:hover {
    background: #FFF2E8;
    color: #FF7D00;
  }

  &.video-btn:hover {
    background: #E8F7FF;
    color: #00B4FF;
  }
}

.header-divider {
  width: 1px;
  height: 16px;
  background: var(--dt-border-light);
  margin: 0 4px;
}
</style>
