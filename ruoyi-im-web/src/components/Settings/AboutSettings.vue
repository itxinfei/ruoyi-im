<template>
  <div class="about-settings">
    <div class="about-content">
      <!-- Logo 区域 -->
      <div class="logo-section">
        <div class="logo-container">
          <div class="app-logo">
            <span class="logo-text">IM</span>
          </div>
          <div class="logo-glow" />
        </div>
        <h1 class="app-name">
          RuoYi IM
        </h1>
        <div class="version-tag">
          <span class="version-label">版本</span>
          <span class="version-number">4.1.0</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button
          type="primary"
          round
          class="action-btn primary"
          @click="checkUpdate"
        >
          <span
            class="material-icons-outlined"
            style="font-size: 18px;"
          >update</span>
          检查更新
        </el-button>
        <el-button
          round
          class="action-btn"
          @click="openWebsite"
        >
          <span
            class="material-icons-outlined"
            style="font-size: 18px;"
          >language</span>
          官方网站
        </el-button>
      </div>

      <!-- 功能特性 -->
      <div class="features-section">
        <div class="feature-item">
          <span class="material-icons-outlined feature-icon">check_circle</span>
          <span class="feature-text">企业级即时通讯</span>
        </div>
        <div class="feature-item">
          <span class="material-icons-outlined feature-icon">check_circle</span>
          <span class="feature-text">端到端加密</span>
        </div>
        <div class="feature-item">
          <span class="material-icons-outlined feature-icon">check_circle</span>
          <span class="feature-text">多端同步</span>
        </div>
      </div>

      <!-- 法律链接 -->
      <div class="legal-section">
        <a
          href="#"
          class="legal-link"
        >服务协议</a>
        <span class="divider">·</span>
        <a
          href="#"
          class="legal-link"
        >隐私政策</a>
        <span class="divider">·</span>
        <a
          href="#"
          class="legal-link"
        >开源许可</a>
      </div>

      <!-- 版权信息 -->
      <div class="copyright-section">
        <p class="copyright-text">
          Copyright © {{ currentYear }} RuoYi-IM Team. All Rights Reserved.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const currentYear = computed(() => new Date().getFullYear())

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

const openWebsite = () => {
  window.open('https://github.com/yangzongzhuan/RuoYi', '_blank')
}
</script>

<style scoped lang="scss">
.about-settings {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.about-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 400px;
}

// Logo 区域
.logo-section {
  margin-bottom: 32px;
}

.logo-container {
  position: relative;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.app-logo {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-hover) 100%);
  border-radius: var(--dt-radius-2xl);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.logo-text {
  font-size: 36px;
  font-weight: var(--dt-font-weight-bold);
  color: white;
  letter-spacing: 2px;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140px;
  height: 140px;
  background: var(--dt-brand-color);
  opacity: 0.15;
  filter: blur(24px);
  border-radius: 50%;
  z-index: 1;
}

.app-name {
  font-size: 24px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
  margin: 0 0 12px 0;
}

.version-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--dt-bg-hover);
  border-radius: 100px;
  font-size: 13px;
}

.version-label {
  color: var(--dt-text-secondary);
}

.version-number {
  color: var(--dt-brand-color);
  font-weight: var(--dt-font-weight-medium);
  font-family: monospace;
}

// 操作按钮
.action-section {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}

.action-btn {
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  
  &.primary {
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }
}

// 功能特性
.features-section {
  display: flex;
  gap: 24px;
  margin-bottom: 40px;
  flex-wrap: wrap;
  justify-content: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .feature-icon {
    font-size: 16px;
    color: var(--dt-success);
  }
  
  .feature-text {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}

// 法律链接
.legal-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.legal-link {
  font-size: 13px;
  color: var(--dt-text-secondary);
  text-decoration: none;
  transition: color 0.2s ease;
  
  &:hover {
    color: var(--dt-brand-color);
  }
}

.divider {
  color: var(--dt-border-color);
}

// 版权信息
.copyright-section {
  text-align: center;
}

.copyright-text {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin: 0;
}

// 暗黑模式适配
.dark {
  .logo-glow {
    opacity: 0.25;
  }
  
  .version-tag {
    background: var(--dt-bg-hover-dark);
  }
  
  .feature-text {
    color: var(--dt-text-secondary-dark);
  }
  
  .copyright-text {
    color: var(--dt-text-tertiary-dark);
  }
}
</style>
