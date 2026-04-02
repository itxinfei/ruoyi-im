<template>
  <div class="login-container" :class="{ 'dark': isDark }">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon class="logo-icon"><ChatDotRound /></el-icon>
        </div>
        <h1 class="brand-title">IM</h1>
        <p class="brand-slogan">企业即时通讯</p>

        <div class="brand-features">
          <div class="feature">
            <el-icon><ChatLineRound /></el-icon>
            <span>即时沟通</span>
          </div>
          <div class="feature">
            <el-icon><UserFilled /></el-icon>
            <span>群组协作</span>
          </div>
          <div class="feature">
            <el-icon><CircleCheck /></el-icon>
            <span>安全可靠</span>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <p>高效沟通 · 智能协作</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <!-- 主题切换 -->
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '浅色模式' : '深色模式'">
          <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
        </button>

        <div class="form-header">
          <h2>登录</h2>
          <p>开始使用 IM</p>
        </div>

        <!-- 登录 Tab -->
        <div class="login-tabs">
          <button :class="['tab', { active: loginType === 'password' }]" @click="loginType = 'password'">
            账号密码
          </button>
          <button :class="['tab', { active: loginType === 'sms', disabled: true }]" disabled>
            短信验证
          </button>
        </div>

        <!-- 账号密码表单 -->
        <el-form v-if="loginType === 'password'" ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名或手机号" size="large" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password clearable />
          </el-form-item>
        </el-form>

        <!-- 短信表单 -->
        <el-form v-else ref="smsFormRef" :model="smsForm" :rules="smsRules" @keyup.enter="handleSMSLogin">
          <el-form-item prop="phone">
            <el-input v-model="smsForm.phone" placeholder="手机号" size="large" clearable />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-row">
              <el-input v-model="smsForm.code" placeholder="验证码" size="large" clearable />
              <el-button class="code-btn" :disabled="smsCountdown > 0" @click="sendSMSCode">
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <!-- 记住我 & 忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false" @click="handleForgotPassword">忘记密码？</el-link>
        </div>

        <!-- 登录按钮 -->
        <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>

        <!-- 底部版权 -->
        <div class="form-footer">
          <p>© 2026 IM 企业即时通讯</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineRound, UserFilled, CircleCheck, Sunny, Moon } from '@element-plus/icons-vue'
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
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
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

  &.dark {
    .brand-panel {
      background: var(--dt-bg-card-dark);
    }
    .form-panel {
      background: var(--dt-bg-body-dark);
    }
  }
}

// 左侧品牌区 - 参考野火风格
.brand-panel {
  flex: 0 0 420px;
  background: var(--dt-brand-color);
  padding: var(--dt-spacing-xl);
  display: flex;
  flex-direction: column;
  color: var(--dt-text-white);
}

.brand-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-logo {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--dt-spacing-lg);

  .logo-icon {
    font-size: 36px;
    color: var(--dt-text-white);
  }
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 var(--dt-spacing-sm) 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 var(--dt-spacing-xl) 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);

  .feature {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
    font-size: 14px;
    opacity: 0.9;

    .el-icon {
      font-size: 18px;
    }
  }
}

.brand-footer {
  padding-top: var(--dt-spacing-lg);
  border-top: 1px solid rgba(255, 255, 255, 0.2);

  p {
    font-size: 13px;
    opacity: 0.7;
    margin: 0;
  }
}

// 右侧表单区
.form-panel {
  flex: 1;
  background: var(--dt-bg-body);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  padding: var(--dt-spacing-xl);
  position: relative;
}

.theme-toggle {
  position: absolute;
  top: 0;
  right: 0;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-sm);
  transition: all var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
    background: var(--dt-bg-hover);
  }
}

.form-header {
  margin-bottom: var(--dt-spacing-xl);

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 var(--dt-spacing-xs) 0;
  }

  p {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    margin: 0;
  }
}

.login-tabs {
  display: flex;
  gap: var(--dt-spacing-lg);
  margin-bottom: var(--dt-spacing-lg);
  padding-bottom: var(--dt-spacing-sm);
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .tab {
    padding: var(--dt-spacing-sm) 0;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    font-size: 14px;
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

.el-form {
  :deep(.el-form-item) {
    margin-bottom: var(--dt-spacing-md);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-sm);
    border: 1px solid var(--dt-border-color);
    background: var(--dt-bg-card);
    box-shadow: none;
    padding: 0 12px;

    &:hover {
      border-color: var(--dt-border-input-hover);
    }

    &.is-focus {
      border-color: var(--dt-brand-color);
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: var(--dt-text-primary);
    height: 40px;

    &::placeholder {
      color: var(--dt-text-tertiary);
    }
  }
}

.code-row {
  display: flex;
  gap: var(--dt-spacing-sm);

  .el-input {
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
    font-size: 13px;
    cursor: pointer;

    &:hover:not(:disabled) {
      border-color: var(--dt-brand-color);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--dt-spacing-lg);

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-brand-color);
  border: none;
  color: var(--dt-text-white);
  margin-bottom: var(--dt-spacing-lg);

  &:hover:not(:disabled) {
    background: var(--dt-brand-hover);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.form-footer {
  text-align: center;

  p {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin: 0;
  }
}

// 暗色模式
.dark {
  .form-header {
    h2 {
      color: var(--dt-text-primary-dark);
    }
    p {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .el-form {
    :deep(.el-input__wrapper) {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-color);
    }

    :deep(.el-input__inner) {
      color: var(--dt-text-primary-dark);

      &::placeholder {
        color: var(--dt-text-tertiary-dark);
      }
    }
  }

  .code-row .code-btn {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-color);
  }

  .form-options :deep(.el-checkbox__label) {
    color: var(--dt-text-secondary-dark);
  }

  .form-footer p {
    color: var(--dt-text-tertiary-dark);
  }
}
</style>
