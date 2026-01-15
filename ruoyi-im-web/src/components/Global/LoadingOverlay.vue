<template>
  <div class="loading-overlay" :class="{ fullscreen }">
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      <p class="loading-text">{{ text || '加载中...' }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: ''
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped lang="scss">
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);

  &.fullscreen {
    background: rgba(255, 255, 255, 0.95);
  }
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-spinner {
  display: flex;
  gap: 8px;
}

.spinner-ring {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #1677ff;
  border-right-color: #1677ff;
  border-bottom-color: #1677ff;
  border-left-color: #1677ff;
  animation: ring-spin 1.2s cubic-bezier(0.5, 0, 1, 1) infinite;

  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 3px solid transparent;
    border-top-color: #1677ff;
    border-right-color: #1677ff;
    border-bottom-color: #1677ff;
    border-left-color: #1677ff;
  }
}

&:nth-child(1) { animation-delay: 0s; }
&:nth-child(2) { animation-delay: 0.4s; }
&:nth-child(3) { animation-delay: 0.8s; }

@keyframes ring-spin {
  0% {
    transform: rotate(0deg);
    }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 16px;
  color: #595959;
  font-weight: 500;
}
</style>