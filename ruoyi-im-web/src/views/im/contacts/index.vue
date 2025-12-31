<template>
  <div class="contacts-container">
    <!-- 左侧联系人列表 -->
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

      <!-- 分组标签 -->
      <div class="contacts-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeGroup === tab.key }"
          @click="activeGroup = tab.key"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
        </div>
      </div>

      <!-- 联系人列表 -->
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
              {{ contact.online ? '在线' : (contact.lastSeen || '离线') }}
            </div>
          </div>
        </div>

        <el-empty v-if="currentContacts.length === 0" :description="searchText ? '没有找到相关联系人' : '暂无联系人'" />
      </div>
    </div>

    <!-- 右侧详情区域 -->
    <div class="detail-area">
      <template v-if="selectedContact">
        <!-- 详情头部 -->
        <div class="detail-header">
          <el-avatar :size="72" :src="selectedContact.avatar || '/profile/avatar.png'">
            {{ (selectedContact.name || selectedContact.nickname || selectedContact.username)?.charAt(0) || '用' }}
          </el-avatar>
          <div class="header-info">
            <h2>{{ selectedContact.name || selectedContact.nickname || selectedContact.username }}</h2>
            <p class="status" :class="{ online: selectedContact.online }">
              {{ selectedContact.online ? '在线' : '离线' }}
            </p>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="Comment" @click="startChat">发消息</el-button>
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

        <!-- 详情内容 -->
        <div class="detail-content">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">昵称</span>
                <span class="value">{{ selectedContact.nickname || selectedContact.name || '-' }}</span>
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
        </div>
      </template>

      <!-- 空状态 -->
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
  Delete 
} from '@element-plus/icons-vue'
import {
  listContact,
  deleteContact,
  searchContacts,
  getContactStatus,
  getFriendGroups,
  updateContactRemark
} from '@/api/im/contact'

const router = useRouter()

// 状态
const searchText = ref('')
const activeGroup = ref('all')
const selectedContact = ref(null)
const loading = ref(false)
const contacts = ref([])
const friendGroups = ref([])

