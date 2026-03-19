<template>
  <el-dialog
    v-model="visible"
    title="编辑个人资料"
    width="500px"
    class="edit-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="profile-form"
    >
      <!-- 头像 -->
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="triggerAvatarUpload">
          <el-avatar :size="80" :src="form.avatar">
            {{ form.nickname?.charAt(0) || form.username?.charAt(0) }}
          </el-avatar>
          <div class="avatar-mask">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        <p class="avatar-tip">
          点击更换头像
        </p>
        <input
          ref="avatarInput"
          type="file"
          hidden
          accept="image/*"
          @change="handleAvatarChange"
        >
      </div>

      <!-- 基本信息 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          placeholder="请输入昵称"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="1">
            男
          </el-radio>
          <el-radio :label="2">
            女
          </el-radio>
          <el-radio :label="0">
            保密
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="职位" prop="position">
        <el-input v-model="form.position" placeholder="如：高级产品经理" maxlength="30" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="个人签名" prop="signature">
        <el-input
          v-model="form.signature"
          type="textarea"
          :rows="3"
          placeholder="写点什么介绍自己..."
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { updateUser, uploadAvatar } from '@/api/im/user'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const formRef = ref(null)
const avatarInput = ref(null)
const saving = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const form = ref({
  nickname: '',
  username: '',
  avatar: '',
  gender: 0,
  position: '',
  email: '',
  phone: '',
  signature: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }

  // 验证文件大小 (最大2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  try {
    const res = await uploadAvatar(file)
    if (res.code === 200 && res.data) {
      form.value.avatar = res.data
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const currentUserId = currentUser.value.id
        const res = await updateUser(currentUserId, {
          nickname: form.value.nickname,
          gender: form.value.gender,
          position: form.value.position,
          email: form.value.email,
          phone: form.value.phone,
          signature: form.value.signature,
          avatar: form.value.avatar
        })
        if (res.code === 200) {
          // 更新 Vuex 中的用户信息
          store.commit('user/UPDATE_USER_INFO', {
            ...currentUser.value,
            ...form.value
          })
          ElMessage.success('资料更新成功')
          emit('success')
          visible.value = false
        }
      } catch (error) {
        console.error('更新资料失败:', error)
        ElMessage.error('更新失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const initForm = () => {
  const user = currentUser.value
  form.value = {
    nickname: user.nickname || '',
    username: user.username || '',
    avatar: user.avatar || '',
    gender: user.gender || 0,
    position: user.position || '',
    email: user.email || '',
    phone: user.phone || '',
    signature: user.signature || ''
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    initForm()
  }
})
</script>

<style scoped lang="scss">
.edit-profile-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--dt-border-light);

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
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

.profile-form {
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24px;

    .avatar-wrapper {
      position: relative;
      cursor: pointer;
      border-radius: 50%;
      overflow: hidden;

      .el-avatar {
        border: 3px solid var(--dt-border-light);
      }

      .avatar-mask {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.2s;

        .el-icon {
          font-size: 24px;
          color: #fff;
        }
      }

      &:hover .avatar-mask {
        opacity: 1;
      }
    }

    .avatar-tip {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-top: 8px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dark {
  .edit-profile-dialog {
    :deep(.el-dialog__header) {
      border-bottom-color: var(--dt-border-dark);
    }

    :deep(.el-dialog__footer) {
      border-top-color: var(--dt-border-dark);
    }
  }

  .profile-form {
    .avatar-wrapper .el-avatar {
      border-color: var(--dt-border-dark);
    }
  }
}
</style>
