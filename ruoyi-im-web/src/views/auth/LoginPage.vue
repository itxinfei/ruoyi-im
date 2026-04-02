<template>
  <div class="login-container" :class="{ 'dark': isDark }">
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧装饰区 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo-wrapper">
            <div class="logo">
              <el-icon class="logo-icon"><ChatDotRound /></el-icon>
            </div>
            <h1 class="app-name">
              IM
            </h1>
            <p class="app-desc">
              企业即时通讯
            </p>
          </div>
          <div class="hero-feature">
            <div class="hero-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span class="hero-text">即时沟通</span>
          </div>
          <div class="decoration-footer">
            <p>© 2026 IM</p>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-form-section">
        <!-- Logo移动端显示 -->
        <div class="mobile-logo">
          <el-icon><ChatDotRound /></el-icon>
          <span>IM</span>
        </div>

        <div class="login-header">
          <h2 class="welcome-title">
            登录
          </h2>
          <p class="welcome-subtitle">
            开始沟通
          </p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <button
            :class="['tab-btn', { active: loginType === 'password' }]"
            @click="switchLoginType('password')"
          >
            <el-icon><Lock /></el-icon>
            账号密码
          </button>
          <button
            :class="['tab-btn', { active: loginType === 'sms', disabled: true }]"
            disabled
            title="暂未开放"
          >
            <el-icon><Message /></el-icon>
            短信验证
            <span class="coming-soon">（暂未开放）</span>
          </button>
        </div>

        <!-- 登录表单 -->
        <div class="form-content-wrapper">
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
                忘记密码？
              </el-link>
            </div>
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
              <div class="code-input-row">
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
          </el-form>

          <!-- 登录按钮 -->
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <template v-if="!loading">
              登录
            </template>
            <span v-else>登录中...</span>
          </el-button>


        </div>

        <!-- 登录页脚 -->
        <div class="login-footer">
          <p>© 2026 IM</p>
        </div>
      </div>
    </div>

    <!-- 主题切换按钮 -->
    <button
      class="theme-toggle"
      :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
      @click="toggleTheme"
    >
      <el-icon class="sun-icon" :class="{ hidden: isDark }"><Sunny /></el-icon>
      <el-icon class="moon-icon" :class="{ hidden: !isDark }"><Moon /></el-icon>
    </button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineRound, User, UserFilled, Cloudy, CircleCheck, Lock, Message, Sunny, Moon, List, Grid, Folder, Upload, Star, MoreFilled, Close, Iphone } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const store = useStore()
const router = useRouter()
const { isDark, toggleTheme } = useTheme()

// 表单引用
const loginFormRef = ref(null)
const smsFormRef = ref(null)
const loading = ref(false)

// 登录类型
const loginType = ref('password')

// 账号密码登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 短信登录表单
const smsForm = reactive({
  phone: '',
  code: ''
})

// 短信倒计时
const smsCountdown = ref(0)
let smsTimer = null

// 表单验证规则
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

// 切换登录类型
const switchLoginType = (type) => {
  loginType.value = type
  if (type === 'password') {
    smsFormRef.value?.clearValidate()
  } else {
    loginFormRef.value?.clearValidate()
  }
}

// 发送短信验证码
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

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

// 处理登录
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

// 处理短信登录
const handleSMSLogin = () => {
  handleLogin()
}

// 初始化
onMounted(() => {
  const rememberedUsername = localStorage.getItem('remembered_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})

// 清理定时器
onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
    smsTimer = null
  }
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-body);
  padding: 24px;
  position: relative;

  &.dark {
    background: var(--dt-bg-body-dark);
  }
}

// ============================================================================
// 登录卡片
// ============================================================================
.login-card {
  width: 100%;
  max-width: 960px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  display: flex;
  overflow: hidden;
  border: 1px solid var(--dt-border-light);
}

// ============================================================================
// 左侧装饰区
// ============================================================================
.login-decoration {
  flex: 0 0 280px;
  background: var(--dt-brand-color);
  padding: var(--dt-spacing-2xl) var(--dt-spacing-xl);
  color: var(--dt-text-white);
}

.decoration-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;

  .logo-wrapper {
    text-align: center;
    margin-bottom: 48px;

    .logo {
      width: 56px;
      height: 56px;
      background: rgba(255, 255, 255, 0.15);
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;

      .logo-icon {
        font-size: 28px;
      }
    }

    .app-name {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 4px 0;
      letter-spacing: 2px;
    }

    .app-desc {
      font-size: 13px;
      opacity: 0.75;
      margin: 0;
    }
  }

  .hero-feature {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--dt-spacing-md);

    .hero-icon {
      font-size: 48px;
      opacity: 0.9;
    }

    .hero-text {
      font-size: 18px;
      font-weight: 500;
      opacity: 0.9;
    }
  }

  .decoration-footer {
    margin-top: auto;
    text-align: center;

    p {
      font-size: 12px;
      opacity: 0.6;
      margin: 0;
    }
  }
}

// ============================================================================
// 右侧表单区
// ============================================================================
.login-form-section {
  flex: 1;
  padding: var(--dt-spacing-2xl) var(--dt-spacing-xl);
  display: flex;
  flex-direction: column;
}

.mobile-logo {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;

  .material-icons-outlined {
    font-size: 32px;
    color: var(--dt-brand-color);
  }

  span:not(.material-icons-outlined) {
    font-size: 24px;
    font-weight: 700;
    color: var(--dt-text-primary);
  }
}

.login-header {
  margin-bottom: 24px;

  .welcome-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 4px 0;
  }

  .welcome-subtitle {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    margin: 0;
  }
}

.form-content-wrapper {
  width: 100%;
}

// 切换标签
.login-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .tab-btn {
    padding: 8px 0;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    font-size: 14px;
    cursor: pointer;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
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

// 表单
.login-form {
  margin-bottom: 24px;
  width: 100%;

  :deep(.el-form-item) {
    margin-bottom: 16px;
    width: 100%;
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-sm);
    border: 1px solid var(--dt-border-light);
    background: var(--dt-bg-card);
    box-shadow: none !important;
    transition: border-color var(--dt-transition-fast);

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

    &::placeholder {
      color: var(--dt-text-tertiary);
    }
  }

  .code-input-row {
    display: flex;
    gap: 8px;

    .code-input {
      flex: 1;
    }

    .code-btn {
      flex-shrink: 0;
      padding: 0 12px;
      height: 40px;
      border: 1px solid var(--dt-border-light);
      border-radius: var(--dt-radius-sm);
      background: var(--dt-bg-card);
      color: var(--dt-brand-color);
      font-size: 13px;
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
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .checkbox-label {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }

    :deep(.el-checkbox__label) {
      color: var(--dt-text-secondary);
      font-size: 13px;
    }
  }
}

// 登录按钮
.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
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

// 登录页脚
.login-footer {
  text-align: center;
  margin-top: auto;

  p {
    font-size: 12px;
    color: var(--dt-text-quaternary);
    margin: 0;
  }
}

// 主题切换按钮
.theme-toggle {
  position: fixed;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  z-index: 100;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    color: var(--dt-brand-color);
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark {
  .login-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .login-decoration {
    background: #1e3a5f;
  }

  .welcome-title {
    color: var(--dt-text-primary-dark);
  }

  .login-tabs {
    border-bottom-color: var(--dt-border-dark);
  }
}
</style>
