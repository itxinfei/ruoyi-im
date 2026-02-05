/**
 * 发送机器人消息对话框
 * 参考野火IM的机器人消息功能
 * 替代原来的SendDingDialog
 */
<template>
  <el-dialog
    v-model="dialogVisible"
    title="发送机器人消息"
    width="480px"
    :close-on-click-modal="false"
    class="send-bot-dialog"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      class="bot-form"
    >
      <!-- 消息类型 -->
      <el-form-item
        label="消息类型"
        prop="msgType"
      >
        <el-select
          v-model="form.msgType"
          placeholder="选择类型"
          @change="handleTypeChange"
        >
          <el-option
            label="系统通知"
            value="notification"
          />
          <el-option
            label="群公告"
            value="announcement"
          />
          <el-option
            label="定时提醒"
            value="reminder"
          />
          <el-option
            label="审批通知"
            value="approval"
          />
          <el-option
            label="机器人消息"
            value="bot"
          />
        </el-select>
      </el-form-item>

      <!-- 消息内容 -->
      <el-form-item
        label="消息内容"
        prop="content"
      >
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入消息内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 是否紧急 -->
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio-button label="normal">
            普通
          </el-radio-button>
          <el-radio-button label="urgent">
            紧急
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 需要确认 -->
      <el-form-item label="需要确认">
        <el-switch v-model="form.needsConfirm" />
        <span class="form-tip">开启后需要接收方确认</span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="sending"
        @click="handleSend"
      >
        发送
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  session: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'send'])

const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val)
})

const formRef = ref(null)
const sending = ref(false)

const form = reactive({
  msgType: 'notification',
  content: '',
  priority: 'normal',
  needsConfirm: false
})

const rules = {
  msgType: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

const handleTypeChange = val => {
  // 根据类型自动填充一些默认内容
  const defaults = {
    notification: '',
    announcement: '【群公告】',
    reminder: '【定时提醒】',
    approval: '【审批通知】',
    bot: '【机器人消息】'
  }
  if (!form.content || Object.values(defaults).some(d => form.content.startsWith(d))) {
    form.content = defaults[val] || ''
  }
}

const handleSend = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) {return}

    sending.value = true

    // 构造消息数据
    const messageData = {
      type: 'BOT',
      contentType: form.msgType,
      content: form.content,
      priority: form.priority,
      needsConfirm: form.needsConfirm,
      sessionId: props.session?.id,
      targetType: props.session?.type // PRIVATE, GROUP
    }

    emit('send', messageData)

    ElMessage.success('发送成功')
    handleClose()
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    msgType: 'notification',
    content: '',
    priority: 'normal',
    needsConfirm: false
  })
}

// 监听visible变化，重置表单
watch(() => props.visible, val => {
  if (!val) {resetForm()}
})
</script>

<style lang="scss" scoped>
@use '@/styles/design-tokens.scss' as *;

.send-bot-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 20px 10px;
    border-bottom: 1px solid var(--dt-border-color);
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  :deep(.el-dialog__footer) {
    padding: 15px 20px;
    border-top: 1px solid var(--dt-border-color);
    text-align: right;
  }
}

.bot-form {
  .form-tip {
    margin-left: 12px;
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  :deep(.el-form-item__label) {
    color: var(--dt-text-secondary);
    font-weight: 500;
  }

  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    border-radius: var(--dt-radius-md, 8px);
    border-color: var(--dt-border-color);

    &:focus {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 2px rgba(0, 137, 255, 0.1);
    }
  }

  :deep(.el-radio-button__inner) {
    border-radius: var(--dt-radius-md, 8px);
    border-color: var(--dt-border-color);

    &:hover {
      color: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background-color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    color: white;
  }
}
</style>
