<template>
  <teleport to="body">
    <transition name="slide">
      <div
        v-if="visible"
        class="chat-history-overlay"
        @click.self="handleClose"
      >
        <div class="chat-history-panel">
          <!-- 头部 -->
          <div class="history-header">
            <div class="header-title">
              <span class="material-icons-outlined">history</span>
              <span>聊天记录</span>
            </div>
            <button
              class="close-btn"
              @click="handleClose"
            >
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <!-- 分类筛选 -->
          <div class="history-categories">
            <div
              v-for="cat in categories"
              :key="cat.value"
              class="category-item"
              :class="{ active: activeCategory === cat.value }"
              @click="handleCategoryChange(cat.value)"
            >
              <span class="material-icons-outlined category-icon">{{ cat.icon }}</span>
              <span class="category-label">{{ cat.label }}</span>
            </div>
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
          <div
            ref="listRef"
            class="history-list"
          >
            <div
              v-if="loading"
              class="loading-state"
            >
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>加载中...</span>
            </div>

            <div
              v-else-if="filteredMessages.length === 0"
              class="empty-state"
            >
              <span class="material-icons-outlined">forum</span>
              <p>暂无聊天记录</p>
            </div>

            <template v-else>
              <!-- 按日期分组 -->
              <div
                v-for="(group, date) in groupedMessages"
                :key="date"
                class="history-group"
              >
                <div class="group-date">
                  {{ formatDate(date) }}
                </div>

                <div
                  v-for="msg in group"
                  :key="msg.id"
                  class="history-item"
                  :class="{ 'own-message': msg.isOwn }"
                  @click="handleMessageClick(msg)"
                >
                  <div class="message-avatar">
                    <img
                      v-if="msg.senderAvatar"
                      :src="msg.senderAvatar"
                      alt=""
                    >
                    <span
                      v-else
                      class="avatar-placeholder"
                    >{{ msg.senderName?.charAt(0) || '?' }}</span>
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
          <div
            v-if="filteredMessages.length > 0 || !loading"
            class="history-footer"
          >
            <div class="footer-info">
              <span class="record-count">共 {{ filteredMessages.length }} 条记录</span>
            </div>
            <div class="footer-actions">
              <el-button
                size="small"
                :disabled="!hasMore || loading"
                @click="loadMore"
              >
                加载更多
              </el-button>
              <el-button
                size="small"
                @click="handleClose"
              >
                关闭
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMessages, getMessagesByCategory } from '@/api/im/message'
import { formatTime } from '@/utils/format'
import { parseContentString } from '@/utils/message'

const props = defineProps({
  visible: { type: Boolean, default: false },
  conversationId: { type: [String, Number], default: null },
  session: { type: Object, default: null }
})

const emit = defineEmits(['close', 'jump-to-message', 'clear-history'])

const store = useStore()
const currentUserId = computed(() => store.state.im.user?.id)

const searchKeyword = ref('')
const listRef = ref(null)
const loading = ref(false)
const hasMore = ref(true)
const historyMessages = ref([])
const isUnmounted = ref(false) // 标记组件是否已卸载
const currentPage = ref(1)
const pageSize = 50

// 分类筛选
const activeCategory = ref('all')
const categories = [
  { value: 'all', label: '全部', icon: 'chat_bubble' },
  { value: 'image', label: '图片', icon: 'image' },
  { value: 'file', label: '文件', icon: 'attach_file' },
  { value: 'link', label: '链接', icon: 'link' }
]

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
    const date = new Date(msg.sendTime).toDateString()
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(msg)
  })
  return groups
})

// 获取消息预览
const getMessagePreview = content => {
  if (!content) {return ''}
  if (typeof content === 'string') {return content}
  try {
    const parsed = parseContentString(content)
    if (parsed.text) {return parsed.text}
    if (parsed.imageUrl) {return '[图片]'}
    if (parsed.fileName) {return `[文件] ${parsed.fileName}`}
    return '[消息]'
  } catch {
    return String(content).slice(0, 50)
  }
}

