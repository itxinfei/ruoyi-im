<template>
  <div class="contacts-container">
    <!-- 第二栏：联系人列表（钉钉风格，移除分类面板） -->
    <div class="contacts-panel">
      <div class="panel-header">
        <h3 class="panel-title">联系人</h3>
        <el-input
          v-model="searchText"
          placeholder="搜索联系人"
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <!-- 分类标签（水平排列） -->
      <div class="category-tabs">
        <div
          v-for="category in categories"
          :key="category.key"
          class="category-tab"
          :class="{ active: activeCategory === category.key }"
          @click="activeCategory = category.key"
        >
          <component :is="category.icon" class="tab-icon" />
          <span class="tab-label">{{ category.label }}</span>
          <span v-if="category.count > 0" class="tab-count">{{ category.count }}</span>
        </div>
      </div>

      <div class="contacts-list">
        <div
          v-for="contact in currentContacts"
          :key="contact.id"
          class="contact-item"
          :class="{ selected: selectedContact?.id === contact.id }"
          @click="selectContact(contact)"
          @dblclick="startChat"
        >
          <div class="contact-avatar">
            <el-avatar :size="48" :src="contact.avatar || '/profile/avatar.png'">
              {{ contact.name?.charAt(0) || contact.nickname?.charAt(0) || '用' }}
            </el-avatar>
            <span v-if="contact.online" class="online-dot"></span>
          </div>
          <div class="contact-info">
            <div class="contact-name-row">
              <span class="contact-name">
                {{ contact.name || contact.nickname || contact.username }}
              </span>
              <span v-if="contact.starred" class="star-icon">★</span>
            </div>
            <div v-if="contact.signature" class="contact-signature">
              {{ contact.signature }}
            </div>
            <div class="contact-status">
              <span class="status-dot" :class="{ online: contact.online }"></span>
              <span class="status-text">{{ contact.online ? '在线' : '离线' }}</span>
            </div>
          </div>
        </div>

        <el-empty v-if="currentContacts.length === 0" :description="emptyDescription" />
      </div>
    </div>

    <!-- 第三栏：联系人详情 -->
    <div class="detail-panel" :class="{ empty: !selectedContact }">
      <template v-if="selectedContact">
        <div class="detail-header">
          <el-avatar :size="80" :src="selectedContact.avatar || '/profile/avatar.png'">
            {{
              (
                selectedContact.name ||
                selectedContact.nickname ||
                selectedContact.username
              )?.charAt(0) || '用'
            }}
          </el-avatar>
          <div class="header-info">
            <h2>
              {{ selectedContact.name || selectedContact.nickname || selectedContact.username }}
            </h2>
            <p class="status" :class="{ online: selectedContact.online }">
              {{ selectedContact.online ? '在线' : '离线' }}
            </p>
          </div>
        </div>

        <div class="detail-actions">
          <el-button type="primary" size="large" :icon="ChatDotRound" @click="startChat">
            发消息
          </el-button>
          <el-button size="large" :icon="VideoCamera" @click="handleAction('video')">
            视频通话
          </el-button>
        </div>

        <div class="detail-content">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">昵称</span>
                <span class="value">{{
                  selectedContact.nickname || selectedContact.name || '-'
                }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户名</span>
                <span class="value">@{{ selectedContact.username || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱</span>
                <span class="value">{{ selectedContact.email || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机</span>
                <span class="value">{{ selectedContact.phone || '-' }}</span>
              </div>
            </div>
          </div>

          <div v-if="selectedContact.signature" class="info-section">
            <h4>个性签名</h4>
            <p class="signature">{{ selectedContact.signature }}</p>
          </div>
        </div>
      </template>

      <div v-else class="empty-detail">
        <el-empty description="选择一个联系人查看详情" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Comment,
  Phone,
  VideoCamera,
  ChatDotRound,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import {
  listContact,
  deleteContact,
  searchContacts,
  getContactStatus,
  getFriendGroups,
  updateContactRemark,
} from '@/api/im/contact'
import { listUser } from '@/api/im/user'
import { addContact } from '@/api/im/contact'
import { getCurrentUserId } from '@/utils/im-user'

const router = useRouter()

const searchText = ref('')
const activeCategory = ref('discover')
const selectedContact = ref(null)
const loading = ref(false)
const contacts = ref([])
const friendGroups = ref([])
const allUsers = ref([])

const categories = computed(() => [
  {
    key: 'discover',
    label: '发现用户',
    icon: UserFilled,
    count: allUsers.value.length,
  },
  {
    key: 'all',
    label: '全部联系人',
    icon: UserFilled,
    count: contacts.value.length,
  },
  {
    key: 'online',
    label: '在线联系人',
    icon: User,
    count: contacts.value.filter(c => c.online).length,
  },
  {
    key: 'starred',
    label: '星标联系人',
    icon: StarFilled,
    count: contacts.value.filter(c => c.starred).length,
  },
])

const loadContacts = async () => {
  loading.value = true
  try {
    const res = await listContact()
    if (res.code === 200) {
      const dataRows = res.data?.rows || res.data || res.rows || []
      // 后端返回的是 ImFriendVO，需要映射字段：
      // friendId -> id, friendName -> name, friendAvatar -> avatar
      contacts.value = Array.isArray(dataRows)
        ? dataRows.map(c => ({
            id: c.friendId, // 使用 friendId 作为 id
            name: c.friendName || c.remark || c.username, // 使用 friendName 或 remark 或 username
            nickname: c.friendName || c.remark || c.username, // 使用 friendName 作为 nickname
            username: c.username, // 使用 username
            avatar: c.friendAvatar, // 使用 friendAvatar
            email: c.email,
            phone: c.phone,
            signature: c.signature,
            online: c.online || c.status === 'ACTIVE',
            starred: c.starred || false,
            lastSeen: c.lastSeen || '刚刚',
            // 保留原始数据用于后续操作
            _originalId: c.id, // 保存好友关系ID
            _remark: c.remark, // 保存备注
          }))
        : []
    } else {
      contacts.value = []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
    ElMessage.error('加载联系人失败')
    contacts.value = []
  } finally {
    loading.value = false
  }
}

const loadOnlineStatus = async () => {
  try {
    const userIds = contacts.value.map(c => c.id)
    const res = await getContactStatus(userIds)
    if (res.code === 200 && res.data) {
      contacts.value.forEach(contact => {
        contact.online = res.data[contact.id] === 'online' || res.data[contact.id] === 'ACTIVE'
      })
    }
  } catch (error) {
    console.error('加载在线状态失败:', error)
  }
}

const loadFriendGroups = async () => {
  try {
    const res = await getFriendGroups()
    if (res.code === 200) {
      friendGroups.value = res.data || []
    }
  } catch (error) {
    console.error('加载好友分组失败:', error)
  }
}

// 加载所有用户（用于发现）
const loadAllUsers = async () => {
  loading.value = true
  try {
    const res = await listUser()
    if (res.code === 200) {
      const dataRows = res.data?.rows || res.data || res.rows || []
      allUsers.value = Array.isArray(dataRows)
        ? dataRows
            .filter(u => u.id !== getCurrentUserId()) // 排除当前用户
            .map(u => ({
              id: u.id,
              name: u.nickname || u.name,
              nickname: u.nickname || u.name,
              username: u.username,
              avatar: u.avatar,
              email: u.email,
              phone: u.phone,
              signature: u.signature,
              online: u.status === 'ACTIVE' || Math.random() > 0.5,
              starred: false,
              lastSeen: '刚刚',
            }))
        : []
    } else {
      allUsers.value = []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    allUsers.value = []
  } finally {
    loading.value = false
  }
}

// 获取当前用户ID - 已从工具函数导入

const handleSearch = async () => {
  if (!searchText.value) {
    await loadContacts()
    return
  }
  loading.value = true
  try {
    const res = await searchContacts(searchText.value)
    if (res.code === 200) {
      const dataRows = res.data?.rows || res.rows || []
      contacts.value = Array.isArray(dataRows)
        ? dataRows.map(c => ({
            ...c,
            online: c.status === 'ACTIVE' || Math.random() > 0.5,
            starred: Math.random() > 0.7,
            lastSeen: '刚刚',
          }))
        : []
    } else {
      const keyword = searchText.value.toLowerCase()
      contacts.value = contacts.value.filter(c => {
        const name = c.name?.toLowerCase() || ''
        const nickname = c.nickname?.toLowerCase() || ''
        const username = c.username?.toLowerCase() || ''
        return name.includes(keyword) || nickname.includes(keyword) || username.includes(keyword)
      })
    }
  } catch (error) {
    console.error('搜索联系人失败:', error)
    const keyword = searchText.value.toLowerCase()
    contacts.value = contacts.value.filter(c => {
      const name = c.name?.toLowerCase() || ''
      const nickname = c.nickname?.toLowerCase() || ''
      const username = c.username?.toLowerCase() || ''
      return name.includes(keyword) || nickname.includes(keyword) || username.includes(keyword)
    })
  } finally {
    loading.value = false
  }
}

const filteredContacts = computed(() => {
  const contactList = Array.isArray(contacts.value) ? contacts.value : []

  if (!searchText.value) return contactList
  const keyword = searchText.value.toLowerCase()
  return contactList.filter(c => {
    const name = c.name?.toLowerCase() || ''
    const nickname = c.nickname?.toLowerCase() || ''
    const username = c.username?.toLowerCase() || ''
    return name.includes(keyword) || nickname.includes(keyword) || username.includes(keyword)
  })
})

const onlineContacts = computed(() => {
  const contactList = Array.isArray(filteredContacts.value) ? filteredContacts.value : []
  return contactList.filter(c => c.online)
})

const starredContacts = computed(() => {
  const contactList = Array.isArray(filteredContacts.value) ? filteredContacts.value : []
  return contactList.filter(c => c.starred)
})

const currentContacts = computed(() => {
  switch (activeCategory.value) {
    case 'discover':
      return discoverContacts.value
    case 'online':
      return onlineContacts.value
    case 'starred':
      return starredContacts.value
    default:
      return filteredContacts.value
  }
})

// 发现用户列表（搜索）
const discoverContacts = computed(() => {
  const userList = Array.isArray(allUsers.value) ? allUsers.value : []
  if (!searchText.value) return userList
  const keyword = searchText.value.toLowerCase()
  return userList.filter(u => {
    const name = u.name?.toLowerCase() || ''
    const nickname = u.nickname?.toLowerCase() || ''
    const username = u.username?.toLowerCase() || ''
    return name.includes(keyword) || nickname.includes(keyword) || username.includes(keyword)
  })
})

// 空状态描述
const emptyDescription = computed(() => {
  if (activeCategory.value === 'discover') {
    return searchText.value ? '没有找到相关用户' : '暂无其他用户'
  }
  return searchText.value ? '没有找到相关联系人' : '暂无联系人'
})

const selectContact = contact => {
  selectedContact.value = { ...contact }
}

const startChat = () => {
  if (selectedContact.value) {
    router.push(`/im/chat?userId=${selectedContact.value.id}`)
  }
}

// 发送好友请求
const sendFriendRequest = async () => {
  if (!selectedContact.value) return

  try {
    const result = await ElMessageBox.prompt(
      `请输入添加 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username} 为好友的申请理由`,
      '添加好友',
      {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputPlaceholder: '我是...',
        inputValue: '你好，我想添加你为好友',
      }
    )
    const remark = result.value

    await addContact({
      targetUserId: selectedContact.value.id,
      remark: remark,
    })
    ElMessage.success('好友申请已发送')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送好友申请失败:', error)
      ElMessage.error('发送好友申请失败')
    }
  }
}

const handleAction = async command => {
  if (!selectedContact.value) return

  switch (command) {
    case 'call':
      ElMessage.info(
        `正在呼叫 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username}...`
      )
      break
    case 'video':
      ElMessage.info(
        `正在视频呼叫 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username}...`
      )
      break
    case 'star':
      await toggleStar()
      break
    case 'remark':
      editContact()
      break
    case 'delete':
      confirmDelete()
      break
  }
}

const toggleStar = async () => {
  try {
    const newStarred = !selectedContact.value.starred
    await updateContactRemark({
      friendId: selectedContact.value.id,
      starred: newStarred,
    })
    selectedContact.value.starred = newStarred
    const contact = contacts.value.find(c => c.id === selectedContact.value.id)
    if (contact) {
      contact.starred = newStarred
    }
    ElMessage.success(newStarred ? '已添加星标' : '已取消星标')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const editContact = () => {
  ElMessageBox.prompt('请输入备注名称', '编辑备注', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: selectedContact.value.remark || selectedContact.value.nickname || '',
  })
    .then(async ({ value }) => {
      try {
        await updateContactRemark({
          friendId: selectedContact.value.id,
          remark: value,
        })
        selectedContact.value.nickname = value
        const contact = contacts.value.find(c => c.id === selectedContact.value.id)
        if (contact) {
          contact.remark = value
        }
        ElMessage.success('备注已更新')
      } catch (error) {
        console.error('更新备注失败:', error)
        ElMessage.error('更新备注失败')
      }
    })
    .catch(() => {})
}

const confirmDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除联系人 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username} 吗？`,
      '确认删除',
      {
        type: 'warning',
      }
    )
    await deleteContact(selectedContact.value.id)
    const index = contacts.value.findIndex(c => c.id === selectedContact.value.id)
    if (index > -1) {
      contacts.value.splice(index, 1)
      selectedContact.value = null
      ElMessage.success('联系人已删除')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(async () => {
  await loadAllUsers() // 先加载所有用户（发现）
  await loadContacts()
  await loadFriendGroups()
  await loadOnlineStatus()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

// 钉钉风格联系人页面
.contacts-container {
  height: 100%;
  display: flex;
  background-color: #f5f5f5;
}

.contacts-panel {
  width: 320px;
  min-width: 280px;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px 16px 12px;
    border-bottom: 1px solid $border-light;

    .panel-title {
      margin: 0 0 12px 0;
      font-size: 18px;
      font-weight: 500;
      color: $text-primary;
    }

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        background-color: #f5f5f5;
        border: none;
        box-shadow: none;

        &.is-focus {
          background-color: #fff;
          border: 1px solid $primary-color;
        }
      }
    }
  }

  // 分类标签（水平滚动）
  .category-tabs {
    display: flex;
    padding: 8px 12px;
    gap: 4px;
    overflow-x: auto;
    border-bottom: 1px solid $border-light;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }

    .category-tab {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 12px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.2s;
      white-space: nowrap;
      font-size: 13px;
      color: $text-secondary;
      background-color: transparent;

      &:hover {
        background-color: #f5f5f5;
      }

      &.active {
        background-color: #e6f7ff;
        color: $primary-color;
        font-weight: 500;
      }

      .tab-icon {
        width: 16px;
        height: 16px;
      }

      .tab-count {
        font-size: 11px;
        background-color: rgba(24, 144, 255, 0.1);
        border-radius: 8px;
        padding: 1px 6px;
        margin-left: 2px;
      }
    }
  }

  .contacts-list {
    flex: 1;
    overflow-y: auto;

    .contact-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.15s;

      &:hover {
        background-color: #f5f7fa;
      }

      &.selected {
        background-color: #e6f7ff;
      }

      .contact-avatar {
        position: relative;
        margin-right: 12px;
        flex-shrink: 0;

        .online-dot {
          position: absolute;
          bottom: 1px;
          right: 1px;
          width: 12px;
          height: 12px;
          background-color: #52c41a;
          border: 2px solid #fff;
          border-radius: 50%;
        }
      }

      .contact-info {
        flex: 1;
        min-width: 0;

        .contact-name-row {
          display: flex;
          align-items: center;
          gap: 4px;
          margin-bottom: 2px;

          .contact-name {
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .star-icon {
            color: #faad14;
            font-size: 14px;
            flex-shrink: 0;
          }
        }

        .contact-signature {
          font-size: 12px;
          color: $text-secondary;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 2px;
        }

        .contact-status {
          display: flex;
          align-items: center;
          gap: 4px;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: $text-secondary;

            &.online {
              background-color: #52c41a;
            }
          }

          .status-text {
            font-size: 12px;
            color: $text-secondary;
          }
        }
      }
    }
  }
}

.detail-panel {
  flex: 1;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.empty {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .detail-header {
    padding: 32px 24px 24px;
    display: flex;
    align-items: center;
    gap: 20px;
    border-bottom: 1px solid $border-light;

    .header-info {
      flex: 1;

      h2 {
        margin: 0 0 8px;
        font-size: 24px;
        font-weight: 500;
        color: $text-primary;
      }

      .status {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;
        display: inline-flex;
        align-items: center;
        gap: 6px;

        &.online {
          color: #52c41a;

          &::before {
            content: '';
            width: 8px;
            height: 8px;
            background-color: #52c41a;
            border-radius: 50%;
          }
        }
      }
    }
  }

  .detail-actions {
    padding: 16px 24px;
    display: flex;
    gap: 12px;
    border-bottom: 1px solid $border-light;
  }

  .detail-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .info-section {
      margin-bottom: 24px;

      h4 {
        margin: 0 0 16px;
        font-size: 14px;
        font-weight: 500;
        color: $text-secondary;
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px 24px;

        .info-item {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .label {
            font-size: 12px;
            color: $text-secondary;
          }

          .value {
            font-size: 14px;
            color: $text-primary;
            word-wrap: break-word;
          }
        }
      }

      .signature {
        margin: 0;
        padding: 12px 16px;
        background-color: #f5f7fa;
        border-radius: 8px;
        font-size: 14px;
        color: $text-secondary;
        line-height: 1.6;
      }
    }
  }

  .empty-detail {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .contacts-panel {
    width: 100%;
    min-width: unset;

    .detail-panel {
      display: none;
    }
  }

  .detail-panel {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1000;

    .empty-detail {
      display: none;
    }

    &:not(.empty) {
      display: flex;
    }
  }
}
</style>
