<template>
  <div class="notification-settings">
    <!-- 消息通知 -->
    <section class="setting-section">
      <h3 class="section-title">消息通知</h3>
      <div class="setting-list card-style">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-blue">
              <el-icon><Bell /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">桌面通知</div>
            <div class="item-desc">开启后，将在桌面角标处显示新消息提醒</div>
          </div>
          <el-switch v-model="localSettings.notifications.enabled" @change="handleChange" />
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-green">
              <el-icon><Headset /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">新消息提示音</div>
            <div class="item-desc">收到消息时播放提示音</div>
          </div>
          <div class="item-action-group">
            <el-button 
              v-if="localSettings.notifications.sound" 
              type="primary" 
              link 
              size="small" 
              class="test-sound-btn"
              @click="testSound"
            >
              <el-icon><VideoPlay /></el-icon>
              试听
            </el-button>
            <el-select
              v-model="localSettings.notifications.soundType"
              size="small"
              style="width: 100px; margin-right: 12px;"
              @change="handleChange"
              :disabled="!localSettings.notifications.sound"
            >
              <el-option label="默认" value="default" />
              <el-option label="清脆" value="light" />
              <el-option label="柔和" value="soft" />
            </el-select>
            <el-switch v-model="localSettings.notifications.sound" @change="handleChange" />
          </div>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-orange">
              <el-icon><MessageBox /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">消息弹窗预览</div>
            <div class="item-desc">通知中显示发送者姓名和消息摘要</div>
          </div>
          <el-switch v-model="localSettings.notifications.showPreview" @change="handleChange" />
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-purple">
              <el-icon><Mute /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">免打扰模式 (DND)</div>
            <div class="item-desc">在设定时间内自动静音所有消息提醒</div>
          </div>
          <el-switch v-model="localSettings.notifications.dndEnabled" @change="handleChange" />
        </div>
      </div>

      <!-- 免打扰时间设置 -->
      <transition name="el-zoom-in-top">
        <div v-if="localSettings.notifications.dndEnabled" class="dnd-config-card">
          <div class="config-header">
            <el-icon><Clock /></el-icon>
            <span>免打扰生效时间</span>
          </div>
          <div class="time-range-picker">
            <el-time-picker
              v-model="dndStartTime"
              size="default"
              placeholder="开始时间"
              format="HH:mm"
              style="flex: 1"
              @change="handleDndTimeChange"
            />
            <span class="separator">至</span>
            <el-time-picker
              v-model="dndEndTime"
              size="default"
              placeholder="结束时间"
              format="HH:mm"
              style="flex: 1"
              @change="handleDndTimeChange"
            />
          </div>
        </div>
      </transition>
    </section>

    <!-- 快捷键 -->
    <section class="setting-section">
      <h3 class="section-title">快捷键</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-cyan">
              <el-icon><Promotion /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">发送消息</div>
            <div class="item-desc">选择发送消息的快捷键</div>
          </div>
          <el-select
            v-model="localSettings.shortcuts.send"
            size="default"
            style="width: 140px"
            @change="handleChange"
          >
            <el-option label="Enter" value="enter" />
            <el-option label="Ctrl + Enter" value="ctrl-enter" />
          </el-select>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-pink">
              <el-icon><Crop /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">截屏</div>
            <div class="item-desc">全局截图快捷键</div>
          </div>
          <div class="shortcut-display">
            <kbd>Alt</kbd>
            <span>+</span>
            <kbd>A</kbd>
          </div>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-indigo">
              <el-icon><Search /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">唤起搜索</div>
            <div class="item-desc">快速搜索联系人或消息</div>
          </div>
          <div class="shortcut-display">
            <kbd>Ctrl</kbd>
            <span>+</span>
            <kbd>K</kbd>
          </div>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-teal">
              <el-icon><ChatLineRound /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">快速跳转</div>
            <div class="item-desc">快速切换到未读会话</div>
          </div>
          <div class="shortcut-display">
            <kbd>Ctrl</kbd>
            <span>+</span>
            <kbd>Tab</kbd>
          </div>
        </div>
      </div>
    </section>

    <!-- 通知例外 -->
    <section class="setting-section">
      <h3 class="section-title">通知例外</h3>
      <div class="setting-card">
        <div class="exception-list">
          <div class="exception-item">
            <div class="exception-info">
              <el-icon><Star /></el-icon>
              <span>重要联系人</span>
            </div>
            <el-button type="primary" link>管理</el-button>
          </div>
          <div class="exception-desc">重要联系人的消息将始终显示通知</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'
