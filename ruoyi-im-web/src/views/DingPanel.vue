<template>
  <div class="ding-panel">
    <!-- 头部 -->
    <div class="ding-header">
      <h2>DING 消息</h2>
      <el-button type="primary" size="small" @click="showSendDialog = true">
        发起 DING
      </el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="ding-tabs">
      <el-tab-pane label="收到的 DING" name="received">
        <div class="ding-list custom-scrollbar">
          <div v-if="receivedDings.length === 0" class="empty-state">
            <el-icon :size="48"><Bell /></el-icon>
            <span>暂无收到的 DING</span>
          </div>
          <div
            v-for="ding in receivedDings"
            :key="ding.id"
            :class="['ding-card', { 'is-urgent': ding.priority === 'URGENT' }]"
          >
            <div class="ding-card-header">
              <div class="sender-info">
                <DingtalkAvatar :src="ding.senderAvatar" :name="ding.senderName" :size="36" shape="square" />
                <div class="sender-detail">
                  <span class="sender-name">{{ ding.senderName }}</span>
                  <span class="send-time">{{ formatTime(ding.sendTime) }}</span>
                </div>
              </div>
              <el-tag v-if="ding.priority === 'URGENT'" type="danger" size="small">紧急</el-tag>
              <el-tag v-else type="info" size="small">普通</el-tag>
            </div>
            <div class="ding-content">{{ ding.content }}</div>
            <div class="ding-stats">
              <span><el-icon><View /></el-icon> 已读 {{ ding.readCount || 0 }}/{{ ding.sendCount || 0 }}</span>
              <span><el-icon><SuccessFilled /></el-icon> 确认 {{ ding.confirmedCount || 0 }}</span>
            </div>
            <div class="ding-actions">
              <el-button v-if="!ding.isRead" type="primary" size="small" @click="handleRead(ding)">
                标为已读
              </el-button>
              <el-button type="success" size="small" @click="handleConfirm(ding)">
                确认收到
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="发出的 DING" name="sent">
        <div class="ding-list custom-scrollbar">
          <div v-if="sentDings.length === 0" class="empty-state">
            <el-icon :size="48"><Promotion /></el-icon>
            <span>暂无发出的 DING</span>
          </div>
          <div
            v-for="ding in sentDings"
            :key="ding.id"
            :class="['ding-card', { 'is-urgent': ding.priority === 'URGENT' }]"
          >
            <div class="ding-card-header">
              <div class="ding-type">
                <el-tag v-if="ding.dingType === 'APP'" size="small">应用内</el-tag>
                <el-tag v-else-if="ding.dingType === 'SMS'" size="small">短信</el-tag>
                <el-tag v-else-if="ding.dingType === 'CALL'" size="small">电话</el-tag>
                <el-tag v-if="ding.priority === 'URGENT'" type="danger" size="small">紧急</el-tag>
              </div>
              <span class="send-time">{{ formatTime(ding.sendTime) }}</span>
            </div>
            <div class="ding-content">{{ ding.content }}</div>
            <div class="ding-stats">
              <span><el-icon><View /></el-icon> 已读 {{ ding.readCount || 0 }}/{{ ding.sendCount || 0 }}</span>
              <span><el-icon><SuccessFilled /></el-icon> 确认 {{ ding.confirmedCount || 0 }}</span>
              <span :class="['status', ding.status === 'ACTIVE' ? 'is-active' : 'is-expired']">
                {{ ding.status === 'ACTIVE' ? '进行中' : ding.status === 'EXPIRED' ? '已过期' : '已取消' }}
              </span>
            </div>
            <div class="ding-actions">
              <el-button type="info" size="small" @click="viewReceipts(ding)">
                查看回执
              </el-button>
              <el-button
                v-if="ding.status === 'ACTIVE'"
                type="danger"
                plain
                size="small"
                @click="handleCancel(ding)"
              >
                取消 DING
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 发送 DING 弹窗 -->
    <el-dialog v-model="showSendDialog" title="发起 DING" width="500px">
      <el-form :model="dingForm" label-width="80px">
        <el-form-item label="DING 类型">
          <el-radio-group v-model="dingForm.dingType">
            <el-radio label="APP">应用内提醒</el-radio>
            <el-radio label="SMS">短信提醒</el-radio>
            <el-radio label="CALL">电话提醒</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="dingForm.priority">
            <el-radio label="URGENT">紧急</el-radio>
            <el-radio label="NORMAL">普通</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会话">
          <el-select v-model="dingForm.conversationId" placeholder="选择会话" style="width: 100%">
            <el-option
              v-for="session in sessions"
              :key="session.id"
              :label="session.name"
              :value="session.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dingForm.content" type="textarea" rows="3" placeholder="请输入 DING 内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSendDing">发送</el-button>
      </template>
    </el-dialog>

    <!-- 回执弹窗 -->
    <el-dialog v-model="showReceiptDialog" title="DING 回执" width="500px">
      <div v-if="receipts.length === 0" class="empty-state">
        <span>暂无回执信息</span>
      </div>
      <div v-else class="receipt-list">
        <div v-for="receipt in receipts" :key="receipt.userId" class="receipt-item">
          <DingtalkAvatar :src="receipt.userAvatar" :name="receipt.userName" :size="32" shape="square" />
          <span class="receipt-name">{{ receipt.userName }}</span>
          <el-tag v-if="receipt.status === 'READ'" type="success" size="small">已读</el-tag>
          <el-tag v-else-if="receipt.status === 'CONFIRMED'" type="primary" size="small">已确认</el-tag>
          <el-tag v-else type="info" size="small">未读</el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Bell, Promotion, View, SuccessFilled
} from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import {
  getReceivedDings,
  getSentDings,
  markDingAsRead,
  confirmDing,
  getDingReceipts,
  cancelDing,
  sendDing
} from '@/api/im/ding'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const store = useStore()

