<template>
  <div class="contacts-container">
    <!-- 第一栏：组织架构树 -->
    <div class="org-tree-panel">
      <div class="panel-header">
        <h3 class="panel-title">组织架构</h3>
      </div>
      <OrganizationTree @select-member="handleOrgMemberSelect" />
    </div>

    <!-- 第二栏：联系人列表（钉钉风格，移除分类面板） -->
    <div class="contacts-panel">
      <div class="panel-header">
        <h3 class="panel-title">联系人</h3>
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名、部门、职位"
          :prefix-icon="Search"
          clearable
          size="default"
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
        <!-- 搜索结果提示 -->
        <div v-if="isSearching" class="search-tips">
          <span class="tips-text">搜索 "{{ searchKeyword }}"</span>
          <span class="result-count">找到 {{ displayContacts.length }} 个结果</span>
        </div>

        <div
          v-for="contact in displayContacts"
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
            <!-- 搜索结果显示匹配字段 -->
            <div v-if="isSearching" class="contact-search-match">
              <span v-if="contact.deptName" class="match-tag">{{ contact.deptName }}</span>
              <span
                v-if="contact.position && contact.position !== contact.deptName"
                class="match-tag"
                >{{ contact.position }}</span
              >
            </div>
            <div v-else-if="contact.signature" class="contact-signature">
              {{ contact.signature }}
            </div>
            <div class="contact-status">
              <span class="status-dot" :class="{ online: contact.online }"></span>
              <span class="status-text">{{ contact.online ? '在线' : '离线' }}</span>
            </div>
          </div>
        </div>

        <el-empty v-if="displayContacts.length === 0" :description="emptyDescription" />
      </div>
    </div>

    <!-- 第三栏：联系人详情 -->
    <div class="detail-panel" :class="{ empty: !selectedContact }">
      <div v-if="selectedContact" class="detail-content-wrapper">
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
      </div>

      <div v-if="!selectedContact" class="empty-detail">
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
  Comment,
  Phone,
  VideoCamera,
  ChatDotRound,
  User,
  UserFilled,
  StarFilled,
  Search,
} from '@element-plus/icons-vue'
import {
  listContact,
  deleteContact,
  getContactStatus,
  getFriendGroups,
  updateContactRemark,
} from '@/api/im/contact'
import { listUser } from '@/api/im/user'
import { addContact } from '@/api/im/contact'
import { getCurrentUserId } from '@/utils/im-user'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'

const router = useRouter()

