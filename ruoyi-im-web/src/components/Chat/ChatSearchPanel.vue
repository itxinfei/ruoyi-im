<template>
  <teleport to="body">
    <transition name="slide">
      <div v-if="visible" class="chat-search-overlay" @click.self="handleClose">
        <div class="chat-search-panel">
          <!-- 头部 -->
          <div class="search-header">
            <div class="header-title">
              <span class="material-icons-outlined">search</span>
              <span>搜索聊天记录</span>
            </div>
            <button class="close-btn" @click="handleClose">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <!-- 搜索输入区域 -->
          <div class="search-input-area">
            <el-input
              ref="searchInputRef"
              v-model="searchKeyword"
              placeholder="搜索消息内容、发送人..."
              size="large"
              clearable
              @input="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <span class="material-icons-outlined">search</span>
              </template>
            </el-input>

            <!-- 搜索类型筛选 -->
            <div class="search-filters">
              <button
                v-for="filter in searchFilters"
                :key="filter.key"
                class="filter-btn"
                :class="{ active: activeFilter === filter.key }"
                @click="activeFilter = filter.key"
              >
                <span class="material-icons-outlined">{{ filter.icon }}</span>
                <span>{{ filter.label }}</span>
                <span v-if="filter.count > 0" class="filter-count">{{ filter.count }}</span>
              </button>
            </div>
          </div>

          <!-- 搜索结果 -->
          <div class="search-results" ref="resultsRef">
            <!-- 加载状态 -->
            <div v-if="searching" class="loading-state">
              <el-icon class="is-loading" :size="32"><Loading /></el-icon>
              <span>搜索中...</span>
            </div>

            <!-- 空状态 - 未搜索 -->
            <div v-else-if="!hasSearched" class="empty-initial">
              <div class="empty-icon">
                <span class="material-icons-outlined">search</span>
              </div>
              <p>输入关键词搜索聊天记录</p>
              <div class="search-tips">
                <span class="tip-title">搜索提示：</span>
                <span>支持搜索消息内容、发送人、文件名等</span>
              </div>
            </div>

            <!-- 空状态 - 无结果 -->
            <div v-else-if="searchResults.length === 0" class="empty-no-result">
              <span class="material-icons-outlined">search_off</span>
              <p>未找到相关内容</p>
              <span class="hint">试试其他关键词</span>
            </div>

            <!-- 搜索结果列表 -->
            <div v-else class="results-list">
              <div class="results-summary">
                找到 <strong>{{ searchResults.length }}</strong> 条相关消息
              </div>

              <!-- 按日期分组的结果 -->
              <div v-for="(group, date) in groupedResults" :key="date" class="result-group">
                <div class="group-date">{{ formatDate(date) }}</div>

                <div
                  v-for="item in group"
                  :key="item.id"
                  class="result-item"
                  @click="handleResultClick(item)"
                >
                  <div class="result-avatar">
                    <DingtalkAvatar
                      :src="item.senderAvatar"
                      :name="item.senderName"
                      :user-id="item.senderId"
                      :size="36"
                      shape="square"
                    />
                  </div>
                  <div class="result-content">
                    <div class="result-header">
                      <span class="sender-name">{{ item.senderName }}</span>
                      <span class="message-time">{{ formatTime(item.timestamp) }}</span>
                    </div>
                    <div class="result-text">
                      <template v-if="item.type === 'TEXT'">
                        <span v-html="highlightKeyword(item.content)"></span>
                      </template>
                      <template v-else-if="item.type === 'IMAGE'">
                        <span class="material-icons-outlined">image</span>
                        <span>[图片]</span>
                      </template>
                      <template v-else-if="item.type === 'FILE'">
                        <span class="material-icons-outlined">description</span>
                        <span>[文件] {{ item.fileName }}</span>
                      </template>
                      <template v-else-if="item.type === 'VOICE'">
                        <span class="material-icons-outlined">mic</span>
                        <span>[语音] {{ item.duration }}秒</span>
                      </template>
                      <template v-else-if="item.type === 'VIDEO'">
                        <span class="material-icons-outlined">videocam</span>
                        <span>[视频]</span>
                      </template>
                      <template v-else>
                        <span>[{{ item.type }}]</span>
                      </template>
                    </div>
                  </div>
                  <div class="result-action">
                    <span class="material-icons-outlined">chevron_right</span>
                  </div>
                </div>
              </div>

              <!-- 加载更多 -->
              <div v-if="hasMore" class="load-more" @click="loadMore">
                <span>加载更多</span>
              </div>
            </div>
          </div>

          <!-- 底部快捷操作 -->
          <div v-if="hasSearched && searchResults.length > 0" class="search-footer">
            <el-button size="small" @click="handleExportResults">
              <span class="material-icons-outlined">download</span>
              导出结果
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { searchMessages } from '@/api/im/message'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatTime } from '@/utils/format'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sessionId: { type: [String, Number], default: null },
  messages: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'jump-to-message', 'export'])

