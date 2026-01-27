<template>
  <div class="link-card" @click="openLink">
    <div v-if="link.image" class="link-image">
      <img :src="link.image" :alt="link.title" loading="lazy" />
    </div>
    <div class="link-content">
      <div class="link-title">{{ link.title || link.url }}</div>
      <div v-if="link.description" class="link-description">{{ link.description }}</div>
      <div class="link-url">{{ formatUrl(link.url) }}</div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  link: {
    type: Object,
    required: true,
    validator: (value) => {
      return value.url && typeof value.url === 'string'
    }
  }
})

const formatUrl = (url) => {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname + urlObj.pathname
  } catch {
    return url
  }
}

const openLink = () => {
  window.open(props.link.url, '_blank')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.link-card {
  display: flex;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  max-width: 320px;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
    transform: translateY(-1px);
  }

  .link-image {
    width: 100px;
    flex-shrink: 0;
    background: #e2e8f0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  .link-content {
    flex: 1;
    padding: 10px 12px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .link-title {
    font-size: 14px;
    font-weight: 500;
    color: #1f2329;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .link-description {
    font-size: 12px;
    color: #64748b;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .link-url {
    font-size: 11px;
    color: #94a3b8;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: auto;
  }
}

// 无图片样式
.link-card:has(.link-image:empty) .link-content,
.link-card:not(:has(.link-image)) .link-content {
  padding: 12px;
}

.dark .link-card {
  background: #1e293b;
  border-color: #334155;

  .link-title {
    color: #f1f5f9;
  }

  .link-description {
    color: #94a3b8;
  }

  .link-url {
    color: #64748b;
  }

  .link-image {
    background: #334155;
  }

  &:hover {
    border-color: var(--dt-brand-color);
  }
}
</style>
