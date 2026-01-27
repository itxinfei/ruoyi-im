<template>
  <el-dialog
    v-model="visible"
    title="全局搜索"
    width="700px"
    :close-on-click-modal="true"
    @close="handleClose"
    class="global-search-dialog"
  >
    <div class="search-container">
      <!-- 搜索输入框 -->
      <div class="search-input-wrapper">
        <el-input
          ref="searchInputRef"
          v-model="searchKeyword"
          placeholder="搜索消息、联系人、群组、文件..."
          size="large"
          clearable
          @input="handleSearchInput"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 搜索类型过滤 -->
      <div class="search-filters">
        <el-radio-group v-model="searchType" size="small" @change="handleSearch">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="MESSAGE">消息</el-radio-button>
          <el-radio-button label="CONTACT">联系人</el-radio-button>
          <el-radio-button label="GROUP">群组</el-radio-button>
          <el-radio-button label="FILE">文件</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 热门搜索 -->
      <div v-if="!searchResult && !searching" class="hot-keywords">
        <div class="section-title">热门搜索</div>
        <div class="keyword-list">
          <el-tag
            v-for="keyword in hotKeywords"
            :key="keyword"
            class="keyword-tag"
            @click="handleKeywordClick(keyword)"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchResult && !searching" class="search-results">
        <!-- 消息结果 -->
        <div v-if="searchResult.messages?.length > 0" class="result-section">
          <div class="section-header">
            <span class="section-title">消息</span>
            <span class="section-count">{{ searchResult.messages.length }}</span>
          </div>
          <div class="result-list">
            <div
              v-for="item in searchResult.messages.slice(0, 5)"
              :key="item.id"
              class="result-item message-item"
              @click="handleMessageClick(item)"
            >
              <div class="item-header">
                <span class="item-name">{{ item.senderName }}</span>
                <span class="item-time">{{ formatTime(item.timestamp) }}</span>
              </div>
              <div class="item-content">{{ item.content }}</div>
            </div>
          </div>
        </div>

        <!-- 联系人结果 -->
        <div v-if="searchResult.contacts?.length > 0" class="result-section">
          <div class="section-header">
            <span class="section-title">联系人</span>
            <span class="section-count">{{ searchResult.contacts.length }}</span>
          </div>
          <div class="result-list">
            <div
              v-for="item in searchResult.contacts.slice(0, 5)"
              :key="item.id"
              class="result-item contact-item"
              @click="handleContactClick(item)"
            >
              <el-avatar :size="36" :src="item.avatar">
                {{ item.name?.charAt(0) }}
              </el-avatar>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-remark">{{ item.remark || item.department || '' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 群组结果 -->
        <div v-if="searchResult.groups?.length > 0" class="result-section">
          <div class="section-header">
            <span class="section-title">群组</span>
            <span class="section-count">{{ searchResult.groups.length }}</span>
          </div>
          <div class="result-list">
            <div
              v-for="item in searchResult.groups.slice(0, 5)"
              :key="item.id"
              class="result-item group-item"
              @click="handleGroupClick(item)"
            >
              <el-avatar :size="36" :src="item.avatar">
                {{ item.name?.charAt(0) }}
              </el-avatar>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-remark">{{ item.memberCount || 0 }}人</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 文件结果 -->
        <div v-if="searchResult.files?.length > 0" class="result-section">
          <div class="section-header">
            <span class="section-title">文件</span>
            <span class="section-count">{{ searchResult.files.length }}</span>
          </div>
          <div class="result-list">
            <div
              v-for="item in searchResult.files.slice(0, 5)"
              :key="item.id"
              class="result-item file-item"
              @click="handleFileClick(item)"
            >
              <el-icon class="file-icon" :size="32"><Document /></el-icon>
              <div class="item-info">
                <div class="item-name">{{ item.fileName }}</div>
                <div class="item-remark">{{ formatFileSize(item.fileSize) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 无结果 -->
        <div v-if="!hasResults" class="no-results">
          <el-empty description="未找到相关结果" />
        </div>
      </div>

      <!-- 搜索中 -->
      <div v-if="searching" class="search-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>搜索中...</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { Search, Document, Loading } from '@element-plus/icons-vue'
import { globalSearch, getHotKeywords } from '@/api/im/globalSearch'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'select-message', 'select-contact', 'select-group', 'select-file'])

