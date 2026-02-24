<template>
  <div class="message-bubble-v2">
    <!-- 悬停操作栏 -->
    <MessageHoverActions
      v-if="!isSystem"
      :is-own="message.isOwn"
      @action="handleAction"
    />

    <div
      class="bubble-container"
      :class="[message.isOwn ? 'is-own' : 'is-other']"
    >
      <component
        :is="getBubbleComponent()"
        :message="message"
        @preview="$emit('preview', $event)"
        @re-edit="$emit('re-edit', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TextBubble from './message-bubble/bubbles/TextBubble.vue'
import ImageBubble from './message-bubble/bubbles/ImageBubble.vue'
import FileBubble from './message-bubble/bubbles/FileBubble.vue'
import VideoBubble from './message-bubble/bubbles/VideoBubble.vue'
import SystemBubble from './message-bubble/bubbles/SystemBubble.vue'
import RecalledBubble from './message-bubble/bubbles/RecalledBubble.vue'
import MessageHoverActions from './message-bubble/parts/MessageHoverActions.vue'

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['command', 'preview', 're-edit'])

const isSystem = computed(() => props.message.type === 'SYSTEM' || props.message.isRevoked)

const getBubbleComponent = () => {
  const type = props.message.type?.toUpperCase()
  if (props.message.isRevoked) return RecalledBubble
  if (type === 'IMAGE') return ImageBubble
  if (type === 'FILE') return FileBubble
  if (type === 'VIDEO') return VideoBubble
  if (type === 'SYSTEM') return SystemBubble
  return TextBubble
}

const handleAction = action => emit('command', action, props.message)
</script>

<style scoped lang="scss">
.message-bubble-v2 {
  position: relative;
  max-width: 100%;
}

.bubble-container {
  position: relative;
  padding: 12px 16px; // 提升至 12px 16px，增加丰盈度
  font-size: 15px;
  line-height: 1.6;
  min-height: 44px; // 相应提升最小高度
  min-width: 44px;
  border-radius: 10px; // 稍微增大圆角，视觉更柔和
  box-sizing: border-box;
  word-break: break-word;

  // 对方：纯白带灰边
  &.is-other {
    background: #FFFFFF;
    border: 1px solid #E5E6EB;
    color: #1D2129;
    
    // 钉钉风格尖角
    &::before {
      content: ''; position: absolute;
      left: -6px; top: 14px; // 向下微调以对齐新的高度
      border-top: 5px solid transparent;
      border-bottom: 5px solid transparent;
      border-right: 6px solid #FFFFFF;
      z-index: 2;
    }
    &::after {
      content: ''; position: absolute;
      left: -7px; top: 14px;
      border-top: 5px solid transparent;
      border-bottom: 5px solid transparent;
      border-right: 6px solid #E5E6EB;
      z-index: 1;
    }
  }

  // 己方：品牌蓝
  &.is-own {
    background: #165DFF;
    color: #FFFFFF;
    border: none;

    &::before {
      content: ''; position: absolute;
      right: -6px; top: 14px;
      border-top: 5px solid transparent;
      border-bottom: 5px solid transparent;
      border-left: 6px solid #165DFF;
    }

    // 强制子元素白字
    :deep(*) { color: #FFFFFF !important; }
  }
}

// 媒体类特殊处理：去掉尖角
.bubble-container:has(.image-bubble),
.bubble-container:has(.video-bubble) {
  padding: 0;
  background: transparent !important;
  border: none !important;
  &::before, &::after { display: none !important; }
  :deep(img), :deep(video) { border-radius: 8px; border: 1px solid #E5E6EB; }
}

.bubble-container:active { transform: scale(0.98); }
</style>
