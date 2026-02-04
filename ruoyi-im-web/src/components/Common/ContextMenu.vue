/**
 * 通用右键菜单组件
 *
 * 特性：
 * - 配置驱动：菜单项由数组定义
 * - 边界智能检测：自动反转方向防止超出视口
 * - 键盘导航：↑↓选择，Enter确认，Esc关闭
 * - 图标支持：Material Icons
 * - 分组分隔线：support divider
 * - 禁用状态：support disabled
 * - 快捷键提示：support shortcut 显示
 * - 暗色模式：自动适配
 */
<template>
  <Teleport to="body">
    <Transition name="context-menu-fade">
      <div
        v-if="computedShow"
        ref="menuRef"
        class="context-menu"
        :class="{
          'context-menu--dark': isDark,
          'context-menu--keyboard': isKeyboardMode
        }"
        :style="menuStyle"
        :tabindex="-1"
        @click="handleMenuClick"
        @contextmenu.prevent
      >
        <!-- 菜单项列表 -->
        <div class="context-menu__list" role="menu">
          <template v-for="(item, index) in visibleItems" :key="index">
            <!-- 分隔线 -->
            <div
              v-if="item.divider"
              class="context-menu__divider"
              :class="{ 'context-menu__divider--margin': hasAdjacentItem(index) }"
              role="separator"
              :aria-hidden="true"
            />
            <!-- 菜单项 -->
            <div
              v-else
              class="context-menu__item"
              :class="{
                'context-menu__item--disabled': item.disabled,
                'context-menu__item--danger': item.danger,
                'context-menu__item--focused': focusedIndex === index
              }"
              role="menuitem"
              :tabindex="item.disabled ? -1 : 0"
              @click.stop="handleItemClick(item, index)"
              @mouseenter="handleItemMouseEnter(index)"
            >
              <!-- 图标 -->
              <span v-if="item.icon" class="context-menu__icon">
                <span v-if="isMaterialIcon(item.icon)" class="material-icons-outlined">{{ item.icon }}</span>
                <component v-else :is="item.icon" />
              </span>
              <!-- 标签 -->
              <span class="context-menu__label">{{ item.label }}</span>
              <!-- 快捷键提示 -->
              <span v-if="item.shortcut" class="context-menu__shortcut">{{ item.shortcut }}</span>
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, getCurrentInstance } from 'vue'
import { useDark } from '@vueuse/core'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  x: {
    type: Number,
    default: 0
  },
  y: {
    type: Number,
    default: 0
  },
  items: {
    type: Array,
    default: () => []
  },
  boundary: {
    type: Object,
    default: null // HTMLElement or window
  },
  offsetX: {
    type: Number,
    default: 8
  },
  offsetY: {
    type: Number,
    default: 8
  },
  maxWidth: {
    type: Number,
    default: 240
  },
  minWidth: {
    type: Number,
    default: 160
  },
  zIndex: {
    type: Number,
    default: 2000
  },
  closeOnClick: {
    type: Boolean,
    default: true
  },
  closeOnScroll: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:show', 'select', 'close'])

// 获取全局暗色模式状态
const isDark = useDark()

// 内部显示状态
const computedShow = ref(props.show)
const menuRef = ref(null)
const focusedIndex = ref(-1)
const isKeyboardMode = ref(false)

// 菜单尺寸（用于边界检测）
const menuWidth = ref(0)
const menuHeight = ref(0)

// 可见的菜单项（过滤掉隐藏的）
const visibleItems = computed(() => {
  return props.items.filter(item => item.visible !== false)
})

// 检查是否有相邻项（用于分隔线边距）
const hasAdjacentItem = (index) => {
  const prevItem = visibleItems.value[index - 1]
  const nextItem = visibleItems.value[index + 1]
  return !!(prevItem && !prevItem.divider) || !!(nextItem && !nextItem.divider)
}

// 计算菜单位置（智能边界检测）
const menuStyle = computed(() => {
  if (!computedShow.value) {
    return { display: 'none' }
  }

  let x = props.x + props.offsetX
  let y = props.y + props.offsetY

  // 获取边界
  const boundary = props.boundary
  let boundaryRect
  if (boundary instanceof HTMLElement) {
    boundaryRect = boundary.getBoundingClientRect()
  } else {
    boundaryRect = {
      top: 0,
      left: 0,
      width: window.innerWidth,
      height: window.innerHeight
    }
  }

  const padding = 8 // 边界内边距

  // 右边界检测
  if (x + menuWidth.value > boundaryRect.width - padding) {
    x = props.x - menuWidth.value - props.offsetX
  }

  // 下边界检测
  if (y + menuHeight.value > boundaryRect.height - padding) {
    y = props.y - menuHeight.value - props.offsetY
  }

  // 确保不超出左/上边界
  x = Math.max(padding, x)
  y = Math.max(padding, y)

  return {
    position: 'fixed',
    left: `${x}px`,
    top: `${y}px`,
    minWidth: `${props.minWidth}px`,
    maxWidth: `${props.maxWidth}px`,
    zIndex: props.zIndex
  }
})

// 判断是否为 Material Icons
const isMaterialIcon = (icon) => {
  return typeof icon === 'string'
}

// 处理菜单点击
const handleMenuClick = () => {
  if (props.closeOnClick) {
    close()
  }
}

// 处理菜单项点击
const handleItemClick = (item, index) => {
  if (item.disabled) return

  focusedIndex.value = index
  emit('select', item)
  close()
}

