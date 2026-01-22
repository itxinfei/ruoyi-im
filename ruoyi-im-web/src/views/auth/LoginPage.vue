<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <div class="logo-icon">钉</div>
        </div>
        <h1 class="title">RuoYi IM</h1>
        <p class="subtitle">企业级即时通讯平台</p>
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
        <p>© 2026 RuoYi IM. All rights reserved.</p>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 36px 32px 32px;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .logo {
    display: flex;
    justify-content: center;
    margin-bottom: 16px;

    .logo-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #0089ff, #0066cc);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      font-size: 26px;
      font-weight: bold;
      box-shadow: 0 8px 16px rgba(0, 137, 255, 0.3);
    }
  }

  .title {
    font-size: 26px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 6px 0;
  }

  .subtitle {
    font-size: 13px;
    color: #8c8c8c;
    margin: 0;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    padding: 12px 16px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e8e8e8 inset;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px #0089ff inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px #0089ff inset;
    }
  }

  :deep(.el-checkbox) {
    .el-checkbox__label {
      color: #595959;
      font-size: 14px;
    }
  }

  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 8px;
    background: linear-gradient(135deg, #0089ff, #0066cc);
    border: none;
    box-shadow: 0 4px 12px rgba(0, 137, 255, 0.3);
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #0066cc, #0052a3);
      box-shadow: 0 6px 16px rgba(0, 137, 255, 0.4);
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  margin-top: 20px;
  text-align: center;

  p {
    font-size: 12px;
    color: #bfbfbf;
    margin: 0;
  }
}
</style>
