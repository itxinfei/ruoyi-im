<template>
  <el-dialog
    v-model="visible"
    title="编辑资料"
    width="500px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <!-- 头像上传 -->
      <el-form-item label="头像">
        <div class="avatar-upload">
          <el-avatar :size="80" :src="form.avatar || ''">
            {{ form.nickname?.charAt(0) || '?' }}
          </el-avatar>
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-success="handleAvatarSuccess"
            name="avatarfile"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50" show-word-limit />
      </el-form-item>

      <!-- 个性签名 -->
      <el-form-item label="个性签名" prop="signature">
        <el-input
          v-model="form.signature"
          type="textarea"
          :rows="3"
          placeholder="介绍一下自己..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 性别 -->
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="0">保密</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 生日 -->
      <el-form-item label="生日" prop="birthday">
        <el-date-picker
          v-model="form.birthday"
          type="date"
          placeholder="选择生日"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { updateUser } from '@/api/im/user'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const formRef = ref(null)
const saving = ref(false)
const visible = ref(false)

// 上传配置
const uploadUrl = computed(() => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseURL}/im/user/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || ''
  return { Authorization: `Bearer ${token}` }
})

// 表单数据
const form = reactive({
  nickname: '',
  signature: '',
  gender: 0,
  birthday: '',
  email: '',
  avatar: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称长度在2-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.warning('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.warning('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    form.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 初始化表单
const initForm = () => {
  const currentUser = store.getters['user/currentUser'] || store.state.im.currentUser
  form.nickname = currentUser.nickname || ''
  form.signature = currentUser.signature || ''
  form.gender = currentUser.gender ?? 0
  form.birthday = currentUser.birthday || ''
  form.email = currentUser.email || ''
  form.avatar = currentUser.avatar || ''
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const userId = store.getters['im/currentUserId'] || store.state.im.currentUser.id
    await updateUser(userId, {
      nickname: form.nickname,
      signature: form.signature,
      gender: form.gender,
      birthday: form.birthday || null,
      email: form.email || null
    })

    // 更新本地用户信息
    store.commit('im/SET_CURRENT_USER', {
      ...store.state.im.currentUser,
      ...form
    })

    ElMessage.success('保存成功')
    emit('success')
    visible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 对话框关闭后重置
const handleClosed = () => {
  formRef.value?.resetFields()
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initForm()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-uploader {
  :deep(.el-upload) {
    display: block;
  }
}
</style>
