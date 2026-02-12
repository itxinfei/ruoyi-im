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

.chat-header {
  height: var(--dt-chat-header-height);
  padding: 0 16px; // 钉钉标准：16px 左右内边距
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04); // 钉钉标准：更轻的阴影

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
    box-shadow: none;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px; // 优化：10px 间距
  flex: 1;
  min-width: 0;
  cursor: pointer;
  padding: 4px 8px; // 优化：4px 上下，8px 左右
  border-radius: 8px;
  margin-left: -4px;
  transition: all 0.2s var(--dt-ease-out);

  &:hover {
    background: var(--dt-bg-subtle-hover);
  }
}

.header-avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-avatar {
  width: 40px; // 钉钉标准：40px
  height: 40px; // 钉钉标准：40px
  border-radius: 6px; // 钉钉标准：6px 圆角
  overflow: hidden;

  :deep(.dingtalk-avatar) {
    border-radius: 6px !important;
  }
}

.group-avatar {
  background: var(--dt-brand-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  .material-icons-outlined {
    font-size: 24px;
  }
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 11px;
  height: 11px;
  background: var(--dt-success-color);
  border: 2px solid #fff;
  border-radius: 50%;
  z-index: 2;

  .dark & {
    border-color: var(--dt-bg-card-dark);
  }
}

.header-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  gap: 2px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  line-height: 1.2;
  display: flex;
  align-items: center;
  gap: 6px;

  .member-count {
    font-size: 13px;
    color: var(--dt-text-quaternary);
    font-weight: normal;
  }
}

.user-online-status {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin: 0;
  line-height: 1;

  &:hover {
    color: var(--dt-brand-color);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px; // 钉钉标准：4px 间距
}

.action-btn {
  width: 32px; // 钉钉标准：32px
  height: 32px; // 钉钉标准：32px
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px; // 钉钉标准：6px 圆角
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }
}

.header-divider {
  width: 1px;
  height: 16px;
  background: var(--dt-border-light);
  margin: 0 4px;
}
</style>
