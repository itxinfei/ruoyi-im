<template>
  <div class="search-panel">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-input-wrapper">
        <el-icon class="search-icon">
          <Search />
        </el-icon>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索消息、联系人、群组、文件..."
          autofocus
          @input="handleInstantSearch"
          @keyup.enter="handleSearch"
        >
        <el-icon v-if="keyword" class="clear-icon" @click="clearSearch">
          <Close />
        </el-icon>
      </div>
      <div class="search-scope">
        <div class="scope-tabs">
          <button :class="{ active: scopeMode === 'global' }" @click="setScopeMode('global')">
            全局
          </button>
          <button :class="{ active: scopeMode === 'session' }" @click="setScopeMode('session')">
            会话内
          </button>
        </div>
        <div v-if="scopeMode === 'session'" class="scope-tip">
          <span>当前会话</span>
          <span v-if="scopeSession?.name" class="scope-name">{{ scopeSession.name }}</span>
          <button class="scope-clear" @click="clearScope">
            清除
          </button>
        </div>
      </div>
      <div class="search-type-tabs">
        <div
          v-for="tab in searchTabs"
          :key="tab.value"
          class="type-tab"
          :class="{ active: searchType === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-loading="loading" class="search-content">
      <!-- 搜索历史与热门搜索 -->
      <div v-if="!keyword && !searched" class="initial-section">
        <!-- 搜索历史 -->
        <div v-if="searchHistory.length > 0" class="history-section">
          <div class="section-title">
            <span>搜索历史</span>
            <button class="clear-btn" @click="clearSearchHistory">
              清空
            </button>
          </div>
          <div class="history-list">
            <div
              v-for="(item, index) in searchHistory"
              :key="`history-${index}`"
              class="history-item"
              @click="searchWithKeyword(item)"
            >
              <el-icon class="history-icon">
                <Clock />
              </el-icon>
              <span class="history-text">{{ item }}</span>
            </div>
          </div>
        </div>
        <!-- 热门搜索 -->
        <div class="hot-searches">
          <div class="section-title">
            热门搜索
          </div>
          <div class="hot-keyword-list">
            <div
              v-for="(kw, index) in hotKeywords"
              :key="`hot-${index}`"
              class="hot-keyword"
              @click="searchWithKeyword(kw)"
            >
              {{ kw }}
            </div>
            <div v-if="!hotKeywords.length" class="empty-tip">
              暂无热门搜索
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索结果列表 -->
      <div v-else-if="searched" class="search-results">
        <template v-if="searchType === 'all'">
          <!-- 消息结果 -->
          <div v-if="results.messages?.length" class="result-section">
            <div class="section-title">
              <el-icon><ChatDotRound /></el-icon>
              消息
              <span class="count">({{ results.messages.length }})</span>
            </div>
            <div class="result-list">
              <div
                v-for="msg in results.messages.slice(0, 5)"
                :key="msg.id"
                class="result-item"
                @click="goToMessage(msg)"
              >
                <DingtalkAvatar :src="msg.senderAvatar" :name="msg.senderName" :size="36" />
                <div class="item-content">
                  <div class="item-title">
                    {{ msg.senderName }}
                  </div>
                  <div class="item-desc" v-html="highlightKeyword(msg.content, keyword)" />
                  <div class="item-time">
                    {{ formatTime(msg.createTime) }}
                  </div>
                </div>
                <button
                  v-if="scopeMode === 'session'"
                  class="locate-btn"
                  @click.stop="locateMessage(msg)"
                >
                  定位
                </button>
              </div>
            </div>
          </div>

          <!-- 联系人结果 -->
          <div v-if="results.contacts?.length" class="result-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              联系人
              <span class="count">({{ results.contacts.length }})</span>
            </div>
            <div class="result-list">
              <div
                v-for="contact in results.contacts.slice(0, 5)"
                :key="contact.userId"
                class="result-item"
                @click="goToContact(contact)"
              >
                <DingtalkAvatar :src="contact.avatar" :name="contact.nickname" :size="36" />
                <div class="item-content">
                  <div class="item-title">
                    {{ contact.nickname }}
                  </div>
                  <div class="item-desc">
                    {{ contact.department || '暂无部门' }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 群组结果 -->
          <div v-if="results.groups?.length" class="result-section">
            <div class="section-title">
              <el-icon><ChatDotRound /></el-icon>
              群组
              <span class="count">({{ results.groups.length }})</span>
            </div>
            <div class="result-list">
              <div
                v-for="group in results.groups.slice(0, 5)"
                :key="group.groupId"
                class="result-item"
                @click="goToGroup(group)"
              >
                <div class="group-avatar">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">
                    {{ group.groupName }}
                  </div>
                  <div class="item-desc">
                    {{ group.memberCount }}人
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 文件结果 -->
          <div v-if="results.files?.length" class="result-section">
            <div class="section-title">
              <el-icon><Document /></el-icon>
              文件
              <span class="count">({{ results.files.length }})</span>
            </div>
            <div class="result-list">
              <div
                v-for="file in results.files.slice(0, 5)"
                :key="file.id"
                class="result-item"
                @click="downloadFile(file)"
              >
                <div class="file-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">
                    {{ file.fileName }}
                  </div>
                  <div class="item-desc">
                    {{ formatFileSize(file.fileSize) }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空结果 -->
          <div v-if="isEmptyResults" class="empty-results">
            <el-icon><Search /></el-icon>
            <p>未找到相关结果</p>
          </div>
        </template>

        <!-- 单类型搜索结果 -->
        <template v-else>
          <div v-if="currentResults.length" class="result-list">
            <div
              v-for="item in currentResults"
              :key="item.id || item.userId || item.groupId"
              class="result-item"
              @click="handleItemClick(item)"
            >
              <template v-if="searchType === 'message'">
                <DingtalkAvatar :src="item.senderAvatar" :name="item.senderName" :size="36" />
                <div class="item-content">
                  <div class="item-title">
                    {{ item.senderName }}
                  </div>
                  <div class="item-desc" v-html="highlightKeyword(item.content, keyword)" />
                  <div class="item-time">
                    {{ formatTime(item.createTime) }}
                  </div>
                </div>
              </template>
              <template v-else-if="searchType === 'contact'">
                <DingtalkAvatar :src="item.avatar" :name="item.nickname" :size="36" />
                <div class="item-content">
                  <div class="item-title">
                    {{ item.nickname }}
                  </div>
                  <div class="item-desc">
                    {{ item.department || '暂无部门' }}
                  </div>
                </div>
              </template>
              <template v-else-if="searchType === 'group'">
                <div class="group-avatar">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">
                    {{ item.groupName }}
                  </div>
                  <div class="item-desc">
                    {{ item.memberCount }}人
                  </div>
                </div>
              </template>
              <template v-else-if="searchType === 'file'">
                <div class="file-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">
                    {{ item.fileName }}
                  </div>
                  <div class="item-desc">
                    {{ formatFileSize(item.fileSize) }}
                  </div>
                </div>
              </template>
            </div>
          </div>
          <div v-else class="empty-results">
            <el-icon><Search /></el-icon>
            <p>未找到相关结果</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { globalSearch, getHotKeywords } from '@/api/im/search'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'
import { Search, Close, Clock, ChatDotRound, User, Document } from '@element-plus/icons-vue'

const emit = defineEmits(['show-user', 'go-to-session', 'go-to-group'])

const keyword = ref('')
const searchType = ref('all')
const loading = ref(false)
const searched = ref(false)
const hotKeywords = ref([])
const searchHistory = ref([])
const results = ref({})
const scopeSession = ref(null)
const scopeMode = ref('global')
let searchTimer = null  // 搜索防抖定时器

const searchTabs = [
  { label: '全部', value: 'all' },
  { label: '消息', value: 'message' },
  { label: '联系人', value: 'contact' },
  { label: '群组', value: 'group' },
  { label: '文件', value: 'file' }
]

const currentResults = computed(() => {
  const type = searchType.value
  if (type === 'message') return results.value.messages || []
  if (type === 'contact') return results.value.contacts || []
  if (type === 'group') return results.value.groups || []
  if (type === 'file') return results.value.files || []
  if (type === 'workbench') return results.value.workbench || []
  return []
})

const isEmptyResults = computed(() => {
  const r = results.value
  return !(r.messages?.length || r.contacts?.length || r.groups?.length || r.files?.length)
})

// 加载热门搜索
const loadHotKeywords = async () => {
  try {
    const res = await getHotKeywords()
    if (res.code === 200) {
      hotKeywords.value = res.data || []
    }
  } catch (e) {
    console.error('获取热门搜索失败', e)
    ElMessage.error('获取热门搜索失败')
  }
}

// 加载搜索历史
const loadSearchHistory = () => {
  try {
    const saved = localStorage.getItem('im_search_history')
    if (saved) searchHistory.value = JSON.parse(saved)
  } catch (e) {
    console.error('加载搜索历史失败', e)
    ElMessage.error('加载搜索历史失败')
  }
}

// 保存到搜索历史
const saveToHistory = (kw) => {
  if (!kw || !kw.trim()) return
  const trimmed = kw.trim()
  const index = searchHistory.value.indexOf(trimmed)
  if (index !== -1) searchHistory.value.splice(index, 1)
  searchHistory.value.unshift(trimmed)
  searchHistory.value = searchHistory.value.slice(0, 10)
  localStorage.setItem('im_search_history', JSON.stringify(searchHistory.value))
}

// 清空搜索历史
const clearSearchHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('im_search_history')
}

const handleSearch = async () => {
  if (!keyword.value.trim()) return

  loading.value = true
  searched.value = true

  // 保存到搜索历史
  saveToHistory(keyword.value.trim())

  try {
    const res = await globalSearch({
      keyword: keyword.value.trim(),
      searchType: searchType.value,
      conversationId: scopeMode.value === 'session' ? (scopeSession.value?.sessionId || undefined) : undefined
    })
    if (res.code === 200) {
      const data = res.data || {}
      if (scopeMode.value === 'session' && scopeSession.value?.sessionId && Array.isArray(data.messages)) {
        data.messages = data.messages.filter(m => String(m.conversationId || m.sessionId || '') === String(scopeSession.value.sessionId))
      }
      results.value = data
    } else {
      ElMessage.error(res.msg || '搜索失败')
    }
  } catch (e) {
    console.error('搜索失败', e)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 即时搜索（钉钉规范：输入即搜索，防抖 300ms）
const handleInstantSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)

  if (!keyword.value.trim()) {
    searched.value = false
    results.value = {}
    return
  }

  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300)
}

