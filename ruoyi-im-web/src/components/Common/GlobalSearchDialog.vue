<template>
  <el-dialog
    v-model="visible"
    title="全局搜索"
    :width="dialogWidth"
    :show-close="true"
    :close-on-click-modal="true"
    class="classic-search-dialog"
    destroy-on-close
    append-to-body
    :modal-append-to-body="true"
    :lock-scroll="true"
  >
    <div class="classic-container">
      <!-- 搜索栏区：现代化设计 -->
      <div class="modern-search-bar">
        <div class="search-container">
          <el-input
            ref="searchInputRef"
            v-model="searchKeyword"
            placeholder="搜索联系人、群组、聊天记录、文件..."
            class="modern-input"
            clearable
            autofocus
            @input="handleInput"
            @keyup.enter="handleSearch"
            @focus="handleFocus"
            @blur="handleBlur"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #append>
              <el-button class="search-button" @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
          
          <!-- 快捷键提示 -->
          <div class="shortcut-hints">
            <span class="hint-item">
              <kbd>⌘</kbd><kbd>K</kbd>
              <span class="hint-text">快速搜索</span>
            </span>
          </div>
          
          <!-- 自动补全建议 -->
          <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
            <div
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              :class="{ active: suggestionIndex === index }"
              @click="selectSuggestion(suggestion)"
              @mouseenter="suggestionIndex = index"
            >
              <el-icon class="suggestion-icon"><Search /></el-icon>
              <span class="suggestion-text" v-html="highlightSuggestion(suggestion)"></span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="classic-body">
        <!-- 左侧分类：方正列表 -->
        <div class="classic-aside" :style="{ width: sidebarWidth }">
          <div
            v-for="cat in categories"
            :key="cat.key"
            class="cat-tab"
            :class="{ active: searchType === cat.key }"
            @click="handleCategoryChange(cat.key)"
          >
            <el-icon class="cat-icon">{{ getCategoryIcon(cat.key) }}</el-icon>
            <span class="label">{{ cat.label }}</span>
            <span v-if="getCategoryCount(cat.key)" class="count-badge">{{ getCategoryCount(cat.key) }}</span>
          </div>
        </div>

        <!-- 右侧结果：现代化列表 -->
        <div class="modern-main scrollbar-thin">
          <!-- 骨架屏加载状态 -->
          <div v-if="searching" class="skeleton-container">
            <div v-for="i in 5" :key="i" class="skeleton-item">
              <div class="skeleton-avatar"></div>
              <div class="skeleton-content">
                <div class="skeleton-line skeleton-title"></div>
                <div class="skeleton-line skeleton-desc"></div>
                <div class="skeleton-line skeleton-meta"></div>
              </div>
            </div>
          </div>

          <!-- 默认状态：历史记录和热门搜索 -->
          <div v-else-if="!searchKeyword" class="default-state">
            <!-- 欢迎卡片 -->
            <div class="welcome-card">
              <div class="welcome-icon">
                <el-icon class="icon-large"><Search /></el-icon>
              </div>
              <h3 class="welcome-title">智能搜索</h3>
              <p class="welcome-desc">快速查找联系人、群组、消息和文件</p>
              <div class="quick-actions">
                <div class="action-item" @click="handleQuickSearch('@')">
                  <el-icon><User /></el-icon>
                  <span>@联系人</span>
                </div>
                <div class="action-item" @click="handleQuickSearch('#')">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>#群组</span>
                </div>
                <div class="action-item" @click="handleQuickSearch('file:')">
                  <el-icon><Document /></el-icon>
                  <span>文件</span>
                </div>
              </div>
            </div>

            <!-- 最近搜索历史 -->
            <div v-if="historyKeywords.length > 0" class="history-section">
              <div class="section-header">
                <div class="section-title-wrapper">
                  <el-icon class="section-icon"><Timer /></el-icon>
                  <h3 class="section-title">最近搜索</h3>
                </div>
                <el-button 
                  type="text" 
                  size="small" 
                  class="clear-history-btn"
                  @click="clearAllHistory"
                >
                  <el-icon><Delete /></el-icon>
                  清空
                </el-button>
              </div>
              <div class="history-grid">
                <div 
                  v-for="kw in historyKeywords" 
                  :key="kw" 
                  class="history-item"
                  @click="searchKeyword = kw; handleSearch()"
                >
                  <el-icon class="history-icon"><Clock /></el-icon>
                  <span class="history-text">{{ kw }}</span>
                  <el-icon class="close-icon" @click.stop="removeHistory(kw)"><Close /></el-icon>
                </div>
              </div>
            </div>
            
            <!-- 热门搜索 -->
            <div class="trending-section">
              <div class="section-title-wrapper">
                <el-icon class="section-icon"><TrendCharts /></el-icon>
                <h3 class="section-title">热门搜索</h3>
              </div>
              <div class="trending-tags">
                <span 
                  v-for="(tag, index) in trendingTags" 
                  :key="index"
                  class="trending-tag"
                  @click="searchKeyword = tag; handleSearch()"
                >
                  <span class="tag-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <!-- 搜索结果列表 -->
          <div v-else-if="hasResults" class="results-container">
            <div v-for="(section, type) in filteredResultSections" :key="type" class="result-section">
              <div class="section-header">
                <div class="section-title with-icon">
                  <el-icon class="section-icon">{{ getSectionIcon(type) }}</el-icon>
                  {{ section.title }}
                  <span class="result-count">({{ section.data.length }})</span>
                </div>
                <el-button
                  v-if="section.data.length > 5"
                  type="text"
                  size="small"
                  class="view-more-btn"
                  @click="viewMore(type)"
                >
                  查看更多
                </el-button>
              </div>
              
              <div class="result-list">
                <div
                  v-for="(item, index) in section.data.slice(0, 5)"
                  :key="item.id || index"
                  class="result-card"
                  :class="'result-' + index"
                  @click="handleItemClick(type, item)"
                >
                  <!-- 头像/图标 -->
                  <div class="result-avatar">
                    <DingtalkAvatar
                      v-if="type !== 'files'"
                      :name="item.name || item.friendName || item.senderName"
                      :src="item.avatar || item.friendAvatar"
                      :size="44"
                      shape="square"
                      custom-class="result-avatar-img"
                    />
                    <div v-else class="file-icon-container">
                      <el-icon class="file-icon"><Document /></el-icon>
                      <span class="file-ext">{{ getFileExtension(item.fileName) }}</span>
                    </div>
                  </div>

                  <!-- 信息 -->
                  <div class="result-info">
                    <div class="result-header">
                      <h4 class="result-title" v-html="highlight(item.name || item.friendName || item.senderName || item.fileName)"></h4>
                      <span class="result-time">{{ formatTime(item.timestamp) }}</span>
                    </div>
                    <div class="result-desc" v-html="highlight(item.content || item.remark || item.departmentName || (item.memberCount ? item.memberCount + '人' : ''))"></div>
                    <div class="result-meta">
                      <span v-if="type === 'messages'" class="meta-tag message-tag">
                        <el-icon><ChatLineRound /></el-icon>
                        消息
                      </span>
                      <span v-else-if="type === 'files'" class="meta-tag file-tag">
                        <el-icon><Folder /></el-icon>
                        文件
                      </span>
                      <span v-else-if="type === 'contacts'" class="meta-tag contact-tag">
                        <el-icon><User /></el-icon>
                        联系人
                      </span>
                      <span v-else-if="type === 'groups'" class="meta-tag group-tag">
                        <el-icon><ChatDotRound /></el-icon>
                        群组
                      </span>
                    </div>
                  </div>
                  
                  <!-- 快捷操作 -->
                  <div class="result-actions">
                    <el-button
                      type="text"
                      size="small"
                      class="action-btn"
                      @click.stop="handlePreview(type, item)"
                    >
                      <el-icon><View /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="searchKeyword && !searching" class="empty-state">
            <div class="empty-icon-wrapper">
              <el-icon class="empty-icon"><Search /></el-icon>
            </div>
            <h3 class="empty-title">未找到相关结果</h3>
            <p class="empty-desc">没有找到与"<strong>{{ searchKeyword }}</strong>"相关的内容</p>
            <div class="empty-suggestions">
              <p class="suggestion-title">搜索建议：</p>
              <div class="suggestion-list">
                <span class="suggestion-item" @click="searchKeyword = ''; resetSearch()">清除搜索关键词</span>
                <span class="suggestion-item" @click="handleCategoryChange('ALL')">切换到"全部内容"分类</span>
                <span class="suggestion-item" @click="searchKeyword = searchKeyword.slice(0, -1)">删除最后一个字符</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 页脚 -->
      <div class="classic-footer">
        <div class="footer-shortcuts">
          <span class="shortcut-item">
            <kbd>Enter</kbd> 搜索/查看
          </span>
          <span class="shortcut-item">
            <kbd>Esc</kbd> 关闭
          </span>
          <span class="shortcut-item">
            <kbd>↑</kbd><kbd>↓</kbd> 导航
          </span>
          <span class="shortcut-item">
            <kbd>←</kbd><kbd>→</kbd> 切换分类
          </span>
        </div>
        <div v-if="totalCount > 0" class="result-count">
          <el-icon class="count-icon"><DataLine /></el-icon>
          找到 <strong>{{ totalCount }}</strong> 条结果
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { getJSON, setJSON } from '@/utils/storage'

