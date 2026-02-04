<template>
  <teleport to="body">
    <transition name="search-fade">
      <div v-if="visible" class="global-search-overlay" @click="handleOverlayClick">
        <!-- 搜索弹窗容器 -->
        <div 
          class="search-modal" 
          :class="{ 'is-mobile': isMobile, 'is-tablet': isTablet }"
          @click.stop
        >
          <!-- 搜索头部 -->
          <div class="search-header">
            <div class="search-input-wrapper">
              <!-- 搜索图标 -->
              <div class="input-icon search-icon">
                <el-icon><Search /></el-icon>
              </div>
              
              <!-- 搜索输入框 -->
              <input
                ref="searchInputRef"
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="搜索联系人、群组、消息、文件..."
                @input="handleInput"
                @focus="handleFocus"
                @blur="handleBlur"
                @keydown="handleKeyDown"
              />
              
              <!-- 清除按钮 -->
              <transition name="fade-scale">
                <button 
                  v-if="searchKeyword" 
                  class="input-icon clear-btn"
                  @click="clearSearch"
                >
                  <el-icon><CircleClose /></el-icon>
                </button>
              </transition>
              
              <!-- 语音搜索按钮 -->
              <button class="input-icon voice-btn" @click="handleVoiceSearch">
                <el-icon><Microphone /></el-icon>
              </button>
              
              <!-- 搜索按钮 -->
              <button class="search-submit-btn" @click="handleSearch">
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
            
            <!-- 快捷筛选标签 -->
            <div class="quick-filters">
              <button 
                v-for="filter in quickFilters" 
                :key="filter.key"
                class="filter-tag"
                :class="{ active: activeFilter === filter.key }"
                @click="handleFilterClick(filter.key)"
              >
                <el-icon><component :is="filter.icon" /></el-icon>
                <span>{{ filter.label }}</span>
              </button>
            </div>
          </div>
          
          <!-- 搜索建议下拉面板 -->
          <transition name="slide-down">
            <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-panel">
              <div class="suggestions-header">
                <span class="suggestions-title">搜索建议</span>
                <button class="close-suggestions" @click="showSuggestions = false">
                  <el-icon><Close /></el-icon>
                </button>
              </div>
              <div class="suggestions-list">
                <div
                  v-for="(item, index) in suggestions"
                  :key="index"
                  class="suggestion-item"
                  :class="{ active: activeSuggestionIndex === index }"
                  @click="selectSuggestion(item)"
                  @mouseenter="activeSuggestionIndex = index"
                >
                  <div class="suggestion-icon">
                    <el-icon><component :is="item.icon || 'Search'" /></el-icon>
                  </div>
                  <div class="suggestion-content">
                    <span class="suggestion-text" v-html="highlightText(item.text)"></span>
                    <span v-if="item.type" class="suggestion-type">{{ item.type }}</span>
                  </div>
                  <el-icon class="suggestion-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </transition>
          
          <!-- 搜索主体内容 -->
          <div class="search-body">
            <!-- 左侧分类导航 -->
            <aside class="category-sidebar" :class="{ collapsed: isMobile }">
              <div class="category-list">
                <button
                  v-for="cat in categories"
                  :key="cat.key"
                  class="category-item"
                  :class="{ active: searchType === cat.key }"
                  @click="handleCategoryChange(cat.key)"
                >
                  <div class="category-icon">
                    <el-icon><component :is="cat.icon" /></el-icon>
                  </div>
                  <span class="category-label">{{ cat.label }}</span>
                  <span v-if="getCategoryCount(cat.key)" class="category-count">
                    {{ getCategoryCount(cat.key) }}
                  </span>
                  <div v-if="searchType === cat.key" class="active-indicator"></div>
                </button>
              </div>
            </aside>
            
            <!-- 右侧结果区域 -->
            <main class="results-area">
              <!-- 加载状态 - 骨架屏 -->
              <div v-if="searching" class="loading-state">
                <div class="skeleton-list">
                  <div v-for="i in 6" :key="i" class="skeleton-card">
                    <div class="skeleton-avatar"></div>
                    <div class="skeleton-content">
                      <div class="skeleton-line title"></div>
                      <div class="skeleton-line desc"></div>
                      <div class="skeleton-line meta"></div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 默认状态 - 欢迎和历史记录 -->
              <div v-else-if="!searchKeyword" class="default-state">
                <!-- 欢迎卡片 -->
                <div class="welcome-section">
                  <div class="welcome-illustration">
                    <div class="illustration-icon">
                      <el-icon><Search /></el-icon>
                    </div>
                  </div>
                  <h2 class="welcome-title">开始搜索</h2>
                  <p class="welcome-desc">快速查找联系人、群组、消息记录和文件</p>
                  
                  <!-- 快捷操作 -->
                  <div class="quick-actions">
                    <div class="action-card" @click="handleQuickAction('contact')">
                      <div class="action-icon bg-blue">
                        <el-icon><User /></el-icon>
                      </div>
                      <span class="action-label">找联系人</span>
                    </div>
                    <div class="action-card" @click="handleQuickAction('group')">
                      <div class="action-icon bg-purple">
                        <el-icon><ChatDotRound /></el-icon>
                      </div>
                      <span class="action-label">找群组</span>
                    </div>
                    <div class="action-card" @click="handleQuickAction('file')">
                      <div class="action-icon bg-orange">
                        <el-icon><Document /></el-icon>
                      </div>
                      <span class="action-label">找文件</span>
                    </div>
                    <div class="action-card" @click="handleQuickAction('message')">
                      <div class="action-icon bg-green">
                        <el-icon><ChatLineRound /></el-icon>
                      </div>
                      <span class="action-label">搜消息</span>
                    </div>
                  </div>
                </div>
                
                <!-- 最近搜索 -->
                <div v-if="recentSearches.length > 0" class="recent-section">
                  <div class="section-header">
                    <div class="section-title">
                      <el-icon><Clock /></el-icon>
                      <span>最近搜索</span>
                    </div>
                    <button class="clear-btn" @click="clearRecentSearches">
                      <el-icon><Delete /></el-icon>
                      <span>清空</span>
                    </button>
                  </div>
                  <div class="recent-list">
                    <div
                      v-for="(item, index) in recentSearches"
                      :key="index"
                      class="recent-item"
                      @click="handleRecentClick(item)"
                    >
                      <el-icon class="recent-icon"><Timer /></el-icon>
                      <span class="recent-text">{{ item }}</span>
                      <button class="remove-btn" @click.stop="removeRecentItem(item)">
                        <el-icon><Close /></el-icon>
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 热门搜索 -->
                <div class="trending-section">
                  <div class="section-header">
                    <div class="section-title">
                      <el-icon><TrendCharts /></el-icon>
                      <span>热门搜索</span>
                    </div>
                  </div>
                  <div class="trending-list">
                    <span
                      v-for="(tag, index) in trendingSearches"
                      :key="index"
                      class="trending-tag"
                      :class="'rank-' + (index + 1)"
                      @click="handleTrendingClick(tag)"
                    >
                      <span class="rank-number">{{ index + 1 }}</span>
                      <span class="tag-text">{{ tag }}</span>
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- 搜索结果列表 -->
              <div v-else-if="hasResults" class="results-list">
                <div
                  v-for="(section, type) in filteredResults"
                  :key="type"
                  class="result-section"
                >
                  <!-- 分类标题 -->
                  <div class="section-header sticky-header">
                    <div class="section-title">
                      <el-icon><component :is="getSectionIcon(type)" /></el-icon>
                      <span>{{ section.title }}</span>
                      <span class="result-count">{{ section.data.length }}</span>
                    </div>
                    <button 
                      v-if="section.data.length > 5" 
                      class="view-more-btn"
                      @click="viewMore(type)"
                    >
                      查看更多
                      <el-icon><ArrowRight /></el-icon>
                    </button>
                  </div>
                  
                  <!-- 结果卡片列表 -->
                  <div class="result-cards">
                    <div
                      v-for="(item, index) in section.data.slice(0, 5)"
                      :key="item.id || index"
                      class="result-card"
                      :class="['type-' + type, 'animate-' + index]"
                      @click="handleResultClick(type, item)"
                    >
                      <!-- 头像/图标区域 -->
                      <div class="card-media">
                        <template v-if="type === 'contacts' || type === 'groups'">
                          <DingtalkAvatar
                            :name="item.name || item.friendName || item.groupName"
                            :src="item.avatar || item.friendAvatar || item.groupAvatar"
                            :size="48"
                            shape="square"
                          />
                        </template>
                        <template v-else-if="type === 'files'">
                          <div class="file-preview" :class="getFileTypeClass(item.fileName)">
                            <el-icon><Document /></el-icon>
                            <span class="file-ext">{{ getFileExtension(item.fileName) }}</span>
                          </div>
                        </template>
                        <template v-else-if="type === 'messages'">
                          <div class="message-avatar">
                            <DingtalkAvatar
                              :name="item.senderName"
                              :src="item.senderAvatar"
                              :size="40"
                              shape="circle"
                            />
                            <div class="message-badge">
                              <el-icon><ChatDotRound /></el-icon>
                            </div>
                          </div>
                        </template>
                      </div>
                      
                      <!-- 内容区域 -->
                      <div class="card-content">
                        <div class="content-header">
                          <h4 class="content-title" v-html="highlightText(getItemTitle(item, type))"></h4>
                          <span class="content-time">{{ formatTime(item.timestamp || item.updateTime) }}</span>
                        </div>
                        <p class="content-desc" v-html="highlightText(getItemDesc(item, type))"></p>
                        <div class="content-meta">
                          <span class="meta-tag" :class="type">
                            <el-icon><component :is="getMetaIcon(type)" /></el-icon>
                            <span>{{ getMetaLabel(type) }}</span>
                          </span>
                          <span v-if="item.sessionName" class="meta-source">
                            来自: {{ item.sessionName }}
                          </span>
                        </div>
                      </div>
                      
                      <!-- 操作区域 -->
                      <div class="card-actions">
                        <button class="action-btn preview" @click.stop="handlePreview(type, item)">
                          <el-icon><View /></el-icon>
                        </button>
                        <button class="action-btn more" @click.stop="handleMore(type, item)">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 空状态 -->
              <div v-else class="empty-state">
                <div class="empty-illustration">
                  <div class="empty-icon-wrapper">
                    <el-icon><Search /></el-icon>
                  </div>
                </div>
                <h3 class="empty-title">未找到相关结果</h3>
                <p class="empty-desc">
                  没有找到与 "<strong>{{ searchKeyword }}</strong>" 相关的内容
                </p>
                <div class="empty-suggestions">
                  <p class="suggestions-label">试试以下建议：</p>
                  <div class="suggestion-chips">
                    <button class="chip" @click="clearSearch">清除搜索词</button>
                    <button class="chip" @click="handleCategoryChange('ALL')">查看全部</button>
                    <button class="chip" @click="handleTypoFix">纠正拼写</button>
                  </div>
                </div>
              </div>
            </main>
          </div>
          
          <!-- 搜索底部 -->
          <div class="search-footer">
            <div class="footer-shortcuts">
              <span class="shortcut">
                <kbd>Enter</kbd>
                <span>搜索</span>
              </span>
              <span class="shortcut">
                <kbd>↑</kbd>
                <kbd>↓</kbd>
                <span>选择</span>
              </span>
              <span class="shortcut">
                <kbd>Esc</kbd>
                <span>关闭</span>
              </span>
            </div>
            <div v-if="totalCount > 0" class="footer-stats">
              <el-icon><DataLine /></el-icon>
              <span>找到 <strong>{{ totalCount }}</strong> 条结果</span>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Search, CircleClose, Microphone, ArrowRight, Close,
  User, ChatDotRound, Document, ChatLineRound,
  Clock, Delete, Timer, TrendCharts, View, MoreFilled,
  DataLine, Folder, Message
} from '@element-plus/icons-vue'
import { globalSearch } from '@/api/im/globalSearch'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatTime, formatFileSize } from '@/utils/format'
import { getJSON, setJSON } from '@/utils/storage'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'select'])

