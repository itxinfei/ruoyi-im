<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="visible"
        class="search-overlay"
        @click="visible = false"
      >
        <div
          class="search-modal"
          @click.stop
        >
          <div class="modal-head">
            <div class="head-title-wrap">
              <span class="material-icons-outlined">manage_search</span>
              <h3>全局搜索</h3>
              <span class="shortcut-hint">Ctrl/Cmd + K</span>
            </div>
            <button
              class="close-btn"
              @click="visible = false"
            >
              <el-icon>
                <CircleClose />
              </el-icon>
            </button>
          </div>

          <!-- 搜索输入框 -->
          <div class="search-header">
            <div class="search-input-wrapper">
              <el-icon class="search-icon">
                <Search />
              </el-icon>
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
              <button
                v-if="searchKeyword"
                class="clear-btn"
                @click="clearSearch"
              >
                <el-icon>
                  <CircleClose />
                </el-icon>
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
                <span class="tab-label">{{ cat.label }}</span>
              </button>
            </div>
          </div>

          <!-- 搜索结果 -->
          <div class="search-body">
            <!-- 加载中 -->
            <div
              v-if="searching"
              class="loading"
            >
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>搜索中...</span>
            </div>

            <!-- 结果列表 -->
            <div
              v-else-if="hasResults"
              class="results"
            >
              <div
                v-for="(section, type) in filteredResults"
                :key="type"
                class="result-section"
              >
                <div class="section-title">
                  <div class="section-title-left">
                    <span class="material-icons-outlined">{{ sectionIconMap[type] || 'category' }}</span>
                    <span>{{ section.title }}</span>
                  </div>
                  <span class="section-count">{{ section.data.length }}</span>
                </div>
                <div
                  v-for="item in section.data"
                  :key="item.id || item.userId || item.groupId || item.fileId"
                  class="result-item"
                  @click="handleResultClick(type, item)"
                >
                  <!-- 头像 (仅联系人和群组显示) -->
                  <el-avatar
                    v-if="type === 'contacts' || type === 'groups'"
                    :size="40"
                    :src="item.avatar"
                    class="item-avatar"
                  >
                    {{ getItemTitle(item, type).charAt(0) }}
                  </el-avatar>

                  <div class="item-content">
                    <div class="item-title-row">
                      <div class="item-title">
                        {{ getItemTitle(item, type) }}
                      </div>
                      <span
                        v-if="type === 'files'"
                        class="item-meta"
                      >
                        {{ formatFileSize(item.fileSize) }}
                      </span>
                    </div>
                    <div class="item-desc">
                      {{ getItemDesc(item, type) }}
                    </div>
                    <div
                      v-if="type === 'messages'"
                      class="item-subdesc"
                    >
                      {{ item.content || '' }}
                    </div>
                  </div>

                  <!-- 右侧箭头 -->
                  <el-icon class="item-arrow">
                    <ArrowRight />
                  </el-icon>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div
              v-else-if="searchKeyword"
              class="empty"
            >
              <el-icon>
                <Search />
              </el-icon>
              <p>
                未找到相关结果
              </p>
              <p class="empty-hint">
                试试在通讯录中搜索，或检查关键词
              </p>
            </div>

            <!-- 默认状态 -->
            <div
              v-else
              class="default"
            >
              <el-icon>
                <Search />
              </el-icon>
              <p>
                输入关键词开始搜索
              </p>
              <p class="empty-hint">
                支持联系人、群组、消息内容、文件名检索
              </p>
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
import { Search, CircleClose, Loading, ArrowRight } from '@element-plus/icons-vue'
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

const sectionIconMap = {
  contacts: 'person',
  groups: 'groups',
  messages: 'chat',
  files: 'description'
}

// 计算属性
const hasResults = computed(() => {
  if (!searchResult.value) {return false}
  return (searchResult.value.contacts?.length || 0) +
    (searchResult.value.groups?.length || 0) +
    (searchResult.value.messages?.length || 0) +
    (searchResult.value.files?.length || 0) > 0
})

