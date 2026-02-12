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
        v-if="groupPosition === 'single' || groupPosition === 'first'"
        :src="message.senderAvatar"
        :name="message.senderName"
        :user-id="message.senderId"
        :size="40"
        shape="square"
        custom-class="message-avatar"
      />
    </div>

    <div
      class="content-wrapper"
      :class="{ 'is-merged': message.isMerged }"
    >
      <!-- 消息气泡与状态行 -->
      <div class="bubble-row">
        <!-- 发送者姓名（仅群聊、非自己、且非合并连续消息时显示） -->
        <!-- 使用绝对定位，避免影响气泡的布局和对齐 -->
        <div
          v-if="showSenderName"
          class="sender-name-absolute"
        >
          {{ message.senderName }}
        </div>
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
import MessageBubble from './MessageBubbleRefactored.vue'

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
  return props.sessionType === 'GROUP' && !props.message.isOwn && !props.message.isMerged
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
  align-items: center;
  min-height: 48px;
  gap: 10px; // 优化：10px 间距
  margin-bottom: 8px; // 钉钉标准：8px 消息间距
  position: relative;
  padding: 0;
  transition: all 0.2s var(--dt-ease-out);

  &.is-right {
    flex-direction: row-reverse;
  }

  &.has-sender-name {
    padding-top: 18px; // 钉钉标准：18px 顶部间距
  }

  &.is-merged {
    margin-bottom: 2px; // 钉钉标准：合并消息间距 2px
  }
}

.time-divider-wrapper {
  width: 100%;
  text-align: center;
  margin: 20px 0; // 优化：20px 上下间距
}

.time-text {
  background: var(--dt-bg-subtle-hover);
  color: var(--dt-text-quaternary);
  font-size: 11px;
  padding: 3px 10px; // 优化：3px 上下，10px 左右
  border-radius: 8px; // 优化：8px 圆角
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  margin: 0 12px;
  flex-shrink: 0;
  height: 40px;

  :deep(.el-checkbox__inner) {
    border-radius: 4px;
  }
}

.sender-name-absolute {
  position: absolute;
  top: -20px;
  left: 0;
  font-size: 12px; // 钉钉标准：12px
  color: var(--dt-text-tertiary); // 钉钉标准：使用次级文本色
  white-space: nowrap;
  max-width: 200px; // 钉钉标准：200px 最大宽度
  overflow: hidden;
  text-overflow: ellipsis;
  pointer-events: none;
  font-weight: 500;
  line-height: 1.4;
}

.message-item.is-multi-select {
  padding: 0 12px;
  cursor: pointer;
  border-radius: 16px;

  &:hover {
    background: var(--dt-bg-subtle-hover);
  }

  &.is-selected {
    background: var(--dt-brand-bg);
  }
}

.avatar-wrapper {
  width: 40px; // 钉钉标准：40px
  height: 40px; // 钉钉标准：40px
  flex-shrink: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px; // 钉钉标准：6px 圆角
  overflow: hidden;
  transition: all 0.2s ease;

  &.avatar-hidden {
    width: 0;
    height: 0;
    margin: 0;
    visibility: hidden;
    pointer-events: none;
    overflow: hidden;
  }

  :deep(.dingtalk-avatar) {
    border-radius: 6px !important;
  }
}

.content-wrapper {
  max-width: 60%; // 钉钉标准：60% 最大宽度
  display: flex;
  flex-direction: column;
  margin: 0 8px; // 钉钉标准：8px 边距
  position: relative;

  &.is-merged {
    margin-top: 0;
  }
}

.message-item:not(.is-right) .content-wrapper {
  align-items: flex-start;
}

.message-item.is-right .content-wrapper {
  align-items: flex-end;

  .bubble-row {
    flex-direction: row-reverse;
  }
}

.bubble-row {
  display: flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
}

.status-container {
  flex-shrink: 0;
  margin-bottom: 2px;
}

.message-footer {
  display: flex;
  align-items: center;
  margin-top: 4px; // 钉钉标准：4px 顶部间距
  padding: 0 4px; // 钉钉标准：4px 左右内边距
}

.message-time {
  color: var(--dt-text-quaternary); // 钉钉标准：使用四级文本色
  font-size: 11px; // 钉钉标准：11px
  font-family: var(--dt-font-family);
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