const switchTab = (type) => {
  searchType.value = type
  if (keyword.value.trim()) {
    handleSearch()
  }
}

const searchWithKeyword = (kw) => {
  keyword.value = kw
  handleSearch()
}

const clearSearch = () => {
  keyword.value = ''
  searched.value = false
  results.value = {}
}

const loadScope = () => {
  try {
    const raw = localStorage.getItem('im_search_scope_session')
    if (raw) scopeSession.value = JSON.parse(raw)
  } catch {
    // 忽略解析错误
  }
}

const loadKeyword = () => {
  try {
    const raw = localStorage.getItem('im_search_keyword')
    if (raw) keyword.value = raw
  } catch {
    // 忽略解析错误
  }
}

const clearScope = () => {
  scopeSession.value = null
  localStorage.removeItem('im_search_scope_session')
}

const setScopeMode = (mode) => {
  scopeMode.value = mode
  if (mode === 'session' && !scopeSession.value) {
    ElMessage.info('请从会话详情进入以限定范围')
  }
  if (keyword.value.trim()) handleSearch()
}

const handleItemClick = (item) => {
  if (searchType.value === 'message') {
    emit('go-to-session', { targetId: item.senderId, type: 'PRIVATE' })
  } else if (searchType.value === 'contact') {
    emit('show-user', item.userId)
  } else if (searchType.value === 'group') {
    emit('go-to-group', item.groupId)
  }
}