const store = useStore()

// 状态
const visible = ref(false)
const searchKeyword = ref('')
const searchType = ref('ALL')
const searchResult = ref(null)
const searching = ref(false)
const searchInputRef = ref(null)
const activeSuggestionIndex = ref(-1)
const showSuggestions = ref(false)
const activeFilter = ref('all')

// 响应式断点
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 1200)

// 最近搜索
const recentSearches = ref(getJSON('global_search_recent', []))

// 热门搜索
const trendingSearches = ref([
  '项目进度汇报',
  '周会会议纪要',
  '产品需求文档',
  '技术方案评审',
  'Bug修复进度',
  '测试报告',
  '发布计划',
  '团队建设'
])

// 快捷筛选
const quickFilters = [
  { key: 'all', label: '全部', icon: 'Search' },
  { key: 'contact', label: '联系人', icon: 'User' },
  { key: 'group', label: '群组', icon: 'ChatDotRound' },
  { key: 'file', label: '文件', icon: 'Document' }
]

// 分类配置
const categories = [
  { key: 'ALL', label: '全部', icon: 'Search' },
  { key: 'CONTACT', label: '联系人', icon: 'User' },
  { key: 'GROUP', label: '群组', icon: 'ChatDotRound' },
  { key: 'MESSAGE', label: '消息', icon: 'ChatLineRound' },
  { key: 'FILE', label: '文件', icon: 'Document' }
]

