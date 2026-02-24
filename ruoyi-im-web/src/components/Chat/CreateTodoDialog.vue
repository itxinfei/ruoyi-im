<template>
  <el-dialog
    v-model="dialogVisible"
    title="创建待办"
    width="600px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div class="create-todo-dialog">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        label-position="top"
      >
        <!-- 待办内容 -->
        <el-form-item
          label="待办内容"
          prop="content"
        >
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="3"
            placeholder="输入待办事项..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 截止日期 -->
        <el-form-item
          label="截止日期"
          prop="dueDate"
        >
          <el-date-picker
            v-model="formData.dueDate"
            type="datetime"
            placeholder="选择截止日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 优先级 -->
        <el-form-item
          label="优先级"
          prop="priority"
        >
          <el-radio-group v-model="formData.priority">
            <el-radio-button label="high">
              <span class="priority-high">高</span>
            </el-radio-button>
            <el-radio-button label="medium">
              <span class="priority-medium">中</span>
            </el-radio-button>
            <el-radio-button label="low">
              <span class="priority-low">低</span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 关联消息（如果有） -->
        <el-form-item
          v-if="relatedMessage"
          label="关联消息"
        >
          <div class="related-message">
            <div class="message-content">
              {{ relatedMessage.content?.text || relatedMessage.content }}
            </div>
            <el-tag
              size="small"
              type="info"
            >
              来自聊天
            </el-tag>
          </div>
        </el-form-item>

        <!-- 提醒设置 -->
        <el-form-item label="提醒">
          <el-checkbox v-model="formData.enableReminder">
            开启提醒
          </el-checkbox>
          <el-time-picker
            v-if="formData.enableReminder"
            v-model="formData.reminderTime"
            placeholder="提醒时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%; margin-top: 8px"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          创建待办
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createTodo } from '@/api/im/workbench'

const props = defineProps({
  modelValue: Boolean,
  relatedMessage: {
    type: Object,
    default: null
  },
  sessionId: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'closed'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const formRef = ref(null)
const loading = ref(false)

const formData = ref({
  content: '',
  dueDate: '',
  priority: 'medium',
  enableReminder: false,
  reminderTime: '09:00:00'
})

const formRules = {
  content: [
    { required: true, message: '请输入待办内容', trigger: 'blur' },
    { max: 500, message: '待办内容不能超过 500 字', trigger: 'blur' }
  ],
  dueDate: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ]
}

watch(() => props.modelValue, val => {
  if (val) {
    // 重置表单
    formData.value = {
      content: '',
      dueDate: '',
      priority: 'medium',
      enableReminder: false,
      reminderTime: '09:00:00'
    }
  }
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await createTodo({
      content: formData.value.content,
      dueDate: formData.value.dueDate,
      priority: formData.value.priority,
      enableReminder: formData.value.enableReminder,
      reminderTime: formData.value.enableReminder ? formData.value.reminderTime : null,
      relatedMessageId: props.relatedMessage?.id,
      sessionId: props.sessionId
    })

    ElMessage.success('待办创建成功')
    emit('success', formData.value)
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('创建失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleClosed = () => {
  emit('closed')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.create-todo-dialog {
  padding: 8px 0;
}

.priority-high {
  color: var(--dt-error-color, #ff4d4f);
  font-weight: 600;
}

.priority-medium {
  color: var(--dt-warning-color, #faad14);
  font-weight: 600;
}

.priority-low {
  color: var(--dt-success-color, #52c41a);
  font-weight: 600;
}

.related-message {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--dt-bg-card-hover, #f5f7fa);
  border-radius: 8px;
  border-left: 3px solid var(--dt-brand-color, #3296fa);

  .message-content {
    font-size: 13px;
    color: var(--dt-text-secondary, #5f6672);
    line-height: 1.5;
  }

  .dark & {
    background: var(--dt-bg-hover-dark, rgba(255,255,255,0.05));
    border-left-color: var(--dt-brand-color, #3296fa);

    .message-content {
      color: var(--dt-text-secondary-dark, #a0a8b8);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
