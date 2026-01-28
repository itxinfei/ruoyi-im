<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <!-- 加载中状态 - 骨架屏 -->
    <div v-if="loading" class="skeleton-wrapper">
      <div v-for="i in 5" :key="i" class="message-skeleton">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-name"></div>
          <div class="skeleton-bubble">
            <div class="skeleton-line long"></div>
            <div class="skeleton-line"></div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
    
    <!-- 消息内容 -->
    <template v-else>
      <div v-for="msg in messagesWithDividers" :key="msg.id || msg.timeText" :data-id="msg.id" class="message-wrapper">
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
    </template>

    <!-- 回到底部按钮 -->
    <Transition name="fade">
      <div v-if="showScrollToBottom" class="scroll-to-bottom-btn" @click="scrollToBottom()">
        <el-icon><ArrowDown /></el-icon>
        <span>回到最新</span>
      </div>
    </Transition>

    <!-- 图片预览器（使用 v-viewer） -->
    <div v-viewer class="image-viewer-container" style="display: none;">
      <img
        v-for="(url, index) in imagePreviewUrls"
        :key="index"
        :src="url"
        :index="index"
        style="display: none;"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { Loading, ArrowDown, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageReadUsers } from '@/api/im/message'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

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

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more', 'edit', 'command', 'at', 'show-user', 'retry', 'reaction-update'])

const listRef = ref(null)
const readUsersMap = ref({})
const loadingReadUsers = ref({})
const showScrollToBottom = ref(false)

// 图片预览状态
const imagePreviewUrls = ref([])

// ============================================================================
// 性能优化：消息分页渲染
// ============================================================================

// 每页渲染的消息数量
const PAGE_SIZE = 30
// 当前渲染的页数
const currentPage = ref(1)
// 是否启用分页渲染（消息数超过阈值时启用）
const ENABLE_PAGINATION_THRESHOLD = 50

// 是否启用分页
const isPaginationEnabled = computed(() => props.messages.length > ENABLE_PAGINATION_THRESHOLD)

// 分页渲染的消息列表
const paginatedMessages = computed(() => {
  if (!isPaginationEnabled.value) {
    return messagesWithDividers.value
  }
  // 始终渲染最后 N 条消息，避免一次性渲染全部
  const startIndex = Math.max(0, messagesWithDividers.value.length - (currentPage.value * PAGE_SIZE))
  return messagesWithDividers.value.slice(startIndex)
})

// 加载更多历史消息（向前加载）
const loadMoreHistory = () => {
  if (!isPaginationEnabled.value) return
  const totalPages = Math.ceil(messagesWithDividers.value.length / PAGE_SIZE)
  if (currentPage.value < totalPages) {
    currentPage.value++
  }
}

// 监听消息变化，重置分页
watch(() => props.sessionId, () => {
  currentPage.value = 1
})

// 监听新消息，如果是自己的消息则滚动到底部
watch(() => props.messages.length, (newLength, oldLength) => {
  if (newLength > oldLength) {
    const newMessage = props.messages[props.messages.length - 1]
    if (newMessage?.isOwn) {
      scrollToBottom()
    }
  }
})

// 获取已读用户列表
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

// 计算未读人数（群聊中需要知道群成员总数）
const unreadCount = (msg) => {
  if (!msg.groupMemberCount) return null
  const readCount = msg.readCount || 0
  return Math.max(0, msg.groupMemberCount - readCount)
}

