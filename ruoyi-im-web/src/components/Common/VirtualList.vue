/**
 * 虚拟滚动列表组件
 * 用于高性能渲染大量消息列表
 *
 * @author ruoyi
 */
<template>
  <div
    ref="containerRef"
    class="virtual-list"
    :style="{ height: `${height}px` }"
    @scroll="handleScroll"
  >
    <!-- 撑开滚动高度的占位元素 -->
    <div class="virtual-list-phantom" :style="{ height: `${totalHeight}px` }"></div>

    <!-- 可见区域的内容 -->
    <div
      class="virtual-list-content"
      :style="{ transform: `translateY(${offset}px)` }"
    >
      <div
        v-for="item in visibleData"
        :key="item._index"
        :data-index="item._index"
        class="virtual-list-item"
        :style="{ height: `${itemSize}px` }"
      >
        <slot :item="item.data" :index="item._index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  // 数据源
  items: {
    type: Array,
    default: () => []
  },
  // 每项的高度（固定高度模式）
  itemSize: {
    type: Number,
    default: 80
  },
  // 容器高度
  height: {
    type: Number,
    default: 400
  },
  // 缓冲区大小（额外渲染的不可见项数）
  buffer: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['scroll', 'visible-change'])

const containerRef = ref(null)
const scrollTop = ref(0)
const containerHeight = ref(props.height)

// 计算总高度
const totalHeight = computed(() => props.items.length * props.itemSize)

// 计算可见区域的起始索引
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemSize) - props.buffer)
})

// 计算可见区域的结束索引
const endIndex = computed(() => {
  return Math.min(
    props.items.length,
    Math.ceil((scrollTop.value + containerHeight.value) / props.itemSize) + props.buffer
  )
})

// 计算偏移量
const offset = computed(() => {
  return startIndex.value * props.itemSize
})

// 可见数据
const visibleData = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value).map((data, i) => ({
    data,
    _index: startIndex.value + i
  }))
})

// 处理滚动事件
const handleScroll = (e) => {
  scrollTop.value = e.target.scrollTop
  emit('scroll', e)

  // 检测是否滚动到底部
  const { scrollHeight, clientHeight, scrollTop: currentScrollTop } = e.target
  const distanceFromBottom = scrollHeight - clientHeight - currentScrollTop

  if (distanceFromBottom < props.itemSize * 2) {
    emit('visible-change', { nearBottom: true })
  }
}

// 滚动到指定索引
const scrollToIndex = (index) => {
  if (!containerRef.value) return
  const target = Math.max(0, Math.min(index, props.items.length - 1))
  const top = target * props.itemSize
  containerRef.value.scrollTop = top
}

// 滚动到顶部
const scrollToTop = (smooth = true) => {
  if (!containerRef.value) return
  containerRef.value.scrollTo({
    top: 0,
    behavior: smooth ? 'smooth' : 'auto'
  })
}

// 滚动到底部
const scrollToBottom = (smooth = true) => {
  if (!containerRef.value) return
  containerRef.value.scrollTo({
    top: totalHeight.value,
    behavior: smooth ? 'smooth' : 'auto'
  })
}

// 获取滚动位置
const getScrollInfo = () => {
  if (!containerRef.value) return null
  return {
    scrollTop: containerRef.value.scrollTop,
    scrollHeight: containerRef.value.scrollHeight,
    clientHeight: containerRef.value.clientHeight
  }
}

// 监听数据变化，保持滚动位置（如果需要在底部）
watch(() => props.items.length, () => {
  // 可以在这里添加逻辑，比如如果之前在底部，新数据来了之后保持底部
})

defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom,
  getScrollInfo,
  containerRef
})
</script>

<style scoped lang="scss">
.virtual-list {
  position: relative;
  overflow-y: auto;
  overflow-anchor: none;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
  }

  .dark &:hover::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
  }
}

.virtual-list-phantom {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: -1;
}

.virtual-list-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.virtual-list-item {
  box-sizing: border-box;
}
</style>
