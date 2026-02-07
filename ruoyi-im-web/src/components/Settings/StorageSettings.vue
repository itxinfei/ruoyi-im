<template>
  <div class="storage-settings">
    <!-- 缓存管理 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-orange">
          <span class="material-icons-outlined">folder_open</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            缓存管理
          </h3>
          <p class="section-desc">
            管理本地缓存文件，释放磁盘空间
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="storage-info-row">
          <div class="storage-icon-large bg-orange-light">
            <span class="material-icons-outlined">storage</span>
          </div>
          <div class="storage-info">
            <div class="storage-size">
              {{ cacheSize }}
            </div>
            <div class="storage-desc">
              已使用缓存空间
            </div>
          </div>
          <div class="storage-action">
            <el-button
              type="danger"
              plain
              @click="$emit('clear-cache')"
            >
              <span
                class="material-icons-outlined"
                style="font-size: 16px; margin-right: 4px;"
              >delete_sweep</span>
              清理缓存
            </el-button>
          </div>
        </div>
        
        <div class="storage-details">
          <div class="detail-item">
            <span class="material-icons-outlined detail-icon">image</span>
            <span class="detail-text">聊天图片缓存</span>
          </div>
          <div class="detail-item">
            <span class="material-icons-outlined detail-icon">videocam</span>
            <span class="detail-text">视频文件缓存</span>
          </div>
          <div class="detail-item">
            <span class="material-icons-outlined detail-icon">description</span>
            <span class="detail-text">文档预览缓存</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据备份 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">backup</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            数据备份
          </h3>
          <p class="section-desc">
            备份和导出您的聊天记录
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div
          class="backup-row"
          @click="$emit('export-chat')"
        >
          <div class="backup-icon-wrapper bg-blue-light">
            <span class="material-icons-outlined">download</span>
          </div>
          <div class="backup-content">
            <div class="backup-title">
              导出聊天记录
            </div>
            <div class="backup-desc">
              将所有聊天历史备份为 JSON 文件
            </div>
          </div>
          <span class="material-icons-outlined arrow-icon">chevron_right</span>
        </div>
      </div>
    </section>

    <!-- 数据安全 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-green">
          <span class="material-icons-outlined">security</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            数据安全
          </h3>
          <p class="section-desc">
            配置本地数据的保留策略
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-main">
            <div class="item-label-row">
              <span class="material-icons-outlined item-icon">save</span>
              <span class="item-label">退出时保留数据</span>
            </div>
            <span class="item-desc">退出登录时不清理本地聊天缓存</span>
          </div>
          <div class="item-action">
            <el-switch 
              v-model="localSettings.data.keepOnLogout" 
              @change="handleChange" 
            />
          </div>
        </div>
      </div>
    </section>
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

watch(() => props.modelValue, newVal => {
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
  
  &.bg-gradient-orange {
    background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  }
  
  &.bg-gradient-blue {
    background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
  }
  
  &.bg-gradient-green {
    background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
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
  padding: 24px;
}

// 存储信息行
.storage-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.storage-icon-large {
  width: 64px;
  height: 64px;
  border-radius: var(--dt-radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 32px;
    color: #fa8c16;
  }
  
  &.bg-orange-light {
    background: var(--dt-warning-bg);
  }
}

.storage-info {
  flex: 1;
}

.storage-size {
  font-size: 28px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-primary);
  line-height: 1.2;
}

.storage-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin-top: 4px;
}

// 存储详情
.storage-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid var(--dt-border-light);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  
  .detail-icon {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
  
  .detail-text {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}

// 备份行
.backup-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: var(--dt-bg-hover);
    
    .arrow-icon {
      color: var(--dt-brand-color);
      transform: translateX(4px);
    }
  }
}

.backup-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
    color: #1890ff;
  }
  
  &.bg-blue-light {
    background: var(--dt-brand-light);
  }
}

.backup-content {
  flex: 1;
}

.backup-title {
  font-size: 14px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.backup-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.arrow-icon {
  font-size: 24px;
  color: var(--dt-text-tertiary);
  transition: all 0.2s ease;
}

// 设置项（复用）
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
}

.item-main {
  flex: 1;
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
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
  
  .storage-details {
    border-color: var(--dt-border-dark);
  }
  
  .detail-item {
    background: var(--dt-bg-hover-dark);
  }
  
  .backup-row:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