const goToMessage = (msg) => {
  emit('go-to-session', { targetId: msg.senderId, type: 'PRIVATE' })
}

const locateMessage = (msg) => {
  if (!scopeSession.value?.sessionId) return
  emit('go-to-session', { targetId: scopeSession.value.sessionId, type: 'SESSION', messageId: msg.id || msg.messageId })
}

const goToContact = (contact) => {
  emit('show-user', contact.userId)
}

const goToGroup = (group) => {
  emit('go-to-group', group.groupId)
}

onMounted(() => {
  loadScope()
  loadKeyword()
  loadSearchHistory()
  if (scopeSession.value?.sessionId) scopeMode.value = 'session'
  if (keyword.value.trim()) handleSearch()
})

const downloadFile = (file) => {
  window.open(file.fileUrl)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

  return date.toLocaleDateString()
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

// HTML转义防止XSS
const escapeHtml = (str) => {
  if (!str) return ''
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

// 高亮搜索关键词
const highlightKeyword = (text, keyword) => {
  if (!text || !keyword) return escapeHtml(text) || ''
  const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  const escapedText = escapeHtml(text)
  return escapedText.replace(regex, '<mark class="search-highlight">$1</mark>')
}

// 初始化
loadHotKeywords()
</script>

<style scoped lang="scss">
.search-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
}

.search-header {
  padding: var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);

  .search-icon {
    color: var(--dt-text-tertiary);
    margin-right: var(--dt-spacing-sm);
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: var(--dt-font-size-sm);

    &::placeholder {
      color: var(--dt-text-tertiary);
    }
  }

  .clear-icon {
    color: var(--dt-text-tertiary);
    cursor: pointer;
    font-size: var(--dt-icon-size-lg);

    &:hover {
      color: var(--dt-text-secondary);
    }
  }
}

