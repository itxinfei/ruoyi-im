<template>
  <div class="search-panel">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-input-wrapper">
        <span class="material-icons-outlined search-icon">search</span>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索消息、联系人、群组、文件..."
          autofocus
          @keyup.enter="handleSearch"
        >
        <span v-if="keyword" class="material-icons-outlined clear-icon" @click="clearSearch">close</span>
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
      <!-- 热门搜索 -->
      <div v-if="!keyword && !searched" class="hot-searches">
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

      <!-- 搜索结果列表 -->
      <div v-else-if="searched" class="search-results">
        <template v-if="searchType === 'all'">
          <!-- 消息结果 -->
          <div v-if="results.messages?.length" class="result-section">
            <div class="section-title">
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
                <DingtalkAvatar :src="msg.senderAvatar" :name="msg.senderName" :size="36" />
                <div class="item-content">
                  <div class="item-title">
                    {{ msg.senderName }}
                  </div>
                  <div class="item-desc">
                    {{ msg.content }}
                  </div>
                  <div class="item-time">
                    {{ formatTime(msg.createTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 联系人结果 -->
          <div v-if="results.contacts?.length" class="result-section">
            <div class="section-title">
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
              <span class="material-icons-outlined">group</span>
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
                  <span class="material-icons-outlined">group</span>
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
              <span class="material-icons-outlined">insert_drive_file</span>
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
                  <span class="material-icons-outlined">description</span>
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
            <span class="material-icons-outlined">search_off</span>
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
                  <div class="item-desc">
                    {{ item.content }}
                  </div>
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
                  <span class="material-icons-outlined">group</span>
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
                  <span class="material-icons-outlined">description</span>
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
            <span class="material-icons-outlined">search_off</span>
            <p>未找到相关结果</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { globalSearch, getHotKeywords } from '@/api/im/search'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['show-user', 'go-to-session', 'go-to-group'])

const keyword = ref('')
const searchType = ref('all')
const loading = ref(false)
const searched = ref(false)
const hotKeywords = ref([])
const results = ref({})

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
  }
}

const handleSearch = async () => {
  if (!keyword.value.trim()) return

  loading.value = true
  searched.value = true

  try {
    const res = await globalSearch({
      keyword: keyword.value.trim(),
      searchType: searchType.value
    })
    if (res.code === 200) {
      results.value = res.data || {}
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

const goToContact = (contact) => {
  emit('show-user', contact.userId)
}

const goToGroup = (group) => {
  emit('go-to-group', group.groupId)
}

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
  padding: var(--dt-spacing-md, 16px);
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  padding: var(--dt-spacing-xs, 8px) var(--dt-spacing-sm, 12px);

  .search-icon {
    color: var(--dt-text-tertiary);
    margin-right: var(--dt-spacing-xs, 8px);
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
    font-size: var(--dt-icon-size-lg, 18px);

    &:hover {
      color: var(--dt-text-secondary);
    }
  }
}

.search-type-tabs {
  display: flex;
  gap: var(--dt-spacing-xs, 8px);
  margin-top: var(--dt-spacing-sm, 12px);

  .type-tab {
    padding: var(--dt-spacing-xs, 6px) var(--dt-spacing-sm, 12px);
    border-radius: var(--dt-radius-full, 16px);
    font-size: var(--dt-font-size-xs);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);

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

.search-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-md, 16px);
}

.hot-searches {
  .section-title {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
    margin-bottom: var(--dt-spacing-sm, 12px);
  }

  .hot-keyword-list {
    display: flex;
    flex-wrap: wrap;
    gap: var(--dt-spacing-xs, 8px);

    .hot-keyword {
      padding: var(--dt-spacing-xs, 6px) var(--dt-spacing-md, 16px);
      background: var(--dt-bg-body);
      border-radius: var(--dt-radius-full, 16px);
      font-size: var(--dt-font-size-xs);
      cursor: pointer;
      transition: all var(--dt-transition-fast);

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
  margin-bottom: var(--dt-spacing-lg, 24px);

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs, 6px);
    font-size: var(--dt-font-size-sm);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
    margin-bottom: var(--dt-spacing-sm, 12px);

    .material-icons-outlined {
      font-size: var(--dt-icon-size-lg, 18px);
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
    padding: var(--dt-spacing-sm, 12px);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: background var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-body);
    }

    .item-content {
      flex: 1;
      margin-left: var(--dt-spacing-sm, 12px);
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
        margin-top: var(--dt-spacing-xs, 4px);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-time {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        margin-top: var(--dt-spacing-xs, 4px);
      }
    }

    .group-avatar,
    .file-icon {
      width: var(--dt-avatar-size-md, 36px);
      height: var(--dt-avatar-size-md, 36px);
      border-radius: var(--dt-radius-md);
      background: var(--dt-brand-bg);
      display: flex;
      align-items: center;
      justify-content: center;

      .material-icons-outlined {
        color: var(--dt-brand-color);
      }
    }
  }
}

.empty-results {
  text-align: center;
  padding: var(--dt-spacing-2xl, 60px) 0;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: var(--dt-icon-size-xl, 48px);
    opacity: 0.5;
  }

  p {
    margin-top: var(--dt-spacing-sm, 12px);
    font-size: var(--dt-font-size-sm);
  }
}
</style>
