<template>
  <div class="dt-avatar" :class="[sizeClass, { clickable }]" @click="handleClick">
    <img 
      v-if="avatar && avatar !== ''" 
      :src="avatar" 
      :alt="name"
      class="avatar-image"
      @error="handleImageError"
    />
    <div v-else class="avatar-placeholder">
      {{ getAvatarText(name) }}
    </div>
    
    <!-- 在线状态指示器 -->
    <div v-if="showOnlineStatus" class="online-indicator" :class="onlineStatusClass"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    default: ''
  },
  avatar: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 32,
    validator: (value) => [24, 28, 32, 36, 40, 48, 64, 80].includes(value)
  },
  clickable: {
    type: Boolean,
    default: false
  },
  showOnlineStatus: {
    type: Boolean,
    default: false
  },
  onlineStatus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const sizeClass = computed(() => `size-${props.size}`)

const onlineStatusClass = computed(() => ({
  'status-online': props.onlineStatus,
  'status-offline': !props.onlineStatus
}))

function getAvatarText(name) {
  if (!name) return '?'
  
  // 优先取中文字符的前两个
  const chineseMatch = name.match(/[\u4e00-\u9fa5]/g)
  if (chineseMatch && chineseMatch.length >= 2) {
    return chineseMatch.slice(0, 2).join('')
  } else if (chineseMatch && chineseMatch.length === 1) {
    return chineseMatch[0] + (name.charAt(1) || '')
  }
  
  // 英文取首字母
  const words = name.trim().split(/\s+/)
  if (words.length >= 2) {
    return (words[0][0] + words[1][0]).toUpperCase()
  } else {
    return name.substring(0, 2).toUpperCase()
  }
}

function handleClick() {
  if (props.clickable) {
    emit('click')
  }
}

function handleImageError() {
  // 图片加载失败时可以使用默认头像
  console.warn('Avatar image load error:', props.avatar)
}
</script>

<style scoped>
.dt-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  font-weight: 600;
  color: #fff;
  user-select: none;
}

.dt-avatar.clickable {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.dt-avatar.clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 尺寸变体 */
.size-24 {
  width: 24px;
  height: 24px;
}

.size-24 .avatar-placeholder {
  font-size: 8px;
}

.size-28 {
  width: 28px;
  height: 28px;
}

.size-28 .avatar-placeholder {
  font-size: 9px;
}

.size-32 {
  width: 32px;
  height: 32px;
}

.size-32 .avatar-placeholder {
  font-size: 10px;
}

.size-36 {
  width: 36px;
  height: 36px;
}

.size-36 .avatar-placeholder {
  font-size: 11px;
}

.size-40 {
  width: 40px;
  height: 40px;
}

.size-40 .avatar-placeholder {
  font-size: 12px;
}

.size-48 {
  width: 48px;
  height: 48px;
}

.size-48 .avatar-placeholder {
  font-size: 14px;
}

.size-64 {
  width: 64px;
  height: 64px;
}

.size-64 .avatar-placeholder {
  font-size: 18px;
}

.size-80 {
  width: 80px;
  height: 80px;
}

.size-80 .avatar-placeholder {
  font-size: 22px;
}

/* 在线状态指示器 */
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 25%;
  height: 25%;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
}

.status-online {
  background: #52c41a;
}

.status-offline {
  background: #d9d9d9;
}

/* 不同尺寸下的指示器大小 */
.size-24 .online-indicator {
  width: 8px;
  height: 8px;
  border-width: 1px;
}

.size-28 .online-indicator {
  width: 8px;
  height: 8px;
  border-width: 1px;
}

.size-32 .online-indicator {
  width: 10px;
  height: 10px;
  border-width: 2px;
}

.size-36 .online-indicator {
  width: 10px;
  height: 10px;
  border-width: 2px;
}

.size-40 .online-indicator {
  width: 12px;
  height: 12px;
  border-width: 2px;
}

.size-48 .online-indicator {
  width: 14px;
  height: 14px;
  border-width: 2px;
}

.size-64 .online-indicator {
  width: 18px;
  height: 18px;
  border-width: 2px;
}

.size-80 .online-indicator {
  width: 22px;
  height: 22px;
  border-width: 3px;
}
</style>