import { Search, Document, Loading, Timer, Close, User, ChatDotRound, ChatLineRound,
         Folder, View, TrendCharts, Clock, Delete, ArrowRight, DataLine } from '@element-plus/icons-vue'
import { globalSearch } from '@/api/im/globalSearch'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatTime } from '@/utils/format'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'select-message', 'select-contact', 'select-group', 'select-file'])

const visible = ref(false)
const searchKeyword = ref('')
const searchType = ref('ALL')
const searchResult = ref(null)
const searching = ref(false)
const searchInputRef = ref(null)
const historyKeywords = ref(getJSON('search_history', []))

// 自动补全和搜索建议
const suggestions = ref([])
const showSuggestions = ref(false)
const suggestionIndex = ref(-1)

// 热门搜索标签
const trendingTags = ref([
  '项目进度',
  '会议纪要',
  '周报',
  '产品需求',
  '技术方案',
  'bug修复',
  '测试报告',
  '发布计划'
])

// 响应式尺寸
const windowWidth = ref(window.innerWidth)

const dialogWidth = computed(() => {
  if (windowWidth.value < 576) return '95%'
  if (windowWidth.value < 768) return '92%'
  if (windowWidth.value < 992) return '850px'
  return '900px'
})

const dialogHeight = computed(() => {
  if (windowWidth.value < 576) return '80vh'
  if (windowWidth.value < 768) return '85vh'
  return '620px'
})

const sidebarWidth = computed(() => {
  if (windowWidth.value < 768) return '120px'
  return '160px'
})

const categories = [
  { label: '全部内容', key: 'ALL' },
  { label: '联系人', key: 'CONTACT' },
  { label: '群组', key: 'GROUP' },
  { label: '消息记录', key: 'MESSAGE' },
  { label: '文件', key: 'FILE' }
]

// 监听弹窗显示
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    // 强制清理之前的状态，确保重新聚焦
    searchResult.value = null
    nextTick(() => {
      // 深度聚焦逻辑
      const input = searchInputRef.value?.$el?.querySelector('input')
      if (input) {
        input.focus()
        input.select()
      }
    })
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

const totalCount = computed(() => {
  if (!searchResult.value) return 0
  return (searchResult.value.contacts?.length || 0) + 
         (searchResult.value.groups?.length || 0) + 
         (searchResult.value.messages?.length || 0) + 
         (searchResult.value.files?.length || 0)
})

const hasResults = computed(() => totalCount.value > 0)

const filteredResultSections = computed(() => {
  if (!searchResult.value) return {}
  const sections = {
    contacts: { title: '联系人', data: searchResult.value.contacts || [] },
    groups: { title: '群组', data: searchResult.value.groups || [] },
    messages: { title: '消息记录', data: searchResult.value.messages || [] },
    files: { title: '文件', data: searchResult.value.files || [] }
  }
  if (searchType.value !== 'ALL') {
    const keyMap = { CONTACT: 'contacts', GROUP: 'groups', MESSAGE: 'messages', FILE: 'files' }
    const targetKey = keyMap[searchType.value]
    return { [targetKey]: sections[targetKey] }
  }
  return Object.fromEntries(Object.entries(sections).filter(([_, s]) => s.data.length > 0))
})

const getCategoryCount = (key) => {
  if (!searchResult.value || key === 'ALL') return 0
  const keyMap = { CONTACT: 'contacts', GROUP: 'groups', MESSAGE: 'messages', FILE: 'files' }
  return searchResult.value[keyMap[key]]?.length || 0
}

// 获取分类图标
const getCategoryIcon = (key) => {
  const iconMap = {
    'ALL': 'Search',
    'CONTACT': 'User',
    'GROUP': 'ChatDotRound',
    'MESSAGE': 'ChatLineRound',
    'FILE': 'Document'
  }
  return iconMap[key] || 'Search'
}

// 获取文件扩展名
const getFileExtension = (fileName) => {
  if (!fileName) return ''
  const parts = fileName.split('.')
  return parts.length > 1 ? parts[parts.length - 1].toUpperCase() : ''
}

// 快速搜索
const handleQuickSearch = (prefix) => {
  searchKeyword.value = prefix
  nextTick(() => {
    searchInputRef.value?.$el?.querySelector('input')?.focus()
  })
}

// 查看更多
const viewMore = (type) => {
  ElMessage.info('加载更多功能开发中...')
}

// 预览
const handlePreview = (type, item) => {
  ElMessage.info('预览功能开发中...')
}

// 搜索结果缓存
const searchCache = ref(new Map())
let timer = null

const handleInput = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResult.value = null
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  
  // 生成搜索建议
  generateSuggestions()
  
  // 防抖处理
  clearTimeout(timer)
  timer = setTimeout(async () => {
    // 检查缓存
    const cacheKey = `${keyword}_${searchType.value}`
    if (searchCache.value.has(cacheKey)) {
      searchResult.value = searchCache.value.get(cacheKey)
      searching.value = false
      return
    }
    
    await handleSearch()
  }, 300)
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  
  searching.value = true
  try {
    const cacheKey = `${keyword}_${searchType.value}`
    
    // 检查缓存
    if (searchCache.value.has(cacheKey)) {
      searchResult.value = searchCache.value.get(cacheKey)
      return
    }
    
    const res = await globalSearch({ keyword, searchType: searchType.value, pageNum: 1, pageSize: 50 })
    const result = res.data || {}
    
    // 缓存结果，设置过期时间
    searchCache.value.set(cacheKey, result)
    
    // 限制缓存大小
    if (searchCache.value.size > 20) {
      const keys = Array.from(searchCache.value.keys())
      for (let i = 0; i < 5; i++) {
        searchCache.value.delete(keys[i])
      }
    }
    
    searchResult.value = result
    
    // 更新历史记录
    if (!historyKeywords.value.includes(keyword)) {
      historyKeywords.value.unshift(keyword)
      historyKeywords.value = historyKeywords.value.slice(0, 10)
      setJSON('search_history', historyKeywords.value)
    }
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    searching.value = false
  }
}

const handleCategoryChange = (key) => {
  searchType.value = key
  if (searchKeyword.value) handleSearch()
}

