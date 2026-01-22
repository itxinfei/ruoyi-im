<template>
  <div class="loading-container">
    <!-- 全屏加载 -->
    <div
      v-if="fullscreen"
      class="fullscreen-loading"
      :class="{ 'with-background': showBackground }"
    >
      <div class="loading-content">
        <!-- 加载动画 -->
        <div class="loading-animation" :class="animationClass">
          <template v-if="type === 'spinner'">
            <el-icon class="is-loading"><Loading /></el-icon>
          </template>
          <template v-else-if="type === 'dots'">
            <div class="dots-loading">
              <div class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${(i - 1) * 0.16}s` }"></div>
            </div>
          </template>
          <template v-else-if="type === 'pulse'">
            <div class="pulse-loading">
              <div class="pulse-circle"></div>
            </div>
          </template>
          <template v-else-if="type === 'wave'">
            <div class="wave-loading">
              <div class="wave-bar" v-for="i in 5" :key="i" :style="{ animationDelay: `${(i - 1) * 0.1}s` }"></div>
            </div>
          </template>
          <template v-else-if="type === 'custom' && customAnimation">
            <component :is="customAnimation" />
          </template>
          <template v-else>
            <el-icon class="is-loading"><Loading /></el-icon>
          </template>
        </div>

        <!-- 加载文本 -->
        <div v-if="text" class="loading-text">
          {{ text }}
        </div>

        <!-- 进度条 -->
        <div v-if="showProgress && progress >= 0" class="loading-progress">
          <el-progress
            :percentage="progress"
            :status="progressStatus"
            :stroke-width="4"
            :show-text="false"
          />
          <div class="progress-text">
            {{ progress }}%
          </div>
        </div>

        <!-- 加载提示 -->
        <div v-if="tip" class="loading-tip">
          {{ tip }}
        </div>

        <!-- 取消按钮 -->
        <el-button
          v-if="cancellable"
          type="text"
          size="small"
          @click="handleCancel"
          class="cancel-button"
        >
          取消
        </el-button>
      </div>
    </div>

    <!-- 局部加载 -->
    <div
      v-else
      class="local-loading"
      :class="[sizeClass, { 'inline': inline }]"
      v-bind="$attrs"
    >
      <template v-if="inline">
        <!-- 内联模式 -->
        <el-icon class="is-loading inline-loading"><Loading /></el-icon>
        <span v-if="text" class="inline-text">{{ text }}</span>
      </template>

      <template v-else>
        <!-- 卡片模式 -->
        <div class="local-loading-content">
          <div class="loading-animation" :class="animationClass">
            <template v-if="type === 'spinner'">
              <el-icon class="is-loading"><Loading /></el-icon>
            </template>
            <template v-else-if="type === 'dots'">
              <div class="dots-loading">
                <div class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${(i - 1) * 0.16}s` }"></div>
              </div>
            </template>
            <template v-else-if="type === 'pulse'">
              <div class="pulse-loading">
                <div class="pulse-circle"></div>
              </div>
            </template>
            <template v-else>
              <el-icon class="is-loading"><Loading /></el-icon>
            </template>
          </div>

          <div v-if="text" class="loading-text">{{ text }}</div>
        </div>
      </template>
    </div>

    <!-- 骨架屏加载 -->
    <div v-else-if="skeleton" class="skeleton-loading">
      <slot name="skeleton">
        <!-- 默认骨架屏 -->
        <el-skeleton :rows="skeletonRows" :animated="true" :loading="true" />
      </slot>
    </div>

    <!-- 空状态加载 -->
    <div v-else-if="empty" class="empty-loading">
      <slot name="empty">
        <el-empty :description="text || '暂无数据'">
          <template #image>
            <el-icon class="empty-icon"><Loading /></el-icon>
          </template>
        </el-empty>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'

// Props 定义
const props = defineProps({
  // 加载类型: fullscreen, local, skeleton, empty, inline
  mode: {
    type: String,
    default: 'local',
    validator: (value) => ['fullscreen', 'local', 'skeleton', 'empty', 'inline'].includes(value)
  },
  
  // 加载状态
  loading: {
    type: Boolean,
    default: true
  },
  
  // 加载文本
  text: {
    type: String,
    default: ''
  },
  
  // 加载提示
  tip: {
    type: String,
    default: ''
  },
  
  // 动画类型: spinner, dots, pulse, wave, custom
  type: {
    type: String,
    default: 'spinner'
  },
  
  // 自定义动画组件
  customAnimation: {
    type: [Object, Function],
    default: null
  },
  
  // 尺寸: large, medium, small, mini
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['large', 'medium', 'small', 'mini'].includes(value)
  },
  
  // 全屏模式是否显示背景
  showBackground: {
    type: Boolean,
    default: true
  },
  
  // 内联模式
  inline: {
    type: Boolean,
    default: false
  },
  
  // 骨架屏行数
  skeletonRows: {
    type: Number,
    default: 5
  },
  
  // 是否可取消
  cancellable: {
    type: Boolean,
    default: false
  },
  
  // 进度条
  showProgress: {
    type: Boolean,
    default: false
  },
  
  // 进度值 (0-100)
  progress: {
    type: Number,
    default: 0
  },
  
  // 进度状态: success, exception, warning
  progressStatus: {
    type: String,
    default: ''
  }
})

