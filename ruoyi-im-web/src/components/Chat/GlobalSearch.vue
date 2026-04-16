<template>
  <div class="global-search-container">
    <div v-if="visible" class="search-overlay" @click="close" />
    <div v-show="visible" class="search-results-panel shadow-xl">
      <!-- 搜索历史 -->
      <div v-if="!keyword" class="search-initial">
        <div v-if="history.length > 0" class="search-section">
          <div class="section-header">
            <span>搜索历史</span>
            <button class="clear-btn" @click="clearHistory">
              清空
            </button>
          </div>
          <div class="history-list">
            <div
              v-for="item in history"
              :key="item"
              class="history-item"
              @click="handleSelect(item)"
            >
              <el-icon class="text-sm mr-2">
                <Clock />
              </el-icon>
              <span class="truncate">{{ item }}</span>
            </div>
          </div>
        </div>
        <div class="search-placeholder">
          <el-icon class="text-4xl mb-2 text-slate-300">
            <Search />
          </el-icon>
          <p>搜索联系人、群组或聊天记录</p>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-else class="search-results scrollbar-thin">
        <!-- 联系人 -->
        <div v-if="filteredContacts.length > 0" class="search-section">
          <div class="section-header">
            联系人
          </div>
          <div
            v-for="user in filteredContacts"
            :key="user.id"
            class="result-item"
            @click="handleUserClick(user)"
          >
            <div class="avatar bg-blue-500">
              {{ user.nickname?.charAt(0) || user.username?.charAt(0) }}
            </div>
            <span class="name">{{ user.nickname || user.username }}</span>
          </div>
        </div>

        <!-- 群组 -->
        <div v-if="filteredGroups.length > 0" class="search-section">
          <div class="section-header">
            群组
          </div>
          <div
            v-for="group in filteredGroups"
            :key="group.id"
            class="result-item"
            @click="handleGroupClick(group)"
          >
            <div class="avatar bg-primary">
              <el-icon class="text-sm">
                <ChatDotRound />
              </el-icon>
            </div>
            <span class="name">{{ group.name }}</span>
          </div>
        </div>

        <!-- 聊天记录 -->
        <div v-if="messageResults.length > 0" class="search-section">
          <div class="section-header">
            聊天记录
          </div>
          <div
            v-for="msg in messageResults"
            :key="msg.id"
            class="message-item"
            @click="handleMessageClick(msg)"
          >
            <div class="msg-header">
              <span class="msg-name">{{ msg.senderName }}</span>
              <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="msg-content" v-html="highlight(msg.content)" />
          </div>
        </div>

        <div v-if="loading" class="search-loading">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
          <span>正在搜索...</span>
        </div>

        <div v-if="!loading && keyword && noResults" class="search-empty">
          <p>未找到相关结果</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Loading, Clock, Search, ChatDotRound } from '@element-plus/icons-vue'
import { searchMessages } from '@/api/im/message'
import { createConversation } from '@/api/im/conversation'
import { highlightText } from '@/utils/htmlSanitizer'

const props = defineProps({
  visible: Boolean,
  keyword: String
})

const emit = defineEmits(['update:visible', 'select'])
const store = useStore()

const history = ref([])
const messageResults = ref([])
const loading = ref(false)

const contacts = computed(() => store.state.im.contact?.contacts || [])
const groups = computed(() => store.state.im.contact?.groups || [])

const filteredContacts = computed(() => {
  if (!props.keyword) return []
  return contacts.value.filter(c =>
    (c.nickname || '').includes(props.keyword) || (c.username || '').includes(props.keyword)
  ).slice(0, 5)
})

const filteredGroups = computed(() => {
  if (!props.keyword) return []
  return groups.value.filter(g =>
    (g.name || '').includes(props.keyword)
  ).slice(0, 5)
})

const noResults = computed(() => {
  return filteredContacts.value.length === 0 &&
         filteredGroups.value.length === 0 &&
         messageResults.value.length === 0
})

// 加载历史记录
const loadHistory = () => {
  const saved = localStorage.getItem('im_search_history')
  if (saved) history.value = JSON.parse(saved)
}

