<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑待办' : '新建待办'"
    width="480px"
    class="todo-dialog"
    :close-on-click-modal="true"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      label-position="top"
      class="todo-form"
    >
      <el-form-item
        label="待办标题"
        prop="title"
      >
        <el-input
          v-model="form.title"
          placeholder="想要做点什么？"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item
        label="详细描述"
        prop="content"
      >
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="添加补充说明（可选）"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <div class="form-row">
        <el-form-item
          label="优先级"
          prop="priority"
          class="flex-1"
        >
          <el-select
            v-model="form.priority"
            placeholder="请选择"
          >
            <el-option
              label="紧急"
              value="high"
            >
              <div class="priority-option high">
                <span class="dot" /> 紧急
              </div>
            </el-option>
            <el-option
              label="普通"
              value="medium"
            >
              <div class="priority-option medium">
                <span class="dot" /> 普通
              </div>
            </el-option>
            <el-option
              label="低"
              value="low"
            >
              <div class="priority-option low">
                <span class="dot" /> 低
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          label="截止时间"
          prop="dueDate"
          class="flex-1"
        >
          <el-date-picker
            v-model="form.dueDate"
            type="datetime"
            placeholder="点击选择"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createTodo, updateTodo } from '@/api/im/workbench'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  todo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const isEdit = computed(() => !!props.todo)

const form = reactive({
  title: '',
  content: '',
  priority: 'medium',
  dueDate: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const dateShortcuts = [
  { text: '今天', value: new Date() },
  { text: '明天', value: () => { const date = new Date(); date.setTime(date.getTime() + 3600 * 1000 * 24); return date } },
  { text: '一周后', value: () => { const date = new Date(); date.setTime(date.getTime() + 3600 * 1000 * 24 * 7); return date } }
]

watch(() => props.modelValue, val => {
  visible.value = val
  if (val && props.todo) {
    form.title = props.todo.title || ''
    form.content = props.todo.content || ''
    form.priority = props.todo.priority || 'medium'
    form.dueDate = props.todo.dueDate || ''
  }
})

watch(visible, val => {
  emit('update:modelValue', val)
  if (!val) {resetForm()}
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const priorityMap = { low: 1, medium: 2, high: 3 }
    const apiData = {
      title: form.title,
      description: form.content,
      priority: priorityMap[form.priority],
      dueDate: form.dueDate || null
    }

    const res = isEdit.value 
      ? await updateTodo(props.todo.id, apiData)
      : await createTodo(apiData)

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      emit('success', res.data)
      handleClose()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== false) {console.error('Todo operation failed:', error)}
  } finally {
    submitting.value = false
  }
}

const handleClose = () => { visible.value = false }

const resetForm = () => {
  formRef.value?.resetFields()
  form.title = ''
  form.content = ''
  form.priority = 'medium'
  form.dueDate = ''
}
</script>

<style scoped lang="scss">
.todo-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
    
    .el-dialog__header {
      padding: 20px 24px;
      margin-right: 0;
      border-bottom: 1px solid var(--dt-border-light);
      .el-dialog__title {
        font-size: 16px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }
    }
    
    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid var(--dt-border-light);
      background: var(--dt-bg-subtle);
    }
  }
}

.todo-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    color: var(--dt-text-secondary);
    font-weight: 500;
    margin-bottom: 4px;
    padding: 0;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: 1px solid var(--dt-border-medium);
    border-radius: var(--dt-radius-md);
    transition: all 0.2s;
    background: var(--dt-bg-body);

    &:hover { border-color: var(--dt-brand-color); }
    &.is-focus { border-color: var(--dt-brand-color); box-shadow: 0 0 0 2px var(--dt-brand-bg) !important; }
  }
}

.form-row {
  display: flex;
  gap: 16px;
  .flex-1 { flex: 1; }
  :deep(.el-date-editor.el-input) { width: 100%; }
}

.priority-option {
  display: flex;
  align-items: center;
  gap: 8px;
  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
  &.high .dot { background: var(--dt-priority-high); }
  &.medium .dot { background: var(--dt-priority-medium); }
  &.low .dot { background: var(--dt-priority-low); }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
