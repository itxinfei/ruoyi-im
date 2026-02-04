<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="400px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="true"
    class="change-password-dialog"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="mt-2"
    >
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码（6-20位）"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="flex justify-end gap-2 px-4 pb-4">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          提交修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { updateUserPassword } from '@/api/im/user_password'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 对接后端业务 API
        const res = await updateUserPassword(form.oldPassword, form.newPassword)
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          visible.value = false
          
          // 强制重新登录
          setTimeout(async () => {
            await store.dispatch('user/logout')
            router.push('/login')
          }, 1500)
        }
      } catch (error) {
        ElMessage.error(error.message || '修改失败，请检查旧密码是否正确')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.change-password-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--dt-border-light);
  }

  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--dt-border-light);
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--dt-text-secondary);
    font-size: 14px;
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-lg);
    border: 1.5px solid var(--dt-border-color);
    transition: all var(--dt-transition-fast);

    &:hover {
      border-color: var(--dt-border-input-hover);
    }

    &.is-focus {
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 3px var(--dt-brand-lighter);
    }
  }

  :deep(.el-input__inner) {
    font-size: 15px;
    color: var(--dt-text-primary);

    &::placeholder {
      color: var(--dt-text-quaternary);
    }
  }

  :deep(.el-button) {
    border-radius: var(--dt-radius-lg);
    font-weight: 500;
    padding: 10px 20px;
    height: 40px;
    transition: all var(--dt-transition-fast);

    &:not(.el-button--primary) {
      border-color: var(--dt-border-color);
      color: var(--dt-text-secondary);

      &:hover {
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
        background: var(--dt-brand-bg);
      }
    }

    &.el-button--primary {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);

      &:hover {
        background: var(--dt-brand-hover);
        border-color: var(--dt-brand-hover);
        transform: translateY(-1px);
        box-shadow: var(--dt-shadow-brand);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

.dark .change-password-dialog {
  :deep(.el-dialog__header) {
    border-bottom-color: var(--dt-border-dark);
  }

  :deep(.el-dialog__title) {
    color: var(--dt-text-primary-dark);
  }

  :deep(.el-dialog__footer) {
    border-top-color: var(--dt-border-dark);
  }

  :deep(.el-form-item__label) {
    color: var(--dt-text-secondary-dark);
  }

  :deep(.el-input__wrapper) {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      border-color: var(--dt-border-input-hover);
    }
  }

  :deep(.el-input__inner) {
    color: var(--dt-text-primary-dark);

    &::placeholder {
      color: var(--dt-text-quaternary-dark);
    }
  }

  :deep(.el-button:not(.el-button--primary)) {
    border-color: var(--dt-border-dark);
    color: var(--dt-text-secondary-dark);
    background: var(--dt-bg-hover-dark);

    &:hover {
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
      background: var(--dt-bg-active-dark);
    }
  }
}
</style>
