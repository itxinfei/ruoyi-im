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
import { ref, computed, watch, nextTick, onMounted } from 'vue'
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

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})
</script>

<style scoped lang="scss">
.classic-search-dialog {
  // 核心修复：确保对话框及其遮罩层不会阻挡输入
  pointer-events: auto !important;

  :deep(.el-dialog) {
    padding: 0;
    border-radius: 12px;
    overflow: hidden;
    position: relative;
    z-index: 3000;
    
    // 移动端全屏优化
    @media (max-width: 480px) {
      border-radius: 0;
      margin: 0 !important;
      height: 100vh;
      max-height: 100vh;
    }
    
    .el-dialog__header {
      margin: 0;
      padding: 16px 20px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      .dark & { background: #1e293b; border-color: #334155; }
      
      .el-dialog__title { 
        font-size: 16px; 
        font-weight: 600;
        color: #1e293b;
        .dark & { color: #f1f5f9; }
      }
      
      .el-dialog__headerbtn {
        top: 16px;
        right: 20px;
        
        .el-dialog__close {
          color: #64748b;
          font-size: 18px;
          
          &:hover {
            color: #1e293b;
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
  
  // 移动端适配
  @media (max-width: 576px) {
    height: 80vh;
  }
  
  @media (max-width: 768px) {
    height: 85vh;
  }
  
  @media (max-width: 480px) {
    height: 100vh;
    max-height: 100vh;
  }
}

.classic-search-bar {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
  .dark & { border-color: #1e293b; background: #1e293b; }

  .square-input {
    // 确保 input 元素本身是可交互的
    :deep(input) {
      cursor: text !important;
      pointer-events: auto !important;
      font-size: 14px;
      padding: 0 12px;
    }

    :deep(.el-input__wrapper) {
      border-radius: 8px;
      box-shadow: 0 0 0 1px #e2e8f0;
      transition: all 0.2s ease;
      
      &:hover {
        box-shadow: 0 0 0 1px #cbd5e1;
      }
      
      &.is-focus { 
        box-shadow: 0 0 0 2px #1677ff;
        border-color: #1677ff; 
      }
      
      .dark & { 
        background: #0f172a; 
        border-color: #334155;
        box-shadow: 0 0 0 1px #334155;
        
        &:hover {
          box-shadow: 0 0 0 1px #475569;
        }
      }
    }
    :deep(.el-input-group__append) {
      background: #1677ff;
      color: #fff;
      border: none;
      border-radius: 0 8px 8px 0;
      font-weight: 500;
      padding: 0 16px;
      transition: all 0.2s ease;
      
      &:hover {
        background: #0958d9;
      }
    }
  }
  
  // 移动端优化
  @media (max-width: 480px) {
    padding: 12px 16px;
    
    .square-input {
      :deep(input) {
        font-size: 16px; // 防止iOS缩放
      }
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
  
  @media (max-width: 768px) {
    width: 120px;
  }

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

    &:hover { 
      background: #f1f5f9; 
      color: #1e293b; 
      .dark & { background: #0f172a; color: #f1f5f9; } 
    }
    &.active {
      background: #fff;
      color: #1677ff;
      font-weight: 600;
      border-right: 3px solid #1677ff;
      box-shadow: inset 0 0 0 1px #e2e8f0;
      .dark & { background: #0f172a; }
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
  
  // 移动端适配
  @media (max-width: 768px) {
    width: 100px;
    
    .cat-tab {
      padding: 10px 8px;
      font-size: 12px;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 60px;
      
      .label {
        font-size: 11px;
        margin-bottom: 2px;
        text-align: center;
      }
      
      .count-badge {
        position: absolute;
        top: 4px;
        right: 4px;
        font-size: 10px;
        padding: 1px 4px;
      }
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
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    color: #475569;
    transition: all 0.2s ease;
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
      border-radius: 4px;
      transition: all 0.2s ease;
      
      // 移动端始终显示
      @media (max-width: 768px) {
        opacity: 1;
      }
      
      &:hover { 
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444; 
      }
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
       padding: 14px 20px;
       display: flex;
       gap: 12px;
       cursor: pointer;
       border-bottom: 1px solid #f8fafc;
       transition: all 0.2s ease;
       min-height: 58px; // 确保一致的高度
       .dark & { border-color: #1e293b; }
       &:hover { 
         background: #f1f5f9; 
         .dark & { background: #1e293b; } 
         
         // 添加轻微的缩放效果
         transform: translateX(2px);
       }

       .avatar-box {
         flex-shrink: 0;
         width: 34px;
         height: 34px;
         
         .file-icon-bg {
           width: 34px; 
           height: 34px;
           background: #eff6ff; 
           color: #3b82f6;
           display: flex; 
           align-items: center; 
           justify-content: center;
           border-radius: 6px;
           font-size: 16px;
         }
       }

       .info-box {
         flex: 1; 
         min-width: 0;
         display: flex;
         flex-direction: column;
         justify-content: center;
         
         .info-top {
           display: flex; 
           justify-content: space-between; 
           align-items: center;
           margin-bottom: 4px;
           
           .name-text { 
             font-size: 14px; 
             font-weight: 500; 
             color: #1e293b; 
             .dark & { color: #f1f5f9; }
           }
           .time-text { 
             font-size: 11px; 
             color: #94a3b8;
             flex-shrink: 0;
             margin-left: 8px;
           }
         }
         .desc-text { 
           font-size: 12px; 
           color: #64748b; 
           overflow: hidden; 
           text-overflow: ellipsis; 
           white-space: nowrap;
           line-height: 1.4;
         }
       }
     }
     
     // 移动端优化
     @media (max-width: 768px) {
       .result-row {
         padding: 12px 16px;
         
         &:active {
           background: #e2e8f0;
           transform: scale(0.98);
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
  
  // 移动端隐藏快捷键提示
  @media (max-width: 768px) {
    .flex:first-child {
      display: none;
    }
    
    padding: 8px 16px;
    justify-content: center;
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
