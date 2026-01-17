<template>
  <div v-if="visible" class="global-search-container" @click.self="handleClose">
    <div class="search-dialog" :class="{ 'has-results': searchResults.length > 0 || searching }">
      <!-- 搜索输入框 -->
      <div class="search-header">
        <div class="search-input-wrapper">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            ref="searchInputRef"
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索消息、联系人、群组、文件..."
            @input="handleSearchInput"
            @keydown="handleKeydown"
          />
          <el-icon v-if="searchKeyword" class="clear-icon" @click="clearSearch">
            <CircleClose />
          </el-icon>
        </div>
        <div class="search-actions">
          <span class="search-shortcut">ESC</span>
          <el-button text @click="handleClose">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 搜索类型筛选 -->
      <div v-if="searchKeyword" class="search-filters">
        <div
          v-for="filter in searchFilters"
          :key="filter.key"
          class="filter-item"
          :class="{ active: activeFilter === filter.key }"
          @click="setActiveFilter(filter.key)"
        >
          <el-icon>
            <component :is="filter.icon" />
          </el-icon>
          <span>{{ filter.label }}</span>
          <span v-if="filter.count > 0" class="filter-count">{{ filter.count }}</span>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-loading="searching" class="search-results">
        <!-- 空状态 -->
        <div v-if="!searchKeyword && !searching" class="search-empty">
          <div class="empty-icon">
            <el-icon :size="64"><Search /></el-icon>
          </div>
          <div class="empty-text">
            <p>输入关键词搜索</p>
            <p class="empty-hint">支持搜索消息、联系人、群组、文件</p>
          </div>
          <div class="search-tips">
            <div class="tip-item">
              <kbd>Ctrl</kbd> + <kbd>K</kbd>
              <span>快速打开搜索</span>
            </div>
            <div class="tip-item">
              <kbd>↑</kbd> <kbd>↓</kbd>
              <span>选择结果</span>
            </div>
            <div class="tip-item">
              <kbd>Enter</kbd>
              <span>打开选中项</span>
            </div>
          </div>
        </div>

        <!-- 无搜索结果 -->
        <div v-else-if="!searching && searchResults.length === 0" class="no-results">
          <el-icon :size="48" class="no-result-icon"><DocumentDelete /></el-icon>
          <p>未找到相关结果</p>
          <p class="no-result-hint">试试其他关键词</p>
        </div>

        <!-- 搜索结果列表 -->
        <div v-else-if="searchResults.length > 0" class="results-list">
          <div v-for="(group, category) in groupedResults" :key="category" class="result-group">
            <div class="group-header">
              <el-icon class="category-icon">
                <component :is="getCategoryIcon(category)" />
              </el-icon>
              <span class="category-name">{{ getCategoryName(category) }}</span>
              <span class="category-count">{{ group.length }}</span>
            </div>
            <div
              v-for="(item, index) in group"
              :key="item.id"
              class="result-item"
              :class="{
                selected: selectedIndex === getGlobalIndex(group, category, index),
                unread: item.unread,
              }"
              @click="handleResultClick(item)"
              @mouseenter="selectedIndex = getGlobalIndex(group, category, index)"
            >
              <!-- 消息类型结果 -->
              <div v-if="item.type === 'message'" class="result-content message-result">
                <el-avatar :size="40" :src="item.avatar">
                  {{ item.senderName?.charAt(0) }}
                </el-avatar>
                <div class="result-info">
                  <div class="result-title">
                    <span class="sender-name">{{ item.senderName }}</span>
                    <span class="message-time">{{ formatTime(item.timestamp) }}</span>
                  </div>
                  <div class="result-desc">
                    <span class="session-name">{{ item.sessionName }} · </span>
                    <span v-html="highlightKeyword(item.content, searchKeyword)"></span>
                  </div>
                </div>
              </div>

              <!-- 联系人类型结果 -->
              <div v-else-if="item.type === 'contact'" class="result-content contact-result">
                <el-avatar :size="40" :src="item.avatar">
                  {{ item.name?.charAt(0) }}
                </el-avatar>
                <div class="result-info">
                  <div class="result-title">
                    <span v-html="highlightKeyword(item.name, searchKeyword)"></span>
                  </div>
                  <div class="result-desc">
                    <span>{{ item.deptName || item.position || '联系人' }}</span>
                    <span v-if="item.mobile" class="contact-detail">{{ item.mobile }}</span>
                  </div>
                </div>
                <div class="result-action">
                  <el-button size="small" type="primary" @click.stop="handleStartChat(item)">
                    发消息
                  </el-button>
                </div>
              </div>

              <!-- 群组类型结果 -->
              <div v-else-if="item.type === 'group'" class="result-content group-result">
                <div class="group-avatar">
                  <el-icon :size="24"><ChatDotRound /></el-icon>
                </div>
                <div class="result-info">
                  <div class="result-title">
                    <span v-html="highlightKeyword(item.groupName, searchKeyword)"></span>
                  </div>
                  <div class="result-desc">
                    <span>{{ item.memberCount || 0 }}人</span>
                    <span v-if="item.description" class="group-desc">{{ item.description }}</span>
                  </div>
                </div>
                <div class="result-action">
                  <el-button size="small" type="primary" @click.stop="handleStartChat(item)">
                    进入群聊
                  </el-button>
                </div>
              </div>

              <!-- 文件类型结果 -->
              <div v-else-if="item.type === 'file'" class="result-content file-result">
                <div class="file-icon" :class="`file-${getFileExtension(item.fileName)}`">
                  <el-icon :size="28"><Document /></el-icon>
                </div>
                <div class="result-info">
                  <div class="result-title">
                    <span v-html="highlightKeyword(item.fileName, searchKeyword)"></span>
                  </div>
                  <div class="result-desc">
                    <span>{{ formatFileSize(item.fileSize) }}</span>
                    <span>{{ item.sessionName }}</span>
                    <span>{{ formatTime(item.uploadTime) }}</span>
                  </div>
                </div>
                <div class="result-action">
                  <el-button size="small" @click.stop="handleDownloadFile(item)">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div v-if="searchResults.length > 0" class="search-footer">
        <span class="result-summary">共 {{ totalResults }} 条结果</span>
        <div class="footer-tips">
          <span class="tip-key"><kbd>↑</kbd> <kbd>↓</kbd></span> 导航
          <span class="tip-key"><kbd>Enter</kbd></span> 打开
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Search,
  Close,
  CircleClose,
  ChatDotRound,
  User,
  UserFilled,
  Document,
  DocumentDelete,
  Download,
} from '@element-plus/icons-vue'
import * as messageApi from '@/api/im/message'
import * as contactApi from '@/api/im/contact'
import * as conversationApi from '@/api/im/conversation'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'resultClick'])

