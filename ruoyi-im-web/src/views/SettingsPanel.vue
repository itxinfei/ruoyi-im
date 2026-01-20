<template>
  <div class="settings-panel">
    <div class="settings-content">
      <div class="settings-header">
        <h1 class="settings-title">设置</h1>
        <p class="settings-subtitle">管理你的账号和应用偏好设置</p>
      </div>

      <div class="settings-section">
        <h2 class="section-title">账号设置</h2>
        <div class="settings-list">
          <div
            v-for="(setting, index) in accountSettings"
            :key="setting.id"
            class="setting-item"
          >
            <button class="setting-btn" @click="handleSettingClick(setting)">
              <div class="setting-left">
                <div class="setting-icon icon-blue">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                  <p v-if="setting.description" class="setting-desc">{{ setting.description }}</p>
                </div>
              </div>
              <el-icon class="setting-arrow"><ArrowRight /></el-icon>
            </button>
            <div v-if="index < accountSettings.length - 1" class="setting-divider"></div>
          </div>
        </div>
      </div>

      <div class="settings-section">
        <h2 class="section-title">通知设置</h2>
        <div class="settings-list">
          <div
            v-for="(setting, index) in notificationSettings"
            :key="setting.id"
            class="setting-item"
          >
            <div class="setting-btn setting-toggle">
              <div class="setting-left">
                <div class="setting-icon icon-green">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                </div>
              </div>
              <el-switch v-model="setting.value" />
            </div>
            <div v-if="index < notificationSettings.length - 1" class="setting-divider"></div>
          </div>
        </div>
      </div>

      <div class="settings-section">
        <h2 class="section-title">隐私设置</h2>
        <div class="settings-list">
          <div
            v-for="(setting, index) in privacySettings"
            :key="setting.id"
            class="setting-item"
          >
            <button
              v-if="setting.type === 'link'"
              class="setting-btn"
              @click="handleSettingClick(setting)"
            >
              <div class="setting-left">
                <div class="setting-icon icon-purple">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                  <p v-if="setting.description" class="setting-desc">{{ setting.description }}</p>
                </div>
              </div>
              <el-icon class="setting-arrow"><ArrowRight /></el-icon>
            </button>
            <div v-else class="setting-btn setting-toggle">
              <div class="setting-left">
                <div class="setting-icon icon-purple">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                  <p v-if="setting.description" class="setting-desc">{{ setting.description }}</p>
                </div>
              </div>
              <el-switch v-model="setting.value" />
            </div>
            <div v-if="index < privacySettings.length - 1" class="setting-divider"></div>
          </div>
        </div>
      </div>

      <div class="settings-section">
        <h2 class="section-title">通用设置</h2>
        <div class="settings-list">
          <div
            v-for="(setting, index) in generalSettings"
            :key="setting.id"
            class="setting-item"
          >
            <button class="setting-btn" @click="handleSettingClick(setting)">
              <div class="setting-left">
                <div class="setting-icon icon-orange">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                  <p v-if="setting.description" class="setting-desc">{{ setting.description }}</p>
                </div>
              </div>
              <el-icon class="setting-arrow"><ArrowRight /></el-icon>
            </button>
            <div v-if="index < generalSettings.length - 1" class="setting-divider"></div>
          </div>
        </div>
      </div>

      <div class="settings-section">
        <h2 class="section-title">其他</h2>
        <div class="settings-list">
          <div
            v-for="(setting, index) in otherSettings"
            :key="setting.id"
            class="setting-item"
          >
            <button class="setting-btn" @click="handleSettingClick(setting)">
              <div class="setting-left">
                <div class="setting-icon icon-gray">
                  <el-icon><component :is="setting.icon" /></el-icon>
                </div>
                <div class="setting-info">
                  <p class="setting-label">{{ setting.label }}</p>
                  <p v-if="setting.description" class="setting-desc">{{ setting.description }}</p>
                </div>
              </div>
              <el-icon class="setting-arrow"><ArrowRight /></el-icon>
            </button>
            <div v-if="index < otherSettings.length - 1" class="setting-divider"></div>
          </div>
        </div>
      </div>

      <div class="settings-section">
        <el-button class="logout-btn">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  User,
  Lock,
  Bell,
  View,
  Location,
  Brush,
  QuestionFilled,
  InfoFilled,
  ArrowRight,
  SwitchButton
} from '@element-plus/icons-vue'

