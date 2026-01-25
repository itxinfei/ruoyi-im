<template>
  <div class="ding-message-bubble" :class="`ding-${dingType}`">
    <div class="ding-header">
      <div class="ding-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          <circle cx="12" cy="12" r="3" fill="white"/>
        </svg>
      </div>
      <div class="ding-title">
        <span class="ding-label">DING</span>
        <span v-if="priority === 'URGENT'" class="priority-urgent">紧急</span>
        <span v-else class="priority-normal">普通</span>
        <span class="ding-type-label">{{ dingTypeLabel }}</span>
      </div>
    </div>

    <div class="ding-content">
      {{ content }}
    </div>

    <div class="ding-footer">
      <div class="read-status">
        <span class="read-count">已读 {{ readCount }}/{{ sendCount }} 人</span>
        <el-progress
          :percentage="readPercentage"
          :show-text="false"
          :stroke-width="4"
          :color="priority === 'URGENT' ? '#f56c6c' : '#409eff'"
        />
      </div>
      <div class="ding-actions">
        <el-button
          v-if="!isRead && !isSender"
          type="primary"
          size="small"
          @click="handleMarkAsRead"
        >
          标记已读
        </el-button>
        <el-button
          v-if="isSender"
          type="danger"
          size="small"
          plain
          @click="handleCancel"
        >
          取消DING
        </el-button>
        <el-button
          size="small"
          @click="handleViewDetail"
        >
          查看详情
        </el-button>
      </div>
    </div>

    <div v-if="expireTime" class="ding-expire">
      过期时间：{{ formatTime(expireTime) }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { markDingAsRead, cancelDing } from '@/api/im/ding'
import dayjs from 'dayjs'

const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  content: {
    type: String,
    default: ''
  },
  dingType: {
    type: String,
    default: 'APP'
  },
  priority: {
    type: String,
    default: 'NORMAL'
  },
  readCount: {
    type: Number,
    default: 0
  },
  sendCount: {
    type: Number,
    default: 0
  },
  isRead: {
    type: Boolean,
    default: false
  },
  isSender: {
    type: Boolean,
    default: false
  },
  expireTime: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'ACTIVE'
  }
})

const emit = defineEmits(['read', 'cancel', 'detail'])

const dingTypeLabel = computed(() => {
  const typeMap = {
    'APP': '应用内提醒',
    'SMS': '短信提醒',
    'CALL': '电话提醒'
  }
  return typeMap[props.dingType] || '应用内提醒'
})

const readPercentage = computed(() => {
  if (props.sendCount === 0) return 0
  return Math.round((props.readCount / props.sendCount) * 100)
})

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleMarkAsRead = async () => {
  try {
    await markDingAsRead(props.id)
    ElMessage.success('已标记为已读')
    emit('read', props.id)
  } catch (error) {
    ElMessage.error('标记失败')
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此DING消息吗？', '确认', {
      type: 'warning'
    })
    await cancelDing(props.id)
    ElMessage.success('DING已取消')
    emit('cancel', props.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleViewDetail = () => {
  emit('detail', props.id)
}
</script>

<style lang="scss" scoped>
.ding-message-bubble {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border: 1px solid #ffd6d6;
  border-radius: 8px;
  padding: 12px;
  min-width: 280px;
  max-width: 400px;

  &.ding-APP {
    background: linear-gradient(135deg, #e8f4ff 0%, #fff 100%);
    border-color: #b3d8ff;
  }

  &.ding-SMS {
    background: linear-gradient(135deg, #fff7e6 0%, #fff 100%);
    border-color: #ffd591;
  }

  &.ding-CALL {
    background: linear-gradient(135deg, #fff1f0 0%, #fff 100%);
    border-color: #ffccc7;
  }

  .ding-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .ding-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      svg {
        width: 18px;
        height: 18px;
      }
    }

    .ding-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 500;

      .ding-label {
        font-weight: 700;
        font-size: 16px;
        color: #ee5a6f;
      }

      .priority-urgent {
        padding: 2px 8px;
        background: #fef0f0;
        color: #f56c6c;
        border-radius: 4px;
        font-size: 12px;
      }

      .priority-normal {
        padding: 2px 8px;
        background: #f0f9ff;
        color: #409eff;
        border-radius: 4px;
        font-size: 12px;
      }

      .ding-type-label {
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .ding-content {
    color: #303133;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 12px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .ding-footer {
    .read-status {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .read-count {
        font-size: 12px;
        color: #909399;
        min-width: 100px;
      }

      .el-progress {
        flex: 1;
      }
    }

    .ding-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .ding-expire {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
