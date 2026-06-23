<template>
  <div ref="containerRef" class="v6-message-viewport custom-scrollbar" @scroll="handleScroll">
    <div class="v6-message-content">
      <div
        v-for="(message, index) in messages"
        :key="message.clientMsgId || message.messageId"
        class="v6-message-row-wrapper"
        :ref="el => setMessageRef(el, index)"
        :class="{
          'is-cluster': isCluster(index),
          'is-me': message.isSelf
        }"
      >
        <ChatMessageBubble
          :message="message"
          :is-me="message.isSelf"
          :is-grouped="isCluster(index)"
          :show-time="checkShowTime(index)"
          :is-selection-mode="isSelectionMode"
          :is-selected="isMessageSelected(message)"
          @select-message="$emit('select-message', $event)"
          @show-user="$emit('show-user', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
import { ref, watch, nextTick } from 'vue'
import ChatMessageBubble from '../ChatMessageBubble.vue'

const props = defineProps({
  messages: Array,
  isSelectionMode: Boolean,
  selectedMessages: Object
})

const emit = defineEmits(['load-more', 'select-message', 'show-user'])

const containerRef = ref(null)
const messageRefs = ref([])
const isLoadingMore = ref(false)
const lastScrollHeight = ref(0)

// 设置消息元素的 ref
const setMessageRef = (el, index) => {
  if (el) {
    messageRefs.value[index] = el
  }
}

// 核心：判断是否属于同一消息簇 (对齐钉钉 4px 逻辑)
const isCluster = (index) => {
  if (index === 0) return false
  const cur = props.messages[index]
  const prev = props.messages[index - 1]
  if (!cur || !prev) return false
  // 同一发送者且时间间隔小于 2 分钟
  return cur.senderId === prev.senderId &&
         (new Date(cur.sendTime) - new Date(prev.sendTime) < 120000)
}

// 核心：判断是否显示时间戳
const checkShowTime = (index) => {
  if (index === 0) return true
  const cur = props.messages[index]
  const prev = props.messages[index - 1]
  if (!cur || !prev) return false
  // 超过 5 分钟显示时间
  return new Date(cur.sendTime) - new Date(prev.sendTime) >= 300000
}

// 判断消息是否被选中
const isMessageSelected = (message) => {
  return props.selectedMessages?.has(message.messageId)
}

// 滚动事件处理
const handleScroll = async () => {
  const container = containerRef.value
  if (!container) return

  // 触顶加载更多历史消息
  if (container.scrollTop < 100 && !isLoadingMore.value && props.messages.length > 0) {
    isLoadingMore.value = true
    lastScrollHeight.value = container.scrollHeight

    // 触发加载更多
    emit('load-more')

    // 等待新消息渲染完成后恢复滚动位置
    await nextTick()
    // 保持滚动位置不变（新消息加载到上方）
    container.scrollTop = container.scrollHeight - lastScrollHeight.value

    isLoadingMore.value = false
  }
}

// 监听消息变化，自动滚动到底部（当发送新消息时）
watch(() => props.messages.length, async (newLen, oldLen) => {
  if (!containerRef.value) return

  if (newLen > oldLen) {
    const lastMessage = props.messages[props.messages.length - 1]
    // 如果是自身发送的消息，自动滚动到底部
    if (lastMessage && lastMessage.isSelf) {
      await nextTick()
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  }
})

// 暴露容器 ref 给父组件
defineExpose({
  containerRef
})
</script>

<style scoped lang="scss">
/* 钉钉规范：消息列表容器 */
.v6-message-viewport {
  flex: 1;
  overflow: visible;
  padding: var(--dt-spacing-md) 0;
  background-color: var(--dt-bg-card);

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-bg);
    border-radius: var(--dt-radius-xs);
    &:hover {
      background: var(--dt-scrollbar-thumb-bg-hover);
    }
  }
}

.v6-message-content {
  width: 100%;
}

/* 消息行间距 */
.v6-message-row-wrapper {
  padding: 0;
  margin-bottom: var(--dt-spacing-sm);  // 8px 间距

  /* 连续消息：压缩间距 */
  &.is-cluster {
    margin-top: -4px;  // 钉钉：连续消息压缩 -4px
    margin-bottom: 0;
  }
}
</style>