const filteredResults = computed(() => {
  if (!searchResult.value) {return {}}

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
    contacts: item.nickname || item.userName || '未知用户',
    groups: item.groupName || '未知群组',
    messages: item.senderName || '未知用户',
    files: item.fileName || '未命名文件'
  }
  return titleMap[type] || ''
}

const getItemDesc = (item, type) => {
  const descMap = {
    contacts: item.department || item.position || item.mobile || '',
    groups: `共 ${item.memberCount || 0} 人`,
    messages: item.sessionName || item.conversationName || item.senderName || '',
    files: item.uploaderName || item.uploader || item.fileType || ''
  }
  return descMap[type] || ''
}

const formatFileSize = size => {
  if (!size || Number(size) <= 0) { return '' }
  const units = ['B', 'KB', 'MB', 'GB']
  let value = Number(size)
  let index = 0
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024
    index++
  }
  return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`
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
  if (!keyword) {return}

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
      // 调试日志
      console.log('搜索结果:', res.data)
      console.log('联系人数量:', res.data.contacts?.length || 0)
      console.log('群组数量:', res.data.groups?.length || 0)
      console.log('消息数量:', res.data.messages?.length || 0)
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
  background: var(--dt-overlay-50);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.search-modal {
  width: min(860px, 96vw);
  height: min(680px, 92vh);
  background: var(--dt-bg-card, #ffffff);
  border-radius: 16px;
  box-shadow: var(--dt-shadow-2xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px 10px;
  border-bottom: 1px solid #edf0f5;
  background: linear-gradient(135deg, #f8fbff 0%, #f4f8ff 100%);
}

.head-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;

  .material-icons-outlined {
    font-size: 20px;
    color: #2563eb;
  }

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
  }
}

.shortcut-hint {
  font-size: 11px;
  color: #64748b;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 2px 8px;
}

.close-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;

  &:hover {
    background: #e2e8f0;
    color: #0f172a;
  }
}

.search-header {
  padding: 14px 18px;
  border-bottom: 1px solid #e5e7eb;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 12px;

  &:focus-within {
    border-color: var(--dt-brand-color, #3b82f6);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
  }
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
  padding: 7px 12px;
  border: none;
  background: #f1f5f9;
  color: #6b7280;
  font-size: 13px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #e5e7eb;
  }

  &.active {
    background: #2563eb;
    color: #fff;
  }
}

.search-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px 18px;
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

  .empty-hint {
    margin-top: 8px;
    font-size: 12px;
    color: var(--dt-text-quaternary, #bbb);
  }
}

.results {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.result-section {
  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    font-weight: 600;
    color: #475569;
    margin-bottom: 8px;
  }
}

.section-title-left {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  .material-icons-outlined {
    font-size: 16px;
    color: #64748b;
  }
}

.section-count {
  background: #e2e8f0;
  color: #334155;
  border-radius: 999px;
  padding: 1px 8px;
  font-size: 11px;
}

.result-item {
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;

  &:hover {
    background: #f1f5f9;
    border-color: #cbd5e1;
    transform: translateX(2px);
  }
}

.item-content {
  flex: 1;
  min-width: 0;

  .item-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .item-title {
    font-size: 14px;
    font-weight: 500;
    color: #111827;
    margin-bottom: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .item-desc {
    font-size: 12px;
    color: #1f2937;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .item-subdesc {
    font-size: 12px;
    color: #6b7280;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.item-meta {
  font-size: 11px;
  color: #64748b;
  background: #e2e8f0;
  border-radius: 6px;
  padding: 1px 6px;
}

.item-arrow {
  color: #94a3b8;
}

@media (max-width: 768px) {
  .search-overlay {
    padding: 10px;
  }

  .search-modal {
    width: 100%;
    height: 100%;
    border-radius: 12px;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }
}
</style>
