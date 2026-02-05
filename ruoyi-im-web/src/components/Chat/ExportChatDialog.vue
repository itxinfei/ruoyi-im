<template>
  <el-dialog
    v-model="visible"
    title="导出聊天记录"
    width="500px"
    :close-on-click-modal="false"
    class="export-chat-dialog"
    @open="handleOpen"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
    >
      <!-- 导出格式 -->
      <el-form-item
        label="导出格式"
        prop="format"
      >
        <el-radio-group v-model="form.format">
          <el-radio-button label="txt">
            <div class="format-option">
              <span class="material-icons-outlined">description</span>
              <span>文本文件</span>
            </div>
          </el-radio-button>
          <el-radio-button label="html">
            <div class="format-option">
              <span class="material-icons-outlined">code</span>
              <span>网页文件</span>
            </div>
          </el-radio-button>
          <el-radio-button
            label="pdf"
            :disabled="true"
          >
            <div class="format-option">
              <span class="material-icons-outlined">picture_as_pdf</span>
              <span>PDF文件</span>
              <el-tag
                size="small"
                type="info"
                style="margin-left: 4px"
              >
                暂不支持
              </el-tag>
            </div>
          </el-radio-button>
        </el-radio-group>
        <div class="format-hint">
          {{ formatHints[form.format] }}
        </div>
      </el-form-item>

      <!-- 时间范围 -->
      <el-form-item label="时间范围">
        <el-radio-group
          v-model="form.timeRange"
          @change="handleTimeRangeChange"
        >
          <el-radio-button label="all">
            全部消息
          </el-radio-button>
          <el-radio-button label="custom">
            自定义
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 自定义时间 -->
      <el-form-item
        v-if="form.timeRange === 'custom'"
        label="选择时间"
        prop="dateRange"
      >
        <el-date-picker
          v-model="form.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :disabled-date="disabledDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 导出预览 -->
      <el-form-item label="导出预览">
        <div class="export-preview">
          <div class="preview-stat">
            <span class="material-icons-outlined stat-icon">chat_bubble_outline</span>
            <div class="stat-content">
              <span class="stat-value">{{ messageCount }}</span>
              <span class="stat-label">条消息</span>
            </div>
          </div>
          <div class="preview-stat">
            <span class="material-icons-outlined stat-icon">calendar_today</span>
            <div class="stat-content">
              <span class="stat-value">{{ dateRangeCount }}</span>
              <span class="stat-label">天</span>
            </div>
          </div>
          <div class="preview-stat">
            <span class="material-icons-outlined stat-icon">data_usage</span>
            <div class="stat-content">
              <span class="stat-value">{{ formatFileSize(estimatedSize) }}</span>
              <span class="stat-label">预计大小</span>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="exporting"
        @click="handleExport"
      >
        <el-icon><Download /></el-icon>
        {{ exporting ? '导出中...' : '开始导出' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { exportChat, EXPORT_FORMAT, generateExportFileName } from '@/utils/chatExport'
import { useStore } from 'vuex'
import { formatFileSize } from '@/utils/format'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  messages: {
    type: Array,
    default: () => []
  },
  contactName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

const formRef = ref(null)
const exporting = ref(false)

const form = ref({
  format: 'html',
  timeRange: 'all',
  dateRange: null
})

const rules = {
  format: [{ required: true, message: '请选择导出格式', trigger: 'change' }],
  dateRange: [
    {
      validator: (rule, value, callback) => {
        if (form.value.timeRange === 'custom' && !value) {
          callback(new Error('请选择时间范围'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const formatHints = {
  txt: '纯文本格式，兼容性好，适合保存和分享',
  html: '网页格式，保留样式和排版，适合在浏览器中查看',
  pdf: 'PDF 文档，格式固定，适合打印和归档（开发中）'
}

// 过滤后的消息列表
const filteredMessages = computed(() => {
  let messages = props.messages || []

  if (form.value.timeRange === 'custom' && form.value.dateRange) {
    const [start, end] = form.value.dateRange
    const startTime = new Date(start).getTime()
    const endTime = new Date(end).getTime()

    messages = messages.filter(msg => {
      const msgTime = new Date(msg.timestamp || msg.sendTime || msg.createTime).getTime()
      return msgTime >= startTime && msgTime <= endTime
    })
  }

  return messages
})

// 消息数量
const messageCount = computed(() => filteredMessages.value.length)

// 日期范围天数
const dateRangeCount = computed(() => {
  if (filteredMessages.value.length === 0) {return 0}

  const timestamps = filteredMessages.value
    .map(msg => new Date(msg.timestamp || msg.sendTime || msg.createTime).getTime())
    .sort((a, b) => a - b)

  if (timestamps.length < 2) {return 1}

  const days = Math.ceil((timestamps[timestamps.length - 1] - timestamps[0]) / (1000 * 60 * 60 * 24))
  return Math.max(1, days)
})

// 预估文件大小
const estimatedSize = computed(() => {
  let size = 0
  filteredMessages.value.forEach(msg => {
    if (msg.type === 'TEXT') {
      size += (msg.content?.length || 0) * 2 // UTF-16
    } else {
      size += 100 // 其他类型的消息预估
    }
    size += 50 // 消息元数据
  })

  if (form.value.format === 'html') {
    size *= 3 // HTML 格式会更大
  }

  return size
})

// 禁用日期
const disabledDate = time => {
  return time.getTime() > Date.now()
}

// 时间范围变化
const handleTimeRangeChange = () => {
  if (form.value.timeRange === 'all') {
    form.value.dateRange = null
  }
}

// 对话框打开时
const handleOpen = () => {
  form.value = {
    format: 'html',
    timeRange: 'all',
    dateRange: null
  }
}

// 执行导出
const handleExport = async () => {
  await formRef.value?.validate()

  if (filteredMessages.value.length === 0) {
    ElMessage.warning('没有可导出的消息')
    return
  }

  exporting.value = true
  try {
    const fileName = generateExportFileName(props.contactName, form.value.format)
    const startTime = form.value.timeRange === 'custom' ? form.value.dateRange?.[0] : null
    const endTime = form.value.timeRange === 'custom' ? form.value.dateRange?.[1] : null

    // 调用导出函数
    exportChat(filteredMessages.value, {
      format: form.value.format,
      contactName: props.contactName,
      title: `与 ${props.contactName} 的聊天记录`,
      startTime,
      endTime,
      currentUser: currentUser.value
    })

    ElMessage.success(`导出成功: ${fileName}`)
    visible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + error.message)
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped lang="scss">
.export-chat-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.format-option {
  display: flex;
  align-items: center;
  gap: 4px;

  .material-icons-outlined {
    font-size: 16px;
  }
}

.format-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.export-preview {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: var(--dt-radius-md);

  .preview-stat {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;

    .stat-icon {
      font-size: 32px;
      color: var(--el-color-primary);
      opacity: 0.8;
    }

    .stat-content {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        line-height: 1;
      }

      .stat-label {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }
    }
  }
}

:deep(.el-radio-button) {
  .el-radio-button__inner {
    padding: 8px 12px;

    &.is-disabled {
      opacity: 0.6;
    }
  }
}
</style>
