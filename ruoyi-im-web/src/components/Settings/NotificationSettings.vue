<template>
  <div class="notification-settings">
    <!-- 消息通知设置 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-green">
          <span class="material-icons-outlined">notifications</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            消息通知
          </h3>
          <p class="section-desc">
            配置新消息的提醒方式
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <!-- 桌面通知 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">desktop_windows</span>
              <span class="item-label">桌面通知</span>
            </div>
            <span class="item-desc">接收新消息时在桌面显示通知弹窗</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="settings.enabled" 
              :loading="saving" 
              @change="handleSettingChange('enabled', $event)" 
            />
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- 提示音 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">volume_up</span>
              <span class="item-label">提示音</span>
            </div>
            <span class="item-desc">新消息到达时播放声音提醒</span>
          </div>
          <div class="item-action action-group">
            <el-select
              v-model="settings.soundType"
              size="small"
              style="width: 90px"
              :disabled="!settings.enabled || !settings.sound"
              @change="handleSettingChange('soundType', $event)"
            >
              <el-option
                label="默认"
                value="default"
              />
              <el-option
                label="清脆"
                value="light"
              />
              <el-option
                label="柔和"
                value="soft"
              />
            </el-select>
            <el-button
              v-if="settings.enabled && settings.sound"
              circle
              type="info"
              plain
              size="small"
              :loading="testingSound"
              @click="testSound"
            >
              <span
                class="material-icons-outlined"
                style="font-size: 16px;"
              >play_arrow</span>
            </el-button>
            <el-switch 
              v-model="settings.sound" 
              :disabled="!settings.enabled" 
              @change="handleSettingChange('sound', $event)" 
            />
          </div>
        </div>

        <el-divider class="item-divider" />

        <!-- 弹窗预览 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">preview</span>
              <span class="item-label">消息预览</span>
            </div>
            <span class="item-desc">在通知中显示消息内容摘要</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="settings.showPreview" 
              :disabled="!settings.enabled" 
              @change="handleSettingChange('showPreview', $event)" 
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 免打扰设置 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-purple">
          <span class="material-icons-outlined">do_not_disturb_on</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            免打扰模式
          </h3>
          <p class="section-desc">
            在指定时间段内自动静音
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">nightlight</span>
              <span class="item-label">自动免打扰</span>
            </div>
            <span class="item-desc">在设定的时间段内自动关闭通知</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="settings.dndEnabled" 
              @change="handleSettingChange('dndEnabled', $event)" 
            />
          </div>
        </div>

        <Transition name="expand">
          <div
            v-if="settings.dndEnabled"
            class="dnd-panel"
          >
            <div class="dnd-content">
              <div class="time-row">
                <span class="time-label">开始时间</span>
                <el-time-picker
                  v-model="dndStartTime"
                  size="default"
                  placeholder="开始时间"
                  format="HH:mm"
                  style="width: 120px"
                  :disabled="saving"
                  @change="handleDndTimeChange"
                />
              </div>
              <div class="time-divider">
                <span class="material-icons-outlined">arrow_forward</span>
              </div>
              <div class="time-row">
                <span class="time-label">结束时间</span>
                <el-time-picker
                  v-model="dndEndTime"
                  size="default"
                  placeholder="结束时间"
                  format="HH:mm"
                  style="width: 120px"
                  :disabled="saving"
                  @change="handleDndTimeChange"
                />
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </section>

    <!-- 快捷键设置 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">keyboard</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            快捷键
          </h3>
          <p class="section-desc">
            设置发送消息的快捷键
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">send</span>
              <span class="item-label">发送消息</span>
            </div>
            <span class="item-desc">选择发送消息的快捷键组合</span>
          </div>
          <div class="item-action">
            <el-radio-group 
              v-model="settings.send" 
              size="small"
              @change="handleSettingChange('send', $event)"
            >
              <el-radio-button label="enter">
                <span class="kbd">Enter</span>
              </el-radio-button>
              <el-radio-button label="ctrl-enter">
                <span class="kbd">Ctrl</span> + <span class="kbd">Enter</span>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationSetting, updateNotificationSetting } from '@/api/im/notificationSetting'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 后端设置数据
const settings = reactive({
  enabled: true,
  sound: true,
  soundType: 'default',
  showPreview: true,
  dndEnabled: false,
  dndStart: '22:00',
  dndEnd: '08:00',
  send: 'enter'
})

// 免打扰时间
const dndStartTime = ref(null)
const dndEndTime = ref(null)

// 加载状态
const saving = ref(false)
const testingSound = ref(false)
const saveTimer = ref(null)

onMounted(async () => {
  await loadSettings()
})

