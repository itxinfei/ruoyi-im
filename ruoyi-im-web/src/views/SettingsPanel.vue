<template>
  <div class="desktop-settings-panel">
    <!-- 分组设置列表 (Desktop Optimized) -->
    <div class="settings-scroll-area custom-scrollbar">
      <!-- 1. 通知设置组 -->
      <div class="settings-group">
        <div class="group-title">
          通知提醒
        </div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="label-box">
              <span class="title">消息通知</span>
              <span class="desc">接收新消息推送提醒</span>
            </div>
            <el-switch v-model="notificationSettings.messageNotify" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="label-box">
              <span class="title">声音提示</span>
              <span class="desc">收到消息时播放提示音</span>
            </div>
            <el-switch v-model="notificationSettings.soundNotify" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="label-box">
              <span class="title">振动提醒</span>
            </div>
            <el-switch v-model="notificationSettings.vibrateNotify" @change="saveNotificationSettings" />
          </div>
        </div>
      </div>

      <!-- 2. 隐私设置组 -->
      <div class="settings-group">
        <div class="group-title">
          隐私与安全
        </div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="label-box">
              <span class="title">在线状态</span>
              <span class="desc">允许他人查看我的实时在线状态</span>
            </div>
            <el-switch v-model="privacySettings.showOnlineStatus" @change="savePrivacySettings" />
          </div>
          <div class="setting-row">
            <div class="label-box">
              <span class="title">陌生人消息</span>
              <span class="desc">接收未添加好友的用户消息</span>
            </div>
            <el-switch v-model="privacySettings.allowStrangerMsg" @change="savePrivacySettings" />
          </div>
        </div>
      </div>

      <!-- 3. 常规设置组 -->
      <div class="settings-group">
        <div class="group-title">
          常规
        </div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="label-box">
              <span class="title">外观主题</span>
            </div>
            <el-select
              v-model="generalSettings.theme"
              size="default"
              style="width: 140px"
              @change="handleThemeChange"
            >
              <el-option label="跟随系统" value="auto" />
              <el-option label="亮色模式" value="light" />
              <el-option label="深色模式" value="dark" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="label-box">
              <span class="title">语言选择</span>
            </div>
            <el-select
              v-model="generalSettings.language"
              size="default"
              style="width: 140px"
              @change="saveGeneralSettings"
            >
              <el-option label="简体中文" value="zh_CN" />
              <el-option label="English" value="en_US" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 4. 黑名单 -->
      <div class="settings-group">
        <div class="group-title">
          名单管理
        </div>
        <div class="settings-card">
          <div class="setting-row clickable" @click="activeMenu = 'blocked'">
            <div class="label-box">
              <span class="title">黑名单</span>
              <span class="desc">已屏蔽 {{ blockedUsers.length }} 位联系人</span>
            </div>
            <span class="material-icons-outlined arrow">chevron_right</span>
          </div>
        </div>
      </div>

      <!-- 关于信息 -->
      <div class="about-footer">
        <div class="app-brand">
          IM
        </div>
        <div class="app-version">
          Version 1.2.0 (Stable)
        </div>
        <div class="copyright">
          © 2026 XINLIU. All Rights Reserved.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getNotificationSettings, updateNotificationSettings,
  getPrivacySettings, updatePrivacySettings,
  getGeneralSettings, updateGeneralSettings,
  getBlockedUsers
} from '@/api/im/config'
import { ElMessage } from 'element-plus'

const notificationSettings = ref({ messageNotify: true, soundNotify: true, vibrateNotify: false })
const privacySettings = ref({ showOnlineStatus: true, allowStrangerMsg: false })
const generalSettings = ref({ language: 'zh_CN', theme: 'light' })
const blockedUsers = ref([])

// 加载逻辑 (保持接口闭环)
const loadSettings = async () => {
  try {
    const [notif, priv, gen, blocked] = await Promise.all([
      getNotificationSettings(), getPrivacySettings(), getGeneralSettings(), getBlockedUsers()
    ])
    if (notif.code === 200) notificationSettings.value = notif.data
    if (priv.code === 200) privacySettings.value = priv.data
    if (gen.code === 200) generalSettings.value = gen.data
    if (blocked.code === 200) blockedUsers.value = blocked.data || []
  } catch (e) {
    console.warn('部分设置项加载失败', e)
  }
}

const saveNotificationSettings = () => updateNotificationSettings(notificationSettings.value).then(() => ElMessage.success('已应用通知变更'))
const savePrivacySettings = () => updatePrivacySettings(privacySettings.value).then(() => ElMessage.success('已更新隐私策略'))
const saveGeneralSettings = () => updateGeneralSettings(generalSettings.value).then(() => ElMessage.success('已保存配置'))

const handleThemeChange = (val) => {
  const isDark = val === 'dark' || (val === 'auto' && window.matchMedia('(prefers-color-scheme: dark)').matches)
  document.documentElement.classList.toggle('dark', isDark)
  localStorage.setItem('im_theme_dark', String(isDark))
  saveGeneralSettings()
}

onMounted(loadSettings)
</script>

<style scoped lang="scss">
.desktop-settings-panel {
  height: 100%;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
}

.settings-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-xl);
}

.settings-group {
  margin-bottom: var(--dt-spacing-xl);

  .group-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-secondary);
    margin-bottom: 8px;
    padding-left: 4px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.settings-card {
  background: var(--dt-bg-card);
  border-radius: 12px;
  border: 1px solid var(--dt-border-light);
  overflow: hidden;
  box-shadow: var(--dt-shadow-1);

  .setting-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--dt-border-lighter);
    transition: background 0.2s;

    &:last-child { border-bottom: none; }

    &.clickable {
      cursor: pointer;
      &:hover { background: var(--dt-bg-body); }
    }

    .label-box {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .title {
        font-size: 14px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }

      .desc {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }

    .arrow {
      color: var(--dt-border-color);
      font-size: 20px;
    }
  }
}

.about-footer {
  margin-top: 40px;
  padding-bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .app-brand {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
    border-radius: 12px;
    color: var(--dt-text-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 18px;
    box-shadow: var(--dt-shadow-2);
  }

  .app-version {
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-secondary);
  }

  .copyright {
    font-size: 11px;
    color: var(--dt-text-tertiary);
  }
}

// 暗色模式兼容
:global(.dark) .desktop-settings-panel {
  background: var(--dt-bg-body-dark);
  .settings-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    .setting-row {
      border-color: var(--dt-border-dark);
      .label-box .title { color: var(--dt-text-primary-dark); }
      .label-box .desc { color: var(--dt-text-tertiary-dark); }
      &.clickable:hover { background: var(--dt-bg-hover-dark); }
    }
  }
  .about-footer .app-version { color: var(--dt-text-tertiary-dark); }
}
</style>
