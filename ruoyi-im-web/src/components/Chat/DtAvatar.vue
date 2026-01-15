<template>
  <div
    class="dt-avatar"
    :class="[`dt-avatar-${size}`, { 'clickable': clickable, 'show-status': showStatus }]"
    :style="{ width: finalSize + 'px', height: finalSize + 'px' }"
    @click="handleClick"
  >
    <!-- 单人头像 -->
    <template v-if="!isGroup || !members || members.length === 0">
      <img
        v-if="hasAvatar && avatarUrl"
        :src="avatarUrl"
        :alt="name"
        class="avatar-img"
        @error="handleImageError"
      />
      <span v-else class="avatar-text">{{ avatarText }}</span>
    </template>

    <!-- 群组拼接头像 -->
    <template v-else>
      <DtGroupAvatar
        :members="members"
        :size="size"
        :default-text="name"
      />
    </template>

    <!-- 在线状态指示器 -->
    <span v-if="showStatus && status" class="avatar-status" :class="`status-${status}`"></span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DtGroupAvatar from './DtGroupAvatar.vue'

const props = defineProps({
  // 头像URL
  avatar: String,
  // 用户名/群名
  name: {
    type: String,
    default: ''
  },
  // 头像尺寸：xs/sm/md/lg/xl 或 自定义数字
  size: {
    type: [String, Number],
    default: 'md'
  },
  // 是否为群组
  isGroup: Boolean,
  // 群组成员（群组拼接头像使用）
  members: {
    type: Array,
    default: () => []
  },
  // 是否显示在线状态
  showStatus: Boolean,
  // 在线状态：online/busy/away/offline
  status: {
    type: String,
    default: 'offline'
  },
  // 是否可点击
  clickable: Boolean
})

const emit = defineEmits(['click', 'error'])

const imageError = ref(false)

// 尺寸映射
const sizeMap = {
  xs: 24,
  sm: 32,
  md: 40,
  lg: 48,
  xl: 64
}

// 最终尺寸
const finalSize = computed(() => {
  if (typeof props.size === 'number') return props.size
  return sizeMap[props.size] || sizeMap.md
})

// 是否有头像
const hasAvatar = computed(() => {
  return props.avatar && !imageError.value
})

// 头像URL
const avatarUrl = computed(() => {
  return props.avatar
})

// 头像文字
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

// 处理图片加载失败
const handleImageError = () => {
  imageError.value = true
  emit('error')
}

// 点击处理
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
.dt-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0089FF 0%, #00A0FF 100%);
  color: #ffffff;
  font-weight: 500;
  overflow: hidden;
  flex-shrink: 0;
  border-radius: 8px;

  &.clickable {
    cursor: pointer;

    &:hover {
      opacity: 0.85;
    }
  }

  // 群组模式不显示背景色
  &:deep(.dt-group-avatar) {
    background: transparent;
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-text {
    line-height: 1;
  }

  // 尺寸规范
  &.dt-avatar-xs {
    font-size: 12px;
  }

  &.dt-avatar-sm {
    font-size: 14px;
  }

  &.dt-avatar-md {
    font-size: 16px;
  }

  &.dt-avatar-lg {
    font-size: 18px;
  }

  &.dt-avatar-xl {
    font-size: 24px;
  }

  // 在线状态指示器（钉钉7.6规范）
  .avatar-status {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid #ffffff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);

    &.status-online {
      background-color: #52C41A;
      animation: online-pulse 2s infinite;
    }

    &.status-offline {
      background-color: #D9D9D9;
    }

    &.status-busy {
      background-color: #F5222D;
    }

    &.status-away {
      background-color: #FAAD14;
    }
  }
}

// 在线状态脉冲动画
@keyframes online-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(82, 196, 26, 0);
  }
}
</style>
