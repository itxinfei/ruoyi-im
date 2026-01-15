<template>
  <div class="link-card-message" @click="handleClick">
    <!-- 网站图标和标题 -->
    <div class="link-header">
      <div class="site-icon">
        <img v-if="linkData.favicon" :src="linkData.favicon" @error="faviconError = true" />
        <el-icon v-else><Link /></el-icon>
      </div>
      <span class="site-name">{{ linkData.siteName || getSiteName(linkData.url) }}</span>
    </div>

    <!-- 缩略图 -->
    <div v-if="linkData.image" class="link-image">
      <img :src="linkData.image" @error="imageError = true" />
      <div v-if="imageError" class="image-placeholder">
        <el-icon><Picture /></el-icon>
      </div>
    </div>

    <!-- 描述信息 -->
    <div class="link-info">
      <div class="link-title">{{ linkData.title || '链接预览' }}</div>
      <div class="link-description">{{ linkData.description || linkData.url }}</div>
    </div>

    <!-- URL显示 -->
    <div class="link-url">{{ getDisplayUrl(linkData.url) }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Link, Picture } from '@element-plus/icons-vue'

const props = defineProps({
  // 链接数据
  content: {
    type: [String, Object],
    default: () => ({})
  }
})

const emit = defineEmits(['click'])

const faviconError = ref(false)
const imageError = ref(false)

// 链接数据
const linkData = reactive({
  url: '',
  title: '',
  description: '',
  image: '',
  favicon: '',
  siteName: ''
})

// 初始化链接数据
onMounted(() => {
  if (typeof props.content === 'string') {
    linkData.url = props.content
  } else {
    Object.assign(linkData, props.content)
  }

  // 如果没有元数据，尝试从URL提取
  if (!linkData.favicon) {
    linkData.favicon = getFaviconUrl(linkData.url)
  }
})

// 从URL提取网站名称
const getSiteName = (url) => {
  try {
    const hostname = new URL(url).hostname
    return hostname.replace('www.', '')
  } catch {
    return '网页链接'
  }
}

// 获取显示的URL
const getDisplayUrl = (url) => {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname + urlObj.pathname
  } catch {
    return url
  }
}

// 获取favicon URL
const getFaviconUrl = (url) => {
  try {
    const origin = new URL(url).origin
    return `${origin}/favicon.ico`
  } catch {
    return ''
  }
}

// 点击处理
const handleClick = () => {
  emit('click', linkData.url)
  // 新窗口打开
  window.open(linkData.url, '_blank')
}
</script>

<style lang="scss" scoped>
.link-card-message {
  width: 280px;
  background: #F5F7FA;
  border: 1px solid #E5E8EB;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F0F2F5;
    border-color: #D9D9D9;
  }

  .link-header {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    gap: 6px;

    .site-icon {
      width: 16px;
      height: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      .el-icon {
        font-size: 14px;
        color: #858B8F;
      }
    }

    .site-name {
      font-size: 12px;
      color: #858B8F;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .link-image {
    width: 100%;
    aspect-ratio: 16 / 9;
    background: #E5E8EB;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F0F2F5;

      .el-icon {
        font-size: 32px;
        color: #D9D9D9;
      }
    }
  }

  .link-info {
    padding: 10px 12px 6px;

    .link-title {
      font-size: 14px;
      font-weight: 500;
      color: #171A1A;
      line-height: 1.4;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .link-description {
      font-size: 12px;
      color: #858B8F;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .link-url {
    padding: 6px 12px 10px;
    font-size: 11px;
    color: #0089FF;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