const highlight = (text) => {
  if (!text || !searchKeyword.value) return text
  const reg = new RegExp(`(${searchKeyword.value})`, 'gi')
  return String(text).replace(reg, '<span style="color: #0089FF; font-weight: bold;">$1</span>')
}

const handleItemClick = (type, item) => {
  const eventMap = { contacts: 'select-contact', groups: 'select-group', messages: 'select-message', files: 'select-file' }
  emit(eventMap[type], item)
  visible.value = false
}

const removeHistory = (kw) => {
  historyKeywords.value = historyKeywords.value.filter(k => k !== kw)
  setJSON('search_history', historyKeywords.value)
}

// 自动补全和搜索建议相关方法
const handleFocus = () => {
  if (searchKeyword.value) {
    generateSuggestions()
  }
}

const handleBlur = () => {
  // 延迟隐藏建议，以便点击建议时能够触发点击事件
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const generateSuggestions = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  
  // 从历史记录中生成建议
  const historySuggestions = historyKeywords.value
    .filter(item => item.toLowerCase().includes(keyword.toLowerCase()))
    .slice(0, 5)
  
  // 添加一些默认建议
  const defaultSuggestions = [
    `@${keyword}`,
    `#${keyword}`,
    `文件: ${keyword}`,
    `群组: ${keyword}`
  ].filter(item => item.toLowerCase().includes(keyword.toLowerCase()))
  
  suggestions.value = [...historySuggestions, ...defaultSuggestions].slice(0, 8)
  showSuggestions.value = suggestions.value.length > 0
  suggestionIndex.value = -1
}

const selectSuggestion = (suggestion) => {
  searchKeyword.value = suggestion
  showSuggestions.value = false
  handleSearch()
}

const highlightSuggestion = (text) => {
  if (!text || !searchKeyword.value) return text
  const reg = new RegExp(`(${searchKeyword.value})`, 'gi')
  return String(text).replace(reg, '<span class="highlight-text">$1</span>')
}

// 获取分类图标
const getSectionIcon = (type) => {
  const iconMap = {
    contacts: 'User',
    groups: 'ChatDotRound',
    messages: 'ChatLineRound',
    files: 'Document'
  }
  return iconMap[type] || 'Search'
}

// 监听键盘事件，支持上下箭头选择建议
const handleKeyDown = (event) => {
  if (!showSuggestions.value && !visible.value) return

  // 快捷键映射到数字键 1-5 快速切换搜索类型
  const categoryKeys = ['1', '2', '3', '4', '5']
  if (event.key >= '1' && event.key <= '5' && event.altKey) {
    event.preventDefault()
    const typeMap = { '1': 'ALL', '2': 'CONTACT', '3': 'GROUP', '4': 'MESSAGE', '5': 'FILE' }
    handleCategoryChange(typeMap[event.key])
    return
  }

  switch (event.key) {
    case 'ArrowDown':
      if (showSuggestions.value) {
        event.preventDefault()
        suggestionIndex.value = (suggestionIndex.value + 1) % suggestions.value.length
      } else {
        // 在结果列表中向下导航
        event.preventDefault()
      }
      break
    case 'ArrowUp':
      if (showSuggestions.value) {
        event.preventDefault()
        suggestionIndex.value = suggestionIndex.value <= 0 ? suggestions.value.length - 1 : suggestionIndex.value - 1
      } else {
        // 在结果列表中向上导航
        event.preventDefault()
      }
      break
    case 'ArrowLeft':
      // 切换到上一个搜索类型
      event.preventDefault()
      const typeOrder = ['ALL', 'CONTACT', 'GROUP', 'MESSAGE', 'FILE']
      const currentIndex = typeOrder.indexOf(searchType.value)
      const prevIndex = currentIndex <= 0 ? typeOrder.length - 1 : currentIndex - 1
      handleCategoryChange(typeOrder[prevIndex])
      break
    case 'ArrowRight':
      // 切换到下一个搜索类型
      event.preventDefault()
      const nextIndex = (typeOrder.indexOf(searchType.value) + 1) % typeOrder.length
      handleCategoryChange(typeOrder[nextIndex])
      break
    case 'Enter':
      if (showSuggestions.value && suggestionIndex.value >= 0) {
        event.preventDefault()
        selectSuggestion(suggestions.value[suggestionIndex.value])
      } else {
        // 执行搜索
        event.preventDefault()
        handleSearch()
      }
      break
    case 'Escape':
      if (showSuggestions.value) {
        showSuggestions.value = false
      } else {
        // 关闭搜索弹窗
        visible.value = false
        emit('update:modelValue', false)
      }
      break
    case 'Tab':
      if (!event.shiftKey) {
        event.preventDefault()
        const typeOrder = ['ALL', 'CONTACT', 'GROUP', 'MESSAGE', 'FILE']
        const nextIndex = (typeOrder.indexOf(searchType.value) + 1) % typeOrder.length
        handleCategoryChange(typeOrder[nextIndex])
      }
      break
    case 'Delete':
    case 'Backspace':
      // 如果在历史记录中，删除最后一个搜索词
      if (!searchKeyword.value && historyKeywords.value.length > 0 && document.activeElement?.classList?.contains('history-item')) {
        event.preventDefault()
        // 需要实现删除特定历史记录项的功能
      }
      break
  }
}

// 清空所有历史记录
const clearAllHistory = () => {
  historyKeywords.value = []
  setJSON('search_history', historyKeywords.value)
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchResult.value = null
  suggestions.value = []
  showSuggestions.value = false
}

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
  
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
})