// 图片预览 - 使用 v-viewer
const previewImage = (clickedUrl) => {
  // 收集会话中所有图片消息的 URL
  const imageUrls = []
  let clickedIndex = 0

  props.messages.forEach((msg) => {
    if (msg.type === 'IMAGE') {
      let imageUrl = ''
      // 尝试从不同字段获取图片 URL
      if (typeof msg.content === 'string') {
        try {
          const contentObj = JSON.parse(msg.content)
          imageUrl = contentObj.imageUrl || contentObj.url || ''
        } catch {
          imageUrl = msg.content
        }
      } else if (typeof msg.content === 'object') {
        imageUrl = msg.content.imageUrl || msg.content.url || ''
      }

      if (imageUrl) {
        imageUrls.push(imageUrl)
        // 记录被点击图片的索引
        if (imageUrl === clickedUrl || imageUrl === decodeURIComponent(clickedUrl)) {
          clickedIndex = imageUrls.length - 1
        }
      }
    }
  })

  if (imageUrls.length > 0) {
    imagePreviewUrls.value = imageUrls
    // 使用 v-viewer 的 API 显示预览
    nextTick(() => {
      const viewerContainer = document.querySelector('.image-viewer-container')
      if (viewerContainer) {
        // 创建临时 img 元素来触发 viewer
        const imgElements = viewerContainer.querySelectorAll('img')
        if (imgElements[clickedIndex]) {
          // 使用 v-viewer 的 show 方法
          const viewer = viewerContainer.$viewer
          if (viewer && viewer.show) {
            viewer.show(clickedIndex)
          } else {
            // 备选方案：直接点击图片
            imgElements[clickedIndex].click()
          }
        }
      }
    })
  } else {
    // 降级到新窗口打开
    window.open(clickedUrl, '_blank')
  }
}

