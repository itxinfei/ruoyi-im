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
const emit = defineEmits(['load-more', 'show-user'])

const listRef = ref(null)
const showScrollBottom = ref(false)

const showTimeDivider = (msg, index) => {
  if (index === 0) return true
  const prevMsg = props.messages[index - 1]
  return msg.timestamp - prevMsg.timestamp > 5 * 60 * 1000 // 5分钟间隔
}

const formatTime = (ts) => {
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
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
  background: #ffffff;
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
  font-size: 12px;
  color: #bfbfbf;
  margin: 8px 0;
}

.loading-status {
  text-align: center;
  padding: 10px;
  font-size: 12px;
  color: #999;
}

.new-msg-tip {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: #1677ff;
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
</style>
