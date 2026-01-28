<template>
  <teleport to="body">
    <transition name="slide">
      <div v-if="visible" class="chat-history-overlay" @click.self="handleClose">
        <div class="chat-history-panel">
          <!-- 头部 -->
          <div class="history-header">
            <h3>聊天记录</h3>
            <button class="close-btn" @click="handleClose">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <!-- 搜索框 -->
          <div class="history-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息内容..."
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <span class="material-icons-outlined">search</span>
              </template>
            </el-input>
          </div>

          <!-- 消息列表 -->
          <div class="history-list" ref="listRef">
            <div v-if="loading" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>

            <div v-else-if="filteredMessages.length === 0" class="empty-state">
              <span class="material-icons-outlined">forum</span>
              <p>暂无聊天记录</p>
            </div>

            <template v-else>
              <!-- 按日期分组 -->
              <div v-for="(group, date) in groupedMessages" :key="date" class="history-group">
                <div class="group-date">{{ formatDate(date) }}</div>

                <div
                  v-for="msg in group"
                  :key="msg.id"
                  class="history-item"
                  :class="{ 'own-message': msg.isOwn }"
                  @click="handleMessageClick(msg)"
                >
                  <div class="message-avatar">
                    <img v-if="msg.senderAvatar" :src="msg.senderAvatar" alt="" />
                    <span v-else class="avatar-placeholder">{{ msg.senderName?.charAt(0) || '?' }}</span>
                  </div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="sender-name">{{ msg.senderName }}</span>
                      <span class="message-time">{{ formatTime(msg.sendTime) }}</span>
                    </div>
                    <div class="message-text">
                      <template v-if="msg.type === 'IMAGE'">
                        <span class="material-icons-outlined">image</span>
                        [图片]
                      </template>
                      <template v-else-if="msg.type === 'FILE'">
                        <span class="material-icons-outlined">attach_file</span>
                        [文件]
                      </template>
                      <template v-else-if="msg.type === 'VOICE'">
                        <span class="material-icons-outlined">mic</span>
                        [语音]
                      </template>
                      <template v-else-if="msg.type === 'VIDEO'">
                        <span class="material-icons-outlined">videocam</span>
                        [视频]
                      </template>
                      <template v-else>
                        {{ getMessagePreview(msg.content) }}
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 底部操作 -->
          <div class="history-footer">
            <el-button size="small" @click="loadMore" :disabled="!hasMore">
              加载更多
            </el-button>
            <el-button size="small" type="danger" plain @click="handleClearHistory">
              清空记录
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMessages } from '@/api/im/message'

const props = defineProps({
  visible: { type: Boolean, default: false },
  conversationId: { type: [String, Number], default: null }
})

const emit = defineEmits(['close', 'jump-to-message', 'clear-history'])

const store = useStore()
const currentUserId = computed(() => store.state.im.user?.id)

const searchKeyword = ref('')
const listRef = ref(null)
const loading = ref(false)
const hasMore = ref(true)
const historyMessages = ref([])
const currentPage = ref(1)
const pageSize = 50

// 过滤后的消息
const filteredMessages = computed(() => {
  if (!searchKeyword.value) {
    return historyMessages.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return historyMessages.value.filter(msg => {
    const content = getMessagePreview(msg.content).toLowerCase()
    return content.includes(keyword)
  })
})

// 按日期分组
const groupedMessages = computed(() => {
  const groups = {}
  filteredMessages.value.forEach(msg => {
    // 使用 sendTime 而不是 timestamp
    const date = new Date(msg.sendTime).toDateString()
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(msg)
  })
  return groups
})

// 获取消息预览
const getMessagePreview = (content) => {
  if (!content) return ''
  if (typeof content === 'string') return content
  try {
    const parsed = JSON.parse(content)
    if (parsed.text) return parsed.text
    if (parsed.imageUrl) return '[图片]'
    if (parsed.fileName) return `[文件] ${parsed.fileName}`
    return '[消息]'
  } catch {
    return String(content).slice(0, 50)
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 格式化时间
const formatTime = (sendTime) => {
  const date = new Date(sendTime)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 加载聊天记录
const loadHistoryMessages = async () => {
  if (!props.conversationId) return

  loading.value = true
  try {
    const response = await getMessages(props.conversationId, {
      pageNum: currentPage.value,
      pageSize: pageSize
    })

    if (response.code === 200 && response.data) {
      const newMessages = response.data.records || response.data || []
      // 为每条消息添加 isOwn 标识
      const messagesWithOwner = newMessages.map(msg => ({
        ...msg,
        isOwn: msg.senderId === currentUserId.value
      }))

      if (currentPage.value === 1) {
        historyMessages.value = messagesWithOwner
      } else {
        historyMessages.value = [...historyMessages.value, ...messagesWithOwner]
      }

      // 判断是否还有更多数据
      const totalCount = response.data.total || 0
      hasMore.value = historyMessages.value.length < totalCount
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    ElMessage.error('加载聊天记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索时滚动到顶部
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = 0
    }
  })
}

// 点击消息
const handleMessageClick = (msg) => {
  emit('jump-to-message', msg)
}

// 加载更多
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  await loadHistoryMessages()
}

// 清空历史
const handleClearHistory = () => {
  emit('clear-history')
}

// 关闭
const handleClose = () => {
  emit('close')
}

// 监听 visible 变化
watch(() => props.visible, (val) => {
  if (val) {
    // 打开面板时重置状态并加载消息
    searchKeyword.value = ''
    currentPage.value = 1
    historyMessages.value = []
    loadHistoryMessages()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
}

.slide-enter-from .chat-history-panel,
.slide-leave-to .chat-history-panel {
  transform: translateX(100%);
}

.chat-history-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

.chat-history-panel {
  width: 400px;
  height: 100%;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .dark & {
      color: var(--dt-text-primary-dark);
    }
  }

  .close-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }
  }
}

.history-search {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 20px;
    }

    .el-input__prefix {
      color: var(--dt-text-tertiary);
    }
  }
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);
  gap: 12px;

  .material-icons-outlined {
    font-size: 48px;
    opacity: 0.5;
  }
}

.history-group {
  margin-bottom: 20px;
}

.group-date {
  position: sticky;
  top: 0;
  padding: 8px 12px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-card);
  z-index: 1;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.history-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.own-message {
    background: var(--dt-brand-bg);

    &:hover {
      background: var(--dt-brand-bg);
      filter: brightness(0.95);
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-brand-color);
    color: #fff;
    font-size: 16px;
    font-weight: 600;
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.sender-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-primary);

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.message-time {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.message-text {
  font-size: 14px;
  color: var(--dt-text-secondary);
  word-break: break-all;
  line-height: 1.5;

  .dark & {
    color: var(--dt-text-secondary-dark);
  }

  .material-icons-outlined {
    font-size: 16px;
    vertical-align: middle;
    margin-right: 4px;
  }
}

.history-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}
</style>
