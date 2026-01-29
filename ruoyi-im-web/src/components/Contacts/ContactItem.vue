<template>
  <div
    class="contact-item"
    :class="{ 
      active: isActive,
      'swiped-left': isSwipedLeft
    }"
    @click="$emit('click', item)"
    @contextmenu.prevent="$emit('contextmenu', $event, item)"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <!-- 主要内容 -->
    <div class="contact-content">
      <DingtalkAvatar
        :name="item.name || item.displayName"
        :size="36"
        :src="item.avatar"
        :shape="item.type === 'group' ? 'square' : 'circle'"
      />
      <div class="item-info">
        <div class="item-header">
          <span class="item-name" v-html="highlightName"></span>
          <span v-if="item.tag" class="item-tag">{{ item.tag }}</span>
        </div>
        <div class="item-desc" v-if="item.description || item.dept || item.position">
            {{ item.description || item.position || item.dept }}
        </div>
      </div>
    </div>

    <!-- 滑动操作菜单 (Mobile) -->
    <div class="swipe-actions">
      <button class="action-btn delete" @click.stop="$emit('delete', item)">
        删除
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  },
  searchQuery: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click', 'contextmenu', 'delete'])

// 高亮显示搜索关键词
const highlightName = computed(() => {
  const name = props.item.name || props.item.displayName || ''
  if (!props.searchQuery) return name
  const reg = new RegExp(`(${props.searchQuery})`, 'gi')
  return name.replace(reg, '<span class="highlight">$1</span>')
})

// ========== Touch Swipe Logic ==========
const touchStartX = ref(0)
const touchStartY = ref(0)
const isSwipedLeft = ref(false)

const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
}

const handleTouchMove = (e) => {
  // Simple Horizontal Swipe Detection
  const deltaX = e.touches[0].clientX - touchStartX.value
  const deltaY = e.touches[0].clientY - touchStartY.value
  
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 30) {
      // Horizontal move dominant
      if (deltaX < 0) {
          isSwipedLeft.value = true
      } else {
          isSwipedLeft.value = false
      }
  }
}

const handleTouchEnd = () => {
  // Optional: Add snap logic or velocity check
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-item {
  position: relative;
  height: 64px;
  overflow: hidden;
  background: #ffffff;
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-brand-bg);

    .item-name {
      color: var(--dt-brand-color);
    }
  }
}

// 暗色模式适配
.dark .contact-item {
  background: var(--dt-bg-card-dark);

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.active {
    background: rgba(22, 119, 255, 0.15);
  }
}

.contact-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 16px;
  background: inherit;
  transition: transform var(--dt-transition-base);
}

// 滑动删除效果
.swiped-left .contact-content {
  transform: translateX(-70px);
}

.item-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .dark & {
    color: var(--dt-text-primary-dark);
  }

  :deep(.highlight) {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-semibold);
  }
}

.item-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-weight: var(--dt-font-weight-medium);
  flex-shrink: 0;
}

.item-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;

  .dark & {
    color: var(--dt-text-secondary-dark);
  }
}

// 滑动操作按钮
.swipe-actions {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 70px;
  display: flex;
  z-index: 1;
}

.action-btn {
  flex: 1;
  border: none;
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: opacity var(--dt-transition-fast);

  &:active {
    opacity: 0.8;
  }

  &.delete {
    background: var(--dt-error-color);
  }
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 768px) {
  .contact-item {
    height: 60px;
  }

  .contact-content {
    padding: 0 12px;
  }
}
</style>
