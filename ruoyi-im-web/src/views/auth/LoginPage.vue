<template>
  <div class="login-container" :class="{ 'dark-mode': isDark }">
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
      <!-- 左侧装饰 -->
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
              <span class="material-icons-outlined">chat</span>
              <span>即时通讯</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-outlined">groups</span>
              <span>群组协作</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-outlined">cloud_done</span>
              <span>云端同步</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-section">
        <div class="login-header">
          <h2 class="welcome-title">欢迎回来</h2>
          <p class="welcome-subtitle">请登录您的账户</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <button 
            :class="['tab-btn', { active: loginType === 'password' }]"
            @click="switchLoginType('password')"
          >
            账号密码
          </button>
          <button 
            :class="['tab-btn', { active: loginType === 'sms' }]"
            @click="switchLoginType('sms')"
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
            <div class="input-wrapper">
              <span class="material-icons-outlined input-icon">person_outline</span>
              <el-input
                v-model="loginForm.username"
                placeholder="用户名 / 手机号"
                size="large"
                clearable
                @focus="handleFocus('username')"
                @blur="handleBlur('username')"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <span class="material-icons-outlined input-icon">lock_outline</span>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                clearable
                @focus="handleFocus('password')"
                @blur="handleBlur('password')"
              />
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">
              记住密码
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
            <div class="input-wrapper">
              <span class="material-icons-outlined input-icon">phone</span>
              <el-input
                v-model="smsForm.phone"
                placeholder="请输入手机号"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-form-item prop="code">
            <div class="input-wrapper">
              <span class="material-icons-outlined input-icon">sms</span>
              <el-input
                v-model="smsForm.code"
                placeholder="请输入验证码"
                size="large"
                clearable
              >
                <template #append>
                  <el-button
                    :disabled="smsCountdown > 0"
                    @click="sendSMSCode"
                    class="code-btn"
                  >
                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
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
            <span class="material-icons-outlined">login</span>
            {{ loginType === 'password' ? '登录' : '验证登录' }}
          </template>
          <span v-else>登录中...</span>
        </el-button>

        <!-- 第三方登录 -->
        <div class="third-party-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="third-party-icons">
            <el-button circle size="large" class="third-party-btn" title="微信登录">
              <span class="material-icons-outlined">wechat</span>
            </el-button>
            <el-button circle size="large" class="third-party-btn" title="企业微信">
              <span class="material-icons-outlined">business</span>
            </el-button>
            <el-button circle size="large" class="third-party-btn" title="钉钉">
              <span class="material-icons-outlined">chat_bubble</span>
            </el-button>
          </div>
        </div>

        <!-- 登录页脚 -->
        <div class="login-footer">
          <p>© 2026 IM即时通讯系统. All rights reserved.</p>
          <div class="footer-links">
            <el-link :underline="false" type="info">用户协议</el-link>
            <el-link :underline="false" type="info">隐私政策</el-link>
            <el-link :underline="false" type="info">帮助中心</el-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 主题切换按钮 -->
    <el-button 
      circle 
      class="theme-toggle" 
      @click="toggleTheme"
      :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
    >
      <span class="material-icons-outlined">
        {{ isDark ? 'light_mode' : 'dark_mode' }}
      </span>
    </el-button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  // 清除表单验证
  if (type === 'password') {
    smsFormRef.value?.clearValidate()
  } else {
    loginFormRef.value?.clearValidate()
  }
}

// 处理输入框聚焦
const handleFocus = (field) => {
  // 聚焦动画效果
  console.log('Focus:', field)
}

const handleBlur = (field) => {
  // 失焦动画效果
  console.log('Blur:', field)
}

