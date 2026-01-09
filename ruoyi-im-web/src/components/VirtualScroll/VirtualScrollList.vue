<template>
  <div
    ref="containerRef"
    class="virtual-scroll-list"
    :style="containerStyle"
    @scroll="handleScroll"
  >
    <div class="virtual-scroll-phantom" :style="{ height: `${totalHeight}px` }"></div>
    <div class="virtual-scroll-content" :style="{ transform: `translateY(${offsetY}px)` }">
      <slot
        v-for="item in visibleData"
        :key="item[keyProp] || item.index"
        name="item"
        :item="item"
        :index="item.index"
      ></slot>
    </div>
    <div v-if="loading" class="virtual-scroll-loading">
      <slot name="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { useDebounceFn } from '@/composables/performance/useDebounce'

const props = defineProps({
  // 数据源
  data: {
    type: Array,
    default: () => [],
  },
  // 每项高度（固定高度模式）
  itemHeight: {
    type: Number,
    default: 50,
  },
  // 高度计算函数（动态高度模式）
  itemHeightGetter: {
    type: Function,
    default: null,
  },
  // 容器高度
  height: {
    type: [String, Number],
    default: '100%',
  },
  // 缓冲区数量（上下各渲染多少条）
  bufferCount: {
    type: Number,
    default: 5,
  },
  // 唯一标识字段名
  keyProp: {
    type: String,
    default: 'id',
  },
  // 是否加载更多
  hasMore: {
    type: Boolean,
    default: true,
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false,
  },
  // 触底距离（像素）
  threshold: {
    type: Number,
    default: 100,
  },
})

const emit = defineEmits(['load-more'])

const containerRef = ref(null)
const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算容器样式
const containerStyle = computed(() => {
  const h = typeof props.height === 'number' ? `${props.height}px` : props.height
  return { height: h, overflow: 'auto', position: 'relative' }
})

// 位置缓存
const positions = ref([])

// 初始化位置缓存
const initPositions = () => {
  positions.value = props.data.map((_, index) => ({
    index,
    height: props.itemHeight,
    top: index * props.itemHeight,
    bottom: (index + 1) * props.itemHeight,
  }))
}

// 更新指定项的位置信息
const updatePositions = index => {
  const item = positions.value[index]
  if (!item) return

  const dom = containerRef.value?.querySelector(`[data-index="${index}"]`)
  if (dom) {
    const height = dom.getBoundingClientRect().height
    const oldHeight = item.height
    const diff = height - oldHeight

    if (diff) {
      item.height = height
      item.bottom = item.bottom + diff

      // 更新后面所有项的位置
      for (let i = index + 1; i < positions.value.length; i++) {
        positions.value[i].top = positions.value[i - 1].bottom
        positions.value[i].bottom = positions.value[i].bottom + diff
      }
    }
  }
}

// 批量更新位置
const updateAllPositions = () => {
  positions.value.forEach((_, index) => {
    updatePositions(index)
  })
}

// 计算总高度
const totalHeight = computed(() => {
  const lastItem = positions.value[positions.value.length - 1]
  return lastItem ? lastItem.bottom : 0
})

// 计算当前可视区域的起始索引
const getStartIndex = (scrollTop = 0) => {
  let start = 0
  let end = positions.value.length - 1

  // 二分查找
  while (start < end) {
    const mid = Math.floor((start + end) / 2)
    const item = positions.value[mid]

    if (item.bottom < scrollTop) {
      start = mid + 1
    } else if (item.top > scrollTop) {
      end = mid - 1
    } else {
      start = mid
      break
    }
  }

  return Math.max(0, start - props.bufferCount)
}

// 计算当前可视区域的结束索引
const getEndIndex = (scrollTop = 0) => {
  const viewportBottom = scrollTop + containerHeight.value
  let end = positions.value.length - 1
  let start = 0

  while (start < end) {
    const mid = Math.floor((start + end) / 2)
    const item = positions.value[mid]

    if (item.top > viewportBottom) {
      end = mid - 1
    } else if (item.bottom < viewportBottom) {
      start = mid + 1
    } else {
      end = mid
      break
    }
  }

  return Math.min(positions.value.length - 1, end + props.bufferCount)
}

// 计算偏移量
const offsetY = computed(() => {
  const startIndex = getStartIndex(scrollTop.value)
  const startItem = positions.value[startIndex]
  return startItem ? startItem.top : 0
})

// 计算可见数据
const visibleData = computed(() => {
  const startIndex = getStartIndex(scrollTop.value)
  const endIndex = getEndIndex(scrollTop.value)

  return props.data.slice(startIndex, endIndex + 1).map((item, i) => ({
    ...item,
    index: startIndex + i,
  }))
})

// 处理滚动事件（防抖）
const handleScroll = useDebounceFn(e => {
  const target = e.target
  scrollTop.value = target.scrollTop
  containerHeight.value = target.clientHeight

  // 检查是否需要加载更多
  const { scrollHeight, clientHeight, scrollTop: currentScrollTop } = target
  const distanceToBottom = scrollHeight - clientHeight - currentScrollTop

  if (distanceToBottom < props.threshold && props.hasMore && !props.loading) {
    emit('load-more')
  }
}, 16) // 60fps

// 更新容器高度
const updateContainerHeight = () => {
  if (containerRef.value) {
    containerHeight.value = containerRef.value.clientHeight
  }
}

// 监听数据变化
watch(
  () => props.data,
  () => {
    initPositions()
    nextTick(() => {
      updateAllPositions()
    })
  },
  { immediate: true }
)

// 监听窗口大小变化
const handleResize = useDebounceFn(() => {
  updateContainerHeight()
  updateAllPositions()
}, 200)

onMounted(() => {
  updateContainerHeight()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露方法
defineExpose({
  updatePositions,
  updateAllPositions,
  scrollToTop: () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = 0
    }
  },
  scrollToBottom: () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  },
  getScrollTop: () => scrollTop.value,
})
</script>

<style lang="scss" scoped>
.virtual-scroll-list {
  position: relative;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
}

.virtual-scroll-phantom {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  z-index: -1;
}

.virtual-scroll-content {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  will-change: transform;
}

.virtual-scroll-loading {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  .el-icon {
    font-size: 18px;
  }
}
</style>