// 格式化日期
const formatDate = dateStr => {
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

// 加载聊天记录
const loadHistoryMessages = async () => {
  const conversationId = getConversationId()
  if (!conversationId) {return}

  loading.value = true
  try {
    let response
    // 根据分类选择不同的API
    if (activeCategory.value === 'all') {
      response = await getMessages(conversationId, {
        pageNum: currentPage.value,
        pageSize: pageSize
      })
    } else {
      response = await getMessagesByCategory(conversationId, activeCategory.value, {
        lastId: historyMessages.value.length > 0 ? historyMessages.value[historyMessages.value.length - 1].id : null,
        limit: pageSize
      })
    }

    if (response.code === 200 && response.data) {
      const newMessages = response.data.records || response.data || []
      const messagesWithOwner = newMessages.map(msg => ({
        ...msg,
        isOwn: String(msg.senderId || '') === String(currentUserId.value || '')
      }))

      if (currentPage.value === 1) {
        historyMessages.value = messagesWithOwner
      } else {
        historyMessages.value = [...historyMessages.value, ...messagesWithOwner]
      }

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
  nextTick(() => {
    if (isUnmounted.value) {return}
    if (listRef.value) {
      listRef.value.scrollTop = 0
    }
  })
}

// 分类切换处理
const handleCategoryChange = category => {
  activeCategory.value = category
}

// 点击消息
const handleMessageClick = msg => {
  emit('jump-to-message', msg)
}

// 加载更多
const loadMore = async () => {
  if (!hasMore.value || loading.value) {return}
  currentPage.value++
  await loadHistoryMessages()
}

// 关闭
const handleClose = () => {
  emit('close')
}

// 监听 visible 变化
watch(() => props.visible, val => {
  if (val) {
    searchKeyword.value = ''
    activeCategory.value = 'all'
    currentPage.value = 1
    historyMessages.value = []
    loadHistoryMessages()
  }
})

// 监听分类变化
watch(activeCategory, () => {
  currentPage.value = 1
  historyMessages.value = []
  loadHistoryMessages()
})

// 获取会话ID
const getConversationId = () => {
  return props.conversationId || props.session?.conversationId || props.session?.id
}

// 组件卸载时标记
onUnmounted(() => {
  isUnmounted.value = true
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 过渡动画
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from,
.slide-leave-to {
  .chat-history-panel {
    transform: translateX(100%);
    opacity: 0;
  }
}

// 遮罩层
.chat-history-overlay {
  position: fixed;
  inset: 0;
  background: var(--dt-bg-overlay);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
  backdrop-filter: blur(4px);
}

// 面板
.chat-history-panel {
  width: 420px;
  height: 100%;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  box-shadow: var(--dt-shadow-lg);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

// 头部
.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    .dark & {
      color: var(--dt-text-secondary-dark);
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
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }
}

// 搜索
.history-categories {
  display: flex;
  padding: 12px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  gap: 8px;
  overflow-x: auto;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .category-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    border-radius: var(--dt-radius-2xl);
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
    flex-shrink: 0;

    .category-icon {
      font-size: 16px;
      color: var(--dt-text-secondary);
    }

    .category-label {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.active {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);

      .category-icon,
      .category-label {
        color: var(--dt-brand-color);
      }
    }
  }
}

.history-search {
  padding: 12px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: var(--dt-radius-md);
      padding: 4px 12px;
      box-shadow: none;
      border: 1px solid var(--dt-border-light);

      &:hover {
        border-color: var(--dt-border);
      }

      &.is-focus {
        border-color: var(--dt-brand-color);
      }
    }

    .el-input__prefix {
      color: var(--dt-text-tertiary);

      .material-icons-outlined {
        font-size: 18px;
      }
    }

    .el-input__inner {
      font-size: 14px;
    }
  }
}

// 列表
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-text-tertiary);
    }
  }
}

// 加载和空状态
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--dt-text-tertiary);
  gap: 12px;

  .material-icons-outlined {
    font-size: 48px;
    opacity: 0.4;
  }

  p {
    margin: 0;
    font-size: 14px;
  }

  .el-icon {
    font-size: 24px;
    color: var(--dt-brand-color);
  }
}

// 日期分组
.history-group {
  margin-bottom: 4px;
}

.group-date {
  padding: 8px 12px;
  margin-bottom: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-text-tertiary);
  text-align: center;
}

// 消息项
.history-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.own-message {
    background: var(--dt-brand-bg);

    &:hover {
      background: var(--dt-brand-light);
    }
  }
}

// 头像
.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-full);
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
    font-size: 14px;
    font-weight: 600;
  }
}

// 内容
.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  .sender-name {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-primary);

    .dark & {
      color: var(--dt-text-primary-dark);
    }
  }

  .message-time {
    font-size: 11px;
    color: var(--dt-text-tertiary);
    margin-left: auto;
  }
}

.message-text {
  font-size: 13px;
  color: var(--dt-text-secondary);
  word-break: break-all;
  line-height: 1.5;

  .dark & {
    color: var(--dt-text-secondary-dark);
  }

  .material-icons-outlined {
    font-size: 14px;
    vertical-align: middle;
    margin-right: 3px;
    color: var(--dt-text-tertiary);
  }
}

// 底部
.history-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);

  .dark & {
    border-top-color: var(--dt-border-dark);
    background: var(--dt-bg-card-dark);
  }

  .footer-info {
    margin-bottom: 12px;
    text-align: center;

    .record-count {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .footer-actions {
    display: flex;
    gap: 8px;
    justify-content: center;

    .el-button {
      flex: 1;
      border-radius: var(--dt-radius-md);

      &.el-button--small {
        height: 28px;
        font-size: 13px;
      }
    }
  }
}
</style>
