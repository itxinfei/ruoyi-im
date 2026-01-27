<template>
  <el-dialog
    v-model="visible"
    title="全局搜索"
    width="800px"
    :show-close="true"
    class="classic-search-dialog"
    destroy-on-close
    append-to-body
    :modal-append-to-body="true"
    :lock-scroll="true"
  >
    <div class="classic-container">
      <!-- 搜索栏区：直角、硬朗 -->
      <div class="classic-search-bar">
        <el-input
          ref="searchInputRef"
          v-model="searchKeyword"
          placeholder="搜索联系人、群组、聊天记录..."
          class="square-input"
          clearable
          autofocus
          @input="handleInput"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">检索</el-button>
          </template>
        </el-input>
      </div>

      <div class="classic-body">
        <!-- 左侧分类：方正列表 -->
        <div class="classic-aside">
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

        <!-- 右侧结果：标准列表 -->
        <div class="classic-main scrollbar-thin">
          <!-- 加载状态 -->
          <div v-if="searching" class="p-20 flex flex-col items-center justify-center">
            <el-icon class="is-loading text-2xl text-primary mb-2"><Loading /></el-icon>
            <span class="text-xs text-slate-400">正在搜索...</span>
          </div>

          <!-- 默认状态：历史记录 -->
          <div v-else-if="!searchKeyword && historyKeywords.length > 0" class="p-4">
            <div class="section-label">最近搜索历史</div>
            <div class="history-grid">
              <div 
                v-for="kw in historyKeywords" 
                :key="kw" 
                class="history-item"
                @click="searchKeyword = kw; handleSearch()"
              >
                <el-icon class="mr-2 opacity-40"><Timer /></el-icon>
                <span class="flex-1 truncate">{{ kw }}</span>
                <el-icon class="close-icon" @click.stop="removeHistory(kw)"><Close /></el-icon>
              </div>
            </div>
          </div>

          <!-- 搜索结果列表 -->
          <div v-else-if="hasResults" class="results-list">
            <div v-for="(section, type) in filteredResultSections" :key="type" class="result-group">
              <div class="group-header">{{ section.title }}</div>
              
              <div
                v-for="item in section.data"
                :key="item.id"
                class="result-row"
                @click="handleItemClick(type, item)"
              >
                <!-- 头像 -->
                <div class="avatar-box">
                  <DingtalkAvatar
                    v-if="type !== 'files'"
                    :name="item.name || item.friendName || item.senderName"
                    :src="item.avatar || item.friendAvatar"
                    :size="34"
                    shape="square"
                  />
                  <div v-else class="file-icon-bg">
                    <el-icon><Document /></el-icon>
                  </div>
                </div>

                <!-- 信息 -->
                <div class="info-box">
                  <div class="info-top">
                    <span class="name-text" v-html="highlight(item.name || item.friendName || item.senderName || item.fileName)"></span>
                    <span class="time-text">{{ formatTime(item.timestamp) }}</span>
                  </div>
                  <div class="desc-text" v-html="highlight(item.content || item.remark || item.departmentName || (item.memberCount ? item.memberCount + '人' : ''))"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="searchKeyword && !searching" class="py-20">
            <el-empty description="未找到匹配项" :image-size="80" />
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
import { ref, computed, watch, nextTick } from 'vue'
import { Search, Document, Loading, Timer, Close } from '@element-plus/icons-vue'
import { globalSearch } from '@/api/im/globalSearch'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'select-message', 'select-contact', 'select-group', 'select-file'])

const visible = ref(false)
const searchKeyword = ref('')
const searchType = ref('ALL')
const searchResult = ref(null)
const searching = ref(false)
const searchInputRef = ref(null)
const historyKeywords = ref(JSON.parse(localStorage.getItem('search_history') || '[]'))

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

let timer = null
const handleInput = () => {
  if (!searchKeyword.value.trim()) {
    searchResult.value = null
    return
  }
  clearTimeout(timer)
  timer = setTimeout(handleSearch, 300)
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  searching.value = true
  try {
    const res = await globalSearch({ keyword, searchType: searchType.value, pageNum: 1, pageSize: 50 })
    searchResult.value = res.data || {}
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
  return String(text).replace(reg, '<span style="color: #1677ff; font-weight: bold;">$1</span>')
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

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts); const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
  return (d.getMonth() + 1) + '-' + d.getDate()
}
</script>

