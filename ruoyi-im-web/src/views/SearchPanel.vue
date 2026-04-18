<template>
  <div class="search-panel-v2">
    <!-- 1. 顶部搜索框 (Spotlight 风格) -->
    <div class="search-top-bar">
      <div class="search-input-box" :class="{ 'is-active': keyword }">
        <el-icon class="prefix-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索联系人、群组、聊天记录、文件..."
          autofocus
          @input="handleInstantSearch"
          @keyup.enter="handleSearch"
        >
        <el-icon v-if="keyword" class="clear-icon" @click="clearSearch"><Close /></el-icon>
      </div>
    </div>

    <!-- 2. 主体区域 (左侧分类 + 右侧结果) -->
    <div class="search-main">
      <!-- 左侧分类导航 -->
      <div class="search-nav">
        <div
          v-for="tab in searchTabs"
          :key="tab.value"
          class="nav-item"
          :class="{ active: searchType === tab.value }"
          @click="switchTab(tab.value)"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </div>
      </div>

      <!-- 右侧结果区 -->
      <div v-loading="loading" class="search-body">
        <!-- 初始状态：搜索历史 -->
        <div v-if="!keyword && !searched" class="history-view">
          <div class="view-title">最近搜索</div>
          <div class="history-tags">
            <div v-for="h in searchHistory" :key="h" class="h-tag" @click="searchWithKeyword(h)">
              <el-icon><Clock /></el-icon>
              <span>{{ h }}</span>
            </div>
            <div v-if="!searchHistory.length" class="empty-text">暂无搜索记录</div>
          </div>
        </div>

        <!-- 结果列表 -->
        <div v-else-if="searched" class="results-view">
          <template v-if="searchType === 'all'">
            <!-- 综合搜索逻辑 -->
            <div v-for="section in categorizedResults" :key="section.type" class="result-section">
              <div v-if="section.list.length" class="section-header">
                {{ section.label }} ({{ section.list.length }})
              </div>
              <div class="section-list">
                <div v-for="item in section.list.slice(0, 5)" :key="item.id || item.userId || item.groupId" class="result-item" @click="handleItemClick(item, section.type)">
                  <DingtalkAvatar v-if="section.type !== 'file'" :src="item.avatar || item.senderAvatar" :name="item.nickname || item.senderName || item.groupName" :size="32" />
                  <div v-else class="file-thumb"><el-icon><Document /></el-icon></div>
                  
                  <div class="item-info">
                    <div class="item-title" v-html="highlightKeyword(item.nickname || item.senderName || item.groupName || item.fileName, keyword)"></div>
                    <div class="item-sub" v-html="highlightKeyword(item.content || item.department || item.source || '', keyword)"></div>
                  </div>
                  <div v-if="item.createTime" class="item-time">{{ formatTime(item.createTime) }}</div>
                </div>
              </div>
            </div>
          </template>

          <!-- 单分类搜索 -->
          <template v-else>
            <div class="section-list is-standalone">
              <div v-for="item in currentResults" :key="item.id || item.userId || item.groupId" class="result-item" @click="handleItemClick(item, searchType)">
                <!-- 渲染同上 -->
                <DingtalkAvatar v-if="searchType !== 'file'" :src="item.avatar || item.senderAvatar" :name="item.nickname || item.senderName || item.groupName" :size="36" />
                <div v-else class="file-thumb"><el-icon><Document /></el-icon></div>
                <div class="item-info">
                  <div class="item-title" v-html="highlightKeyword(item.nickname || item.senderName || item.groupName || item.fileName, keyword)"></div>
                  <div class="item-sub" v-html="highlightKeyword(item.content || item.department || item.source || '', keyword)"></div>
                </div>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-if="isEmptyResults" class="no-results">
            <el-icon><Search /></el-icon>
            <p>未找到与“{{ keyword }}”相关的结果</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { globalSearch } from '@/api/im/search'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'
import { Search, Close, Clock, ChatDotRound, User, Document, Collection } from '@element-plus/icons-vue'
import { useSearchHistory } from '@/composables/useSearchHistory'

const emit = defineEmits(['show-user', 'go-to-session', 'go-to-group'])
const { history: searchHistory, addToHistory, clearHistory } = useSearchHistory()

const keyword = ref('')
const searchType = ref('all')
const loading = ref(false)
const searched = ref(false)
const results = ref({ messages: [], contacts: [], groups: [], files: [] })
let searchTimer = null

const searchTabs = [
  { label: '全部', value: 'all', icon: Collection },
  { label: '联系人', value: 'contact', icon: User },
  { label: '群组', value: 'group', icon: ChatDotRound },
  { label: '记录', value: 'message', icon: ChatDotRound },
  { label: '文件', value: 'file', icon: Document }
]

