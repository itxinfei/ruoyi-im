<template>
  <!-- 侧边搜索面板 -->
  <transition name="slide-right">
    <div
      v-if="dialogVisible"
      class="chat-search-sidebar"
    >
      <div
        class="sidebar-overlay"
        @click="handleClose"
      />

      <div class="sidebar-panel">
        <!-- 头部 -->
        <div class="sidebar-header">
          <h3 class="header-title">
            搜索聊天记录
          </h3>
          <button
            class="close-btn"
            @click="handleClose"
          >
            <span class="material-icons-outlined">close</span>
          </button>
        </div>
        <div class="chat-search">
          <!-- 搜索栏 -->
          <div class="search-bar">
            <div class="search-input-wrapper">
              <span class="material-icons-outlined search-icon">search</span>
              <input
                ref="searchInputRef"
                v-model="searchKeyword"
                class="search-input"
                placeholder="搜索聊天记录..."
                type="text"
                @input="handleSearch"
                @keydown.down="handleKeyDown"
                @keydown.up="handleKeyUp"
                @keydown.enter="handleEnter"
              >
              <span
                v-if="searchKeyword"
                class="clear-btn"
                @click="handleClear"
              >
                <span class="material-icons-outlined">close</span>
              </span>
            </div>
            <div class="search-actions">
              <button
                v-if="totalCount > 0"
                class="nav-btn"
                title="上一个"
                @click="navigatePrev"
              >
                <span class="material-icons-outlined">chevron_left</span>
              </button>
              <span
                v-if="totalCount > 0"
                class="result-count"
              >
                {{ currentIndex + 1 }}&nbsp;/&nbsp;{{ totalCount }}
              </span>
              <button
                v-if="totalCount > 0"
                class="nav-btn"
                title="下一个"
                @click="navigateNext"
              >
                <span class="material-icons-outlined">chevron_right</span>
              </button>
            </div>
          </div>

          <!-- 搜索结果列表 -->
          <div
            v-if="!loading && searchResults.length > 0"
            class="search-results"
          >
            <div class="results-header">
              <span class="results-title">找到 {{ totalCount }} 条结果</span>
            </div>
            <div
              ref="resultsListRef"
              class="results-list"
            >
              <div
                v-for="(result, index) in searchResults"
                :key="result.id"
                class="result-item"
                :class="{ active: index === currentIndex }"
                @click="handleSelectResult(result, index)"
              >
                <div class="result-header">
                  <DingtalkAvatar
                    :name="result.senderName"
                    :user-id="result.senderId"
                    :src="result.senderAvatar"
                    :size="28"
                    shape="square"
                  />
                  <span class="sender-name">{{ result.senderName }}</span>
                  <span class="result-time">{{ formatTime(result.sendTime) }}</span>
                </div>
                <div class="result-content">
                  <div
                    v-if="result.type === 'TEXT'"
                    class="text-result"
                    v-html="highlightKeyword(result.content)"
                  />
                  <div
                    v-else-if="result.type === 'IMAGE'"
                    class="media-result"
                  >
                    <span class="material-icons-outlined">image</span>
                    <span>[图片]</span>
                  </div>
                  <div
                    v-else-if="result.type === 'FILE'"
                    class="media-result"
                  >
                    <span class="material-icons-outlined">insert_drive_file</span>
                    <span>[文件] {{ getFileName(result.content) }}</span>
                  </div>
                  <div
                    v-else-if="result.type === 'VIDEO'"
                    class="media-result"
                  >
                    <span class="material-icons-outlined">videocam</span>
                    <span>[视频]</span>
                  </div>
                  <div
                    v-else-if="result.type === 'VOICE'"
                    class="media-result"
                  >
                    <span class="material-icons-outlined">mic</span>
                    <span>[语音]</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 无结果 -->
          <div
            v-else-if="!loading && searchKeyword && searchResults.length === 0"
            class="no-results"
          >
            <span class="material-icons-outlined empty-icon">search_off</span>
            <p>未找到 "{{ searchKeyword }}" 相关消息</p>
            <span class="hint">试试其他关键词</span>
          </div>

          <!-- 搜索中 -->
          <div
            v-if="loading"
            class="search-loading"
          >
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <span>搜索中...</span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, ref, watch, nextTick, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { parseContentString } from '@/utils/message'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'select-message', 'close'])