const activeTab = ref('received')
const showSendDialog = ref(false)
const showReceiptDialog = ref(false)

const receivedDings = ref([])
const sentDings = ref([])
const receipts = ref([])
const dingForm = ref({
  dingType: 'APP',
  priority: 'NORMAL',
  conversationId: null,
  content: ''
})

const sessions = computed(() => store.state.im?.session?.sessions || [])

// 加载收到的 DING
const loadReceivedDings = async () => {
  try {
    const res = await getReceivedDings()
    if (res.code === 200) {
      receivedDings.value = res.data || []
    }
  } catch (e) {
    console.error('加载收到的 DING 失败', e)
    ElMessage.error('加载收到的 DING 失败')
  }
}

// 加载发出的 DING
const loadSentDings = async () => {
  try {
    const res = await getSentDings()
    if (res.code === 200) {
      sentDings.value = res.data || []
    }
  } catch (e) {
    console.error('加载发出的 DING 失败', e)
    ElMessage.error('加载发出的 DING 失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

// 标为已读
const handleRead = async (ding) => {
  try {
    const res = await markDingAsRead(ding.id)
    if (res.code === 200) {
      ding.isRead = true
      ElMessage.success('已标记为已读')
    }
  } catch (e) {
    console.error('标记已读失败', e)
    ElMessage.error('标记已读失败')
  }
}

// 确认 DING
const handleConfirm = async (ding) => {
  try {
    const res = await confirmDing(ding.id)
    if (res.code === 200) {
      ding.isRead = true
      ElMessage.success('已确认收到')
    }
  } catch (e) {
    console.error('确认失败', e)
    ElMessage.error('确认失败')
  }
}

// 查看回执
const viewReceipts = async (ding) => {
  try {
    const res = await getDingReceipts(ding.id)
    if (res.code === 200) {
      receipts.value = res.data || []
      showReceiptDialog.value = true
    }
  } catch (e) {
    console.error('获取回执失败', e)
    ElMessage.error('获取回执失败')
  }
}

// 取消 DING
const handleCancel = async (ding) => {
  try {
    const res = await cancelDing(ding.id)
    if (res.code === 200) {
      ding.status = 'CANCELLED'
      ElMessage.success('已取消 DING')
    }
  } catch (e) {
    console.error('取消失败', e)
    ElMessage.error('取消 DING 失败')
  }
}

// 发送 DING
const handleSendDing = async () => {
  if (!dingForm.value.conversationId || !dingForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await sendDing(dingForm.value)
    if (res.code === 200) {
      ElMessage.success('DING 发送成功')
      showSendDialog.value = false
      dingForm.value = { dingType: 'APP', priority: 'NORMAL', conversationId: null, content: '' }
      loadSentDings()
    }
  } catch (e) {
    console.error('发送失败', e)
    ElMessage.error('DING 发送失败')
  }
}

onMounted(() => {
  loadReceivedDings()
  loadSentDings()
})
</script>

<style lang="scss" scoped>
.ding-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
}

.ding-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }
}

.ding-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.el-tabs__content) {
    flex: 1;
    overflow: hidden;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}

.ding-list {
  height: 100%;
  overflow-y: auto;
  padding: 16px 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  gap: 12px;

  .el-icon {
    opacity: 0.5;
  }
}

.ding-card {
  background-color: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  padding: 16px;
  margin-bottom: 12px;
  border-left: 3px solid var(--dt-border-light);

  &.is-urgent {
    border-left-color: var(--dt-error-color);
  }
}

.ding-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .sender-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .sender-detail {
      display: flex;
      flex-direction: column;

      .sender-name {
        font-size: 14px;
        color: var(--dt-text-primary);
        font-weight: 500;
      }

      .send-time {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }

  .ding-type {
    display: flex;
    gap: 8px;
  }

  .send-time {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.ding-content {
  font-size: 14px;
  color: var(--dt-text-primary);
  line-height: 1.5;
  margin-bottom: 12px;
}

.ding-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 12px;

  .el-icon {
    margin-right: 4px;
  }

  .status {
    &.is-active {
      color: var(--dt-success-color);
    }
    &.is-expired {
      color: var(--dt-text-tertiary);
    }
  }
}

.ding-actions {
  display: flex;
  gap: 8px;
}

.receipt-list {
  .receipt-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    border-bottom: 1px solid var(--dt-border-light);

    &:last-child {
      border-bottom: none;
    }

    .receipt-name {
      flex: 1;
      font-size: 14px;
      color: var(--dt-text-primary);
    }
  }
}
</style>
