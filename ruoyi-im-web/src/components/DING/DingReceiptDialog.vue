<template>
  <el-dialog
    v-model="dialogVisible"
    title="DING已读回执"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="ding-info">
      <div class="ding-content">{{ dingInfo.content }}</div>
      <div class="ding-time">发送时间：{{ formatTime(dingInfo.createTime) }}</div>
    </div>

    <el-tabs v-model="activeTab" class="receipt-tabs">
      <el-tab-pane name="all">
        <template #label>
          <span class="tab-label">
            全部
            <el-badge :value="totalCount" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="read">
        <template #label>
          <span class="tab-label">
            已读
            <el-badge :value="readCount" type="success" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="unread">
        <template #label>
          <span class="tab-label">
            未读
            <el-badge :value="unreadCount" type="danger" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div v-loading="loading" class="receipt-list">
      <div v-for="item in filteredReceipts" :key="item.userId" class="receipt-item">
        <el-avatar :size="40" :src="item.avatar">
          {{ item.name?.charAt(0) }}
        </el-avatar>
        <div class="receipt-info">
          <div class="receipt-name">{{ item.name }}</div>
          <div class="receipt-status">
            <el-tag :type="item.isRead ? 'success' : 'danger'" size="small">
              {{ item.isRead ? '已读' : '未读' }}
            </el-tag>
            <span v-if="item.isRead" class="read-time">
              {{ formatTime(item.readTime) }}
            </span>
          </div>
        </div>
      </div>
      <el-empty v-if="filteredReceipts.length === 0" description="暂无数据" :image-size="60" />
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" :disabled="unreadCount === 0" @click="handleRemind">
        提醒未读人员
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: Boolean,
  dingId: String,
})

const emit = defineEmits(['update:visible'])

const dialogVisible = ref(false)
const loading = ref(false)
const activeTab = ref('all')
const dingInfo = ref({})
const receiptList = ref([])

const totalCount = computed(() => receiptList.value.length)
const readCount = computed(() => receiptList.value.filter(item => item.isRead).length)
const unreadCount = computed(() => receiptList.value.filter(item => !item.isRead).length)

const filteredReceipts = computed(() => {
  if (activeTab.value === 'all') {
    return receiptList.value
  } else if (activeTab.value === 'read') {
    return receiptList.value.filter(item => item.isRead)
  } else {
    return receiptList.value.filter(item => !item.isRead)
  }
})

const loadDingInfo = async () => {
  try {
    const response = await fetch(`/api/im/ding/${props.dingId}`, {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      dingInfo.value = data.data || {}
    }
  } catch (error) {
    console.error('加载DING信息失败:', error)
  }
}

const loadReceiptList = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/im/ding/${props.dingId}/receipt`, {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      receiptList.value = data.data || []
    }
  } catch (error) {
    console.error('加载已读回执失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRemind = async () => {
  try {
    await ElMessageBox.confirm(`确定要提醒${unreadCount.value}位未读人员吗？`, '提醒确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const unreadUsers = receiptList.value.filter(item => !item.isRead).map(item => item.userId)

    const response = await fetch(`/api/im/ding/${props.dingId}/remind`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken(),
      },
      body: JSON.stringify({ userIds: unreadUsers }),
    })
    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('提醒发送成功')
    } else {
      ElMessage.error(data.msg || '提醒发送失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提醒失败:', error)
      ElMessage.error('提醒发送失败')
    }
  }
}

const formatTime = time => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:visible', false)
}

watch(
  () => props.visible,
  visible => {
    dialogVisible.value = visible
    if (visible && props.dingId) {
      loadDingInfo()
      loadReceiptList()
    }
  }
)

watch(dialogVisible, val => {
  if (!val) {
    emit('update:visible', false)
  }
})
</script>

<style lang="scss" scoped>
.ding-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.ding-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.6;
}

.ding-time {
  font-size: 12px;
  color: #999;
}

.receipt-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 0 16px;
  }

  .tab-label {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .tab-badge {
    :deep(.el-badge__content) {
      transform: translateY(-2px) scale(0.8);
    }
  }
}

.receipt-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px 0;
}

.receipt-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }

  &:last-child {
    border-bottom: none;
  }
}

.receipt-info {
  flex: 1;
  margin-left: 12px;
}

.receipt-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.receipt-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.read-time {
  font-size: 12px;
  color: #999;
}
</style>
