import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 虚拟列表组合式函数
 * @param {Object} options - 配置选项
 * @returns {Object} 虚拟列表相关的方法和状态
 */
export function useVirtualList(options = {}) {
  const {
    data = ref([]),
    itemHeight = 50,
    containerHeight = 600,
    overscan = 5,
    keyField = 'id'
  } = options

  // 容器引用
  const containerRef = ref(null)
  const scrollTop = ref(0)
  const isScrolling = ref(false)
  let scrollTimer = null

  // 位置缓存
  const positions = ref([])

  // 初始化位置缓存
  const initPositions = () => {
    positions.value = data.value.map((item, index) => ({
      [keyField]: item[keyField],
      index,
      height: itemHeight,
      top: index * itemHeight,
      bottom: (index + 1) * itemHeight,
      dHeight: 0 // 动态高度差
    }))
  }

  // 更新指定项的位置
  const updateItemSize = (index) => {
    const position = positions.value[index]
    if (!position) return

    const node = containerRef.value?.querySelector(`[data-index="${index}"]`)
    if (!node) return

    const oldHeight = position.height
    const newHeight = node.getBoundingClientRect().height
    const dHeight = newHeight - oldHeight

    // 如果高度有变化，更新位置
    if (dHeight) {
      position.height = newHeight
      position.bottom = position.bottom + dHeight
      position.dHeight = dHeight

      // 更新后面所有项的位置
      for (let i = index + 1; i < positions.value.length; i++) {
        const nextPosition = positions.value[i]
        nextPosition.top = positions.value[i - 1].bottom
        nextPosition.bottom = nextPosition.bottom + dHeight
      }
    }
  }

  // 批量更新位置
  const updateAllSizes = () => {
    positions.value.forEach((_, index) => {
      updateItemSize(index)
    })
  }

  // 获取总高度
  const totalHeight = computed(() => {
    const lastPosition = positions.value[positions.value.length - 1]
    return lastPosition ? lastPosition.bottom : 0
  })

  // 计算可见范围
  const getVisibleRange = (scrollTop = 0) => {
    const start = getStartIndex(scrollTop)
    const end = getEndIndex(scrollTop)
    return { start, end }
  }

  // 二分查找起始索引
  const getStartIndex = (scrollTop = 0) => {
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.bottom < scrollTop) {
        start = mid + 1
      } else if (position.top > scrollTop) {
        end = mid - 1
      } else {
        start = mid
        break
      }
    }

    return Math.max(0, start - overscan)
  }

  // 二分查找结束索引
  const getEndIndex = (scrollTop = 0) => {
    const viewportBottom = scrollTop + containerHeight
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.top > viewportBottom) {
        end = mid - 1
      } else if (position.bottom < viewportBottom) {
        start = mid + 1
      } else {
        end = mid
        break
      }
    }

    return Math.min(positions.value.length - 1, end + overscan)
  }

  // 计算可见数据
  const visibleData = computed(() => {
    const { start, end } = getVisibleRange(scrollTop.value)
    return data.value.slice(start, end + 1).map((item, i) => ({
      ...item,
      __index: start + i
    }))
  })

  // 计算偏移量
  const offset = computed(() => {
    const startIndex = getStartIndex(scrollTop.value)
    const position = positions.value[startIndex]
    return position ? position.top : 0
  })

  // 处理滚动事件
  const handleScroll = (e) => {
    const target = e.target
    scrollTop.value = target.scrollTop
    isScrolling.value = true

    // 滚动结束检测
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
    scrollTimer = setTimeout(() => {
      isScrolling.value = false
    }, 150)
  }

  // 滚动到指定索引
  const scrollToIndex = (index) => {
    const position = positions.value[index]
    if (!position || !containerRef.value) return

    const scrollTop = position.top - containerHeight / 2 + position.height / 2
    containerRef.value.scrollTop = scrollTop
  }

  // 滚动到顶部
  const scrollToTop = () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = 0
    }
  }

  // 滚动到底部
  const scrollToBottom = () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  }

  // 获取滚动信息
  const getScrollInfo = () => {
    return {
      scrollTop: scrollTop.value,
      isScrolling: isScrolling.value,
      visibleRange: getVisibleRange(scrollTop.value),
      totalHeight: totalHeight.value
    }
  }

  // 监听数据变化
  watch(data, () => {
    initPositions()
    nextTick(() => {
      updateAllSizes()
    })
  }, { deep: true, immediate: true })

  // 组件挂载后初始化
  onMounted(() => {
    initPositions()
    nextTick(() => {
      updateAllSizes()
    })
  })

  // 组件卸载时清理
  onUnmounted(() => {
    if (scrollTimer) {
      clearTimeout(scrollTimer)
    }
  })

  return {
    // 引用
    containerRef,
    // 状态
    scrollTop,
    isScrolling,
    // 计算属性
    visibleData,
    totalHeight,
    offset,
    // 方法
    handleScroll,
    scrollToIndex,
    scrollToTop,
    scrollToBottom,
    updateItemSize,
    updateAllSizes,
    getScrollInfo,
    getVisibleRange,
    // 内部状态（用于调试）
    positions
  }
}

/**
 * 动态高度的虚拟列表
 * @param {Object} options - 配置选项
 * @returns {Object} 虚拟列表相关的方法和状态
 */
