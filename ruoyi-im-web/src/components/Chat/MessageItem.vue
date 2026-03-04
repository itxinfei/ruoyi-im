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
                       <el-dropdown-item command="edit-history" v-if="message.isEdited === 1 && message.type === 'TEXT'">
                         <el-icon><Document /></el-icon> 编辑历史
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
import { ChatLineSquare, MoreFilled, Loading, WarningFilled, View, Document } from '@element-plus/icons-vue'
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
  return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
})
</script>

<style scoped lang="scss">
// ============================================================================
// 消息项容器 - 强制布局血统
// ============================================================================
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-item {
  display: flex !important;
  width: 100% !important;
  margin-bottom: var(--dt-spacing-lg) !important;
  position: relative;
  padding: 0 var(--dt-spacing-lg);
  box-sizing: border-box;
  transition: opacity var(--dt-transition-base);

  &.is-own {
    flex-direction: row-reverse !important;
    justify-content: flex-start !important;
  }
}

// ============================================================================
// 时间分割线
// ============================================================================
.time-divider {
  width: 100%;
  text-align: center;
  margin: var(--dt-spacing-md) 0;

  .time-text {
    background: var(--dt-brand-lighter);
    color: var(--dt-text-tertiary);
    font-size: var(--dt-font-size-xs);
    padding: 2px var(--dt-spacing-sm);
    border-radius: var(--dt-radius-full);
    display: inline-block;
  }
}

// ============================================================================
// 头像区域 - 头像顶部与气泡文本首行对齐 (钉钉红线)
// ============================================================================
.avatar-container {
  flex-shrink: 0;
  cursor: pointer;
  align-self: flex-start;
  position: relative;
  margin-top: 2px; 
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: var(--dt-bg-mask);
  border-radius: var(--dt-radius-sm);
  @include flex-center;
  color: #fff;
}

// ============================================================================
// 内容包装层 - 防崩防撑爆 (min-width: 0)
// ============================================================================
.content-wrapper {
  max-width: calc(100% - 110px);
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xs);
  margin: 0 var(--dt-spacing-md);
  align-items: flex-start !important;
  min-width: 0; 
}

.is-own .content-wrapper {
  align-items: flex-end !important;
  margin-left: var(--dt-spacing-12); // 给左侧预留操作空间
  margin-right: var(--dt-spacing-md);
}

.sender-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  margin-bottom: var(--dt-spacing-xs);
  margin-left: var(--dt-spacing-xs);
}

.is-own .sender-name {
  display: none; // 钉钉风格：己方右侧隐藏昵称
}

// ============================================================================
// 消息核心主区
// ============================================================================
.message-content-main {
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: var(--dt-spacing-sm);
  max-width: 100%;

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

// ============================================================================
// 悬停操作条 (微交互质感)
// ============================================================================
.message-actions-floating {
  opacity: 0;
  visibility: hidden;
  transition: all var(--dt-transition-fast);
  
  .action-bar-min {
    display: flex;
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-light);
    border-radius: var(--dt-radius-md);
    box-shadow: var(--dt-shadow-float);
    padding: 2px;
  }
}

.mini-btn {
  width: var(--dt-btn-height-sm);
  height: var(--dt-btn-height-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-base);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
    color: var(--dt-brand-color);
  }
}

// ============================================================================
// 消息页脚 (状态与时间)
// ============================================================================
.message-footer {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  margin-top: var(--dt-spacing-xs);
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
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  cursor: default;
  user-select: none;

  &.is-read {
    color: var(--dt-unread-color);
    font-weight: var(--dt-font-weight-medium);
  }
}

.status-loading-icon {
  font-size: 12px;
  color: var(--dt-brand-color);
}

.status-failed-icon {
  color: var(--dt-error-color);
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.time {
  color: var(--dt-text-quaternary);
  font-size: var(--dt-font-size-xs);
}

// ============================================================================
// 上传进度条
// ============================================================================
.upload-progress-bar {
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--dt-brand-lighter);
  border-radius: 1px;
  overflow: hidden;

  .progress-fill {
    height: 100%;
    background: var(--dt-brand-color);
    transition: width 0.3s;
  }
}

// ============================================================================
// 暗色模式适配
// ============================================================================
:global(.dark) {
  .message-actions-floating .action-bar-min {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .mini-btn:hover {
    background: var(--dt-bg-active-dark) !important;
  }

  .upload-progress-bar {
    background: var(--dt-bg-input-dark);
  }
}
</style>
