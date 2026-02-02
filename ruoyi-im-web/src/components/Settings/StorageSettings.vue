<template>
  <div class="storage-settings">
    <div class="setting-group">
      <h3 class="group-title">缓存管理</h3>
      <div class="setting-list card-style">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-orange">
              <el-icon><Delete /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">图片与视频缓存</div>
            <div class="item-desc">当前占用空间: <span class="size-text">{{ cacheSize }}</span></div>
            <div class="item-tip">包含聊天对话中的图片、视频及文件预览</div>
          </div>
          <el-button type="danger" plain size="small" @click="$emit('clear-cache')">清理缓存</el-button>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">数据备份与安全</h3>
      <div class="setting-list card-style">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-blue">
              <el-icon><Download /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">导出聊天记录</div>
            <div class="item-desc">将所有本地聊天历史备份为 .json 文件</div>
          </div>
          <el-button type="primary" plain size="small" @click="$emit('export-chat')">立即导出</el-button>
        </div>
        
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-green">
              <el-icon><Lock /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">退出时保留本地数据</div>
            <div class="item-desc">开启后，退出登录将不会清除本地聊天缓存</div>
          </div>
          <el-switch v-model="localSettings.data.keepOnLogout" @change="handleChange" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { Delete, Download, Lock } from '@element-plus/icons-vue'

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
  max-width: 100%;
  box-sizing: border-box;
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

.card-style {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  gap: 16px;
  border-bottom: 1px solid var(--dt-border-lighter);
  transition: background-color 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

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

  &.bg-orange {
    background: rgba(255, 153, 0, 0.1);
    color: #ff9900;
  }

  &.bg-blue {
    background: rgba(0, 137, 255, 0.1);
    color: #0089FF;
  }

  &.bg-green {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }
}

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
  margin-bottom: 4px;
}

.size-text {
  color: var(--dt-brand-color);
  font-weight: 600;
}

.item-tip {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-style: italic;
}
</style>
