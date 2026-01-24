<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <div class="logo-icon">钉</div>
        </div>
        <h1 class="title">IM即时通讯</h1>
        <p class="subtitle">IM即时通讯</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">
            记住密码
          </el-checkbox>
        </el-form-item>

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

      <div class="login-footer">
        <p>© 2026  IM. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    await loginFormRef.value.validate()

    loading.value = true

    // 调用 Vuex 登录 action
    await store.dispatch('user/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    // 处理记住用户名逻辑
    if (loginForm.rememberMe) {
      localStorage.setItem('remembered_username', loginForm.username)
    } else {
      localStorage.removeItem('remembered_username')
    }

    ElMessage.success('登录成功')

    // 跳转到主页 (或跳转回重定向前的页面)
    const redirectUrl = router.currentRoute.value.query.redirect || '/'
    router.push(redirectUrl)
    
  } catch (error) {
    console.error('登录失败:', error)
    const errorMsg = error.message || (error.response?.data?.msg) || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 初始化：读取记住的用户名
const rememberedUsername = localStorage.getItem('remembered_username')
if (rememberedUsername) {
  loginForm.username = rememberedUsername
  loginForm.rememberMe = true
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  // 使用生成的背景图
  background: url('@/assets/images/login-bg.png') no-repeat center center;
  background-size: cover;
  padding: 24px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(15, 23, 42, 0.4); // 遮罩层
    z-index: 1;
  }
}

.login-box {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.7); // 玻璃拟态基础色
  backdrop-filter: blur(12px); // 高斯模糊
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 40px;
  z-index: 10;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.35);
    background: rgba(255, 255, 255, 0.75);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;

    .logo-icon {
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, #1677ff, #0958d9);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      font-size: 30px;
      font-weight: bold;
      box-shadow: 0 10px 20px rgba(22, 119, 255, 0.3);
    }
  }

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 8px 0;
    letter-spacing: -0.5px;
  }

  .subtitle {
    font-size: 14px;
    color: #64748b;
    margin: 0;
    font-weight: 400;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    padding: 12px 16px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.8);
    box-shadow: none !important;
    border: 1px solid #e2e8f0;
    transition: all 0.2s ease;

    &:hover {
      border-color: #1677ff;
    }

    &.is-focus {
      border-color: #1677ff;
      border-width: 2px;
    }
  }

  :deep(.el-checkbox) {
    .el-checkbox__label {
      color: #475569;
      font-size: 14px;
    }
  }

  .login-button {
    width: 100%;
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    background: linear-gradient(135deg, #1677ff, #0958d9);
    border: none;
    box-shadow: 0 10px 25px -5px rgba(22, 119, 255, 0.4);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    letter-spacing: 1px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 15px 30px -5px rgba(22, 119, 255, 0.5);
      background: linear-gradient(135deg, #4096ff, #1677ff);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  margin-top: 32px;
  text-align: center;

  p {
    font-size: 12px;
    color: #94a3b8;
    margin: 0;
  }
}

// 适配移动端
@media (max-width: 480px) {
  .login-box {
    padding: 32px 24px;
    border-radius: 16px;
  }
  .login-header .title {
    font-size: 24px;
  }
}
</style>