export function useDynamicVirtualList(options = {}) {
  const {
    data = ref([]),
    estimatedItemHeight = 50,
    containerHeight = 600,
    overscan = 5,
    keyField = 'id',
    getItemHeight = null
  } = options

  const containerRef = ref(null)
  const scrollTop = ref(0)

  // 使用最小估计高度初始化
  const positions = ref([])

  const initPositions = () => {
    positions.value = data.value.map((item, index) => ({
      [keyField]: item[keyField],
      index,
      height: estimatedItemHeight,
      top: index * estimatedItemHeight,
      bottom: (index + 1) * estimatedItemHeight,
      measured: false // 是否已测量
    }))
  }

  // 更新项目高度
  const updateItemSize = (index, height) => {
    const position = positions.value[index]
    if (!position) return

    if (position.measured && position.height === height) {
      return
    }

    const oldHeight = position.height
    const dHeight = height - oldHeight

    position.height = height
    position.bottom = position.bottom + dHeight
    position.measured = true

    // 更新后续项的位置
    for (let i = index + 1; i < positions.value.length; i++) {
      const nextPosition = positions.value[i]
      nextPosition.top = positions.value[i - 1].bottom
      nextPosition.bottom = nextPosition.bottom + dHeight
    }
  }

  // 测量并更新所有可见项的高度
  const measureVisibleItems = () => {
    const { start, end } = getVisibleRange(scrollTop.value)

    for (let i = start; i <= end; i++) {
      const node = containerRef.value?.querySelector(`[data-index="${i}"]`)
      if (node) {
        const height = node.getBoundingClientRect().height
        updateItemSize(i, height)
      }
    }
  }

  const totalHeight = computed(() => {
    const lastPosition = positions.value[positions.value.length - 1]
    return lastPosition ? lastPosition.bottom : 0
  })

  const getStartIndex = (scrollTop = 0) => {
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.bottom < scrollTop) {
        start = mid + 1
      } else if (position.top > scrollTop) {
        end = mid - 1
      } else {
        start = mid
        break
      }
    }

    return Math.max(0, start - overscan)
  }

  const getEndIndex = (scrollTop = 0) => {
    const viewportBottom = scrollTop + containerHeight
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.top > viewportBottom) {
        end = mid - 1
      } else if (position.bottom < viewportBottom) {
        start = mid + 1
      } else {
        end = mid
        break
      }
    }

    return Math.min(positions.value.length - 1, end + overscan)
  }

  const getVisibleRange = (scrollTop = 0) => {
    return {
      start: getStartIndex(scrollTop),
      end: getEndIndex(scrollTop)
    }
  }

  const visibleData = computed(() => {
    const { start, end } = getVisibleRange(scrollTop.value)
    return data.value.slice(start, end + 1).map((item, i) => ({
      ...item,
      __index: start + i
    }))
  })

  const offset = computed(() => {
    const startIndex = getStartIndex(scrollTop.value)
    const position = positions.value[startIndex]
    return position ? position.top : 0
  })

  const handleScroll = (e) => {
    scrollTop.value = e.target.scrollTop
  }

  const scrollToIndex = (index) => {
    const position = positions.value[index]
    if (!position || !containerRef.value) return

    const scrollTop = position.top - containerHeight / 2 + position.height / 2
    containerRef.value.scrollTop = scrollTop
  }

  watch(data, () => {
    initPositions()
  }, { deep: true, immediate: true })

  onMounted(() => {
    initPositions()
    // 延迟测量，确保DOM已渲染
    nextTick(() => {
      measureVisibleItems()
    })
  })

  return {
    containerRef,
    scrollTop,
    visibleData,
    totalHeight,
    offset,
    handleScroll,
    scrollToIndex,
    updateItemSize,
    measureVisibleItems,
    getVisibleRange,
    positions
  }
}

/**
 * 横向虚拟列表
 * @param {Object} options - 配置选项
 * @returns {Object} 虚拟列表相关的方法和状态
 */
export function useHorizontalVirtualList(options = {}) {
  const {
    data = ref([]),
    itemWidth = 100,
    containerWidth = 800,
    overscan = 3,
    keyField = 'id'
  } = options

  const containerRef = ref(null)
  const scrollLeft = ref(0)

  const positions = ref([])

  const initPositions = () => {
    positions.value = data.value.map((item, index) => ({
      [keyField]: item[keyField],
      index,
      width: itemWidth,
      left: index * itemWidth,
      right: (index + 1) * itemWidth
    }))
  }

  const totalWidth = computed(() => {
    const lastPosition = positions.value[positions.value.length - 1]
    return lastPosition ? lastPosition.right : 0
  })

  const getStartIndex = (scrollLeft = 0) => {
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.right < scrollLeft) {
        start = mid + 1
      } else if (position.left > scrollLeft) {
        end = mid - 1
      } else {
        start = mid
        break
      }
    }

    return Math.max(0, start - overscan)
  }

  const getEndIndex = (scrollLeft = 0) => {
    const viewportRight = scrollLeft + containerWidth
    let start = 0
    let end = positions.value.length - 1

    while (start < end) {
      const mid = Math.floor((start + end) / 2)
      const position = positions.value[mid]

      if (position.left > viewportRight) {
        end = mid - 1
      } else if (position.right < viewportRight) {
        start = mid + 1
      } else {
        end = mid
        break
      }
    }

    return Math.min(positions.value.length - 1, end + overscan)
  }

  const visibleData = computed(() => {
    const start = getStartIndex(scrollLeft.value)
    const end = getEndIndex(scrollLeft.value)
    return data.value.slice(start, end + 1).map((item, i) => ({
      ...item,
      __index: start + i
    }))
  })

  const offset = computed(() => {
    const startIndex = getStartIndex(scrollLeft.value)
    const position = positions.value[startIndex]
    return position ? position.left : 0
  })

  const handleScroll = (e) => {
    scrollLeft.value = e.target.scrollLeft
  }

  watch(data, () => {
    initPositions()
  }, { deep: true, immediate: true })

  return {
    containerRef,
    scrollLeft,
    visibleData,
    totalWidth,
    offset,
    handleScroll,
    positions
  }
}
