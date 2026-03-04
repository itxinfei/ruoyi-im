<template>
  <el-dialog
    v-model="visible"
    title="消息已读详情"
    width="480px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="read-detail-container">
      <!-- 消息预览 -->
      <div class="message-preview">
        <div class="preview-label">消息内容</div>
        <div class="preview-content">{{ messagePreview }}</div>
        <div class="preview-meta">
          <span>发送时间：{{ sendTime }}</span>
          <span class="read-status" :class="readPercent === 100 ? 'all-read' : ''">
            已读：{{ readCount }}/{{ totalCount }}
            <span v-if="totalCount > 0">{{ readPercent }}%</span>
          </span>
        </div>
      </div>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" class="read-tabs">
        <!-- 已读列表 -->
        <el-tab-pane :label="`已读 (${readCount})`" name="read">
          <div v-if="readUsers.length > 0" class="user-list">
            <div
              v-for="user in readUsers"
              :key="user.userId"
              class="user-item"
            >
              <img :src="user.avatar || '/default-avatar.png'" class="user-avatar" @error="handleAvatarError" />
              <div class="user-info">
                <div class="user-name">{{ user.userName }}</div>
                <div class="read-time">{{ formatReadTime(user.readTime) }}</div>
              </div>
              <el-tag v-if="user.userId === senderId" size="small" type="info">发送者</el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无已读" :image-size="80" />
        </el-tab-pane>

        <!-- 未读列表 -->
        <el-tab-pane :label="`未读 (${unreadCount})`" name="unread">
          <div v-if="unreadUsers.length > 0" class="user-list">
            <div
              v-for="user in unreadUsers"
              :key="user.userId"
              class="user-item"
            >
              <img :src="user.avatar || '/default-avatar.png'" class="user-avatar" @error="handleAvatarError" />
              <div class="user-info">
                <div class="user-name">{{ user.userName }}</div>
                <div class="user-status">尚未阅读</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="全部已读" :image-size="80" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getMessageReadDetail } from '../../api/im/message'

const props = defineProps({
  modelValue: Boolean,
  messageId: Number,
  conversationId: Number,
  senderId: Number
})

const emit = defineEmits(['update:modelValue', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('read')

// 消息预览信息
const messagePreview = ref('')
const sendTime = ref('')
const totalCount = ref(0)
const readCount = ref(0)
const unreadCount = ref(0)
const readUsers = ref([])
const unreadUsers = ref([])

// 已读百分比
const readPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((readCount.value / totalCount.value) * 100)
})

// 加载已读详情
const loadReadDetail = async () => {
  if (!props.messageId) return

  try {
    const res = await getMessageReadDetail(props.messageId)
    if (res.code === 200 && res.data) {
      const data = res.data
      messagePreview.value = data.messagePreview || ''
      sendTime.value = data.sendTime ? dayjs(data.sendTime).format('YYYY-MM-DD HH:mm') : ''
      totalCount.value = data.totalCount || 0
      readCount.value = data.readCount || 0
      unreadCount.value = data.unreadCount || 0
      readUsers.value = data.readUsers || []
      unreadUsers.value = data.unreadUsers || []
    }
  } catch (e) {
    console.error('获取已读详情失败:', e)
    ElMessage.error('获取已读详情失败')
  }
}

// 格式化已读时间
const formatReadTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const t = dayjs(time)

  // 今天
  if (now.isSame(t, 'day')) {
    return `今天 ${t.format('HH:mm')}`
  }

  // 昨天
  if (now.subtract(1, 'day').isSame(t, 'day')) {
    return `昨天 ${t.format('HH:mm')}`
  }

  // 7 天内
  if (now.diff(t, 'day') < 7) {
    return t.format('dddd HH:mm')
  }

  return t.format('YYYY-MM-DD HH:mm')
}

// 头像加载错误处理
const handleAvatarError = (e) => {
  e.target.src = '/default-avatar.png'
}

// 监听打开
watch(() => visible.value, (val) => {
  if (val && props.messageId) {
    activeTab.value = 'read'
    loadReadDetail()
  }
})

// 监听 messageId 变化
watch(() => props.messageId, (val) => {
  if (visible.value && val) {
    loadReadDetail()
  }
})
</script>

<style scoped lang="scss">
.read-detail-container {
  min-height: 300px;
}

.message-preview {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;

  .preview-label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 6px;
  }

  .preview-content {
    font-size: 14px;
    color: #303133;
    line-height: 1.5;
    margin-bottom: 8px;
    max-height: 80px;
    overflow-y: auto;
  }

  .preview-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;

    .read-status {
      color: #f56c6c;
      font-weight: 500;

      &.all-read {
        color: #67c23a;
      }
    }
  }
}

.read-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
  }
}

.user-list {
  max-height: 280px;
  overflow-y: auto;

  .user-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
      margin-right: 12px;
    }

    .user-info {
      flex: 1;

      .user-name {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        margin-bottom: 4px;
      }

      .read-time, .user-status {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
