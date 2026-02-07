<template>
  <div
    ref="listRef"
    class="message-list"
    @scroll="handleScroll"
  >
    <!-- 首次加载骨架屏 -->
    <SkeletonLoader
      v-if="loading && messages.length === 0"
      type="message"
      :count="6"
    />

    <!-- 空状态 -->
    <div
      v-else-if="messages.length === 0 && !loading"
      class="empty"
    >
      暂无消息
    </div>

    <!-- 消息内容 - 虚拟滚动优化 -->
    <template v-else>
      <!-- 顶部占位符：维持滚动高度 -->
      <div
        v-if="isLazyLoadingEnabled"
        class="virtual-spacer-top"
        :style="{ height: topSpacerHeight + 'px' }"
      />

      <!-- 可见消息列表 -->
      <div
        v-for="msg in visibleMessagesComputed"
        :key="msg.id || msg.timeText"
        :data-id="msg.id"
        class="message-wrapper"
      >
        <!-- 时间分隔符 -->
        <div
          v-if="msg.isTimeDivider"
          class="time-divider"
        >
          <span class="time-text">{{ msg.timeText }}</span>
        </div>

        <!-- 消息项组件 -->
        <MessageItem
          v-else
          :message="msg"
          :multi-select-mode="multiSelectMode"
          @reply="$emit('reply', $event)"
          @reaction="handleReaction"
          @command="handleCommand"
          @scroll-to="scrollToMsg"
          @at="$emit('at', $event)"
          @show-user="$emit('show-user', $event)"
          @retry="$emit('retry', $event)"
          @nudge="handleNudge"
          @long-press="$emit('long-press', $event)"
        >
          <!-- 消息气泡内容插槽 -->
          <template #bubble>
            <MessageBubble
              :message="msg"
              :session-type="sessionType"
              :is-large-group="isLargeGroup"
              @command="handleCommand($event, msg)"
              @at="$emit('at', msg)"
              @preview="previewImage"
              @download="downloadFile"
              @retry="$emit('retry', $event)"
              @add-reaction="handleAddReaction"
              @re-edit="handleReEdit"
            />
          </template>

          <!-- 已读状态插槽 -->
          <template #read-status>
            <!-- 群聊：显示已读人数，可悬停查看详情 -->
            <el-popover
              v-if="sessionType === 'GROUP' && (msg.readCount > 0 || msg.isRead)"
              placement="top"
              :width="220"
              trigger="hover"
              popper-class="read-receipt-popover"
              @before-enter="fetchReadUsers(msg)"
            >
              <template #reference>
                <span class="read-count clickable">
                  {{ msg.readCount > 0 ? `${msg.readCount}人已读` : '已读' }}
                </span>
              </template>
              <div
                v-loading="loadingReadUsers[msg.id]"
                class="read-users-list"
              >
                <div class="read-users-header">
                  <span class="read-title">已读成员</span>
                  <span
                    v-if="readUsersMap[msg.id] && readUsersMap[msg.id].length > 0"
                    class="read-count-badge"
                  >{{
                    readUsersMap[msg.id].length }}</span>
                </div>
                <div class="read-users-body">
                  <div
                    v-for="user in readUsersMap[msg.id]"
                    :key="user.id"
                    class="read-user-item"
                  >
                    <DingtalkAvatar
                      :src="user.avatar"
                      :name="user.name"
                      :user-id="user.id"
                      :size="32"
                      shape="square"
                    />
                    <span class="user-name">{{ user.name }}</span>
                  </div>
                  <div
                    v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)"
                    class="empty-state"
                  >
                    <el-icon>
                      <User />
                    </el-icon>
                    <span>暂无已读成员</span>
                  </div>
                </div>
                <!-- 显示未读人数 -->
                <div
                  v-if="unreadCount(msg)"
                  class="unread-users-footer"
                >
                  <span>未读 {{ unreadCount(msg) }} 人</span>
                </div>
              </div>
            </el-popover>
            <!-- 单聊：只显示已读/未读 -->
            <span
              v-else-if="sessionType === 'PRIVATE'"
              class="read-status-simple"
              :class="{ read: msg.isRead || msg.readCount > 0, unread: !msg.isRead && msg.readCount === 0 }"
            >
              {{ msg.isRead || msg.readCount > 0 ? '已读' : '未读' }}
            </span>
            <!-- 无已读数据时显示未读 -->
            <span
              v-else
              class="unread"
            >未读</span>
          </template>
        </MessageItem>
      </div>

      <!-- 底部占位符：维持滚动高度 -->
      <div
        v-if="isLazyLoadingEnabled"
        class="virtual-spacer-bottom"
        :style="{ height: bottomSpacerHeight + 'px' }"
      />

      <!-- 加载更多骨架屏 -->
      <SkeletonLoader
        v-if="loading && messages.length > 0"
        type="message"
        :count="3"
      />

      <!-- 滚动到底部按钮 -->
      <transition name="fade">
        <div
          v-if="showScrollToBottom"
          class="scroll-to-bottom"
          @click="scrollToBottom"
        >
          <el-icon>
            <ArrowDown />
          </el-icon>
          <span>回到底部</span>
        </div>
      </transition>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { ArrowDown, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendNudge } from '@/api/im/nudge'
