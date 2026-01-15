<template>
  <div class="ding-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">DING消息</h2>
        <span class="page-subtitle">重要事务提醒，确保送达</span>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate"> 发送DING </el-button>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="ding-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="ding-tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span>{{ tab.label }}</span>
        <span v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</span>
      </div>
    </div>

    <!-- DING消息列表 -->
    <div class="ding-list">
      <!-- 空状态 -->
      <div v-if="dingList.length === 0 && !loading" class="empty-state">
        <el-icon :size="64"><Bell /></el-icon>
        <p>暂无DING消息</p>
      </div>

      <!-- 列表项 -->
      <div
        v-for="ding in dingList"
        :key="ding.id"
        class="ding-item"
        :class="{
          urgent: ding.isUrgent,
          unread: !ding.isRead,
          'is-sender': ding.isSender,
        }"
        @click="handleViewDetail(ding)"
      >
        <!-- DING图标 -->
        <div class="ding-icon">
          <el-icon :size="24"><Bell /></el-icon>
          <span v-if="ding.isUrgent" class="urgent-mark">!</span>
        </div>

        <!-- DING内容 -->
        <div class="ding-content">
          <div class="ding-header">
            <span class="ding-title">{{ ding.title }}</span>
            <el-tag v-if="ding.isUrgent" type="danger" size="small" effect="plain"> 紧急 </el-tag>
            <el-tag v-if="ding.status === 'DRAFT'" type="info" size="small" effect="plain">
              草稿
            </el-tag>
            <el-tag v-if="ding.status === 'SCHEDULED'" type="warning" size="small" effect="plain">
              定时
            </el-tag>
          </div>

          <div class="ding-text">{{ ding.content }}</div>

          <div class="ding-meta">
            <span class="meta-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(ding.sendTime) }}
            </span>
            <span v-if="!ding.isSender" class="meta-sender">
              <el-icon><User /></el-icon>
              {{ ding.senderName }}
            </span>
            <span v-if="ding.isSender && ding.receiptTotal > 0" class="meta-receipt">
              <el-icon><View /></el-icon>
              已读 {{ ding.receiptRead }}/{{ ding.receiptTotal }}
            </span>
          </div>
        </div>

        <!-- DING操作 -->
        <div class="ding-actions" @click.stop>
          <el-dropdown trigger="click">
            <el-button text :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="ding.isSender && ding.status === 'SENT'"
                  @click="handleRemind(ding)"
                >
                  <el-icon><Bell /></el-icon>
                  再次提醒
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="ding.isSender && ding.status === 'DRAFT'"
                  @click="handleEdit(ding)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-dropdown-item>
                <el-dropdown-item @click="handleViewReceipt(ding)">
                  <el-icon><View /></el-icon>
                  查看回执
                </el-dropdown-item>
                <el-dropdown-item v-if="ding.isSender" @click="handleCopy(ding)">
                  <el-icon><DocumentCopy /></el-icon>
                  转发
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="!ding.isSender && !ding.isRead"
                  @click="handleMarkRead(ding)"
                >
                  <el-icon><Check /></el-icon>
                  标记已读
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleDelete(ding)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <el-button text @click="loadMore"> 加载更多 </el-button>
    </div>

    <!-- DING发送对话框 -->
    <DingSendDialog v-model="showSendDialog" @success="handleSendSuccess" />

    <!-- DING回执对话框 -->
    <DingReceiptDialog v-model="showReceiptDialog" :ding-id="currentDingId" />

    <!-- DING详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="currentDing?.title"
      width="700px"
      class="ding-detail-dialog"
    >
      <div v-if="currentDing" class="ding-detail">
        <!-- DING内容 -->
        <div class="detail-content">
          <div class="content-text">{{ currentDing.content }}</div>

          <!-- 附件 -->
          <div
            v-if="currentDing.attachments && currentDing.attachments.length > 0"
            class="detail-attachments"
          >
            <div v-for="file in currentDing.attachments" :key="file.id" class="attachment-item">
              <el-icon><Document /></el-icon>
              <span>{{ file.name }}</span>
            </div>
          </div>

          <!-- 提醒方式 -->
          <div class="detail-reminders">
            <el-tag v-if="currentDing.appNotification" type="success" size="small">
              应用通知
            </el-tag>
            <el-tag v-if="currentDing.smsNotification" type="warning" size="small">
              短信提醒
            </el-tag>
            <el-tag v-if="currentDing.phoneNotification" type="danger" size="small">
              电话提醒
            </el-tag>
          </div>
        </div>

        <!-- 接收人列表 -->
        <div class="detail-receivers">
          <div class="receivers-header">
            <span class="header-title">接收人 ({{ currentDing.receivers?.length || 0 }})</span>
            <el-tag
              :type="currentDing.receiptRead === currentDing.receiptTotal ? 'success' : 'warning'"
              size="small"
            >
              已读 {{ currentDing.receiptRead || 0 }}/{{ currentDing.receiptTotal || 0 }}
            </el-tag>
          </div>
          <div class="receivers-list">
            <div v-for="receiver in currentDing.receivers" :key="receiver.id" class="receiver-item">
              <el-avatar :size="32" :src="receiver.avatar">
                {{ receiver.name?.charAt(0) }}
              </el-avatar>
              <span class="receiver-name">{{ receiver.name }}</span>
              <el-icon v-if="receiver.hasRead" class="read-icon" :size="16">
                <CircleCheck />
              </el-icon>
              <el-icon v-else class="unread-icon" :size="16">
                <Clock />
              </el-icon>
            </div>
          </div>
        </div>

        <!-- 发送时间 -->
        <div class="detail-time">
          <el-icon><Clock /></el-icon>
          发送时间: {{ formatDateTime(currentDing.sendTime) }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import {
  Plus,
  Bell,
  Clock,
  User,
  View,
  Edit,
  Delete,
  DocumentCopy,
  Check,
  MoreFilled,
  Document,
  CircleCheck,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingSendDialog from '@/components/DING/DingSendDialog.vue'
import DingReceiptDialog from '@/components/DING/DingReceiptDialog.vue'
import { getDingList, deleteDing, remindDing, markDingRead } from '@/api/im/ding'

// 标签页
const activeTab = ref('all')
const tabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'sent', label: '我发送的', count: 0 },
  { key: 'received', label: '我收到的', count: 0 },
  { key: 'unread', label: '未读', count: 0 },
  { key: 'urgent', label: '紧急', count: 0 },
])

