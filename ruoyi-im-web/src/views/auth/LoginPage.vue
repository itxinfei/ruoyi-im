<template>
  <div class="login-page" :class="{ 'dark': isDark }">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-gradient" />
      <div class="bg-grid" />
      <div class="bg-orb bg-orb-1" />
      <div class="bg-orb bg-orb-2" />
      <div class="bg-orb bg-orb-3" />
    </div>

    <!-- 登录容器 -->
    <div class="login-container">
      <!-- 左侧品牌展示 -->
      <div class="brand-showcase">
        <div class="brand-content">
          <h2>高效协作，从 IM 开始</h2>
          <p>安全、稳定的企业级即时通讯解决方案</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Message /></el-icon>
              <span>即时消息</span>
            </div>
            <div class="feature-item">
              <el-icon><Bell /></el-icon>
              <span>实时通知</span>
            </div>
            <div class="feature-item">
              <el-icon><FolderOpened /></el-icon>
              <span>文件共享</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-card">
        <!-- Logo 区域 -->
        <div class="login-header">
          <div class="logo-wrapper">
            <div class="logo">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="logo-glow" />
          </div>
          <h1>欢迎回来</h1>
          <p class="subtitle">登录您的账户继续协作</p>
        </div>

        <!-- 表单 -->
        <form @submit.prevent="handleLogin" class="login-form">
          <!-- 用户名输入 -->
          <div class="field-group" :class="{ focused: usernameFocused, filled: loginForm.username }">
            <label class="field-label">用户名</label>
            <div class="field-input-wrapper">
              <el-icon class="field-icon"><User /></el-icon>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                autocomplete="username"
                @focus="usernameFocused = true"
                @blur="usernameFocused = false"
              />
            </div>
          </div>

          <!-- 密码输入 -->
          <div class="field-group" :class="{ focused: passwordFocused, filled: loginForm.password }">
            <label class="field-label">密码</label>
            <div class="field-input-wrapper">
              <el-icon class="field-icon"><Lock /></el-icon>
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
                @focus="passwordFocused = true"
                @blur="passwordFocused = false"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                <el-icon>
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </button>
            </div>
          </div>

          <!-- 选项行 -->
          <div class="options-row">
            <label class="checkbox-wrapper">
              <input type="checkbox" v-model="loginForm.rememberMe" />
              <span class="checkbox-custom" />
              <span class="checkbox-label">记住我</span>
            </label>
            <button type="button" class="link-btn" @click="handleForgotPassword">忘记密码？</button>
          </div>

          <!-- 登录按钮 -->
          <button type="submit" class="btn-login" :class="{ loading }" :disabled="loading">
            <span v-if="!loading" class="btn-text">
              登录
              <el-icon class="btn-arrow"><ArrowRight /></el-icon>
            </span>
            <span v-else class="btn-loading">
              <span class="loading-dot" />
              <span class="loading-dot" />
              <span class="loading-dot" />
            </span>
          </button>
        </form>

        <!-- 底部信息 -->
        <div class="login-footer">
          <span>还没有账户？</span>
          <button type="button" class="link-btn">联系管理员</button>
        </div>
      </div>

    </div>

    <!-- 主题切换 -->
    <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
      <el-icon>
        <Sunny v-if="isDark" />
        <Moon v-else />
      </el-icon>
    </button>

    <!-- 版权信息 -->
    <div class="copyright">
      © 2026 IM 企业通讯平台. All rights reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Sunny,
  Moon,
  User,
  Lock,
  View,
  Hide,
  ArrowRight,
  Message,
  Bell,
  FolderOpened
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const store = useStore()
const router = useRouter()
const { isDark, toggleTheme } = useTheme()

