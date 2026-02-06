/**
* 消息项组件 - 重构版本
*
* 职责：包裹消息气泡，处理头像、多选、布局方向
* 消息内容的具体渲染由 MessageBubble 组件负责
*/
<template>
  <!-- 时间分割线 -->
  <div v-if="message.isTimeDivider" class="time-divider-wrapper">
    <span class="time-text">{{ message.timeText }}</span>
  </div>

  <!-- 消息主体 -->
  <div v-else class="message-item" :class="itemClasses" :data-id="message.id">
    <!-- 多选复选框 -->
    <div v-if="multiSelectMode" class="checkbox-wrapper">
      <el-checkbox :model-value="isSelected" @change="toggleSelection" @click.stop />
    </div>

    <!-- 头像区域 -->
    <div class="avatar-wrapper" title="右键@提及 | 查看资料 | 双击拍一拍" @contextmenu.prevent="$emit('at', message)"
      @click="$emit('show-user', message.senderId)" @dblclick="handleNudge">
      <DingtalkAvatar :src="message.senderAvatar" :name="message.senderName" :user-id="message.senderId" :size="40"
        shape="square" custom-class="message-avatar" />
    </div>

    <div class="content-wrapper" :class="{ 'is-merged': message.isMerged }">
      <!-- 发送者姓名（仅群聊、非自己、且非合并连续消息时显示） -->
      <div v-if="showSenderName" class="sender-name">
        {{ message.senderName }}
      </div>

      <!-- 消息气泡与状态行 -->
      <div class="bubble-row">
        <slot name="bubble">
          <MessageBubble :message="message" :session-type="sessionType" @command="$emit('command', $event, message)"
            @at="$emit('at', message)" @preview="$emit('preview', $event)" @download="$emit('download', $event)"
            @retry="$emit('retry', message)" @add-reaction="$emit('add-reaction', $event, message)"
            @re-edit="$emit('re-edit', $event)" @scroll-to="$emit('scroll-to', $event)"
            @long-press="$emit('long-press', $event)" @show-user="$emit('show-user', $event)" />
        </slot>

        <!-- 已读状态（仅发送方消息显示） -->
        <div v-if="message.isOwn" class="status-container">
          <slot name="read-status" />
        </div>
      </div>

      <!-- 消息页脚（时间） -->
      <div v-if="!hideFooter" class="message-footer">
        <span class="message-time">{{ formattedTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import MessageBubble from './MessageBubbleRefactored.vue'

const props = defineProps({
  message: { type: Object, required: true },
  multiSelectMode: { type: Boolean, default: false },
  sessionType: { type: String, default: 'PRIVATE' },
  hideFooter: { type: Boolean, default: false }
})

const emit = defineEmits([
  'reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user',
  'retry', 'toggle-select', 'nudge', 'preview', 'download',
  'add-reaction', 're-edit', 'long-press'
])

const store = useStore()

// ==================== 计算属性 ====================

const showSenderName = computed(() => {
  return props.sessionType === 'GROUP' && !props.message.isOwn && !props.message.isMerged
})

const itemClasses = computed(() => ({
  'is-own': props.message.isOwn,
  'is-multi-select': props.multiSelectMode,
  'is-merged': props.message.isMerged,
  'has-sender-name': showSenderName.value
}))

// 是否被选中
const isSelected = computed(() => {
  return store.state.im.message.selectedMessages.has(props.message.id)
})

// 格式化时间
const formattedTime = computed(() => {
  if (!props.message.timestamp) { return '' }
  const date = new Date(props.message.timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
})

// ==================== 事件处理 ====================

// 切换选中状态
const toggleSelection = () => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', props.message.id)
}

// 处理拍一拍
const handleNudge = () => {
  // 只对对方的消息生效
  if (!props.message.isOwn) {
    emit('nudge', props.message.senderId)
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px; // 野火IM标准:消息间距12px
  position: relative;
  padding: 0;
  transition: background var(--dt-transition-fast);
  animation: messageFadeIn 0.2s var(--dt-ease-out) both;

  &.is-own {
    flex-direction: row-reverse;
  }
}

// 使用全局 fadeInUp 动画 (@/styles/animations.scss)

// 时间分割线
.time-divider-wrapper {
  width: 100%;
  text-align: center;
  margin: 12px 0;
}

.time-text {
  background: var(--dt-bg-body);
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-xs);
  padding: 3px 10px;
  border-radius: var(--dt-radius-sm);
}

// 多选复选框
.checkbox-wrapper {
  display: flex;
  align-items: flex-start;
  padding-top: 8px;
  margin: 0 8px;
  flex-shrink: 0;
  width: 20px;

  :deep(.el-checkbox) {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }
  }
}

.message-item.has-sender-name {
  .avatar-wrapper {
    margin-top: 18px;
  }

  .checkbox-wrapper {
    padding-top: 26px;
  }
}

// 多选模式样式
.message-item.is-multi-select {
  padding: 0 8px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }
}

// 头像区域
.avatar-wrapper {
  width: 40px;
  height: 40px;
  margin: 0;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);
  display: flex;
  align-items: flex-start; // 改为顶部对齐,确保头像和气泡在一条直线上
  padding-top: 0; // 确保无额外padding

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: 4px; // 野火IM:方形头像,4px圆角
  }
}

// 内容包裹层
.content-wrapper {
  max-width: 85%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  margin: 0 12px; // 野火IM标准:头像与气泡间距12px
  padding-top: 0;

  &.is-merged {
    margin-top: -8px; // 钉钉标准:合并消息 -8px
  }
}

// 对方消息左对齐
.message-item:not(.is-own) .content-wrapper {
  align-items: flex-start;
}

// 自己消息右对齐
.message-item.is-own .content-wrapper {
  align-items: flex-end;

  .bubble-row {
    flex-direction: row-reverse;
  }
}

// 气泡与状态行布局
.bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  max-width: 100%;
}

.status-container {
  flex-shrink: 0;
  margin-bottom: 2px; // 对齐气泡底部稍微抬起一点
  align-self: flex-end;
}

// 发送者姓名样式
.sender-name {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin-bottom: 4px;
  margin-left: 4px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 消息页脚
.message-footer {
  display: flex;
  align-items: center;
  margin-top: 4px; // 钉钉标准：4px 间距
  font-size: var(--dt-font-size-xs);
}

.message-time {
  color: var(--dt-text-quaternary);
  font-variant-numeric: tabular-nums;
}

// 暗色模式适配
:global(.dark) {
  .time-text {
    background: var(--dt-bg-card-dark);
    color: var(--dt-text-secondary);
  }

  .message-item.is-multi-select:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
