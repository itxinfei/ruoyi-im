<template>
  <el-dialog
    v-model="visible"
    title="搜索消息"
    :width="600"
    :close-on-click-modal="false"
    @close="handleClose"
    class="message-search-dialog"
  >
    <div class="search-container">
      <!-- 搜索输入框 -->
      <div class="search-input-wrapper">
        <el-input
          ref="searchInput"
          v-model="searchKeyword"
          placeholder="搜索消息内容..."
          clearable
          @input="handleSearchInput"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 搜索类型筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="selectedType" size="small" @change="handleTypeChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="text">文本</el-radio-button>
          <el-radio-button label="image">图片</el-radio-button>
          <el-radio-button label="file">文件</el-radio-button>
          <el-radio-button label="voice">语音</el-radio-button>
          <el-radio-button label="video">视频</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results">
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>搜索中...</span>
        </div>

        <div v-else-if="searchResults.length === 0 && !hasSearched" class="empty-state">
          <el-icon><Search /></el-icon>
          <p>输入关键词搜索消息</p>
        </div>

        <div v-else-if="searchResults.length === 0 && hasSearched" class="empty-state">
          <el-icon><DocumentDelete /></el-icon>
          <p>未找到相关消息</p>
        </div>

        <div v-else class="result-list">
          <div class="result-info">找到 {{ total }} 条相关消息</div>
          <div
            v-for="item in searchResults"
            :key="item.id"
            class="result-item"
            @click="handleResultClick(item)"
          >
            <div class="result-header">
              <span class="sender-name">{{ item.senderName }}</span>
              <span class="message-time">{{ formatTime(item.timestamp) }}</span>
            </div>
            <div class="result-content">
              <!-- 文本消息 -->
              <div v-if="item.type === 'text'" class="content-text" v-html="highlightKeyword(item.content)"></div>
              <!-- 图片消息 -->
              <div v-else-if="item.type === 'image'" class="content-image">
                <img :src="item.content" alt="图片" />
                <span class="content-type-label">[图片]</span>
              </div>
              <!-- 文件消息 -->
              <div v-else-if="item.type === 'file'" class="content-file">
                <el-icon><Document /></el-icon>
                <span>{{ item.content?.name || '文件' }}</span>
              </div>
              <!-- 语音消息 -->
              <div v-else-if="item.type === 'voice'" class="content-voice">
                <el-icon><Microphone /></el-icon>
                <span>{{ item.content?.duration || 0 }}秒</span>
              </div>
              <!-- 视频消息 -->
              <div v-else-if="item.type === 'video'" class="content-video">
                <el-icon><VideoCamera /></el-icon>
                <span>[视频]</span>
              </div>
              <!-- 其他类型 -->
              <div v-else class="content-other">
                <span>{{ item.content }}</span>
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <el-button text @click="loadMore">加载更多</el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Search, Loading, DocumentDelete, Document, Microphone, VideoCamera } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  sessionId: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'select-message'])

const store = useStore()
const searchInput = ref(null)
const searchKeyword = ref('')
const selectedType = ref('')
const searchResults = ref([])
const loading = ref(false)
const hasSearched = ref(false)
const total = ref(0)
const hasMore = ref(false)
const currentPage = ref(1)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

// 防抖计时器
let searchTimer = null

// 监听对话框打开，自动聚焦搜索框
watch(visible, (val) => {
  if (val) {
    nextTick(() => {
      searchInput.value?.focus()
    })
  } else {
    resetSearch()
  }
})

// 重置搜索状态
const resetSearch = () => {
  searchKeyword.value = ''
  selectedType.value = ''
  searchResults.value = []
  hasSearched.value = false
  total.value = 0
  hasMore.value = false
  currentPage.value = 1
}

// 处理搜索输入（防抖）
const handleSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    if (searchKeyword.value.trim()) {
      handleSearch()
    }
  }, 500)
}

// 处理类型变化
const handleTypeChange = () => {
  if (searchKeyword.value.trim()) {
    handleSearch()
  }
}

// 执行搜索
const handleSearch = async (page = 1) => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  try {
    const result = await store.dispatch('im/searchMessages', {
      sessionId: props.sessionId,
      keyword,
      messageType: selectedType.value,
      page,
      pageSize: 20,
    })

    if (page === 1) {
      searchResults.value = result.messages
    } else {
      searchResults.value.push(...result.messages)
    }

    total.value = result.total
    hasMore.value = result.hasMore
    hasSearched.value = true
    currentPage.value = page
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  handleSearch(currentPage.value + 1)
}

// 高亮关键词
const highlightKeyword = (text) => {
  if (!searchKeyword.value) return text
  const keyword = searchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight-keyword">$1</span>')
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  if (diff < 48 * 60 * 60 * 1000 && date.getDate() === now.getDate() - 1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 更早
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' +
    date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 点击搜索结果
const handleResultClick = (message) => {
  emit('select-message', message)
  visible.value = false
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.message-search-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
  }
}

.search-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-input-wrapper {
  :deep(.el-input-group__append) {
    background-color: $primary-color;
    border-color: $primary-color;
    color: white;

    .el-button {
      color: white;
    }
  }
}

.filter-bar {
  display: flex;
  justify-content: center;
}

.search-results {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid $border-color-light;
  border-radius: 8px;
  padding: 8px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: $text-secondary;
  gap: 12px;

  .el-icon {
    font-size: 48px;
    color: $text-tertiary;
  }
}

.result-list {
  .result-info {
    padding: 8px 12px;
    color: $text-secondary;
    font-size: 13px;
    text-align: center;
    border-bottom: 1px solid $border-color-lighter;
    margin-bottom: 8px;
  }

  .result-item {
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 8px;

    &:hover {
      background-color: #f5f7fa;
    }

    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .sender-name {
        font-size: 13px;
        font-weight: 500;
        color: $text-primary;
      }

      .message-time {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    .result-content {
      .content-text {
        color: $text-secondary;
        font-size: 14px;
        line-height: 1.5;
        word-break: break-all;

        :deep(.highlight-keyword) {
          background-color: #fffbdd;
          padding: 0 2px;
          border-radius: 2px;
        }
      }

      .content-image {
        display: flex;
        align-items: center;
        gap: 8px;

        img {
          width: 40px;
          height: 40px;
          object-fit: cover;
          border-radius: 4px;
        }

        .content-type-label {
          color: $text-secondary;
          font-size: 13px;
        }
      }

      .content-file,
      .content-voice,
      .content-video {
        display: flex;
        align-items: center;
        gap: 8px;
        color: $text-secondary;
        font-size: 13px;

        .el-icon {
          font-size: 18px;
          color: $primary-color;
        }
      }

      .content-other {
        color: $text-secondary;
        font-size: 13px;
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 12px;
    border-top: 1px solid $border-color-lighter;
    margin-top: 8px;
  }
}
</style>