// 搜索建议
const suggestions = ref([])

// 计算属性
const totalCount = computed(() => {
  if (!searchResult.value) return 0
  return (searchResult.value.contacts?.length || 0) +
         (searchResult.value.groups?.length || 0) +
         (searchResult.value.messages?.length || 0) +
         (searchResult.value.files?.length || 0)
})

const hasResults = computed(() => totalCount.value > 0)

const filteredResults = computed(() => {
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
    return targetKey ? { [targetKey]: sections[targetKey] } : {}
  }
  
  return Object.fromEntries(
    Object.entries(sections).filter(([_, s]) => s.data.length > 0)
  )
})

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    nextTick(() => {
      searchInputRef.value?.focus()
    })
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
    resetSearch()
  }
})

// 方法
const getCategoryCount = (key) => {
  if (!searchResult.value || key === 'ALL') return 0
  const keyMap = { CONTACT: 'contacts', GROUP: 'groups', MESSAGE: 'messages', FILE: 'files' }
  return searchResult.value[keyMap[key]]?.length || 0
}

const getSectionIcon = (type) => {
  const iconMap = {
    contacts: 'User',
    groups: 'ChatDotRound',
    messages: 'ChatLineRound',
    files: 'Document'
  }
  return iconMap[type] || 'Search'
}

const getMetaIcon = (type) => {
  const iconMap = {
    contacts: 'User',
    groups: 'ChatDotRound',
    messages: 'Message',
    files: 'Folder'
  }
  return iconMap[type] || 'Search'
}

const getMetaLabel = (type) => {
  const labelMap = {
    contacts: '联系人',
    groups: '群组',
    messages: '消息',
    files: '文件'
  }
  return labelMap[type] || ''
}

const getItemTitle = (item, type) => {
  const titleMap = {
    contacts: item.friendName || item.name || item.username,
    groups: item.groupName || item.name,
    messages: item.senderName || '未知用户',
    files: item.fileName || '未命名文件'
  }
  return titleMap[type] || ''
}

const getItemDesc = (item, type) => {
  const descMap = {
    contacts: item.remark || item.departmentName || '暂无描述',
    groups: item.notice || `共 ${item.memberCount || 0} 人`,
    messages: item.content || '',
    files: item.fileSize ? formatFileSize(item.fileSize) : ''
  }
  return descMap[type] || ''
}

