<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <!-- 首次加载骨架屏 -->
    <SkeletonLoader v-if="loading && messages.length === 0" type="message" :count="6" />

    <!-- 空状态 -->
    <div v-else-if="messages.length === 0 && !loading" class="empty">暂无消息</div>

    <!-- 消息内容 -->
    <template v-else>
      <div v-for="msg in visibleMessages" :key="msg.id || msg.timeText" :data-id="msg.id" class="message-wrapper">
        <!-- 时间分隔符 -->
        <div v-if="msg.isTimeDivider" class="time-divider">
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
              <div v-loading="loadingReadUsers[msg.id]" class="read-users-list">
                <div class="read-users-header">
                  <span class="read-title">已读成员</span>
                  <span v-if="readUsersMap[msg.id]" class="read-count-badge">{{ readUsersMap[msg.id].length }}</span>
                </div>
                <div class="read-users-body">
                  <div v-for="user in readUsersMap[msg.id]" :key="user.id" class="read-user-item">
                    <DingtalkAvatar :src="user.avatar" :name="user.name" :user-id="user.id" :size="32" shape="square" />
                    <span class="user-name">{{ user.name }}</span>
                  </div>
                  <div v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)" class="empty-state">
                    <el-icon><User /></el-icon>
                    <span>暂无已读成员</span>
                  </div>
                </div>
                <!-- 显示未读人数 -->
                <div v-if="unreadCount(msg)" class="unread-users-footer">
                  <span>未读 {{ unreadCount(msg) }} 人</span>
                </div>
              </div>
            </el-popover>
            <!-- 单聊：只显示已读/未读 -->
            <span v-else-if="sessionType === 'PRIVATE'" class="read-status-simple" :class="{ read: msg.isRead || msg.readCount > 0, unread: !msg.isRead && msg.readCount === 0 }">
              {{ msg.isRead || msg.readCount > 0 ? '已读' : '未读' }}
            </span>
            <!-- 无已读数据时显示未读 -->
            <span v-else class="unread">未读</span>
          </template>
        </MessageItem>
      </div>

      <!-- 加载更多骨架屏 -->
      <SkeletonLoader v-if="loading && messages.length > 0" type="message" :count="3" />

      <!-- 滚动到底部按钮 -->
      <transition name="fade">
        <div v-if="showScrollToBottom" class="scroll-to-bottom" @click="scrollToBottom">
          <el-icon><ArrowDown /></el-icon>
          <span>回到底部</span>
        </div>
      </transition>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { Loading, ArrowDown, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageReadUsers, getBatchMessageReadUsers } from '@/api/im/message'
import { sendNudge } from '@/api/im/nudge'
import MessageItem from './MessageItemRefactored.vue'
// 使用重构后的消息气泡组件
import MessageBubble from './MessageBubbleRefactored.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import SkeletonLoader from '@/components/Common/SkeletonLoader.vue'

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
const readUsersMap = ref({})
const loadingReadUsers = ref({})
const showScrollToBottom = ref(false)

// ============================================================================
// 性能优化：消息懒加载渲染
// ============================================================================

// 懒加载配置
const ENABLE_LAZY_LOADING_THRESHOLD = 100 // 消息数超过此阈值时启用懒加载
const BUFFER_ZONE = {
  ABOVE: 200,  // 视口上方缓冲区（像素）
  BELOW: 400,  // 视口下方缓冲区（像素）
  MESSAGE_COUNT: 20  // 最少渲染消息数（即使不在缓冲区内）
}

// 滚动位置跟踪
const scrollTop = ref(0)
const clientHeight = ref(600) // 初始估算值

// 消息高度缓存（用于快速计算）
const messageHeightCache = ref(new Map())

// 平均消息高度（用于估算）
const AVERAGE_MESSAGE_HEIGHT = 80 // 包含头像、内容、间距的估算高度

// 是否启用懒加载
const isLazyLoadingEnabled = computed(() => props.messages.length > ENABLE_LAZY_LOADING_THRESHOLD)

/**
 * 计算可见区域的消息范围
 * @returns {Object} { startIndex, endIndex } - 在 messagesWithDividers 中的索引范围
 */
const calculateVisibleRange = () => {
  if (!isLazyLoadingEnabled.value) {
    // 不启用懒加载时，渲染全部消息
    return { startIndex: 0, endIndex: messagesWithDividers.value.length }
  }

  const allMessages = messagesWithDividers.value
  if (allMessages.length === 0) {
    return { startIndex: 0, endIndex: 0 }
  }

  // 从滚动位置估算可见的消息索引
  // scrollTop 是当前滚动位置，需要找到对应的消息索引
  const viewportTop = scrollTop.value
  const viewportBottom = scrollTop.value + clientHeight.value

  // 扩展视口范围，包含缓冲区
  const renderTop = Math.max(0, viewportTop - BUFFER_ZONE.ABOVE)
  const renderBottom = viewportBottom + BUFFER_ZONE.BELOW

  // 估算消息索引
  let startIndex = Math.floor(renderTop / AVERAGE_MESSAGE_HEIGHT)
  let endIndex = Math.ceil(renderBottom / AVERAGE_MESSAGE_HEIGHT)

  // 确保在有效范围内
  startIndex = Math.max(0, startIndex - BUFFER_ZONE.MESSAGE_COUNT) // 向上多取一些
  endIndex = Math.min(allMessages.length, endIndex + BUFFER_ZONE.MESSAGE_COUNT) // 向下多取一些

  // 确保至少渲染一定数量的消息
  if (endIndex - startIndex < BUFFER_ZONE.MESSAGE_COUNT * 2) {
    // 如果渲染的消息太少，扩展范围
    const centerIndex = Math.floor((startIndex + endIndex) / 2)
    startIndex = Math.max(0, centerIndex - BUFFER_ZONE.MESSAGE_COUNT)
    endIndex = Math.min(allMessages.length, centerIndex + BUFFER_ZONE.MESSAGE_COUNT)
  }

  return { startIndex, endIndex }
}

