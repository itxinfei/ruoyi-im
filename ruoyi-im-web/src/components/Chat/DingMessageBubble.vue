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
  background: linear-gradient(135deg, var(--dt-error-bg) 0%, #fff 100%);
  border: 1px solid var(--dt-error-color);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-md);
  min-width: 280px;
  max-width: 400px;

  &.ding-APP {
    background: linear-gradient(135deg, var(--dt-brand-bg) 0%, #fff 100%);
    border-color: var(--dt-brand-light);
  }

  &.ding-SMS {
    background: linear-gradient(135deg, var(--dt-warning-bg) 0%, #fff 100%);
    border-color: var(--dt-warning-color);
  }

  &.ding-CALL {
    background: linear-gradient(135deg, var(--dt-error-bg) 0%, #fff 100%);
    border-color: var(--dt-error-color);
  }

  .ding-header {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    margin-bottom: var(--dt-spacing-md);

    .ding-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, var(--dt-error-color) 0%, var(--dt-error-color) 100%);
      border-radius: var(--dt-radius-md);
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
      gap: var(--dt-spacing-sm);
      font-size: var(--dt-font-size-base);
      font-weight: var(--dt-font-weight-medium);

      .ding-label {
        font-weight: var(--dt-font-weight-bold);
        font-size: var(--dt-font-size-md);
        color: var(--dt-error-color);
      }

      .priority-urgent {
        padding: 2px var(--dt-spacing-sm);
        background: var(--dt-error-bg);
        color: var(--dt-error-color);
        border-radius: var(--dt-radius-sm);
        font-size: var(--dt-font-size-xs);
      }

      .priority-normal {
        padding: 2px var(--dt-spacing-sm);
        background: var(--dt-brand-bg);
        color: var(--dt-brand-color);
        border-radius: var(--dt-radius-sm);
        font-size: var(--dt-font-size-xs);
      }

      .ding-type-label {
        color: var(--dt-text-tertiary);
        font-size: var(--dt-font-size-xs);
      }
    }
  }

  .ding-content {
    color: var(--dt-text-primary);
    font-size: var(--dt-font-size-base);
    line-height: 1.6;
    margin-bottom: var(--dt-spacing-md);
    white-space: pre-wrap;
    word-break: break-word;
  }

  .ding-footer {
    .read-status {
      display: flex;
      align-items: center;
      gap: var(--dt-spacing-md);
      margin-bottom: var(--dt-spacing-md);

      .read-count {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        min-width: 100px;
      }

      .el-progress {
        flex: 1;
      }
    }

    .ding-actions {
      display: flex;
      gap: var(--dt-spacing-sm);
      flex-wrap: wrap;
    }
  }

  .ding-expire {
    margin-top: var(--dt-spacing-sm);
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-tertiary);
  }
}
</style>
