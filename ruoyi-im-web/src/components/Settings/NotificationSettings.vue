<template>
  <div class="notification-settings">
    <div class="setting-group">
      <h3 class="group-title">消息通知</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">桌面通知</div>
            <div class="item-desc">收到新消息时在屏幕右下角显示通知</div>
          </div>
          <el-switch v-model="localSettings.notifications.enabled" @change="handleChange" />
        </div>
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">提示音</div>
            <div class="item-desc">收到新消息时播放声音</div>
          </div>
          <el-switch v-model="localSettings.notifications.sound" @change="handleChange" />
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">快捷键</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">发送消息</div>
          </div>
          <el-select v-model="localSettings.shortcuts.send" size="default" style="width: 140px" @change="handleChange">
            <el-option label="Enter" value="enter" />
            <el-option label="Ctrl + Enter" value="ctrl-enter" />
          </el-select>
        </div>
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">截屏</div>
            <div class="item-desc">全局截图快捷键</div>
          </div>
          <div class="shortcut-display">Alt + A</div>
        </div>
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">唤起搜索</div>
            <div class="item-desc">快速搜索联系人或消息</div>
          </div>
          <div class="shortcut-display">Ctrl + K</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const localSettings = reactive(JSON.parse(JSON.stringify(props.modelValue)))

// Watch for external changes
watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

const handleChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
  emit('change')
}
</script>

<style scoped lang="scss">
.notification-settings {
  padding-bottom: 20px;
}

.setting-group {
  margin-bottom: 32px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
  padding-left: 4px;
}

.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  
  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }
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

.shortcut-display {
  display: inline-block;
  padding: 6px 10px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  border-radius: 6px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-weight: 500;
  color: var(--dt-text-primary);
  min-width: 80px;
  text-align: center;
}
</style>