const loadSettings = async () => {
  try {
    const res = await getNotificationSetting()
    if (res.code === 200 && res.data) {
      Object.assign(settings, {
        enabled: res.data.enabled === 1,
        sound: res.data.soundEnabled === 1,
        soundType: res.data.soundType || 'default',
        showPreview: res.data.showPreview === 1,
        dndEnabled: res.data.dndEnabled === 1,
        dndStart: res.data.dndStartTime || '22:00',
        dndEnd: res.data.dndEndTime || '08:00'
      })
      
      if (res.data.dndEnabled === 1) {
        initDndTime()
      }
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
  }
}

const initDndTime = () => {
  const now = new Date()
  const [startHours, startMinutes] = settings.dndStart.split(':').map(Number)
  const [endHours, endMinutes] = settings.dndEnd.split(':').map(Number)
  
  dndStartTime.value = new Date(2000, 0, 1, startHours, startMinutes, 0)
  dndEndTime.value = new Date(2000, 0, 1, endHours, endMinutes, 0)
}

const handleSettingChange = async (key, value) => {
  settings[key] = value

  if (key === 'dndEnabled' && value) {
    initDndTime()
  }

  if (saveTimer.value) {clearTimeout(saveTimer.value)}
  saveTimer.value = setTimeout(async () => {
    await saveSettings()
  }, 500)
}

const handleDndTimeChange = () => {
  if (dndStartTime.value && dndEndTime.value) {
    const startHours = dndStartTime.value.getHours().toString().padStart(2, '0')
    const startMinutes = dndStartTime.value.getMinutes().toString().padStart(2, '0')
    const endHours = dndEndTime.value.getHours().toString().padStart(2, '0')
    const endMinutes = dndEndTime.value.getMinutes().toString().padStart(2, '0')
    
    settings.dndStart = `${startHours}:${startMinutes}`
    settings.dndEnd = `${endHours}:${endMinutes}`
    
    handleSettingChange('dndStart', settings.dndStart)
    handleSettingChange('dndEnd', settings.dndEnd)
  }
}

const saveSettings = async () => {
  try {
    saving.value = true
    
    const backendData = {
      enabled: settings.enabled ? 1 : 0,
      desktopNotification: settings.enabled ? 1 : 0,
      soundEnabled: settings.sound ? 1 : 0,
      soundType: settings.soundType,
      customSoundUrl: null,
      showPreview: settings.showPreview ? 1 : 0,
      dndEnabled: settings.dndEnabled ? 1 : 0,
      dndStart: settings.dndEnabled ? settings.dndStart : null,
      dndEnd: settings.dndEnabled ? settings.dndEnd : null,
      mentionOnly: 0
    }
    
    const res = await updateNotificationSetting(backendData)
    if (res.code === 200) {
      ElMessage.success('设置已保存')
      emit('change')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

const testSound = async () => {
  try {
    testingSound.value = true
    const audio = new Audio('/assets/audio/notification.mp3')
    await audio.play()
    setTimeout(() => {
      testingSound.value = false
    }, 1000)
  } catch (error) {
    testingSound.value = false
    ElMessage.error('播放失败')
  }
}

watch(() => props.modelValue, newVal => {
  if (JSON.stringify(newVal) !== JSON.stringify(settings)) {
    Object.assign(settings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.notification-settings {
  max-width: 720px;
}

.setting-section {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.section-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
    color: white;
  }
  
  &.bg-gradient-green {
    background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
  }
  
  &.bg-gradient-purple {
    background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
  }
  
  &.bg-gradient-blue {
    background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
  }
}

.section-title-group {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.setting-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
}

.item-divider {
  margin: 0;
  border-color: var(--dt-border-light);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

.item-main {
  flex: 1;
  margin-right: 20px;
}

.item-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-label {
  font-size: 14px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.item-icon {
  font-size: 20px;
  color: var(--dt-text-secondary);
}

.item-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  padding-left: 28px;
}

.item-action {
  display: flex;
  align-items: center;
  gap: 12px;
  
  &.action-group {
    gap: 8px;
  }
}

// 免打扰面板
.dnd-panel {
  background: var(--dt-bg-page);
  border-top: 1px solid var(--dt-border-light);
  padding: 20px;
}

.dnd-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.time-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 12px;
  color: var(--dt-text-secondary);
}

.time-divider {
  color: var(--dt-text-tertiary);
  
  span {
    font-size: 20px;
  }
}

// 键盘按键样式
.kbd {
  display: inline-block;
  padding: 2px 6px;
  font-family: monospace;
  font-size: 12px;
  background: var(--dt-bg-hover);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-sm);
  box-shadow: 0 1px 0 var(--dt-border-color);
}

// 展开动画
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  opacity: 1;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  padding: 0;
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
  
  .item-divider {
    border-color: var(--dt-border-dark);
  }
  
  .setting-item:hover {
    background-color: var(--dt-bg-hover-dark);
  }
  
  .dnd-panel {
    background: var(--dt-bg-body-dark);
    border-color: var(--dt-border-dark);
  }
  
  .kbd {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);
    box-shadow: 0 1px 0 var(--dt-border-dark);
  }
}
</style>