import MessageItem from './MessageItemRefactored.vue'
// 使用重构后的消息气泡组件
import MessageBubble from './MessageBubbleRefactored.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import SkeletonLoader from '@/components/Common/SkeletonLoader.vue'
import { copyToClipboard } from '@/utils/format'

// 组合式函数
import { useMessageVirtualScroll, AVERAGE_MESSAGE_HEIGHT } from './composables/useMessageVirtualScroll.js'
import { useMessageReadUsers } from './composables/useMessageReadUsers.js'
import { useMessageScroll } from './composables/useMessageScroll.js'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  currentUser: Object,
  loading: Boolean,
  sessionId: [String, Number],
  sessionType: {
    type: String,
    default: 'PRIVATE'
  },
  multiSelectMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more', 'edit', 'command', 'at', 'show-user', 'retry', 'reaction-update', 're-edit', 'preview', 'nudge-success', 'long-press'])

const listRef = ref(null)
const isUnmounted = ref(false) // 标记组件是否已卸载，防止卸载后执行 DOM 操作

// ============================================================================
// 组合式函数：虚拟滚动、已读用户、滚动处理
// ============================================================================

// 虚拟滚动（传递获取消息的函数，避免循环依赖）
const {
  isLargeGroup,
  isLazyLoadingEnabled,
  topSpacerHeight,
  bottomSpacerHeight,
  visibleMessages: visibleMessagesComputed,
  updateScrollPosition,
  scrollTop: scrollY,
  clientHeight: containerHeight
} = useMessageVirtualScroll(props, () => messagesWithDividers.value)

// 已读用户管理
const {
  readUsersMap,
  loadingReadUsers,
  fetchReadUsers,
  prefetchReadUsers
} = useMessageReadUsers(computed(() => props.sessionId))

// 滚动处理
const {
  showScrollToBottom,
  scrollToBottom,
  scrollToMsg: scrollToMsgFromComposable,
  handleScroll: handleScrollFromComposable,
  maintainScrollPosition
} = useMessageScroll(listRef, emit, isUnmounted)

// 使用本地增强版 scrollToMsg，但调用 composable 的基础功能
const handleScroll = event => {
  // 更新虚拟滚动的位置信息
  if (listRef.value) {
    updateScrollPosition(listRef.value)
  }
  // 调用 composable 的滚动处理
  handleScrollFromComposable(event)
}

// 监听新消息，如果是自己的消息则滚动到底部
watch(() => props.messages.length, (newLength, oldLength) => {
  if (newLength > oldLength) {
    const newMessage = props.messages[props.messages.length - 1]
    if (newMessage?.isOwn) {
      scrollToBottom()
    }
  }

  // 批量预加载可见区域的已读用户
  const recentMessages = props.messages.slice(-20)
  prefetchReadUsers(recentMessages)
})

// 计算未读人数（群聊中需要知道群成员总数）
const unreadCount = msg => {
  if (!msg.groupMemberCount) { return null }
  const readCount = msg.readCount || 0
  return Math.max(0, msg.groupMemberCount - readCount)
}

