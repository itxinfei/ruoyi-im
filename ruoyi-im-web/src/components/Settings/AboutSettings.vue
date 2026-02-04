<template>
  <div class="about-settings">
    <div class="about-content">
      <div class="logo-container">
        <div class="app-logo">
          <span class="logo-icon">IM</span>
        </div>
        <div class="logo-glow"></div>
      </div>
      
      <h1 class="app-name">RuoYi IM</h1>
      <div class="version-badge">Version 4.1.0</div>
      
      <div class="action-buttons">
        <el-button type="primary" round class="update-btn" @click="checkUpdate">
          检查更新
        </el-button>
        <el-button round @click="openWebsite">官方网站</el-button>
      </div>
      
      <div class="legal-links">
        <el-link :underline="false">服务协议</el-link>
        <span class="divider">|</span>
        <el-link :underline="false">隐私政策</el-link>
        <span class="divider">|</span>
        <el-link :underline="false">开源许可</el-link>
      </div>
      
      <p class="copyright">Copyright © 2025 RuoYi-IM Team. All Rights Reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

const openWebsite = () => {
  window.open('https://github.com', '_blank')
}
</script>

<style scoped lang="scss">
.about-settings {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 40px;
  box-sizing: border-box;
}

.about-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-container {
  position: relative;
  margin-bottom: 24px;
}

.app-logo {
  width: 96px;
  height: 96px;
  background: linear-gradient(135deg, var(--el-color-primary, #409eff) 0%, #0e5fd9 100%);
  border-radius: var(--dt-radius-3xl);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: var(--dt-shadow-brand-strong);
}

.logo-icon {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2px;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  background: var(--el-color-primary, #409eff);
  opacity: 0.2;
  filter: blur(20px);
  border-radius: 50%;
  z-index: 1;
}

.app-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
  margin: 0 0 12px;
}

.version-badge {
  display: inline-block;
  padding: 4px 12px;
  background: var(--bg-color-hover, #f5f7fa);
  border-radius: 100px;
  font-size: 13px;
  color: var(--text-color-secondary, #909399);
  margin-bottom: 32px;
  font-family: monospace;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
  
  .update-btn {
    min-width: 120px;
  }
}

.legal-links {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  
  .divider {
    color: var(--border-color-light, #e4e7ed);
  }
}

.copyright {
  font-size: 12px;
  color: var(--text-color-placeholder, #c0c4cc);
  margin: 0;
}
</style>
