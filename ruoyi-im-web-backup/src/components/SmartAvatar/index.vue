<template>
  <div class="smart-avatar" :class="{ 'is-clickable': clickable, [`status-${status}`]: showStatus }" @click="handleClick">
    <img
      v-if="hasImage"
      class="avatar-img"
      :src="avatarUrl"
      :alt="displayName"
      :style="imgStyle"
      @error="handleError"
    />
    <span v-else class="avatar-text" :style="textStyle">
      {{ displayName }}
    </span>
    
    <!-- 边框 -->
    <span v-if="showBorder" class="avatar-border" :style="borderStyle"></span>
    
    <!-- 在线状态指示器 -->
    <div v-if="showStatus" class="status-indicator" :class="status"></div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  avatar: {
    type: String,
    default: '',
  },
  name: {
    type: String,
    default: '',
  },
  nickname: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 36,
    validator: (value) => [24, 32, 36, 40, 48, 64, 80, 100].includes(value)
  },
  showBorder: {
    type: Boolean,
    default: false,
  },
  borderColor: {
    type: String,
    default: 'rgba(22, 119, 255, 0.3)',
  },
  showStatus: {
    type: Boolean,
    default: false,
  },
  status: {
    type: String,
    default: 'offline',
    validator: (value) => ['online', 'away', 'busy', 'offline', 'hidden'].includes(value)
  },
  clickable: {
    type: Boolean,
    default: false,
  },
  shape: {
    type: String,
    default: 'circle',
    validator: (value) => ['circle', 'square'].includes(value)
  },
})

const emit = defineEmits(['click', 'error'])

const imageError = ref(false)

const hasImage = computed(() => {
  return props.avatar && props.avatar.trim() !== '' && !imageError.value
})

const displayName = computed(() => {
  const name = props.nickname || props.name || 'U'
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(name.length - 1)
  }
  return name.trim().charAt(0).toUpperCase()
})

const avatarUrl = computed(() => {
  return props.avatar
})

const imgStyle = computed(() => {
  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    border: props.showBorder
      ? `2px solid ${props.borderColor || 'rgba(255, 255, 255, 0.3)'}`
      : 'none',
  }
})

const textStyle = computed(() => {
  const fontSize = Math.round(props.size * 0.4)

  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    background: '#0089FF',
    color: '#ffffff',
    fontSize: `${fontSize}px`,
    fontWeight: '500',
    border: props.showBorder
      ? `2px solid ${props.borderColor || 'rgba(255, 255, 255, 0.3)'}`
      : 'none',
    borderRadius: '4px',
  }
})

const borderStyle = computed(() => {
  const borderColor = props.borderColor || 'rgba(255, 255, 255, 0.3)'
  return {
    borderColor: borderColor,
  }
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

const handleError = () => {
  imageError.value = true
  emit('error')
}
</script>

<style lang="scss" scoped>
$avatar-color: #f0f2f5;
$avatar-text-color: #1677ff;
$border-color: rgba(22, 119, 255, 0.3);

.smart-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: $avatar-color;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;

  // 方形形状
  &.shape-square {
    border-radius: 8px;
  }

  &.is-clickable {
    cursor: pointer;
  }

  &:not(.is-clickable):hover {
    transform: scale(1.05);
  }

  .avatar-img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: inherit;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }

  .avatar-text {
    font-weight: 600;
    color: $avatar-text-color;
    line-height: 1;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
  }

  .avatar-border {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border: 2px solid rgba(22, 119, 255, 0.3);
    border-radius: inherit;
    pointer-events: none;
    z-index: 1;
  }

  // 在线状态指示器
  .status-indicator {
    position: absolute;
    bottom: -1px;
    right: -1px;
    width: 12px;
    height: 12px;
    border: 2px solid #ffffff;
    border-radius: 50%;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
    transition: all 0.2s ease;

    &.status-online {
      background: #52c41a;
      animation: online-pulse 2s ease-in-out infinite;
    }

    &.status-away {
      background: #faad14;
    }

    &.status-busy {
      background: #ff4d4f;
    }

    &.status-offline {
      background: #bfbfbf;
    }

    &.status-hidden {
      background: #d9d9d9;
    }
  }
}

// 在线状态动画
@keyframes online-pulse {
  0%, 100% { 
    opacity: 1;
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.7);
  }
  50% { 
    opacity: 0.8;
    box-shadow: 0 0 0 6px rgba(82, 196, 26, 0);
  }
}
</style>
