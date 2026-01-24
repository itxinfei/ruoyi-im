<template>
  <div class="global-search-container">
    <div class="search-overlay" v-if="visible" @click="close"></div>
    <div v-show="visible" class="search-results-panel shadow-xl">
      <!-- 搜索历史 -->
      <div v-if="!keyword" class="search-initial">
        <div v-if="history.length > 0" class="search-section">
          <div class="section-header">
            <span>搜索历史</span>
            <button @click="clearHistory" class="clear-btn">清空</button>
          </div>
          <div class="history-list">
            <div 
              v-for="item in history" 
              :key="item" 
              class="history-item"
              @click="handleSelect(item)"
            >
              <span class="material-icons-outlined text-sm mr-2">history</span>
              <span class="truncate">{{ item }}</span>
            </div>
          </div>
        </div>
        <div class="search-placeholder">
          <span class="material-icons-outlined text-4xl mb-2 text-slate-300">search</span>
          <p>搜索联系人、群组或聊天记录</p>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-else class="search-results scrollbar-thin">
        <!-- 联系人 -->
        <div v-if="filteredContacts.length > 0" class="search-section">
          <div class="section-header">联系人</div>
          <div 
            v-for="user in filteredContacts" 
            :key="user.id" 
            class="result-item"
            @click="handleUserClick(user)"
          >
            <div class="avatar bg-blue-500">{{ user.nickname?.charAt(0) || user.username?.charAt(0) }}</div>
            <span class="name">{{ user.nickname || user.username }}</span>
          </div>
        </div>

        <!-- 群组 -->
        <div v-if="filteredGroups.length > 0" class="search-section">
          <div class="section-header">群组</div>
          <div 
            v-for="group in filteredGroups" 
            :key="group.id" 
            class="result-item"
            @click="handleGroupClick(group)"
          >
            <div class="avatar bg-primary">
              <span class="material-icons-outlined text-sm">groups</span>
            </div>
            <span class="name">{{ group.name }}</span>
          </div>
        </div>

        <!-- 聊天记录 -->
        <div v-if="messageResults.length > 0" class="search-section">
          <div class="section-header">聊天记录</div>
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
            <div class="msg-content" v-html="highlight(msg.content)"></div>
          </div>
        </div>

        <div v-if="loading" class="search-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Loading } from '@element-plus/icons-vue'
import { searchMessages } from '@/api/im/message'
import { createConversation } from '@/api/im/conversation'

const props = defineProps({
  visible: Boolean,
  keyword: String
})

const emit = defineEmits(['update:visible', 'select'])
const store = useStore()

const history = ref([])
const messageResults = ref([])
const loading = ref(false)

const contacts = computed(() => store.state.im.contacts || [])
const groups = computed(() => store.state.im.groups || [])

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
      store.dispatch('im/selectSession', res.data)
      close()
    }
  } catch (e) {
    console.error('打开私聊平台失败', e)
  }
}

const handleGroupClick = async (group) => {
  saveToHistory(props.keyword)
  try {
    const res = await createConversation({ type: 'GROUP', targetId: group.id })
    if (res.code === 200) {
      store.dispatch('im/selectSession', res.data)
      close()
    }
  } catch (e) {
    console.error('打开群聊失败', e)
  }
}

const handleMessageClick = (msg) => {
  saveToHistory(props.keyword)
  // 获取对应会话并跳转
  const session = {
    id: msg.conversationId,
    type: msg.sessionType || 'GROUP', // 这需要后端或API提供sessionType，此处先做假设或匹配
    name: msg.sessionName || '搜索结果'
  }
  emit('select', msg)
  ElMessage.success('正在定位消息...')
  close()
}

const highlight = (content) => {
  if (!props.keyword) return content
  const reg = new RegExp(`(${props.keyword})`, 'gi')
  return content.replace(reg, '<span class="highlight">$1</span>')
}

const formatTime = (ts) => {
  const date = new Date(ts)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

onMounted(() => {
  loadHistory()
  if (contacts.value.length === 0) store.dispatch('im/loadContacts')
  if (groups.value.length === 0) store.dispatch('im/loadGroups')
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
  background: #fff;
  border-radius: 8px;
  z-index: 101;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e8e8e8;

  .dark & {
    background: #1e293b;
    border-color: #334155;
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
    font-size: 12px;
    color: #8f959e;
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
  background: #f2f3f5;
  border-radius: 14px;
  font-size: 13px;
  color: #1f2329;
  max-width: 150px;
  cursor: pointer;

  &:hover {
    background: #e5e6eb;
  }

  .dark & {
    background: #334155;
    color: #f1f5f9;
  }
}

.result-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;

  &:hover {
    background: #f5f6f7;
    .dark & { background: rgba(255, 255, 255, 0.05); }
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 10px;
    font-size: 14px;
  }

  .name {
    font-size: 14px;
    color: #1f2329;
    .dark & { color: #f1f5f9; }
  }
}

.message-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;

  &:hover {
    background: #f5f6f7;
    .dark & { background: rgba(255, 255, 255, 0.05); }
  }

  .msg-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 4px;
    
    .msg-name { font-size: 12px; color: #646a73; }
    .msg-time { font-size: 11px; color: #8f959e; }
  }

  .msg-content {
    font-size: 13px;
    color: #1f2329;
    .dark & { color: #f1f5f9; }
    
    :deep(.highlight) {
      color: #0089ff;
      font-weight: 600;
    }
  }
}

.search-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #8f959e;
  font-size: 13px;
}

.search-loading, .search-empty {
  padding: 24px;
  text-align: center;
  color: #8f959e;
  font-size: 13px;
}

.clear-btn {
  background: none;
  border: none;
  color: #0089ff;
  font-size: 12px;
  cursor: pointer;
}

.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 4px; }
</style>
