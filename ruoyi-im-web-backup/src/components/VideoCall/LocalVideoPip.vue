<!--
  本地视频画中画组件
  支持拖拽、大小切换、位置吸附
  @author RuoYi-IM
-->
<template>
  <div
    ref="pipRef"
    class="local-video-pip"
    :class="[
      `size-${currentSize}`,
      `position-${position}`,
      { 'is-minimized': isMinimized, 'is-dragging': isDragging },
    ]"
    :style="dragStyle"
    data-drag-handle
  >
    <!-- 拖拽手柄区域 -->
    <div
      class="pip-header"
      data-drag-handle
      @mousedown="onMouseDown"
      @touchstart.prevent="onTouchStart"
    >
      <span class="drag-indicator">
        <i class="el-icon-more"></i>
      </span>
    </div>

    <!-- 视频内容 -->
    <div class="pip-content">
      <video
        ref="videoRef"
        class="pip-video"
        autoplay
        playsinline
        muted
        :class="{ mirrored: mirrored }"
      ></video>

      <!-- 占位符（无视频流时） -->
      <div v-if="!stream" class="pip-placeholder">
        <img :src="userAvatar" class="placeholder-avatar" />
      </div>

      <!-- 静音状态指示 -->
      <div v-if="isMuted && stream" class="mute-indicator">
        <i class="el-icon-microphone-off"></i>
      </div>
    </div>

    <!-- 控制按钮 -->
    <div v-if="!isMinimized && showControls" class="pip-controls">
      <button class="pip-btn" title="切换大小" @click="toggleSize">
        <i :class="sizeIcon"></i>
      </button>
      <button class="pip-btn" title="最小化" @click="toggleMinimize">
        <i class="el-icon-minus"></i>
      </button>
    </div>

    <!-- 最小化状态下的展开按钮 -->
    <button v-if="isMinimized" class="pip-expand-btn" @click="toggleMinimize">
      <i class="el-icon-d-arrow-right"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useDraggable } from '@/composables/useDraggable.js'

// Props
const props = defineProps({
  /** 本地媒体流 */
  stream: {
    type: [MediaStream, null],
    default: null,
  },
  /** 用户头像（无视频流时显示） */
  userAvatar: {
    type: String,
    default: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  },
  /** 是否镜像翻转 */
  mirrored: {
    type: Boolean,
    default: true,
  },
  /** 是否静音 */
  isMuted: {
    type: Boolean,
    default: false,
  },
  /** 是否显示控制按钮 */
  showControls: {
    type: Boolean,
    default: true,
  },
  /** 初始大小 */
  initialSize: {
    type: String,
    default: 'large', // large | small
  },
  /** 初始位置 */
  initialPosition: {
    type: String,
    default: 'bottom-right', // top-left | top-right | bottom-left | bottom-right
  },
})

// Emits
const emit = defineEmits(['toggleSize', 'minimize', 'expand'])

// Refs
const pipRef = ref(null)
const videoRef = ref(null)

// 状态
const currentSize = ref(props.initialSize)
const isMinimized = ref(false)
const position = ref(props.initialPosition)

// 使用拖拽功能
const { isDragging, style: dragStyle } = useDraggable({
  target: pipRef,
  snapThreshold: 40,
  padding: 0,
})

// 计算属性
const sizeIcon = computed(() => {
  return currentSize.value === 'large' ? 'el-icon-zoom-out' : 'el-icon-zoom-in'
})

// 切换大小
const toggleSize = () => {
  currentSize.value = currentSize.value === 'large' ? 'small' : 'large'
  emit('toggleSize', currentSize.value)
}

// 切换最小化
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
  emit(isMinimized.value ? 'minimize' : 'expand')
}

// 设置视频流
const setStream = stream => {
  if (videoRef.value) {
    videoRef.value.srcObject = stream || null
  }
}

// 拖拽事件处理
const onMouseDown = e => {
  // 确保点击的是拖拽手柄
  if (e.target.closest('[data-drag-handle]')) {
    // 让 useDraggable 处理
  }
}

const onTouchStart = e => {
  // 触摸开始
}

// 监听流变化
watch(
  () => props.stream,
  newStream => {
    setStream(newStream)
  },
  { immediate: true }
)

// 监听静音状态变化
watch(
  () => props.isMuted,
  muted => {
    if (videoRef.value) {
      videoRef.value.muted = muted
    }
  }
)

// 组件挂载后设置视频
onMounted(() => {
  if (props.stream && videoRef.value) {
    videoRef.value.srcObject = props.stream
  }
})

// 暴露方法给父组件
defineExpose({
  setStream,
  toggleSize,
  toggleMinimize,
  resetPosition: () => {
    // 重置到默认位置
  },
})
</script>

<style lang="scss" scoped>
.local-video-pip {
  position: fixed;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.15);
  transition:
    width 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    height 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.2s ease;

  &.is-dragging {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.6);
    z-index: 100;
  }

  // 大尺寸模式
  &.size-large {
    width: 240px;
    height: 320px;
  }

  // 小尺寸模式
  &.size-small {
    width: 160px;
    height: 213px;
  }

  // 最小化模式
  &.is-minimized {
    width: 48px;
    height: 48px;
    border-radius: 50%;

    .pip-header,
    .pip-controls {
      display: none;
    }

    .pip-content {
      border-radius: 50%;
    }

    .pip-video,
    .pip-placeholder {
      border-radius: 50%;
      object-fit: cover;
    }
  }

  // 拖拽时改变光标
  &[data-drag-handle] {
    cursor: grab;

    &:active {
      cursor: grabbing;
    }
  }
}

// 拖拽手柄
.pip-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent);
  cursor: grab;
  z-index: 10;

  &:active {
    cursor: grabbing;
  }

  .drag-indicator {
    color: rgba(255, 255, 255, 0.6);
    font-size: 14px;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &:hover .drag-indicator {
    opacity: 1;
  }
}

.local-video-pip:hover .pip-header .drag-indicator {
  opacity: 1;
}

// 视频内容区
.pip-content {
  position: relative;
  width: 100%;
  height: 100%;
  background: #1a1a1a;
}

.pip-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;

  &.mirrored {
    transform: scaleX(-1);
  }
}

// 占位符
.pip-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2c3e50, #1a1a1a);

  .placeholder-avatar {
    width: 60%;
    height: 60%;
    border-radius: 50%;
    object-fit: cover;
    opacity: 0.8;
  }
}

// 静音指示
.mute-indicator {
  position: absolute;
  bottom: 8px;
  left: 8px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}

// 控制按钮
.pip-controls {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.local-video-pip:hover .pip-controls {
  opacity: 1;
}

.pip-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
  }
}

// 展开按钮（最小化时显示）
.pip-expand-btn {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.8);
  }
}

// 响应式
@media (max-width: 768px) {
  .local-video-pip {
    &.size-large {
      width: 120px;
      height: 160px;
    }

    &.size-small {
      width: 80px;
      height: 107px;
    }

    &.is-minimized {
      width: 40px;
      height: 40px;
    }
  }

  .pip-controls,
  .pip-expand-btn {
    opacity: 1; // 移动端始终显示
  }
}
</style>
