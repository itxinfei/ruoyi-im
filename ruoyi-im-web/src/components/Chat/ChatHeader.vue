<template>
  <div class="chat-header">
    <div class="header-left" @click="handleShowDetail" role="button" tabindex="0">
      <div class="header-avatar-wrapper">
        <DingtalkAvatar
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId"
          :is-group="session?.type === 'GROUP'"
          :members="session?.members || []"
          :size="40"
          shape="square"
          custom-class="header-avatar"
        />
        <!-- 在线状态指示器 (钉钉标准) -->
        <span v-if="session?.type !== 'GROUP'" class="online-indicator" :class="{ online: isOnline }"></span>
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <div class="meta-row">
          <span v-if="session?.type === 'GROUP'" class="meta-info">
            <el-icon class="meta-icon"><User /></el-icon>
            {{ session?.memberCount || 0 }} 人
          </span>
          <span v-else class="meta-info" :class="{ online: isOnline }">
            {{ isOnline ? '手机在线' : '离线' }}
          </span>
        </div>
      </div>
    </div>

    <div class="header-actions">
      <div class="action-group">
        <el-tooltip content="语音通话" placement="bottom">
          <button class="action-btn" @click="handleVoiceCall"><el-icon><Phone /></el-icon></button>
        </el-tooltip>
        <el-tooltip content="视频通话" placement="bottom">
          <button class="action-btn" @click="handleVideoCall"><el-icon><VideoCamera /></el-icon></button>
        </el-tooltip>
      </div>

      <div class="divider"></div>

      <div class="action-group">
        <el-tooltip content="搜索消息" placement="bottom">
          <button class="action-btn" @click="handleMenuCommand('search')"><el-icon><Search /></el-icon></button>
        </el-tooltip>
        <el-dropdown trigger="click" @command="handleMenuCommand" placement="bottom-end">
          <button class="action-btn"><el-icon><MoreFilled /></el-icon></button>
          <template #dropdown>
            <el-dropdown-menu class="header-dropdown">
              <el-dropdown-item command="search"><el-icon><Search /></el-icon> 搜索聊天记录</el-dropdown-item>
              <el-dropdown-item command="files"><el-icon><Folder /></el-icon> 查看文件</el-dropdown-item>
              <el-dropdown-item divided command="pin"><el-icon><Top /></el-icon> {{ session?.isPinned ? '取消置顶' : '置顶会话' }}</el-dropdown-item>
              <el-dropdown-item command="mute"><el-icon><BellFilled /></el-icon> {{ session?.isMuted ? '取消免打扰' : '消息免打扰' }}</el-dropdown-item>
              <el-dropdown-item divided command="clear" class="text-danger"><el-icon><Delete /></el-icon> 清空聊天记录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="divider"></div>

      <button 
        class="action-btn info-btn" 
        :class="{ active: showDetail }" 
        @click="$emit('toggle-sidebar')"
        title="会话详情"
      >
        <el-icon><InfoFilled /></el-icon>
      </button>
    </div>

    <UserDetailDrawer 
      v-model="showUserDetail" 
      :session="session" 
      @send-message="handleSendMessage" 
      @voice-call="handleVoiceCall" 
      @video-call="handleVideoCall" 
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { 
  Phone, VideoCamera, Search, MoreFilled, 
  Folder, Top, BellFilled, Delete, InfoFilled, User 
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import UserDetailDrawer from './UserDetailDrawer.vue'

const props = defineProps({
  session: Object,
  showDetail: Boolean
})

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'search', 'files', 'pin', 'mute', 'clear', 'toggle-sidebar'])
const showUserDetail = ref(false)
const store = useStore()

const isOnline = computed(() => {
  if (props.session?.type === 'GROUP') return false
  const userId = props.session?.targetId
  return userId ? (store.state.im.contact.userStatus[userId] === 'online') : (props.session?.peerOnline ?? false)
})

const handleShowDetail = () => { showUserDetail.value = true; emit('show-detail', props.session) }
const handleSendMessage = () => { showUserDetail.value = false }
const handleVoiceCall = () => { emit('voice-call', props.session) }
const handleVideoCall = () => { emit('video-call', props.session) }
const handleMenuCommand = (command) => { emit(command, props.session) }
</script>

<style scoped lang="scss">
// ============================================================================
// 聊天头部容器 - 强制高度 60px
// ============================================================================
.chat-header {
  height: var(--dt-chat-header-height);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--dt-spacing-lg);
  background: var(--dt-bg-input);
  flex-shrink: 0;
  z-index: var(--dt-z-sticky);
}

// ============================================================================
// 左侧会话信息
// ============================================================================
.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  cursor: pointer;
  padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-base);
  min-width: 0;

  &:hover {
    background: var(--dt-bg-session-hover);
  }
}

.header-avatar-wrapper {
  position: relative;
  flex-shrink: 0;

  .online-indicator {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #c9cdd4;
    border: 2px solid #fff;
    
    &.online {
      background: var(--dt-success-color);
    }
  }
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;

  .header-name {
    font-size: var(--dt-font-size-lg);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin: 0;
    line-height: 1.2;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .meta-row {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);

    .meta-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-tertiary);
      
      .meta-icon {
        font-size: 12px;
      }

      &.online {
        color: var(--dt-success-color);
      }
    }
  }
}

// ============================================================================
// 右侧操作区
// ============================================================================
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);

  .action-group {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .action-btn {
    width: 34px;
    height: 34px;
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    color: var(--dt-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }

    &:active {
      transform: scale(0.92);
    }

    &.info-btn.active {
      color: var(--dt-brand-color);
      background: var(--dt-brand-lighter);
    }

    .el-icon {
      font-size: 20px;
    }
  }

  .divider {
    width: 1px;
    height: 16px;
    background: var(--dt-border-light);
    margin: 0 var(--dt-spacing-sm);
  }
}

// ============================================================================
// 下拉菜单定制
// ============================================================================
:deep(.header-dropdown) {
  border-radius: var(--dt-radius-md);
  padding: 4px;
  
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    font-size: var(--dt-font-size-base);
    border-radius: var(--dt-radius-sm);
    
    .el-icon {
      font-size: 16px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background-color: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
      .el-icon { color: var(--dt-brand-color); }
    }

    &.text-danger {
      color: var(--dt-error-color);
      &:hover { background-color: var(--dt-error-bg); }
    }
  }
}
</style>
