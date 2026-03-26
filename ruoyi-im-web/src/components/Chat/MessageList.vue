<template>
  <div ref="listRef" class="message-list" @scroll="handleScroll">
    <div v-if="jumping" class="jumping-status">
      正在定位消息...
    </div>
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
          :class="getSpacingClass(getActualIndex(index))"
        >
          <!-- 时间分割线 -->
          <div v-if="showTimeDivider(msg, getActualIndex(index))" class="time-divider">
            {{ formatTime(msg.timestamp) }}
          </div>

          <MessageItem
            :message="msg"
            :session-type="sessionType"
            :highlighted="highlightedId === (msg.messageId || msg.id)"
            @show-user="(uid) => $emit('show-user', uid)"
            @retry="(msg) => $emit('retry', msg)"
            @command="(c, m) => $emit('command', c, m)"
            @context="handleContextMenu"
          >
            <template #bubble>
              <MessageBubble :message="msg" @jump="(id) => $emit('jump', id)" />
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

    <!-- 右键菜单 -->
    <teleport to="body">
      <div
        v-if="contextMenu.show"
        class="msg-context-menu"
        :style="contextMenuStyle"
        @click.stop
      >
        <!-- 表情回应 -->
        <div class="menu-item emoji-row">
          <span
            v-for="emoji in quickEmojis"
            :key="emoji"
            class="emoji-btn"
            @click="handleQuickReaction(emoji)"
          >{{ emoji }}</span>
          <el-popover
            placement="top"
            :width="200"
            trigger="click"
            popper-class="emoji-picker-popper"
          >
            <template #reference>
              <span class="emoji-btn more">+</span>
            </template>
            <div class="emoji-grid">
              <span
                v-for="e in allEmojis"
                :key="e"
                class="emoji-option"
                @click="handleQuickReaction(e)"
              >{{ e }}</span>
            </div>
          </el-popover>
        </div>
        <div class="menu-divider" />
        <div class="menu-item" @click="emitMenuCommand('reply')">
          回复
        </div>
        <div class="menu-item" @click="emitMenuCommand('copy')">
          复制
        </div>
        <div class="menu-item" @click="emitMenuCommand('forward')">
          转发
        </div>
        <div v-if="sessionType === 'GROUP'" class="menu-item" @click="emitMenuCommand('readDetail')">
          查看已读详情
        </div>
        <div
          v-if="contextMenu.message?.isOwn && contextMenu.message?.type === 'TEXT'"
          class="menu-item"
          @click="emitMenuCommand('edit')"
        >
          编辑
        </div>
        <div
          v-if="contextMenu.message?.isOwn"
          class="menu-item danger"
          @click="emitMenuCommand('recall')"
        >
          撤回
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, reactive, computed, onUnmounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({ messages: Array, loading: Boolean, sessionType: String, jumping: Boolean, highlightedId: [String, Number] })
const emit = defineEmits(['load-more', 'show-user', 'retry', 'command', 'jump'])

const listRef = ref(null)
const showScrollBottom = ref(false)
const newMessageCount = ref(0)
// eslint-disable-next-line no-unused-vars
let lastMessageLength = 0

// 虚拟滚动相关状态
const containerHeight = ref(500) // 容器高度 (固定值，用于初始化)
const itemHeight = ref(80) // 默认消息项高度 (参考 Design Token: --dt-message-item-height)
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

const getSpacingClass = (absoluteIndex) => {
  if (absoluteIndex <= 0) return 'spacing-none'
  const prev = props.messages?.[absoluteIndex - 1]
  const curr = props.messages?.[absoluteIndex]
  if (!prev || !curr) return 'spacing-none'
  return prev.isOwn === curr.isOwn ? 'spacing-small' : 'spacing-large'
}

// eslint-disable-next-line no-unused-vars
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

const scrollToMessage = (messageId) => {
  if (!listRef.value || !props.messages?.length) return
  const index = props.messages.findIndex(m => (m.messageId || m.id) === messageId)
  if (index === -1) return
  const top = props.messages.slice(0, index).reduce((total, msg) => {
    return total + calculateMessageHeight(msg)
  }, 0)
  listRef.value.scrollTop = top
  updateVirtualScroll()
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  showScrollBottom.value = scrollHeight - scrollTop - clientHeight > 300
  if (scrollTop === 0 && !props.loading) emit('load-more')

  // 更新虚拟滚动
  updateVirtualScroll()
}

