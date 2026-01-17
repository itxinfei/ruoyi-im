<template>
  <div
    class="ding-avatar"
    :class="[`ding-avatar-${size}`]"
    :style="{ backgroundColor: hasAvatar ? 'transparent' : bgColor }"
  >
    <img
      v-if="hasAvatar && avatarUrl"
      :src="avatarUrl"
      :alt="name"
      class="avatar-img"
      @error="handleImageError"
    />
    <span v-else class="avatar-text">{{ avatarText }}</span>

    <!-- 在线状态指示器（可选） -->
    <span v-if="showStatus && status" class="avatar-status" :class="`status-${status}`"></span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  // 头像URL
  avatar: String,
  // 用户名/昵称
  name: { type: String, default: '' },
  // 头像尺寸
  size: {
    type: String,
    default: 'md',
    validator: val => ['xs', 'sm', 'md', 'lg', 'xl'].includes(val),
  },
  // 是否显示状态
  showStatus: Boolean,
  // 在线状态: online, offline, busy, away
  status: { type: String, default: 'offline' },
})

const emit = defineEmits(['error'])

const imageError = ref(false)

// 是否有头像
const hasAvatar = computed(() => {
  return props.avatar && !imageError.value
})

// 头像URL
const avatarUrl = computed(() => {
  return props.avatar
})

// 头像文字（取名字最后一个字）
const avatarText = computed(() => {
  const name = props.name || ''
  if (!name) return '?'

  // 中文名取最后一个字
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(name.length - 1)
  }

  // 英文名取首字母（大写）
  return name.charAt(0).toUpperCase()
})

// 背景色（钉钉蓝 #0089FF）
const bgColor = '#0089FF'

// 处理图片加载失败
const handleImageError = () => {
  imageError.value = true
  emit('error')
}
</script>

<style lang="scss" scoped>
.ding-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #0089ff;
  color: #ffffff;
  font-weight: 500;
  overflow: hidden;
  border-radius: 4px; // 钉钉5.6风格：小圆角
  flex-shrink: 0;

  // 尺寸规范
  &.ding-avatar-xs {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }

  &.ding-avatar-sm {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  &.ding-avatar-md {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  &.ding-avatar-lg {
    width: 48px;
    height: 48px;
    font-size: 18px;
  }

  &.ding-avatar-xl {
    width: 64px;
    height: 64px;
    font-size: 24px;
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-text {
    line-height: 1;
  }

  // 在线状态指示器
  .avatar-status {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid #ffffff;

    &.status-online {
      background-color: #52c41a;
    }

    &.status-offline {
      background-color: #d9d9d9;
    }

    &.status-busy {
      background-color: #f5222d;
    }

    &.status-away {
      background-color: #faad14;
    }
  }
}
</style>