const getFileExtension = (fileName) => {
  if (!fileName) return ''
  const ext = fileName.split('.').pop()
  return ext.length <= 4 ? ext.toUpperCase() : ext.slice(0, 4).toUpperCase()
}

const getFileTypeClass = (fileName) => {
  if (!fileName) return 'default'
  const ext = fileName.split('.').pop().toLowerCase()
  const typeMap = {
    pdf: 'pdf',
    doc: 'doc', docx: 'doc',
    xls: 'xls', xlsx: 'xls',
    ppt: 'ppt', pptx: 'ppt',
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image',
    mp4: 'video', avi: 'video', mov: 'video',
    mp3: 'audio', wav: 'audio',
    zip: 'zip', rar: 'zip', '7z': 'zip'
  }
  return typeMap[ext] || 'default'
}

const highlightText = (text) => {
  if (!text || !searchKeyword.value) return text
  const reg = new RegExp(`(${searchKeyword.value})`, 'gi')
  return String(text).replace(reg, '<mark>$1</mark>')
}

// 搜索处理
let searchTimer = null
const searchCache = new Map()

const handleInput = () => {
  const keyword = searchKeyword.value.trim()
  
  if (!keyword) {
    searchResult.value = null
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  
  // 生成建议
  generateSuggestions(keyword)
  
  // 防抖搜索
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => performSearch(keyword), 300)
}

const generateSuggestions = (keyword) => {
  const historySuggestions = recentSearches.value
    .filter(item => item.toLowerCase().includes(keyword.toLowerCase()))
    .slice(0, 3)
    .map(text => ({ text, icon: 'Clock', type: '历史' }))
  
  const defaultSuggestions = [
    { text: `联系人: ${keyword}`, icon: 'User', type: '筛选' },
    { text: `群组: ${keyword}`, icon: 'ChatDotRound', type: '筛选' },
    { text: `文件: ${keyword}`, icon: 'Document', type: '筛选' }
  ]
  
  suggestions.value = [...historySuggestions, ...defaultSuggestions]
  showSuggestions.value = suggestions.value.length > 0
  activeSuggestionIndex.value = -1
}

const performSearch = async (keyword) => {
  if (!keyword) return
  
  searching.value = true
  showSuggestions.value = false
  
  try {
    const cacheKey = `${keyword}_${searchType.value}`
    
    if (searchCache.has(cacheKey)) {
      searchResult.value = searchCache.get(cacheKey)
      searching.value = false
      return
    }
    
    const res = await globalSearch({
      keyword,
      searchType: searchType.value,
      pageNum: 1,
      pageSize: 50
    })
    
    if (res.code === 200) {
      searchResult.value = res.data || {}
      searchCache.set(cacheKey, searchResult.value)
      
      // 限制缓存大小
      if (searchCache.size > 20) {
        const firstKey = searchCache.keys().next().value
        searchCache.delete(firstKey)
      }
      
      // 保存到历史
      saveToRecent(keyword)
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    performSearch(keyword)
  }
}

const saveToRecent = (keyword) => {
  const index = recentSearches.value.indexOf(keyword)
  if (index > -1) {
    recentSearches.value.splice(index, 1)
  }
  recentSearches.value.unshift(keyword)
  if (recentSearches.value.length > 10) {
    recentSearches.value = recentSearches.value.slice(0, 10)
  }
  setJSON('global_search_recent', recentSearches.value)
}

// 事件处理
const handleOverlayClick = () => {
  visible.value = false
}

const clearSearch = () => {
  searchKeyword.value = ''
  searchResult.value = null
  suggestions.value = []
  showSuggestions.value = false
  nextTick(() => searchInputRef.value?.focus())
}

const resetSearch = () => {
  searchKeyword.value = ''
  searchResult.value = null
  searchType.value = 'ALL'
  activeFilter.value = 'all'
  suggestions.value = []
  showSuggestions.value = false
}

const handleFocus = () => {
  if (searchKeyword.value && suggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

const handleBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const selectSuggestion = (item) => {
  searchKeyword.value = item.text.replace(/^(联系人|群组|文件):\s*/, '')
  showSuggestions.value = false
  handleSearch()
}

const handleFilterClick = (key) => {
  activeFilter.value = key
  const typeMap = { all: 'ALL', contact: 'CONTACT', group: 'GROUP', file: 'FILE' }
  handleCategoryChange(typeMap[key])
}

const handleCategoryChange = (key) => {
  searchType.value = key
  if (searchKeyword.value) {
    handleSearch()
  }
}

const handleQuickAction = (type) => {
  const prefixMap = { contact: '@', group: '#', file: 'file:', message: '' }
  searchKeyword.value = prefixMap[type] || ''
  nextTick(() => searchInputRef.value?.focus())
}

const handleRecentClick = (item) => {
  searchKeyword.value = item
  handleSearch()
}

const removeRecentItem = (item) => {
  recentSearches.value = recentSearches.value.filter(i => i !== item)
  setJSON('global_search_recent', recentSearches.value)
}

const clearRecentSearches = () => {
  recentSearches.value = []
  setJSON('global_search_recent', [])
}

const handleTrendingClick = (tag) => {
  searchKeyword.value = tag
  handleSearch()
}

const handleResultClick = (type, item) => {
  emit('select', { type, data: item })
  visible.value = false
}

const handlePreview = (type, item) => {
  // 根据类型提供不同的预览行为
  if (type === 'message') {
    emit('preview-message', item)
  } else if (type === 'file') {
    if (item.url) {
      window.open(item.url, '_blank')
    } else {
      ElMessage.warning('文件预览暂不支持')
    }
  } else {
    ElMessage.info('预览功能开发中...')
  }
}

const handleMore = (type, item) => {
  // 显示更多操作菜单
  if (type === 'contact') {
    ElMessage.info('更多联系人操作开发中...')
  } else if (type === 'message') {
    ElMessage.info('更多消息操作开发中...')
  } else {
    ElMessage.info('更多操作开发中...')
  }
}

const viewMore = (type) => {
  // 跳转到更多结果页面
  if (type === 'message') {
    emit('view-all-messages', searchKeyword.value)
  } else if (type === 'contact') {
    emit('view-all-contacts', searchKeyword.value)
  } else if (type === 'file') {
    emit('view-all-files', searchKeyword.value)
  } else {
    ElMessage.info('查看更多功能开发中...')
  }
}

const handleTypoFix = () => {
  // 简单的拼写纠正逻辑
  const keyword = searchKeyword.value
  if (keyword.length > 3) {
    searchKeyword.value = keyword.slice(0, -1)
    handleSearch()
  }
}

const handleVoiceSearch = async () => {
  // 检查浏览器是否支持语音识别
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    ElMessage.warning('您的浏览器不支持语音搜索，请使用 Chrome 浏览器')
    return
  }

  try {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    const recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.continuous = false
    recognition.interimResults = false

    recognition.onstart = () => {
      ElMessage.info('正在聆听...')
    }

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      searchKeyword.value = transcript
      handleSearch()
    }

    recognition.onerror = (event) => {
      if (event.error === 'not-allowed') {
        ElMessage.warning('请允许麦克风访问权限以使用语音搜索')
      } else {
        ElMessage.error('语音识别失败: ' + event.error)
      }
    }

    recognition.onend = () => {
      // 识别结束
    }

    recognition.start()
  } catch (error) {
    ElMessage.warning('语音搜索功能开发中，敬请期待')
  }
}

// 键盘导航
const handleKeyDown = (e) => {
  if (showSuggestions.value && suggestions.value.length > 0) {
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault()
        activeSuggestionIndex.value = (activeSuggestionIndex.value + 1) % suggestions.value.length
        return
      case 'ArrowUp':
        e.preventDefault()
        activeSuggestionIndex.value = activeSuggestionIndex.value <= 0 
          ? suggestions.value.length - 1 
          : activeSuggestionIndex.value - 1
        return
      case 'Enter':
        if (activeSuggestionIndex.value >= 0) {
          e.preventDefault()
          selectSuggestion(suggestions.value[activeSuggestionIndex.value])
          return
        }
        break
      case 'Escape':
        showSuggestions.value = false
        return
    }
  }
  
  switch (e.key) {
    case 'Escape':
      visible.value = false
      break
    case 'Enter':
      if (!showSuggestions.value) {
        handleSearch()
      }
      break
  }
}

