<!--
  写邮件对话框
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    title="写邮件"
    :width="800"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="收件人" prop="toIds">
        <el-select
          v-model="form.toIds"
          multiple
          filterable
          remote
          reserve-keyword
          placeholder="选择收件人"
          :remote-method="searchUsers"
          :loading="userLoading"
          style="width: 100%"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.nickname || user.username"
            :value="user.id"
          >
            <div class="user-option">
              <el-avatar :size="24" :src="user.avatar">{{ user.nickname?.[0] }}</el-avatar>
              <span>{{ user.nickname || user.username }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="主题" prop="subject">
        <el-input v-model="form.subject" placeholder="请输入邮件主题" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <div class="editor-container">
          <div class="editor-toolbar">
            <el-button-group size="small">
              <el-button @click="execCommand('bold')">
                <i class="el-icon-bold"></i>
              </el-button>
              <el-button @click="execCommand('italic')">
                <i class="el-icon-italic"></i>
              </el-button>
              <el-button @click="execCommand('underline')">
                <i class="el-icon-underline"></i>
              </el-button>
            </el-button-group>

            <el-button-group size="small" style="margin-left: 8px">
              <el-button @click="execCommand('insertUnorderedList')">
                <i class="el-icon-s-unfold"></i>
              </el-button>
              <el-button @click="execCommand('insertOrderedList')">
                <i class="el-icon-s-fold"></i>
              </el-button>
            </el-button-group>

            <el-upload
              :show-file-list="false"
              :before-upload="handleAttachUpload"
              style="margin-left: 8px"
            >
              <el-button size="small"> <i class="el-icon-paperclip"></i> 添加附件 </el-button>
            </el-upload>
          </div>

          <div
            ref="editorRef"
            class="editor-content"
            contenteditable="true"
            @input="form.content = $event.target.innerHTML"
            @blur="form.content = $event.target.innerHTML"
          ></div>

          <!-- 附件列表 -->
          <div v-if="attachments.length > 0" class="attachment-list">
            <div v-for="(file, index) in attachments" :key="index" class="attachment-item">
              <i class="el-icon-document"></i>
              <span>{{ file.name }}</span>
              <el-button type="text" size="small" @click="removeAttachment(index)">
                <i class="el-icon-close"></i>
              </el-button>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 回复引用 -->
      <div v-if="replyToEmail" class="reply-quote">
        <div class="quote-header">
          <span>----- {{ replyToEmail.isForward ? '转发邮件' : '原始邮件' }} -----</span>
        </div>
        <div class="quote-content">
          <div>发件人: {{ replyToEmail.senderName || replyToEmail.senderEmail }}</div>
          <div>主题: {{ replyToEmail.subject }}</div>
          <div>时间: {{ formatTime(replyToEmail.sendTime || replyToEmail.createTime) }}</div>
          <div class="quote-text">{{ getQuoteText(replyToEmail) }}</div>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button :loading="sending" @click="handleSaveDraft">保存草稿</el-button>
      <el-button type="primary" :loading="sending" @click="handleSend">
        <i class="el-icon-s-promotion"></i> 发送
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as emailApi from '@/api/im/email'

const props = defineProps({
  visible: Boolean,
  replyTo: Object,
})

const emit = defineEmits(['update:visible', 'sent'])

const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val),
})

const formRef = ref(null)
const editorRef = ref(null)

const form = ref({
  toIds: [],
  subject: '',
  content: '',
})

const attachments = ref([])

const userOptions = ref([])
const userLoading = ref(false)
const sending = ref(false)

const rules = {
  toIds: [{ required: true, message: '请选择收件人', trigger: 'change' }],
  subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
}

// 回复邮件处理
const replyToEmail = computed(() => props.replyTo)

watch(
  replyToEmail,
  email => {
    if (email) {
      const isReply = !email.isForward
      form.value.subject = isReply ? `Re: ${email.subject}` : `Fwd: ${email.subject}`
      form.value.toIds = isReply ? [email.senderId] : []
    }
  },
  { immediate: true }
)

watch(dialogVisible, val => {
  if (val) {
    resetForm()
  }
})

const resetForm = () => {
  form.value = {
    toIds: replyToEmail.value?.isForward
      ? []
      : replyToEmail.value
        ? [replyToEmail.value.senderId]
        : [],
    subject: replyToEmail.value
      ? replyToEmail.value.isForward
        ? `Fwd: ${replyToEmail.value.subject}`
        : `Re: ${replyToEmail.value.subject}`
      : '',
    content: '',
  }
  attachments.value = []

  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerHTML = ''
    }
  })
}

const searchUsers = async keyword => {
  if (!keyword) {
    userOptions.value = []
    return
  }

  userLoading.value = true
  try {
    // 这里应该调用用户搜索 API
    // const response = await searchUsersApi(keyword)
    // userOptions.value = response.data

    // 模拟数据
    userOptions.value = [
      { id: 1, nickname: '张三', username: 'zhangsan' },
      { id: 2, nickname: '李四', username: 'lisi' },
      { id: 3, nickname: '王五', username: 'wangwu' },
    ].filter(u => u.nickname.includes(keyword) || u.username.includes(keyword))
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userLoading.value = false
  }
}

const execCommand = command => {
  document.execCommand(command, false, null)
  editorRef.value?.focus()
}

const handleAttachUpload = file => {
  attachments.value.push(file)
  return false // 阻止自动上传
}

const removeAttachment = index => {
  attachments.value.splice(index, 1)
}

const handleSend = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!form.value.content) {
    ElMessage.warning('请输入邮件内容')
    return
  }

  sending.value = true
  try {
    await emailApi.sendEmail({
      toIds: form.value.toIds,
      subject: form.value.subject,
      content: form.value.content,
    })

    ElMessage.success('邮件发送成功')
    emit('sent')
    handleClose()
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
  } finally {
    sending.value = false
  }
}

const handleSaveDraft = async () => {
  sending.value = true
  try {
    await emailApi.saveDraft({
      subject: form.value.subject,
      content: form.value.content,
    })
    ElMessage.success('草稿已保存')
    handleClose()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    sending.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

const formatTime = time => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getQuoteText = email => {
  const text = email.textContent || email.htmlContent || ''
  const plainText = text.replace(/<[^>]*>/g, '').trim()
  return plainText.substring(0, 200) + (plainText.length > 200 ? '...' : '')
}
</script>

<style lang="scss" scoped>
.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-container {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;

  .editor-toolbar {
    padding: 8px;
    border-bottom: 1px solid #dcdfe6;
    background: #fafafa;
  }

  .editor-content {
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
    padding: 12px;
    outline: none;

    &:empty:before {
      content: '请输入邮件内容...';
      color: #999;
    }
  }

  .attachment-list {
    padding: 8px 12px;
    border-top: 1px solid #dcdfe6;
    background: #fafafa;

    .attachment-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      margin-right: 8px;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      font-size: 12px;

      i.el-icon-document {
        color: #409eff;
      }
    }
  }
}

.reply-quote {
  margin-top: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-left: 3px solid #dcdfe6;
  border-radius: 4px;

  .quote-header {
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
  }

  .quote-content {
    font-size: 13px;
    color: #666;
    line-height: 1.6;

    div {
      margin-bottom: 4px;
    }

    .quote-text {
      margin-top: 8px;
      padding-left: 12px;
      color: #999;
    }
  }
}
</style>
