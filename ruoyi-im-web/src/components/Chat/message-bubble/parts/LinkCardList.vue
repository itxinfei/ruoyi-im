<template>
  <div class="link-card-list">
    <a
      v-for="(link, index) in links"
      :key="index"
      :href="link"
      class="link-card"
      target="_blank"
      rel="noopener noreferrer"
    >
      <div class="link-icon">
        <span class="material-icons-outlined">link</span>
      </div>
      <div class="link-info">
        <div class="link-url">{{ truncateUrl(link) }}</div>
      </div>
      <div class="link-arrow">
        <span class="material-icons-outlined">open_in_new</span>
      </div>
    </a>
  </div>
</template>

<script setup>
const props = defineProps({
  links: { type: Array, default: () => [] }
})

const truncateUrl = url => {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname + (urlObj.pathname.length > 1 ? '/...' : '')
  } catch {
    return url.length > 40 ? url.substring(0, 40) + '...' : url
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.link-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  text-decoration: none;
  transition: all var(--dt-transition-base);

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-card);
    transform: translateY(-1px);
  }
}

.link-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-brand-color);
  flex-shrink: 0;

  .material-icons-outlined {
    font-size: 16px;
  }
}

.link-info {
  flex: 1;
  min-width: 0;
}

.link-url {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-arrow {
  color: var(--dt-text-tertiary);
  flex-shrink: 0;

  .material-icons-outlined {
    font-size: 16px;
  }
}
</style>