// 发送短信验证码
const sendSMSCode = async () => {
  if (!smsForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  try {
    // 验证手机号
    await smsFormRef.value.validateField('phone')
    
    // 调用发送短信接口
    // await store.dispatch('auth/sendSMS', smsForm.phone)
    
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
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
    // 表单验证
    await formRef.validate()
    
    loading.value = true
    
    if (loginType.value === 'password') {
      // 账号密码登录
      await store.dispatch('user/login', {
        username: formData.username,
        password: formData.password
      })
      
      // 处理记住用户名
      if (formData.rememberMe) {
        localStorage.setItem('remembered_username', formData.username)
      } else {
        localStorage.removeItem('remembered_username')
      }
    } else {
      // 短信验证码登录
      // await store.dispatch('user/smsLogin', {
      //   phone: formData.phone,
      //   code: formData.code
      // })
      ElMessage.info('短信登录功能开发中')
      return
    }
    
    ElMessage.success('登录成功')
    
    // 跳转到主页
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
  // 读取记住的用户名
  const rememberedUsername = localStorage.getItem('remembered_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 24px;
  position: relative;
  overflow: hidden;
  
  &.dark-mode {
    background: linear-gradient(135deg, #1a1c2e 0%, #3d415c 100%);
  }
}

// 背景装饰
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  
  .bg-shapes {
    position: absolute;
    width: 100%;
    height: 100%;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      animation: float 6s ease-in-out infinite;
      
      &.shape-1 {
        width: 200px;
        height: 200px;
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }
      
      &.shape-2 {
        width: 150px;
        height: 150px;
        top: 70%;
        right: 10%;
        animation-delay: 2s;
      }
      
      &.shape-3 {
        width: 100px;
        height: 100px;
        bottom: 20%;
        left: 50%;
        animation-delay: 4s;
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.8;
  }
}

// 登录卡片
.login-card {
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 10;
  
  .dark-mode & {
    background: #1e293b;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  }
}

// 左侧装饰
.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  padding: 60px 40px;
  color: white;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Ccircle cx='30' cy='30' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    animation: rotate 30s linear infinite;
  }
  
  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
}

.decoration-content {
  position: relative;
  z-index: 2;
  
  .logo-wrapper {
    text-align: center;
    margin-bottom: 60px;
    
    .logo {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      backdrop-filter: blur(10px);
      
      .logo-icon {
        font-size: 40px;
        color: white;
      }
    }
    
    .app-name {
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 8px;
      letter-spacing: 2px;
    }
    
    .app-desc {
      font-size: 16px;
      opacity: 0.8;
      margin: 0;
    }
  }
  
  .features {
    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
      opacity: 0.9;
      transition: all 0.3s ease;
      
      &:hover {
        opacity: 1;
        transform: translateX(10px);
      }
      
      .material-icons-outlined {
        font-size: 24px;
      }
      
      span {
        font-size: 16px;
      }
    }
  }
}

// 右侧登录表单
.login-form-section {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 600px;
  
  .login-header {
    text-align: center;
    margin-bottom: 40px;
    
    .welcome-title {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 8px;
      
      .dark-mode & {
        color: #f1f5f9;
      }
    }
    
    .welcome-subtitle {
      font-size: 16px;
      color: #64748b;
      margin: 0;
      
      .dark-mode & {
        color: #94a3b8;
      }
    }
  }
  
  .login-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 32px;
    background: #f1f5f9;
    padding: 4px;
    border-radius: 12px;
    
    .dark-mode & {
      background: #334155;
    }
    
    .tab-btn {
      flex: 1;
      padding: 12px;
      border: none;
      background: transparent;
      color: #64748b;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &.active {
        background: white;
        color: #1677ff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        .dark-mode & {
          background: #1e293b;
          color: #60a5fa;
        }
      }
      
      &:hover:not(.active) {
        color: #475569;
      }
    }
  }
  
  .login-form {
    margin-bottom: 32px;
    
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }
    
    .input-wrapper {
      position: relative;
      
      .input-icon {
        position: absolute;
        left: 16px;
        top: 50%;
        transform: translateY(-50%);
        color: #94a3b8;
        font-size: 20px;
        z-index: 10;
        pointer-events: none;
        transition: color 0.3s ease;
      }
      
      :deep(.el-input__wrapper) {
        padding-left: 48px;
        border-radius: 12px;
        border: 2px solid #e2e8f0;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: #cbd5e1;
        }
        
        &.is-focus {
          border-color: #1677ff;
          box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
        }
        
        .dark-mode & {
          background: #334155;
          border-color: #475569;
          
          &:hover {
            border-color: #64748b;
          }
        }
      }
      
      :deep(.el-input__inner) {
        font-size: 15px;
        color: #1e293b;
        
        .dark-mode & {
          color: #f1f5f9;
          background: transparent;
        }
        
        &::placeholder {
          color: #94a3b8;
        }
      }
    }
    
    .code-btn {
      border: none;
      background: transparent;
      color: #1677ff;
      font-size: 14px;
      
      &:hover {
        color: #0958d9;
      }
      
      &:disabled {
        color: #94a3b8;
      }
    }
    
    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32px;
      
      :deep(.el-checkbox__label) {
        color: #64748b;
        font-size: 14px;
        
        .dark-mode & {
          color: #94a3b8;
        }
      }
    }
  }
  
  .login-button {
    width: 100%;
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
    border: none;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.3s ease;
    margin-bottom: 32px;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 25px rgba(22, 119, 255, 0.35);
      background: linear-gradient(135deg, #4096ff 0%, #1677ff 100%);
    }
    
    &:active {
      transform: translateY(0);
    }
    
    .material-icons-outlined {
      font-size: 20px;
    }
  }
  
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
        background: #e2e8f0;
        
        .dark-mode & {
          background: #475569;
        }
      }
      
      span {
        background: white;
        padding: 0 16px;
        color: #94a3b8;
        font-size: 14px;
        position: relative;
        
        .dark-mode & {
          background: #1e293b;
        }
      }
    }
    
    .third-party-icons {
      display: flex;
      justify-content: center;
      gap: 16px;
      
      .third-party-btn {
        width: 48px;
        height: 48px;
        border: 2px solid #e2e8f0;
        background: white;
        color: #64748b;
        transition: all 0.3s ease;
        
        .dark-mode & {
          background: #334155;
          border-color: #475569;
          color: #94a3b8;
        }
        
        &:hover {
          border-color: #1677ff;
          color: #1677ff;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
        }
      }
    }
  }
  
  .login-footer {
    text-align: center;
    margin-top: auto;
    
    p {
      font-size: 12px;
      color: #94a3b8;
      margin: 0 0 8px;
    }
    
    .footer-links {
      display: flex;
      justify-content: center;
      gap: 16px;
      
      .el-link {
        font-size: 12px;
      }
    }
  }
}

// 主题切换按钮
.theme-toggle {
  position: fixed;
  top: 24px;
  right: 24px;
  width: 48px;
  height: 48px;
  background: white;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  transition: all 0.3s ease;
  
  .dark-mode & {
    background: #1e293b;
    border-color: #475569;
    color: #f1f5f9;
  }
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .login-card {
    max-width: 600px;
    
    .login-decoration {
      display: none;
    }
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: 16px;
  }
  
  .login-form-section {
    padding: 40px 24px;
    
    .welcome-title {
      font-size: 24px;
    }
    
    .login-button {
      height: 48px;
      font-size: 15px;
    }
  }
}
</style>