// 下载文件
const downloadFile = (fileInfo) => {
  if (!fileInfo.fileUrl) return
  window.open(fileInfo.fileUrl, '_blank')
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

// 计算带时间分割线的消息列表
const messagesWithDividers = computed(() => {
  const res = []

  props.messages.forEach((msg, index) => {
    // 判断是否需要显示时间分割线
    let showDivider = false
    if (index === 0) {
      showDivider = true
    } else {
      const prevMsg = props.messages[index - 1]
      const timeDiff = msg.timestamp - prevMsg.timestamp
      // 30分钟间隔或每10条消息显示一次时间
      if (timeDiff > 30 * 60 * 1000 || index % 10 === 0) {
        showDivider = true
      }
    }

    if (showDivider) {
      res.push({
        isTimeDivider: true,
        timeText: formatTimeDivider(msg.timestamp),
        id: 'divider-' + msg.timestamp + '-' + index
      })
    }

    res.push({ ...msg, isMerged: false })
  })
  return res
})

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

// 监听消息变化自动滚动
watch(() => props.messages.length, () => scrollToBottom())

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
const scrollToMsg = (id) => {
  const el = listRef.value?.querySelector(`[data-id="${id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('highlight-msg-active')
    setTimeout(() => {
      el.classList.remove('highlight-msg-active')
    }, 2000)
  } else {
    ElMessage.warning('消息时间过久，请向上翻阅查找')
  }
}

// 监听滚动事件
const handleScroll = () => {
  if (!listRef.value || props.loading) return
  
  const { scrollTop, clientHeight, scrollHeight } = listRef.value
  
  // 滚动到顶部加载更多
  if (scrollTop === 0) {
    emit('load-more')
  }
  
  // 检测是否接近底部
  const distanceFromBottom = scrollHeight - scrollTop - clientHeight
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
})

onUnmounted(() => {
  if (observer.value) observer.value.disconnect()
})

defineExpose({ scrollToBottom, maintainScroll })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: var(--dt-bg-chat);
  position: relative;
  min-height: 0; // 修复 flex 容器溢出问题

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

// 骨架屏样式
.skeleton-wrapper {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-skeleton {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.5s var(--dt-ease-out) both;

  &:nth-child(1) { animation-delay: 0ms; }
  &:nth-child(2) { animation-delay: 100ms; }
  &:nth-child(3) { animation-delay: 200ms; }
  &:nth-child(4) { animation-delay: 300ms; }
  &:nth-child(5) { animation-delay: 400ms; }
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-lg);
  @include skeleton-loading;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 400px;
}

.skeleton-name {
  width: 80px;
  height: 14px;
  border-radius: 4px;
  @include skeleton-loading;
}

.skeleton-bubble {
  padding: 10px 14px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid var(--dt-border-light);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.skeleton-line {
  height: 12px;
  border-radius: 3px;
  @include skeleton-loading;
  width: 100%;

  &.long {
    width: 80%;
  }
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

.read-status {
  font-size: 11px;
  cursor: default;

  .read { color: var(--dt-text-quaternary); }
  .unread { color: var(--dt-brand-color); }
  .read-count {
    color: var(--dt-brand-color);
    cursor: pointer;
    transition: color var(--dt-transition-fast);

    &:hover { text-decoration: underline; }
  }
}

.read-users-list {
  .read-users-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px 8px;
    border-bottom: 1px solid var(--dt-border-lighter);

    .read-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    .read-count-badge {
      background: var(--dt-brand-color);
      color: #fff;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 10px;
      min-width: 20px;
      text-align: center;
    }
  }

  .read-users-body {
    max-height: 240px;
    overflow-y: auto;
    padding: 4px 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--dt-border);
      border-radius: 2px;
    }

    .read-user-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 16px;
      font-size: 13px;
      transition: background var(--dt-transition-fast);
      cursor: default;

      &:hover {
        background: var(--dt-bg-hover);
      }

      .user-name {
        color: var(--dt-text-primary);
        font-weight: 400;
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 32px 16px;
      color: var(--dt-text-tertiary);
      gap: 8px;

      .el-icon {
        font-size: 32px;
        opacity: 0.5;
      }
    }
  }

  .unread-users-footer {
    padding: 8px 16px;
    border-top: 1px solid var(--dt-border-lighter);
    color: var(--dt-text-secondary);
    font-size: 12px;
    text-align: center;
  }
}

.read-status-simple {
  &.read {
    color: var(--dt-text-quaternary);
  }

  &.unread {
    color: var(--dt-brand-color);
  }
}

.read-count.clickable {
  cursor: pointer;
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
  }
}

.scroll-to-bottom-btn {
  position: absolute;
  right: 20px;
  bottom: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-full);
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  box-shadow: var(--dt-shadow-2);
  font-size: 13px;
  color: var(--dt-text-secondary);
  z-index: 10;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-3);
  }

  &:active {
    transform: translateY(0);
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity var(--dt-transition-base), transform var(--dt-transition-base);
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

// 图片预览器过渡动画
.viewer-fade-enter-active,
.viewer-fade-leave-active {
  transition: opacity 0.3s;
}

.viewer-fade-enter-from,
.viewer-fade-leave-to {
  opacity: 0;
}
</style>

<style lang="scss">
@use '@/styles/design-tokens.scss' as *;

@keyframes highlight-pulse {
  0% { background-color: transparent; }
  15% { background-color: var(--dt-brand-lighter); }
  85% { background-color: var(--dt-brand-lighter); }
  100% { background-color: transparent; }
}

.highlight-msg {
  animation: highlight-pulse 2s var(--dt-ease-in-out) forwards;
  border-radius: var(--dt-radius-md);
}

/* 全局样式用于气泡右键菜单 */
.message-context-menu {
  padding: 4px 0 !important;
  border-radius: 6px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid #e5e7eb !important;
  background: #fff !important;
  min-width: 120px !important;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px !important;
    font-size: 13px !important;
    color: #1f2329 !important;
    transition: all var(--dt-transition-fast);

    .el-icon {
      font-size: 16px;
      color: #646a73;
      margin-right: 0;
      flex-shrink: 0;
    }

    &:hover {
      background-color: #f5f6f7 !important;
      color: #1677ff !important;
      .el-icon { color: #1677ff; }
    }

    &.danger {
      color: #f5222d !important;
      &:hover {
        background-color: #fff1f0 !important;
        .el-icon { color: #f5222d; }
      }
    }

    &.is-pinned {
      color: #1677ff !important;
      .el-icon {
        color: #1677ff;
      }
    }
  }

  .menu-divider {
    height: 1px;
    background-color: #e5e7eb;
    margin: 4px 0;
  }
}

.dark .message-context-menu {
  border-color: #334155 !important;
  background: #1e293b !important;

  .el-dropdown-menu__item {
    color: #f1f5f9 !important;

    .el-icon {
      color: #bdc3c9;
    }

    &:hover {
      background-color: #334155 !important;
      color: #1677ff !important;
      .el-icon { color: #1677ff; }
    }

    &.danger {
      color: #ef4444 !important;
      &:hover {
        background-color: rgba(239, 68, 68, 0.1) !important;
        .el-icon { color: #ef4444; }
      }
    }
  }

  .menu-divider {
    background-color: #334155;
  }
}

/* 已读回执弹窗样式 */
.read-receipt-popover {
  padding: 0 !important;
  border-radius: 8px !important;
  overflow: hidden;

  .el-popover__title {
    display: none;
  }
}

:global(.dark) .read-receipt-popover {
  background: #1e293b !important;
  border-color: #334155 !important;
}
</style>
