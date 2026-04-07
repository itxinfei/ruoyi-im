<template>
  <div class="frequent-contacts-view">
    <!-- 头部 -->
    <header class="view-header">
      <div class="header-left">
        <span class="view-title">常用联系人</span>
        <span class="count-badge">{{ contacts.length }}</span>
      </div>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索"
        prefix-icon="Search"
        clearable
        size="small"
        class="search-input"
        @input="handleSearch"
      />
    </header>

    <!-- 联系人列表 -->
    <div class="view-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredContacts.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Clock /></el-icon>
        <p class="empty-text">{{ searchKeyword ? '未找到匹配的联系人' : '暂无常用联系人' }}</p>
        <p class="empty-hint">经常联系的成员会显示在这里</p>
      </div>

      <div v-else class="contacts-grid">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="contact-card"
          @click="handleSelect(contact)"
        >
          <div class="avatar-section">
            <DingtalkAvatar
              :src="contact.avatar"
              :name="contact.name || contact.userName"
              :user-id="contact.id"
              :size="52"
              shape="square"
            />
            <div v-if="contact.online" class="online-dot" />
          </div>
          <div class="info-section">
            <div class="contact-name">{{ contact.name || contact.userName }}</div>
            <div class="contact-dept">{{ contact.departmentName || contact.position || '成员' }}</div>
          </div>
          <div class="action-section">
            <el-button
              type="primary"
              size="small"
              circle
              @click.stop="handleChat(contact)"
            >
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
          </div>
          <div class="freq-badge">
            <span>{{ contact.frequency || 0 }}次</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Clock, ChatDotRound, Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getContacts } from '@/api/im/contact'
import { createConversation } from '@/api/im/conversation'

const emit = defineEmits(['select-member', 'back'])
const store = useStore()

const loading = ref(false)
const contacts = ref([])
const searchKeyword = ref('')

const filteredContacts = computed(() => {
  if (!searchKeyword.value.trim()) {
    return contacts.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return contacts.value.filter(c =>
    (c.name || c.userName || '').toLowerCase().includes(keyword) ||
    (c.departmentName || '').toLowerCase().includes(keyword)
  )
})

const loadContacts = async () => {
  loading.value = true
  try {
    // 获取好友列表，按联系频率排序
    const res = await getContacts()
    if (res.code === 200) {
      // 模拟联系频率数据，实际应从后端获取
      contacts.value = (res.data || []).map(c => ({
        ...c,
        frequency: Math.floor(Math.random() * 50) + 1,
        online: Math.random() > 0.5
      })).sort((a, b) => b.frequency - a.frequency)
    }
  } catch (e) {
    console.error('加载常用联系人失败', e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 本地过滤，无需重新请求
}

const handleSelect = (contact) => {
  emit('select-member', contact)
}

const handleChat = async (contact) => {
  try {
    const userId = contact.friendId || contact.id
    const res = await createConversation({ type: 'PRIVATE', targetId: userId })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      ElMessage.success(`已打开与 ${contact.name || contact.friendName} 的会话`)
    }
  } catch (e) {
    ElMessage.error('无法发起对话')
  }
}

onMounted(() => {
  loadContacts()
})
</script>

<style scoped lang="scss">
.frequent-contacts-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
}

.view-header {
  height: 56px;
  padding: 0 var(--dt-spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
  }

  .view-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .count-badge {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    background: var(--dt-bg-body);
    padding: 2px 8px;
    border-radius: var(--dt-radius-lg);
  }

  .search-input {
    width: 200px;
  }
}

.view-content {
  flex: 1;
  padding: var(--dt-spacing-lg);
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  gap: var(--dt-spacing-sm);

  .empty-icon {
    font-size: 48px;
  }

  .empty-text {
    font-size: 14px;
    margin: 0;
  }

  .empty-hint {
    font-size: 12px;
    margin: 0;
    color: var(--dt-text-quaternary);
  }
}

.contacts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--dt-spacing-md);
}

.contact-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border: 1px solid var(--dt-border-lighter);
  position: relative;

  &:hover {
    box-shadow: var(--dt-shadow-card-hover);
    border-color: var(--dt-brand-light);

    .action-section {
      opacity: 1;
    }
  }

  .avatar-section {
    position: relative;
    margin-bottom: var(--dt-spacing-sm);

    .online-dot {
      position: absolute;
      right: 0;
      bottom: 0;
      width: 12px;
      height: 12px;
      background: var(--dt-success-color);
      border: 2px solid var(--dt-bg-card);
      border-radius: 50%;
    }
  }

  .info-section {
    text-align: center;
    flex: 1;
    width: 100%;

    .contact-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .contact-dept {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-top: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .action-section {
    margin-top: var(--dt-spacing-sm);
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }

  .freq-badge {
    position: absolute;
    top: var(--dt-spacing-sm);
    right: var(--dt-spacing-sm);
    font-size: 10px;
    color: var(--dt-text-quaternary);
    background: var(--dt-bg-body);
    padding: 2px 6px;
    border-radius: var(--dt-radius-lg);
  }
}
</style>