// 窗口大小监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  clearTimeout(searchTimer)
})
</script>

<style scoped lang="scss">
@use "sass:color";

// 变量定义
$primary-color: #0089FF;
$primary-light: rgba(0, 137, 255, 0.1);
$border-radius: 16px;
$border-radius-sm: 12px;
$transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

// 遮罩层动画
.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.3s ease;
}

.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
}

// 滑入动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 淡入缩放
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.2s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

// 遮罩层
.global-search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
  
  @media (max-width: 768px) {
    padding: 0;
    align-items: flex-end;
  }
}

// 搜索弹窗
.search-modal {
  width: 900px;
  max-width: 90vw;
  max-height: 80vh;
  background: #fff;
  border-radius: $border-radius;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalSlideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  @keyframes modalSlideIn {
    from {
      opacity: 0;
      transform: translateY(-20px) scale(0.98);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }
  
  // 深色模式
  :global(.dark) & {
    background: #1e293b;
  }
  
  // 移动端适配
  &.is-mobile {
    width: 100%;
    max-width: 100%;
    max-height: 90vh;
    border-radius: $border-radius $border-radius 0 0;
    animation: mobileSlideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    @keyframes mobileSlideUp {
      from {
        transform: translateY(100%);
      }
      to {
        transform: translateY(0);
      }
    }
  }
  
  // 平板适配
  &.is-tablet {
    width: 700px;
  }
}

// 搜索头部
.search-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  
  :global(.dark) & {
    border-color: #334155;
  }
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

// 搜索输入框包装器
.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 28px;
  padding: 4px;
  transition: $transition;
  
  &:focus-within {
    background: #fff;
    box-shadow: 0 0 0 3px $primary-light, 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  :global(.dark) & {
    background: #334155;
    
    &:focus-within {
      background: #1e293b;
      box-shadow: 0 0 0 3px rgba(0, 137, 255, 0.2);
    }
  }
}

// 输入框图标
.input-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 20px;
  flex-shrink: 0;
  
  &.search-icon {
    color: $primary-color;
  }
  
  &.clear-btn,
  &.voice-btn {
    cursor: pointer;
    border-radius: 50%;
    transition: $transition;
    
    &:hover {
      background: rgba(0, 0, 0, 0.05);
      color: #1e293b;
    }
  }
  
  :global(.dark) & {
    color: #94a3b8;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: #f1f5f9;
    }
  }
}

