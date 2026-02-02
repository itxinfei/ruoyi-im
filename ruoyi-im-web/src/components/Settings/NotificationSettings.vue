<template>
  <div class="notification-settings">
    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">消息通知</h3>
        <p class="section-desc">管理桌面通知、提示音和免打扰设置</p>
      </div>

      <div class="setting-card">
        <!-- 桌面通知 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-title">
              <el-icon class="item-icon"><Bell /></el-icon>
              桌面通知
            </div>
            <div class="item-desc">开启后将收到系统桌面通知</div>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="settings.enabled" 
              :loading="saving" 
              @change="handleSettingChange('enabled', $event)" 
            />
          </div>
        </div>

        <el-divider />

        <!-- 提示音 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-title">
              <el-icon class="item-icon"><Headset /></el-icon>
              提示音
            </div>
            <div class="item-desc">新消息到达时的声音提醒</div>
          </div>
          <div class="item-action">
            <div class="sound-controls">
              <el-select
                v-model="settings.soundType"
                size="small"
                style="width: 100px"
                :disabled="!settings.enabled"
                @change="handleSettingChange('soundType', $event)"
              >
                <el-option label="默认" value="default" />
                <el-option label="清脆" value="light" />
                <el-option label="柔和" value="soft" />
              </el-select>
              <el-button
                v-if="settings.enabled"
                circle
                type="info"
                link
                size="small"
                @click="testSound"
                :loading="testingSound"
              >
                <el-icon><VideoPlay /></el-icon>
              </el-button>
            </div>
            <el-switch 
              v-model="settings.sound" 
              :disabled="!settings.enabled" 
              @change="handleSettingChange('sound', $event)" 
            />
          </div>
        </div>

        <el-divider />

        <!-- 弹窗预览 -->
        <div class="setting-item">
          <div class="item-main">
            <div class="item-title">
              <el-icon class="item-icon"><MessageBox /></el-icon>
              弹窗预览
            </div>
            <div class="item-desc">通知中显示消息内容预览</div>
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
    </div>

    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">免打扰设置</h3>
        <p class="section-desc">设置自动免打扰时间段</p>
      </div>

      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-title">
              <el-icon class="item-icon"><Mute /></el-icon>
              自动免打扰
            </div>
            <div class="item-desc">在指定时间段内自动静音</div>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="settings.dndEnabled" 
              @change="handleSettingChange('dndEnabled', $event)" 
            />
          </div>
        </div>

        <div v-if="settings.dndEnabled" class="setting-sub-panel">
          <div class="dnd-time-row">
            <span class="dnd-label">生效时间段</span>
            <div class="time-picker-group">
              <el-time-picker
                v-model="dndStartTime"
                size="small"
                placeholder="开始时间"
                format="HH:mm"
                style="width: 110px"
                @change="handleDndTimeChange"
                :disabled="saving"
              />
              <span class="time-separator">至</span>
              <el-time-picker
                v-model="dndEndTime"
                size="small"
                placeholder="结束时间"
                format="HH:mm"
                style="width: 110px"
                @change="handleDndTimeChange"
                :disabled="saving"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">快捷键</h3>
        <p class="section-desc">设置发送消息的快捷键方式</p>
      </div>

      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-title">
              <el-icon class="item-icon"><Promotion /></el-icon>
              发送消息
            </div>
            <div class="item-desc">聊天窗口发送消息的快捷键</div>
          </div>
          <div class="item-action">
            <el-select 
              v-model="settings.send" 
              style="width: 140px" 
              @change="handleSettingChange('send', $event)"
            >
              <el-option label="Enter" value="enter" />
              <el-option label="Ctrl + Enter" value="ctrl-enter" />
            </el-select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted } from 'vue'
import { Bell, Headset, MessageBox, Mute, Promotion, VideoPlay } from '@element-plus/icons-vue'
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

// 免打扰时间（用于时间选择器）
const dndStartTime = ref(null)
const dndEndTime = ref(null)

