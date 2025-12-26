<template>
  <div class="contacts-container">
    <!-- 左侧联系人列表 -->
    <div class="contacts-panel">
      <div class="panel-header">
        <el-input
          v-model="searchText"
          placeholder="搜索联系人"
          prefix-icon="el-icon-search"
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
            <el-avatar :size="40" :src="contact.avatar">
              {{ contact.name?.charAt(0) }}
            </el-avatar>
            <span v-if="contact.online" class="online-dot"></span>
          </div>
          <div class="contact-info">
            <div class="contact-name">
              {{ contact.name }}
              <i v-if="contact.starred" class="el-icon-star-on star-icon"></i>
            </div>
            <div class="contact-status">
              {{ contact.online ? '在线' : contact.lastSeen }}
            </div>
          </div>
        </div>

        <el-empty v-if="currentContacts.length === 0" description="暂无联系人" />
      </div>
    </div>

    <!-- 右侧详情区域 -->
    <div class="detail-area">
      <template v-if="selectedContact">
        <!-- 详情头部 -->
        <div class="detail-header">
          <el-avatar :size="72" :src="selectedContact.avatar">
            {{ selectedContact.name?.charAt(0) }}
          </el-avatar>
          <div class="header-info">
            <h2>{{ selectedContact.name }}</h2>
            <p class="status" :class="{ online: selectedContact.online }">
              {{ selectedContact.online ? '在线' : '离线' }}
            </p>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="ChatDotRound" @click="startChat">发消息</el-button>
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
                    <i :class="selectedContact.starred ? 'el-icon-star-off' : 'el-icon-star-on'"></i>
                    {{ selectedContact.starred ? '取消星标' : '添加星标' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="edit">
                    <i class="el-icon-edit"></i> 编辑资料
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
                <span class="value">{{ selectedContact.nickname || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">账号</span>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, More } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

// 状态
const searchText = ref('')
const activeGroup = ref('all')
const selectedContact = ref(null)

// 模拟联系人数据
const contacts = ref([
  {
    id: '1',
    name: '张三',
    nickname: '小张',
    username: 'zhangsan',
    avatar: '',
    email: 'zhangsan@example.com',
    phone: '13800138001',
    signature: '今天天气真不错！',
    online: true,
    starred: true,
    lastSeen: '刚刚',
  },
  {
    id: '2',
    name: '李四',
    nickname: '小李',
    username: 'lisi',
    avatar: '',
    email: 'lisi@example.com',
    phone: '13800138002',
    signature: '努力工作，快乐生活',
    online: false,
    starred: false,
    lastSeen: '2小时前',
  },
  {
    id: '3',
    name: '王五',
    nickname: '老王',
    username: 'wangwu',
    avatar: '',
    email: 'wangwu@example.com',
    phone: '13800138003',
    signature: '技术改变世界',
    online: true,
    starred: true,
    lastSeen: '刚刚',
  },
])

// 计算属性
const filteredContacts = computed(() => {
  if (!searchText.value) return contacts.value
  const keyword = searchText.value.toLowerCase()
  return contacts.value.filter(
    c => c.name.toLowerCase().includes(keyword) || c.username.toLowerCase().includes(keyword)
  )
})

const onlineContacts = computed(() => filteredContacts.value.filter(c => c.online))
const starredContacts = computed(() => filteredContacts.value.filter(c => c.starred))

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
const handleSearch = () => {}

const selectContact = (contact) => {
  selectedContact.value = contact
}

const startChat = () => {
  if (selectedContact.value) {
    router.push(`/chat?userId=${selectedContact.value.id}`)
  }
}

const handleAction = (command) => {
  if (!selectedContact.value) return

  switch (command) {
    case 'call':
      ElMessage.info(`正在呼叫 ${selectedContact.value.name}...`)
      break
    case 'video':
      ElMessage.info(`正在视频呼叫 ${selectedContact.value.name}...`)
      break
    case 'star':
      selectedContact.value.starred = !selectedContact.value.starred
      ElMessage.success(selectedContact.value.starred ? '已添加星标' : '已取消星标')
      break
    case 'edit':
      ElMessage.info('编辑功能开发中...')
      break
    case 'delete':
      confirmDelete()
      break
  }
}

const confirmDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除联系人 ${selectedContact.value.name} 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    const index = contacts.value.findIndex(c => c.id === selectedContact.value.id)
    if (index > -1) {
      contacts.value.splice(index, 1)
      selectedContact.value = null
      ElMessage.success('联系人已删除')
    }
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
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
        margin-left: 4px;
        font-size: 12px;
        color: $text-placeholder;
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
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;

        .info-item {
          .label {
            display: block;
            font-size: 12px;
            color: $text-placeholder;
            margin-bottom: 4px;
          }

          .value {
            font-size: 14px;
            color: $text-primary;
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
