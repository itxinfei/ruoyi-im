<template>
  <div class="contacts-container">
    <!-- 第二栏：联系人分类 -->
    <div class="category-panel">
      <div class="category-header">
        <span class="category-title">联系人</span>
      </div>
      <div class="category-list">
        <div
          v-for="category in categories"
          :key="category.key"
          class="category-item"
          :class="{ active: activeCategory === category.key }"
          @click="activeCategory = category.key"
        >
          <component :is="category.icon" class="category-icon" />
          <span class="category-label">{{ category.label }}</span>
          <span v-if="category.count > 0" class="category-count">{{ category.count }}</span>
        </div>
      </div>
    </div>

    <!-- 第三栏：联系人列表 -->
    <div class="contacts-panel">
      <div class="panel-header">
        <el-input
          v-model="searchText"
          placeholder="搜索联系人"
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <div class="contacts-list">
        <div
          v-for="contact in currentContacts"
          :key="contact.id"
          class="contact-item"
          :class="{ selected: selectedContact?.id === contact.id }"
          @click="selectContact(contact)"
        >
          <div class="contact-avatar">
            <el-avatar :size="40" :src="contact.avatar || '/profile/avatar.png'">
              {{ contact.name?.charAt(0) || contact.nickname?.charAt(0) || '用' }}
            </el-avatar>
            <span v-if="contact.online" class="online-dot"></span>
          </div>
          <div class="contact-info">
            <div class="contact-name">
              {{ contact.name || contact.nickname || contact.username }}
              <i v-if="contact.starred" class="el-icon-star-on star-icon" :size="12"></i>
            </div>
            <div class="contact-status">
              {{ contact.online ? '在线' : contact.lastSeen || '离线' }}
            </div>
          </div>
        </div>

        <el-empty v-if="currentContacts.length === 0" :description="emptyDescription" />
      </div>
    </div>

    <!-- 第四栏：联系人详情 -->
    <div class="detail-panel">
      <template v-if="selectedContact">
        <div class="detail-header">
          <el-avatar :size="72" :src="selectedContact.avatar || '/profile/avatar.png'">
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
          <div class="header-actions">
            <!-- 发现用户模式：显示添加好友按钮 -->
            <el-button
              v-if="activeCategory === 'discover'"
              type="primary"
              :icon="Plus"
              @click="sendFriendRequest"
            >
              添加好友
            </el-button>
            <el-button v-else type="primary" :icon="Comment" @click="startChat">发消息</el-button>
            <el-dropdown trigger="click" @command="handleAction">
              <el-button :icon="More" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="call">
                    <i class="el-icon-phone"></i> 语音通话
                  </el-dropdown-item>
                  <el-dropdown-item command="video">
                    <i class="el-icon-video-camera"></i> 视频通话
                  </el-dropdown-item>
                  <el-dropdown-item divided command="star">
                    <i
                      :class="selectedContact.starred ? 'el-icon-star-off' : 'el-icon-star-on'"
                    ></i>
                    {{ selectedContact.starred ? '取消星标' : '添加星标' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="remark">
                    <i class="el-icon-edit"></i> 编辑备注
                  </el-dropdown-item>
                  <el-dropdown-item divided command="delete">
                    <i class="el-icon-delete"></i> 删除联系人
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
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
                <span class="value">{{ selectedContact.username || '-' }}</span>
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

          <div class="info-section">
            <h4>个性签名</h4>
            <p class="signature">{{ selectedContact.signature || '这个人很懒，什么都没留下' }}</p>
          </div>

          <div class="info-section">
            <h4>快捷操作</h4>
            <div class="quick-actions">
              <el-button
                v-if="activeCategory === 'discover'"
                type="primary"
                :icon="Plus"
                @click="sendFriendRequest"
              >
                添加好友
              </el-button>
              <el-button
                v-if="activeCategory !== 'discover'"
                :icon="ChatDotRound"
                @click="startChat"
              >
                发消息
              </el-button>
              <el-button :icon="Phone" @click="handleAction('call')">语音通话</el-button>
              <el-button :icon="VideoCamera" @click="handleAction('video')">视频通话</el-button>
            </div>
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
  More,
  Phone,
  VideoCamera,
  Star,
  Edit,
  Delete,
  ChatDotRound,
  User,
  StarFilled,
  UserFilled,
  Plus,
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

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userId || userInfo.id || 1
}

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

.contacts-container {
  height: 100%;
  display: flex;
  background-color: #f5f5f5;
}

.category-panel {
  width: 200px;
  min-width: 200px;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid $border-light;
  display: flex;
  flex-direction: column;

  .category-header {
    padding: 16px;
    border-bottom: 1px solid $border-light;

    .category-title {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .category-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;

    .category-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s;
      gap: 12px;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #e6f7ff;
        color: $primary-color;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 20px;
          background-color: $primary-color;
          border-radius: 0 2px 2px 0;
        }
      }

      .category-icon {
        width: 20px;
        height: 20px;
        color: $text-secondary;
        flex-shrink: 0;

        .active & {
          color: $primary-color;
        }
      }

      .category-label {
        flex: 1;
        font-size: 14px;
        color: $text-primary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .category-count {
        font-size: 12px;
        color: $text-secondary;
        background-color: #f0f0f0;
        border-radius: 10px;
        padding: 2px 8px;
        min-width: 20px;
        text-align: center;
      }
    }
  }
}

.contacts-panel {
  width: 320px;
  min-width: 320px;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid $border-light;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 12px;
    border-bottom: 1px solid $border-light;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        background-color: #f5f5f5;
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
      transition: background-color 0.2s;

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
          bottom: 2px;
          right: 2px;
          width: 10px;
          height: 10px;
          background-color: #52c41a;
          border: 2px solid #fff;
          border-radius: 50%;
        }
      }

      .contact-info {
        flex: 1;
        min-width: 0;

        .contact-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          display: flex;
          align-items: center;
          gap: 4px;

          .star-icon {
            color: #faad14;
            font-size: 12px;
          }
        }

        .contact-status {
          font-size: 12px;
          color: $text-secondary;
          margin-top: 2px;
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

  .detail-header {
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    border-bottom: 1px solid $border-light;

    .header-info {
      flex: 1;

      h2 {
        margin: 0 0 4px;
        font-size: 20px;
        font-weight: 500;
      }

      .status {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;

        &.online {
          color: #52c41a;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
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
        grid-template-columns: repeat(1, 1fr);
        gap: 16px;

        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

          .label {
            font-size: 14px;
            color: #666;
            width: 80px;
          }

          .value {
            font-size: 14px;
            color: #333;
            text-align: right;
            flex: 1;
            word-wrap: break-word;
            word-break: break-all;
          }
        }
      }

      .signature {
        margin: 0;
        padding: 12px 16px;
        background-color: #f5f7fa;
        border-radius: 8px;
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }

      .quick-actions {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
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
</style>
