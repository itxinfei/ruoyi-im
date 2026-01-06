<template>
  <div class="notification-panel">
    <div class="panel-header">
      <span class="panel-title">通知</span>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" />
        <el-button link type="primary" size="small" @click="markAllRead" v-if="unreadCount > 0">
          全部已读
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
    </el-tabs>

    <div class="panel-content" v-loading="loading">
      <div class="notification-list">
        <div
          v-for="item in notificationList"
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
          </div>
          <div class="notification-actions">
            <el-button link type="danger" :icon="Delete" @click.stop="handleDelete(item)" />
          </div>
        </div>
        <el-empty v-if="notificationList.length === 0" description="暂无通知" :image-size="60" />
      </div>
    </div>

    <div class="panel-footer">
      <el-button link type="primary" @click="viewAll">查看全部</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, ChatDotRound, Document, Warning, Delete } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/im/notification'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close'])

const activeTab = ref('all')
const loading = ref(false)
const notificationList = ref([])
const unreadCount = ref(0)

const currentType = computed(() => {
  if (activeTab.value === 'all') return ''
  return activeTab.value
})

const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await getNotifications(currentType.value)
    if (response.code === 200) {
      notificationList.value = response.data || []
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
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

const handleNotificationClick = async (item) => {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  // 处理通知点击跳转
  if (item.relatedType && item.relatedId) {
    handleNavigation(item.relatedType, item.relatedId)
  }
}

const handleNavigation = (type, id) => {
  // TODO: 根据类型跳转到对应页面
  console.log('跳转:', type, id)
}

const handleDelete = async (item) => {
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

const viewAll = () => {
  emit('close')
  // 跳转到通知页面
}

const getTypeIcon = (type) => {
  const map = {
    SYSTEM: Warning,
    APPROVAL: Document,
    MESSAGE: ChatDotRound,
  }
  return map[type] || Bell
}

const formatTime = (time) => {
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

watch(() => props.visible, (visible) => {
  if (visible) {
    loadNotifications()
    loadUnreadCount()
  }
})

onMounted(() => {
  loadUnreadCount()
})

defineExpose({
  loadUnreadCount,
})
</script>

<style lang="scss" scoped>
.notification-panel {
  width: 360px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-tabs {
  padding: 0 16px;

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 13px;
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
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }

  &.unread {
    background: #f0f7ff;
  }
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;

  &.type-system {
    background: #fff7e6;
    color: #faad14;
  }

  &.type-approval {
    background: #e6f7ff;
    color: #1677ff;
  }

  &.type-message {
    background: #f6ffed;
    color: #52c41a;
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #999;
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

.panel-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}
</style>
