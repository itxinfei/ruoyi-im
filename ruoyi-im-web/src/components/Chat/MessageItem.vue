<template>
  <div
    class="message-item message-enter"
    :class="{ 'is-own': message.isOwn, 'is-multi-select': multiSelectMode }"
  >
    <!-- 时间分割线 (如果是时间消息) -->
    <div v-if="message.isTimeDivider" class="time-divider">
      <span class="time-text">{{ message.timeText }}</span>
    </div>

    <template v-else>
      <!-- 多选复选框 -->
      <div v-if="multiSelectMode" class="checkbox-container">
        <el-checkbox
          :model-value="isSelected"
          @change="handleCheckboxChange"
          @click.stop
        />
      </div>

      <!-- 头像 -->
      <div
        class="avatar-container"
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
      </div>

      <div class="content-wrapper">
        <!-- 发送者姓名 -->
        <div v-if="!message.isOwn" class="sender-name">{{ message.senderName }}</div>

        <div class="message-content-main">
          <!-- 悬停快捷按钮区 (还原钉钉微交互) -->
          <div class="message-actions-floating" v-if="message.type !== 'RECALLED'">
            <div class="action-bar-min">
               <el-tooltip content="回复" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reply', message)"><el-icon><ChatLineSquare /></el-icon></button>
               </el-tooltip>
               <el-tooltip content="点赞" placement="top" :show-after="400">
                  <button class="mini-btn" @click="$emit('reaction', message, '👍')">👍</button>
               </el-tooltip>
            </div>
          </div>

          <!-- 消息气泡内容插槽 -->
          <slot name="bubble"></slot>
        </div>

        <!-- 消息页脚 (状态与时间) -->
        <div class="message-footer">
          <div v-if="message.isOwn" class="status-container">
            <!-- 发送中 / 上传中 -->
            <el-icon v-if="['sending', 'uploading'].includes(message.status)" class="is-loading status-icon"><Loading /></el-icon>
            
            <!-- 发送失败 -->
            <el-icon v-else-if="message.status === 'failed'" class="status-icon error" title="发送失败，点击重试" @click="$emit('retry', message)">
              <WarningFilled />
            </el-icon>

            <!-- 已读状态 (钉钉风格：不分人数显示，仅显示已读/未读) -->
            <div v-else class="read-status" :class="{ 'read': message.readCount > 0, 'unread': message.readCount === 0 }">
              {{ message.readCount > 0 ? '已读' : '未读' }}
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
import { useStore } from 'vuex'
import { ChatLineSquare, Loading, WarningFilled } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const store = useStore()

const props = defineProps({
  message: { type: Object, required: true },
  multiSelectMode: { type: Boolean, default: false }
})

const emit = defineEmits(['reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user', 'retry', 'toggle-select'])

// 消息是否被选中
const isSelected = computed(() => {
  return store.state.im.message.selectedMessages.has(props.message.id)
})

// 处理复选框变化
const handleCheckboxChange = () => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
}

const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  const date = new Date(props.message.timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-item {
  display: flex;
  margin-bottom: 20px;
  position: relative;
  padding: 0 16px;
  animation: fadeInUp 0.3s var(--dt-ease-out) both;

  &.is-own {
    flex-direction: row-reverse;
  }
}

.time-divider {
  width: 100%;
  text-align: center;
  margin: 12px 0;

  .time-text {
    background: var(--dt-bg-body);
    color: var(--dt-text-tertiary);
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 4px;
  }
}

// 多选复选框容器
.checkbox-container {
  display: flex;
  align-items: flex-start;
  padding-top: 8px;
  margin: 0 8px;
  flex-shrink: 0;

  :deep(.el-checkbox) {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }
  }
}

// 多选模式下的消息项样式
.message-item.is-multi-select {
  padding: 0 8px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.is-own {
    flex-direction: row-reverse;
  }
}

.avatar-container {
  margin: 0 10px;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: 4px;
  }
}

.content-wrapper {
  max-width: 85%;
  display: flex;
  flex-direction: column;
}

.sender-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
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

.message-actions-floating {
  position: absolute;
  top: -28px;
  left: 0;
  opacity: 0;
  transform: translateY(4px);
  transition: all var(--dt-transition-fast);
  z-index: 10;

  .action-bar-min {
    display: flex;
    gap: 2px;
    padding: 3px 6px;
    background: var(--dt-bg-card);
    border-radius: 6px;
    border: 1px solid var(--dt-border-light);
    box-shadow: var(--dt-shadow-2);
    backdrop-filter: blur(8px);

    .mini-btn {
      background: none;
      border: none;
      padding: 5px;
      color: var(--dt-text-secondary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      font-size: 14px;
      transition: all var(--dt-transition-fast);

      &:hover {
        background: var(--dt-bg-hover);
        color: var(--dt-brand-color);
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.is-own .message-actions-floating {
  left: auto;
  right: 0;
}

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

.read-status {
  &.unread {
    color: var(--dt-brand-color);
  }
  &.read {
    color: var(--dt-text-quaternary);
  }
}

.status-icon {
  font-size: 14px;
  color: var(--dt-text-secondary);
  transition: color var(--dt-transition-fast);

  &.error {
    color: var(--dt-error-color);
    cursor: pointer;

    &:hover {
      color: #d9363e;
    }
  }
}

.time {
  color: var(--dt-text-quaternary);
  font-variant-numeric: tabular-nums;
}

:global(.dark) {
  .message-actions-floating .action-bar-min {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .mini-btn:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
