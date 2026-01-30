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
            <span class="label">{{ cat.label }}</span>
            <span v-if="getCategoryCount(cat.key)" class="count-badge">{{ getCategoryCount(cat.key) }}</span>
          </div>
        </div>

        <!-- 右侧结果：现代化列表 -->
        <div class="modern-main scrollbar-thin">
          <!-- 加载状态 -->
          <div v-if="searching" class="loading-state flex flex-col items-center justify-center">
            <el-icon class="is-loading text-2xl text-primary mb-3"><Loading /></el-icon>
            <span class="loading-text">正在搜索...</span>
          </div>

          <!-- 默认状态：历史记录和热门搜索 -->
          <div v-else-if="!searchKeyword" class="default-state">
            <!-- 最近搜索历史 -->
            <div v-if="historyKeywords.length > 0" class="history-section">
              <div class="section-header">
                <h3 class="section-title">最近搜索</h3>
                <el-button 
                  type="text" 
                  size="small" 
                  class="clear-history-btn"
                  @click="clearAllHistory"
                >
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
                  <el-icon class="history-icon"><Timer /></el-icon>
                  <span class="history-text">{{ kw }}</span>
                  <el-icon class="close-icon" @click.stop="removeHistory(kw)"><Close /></el-icon>
                </div>
              </div>
            </div>
            
            <!-- 热门搜索 -->
            <div class="trending-section">
              <h3 class="section-title">热门搜索</h3>
              <div class="trending-tags">
                <span 
                  v-for="(tag, index) in trendingTags" 
                  :key="index"
                  class="trending-tag"
                  @click="searchKeyword = tag; handleSearch()"
                >
                  <span class="tag-rank">{{ index + 1 }}</span>
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
              </div>
              
              <div class="result-list">
                <div
                  v-for="(item, index) in section.data"
                  :key="item.id || index"
                  class="result-card"
                  @click="handleItemClick(type, item)"
                >
                  <!-- 头像/图标 -->
                  <div class="result-avatar">
                    <DingtalkAvatar
                      v-if="type !== 'files'"
                      :name="item.name || item.friendName || item.senderName"
                      :src="item.avatar || item.friendAvatar"
                      :size="40"
                      shape="square"
                      custom-class="result-avatar-img"
                    />
                    <div v-else class="file-icon-container">
                      <el-icon class="file-icon"><Document /></el-icon>
                    </div>
                  </div>

                  <!-- 信息 -->
                  <div class="result-info">
                    <div class="result-header">
                      <h4 class="result-title" v-html="highlight(item.name || item.friendName || item.senderName || item.fileName)"></h4>
                      <span class="result-time">{{ formatTime(item.timestamp) }}</span>
                    </div>
                    <div class="result-desc" v-html="highlight(item.content || item.remark || item.departmentName || (item.memberCount ? item.memberCount + '人' : ''))"></div>
                    <div v-if="type === 'messages'" class="result-meta">
                      <span class="meta-tag">消息</span>
                    </div>
                    <div v-else-if="type === 'files'" class="result-meta">
                      <span class="meta-tag">文件</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="searchKeyword && !searching" class="empty-state">
            <div class="empty-icon">
              <el-icon class="text-4xl text-gray-300"><Search /></el-icon>
            </div>
            <h3 class="empty-title">未找到匹配项</h3>
            <p class="empty-desc">尝试使用其他关键词或检查拼写</p>
            <el-button class="empty-btn" @click="resetSearch">清除搜索</el-button>
          </div>
        </div>
      </div>

      <!-- 页脚 -->
      <div class="classic-footer">
        <div class="flex items-center gap-4">
          <span class="flex items-center gap-1"><kbd>Enter</kbd> 查看详情</span>
          <span class="flex items-center gap-1"><kbd>Esc</kbd> 关闭窗口</span>
        </div>
        <div v-if="totalCount > 0">找到 {{ totalCount }} 条结果</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Search, Document, Loading, Timer, Close } from '@element-plus/icons-vue'
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
const historyKeywords = ref(JSON.parse(localStorage.getItem('search_history') || '[]'))

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
  }, 250)
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
      localStorage.setItem('search_history', JSON.stringify(historyKeywords.value))
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
  localStorage.setItem('search_history', JSON.stringify(historyKeywords.value))
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

// 监听键盘事件，支持上下箭头选择建议
const handleKeyDown = (event) => {
  if (!showSuggestions.value) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      suggestionIndex.value = (suggestionIndex.value + 1) % suggestions.value.length
      break
    case 'ArrowUp':
      event.preventDefault()
      suggestionIndex.value = suggestionIndex.value <= 0 ? suggestions.value.length - 1 : suggestionIndex.value - 1
      break
    case 'Enter':
      if (suggestionIndex.value >= 0) {
        event.preventDefault()
        selectSuggestion(suggestions.value[suggestionIndex.value])
      }
      break
    case 'Escape':
      showSuggestions.value = false
      break
  }
}

// 清空所有历史记录
const clearAllHistory = () => {
  historyKeywords.value = []
  localStorage.setItem('search_history', JSON.stringify(historyKeywords.value))
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchResult.value = null
  suggestions.value = []
  showSuggestions.value = false
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
    animation: dialogFadeIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    
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
