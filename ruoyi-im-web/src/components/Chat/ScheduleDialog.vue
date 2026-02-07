<template>
  <el-dialog
    v-model="visible"
    title="新建日程"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item
        label="标题"
        prop="title"
      >
        <el-input
          v-model="form.title"
          placeholder="请输入日程标题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item
        label="开始时间"
        prop="startTime"
      >
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :disabled-date="disableStartDate"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item
        label="结束时间"
        prop="endTime"
      >
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :disabled-date="disableEndDate"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item
        label="地点"
        prop="location"
      >
        <el-input
          v-model="form.location"
          placeholder="请输入地点（可选）"
          prefix-icon="Location"
        />
      </el-form-item>

      <el-form-item
        label="描述"
        prop="description"
      >
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入日程描述（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="颜色标识">
        <div class="color-options">
          <div
            v-for="color in colorOptions"
            :key="color.value"
            class="color-option"
            :class="{ active: form.color === color.value }"
            :style="{ backgroundColor: color.value }"
            @click="form.color = color.value"
          >
            <span
              v-if="form.color === color.value"
              class="material-icons-outlined"
            >check</span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="提醒时间">
        <el-select
          v-model="form.reminderType"
          placeholder="选择提醒时间"
          style="width: 100%"
        >
          <el-option
            label="不提醒"
            value=""
          />
          <el-option
            label="开始前15分钟"
            value="15"
          />
          <el-option
            label="开始前30分钟"
            value="30"
          />
          <el-option
            label="开始前1小时"
            value="60"
          />
          <el-option
            label="开始前1天"
            value="1440"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="saving"
        @click="handleSave"
      >
        {{ saving ? '保存中...' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createEvent } from '@/api/im/schedule'
import { formatDateTimeISO } from '@/utils/format'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  defaultDate: { type: [String, Date], default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  title: '',
  startTime: '',
  endTime: '',
  location: '',
  description: '',
  color: '#409eff',
  reminderType: '15'
})

const colorOptions = [
  { value: '#409eff', label: '蓝色' },
  { value: '#67c23a', label: '绿色' },
  { value: '#e6a23c', label: '橙色' },
  { value: '#f56c6c', label: '红色' },
  { value: '#909399', label: '灰色' },
  { value: '#9c27b0', label: '紫色' }
]

const rules = {
  title: [
    { required: true, message: '请输入日程标题', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.startTime && new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须大于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 禁用早于今天的日期
const disableStartDate = time => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用昨天之前的日期
}

const disableEndDate = time => {
  if (!form.startTime) {return false}
  return time.getTime() < new Date(form.startTime).getTime()
}

const handleSave = async () => {
  if (!formRef.value) {return}

  try {
    await formRef.value.validate()

    saving.value = true
    const data = {
      title: form.title,
      startTime: form.startTime,
      endTime: form.endTime,
      location: form.location,
      description: form.description,
      color: form.color
    }

    // 计算提醒时间
    if (form.reminderType) {
      const reminderMinutes = parseInt(form.reminderType)
      const startTime = new Date(form.startTime)
      const reminderTime = new Date(startTime.getTime() - reminderMinutes * 60000)
      data.reminderTime = formatDateTime(reminderTime)
    }

    await createEvent(data)
    ElMessage.success('日程已创建')
    emit('saved')
    handleClose()
  } catch (error) {
    if (error !== false) { // 表单验证失败时 error 为 false
      ElMessage.error('创建日程失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 使用共享工具函数
const formatDateTime = formatDateTimeISO

const resetForm = () => {
  Object.assign(form, {
    title: '',
    startTime: '',
    endTime: '',
    location: '',
    description: '',
    color: '#409eff',
    reminderType: '15'
  })
  formRef.value?.clearValidate()
}

const handleClose = () => {
  resetForm()
  visible.value = false
  emit('update:modelValue', false)
}

// 设置默认日期时间
const setDefaultDateTime = () => {
  if (props.defaultDate) {
    const baseDate = new Date(props.defaultDate)
    const now = new Date()
    const startDate = new Date(baseDate)
    startDate.setHours(now.getHours(), now.getMinutes() + 30, 0, 0)

    const endDate = new Date(startDate)
    endDate.setHours(startDate.getHours() + 1)

    form.startTime = formatDateTime(startDate)
    form.endTime = formatDateTime(endDate)
  }
}

watch(() => props.modelValue, val => {
  visible.value = val
  if (val) {
    setDefaultDateTime()
  }
})

watch(visible, val => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
.color-options {
  display: flex;
  gap: 12px;

  .color-option {
    width: 36px;
    height: 36px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid transparent;
    transition: all 0.2s;

    &:hover {
      transform: scale(1.1);
    }

    &.active {
      border-color: #303133;
      box-shadow: 0 0 0 2px var(--dt-brand-hover-03);
    }

    .material-icons-outlined {
      color: #fff;
      font-size: 20px;
    }
  }
}
</style>