// 搜索输入框
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  color: #1e293b;
  padding: 0 8px;
  height: 44px;
  outline: none;
  
  &::placeholder {
    color: #94a3b8;
  }
  
  :global(.dark) & {
    color: #f1f5f9;
    
    &::placeholder {
      color: #64748b;
    }
  }
  
  @media (max-width: 768px) {
    font-size: 15px;
  }
}

// 搜索提交按钮
.search-submit-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: $primary-color;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: $transition;
  flex-shrink: 0;
  
  &:hover {
    background: color.scale($primary-color, $lightness: -10%);
    transform: scale(1.05);
  }
  
  &:active {
    transform: scale(0.95);
  }
}

// 快捷筛选
.quick-filters {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.filter-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 16px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 13px;
  border: none;
  cursor: pointer;
  transition: $transition;
  
  .el-icon {
    font-size: 14px;
  }
  
  &:hover {
    background: #e2e8f0;
    color: #475569;
  }
  
  &.active {
    background: $primary-light;
    color: $primary-color;
    font-weight: 500;
  }
  
  :global(.dark) & {
    background: #334155;
    color: #94a3b8;
    
    &:hover {
      background: #475569;
      color: #cbd5e1;
    }
    
    &.active {
      background: rgba(0, 137, 255, 0.2);
      color: #60a5fa;
    }
  }
}

// 建议面板
.suggestions-panel {
  position: absolute;
  top: 100%;
  left: 24px;
  right: 24px;
  margin-top: 8px;
  background: #fff;
  border-radius: $border-radius-sm;
  box-shadow: var(--dt-shadow-2xl);
  z-index: 100;
  overflow: hidden;

  :global(.dark) & {
    background: #1e293b;
    box-shadow: var(--dt-shadow-3xl);
  }
  
  @media (max-width: 768px) {
    left: 16px;
    right: 16px;
  }
}

.suggestions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  
  :global(.dark) & {
    border-color: #334155;
  }
}

.suggestions-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.close-suggestions {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  border-radius: 4px;
  
  &:hover {
    background: #f1f5f9;
    color: #64748b;
  }
}

.suggestions-list {
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: $transition;
  
  &:hover,
  &.active {
    background: #f8fafc;
    
    :global(.dark) & {
      background: #334155;
    }
    
    .suggestion-arrow {
      opacity: 1;
      transform: translateX(4px);
    }
  }
}

.suggestion-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 8px;
  color: #64748b;
  font-size: 16px;
  flex-shrink: 0;
  
  :global(.dark) & {
    background: #475569;
    color: #94a3b8;
  }
}

.suggestion-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.suggestion-text {
  font-size: 14px;
  color: #1e293b;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  :deep(mark) {
    background: rgba(0, 137, 255, 0.2);
    color: $primary-color;
    font-weight: 600;
    padding: 0 2px;
    border-radius: 2px;
  }
  
  :global(.dark) & {
    color: #f1f5f9;
  }
}

.suggestion-type {
  font-size: 11px;
  color: #94a3b8;
  padding: 2px 6px;
  background: #f1f5f9;
  border-radius: 4px;
  flex-shrink: 0;
  
  :global(.dark) & {
    background: #475569;
    color: #94a3b8;
  }
}

.suggestion-arrow {
  font-size: 14px;
  color: #94a3b8;
  opacity: 0;
  transition: $transition;
}

// 搜索主体
.search-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

// 分类侧边栏
.category-sidebar {
  width: 180px;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
  padding: 16px 12px;
  flex-shrink: 0;
  overflow-y: auto;
  
  :global(.dark) & {
    background: #1e293b;
    border-color: #334155;
  }
  
  @media (max-width: 768px) {
    display: none;
  }
  
  &.collapsed {
    width: 0;
    padding: 0;
    overflow: hidden;
  }
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: $transition;
  position: relative;
  text-align: left;
  
  &:hover {
    background: #f1f5f9;
    color: #475569;
  }
  
  &.active {
    background: #fff;
    color: $primary-color;
    font-weight: 500;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    
    :global(.dark) & {
      background: #334155;
    }
  }
  
  :global(.dark) & {
    color: #94a3b8;
    
    &:hover {
      background: #334155;
      color: #cbd5e1;
    }
    
    &.active {
      color: #60a5fa;
    }
  }
}

.category-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.category-label {
  flex: 1;
}

.category-count {
  font-size: 11px;
  padding: 2px 6px;
  background: #e2e8f0;
  border-radius: 10px;
  color: #64748b;
  min-width: 20px;
  text-align: center;
  
  :global(.dark) & {
    background: #475569;
    color: #94a3b8;
  }
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: $primary-color;
  border-radius: 0 2px 2px 0;
}

// 结果区域
.results-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

