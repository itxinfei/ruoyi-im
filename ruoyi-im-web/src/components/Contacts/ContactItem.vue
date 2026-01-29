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
.contact-item {
  position: relative;
  height: 60px;
  overflow: hidden; // Hide swipe actions
  background: #fff;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: var(--dt-bg-card-hover);
  }
  
  &.active {
    background: var(--dt-brand-light);
    
    .item-name {
        color: var(--dt-brand-color);
    }
  }
}

.dark .contact-item {
    background: var(--dt-bg-card-dark);
    
    &:hover {
        background: var(--dt-bg-hover-dark);
    }
    
    &.active {
        background: rgba(22, 119, 255, 0.2);
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
  transition: transform 0.3s ease;
}

// Swipe Effect
.swiped-left .contact-content {
    transform: translateX(-70px); // Width of delete button
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
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  :deep(.highlight) {
      color: var(--dt-brand-color);
      font-weight: bold;
  }
}

.dark .item-name {
    color: var(--dt-text-primary-dark);
}

.item-tag {
    font-size: 10px;
    padding: 1px 4px;
    border-radius: 4px;
    background: var(--dt-brand-light);
    color: var(--dt-brand-color);
}

.item-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.dark .item-desc {
    color: var(--dt-text-secondary-dark);
}

// Swipe Actions
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
  font-size: 13px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  &.delete {
      background: var(--dt-error-color);
  }
}
</style>