// 状态
const dingList = ref([])
const loading = ref(false)
const hasMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)

// 对话框
const showSendDialog = ref(false)
const showReceiptDialog = ref(false)
const showDetailDialog = ref(false)
const currentDingId = ref(null)

// 当前DING详情
const currentDing = computed(() => {
  if (!currentDingId.value) return null
  return dingList.value.find(d => d.id === currentDingId.value)
})

// 加载DING列表
const loadDingList = async (refresh = false) => {
  if (loading.value) return

  loading.value = true

  if (refresh) {
    currentPage.value = 1
    dingList.value = []
  }

  try {
    const { data } = await getDingList({
      type: activeTab.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })

    if (data.code === 200) {
      const records = data.data?.records || []

      if (refresh) {
        dingList.value = records
      } else {
        dingList.value.push(...records)
      }

      hasMore.value = records.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载DING列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  currentPage.value++
  loadDingList()
}

// 刷新列表
const refreshList = () => {
  loadDingList(true)
}

// 创建DING
const handleCreate = () => {
  showSendDialog.value = true
}

// 发送成功
const handleSendSuccess = () => {
  refreshList()
}

// 查看详情
const handleViewDetail = ding => {
  currentDingId.value = ding.id
  showDetailDialog.value = true

  // 如果是未读的接收消息，标记为已读
  if (!ding.isRead && !ding.isSender) {
    handleMarkRead(ding)
  }
}

// 查看回执
const handleViewReceipt = ding => {
  currentDingId.value = ding.id
  showReceiptDialog.value = true
}

// 再次提醒
const handleRemind = async ding => {
  try {
    await remindDing(ding.id)
    ElMessage.success('已发送提醒')
  } catch (error) {
    ElMessage.error('发送提醒失败')
  }
}

// 编辑
const handleEdit = ding => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能即将上线')
}

// 转发
const handleCopy = ding => {
  // TODO: 实现转发功能
  ElMessage.info('转发功能即将上线')
}

// 标记已读
const handleMarkRead = async ding => {
  try {
    await markDingRead(ding.id)
    ding.isRead = true

    // 更新标签数量
    const unreadTab = tabs.value.find(t => t.key === 'unread')
    if (unreadTab && unreadTab.count > 0) {
      unreadTab.count--
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 删除
const handleDelete = async ding => {
  try {
    await ElMessageBox.confirm('确定要删除这条DING消息吗？', '删除确认', { type: 'warning' })

    await deleteDing(ding.id)

    // 从列表中移除
    const index = dingList.value.findIndex(d => d.id === ding.id)
    if (index > -1) {
      dingList.value.splice(index, 1)
    }

    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消或删除失败
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 格式化时间
const formatTime = time => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `昨天 ${hours}:${minutes}`
  }

  // 其他日期
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 格式化完整日期时间
const formatDateTime = time => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 监听标签切换
watch(activeTab, () => {
  refreshList()
})

// 生命周期
onMounted(() => {
  loadDingList(true)
})
</script>

<script>
export default {
  name: 'DingListPage',
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.ding-list-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px; // 修改：20px -> 16px
  background: #f5f7fa; // 钉钉规范
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  .page-title {
    margin: 0;
    font-size: 20px; // 特大标题（主标题）
    font-weight: 500; // 修改：600 -> 500
    color: #262626;
  }

  .page-subtitle {
    margin-left: 12px;
    font-size: 14px;
    color: #999999; // 辅助文字
  }
}

.ding-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 0 4px;

  .ding-tab {
    display: flex;
    align-items: center;
    gap: 8px; // 修改：6px -> 8px
    padding: 8px 16px;
    border-radius: 4px; // 修改：6px -> 4px
    cursor: pointer;
    transition: all 0.2s;
    color: #666666;
    font-size: 14px;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #0089ff; // 钉钉规范
      color: white;
    }

    .tab-badge {
      padding: 2px 6px;
      border-radius: 8px; // 修改：10px -> 8px
      font-size: 12px;
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

.ding-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #999999; // 辅助文字

  .el-icon {
    margin-bottom: 16px; // 修改：12px -> 16px
    opacity: 0.5;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

.ding-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); // 修改：0.08（符合规范）
  }

  &.urgent {
    border-left: 3px solid #ff4d4f;
  }

  &.unread {
    background: #e6f7ff;
  }

  &.is-sender {
    .ding-icon {
      background: #f0f2f5;
    }
  }
}

.ding-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6f7ff;
  border-radius: 50%;
  color: #0089ff;
  position: relative;
  flex-shrink: 0;

  .urgent-mark {
    position: absolute;
    top: -4px;
    right: -4px;
    width: 16px;
    height: 16px;
    background: #ff4d4f;
    color: white;
    border-radius: 50%;
    font-size: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 500; // 修改：bold -> 500
  }
}

.ding-content {
  flex: 1;
  min-width: 0;
}

.ding-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px; // 修改：4px -> 8px
}

.ding-title {
  font-weight: 500;
  color: #262626; // 标题色
  font-size: 16px; // 修改：15px -> 16px（小标题规范）
}

.ding-text {
  color: #333333; // 正文色
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ding-meta {
  display: flex;
  gap: 16px;
  color: #999999; // 辅助文字
  font-size: 12px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8px; // 修改：4px -> 8px
  }
}

.ding-actions {
  flex-shrink: 0;
}

.load-more {
  text-align: center;
  padding: 16px 0;
}

// DING详情对话框
.ding-detail-dialog {
  .ding-detail {
    .detail-content {
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 16px;
    }

    .content-text {
      color: #333333; // 正文色
      font-size: 15px;
      line-height: 1.6;
      margin-bottom: 12px;
      white-space: pre-wrap;
    }

    .detail-attachments {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;
    }

    .attachment-item {
      display: flex;
      align-items: center;
      gap: 8px; // 修改：6px -> 8px
      padding: 8px 12px;
      background: white;
      border-radius: 4px;
      font-size: 13px;
      color: #666666;
    }

    .detail-reminders {
      display: flex;
      gap: 8px;
    }

    .detail-receivers {
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 16px;
    }

    .receivers-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .header-title {
      font-weight: 500;
      color: #262626;
    }

    .receivers-list {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
    }

    .receiver-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .receiver-name {
      font-size: 14px;
      color: #666666;
    }

    .read-icon {
      color: #52c41a;
    }

    .unread-icon {
      color: #999999;
    }

    .detail-time {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #999999;
      font-size: 13px;
    }
  }
}
</style>