// 保存历史记录
const saveToHistory = (kw) => {
  if (!kw) return
  const index = history.value.indexOf(kw)
  if (index !== -1) history.value.splice(index, 1)
  history.value.unshift(kw)
  history.value = history.value.slice(0, 10)
  localStorage.setItem('im_search_history', JSON.stringify(history.value))
}

const clearHistory = () => {
  history.value = []
  localStorage.removeItem('im_search_history')
}

// 搜索消息 (防抖)
let timer = null
watch(() => props.keyword, (val) => {
  clearTimeout(timer)
  if (!val) {
    messageResults.value = []
    return
  }
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const res = await searchMessages({ keyword: val, pageSize: 10 })
      if (res.code === 200) {
        messageResults.value = res.data || []
      }
    } catch (e) {
      console.warn('搜索消息失败', e)
    } finally {
      loading.value = false
    }
  }, 500)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
})

const close = () => {
  emit('update:visible', false)
}

const handleSelect = (item) => {
  emit('select', item)
}

const handleUserClick = async (user) => {
  saveToHistory(props.keyword)
  // 创建或获取会话
  try {
    const res = await createConversation({ type: 'PRIVATE', targetId: user.id })
    if (res.code === 200) {
      store.dispatch('im/session/selectSession', res.data)
      close()
    }
  } catch (e) {
    console.error('打开私聊平台失败', e)
    ElMessage.error('打开私聊失败')
  }
}

const handleGroupClick = async (group) => {
  saveToHistory(props.keyword)
  try {
    const res = await createConversation({ type: 'GROUP', targetId: group.id })
    if (res.code === 200) {
      store.dispatch('im/session/selectSession', res.data)
      close()
    }
  } catch (e) {
    console.error('打开群聊失败', e)
    ElMessage.error('打开群聊失败')
  }
}

const handleMessageClick = (msg) => {
  saveToHistory(props.keyword)
  emit('select', msg)
  ElMessage.success('正在定位消息...')
  close()
}

const highlight = (content) => {
  // 使用安全的高亮函数，防止 XSS 攻击
  return highlightText(content, props.keyword, 'span', 'highlight')
}

const formatTime = (ts) => {
  const date = new Date(ts)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

onMounted(() => {
  loadHistory()
  if (contacts.value.length === 0) store.dispatch('im/contact/loadContacts')
  if (groups.value.length === 0) store.dispatch('im/contact/loadGroups')
})
</script>

<style scoped lang="scss">
.global-search-container {
  position: relative;
}

.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
}

.search-results-panel {
  position: absolute;
  top: 4px;
  left: 0;
  right: 0;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  z-index: 101;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--dt-border-light);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.search-initial {
  padding: 12px;
}

.search-section {
  margin-bottom: 16px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-desc);
    margin-bottom: 8px;
    padding: 0 4px;
  }
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 4px 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-xl);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  max-width: 150px;
  cursor: pointer;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .dark & {
    background: var(--dt-bg-hover-dark);
    color: var(--dt-text-primary-dark);
  }
}

.result-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: var(--dt-radius-sm);

  &:hover {
    background: var(--dt-bg-hover);
    .dark & { background: var(--dt-bg-hover-dark); }
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-sm); /* 钉钉规范：4px 圆角 */
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-white);
    margin-right: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-base);
  }

  .name {
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    .dark & { color: var(--dt-text-primary-dark); }
  }
}

.message-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: var(--dt-radius-sm);

  &:hover {
    background: var(--dt-bg-hover);
    .dark & { background: var(--dt-bg-hover-dark); }
  }

  .msg-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 4px;

    .msg-name { font-size: var(--dt-font-size-xs); color: var(--dt-text-secondary); }
    .msg-time { font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); }
  }

  .msg-content {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    .dark & { color: var(--dt-text-primary-dark); }

    :deep(.highlight) {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }
}

.search-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--dt-spacing-2xl) 0;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

.search-loading, .search-empty {
  padding: 24px;
  text-align: center;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

.clear-btn {
  background: none;
  border: none;
  color: var(--dt-brand-color);
  font-size: var(--dt-font-size-xs);
  cursor: pointer;
}

.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: var(--dt-scrollbar-thumb-bg); border-radius: var(--dt-radius-sm); }
</style>
