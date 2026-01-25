<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="400px"
    append-to-body
    destroy-on-close
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
  :deep(.el-dialog__body) {
    padding: 10px 24px;
  }
}
</style>
