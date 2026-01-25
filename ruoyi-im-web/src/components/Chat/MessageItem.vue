<template>
  <div
    class="message-item message-enter"
    :class="{ 'is-own': message.isOwn }"
  >
    <!-- 头像 -->
    <div
      class="avatar-container"
      @contextmenu.prevent="$emit('at', message)"
      @click="$emit('show-user', message.senderId)"
      title="右键 @提及，左键查看资料"
    >
      <DingtalkAvatar
        :src="avatarUrl"
        :name="message.senderName"
        :user-id="message.senderId"
        :size="36"
        shape="square"
        custom-class="message-avatar"
      />
    </div>

    <div class="content-wrapper">
      <!-- 发送者姓名 -->
      <div v-if="!message.isOwn" class="sender-name">{{ message.senderName }}</div>

      <!-- 回复消息预览 -->
      <div v-if="message.replyTo" class="reply-wrapper" @click="$emit('scroll-to', message.replyTo.id)">
        <span class="reply-sender">{{ message.replyTo.senderName }}: </span>
        <span class="reply-content">{{ message.replyTo.content }}</span>
      </div>

      <div class="message-content-main relative">
        <!-- 悬停快捷按钮 -->
        <div class="message-actions">
          <button class="action-btn" @click="$emit('reply', message)" title="回复">
            <el-icon><ChatLineSquare /></el-icon>
          </button>
          <button class="action-btn" @click="$emit('reaction', message, '👍')" title="点赞">
            <span>👍</span>
          </button>
          <el-dropdown trigger="click" @command="(cmd) => $emit('command', cmd, message)">
             <button class="action-btn" title="更多">
               <el-icon><MoreFilled /></el-icon>
             </button>
             <template #dropdown>
               <el-dropdown-menu>
                 <el-dropdown-item command="forward"><el-icon><Share /></el-icon> <span>转发</span></el-dropdown-item>
                 <el-dropdown-item command="copy" v-if="message.type === 'TEXT'"><el-icon><CopyDocument /></el-icon> <span>复制</span></el-dropdown-item>
               </el-dropdown-menu>
             </template>
          </el-dropdown>
        </div>

        <!-- 消息气泡组件 -->
        <slot name="bubble"></slot>
      </div>

        <!-- 消息页脚 (状态与时间) -->
        <div class="message-footer">
          <div v-if="message.isOwn" class="status-container">
            <!-- 发送中 / 上传中 -->
            <el-icon v-if="['sending', 'uploading'].includes(message.status)" class="is-loading status-icon" :title="message.status === 'uploading' ? '上传中...' : '发送中...'"><Loading /></el-icon>
            
            <!-- 发送失败 -->
            <el-icon v-else-if="message.status === 'failed'" class="status-icon error" title="发送失败，点击重试" @click="$emit('retry', message)">
              <WarningFilled />
            </el-icon>

            <!-- 已读状态 (钉钉风格) -->
            <div v-else class="read-status" :class="{ 'read': message.readCount > 0, 'unread': message.readCount === 0 }">
              {{ message.readCount > 0 ? '已读' : '未读' }}
            </div>
          </div>
          <div class="time">{{ formattedTime }}</div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChatLineSquare, MoreFilled, Share, CopyDocument, Loading, WarningFilled } from '@element-plus/icons-vue'
import { addTokenToUrl } from '@/utils/file'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user'])

const avatarUrl = computed(() => addTokenToUrl(props.message.senderAvatar))

const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  return new Date(props.message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})
</script>

<style scoped lang="scss">
.message-item {
  display: flex;
  margin-bottom: 24px;
  position: relative;

  &.is-own {
    flex-direction: row-reverse;
  }
}

.avatar-container {
  margin: 0 8px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s;
  &:hover { transform: scale(1.05); }
}

.content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.sender-name {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  padding-left: 4px;
}

.is-own .sender-name {
  text-align: right;
  padding-right: 4px;
}

.message-content-main {
  &:hover {
    .message-actions {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.message-actions {
  position: absolute;
  top: -32px;
  left: 0;
  display: flex;
  background: #fff;
  border: 1px solid var(--dt-border-light);
  border-radius: 20px;
  padding: 2px 8px;
  box-shadow: var(--dt-shadow-md);
  opacity: 0;
  transform: translateY(5px);
  transition: all 0.2s ease-in-out;
  z-index: 10;
  gap: 4px;

  .action-btn {
    background: transparent;
    border: none;
    padding: 2px 6px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    font-size: 16px;
    display: flex;
    align-items: center;
    border-radius: 10px;

    &:hover {
      background-color: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }

    span { font-size: 14px; }
  }
}

.is-own .message-actions {
  left: auto;
  right: 0;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  min-height: 16px; 
}

.read-status {
  font-size: 11px;
  cursor: default;
  user-select: none;
  
  &.unread {
    color: #1677ff;
    font-weight: 500;
  }
  
  &.read {
    color: #8f959e;
  }
}

.status-container {
  display: flex;
  align-items: center;
}

.status-icon {
  font-size: 14px;
  color: #8c8c8c;
  
  &.error {
    color: #ff4d4f;
    cursor: pointer;
  }
}

.is-own .message-footer {
  flex-direction: row-reverse;
}

.time {
  color: #bfbfbf;
}

.reply-wrapper {
  background: #f4f6f8;
  border-left: 3px solid #0089ff;
  padding: 6px 10px;
  border-radius: 4px;
  margin-bottom: 6px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.2s;
  
  .dark & {
    background: rgba(255, 255, 255, 0.05);
    color: #94a3b8;
  }

  &:hover {
    background: #ebf4ff;
    .dark & { background: rgba(255, 255, 255, 0.08); }
  }

  .reply-sender {
    color: #1f2329;
    font-weight: 600;
    .dark & { color: #f1f5f9; }
  }
}

.is-own .reply-wrapper {
  background: rgba(0, 0, 0, 0.05);
  align-self: flex-end;
}

.dark .message-actions {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

/* 消息进入动画 - 自己发送的消息 */
.message-item.message-enter.is-own {
  animation: bounceInRight 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

/* 消息进入动画 - 他人发送的消息 */
.message-item.message-enter:not(.is-own) {
  animation: slideInLeft 0.3s ease-out;
}

@keyframes bounceInRight {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 支持无障碍访问 */
@media (prefers-reduced-motion: reduce) {
  .message-item.message-enter {
    animation: none !important;
  }
}
</style>
