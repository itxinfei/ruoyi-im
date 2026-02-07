<template>
  <div
    class="dingtalk-avatar"
    :class="avatarClass"
    :style="{ width: size + 'px', height: size + 'px', fontSize: fontSize + 'px', backgroundColor: bgColor }"
  >
    <img
      v-if="imageUrl && !imageError"
      :src="imageUrl"
      class="avatar-img"
      @error="handleImageError"
    >
    <span
      v-else
      class="avatar-text"
    >{{ displayName }}</span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  // 头像URL
  src: String,
  // ... (keeping other props matching original, but defineProps is reactive anyway)
  name: {
    type: String,
    default: ''
  },
  userId: [String, Number],
  size: {
    type: Number,
    default: 36
  },
  shape: {
    type: String,
    default: 'circle'
  },
  customClass: String
})

const imageError = ref(false)
const imageUrl = computed(() => addTokenToUrl(props.src))
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

<style scoped lang="scss">
.dingtalk-avatar {
  display: flex;  // 改为 flex，确保在 flex 容器中正常居中
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 500;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
  transition: all var(--dt-transition-fast);

  &:hover {
    transform: scale(1.05);
    box-shadow: var(--dt-shadow-3);
  }
}

/* 圆形头像（单聊） */
.avatar-circle {
  border-radius: var(--dt-radius-full);  // 钉钉单聊头像是完全圆形的
  border: none;  // 移除边框
  box-shadow: var(--dt-shadow-2);
}

/* 方形头像（群聊） */
.avatar-square {
  border-radius: var(--dt-radius-md);  // 钉钉群聊头像：小圆角方形
  border: 1px solid var(--dt-primary-alpha-10);
  box-shadow: var(--dt-shadow-2);
}

/* 消息中的特殊头像样式 */
.dingtalk-avatar.message-avatar {
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--dt-primary-alpha-15);
}

/* 用户信息头像 */
.dingtalk-avatar.user-info-avatar {
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--dt-primary-alpha-15);
}

/* 头像图片 */
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;  // 移除 inline，确保完全填充
}

/* 头像文字（首字母） */
.avatar-text {
  white-space: nowrap;
  user-select: none;
  text-transform: uppercase;
  display: flex;  // 确保文字居中
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
</style>
