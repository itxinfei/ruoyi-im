<template>
  <div class="chat-header">
    <div class="header-left" @click="handleShowDetail" role="button" tabindex="0">
      <div class="header-avatar-wrapper">
        <div v-if="session?.type === 'GROUP'" class="header-avatar group-avatar">
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
        <span v-if="session?.type !== 'GROUP' && isOnline" class="online-indicator"></span>
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <div class="meta-row">
          <span v-if="session?.type === 'GROUP'" class="meta-info">
            <span class="material-icons-outlined meta-icon">people</span>
            {{ session?.memberCount || 0 }} 人
          </span>
          <span v-else class="meta-info" :class="{ online: isOnline }">
            <span class="material-icons-outlined meta-icon">circle</span>
            {{ isOnline ? '在线' : '离线' }}
          </span>
        </div>
      </div>
    </div>
    <div class="header-actions">
      <el-tooltip content="语音通话" placement="bottom"><button class="action-btn" @click="handleVoiceCall"><span class="material-icons-outlined">phone</span></button></el-tooltip>
      <el-tooltip content="视频通话" placement="bottom"><button class="action-btn" @click="handleVideoCall"><span class="material-icons-outlined">videocam</span></button></el-tooltip>
      <el-dropdown trigger="click" @command="handleMenuCommand" placement="bottom-end">
        <button class="action-btn"><span class="material-icons-outlined">more_horiz</span></button>
        <template #dropdown>
          <el-dropdown-menu class="header-dropdown">
            <el-dropdown-item command="search"><span class="material-icons-outlined item-icon">search</span> 搜索聊天记录</el-dropdown-item>
            <el-dropdown-item command="files"><span class="material-icons-outlined item-icon">folder</span> 查看文件</el-dropdown-item>
            <el-dropdown-item divided command="pin"><span class="material-icons-outlined item-icon">push_pin</span> {{ session?.isPinned ? '取消置顶' : '置顶会话' }}</el-dropdown-item>
            <el-dropdown-item command="mute"><span class="material-icons-outlined item-icon">notifications_off</span> {{ session?.isMuted ? '取消免打扰' : '消息免打扰' }}</el-dropdown-item>
            <el-dropdown-item divided command="clear" class="danger-item"><span class="material-icons-outlined item-icon">delete_outline</span> 清空聊天记录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div class="divider"></div>
      <button class="action-btn info-btn" :class="{active: showDetail}" @click="$emit('toggle-sidebar')"><span class="material-icons-outlined">info</span></button>
    </div>

    <UserDetailDrawer v-model="showUserDetail" :session="session" @send-message="handleSendMessage" @voice-call="handleVoiceCall" @video-call="handleVideoCall" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
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
.chat-header { height: 60px; border-bottom: 1px solid #f2f3f5; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; background: #fff; flex-shrink: 0; z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; cursor: pointer; border-radius: 6px; transition: background 0.2s;
  &:hover { background: #f5f6f7; }
}
.header-avatar { border-radius: 4px; }
.group-avatar { width: 40px; height: 40px; border-radius: 4px; background: #1677ff; color: #fff; display: flex; align-items: center; justify-content: center; .material-icons-outlined { font-size: 24px; } }
.header-info { display: flex; flex-direction: column; gap: 2px;
  .header-name { font-size: 16px; font-weight: 600; color: #1f2329; margin: 0; line-height: 1.4; }
  .meta-row { display: flex; align-items: center; gap: 8px;
    .meta-info { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #8f959e; 
      .meta-icon { font-size: 10px; }
      &.online { color: #52c41a; }
    }
  }
}
.header-actions { display: flex; align-items: center; gap: 4px;
  .action-btn { width: 34px; height: 34px; border: none; background: transparent; border-radius: 6px; color: #646a73; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s;
    &:hover { background: #f2f3f5; color: #1677ff; }
    &.info-btn.active { color: #1677ff; background: #e8f4ff; }
    .material-icons-outlined { font-size: 22px; }
  }
  .divider { width: 1px; height: 16px; background: #f2f3f5; margin: 0 8px; }
}
:deep(.header-dropdown) {
  .el-dropdown-menu__item { display: flex; align-items: center; gap: 10px; padding: 10px 16px; font-size: 14px;
    .item-icon { font-size: 18px; color: #646a73; }
    &:hover .item-icon { color: #1677ff; }
  }
}
</style>
