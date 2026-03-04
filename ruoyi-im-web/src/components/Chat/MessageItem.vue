<template>
  <div
    class="message-item"
    :class="{ 
      'is-own': message.isOwn,
      'is-sending': message.status === 'sending',
      'is-failed': message.status === 'failed',
      'message-enter': message.isNew
    }"
  >
    <!-- 时间分割线 (如果是时间消息) -->
    <div v-if="message.isTimeDivider" class="time-divider">
      <span class="time-text">{{ message.timeText }}</span>
    </div>

    <template v-else>
      <!-- 头像 -->
      <div
        class="avatar-container"
        :class="{ 'avatar-sending': message.status === 'sending' }"
        @contextmenu.prevent="$emit('at', message)"
        @click="$emit('show-user', message.senderId)"
        title="右键 @提及，左键查看资料"
      >
        <DingtalkAvatar
          :src="message.senderAvatar"
          :name="message.senderName"
          :user-id="message.senderId"
          :size="36"
          shape="square"
          custom-class="message-avatar"
        />
        <!-- 发送中遮罩 -->
        <div v-if="message.status === 'sending'" class="avatar-overlay">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
      </div>

      <div class="content-wrapper">
        <!-- 发送者姓名 -->
        <div v-if="!message.isOwn" class="sender-name">{{ message.senderName }}</div>

        <div class="message-content-main">
          <!-- 消息气泡内容插槽 -->
          <slot name="bubble"></slot>

          <!-- 悬停快捷按钮区 (钉钉风格微交互) -->
          <div class="message-actions-floating" v-if="message.type !== 'RECALLED' && !['sending', 'uploading'].includes(message.status)">
            <div class="action-bar-min">
               <el-tooltip content="回复" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reply', message)"><el-icon><ChatLineSquare /></el-icon></button>
               </el-tooltip>
               <el-tooltip content="点赞" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reaction', message, '👍')">👍</button>
               </el-tooltip>
               <el-dropdown @command="(c) => $emit('command', c, message)" trigger="click">
                  <button class="mini-btn"><el-icon><MoreFilled /></el-icon></button>
                  <template #dropdown>
                    <el-dropdown-menu>
                       <el-dropdown-item command="read-detail" v-if="message.isOwn && message.type !== 'SYSTEM'">
                         <el-icon><View /></el-icon> 查看已读
                       </el-dropdown-item>
                       <el-dropdown-item command="forward">转发</el-dropdown-item>
                       <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">复制</el-dropdown-item>
                       <el-dropdown-item command="recall" v-if="message.isOwn && canRecall">撤回</el-dropdown-item>
                       <el-dropdown-item command="delete" divided class="text-danger">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
               </el-dropdown>
            </div>
          </div>
          
          <!-- 上传进度条 (仅图片/文件上传时显示) -->
          <div v-if="message.status === 'uploading' && message.uploadProgress" class="upload-progress-bar">
            <div class="progress-fill" :style="{ width: message.uploadProgress + '%' }"></div>
          </div>
        </div>

        <!-- 消息页脚 (状态与时间) -->
        <div class="message-footer">
          <div v-if="message.isOwn" class="status-container">
            <!-- 已读状态 (钉钉风格) -->
            <div v-if="message.status !== 'sending' && message.status !== 'failed' && message.status !== 'uploading'" 
                 class="read-status" :class="{ 'is-read': message.readCount > 0 }">
              <span>{{ message.readCount > 0 ? '已读' : '未读' }}</span>
            </div>
            
            <!-- 发送中状态 -->
            <el-icon v-if="message.status === 'sending' || message.status === 'uploading'" class="is-loading status-loading-icon"><Loading /></el-icon>

            <!-- 发送失败状态 -->
            <div v-else-if="message.status === 'failed'" class="status-failed-icon" @click="$emit('retry', message)">
              <el-icon><WarningFilled /></el-icon>
            </div>
          </div>
          <div class="time">{{ formattedTime }}</div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChatLineSquare, MoreFilled, Loading, WarningFilled, Check, Star, View } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  message: { type: Object, required: true }
})

defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user', 'retry', 'multi-select'])

// 消息是否可撤回 (5分钟内)
const canRecall = computed(() => {
  if (!props.message.timestamp) return false
  const now = Date.now()
  const msgTime = new Date(props.message.timestamp).getTime()
  return (now - msgTime) < 5 * 60 * 1000
})

const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  const date = new Date(props.message.timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
})
</script>

<style scoped lang="scss">
// ============================================================================
// 消息项容器
// ============================================================================
.message-item {
  display: flex !important;
  width: 100% !important;
  margin-bottom: 16px;
  position: relative;
  padding: 0 16px;
  box-sizing: border-box;
  transition: opacity 0.2s;

  &.is-own {
    flex-direction: row-reverse !important;
    justify-content: flex-start !important;
  }
}

// ============================================================================
// 头像区域
// ============================================================================
.avatar-container {
  flex-shrink: 0;
  cursor: pointer;
  align-self: flex-start;
  position: relative;
}

.content-wrapper {
  max-width: calc(100% - 110px);
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 0 12px;
  align-items: flex-start !important;
  min-width: 0;
}

.is-own .content-wrapper {
  align-items: flex-end !important;
  margin-left: 48px; 
  margin-right: 12px;
}

.sender-name {
  font-size: 12px;
  color: #8f959e;
  margin-bottom: 2px;
  margin-left: 4px;
}

.is-own .sender-name {
  display: none; // 钉钉在右侧自己的消息通常不显示名字
}

.message-content-main {
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 100%;

  // 悬停显示动作条
  &:hover {
    .message-actions-floating {
      opacity: 1;
      visibility: visible;
    }
  }
}

.is-own .message-content-main {
  flex-direction: row-reverse;
}

.message-actions-floating {
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s;
  
  .action-bar-min {
    display: flex;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    padding: 2px;
  }
}

.mini-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: #64748b;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    background: #f1f5f9;
    color: #1677ff;
  }
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  min-height: 16px;
}

.is-own .message-footer {
  flex-direction: row-reverse;
}

.status-container {
  display: flex;
  align-items: center;
}

.read-status {
  font-size: 11px;
  color: #8f959e;
  cursor: default;
  user-select: none;

  &.is-read {
    color: #1677ff;
    font-weight: 500;
  }
}

.status-loading-icon {
  font-size: 12px;
  color: #1677ff;
}

.status-failed-icon {
  color: #ff4d4f;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.time {
  color: #bfbfbf;
  font-size: 11px;
}

.upload-progress-bar {
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 1px;
  overflow: hidden;

  .progress-fill {
    height: 100%;
    background: #1677ff;
    transition: width 0.3s;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .message-actions-floating .action-bar-min {
    background: #1e293b;
    border-color: #334155;
  }

  .mini-btn:hover {
    background: rgba(22, 119, 255, 0.15) !important;
  }

  .time-divider .time-text {
    background: rgba(255, 255, 255, 0.08);
    color: #94a3b8;
  }

  .avatar-overlay {
    background: rgba(0, 0, 0, 0.6);
  }

  .upload-progress-bar {
    background: #334155;
  }

  .status-failed:hover {
    background: rgba(255, 77, 79, 0.15);
  }
}
</style>
