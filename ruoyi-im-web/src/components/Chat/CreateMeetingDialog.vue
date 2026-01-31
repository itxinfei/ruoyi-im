<template>
  <el-dialog
    v-model="visible"
    :title="isEditMode ? '编辑会议' : '发起视频会议'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="会议主题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入会议主题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="会议描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入会议描述（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="开始时间" prop="scheduledStartTime">
        <el-date-picker
          v-model="form.scheduledStartTime"
          type="datetime"
          placeholder="选择开始时间"
          :disabled-date="disabledDate"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="会议类型" prop="meetingType">
        <el-radio-group v-model="form.meetingType">
          <el-radio label="INSTANT">立即开始</el-radio>
          <el-radio label="SCHEDULED">预约会议</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="参会人员" prop="participantIds">
        <el-select
          v-model="form.participantIds"
          multiple
          filterable
          placeholder="选择参会人员"
          style="width: 100%"
        >
          <el-option
            v-for="user in availableUsers"
            :key="user.id"
            :label="user.nickName"
            :value="user.id"
          >
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-avatar :src="user.avatar" :size="24">{{ user.nickName?.charAt(0) }}</el-avatar>
              <span>{{ user.nickName }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="需要密码" prop="requirePassword">
        <el-switch v-model="form.requirePassword" />
        <el-input
          v-if="form.requirePassword"
          v-model="form.password"
          placeholder="设置会议密码"
          maxlength="6"
          style="width: 150px; margin-left: 12px;"
        />
      </el-form-item>

      <el-form-item label="开启视频">
        <el-switch v-model="form.videoEnabled" />
        <span style="margin-left: 8px; font-size: 12px; color: var(--dt-text-secondary);">
          参会者入会时开启视频
        </span>
      </el-form-item>

      <el-form-item label="开启麦克风">
        <el-switch v-model="form.audioEnabled" />
        <span style="margin-left: 8px; font-size: 12px; color: var(--dt-text-secondary);">
          参会者入会时开启麦克风
        </span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ form.meetingType === 'INSTANT' ? '立即开始' : '创建会议' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createMeeting, updateMeeting, getMeetingDetail } from '@/api/im/meeting'
import { getContacts } from '@/api/im/contact'

const props = defineProps({
  modelValue: Boolean,
  meetingId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEditMode = computed(() => !!props.meetingId)

const formRef = ref(null)
const submitting = ref(false)
const availableUsers = ref([])

const form = ref({
  title: '',
  description: '',
  meetingType: 'INSTANT',
  scheduledStartTime: null,
  participantIds: [],
  requirePassword: false,
  password: '',
  videoEnabled: false,
  audioEnabled: false
})

const rules = {
  title: [
    { required: true, message: '请输入会议主题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  scheduledStartTime: [
    {
      validator: (rule, value, callback) => {
        if (form.value.meetingType === 'SCHEDULED' && !value) {
          callback(new Error('请选择开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 加载可选用户
const loadUsers = async () => {
  try {
    const res = await getContacts()
    if (res.code === 200) {
      // 假设返回的数据结构是 { data: { list: [...] } } 或 { data: [...] }
      const userList = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      availableUsers.value = userList.map(item => ({
        id: item.friendId || item.id || item.userId,
        nickName: item.friendName || item.name || item.nickName,
        avatar: item.friendAvatar || item.avatar
      }))
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  }
}

// 加载会议数据（编辑模式）
const loadMeetingData = async () => {
  try {
    const res = await getMeetingDetail(props.meetingId)
    if (res.code === 200 && res.data) {
      form.value = {
        title: res.data.title || '',
        description: res.data.description || '',
        meetingType: res.data.meetingType || 'SCHEDULED',
        scheduledStartTime: res.data.scheduledStartTime || null,
        participantIds: res.data.participantIds || [],
        requirePassword: !!res.data.password,
        password: res.data.password || '',
        videoEnabled: res.data.videoEnabled || false,
        audioEnabled: res.data.audioEnabled || false
      }
    }
  } catch (error) {
    ElMessage.error('加载会议信息失败')
  }
}

// 监听会议类型变化
watch(() => form.value.meetingType, (newType) => {
  if (newType === 'INSTANT') {
    form.value.scheduledStartTime = new Date().toISOString().slice(0, 19) + 'Z'
  }
})

// 监听对话框打开
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadUsers()
    if (props.meetingId) {
      loadMeetingData()
    } else {
      resetForm()
    }
  }
})

const resetForm = () => {
  form.value = {
    title: '',
    description: '',
    meetingType: 'INSTANT',
    scheduledStartTime: new Date().toISOString().slice(0, 19) + 'Z',
    participantIds: [],
    requirePassword: false,
    password: '',
    videoEnabled: false,
    audioEnabled: false
  }
  formRef.value?.clearValidate()
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...form.value,
      requirePassword: form.value.requirePassword ? 1 : 0
    }

    let res
    if (isEditMode.value) {
      // 编辑模式
      res = await updateMeeting(props.meetingId, data)
      if (res.code === 200) {
        ElMessage.success('会议更新成功')
        emit('success', { ...res.data, id: props.meetingId })
        visible.value = false
      } else {
        ElMessage.error(res.msg || '更新失败')
      }
    } else {
      // 创建模式
      res = await createMeeting(data)
      if (res.code === 200) {
        ElMessage.success('会议创建成功')
        emit('success', res.data)
        visible.value = false
      } else {
        ElMessage.error(res.msg || '创建失败')
      }
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败：' + (error.message || '未知错误') : '创建失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.el-form-item__label) {
  color: var(--dt-text-secondary);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  background: var(--dt-bg-body);
  border-color: var(--dt-border);
  color: var(--dt-text-primary);

  &:focus {
    border-color: var(--dt-brand-color);
  }
}
</style>
