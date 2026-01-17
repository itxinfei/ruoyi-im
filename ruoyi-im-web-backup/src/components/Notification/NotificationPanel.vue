<template>
  <div v-if="visible" class="notification-panel">
    <div class="panel-header">
      <span class="panel-title">通知</span>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" />
        <el-button link type="primary" size="small" @click="showDingSend = true">
          发送DING
        </el-button>
        <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="markAllRead">
          全部已读
        </el-button>
        <el-button link type="primary" size="small" @click="showSettings = true"> 设置 </el-button>
        <el-button link type="info" size="small" @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="notification-tabs">
      <el-tab-pane name="all">
        <template #label>
          <span class="tab-label">全部</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="SYSTEM">
        <template #label>
          <span class="tab-label">系统</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="APPROVAL">
        <template #label>
          <span class="tab-label">审批</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="MESSAGE">
        <template #label>
          <span class="tab-label">消息</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="DING">
        <template #label>
          <span class="tab-label">DING</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索通知内容"
        :prefix-icon="Search"
        clearable
        size="small"
        @input="handleSearch"
      />
      <el-dropdown trigger="click" @command="handleDateFilter">
        <el-button size="small" :icon="Filter">
          {{ dateFilterText }}
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="all">全部时间</el-dropdown-item>
            <el-dropdown-item command="today">今天</el-dropdown-item>
            <el-dropdown-item command="week">最近7天</el-dropdown-item>
            <el-dropdown-item command="month">最近30天</el-dropdown-item>
            <el-dropdown-item command="custom">自定义</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div v-loading="loading" class="panel-content">
      <div class="notification-list">
        <div
          v-for="item in filteredNotifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleNotificationClick(item)"
        >
          <div class="notification-icon" :class="`type-${item.type?.toLowerCase()}`">
            <el-icon>
              <component :is="getTypeIcon(item.type)" />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
            <div v-if="item.type === 'DING' && item.readReceipt" class="ding-receipt">
              <el-tag size="small" type="info"
                >已读 {{ item.readCount }}/{{ item.totalCount }}</el-tag
              >
            </div>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="item.type === 'DING' && item.readReceipt"
              link
              type="primary"
              @click.stop="showDingReceipt(item)"
            >
              查看回执
            </el-button>
            <el-button link type="danger" :icon="Delete" @click.stop="handleDelete(item)" />
          </div>
        </div>
        <el-empty
          v-if="filteredNotifications.length === 0"
          description="暂无通知"
          :image-size="60"
        />
      </div>

      <div v-if="hasMore" class="load-more">
        <el-button link type="primary" :loading="loadingMore" @click="loadMore">
          加载更多
        </el-button>
      </div>
    </div>

    <div class="panel-footer">
      <el-button link type="primary" @click="viewAll">查看全部</el-button>
    </div>

    <notification-settings v-model:visible="showSettings" @saved="handleSettingsSaved" />
    <ding-send-dialog v-model:visible="showDingSend" @sent="handleDingSent" />
    <ding-receipt-dialog v-model:visible="showDingReceiptDialog" :ding-id="currentDingId" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Bell,
  ChatDotRound,
  Document,
  Warning,
  Delete,
  Search,
  Filter,
  Close,
} from '@element-plus/icons-vue'
import {
  listNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification,
} from '@/api/im/notification'
import NotificationSettings from './NotificationSettings.vue'
import DingSendDialog from '../DING/DingSendDialog.vue'
import DingReceiptDialog from '../DING/DingReceiptDialog.vue'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close', 'update:visible'])

const activeTab = ref('all')
const loading = ref(false)
const loadingMore = ref(false)
const notificationList = ref([])
const unreadCount = ref(0)
const searchKeyword = ref('')
const dateFilter = ref('all')
const showSettings = ref(false)
const showDingSend = ref(false)
const showDingReceiptDialog = ref(false)
const currentDingId = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(false)

const dateFilterText = computed(() => {
  const map = {
    all: '全部时间',
    today: '今天',
    week: '最近7天',
    month: '最近30天',
    custom: '自定义',
  }
  return map[dateFilter.value] || '全部时间'
})

