<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading" class="loading-status">加载中...</div>
    
    <div class="message-flow">
      <template v-for="(msg, index) in messages" :key="msg.id || index">
        <!-- 简单的时间分割线 -->
        <div v-if="showTimeDivider(msg, index)" class="time-divider">
          {{ formatTime(msg.timestamp) }}
        </div>

        <MessageItem
          :message="msg"
          @show-user="(uid) => $emit('show-user', uid)"
          @retry="(msg) => $emit('retry', msg)"
        >
          <template #bubble>
            <MessageBubble :message="msg" />
          </template>
        </MessageItem>
      </template>
    </div>

    <!-- 极简的新消息提示 -->
    <div v-if="showScrollBottom" class="new-msg-tip" @click="scrollToBottom">
      有新消息
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({ messages: Array, loading: Boolean })
const emit = defineEmits(['load-more', 'show-user', 'retry'])

const listRef = ref(null)
const showScrollBottom = ref(false)

const showTimeDivider = (msg, index) => {
  if (index === 0) return true
  const prevMsg = props.messages[index - 1]
  const msgDate = new Date(msg.timestamp).toDateString()
  const prevDate = new Date(prevMsg.timestamp).toDateString()
  // 不同日期或间隔超过5分钟都显示分割线
  return msgDate !== prevDate || msg.timestamp - prevMsg.timestamp > 5 * 60 * 1000
}

const formatTime = (ts) => {
  const d = new Date(ts)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const msgDay = new Date(d.getFullYear(), d.getMonth(), d.getDate())

  const timeStr = `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`

  if (msgDay.getTime() === today.getTime()) {
    return timeStr
  } else if (msgDay.getTime() === yesterday.getTime()) {
    return `昨天 ${timeStr}`
  } else if (d.getFullYear() === now.getFullYear()) {
    return `${d.getMonth() + 1}月${d.getDate()}日 ${timeStr}`
  } else {
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${timeStr}`
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  showScrollBottom.value = scrollHeight - scrollTop - clientHeight > 300
  if (scrollTop === 0 && !props.loading) emit('load-more')
}

watch(() => props.messages?.length, () => {
  if (!showScrollBottom.value) scrollToBottom()
})

onMounted(() => scrollToBottom())
defineExpose({ scrollToBottom })
</script>

<style scoped lang="scss">
.message-list {
  flex: 1;
  overflow-y: auto;
  background: var(--dt-bg-chat);
  padding: 20px 0;
  position: relative;
}

.message-flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.time-divider {
  text-align: center;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-quaternary);
  margin: 8px 0;
}

.loading-status {
  text-align: center;
  padding: 10px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.new-msg-tip {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: var(--dt-brand-color);
  color: #ffffff;
  padding: 6px 16px;
  border-radius: var(--dt-radius-full);
  font-size: var(--dt-font-size-sm);
  cursor: pointer;
  box-shadow: var(--dt-shadow-2);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-1px);
  }
}
</style>
