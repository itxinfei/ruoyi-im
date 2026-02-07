<template>
  <el-dialog
    v-model="visible"
    title="定时发送"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="schedule-message-content">
      <!-- 消息预览 -->
      <div class="message-preview">
        <div class="preview-header">
          消息预览
        </div>
        <div class="preview-content">
          {{ messagePreview }}
        </div>
      </div>

      <!-- 定时选择 -->
      <div class="schedule-section">
        <div class="section-title">
          <span class="material-icons-outlined">schedule</span>
          发送时间
        </div>

        <!-- 快捷时间选项 -->
        <div class="quick-time-options">
          <div
            v-for="option in quickTimeOptions"
            :key="option.value"
            class="time-option"
            :class="{ active: selectedTimeOption === option.value }"
            @click="selectQuickTime(option)"
          >
            <span class="option-icon material-icons-outlined">{{ option.icon }}</span>
            <span class="option-label">{{ option.label }}</span>
            <span class="option-time">{{ option.time }}</span>
          </div>
        </div>

        <!-- 自定义时间 -->
        <el-date-picker
          v-model="customTime"
          type="datetime"
          placeholder="或选择自定义时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm"
          :disabled-date="disableDate"
          style="width: 100%; margin-top: 12px"
        />
      </div>

      <!-- 提示信息 -->
      <div class="schedule-tips">
        <span class="material-icons-outlined tips-icon">info</span>
        <span>定时发送后，消息将在指定时间自动发送</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">
        取消
      </el-button>
      <el-button
        type="primary"
        :disabled="!canSchedule"
        :loading="scheduling"
        @click="handleSchedule"
      >
        {{ scheduling ? '设置中...' : '确定' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleMessage } from '@/api/im/message'
import { isFeatureEnabled, FeatureFlags } from '@/config/featureFlags'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  messageContent: { type: String, default: '' },
  conversationId: { type: [String, Number], default: null },
  messageType: { type: String, default: 'TEXT' },
  // 功能开关：定时消息是否启用
  featureEnabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'scheduled'])

const visible = ref(false)
const scheduling = ref(false)
const customTime = ref('')
const selectedTimeOption = ref('')

// 快捷时间选项
const quickTimeOptions = [
  { value: '1h', label: '1小时后', icon: 'schedule', minutes: 60 },
  { value: '2h', label: '2小时后', icon: 'schedule', minutes: 120 },
  { value: 'tomorrow', label: '明天上午9点', icon: 'wb_sunny', minutes: 1440 + 540 },
  { value: 'monday', label: '下周9点', icon: 'event', minutes: null }
]

// 计算快捷时间的显示文本
const getQuickTimeText = option => {
  const now = new Date()
  let targetTime

  switch (option.value) {
    case '1h':
      targetTime = new Date(now.getTime() + 60 * 60 * 1000)
      break
    case '2h':
      targetTime = new Date(now.getTime() + 2 * 60 * 60 * 1000)
      break
    case 'tomorrow':
      targetTime = new Date(now)
      targetTime.setDate(targetTime.getDate() + 1)
      targetTime.setHours(9, 0, 0, 0)
      break
    case 'monday': {
      targetTime = new Date(now)
      const day = targetTime.getDay()
      const diff = 7 - (day || 7)
      targetTime.setDate(targetTime.getDate() + diff)
      targetTime.setHours(9, 0, 0, 0)
      break
    }
  }

  return formatDate(targetTime)
}