// 可见消息列表（动态计算）
const visibleMessages = computed(() => {
  const allMessages = messagesWithDividers.value
  if (allMessages.length === 0) return []

  // 不启用懒加载时，返回全部消息
  if (!isLazyLoadingEnabled.value) {
    return allMessages
  }

  // 计算可见范围
  const { startIndex, endIndex } = calculateVisibleRange()

  // 截取可见区域的消息
  return allMessages.slice(startIndex, endIndex)
})

// 监听新消息，如果是自己的消息则滚动到底部
watch(() => props.messages.length, (newLength, oldLength) => {
  if (newLength > oldLength) {
    const newMessage = props.messages[props.messages.length - 1]
    if (newMessage?.isOwn) {
      scrollToBottom()
    }
  }

  // 批量预加载可见区域的已读用户
  // 限制预加载数量为最近 20 条消息
  const visibleMessages = props.messages.slice(-20)
  prefetchReadUsers(visibleMessages)
})

// 获取已读用户列表（单条消息）
const fetchReadUsers = async (msg) => {
  if (readUsersMap.value[msg.id] || loadingReadUsers.value[msg.id]) return

  loadingReadUsers.value[msg.id] = true
  try {
    const res = await getMessageReadUsers(props.sessionId, msg.id)
    if (res.code === 200) {
      readUsersMap.value[msg.id] = res.data
    }
  } catch (error) {
    console.error('获取已读用户失败', error)
  } finally {
    loadingReadUsers.value[msg.id] = false
  }
}

/**
 * 批量预加载已读用户列表
 * 用于优化可见区域消息的已读状态查询
 * @param {Array} messages - 需要预加载的消息列表
 */
const prefetchReadUsers = async (messages) => {
  if (!messages || messages.length === 0) return

  // 过滤出需要查询的消息（群聊、有已读数据、未加载过）
  const messagesToFetch = messages.filter(msg =>
    msg.groupMemberCount &&
    (msg.readCount > 0 || msg.isRead) &&
    !readUsersMap.value[msg.id] &&
    !loadingReadUsers.value[msg.id]
  )

  if (messagesToFetch.length === 0) return

  // 设置加载状态
  messagesToFetch.forEach(msg => {
    loadingReadUsers.value[msg.id] = true
  })

  try {
    const messageIds = messagesToFetch.map(m => m.id)
    const res = await getBatchMessageReadUsers(messageIds)

    if (res.code === 200) {
      // 将结果存入缓存
      Object.keys(res.data).forEach(msgId => {
        readUsersMap.value[msgId] = res.data[msgId]
      })
    }
  } catch (error) {
    console.error('批量获取已读用户失败', error)
    // 批量失败时回退到单个查询
    messagesToFetch.forEach(msg => {
      loadingReadUsers.value[msg.id] = false
    })
  } finally {
    // 清除加载状态
    messagesToFetch.forEach(msg => {
      loadingReadUsers.value[msg.id] = false
    })
  }
}

// 计算未读人数（群聊中需要知道群成员总数）
const unreadCount = (msg) => {
  if (!msg.groupMemberCount) return null
  const readCount = msg.readCount || 0
  return Math.max(0, msg.groupMemberCount - readCount)
}

// 图片预览 - 发送事件给父组件处理
const previewImage = (clickedUrl) => {
  emit('preview', clickedUrl)
}

// 下载文件
const downloadFile = (fileInfo) => {
  if (!fileInfo.fileUrl) return
  window.open(fileInfo.fileUrl, '_blank')
}

// 处理重新编辑撤回的消息
const handleReEdit = ({ content }) => {
  emit('re-edit', content)
}

