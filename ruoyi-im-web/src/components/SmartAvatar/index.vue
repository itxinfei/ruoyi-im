<template>
  <div class="smart-avatar" :class="{ 'is-clickable': clickable }" @click="handleClick">
    <el-avatar
      v-if="hasImage"
      :size="size"
      :src="avatarUrl"
      :style="avatarStyle"
    >
      {{ defaultText }}
    </el-avatar>
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
  const firstChar = name.trim().charAt(0).toUpperCase()
  return /[A-Z]/.test(firstChar) ? firstChar : 'U'
})

const defaultText = computed(() => {
  return displayText.value
})

const avatarUrl = computed(() => {
  return props.avatar
})

const avatarStyle = computed(() => {
  if (props.showBorder) {
    return {
      border: `2px solid ${props.borderColor || getBorderColor()}`
    }
  }
  return {}
})

const textStyle = computed(() => {
  const bgColor = getBackgroundColor()
  const borderColor = props.borderColor || getBorderColor()
  const textColor = '#ffffff'
  const fontSize = Math.round(props.size * 0.45)

  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    background: `linear-gradient(135deg, ${bgColor[0]} 0%, ${bgColor[1]} 100%)`,
    color: textColor,
    fontSize: `${fontSize}px`,
    fontWeight: '500',
    border: props.showBorder ? `2px solid ${borderColor}` : 'none',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
  }
})

const borderStyle = computed(() => {
  const borderColor = props.borderColor || getBorderColor()
  return {
    borderColor: borderColor
  }
})

function getBackgroundColor() {
  const charCode = displayText.value.charCodeAt(0)
  const colorIndex = charCode % avatarColors.length
  return avatarColors[colorIndex]
}

function getBorderColor() {
  const charCode = displayText.value.charCodeAt(0)
  const colorIndex = charCode % borderColors.length
  return borderColors[colorIndex]
}

function handleClick() {
  if (props.clickable) {
    emit('click')
  }
}

function handleError() {
  imageError.value = true
  emit('error')
}

const avatarColors = [
  ['#FF6B6B', '#FF8E72'],
  ['#4ECDC4', '#45B7AA'],
  ['#667EEA', '#764BA2'],
  ['#F093FB', '#F5576C'],
  ['#4FACFE', '#00F2FE'],
  ['#43E97B', '#38F9D7'],
  ['#FA709A', '#FEE140'],
  ['#30CFD0', '#330867'],
  ['#FF9A9E', '#FECFEF'],
  ['#A18CD1', '#FBC2EB'],
  ['#FF758C', '#FF7EB3'],
  ['#7F7FD5', '#86A8E7'],
  ['#91EAE4', '#86A8E7'],
  ['#FDC836', '#F7862A'],
  ['#E0C3FC', '#8EC5FC'],
  ['#C1DFC4', '#DEECD6']
]

const borderColors = [
  '#FF6B6B',
  '#4ECDC4',
  '#667EEA',
  '#F093FB',
  '#4FACFE',
  '#43E97B',
  '#FA709A',
  '#30CFD0',
  '#FF9A9E',
  '#A18CD1',
  '#FF758C',
  '#7F7FD5',
  '#91EAE4',
  '#FDC836',
  '#E0C3FC',
  '#C1DFC4'
]
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

  .avatar-text {
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    text-transform: uppercase;
    letter-spacing: 1px;
    transition: all 0.2s ease;
  }

  :deep(.el-avatar) {
    border-radius: 50%;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }

  .avatar-border {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border-radius: 50%;
    pointer-events: none;
    z-index: 1;
  }

  .online-indicator {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid #fff;
    z-index: 2;

    &.online {
      background-color: #52c41a;
    }

    &.away {
      background-color: #faad14;
    }

    &.busy {
      background-color: #ff4d4f;
    }

    &.offline {
      background-color: #d9d9d9;
    }
  }
}
</style>