// 格式化日期时间
const formatDate = date => {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 更新快捷时间选项的显示时间
quickTimeOptions.forEach(option => {
  option.time = getQuickTimeText(option)
})

// 消息预览
const messagePreview = computed(() => {
  const content = props.messageContent
  if (!content) {return '暂无消息内容'}
  return content.length > 50 ? content.substring(0, 50) + '...' : content
})

// 是否可以设置定时发送
const canSchedule = computed(() => {
  return selectedTimeOption.value || customTime.value
})

// 禁用过去的日期
const disableDate = time => {
  return time.getTime() < Date.now() - 60000 // 至少1分钟后
}

// 选择快捷时间
const selectQuickTime = option => {
  selectedTimeOption.value = option.value
  customTime.value = getQuickTimeText(option)
}

// 设置定时发送
const handleSchedule = async () => {
  if (!canSchedule.value) {return}

  // 检查功能开关
  if (!props.featureEnabled) {
    ElMessage.warning('定时消息功能暂未开放，敬请期待')
    return
  }

  const scheduledTime = customTime.value

  // 校验会话ID
  if (!props.conversationId) {
    ElMessage.warning('无法获取会话信息，请重试')
    return
  }

  try {
    scheduling.value = true

    // 调用后端 API 创建定时消息
    await scheduleMessage({
      conversationId: props.conversationId,
      messageType: props.messageType || 'TEXT',
      content: props.messageContent,
      scheduledTime: scheduledTime
    })

    ElMessage.success(`消息已设置在 ${scheduledTime} 发送`)
    emit('scheduled', { scheduledTime })
    handleClose()
  } catch (error) {
    // API 可能尚未实现，降级处理
    console.warn('[ScheduledMessage] API 调用失败，可能后端尚未实现:', error)
    const errorMsg = error?.msg || error?.message
    const statusCode = error?.response?.status

    if (statusCode === 404) {
      ElMessage.warning('定时消息功能开发中，敬请期待')
    } else if (errorMsg) {
      ElMessage.error('设置失败: ' + errorMsg)
    } else {
      ElMessage.error('设置定时发送失败，请稍后重试')
    }
  } finally {
    scheduling.value = false
  }
}

const handleClose = () => {
  selectedTimeOption.value = ''
  customTime.value = ''
  visible.value = false
  emit('update:modelValue', false)
}

watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/z-index.scss' as *;

// 设置定时消息弹窗的 z-index
:deep(.el-dialog) {
  z-index: $z-modal-above;
}

:deep(.el-overlay) {
  z-index: $z-modal-above - 1;
}

.schedule-message-content {
  .message-preview {
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-md);
    padding: 12px;
    margin-bottom: 20px;

    .preview-header {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-bottom: 8px;
    }

    .preview-content {
      font-size: 13px;
      color: var(--dt-text-primary);
      line-height: 1.5;
      min-height: 40px;
    }
  }

  .schedule-section {
    .section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin-bottom: 12px;

      .material-icons-outlined {
        font-size: 16px;
        color: var(--dt-brand-color);
      }
    }

    .quick-time-options {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      margin-bottom: 12px;
    }

    .time-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      padding: 12px 8px;
      background: var(--dt-bg-body);
      border: 1px solid var(--dt-border-light);
      border-radius: var(--dt-radius-md);
      cursor: pointer;
      transition: all var(--dt-transition-fast);

      &:hover {
        background: var(--dt-brand-bg);
        border-color: var(--dt-brand-color);
      }

      &.active {
        background: var(--dt-brand-bg);
        border-color: var(--dt-brand-color);

        .option-icon {
          color: var(--dt-brand-color);
        }

        .option-label {
          color: var(--dt-brand-color);
          font-weight: 500;
        }
      }

      .option-icon {
        font-size: 20px;
        color: var(--dt-text-secondary);
        margin-bottom: 2px;
      }

      .option-label {
        font-size: 13px;
        color: var(--dt-text-primary);
      }

      .option-time {
        font-size: 11px;
        color: var(--dt-text-quaternary);
      }
    }
  }

  .schedule-tips {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 12px;
    background: var(--dt-info-bg);
    border-radius: var(--dt-radius-md);
    margin-top: 16px;

    .tips-icon {
      font-size: 16px;
      color: var(--dt-info-color);
      flex-shrink: 0;
      margin-top: 1px;
    }

    span {
      font-size: 12px;
      color: var(--dt-text-secondary);
      line-height: 1.5;
    }
  }
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-date-picker) {
  width: 100%;
}
</style>