const searchKeyword = ref('')
const searchInputRef = ref(null)
const resultsRef = ref(null)
const searching = ref(false)
const hasSearched = ref(false)
const searchResults = ref([])
const hasMore = ref(false)
const currentPage = ref(1)
const activeFilter = ref('all')

// 搜索类型筛选
const searchFilters = computed(() => [
  { key: 'all', label: '全部', icon: 'apps', count: 0 },
  { key: 'TEXT', label: '文本', icon: 'text_fields', count: searchResults.value.filter(m => m.type === 'TEXT').length },
  { key: 'IMAGE', label: '图片', icon: 'image', count: searchResults.value.filter(m => m.type === 'IMAGE').length },
  { key: 'FILE', label: '文件', icon: 'folder', count: searchResults.value.filter(m => m.type === 'FILE').length },
  { key: 'VOICE', label: '语音', icon: 'mic', count: searchResults.value.filter(m => m.type === 'VOICE').length }
])

// 按日期分组的结果
const groupedResults = computed(() => {
  let results = searchResults.value

  // 根据选中的筛选器过滤
  if (activeFilter.value !== 'all') {
    results = results.filter(m => m.type === activeFilter.value)
  }

  const groups = {}
  results.forEach(item => {
    const date = new Date(item.timestamp).toDateString()
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(item)
  })
  return groups
})

// 高亮关键词
const highlightKeyword = (text) => {
  if (!searchKeyword.value) return text
  const keyword = searchKeyword.value.trim()
  if (!keyword) return text

  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
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
    const diff = Math.floor((today - date) / (1000 * 60 * 60 * 24))
    if (diff < 7) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()]
    }
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 执行搜索
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    hasSearched.value = false
    return
  }

  searching.value = true
  hasSearched.value = true
  currentPage.value = 1

  try {
    // 先从本地消息中搜索
    let results = []

    if (props.messages && props.messages.length > 0) {
      results = props.messages.filter(msg => {
        // 搜索消息内容
        if (msg.type === 'TEXT' && msg.content) {
          if (msg.content.toLowerCase().includes(keyword.toLowerCase())) {
            return true
          }
        }

        // 搜索发送人
        if (msg.senderName && msg.senderName.toLowerCase().includes(keyword.toLowerCase())) {
          return true
        }

        // 搜索文件名
        if (msg.type === 'FILE' && msg.content) {
          const content = parseMessageContent(msg)
          if (content?.fileName?.toLowerCase().includes(keyword.toLowerCase())) {
            return true
          }
        }

        return false
      }).map(msg => {
        const content = parseMessageContent(msg) || {}
        return {
          ...msg,
          // 解析内容用于显示
          content: msg.type === 'TEXT' ? msg.content : '',
          fileName: msg.type === 'FILE' ? content.fileName || '' : '',
          duration: msg.type === 'VOICE' ? content.duration || '' : ''
        }
      })
    }

    // 如果有API，也调用API搜索
    if (props.sessionId) {
      try {
        const res = await searchMessages({
          conversationId: props.sessionId,
          keyword,
          pageNum: 1,
          pageSize: 50
        })

        if (res.code === 200 && res.data) {
          // 合并API结果和本地结果，去重
          const apiIds = new Set(results.map(m => m.id))
          res.data.forEach(msg => {
            if (!apiIds.has(msg.id)) {
              results.push(msg)
            }
          })
        }
      } catch (error) {
        console.error('API搜索失败，使用本地搜索结果:', error)
      }
    }

    searchResults.value = results
    hasMore.value = results.length >= 50
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

// 加载更多
const loadMore = async () => {
  if (!searchKeyword.value || searching.value) return

  searching.value = true
  currentPage.value++

  try {
    const res = await searchMessages({
      conversationId: props.sessionId,
      keyword: searchKeyword.value.trim(),
      pageNum: currentPage.value,
      pageSize: 50
    })

    if (res.code === 200 && res.data) {
      searchResults.value.push(...res.data)
      hasMore.value = res.data.length >= 50
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载更多失败:', error)
  } finally {
    searching.value = false
  }
}

// 点击搜索结果
const handleResultClick = (item) => {
  emit('jump-to-message', item)
  handleClose()
}

// 导出结果
const handleExportResults = () => {
  emit('export', searchResults.value)
  ElMessage.success('正在导出搜索结果...')
}

// 关闭
const handleClose = () => {
  emit('close')
}

// 监听 visible 变化
watch(() => props.visible, (val) => {
  if (val) {
    searchKeyword.value = ''
    searchResults.value = []
    hasSearched.value = false
    activeFilter.value = 'all'
    nextTick(() => {
      searchInputRef.value?.focus()
    })
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

.slide-enter-from .chat-search-panel,
.slide-leave-to .chat-search-panel {
  transform: translateX(100%);
}

.chat-search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
  backdrop-filter: blur(2px);
}

.chat-search-panel {
  width: 450px;
  height: 100%;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 30px rgba(0, 0, 0, 0.2);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

// 头部
.search-header {
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
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 20px;
      color: var(--dt-brand-color);
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

// 搜索输入区域
.search-input-area {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      padding: 8px 16px;

      &:hover, &.is-focus {
        box-shadow: 0 2px 12px rgba(0, 137, 255, 0.2);
      }
    }

    .el-input__prefix {
      color: var(--dt-text-tertiary);

      .material-icons-outlined {
        font-size: 18px;
      }
    }
  }

  .search-filters {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    overflow-x: auto;
    padding-bottom: 4px;

    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--dt-border);
      border-radius: 2px;
    }

    .filter-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 10px;
      background: var(--dt-bg-body);
      border: 1px solid var(--dt-border-light);
      border-radius: 20px;
      font-size: 12px;
      color: var(--dt-text-secondary);
      cursor: pointer;
      transition: all 0.2s;
      white-space: nowrap;

      .material-icons-outlined {
        font-size: 14px;
      }

      .filter-count {
        padding: 2px 6px;
        background: var(--dt-bg-hover);
        border-radius: 10px;
        font-size: 10px;
      }

      &:hover {
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
      }

      &.active {
        background: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
        color: #fff;

        .filter-count {
          background: rgba(255, 255, 255, 0.25);
        }
      }
    }
  }
}

// 搜索结果区域
.search-results {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border);
    border-radius: 3px;
  }
}

