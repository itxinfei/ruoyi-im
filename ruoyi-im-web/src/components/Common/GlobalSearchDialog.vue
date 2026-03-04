<template>
  <el-dialog
    v-model="visible"
    width="680px"
    :show-close="false"
    class="global-search-dialog"
    destroy-on-close
    append-to-body
    @open="handleOpen"
  >
    <div class="search-container">
      <!-- 搜索头部 -->
      <div class="search-header">
        <span class="material-icons-outlined search-icon">search</span>
        <input
          ref="searchInputRef"
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索消息、联系人、群组、文件..."
          @keyup.enter="handleSearch"
          @keyup.esc="handleClose"
        />
        <span v-if="keyword" class="material-icons-outlined clear-icon" @click="clearKeyword">close</span>
      </div>

      <!-- 搜索类型标签 -->
      <div class="search-tabs">
        <div
          v-for="tab in searchTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: searchType === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 搜索内容区 -->
      <div class="search-content" v-loading="loading">
        <!-- 热门搜索 -->
        <div v-if="!keyword && !searched" class="hot-section">
          <div class="section-title">
            <span class="material-icons-outlined">local_fire_department</span>
            热门搜索
          </div>
          <div class="hot-keywords">
            <span
              v-for="(kw, index) in hotKeywords"
              :key="index"
              class="hot-keyword"
              @click="searchWithKeyword(kw)"
            >
              {{ kw }}
            </span>
            <div v-if="!hotKeywords.length" class="empty-tip">暂无热门搜索</div>
          </div>
        </div>

        <!-- 搜索历史 -->
        <div v-if="!keyword && !searched && searchHistory.length" class="history-section">
          <div class="section-title">
            <span class="material-icons-outlined">history</span>
            搜索历史
            <span class="clear-history" @click="clearHistory">清空</span>
          </div>
          <div class="history-list">
            <div
              v-for="(item, index) in searchHistory"
              :key="index"
              class="history-item"
              @click="searchWithKeyword(item)"
            >
              <span class="material-icons-outlined history-icon">history</span>
              <span class="history-text">{{ item }}</span>
            </div>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-else-if="searched" class="results-section">
          <!-- 无结果 -->
          <div v-if="isEmpty" class="empty-result">
            <span class="material-icons-outlined empty-icon">search_off</span>
            <p>未找到相关内容</p>
          </div>

          <!-- 消息结果 -->
          <template v-else>
            <div v-if="results.messages?.length" class="result-group">
              <div class="group-header">
                <span class="material-icons-outlined">chat</span>
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
                  <DingtalkAvatar :src="msg.senderAvatar" :name="msg.senderName" :size="40" />
                  <div class="item-content">
                    <div class="item-header">
                      <span class="item-title">{{ msg.senderName }}</span>
                      <span class="item-time">{{ formatTime(msg.createTime) }}</span>
                    </div>
                    <div class="item-preview" v-html="highlightKeyword(msg.content)"></div>
                  </div>
                </div>
                <div v-if="results.messages.length > 5" class="view-more" @click="viewMore('messages')">
                  查看更多消息
                </div>
              </div>
            </div>

            <!-- 联系人结果 -->
            <div v-if="results.contacts?.length" class="result-group">
              <div class="group-header">
                <span class="material-icons-outlined">person</span>
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
                  <DingtalkAvatar :src="contact.avatar" :name="contact.nickname" :size="40" />
                  <div class="item-content">
                    <div class="item-header">
                      <span class="item-title" v-html="highlightKeyword(contact.nickname)"></span>
                    </div>
                    <div class="item-desc">{{ contact.department || '暂无部门' }}</div>
                  </div>
                </div>
                <div v-if="results.contacts.length > 5" class="view-more" @click="viewMore('contacts')">
                  查看更多联系人
                </div>
              </div>
            </div>

            <!-- 群组结果 -->
            <div v-if="results.groups?.length" class="result-group">
              <div class="group-header">
                <span class="material-icons-outlined">groups</span>
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
                  <DingtalkAvatar :src="group.avatar" :name="group.groupName" :size="40" shape="square" />
                  <div class="item-content">
                    <div class="item-header">
                      <span class="item-title" v-html="highlightKeyword(group.groupName)"></span>
                    </div>
                    <div class="item-desc">{{ group.memberCount || 0 }} 人</div>
                  </div>
                </div>
                <div v-if="results.groups.length > 5" class="view-more" @click="viewMore('groups')">
                  查看更多群组
                </div>
              </div>
            </div>

            <!-- 文件结果 -->
            <div v-if="results.files?.length" class="result-group">
              <div class="group-header">
                <span class="material-icons-outlined">folder</span>
                文件
                <span class="count">({{ results.files.length }})</span>
              </div>
              <div class="result-list">
                <div
                  v-for="file in results.files.slice(0, 5)"
                  :key="file.id"
                  class="result-item"
                  @click="goToFile(file)"
                >
                  <div class="file-icon">
                    <span class="material-icons-outlined">{{ getFileIcon(file.fileType) }}</span>
                  </div>
                  <div class="item-content">
                    <div class="item-header">
                      <span class="item-title" v-html="highlightKeyword(file.fileName)"></span>
                    </div>
                    <div class="item-desc">{{ formatFileSize(file.fileSize) }} · {{ file.senderName }}</div>
                  </div>
                </div>
                <div v-if="results.files.length > 5" class="view-more" @click="viewMore('files')">
                  查看更多文件
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { globalSearch, getHotKeywords } from '@/api/im/search'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'go-message', 'go-contact', 'go-group', 'go-file'])

