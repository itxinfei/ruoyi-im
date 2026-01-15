<template>
  <div class="contacts-workspace">
    <!-- 联系人侧边栏 -->
    <div class="contacts-sidebar">
      <div class="section-title">通讯录</div>
      <div class="category-list">
        <div
          v-for="category in categories"
          :key="category.key"
          class="category-item"
          :class="{ active: activeCategory === category.key }"
          @click="selectCategory(category.key)"
        >
          <el-icon>
            <component :is="category.icon" />
          </el-icon>
          <span class="category-label">{{ category.label }}</span>
          <span v-if="category.count > 0" class="category-count">{{ category.count }}</span>
        </div>
      </div>
    </div>

    <!-- 联系人列表 -->
    <div class="contacts-list-panel">
      <div class="list-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人..."
          :prefix-icon="Search"
          clearable
          size="small"
        />
      </div>

      <!-- 带字母索引的联系人列表 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="error" class="error-state">
        <el-empty :description="error">
          <el-button type="primary" @click="loadContacts">重试</el-button>
        </el-empty>
      </div>

      <div v-else class="indexed-contacts">
        <div
          v-for="group in indexedContacts"
          :key="group.letter"
          class="contact-group"
        >
          <div class="group-letter">{{ group.letter }}</div>
          <div
            v-for="contact in group.contacts"
            :key="contact.id"
            class="contact-item"
            @click="handleContactClick(contact)"
          >
            <SmartAvatar
              :name="contact.name"
              :avatar="contact.avatar"
              :size="36"
              :show-status="true"
              :status="contact.online ? 'online' : 'offline'"
            />
            <div class="contact-info">
              <div class="contact-name">{{ contact.name }}</div>
              <div class="contact-status">
                <span
                  class="status-dot"
                  :class="contact.online ? 'online' : 'offline'"
                ></span>
                <span class="status-text">{{ contact.online ? '在线' : '离线' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="indexedContacts.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无联系人">
            <el-button type="primary" @click="showAddContact">添加联系人</el-button>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Search, User, UserFilled, Users, Star } from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
})

const store = useStore()

// 状态
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')
const activeCategory = ref('friends')

// 计算属性
const currentUser = computed(() => store.state.user.userInfo)

// 分类列表
const categories = computed(() => [
  {
    key: 'friends',
    label: '好友',
    icon: User,
    count: contacts.value.filter(c => c.friendType === 'friend').length
  },
  {
    key: 'groups',
    label: '群组',
    icon: Users,
    count: contacts.value.filter(c => c.type === 'GROUP').length
  },
  {
    key: 'organization',
    label: '组织架构',
    icon: UserFilled,
    count: 0
  },
  {
    key: 'starred',
    label: '星标',
    icon: Star,
    count: contacts.value.filter(c => c.isStarred).length
  },
])

// 联系人数据
const contacts = computed(() => store.state.im.contacts || [])

// 按字母索引的联系人
const indexedContacts = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  const activeKey = activeCategory.value

  let filteredContacts = contacts.value

  // 按分类过滤
  if (activeKey === 'friends') {
    filteredContacts = contacts.value.filter(c => c.friendType === 'friend')
  } else if (activeKey === 'groups') {
    filteredContacts = contacts.value.filter(c => c.type === 'GROUP')
  } else if (activeKey === 'starred') {
    filteredContacts = contacts.value.filter(c => c.isStarred)
  }
  // organization 显示全部联系人

  // 搜索过滤
  if (keyword) {
    filteredContacts = filteredContacts.filter(contact =>
      contact.name.toLowerCase().includes(keyword) ||
      (contact.remark && contact.remark.toLowerCase().includes(keyword))
    )
  }

  // 按首字母分组
  const groups = {}
  filteredContacts.forEach(contact => {
    const letter = getFirstLetter(contact.name || '')
    if (!groups[letter]) {
      groups[letter] = [letter, []]
    }
    groups[letter][1].push(contact)
  })

  // 转换为数组并排序
  return Object.values(groups).map(([letter, items]) => ({
    letter,
    contacts: items
  })).sort((a, b) => a.letter.localeCompare(b.letter))
})

// 方法
const getFirstLetter = (name) => {
  if (!name) return '#'
  if (/[\u4e00-\u9fa5]/.test(name)) {
    // 中文按拼音首字母分组（这里简化为按字分组）
    return name.charAt(0)
  }
  return name.charAt(0).toUpperCase()
}

const selectCategory = (key) => {
  activeCategory.value = key
}

const handleContactClick = (contact) => {
  // 查找或创建与该联系人的会话
  const existingSession = store.state.im.sessions.find(
    s => s.type === 'PRIVATE' &&
    s.targetId === contact.id
  )

  if (existingSession) {
    // 切换到已有会话
    store.dispatch('im/switchSession', existingSession)
  } else {
    // 创建新的私聊会话
    createPrivateChat(contact)
  }
}

const createPrivateChat = async (contact) => {
  try {
    // TODO: 调用API创建会话
    const session = {
      id: Date.now(),
      type: 'PRIVATE',
      targetId: contact.id,
      name: contact.name,
      avatar: contact.avatar,
      memberCount: 2,
      unreadCount: 0
    }
    store.commit('im/ADD_SESSION', session)
    store.dispatch('im/switchSession', session)
    ElMessage.success(`已创建与 ${contact.name} 的会话`)
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败，请重试')
  }
}

const showAddContact = () => {
  ElMessage.info('添加联系人功能开发中...')
}

const loadContacts = async () => {
  loading.value = true
  error.value = ''

  try {
    // 加载联系人列表
    await store.dispatch('im/loadContacts')
  } catch (err) {
    console.error('加载联系人失败:', err)
    error.value = '加载联系人失败'
    ElMessage.error('加载联系人失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadContacts()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/dingtalk-theme.scss' as *;

.contacts-workspace {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
}

.contacts-sidebar {
  width: 200px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  flex-shrink: 0;

  .section-title {
    padding: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #262626;
    border-bottom: 1px solid #e8e8e8;
  }

  .category-list {
    flex: 1;
    overflow-y: auto;

    .category-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 14px;
      color: #595959;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }

      &.active {
        background: rgba(22, 119, 255, 0.1);
        color: #1677ff;
        font-weight: 500;
      }

      .el-icon {
        margin-right: 8px;
        font-size: 16px;
      }

      .category-label {
        flex: 1;
      }

      .category-count {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.contacts-list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.indexed-contacts {
  flex: 1;
  overflow-y: auto;

  .contact-group {
    .group-letter {
      padding: 12px 16px 8px;
      font-size: 14px;
      font-weight: 600;
      color: #8c8c8c;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
    }

    .contact-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s ease;
      border-bottom: 1px solid #f5f5f5;

      &:hover {
        background: rgba(0, 0, 0, 0.02);
      }

      .contact-info {
        margin-left: 12px;
        flex: 1;
        min-width: 0;

        .contact-name {
          font-size: 14px;
          color: #262626;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .contact-status {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: #8c8c8c;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            margin-right: 6px;

            &.online {
              background: #52c41a;
            }

            &.offline {
              background: #d9d9d9;
            }
          }

          .status-text {
            margin-left: 4px;
          }
        }
      }
    }
  }
}

.loading-state,
.error-state,
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.empty-state {
  :deep(.el-empty) {
    padding: 40px;
  }
}
</style>