// 搜索关键词
const searchKeyword = ref('')
// 搜索结果
const searchResults = ref([])
// 当前选中索引
const currentIndex = ref(0)
// 加载状态
const loading = ref(false)
// 搜索防抖定时器
let searchTimer = null

const searchInputRef = ref(null)
const resultsListRef = ref(null)
const isUnmounted = ref(false) // 标记组件是否已卸载

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val)
})

// 总结果数
const totalCount = computed(() => searchResults.value.length)

// 监听显示状态
const handleOpen = () => {
  nextTick(() => {
    if (isUnmounted.value) { return }
    searchInputRef.value?.focus()
  })
}

const handleClose = () => {
  handleClear()
}

// 搜索处理（防抖）
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    performSearch()
  }, 300)
}

// 执行搜索
const performSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    currentIndex.value = 0
    return
  }

  loading.value = true

  // 模拟异步搜索
  setTimeout(() => {
    const results = props.messages.filter(msg => {
      if (msg.type === 'TEXT') {
        return msg.content?.toLowerCase().includes(keyword.toLowerCase())
      }
      if (msg.type === 'FILE') {
        const fileName = getFileName(msg.content)
        return fileName?.toLowerCase().includes(keyword.toLowerCase())
      }
      return false
    })

    searchResults.value = results.map(msg => ({
      id: msg.id,
      type: msg.type,
      content: msg.content,
      senderId: msg.senderId,
      senderName: msg.senderName,
      senderAvatar: msg.senderAvatar,
      sendTime: msg.sendTime
    }))

    currentIndex.value = 0
    loading.value = false
  }, 100)
}

// 高亮关键词
const highlightKeyword = text => {
  if (!searchKeyword.value) { return escapeHtml(text) }
  const keyword = searchKeyword.value.trim()
  const regex = new RegExp(`(${escapeRegExp(keyword)})`, 'gi')
  return escapeHtml(text).replace(regex, '<mark class="highlight">$1</mark>')
}

// 转义 HTML 特殊字符，防止 XSS 攻击
const escapeHtml = str => {
  if (!str) { return '' }
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

// 转义正则特殊字符
const escapeRegExp = string => {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

// 获取文件名
const getFileName = content => {
  try {
    const parsed = parseContentString(content)
    return parsed.fileName || parsed.name || ''
  } catch {
    return ''
  }
}

// 格式化时间
const formatTime = time => {
  if (!time) { return '' }
  const date = dayjs(time)
  const now = dayjs()
  const diffDays = now.diff(date, 'day')

  if (diffDays === 0) {
    return date.format('HH:mm')
  } else if (diffDays === 1) {
    return '昨天 ' + date.format('HH:mm')
  } else if (diffDays < 7) {
    return date.format('ddd HH:mm')
  } else {
    return date.format('MM-DD HH:mm')
  }
}

// 清除搜索
const handleClear = () => {
  searchKeyword.value = ''
  searchResults.value = []
  currentIndex.value = 0
}

// 选择结果
const handleSelectResult = (result, index) => {
  currentIndex.value = index
  emit('select-message', result.id)
  // 选择后关闭弹窗
  dialogVisible.value = false
}

// 导航到上一个
const navigatePrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    const result = searchResults.value[currentIndex.value]
    emit('select-message', result.id)
    scrollToCurrentResult()
  }
}

// 导航到下一个
const navigateNext = () => {
  if (currentIndex.value < totalCount.value - 1) {
    currentIndex.value++
    const result = searchResults.value[currentIndex.value]
    emit('select-message', result.id)
    scrollToCurrentResult()
  }
}

// 滚动到当前结果
const scrollToCurrentResult = () => {
  nextTick(() => {
    if (isUnmounted.value) { return }
    const activeElement = resultsListRef.value?.querySelector('.result-item.active')
    if (activeElement) {
      activeElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    }
  })
}

// 键盘导航
const handleKeyDown = () => {
  if (totalCount.value > 0) {
    navigatePrev()
  }
}

const handleKeyUp = () => {
  if (totalCount.value > 0) {
    navigateNext()
  }
}

const handleEnter = () => {
  if (totalCount.value > 0) {
    const result = searchResults.value[currentIndex.value]
    emit('select-message', result.id)
    dialogVisible.value = false
  }
}