const router = useRouter()
const store = useStore()

// 搜索输入框引用
const searchInputRef = ref(null)

// 搜索状态
const searchKeyword = ref('')
const searching = ref(false)
const searchResults = ref([])
const selectedIndex = ref(-1)
const activeFilter = ref('all')

// 搜索类型筛选
const searchFilters = ref([
  { key: 'all', label: '全部', icon: Search, count: 0 },
  { key: 'message', label: '消息', icon: ChatDotRound, count: 0 },
  { key: 'contact', label: '联系人', icon: User, count: 0 },
  { key: 'group', label: '群组', icon: UserFilled, count: 0 },
  { key: 'file', label: '文件', icon: Document, count: 0 },
])

// 计算属性：分组结果
const groupedResults = computed(() => {
  const groups = {}
  searchResults.value.forEach(item => {
    const category = item.type
    if (!groups[category]) {
      groups[category] = []
    }
    groups[category].push(item)
  })
  return groups
})

// 总结果数
const totalResults = computed(() => searchResults.value.length)

// 搜索输入防抖
let searchDebounceTimer = null

const handleSearchInput = () => {
  clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    performSearch()
  }, 300)
}

// 执行搜索
const performSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    updateFilterCounts()
    return
  }

  searching.value = true
  selectedIndex.value = -1

  try {
    // 根据筛选类型执行搜索
    let results = []

    if (activeFilter.value === 'all' || activeFilter.value === 'message') {
      const messageResults = await searchMessages(keyword)
      results = [...results, ...messageResults]
    }

    if (activeFilter.value === 'all' || activeFilter.value === 'contact') {
      const contactResults = await searchContacts(keyword)
      results = [...results, ...contactResults]
    }

    if (activeFilter.value === 'all' || activeFilter.value === 'group') {
      const groupResults = await searchGroups(keyword)
      results = [...results, ...groupResults]
    }

    if (activeFilter.value === 'all' || activeFilter.value === 'file') {
      const fileResults = await searchFiles(keyword)
      results = [...results, ...fileResults]
    }

    searchResults.value = results.slice(0, 50) // 限制最多50条结果
    updateFilterCounts()
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

// 搜索消息
const searchMessages = async keyword => {
  try {
    const results = []

    // 1. 调用后端API搜索所有历史消息
    try {
      const response = await messageApi.searchMessages({
        keyword,
        messageType: 'TEXT',
        pageNum: 1,
        pageSize: 20,
      })

      if (response.code === 200 && response.data?.items) {
        // 获取会话列表用于匹配会话名称
        const sessions = store.state.im.sessions || []
        const sessionMap = new Map()
        sessions.forEach(s => {
          sessionMap.set(String(s.sessionId), s)
        })

        response.data.items.forEach(item => {
          const session = sessionMap.get(String(item.conversationId))
          results.push({
            id: `msg-${item.id}`,
            type: 'message',
            content: item.content || item.highlightSnippet || '',
            senderName: item.senderName || '未知',
            senderId: item.senderId,
            sessionName: session?.name || '会话',
            sessionId: item.conversationId,
            sessionType: session?.type || 'PRIVATE',
            timestamp: item.sendTime,
            avatar: item.senderAvatar,
          })
        })
      }
    } catch (apiError) {
      console.warn('后端搜索失败，使用本地搜索:', apiError)
    }

    // 2. 如果后端搜索失败或结果较少，补充本地缓存搜索
    if (results.length < 10) {
      const messageList = store.state.im.messageList || {}
      const sessions = store.state.im.sessions || []
      const existingIds = new Set(results.map(r => r.id))

      sessions.forEach(session => {
        const messages = messageList[session.sessionId] || []
        messages.forEach(msg => {
          const msgId = `msg-${msg.id}`
          if (
            !existingIds.has(msgId) &&
            (msg.messageType === 'TEXT' || msg.type === 'text') &&
            msg.content?.toLowerCase().includes(keyword.toLowerCase())
          ) {
            results.push({
              id: msgId,
              type: 'message',
              content: msg.content,
              senderName: msg.senderName || '未知',
              senderId: msg.senderId,
              sessionName: session.name,
              sessionId: session.sessionId,
              sessionType: session.type,
              timestamp: msg.sendTime || msg.timestamp,
              avatar: msg.senderAvatar,
            })
          }
        })
      })
    }

    return results
  } catch (error) {
    console.error('搜索消息失败:', error)
    return []
  }
}

// 搜索联系人
const searchContacts = async keyword => {
  try {
    const results = []

    // 1. 调用后端API搜索联系人
    try {
      const response = await contactApi.searchContacts(keyword)
      if (response.code === 200 && response.data) {
        const contacts = Array.isArray(response.data) ? response.data : []
        contacts.forEach(contact => {
          results.push({
            id: `contact-${contact.userId}`,
            type: 'contact',
            name: contact.name || contact.nickname,
            userId: contact.userId,
            avatar: contact.avatar,
            mobile: contact.mobile,
            email: contact.email,
            deptName: contact.deptName,
            position: contact.position,
            unread: contact.unreadCount > 0,
          })
        })
      }
    } catch (apiError) {
      console.warn('后端搜索联系人失败，使用本地搜索:', apiError)
    }

    // 2. 如果后端搜索失败或结果较少，补充本地搜索
    if (results.length < 5) {
      const contacts = store.state.im.contacts || []
      const existingIds = new Set(results.map(r => r.id))

      contacts.forEach(contact => {
        const contactId = `contact-${contact.userId}`
        if (!existingIds.has(contactId)) {
          const searchFields = [
            contact.name,
            contact.nickname,
            contact.remark,
            contact.mobile,
            contact.email,
          ].filter(Boolean)

          if (searchFields.some(field => field.toLowerCase().includes(keyword.toLowerCase()))) {
            results.push({
              id: contactId,
              type: 'contact',
              name: contact.name || contact.nickname,
              userId: contact.userId,
              avatar: contact.avatar,
              mobile: contact.mobile,
              email: contact.email,
              deptName: contact.deptName,
              position: contact.position,
              unread: contact.unreadCount > 0,
            })
          }
        }
      })
    }

    return results
  } catch (error) {
    console.error('搜索联系人失败:', error)
    return []
  }
}

// 搜索群组
const searchGroups = async keyword => {
  try {
    const results = []

    // 1. 调用后端API搜索群组
    try {
      const response = await conversationApi.searchConversation(keyword)
      if (response.code === 200 && response.data) {
        const conversations = Array.isArray(response.data) ? response.data : []
        conversations.forEach(conv => {
          if (conv.type === 'GROUP') {
            results.push({
              id: `group-${conv.id}`,
              type: 'group',
              groupName: conv.name,
              sessionId: conv.id,
              avatar: conv.avatar,
              memberCount: conv.memberCount || 0,
              description: conv.description,
              unread: conv.unreadCount > 0,
            })
          }
        })
      }
    } catch (apiError) {
      console.warn('后端搜索群组失败，使用本地搜索:', apiError)
    }

    // 2. 如果后端搜索失败或结果较少，补充本地搜索
    if (results.length < 5) {
      const sessions = store.state.im.sessions || []
      const existingIds = new Set(results.map(r => r.id))

      sessions.forEach(session => {
        const groupId = `group-${session.sessionId}`
        if (
          !existingIds.has(groupId) &&
          session.type === 'GROUP' &&
          session.name?.toLowerCase().includes(keyword.toLowerCase())
        ) {
          results.push({
            id: groupId,
            type: 'group',
            groupName: session.name,
            sessionId: session.sessionId,
            avatar: session.avatar,
            memberCount: session.memberCount || 0,
            description: session.description,
            unread: session.unreadCount > 0,
          })
        }
      })
    }

    return results
  } catch (error) {
    console.error('搜索群组失败:', error)
    return []
  }
}

// 搜索文件
const searchFiles = async keyword => {
  try {
    const files = store.state.im.files || []

    return files
      .filter(file => {
        return file.fileName?.toLowerCase().includes(keyword.toLowerCase())
      })
      .map(file => ({
        id: `file-${file.id}`,
        type: 'file',
        fileName: file.fileName,
        fileSize: file.fileSize,
        fileUrl: file.fileUrl,
        uploadTime: file.uploadTime,
        sessionId: file.sessionId,
        sessionName: file.sessionName,
      }))
  } catch (error) {
    console.error('搜索文件失败:', error)
    return []
  }
}

// 更新筛选计数
const updateFilterCounts = () => {
  const counts = {
    all: searchResults.value.length,
    message: 0,
    contact: 0,
    group: 0,
    file: 0,
  }

  searchResults.value.forEach(item => {
    if (counts[item.type] !== undefined) {
      counts[item.type]++
    }
  })

  searchFilters.value.forEach(filter => {
    filter.count = counts[filter.key] || 0
  })
}

// 设置激活筛选
const setActiveFilter = filterKey => {
  activeFilter.value = filterKey
  performSearch()
}

// 高亮关键词
const highlightKeyword = (text, keyword) => {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${escapeRegex(keyword)})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

const escapeRegex = str => {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

// 获取分类图标
const getCategoryIcon = category => {
  const icons = {
    message: ChatDotRound,
    contact: User,
    group: UserFilled,
    file: Document,
  }
  return icons[category] || Search
}

// 获取分类名称
const getCategoryName = category => {
  const names = {
    message: '消息',
    contact: '联系人',
    group: '群聊',
    file: '文件',
  }
  return names[category] || category
}

// 获取全局索引（用于键盘导航）
const getGlobalIndex = (group, category, index) => {
  let globalIndex = 0
  for (const [cat, items] of Object.entries(groupedResults.value)) {
    if (cat === category) {
      return globalIndex + index
    }
    globalIndex += items.length
  }
  return globalIndex
}

// 获取文件扩展名
const getFileExtension = fileName => {
  if (!fileName) return 'unknown'
  const ext = fileName.split('.').pop()?.toLowerCase()
  return ext || 'unknown'
}

// 格式化文件大小
const formatFileSize = bytes => {
  if (!bytes) return '未知'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

// 格式化时间
const formatTime = timestamp => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

// 键盘事件处理
const handleKeydown = e => {
  const total = totalResults.value

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      selectedIndex.value = selectedIndex.value < total - 1 ? selectedIndex.value + 1 : 0
      break
    case 'ArrowUp':
      e.preventDefault()
      selectedIndex.value = selectedIndex.value > 0 ? selectedIndex.value - 1 : total - 1
      break
    case 'Enter':
      e.preventDefault()
      if (selectedIndex.value >= 0 && selectedIndex.value < total) {
        const flatResults = getFlatResults()
        const selectedItem = flatResults[selectedIndex.value]
        if (selectedItem) {
          handleResultClick(selectedItem)
        }
      }
      break
    case 'Escape':
      e.preventDefault()
      handleClose()
      break
  }
}

// 获取扁平化的结果列表
const getFlatResults = () => {
  const flat = []
  for (const items of Object.values(groupedResults.value)) {
    flat.push(...items)
  }
  return flat
}

// 处理结果点击
const handleResultClick = item => {
  emit('resultClick', item)
  handleClose()

  // 根据类型执行相应操作
  if (item.type === 'message') {
    // 跳转到会话并定位消息
    store.dispatch('im/switchSession', { sessionId: item.sessionId })
    // TODO: 滚动到指定消息
  } else if (item.type === 'contact') {
    // 打开与联系人的会话
    store.dispatch('im/startChat', { userId: item.userId })
  } else if (item.type === 'group') {
    // 跳转到群聊
    store.dispatch('im/switchSession', { sessionId: item.sessionId })
  } else if (item.type === 'file') {
    // 预览或下载文件
    if (item.fileUrl) {
      window.open(item.fileUrl, '_blank')
    }
  }
}

// 发起聊天
const handleStartChat = item => {
  if (item.type === 'contact') {
    store.dispatch('im/startChat', { userId: item.userId })
  } else if (item.type === 'group') {
    store.dispatch('im/switchSession', { sessionId: item.sessionId })
  }
  handleClose()
}

// 下载文件
const handleDownloadFile = item => {
  if (item.fileUrl) {
    const link = document.createElement('a')
    link.href = item.fileUrl
    link.download = item.fileName
    link.click()
  }
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  selectedIndex.value = -1
  updateFilterCounts()
  nextTick(() => {
    searchInputRef.value?.focus()
  })
}

// 关闭搜索
const handleClose = () => {
  emit('close')
}

// 监听显示状态变化
watch(
  () => props.visible,
  visible => {
    if (visible) {
      nextTick(() => {
        searchInputRef.value?.focus()
      })
    } else {
      clearSearch()
    }
  }
)

// 全局快捷键监听
const handleGlobalKeydown = e => {
  // Ctrl+K 打开搜索
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    if (!props.visible) {
      emit('update:visible', true)
    } else {
      searchInputRef.value?.focus()
    }
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
  clearTimeout(searchDebounceTimer)
})
</script>

<style lang="scss" scoped>
.global-search-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 15vh;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.search-dialog {
  width: 640px;
  max-width: 90vw;
  background: #fff;
  border-radius: 12px;
  box-shadow:
    0 12px 48px 16px rgba(0, 0, 0, 0.12),
    0 8px 16px -8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  animation: slideDown 0.3s ease;

  &.has-results {
    .search-results {
      max-height: 480px;
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.search-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e6eb;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 0 12px;
  height: 44px;

  &:focus-within {
    background: #fff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  }
}

.search-icon {
  font-size: 18px;
  color: #8b95a1;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #1d2129;
  outline: none;

  &::placeholder {
    color: #8b95a1;
  }
}

.clear-icon {
  font-size: 16px;
  color: #8b95a1;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #4e5969;
  }
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 16px;
}

.search-shortcut {
  font-size: 12px;
  color: #8b95a1;
  background: #f3f4f6;
  padding: 4px 8px;
  border-radius: 4px;
}

.search-filters {
  display: flex;
  gap: 4px;
  padding: 12px 20px;
  border-bottom: 1px solid #e5e6eb;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #4e5969;
  transition: all 0.2s;

  &:hover {
    background: #f3f4f6;
  }

  &.active {
    background: #e6f7ff;
    color: #1890ff;
  }

  .filter-count {
    background: rgba(0, 0, 0, 0.06);
    padding: 0 6px;
    border-radius: 10px;
    font-size: 11px;
  }
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
  }
}

.search-empty,
.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
  text-align: center;
}

.empty-icon {
  color: #c9cdd4;
  margin-bottom: 16px;
}

.empty-text {
  p {
    margin: 0;
    font-size: 15px;
    color: #1d2129;
  }

  .empty-hint {
    font-size: 13px;
    color: #8b95a1;
    margin-top: 4px;
  }
}

.search-tips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 32px;
}

