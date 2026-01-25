<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      label-position="left"
    >
      <el-form-item label="收件人" prop="to">
        <el-input
          v-model="form.to"
          placeholder="多个收件人用逗号分隔"
          maxlength="200"
        />
      </el-form-item>

      <el-form-item label="主题" prop="subject">
        <el-input
          v-model="form.subject"
          placeholder="请输入邮件主题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请输入邮件内容"
          :rows="10"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="附件">
        <el-upload
          v-model:file-list="fileList"
          action="#"
          :auto-upload="false"
          :multiple="true"
          :limit="5"
        >
          <el-button size="small" type="primary">选择文件</el-button>
          <template #tip>
            <div class="upload-tip">
              最多上传5个文件，单个文件不超过10MB
            </div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          发送
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMail } from '@/api/im/mail'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  replyTo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const fileList = ref([])

// 对话框标题
const dialogTitle = computed(() => {
  if (props.replyTo?.isReply) return '回复邮件'
  if (props.replyTo?.isForward) return '转发邮件'
  return '写邮件'
})

// 表单数据
const form = reactive({
  to: '',
  subject: '',
  content: ''
})

// 表单校验规则
const rules = {
  to: [
    { required: true, message: '请输入收件人', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请输入邮件主题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入邮件内容', trigger: 'blur' }
  ]
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.replyTo) {
    // 回复/转发模式：填充表单
    if (props.replyTo.isReply) {
      form.to = props.replyTo.to || ''
      form.subject = props.replyTo.subject || ''
      form.content = '\n\n---------- 原始邮件 ----------\n'
    } else if (props.replyTo.isForward) {
      form.subject = props.replyTo.subject || ''
      form.content = props.replyTo.content || ''
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      to: form.to,
      subject: form.subject,
      content: form.content,
      attachments: fileList.value.map(file => ({
        name: file.name,
        size: file.size,
        url: file.url || '' // 实际项目中需要先上传文件获取URL
      }))
    }

    const res = await sendMail(data)
    if (res.code === 200) {
      ElMessage.success('邮件发送成功')
      emit('success', res.data)
      handleClose()
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('发送邮件失败', error)
      ElMessage.error('发送失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  form.to = ''
  form.subject = ''
  form.content = ''
  fileList.value = []
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>
