<template>
  <div class="login-container" :class="{ 'dark': isDark }">
    <!-- javafx: 居中卡片，参考钉钉极简风格 -->
    <div class="login-card">
      <!-- 顶部品牌区 -->
      <div class="brand-area">
        <div class="brand-logo">
          <el-icon class="logo-icon"><ChatDotRound /></el-icon>
        </div>
        <span class="brand-name">IM</span>
      </div>

      <!-- 表单区 -->
      <div class="form-area">
        <h1 class="form-title">登录</h1>
        <p class="form-subtitle">开始沟通</p>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <button
            :class="['tab-btn', { active: loginType === 'password' }]"
            @click="switchLoginType('password')"
          >
            账号密码
          </button>
          <button
            :class="['tab-btn', { active: loginType === 'sms', disabled: true }]"
            disabled
            title="暂未开放"
          >
            短信验证
          </button>
        </div>

        <!-- 账号密码登录 -->
        <el-form
          v-if="loginType === 'password'"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名或手机号"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">
              <span class="checkbox-label">记住我</span>
            </el-checkbox>
            <el-link type="primary" :underline="false" @click="handleForgotPassword">
              忘记密码
            </el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <template v-if="!loading">登录</template>
            <span v-else>登录中...</span>
          </el-button>
        </el-form>

        <!-- 短信验证登录 -->
        <el-form
          v-else
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          class="login-form"
          @keyup.enter="handleSMSLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="手机号"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-row">
              <el-input
                v-model="smsForm.code"
                placeholder="验证码"
                size="large"
                clearable
                class="code-input"
              />
              <el-button
                :disabled="smsCountdown > 0"
                class="code-btn"
                :class="{ counting: smsCountdown > 0 }"
                @click="sendSMSCode"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <template v-if="!loading">登录</template>
            <span v-else>登录中...</span>
          </el-button>
        </el-form>
      </div>

      <!-- 主题切换 -->
      <button
        class="theme-toggle"
        :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
        @click="toggleTheme"
      >
        <el-icon><Sunny v-if="!isDark" /><Moon v-else /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Sunny, Moon } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const store = useStore()
const router = useRouter()
const { isDark, toggleTheme } = useTheme()

const loginFormRef = ref(null)
const smsFormRef = ref(null)
const loading = ref(false)
const loginType = ref('password')

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const smsForm = reactive({
  phone: '',
  code: ''
})

const smsCountdown = ref(0)
let smsTimer = null

const loginRules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const switchLoginType = (type) => {
  loginType.value = type
  if (type === 'password') {
    smsFormRef.value?.clearValidate()
  } else {
    loginFormRef.value?.clearValidate()
  }
}

const sendSMSCode = async () => {
  if (!smsForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  try {
    await smsFormRef.value.validateField('phone')
    ElMessage.success('验证码已发送')

    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

const handleLogin = async () => {
  const formRef = loginType.value === 'password' ? loginFormRef.value : smsFormRef.value
  const formData = loginType.value === 'password' ? loginForm : smsForm

  if (!formRef) return

  try {
    await formRef.validate()
    loading.value = true

    if (loginType.value === 'password') {
      await store.dispatch('user/login', {
        username: formData.username,
        password: formData.password
      })

      if (formData.rememberMe) {
        localStorage.setItem('remembered_username', formData.username)
      } else {
        localStorage.removeItem('remembered_username')
      }
    } else {
      ElMessage.info('短信登录暂未开放，请使用账号密码登录')
      return
    }

    ElMessage.success('登录成功')

    const redirectUrl = router.currentRoute.value.query.redirect || '/'
    router.push(redirectUrl)

  } catch (error) {
    console.error('登录失败:', error)
    const errorMsg = error.message || (error.response?.data?.msg) || '登录失败，请检查您的账户信息'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const handleSMSLogin = () => {
  handleLogin()
}

onMounted(() => {
  const rememberedUsername = localStorage.getItem('remembered_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})

onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
    smsTimer = null
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-body);
  padding: var(--dt-spacing-lg);

  &.dark {
    background: var(--dt-bg-body-dark);
  }
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-xl);
  position: relative;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.brand-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--dt-spacing-sm);
  margin-bottom: var(--dt-spacing-xl);

  .brand-logo {
    width: 40px;
    height: 40px;
    background: var(--dt-brand-color);
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;

    .logo-icon {
      font-size: 22px;
      color: var(--dt-text-white);
    }
  }

  .brand-name {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .dark & {
      color: var(--dt-text-primary-dark);
    }
  }
}

.form-area {
  width: 100%;
}

.form-title {
  font-size: var(--dt-font-size-xl);
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-spacing-xs) 0;
  text-align: center;

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.form-subtitle {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  margin: 0 0 var(--dt-spacing-lg) 0;
  text-align: center;

  .dark & {
    color: var(--dt-text-tertiary-dark);
  }
}

.login-tabs {
  display: flex;
  gap: var(--dt-spacing-md);
  margin-bottom: var(--dt-spacing-lg);
  padding-bottom: var(--dt-spacing-sm);
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .tab-btn {
    flex: 1;
    padding: var(--dt-spacing-sm) 0;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    font-size: var(--dt-font-size-base);
    cursor: pointer;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -9px;
      left: 0;
      right: 0;
      height: 2px;
      background: transparent;
    }

    &.active {
      color: var(--dt-brand-color);
      font-weight: 500;

      &::after {
        background: var(--dt-brand-color);
      }
    }

    &.disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: var(--dt-spacing-md);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-sm);
    border: 1px solid var(--dt-border-color);
    background: var(--dt-bg-card);
    box-shadow: none;
    padding: 0 12px;
    transition: border-color var(--dt-transition-fast);

    &:hover {
      border-color: var(--dt-border-input-hover);
    }

    &.is-focus {
      border-color: var(--dt-brand-color);
    }

    .dark & {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-color);

      &:hover {
        border-color: var(--dt-border-input-hover);
      }
    }
  }

  :deep(.el-input__inner) {
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    height: 40px;

    &::placeholder {
      color: var(--dt-text-tertiary);
    }

    .dark & {
      color: var(--dt-text-primary-dark);

      &::placeholder {
        color: var(--dt-text-tertiary-dark);
      }
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--dt-spacing-lg);

  .checkbox-label {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);

    .dark & {
      color: var(--dt-text-secondary-dark);
    }
  }

  :deep(.el-checkbox__label) {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);

    .dark & {
      color: var(--dt-text-secondary-dark);
    }
  }
}

.code-row {
  display: flex;
  gap: var(--dt-spacing-sm);

  .code-input {
    flex: 1;
  }

  .code-btn {
    flex-shrink: 0;
    padding: 0 var(--dt-spacing-md);
    height: 40px;
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
    background: var(--dt-bg-card);
    color: var(--dt-brand-color);
    font-size: var(--dt-font-size-sm);
    cursor: pointer;
    transition: border-color var(--dt-transition-fast);

    &:hover:not(:disabled) {
      border-color: var(--dt-brand-color);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    &.counting {
      color: var(--dt-text-tertiary);
    }

    .dark & {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-color);
    }
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: var(--dt-font-size-base);
  font-weight: 500;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-brand-color);
  border: none;
  color: var(--dt-text-white);

  &:hover:not(:disabled) {
    background: var(--dt-brand-hover);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.theme-toggle {
  position: absolute;
  top: var(--dt-spacing-md);
  right: var(--dt-spacing-md);
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-sm);
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
  }
}
</style>