// 图片预览 - 发送事件给父组件处理
const previewImage = clickedUrl => {
  emit('preview', clickedUrl)
}

// 下载文件
const downloadFile = fileInfo => {
  if (!fileInfo.fileUrl) { return }
  window.open(fileInfo.fileUrl, '_blank')
}

// 处理重新编辑撤回的消息
const handleReEdit = ({ content }) => {
  emit('re-edit', content)
}

// 处理拍一拍
const handleNudge = async nudgedUserId => {
  if (!props.sessionId) { return }

  try {
    const res = await sendNudge({
      conversationId: props.sessionId,
      nudgedUserId
    })

    if (res.code === 200) {
      ElMessage.success(`${res.data.nudgerName} 拍了拍 ${res.data.nudgedUserName}`)
      emit('nudge-success', res.data)
    } else {
      ElMessage.warning(res.msg || '拍一拍失败')
    }
  } catch (error) {
    if (error.message) {
      ElMessage.warning(error.message)
    } else {
      console.error('拍一拍失败:', error)
      ElMessage.error('拍一拍失败，请稍后重试')
    }
  }
}

// 格式化时间分隔符文案
const formatTimeDivider = timestamp => {
  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })

  if (diffDays === 0) { return timeStr }
  if (diffDays === 1) { return `昨天 ${timeStr}` }
  if (diffDays === 2) { return `前天 ${timeStr}` }
  if (diffDays < 7) {
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return `${weekDays[date.getDay()]} ${timeStr}`
  }

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

// ============================================================================
// 消息合并逻辑
// ============================================================================

// 判断两条消息是否可以合并显示（钉钉风格）
// 规则：
// 1. 同一发送者
// 2. 时间间隔小于 2 分钟
// 3. 非特殊消息类型（如撤回消息、系统消息、拍一拍等）
// 4. 自己的消息和对方的消息都可以合并
const canMergeWith = (currentMsg, prevMsg) => {
  if (!prevMsg) { return false }

  // 必须是同一发送者
  if (currentMsg.senderId !== prevMsg.senderId) { return false }

  // 必须是同一消息归属（自己的/对方的）
  if (currentMsg.isOwn !== prevMsg.isOwn) { return false }

  // 时间间隔小于 2 分钟（120000ms）
  const MERGE_TIME_THRESHOLD = 2 * 60 * 1000
  const timeDiff = currentMsg.timestamp - prevMsg.timestamp
  if (timeDiff > MERGE_TIME_THRESHOLD) { return false }

  // 特殊消息类型不合并
  const nonMergeableTypes = ['RECALLED', 'SYSTEM', 'NOTICE', 'NUDGE']
  if (nonMergeableTypes.includes(currentMsg.type)) { return false }
  if (nonMergeableTypes.includes(prevMsg.type)) { return false }

  return true
}

// 计算带时间分割线和合并标记的消息列表
const messagesWithDividers = computed(() => {
  const res = []
  // 保存已处理的最后一条真实消息（用于判断合并）
  let lastRealMessage = null

  props.messages.forEach((msg, index) => {
    // 添加时间分割线
    if (index === 0 || shouldAddTimeDivider(msg, props.messages[index - 1])) {
      res.push({
        id: `time-${msg.id}`,
        isTimeDivider: true,
        timeText: formatTimeDivider(msg.timestamp)
      })
    }

    // 判断是否需要与上一条消息合并
    // 直接使用 lastRealMessage 判断，避免在 res 数组中查找
    let isMerged = false
    if (lastRealMessage && canMergeWith(msg, lastRealMessage)) {
      isMerged = true
    }

    // 更新最后一条真实消息
    lastRealMessage = msg

    res.push({ ...msg, isMerged })
  })
  return res
})

// 判断是否需要添加时间分割线
const shouldAddTimeDivider = (currentMsg, prevMsg) => {
  if (!prevMsg) { return true }

  const currentTime = currentMsg.timestamp
  const prevTime = prevMsg.timestamp

  // 时间间隔超过 5 分钟添加分割线
  const TIME_DIVIDER_THRESHOLD = 5 * 60 * 1000
  return currentTime - prevTime > TIME_DIVIDER_THRESHOLD
}

