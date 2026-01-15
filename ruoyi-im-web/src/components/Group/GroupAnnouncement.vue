<template>
  <el-dialog
    v-model="visible"
    title="群公告"
    :width="480"
    :modal="true"
    :close-on-click-modal="false"
    class="group-announcement-dialog"
    @close="handleClose"
  >
    <div class="announcement-content">
      <!-- 公告标题 -->
      <div class="announcement-title">{{ announcement.title }}</div>

      <!-- 公告正文 -->
      <div class="announcement-text">{{ announcement.content }}</div>

      <!-- 发布信息 -->
      <div class="announcement-meta">
        <div class="meta-item">
          <span class="meta-label">发布人：</span>
          <span class="meta-value">{{ announcement.publisherName }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">发布时间：</span>
          <span class="meta-value">{{ formatTime(announcement.publishTime) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleConfirm">我知道了</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  // 公告数据
  announcement: {
    type: Object,
    default: () => ({})
  },
  // 群组ID
  groupId: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'read'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  if (diff < 172800000) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 更早
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleConfirm = () => {
  emit('confirm')
  emit('read', { groupId: props.groupId, announcementId: props.announcement.id })
  visible.value = false
}

const handleClose = () => {
  emit('read', { groupId: props.groupId, announcementId: props.announcement.id })
}
</script>

<style lang="scss" scoped>
.group-announcement-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid #E5E8EB;
  }

  :deep(.el-dialog__title) {
    font-size: 16px;
    font-weight: 600;
    color: #171A1A;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #E5E8EB;
  }
}

.announcement-content {
  .announcement-title {
    font-size: 16px;
    font-weight: 600;
    color: #171A1A;
    margin-bottom: 16px;
    line-height: 1.5;
  }

  .announcement-text {
    font-size: 14px;
    color: #5F6468;
    line-height: 1.75;
    margin-bottom: 24px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .announcement-meta {
    padding-top: 16px;
    border-top: 1px solid #F0F2F5;

    .meta-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      font-size: 13px;

      &:last-child {
        margin-bottom: 0;
      }

      .meta-label {
        color: #858B8F;
        min-width: 70px;
      }

      .meta-value {
        color: #5F6468;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;

  .el-button {
    min-width: 100px;
  }
}
</style>
