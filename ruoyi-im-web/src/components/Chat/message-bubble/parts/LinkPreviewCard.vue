<template>
  <div
    class="link-preview-card"
    :class="{ 'has-image': preview.image, 'is-loading': isLoading }"
  >
    <!-- 加载状态 -->
    <div
      v-if="isLoading"
      class="link-preview-loading"
    >
      <el-skeleton
        :rows="2"
        animated
      />
    </div>
    
    <!-- 预览内容 -->
    <a
      v-else
      :href="preview.url"
      class="link-preview-content"
      target="_blank"
      rel="noopener noreferrer"
      @click="handleClick"
    >
      <!-- 图片区域 -->
      <div
        v-if="preview.image"
        class="link-preview-image"
      >
        <img 
          :src="preview.image" 
          :alt="preview.title"
          loading="lazy"
          @error="handleImageError"
        >
      </div>
      
      <!-- 信息区域 -->
      <div class="link-preview-info">
        <div class="link-preview-header">
          <!-- Favicon -->
          <img 
            v-if="preview.favicon" 
            :src="preview.favicon" 
            class="link-favicon"
            alt=""
            @error="handleFaviconError"
          >
          <span
            v-else
            class="link-favicon-placeholder"
          >
            <span class="material-icons-outlined">language</span>
          </span>
          
          <!-- 站点名称 -->
          <span class="link-site-name">{{ preview.siteName }}</span>
        </div>
        
        <!-- 标题 -->
        <div
          class="link-title"
          :title="preview.title"
        >
          {{ preview.title }}
        </div>
        
        <!-- 描述 -->
        <div
          v-if="preview.description && !preview.isDefault" 
          class="link-description" 
          :title="preview.description"
        >
          {{ truncateDescription(preview.description) }}
        </div>
        
        <!-- URL -->
        <div class="link-url">{{ truncateUrl(preview.url) }}</div>
      </div>
      
      <!-- 打开图标 -->
      <div class="link-open-icon">
        <span class="material-icons-outlined">open_in_new</span>
      </div>
    </a>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  preview: {
    type: Object,
    required: true,
    validator(value) {
      return value.url && value.title
    }
  },
  isLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'image-error'])

/**
 * 截断描述文字
 */
const truncateDescription = desc => {
  if (!desc) {return ''}
  const maxLength = 100
  return desc.length > maxLength ? desc.substring(0, maxLength) + '...' : desc
}

/**
 * 截断 URL 显示
 */
const truncateUrl = url => {
  try {
    const urlObj = new URL(url)
    const path = urlObj.pathname.length > 15 
      ? urlObj.pathname.substring(0, 15) + '...'
      : urlObj.pathname
    return urlObj.hostname + (path !== '/' ? path : '')
  } catch {
    return url.length > 40 ? url.substring(0, 40) + '...' : url
  }
}

/**
 * 处理点击事件
 */
const handleClick = e => {
  emit('click', props.preview)
}

/**
 * 处理图片加载失败
 */
const handleImageError = e => {
  e.target.style.display = 'none'
  emit('image-error', props.preview.url)
}

/**
 * 处理 favicon 加载失败
 */
const handleFaviconError = e => {
  e.target.style.display = 'none'
  e.target.nextElementSibling?.classList.add('show')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.link-preview-card {
  margin-top: 8px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  transition: all var(--dt-transition-base);
  
  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-card);
  }
  
  &.is-loading {
    padding: 16px;
  }
  
  &.has-image {
    .link-preview-content {
      flex-direction: column;
    }
    
    .link-preview-image {
      width: 100%;
      height: 140px;
      overflow: hidden;
      background: var(--dt-bg-body);
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform var(--dt-transition-base);
      }
    }
    
    &:hover .link-preview-image img {
      transform: scale(1.02);
    }
    
    .link-preview-info {
      padding: 12px;
    }
  }
}

.link-preview-content {
  display: flex;
  text-decoration: none;
  color: inherit;
}

.link-preview-info {
  flex: 1;
  padding: 12px 16px;
  min-width: 0;
}

.link-preview-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.link-favicon {
  width: 16px;
  height: 16px;
  border-radius: 2px;
  flex-shrink: 0;
}

.link-favicon-placeholder {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-tertiary);
  flex-shrink: 0;
  
  .material-icons-outlined {
    font-size: 14px;
  }
  
  &:not(.show) {
    display: none;
  }
}

.link-site-name {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.link-title {
  font-size: var(--dt-font-size-sm);
  font-weight: 600;
  color: var(--dt-text-primary);
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.link-description {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-secondary);
  line-height: 1.4;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.link-url {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-open-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  color: var(--dt-text-tertiary);
  flex-shrink: 0;
  align-self: flex-start;
  
  .material-icons-outlined {
    font-size: 18px;
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }
}

.link-preview-card:hover .link-open-icon .material-icons-outlined {
  opacity: 1;
}

// 暗色模式适配
:global(.dark) {
  .link-preview-card {
    background: var(--dt-bg-card);
    border-color: var(--dt-border-dark);
    
    &:hover {
      border-color: var(--dt-brand-color);
    }
    
    .link-preview-image {
      background: var(--dt-bg-body);
    }
  }
  
  .link-title {
    color: var(--dt-text-primary);
  }
  
  .link-description {
    color: var(--dt-text-secondary);
  }
  
  .link-url,
  .link-site-name {
    color: var(--dt-text-tertiary);
  }
}

// 移动端适配
@media (max-width: 480px) {
  .link-preview-card.has-image {
    .link-preview-image {
      height: 120px;
    }
  }
  
  .link-preview-info {
    padding: 10px 12px;
  }
  
  .link-title {
    font-size: 13px;
  }
}
</style>