const accountSettings = ref([
  {
    id: 'profile',
    icon: User,
    label: '个人资料',
    description: '修改头像、昵称等信息',
    type: 'link'
  },
  {
    id: 'account',
    icon: Lock,
    label: '账号与安全',
    description: '密码、手机号、邮箱',
    type: 'link'
  }
])

const notificationSettings = ref([
  {
    id: 'message',
    icon: Bell,
    label: '消息通知',
    value: true,
    type: 'toggle'
  },
  {
    id: 'meeting',
    icon: Bell,
    label: '会议通知',
    value: true,
    type: 'toggle'
  },
  {
    id: 'approval',
    icon: Bell,
    label: '审批通知',
    value: true,
    type: 'toggle'
  },
  {
    id: 'sound',
    icon: Bell,
    label: '声音提醒',
    value: false,
    type: 'toggle'
  }
])

const privacySettings = ref([
  {
    id: 'online',
    icon: View,
    label: '在线状态',
    description: '显示我的在线状态',
    type: 'link'
  },
  {
    id: 'read',
    icon: View,
    label: '已读回执',
    value: true,
    type: 'toggle'
  },
  {
    id: 'typing',
    icon: View,
    label: '正在输入',
    value: true,
    type: 'toggle'
  }
])

const generalSettings = ref([
  {
    id: 'language',
    icon: Location,
    label: '语言',
    description: '简体中文',
    type: 'link'
  },
  {
    id: 'theme',
    icon: Brush,
    label: '主题',
    description: '跟随系统',
    type: 'link'
  }
])

const otherSettings = ref([
  {
    id: 'help',
    icon: QuestionFilled,
    label: '帮助与反馈',
    type: 'link'
  },
  {
    id: 'about',
    icon: InfoFilled,
    label: '关于钉钉',
    description: '版本 7.0.0',
    type: 'link'
  }
])

const handleSettingClick = (setting) => {
  console.log('Setting clicked:', setting)
}
</script>

<style scoped>
.settings-panel {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.settings-content {
  max-width: 896px;
  margin: 0 auto;
  padding: 24px;
}

.settings-header {
  margin-bottom: 24px;

  .settings-title {
    font-size: 24px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 4px 0;
  }

  .settings-subtitle {
    font-size: 14px;
    color: #8c8c8c;
    margin: 0;
  }
}

.settings-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 16px 0;
}

.settings-list {
  display: flex;
  flex-direction: column;
}

.setting-item {
  position: relative;
}

.setting-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  background: transparent;
  border: none;
  text-align: left;

  &:hover {
    background-color: #f5f5f5;
  }

  &.setting-toggle {
    cursor: default;

    &:hover {
      background-color: transparent;
    }
  }
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.setting-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }

  &.icon-blue {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.icon-green {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.icon-purple {
    background-color: #f9f0ff;
    color: #722ed1;
  }

  &.icon-orange {
    background-color: #fff7e6;
    color: #fa8c16;
  }

  &.icon-gray {
    background-color: #f5f5f5;
    color: #595959;
  }
}

.setting-info {
  flex: 1;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 2px 0;
}

.setting-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.setting-arrow {
  font-size: 20px;
  color: #bfbfbf;
  flex-shrink: 0;
}

.setting-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 0;
}

.logout-btn {
  width: 100%;
  color: #f5222d;
  border-color: #ffccc7;
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    color: #f5222d;
    background-color: #fff1f0;
    border-color: #ffccc7;
  }
}
</style>
