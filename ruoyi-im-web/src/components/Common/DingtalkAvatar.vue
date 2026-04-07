<template>
  <div class="dingtalk-avatar" :class="avatarClass" :style="{ width: size + 'px', height: size + 'px', fontSize: fontSize + 'px', backgroundColor: isGroup ? 'var(--dt-bg-body)' : bgColor }">
    <!-- 群组聚合头像 -->
    <div v-if="isGroup" class="group-avatar-grid" :class="'grid-' + displayMembers.length">
      <div
        v-for="(m, index) in displayMembers"
        :key="m.userId || index"
        class="grid-item"
        :style="{ backgroundColor: getMemberBgColor(m) }"
      >
        <img
          v-if="m.avatar && !m.error"
          :src="addTokenToUrl(m.avatar)"
          class="grid-img"
          @error="m.error = true"
        >
        <span v-else class="grid-text">{{ getMemberName(m) }}</span>
      </div>
      <!-- overflow 指示：显示未显示的额外成员数量 -->
      <div v-if="overflowCount > 0" class="grid-item overflow-indicator">
        +{{ overflowCount }}
      </div>
    </div>

    <!-- 普通单人头像 -->
    <template v-else>
      <img
        v-if="imageUrl && !imageError"
        :src="imageUrl"
        class="avatar-img"
        @error="handleImageError"
      >
      <span v-else class="avatar-text">{{ displayName }}</span>
    </template>
  </div>
  </template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  // 头像URL
  src: String,
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
  customClass: String,
  // 是否为群组
  isGroup: {
    type: Boolean,
    default: false
  },
  // 群成员列表 (用于聚合头像)
  members: {
    type: Array,
    default: () => []
  },
  // 最大可视成员数（聚合头像显示上限）
  maxVisible: {
    type: Number,
    default: 9
  }
})

const imageError = ref(false)
const imageUrl = computed(() => addTokenToUrl(props.src))

// 处理聚合头像的成员显示
const displayMembers = computed(() => {
  if (!props.isGroup) return []
  return props.members.slice(0, props.maxVisible).map(m => reactive({ ...m, error: false }))
})

// 溢出成员数量
const overflowCount = computed(() => {
  if (!props.isGroup || !props.members) return 0
  return Math.max(0, props.members.length - props.maxVisible)
})

const getMemberName = (m) => {
  const name = m.name || m.nickname || '?'
  return name.charAt(0).toUpperCase()
}

const displayName = computed(() => {
  const name = props.name || '?'
  return name.charAt(0).toUpperCase()
})

// 字体大小比例（头像尺寸下的 40%）
const fontSize = computed(() => Math.max(12, Math.floor(props.size * (props.isGroup ? 0.25 : 0.4))))

const colorPalette = [
  '#F5A623', '#6DD400', '#44B6AE', '#4A90E2', '#9013FE',
  '#F5E066', '#FF6B6B', '#6BCF7F', '#4ECDC4', '#45B7D1',
  '#96CEB4', '#FFEAA7', '#DFE6E9', '#74B9FF', '#A29BFE'
]

const getMemberBgColor = (m) => {
  const id = String(m.userId || m.id || m.name || '')
  let hash = 0
  for (let i = 0; i < id.length; i++) {
    hash = ((hash << 5) - hash) + id.charCodeAt(i)
    hash |= 0
  }
  return colorPalette[Math.abs(hash) % colorPalette.length]
}

const bgColor = computed(() => {
  if (props.userId) {
    const id = String(props.userId)
    let hash = 0
    for (let i = 0; i < id.length; i++) {
      hash = ((hash << 5) - hash) + id.charCodeAt(i)
      hash |= 0
    }
    return colorPalette[Math.abs(hash) % colorPalette.length]
  }
  return '#4A90E2'
})

const avatarClass = computed(() => {
  const classes = []
  classes.push(props.shape === 'square' ? 'avatar-square' : 'avatar-circle')
  if (props.isGroup) classes.push('is-group')
  if (props.customClass) classes.push(props.customClass)
  return classes.join(' ')
})

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped lang="scss">
.dingtalk-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-white);
  font-weight: 500;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
  transition: opacity var(--dt-transition-fast);
  line-height: 1;
  box-sizing: border-box;
  background-color: var(--dt-border-light);

  &.is-group {
    padding: var(--dt-spacing-xxs);
    background-color: var(--dt-bg-body);
  }
}

.avatar-circle { border-radius: 50%; }
.avatar-square { border-radius: var(--dt-radius-sm); }

.avatar-img { width: 100%; height: 100%; object-fit: cover; }

/* 群组网格布局 */
.group-avatar-grid {
  display: grid;
  width: 100%;
  height: 100%;
  gap: var(--dt-group-avatar-gap, 1px);
  background: var(--dt-bg-body);

  .grid-item {
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    color: var(--dt-text-white);
    font-size: 10px;
    line-height: 1;
    font-weight: 600;
  }

  .grid-img { width: 100%; height: 100%; object-fit: cover; }

  /* 不同人数的网格分布 */
  &.grid-0 { background-color: var(--dt-brand-color); display: flex; align-items: center; justify-content: center;
    &::after { content: 'G'; color: var(--dt-text-white); font-size: 20px; }
  }
  &.grid-1 { grid-template-columns: 1fr; .grid-item { font-size: 16px; } }
  &.grid-2 { grid-template-columns: 1fr 1fr; .grid-item { height: 100%; font-size: 14px; } }
  &.grid-3 {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    .grid-item:first-child { grid-column: 1 / 3; }
  }
  &.grid-4 { grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; }
  &.grid-5, &.grid-6 { grid-template-columns: 1fr 1fr 1fr; grid-template-rows: 1fr 1fr; .grid-item { font-size: 9px; } }
  &.grid-7, &.grid-8, &.grid-9 { grid-template-columns: 1fr 1fr 1fr; grid-template-rows: 1fr 1fr 1fr; .grid-item { font-size: 8px; } }
}

.overflow-indicator {
  font-weight: 700;
  color: var(--dt-group-avatar-overflow-color, var(--dt-text-white));
  background-color: var(--dt-group-avatar-overflow-bg, var(--dt-brand-color));
}
</style>
