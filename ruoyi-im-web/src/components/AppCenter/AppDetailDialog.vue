<template>
  <el-dialog
    v-model="visible"
    :title="app?.name || '应用详情'"
    width="560px"
    :close-on-click-modal="false"
    class="app-detail-dialog"
    @close="handleClose"
  >
    <div v-if="app" class="app-detail">
      <!-- 应用头部 -->
      <div class="detail-header">
        <div class="app-icon-large" :style="{ background: app.iconColor }">
          <img v-if="app.iconUrl" :src="app.iconUrl" :alt="app.name" class="app-icon-img" />
          <span v-else class="app-icon-text">{{ app.name.charAt(0) }}</span>
        </div>
        <div class="app-header-info">
          <h3 class="app-name">{{ app.name }}</h3>
          <div class="app-meta">
            <span class="meta-item">
              <span class="material-icons-outlined">category</span>
              {{ app.categoryName }}
            </span>
            <span class="meta-item">
              <span class="material-icons-outlined">star</span>
              {{ app.rating || 5.0 }} 分
            </span>
          </div>
        </div>
        <div class="app-status">
          <el-tag v-if="isInstalled" type="success" size="small">已安装</el-tag>
          <el-tag v-if="app.isHot" type="danger" size="small">热门</el-tag>
          <el-tag v-if="app.isNew" type="primary" size="small">新上架</el-tag>
        </div>
      </div>

      <!-- 应用截图 -->
      <div v-if="app.screenshots && app.screenshots.length > 0" class="app-screenshots">
        <div
          v-for="(shot, index) in app.screenshots"
          :key="index"
          class="screenshot-item"
          :class="{ active: currentScreenshot === index }"
          @click="currentScreenshot = index"
        >
          <img :src="shot" :alt="`截图 ${index + 1}`" />
        </div>
      </div>

      <!-- 应用描述 -->
      <div class="detail-section">
        <h4 class="section-title">应用介绍</h4>
        <p class="app-description">{{ app.description || app.summary }}</p>
      </div>

      <!-- 功能特性 -->
      <div v-if="app.features && app.features.length > 0" class="detail-section">
        <h4 class="section-title">功能特性</h4>
        <ul class="features-list">
          <li v-for="(feature, index) in app.features" :key="index">
            <span class="material-icons-outlined feature-icon">check_circle</span>
            {{ feature }}
          </li>
        </ul>
      </div>

      <!-- 开发者信息 -->
      <div v-if="app.developer" class="detail-section">
        <h4 class="section-title">开发者</h4>
        <p class="developer-info">{{ app.developer }}</p>
      </div>

      <!-- 版本信息 -->
      <div class="detail-section">
        <h4 class="section-title">版本信息</h4>
        <div class="version-info">
          <span class="version-label">当前版本：</span>
          <span class="version-value">{{ app.version || '1.0.0' }}</span>
          <span class="version-label">更新时间：</span>
          <span class="version-value">{{ app.updateTime || '最近' }}</span>
        </div>
      </div>

      <!-- 权限说明 -->
      <div v-if="app.permissions && app.permissions.length > 0" class="detail-section">
        <h4 class="section-title">所需权限</h4>
        <div class="permissions-list">
          <el-tag
            v-for="(perm, index) in app.permissions"
            :key="index"
            size="small"
            type="info"
          >
            {{ perm }}
          </el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="!isInstalled"
          type="primary"
          :loading="installing"
          @click="handleInstall"
        >
          安装应用
        </el-button>
        <el-button
          v-else
          type="primary"
          @click="handleOpen"
        >
          打开应用
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  app: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'install', 'open'])

const visible = ref(false)
const installing = ref(false)
const currentScreenshot = ref(0)

const isInstalled = computed(() => {
  return props.app?.installed || false
})

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

const handleInstall = () => {
  if (props.app) {
    emit('install', props.app)
  }
}

const handleOpen = () => {
  if (props.app) {
    emit('open', props.app)
    handleClose()
  }
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    currentScreenshot.value = 0
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.app-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 24px;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.app-icon-large {
  width: 80px;
  height: 80px;
  border-radius: var(--dt-radius-2xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .app-icon-img {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }

  .app-icon-text {
    font-size: 32px;
    font-weight: 700;
    color: #fff;
  }
}

.app-header-info {
  flex: 1;

  .app-name {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
  }

  .app-meta {
    display: flex;
    gap: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--dt-text-secondary);
    }
  }
}

.app-status {
  display: flex;
  gap: 6px;
}

.detail-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 12px 0;
  }
}

.app-description {
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
    color: var(--dt-text-primary);

    .feature-icon {
      color: var(--dt-success-color);
      font-size: 18px;
      flex-shrink: 0;
      margin-top: 2px;
    }
  }
}

.developer-info {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.version-info {
  .version-label {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin-right: 4px;
  }

  .version-value {
    font-size: 13px;
    color: var(--dt-text-primary);
    font-weight: 500;
  }
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.app-screenshots {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 4px;

  .screenshot-item {
    width: 60px;
    height: 60px;
    border-radius: var(--dt-radius-md);
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &:hover,
    &.active {
      border-color: var(--dt-brand-color);
    }

    &.active {
      border-color: var(--dt-brand-color);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