const store = useStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const searchInputRef = ref(null)
const keyword = ref('')
const searchType = ref('all')
const loading = ref(false)
const searched = ref(false)
const hotKeywords = ref([])
const searchHistory = ref([])
const results = ref({
  messages: [],
  contacts: [],
  groups: [],
  files: []
})

const searchTabs = [
  { label: '全部', value: 'all' },
  { label: '消息', value: 'message' },
  { label: '联系人', value: 'contact' },
  { label: '群组', value: 'group' },
  { label: '文件', value: 'file' }
]

const isEmpty = computed(() => {
  return !results.value.messages?.length &&
         !results.value.contacts?.length &&
         !results.value.groups?.length &&
         !results.value.files?.length
})

const handleOpen = async () => {
  await nextTick()
  searchInputRef.value?.focus()
  loadHotKeywords()
  loadSearchHistory()
}

const handleClose = () => {
  visible.value = false
}

const clearKeyword = () => {
  keyword.value = ''
  searched.value = false
  results.value = { messages: [], contacts: [], groups: [], files: [] }
  searchInputRef.value?.focus()
}

const switchTab = (type) => {
  searchType.value = type
  if (keyword.value) {
    handleSearch()
  }
}

const handleSearch = async () => {
  if (!keyword.value.trim()) return

  loading.value = true
  searched.value = true

  try {
    const res = await globalSearch({
      keyword: keyword.value,
      searchType: searchType.value
    })

    if (res.code === 200) {
      results.value = {
        messages: res.data.messages || [],
        contacts: res.data.contacts || [],
        groups: res.data.groups || [],
        files: res.data.files || []
      }
      saveSearchHistory(keyword.value)
    }
  } catch (e) {
    console.error('搜索失败', e)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const searchWithKeyword = (kw) => {
  keyword.value = kw
  handleSearch()
}

const loadHotKeywords = async () => {
  try {
    const res = await getHotKeywords()
    if (res.code === 200) {
      hotKeywords.value = res.data || []
    }
  } catch (e) {
    console.error('加载热门搜索失败', e)
  }
}

const loadSearchHistory = () => {
  const history = localStorage.getItem('im_search_history')
  if (history) {
    searchHistory.value = JSON.parse(history).slice(0, 10)
  }
}

const saveSearchHistory = (kw) => {
  let history = JSON.parse(localStorage.getItem('im_search_history') || '[]')
  history = [kw, ...history.filter(item => item !== kw)].slice(0, 10)
  localStorage.setItem('im_search_history', JSON.stringify(history))
  searchHistory.value = history
}

const clearHistory = () => {
  localStorage.removeItem('im_search_history')
  searchHistory.value = []
  ElMessage.success('已清空搜索历史')
}

const highlightKeyword = (text) => {
  if (!text || !keyword.value) return text
  const regex = new RegExp(`(${keyword.value})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}/${date.getDate()}`
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

const getFileIcon = (type) => {
  const icons = {
    'image': 'image',
    'video': 'videocam',
    'audio': 'audiotrack',
    'pdf': 'picture_as_pdf',
    'doc': 'description',
    'xls': 'table_chart',
    'ppt': 'slideshow',
    'zip': 'folder_zip'
  }
  return icons[type] || 'insert_drive_file'
}

const goToMessage = (msg) => {
  emit('go-message', msg)
  handleClose()
}

const goToContact = (contact) => {
  emit('go-contact', contact)
  handleClose()
}

const goToGroup = (group) => {
  emit('go-group', group)
  handleClose()
}

const goToFile = (file) => {
  emit('go-file', file)
  handleClose()
}

const viewMore = (type) => {
  searchType.value = type
  handleSearch()
}
</script>

<style scoped lang="scss">
.global-search-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
    border-radius: 12px;
    overflow: hidden;
  }
}

.search-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.search-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .search-icon {
    font-size: 24px;
    color: #8f959e;
    margin-right: 12px;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 16px;
    color: #1f2329;

    &::placeholder {
      color: #c9cdd4;
    }
  }

  .clear-icon {
    font-size: 20px;
    color: #8f959e;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;

    &:hover {
      background: #f5f6f7;
    }
  }
}

.search-tabs {
  display: flex;
  padding: 12px 20px;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;

  .tab-item {
    padding: 6px 16px;
    font-size: 14px;
    color: #646a73;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #f5f6f7;
    }

    &.active {
      background: #e8f4ff;
      color: #1677ff;
      font-weight: 500;
    }
  }
}

