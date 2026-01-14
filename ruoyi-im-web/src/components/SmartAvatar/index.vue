<template>
  <div class="smart-avatar" :class="{ 'is-clickable': clickable }" @click="handleClick">
    <img
      v-if="hasImage"
      class="avatar-img"
      :src="avatarUrl"
      :style="imgStyle"
      @error="handleError"
      alt=""
    />
    <div
      v-else
      class="avatar-text"
      :style="textStyle"
    >
      {{ displayText }}
    </div>
    <span v-if="showBorder" class="avatar-border" :style="borderStyle"></span>
    <span v-if="showOnline && online" class="online-indicator" :class="onlineStatus"></span>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  avatar: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  nickname: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 40
  },
  showBorder: {
    type: Boolean,
    default: true
  },
  borderColor: {
    type: String,
    default: ''
  },
  showOnline: {
    type: Boolean,
    default: false
  },
  online: {
    type: Boolean,
    default: false
  },
  onlineStatus: {
    type: String,
    default: 'online'
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'error'])

const imageError = ref(false)

const hasImage = computed(() => {
  return props.avatar && props.avatar.trim() !== '' && !imageError.value
})

const displayText = computed(() => {
  const name = props.nickname || props.name || 'U'
  // 中文名取最后一个字
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(name.length - 1)
  }
  // 英文名取首字母
  return name.trim().charAt(0).toUpperCase()
})

const avatarUrl = computed(() => {
  return props.avatar
})

const imgStyle = computed(() => {
  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    border: props.showBorder ? `2px solid ${props.borderColor || 'rgba(255, 255, 255, 0.3)'}` : 'none'
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
    border: props.showBorder ? `2px solid ${props.borderColor || 'rgba(255, 255, 255, 0.3)'}` : 'none',
    borderRadius: '4px'
  }
})

const borderStyle = computed(() => {
  const borderColor = props.borderColor || 'rgba(255, 255, 255, 0.3)'
  return {
    borderColor: borderColor
  }
})

function handleClick() {
  if (props.clickable) {
    emit('click')
  }
}

function handleError() {
  imageError.value = true
  emit('error')
}
</script>

<style lang="scss" scoped>
.smart-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.is-clickable {
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.05);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  .avatar-img {
    display: block;
    object-fit: cover;
    border-radius: 4px;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }
  }

  .avatar-text {
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
  }

  .avatar-border {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border-radius: 4px;
    pointer-events: none;
    z-index: 1;
  }

  .online-indicator {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid #fff;
    z-index: 2;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;

    &.online {
      background-color: #00C853;
      box-shadow: 0 0 0 2px rgba(0, 200, 83, 0.2);
    }

    &.away {
      background-color: #FF9800;
      box-shadow: 0 0 0 2px rgba(255, 152, 0, 0.2);
    }

    &.busy {
      background-color: #F5222D;
      box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
    }

    &.offline {
      background-color: #B8B8B8;
      box-shadow: 0 0 0 2px rgba(184, 184, 184, 0.2);
    }

    &.dnd {
      background-color: #F5222D;
      box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
    }
  }
}
</style>
