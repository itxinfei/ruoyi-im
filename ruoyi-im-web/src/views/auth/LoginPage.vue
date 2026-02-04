<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧装饰区 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo-wrapper">
            <div class="logo">
              <span class="material-icons-outlined logo-icon">forum</span>
            </div>
            <h1 class="app-name">IM</h1>
            <p class="app-desc">企业即时通讯</p>
          </div>
          <div class="features">
            <div class="feature-item">
              <span class="material-icons-outlined">chat_bubble_outline</span>
              <span>即时通讯</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-outlined">diversity_3</span>
              <span>群组协作</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-outlined">cloud_sync</span>
              <span>云端同步</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-outlined">verified_user</span>
              <span>安全可靠</span>
            </div>
          </div>
          <div class="decoration-footer">
            <p>高效沟通 · 智能协作</p>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-form-section">
        <!-- Logo移动端显示 -->
        <div class="mobile-logo">
          <span class="material-icons-outlined">forum</span>
          <span>IM</span>
        </div>

        <div class="login-header">
          <h2 class="welcome-title">登录</h2>
          <p class="welcome-subtitle">欢迎回到企业即时通讯平台</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <button
            :class="['tab-btn', { active: loginType === 'password' }]"
            @click="switchLoginType('password')"
          >
            <span class="material-icons-outlined">lock</span>
            账号密码
          </button>
          <button
            :class="['tab-btn', { active: loginType === 'sms' }]"
            @click="switchLoginType('sms')"
          >
            <span class="material-icons-outlined">sms</span>
            短信验证
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
              <div class="input-wrapper">
                <span class="material-icons-outlined input-icon">person</span>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名或手机号"
                  size="large"
                  clearable
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <span class="material-icons-outlined input-icon">lock</span>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  clearable
                />
              </div>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">
                <span class="checkbox-label">记住我</span>
              </el-checkbox>
              <el-link type="primary" underline="never" @click="handleForgotPassword">
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
              <div class="input-wrapper">
                <span class="material-icons-outlined input-icon">smartphone</span>
                <el-input
                  v-model="smsForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  clearable
                />
              </div>
            </el-form-item>

            <el-form-item prop="code">
              <div class="input-wrapper code-input-wrapper">
                <span class="material-icons-outlined input-icon">verified</span>
                <el-input
                  v-model="smsForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  clearable
                  class="code-input"
                />
                <el-button
                  :disabled="smsCountdown > 0"
                  @click="sendSMSCode"
                  class="code-btn"
                  :class="{ counting: smsCountdown > 0 }"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重新获取` : '获取验证码' }}
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
              <span class="material-icons-outlined">arrow_forward</span>
              登录
            </template>
            <span v-else>登录中...</span>
          </el-button>

          <!-- 第三方登录 -->
          <div class="third-party-login">
            <div class="divider">
              <span>其他登录方式</span>
            </div>
            <div class="third-party-icons">
              <button class="third-party-btn wechat" title="微信登录">
                <svg class="icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.27-.027-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                </svg>
              </button>
              <button class="third-party-btn dingtalk" title="钉钉登录">
                <svg class="icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </button>
              <button class="third-party-btn feishu" title="飞书登录">
                <svg class="icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14h-2v-4H6v-2h4V7h2v4h4v2h-4v4z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 登录页脚 -->
        <div class="login-footer">
          <p>© 2026 IM 即时通讯 · 企业级通信平台</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getRememberedUsername, removeItem, setRememberedUsername } from '@/utils/storage'
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { smsLogin, sendSmsCode } from '@/api/im/user'
const store = useStore()
const router = useRouter()

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

    // 调用发送验证码 API
    const res = await sendSmsCode({
      phone: smsForm.phone,
      type: 'login'
    })

    if (res.code === 404) {
      ElMessage.warning('短信验证码功能开发中，敬请期待')
      return
    }

    if (res.code !== 200) {
      ElMessage.error(res.msg || '发送验证码失败')
      return
    }

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
    if (error.response?.status !== 404) {
      ElMessage.error('发送验证码失败，请稍后重试')
    }
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
        setRememberedUsername(formData.username)
      } else {
        removeItem('remembered_username')
      }
    } else {
      // 短信验证码登录
      if (!smsForm.phone || !smsForm.code) {
        ElMessage.warning('请输入手机号和验证码')
        loading.value = false
        return
      }

      const res = await smsLogin({
        phone: smsForm.phone,
        code: smsForm.code
      })

      if (res.code === 404) {
        ElMessage.warning('短信登录功能开发中，敬请期待')
        loading.value = false
        return
      }

      if (res.code !== 200) {
        ElMessage.error(res.msg || '登录失败')
        loading.value = false
        return
      }

      // 存储token和用户信息
      if (res.data.token) {
        store.commit('user/SET_TOKEN', res.data.token)
      }
      if (res.data.userInfo) {
        store.commit('user/SET_USER_INFO', res.data.userInfo)
      }
    }

    ElMessage.success('登录成功')

    // 根据角色判断跳转
    const userRole = store.getters['user/userRole']
    const redirectParam = router.currentRoute.value.query.redirect

    let redirectUrl
    if (redirectParam) {
      // 如果有指定跳转地址，使用指定地址
      redirectUrl = redirectParam
    } else if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
      // 管理员默认跳转到管理后台
      redirectUrl = '/admin/dashboard'
    } else {
      // 普通用户跳转到首页
      redirectUrl = '/'
    }

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
  const rememberedUsername = getRememberedUsername()
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

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
  overflow: hidden;

  // 统一 Material Icons 基础样式，防止对齐偏移
  .material-icons-outlined {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    vertical-align: middle;
    width: 1em;
    height: 1em;
    line-height: 1;
    text-indent: 0;
    text-align: center;
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--dt-brand-color), transparent);
    opacity: 0.3;
  }

  // 强制覆盖输入框边框样式，确保始终显示
  :deep(.el-input__wrapper) {
    border: 1.5px solid #E5E9EF !important;
    box-shadow: none !important;
  }

  // 所有状态下都强制显示边框
  :deep(.el-input__wrapper:hover),
  :deep(.el-input__wrapper.is-focus),
  :deep(.el-input:hover .el-input__wrapper),
  :deep(.el-input.is-active .el-input__wrapper) {
    border: 1.5px solid #0089FF !important;
    box-shadow: none !important;
  }

  // 覆盖 Element Plus 默认的 box-shadow inset 效果
  :deep(.el-input__inner) {
    border: none !important;
    box-shadow: none !important;
  }

  // 确保所有状态下的输入框 wrapper 都有边框
  :deep(.login-form .el-input__wrapper),
  :deep(.el-form-item .el-input__wrapper) {
    border: 1.5px solid #E5E9EF !important;
    box-shadow: none !important;
  }

}

// 进入动画
.login-container {
  animation: fadeIn 0.4s var(--dt-ease-out);
}

.login-card {
  animation: scaleIn 0.5s var(--dt-ease-bounce);
}

.login-decoration {
  .logo-wrapper {
    animation: fadeInDown 0.6s var(--dt-ease-out);
  }

  .features {
    .feature-item {
      animation: fadeInLeft 0.5s var(--dt-ease-out) both;

      &:nth-child(1) { animation-delay: 0.1s; }
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.3s; }
      &:nth-child(4) { animation-delay: 0.4s; }
    }
  }

  .decoration-footer {
    animation: fadeIn 0.8s var(--dt-ease-out) 0.3s both;
  }
}

.login-form-section {
  animation: fadeInRight 0.6s var(--dt-ease-out);

  .login-header {
    animation: fadeInDown 0.5s var(--dt-ease-out);
  }

  .login-tabs {
    animation: fadeIn 0.6s var(--dt-ease-out) 0.1s both;
  }

  .form-content-wrapper {
    > * {
      animation: fadeInUp 0.4s var(--dt-ease-out) both;

      &:nth-child(1) { animation-delay: 0.1s; }
      &:nth-child(2) { animation-delay: 0.15s; }
      &:nth-child(3) { animation-delay: 0.2s; }
      &:nth-child(4) { animation-delay: 0.25s; }
    }
  }
}

// ============================================================================
// 背景装饰
// ============================================================================
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  opacity: 0.6;
}

.bg-shapes {
  position: absolute;
  inset: 0;

  .shape {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    animation: float 12s ease-in-out infinite;

    &.shape-1 {
      width: 350px;
      height: 350px;
      top: -80px;
      left: -80px;
      background: var(--dt-brand-light);
      animation-delay: 0s;
    }

    &.shape-2 {
      width: 280px;
      height: 280px;
      bottom: -80px;
      right: -80px;
      background: var(--dt-brand-bg);
      opacity: 0.5;
      animation-delay: 3s;
    }

    &.shape-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      right: 15%;
      background: var(--dt-info-bg);
      opacity: 0.4;
      animation-delay: 6s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(20px, -20px) scale(1.05);
  }
  66% {
    transform: translate(-10px, 15px) scale(0.95);
  }
}

// ============================================================================
// 登录卡片
// ============================================================================
.login-card {
  width: 100%;
  max-width: 960px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-2xl);
  box-shadow: var(--dt-shadow-modal);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 10;
  border: 1px solid var(--dt-border-light);
}

// ============================================================================
// 左侧装饰区
// ============================================================================
.login-decoration {
  flex: 0 0 380px;
  background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
  padding: 48px 40px;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.05) 0%, transparent 40%);
  }

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 120px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.1), transparent);
  }
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
      width: 72px;
      height: 72px;
      background: rgba(255, 255, 255, 0.15);
      border-radius: var(--dt-radius-xl);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);

      .logo-icon {
        font-size: 36px;
      }
    }

    .app-name {
      font-size: 28px;
      font-weight: 700;
      margin: 0 0 8px 0;
      letter-spacing: 4px;
    }

    .app-desc {
      font-size: 14px;
      opacity: 0.85;
      margin: 0;
      font-weight: 300;
    }
  }

  .features {
    flex: 1;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 14px;
      margin-bottom: 28px;
      opacity: 0.9;
      transition: all var(--dt-transition-base);
      padding: 12px 16px;
      border-radius: var(--dt-radius-lg);

      &:hover {
        opacity: 1;
        background: rgba(255, 255, 255, 0.1);
        transform: translateX(8px);
      }

      .material-icons-outlined {
        opacity: 0.9;
      }

      span:not(.material-icons-outlined) {
        font-size: 15px;
        font-weight: 400;
      }
    }
  }

  .decoration-footer {
    margin-top: auto;
    text-align: center;
    padding-top: 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.15);

    p {
      font-size: 13px;
      opacity: 0.75;
      margin: 0;
      font-weight: 300;
      letter-spacing: 2px;
    }
  }
}

// ============================================================================
// 右侧表单区
// ============================================================================
.login-form-section {
  flex: 1;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--dt-border-light), transparent);
  }
}

.mobile-logo {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;

  .material-icons-outlined {
    color: var(--dt-brand-color);
  }

  span:not(.material-icons-outlined) {
    font-size: 24px;
    font-weight: 700;
    color: var(--dt-text-primary);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .welcome-title {
    font-size: 26px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
  }

  .welcome-subtitle {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    margin: 0;
  }
}

.form-content-wrapper {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

// 切换标签
.login-tabs {
  width: 100%;
  max-width: 400px;
  margin: 0 auto 28px;
  display: flex;
  gap: 8px;
  padding: 4px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-lg);

  .tab-btn {
    flex: 1;
    padding: 12px 16px;
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    font-size: 14px;
    font-weight: 500;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    &.active {
      background: var(--dt-bg-card);
      color: var(--dt-brand-color);
      box-shadow: var(--dt-shadow-2);
      font-weight: 600;
    }

    &:hover:not(.active) {
      color: var(--dt-text-primary);
      background: rgba(0, 0, 0, 0.03);
    }

    &:active:not(.active) {
      transform: scale(0.98);
    }
  }
}

// 表单
.login-form {
  margin-bottom: 24px;
  width: 100%;

  :deep(.el-form-item) {
    margin-bottom: 20px;
    width: 100%;
  }

  .input-wrapper {
    position: relative;
    width: 100%;

    .input-icon {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--dt-text-quaternary);
      font-size: 20px;
      width: 20px; // 显式宽度
      height: 20px; // 显式高度
      z-index: 10;
      pointer-events: none;
      transition: color var(--dt-transition-fast);
    }

    :deep(.el-input) {
      width: 100%;
    }

    :deep(.el-input__wrapper) {
      padding-left: 48px;
      border-radius: var(--dt-radius-lg);
      border: 1.5px solid var(--dt-border-color);
      background: var(--dt-bg-input);
      box-shadow: none !important;
      transition: all var(--dt-transition-fast);

      &:hover {
        border-color: var(--dt-border-input-hover);
      }

      &.is-focus {
        border-color: var(--dt-brand-color);
        background: var(--dt-bg-card);
        box-shadow: 0 0 0 3px var(--dt-brand-lighter) !important;
      }
    }

    :deep(.el-input__inner) {
      font-size: 15px;
      color: var(--dt-text-primary);
      font-weight: 400;

      &::placeholder {
        color: var(--dt-text-quaternary);
        font-weight: 400;
      }
    }

    &.code-input-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;

      .code-input {
        flex: 1;

        :deep(.el-input__wrapper) {
          padding-right: 16px;
          border: 1.5px solid var(--dt-border-color);
          box-shadow: none !important;

          &:hover {
            border-color: var(--dt-border-input-hover);
          }

          &.is-focus {
            border-color: var(--dt-brand-color);
            box-shadow: 0 0 0 3px var(--dt-brand-lighter) !important;
          }
        }
      }

      .code-btn {
        flex-shrink: 0;
        padding: 0 16px;
        height: 40px;
        border: 1.5px solid var(--dt-border-color);
        border-radius: var(--dt-radius-md);
        background: var(--dt-bg-card);
        color: var(--dt-brand-color);
        font-size: 13px;
        font-weight: 500;
        white-space: nowrap;
        transition: all var(--dt-transition-fast);

        &:hover:not(:disabled) {
          border-color: var(--dt-brand-color);
          background: var(--dt-brand-bg);
        }

        &:active:not(:disabled) {
          transform: scale(0.96);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        &.counting {
          color: var(--dt-text-secondary);
          border-color: var(--dt-border-light);
        }
      }

      .input-icon {
        left: 16px;
      }
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;

    .checkbox-label {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }

    :deep(.el-checkbox__label) {
      color: var(--dt-text-secondary);
      font-size: 13px;
    }

    :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
      color: var(--dt-brand-color);
    }
  }
}

// 登录按钮
.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-brand-color);
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--dt-transition-fast);
  margin-bottom: 28px;
  box-shadow: 0 2px 8px rgba(0, 137, 255, 0.25);

  &:hover:not(:disabled) {
    background: var(--dt-brand-hover);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 137, 255, 0.35);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
  }
}

// 第三方登录
.third-party-login {
  .divider {
    text-align: center;
    position: relative;
    margin-bottom: 24px;

    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      height: 1px;
      background: var(--dt-border-light);
    }

    span {
      background: var(--dt-bg-card);
      padding: 0 16px;
      color: var(--dt-text-quaternary);
      font-size: 13px;
      position: relative;
    }
  }

  .third-party-icons {
    display: flex;
    justify-content: center;
    gap: 16px;

    .third-party-btn {
      width: 48px;
      height: 48px;
      border: 1.5px solid var(--dt-border-color);
      background: var(--dt-bg-card);
      color: var(--dt-text-secondary);
      transition: all var(--dt-transition-fast);
      border-radius: var(--dt-radius-lg);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        width: 24px;
        height: 24px;
      }

      &:hover {
        transform: translateY(-3px);
        box-shadow: var(--dt-shadow-3);
      }

      &.wechat:hover {
        border-color: #07c160;
        color: #07c160;
        background: #f6ffed;
      }

      &.dingtalk:hover {
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
        background: var(--dt-brand-bg);
      }

      &.feishu:hover {
        border-color: #00d6b9;
        color: #00d6b9;
        background: #e6fffb;
      }

      &:active {
        transform: translateY(-1px);
      }
    }
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



// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 960px) {
  .login-card {
    max-width: 480px;
    flex-direction: column;

    .login-decoration {
      display: none;
    }
  }

  .mobile-logo {
    display: flex;
  }

  .login-form-section {
    padding: 40px 32px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }

  .login-card {
    border-radius: var(--dt-radius-xl);
  }

  .login-form-section {
    padding: 32px 24px;
  }

  .login-header .welcome-title {
    font-size: 22px;
  }

  .login-button {
    height: 44px;
  }



  .third-party-icons .third-party-btn {
    width: 44px;
    height: 44px;
  }
}
</style>