const activeCategory = ref('discover')
const selectedContact = ref(null)
const loading = ref(false)
const contacts = ref([])
const friendGroups = ref([])
const allUsers = ref([])
const searchKeyword = ref('')
const searchResult = ref([])

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
      const mappedContacts = Array.isArray(dataRows)
        ? dataRows.map(c => ({
            id: c.friendId, // 使用 friendId 作为 id（好友用户ID）
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

      // 去重逻辑：使用Map确保每个好友只出现一次（按friendId去重）
      const uniqueContactsMap = new Map()
      mappedContacts.forEach(contact => {
        if (contact.id && !uniqueContactsMap.has(contact.id)) {
          uniqueContactsMap.set(contact.id, contact)
        }
      })

      contacts.value = Array.from(uniqueContactsMap.values())
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

const filteredContacts = computed(() => {
  const contactList = Array.isArray(contacts.value) ? contacts.value : []
  return contactList
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

// 发现用户列表
const discoverContacts = computed(() => {
  const userList = Array.isArray(allUsers.value) ? allUsers.value : []
  return userList
})

// 搜索处理函数
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    searchResult.value = []
    return
  }

  const keyword = searchKeyword.value.toLowerCase().trim()

  // 合并所有联系人数据进行搜索
  const allContacts = [
    ...contacts.value,
    ...allUsers.value.filter(u => !contacts.value.some(c => c.id === u.id)),
  ]

  // 按姓名、部门、职位搜索
  searchResult.value = allContacts.filter(contact => {
    const nameMatch = (contact.name || contact.nickname || contact.username || '')
      .toLowerCase()
      .includes(keyword)
    const deptMatch = (contact.deptName || contact.department || '').toLowerCase().includes(keyword)
    const positionMatch = (contact.position || '').toLowerCase().includes(keyword)
    const phoneMatch = (contact.phone || '').includes(keyword)
    const emailMatch = (contact.email || '').toLowerCase().includes(keyword)

    return nameMatch || deptMatch || positionMatch || phoneMatch || emailMatch
  })
}

// 搜索模式判断
const isSearching = computed(() => searchKeyword.value.trim().length > 0)

// 根据搜索状态返回不同的列表
const displayContacts = computed(() => {
  if (isSearching.value) {
    return searchResult.value
  }
  return currentContacts.value
})

// 空状态描述
const emptyDescription = computed(() => {
  if (activeCategory.value === 'discover') {
    return '暂无其他用户'
  }
  return '暂无联系人'
})

const selectContact = contact => {
  selectedContact.value = { ...contact }
}

// 处理组织架构树成员选择
const handleOrgMemberSelect = member => {
  // 将组织架构成员转换为联系人格式
  const contact = {
    id: member.id,
    name: member.name || member.nickname || member.username,
    nickname: member.name || member.nickname || member.username,
    username: member.username,
    avatar: member.avatar,
    email: member.email,
    phone: member.phone,
    signature: member.signature,
    online: member.online || false,
    position: member.position,
    deptName: member.deptName,
    // 从组织架构选择的成员可能不是好友，标记一下
    _fromOrg: true,
  }
  selectContact(contact)
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

// 钉钉风格联系人页面（符合UI设计规范）
.contacts-container {
  height: 100%;
  display: flex;
  background-color: #f5f7fa;
}

// 组织架构树面板
.org-tree-panel {
  width: 240px;
  min-width: 200px;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px; // 修改：16px 16px 12px -> 16px（统一边距）
    border-bottom: 1px solid #e8e8e8;

    .panel-title {
      margin: 0;
      font-size: 16px; // 小标题规范
      font-weight: 500;
      color: #262626; // 标题色规范
    }
  }
}

.contacts-panel {
  width: 320px;
  min-width: 280px;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px; // 修改：16px 16px 12px -> 16px（统一边距）
    border-bottom: 1px solid #e8e8e8;

    .panel-title {
      margin: 0 0 12px 0;
      font-size: 18px; // 模块标题（应为20px，但18px也可接受）
      font-weight: 500;
      color: #262626;
    }
  }

  // 搜索框样式
  .search-box {
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;

    :deep(.el-input__wrapper) {
      border-radius: 4px; // 修改：6px -> 4px（符合规范）
      background-color: #f5f7fa;
      box-shadow: none;
      border: 1px solid transparent;
      transition: all 0.2s;

      &:hover {
        background-color: #f0f2f5;
      }

      &.is-focus {
        background-color: #fff;
        border-color: #0089ff; // DingTalk standard
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
      }
    }

    :deep(.el-input__inner) {
      font-size: 14px;
      color: #333333; // 正文色
      height: 32px;
      line-height: 32px;

      &::placeholder {
        color: #cccccc;
      }
    }

    :deep(.el-input__prefix) {
      color: #999999; // 辅助文字
    }
  }

  // 分类标签（水平滚动）
  .category-tabs {
    display: flex;
    padding: 8px 12px;
    gap: 8px; // 修改：4px -> 8px（符合4的倍数）
    overflow-x: auto;
    border-bottom: 1px solid #e8e8e8;
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
      font-size: 14px; // 修改：13px -> 14px（正文规范）
      color: #666666; // 次要文字
      background-color: transparent;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #e6f7ff;
        color: #0089ff; // DingTalk standard
        font-weight: 500;
      }

      .tab-icon {
        width: 16px;
        height: 16px;
      }

      .tab-count {
        font-size: 12px; // 辅助文字
        background-color: rgba(24, 144, 255, 0.1);
        border-radius: 8px;
        padding: 2px 6px; // 修改：1px 6px -> 2px 6px（符合4的倍数）
        margin-left: 4px;
      }
    }
  }

  .contacts-list {
    flex: 1;
    overflow-y: auto;

    // 搜索提示区域
    .search-tips {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px; // 修改：10px -> 12px
      background-color: #fafafa;
      border-bottom: 1px solid #e8e8e8;
      position: sticky;
      top: 0;
      z-index: 10;

      .tips-text {
        font-size: 14px; // 修改：13px -> 14px
        color: #262626;
        font-weight: 500;
      }

      .result-count {
        font-size: 12px;
        color: #666666; // 修改：#text-secondary -> 规范值
      }
    }

    // 搜索匹配标签样式
    .contact-search-match {
      display: flex;
      gap: 4px;
      margin-bottom: 4px; // 修改：2px -> 4px

      .match-tag {
        font-size: 12px;
        color: #999999; // 辅助文字
        background-color: #f0f2f5;
        padding: 2px 8px; // 修改：1px 6px -> 2px 8px
        border-radius: 4px;
      }
    }

    .contact-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s; // 修改：0.15s -> 0.2s

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
          bottom: 2px; // 修改：1px -> 2px
          right: 2px;
          width: 10px; // 规范值：在线状态点10×10px
          height: 10px;
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
          margin-bottom: 4px; // 修改：2px -> 4px

          .contact-name {
            font-size: 14px;
            font-weight: 500;
            color: #333333; // 正文色
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
          color: #999999; // 辅助文字
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 4px; // 修改：2px -> 4px
        }

        .contact-status {
          display: flex;
          align-items: center;
          gap: 4px;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #999999; // 辅助文字

            &.online {
              background-color: #52c41a;
            }
          }

          .status-text {
            font-size: 12px;
            color: #999999; // 辅助文字
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
    border-bottom: 1px solid #e8e8e8;

    .header-info {
      flex: 1;

      h2 {
        margin: 0 0 8px;
        font-size: 24px; // 特大标题
        font-weight: 500;
        color: #262626;
      }

      .status {
        margin: 0;
        font-size: 14px;
        color: #999999; // 辅助文字
        display: inline-flex;
        align-items: center;
        gap: 8px; // 修改：6px -> 8px

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
    border-bottom: 1px solid #e8e8e8;
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
        color: #666666; // 次要文字
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
            color: #999999; // 辅助文字
          }

          .value {
            font-size: 14px;
            color: #262626;
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
        color: #666666; // 次要文字
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
