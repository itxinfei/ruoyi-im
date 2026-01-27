<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="is-loading"><Loading /></el-icon>数据加载中...
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
            />
          </template>

          <!-- 已读状态插槽 -->
          <template #read-status>
            <span v-if="msg.isRead" class="read">已读</span>
            <el-popover
              v-else-if="msg.readCount > 0"
              placement="top"
              :width="200"
              trigger="hover"
              @before-enter="fetchReadUsers(msg)"
            >
              <template #reference>
                <span class="read-count">{{ msg.readCount }}人已读</span>
              </template>
              <div v-loading="loadingReadUsers[msg.id]" class="read-users-list">
                <div v-for="user in readUsersMap[msg.id]" :key="user.id" class="read-user-item">
                  <DingtalkAvatar :src="user.avatar" :name="user.name" :user-id="user.id" :size="24" shape="circle" />
                  <span>{{ user.name }}</span>
                </div>
                <div v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)" class="empty">
                  加载中...
                </div>
              </div>
            </el-popover>
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
import { Loading, ArrowDown } from '@element-plus/icons-vue'
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

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more', 'edit', 'command', 'at', 'show-user', 'retry'])

const listRef = ref(null)
const readUsersMap = ref({})
const loadingReadUsers = ref({})
const showScrollToBottom = ref(false)

// 图片预览状态
const imagePreviewUrls = ref([])

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
    res.push(msg)
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
const handleCommand = (cmd, msg) => {
  if (cmd === 'copy') {
    navigator.clipboard.writeText(msg.content)
    ElMessage.success('已复制')
  } else if (cmd === 'at') {
    emit('at', msg)
  } else {
    emit('command', cmd, msg)
  }
}

// 处理表情反应
const handleReaction = (msg, reaction) => {
  ElMessage.success(`你对消息做出了 ${reaction} 反应`)
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
@import '@/styles/design-tokens.scss';

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
  max-height: 200px;
  overflow-y: auto;

  .read-user-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 13px;
    transition: background var(--dt-transition-fast);
    border-radius: var(--dt-radius-md);

    &:hover { background: var(--dt-bg-hover); }
    &:not(:last-child) { border-bottom: 1px solid var(--dt-border-lighter); }
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
@import '@/styles/design-tokens.scss';

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
</style>