.search-type-tabs {
  display: flex;
  gap: var(--dt-spacing-sm);
  margin-top: var(--dt-spacing-md);

  .type-tab {
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    border-radius: var(--dt-radius-full);
    font-size: var(--dt-font-size-xs);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    &.active {
      background: var(--dt-brand-color);
      color: var(--dt-text-primary);
    }
  }
}

.search-scope {
  margin-top: 8px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.scope-tabs {
  display: flex;
  gap: 6px;
}

.scope-tabs button {
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
  color: var(--dt-text-secondary);
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--dt-radius-xl);
  cursor: pointer;
}

.scope-tabs button.active {
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  border-color: var(--dt-brand-light);
}

.scope-tip {
  display: flex;
  align-items: center;
  gap: 8px;
}

.scope-name {
  color: var(--dt-text-secondary);
}

.scope-clear {
  border: none;
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  font-size: 12px;
}

.search-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-lg);
}

.initial-section {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xl);
}

.history-section {
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
    margin-bottom: var(--dt-spacing-md);

    .clear-btn {
      border: none;
      background: transparent;
      color: var(--dt-brand-color);
      font-size: var(--dt-font-size-xs);
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: var(--dt-spacing-sm);
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs);
    padding: var(--dt-spacing-xs) var(--dt-spacing-sm);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-full);
    font-size: var(--dt-font-size-xs);
    cursor: pointer;
    transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
    max-width: 150px;

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    .history-icon {
      font-size: var(--dt-icon-size-md);
      color: var(--dt-text-tertiary);
    }

    .history-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.hot-searches {
  .section-title {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
    margin-bottom: var(--dt-spacing-md);
  }

  .hot-keyword-list {
    display: flex;
    flex-wrap: wrap;
    gap: var(--dt-spacing-xs);

    .hot-keyword {
      padding: var(--dt-spacing-xs) var(--dt-spacing-md);
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-full);
      font-size: var(--dt-font-size-xs);
      cursor: pointer;
      transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);

      &:hover {
        background: var(--dt-brand-bg);
        color: var(--dt-brand-color);
      }
    }

    .empty-tip {
      color: var(--dt-text-tertiary);
      font-size: var(--dt-font-size-xs);
    }
  }
}

.result-section {
  margin-bottom: var(--dt-spacing-xl);

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
    margin-bottom: var(--dt-spacing-md);

    .el-icon {
      font-size: var(--dt-icon-size-lg);
      color: var(--dt-brand-color);
    }

    .count {
      font-weight: normal;
      color: var(--dt-text-tertiary);
    }
  }
}

.result-list {
  .result-item {
    display: flex;
    align-items: center;
    padding: var(--dt-spacing-md);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: background var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-body);
    }

    .item-content {
      flex: 1;
      margin-left: var(--dt-spacing-md);
      min-width: 0;

      .item-title {
        font-size: var(--dt-font-size-sm);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-desc {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        margin-top: var(--dt-spacing-xs);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-time {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        margin-top: var(--dt-spacing-xs);
      }
    }

    .group-avatar,
    .file-icon {
      width: var(--dt-avatar-size-md);
      height: var(--dt-avatar-size-md);
      border-radius: var(--dt-radius-md);
      background: var(--dt-brand-bg);
      display: flex;
      align-items: center;
      justify-content: center;

      .el-icon {
        color: var(--dt-brand-color);
      }
    }
  }
}

.locate-btn {
  margin-left: auto;
  border: none;
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  font-size: 12px;
  padding: 4px 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
}

.empty-results {
  text-align: center;
  padding: var(--dt-spacing-2xl) 0;
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: var(--dt-icon-size-xl);
    opacity: 0.5;
  }

  p {
    margin-top: var(--dt-spacing-md);
    font-size: var(--dt-font-size-sm);
  }
}

/* 搜索关键词高亮 */
:deep(.search-highlight) {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  padding: 0 2px;
  border-radius: var(--dt-radius-xs);
}
</style>
