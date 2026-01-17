<!--
  来电提醒弹窗组件
  显示来电信息，支持接听/拒绝操作
  @author RuoYi-IM
-->
<template>
  <transition name="modal-fade">
    <div v-if="visible" class="incoming-call-modal" @click="handleBackdropClick">
      <div class="modal-content" @click.stop>
        <!-- 来电动画效果 -->
        <div v-if="showRipple" class="ripple-container">
          <div
            v-for="i in 3"
            :key="i"
            class="ripple"
            :style="{ animationDelay: `${i * 0.5}s` }"
          ></div>
        </div>

        <!-- 来电者信息 -->
        <div class="caller-info">
          <div class="avatar-container">
            <img
              :src="callerAvatar || defaultAvatar"
              class="caller-avatar"
              :class="{ 'is-pulsing': showRipple }"
            />
            <div v-if="callType === 'video'" class="video-indicator">
              <i class="el-icon-video-camera"></i>
            </div>
            <div v-else class="voice-indicator">
              <i class="el-icon-phone"></i>
            </div>
          </div>

          <h2 class="caller-name">{{ callerName || '对方' }}</h2>
          <p class="call-type-text">{{ callType === 'video' ? '视频通话' : '语音通话' }}邀请</p>
          <p v-if="timeout > 0" class="timeout-text">{{ timeout }}秒后自动挂断</p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="action-btn reject" @click="handleReject">
            <i class="el-icon-close"></i>
            <span>拒绝</span>
          </button>
          <button class="action-btn accept" @click="handleAccept">
            <i class="el-icon-check"></i>
            <span>接听</span>
          </button>
        </div>

        <!-- 快捷键提示 -->
        <div class="keyboard-hint">
          <span>空格接听</span>
          <span>ESC拒绝</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

// Props
const props = defineProps({
  /** 是否显示弹窗 */
  visible: {
    type: Boolean,
    default: false,
  },
  /** 来电者头像 */
  callerAvatar: {
    type: String,
    default: '',
  },
  /** 来电者名称 */
  callerName: {
    type: String,
    default: '',
  },
  /** 通话类型 */
  callType: {
    type: String,
    default: 'video', // video | voice
  },
  /** 超时时间（秒） */
  timeoutDuration: {
    type: Number,
    default: 30,
  },
  /** 是否显示波纹动画 */
  showRipple: {
    type: Boolean,
    default: true,
  },
})

// Emits
const emit = defineEmits(['accept', 'reject', 'timeout'])

// 状态
const timeout = ref(props.timeoutDuration)
let timeoutTimer = null

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 处理接听
const handleAccept = () => {
  emit('accept')
}

// 处理拒绝
const handleReject = () => {
  emit('reject')
}

// 处理背景点击（不关闭）
const handleBackdropClick = () => {
  // 点击背景不关闭，必须明确选择接听或拒绝
}

// 处理键盘事件
const handleKeydown = e => {
  if (!props.visible) return

  switch (e.code) {
    case 'Space':
      e.preventDefault()
      handleAccept()
      break
    case 'Escape':
      e.preventDefault()
      handleReject()
      break
  }
}

// 启动倒计时
const startTimeout = () => {
  timeout.value = props.timeoutDuration
  timeoutTimer = setInterval(() => {
    timeout.value--
    if (timeout.value <= 0) {
      stopTimeout()
      emit('timeout')
    }
  }, 1000)
}

// 停止倒计时
const stopTimeout = () => {
  if (timeoutTimer) {
    clearInterval(timeoutTimer)
    timeoutTimer = null
  }
}

// 监听显示状态变化
watch(
  () => props.visible,
  val => {
    if (val) {
      startTimeout()
    } else {
      stopTimeout()
    }
  }
)

// 组件挂载
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  if (props.visible) {
    startTimeout()
  }
})

// 组件卸载
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  stopTimeout()
})
</script>

<style lang="scss" scoped>
.incoming-call-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 32px;
  background: linear-gradient(135deg, #2c3e50 0%, #1a1a1a 100%);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  max-width: 400px;
  width: 100%;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

// 波纹动画
.ripple-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  pointer-events: none;
  z-index: 0;

  .ripple {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    border: 2px solid rgba(82, 196, 26, 0.3);
    border-radius: 50%;
    animation: ripple 2s ease-out infinite;
  }
}

// 来电者信息
.caller-info {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
  text-align: center;

  .avatar-container {
    position: relative;
    margin-bottom: 20px;
  }

  .caller-avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);

    &.is-pulsing {
      animation: pulse 2s ease-in-out infinite;
    }
  }

  .video-indicator,
  .voice-indicator {
    position: absolute;
    bottom: 4px;
    right: 4px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #fff;
  }

  .video-indicator {
    background: linear-gradient(135deg, #1890ff, #096dd9);
  }

  .voice-indicator {
    background: linear-gradient(135deg, #52c41a, #389e0d);
  }

  .caller-name {
    font-size: 24px;
    font-weight: 500;
    color: #fff;
    margin: 0 0 8px 0;
  }

  .call-type-text {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.65);
    margin: 0 0 4px 0;
  }

  .timeout-text {
    font-size: 14px;
    color: #faad14;
    margin: 0;
  }
}

// 操作按钮
.action-buttons {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 40px;

  .action-btn {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    border: none;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    transition: all 0.2s ease;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);

    i {
      font-size: 24px;
      color: #fff;
    }

    span {
      font-size: 12px;
      color: #fff;
    }

    &:hover {
      transform: scale(1.1);
    }

    &:active {
      transform: scale(0.95);
    }

    &.reject {
      background: linear-gradient(135deg, #ff4d4f, #cf1322);

      &:hover {
        background: linear-gradient(135deg, #ff7875, #ff4d4f);
      }
    }

    &.accept {
      background: linear-gradient(135deg, #52c41a, #389e0d);

      &:hover {
        background: linear-gradient(135deg, #73d13d, #52c41a);
      }
    }
  }
}

// 快捷键提示
.keyboard-hint {
  position: relative;
  z-index: 1;
  margin-top: 24px;
  display: flex;
  gap: 24px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);

  span {
    display: flex;
    align-items: center;
    gap: 4px;

    &::before {
      content: '';
      display: inline-block;
      padding: 2px 6px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      font-family: monospace;
    }
  }

  span:first-child::before {
    content: 'Space';
  }

  span:last-child::before {
    content: 'Esc';
  }
}

// 动画
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

@keyframes pulse {
  0%,
  100% {
    box-shadow: 0 8px 30px rgba(82, 196, 26, 0.3);
  }
  50% {
    box-shadow: 0 8px 40px rgba(82, 196, 26, 0.5);
  }
}

// 弹窗过渡动画
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-content {
  transform: translateY(30px);
}

.modal-fade-leave-to .modal-content {
  transform: scale(0.9);
}

// 响应式
@media (max-width: 480px) {
  .modal-content {
    padding: 32px 24px;
    margin: 20px;
  }

  .caller-info .caller-avatar {
    width: 100px;
    height: 100px;
  }

  .action-buttons .action-btn {
    width: 64px;
    height: 64px;

    i {
      font-size: 20px;
    }
  }

  .keyboard-hint {
    display: none;
  }
}
</style>