// 加载状态
const saving = ref(false)
const testingSound = ref(false)

// 初始化
onMounted(async () => {
  await loadSettings()
})

// 加载设置
const loadSettings = async () => {
  try {
    const res = await getNotificationSetting()
    if (res.code === 200 && res.data) {
      // 合并后端设置到本地状态
      Object.assign(settings, {
        enabled: res.data.enabled === 1,
        sound: res.data.soundEnabled === 1,
        soundType: res.data.soundType || 'default',
        showPreview: res.data.showPreview === 1,
        dndEnabled: res.data.dndEnabled === 1,
        dndStart: res.data.dndStartTime || '22:00',
        dndEnd: res.data.dndEndTime || '08:00'
      })
      
      // 初始化时间选择器
      if (res.data.dndEnabled === 1) {
        initDndTime()
      }
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
    ElMessage.error('加载设置失败')
  }
}

// 初始化免打扰时间
const initDndTime = () => {
  const now = new Date()
  const [startHours, startMinutes] = settings.dndStart.split(':').map(Number)
  const [endHours, endMinutes] = settings.dndEnd.split(':').map(Number)
  
  dndStartTime.value = new Date(2000, 0, 1, startHours, startMinutes, 0)
  dndEndTime.value = new Date(2000, 0, 1, endHours, endMinutes, 0)
}

// 防抖定时器（必须在 handleSettingChange 之前声明）
const saveTimer = ref(null)

// 处理设置变更
const handleSettingChange = async (key, value) => {
  // 更新本地状态
  settings[key] = value

  // 如果开启了免打扰，初始化时间
  if (key === 'dndEnabled' && value) {
    initDndTime()
  }

  // 防抖保存到后端
  if (saveTimer.value) clearTimeout(saveTimer.value)
  saveTimer.value = setTimeout(async () => {
    await saveSettings()
  }, 500)
}

// 处理免打扰时间变更
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

// 保存设置到后端
const saveSettings = async () => {
  try {
    saving.value = true
    
    // 转换为后端格式
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
    } else {
      ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

// 测试提示音
const testSound = async () => {
  try {
    testingSound.value = true
    const audio = new Audio('/assets/audio/notification.mp3')
    await audio.play()
    setTimeout(() => {
      testingSound.value = false
    }, 1000)
  } catch (error) {
    console.error('播放提示音失败:', error)
    ElMessage.error('播放失败')
  }
}

// 监听 props 变化
watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(settings)) {
    Object.assign(settings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.notification-settings {
  padding: 0;
  box-sizing: border-box;
}

.setting-section {
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  margin-bottom: 12px;
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary, #333);
    margin: 0 0 4px 0;
  }
  
  .section-desc {
    font-size: 12px;
    color: var(--text-color-secondary, #909399);
    margin: 0;
  }
}

.setting-card {
  background: var(--bg-color-overlay, #fff);
  border: 1px solid var(--border-color-light, #e4e7ed);
  border-radius: 8px;
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: var(--bg-color-hover, #f5f7fa);
  }
}

.item-main {
  flex: 1;
  margin-right: 20px;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-primary, #303133);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  
  .item-icon {
    font-size: 16px;
    color: var(--text-color-secondary, #909399);
  }
}

.item-desc {
  font-size: 12px;
  color: var(--text-color-secondary, #909399);
  line-height: 1.4;
  margin-left: 24px;
}

.item-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sound-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.setting-sub-panel {
  padding: 12px 20px;
  background-color: var(--bg-color-page, #f5f7fa);
  border-top: 1px solid var(--border-color-light, #e4e7ed);
  margin-left: 20px;
  margin-right: 20px;
  margin-bottom: 16px;
  border-radius: 6px;
}

.dnd-time-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dnd-label {
  font-size: 13px;
  color: var(--text-color-regular, #606266);
}

.time-picker-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-separator {
  color: var(--text-color-secondary, #909399);
}

:deep(.el-divider) {
  margin: 0;
  border-color: var(--border-color-lighter, #ebeef5);
}
</style>