// 处理菜单命令
const handleCommand = async (cmd, msg) => {
  if (cmd === 'copy') {
    copyToClipboard(msg.content)
  } else if (cmd === 'at') {
    emit('at', msg)
  } else if (cmd === 'pin') {
    // 处理置顶/取消置顶
    await handlePinMessage(msg)
  } else {
    emit('command', cmd, msg)
  }
}

// 处理置顶消息
const handlePinMessage = async msg => {
  try {
    const { pinMessage, unpinMessage } = await import('@/api/im/message')

    if (msg.isPinned) {
      await unpinMessage(msg.id)
      ElMessage.success('已取消置顶')
    } else {
      await pinMessage(msg.id)
      ElMessage.success('已置顶消息')
    }

    // 通过事件更新消息状态
    emit('message-update', { ...msg, isPinned: !msg.isPinned })
  } catch (error) {
    console.error('置顶操作失败:', error)
    ElMessage.error(msg.isPinned ? '取消置顶失败' : '置顶失败')
  }
}

// 处理表情反应（从 MessageItem 组件触发）
const handleReaction = (msg, reaction) => {
  ElMessage.success(`你对消息做出了 ${reaction} 反应`)
}

// 处理表情回复（从 MessageBubble 组件触发）
const handleAddReaction = (messageId, emoji, isAdded) => {
  // 表情回复已经在 MessageBubble 中处理了 API 调用
  // 触发父组件事件，用于 WebSocket 广播
  emit('reaction-update', { messageId, emoji, isAdded })
}

// 滚动到指定消息
// 支持参数: id (消息ID) 或 { messageId, highlight } 对象
const scrollToMsg = param => {
  let messageId
  let highlight = false

  // 支持两种调用方式
  if (typeof param === 'string' || typeof param === 'number') {
    messageId = param
  } else if (param && typeof param === 'object') {
    messageId = param.messageId || param.id
    highlight = param.highlight === true
  }

  // 在懒加载模式下，临时禁用懒加载以确保消息在 DOM 中
  // 这里的策略是：暂时扩大渲染范围，等待 DOM 更新后再滚动
  const tempDisableLazyLoading = () => {
    if (!isLazyLoadingEnabled.value) {
      performScroll()
      return
    }

    // 临时扩大渲染范围：找到目标消息的索引
    const targetIndex = messagesWithDividers.value.findIndex(m => m.id === messageId)
    if (targetIndex === -1) {
      ElMessage.warning('消息不存在')
      return
    }

    // 计算需要渲染的范围（包含目标消息 + 上下各 10 条消息）
    const rangeSize = 20
    const rangeStart = Math.max(0, targetIndex - rangeSize / 2)
    const rangeEnd = Math.min(messagesWithDividers.value.length, targetIndex + rangeSize / 2)

    // 临时渲染更大范围的消息
    // 注意：这里我们使用 nextTick 确保 DOM 更新后再滚动
    // 但由于 visibleMessages 是 computed，我们需要用其他方法
    // 简单方案：直接滚动并等待
    performScroll()
  }

  const performScroll = () => {
    // 使用 nextTick 确保 DOM 更新
    nextTick(() => {
      if (isUnmounted.value) { return } // 组件已卸载，不执行 DOM 操作
      const el = listRef.value?.querySelector(`[data-id="${messageId}"]`)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })

        // 根据是否需要高亮闪烁，选择不同的动画类
        const highlightClass = highlight ? 'message-highlight-flash' : 'highlight-msg-active'
        const duration = highlight ? 1500 : 2000

        el.classList.add(highlightClass)
        setTimeout(() => {
          el.classList.remove(highlightClass)
        }, duration)
      } else {
        ElMessage.warning('消息时间过久，请向上翻阅查找')
      }
    })
  }

  tempDisableLazyLoading()
}

// 可见区域跟踪（用于已读上报优化）
const visibleRange = ref({ start: 0, end: 0 })