import {
  Mute,
  Promotion,
  Crop,
  Search,
  ChatLineRound,
  Star,
  VideoPlay,
  Clock
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

// 免打扰时间
const dndStartTime = ref(null)
const dndEndTime = ref(null)

// 初始化时间
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
  // 模拟播放提示音（实际开发中可调用系统提示音或音频文件）
  const audio = new Audio('/assets/audio/notification.mp3')
  audio.play().catch(() => {
    // 忽略音频播放错误（如用户未点击过页面）
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
  max-width: 680px;
}

// 分区标题
.setting-section {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
  padding-left: 4px;
}

// 设置列表
.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 16px;
  border-bottom: 1px solid var(--dt-border-light);
  transition: background-color 0.2s;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

// 设置列表优化 (统一样式)
.card-style {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.test-sound-btn {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  
  .el-icon {
    font-size: 16px;
  }
}

// 图标样式
.item-icon-wrapper {
  flex-shrink: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.bg-blue {
    background: rgba(22, 119, 255, 0.1);
    color: #1677ff;
  }

  &.bg-green {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }

  &.bg-orange {
    background: rgba(250, 140, 22, 0.1);
    color: #fa8c16;
  }

  &.bg-purple {
    background: rgba(114, 46, 209, 0.1);
    color: #722ed1;
  }

  &.bg-cyan {
    background: rgba(19, 194, 194, 0.1);
    color: #13c2c2;
  }

  &.bg-pink {
    background: rgba(235, 47, 150, 0.1);
    color: #eb2f96;
  }

  &.bg-indigo {
    background: rgba(83, 82, 237, 0.1);
    color: #5352ed;
  }

  &.bg-teal {
    background: rgba(0, 150, 136, 0.1);
    color: #009688;
  }
}

// 内容区域
.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.item-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

// 操作组
.item-action-group {
  display: flex;
  align-items: center;
}

// 免打扰时间卡片
.dnd-config-card {
  margin-top: 16px;
  padding: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .config-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 16px;
    
    .el-icon {
      color: var(--dt-brand-color);
      font-size: 18px;
    }
  }
}

.time-range-picker {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .separator {
    color: var(--dt-text-tertiary);
    font-size: 13px;
    font-weight: 500;
  }
}

// 快捷键显示
.shortcut-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--dt-text-secondary);

  kbd {
    display: inline-block;
    padding: 4px 8px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 4px;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-text-primary);
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.1);

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
      box-shadow: 0 1px 0 rgba(255, 255, 255, 0.1);
    }
  }
}

// 设置卡片
.setting-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 20px;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

// 例外列表
.exception-list {
  .exception-item {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .exception-info {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 15px;
      font-weight: 500;
      color: var(--dt-text-primary);

      .el-icon {
        font-size: 20px;
        color: #faad14;
      }
    }
  }

  .exception-desc {
    margin-top: 8px;
    font-size: 13px;
    color: var(--dt-text-secondary);
    padding-left: 32px;
  }
}

// 响应式适配
@media (max-width: 640px) {
  .setting-item {
    padding: 14px 16px;
    flex-wrap: wrap;
  }

  .item-action-group {
    width: 100%;
    margin-top: 12px;
    justify-content: flex-end;
  }

  .time-range-setting {
    flex-wrap: wrap;

    .range-label {
      width: 100%;
      margin-bottom: 8px;
    }
  }

  .shortcut-display {
    kbd {
      padding: 3px 6px;
      font-size: 11px;
    }
  }
}
</style>
