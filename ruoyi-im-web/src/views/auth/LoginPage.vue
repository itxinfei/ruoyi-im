<template>
  <div class="dt-premium-login">
    <!-- 1. 沉浸式动态背景 (对齐钉钉：深邃且冷静) -->
    <div class="v5-bg-engine">
      <div class="mesh-gradient"></div>
      <div class="noise-overlay"></div>
    </div>

    <!-- 2. 极简登录容器 -->
    <main class="v5-login-card">
      <div class="v5-card-header">
        <div class="v5-logo-box">
          <el-icon><Promotion /></el-icon>
        </div>
        <h1 class="v5-title">RuoYi-IM</h1>
        <p class="v5-subtitle">企业级数字化办公协同平台</p>
      </div>

      <!-- 去 Web 化的表单：无边框感、专注输入 -->
      <form class="v5-form" @submit.prevent="handleLogin">
        <div class="v5-field" :class="{ active: usernameFocused }">
          <label>用户名 / 手机号</label>
          <div class="input-wrap">
            <input 
              v-model="loginForm.username" 
              type="text" 
              placeholder="请输入账号" 
              @focus="usernameFocused = true"
              @blur="usernameFocused = false"
            />
          </div>
        </div>

        <div class="v5-field" :class="{ active: passwordFocused }">
          <label>登录密码</label>
          <div class="input-wrap">
            <input 
              v-model="loginForm.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="请输入密码"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
            />
            <el-icon class="eye-icon" @click="showPassword = !showPassword">
              <View v-if="!showPassword" /><Hide v-else />
            </el-icon>
          </div>
        </div>

        <div class="v5-options">
          <el-checkbox v-model="loginForm.rememberMe">自动登录</el-checkbox>
          <button type="button" class="forget-link">忘记密码</button>
        </div>

        <!-- 实体按压感登录按钮 -->
        <button class="v5-login-btn" :class="{ loading }" :disabled="loading">
          <span v-if="!loading">登录</span>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
        </button>
      </form>

      <footer class="v5-footer">
        <span class="v-ver">Version 8.2.0 Premium</span>
        <div class="v-links">
          <span>注册账号</span>
          <span class="dot">·</span>
          <span>隐私协议</span>
        </div>
      </footer>
    </main>

    <!-- 底部版权 (对齐钉钉：微缩极淡) -->
    <div class="v5-copyright">
      Powered by RuoYi Fullstack Architecture © 2026
    </div>
  </div>
</template>

<script setup lang="js">
import { ref, reactive } from 'vue'
import { Promotion, View, Hide, Loading } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)

const loginForm = reactive({ username: '', password: '', rememberMe: false })

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return
  loading.value = true
  try {
    await store.dispatch('user/login', loginForm)
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally { loading.value = false }
}
</script>

<style scoped lang="scss">
.dt-premium-login {
  width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center;
  background: var(--dt-bg-body); position: relative; overflow: hidden;
}

// 1. 动态背景引擎
.v5-bg-engine {
  position: absolute; inset: 0; z-index: 1;
  .mesh-gradient {
    position: absolute; inset: 0;
    background: radial-gradient(at 10% 20%, rgba(39, 126, 251, 0.05) 0px, transparent 50%),
                radial-gradient(at 90% 10%, rgba(114, 46, 209, 0.04) 0px, transparent 50%),
                radial-gradient(at 50% 80%, rgba(34, 171, 92, 0.03) 0px, transparent 50%);
  }
  .noise-overlay { position: absolute; inset: 0; opacity: 0.02; background: url('https://grainy-gradients.vercel.app/noise.svg'); filter: contrast(150%) brightness(100%); }
}

// 2. 极简卡片
.v5-login-card {
  width: 360px; z-index: 10; position: relative;
  display: flex; flex-direction: column; align-items: center;
}

.v5-card-header {
  text-align: center; margin-bottom: 40px;
  .v5-logo-box { width: 56px; height: 56px; background: var(--dt-brand-color); border-radius: 14px; @include flex-center; color: var(--dt-text-white); font-size: 32px; margin: 0 auto 20px; }
  .v5-title { font-size: 24px; font-weight: 800; color: var(--dt-text-primary); letter-spacing: -0.5px; }
  .v5-subtitle { font-size: 13px; color: var(--dt-text-tertiary); margin-top: 8px; }
}

// 3. 去Web化的表单
.v5-form {
  width: 100%;
  .v5-field {
    margin-bottom: 24px; border-bottom: 1.5px solid var(--dt-border-light); transition: var(--dt-transition-base);
    label { font-size: 11px; font-weight: 700; color: var(--dt-text-quaternary); text-transform: uppercase; margin-bottom: 4px; display: block; }
    .input-wrap { display: flex; align-items: center; height: 36px;
      input { flex: 1; border: none; background: transparent; outline: none; font-size: 15px; color: var(--dt-text-primary); &::placeholder { color: var(--dt-text-quaternary); } }
      .eye-icon { color: var(--dt-text-quaternary); cursor: pointer; &:hover { color: var(--dt-brand-color); } }
    }
    &.active { border-bottom-color: var(--dt-brand-color); }
  }
}

.v5-options {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px;
  :deep(.el-checkbox__label) { font-size: 12px; color: var(--dt-text-tertiary); }
  .forget-link { background: none; border: none; font-size: 12px; color: var(--dt-brand-color); cursor: pointer; }
}

.v5-login-btn {
  width: 100%; height: 44px; border: none; border-radius: 22px;
  background: var(--dt-brand-color); color: var(--dt-text-white); font-size: 15px; font-weight: 600;
  cursor: pointer; transition: var(--dt-transition-fast);
  &:hover { background: var(--dt-brand-hover); }
  &.loading { opacity: 0.8; cursor: not-allowed; }
}

.v5-footer {
  margin-top: 60px; text-align: center;
  .v-ver { font-size: 10px; color: var(--dt-text-quaternary); display: block; margin-bottom: 12px; }
  .v-links { font-size: 12px; color: var(--dt-text-tertiary); display: flex; align-items: center; gap: 8px; justify-content: center; span:not(.dot) { cursor: pointer; &:hover { color: var(--dt-text-primary); } } }
}

.v5-copyright { position: absolute; bottom: 24px; font-size: 10px; color: var(--dt-text-quaternary); }
</style>
