<template>
  <div class="storage-settings">
    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">缓存管理</h3>
        <p class="section-desc">管理本地缓存文件，释放磁盘空间</p>
      </div>
      
      <div class="setting-card">
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
          <div class="item-action">
            <el-button type="danger" plain size="small" @click="$emit('clear-cache')">清理缓存</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">数据备份与安全</h3>
        <p class="section-desc">管理您的聊天记录备份和数据保留策略</p>
      </div>
      
      <div class="setting-card">
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
          <div class="item-action">
            <el-button type="primary" plain size="small" @click="$emit('export-chat')">立即导出</el-button>
          </div>
        </div>
        
        <el-divider />
        
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
          <div class="item-action">
            <el-switch v-model="localSettings.data.keepOnLogout" @change="handleChange" />
          </div>
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
  padding: 0;
  width: 100%;
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
    color: var(--text-color-primary, #303133);
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
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  gap: 16px;
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--bg-color-hover, #f5f7fa);
  }
}

.item-icon-wrapper {
  flex-shrink: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.bg-orange {
    background: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }

  &.bg-blue {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }

  &.bg-green {
    background: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-primary, #303133);
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: var(--text-color-secondary, #909399);
  margin-bottom: 4px;
}

.size-text {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
}

.item-tip {
  font-size: 12px;
  color: var(--text-color-secondary, #909399);
  opacity: 0.8;
}

.item-action {
  flex-shrink: 0;
}

:deep(.el-divider) {
  margin: 0;
  border-color: var(--border-color-lighter, #ebeef5);
}
</style>
