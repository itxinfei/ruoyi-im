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
                       <el-dropdown-item command="forward">转发</el-dropdown-item>
                       <el-dropdown-item command="copy" v-if="message.type === 'TEXT'">复制</el-dropdown-item>
                       <el-dropdown-item command="multi-select">多选</el-dropdown-item>
                       <el-dropdown-item command="todo">设为待办</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
               </el-dropdown>
            </div>
          </div>

          <!-- 消息气泡内容插槽 -->
          <slot name="bubble"></slot>
          
          <!-- 上传进度条 (仅图片/文件上传时显示) -->
          <div v-if="message.status === 'uploading' && message.uploadProgress" class="upload-progress-bar">
            <div class="progress-fill" :style="{ width: message.uploadProgress + '%' }"></div>
            <span class="progress-text">{{ message.uploadProgress }}%</span>
          </div>
        </div>

        <!-- 消息页脚 (状态与时间) -->
        <div class="message-footer">
          <div v-if="message.isOwn" class="status-container">
            <!-- 发送中状态 -->
            <div v-if="message.status === 'sending'" class="status-sending">
              <el-icon class="is-loading status-icon"><Loading /></el-icon>
              <span class="status-text">发送中</span>
            </div>
            
            <!-- 上传中状态 -->
            <div v-else-if="message.status === 'uploading'" class="status-uploading">
              <el-icon class="is-loading status-icon"><Loading /></el-icon>
              <span class="status-text">上传中</span>
            </div>

            <!-- 发送失败状态 -->
            <div v-else-if="message.status === 'failed'" class="status-failed" @click="$emit('retry', message)">
              <el-icon class="status-icon"><WarningFilled /></el-icon>
              <span class="status-text">发送失败</span>
              <span class="retry-hint">点击重试</span>
            </div>

            <!-- 已读状态 (钉钉风格) -->
            <div v-else class="read-status" :class="{ 'is-read': message.readCount > 0 }">
              <el-icon v-if="message.readCount > 0" class="read-icon"><Check /></el-icon>
              <span>{{ message.readCount > 0 ? '已读' : '未读' }}</span>
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
import { ChatLineSquare, MoreFilled, Loading, WarningFilled, Check } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  message: { type: Object, required: true }
})

defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user', 'retry', 'multi-select'])

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
  display: flex;
  margin-bottom: 16px;
  position: relative;
  padding: 0 16px;
  transition: opacity 0.2s;

  &.is-own {
    flex-direction: row-reverse;
  }

  // 发送中状态样式
  &.is-sending {
    opacity: 0.85;
    .bubble {
      opacity: 0.9;
    }
  }

  // 发送失败状态样式
  &.is-failed {
    .bubble {
      border: 1px solid #ff4d4f;
      box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
    }
  }

  // 新消息入场动画
  &.message-enter {
    animation: messageSlideIn 0.3s ease-out;
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ============================================================================
// 时间分隔线
// ============================================================================
.time-divider {
  width: 100%;
  text-align: center;
  margin: 12px 0;

  .time-text {
    background: rgba(0, 0, 0, 0.04);
    color: #8f959e;
    font-size: 11px;
    padding: 2px 10px;
    border-radius: 10px;
    display: inline-block;
  }
}

// ============================================================================
// 头像区域
// ============================================================================
.avatar-container {
  margin: 0 10px;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity 0.2s;
  position: relative;

  &:hover {
    opacity: 0.85;
  }

  // 发送中状态
  &.avatar-sending {
    &::after {
      content: '';
      position: absolute;
      inset: -2px;
      border: 2px solid #1677ff;
      border-radius: 8px;
      animation: pulse-ring 1.5s ease-out infinite;
    }
  }
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
}

@keyframes pulse-ring {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

// ============================================================================
// 内容区域
// ============================================================================
.content-wrapper {
  max-width: 85%;
  display: flex;
  flex-direction: column;
}

.sender-name {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  padding: 0 4px;
}

.is-own .sender-name {
  text-align: right;
}

.message-content-main {
  position: relative;

  &:hover .message-actions-floating {
    opacity: 1;
    transform: translateY(0);
  }
}

// ============================================================================
// 悬停操作栏 (钉钉风格)
// ============================================================================
.message-actions-floating {
  position: absolute;
  top: -32px;
  left: 0;
  opacity: 0;
  transform: translateY(4px);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;

  .action-bar-min {
    display: flex;
    gap: 2px;
    padding: 4px 8px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #f0f1f2;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .mini-btn {
      background: none;
      border: none;
      padding: 6px;
      color: #646a73;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      font-size: 14px;
      transition: all 0.15s;

      &:hover {
        background: #e6f4ff;
        color: var(--dt-brand-color);
      }
    }
  }
}

.is-own .message-actions-floating {
  left: auto;
  right: 0;
}

// ============================================================================
// 上传进度条
// ============================================================================
.upload-progress-bar {
  position: relative;
  height: 4px;
  background: #e5e6eb;
  border-radius: 2px;
  margin-top: 8px;
  overflow: hidden;

  .progress-fill {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    background: linear-gradient(90deg, var(--dt-brand-color), #4096ff);
    border-radius: 2px;
    transition: width 0.3s ease;
  }

  .progress-text {
    position: absolute;
    right: 0;
    top: 8px;
    font-size: 10px;
    color: #8f959e;
  }
}

// ============================================================================
// 消息页脚 (状态与时间)
// ============================================================================
.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
}

.is-own .message-footer {
  flex-direction: row-reverse;
}

.status-container {
  display: flex;
  align-items: center;
}

// 发送中状态
.status-sending {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--dt-brand-color);

  .status-icon {
    font-size: 12px;
  }

  .status-text {
    font-size: 11px;
  }
}

// 上传中状态
.status-uploading {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--dt-brand-color);

  .status-icon {
    font-size: 12px;
  }

  .status-text {
    font-size: 11px;
  }
}

// 发送失败状态
.status-failed {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--dt-error-color);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.15s;

  &:hover {
    background: var(--dt-error-bg);
  }

  .status-icon {
    font-size: 12px;
  }

  .status-text {
    font-size: 11px;
    font-weight: 500;
  }

  .retry-hint {
    font-size: 10px;
    opacity: 0.8;
  }
}

// 已读状态 (钉钉风格)
.read-status {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--dt-unread-color);
  transition: color 0.2s;

  &.is-read {
    color: var(--dt-read-color);
  }

  .read-icon {
    font-size: 12px;
  }
}

.time {
  color: #bfbfbf;
  font-size: 11px;
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