// 更新可见区域（用于已读上报）
const updateVisibleRange = (scrollPos, viewportHeight) => {
  const allMessages = messagesWithDividers.value
  if (allMessages.length === 0) { return }

  const viewportTop = scrollPos
  const viewportBottom = scrollPos + viewportHeight

  // 简单估算可见范围
  const startIdx = Math.floor(viewportTop / AVERAGE_MESSAGE_HEIGHT)
  const endIdx = Math.ceil(viewportBottom / AVERAGE_MESSAGE_HEIGHT)

  visibleRange.value = {
    start: Math.max(0, startIdx),
    end: Math.min(allMessages.length - 1, endIdx)
  }
}

// 保持滚动偏移（用于加载更多）
const maintainScroll = oldHeight => {
  nextTick(() => {
    if (isUnmounted.value) { return } // 组件已卸载，不执行 DOM 操作
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight - oldHeight
    }
  })
}

const observer = ref(null)

// 初始化已读上报监听
const initReadObserver = () => {
  if (observer.value) { observer.value.disconnect() }

  observer.value = new IntersectionObserver(entries => {
    // 如果组件已卸载，不处理回调
    if (isUnmounted.value) { return }

    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const msgId = entry.target.getAttribute('data-id')
        const msg = props.messages.find(m => m.id === msgId)
        // 如果消息未读且不是自己发的
        if (msg && !msg.isOwn && !msg.isRead) {
          emit('command', 'mark-read', msg)
        }
      }
    })
  }, { threshold: 0.5 })
}

const updateObserver = () => {
  nextTick(() => {
    if (isUnmounted.value) { return } // 组件已卸载，不执行 DOM 操作
    const items = listRef.value?.querySelectorAll('.message-wrapper[data-id]')
    items?.forEach(el => observer.value?.observe(el))
  })
}

watch(() => props.messages.length, () => {
  scrollToBottom()
  updateObserver()
})

onMounted(() => {
  initReadObserver()
  updateObserver()

  // 初始化视口高度
  if (listRef.value) {
    containerHeight.value = listRef.value.clientHeight
  }

  // 监听容器大小变化
  const resizeObserver = new ResizeObserver(entries => {
    for (const entry of entries) {
      if (entry.target === listRef.value) {
        containerHeight.value = entry.contentRect.height
      }
    }
  })

  if (listRef.value) {
    resizeObserver.observe(listRef.value)
  }

  // 保存 observer 引用以便清理
  onUnmounted(() => {
    isUnmounted.value = true // 标记组件已卸载
    resizeObserver.disconnect()
  })
})

onUnmounted(() => {
  isUnmounted.value = true // 标记组件已卸载
  if (observer.value) { observer.value.disconnect() }
})

defineExpose({ scrollToBottom, maintainScroll: maintainScrollPosition, scrollToMessage: scrollToMsg })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/animations.scss' as *;

.message-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px; // 野火IM标准:20px padding
  background: #f5f5f5; // 野火IM标准:浅灰背景
  position: relative;
  scroll-behavior: smooth;
  // 性能优化
  will-change: scroll-position;
  contain: layout style paint;

  // 自定义滚动条 - 野火IM风格
  &::-webkit-scrollbar {
    width: 6px; // 野火IM:6px宽滚动条
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2); // 浅灰色
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }

  &:hover::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
  }

  // 暗色模式
  .dark &:hover::-webkit-scrollbar-thumb {
    background: rgba(0, 137, 255, 0.3);
  }

  .dark &:hover::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 137, 255, 0.4);
  }
}

.loading-wrapper {
  text-align: center;
  padding: 20px;
  color: var(--dt-text-tertiary);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

// 滚动到底部按钮 - 钉钉风格
.scroll-to-bottom {
  position: fixed;
  right: calc(50% - 280px);
  bottom: 80px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-full);
  color: var(--dt-brand-color);
  font-size: 12px;
  cursor: pointer;
  box-shadow: var(--dt-shadow-sm);
  transition: all var(--dt-transition-fast);
  z-index: 10;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-md);
  }

  .el-icon {
    font-size: 14px;
  }
}

// ============================================================================
// 虚拟滚动占位符 - 维持正确的滚动条高度
// ============================================================================
.virtual-spacer-top,
.virtual-spacer-bottom {
  width: 100%;
  pointer-events: none;
  flex-shrink: 0;
}

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-fast);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--dt-text-quaternary);
  font-size: 13px;
}

