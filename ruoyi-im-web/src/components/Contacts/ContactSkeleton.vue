<template>
  <div class="contact-skeleton">
    <div
      v-for="i in count"
      :key="i"
      class="skeleton-item"
    >
      <el-skeleton-item
        variant="circle"
        class="skeleton-avatar"
      />
      <div class="skeleton-content">
        <el-skeleton-item
          variant="text"
          class="skeleton-name"
        />
        <el-skeleton-item
          variant="text"
          class="skeleton-desc"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 8
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-skeleton {
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
}

.skeleton-item {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md) 0;
  border-bottom: 1px solid var(--dt-border-color-light);

  &:last-child {
    border-bottom: none;
  }
}

.skeleton-avatar {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  margin-left: var(--dt-spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-md);
}

.skeleton-name {
  width: 120px;
  height: 16px;
}

.skeleton-desc {
  width: 180px;
  height: 13px;
}
</style>
