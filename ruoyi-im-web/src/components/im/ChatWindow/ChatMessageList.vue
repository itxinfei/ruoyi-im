<template>
  <div ref="containerRef" class="v6-message-viewport custom-scrollbar" @scroll="handleScroll">
    <div :style="wrapperProps" class="v6-message-content">
      <div
        v-for="item in list"
        :key="item.data.clientMsgId || item.data.messageId"
        class="v6-message-row-wrapper"
        :class="{ 
          'is-cluster': isCluster(item.index),
          'is-me': item.data.isSelf
        }"
      >
        <ChatMessageBubble
          :message="item.data"
          :is-me="item.data.isSelf"
          :is-grouped="isCluster(item.index)"
          :show-time="checkShowTime(item.index)"
          :is-selection-mode="isSelectionMode"
          :is-selected="isMessageSelected(item.data)"
          @select-message="$emit('select-message', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
import { useVirtualList } from '@vueuse/core'
import ChatMessageBubble from '../ChatMessageBubble.vue'

const props = defineProps({
  messages: Array,
  isSelectionMode: Boolean,
  selectedMessages: Object
})

// 核心：判断是否属于同一消息簇 (对齐钉钉 4px 逻辑)
const isCluster = (index) => {
  if (index === 0) return false
  const cur = props.messages[index]
  const prev = props.messages[index - 1]
  // 同一发送者且时间间隔小于 2 分钟
  return cur.senderId === prev.senderId && 
         (new Date(cur.sendTime) - new Date(prev.sendTime) < 120000)
}

const { list, containerRef, wrapperProps } = useVirtualList(
  computed(() => props.messages),
  { itemHeight: 80, overscan: 10 }
)
</script>

<style scoped lang="scss">
.v6-message-viewport {
  flex: 1; overflow-y: auto; padding: 20px 0 0;
  background-color: #ffffff; // Slack 纯白背景
}

.v6-message-content {
  width: 100%; max-width: 1000px; margin: 0 auto;
}

.v6-message-row-wrapper {
  padding: 0;
  margin-bottom: 12px; // 不同用户消息间的自然段落间距
  
  &.is-cluster {
    margin-top: -8px; // 同一用户连续发言的紧凑间距
    margin-bottom: 4px;
  }
}
</style>
