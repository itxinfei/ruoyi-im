<template>
  <div class="storage-settings">
    <div class="setting-group">
      <h3 class="group-title">缓存管理</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">本地缓存</div>
            <div class="item-desc">当前占用空间: {{ cacheSize }}</div>
          </div>
          <el-button type="danger" plain size="small" @click="$emit('clear-cache')">清理缓存</el-button>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">数据备份</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">导出聊天记录</div>
            <div class="item-desc">将所有聊天记录导出为 JSON 文件</div>
          </div>
          <el-button type="primary" plain size="small" @click="$emit('export-chat')">导出</el-button>
        </div>
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">退出时保留数据</div>
            <div class="item-desc">退出登录后保留本地聊天记录</div>
          </div>
          <el-switch v-model="localSettings.data.keepOnLogout" @change="handleChange" />
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
  },
  cacheSize: {
    type: String,
    default: '0 MB'
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'clear-cache', 'export-chat'])

const localSettings = reactive(JSON.parse(JSON.stringify(props.modelValue)))

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
.storage-settings {
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
</style>