// 加载联系人列表
const loadContacts = async () => {
  loading.value = true
  try {
    const res = await listContact()
    if (res.code === 200) {
      // 确保contacts是数组
      contacts.value = Array.isArray(res.rows) ? res.rows : 
                      Array.isArray(res.data) ? res.data : []
    } else {
      // 使用模拟数据
      contacts.value = [
        { 
          id: 1, 
          name: '张三', 
          nickname: '张三', 
          username: 'zhangsan', 
          avatar: '/profile/avatar1.png', 
          email: 'zhangsan@example.com', 
          phone: '13800138001', 
          online: true, 
          lastSeen: '刚刚',
          starred: true,
          signature: '积极向上，热爱生活'
        },
        { 
          id: 2, 
          name: '李四', 
          nickname: '李四', 
          username: 'lisi', 
          avatar: '/profile/avatar2.png', 
          email: 'lisi@example.com', 
          phone: '13800138002', 
          online: false, 
          lastSeen: '2小时前',
          starred: false,
          signature: '认真工作，快乐生活'
        },
        { 
          id: 3, 
          name: '王五', 
          nickname: '王五', 
          username: 'wangwu', 
          avatar: '/profile/avatar3.png', 
          email: 'wangwu@example.com', 
          phone: '13800138003', 
          online: true, 
          lastSeen: '在线',
          starred: false,
          signature: '技术爱好者'
        },
        { 
          id: 4, 
          name: '赵六', 
          nickname: '赵六', 
          username: 'zhaoliu', 
          avatar: '/profile/avatar4.png', 
          email: 'zhaoliu@example.com', 
          phone: '13800138004', 
          online: false, 
          lastSeen: '昨天',
          starred: true,
          signature: '热爱运动'
        },
        { 
          id: 5, 
          name: '孙七', 
          nickname: '孙七', 
          username: 'sunqi', 
          avatar: '/profile/avatar5.png', 
          email: 'sunqi@example.com', 
          phone: '13800138005', 
          online: true, 
          lastSeen: '在线',
          starred: false,
          signature: '音乐爱好者'
        },
      ]
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
    ElMessage.error('加载联系人失败')
    // 使用模拟数据
    contacts.value = [
      { 
        id: 1, 
        name: '张三', 
        nickname: '张三', 
        username: 'zhangsan', 
        avatar: '/profile/avatar1.png', 
        email: 'zhangsan@example.com', 
        phone: '13800138001', 
        online: true, 
        lastSeen: '刚刚',
        starred: true,
        signature: '积极向上，热爱生活'
      },
      { 
        id: 2, 
        name: '李四', 
        nickname: '李四', 
        username: 'lisi', 
        avatar: '/profile/avatar2.png', 
        email: 'lisi@example.com', 
        phone: '13800138002', 
        online: false, 
        lastSeen: '2小时前',
        starred: false,
        signature: '认真工作，快乐生活'
      },
    ]
  } finally {
    loading.value = false
  }
}

// 加载在线状态
const loadOnlineStatus = async () => {
  try {
    const userIds = contacts.value.map(c => c.id)
    const res = await getContactStatus(userIds)
    if (res.code === 200 && res.data) {
      contacts.value.forEach(contact => {
        contact.online = res.data[contact.id] === 'online'
      })
    }
  } catch (error) {
    console.error('加载在线状态失败:', error)
  }
}

// 加载好友分组
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

// 搜索联系人
const handleSearch = async () => {
  if (!searchText.value) {
    await loadContacts()
    return
  }
  loading.value = true
  try {
    const res = await searchContacts(searchText.value)
    if (res.code === 200) {
      // 确保搜索结果是数组
      contacts.value = Array.isArray(res.rows) ? res.rows : 
                      Array.isArray(res.data) ? res.data : []
    } else {
      // 如果搜索API未实现，使用本地搜索
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
    // 使用本地搜索
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

// 计算属性
const filteredContacts = computed(() => {
  // 确保contacts是数组
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
  // 确保filteredContacts是数组
  const contactList = Array.isArray(filteredContacts.value) ? filteredContacts.value : []
  return contactList.filter(c => c.online)
})
const starredContacts = computed(() => {
  // 确保filteredContacts是数组
  const contactList = Array.isArray(filteredContacts.value) ? filteredContacts.value : []
  return contactList.filter(c => c.starred)
})

const currentContacts = computed(() => {
  switch (activeGroup.value) {
    case 'online':
      return onlineContacts.value
    case 'starred':
      return starredContacts.value
    default:
      return filteredContacts.value
  }
})

const tabs = computed(() => [
  { key: 'all', label: '全部', count: filteredContacts.value.length },
  { key: 'online', label: '在线', count: onlineContacts.value.length },
  { key: 'starred', label: '星标', count: starredContacts.value.length },
])

// 方法
const selectContact = contact => {
  selectedContact.value = { ...contact }
}

const startChat = () => {
  if (selectedContact.value) {
    router.push(`/im/chat?userId=${selectedContact.value.id}`)
  }
}

const handleAction = async command => {
  if (!selectedContact.value) return

  switch (command) {
    case 'call':
      ElMessage.info(`正在呼叫 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username}...`)
      break
    case 'video':
      ElMessage.info(`正在视频呼叫 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username}...`)
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
      starred: newStarred
    })
    selectedContact.value.starred = newStarred
    // 更新列表中的对应项
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
  }).then(async ({ value }) => {
    try {
      await updateContactRemark({
        friendId: selectedContact.value.id,
        remark: value
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
  }).catch(() => {})
}

const confirmDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除联系人 ${selectedContact.value.name || selectedContact.value.nickname || selectedContact.value.username} 吗？`, '确认删除', {
      type: 'warning',
    })
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

// 初始化
onMounted(async () => {
  await loadContacts()
  await loadFriendGroups()
  // 加载在线状态
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

// 左侧联系人列表面板
.contacts-panel {
  width: $list-panel-width;
  min-width: $list-panel-width;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 12px;
    border-bottom: 1px solid $border-color-lighter;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        background-color: #f5f5f5;
      }
    }
  }

  .contacts-tabs {
    display: flex;
    padding: 0 12px;
    border-bottom: 1px solid $border-color-lighter;

    .tab-item {
      padding: 12px 16px;
      font-size: 14px;
      color: $text-secondary;
      cursor: pointer;
      position: relative;
      transition: color 0.2s;
      display: flex;
      align-items: center;
      gap: 4px;

      &:hover {
        color: $text-primary;
      }

      &.active {
        color: $primary-color;
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 24px;
          height: 2px;
          background-color: $primary-color;
          border-radius: 1px;
        }
      }

      .tab-count {
        font-size: 12px;
        background-color: #f0f0f0;
        color: #666;
        border-radius: 10px;
        padding: 0 6px;
        min-width: 18px;
        height: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
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

// 右侧详情区域
.detail-area {
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
    border-bottom: 1px solid $border-color-lighter;

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
        color: $text-regular;
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

// 响应式
@media screen and (max-width: 768px) {
  .contacts-panel {
    width: 100%;
  }

  .detail-area {
    display: none;
  }
}
</style>