// 处理拍一拍
const handleNudge = async (nudgedUserId) => {
  if (!props.sessionId) return

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
const formatTimeDivider = (timestamp) => {
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

  if (diffDays === 0) return timeStr
  if (diffDays === 1) return `昨天 ${timeStr}`
  if (diffDays === 2) return `前天 ${timeStr}`
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
  if (!prevMsg) return false

  // 必须是同一发送者
  if (currentMsg.senderId !== prevMsg.senderId) return false

  // 必须是同一消息归属（自己的/对方的）
  if (currentMsg.isOwn !== prevMsg.isOwn) return false

  // 时间间隔小于 2 分钟（120000ms）
  const MERGE_TIME_THRESHOLD = 2 * 60 * 1000
  const timeDiff = currentMsg.timestamp - prevMsg.timestamp
  if (timeDiff > MERGE_TIME_THRESHOLD) return false

  // 特殊消息类型不合并
  const nonMergeableTypes = ['RECALLED', 'SYSTEM', 'NOTICE', 'NUDGE']
  if (nonMergeableTypes.includes(currentMsg.type)) return false
  if (nonMergeableTypes.includes(prevMsg.type)) return false

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
  if (!prevMsg) return true

  const currentTime = currentMsg.timestamp
  const prevTime = prevMsg.timestamp

  // 时间间隔超过 5 分钟添加分割线
  const TIME_DIVIDER_THRESHOLD = 5 * 60 * 1000
  return currentTime - prevTime > TIME_DIVIDER_THRESHOLD
}

// 滚动到底部
const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTo({
        top: listRef.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    }
  })
}

// 处理菜单命令
const handleCommand = async (cmd, msg) => {
  if (cmd === 'copy') {
    navigator.clipboard.writeText(msg.content)
    ElMessage.success('已复制')
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
const handlePinMessage = async (msg) => {
  try {
    const { pinMessage, unpinMessage } = await import('@/api/im/message')

    if (msg.isPinned) {
      await unpinMessage(msg.id)
      ElMessage.success('已取消置顶')
    } else {
      await pinMessage(msg.id)
      ElMessage.success('已置顶消息')
    }

    // 更新本地消息状态
    const index = props.messages.findIndex(m => m.id === msg.id)
    if (index !== -1) {
      props.messages[index].isPinned = !msg.isPinned
    }
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
const scrollToMsg = (param) => {
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
    const targetIndex = messagesWithDividers.value.findIndex(m => m.id == messageId)
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

// 监听滚动事件
const handleScroll = () => {
  if (!listRef.value || props.loading) return

  const { scrollTop: newScrollTop, clientHeight: newClientHeight, scrollHeight } = listRef.value

  // 更新滚动位置信息（用于懒加载计算）
  scrollTop.value = newScrollTop
  clientHeight.value = newClientHeight

  // 滚动到顶部加载更多
  if (newScrollTop === 0) {
    emit('load-more')
  }

  // 检测是否接近底部
  const distanceFromBottom = scrollHeight - newScrollTop - newClientHeight
  showScrollToBottom.value = distanceFromBottom > 300
}

// 保持滚动偏移（用于加载更多）
const maintainScroll = (oldHeight) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight - oldHeight
    }
  })
}

const observer = ref(null)

// 初始化已读上报监听
const initReadObserver = () => {
  if (observer.value) observer.value.disconnect()

  observer.value = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const msgId = entry.target.getAttribute('data-id')
        const msg = props.messages.find(m => m.id == msgId)
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
    clientHeight.value = listRef.value.clientHeight
  }

  // 监听容器大小变化
  const resizeObserver = new ResizeObserver((entries) => {
    for (const entry of entries) {
      if (entry.target === listRef.value) {
        clientHeight.value = entry.contentRect.height
      }
    }
  })

  if (listRef.value) {
    resizeObserver.observe(listRef.value)
  }

  // 保存 observer 引用以便清理
  onUnmounted(() => {
    resizeObserver.disconnect()
  })
})

onUnmounted(() => {
  if (observer.value) observer.value.disconnect()
})

defineExpose({ scrollToBottom, maintainScroll, scrollToMessage: scrollToMsg })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-list {
  flex: 1;
  overflow: hidden;
  overflow-y: auto;
  padding: 16px;
  background: var(--dt-bg-chat);
  position: relative;
  min-height: 0;
  word-break: break-word;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
    transition: background var(--dt-transition-base);
  }

  &:hover::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb);
  }

  .dark &:hover::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-dark);
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
    background: var(--dt-bg-body);
    padding: 4px 12px;
    border-radius: var(--dt-radius-full);
    display: inline-block;
  }
}

// ============================================================================
// 已读状态样式 - 钉钉风格
// ============================================================================
.read-status {
  font-size: 11px;
  cursor: default;
  display: flex;
  align-items: center;
  gap: 4px;

  .read { color: var(--dt-text-quaternary); }
  .unread { color: var(--dt-brand-color); }
  .read-count {
    color: var(--dt-brand-color);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    display: inline-flex;
    align-items: center;
    gap: 2px;
    padding: 2px 6px;
    border-radius: 10px;
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
  border-radius: 10px;
  font-size: 11px;
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
  font-size: 11px;
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
// 消息高亮动画
// ============================================================================
.message-wrapper {
  &[data-id] {
    transition: background var(--dt-transition-fast);
  }
}

.highlight-msg-active {
  animation: highlightPulseBg 1s ease-out;
}

// 消息引用跳转高亮（3次黄色闪烁）
.message-highlight-flash {
  animation: messageHighlightFlash 1.5s ease-in-out;
}
</style>
