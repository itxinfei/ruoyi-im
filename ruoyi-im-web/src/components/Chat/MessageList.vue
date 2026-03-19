<template>
  <div ref="listRef" class="message-list" @scroll="handleScroll">
    <div v-if="loading" class="loading-status">
      加载中...
    </div>

    <!-- 虚拟滚动容器 -->
    <div class="virtual-scroll-container" :style="{ height: containerHeight + 'px' }">
      <div class="virtual-scroll-spacer" :style="{ height: totalHeight + 'px' }" />
      <div class="virtual-scroll-content" :style="{ transform: `translateY(${offsetY}px)` }">
        <div
          v-for="(msg, index) in visibleMessages"
          :key="msg.id || msg.timestamp"
          class="message-item-wrapper"
        >
          <!-- 时间分割线 -->
          <div v-if="showTimeDivider(msg, getActualIndex(index))" class="time-divider">
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
        </div>
      </div>
    </div>

    <!-- 新消息提示 -->
    <div v-if="showScrollBottom && newMessageCount > 0" class="new-msg-tip" @click="scrollToBottom">
      <el-icon><ArrowDown /></el-icon>
      <span>{{ newMessageCount }} 条新消息</span>
    </div>
    <div v-else-if="showScrollBottom" class="new-msg-tip" @click="scrollToBottom">
      <el-icon><ArrowDown /></el-icon>
      <span>回到底部</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({ messages: Array, loading: Boolean })
const emit = defineEmits(['load-more', 'show-user', 'retry'])

const listRef = ref(null)
const showScrollBottom = ref(false)
const newMessageCount = ref(0)
// eslint-disable-next-line no-unused-vars
let lastMessageLength = 0

// 虚拟滚动相关状态
const containerHeight = ref(500) // 容器高度
const itemHeight = ref(80) // 默认消息项高度
const visibleCount = ref(10) // 可见消息数量
const startIndex = ref(0)
const endIndex = ref(0)
const offsetY = ref(0)

// 计算可视消息
const visibleMessages = ref([])
const totalHeight = ref(0)

// 消息高度缓存
const heightCache = new Map()

// 动态计算消息高度
const calculateMessageHeight = (message) => {
  // 根据消息类型和内容长度估算高度
  if (message.type === 'TEXT') {
    // 文本消息：根据内容长度计算
    const contentLength = message.content?.length || 0
    if (contentLength < 50) return 60
    if (contentLength < 100) return 80
    if (contentLength < 200) return 100
    return Math.min(200, 100 + Math.floor(contentLength / 20) * 10)
  } else if (message.type === 'IMAGE') {
    // 图片消息：固定高度
    return 200
  } else if (message.type === 'FILE') {
    // 文件消息：固定高度
    return 80
  } else if (message.type === 'LINK') {
    // 链接消息：固定高度
    return 120
  } else {
    // 其他类型消息：默认高度
    return 80
  }
}

const updateVirtualScroll = () => {
  if (!props.messages || !props.messages.length) {
    startIndex.value = 0
    endIndex.value = 0
    visibleMessages.value = []
    totalHeight.value = 0
    return
  }

  const container = listRef.value
  if (!container) return

  const scrollTop = container.scrollTop
  const clientHeight = container.clientHeight

  // 更新可见消息数量
  visibleCount.value = Math.ceil(clientHeight / itemHeight.value) + 10 // 额外加载10条用于缓冲

  // 计算可视范围
  const start = Math.floor(scrollTop / itemHeight.value)
  const end = Math.min(start + visibleCount.value, props.messages.length)

  startIndex.value = Math.max(0, start - 2) // 预加载前后2条
  endIndex.value = Math.min(props.messages.length, end + 2)

  // 计算偏移量（考虑前面消息的高度）
  offsetY.value = props.messages.slice(0, startIndex.value).reduce((total, msg) => {
    return total + calculateMessageHeight(msg)
  }, 0)

  // 获取可视消息
  visibleMessages.value = props.messages.slice(startIndex.value, endIndex.value)

  // 计算总高度
  totalHeight.value = props.messages.reduce((total, message) => {
    return total + calculateMessageHeight(message)
  }, 0)
}

const getActualIndex = (relativeIndex) => {
  return startIndex.value + relativeIndex
}

const getItemHeight = (index) => {
  const actualIndex = getActualIndex(index)
  if (actualIndex >= 0 && actualIndex < props.messages.length) {
    const message = props.messages[actualIndex]
    const height = calculateMessageHeight(message)
    heightCache.set(actualIndex, height) // 缓存计算的高度
    return height
  }
  return itemHeight.value
}

const showTimeDivider = (msg, absoluteIndex) => {
  if (absoluteIndex === 0) return true
  const prevMsg = props.messages[absoluteIndex - 1]
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
      newMessageCount.value = 0
    }
  })
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  showScrollBottom.value = scrollHeight - scrollTop - clientHeight > 300
  if (scrollTop === 0 && !props.loading) emit('load-more')

  // 更新虚拟滚动
  updateVirtualScroll()
}

watch(() => props.messages?.length, (newLength, oldLength) => {
  // 计算新增消息数量
  if (newLength > oldLength && oldLength !== undefined) {
    newMessageCount.value += (newLength - oldLength)
  }

  if (!showScrollBottom.value) {
    scrollToBottom()
    newMessageCount.value = 0
  }
  lastMessageLength = newLength

  // 更新虚拟滚动
  updateVirtualScroll()
})

onMounted(() => {
  // 初始化虚拟滚动
  updateVirtualScroll()
  scrollToBottom()
})
defineExpose({ scrollToBottom, listRef })
</script>

<style scoped lang="scss">
.message-list {
  flex: 1;
  overflow-y: auto;
  background: var(--dt-bg-chat);
  padding: var(--dt-spacing-lg) var(--dt-spacing-md);
  position: relative;
}

.message-flow {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.time-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--dt-spacing-md);
  margin: var(--dt-spacing-lg) 0;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-quaternary);

  &::before,
  &::after {
    content: '';
    flex: 1;
    max-width: 60px;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent,
      var(--dt-border-color) 20%,
      var(--dt-border-color) 80%,
      transparent
    );
  }
}

.loading-status {
  text-align: center;
  padding: var(--dt-spacing-md);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.new-msg-tip {
  position: absolute;
  bottom: var(--dt-spacing-lg);
  right: var(--dt-spacing-lg);
  background: var(--dt-brand-color);
  color: var(--dt-text-primary);
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  border-radius: var(--dt-radius-full);
  font-size: var(--dt-font-size-sm);
  cursor: pointer;
  box-shadow: var(--dt-shadow-2);
  transition: all var(--dt-transition-fast);
  display: flex;
  align-items: center;
  gap: 6px;

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-1px);
    box-shadow: var(--dt-shadow-3);
  }

  .el-icon {
    font-size: 14px;
  }
}
</style>
