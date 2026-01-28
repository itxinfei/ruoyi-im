<template>
  <el-dialog
    v-model="visible"
    title="发起审批"
    width="600px"
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <!-- 选择审批模板 -->
    <div v-if="step === 1" class="step-select-template">
      <div class="template-list">
        <div
          v-for="template in templates"
          :key="template.id"
          class="template-item"
          :class="{ active: selectedTemplate?.id === template.id }"
          @click="selectTemplate(template)"
        >
          <div class="template-icon">
            <span class="material-icons-outlined">{{ template.icon }}</span>
          </div>
          <div class="template-info">
            <div class="template-name">{{ template.name }}</div>
            <div class="template-desc">{{ template.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 填写审批表单 -->
    <div v-else class="step-fill-form">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="审批标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入审批标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="审批事由" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            placeholder="请输入审批事由"
            :rows="4"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 根据模板类型显示不同字段 -->
        <template v-if="selectedTemplate?.type === 'leave'">
          <el-form-item label="请假类型" prop="leaveType">
            <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
              <el-option label="事假" value="personal" />
              <el-option label="病假" value="sick" />
              <el-option label="年假" value="annual" />
              <el-option label="调休" value="compensatory" />
            </el-select>
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </template>

        <template v-else-if="selectedTemplate?.type === 'expense'">
          <el-form-item label="报销金额" prop="amount">
            <el-input-number
              v-model="form.amount"
              :min="0"
              :precision="2"
              placeholder="请输入报销金额"
              style="width: 100%"
            />
          </el-form-item>
        </template>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button v-if="step === 2" @click="step = 1">上一步</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="step === 1"
          type="primary"
          :disabled="!selectedTemplate"
          @click="step = 2"
        >
          下一步
        </el-button>
        <el-button
          v-else
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交审批
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTemplates, createApproval } from '@/api/im/approval'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const step = ref(1) // 1: 选择模板, 2: 填写表单
const formRef = ref(null)
const submitting = ref(false)
const templates = ref([])
const selectedTemplate = ref(null)

// 表单数据
const form = reactive({
  title: '',
  reason: '',
  leaveType: '',
  startTime: '',
  endTime: '',
  amount: 0
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入审批标题', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入审批事由', trigger: 'blur' }
  ]
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadTemplates()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

// 加载审批模板
const loadTemplates = async () => {
  try {
    const res = await getTemplates()
    if (res.code === 200) {
      templates.value = res.data || []
    }
  } catch (error) {
    console.error('加载模板失败', error)
    // 使用默认模板
    templates.value = [
      { id: 1, name: '请假申请', description: '事假、病假、年假等', type: 'leave', icon: 'event_busy' },
      { id: 2, name: '报销申请', description: '差旅、采购等费用报销', type: 'expense', icon: 'receipt_long' },
      { id: 3, name: '用印申请', description: '公章、合同章等用印申请', type: 'seal', icon: 'verified' }
    ]
  }
}

// 选择模板
const selectTemplate = (template) => {
  selectedTemplate.value = template
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const params = {
      templateId: selectedTemplate.value.id,
      title: form.title
    }

    const data = {
      reason: form.reason,
      ...selectedTemplate.value.type === 'leave' && {
        leaveType: form.leaveType,
        startTime: form.startTime,
        endTime: form.endTime
      },
      ...selectedTemplate.value.type === 'expense' && {
        amount: form.amount
      }
    }

    const res = await createApproval(params, data)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      emit('success', res.data)
      handleClose()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交审批失败', error)
      ElMessage.error('提交失败，请稍后重试')
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
  step.value = 1
  formRef.value?.resetFields()
  selectedTemplate.value = null
  form.title = ''
  form.reason = ''
  form.leaveType = ''
  form.startTime = ''
  form.endTime = ''
  form.amount = 0
}
</script>

<style scoped>
.step-select-template {
  padding: 10px 0;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:hover {
  border-color: #1677ff;
  background: #f0f7ff;
}

.template-item.active {
  border-color: #1677ff;
  background: #e6f7ff;
}

.template-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f0f7ff;
  color: #1677ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.template-icon .material-icons-outlined {
  font-size: 24px;
}

.template-info {
  flex: 1;
}

.template-name {
  font-size: 15px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.template-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
