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

// 高亮显示搜索关键词（转义特殊字符）
const highlightName = computed(() => {
  const name = props.item.name || props.item.displayName || ''
  if (!props.searchQuery) return name

  // 转义正则特殊字符
  const escapedQuery = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const reg = new RegExp(`(${escapedQuery})`, 'gi')
  return name.replace(reg, '<span class="highlight">$1</span>')
})

// ========== Touch Swipe Logic ==========

const SWIPE_THRESHOLD = 30 // 滑动阈值
const touchStartX = ref(0)
const touchStartY = ref(0)
const isSwipedLeft = ref(false)

const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
}

const handleTouchMove = (e) => {
  const deltaX = e.touches[0].clientX - touchStartX.value
  const deltaY = e.touches[0].clientY - touchStartY.value

  // 水平滑动判定：水平移动距离 > 垂直移动距离 && 超过阈值
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_THRESHOLD) {
    isSwipedLeft.value = deltaX < 0
  }
}

const handleTouchEnd = () => {
  // 可选：添加回弹逻辑或速度检测
}

// 防止 XSS 攻击：对用户输入进行转义
const escapeHtml = (str) => {
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-item {
  position: relative;
  height: 60px; /* 钉钉标准列表项高度 */
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
    background: rgba(0, 137, 255, 0.15);
  }
}

.contact-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 12px; /* 钉钉标准内边距 */
  gap: 12px; /* 头像和内容之间的间距 */
  background: inherit;
  transition: transform var(--dt-transition-base);
}

// 滑动删除效果
.swiped-left .contact-content {
  transform: translateX(-70px);
}

.item-info {
  flex: 1;
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
    height: 56px; /* 移动端适配 */
  }

  .contact-content {
    padding: 0 12px;
  }
}
</style>