.time-divider {
  text-align: center;
  margin: 20px 0;
  color: var(--dt-text-tertiary);
  font-size: 12px;
  line-height: 1;

  .time-text {
    background: rgba(0, 0, 0, 0.05); // 钉钉风格：极浅半透明底
    color: var(--dt-text-tertiary);
    padding: 2px 8px; // 优化：更窄的间距
    font-size: 12px;
    border-radius: 4px; // 钉钉风格：小圆角矩形，而非全圆角
    display: inline-block;
  }
}

// ============================================================================
// 已读状态样式 - 钉钉风格
// ============================================================================
.read-status {
  font-size: 12px; // 从11px增大到12px,提升可读性
  cursor: default;
  display: flex;
  align-items: center;
  gap: 4px;

  .read {
    color: var(--dt-text-quaternary);
  }

  .unread {
    color: var(--dt-brand-color);
  }

  .read-count {
    color: var(--dt-brand-color);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    display: inline-flex;
    align-items: center;
    gap: 2px;
    padding: 2px 6px;
    border-radius: var(--dt-radius-lg);
    background: var(--dt-brand-bg);

    &:hover {
      background: var(--dt-brand-hover);
      text-decoration: none;
    }

    // 添加小图标
    &::before {
      content: 'visibility';
      font-family: 'Material Icons Outlined';
      font-size: 12px;
    }
  }
}

// 钉钉风格已读状态
.read-status-simple {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  border-radius: var(--dt-radius-lg);
  font-size: 12px; // 从11px增大到12px
  transition: all var(--dt-transition-fast);

  &.read {
    color: var(--dt-text-quaternary);
  }

  &.unread {
    color: var(--dt-brand-color);
  }
}

.unread {
  color: var(--dt-brand-color);
  font-size: 12px; // 从11px增大到12px
}

// ============================================================================
// 已读用户列表样式
// ============================================================================
.read-users-list {
  .read-users-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--dt-border-lighter);
    margin-bottom: 12px;
  }

  .read-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .read-count-badge {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    font-size: 11px;
    padding: 2px 8px;
    border-radius: var(--dt-radius-full);
  }

  .read-users-body {
    max-height: 200px;
    overflow-y: auto;
  }

  .read-user-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 0;

    &:hover {
      background: var(--dt-bg-hover);
      margin: 0 -8px;
      padding-left: 8px;
      padding-right: 8px;
      border-radius: var(--dt-radius-sm);
    }
  }

  .user-name {
    font-size: 13px;
    color: var(--dt-text-primary);
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0;
    color: var(--dt-text-quaternary);
    font-size: 12px;
    gap: 8px;
  }

  .unread-users-footer {
    padding-top: 12px;
    border-top: 1px solid var(--dt-border-lighter);
    margin-top: 12px;
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

// ============================================================================
// 消息高亮动画 - 野火IM风格
// ============================================================================
.message-wrapper {
  &[data-id] {
    transition: background 0.2s;
  }
}

// 普通高亮（滚动定位）
.highlight-msg-active {
  animation: highlightPulseBg 1s ease-out;
}

// 消息引用跳转高亮（3次闪烁）
.message-highlight-flash {
  animation: messageHighlightFlash 1.5s ease-in-out;
}

// 搜索结果高亮
.search-highlight {
  animation: searchHighlight 2s ease-out;
}

// 动画定义
@keyframes highlightPulseBg {
  0% {
    background: rgba(65, 104, 224, 0.2);
  }

  // 野火蓝
  100% {
    background: transparent;
  }
}

@keyframes messageHighlightFlash {

  0%,
  20%,
  40%,
  60%,
  80% {
    background: rgba(255, 235, 59, 0.4);
  }

  // 黄色闪烁
  10%,
  30%,
  50%,
  70%,
  90%,
  100% {
    background: transparent;
  }
}

@keyframes searchHighlight {
  0% {
    background: rgba(65, 104, 224, 0.3);
    box-shadow: 0 0 0 2px rgba(65, 104, 224, 0.3);
  }

  50% {
    background: rgba(65, 104, 224, 0.15);
    box-shadow: 0 0 0 4px rgba(65, 104, 224, 0.1);
  }

  100% {
    background: transparent;
    box-shadow: none;
  }
}
</style>
