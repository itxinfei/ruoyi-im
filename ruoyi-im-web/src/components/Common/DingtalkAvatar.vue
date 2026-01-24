<template>
  <div
    class="dingtalk-avatar"
    :class="avatarClass"
    :style="{ width: size + 'px', height: size + 'px', fontSize: fontSize + 'px', backgroundColor: bgColor }"
  >
    <img v-if="imageUrl && !imageError" :src="imageUrl" @error="handleImageError" class="avatar-img" />
    <span v-else class="avatar-text">{{ displayName }}</span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  // 头像URL
  src: String,
  // 用户名/昵称（用于生成首字母）
  name: {
    type: String,
    default: ''
  },
  // 用户ID（用于计算背景色）
  userId: [String, Number],
  // 头像尺寸
  size: {
    type: Number,
    default: 36
  },
  // 头像形状：circle=圆形, square=方形
  shape: {
    type: String,
    default: 'circle'
  },
  // 自定义样式类
  customClass: String
})

const imageError = ref(false)
const imageUrl = computed(() => props.src)
const displayName = computed(() => {
  const name = props.name || '?'
  return name.charAt(0).toUpperCase()
})

// 字体大小为头像尺寸的 40%
const fontSize = computed(() => Math.max(14, Math.floor(props.size * 0.4)))

// 钉钉风格的颜色盘（根据用户ID计算固定颜色）
const bgColor = computed(() => {
  const colors = [
    '#F5A623', // 橙色
    '#6DD400', // 绿色
    '#44B6AE', // 青色
    '#4A90E2', // 蓝色
    '#9013FE', // 紫色
    '#F5E066', // 黄色
    '#FF6B6B', // 红色
    '#6BCF7F', // 薄荷绿
    '#4ECDC4', // 青绿
    '#45B7D1', // 天蓝
    '#96CEB4', // 浅绿
    '#FFEAA7', // 浅黄
    '#DFE6E9', // 浅灰
    '#74B9FF', // 浅蓝
    '#A29BFE'  // 浅紫
  ]

  // 如果有 userId，根据 userId 计算颜色索引（确保同一用户颜色一致）
  if (props.userId) {
    const id = String(props.userId)
    let hash = 0
    for (let i = 0; i < id.length; i++) {
      hash = ((hash << 5) - hash) + id.charCodeAt(i)
      hash |= 0
    }
    const index = Math.abs(hash) % colors.length
    return colors[index]
  }

  // 如果没有 userId，根据名字计算
  if (props.name) {
    let hash = 0
    for (let i = 0; i < props.name.length; i++) {
      hash = ((hash << 5) - hash) + props.name.charCodeAt(i)
      hash |= 0
    }
    const index = Math.abs(hash) % colors.length
    return colors[index]
  }

  // 默认颜色
  return '#4A90E2'
})

const avatarClass = computed(() => {
  const classes = []
  if (props.shape === 'square') {
    classes.push('avatar-square')
  } else {
    classes.push('avatar-circle')
  }
  if (props.customClass) {
    classes.push(props.customClass)
  }
  return classes.join(' ')
})

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped>
.dingtalk-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 500;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
}

.avatar-circle {
  border-radius: 50%;
}

.avatar-square {
  border-radius: 8px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  white-space: nowrap;
  user-select: none;
}
</style>