const loading = ref(false)
const showPassword = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  try {
    loading.value = true

    await store.dispatch('user/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    if (loginForm.rememberMe) {
      localStorage.setItem('remembered_username', loginForm.username)
    } else {
      localStorage.removeItem('remembered_username')
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

onMounted(() => {
  const rememberedUsername = localStorage.getItem('remembered_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4f8;
  padding: 24px;
  position: relative;
  overflow: hidden;
  transition: background 0.3s ease;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: var(--dt-brand-color);
  opacity: 0.06;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(39, 126, 251, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(39, 126, 251, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  opacity: 0.3;

  &.bg-orb-1 {
    width: 400px;
    height: 400px;
    background: var(--dt-brand-color);
    top: -100px;
    right: -100px;
    opacity: 0.2;
  }

  &.bg-orb-2 {
    width: 300px;
    height: 300px;
    background: var(--dt-brand-color);
    bottom: -50px;
    left: -50px;
    opacity: 0.15;
  }

  &.bg-orb-3 {
    width: 200px;
    height: 200px;
    background: var(--dt-brand-color);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    opacity: 0.12;
  }
}

/* 登录容器 - 双栏布局 */
.login-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 560px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: var(--dt-shadow-2);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 登录卡片 */
.login-card {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 头部区域 */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-wrapper {
  position: relative;
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
}

.logo {
  width: 56px;
  height: 56px;
  background: var(--dt-brand-color);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  box-shadow: var(--dt-shadow-brand);

  .el-icon {
    font-size: 28px;
    color: #fff;
  }
}

.logo-glow {
  position: absolute;
  inset: -4px;
  background: var(--dt-brand-color);
  border-radius: 18px;
  opacity: 0.3;
}

h1 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .field-label {
    font-size: 13px;
    font-weight: 500;
    color: #374151;
  }
}

.field-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;

  .field-icon {
    position: absolute;
    left: 14px;
    font-size: 18px;
    color: #9ca3af;
    transition: color 0.2s;
  }

  input {
    width: 100%;
    height: 48px;
    padding: 0 44px;
    font-size: 15px;
    color: #1f2937;
    background: #f9fafb;
    border: 2px solid transparent;
    border-radius: 10px;
    outline: none;
    transition: all 0.2s ease;

    &::placeholder {
      color: #9ca3af;
    }

    &:hover {
      background: #f3f4f6;
    }
  }

  .password-toggle {
    position: absolute;
    right: 12px;
    padding: 4px;
    background: none;
    border: none;
    cursor: pointer;
    color: #9ca3af;
    transition: color 0.2s;

    .el-icon {
      font-size: 18px;
    }

    &:hover {
      color: #6b7280;
    }
  }
}

.field-group.focused {
  .field-icon {
    color: #277EFB;
  }

  input {
    background: #fff;
    border-color: #277EFB;
    box-shadow: var(--dt-shadow-brand);
  }
}

/* 选项行 */
.options-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  cursor: pointer;

  input {
    display: none;
  }

  .checkbox-custom {
    width: 18px;
    height: 18px;
    border: 2px solid #d1d5db;
    border-radius: 5px;
    position: relative;
    transition: all 0.2s;

    &::after {
      content: '';
      position: absolute;
      left: 5px;
      top: 2px;
      width: 5px;
      height: 9px;
      border: solid #fff;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
      opacity: 0;
      transition: opacity 0.2s;
    }
  }

  input:checked + .checkbox-custom {
    background: var(--dt-brand-color);
    border-color: #277EFB;

    &::after {
      opacity: 1;
    }
  }

  .checkbox-label {
    font-size: 13px;
    color: #6b7280;
  }
}

.link-btn {
  font-size: 13px;
  color: #277EFB;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 0;
  font-weight: 500;
  transition: color 0.2s;

  &:hover {
    color: #165DFF;
  }
}

/* 登录按钮 */
.btn-login {
  width: 100%;
  height: 50px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #277EFB 0%, #165DFF 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  margin-top: 8px;
  transition: background-color 0.25s, opacity 0.25s;
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #165DFF 0%, #277EFB 100%);
    opacity: 0;
    transition: opacity 0.25s;
  }

  &:hover {
    &::before {
      opacity: 1;
    }
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .btn-text {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
    z-index: 1;

    .btn-arrow {
      font-size: 16px;
      transition: transform 0.2s;
    }
  }

  &:hover .btn-arrow {
    opacity: 0.8;
  }

  .btn-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    position: relative;
    z-index: 1;
  }

  .loading-dot {
    width: 8px;
    height: 8px;
    background: #fff;
    border-radius: 50%;
    opacity: 0.6;

    &:nth-child(1) { opacity: 0.4; }
    &:nth-child(2) { opacity: 0.9; }
  }
}

/* 底部 */
.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
}

/* 品牌展示区 */
.brand-showcase {
  width: 380px;
  background: var(--dt-brand-color);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }
}

.brand-content {
  text-align: center;
  color: #fff;
  position: relative;
  z-index: 1;

  h2 {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 12px 0;
    letter-spacing: -0.3px;
  }

  p {
    font-size: 14px;
    opacity: 0.85;
    margin: 0 0 40px 0;
  }
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  transition: background-color 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }

  .el-icon {
    font-size: 20px;
  }

  span {
    font-size: 14px;
    font-weight: 500;
  }
}

/* 主题切换按钮 */
.theme-toggle {
  position: fixed;
  top: 24px;
  right: 24px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  color: #6b7280;
  transition: background-color 0.2s;
  z-index: 10;

  .el-icon {
    font-size: 20px;
  }

  &:hover {
    background: #f5f5f5;
  }
}

/* 版权信息 */
.copyright {
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #9ca3af;
  z-index: 10;
}

/* ============================================ */
/* Dark Mode */
/* ============================================ */
.dark {
  background: #0f0f1a;

  .bg-gradient {
    opacity: 0.05;
  }

  .bg-grid {
    background-image:
      linear-gradient(rgba(102, 126, 234, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(102, 126, 234, 0.05) 1px, transparent 1px);
  }

  .login-container {
    background: #1a1a2e;
    box-shadow: var(--dt-shadow-2);
  }

  .login-card {
    background: #1a1a2e;
  }

  h1 {
    color: #f3f4f6;
  }

  .subtitle {
    color: #9ca3af;
  }

  .field-label {
    color: #d1d5db;
  }

  .field-input-wrapper {
    input {
      background: #262640;
      color: #f3f4f6;

      &::placeholder {
        color: #6b7280;
      }

      &:hover {
        background: #2d2d4a;
      }
    }
  }

  .field-group.focused {
    input {
      background: #262640;
      border-color: #277EFB;
    }
  }

  .checkbox-label {
    color: #9ca3af;
  }

  .brand-showcase {
    background: var(--dt-brand-color);
  }

  .theme-toggle {
    background: #262640;
    color: #9ca3af;

    &:hover {
      background: #2d2d4a;
      color: #f3f4f6;
    }
  }

  .copyright {
    color: #6b7280;
  }
}

/* ============================================ */
/* 响应式设计 */
/* ============================================ */
@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-container {
    flex-direction: column;
    max-width: 400px;
    min-height: auto;
  }

  .login-card {
    padding: 36px 24px;
  }

  .brand-showcase {
    width: 100%;
    padding: 32px 24px;

    h2 {
      font-size: 20px;
    }
  }

  .feature-list {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }

  .feature-item {
    padding: 10px 16px;

    span {
      font-size: 13px;
    }
  }

  .theme-toggle {
    top: 16px;
    right: 16px;
    width: 40px;
    height: 40px;
  }

  .copyright {
    font-size: 11px;
  }
}
</style>