.loading-state,
.empty-initial,
.empty-no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);
  gap: 16px;

  .material-icons-outlined {
    font-size: 56px;
    opacity: 0.4;
  }

  p {
    margin: 0;
    font-size: 14px;
  }

  .hint {
    font-size: 12px;
    opacity: 0.7;
  }
}

.empty-initial {
  .empty-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: var(--dt-bg-body);
    display: flex;
    align-items: center;
    justify-content: center;

    .material-icons-outlined {
      font-size: 36px;
      color: var(--dt-brand-color);
      opacity: 0.5;
    }
  }

  .search-tips {
    padding: 12px 16px;
    background: var(--dt-bg-body);
    border-radius: 8px;
    font-size: 12px;
    text-align: center;

    .tip-title {
      color: var(--dt-text-secondary);
      font-weight: 500;
    }
  }
}

// 结果列表
.results-list {
  padding: 12px;

  .results-summary {
    padding: 8px 12px;
    margin-bottom: 12px;
    background: var(--dt-brand-bg);
    border-radius: 8px;
    font-size: 13px;
    color: var(--dt-text-secondary);

    strong {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }
}

.result-group {
  margin-bottom: 20px;

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
}

.result-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .result-avatar {
    flex-shrink: 0;
  }

  .result-content {
    flex: 1;
    min-width: 0;
  }

  .result-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .sender-name {
      font-size: 13px;
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    .message-time {
      font-size: 11px;
      color: var(--dt-text-tertiary);
      margin-left: auto;
    }
  }

  .result-text {
    font-size: 13px;
    color: var(--dt-text-secondary);
    line-height: 1.5;
    word-break: break-all;
    display: flex;
    align-items: center;
    gap: 4px;

    .material-icons-outlined {
      font-size: 16px;
      color: var(--dt-text-tertiary);
    }

    mark {
      background: #fff3cd;
      color: #856404;
      padding: 0 2px;
      border-radius: 2px;
    }

    .dark & mark {
      background: rgba(0, 137, 255, 0.2);
      color: #60a5fa;
    }
  }

  .result-action {
    flex-shrink: 0;
    width: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-tertiary);
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .result-action {
    opacity: 1;
  }
}

.load-more {
  padding: 12px;
  text-align: center;
  color: var(--dt-brand-color);
  font-size: 13px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: var(--dt-brand-bg);
  }
}

// 底部
.search-footer {
  display: flex;
  justify-content: center;
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }

  .el-button {
    .material-icons-outlined {
      font-size: 16px;
      margin-right: 4px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .chat-search-panel {
    width: 100%;
  }

  .search-filters {
    justify-content: flex-start;
  }
}
</style>
