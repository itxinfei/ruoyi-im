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
import { useRouter } from 'vue-router'
import { Search, User, UserFilled, Star, Avatar } from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'
import { ElMessage } from 'element-plus'
import { createPrivateConversation } from '@/api/im/conversation'

// Props
const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
})

const store = useStore()
const router = useRouter()

// 状态
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')
const activeCategory = ref('friends')

// 计算属性
const currentUser = computed(() => store.state.user.userInfo)

// 分类列表 - 修复：分别从contacts和groups获取数据
const categories = computed(() => [
  {
    key: 'friends',
    label: '好友',
    icon: User,
    count: contacts.value.filter(c => !c.type || c.type !== 'GROUP').length
  },
  {
    key: 'groups',
    label: '群组',
    icon: Avatar,
    count: groups.value.length
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

// 群组数据
const groups = computed(() => store.state.im.groups || [])

// 按字母索引的联系人/群组
const indexedContacts = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  const activeKey = activeCategory.value

  let filteredItems = []

  // 按分类过滤
  if (activeKey === 'friends') {
    // 好友：排除群组类型的联系人
    filteredItems = contacts.value.filter(c => !c.type || c.type !== 'GROUP')
  } else if (activeKey === 'groups') {
    // 群组：使用 groups 数据
    filteredItems = groups.value.map(g => ({
      ...g,
      id: g.id,
      name: g.name,
      avatar: g.avatar,
      online: true, // 群组默认在线
      isGroup: true,
    }))
  } else if (activeKey === 'starred') {
    filteredItems = contacts.value.filter(c => c.isStarred)
  } else if (activeKey === 'organization') {
    filteredItems = contacts.value
  }

  // 搜索过滤
  if (keyword) {
    filteredItems = filteredItems.filter(item =>
      item.name.toLowerCase().includes(keyword) ||
      (item.remark && item.remark.toLowerCase().includes(keyword))
    )
  }

  // 按首字母分组
  const letterGroups = {}
  filteredItems.forEach(item => {
    const letter = getFirstLetter(item.name || '')
    if (!letterGroups[letter]) {
      letterGroups[letter] = []
    }
    letterGroups[letter].push(item)
  })

  // 转换为数组并排序
  return Object.entries(letterGroups)
    .map(([letter, items]) => ({ letter, contacts: items }))
    .sort((a, b) => a.letter.localeCompare(b.letter))
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
  // 区分群组和联系人
  if (contact.isGroup) {
    // 点击群组 - 处理群聊
    handleGroupClick(contact)
  } else {
    // 点击联系人 - 处理私聊
    handlePrivateChatClick(contact)
  }
}

const handlePrivateChatClick = async (contact) => {
  // 使用 contact.friendId 作为对方用户ID（contacts数据结构中的好友用户ID）
  const userId = contact.friendId || contact.id

  // 查找或创建与该联系人的会话
  const existingSession = store.state.im.sessions.find(
    s => (s.type === 'private' || s.type === 'PRIVATE') &&
    (s.peerId === userId || s.targetId === userId)
  )

  if (existingSession) {
    // 切换到已有会话，并切换到聊天模块
    store.dispatch('im/switchSession', existingSession)
    // 触发模块切换到聊天
    switchToChatModule()
  } else {
    // 创建新的私聊会话
    await createPrivateChat(userId, contact)
  }
}

const handleGroupClick = async (group) => {
  // 查找或创建群聊会话
  const existingSession = store.state.im.sessions.find(
    s => (s.type === 'group' || s.type === 'GROUP') &&
    s.id === group.id
  )

  if (existingSession) {
    // 切换到已有会话
    store.dispatch('im/switchSession', existingSession)
    switchToChatModule()
  } else {
    // 创建群聊会话
    await createGroupChat(group)
  }
}

const createPrivateChat = async (userId, contact) => {
  loading.value = true
  try {
    const response = await createPrivateConversation(userId)

    if (response.code === 200 && response.data) {
      const conversationId = response.data

      // 创建会话对象
      const session = {
        id: conversationId,
        conversationId: conversationId,
        type: 'PRIVATE',
        peerId: userId,
        targetId: userId,
        name: contact.name,
        avatar: contact.avatar,
        memberCount: 2,
        unreadCount: 0,
        lastMessage: null,
        lastMessageTime: null,
        pinned: false,
        muted: false,
      }

      // 添加到会话列表
      store.commit('im/ADD_SESSION', session)
      // 切换到新会话
      await store.dispatch('im/switchSession', session)
      // 切换到聊天模块
      switchToChatModule()

      ElMessage.success(`已创建与 ${contact.name} 的会话`)
    } else {
      throw new Error(response.msg || '创建会话失败')
    }
  } catch (error) {
    console.error('创建私聊会话失败:', error)
    ElMessage.error(error.message || '创建会话失败，请重试')
  } finally {
    loading.value = false
  }
}

const createGroupChat = async (group) => {
  loading.value = true
  try {
    // 使用群组ID作为会话ID
    const session = {
      id: group.id,
      conversationId: group.id,
      type: 'GROUP',
      targetId: group.id,
      name: group.name,
      avatar: group.avatar,
      memberCount: group.memberCount || 0,
      unreadCount: 0,
      lastMessage: null,
      lastMessageTime: null,
      pinned: false,
      muted: false,
    }

    // 添加到会话列表
    store.commit('im/ADD_SESSION', session)
    // 切换到新会话
    await store.dispatch('im/switchSession', session)
    // 切换到聊天模块
    switchToChatModule()

    ElMessage.success(`已进入群聊 ${group.name}`)
  } catch (error) {
    console.error('进入群聊失败:', error)
    ElMessage.error('进入群聊失败，请重试')
  } finally {
    loading.value = false
  }
}

// 切换到聊天模块
const switchToChatModule = () => {
  // 通过事件总线或全局状态通知主布局切换模块
  // 这里我们假设主布局监听 vuex 的状态变化
  // 或者直接路由跳转
  if (router.currentRoute.value.name !== 'ImChat') {
    router.push({ name: 'ImChat' })
  }
  // 触发全局事件，让主布局切换到聊天模块
  window.dispatchEvent(new CustomEvent('switch-im-module', { detail: 'chat' }))
}

const showAddContact = () => {
  ElMessage.info('添加联系人功能开发中...')
}

const loadContacts = async () => {
  loading.value = true
  error.value = ''

  try {
    // 并行加载联系人和群组列表
    await Promise.all([
      store.dispatch('im/loadContacts'),
      store.dispatch('im/loadGroups')
    ])
  } catch (err) {
    console.error('加载数据失败:', err)
    error.value = '加载数据失败'
    ElMessage.error('加载数据失败')
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
@use '@/styles/dingtalk-theme.scss' as *;

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