const categorizedResults = computed(() => [
  { type: 'contact', label: '联系人', list: results.value.contacts || [] },
  { type: 'group', label: '群聊', list: results.value.groups || [] },
  { type: 'message', label: '聊天记录', list: results.value.messages || [] },
  { type: 'file', label: '文件', list: results.value.files || [] }
])

const currentResults = computed(() => {
  if (searchType.value === 'all') return []
  return results.value[`${searchType.value}s`] || results.value.messages || []
})

const isEmptyResults = computed(() => {
  return !categorizedResults.value.some(s => s.list.length > 0)
})

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  addToHistory(keyword.value.trim())
  try {
    const res = await globalSearch({ keyword: keyword.value.trim(), searchType: searchType.value })
    if (res.code === 200) results.value = res.data || {}
  } finally {
    loading.value = false
  }
}

const handleInstantSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword.value.trim()) { searched.value = false; return }
  searchTimer = setTimeout(handleSearch, 300)
}

const switchTab = (val) => {
  searchType.value = val
  if (keyword.value.trim()) handleSearch()
}

const handleItemClick = (item, type) => {
  if (type === 'contact') emit('show-user', item.userId)
  else if (type === 'group') emit('go-to-group', item.groupId)
  else if (type === 'message') emit('go-to-session', { targetId: item.senderId, messageId: item.messageId })
}

const highlightKeyword = (text, kw) => {
  if (!text || !kw) return text
  const regex = new RegExp(`(${kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return String(text).replace(regex, '<mark class="search-highlight">$1</mark>')
}

const formatTime = (t) => t ? new Date(t).toLocaleDateString() : ''
const clearSearch = () => { keyword.value = ''; searched.value = false; results.value = {} }
const searchWithKeyword = (k) => { keyword.value = k; handleSearch() }

onMounted(() => {
  window.addEventListener('keydown', (e) => {
    if (e.ctrlKey && e.key === 'f') { e.preventDefault(); /* 触发唤起逻辑 */ }
  })
})
</script>

<style scoped lang="scss">
.search-panel-v2 {
  height: 100%; display: flex; flex-direction: column; background: #fff;
}

.search-top-bar {
  height: 64px; padding: 0 24px; display: flex; align-items: center; border-bottom: 1px solid var(--dt-border-light);
}

.search-input-box {
  flex: 1; height: 36px; background: var(--dt-bg-hover); border-radius: 18px; display: flex; align-items: center; padding: 0 16px;
  transition: all 0.2s;
  &.is-active { background: #fff; box-shadow: 0 0 0 2px var(--dt-brand-lighter); border: 1px solid var(--dt-brand-color); }
  
  .prefix-icon { color: var(--dt-text-tertiary); margin-right: 10px; }
  input { flex: 1; border: none; background: transparent; outline: none; font-size: 14px; }
  .clear-icon { cursor: pointer; color: var(--dt-text-tertiary); &:hover { color: var(--dt-text-primary); } }
}

.search-main { flex: 1; display: flex; overflow: hidden; }

.search-nav {
  width: 160px; background: #fdfdfe; border-right: 1px solid var(--dt-border-light); padding: 12px 8px;
  .nav-item {
    height: 40px; display: flex; align-items: center; gap: 12px; padding: 0 16px; border-radius: 8px;
    cursor: pointer; color: var(--dt-text-secondary); font-size: 14px; transition: all 0.2s;
    &:hover { background: var(--dt-bg-hover); }
    &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
    .el-icon { font-size: 16px; }
  }
}

.search-body { flex: 1; overflow-y: auto; padding: 16px 24px; }

.view-title { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 12px; }
.history-tags {
  display: flex; flex-wrap: wrap; gap: 8px;
  .h-tag {
    padding: 4px 12px; background: var(--dt-bg-hover); border-radius: 4px; font-size: 13px;
    cursor: pointer; display: flex; align-items: center; gap: 6px;
    &:hover { background: var(--dt-border-light); }
  }
}

.result-section {
  margin-bottom: 24px;
  .section-header { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 8px; }
}

.result-item {
  display: flex; align-items: center; gap: 12px; padding: 10px; border-radius: 8px; cursor: pointer;
  &:hover { background: var(--dt-bg-hover); }
  .item-info { flex: 1; min-width: 0; }
  .item-title { font-size: 14px; color: var(--dt-text-primary); font-weight: 500; }
  .item-sub { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 2px; @include text-ellipsis; }
  .item-time { font-size: 11px; color: var(--dt-text-quaternary); }
  .file-thumb { width: 32px; height: 32px; background: var(--dt-bg-hover); border-radius: 4px; @include flex-center; color: var(--dt-brand-color); }
}

.no-results {
  padding-top: 100px; text-align: center; color: var(--dt-text-tertiary);
  .el-icon { font-size: 48px; opacity: 0.3; margin-bottom: 16px; }
}

:deep(.search-highlight) { background: transparent; color: var(--dt-brand-color); font-weight: 700; padding: 0; }
</style>
