<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑任务' : '新建任务'"
    width="520px"
    class="create-todo-dialog"
    destroy-on-close
    append-to-body
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <el-form-item label="任务标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入任务标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="任务描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入任务描述"
          maxlength="500"
        />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="优先级" prop="priority" class="form-item-half">
          <el-radio-group v-model="form.priority">
            <el-radio :label="2">
              普通
            </el-radio>
            <el-radio :label="3">
              紧急
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="任务分类" prop="taskType" class="form-item-half">
          <el-select v-model="form.taskType" placeholder="请选择分类" style="width: 100%">
            <el-option label="工作" value="WORK" />
            <el-option label="个人" value="PERSONAL" />
            <el-option label="学习" value="STUDY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="截止日期" prop="dueDate" class="form-item-half">
          <el-date-picker
            v-model="form.dueDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="提醒时间" prop="remindTime" class="form-item-half">
          <el-date-picker
            v-model="form.remindTime"
            type="datetime"
            placeholder="选择时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createTask, updateTask } from '@/api/im/task'

const props = defineProps({
  modelValue: Boolean,
  todo: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const saving = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.todo?.id)

const defaultForm = () => ({
  title: '',
  description: '',
  priority: 2,  // 2=普通, 3=紧急
  taskType: 'WORK',
  dueDate: '',
  remindTime: ''
})

const form = ref(defaultForm())

const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 1, max: 100, message: '长度不超过100字符', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务分类', trigger: 'change' }
  ]
}

watch(() => props.todo, (todo) => {
  if (todo?.id) {
    form.value = {
      id: todo.id,
      title: todo.title || '',
      description: todo.description || '',
      priority: todo.priority || 2,
      taskType: todo.taskType || 'WORK',
      dueDate: todo.dueDate ? formatDateForInput(todo.dueDate) : '',
      remindTime: todo.remindTime ? formatDateTimeForInput(todo.remindTime) : ''
    }
  } else {
    form.value = defaultForm()
  }
}, { immediate: true })

watch(() => props.modelValue, (val) => {
  if (val && !props.todo?.id) {
    form.value = defaultForm()
  }
})

const formatDateForInput = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}

const formatDateTimeForInput = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().replace('T', ' ').split('.')[0]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        title: form.value.title,
        description: form.value.description,
        priority: form.value.priority,
        taskType: form.value.taskType,
        dueDate: form.value.dueDate ? new Date(form.value.dueDate).toISOString() : null,
        remindTime: form.value.remindTime ? new Date(form.value.remindTime).toISOString() : null
      }

      let res
      if (isEdit.value) {
        res = await updateTask({ ...payload, id: props.todo.id })
      } else {
        res = await createTask(payload)
      }

      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '任务已更新' : '任务已创建')
        emit('success')
        visible.value = false
      }
    } catch (e) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      saving.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.create-todo-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--dt-border-light);

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--dt-border-light);
  }
}

.form-row {
  display: flex;
  gap: 16px;

  .form-item-half {
    flex: 1;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
