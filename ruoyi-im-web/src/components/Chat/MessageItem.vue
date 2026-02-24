/**
* 消息项组件 - 重构版本
*
* 职责：包裹消息气泡，处理头像、多选、布局方向
* 消息内容的具体渲染由 MessageBubble 组件负责
*/
<template>
  <!-- 时间分割线 -->
  <div
    v-if="message.isTimeDivider"
    class="time-divider-wrapper"
  >
    <span class="time-text">{{ message.timeText }}</span>
  </div>

  <!-- 消息主体 -->
  <div
    v-else
    class="message-item"
    :class="itemClasses"
    :data-id="message.id"
  >
    <!-- 多选复选框 -->
    <div
      v-if="multiSelectMode"
      class="checkbox-wrapper"
    >
      <el-checkbox
        :model-value="isSelected"
        @change="toggleSelection"
        @click.stop
      />
    </div>

    <!-- 头像区域 -->
    <div
      class="avatar-wrapper"
      :class="{ 'avatar-hidden': groupPosition === 'middle' || groupPosition === 'last' }"
      title="右键@提及 | 查看资料 | 双击拍一拍"
      @contextmenu.prevent="$emit('at', message)"
      @click="$emit('show-user', message.senderId)"
      @dblclick="handleNudge"
    >
      <DingtalkAvatar
        :src="message.senderAvatar"
        :name="message.senderName"
        :user-id="message.senderId"
        :size="32"
        :shape="sessionType === 'GROUP' ? 'square' : 'circle'"
        custom-class="message-avatar"
      />
    </div>

    <div
      class="content-wrapper"
      :class="{ 'is-merged': message.isMerged }"
    >
      <!-- 发送者姓名（仅群聊、非自己、且非合并连续消息时显示） -->
      <div
        v-if="showSenderName"
        class="sender-name-container"
      >
        {{ message.senderName }}
      </div>

      <!-- 消息气泡与状态行 -->
      <div class="bubble-row">
        <slot name="bubble">
          <MessageBubble
            :message="message"
            :session-type="sessionType"
            :group-position="groupPosition"
            @command="$emit('command', $event, message)"
            @at="$emit('at', message)"
            @preview="$emit('preview', $event)"
            @download="$emit('download', $event)"
            @retry="$emit('retry', message)"
            @add-reaction="$emit('add-reaction', $event, message)"
            @re-edit="$emit('re-edit', $event)"
            @scroll-to="$emit('scroll-to', $event)"
            @long-press="$emit('long-press', $event)"
            @show-user="$emit('show-user', $event)"
          />
        </slot>

        <!-- 已读状态（仅发送方消息显示） -->
        <div
          v-if="message.isOwn"
          class="status-container"
        >
          <slot name="read-status" />
        </div>
      </div>

      <!-- 消息页脚（时间） -->
      <div
        v-if="!hideFooter"
        class="message-footer"
      >
        <span class="message-time">{{ formattedTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({
  message: { type: Object, required: true },
  multiSelectMode: { type: Boolean, default: false },
  sessionType: { type: String, default: 'PRIVATE' },
  hideFooter: { type: Boolean, default: false },
  groupPosition: { type: String, default: 'single' } // 'single' | 'first' | 'middle' | 'last'
})

const emit = defineEmits([
  'reply', 'reaction', 'command', 'scroll-to', 'at', 'show-user',
  'retry', 'toggle-select', 'nudge', 'preview', 'download',
  'add-reaction', 're-edit', 'long-press'
])

const store = useStore()

// ==================== 计算属性 ====================

const showSenderName = computed(() => {
  // 钉钉标准：仅群聊、非自己、且是消息组的第一条或单条消息时显示名字
  return props.sessionType === 'GROUP' && 
         !props.message.isOwn && 
         (props.groupPosition === 'first' || props.groupPosition === 'single')
})

const itemClasses = computed(() => ({
  'is-right': props.message.isOwn,
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
  min-height: 32px;
  gap: 8px;
  margin-bottom: 4px; // 钉钉标准间距
  position: relative;
  padding: 0;
  transition: background-color 0.2s ease;

  &.is-right {
    flex-direction: row-reverse;
  }

  &.is-merged {
    margin-bottom: 2px; // 合并消息间距更小
  }
}

.avatar-wrapper {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;

  &.avatar-hidden {
    opacity: 0;
    pointer-events: none;
    // 关键：保持高度不塌陷，防止抖动
  }

  :deep(.dingtalk-avatar) {
    border-radius: 4px;
  }
}

.content-wrapper {
  max-width: 70%; // 钉钉标准
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sender-name-container {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  margin-bottom: 2px;
  margin-left: 2px;
  font-weight: 500;
}

.message-item.is-right .sender-name-container {
  text-align: right;
  margin-right: 2px;
}

.bubble-row {
  display: flex;
  align-items: flex-end; // 对齐气泡底部
  gap: 6px;
}

.message-item.is-right .bubble-row {
  flex-direction: row-reverse;
}

.status-container {
  flex-shrink: 0;
  margin-bottom: 4px; // 微调与气泡底部的对齐
}

.message-footer {
  display: flex;
  align-items: center;
  margin-top: 2px;
  padding: 0 4px;
}

.message-time {
  color: var(--dt-text-quaternary);
  font-size: 10px;
}

// 暗色模式（统一使用 :global(.dark) 选择器）
:global(.dark) {
  .time-text {
    background: var(--dt-white-10);
  }
  
  .message-item.is-multi-select:hover {
    background: var(--dt-white-05);
  }
}
</style>
