<template>
  <div class="notification-settings">
    <!-- 消息通知 -->
    <section class="setting-group">
      <h3 class="group-title">消息通知</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Bell /></el-icon>
            桌面通知
          </span>
          <el-switch v-model="settings.enabled" size="small" @change="handleSettingChange('enabled', $event)" :loading="saving" />
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Headset /></el-icon>
            提示音
          </span>
          <div class="row-value">
            <el-select
              v-model="settings.soundType"
              size="small"
              style="width: 70px"
              :disabled="!settings.enabled"
              @change="handleSettingChange('soundType', $event)"
            >
              <el-option label="默认" value="default" />
              <el-option label="清脆" value="light" />
              <el-option label="柔和" value="soft" />
            </el-select>
            <el-button
              v-if="settings.enabled"
              link
              type="primary"
              size="small"
              @click="testSound"
              :loading="testingSound"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
          </div>
          <el-switch v-model="settings.sound" size="small" @change="handleSettingChange('sound', $event)" :disabled="!settings.enabled" />
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><MessageBox /></el-icon>
            弹窗预览
          </span>
          <el-switch v-model="settings.showPreview" size="small" @change="handleSettingChange('showPreview', $event)" :disabled="!settings.enabled" />
        </div>
      </div>

      <!-- 免打扰时间设置 -->
      <div v-if="settings.dndEnabled" class="dnd-time-row">
        <span class="dnd-label">生效时间</span>
        <div class="time-picker-group">
          <el-time-picker
            v-model="dndStartTime"
            size="small"
            placeholder="开始"
            format="HH:mm"
            style="width: 80px"
            @change="handleDndTimeChange"
            :disabled="saving"
          />
          <span class="time-separator">至</span>
          <el-time-picker
            v-model="dndEndTime"
            size="small"
            placeholder="结束"
            format="HH:mm"
            style="width: 80px"
            @change="handleDndTimeChange"
            :disabled="saving"
          />
        </div>
      </div>

      <!-- 快捷键 -->
      <div class="setting-row">
        <span class="row-label">
          <el-icon class="row-icon"><Promotion /></el-icon>
          发送消息
        </span>
        <el-select v-model="settings.send" size="small" style="width: 100px" @change="handleSettingChange('send', $event)">
          <el-option label="Enter" value="enter" />
          <el-option label="Ctrl+Enter" value="ctrl-enter" />
        </el-select>
      </div>
    </section>
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
  max-width: 100%;
  box-sizing: border-box;
}

.setting-group {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.setting-list {
  background: #fafafa;
  border: 1px solid #e8e8e8;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  min-height: 40px;
  border-bottom: 1px solid #e8e8e8;

  &:last-child {
    border-bottom: none;
  }
}

.row-label {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;

  .row-icon {
    font-size: 14px;
    color: #999;
  }
}

.row-value {
  font-size: 13px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dnd-time-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f5f5f5;
  border-top: none;
}

.dnd-label {
  font-size: 12px;
  color: #666;
}

.time-picker-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-separator {
  font-size: 12px;
  color: #999;
}

// 深色模式
.dark {
  .group-title {
    color: #ccc;
    border-color: #333;
  }

  .setting-list {
    background: #252525;
    border-color: #333;
  }

  .setting-row {
    border-color: #333;

    &.clickable:hover {
      background: #333;
    }
  }

  .row-label {
    color: #999;

    .row-icon {
      color: #666;
    }
  }

  .row-value {
    color: #ccc;
  }

  .dnd-time-row {
    background: #333;
    border-color: #333;
  }

  .dnd-label {
    color: #999;
  }

  .time-separator {
    color: #666;
  }
}
</style>