<style scoped lang="scss">
.classic-search-dialog {
  // 核心修复：确保对话框及其遮罩层不会阻挡输入
  pointer-events: auto !important;

  :deep(.el-dialog) {
    padding: 0;
    border-radius: 4px;
    overflow: hidden;
    position: relative;
    z-index: 3000; // 确保在最上层
    
    .el-dialog__header {
      margin: 0;
      padding: 12px 20px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      .el-dialog__title { font-size: 14px; font-weight: bold; }
    }
    .el-dialog__body { padding: 0; }
  }
}

.classic-container {
  display: flex;
  flex-direction: column;
  height: 620px;
  background: #fff;
  .dark & { background: #0f172a; }
  // 确保容器内所有点击事件正常
  position: relative;
  z-index: 10;
}

.classic-search-bar {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  .dark & { border-color: #1e293b; background: #1e293b; }

  .square-input {
    // 确保 input 元素本身是可交互的
    :deep(input) {
      cursor: text !important;
      pointer-events: auto !important;
    }

    :deep(.el-input__wrapper) {
      border-radius: 2px;
      box-shadow: none !important;
      border: 1px solid #cbd5e1;
      &.is-focus { border-color: #1677ff; }
      .dark & { background: #0f172a; border-color: #334155; }
    }
    :deep(.el-input-group__append) {
      background: #1677ff;
      color: #fff;
      border: none;
      border-radius: 0 2px 2px 0;
      font-weight: bold;
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
  .dark & { background: #1e293b; border-color: #334155; }

  .cat-tab {
    padding: 12px 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;
    font-size: 13px;
    color: #64748b;
    transition: all 0.2s;

    &:hover { background: #f1f5f9; color: #1e293b; .dark & { background: #0f172a; color: #f1f5f9; } }
    &.active {
      background: #fff;
      color: #1677ff;
      font-weight: bold;
      border-right: 2px solid #1677ff;
      .dark & { background: #0f172a; }
    }

    .count-badge {
      font-size: 10px;
      background: #e2e8f0;
      padding: 0 6px;
      border-radius: 10px;
      .dark & { background: #334155; }
    }
  }
}

.classic-main {
  flex: 1;
  overflow-y: auto;
  
  .section-label {
    padding: 12px 20px 8px;
    font-size: 11px;
    font-weight: bold;
    color: #94a3b8;
    text-transform: uppercase;
  }
}

.history-grid {
  padding: 0 12px;
  .history-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    color: #475569;
    .dark & { color: #cbd5e1; }
    &:hover {
      background: #f8fafc;
      .dark & { background: #1e293b; }
      .close-icon { opacity: 1; }
    }
    .close-icon {
      opacity: 0;
      font-size: 12px;
      padding: 4px;
      &:hover { color: #ef4444; }
    }
  }
}

.results-list {
  .result-group {
    .group-header {
      padding: 8px 20px;
      background: #f8fafc;
      font-size: 12px;
      font-weight: bold;
      color: #64748b;
      border-bottom: 1px solid #f1f5f9;
      .dark & { background: #1e293b; border-color: #334155; color: #94a3b8; }
    }

    .result-row {
      padding: 12px 20px;
      display: flex;
      gap: 12px;
      cursor: pointer;
      border-bottom: 1px solid #f8fafc;
      .dark & { border-color: #1e293b; }
      &:hover { background: #f1f5f9; .dark & { background: #1e293b; } }

      .avatar-box {
        flex-shrink: 0;
        .file-icon-bg {
          width: 34px; height: 34px;
          background: #eff6ff; color: #3b82f6;
          display: flex; align-items: center; justify-content: center;
          border-radius: 4px;
        }
      }

      .info-box {
        flex: 1; min-width: 0;
        .info-top {
          display: flex; justify-content: space-between; margin-bottom: 2px;
          .name-text { font-size: 14px; font-weight: 500; color: #1e293b; .dark & { color: #f1f5f9; } }
          .time-text { font-size: 11px; color: #94a3b8; }
        }
        .desc-text { font-size: 12px; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      }
    }
  }
}

.classic-footer {
  padding: 8px 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  .dark & { background: #1e293b; border-color: #334155; }
  kbd {
    background: #fff;
    border: 1px solid #e2e8f0;
    padding: 1px 4px;
    border-radius: 3px;
    margin-right: 4px;
    .dark & { background: #0f172a; border-color: #334155; }
  }
}

.scrollbar-thin::-webkit-scrollbar { width: 6px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; .dark & { background: #334155; } }
</style>
