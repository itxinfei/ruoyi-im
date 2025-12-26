<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-animation">
        <div class="floating-shapes">
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
          <div class="shape shape-3"></div>
          <div class="shape shape-4"></div>
          <div class="shape shape-5"></div>
        </div>
      </div>
    </div>

    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <img src="/logo.png" alt="Logo" class="logo-img" />
            <h1 class="logo-text">RuoYi IM</h1>
          </div>
          <p class="welcome-text">欢迎回来，请登录您的账户</p>
        </div>

        <div class="login-form">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名/邮箱/手机号"
                prefix-icon="el-icon-user"
                size="large"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <a href="#" class="forgot-password" @click.prevent="showForgotPassword">忘记密码？</a>
            </div>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="divider">
            <span>或</span>
          </div>

          <div class="social-login">
            <el-button class="social-btn wechat" @click="socialLogin('wechat')">
              <i class="el-icon-chat-dot-round"></i>
              微信登录
            </el-button>
            <el-button class="social-btn qq" @click="socialLogin('qq')">
              <i class="el-icon-connection"></i>
              QQ登录
            </el-button>
          </div>

          <div class="register-link">
            <span>还没有账户？</span>
            <a href="#" @click.prevent="goToRegister">立即注册</a>
          </div>
        </div>
      </div>

      <div class="login-footer">
        <p>&copy; 2024 RuoYi IM. All rights reserved.</p>
        <div class="footer-links">
          <a href="#" @click.prevent="showPrivacyPolicy">隐私政策</a>
          <a href="#" @click.prevent="showTermsOfService">服务条款</a>
          <a href="#" @click.prevent="showHelp">帮助中心</a>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="showForgotPasswordDialog" title="重置密码" width="400px">
      <el-form :model="forgotPasswordForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="forgotPasswordForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-input">
            <el-input v-model="forgotPasswordForm.captcha" placeholder="请输入验证码" />
            <el-button size="small" @click="sendCaptcha">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="forgotPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">重置密码</el-button>
      </template>
    </el-dialog>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegisterDialog" title="注册账户" width="500px">
      <el-form :model="registerForm" :rules="registerRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="registerForm.agreeTerms">
            我已阅读并同意
            <a href="#" @click.prevent="showTermsOfService">服务条款</a>
            和
            <a href="#" @click.prevent="showPrivacyPolicy">隐私政策</a>
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { login, register, forgotPassword } from '@/api/login'

const router = useRouter()
const store = useStore()

// 响应式数据
const loading = ref(false)
const loginFormRef = ref(null)
const showForgotPasswordDialog = ref(false)
const showRegisterDialog = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

// 忘记密码表单
const forgotPasswordForm = reactive({
  email: '',
  captcha: '',
  newPassword: '',
  confirmPassword: '',
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false,
})

// 表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 方法
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await login(loginForm)

    // 保存token和用户信息
    store.commit('user/SET_TOKEN', response.data.token)
    store.commit('user/SET_USER_INFO', response.data.userInfo)

    if (loginForm.rememberMe) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('username', loginForm.username)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('username')
    }

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const socialLogin = platform => {
  ElMessage.info(`${platform}登录功能开发中...`)
}

const showForgotPassword = () => {
  showForgotPasswordDialog.value = true
}

const sendCaptcha = () => {
  if (!forgotPasswordForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  ElMessage.success('验证码已发送到您的邮箱')
}

const resetPassword = async () => {
  if (forgotPasswordForm.newPassword !== forgotPasswordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  try {
    await forgotPassword(forgotPasswordForm)
    ElMessage.success('密码重置成功，请使用新密码登录')
    showForgotPasswordDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '密码重置失败')
  }
}

const goToRegister = () => {
  showRegisterDialog.value = true
}

const handleRegister = async () => {
  if (!registerForm.agreeTerms) {
    ElMessage.warning('请先同意服务条款和隐私政策')
    return
  }

  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    showRegisterDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  }
}

const showPrivacyPolicy = () => {
  ElMessage.info('隐私政策页面开发中...')
}

const showTermsOfService = () => {
  ElMessage.info('服务条款页面开发中...')
}

const showHelp = () => {
  ElMessage.info('帮助中心页面开发中...')
}

// 生命周期
onMounted(() => {
  // 检查是否有记住的用户名
  const rememberMe = localStorage.getItem('rememberMe')
  if (rememberMe === 'true') {
    const savedUsername = localStorage.getItem('username')
    if (savedUsername) {
      loginForm.username = savedUsername
      loginForm.rememberMe = true
    }
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.background-animation {
  position: relative;
  width: 100%;
  height: 100%;
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;

  &.shape-1 {
    width: 80px;
    height: 80px;
    top: 20%;
    left: 10%;
    animation-delay: 0s;
  }

  &.shape-2 {
    width: 120px;
    height: 120px;
    top: 60%;
    right: 10%;
    animation-delay: 2s;
  }

  &.shape-3 {
    width: 60px;
    height: 60px;
    top: 80%;
    left: 20%;
    animation-delay: 4s;
  }

  &.shape-4 {
    width: 100px;
    height: 100px;
    top: 10%;
    right: 30%;
    animation-delay: 1s;
  }

  &.shape-5 {
    width: 40px;
    height: 40px;
    top: 40%;
    left: 60%;
    animation-delay: 3s;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;

    .logo-img {
      width: 48px;
      height: 48px;
      margin-right: 12px;
    }

    .logo-text {
      font-size: 28px;
      font-weight: bold;
      color: #333;
      margin: 0;
    }
  }

  .welcome-text {
    color: #666;
    font-size: 14px;
    margin: 0;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .login-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .forgot-password {
      color: #1890ff;
      text-decoration: none;
      font-size: 14px;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 8px;
  }
}

.divider {
  text-align: center;
  margin: 24px 0;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #e0e0e0;
  }

  span {
    background: white;
    padding: 0 16px;
    color: #999;
    font-size: 14px;
  }
}

.social-login {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  .social-btn {
    flex: 1;
    height: 40px;
    border-radius: 8px;
    font-size: 14px;

    &.wechat {
      background: #07c160;
      border-color: #07c160;
      color: white;

      &:hover {
        background: #06ad56;
        border-color: #06ad56;
      }
    }

    &.qq {
      background: #12b7f5;
      border-color: #12b7f5;
      color: white;

      &:hover {
        background: #0fa3d9;
        border-color: #0fa3d9;
      }
    }
  }
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;

  a {
    color: #1890ff;
    text-decoration: none;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;

  p {
    margin: 0 0 8px 0;
  }

  .footer-links {
    a {
      color: rgba(255, 255, 255, 0.8);
      text-decoration: none;
      margin: 0 8px;

      &:hover {
        color: white;
        text-decoration: underline;
      }
    }
  }
}

.captcha-input {
  display: flex;
  gap: 8px;

  .el-input {
    flex: 1;
  }

  .el-button {
    width: 100px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-content {
    padding: 16px;
  }

  .login-card {
    padding: 24px;
  }

  .social-login {
    flex-direction: column;
  }

  .floating-shapes {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 20px;
  }

  .logo .logo-text {
    font-size: 24px;
  }
}
</style>