const filteredNotifications = computed(() => {
  let result = notificationList.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      item =>
        item.title?.toLowerCase().includes(keyword) || item.content?.toLowerCase().includes(keyword)
    )
  }

  if (dateFilter.value !== 'all') {
    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())

    result = result.filter(item => {
      if (!item.createTime) return false
      const itemDate = new Date(item.createTime)

      switch (dateFilter.value) {
        case 'today':
          return itemDate >= todayStart
        case 'week':
          const weekAgo = new Date(todayStart.getTime() - 7 * 24 * 60 * 60 * 1000)
          return itemDate >= weekAgo
        case 'month':
          const monthAgo = new Date(todayStart.getTime() - 30 * 24 * 60 * 60 * 1000)
          return itemDate >= monthAgo
        default:
          return true
      }
    })
  }

  return result
})

const currentType = computed(() => {
  if (activeTab.value === 'all') return ''
  return activeTab.value
})

const loadNotifications = async (loadMore = false) => {
  if (loadMore) {
    loadingMore.value = true
    currentPage.value += 1
  } else {
    loading.value = true
    currentPage.value = 1
  }

  try {
    const response = await listNotifications({
      type: currentType.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    if (response.code === 200) {
      const newData = response.data?.list || response.data || []
      if (loadMore) {
        notificationList.value = [...notificationList.value, ...newData]
      } else {
        notificationList.value = newData
      }
      hasMore.value = newData.length >= pageSize.value
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

const handleNotificationClick = async item => {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  if (item.relatedType && item.relatedId) {
    handleNavigation(item.relatedType, item.relatedId)
  }
}

const handleNavigation = (type, id) => {
  console.log('跳转:', type, id)
}

const handleDelete = async item => {
  try {
    await deleteNotification(item.id)
    notificationList.value = notificationList.value.filter(n => n.id !== item.id)
    if (!item.isRead) {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const markAllRead = async () => {
  try {
    await markAllAsRead()
    notificationList.value.forEach(item => {
      item.isRead = true
    })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadNotifications()
}

const handleDateFilter = command => {
  dateFilter.value = command
  if (command === 'custom') {
    ElMessage.info('自定义日期筛选功能开发中')
  } else {
    loadNotifications()
  }
}

const loadMore = () => {
  loadNotifications(true)
}

const viewAll = () => {
  emit('close')
  emit('update:visible', false)
}

const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

const handleSettingsSaved = () => {
  loadNotifications()
}

const showDingReceipt = item => {
  currentDingId.value = item.relatedId
  showDingReceiptDialog.value = true
}

const handleDingSent = () => {
  loadNotifications()
  loadUnreadCount()
}

const getTypeIcon = type => {
  const map = {
    SYSTEM: Warning,
    APPROVAL: Document,
    MESSAGE: ChatDotRound,
    DING: Bell,
  }
  return map[type] || Bell
}

const formatTime = time => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

watch(activeTab, () => {
  loadNotifications()
})

watch(
  () => props.visible,
  visible => {
    if (visible) {
      loadNotifications()
      loadUnreadCount()
    }
  }
)

onMounted(() => {
  loadUnreadCount()
})

defineExpose({
  loadUnreadCount,
})
</script>

<style lang="scss" scoped>
.notification-panel {
  width: 380px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 1px solid #e5e6eb;
  overflow: hidden;
  position: absolute;
  top: 60px;
  right: 20px;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e6eb;
  background: #fff;
}

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-tabs {
  padding: 0 12px;

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 0 10px;
    font-size: 13px;
    height: 36px;
    line-height: 36px;
    color: #4e5969;

    &.is-active {
      color: #165dff;
      font-weight: 500;
    }
  }
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  gap: 8px;
  border-bottom: 1px solid #e5e6eb;
  background: #f7f8fa;

  .el-input {
    flex: 1;
  }
}

.panel-content {
  height: 360px;
  overflow-y: auto;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;

  &:hover {
    background: #f5f7fa;
  }

  &.unread {
    background: #f0f7ff;
    border-left: 3px solid #165dff;

    .notification-title {
      font-weight: 500;
    }
  }
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;

  &.type-system {
    background: #fff7e8;
    color: #ff7d00;
  }

  &.type-approval {
    background: #e8f3ff;
    color: #165dff;
  }

  &.type-message {
    background: #e8ffea;
    color: #00b42a;
  }

  &.type-ding {
    background: #ffece8;
    color: #f53f3f;
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 400;
  color: #1d2129;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.notification-desc {
  font-size: 13px;
  color: #4e5969;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #86909c;
  line-height: 1.4;
}

.ding-receipt {
  margin-top: 4px;
}

.notification-actions {
  flex-shrink: 0;
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .notification-actions {
  opacity: 1;
}

.load-more {
  padding: 12px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.panel-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}
</style>
