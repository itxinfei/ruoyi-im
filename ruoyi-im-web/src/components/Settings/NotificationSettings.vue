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
          <el-switch v-model="localSettings.notifications.enabled" size="small" @change="handleChange" />
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Headset /></el-icon>
            提示音
          </span>
          <div class="row-value">
            <el-select
              v-model="localSettings.notifications.soundType"
              size="small"
              style="width: 70px"
              :disabled="!localSettings.notifications.sound"
              @change="handleChange"
            >
              <el-option label="默认" value="default" />
              <el-option label="清脆" value="light" />
              <el-option label="柔和" value="soft" />
            </el-select>
            <el-button
              v-if="localSettings.notifications.sound"
              link
              type="primary"
              size="small"
              @click="testSound"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-switch v-model="localSettings.notifications.sound" size="small" @change="handleChange" />
          </div>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><MessageBox /></el-icon>
            弹窗预览
          </span>
          <el-switch v-model="localSettings.notifications.showPreview" size="small" @change="handleChange" />
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Mute /></el-icon>
            免打扰
          </span>
          <el-switch v-model="localSettings.notifications.dndEnabled" size="small" @change="handleChange" />
        </div>
      </div>

      <!-- 免打扰时间设置 -->
      <div v-if="localSettings.notifications.dndEnabled" class="dnd-time-row">
        <span class="dnd-label">生效时间</span>
        <div class="time-picker-group">
          <el-time-picker
            v-model="dndStartTime"
            size="small"
            placeholder="开始"
            format="HH:mm"
            style="width: 80px"
            @change="handleDndTimeChange"
          />
          <span class="time-separator">至</span>
          <el-time-picker
            v-model="dndEndTime"
            size="small"
            placeholder="结束"
            format="HH:mm"
            style="width: 80px"
            @change="handleDndTimeChange"
          />
        </div>
      </div>
    </section>

    <!-- 快捷键 -->
    <section class="setting-group">
      <h3 class="group-title">快捷键</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Promotion /></el-icon>
            发送消息
          </span>
          <el-select v-model="localSettings.shortcuts.send" size="small" style="width: 100px" @change="handleChange">
            <el-option label="Enter" value="enter" />
            <el-option label="Ctrl+Enter" value="ctrl-enter" />
          </el-select>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Crop /></el-icon>
            截屏
          </span>
          <span class="shortcut-kbd">Alt + A</span>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Search /></el-icon>
            唤起搜索
          </span>
          <span class="shortcut-kbd">Ctrl + K</span>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><ChatLineRound /></el-icon>
            快速跳转
          </span>
          <span class="shortcut-kbd">Ctrl + Tab</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'
import {
  Bell,
  Headset,
  MessageBox,
  Mute,
  Promotion,
  Crop,
  Search,
  ChatLineRound,
  VideoPlay
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const localSettings = reactive({
  notifications: {
    enabled: true,
    sound: true,
    soundType: 'default',
    showPreview: true,
    dndEnabled: false,
    dndStart: '22:00',
    dndEnd: '08:00',
    ...props.modelValue.notifications
  },
  shortcuts: {
    send: 'enter',
    ...props.modelValue.shortcuts
  }
})

const dndStartTime = ref(null)
const dndEndTime = ref(null)

const initDndTime = () => {
  if (localSettings.notifications.dndStart) {
    const [hours, minutes] = localSettings.notifications.dndStart.split(':')
    dndStartTime.value = new Date(2000, 0, 1, hours, minutes)
  }
  if (localSettings.notifications.dndEnd) {
    const [hours, minutes] = localSettings.notifications.dndEnd.split(':')
    dndEndTime.value = new Date(2000, 0, 1, hours, minutes)
  }
}

initDndTime()

watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
    initDndTime()
  }
}, { deep: true })

const handleDndTimeChange = () => {
  if (dndStartTime.value) {
    const hours = dndStartTime.value.getHours().toString().padStart(2, '0')
    const minutes = dndStartTime.value.getMinutes().toString().padStart(2, '0')
    localSettings.notifications.dndStart = `${hours}:${minutes}`
  }
  if (dndEndTime.value) {
    const hours = dndEndTime.value.getHours().toString().padStart(2, '0')
    const minutes = dndEndTime.value.getMinutes().toString().padStart(2, '0')
    localSettings.notifications.dndEnd = `${hours}:${minutes}`
  }
  handleChange()
}

const testSound = () => {
  const audio = new Audio('/assets/audio/notification.mp3')
  audio.play().catch(() => {
    console.log('播放提示音测试')
  })
}

const handleChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
  emit('change')
}
</script>

<style scoped lang="scss">
.notification-settings {
  padding: 0;
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

.shortcut-kbd {
  font-family: monospace;
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 3px;
  border: 1px solid #e0e0e0;
}

.dnd-time-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-top: none;

  .dnd-label {
    font-size: 12px;
    color: #666;
  }

  .time-picker-group {
    display: flex;
    align-items: center;
    gap: 8px;

    .time-separator {
      font-size: 12px;
      color: #999;
    }
  }
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

  .shortcut-kbd {
    color: #999;
    background: #333;
    border-color: #444;
  }

  .dnd-time-row {
    background: #2a2a2a;
    border-color: #333;

    .dnd-label {
      color: #999;
    }

    .time-separator {
      color: #666;
    }
  }
}
</style>