// Emits 定义
const emit = defineEmits(['cancel'])

// 计算属性
const fullscreen = computed(() => props.mode === 'fullscreen')
const skeleton = computed(() => props.mode === 'skeleton')
const empty = computed(() => props.mode === 'empty')

const sizeClass = computed(() => {
  return `loading-${props.size}`
})

const animationClass = computed(() => {
  return `animation-${props.type}`
})

// 方法定义
const handleCancel = () => {
  emit('cancel')
}
</script>

<script>
export default {
  name: 'LoadingState'
}
</script>

<style scoped>
.loading-container {
  position: relative;
}

/* 全屏加载 */
.fullscreen-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.fullscreen-loading.with-background {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(2px);
}

.loading-content {
  text-align: center;
  max-width: 300px;
}

.loading-animation {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.loading-text {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 500;
}

.loading-tip {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  line-height: 1.5;
}

.loading-progress {
  margin-bottom: 16px;
  width: 200px;
  margin: 0 auto 16px auto;
}

.progress-text {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.cancel-button {
  margin-top: 16px;
}

/* 局部加载 */
.local-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.local-loading.inline {
  display: inline-flex;
  padding: 0;
  vertical-align: middle;
}

.local-loading-content {
  text-align: center;
}

.local-loading .loading-text {
  font-size: 14px;
  color: #606266;
}

.local-loading .loading-animation {
  margin-bottom: 8px;
}

.local-loading.inline .loading-text {
  margin-left: 8px;
  margin-bottom: 0;
}

/* 尺寸样式 */
.loading-large {
  padding: 60px 20px;
}

.loading-large .loading-animation {
  transform: scale(1.2);
}

.loading-large .loading-text {
  font-size: 18px;
}

.loading-small {
  padding: 20px;
}

.loading-small .loading-animation {
  transform: scale(0.8);
}

.loading-small .loading-text {
  font-size: 12px;
}

.loading-mini {
  padding: 12px;
}

.loading-mini .loading-animation {
  transform: scale(0.6);
}

.loading-mini .loading-text {
  font-size: 11px;
}

/* 内联加载 */
.inline-loading {
  vertical-align: middle;
}

.inline-text {
  vertical-align: middle;
  font-size: 14px;
  color: #606266;
}

/* 动画样式 */
.animation-spinner .el-icon {
  font-size: 40px;
  color: #409eff;
}

.animation-dots .dots-loading {
  display: flex;
  gap: 4px;
  align-items: center;
}

.animation-dots .dot {
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border-radius: 50%;
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.animation-pulse .pulse-loading {
  position: relative;
  width: 40px;
  height: 40px;
}

.animation-pulse .pulse-circle {
  width: 100%;
  height: 100%;
  background-color: #409eff;
  border-radius: 50%;
  animation: pulse-scale 1.5s infinite ease-in-out;
}

@keyframes pulse-scale {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

.animation-wave .wave-loading {
  display: flex;
  gap: 3px;
  align-items: flex-end;
  height: 32px;
}

.animation-wave .wave-bar {
  width: 4px;
  height: 100%;
  background-color: #409eff;
  border-radius: 2px;
  animation: wave-stretch 1.2s infinite ease-in-out;
}

@keyframes wave-stretch {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

/* 骨架屏 */
.skeleton-loading {
  padding: 20px;
}

/* 空状态 */
.empty-loading {
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  animation: empty-spin 2s linear infinite;
}

@keyframes empty-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-content {
    padding: 0 20px;
  }
  
  .loading-progress {
    width: 100%;
    max-width: 280px;
  }
  
  .loading-text {
    font-size: 14px;
  }
  
  .loading-tip {
    font-size: 13px;
  }
  
  .animation-spinner .el-icon {
    font-size: 32px;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .fullscreen-loading.with-background {
    background-color: rgba(0, 0, 0, 0.8);
  }
  
  .loading-text {
    color: #e4e7ed;
  }
  
  .loading-tip {
    color: #909399;
  }
  
  .local-loading .loading-text {
    color: #c0c4cc;
  }
  
  .inline-text {
    color: #c0c4cc;
  }
}

/* 减少动画（用户偏好） */
@media (prefers-reduced-motion: reduce) {
  .animation-dots .dot,
  .animation-pulse .pulse-circle,
  .animation-wave .wave-bar,
  .empty-icon {
    animation: none;
  }
  
  .el-icon.is-loading {
    animation: none;
  }
}

/* 加载动画延迟 */
.loading-animation {
  animation-delay: 0.2s;
}

.loading-animation * {
  animation-delay: inherit;
}
</style>