// 处理菜单项鼠标进入
const handleItemMouseEnter = (index) => {
  isKeyboardMode.value = false
  focusedIndex.value = index
}

// 关闭菜单
const close = () => {
  computedShow.value = false
  focusedIndex.value = -1
  isKeyboardMode.value = false
  emit('update:show', false)
  emit('close')
}

// 键盘导航处理
const handleKeydown = (e) => {
  if (!computedShow.value) return

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      isKeyboardMode.value = true
      if (focusedIndex.value < visibleItems.value.length - 1) {
        focusedIndex.value++
      } else {
        focusedIndex.value = 0 // 循环到第一个
      }
      scrollToFocusedItem()
      break
    case 'ArrowUp':
      e.preventDefault()
      isKeyboardMode.value = true
      if (focusedIndex.value > 0) {
        focusedIndex.value--
      } else {
        focusedIndex.value = visibleItems.value.length - 1 // 循环到最后一个
      }
      scrollToFocusedItem()
      break
    case 'Enter':
      e.preventDefault()
      if (focusedIndex.value >= 0) {
        const item = visibleItems.value[focusedIndex.value]
        if (item && !item.disabled) {
          handleItemClick(item, focusedIndex.value)
        }
      }
      break
    case 'Escape':
      e.preventDefault()
      close()
      break
    case 'Home':
      e.preventDefault()
      isKeyboardMode.value = true
      focusedIndex.value = 0
      scrollToFocusedItem()
      break
    case 'End':
      e.preventDefault()
      isKeyboardMode.value = true
      focusedIndex.value = visibleItems.value.length - 1
      scrollToFocusedItem()
      break
  }
}

// 滚动到聚焦的菜单项
const scrollToFocusedItem = () => {
  nextTick(() => {
    const focusedElement = menuRef.value?._elRef?.querySelector(`.context-menu__item--focused`)
    if (focusedElement) {
      focusedElement.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  })
}

// 外部点击处理
const handleOutsideClick = (e) => {
  if (!computedShow.value) return
  if (menuRef.value && !menuRef.value.contains(e.target)) {
    close()
  }
}

// 滚动处理
const handleScroll = () => {
  if (computedShow.value && props.closeOnScroll) {
    close()
  }
}

// 更新菜单尺寸
const updateMenuSize = () => {
  if (menuRef.value) {
    menuWidth.value = menuRef.value.offsetWidth || props.maxWidth
    menuHeight.value = menuRef.value.offsetHeight || 0
  }
}

// 监听显示状态变化
watch(() => props.show, (newVal) => {
  computedShow.value = newVal
  if (newVal) {
    nextTick(() => {
      updateMenuSize()
      focusedIndex.value = -1
    })
  }
})

// 监听 items 变化
watch(() => props.items, () => {
  nextTick(updateMenuSize)
}, { deep: true })

// 生命周期
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('scroll', handleScroll, true)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('scroll', handleScroll, true)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.context-menu {
  background: var(--dt-bg-card, #ffffff);
  border: 1px solid var(--dt-border-light, #e5e5e5);
  border-radius: var(--dt-radius-md, 6px);
  box-shadow: var(--dt-shadow-float);
  padding: 4px 0;
  min-width: 160px;
  max-width: 240px;
  user-select: none;
  outline: none;

  // 暗色模式
  &--dark {
    background: #2c2c2c;
    border-color: #3d3d3d;
  }

  // 键盘模式指示器（可选）
  &--keyboard {
    .context-menu__item--focused {
      background: var(--dt-bg-session-hover, #f5f5f5);
    }
  }
}

.context-menu__list {
  display: flex;
  flex-direction: column;
}

.context-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--dt-text-primary, #333333);
  cursor: pointer;
  transition: background-color 0.15s ease;
  border-radius: var(--dt-radius-sm);
  margin: 0 4px;

  &:hover {
    background: var(--dt-bg-session-hover, #f5f5f5);
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color, #0089ff);
    outline-offset: -2px;
  }

  &--focused {
    background: var(--dt-bg-session-hover, #f5f5f5);
  }

  &--disabled {
    color: var(--dt-text-tertiary, #999999);
    cursor: not-allowed;
    opacity: 0.6;

    &:hover {
      background: transparent;
    }
  }

  &--danger {
    color: var(--dt-error-color, #ff4d4f);

    &:hover:not(.context-menu__item--disabled) {
      background: rgba(255, 77, 79, 0.1);
    }
  }
}

.context-menu__icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--dt-text-secondary, #666666);

  .material-icons-outlined {
    font-size: 18px;
  }
}

.context-menu__label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.context-menu__shortcut {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--dt-text-tertiary, #999999);
  margin-left: auto;
}

.context-menu__divider {
  height: 1px;
  background: var(--dt-border-lighter, #e5e5e5);
  margin: 4px 0;

  &--margin {
    margin: 6px 0;
  }
}

// 暗色模式下的特殊样式
.context-menu--dark {
  .context-menu__item {
    color: #e5e5e5;

    &:hover {
      background: #3d3d3d;
    }

    &--focused {
      background: #3d3d3d;
    }

    &--disabled {
      color: #666666;
    }
  }

  .context-menu__icon {
    color: #aaaaaa;
  }

  .context-menu__shortcut {
    color: #666666;
  }

  .context-menu__divider {
    background: #3d3d3d;
  }
}

// 动画
.context-menu-fade-enter-active,
.context-menu-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.context-menu-fade-enter-from,
.context-menu-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.context-menu-fade-enter-to,
.context-menu-fade-leave-from {
  opacity: 1;
  transform: scale(1);
}
</style>