// 加载状态
.loading-state {
  padding: 20px 0;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: $border-radius-sm;
  
  :global(.dark) & {
    background: #334155;
  }
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
  
  :global(.dark) & {
    background: linear-gradient(90deg, #475569 25%, #64748b 50%, #475569 75%);
  }
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 12px;
  border-radius: 4px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  
  :global(.dark) & {
    background: linear-gradient(90deg, #475569 25%, #64748b 50%, #475569 75%);
  }
  
  &.title { width: 40%; }
  &.desc { width: 70%; }
  &.meta { width: 30%; }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

// 默认状态
.default-state {
  max-width: 600px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  padding: 40px 0;
  
  @media (max-width: 768px) {
    padding: 24px 0;
  }
}

.welcome-illustration {
  margin-bottom: 24px;
}

.illustration-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: linear-gradient(135deg, $primary-color, #667eea);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  box-shadow: 0 10px 30px rgba(0, 137, 255, 0.3);
  
  @media (max-width: 768px) {
    width: 64px;
    height: 64px;
    font-size: 28px;
    border-radius: 20px;
  }
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  
  :global(.dark) & {
    color: #f1f5f9;
  }
  
  @media (max-width: 768px) {
    font-size: 20px;
  }
}

.welcome-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 32px 0;
  
  :global(.dark) & {
    color: #94a3b8;
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  background: #f8fafc;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition;
  
  &:hover {
    background: #f1f5f9;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
  
  :global(.dark) & {
    background: #334155;
    
    &:hover {
      background: #475569;
    }
  }
  
  @media (max-width: 768px) {
    padding: 16px 12px;
  }
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  
  &.bg-blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
  &.bg-purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
  &.bg-orange { background: linear-gradient(135deg, #f97316, #ea580c); }
  &.bg-green { background: linear-gradient(135deg, #22c55e, #16a34a); }
  
  @media (max-width: 768px) {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}

.action-label {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  
  :global(.dark) & {
    color: #cbd5e1;
  }
}

// 最近搜索
.recent-section {
  margin-top: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  
  .el-icon {
    font-size: 18px;
    color: $primary-color;
  }
  
  :global(.dark) & {
    color: #f1f5f9;
  }
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: $transition;
  
  &:hover {
    background: #f1f5f9;
    color: #ef4444;
  }
  
  :global(.dark) & {
    color: #94a3b8;
    
    &:hover {
      background: #334155;
      color: #f87171;
    }
  }
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: $transition;
  
  &:hover {
    background: #f1f5f9;
    
    .remove-btn {
      opacity: 1;
    }
  }
  
  :global(.dark) & {
    &:hover {
      background: #334155;
    }
  }
}

.recent-icon {
  font-size: 16px;
  color: #94a3b8;
  flex-shrink: 0;
}

.recent-text {
  flex: 1;
  font-size: 14px;
  color: #475569;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  :global(.dark) & {
    color: #cbd5e1;
  }
}

.remove-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  border-radius: 4px;
  opacity: 0;
  transition: $transition;
  
  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
  
  :global(.dark) & {
    opacity: 1;
    
    &:hover {
      background: rgba(248, 113, 113, 0.2);
      color: #f87171;
    }
  }
}

// 热门搜索
.trending-section {
  margin-top: 24px;
}

.trending-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.trending-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: $transition;
  
  &:hover {
    background: #e2e8f0;
    transform: translateY(-1px);
  }
  
  :global(.dark) & {
    background: #334155;
    color: #cbd5e1;
    
    &:hover {
      background: #475569;
    }
  }
}

.rank-number {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #cbd5e1;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  
  .rank-1 & { background: #ef4444; }
  .rank-2 & { background: #f97316; }
  .rank-3 & { background: #eab308; }
}

// 搜索结果
.results-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.result-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    &.sticky-header {
      position: sticky;
      top: 0;
      background: #fff;
      padding: 8px 0;
      z-index: 10;
      
      :global(.dark) & {
        background: #1e293b;
      }
    }
  }
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
    
    .el-icon {
      color: $primary-color;
    }
    
    :global(.dark) & {
      color: #f1f5f9;
    }
  }
  
  .result-count {
    margin-left: 6px;
    font-size: 12px;
    color: #94a3b8;
    font-weight: 400;
  }
}

.view-more-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  background: transparent;
  color: $primary-color;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: $transition;
  
  &:hover {
    background: $primary-light;
  }
  
  :global(.dark) & {
    color: #60a5fa;
    
    &:hover {
      background: rgba(0, 137, 255, 0.2);
    }
  }
}

.result-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px;
  background: #f8fafc;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition;
  animation: cardFadeIn 0.3s ease-out forwards;
  opacity: 0;
  
  @keyframes cardFadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  &.animate-0 { animation-delay: 0.05s; }
  &.animate-1 { animation-delay: 0.1s; }
  &.animate-2 { animation-delay: 0.15s; }
  &.animate-3 { animation-delay: 0.2s; }
  &.animate-4 { animation-delay: 0.25s; }
  
  &:hover {
    background: #f1f5f9;
    transform: translateX(4px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .card-actions {
      opacity: 1;
    }
  }
  
  :global(.dark) & {
    background: #334155;
    
    &:hover {
      background: #475569;
    }
  }
  
  @media (max-width: 768px) {
    padding: 12px;
    gap: 12px;
  }
}

.card-media {
  flex-shrink: 0;
}

.file-preview {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  font-size: 24px;
  
  &.default {
    background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
    color: #64748b;
  }
  
  &.pdf {
    background: linear-gradient(135deg, #fef2f2, #fee2e2);
    color: #ef4444;
  }
  
  &.doc {
    background: linear-gradient(135deg, #eff6ff, #dbeafe);
    color: #3b82f6;
  }
  
  &.xls {
    background: linear-gradient(135deg, #f0fdf4, #dcfce7);
    color: #22c55e;
  }
  
  &.ppt {
    background: linear-gradient(135deg, #fff7ed, #ffedd5);
    color: #f97316;
  }
  
  &.image {
    background: linear-gradient(135deg, #f5f3ff, #ede9fe);
    color: #8b5cf6;
  }
  
  &.video {
    background: linear-gradient(135deg, #fdf4ff, #fae8ff);
    color: #d946ef;
  }
  
  &.audio {
    background: linear-gradient(135deg, #ecfeff, #cffafe);
    color: #06b6d4;
  }
  
  &.zip {
    background: linear-gradient(135deg, #f8fafc, #f1f5f9);
    color: #64748b;
  }
  
  .file-ext {
    position: absolute;
    bottom: -2px;
    right: -2px;
    background: #fff;
    padding: 1px 4px;
    border-radius: 4px;
    font-size: 9px;
    font-weight: 600;
    color: #475569;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

.message-avatar {
  position: relative;
  
  .message-badge {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 18px;
    height: 18px;
    background: $primary-color;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 10px;
    border: 2px solid #fff;
    
    :global(.dark) & {
      border-color: #1e293b;
    }
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 6px;
}

.content-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  :deep(mark) {
    background: rgba(0, 137, 255, 0.2);
    color: $primary-color;
    font-weight: 600;
    padding: 0 2px;
    border-radius: 2px;
  }
  
  :global(.dark) & {
    color: #f1f5f9;
  }
}

.content-time {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.content-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 8px 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  
  :deep(mark) {
    background: rgba(0, 137, 255, 0.15);
    color: $primary-color;
    font-weight: 500;
    padding: 0 2px;
    border-radius: 2px;
  }
  
  :global(.dark) & {
    color: #94a3b8;
  }
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.contacts {
    background: #d1fae5;
    color: #065f46;
  }
  
  &.groups {
    background: #e0e7ff;
    color: #3730a3;
  }
  
  &.messages {
    background: #e0f2fe;
    color: #0369a1;
  }
  
  &.files {
    background: #fef3c7;
    color: #92400e;
  }
  
  :global(.dark) & {
    &.contacts {
      background: #064e3b;
      color: #6ee7b7;
    }
    
    &.groups {
      background: #312e81;
      color: #a5b4fc;
    }
    
    &.messages {
      background: #0c4a6e;
      color: #7dd3fc;
    }
    
    &.files {
      background: #78350f;
      color: #fcd34d;
    }
  }
}

.meta-source {
  font-size: 12px;
  color: #94a3b8;
  
  :global(.dark) & {
    color: #64748b;
  }
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  opacity: 0;
  transition: $transition;
  
  @media (max-width: 768px) {
    opacity: 1;
    flex-direction: row;
  }
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: $transition;
  
  &:hover {
    background: #e2e8f0;
    color: #64748b;
  }
  
  &.preview:hover {
    background: $primary-light;
    color: $primary-color;
  }
  
  :global(.dark) & {
    color: #64748b;
    
    &:hover {
      background: #475569;
      color: #94a3b8;
    }
    
    &.preview:hover {
      background: rgba(0, 137, 255, 0.2);
      color: #60a5fa;
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60px 20px;
  
  @media (max-width: 768px) {
    padding: 40px 16px;
  }
}

.empty-illustration {
  margin-bottom: 24px;
}

.empty-icon-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  background: #f1f5f9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #cbd5e1;
  
  :global(.dark) & {
    background: #334155;
    color: #64748b;
  }
  
  @media (max-width: 768px) {
    width: 80px;
    height: 80px;
    font-size: 36px;
  }
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  
  :global(.dark) & {
    color: #f1f5f9;
  }
}

.empty-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px 0;
  
  strong {
    color: $primary-color;
  }
  
  :global(.dark) & {
    color: #94a3b8;
  }
}

.empty-suggestions {
  .suggestions-label {
    font-size: 13px;
    color: #94a3b8;
    margin: 0 0 12px 0;
  }
}

.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.chip {
  padding: 8px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: $transition;
  
  &:hover {
    background: #e2e8f0;
    color: #1e293b;
  }
  
  :global(.dark) & {
    background: #334155;
    color: #cbd5e1;
    
    &:hover {
      background: #475569;
      color: #f1f5f9;
    }
  }
}

// 搜索底部
.search-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  font-size: 12px;
  color: #64748b;
  
  :global(.dark) & {
    background: #1e293b;
    border-color: #334155;
    color: #94a3b8;
  }
  
  @media (max-width: 768px) {
    padding: 10px 16px;
    font-size: 11px;
  }
}

.footer-shortcuts {
  display: flex;
  gap: 16px;
  
  @media (max-width: 768px) {
    gap: 12px;
  }
}

.shortcut {
  display: flex;
  align-items: center;
  gap: 6px;
  
  kbd {
    padding: 2px 6px;
    background: #fff;
    border: 1px solid #e2e8f0;
    border-radius: 4px;
    font-family: monospace;
    font-size: 11px;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.05);
    
    :global(.dark) & {
      background: #334155;
      border-color: #475569;
      color: #cbd5e1;
    }
  }
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  
  strong {
    color: $primary-color;
    font-weight: 600;
  }
}
</style>
