<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑待办' : '新建待办'"
    width="520px"
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
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入待办标题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="内容" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入待办内容"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="分类" prop="taskType">
            <el-select v-model="form.taskType" placeholder="请选择分类" style="width: 100%">
              <el-option label="工作" value="WORK" />
              <el-option label="个人" value="PERSONAL" />
              <el-option label="学习" value="STUDY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
              <el-option label="紧急" value="HIGH" />
              <el-option label="普通" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="截止日期" prop="dueDate">
            <el-date-picker
              v-model="form.dueDate"
              type="datetime"
              placeholder="选择截止日期"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="提醒时间" prop="remindTime">
            <el-date-picker
              v-model="form.remindTime"
              type="datetime"
              placeholder="选择提醒时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              :disabled="!form.dueDate"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="提醒方式" prop="remindType">
        <el-radio-group v-model="form.remindType" :disabled="!form.remindTime">
          <el-radio value="ONCE">仅一次</el-radio>
          <el-radio value="REPEAT">重复提醒</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.remindType === 'REPEAT'" label="重复周期" prop="repeatType">
        <el-select v-model="form.repeatType" placeholder="请选择重复周期" style="width: 100%">
          <el-option label="每天" value="DAILY" />
          <el-option label="每周" value="WEEKLY" />
          <el-option label="每月" value="MONTHLY" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createTask, updateTask } from '@/api/im/task'

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

// 判断是否为编辑模式
const isEdit = computed(() => !!props.todo)

// 表单数据
const form = reactive({
  title: '',
  description: '',
  taskType: 'WORK',
  priority: 'MEDIUM',
  dueDate: '',
  remindTime: '',
  remindType: 'ONCE',
  repeatType: ''
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入待办标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  dueDate: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ]
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.todo) {
    // 编辑模式：填充表单
    form.title = props.todo.title || ''
    form.description = props.todo.description || ''
    form.taskType = props.todo.taskType || 'WORK'
    form.priority = props.todo.priority || 'MEDIUM'
    form.dueDate = props.todo.dueDate || ''
    form.remindTime = props.todo.remindTime || ''
    form.remindType = props.todo.remindType || 'ONCE'
    form.repeatType = props.todo.repeatType || ''
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
      title: form.title,
      description: form.description,
      taskType: form.taskType,
      priority: form.priority,
      dueDate: form.dueDate,
      remindTime: form.remindTime || null,
      remindType: form.remindTime ? form.remindType : null,
      repeatType: form.remindType === 'REPEAT' ? form.repeatType : null
    }

    let res
    if (isEdit.value) {
      // 编辑模式
      res = await updateTask({
        id: props.todo.id,
        ...data
      })
      if (res.code === 200) {
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } else {
      // 新建模式
      res = await createTask(data)
      if (res.code === 200) {
        ElMessage.success('创建成功')
      } else {
        ElMessage.error(res.msg || '创建失败')
      }
    }

    if (res.code === 200) {
      emit('success', res.data)
      handleClose()
    }
  } catch (error) {
    if (error !== false) {
      console.error(isEdit.value ? '更新待办失败' : '创建待办失败', error)
      ElMessage.error(isEdit.value ? '保存失败，请稍后重试' : '创建失败，请稍后重试')
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
  form.title = ''
  form.description = ''
  form.taskType = 'WORK'
  form.priority = 'MEDIUM'
  form.dueDate = ''
  form.remindTime = ''
  form.remindType = 'ONCE'
  form.repeatType = ''
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>