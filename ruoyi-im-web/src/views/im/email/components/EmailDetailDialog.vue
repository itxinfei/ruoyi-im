<!--
  邮件详情对话框
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="email?.subject || '(无主题)'"
    :width="800"
    destroy-on-close
  >
    <div v-if="email" class="email-detail">
      <!-- 邮件头部 -->
      <div class="email-header">
        <div class="header-row">
          <span class="label">发件人:</span>
          <span class="value">{{ email.senderName || email.senderEmail }}</span>
          <span class="email-address">&lt;{{ email.senderEmail }}&gt;</span>
        </div>
        <div class="header-row">
          <span class="label">收件人:</span>
          <span class="value">{{ currentUser?.nickname || currentUser?.username }}</span>
        </div>
        <div class="header-row">
          <span class="label">时间:</span>
          <span class="value">{{ formatTime(email.sendTime || email.createTime) }}</span>
        </div>
      </div>

      <!-- 分隔线 -->
      <el-divider />

      <!-- 邮件内容 -->
      <div class="email-content">
        <div v-html="email.htmlContent || email.textContent || ''"></div>
      </div>

      <!-- 附件 -->
      <div v-if="email.attachmentCount > 0" class="email-attachments">
        <div class="attachments-title">
          <i class="el-icon-paperclip"></i> 附件 ({{ email.attachmentCount }})
        </div>
        <div class="attachments-list">
          <!-- 这里应该从附件API获取附件列表 -->
          <div class="attachment-item">
            <i class="el-icon-document"></i>
            <span>附件文件</span>
            <el-button type="text" size="small">下载</el-button>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleReply">
        <i class="el-icon-back"></i> 回复
      </el-button>
      <el-button @click="handleForward">
        <i class="el-icon-right"></i> 转发
      </el-button>
      <el-button @click="handleDelete" type="danger" plain>
        <i class="el-icon-delete"></i> 删除
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as emailApi from '@/api/im/email'

const props = defineProps({
  visible: Boolean,
  emailId: Number,
})

const emit = defineEmits(['update:visible', 'reply', 'forward'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})

const email = ref(null)
const currentUser = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

watch(() => props.emailId, (id) => {
  if (id && props.visible) {
    loadEmailDetail(id)
  }
}, { immediate: true })

watch(dialogVisible, (val) => {
  if (val && props.emailId) {
    loadEmailDetail(props.emailId)
  }
})

const loadEmailDetail = async (id) => {
  try {
    const response = await emailApi.getEmailDetail(id)
    if (response.code === 200) {
      email.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载邮件详情失败')
  }
}

const handleReply = () => {
  emit('reply', email.value)
}

const handleForward = () => {
  emit('forward', email.value)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除此邮件吗？', '提示', {
      type: 'warning'
    })
    await emailApi.moveToTrash(props.emailId)
    ElMessage.success('已移至垃圾箱')
    dialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style lang="scss" scoped>
.email-detail {
  .email-header {
    .header-row {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      .label {
        min-width: 60px;
        color: #999;
        font-size: 14px;
      }

      .value {
        color: #333;
        font-size: 14px;
      }

      .email-address {
        color: #999;
        font-size: 13px;
        margin-left: 8px;
      }
    }
  }

  .email-content {
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
    font-size: 14px;
    line-height: 1.8;

    :deep(img) {
      max-width: 100%;
    }
  }

  .email-attachments {
    margin-top: 16px;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 4px;

    .attachments-title {
      font-size: 14px;
      color: #333;
      margin-bottom: 8px;
    }

    .attachments-list {
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        background: #fff;
        border-radius: 4px;
        margin-bottom: 4px;

        i {
          color: #409eff;
        }
      }
    }
  }
}
</style>
