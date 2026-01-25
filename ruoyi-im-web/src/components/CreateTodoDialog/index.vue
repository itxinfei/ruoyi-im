<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑待办' : '新建待办'"
    width="500px"
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

      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请输入待办内容"
          :rows="4"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
          <el-option label="紧急" value="high" />
          <el-option label="普通" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </el-form-item>

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

// 判断是否为编辑模式
const isEdit = computed(() => !!props.todo)

// 表单数据
const form = reactive({
  title: '',
  content: '',
  priority: 'medium',
  dueDate: ''
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入待办标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
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
    form.content = props.todo.content || ''
    form.priority = props.todo.priority || 'medium'
    form.dueDate = props.todo.dueDate || ''
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

    let res
    if (isEdit.value) {
      // 编辑模式
      res = await updateTodo(props.todo.id, form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } else {
      // 新建模式
      res = await createTodo(form)
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
    if (error !== false) { // 表单验证失败时 error 为 false
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
  form.content = ''
  form.priority = 'medium'
  form.dueDate = ''
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