// 表情回应相关
const quickEmojis = ['👍', '❤️', '😮', '😂', '😢', '🔥']
const allEmojis = ['👍', '❤️', '😮', '😂', '😢', '🔥', '👏', '🎉', '🤔', '🙄', '😍', '🆗', '🙏', '💪', '👎']
const handleQuickReaction = (emoji) => {
  const msg = contextMenu.message
  closeContextMenu()
  if (msg) emit('command', 'reaction', { ...msg, emoji })
}

const contextMenu = reactive({ show: false, x: 0, y: 0, message: null })
const contextMenuStyle = computed(() => ({ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }))

const handleContextMenu = ({ x, y, message }) => {
  const menuWidth = 180
  const menuHeight = message?.isOwn ? 200 : 160
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight

  let posX = x
  let posY = y
  if (posX + menuWidth > windowWidth) posX = windowWidth - menuWidth - 8
  if (posY + menuHeight > windowHeight) posY = windowHeight - menuHeight - 8

  contextMenu.show = true
  contextMenu.x = posX
  contextMenu.y = posY
  contextMenu.message = message

  document.addEventListener('click', closeContextMenu, { once: true })
  document.addEventListener('keydown', handleEsc)
}

const closeContextMenu = () => {
  contextMenu.show = false
  document.removeEventListener('keydown', handleEsc)
}

const handleEsc = (e) => {
  if (e.key === 'Escape') closeContextMenu()
}

const emitMenuCommand = (cmd) => {
  const msg = contextMenu.message
  closeContextMenu()
  if (msg) emit('command', cmd, msg)
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

onUnmounted(() => {
  document.removeEventListener('keydown', handleEsc)
})
defineExpose({ scrollToBottom, scrollToMessage, listRef })
</script>

<style scoped lang="scss">
.message-list {
  flex: 1;
  overflow-y: auto;
  background: var(--dt-bg-chat);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  position: relative;
}

.message-item-wrapper.spacing-none { margin-top: 0; }
.message-item-wrapper.spacing-small { margin-top: 4px; }
.message-item-wrapper.spacing-large { margin-top: 16px; }

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
  margin: var(--dt-spacing-md) 0;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);

  &::before,
  &::after {
    content: '';
    flex: 1;
    max-width: var(--dt-divider-max-width, 60px);
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

.jumping-status {
  text-align: center;
  padding: var(--dt-spacing-sm);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-brand-color);
  background: var(--dt-brand-lighter);
  border-bottom: 1px solid var(--dt-border-light);
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
  gap: var(--dt-spacing-xs);

  &:hover {
    background: var(--dt-brand-hover);
    transform: translateY(-1px);
    box-shadow: var(--dt-shadow-3);
  }

  .el-icon {
    font-size: var(--dt-icon-size-sm, 14px);
  }
}

.msg-context-menu {
  position: fixed;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-sm);
  min-width: 180px;
  padding: 4px;
  z-index: var(--dt-z-popover);
  box-shadow: var(--dt-shadow-2);
}

.msg-context-menu .menu-item {
  padding: 6px 10px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  border-radius: var(--dt-radius-xs);
  cursor: pointer;
}

.msg-context-menu .menu-item:hover {
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
}

.msg-context-menu .menu-item.danger {
  color: var(--dt-error-color);
}

.msg-context-menu .menu-item.danger:hover {
  background: var(--dt-error-bg);
  color: var(--dt-error-color);
}

.msg-context-menu .emoji-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
}

.msg-context-menu .emoji-btn {
  font-size: 18px;
  padding: 2px 4px;
  border-radius: var(--dt-radius-xs);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.msg-context-menu .emoji-btn:hover {
  background: var(--dt-brand-lighter);
  transform: scale(1.2);
}

.msg-context-menu .emoji-btn:active {
  transform: scale(0.95);
}

.msg-context-menu .emoji-btn.more {
  font-size: 14px;
  color: var(--dt-text-tertiary);
  font-weight: 500;
}

.msg-context-menu .menu-divider {
  height: 1px;
  background: var(--dt-border-light);
  margin: 4px 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.emoji-option {
  font-size: 20px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: var(--dt-radius-xs);
  transition: background-color var(--dt-transition-fast);
}

.emoji-option:hover {
  background: var(--dt-bg-hover);
}

.emoji-option:active {
  background: var(--dt-brand-bg);
}
</style>