// 组件卸载时标记
onUnmounted(() => {
  isUnmounted.value = true
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.chat-search {
  padding: 0;
}

// ============================================================================
// 搜索栏
// ============================================================================
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-light);
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: var(--dt-bg-secondary);
  border-radius: var(--dt-radius-lg);
  padding: 0 12px;
  height: 40px;
  border: 1.5px solid transparent;
  transition: all var(--dt-transition-base);

  &:focus-within {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px var(--dt-brand-light);
  }

  .search-icon {
    color: var(--dt-text-tertiary);
    font-size: 20px;
  }

  .search-input {
    flex: 1;
    border: none;
    background: transparent;
    padding: 0 8px;
    font-size: 14px;
    color: var(--dt-text-primary);
    outline: none;

    &::placeholder {
      color: var(--dt-text-quaternary);
    }
  }

  .clear-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: var(--dt-radius-full);
    cursor: pointer;
    color: var(--dt-text-tertiary);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-secondary);
    }

    .material-icons-outlined {
      font-size: 18px;
    }
  }
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 4px;

  .nav-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .result-count {
    font-size: 13px;
    color: var(--dt-text-secondary);
    font-weight: 500;
    min-width: 60px;
    text-align: center;
  }
}

// ============================================================================
// 搜索结果
// ============================================================================
.search-results {
  margin-top: 12px;

  .results-header {
    padding: 8px 0;
    border-bottom: 1px solid var(--dt-border-light);

    .results-title {
      font-size: 13px;
      color: var(--dt-text-secondary);
      font-weight: 500;
    }
  }

  .results-list {
    max-height: 400px;
    overflow-y: auto;
    padding: 8px 0;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--dt-border-medium);
      border-radius: var(--dt-radius-sm);

      &:hover {
        background: var(--dt-text-tertiary);
      }
    }
  }
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-light);

    .sender-name {
      color: var(--dt-brand-color);
    }
  }

  .result-header {
    display: flex;
    align-items: center;
    gap: 8px;

    .sender-name {
      font-size: 13px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .result-time {
      margin-left: auto;
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .result-content {
    font-size: 14px;
    color: var(--dt-text-secondary);
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;

    :deep(.highlight) {
      background-color: var(--dt-search-highlight-bg);
      color: inherit;
      padding: 0 2px;
      border-radius: var(--dt-radius-sm);
    }

    .text-result {
      word-break: break-word;
    }

    .media-result {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--dt-text-tertiary);

      .material-icons-outlined {
        font-size: 16px;
      }
    }
  }
}

// ============================================================================
// 无结果状态
// ============================================================================
.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;

  .empty-icon {
    font-size: 48px;
    color: var(--dt-text-quaternary);
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    color: var(--dt-text-secondary);
    margin: 0 0 4px 0;
  }

  .hint {
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// 加载状态
// ============================================================================
.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: var(--dt-text-tertiary);
  font-size: 14px;

  .el-icon {
    font-size: 18px;
  }
}

// ============================================================================
// 侧边栏容器
// ============================================================================
.chat-search-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

// 遮罩层
.sidebar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--dt-bg-overlay);
  backdrop-filter: blur(2px);
}

// 侧边栏面板
.sidebar-panel {
  position: relative;
  width: 360px;
  height: 100%;
  background: var(--dt-bg-card);
  box-shadow: var(--dt-shadow-md);
  display: flex;
  flex-direction: column;
  z-index: 1;
}

// 头部
.sidebar-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-md);
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }
}

// ============================================================================
// 搜索内容区
// ============================================================================
.chat-search {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-black-10);
    border-radius: 3px;

    &:hover {
      background: var(--dt-black-20);
    }
  }
}

// ============================================================================
// 滑入动画
// ============================================================================
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .sidebar-overlay {
    transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .sidebar-panel {
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.slide-right-enter-from,
.slide-right-leave-to {
  .sidebar-overlay {
    opacity: 0;
  }

  .sidebar-panel {
    transform: translateX(100%);
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark {
  .sidebar-panel {
    background: var(--dt-bg-card-dark);
    border-left: 1px solid var(--dt-border-dark);
  }

  .search-input-wrapper {
    background: var(--dt-white-10);

    &:focus-within {
      background: var(--dt-bg-card-dark);
    }
  }

  .result-item.active {
    background: var(--dt-brand-bg-dark);

    .sender-name {
      color: var(--dt-brand-color);
    }
  }

  :deep(.highlight) {
    background-color: var(--dt-search-highlight-bg-dark);
  }
}
</style>