// 清理事件监听
onUnmounted(() => {
  window.removeEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped lang="scss">
.classic-search-dialog {
  pointer-events: auto !important;

  :deep(.el-dialog) {
    padding: 0;
    border-radius: 16px;
    overflow: hidden;
    position: relative;
    z-index: 3000;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
    animation: dialogFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.98);
    
    .dark & {
      background: rgba(15, 23, 42, 0.98);
    }
    
    @keyframes dialogFadeIn {
      from {
        opacity: 0;
        transform: scale(0.95) translateY(-10px);
      }
      to {
        opacity: 1;
        transform: scale(1) translateY(0);
      }
    }
  }
}

.classic-container {
  display: flex;
  flex-direction: column;
  height: 620px;
  background: #fff;
  .dark & { background: #0f172a; }
}

.modern-search-bar {
  padding: 20px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
  .dark & { border-color: #1e293b; background: #1e293b; }

  .search-container {
    position: relative;
    width: 100%;
  }

  .modern-input {
    width: 100%;
    
    :deep(input) {
      cursor: text !important;
      pointer-events: auto !important;
      font-size: 16px;
      padding: 0 52px 0 44px;
      height: 48px;
      line-height: 48px;
      border-radius: 24px;
    }

    .search-icon {
      font-size: 18px;
      color: #94a3b8;
      transition: all 0.2s ease;
      margin-left: 12px;
    }

    :deep(.el-input__wrapper) {
      border-radius: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.2s ease;
      border: 1px solid transparent;
      background: #f8fafc;
      
      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
        background: #f1f5f9;
      }
      
      &.is-focus { 
        box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.1), 0 2px 8px rgba(0, 0, 0, 0.08);
        border-color: #0089FF;
        background: #fff;
      }
      
      .dark & { 
        background: #1e293b; 
        border-color: #334155;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        
        &:hover {
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
          background: #2563eb;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.2), 0 2px 8px rgba(0, 0, 0, 0.2);
          background: #0f172a;
        }
      }
    }
    
    .search-button {
      background: #0089FF;
      color: #fff;
      border: none;
      border-radius: 20px;
      width: 40px;
      height: 40px;
      margin-right: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      &:hover {
        background: #0958d9;
        transform: scale(1.05);
      }
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
  
  // 快捷键提示
  .shortcut-hints {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    
    .hint-item {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #94a3b8;
      
      .dark & {
        color: #64748b;
      }
      
      kbd {
        background: #f1f5f9;
        border: 1px solid #e2e8f0;
        padding: 2px 6px;
        border-radius: 4px;
        margin-right: 4px;
        font-family: monospace;
        font-size: 11px;
        
        .dark & {
          background: #1e293b;
          border-color: #334155;
          color: #cbd5e1;
        }
      }
      
      .hint-text {
        margin-left: 4px;
      }
    }
  }
  
  // 自动补全建议
  .suggestions-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    margin-top: 8px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    z-index: 1000;
    animation: dropdownFadeIn 0.2s ease-out;
    
    .dark & {
      background: #0f172a;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    }
    
    @keyframes dropdownFadeIn {
      from {
        opacity: 0;
        transform: translateY(-8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  }
  
  .suggestion-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 14px;
    color: #1e293b;
    
    &:hover,
    &.active {
      background: #f0f9ff;
      
      .dark & {
        background: #1e40af;
      }
    }
    
    .dark & {
      color: #f1f5f9;
    }
    
    .suggestion-icon {
      font-size: 14px;
      color: #94a3b8;
      margin-right: 12px;
      
      .dark & {
        color: #94a3b8;
      }
    }
    
    .suggestion-text {
      flex: 1;
      
      .highlight-text {
        color: #0089FF;
        font-weight: 600;
      }
    }
    
    .arrow-icon {
      font-size: 12px;
      color: #94a3b8;
      opacity: 0;
      transition: all 0.2s ease;
      
      .dark & {
        color: #64748b;
      }
    }
    
    &:hover .arrow-icon {
      opacity: 1;
      transform: translateX(4px);
    }
  }

  // 移动端优化
  @media (max-width: 768px) {
    padding: 16px;
    
    .modern-input {
      :deep(input) {
        font-size: 16px;
        height: 44px;
        line-height: 44px;
        padding: 0 44px 0 36px;
      }
    }
    
    .search-icon {
      font-size: 16px;
      margin-left: 8px;
    }
    
    .search-button {
      width: 36px;
      height: 36px;
      margin-right: 2px;
    }
    
    .shortcut-hints {
      display: none;
    }
    
    .suggestions-dropdown {
      margin-top: 6px;
      border-radius: 8px;
      
      .suggestion-item {
        padding: 10px 12px;
        font-size: 13px;
        
        .suggestion-icon {
          font-size: 12px;
          margin-right: 8px;
        }
      }
    }
  }
  
  // 小屏幕手机优化
  @media (max-width: 480px) {
    padding: 12px;
    
    .modern-input {
      :deep(input) {
        font-size: 15px;
        height: 42px;
        line-height: 42px;
        padding: 0 38px 0 32px;
      }
    }
    
    .search-icon {
      font-size: 15px;
      margin-left: 6px;
    }
    
    .search-button {
      width: 32px;
      height: 32px;
    }
  }
}

.classic-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.classic-aside {
  width: 160px;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
  padding: 8px 0;
  flex-shrink: 0;
  transition: width 0.3s ease;
  .dark & { background: #1e293b; border-color: #334155; }

  .cat-tab {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    font-size: 13px;
    color: #64748b;
    transition: all 0.2s ease;
    position: relative;
    animation: categorySwitch 0.3s ease-out forwards;

    &:hover { 
      background: #f1f5f9; 
      color: #1e293b; 
      transform: translateX(4px);
      .dark & { background: #0f172a; color: #f1f5f9; } 
    }
    &.active {
      background: #fff;
      color: #0089FF;
      font-weight: 600;
      border-right: 3px solid #0089FF;
      box-shadow: inset 0 0 0 1px #e2e8f0;
      transform: translateX(4px);
      .dark & { background: #0f172a; }
      
      .cat-icon {
        color: #0089FF;
      }
    }
    
    @keyframes categorySwitch {
      from {
        opacity: 0;
        transform: translateX(-8px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    .cat-icon {
      font-size: 16px;
      transition: all 0.2s ease;
    }

    .label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .count-badge {
      font-size: 11px;
      background: #e2e8f0;
      padding: 2px 6px;
      border-radius: 10px;
      font-weight: 500;
      min-width: 20px;
      text-align: center;
      .dark & { background: #334155; color: #cbd5e1; }
    }
  }
}

.modern-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  
  // 骨架屏加载状态
  .skeleton-container {
    padding: 20px 0;
    
    .skeleton-item {
      display: flex;
      gap: 16px;
      padding: 16px;
      margin-bottom: 12px;
      border-radius: 12px;
      background: #f8fafc;
      
      .dark & {
        background: #1e293b;
      }
      
      .skeleton-avatar {
        width: 44px;
        height: 44px;
        border-radius: 8px;
        background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
        background-size: 200% 100%;
        animation: skeletonShimmer 1.5s infinite;
        
        .dark & {
          background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
        }
      }
      
      .skeleton-content {
        flex: 1;
        
        .skeleton-line {
          height: 14px;
          border-radius: 4px;
          background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
          background-size: 200% 100%;
          animation: skeletonShimmer 1.5s infinite;
          margin-bottom: 8px;
          
          .dark & {
            background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
          }
          
          &.skeleton-title {
            width: 60%;
          }
          
          &.skeleton-desc {
            width: 80%;
          }
          
          &.skeleton-meta {
            width: 40%;
          }
        }
      }
    }
    
    @keyframes skeletonShimmer {
      0% {
        background-position: 200% 0;
      }
      100% {
        background-position: -200% 0;
      }
    }
  }
  
  // 欢迎卡片
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 32px 24px;
    text-align: center;
    color: #fff;
    margin-bottom: 24px;
    animation: welcomeFadeIn 0.5s ease-out;
    
    @keyframes welcomeFadeIn {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
    
    .welcome-icon {
      font-size: 48px;
      margin-bottom: 16px;
      opacity: 0.9;
    }
    
    .welcome-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }
    
    .welcome-desc {
      font-size: 14px;
      opacity: 0.8;
      margin: 0 0 24px 0;
    }
    
    .quick-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
      flex-wrap: wrap;
      
      .action-item {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20px;
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 13px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
          transform: translateY(-2px);
        }
      }
    }
  }
  
  // 加载状态
  .loading-state {
    min-height: 400px;
    
    .loading-text {
      font-size: 14px;
      color: #64748b;
      margin-top: 8px;
      
      .dark & {
        color: #94a3b8;
      }
    }
  }
  
  // 默认状态
  .default-state {
    .history-section,
    .trending-section {
      margin-bottom: 32px;
    }
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .section-title-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .section-icon {
          font-size: 18px;
          color: #0089FF;
        }
      }
      
      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
        
        .dark & {
          color: #f1f5f9;
        }
      }
      
      .clear-history-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #64748b;
        font-size: 13px;
        
        &:hover {
          color: #ef4444;
        }
        
        .dark & {
          color: #94a3b8;
          
          &:hover {
            color: #f87171;
          }
        }
      }
    }
    
    // 历史记录
    .history-grid {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .history-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        border-radius: 10px;
        cursor: pointer;
        font-size: 14px;
        color: #475569;
        transition: all 0.2s ease;
        background: #f8fafc;
        
        .dark & {
          background: #1e293b;
          color: #cbd5e1;
        }
        
        &:hover {
          background: #f1f5f9;
          transform: translateX(4px);
          
          .dark & {
            background: #2563eb;
          }
          
          .close-icon {
            opacity: 1;
          }
        }
        
        .history-icon {
          font-size: 14px;
          color: #94a3b8;
          margin-right: 12px;
          flex-shrink: 0;
          
          .dark & {
            color: #94a3b8;
          }
        }
        
        .history-text {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .close-icon {
          opacity: 0;
          font-size: 14px;
          padding: 4px;
          border-radius: 4px;
          transition: all 0.2s ease;
          color: #64748b;
          
          &:hover { 
            background: rgba(239, 68, 68, 0.1);
            color: #ef4444; 
          }
          
          .dark & {
            color: #94a3b8;
            
            &:hover {
              background: rgba(248, 113, 113, 0.2);
              color: #f87171;
            }
          }
        }
      }
    }
    
    // 热门搜索
    .trending-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      
      .trending-tag {
        display: inline-flex;
        align-items: center;
        padding: 8px 16px;
        border-radius: 20px;
        background: #f8fafc;
        color: #475569;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s ease;
        
        .dark & {
          background: #1e293b;
          color: #cbd5e1;
        }
        
        &:hover {
          background: #f0f9ff;
          color: #0089FF;
          transform: translateY(-2px);
          box-shadow: 0 2px 8px rgba(0, 137, 255, 0.15);
          
          .dark & {
            background: #1e40af;
            box-shadow: 0 2px 8px rgba(0, 137, 255, 0.3);
          }
        }
        
        .tag-rank {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 18px;
          height: 18px;
          border-radius: 9px;
          background: #0089FF;
          color: #fff;
          font-size: 11px;
          font-weight: bold;
          margin-right: 8px;
          
          &.rank-1 { background: #ef4444; }
          &.rank-2 { background: #f97316; }
          &.rank-3 { background: #eab308; }
        }
      }
    }
  }
  
  // 搜索结果
  .results-container {
    .result-section {
      margin-bottom: 24px;
      
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          display: flex;
          align-items: center;
          
          .dark & {
            color: #f1f5f9;
          }
          
          &.with-icon {
            .section-icon {
              font-size: 18px;
              color: #0089FF;
              margin-right: 8px;
            }
            
            .result-count {
              font-size: 13px;
              font-weight: 400;
              color: #64748b;
              margin-left: 8px;
              
              .dark & {
                color: #94a3b8;
              }
            }
          }
        }
        
        .view-more-btn {
          color: #0089FF;
          font-size: 13px;
          
          &:hover {
            color: #0958d9;
          }
          
          .dark & {
            color: #60a5fa;
            
            &:hover {
              color: #93c5fd;
            }
          }
        }
      }
      
      .result-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .result-card {
          display: flex;
          gap: 16px;
          padding: 16px;
          border-radius: 12px;
          background: #f8fafc;
          cursor: pointer;
          transition: all 0.2s ease;
          animation: resultFadeIn 0.3s ease-out forwards;
          opacity: 0;
          position: relative;
          
          // 为每个结果卡片添加不同的动画延迟
          &.result-0 { animation-delay: 0.05s; }
          &.result-1 { animation-delay: 0.1s; }
          &.result-2 { animation-delay: 0.15s; }
          &.result-3 { animation-delay: 0.2s; }
          &.result-4 { animation-delay: 0.25s; }
          
          @keyframes resultFadeIn {
            from {
              opacity: 0;
              transform: translateX(-8px);
            }
            to {
              opacity: 1;
              transform: translateX(0);
            }
          }
          
          .dark & {
            background: #1e293b;
          }
          
          &:hover {
            background: #f1f5f9;
            transform: translateX(4px);
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
            
            .dark & {
              background: #2563eb;
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
            }
            
            .result-actions {
              opacity: 1;
            }
          }
          
          .result-avatar {
            flex-shrink: 0;
            
            .result-avatar-img {
              border-radius: 8px;
            }
            
            .file-icon-container {
              width: 44px;
              height: 44px;
              background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
              color: #3b82f6;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 8px;
              font-size: 20px;
              position: relative;
              
              .dark & {
                background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
                color: #93c5fd;
              }
              
              .file-ext {
                position: absolute;
                bottom: -2px;
                right: -2px;
                background: #fff;
                color: #64748b;
                font-size: 8px;
                font-weight: 600;
                padding: 1px 4px;
                border-radius: 4px;
                
                .dark & {
                  background: #0f172a;
                  color: #94a3b8;
                }
              }
            }
          }
          
          .result-info {
            flex: 1;
            min-width: 0;
            
            .result-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 8px;
              
              .result-title {
                font-size: 15px;
                font-weight: 600;
                color: #1e293b;
                flex: 1;
                margin-right: 12px;
                
                .dark & {
                  color: #f1f5f9;
                }
              }
              
              .result-time {
                font-size: 12px;
                color: #94a3b8;
                flex-shrink: 0;
              }
            }
            
            .result-desc {
              font-size: 14px;
              color: #64748b;
              line-height: 1.4;
              margin-bottom: 8px;
              
              .dark & {
                color: #94a3b8;
              }
            }
            
            .result-meta {
              .meta-tag {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                padding: 3px 8px;
                border-radius: 10px;
                font-size: 11px;
                font-weight: 500;
                
                &.message-tag {
                  background: #e0f2fe;
                  color: #0284c7;
                }
                
                &.file-tag {
                  background: #fef3c7;
                  color: #92400e;
                }
                
                &.contact-tag {
                  background: #d1fae5;
                  color: #065f46;
                }
                
                &.group-tag {
                  background: #e0e7ff;
                  color: #3730a3;
                }
                
                .dark & {
                  &.message-tag {
                    background: #0e7490;
                    color: #7dd3fc;
                  }
                  
                  &.file-tag {
                    background: #78350f;
                    color: #fcd34d;
                  }
                  
                  &.contact-tag {
                    background: #064e3b;
                    color: #6ee7b7;
                  }
                  
                  &.group-tag {
                    background: #312e81;
                    color: #a5b4fc;
                  }
                }
              }
            }
          }
          
          .result-actions {
            opacity: 0;
            transition: all 0.2s ease;
            display: flex;
            flex-direction: column;
            gap: 4px;
            
            .action-btn {
              width: 32px;
              height: 32px;
              border-radius: 6px;
              color: #64748b;
              
              &:hover {
                background: rgba(0, 137, 255, 0.1);
                color: #0089FF;
              }
              
              .dark & {
                color: #94a3b8;
                
                &:hover {
                  background: rgba(0, 137, 255, 0.2);
                  color: #60a5fa;
                }
              }
            }
          }
        }
      }
    }
  }
  
  // 空状态
  .empty-state {
    min-height: 400px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 40px 20px;
    animation: emptyFadeIn 0.3s ease-out;
    
    @keyframes emptyFadeIn {
      from {
        opacity: 0;
        transform: scale(0.95);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }
    
    .empty-icon-wrapper {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20px;
      
      .dark & {
        background: linear-gradient(135deg, #334155 0%, #475569 100%);
      }
      
      .empty-icon {
        font-size: 36px;
        color: #94a3b8;
      }
    }
    
    .empty-title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px 0;
      
      .dark & {
        color: #f1f5f9;
      }
      
      strong {
        color: #0089FF;
      }
    }
    
    .empty-desc {
      font-size: 14px;
      color: #64748b;
      margin: 0 0 24px 0;
      max-width: 300px;
      
      .dark & {
        color: #94a3b8;
      }
    }
    
    .empty-suggestions {
      background: #f8fafc;
      border-radius: 12px;
      padding: 16px;
      max-width: 400px;
      
      .dark & {
        background: #1e293b;
      }
      
      .suggestion-title {
        font-size: 13px;
        font-weight: 600;
        color: #64748b;
        margin: 0 0 12px 0;
        text-align: left;
        
        .dark & {
          color: #94a3b8;
        }
      }
      
      .suggestion-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .suggestion-item {
          font-size: 13px;
          color: #0089FF;
          cursor: pointer;
          text-align: left;
          padding: 6px 8px;
          border-radius: 6px;
          transition: all 0.2s ease;
          
          &:hover {
            background: #e0f2fe;
          }
          
          .dark & {
            color: #60a5fa;
            
            &:hover {
              background: #0e7490;
            }
          }
        }
      }
    }
    
    .empty-btn {
      border-radius: 8px;
      padding: 8px 20px;
    }
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    padding: 16px;
    
    // 骨架屏
    .skeleton-container {
      padding: 16px 0;
      
      .skeleton-item {
        padding: 12px;
        gap: 12px;
      }
    }
    
    // 欢迎卡片
    .welcome-card {
      padding: 24px 16px;
      
      .welcome-icon {
        font-size: 36px;
        margin-bottom: 12px;
      }
      
      .welcome-title {
        font-size: 18px;
      }
      
      .welcome-desc {
        font-size: 13px;
      }
      
      .quick-actions {
        gap: 8px;
        
        .action-item {
          padding: 6px 12px;
          font-size: 12px;
        }
      }
    }
    
    // 默认状态
    .default-state {
      .history-section,
      .trending-section {
        margin-bottom: 24px;
      }
      
      .section-header {
        margin-bottom: 12px;
        
        .section-title {
          font-size: 14px;
        }
      }
      
      .history-grid {
        gap: 6px;
        
        .history-item {
          padding: 10px 12px;
          font-size: 13px;
        }
      }
      
      .trending-tags {
        gap: 8px;
        
        .trending-tag {
          padding: 6px 12px;
          font-size: 12px;
          
          .tag-rank {
            width: 16px;
            height: 16px;
            font-size: 10px;
            margin-right: 6px;
          }
        }
      }
    }
    
    // 搜索结果
    .results-container {
      .result-section {
        margin-bottom: 16px;
        
        .section-header {
          margin-bottom: 12px;
          
          .section-title {
            font-size: 14px;
            
            &.with-icon {
              .section-icon {
                font-size: 16px;
                margin-right: 6px;
              }
              
              .result-count {
                font-size: 12px;
                margin-left: 6px;
              }
            }
          }
        }
        
        .result-list {
          gap: 6px;
          
          .result-card {
            gap: 12px;
            padding: 12px;
            
            .result-avatar {
              .result-avatar-img {
                width: 36px;
                height: 36px;
              }
              
              .file-icon-container {
                width: 36px;
                height: 36px;
                font-size: 16px;
                
                .file-ext {
                  font-size: 7px;
                  padding: 1px 3px;
                }
              }
            }
            
            .result-info {
              .result-header {
                margin-bottom: 6px;
                
                .result-title {
                  font-size: 14px;
                }
                
                .result-time {
                  font-size: 11px;
                }
              }
              
              .result-desc {
                font-size: 13px;
                margin-bottom: 6px;
              }
              
              .result-meta {
                .meta-tag {
                  padding: 2px 6px;
                  font-size: 10px;
                }
              }
            }
            
            .result-actions {
              opacity: 1;
              
              .action-btn {
                width: 28px;
                height: 28px;
              }
            }
          }
        }
      }
    }
    
    // 空状态
    .empty-state {
      min-height: 300px;
      padding: 30px 16px;
      
      .empty-icon-wrapper {
        width: 60px;
        height: 60px;
        margin-bottom: 16px;
        
        .empty-icon {
          font-size: 28px;
        }
      }
      
      .empty-title {
        font-size: 16px;
      }
      
      .empty-desc {
        font-size: 13px;
        margin-bottom: 16px;
      }
      
      .empty-suggestions {
        padding: 12px;
        
        .suggestion-title {
          font-size: 12px;
        }
        
        .suggestion-list {
          .suggestion-item {
            font-size: 12px;
          }
        }
      }
      
      .empty-btn {
        padding: 6px 16px;
        font-size: 13px;
      }
    }
  }
  
  // 小屏幕手机优化
  @media (max-width: 480px) {
    padding: 12px;
    
    // 骨架屏
    .skeleton-container {
      .skeleton-item {
        padding: 10px;
        gap: 10px;
        
        .skeleton-avatar {
          width: 36px;
          height: 36px;
        }
      }
    }
    
    // 欢迎卡片
    .welcome-card {
      padding: 20px 12px;
      
      .quick-actions {
        flex-direction: column;
        
        .action-item {
          width: 100%;
          justify-content: center;
        }
      }
    }
    
    // 默认状态
    .default-state {
      .history-section,
      .trending-section {
        margin-bottom: 20px;
      }
      
      .trending-tags {
        .trending-tag {
          padding: 5px 10px;
          font-size: 11px;
        }
      }
    }
    
    // 搜索结果
    .results-container {
      .result-section {
        .result-list {
          .result-card {
            gap: 10px;
            padding: 10px;
            
            .result-avatar {
              .result-avatar-img {
                width: 32px;
                height: 32px;
              }
              
              .file-icon-container {
                width: 32px;
                height: 32px;
                font-size: 14px;
              }
            }
            
            .result-info {
              .result-header {
                .result-title {
                  font-size: 13px;
                }
              }
              
              .result-desc {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}

.classic-footer {
  padding: 10px 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
  .dark & { background: #1e293b; border-color: #334155; }
  
  .footer-shortcuts {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .shortcut-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    kbd {
      background: #fff;
      border: 1px solid #e2e8f0;
      padding: 2px 6px;
      border-radius: 4px;
      margin-right: 4px;
      font-family: monospace;
      font-size: 11px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
      
      .dark & { 
        background: #0f172a; 
        border-color: #334155;
        color: #cbd5e1;
      }
    }
  }
  
  .result-count {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .count-icon {
      font-size: 14px;
      color: #0089FF;
    }
    
    strong {
      color: #0089FF;
      font-weight: 600;
    }
  }
}

.scrollbar-thin::-webkit-scrollbar { 
  width: 6px; 
}

.scrollbar-thin::-webkit-scrollbar-track { 
  background: transparent; 
}

.scrollbar-thin::-webkit-scrollbar-thumb { 
  background: #cbd5e1; 
  border-radius: 3px; 
  
  &:hover {
    background: #94a3b8;
  }
  
  .dark & { 
    background: #334155;
    
    &:hover {
      background: #475569;
    }
  } 
}
    
    .el-dialog__header {
      margin: 0;
      padding: 20px 24px;
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      border-bottom: 1px solid #e2e8f0;
      .dark & { 
        background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); 
        border-color: #334155; 
      }
      
      .el-dialog__title { 
        font-size: 17px; 
        font-weight: 600;
        color: #0f172a;
        letter-spacing: -0.01em;
        .dark & { color: #f1f5f9; }
      }
      
      .el-dialog__headerbtn {
        top: 20px;
        right: 24px;
        width: 32px;
        height: 32px;
        border-radius: 8px;
        transition: all 0.2s ease;
        
        &:hover {
          background: rgba(0, 0, 0, 0.05);
        }
        
        .dark &:hover {
          background: rgba(255, 255, 255, 0.1);
        }
        
        .el-dialog__close {
          color: #64748b;
          font-size: 18px;
          transition: all 0.2s ease;
          
          &:hover {
            color: #0f172a;
            transform: rotate(90deg);
            .dark & { color: #f1f5f9; }
          }
        }
      }
    }
    
    .el-dialog__body { 
      padding: 0; 
    }
  }
}

.classic-container {
  display: flex;
  flex-direction: column;
  height: 620px;
  background: #fff;
  .dark & { background: #0f172a; }
}

.modern-search-bar {
  padding: 20px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
  .dark & { border-color: #1e293b; background: #1e293b; }

  .search-container {
    position: relative;
    width: 100%;
  }

  .modern-input {
    width: 100%;
    
    :deep(input) {
      cursor: text !important;
      pointer-events: auto !important;
      font-size: 16px;
      padding: 0 52px 0 44px;
      height: 48px;
      line-height: 48px;
      border-radius: 24px;
    }

    .search-icon {
      font-size: 18px;
      color: #94a3b8;
      transition: all 0.2s ease;
      margin-left: 12px;
    }

    :deep(.el-input__wrapper) {
      border-radius: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.2s ease;
      border: 1px solid transparent;
      background: #f8fafc;
      
      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
        background: #f1f5f9;
      }
      
      &.is-focus { 
        box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.1), 0 2px 8px rgba(0, 0, 0, 0.08);
        border-color: #0089FF;
        background: #fff;
      }
      
      .dark & { 
        background: #1e293b; 
        border-color: #334155;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        
        &:hover {
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
          background: #2563eb;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.2), 0 2px 8px rgba(0, 0, 0, 0.2);
          background: #0f172a;
        }
      }
    }
    
    .search-button {
      background: #0089FF;
      color: #fff;
      border: none;
      border-radius: 20px;
      width: 40px;
      height: 40px;
      margin-right: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      &:hover {
        background: #0958d9;
        transform: scale(1.05);
      }
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
  
  // 自动补全建议
  .suggestions-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    margin-top: 8px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    z-index: 1000;
    animation: dropdownFadeIn 0.2s ease-out;
    
    .dark & {
      background: #0f172a;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    }
  }
  
  .suggestion-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 14px;
    color: #1e293b;
    
    &:hover,
    &.active {
      background: #f0f9ff;
      
      .dark & {
        background: #1e40af;
      }
    }
    
    .dark & {
      color: #f1f5f9;
    }
    
    .suggestion-icon {
      font-size: 14px;
      color: #94a3b8;
      margin-right: 12px;
      
      .dark & {
        color: #94a3b8;
      }
    }
    
    .suggestion-text {
      flex: 1;
      
      .highlight-text {
        color: #0089FF;
        font-weight: 600;
      }
    }
  }

  // 移动端优化
  @media (max-width: 768px) {
    padding: 16px;
    
    .modern-input {
      :deep(input) {
        font-size: 16px;
        height: 44px;
        line-height: 44px;
        padding: 0 44px 0 36px;
      }
    }
    
    .search-icon {
      font-size: 16px;
      margin-left: 8px;
    }
    
    .search-button {
      width: 36px;
      height: 36px;
      margin-right: 2px;
    }
    
    .suggestions-dropdown {
      margin-top: 6px;
      border-radius: 8px;
      
      .suggestion-item {
        padding: 10px 12px;
        font-size: 13px;
        
        .suggestion-icon {
          font-size: 12px;
          margin-right: 8px;
        }
      }
    }
  }
  
  // 小屏幕手机优化
  @media (max-width: 480px) {
    padding: 12px;
    
    .modern-input {
      :deep(input) {
        font-size: 15px;
        height: 42px;
        line-height: 42px;
        padding: 0 38px 0 32px;
      }
    }
    
    .search-icon {
      font-size: 15px;
      margin-left: 6px;
    }
    
    .search-button {
      width: 32px;
      height: 32px;
    }
  }
}

.classic-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.classic-aside {
  width: 160px;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
  padding: 8px 0;
  flex-shrink: 0;
  transition: width 0.3s ease;
  .dark & { background: #1e293b; border-color: #334155; }

  .cat-tab {
      padding: 12px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      cursor: pointer;
      font-size: 13px;
      color: #64748b;
      transition: all 0.2s ease;
      position: relative;
      animation: categorySwitch 0.3s ease-out forwards;

      &:hover { 
        background: #f1f5f9; 
        color: #1e293b; 
        transform: translateX(4px);
        .dark & { background: #0f172a; color: #f1f5f9; } 
      }
      &.active {
        background: #fff;
        color: #0089FF;
        font-weight: 600;
        border-right: 3px solid #0089FF;
        box-shadow: inset 0 0 0 1px #e2e8f0;
        transform: translateX(4px);
        .dark & { background: #0f172a; }
      }
      
      // 为每个分类标签添加不同的动画延迟
      &:nth-child(1) { animation-delay: 0.05s; }
      &:nth-child(2) { animation-delay: 0.1s; }
      &:nth-child(3) { animation-delay: 0.15s; }
      &:nth-child(4) { animation-delay: 0.2s; }
      &:nth-child(5) { animation-delay: 0.25s; }

    .label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .count-badge {
      font-size: 11px;
      background: #e2e8f0;
      padding: 2px 6px;
      border-radius: 10px;
      font-weight: 500;
      min-width: 20px;
      text-align: center;
      .dark & { background: #334155; color: #cbd5e1; }
    }
  }
}

.modern-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  
  // 加载状态
  .loading-state {
    min-height: 400px;
    
    .loading-text {
      font-size: 14px;
      color: #64748b;
      margin-top: 8px;
      
      .dark & {
        color: #94a3b8;
      }
    }
  }
  
  // 默认状态
  .default-state {
    .history-section,
    .trending-section {
      margin-bottom: 32px;
    }
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
        
        .dark & {
          color: #f1f5f9;
        }
      }
      
      .clear-history-btn {
        color: #64748b;
        font-size: 13px;
        
        &:hover {
          color: #ef4444;
        }
        
        .dark & {
          color: #94a3b8;
          
          &:hover {
            color: #f87171;
          }
        }
      }
    }
    
    // 历史记录
    .history-grid {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .history-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        border-radius: 10px;
        cursor: pointer;
        font-size: 14px;
        color: #475569;
        transition: all 0.2s ease;
        background: #f8fafc;
        
        .dark & {
          background: #1e293b;
          color: #cbd5e1;
        }
        
        &:hover {
          background: #f1f5f9;
          transform: translateX(4px);
          
          .dark & {
            background: #2563eb;
          }
          
          .close-icon {
            opacity: 1;
          }
        }
        
        .history-icon {
          font-size: 14px;
          color: #94a3b8;
          margin-right: 12px;
          flex-shrink: 0;
          
          .dark & {
            color: #94a3b8;
          }
        }
        
        .history-text {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .close-icon {
          opacity: 0;
          font-size: 14px;
          padding: 4px;
          border-radius: 4px;
          transition: all 0.2s ease;
          color: #64748b;
          
          &:hover { 
            background: rgba(239, 68, 68, 0.1);
            color: #ef4444; 
          }
          
          .dark & {
            color: #94a3b8;
            
            &:hover {
              background: rgba(248, 113, 113, 0.2);
              color: #f87171;
            }
          }
        }
      }
    }
    
    // 热门搜索
    .trending-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      
      .trending-tag {
        display: inline-flex;
        align-items: center;
        padding: 8px 16px;
        border-radius: 20px;
        background: #f8fafc;
        color: #475569;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s ease;
        
        .dark & {
          background: #1e293b;
          color: #cbd5e1;
        }
        
        &:hover {
          background: #f0f9ff;
          color: #0089FF;
          transform: translateY(-2px);
          box-shadow: 0 2px 8px rgba(0, 137, 255, 0.15);
          
          .dark & {
            background: #1e40af;
            box-shadow: 0 2px 8px rgba(0, 137, 255, 0.3);
          }
        }
        
        .tag-rank {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 18px;
          height: 18px;
          border-radius: 9px;
          background: #0089FF;
          color: #fff;
          font-size: 11px;
          font-weight: bold;
          margin-right: 8px;
          
          &:nth-child(1) {
            background: #ef4444;
          }
          
          &:nth-child(2) {
            background: #f97316;
          }
          
          &:nth-child(3) {
            background: #eab308;
          }
        }
      }
    }
  }
  
  // 搜索结果
  .results-container {
    .result-section {
      margin-bottom: 24px;
      
      .section-header {
        margin-bottom: 16px;
        
        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          display: flex;
          align-items: center;
          
          .dark & {
            color: #f1f5f9;
          }
          
          &.with-icon {
            .section-icon {
              font-size: 18px;
              color: #0089FF;
              margin-right: 8px;
            }
            
            .result-count {
              font-size: 13px;
              font-weight: 400;
              color: #64748b;
              margin-left: 8px;
              
              .dark & {
                color: #94a3b8;
              }
            }
          }
        }
      }
      
      .result-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .result-card {
          display: flex;
          gap: 16px;
          padding: 16px;
          border-radius: 12px;
          background: #f8fafc;
          cursor: pointer;
          transition: all 0.2s ease;
          animation: resultFadeIn 0.3s ease-out forwards;
          opacity: 0;
          
          // 为每个结果卡片添加不同的动画延迟
          &:nth-child(1) { animation-delay: 0.05s; }
          &:nth-child(2) { animation-delay: 0.1s; }
          &:nth-child(3) { animation-delay: 0.15s; }
          &:nth-child(4) { animation-delay: 0.2s; }
          &:nth-child(5) { animation-delay: 0.25s; }
          &:nth-child(6) { animation-delay: 0.3s; }
          &:nth-child(7) { animation-delay: 0.35s; }
          &:nth-child(8) { animation-delay: 0.4s; }
          &:nth-child(9) { animation-delay: 0.45s; }
          &:nth-child(10) { animation-delay: 0.5s; }
          
          .dark & {
            background: #1e293b;
          }
          
          &:hover {
            background: #f1f5f9;
            transform: translateX(4px);
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
            
            .dark & {
              background: #2563eb;
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
            }
          }
          
          .result-avatar {
            flex-shrink: 0;
            
            .result-avatar-img {
              border-radius: 8px;
            }
            
            .file-icon-container {
              width: 40px;
              height: 40px;
              background: #eff6ff;
              color: #3b82f6;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 8px;
              font-size: 18px;
              
              .dark & {
                background: #1e40af;
                color: #93c5fd;
              }
            }
          }
          
          .result-info {
            flex: 1;
            min-width: 0;
            
            .result-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 8px;
              
              .result-title {
                font-size: 15px;
                font-weight: 600;
                color: #1e293b;
                flex: 1;
                margin-right: 12px;
                
                .dark & {
                  color: #f1f5f9;
                }
              }
              
              .result-time {
                font-size: 12px;
                color: #94a3b8;
                flex-shrink: 0;
              }
            }
            
            .result-desc {
              font-size: 14px;
              color: #64748b;
              line-height: 1.4;
              margin-bottom: 8px;
              
              .dark & {
                color: #94a3b8;
              }
            }
            
            .result-meta {
              .meta-tag {
                display: inline-block;
                padding: 2px 8px;
                border-radius: 10px;
                background: #e0f2fe;
                color: #0284c7;
                font-size: 11px;
                font-weight: 500;
                
                .dark & {
                  background: #0e7490;
                  color: #7dd3fc;
                }
              }
            }
          }
        }
      }
    }
  }
  
  // 空状态
  .empty-state {
    min-height: 400px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 40px 20px;
    
    .empty-icon {
      margin-bottom: 16px;
    }
    
    .empty-title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px 0;
      
      .dark & {
        color: #f1f5f9;
      }
    }
    
    .empty-desc {
      font-size: 14px;
      color: #64748b;
      margin: 0 0 24px 0;
      max-width: 300px;
      
      .dark & {
        color: #94a3b8;
      }
    }
    
    .empty-btn {
      border-radius: 8px;
      padding: 8px 20px;
    }
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    padding: 16px;
    
    // 默认状态
    .default-state {
      .history-section,
      .trending-section {
        margin-bottom: 24px;
      }
      
      .section-header {
        margin-bottom: 12px;
        
        .section-title {
          font-size: 14px;
        }
      }
      
      .history-grid {
        gap: 6px;
        
        .history-item {
          padding: 10px 12px;
          font-size: 13px;
        }
      }
      
      .trending-tags {
        gap: 8px;
        
        .trending-tag {
          padding: 6px 12px;
          font-size: 12px;
          
          .tag-rank {
            width: 16px;
            height: 16px;
            font-size: 10px;
            margin-right: 6px;
          }
        }
      }
    }
    
    // 搜索结果
    .results-container {
      .result-section {
        margin-bottom: 16px;
        
        .section-header {
          margin-bottom: 12px;
          
          .section-title {
            font-size: 14px;
            
            &.with-icon {
              .section-icon {
                font-size: 16px;
                margin-right: 6px;
              }
              
              .result-count {
                font-size: 12px;
                margin-left: 6px;
              }
            }
          }
        }
        
        .result-list {
          gap: 6px;
          
          .result-card {
            gap: 12px;
            padding: 12px;
            
            .result-avatar {
              .result-avatar-img {
                width: 36px;
                height: 36px;
              }
              
              .file-icon-container {
                width: 36px;
                height: 36px;
                font-size: 16px;
              }
            }
            
            .result-info {
              .result-header {
                margin-bottom: 6px;
                
                .result-title {
                  font-size: 14px;
                }
                
                .result-time {
                  font-size: 11px;
                }
              }
              
              .result-desc {
                font-size: 13px;
                margin-bottom: 6px;
              }
              
              .result-meta {
                .meta-tag {
                  padding: 2px 6px;
                  font-size: 10px;
                }
              }
            }
          }
        }
      }
    }
    
    // 空状态
    .empty-state {
      min-height: 300px;
      padding: 30px 16px;
      
      .empty-icon {
        margin-bottom: 12px;
      }
      
      .empty-title {
        font-size: 16px;
      }
      
      .empty-desc {
        font-size: 13px;
        margin-bottom: 16px;
      }
      
      .empty-btn {
        padding: 6px 16px;
        font-size: 13px;
      }
    }
  }
  
  // 小屏幕手机优化
  @media (max-width: 480px) {
    padding: 12px;
    
    // 默认状态
    .default-state {
      .history-section,
      .trending-section {
        margin-bottom: 20px;
      }
      
      .trending-tags {
        .trending-tag {
          padding: 5px 10px;
          font-size: 11px;
        }
      }
    }
    
    // 搜索结果
    .results-container {
      .result-section {
        .result-list {
          .result-card {
            gap: 10px;
            padding: 10px;
            
            .result-avatar {
              .result-avatar-img {
                width: 32px;
                height: 32px;
              }
              
              .file-icon-container {
                width: 32px;
                height: 32px;
                font-size: 14px;
              }
            }
            
            .result-info {
              .result-header {
                .result-title {
                  font-size: 13px;
                }
              }
              
              .result-desc {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}

.classic-footer {
  padding: 10px 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
  .dark & { background: #1e293b; border-color: #334155; }
  
  .flex {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  kbd {
    background: #fff;
    border: 1px solid #e2e8f0;
    padding: 2px 6px;
    border-radius: 4px;
    margin-right: 4px;
    font-family: monospace;
    font-size: 11px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    
    .dark & { 
      background: #0f172a; 
      border-color: #334155;
      color: #cbd5e1;
    }
  }
}

.scrollbar-thin::-webkit-scrollbar { 
  width: 6px; 
}

.scrollbar-thin::-webkit-scrollbar-track { 
  background: transparent; 
}

.scrollbar-thin::-webkit-scrollbar-thumb { 
  background: #cbd5e1; 
  border-radius: 3px; 
  
  &:hover {
    background: #94a3b8;
  }
  
  .dark & { 
    background: #334155;
    
    &:hover {
      background: #475569;
    }
  } 
}
</style>