.tip-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  kbd {
    display: block;
    font-family: inherit;
    font-size: 12px;
    color: #8b95a1;
    background: #f3f4f6;
    padding: 4px 8px;
    border-radius: 4px;
  }

  span {
    font-size: 12px;
    color: #8b95a1;
  }
}

.no-results {
  .no-result-icon {
    color: #c9cdd4;
    margin-bottom: 12px;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #4e5969;
  }

  .no-result-hint {
    font-size: 12px;
    color: #8b95a1;
    margin-top: 4px;
  }
}

.results-list {
  padding: 0 12px;
}

.result-group {
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 8px 4px;
  font-size: 12px;
  color: #8b95a1;
}

.category-icon {
  font-size: 14px;
}

.category-name {
  font-weight: 500;
}

.category-count {
  margin-left: auto;
  background: #f3f4f6;
  padding: 0 6px;
  border-radius: 10px;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 2px;

  &:hover {
    background: #f3f4f6;
  }

  &.selected {
    background: #e6f7ff;

    .result-title {
      color: #1890ff;
    }
  }

  &.unread {
    &::before {
      content: '';
      width: 8px;
      height: 8px;
      background: #f53f3f;
      border-radius: 50%;
      margin-right: 8px;
    }
  }
}

.result-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 14px;
  color: #1d2129;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-desc {
  font-size: 12px;
  color: #8b95a1;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-action {
  margin-left: 8px;
}

// 消息结果样式
.message-result {
  .message-time {
    margin-left: auto;
    font-size: 12px;
    color: #c9cdd4;
  }

  .session-name {
    color: #8b95a1;
  }
}

// 联系人结果样式
.contact-result {
  .contact-detail {
    color: #1890ff;
  }
}

// 群组结果样式
.group-result {
  .group-avatar {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .group-desc {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 文件结果样式
.file-result {
  .file-icon {
    width: 40px;
    height: 40px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f3f4f6;
    color: #8b95a1;
  }
}

// 高亮关键词
:deep(.highlight) {
  background: #ffec3d;
  color: #1d2129;
  padding: 0 2px;
  border-radius: 2px;
}

.search-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #e5e6eb;
  background: #fafafa;
}

.result-summary {
  font-size: 12px;
  color: #8b95a1;
}

.footer-tips {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #8b95a1;

  .tip-key {
    display: flex;
    align-items: center;
    gap: 2px;

    kbd {
      font-family: inherit;
      font-size: 11px;
      background: #e5e6eb;
      padding: 2px 4px;
      border-radius: 3px;
    }
  }
}
</style>