const visible = ref(false)
const searchKeyword = ref('')
const searchType = ref('ALL')
const searchResult = ref(null)
const searching = ref(false)
const hotKeywords = ref([])
const searchInputRef = ref(null)

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    open()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const hasResults = computed(() => {
  if (!searchResult.value) return false
  return (
    (searchResult.value.messages?.length > 0) ||
    (searchResult.value.contacts?.length > 0) ||
    (searchResult.value.groups?.length > 0) ||
    (searchResult.value.files?.length > 0)
  )
})

const open = async () => {
  visible.value = true
  // 加载热门搜索
  try {
    const res = await getHotKeywords()
    hotKeywords.value = res.data || []
  } catch (e) {
    hotKeywords.value = []
  }
  await nextTick()
  searchInputRef.value?.focus()
}

const handleClose = () => {
  visible.value = false
  searchKeyword.value = ''
  searchResult.value = null
}

const handleSearchInput = () => {
  if (searchKeyword.value.trim() === '') {
    searchResult.value = null
  }
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  searching.value = true
  try {
    const res = await globalSearch({
      keyword,
      searchType: searchType.value,
      pageNum: 1,
      pageSize: 20
    })
    searchResult.value = res.data || {}
  } catch (e) {
    ElMessage.error('搜索失败')
    console.error(e)
  } finally {
    searching.value = false
  }
}

const handleKeywordClick = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

const handleMessageClick = (item) => {
  emit('select-message', item)
  handleClose()
}

const handleContactClick = (item) => {
  emit('select-contact', item)
  handleClose()
}

const handleGroupClick = (item) => {
  emit('select-group', item)
  handleClose()
}

const handleFileClick = (item) => {
  emit('select-file', item)
  handleClose()
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.global-search-dialog {
  .search-container {
    .search-input-wrapper {
      margin-bottom: 16px;
    }

    .search-filters {
      margin-bottom: 20px;
      display: flex;
      justify-content: center;
    }

    .hot-keywords {
      .section-title {
        font-size: 13px;
        color: #8f959e;
        margin-bottom: 12px;
      }

      .keyword-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .keyword-tag {
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: var(--el-color-primary);
            color: #fff;
          }
        }
      }
    }

    .search-results {
      max-height: 400px;
      overflow-y: auto;

      .result-section {
        margin-bottom: 20px;

        .section-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          padding-bottom: 8px;
          border-bottom: 1px solid #f0f0f0;

          .section-title {
            font-size: 14px;
            font-weight: 600;
            color: #1f2329;
          }

          .section-count {
            font-size: 12px;
            color: #8f959e;
            background: #f2f3f5;
            padding: 2px 8px;
            border-radius: 10px;
          }
        }

        .result-list {
          .result-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
              background: #f5f5f5;
            }

            &.message-item {
              flex-direction: column;
              align-items: flex-start;

              .item-header {
                display: flex;
                justify-content: space-between;
                width: 100%;
                margin-bottom: 4px;

                .item-name {
                  font-weight: 500;
                  color: #1f2329;
                }

                .item-time {
                  font-size: 12px;
                  color: #8f959e;
                }
              }

              .item-content {
                font-size: 13px;
                color: #646a73;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                max-width: 100%;
              }
            }

            &.contact-item,
            &.group-item {
              .item-info {
                flex: 1;

                .item-name {
                  font-weight: 500;
                  color: #1f2329;
                }

                .item-remark {
                  font-size: 12px;
                  color: #8f959e;
                  margin-top: 2px;
                }
              }
            }

            &.file-item {
              .file-icon {
                color: #1677ff;
                flex-shrink: 0;
              }

              .item-info {
                flex: 1;

                .item-name {
                  font-weight: 500;
                  color: #1f2329;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }

                .item-remark {
                  font-size: 12px;
                  color: #8f959e;
                  margin-top: 2px;
                }
              }
            }
          }
        }
      }

      .no-results {
        padding: 40px 0;
      }
    }

    .search-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 0;
      color: #8f959e;

      .el-icon {
        font-size: 32px;
        margin-bottom: 12px;
      }
    }
  }
}

.dark {
  .global-search-dialog {
    .search-results {
      .result-section {
        .section-header {
          border-bottom-color: #334155;

          .section-title {
            color: #f1f5f9;
          }

          .section-count {
            background: #334155;
            color: #94a3b8;
          }
        }

        .result-item {
          &:hover {
            background: #334155;
          }

          &.message-item {
            .item-name {
              color: #f1f5f9;
            }

            .item-content {
              color: #cbd5e1;
            }
          }

          &.contact-item,
          &.group-item {
            .item-name {
              color: #f1f5f9;
            }
          }

          &.file-item {
            .item-name {
              color: #f1f5f9;
            }
          }
        }
      }
    }
  }
}
</style>