.search-content {
  max-height: 480px;
  overflow-y: auto;
  padding: 16px 20px;
}

.hot-section,
.history-section {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8f959e;
  margin-bottom: 12px;

  .material-icons-outlined {
    font-size: 16px;
  }

  .clear-history {
    margin-left: auto;
    cursor: pointer;
    color: #1677ff;

    &:hover {
      text-decoration: underline;
    }
  }
}

.hot-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .hot-keyword {
    padding: 6px 14px;
    background: #f5f6f7;
    border-radius: 14px;
    font-size: 13px;
    color: #1f2329;
    cursor: pointer;

    &:hover {
      background: #e8f4ff;
      color: #1677ff;
    }
  }
}

.history-list {
  .history-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    cursor: pointer;
    border-bottom: 1px solid #f5f6f7;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f5f6f7;
      margin: 0 -20px;
      padding: 10px 20px;
    }

    .history-icon {
      font-size: 18px;
      color: #c9cdd4;
      margin-right: 12px;
    }

    .history-text {
      font-size: 14px;
      color: #1f2329;
    }
  }
}

.empty-result {
  text-align: center;
  padding: 60px 0;

  .empty-icon {
    font-size: 48px;
    color: #c9cdd4;
  }

  p {
    margin-top: 16px;
    font-size: 14px;
    color: #8f959e;
  }
}

.result-group {
  margin-bottom: 20px;

  .group-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #1f2329;
    margin-bottom: 12px;

    .material-icons-outlined {
      font-size: 18px;
      color: #8f959e;
    }

    .count {
      font-size: 12px;
      color: #8f959e;
      font-weight: 400;
    }
  }
}

.result-list {
  .result-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;

    &:hover {
      background: #f5f6f7;
    }

    .file-icon {
      width: 40px;
      height: 40px;
      background: #e8f4ff;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      .material-icons-outlined {
        font-size: 22px;
        color: #1677ff;
      }
    }

    .item-content {
      flex: 1;
      margin-left: 12px;
      min-width: 0;

      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .item-title {
        font-size: 14px;
        font-weight: 500;
        color: #1f2329;

        :deep(.highlight) {
          color: #1677ff;
          background: #e8f4ff;
          padding: 0 2px;
          border-radius: 2px;
        }
      }

      .item-time {
        font-size: 12px;
        color: #8f959e;
      }

      .item-preview {
        font-size: 13px;
        color: #646a73;
        margin-top: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        :deep(.highlight) {
          color: #1677ff;
          background: #e8f4ff;
          padding: 0 2px;
          border-radius: 2px;
        }
      }

      .item-desc {
        font-size: 12px;
        color: #8f959e;
        margin-top: 4px;
      }
    }
  }

  .view-more {
    text-align: center;
    padding: 12px;
    font-size: 13px;
    color: #1677ff;
    cursor: pointer;

    &:hover {
      background: #f5f6f7;
    }
  }
}
</style>