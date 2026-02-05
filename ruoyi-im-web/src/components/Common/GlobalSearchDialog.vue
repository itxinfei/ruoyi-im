<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="visible" class="search-overlay" @click="visible = false">
        <div class="search-modal" @click.stop>
          <!-- 搜索输入框 -->
          <div class="search-header">
            <div class="search-input-wrapper">
              <el-icon class="search-icon"><Search /></el-icon>
              <input
                ref="searchInputRef"
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="搜索联系人、群组、消息..."
                @input="handleInput"
                @keydown.enter="handleSearch"
                @keydown.esc="visible = false"
              >
              <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
                <el-icon><CircleClose /></el-icon>
              </button>
            </div>
            
            <!-- 分类筛选 -->
            <div class="filter-tabs">
              <button
                v-for="cat in categories"
                :key="cat.key"
                class="filter-tab"
                :class="{ active: searchType === cat.key }"
                @click="searchType = cat.key; handleSearch()"
              >
                {{ cat.label }}
              </button>
            </div>
          </div>

          <!-- 搜索结果 -->
          <div class="search-body">
            <!-- 加载中 -->
            <div v-if="searching" class="loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>搜索中...</span>
            </div>

            <!-- 结果列表 -->
            <div v-else-if="hasResults" class="results">
              <div v-for="(section, type) in filteredResults" :key="type" class="result-section">
                <div class="section-title">{{ section.title }} ({{ section.data.length }})</div>
                <div
                  v-for="item in section.data"
                  :key="item.id"
                  class="result-item"
                  @click="handleResultClick(type, item)"
                >
                  <div class="item-content">
                    <div class="item-title">{{ getItemTitle(item, type) }}</div>
                    <div class="item-desc">{{ getItemDesc(item, type) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="searchKeyword" class="empty">
              <el-icon><Search /></el-icon>
              <p>未找到相关结果</p>
            </div>

            <!-- 默认状态 -->
            <div v-else class="default">
              <el-icon><Search /></el-icon>
              <p>输入关键词开始搜索</p>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, CircleClose, Loading } from '@element-plus/icons-vue'
import { globalSearch } from '@/api/im/globalSearch'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'select'])

// 状态
const visible = ref(false)
const searchKeyword = ref('')
const searchType = ref('ALL')
const searchResult = ref(null)
const searching = ref(false)
const searchInputRef = ref(null)

// 分类配置
const categories = [
  { key: 'ALL', label: '全部' },
  { key: 'CONTACT', label: '联系人' },
  { key: 'GROUP', label: '群组' },
  { key: 'MESSAGE', label: '消息' },
  { key: 'FILE', label: '文件' }
]

// 计算属性
const hasResults = computed(() => {
  if (!searchResult.value) return false
  return (searchResult.value.contacts?.length || 0) +
         (searchResult.value.groups?.length || 0) +
         (searchResult.value.messages?.length || 0) +
         (searchResult.value.files?.length || 0) > 0
})

const filteredResults = computed(() => {
  if (!searchResult.value) return {}
  
  const sections = {
    contacts: { title: '联系人', data: searchResult.value.contacts || [] },
    groups: { title: '群组', data: searchResult.value.groups || [] },
    messages: { title: '消息', data: searchResult.value.messages || [] },
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
watch(() => props.modelValue, val => {
  visible.value = val
  if (val) {
    nextTick(() => searchInputRef.value?.focus())
  }
})

watch(visible, val => {
  if (!val) {
    emit('update:modelValue', false)
    searchKeyword.value = ''
    searchResult.value = null
  }
})

// 方法
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
    contacts: item.remark || item.departmentName || '',
    groups: `共 ${item.memberCount || 0} 人`,
    messages: item.content || '',
    files: item.fileSize ? `${(item.fileSize / 1024).toFixed(2)} KB` : ''
  }
  return descMap[type] || ''
}

let searchTimer = null
const handleInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    if (searchKeyword.value.trim()) {
      handleSearch()
    } else {
      searchResult.value = null
    }
  }, 300)
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  
  searching.value = true
  try {
    const res = await globalSearch({
      keyword,
      searchType: searchType.value,
      pageNum: 1,
      pageSize: 50
    })
    
    if (res.code === 200) {
      searchResult.value = res.data || {}
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const clearSearch = () => {
  searchKeyword.value = ''
  searchResult.value = null
  nextTick(() => searchInputRef.value?.focus())
}

const handleResultClick = (type, item) => {
  emit('select', { type, data: item })
  visible.value = false
}
</script>

<style scoped lang="scss">
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.search-modal {
  width: 100%;
  max-width: 600px;
  max-height: 80vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f3f4f6;
  border-radius: 8px;
  margin-bottom: 12px;
}

.search-icon {
  font-size: 20px;
  color: #6b7280;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  outline: none;
  
  &::placeholder {
    color: #9ca3af;
  }
}

.clear-btn {
  padding: 4px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
  
  &:hover {
    background: #e5e7eb;
  }
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  padding: 6px 12px;
  border: none;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #e5e7eb;
  }
  
  &.active {
    background: #3b82f6;
    color: #fff;
  }
}

.search-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.loading,
.empty,
.default {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
  
  .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.results {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.result-section {
  .section-title {
    font-size: 13px;
    font-weight: 600;
    color: #6b7280;
    margin-bottom: 8px;
    text-transform: uppercase;
  }
}

.result-item {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
  
  &:hover {
    background: #f3f4f6;
    transform: translateX(4px);
  }
}

.item-content {
  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: #111827;
    margin-bottom: 4px;
  }
  
  .item-desc {
    font-size: 13px;
    color: #6b7280;
  }
}

@media (max-width: 768px) {
  .search-modal {
    max-width: 100%;
    max-height: 90vh;
  }
}
</style>
