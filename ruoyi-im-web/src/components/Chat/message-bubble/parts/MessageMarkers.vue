<template>
  <div class="message-markers">
    <span
      v-for="marker in markers"
      :key="marker.id || marker.markerType"
      class="marker-icon"
      :class="{ completed: marker.isCompleted }"
      :style="{ color: marker.color || '' }"
    >
      <span v-if="marker.markerType === 'FLAG'" class="material-icons-outlined">flag</span>
      <span v-else-if="marker.markerType === 'IMPORTANT'" class="material-icons-outlined">star</span>
      <span v-else-if="marker.markerType === 'TODO'" class="material-icons-outlined">
        {{ marker.isCompleted ? 'check_circle' : 'check_circle_outline' }}
      </span>
    </span>
  </div>
</template>

<script setup>
defineProps({
  markers: { type: Array, default: () => [] }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-markers {
  display: flex;
  gap: 4px;
  margin-top: 4px;
  align-self: flex-start;

  .marker-icon {
    display: inline-flex;
    align-items: center;
    font-size: 14px;
    padding: 2px 4px;
    border-radius: var(--dt-radius-sm);
    background: rgba(0, 0, 0, 0.05);
    transition: all var(--dt-transition-base);
    animation: scaleIn 0.2s var(--dt-ease-bounce);

    &.completed {
      opacity: 0.6;
      text-decoration: line-through;
    }

    &:hover {
      transform: scale(1.15);
    }
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
