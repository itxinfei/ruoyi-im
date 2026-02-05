<template>
  <div class="voice-to-text-container">
    <!-- 转写按钮 -->
    <button
      v-if="!isTranscribing && !transcript"
      class="transcribe-btn"
      :title="`转成文字 (${duration}秒)`"
      @click="handleStartTranscribe"
    >
      <span class="material-icons-outlined">text_format</span>
      <span class="btn-text">转文字</span>
    </button>

    <!-- 转写中状态 -->
    <div
      v-else-if="isTranscribing"
      class="transcribing-state"
    >
      <div class="wave-animation">
        <span class="wave-bar" />
        <span class="wave-bar" />
        <span class="wave-bar" />
        <span class="wave-bar" />
      </div>
      <span class="state-text">正在转写...</span>
      <span class="progress-text">{{ progress }}%</span>
    </div>

    <!-- 转写结果 -->
    <div
      v-else-if="transcript"
      class="transcript-result"
    >
      <div class="transcript-header">
        <span class="material-icons-outlined icon">check_circle</span>
        <span class="header-text">转写结果</span>
        <button
          class="collapse-btn"
          @click="isExpanded = !isExpanded"
        >
          <span class="material-icons-outlined">
            {{ isExpanded ? 'expand_less' : 'expand_more' }}
          </span>
        </button>
      </div>
      <transition name="expand">
        <div
          v-show="isExpanded"
          class="transcript-content"
        >
          {{ transcript }}
        </div>
      </transition>
      <div class="transcript-actions">
        <button
          class="action-btn"
          title="复制"
          @click="handleCopy"
        >
          <span class="material-icons-outlined">content_copy</span>
        </button>
        <button
          class="action-btn"
          title="引用回复"
          @click="handleReply"
        >
          <span class="material-icons-outlined">reply</span>
        </button>
        <button
          class="action-btn danger"
          title="删除"
          @click="handleDelete"
        >
          <span class="material-icons-outlined">delete</span>
        </button>
      </div>
    </div>

    <!-- 错误状态 -->
    <div
      v-else-if="error"
      class="error-state"
    >
      <span class="material-icons-outlined error-icon">error_outline</span>
      <span class="error-text">{{ error }}</span>
      <button
        class="retry-btn"
        @click="handleStartTranscribe"
      >
        重试
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { copyToClipboard } from '@/utils/format'
import { createTranscript, getTranscript } from '@/api/im/transcript'

/**
 * 转写错误类 - 保留完整错误信息用于调试
 */
class TranscriptError extends Error {
  constructor(message, code, originalResponse) {
    super(message)
    this.name = 'TranscriptError'
    this.code = code
    this.originalResponse = originalResponse
  }
}

const props = defineProps({
  messageId: {
    type: [String, Number],
    required: true
  },
  voiceUrl: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 0
  },
  language: {
    type: String,
    default: 'zh-CN'
  },
  existingTranscript: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['transcribe', 'copy', 'reply', 'delete'])

const isTranscribing = ref(false)
const progress = ref(0)
const transcript = ref(props.existingTranscript)
const error = ref('')
const isExpanded = ref(true)

// 中断控制器 - 用于组件卸载时停止轮询
const abortController = ref(null)

// 开始转写
const handleStartTranscribe = async () => {
  if (!props.voiceUrl) {
    error.value = '语音文件不存在'
    return
  }

  // 创建新的中断控制器
  abortController.value = new AbortController()

  isTranscribing.value = true
  error.value = ''
  progress.value = 0

  try {
    // 调用转写接口
    const result = await callTranscribeAPI(abortController.value.signal)

    progress.value = 100

    // 模拟转写延迟用于平滑过渡
    await new Promise(resolve => setTimeout(resolve, 300))

    transcript.value = result.text
    isTranscribing.value = false

    // 通知父组件转写完成
    emit('transcribe', {
      messageId: props.messageId,
      text: result.text,
      language: props.language
    })

    ElMessage.success('语音转文字完成')
  } catch (err) {
    isTranscribing.value = false
    if (err.name === 'AbortError') {
      // 用户主动取消，不显示错误
      console.log('[VoiceToText] 转写已取消')
      return
    }
    error.value = err.message || '转写失败，请重试'
    console.error('[VoiceToText] 转写失败:', err)
  }
}

// 调用转写 API
const callTranscribeAPI = async signal => {
  // 1. 创建转写任务
  const createResponse = await createTranscript({
    messageId: props.messageId,
    voiceUrl: props.voiceUrl,
    duration: props.duration,
    language: props.language
  })

  if (createResponse.code !== 200) {
    throw new TranscriptError(
      createResponse.msg || '创建转写任务失败',
      createResponse.code,
      createResponse
    )
  }

  // 2. 轮询获取转写结果（最多等待30秒）
  const maxPolls = 30
  const pollInterval = 1000

  for (let i = 0; i < maxPolls; i++) {
    // 检查是否被中断
    if (signal?.aborted) {
      throw new TranscriptError('转写已取消', 'ABORTED', null)
    }

    // 更新进度（仅使用轮询进度，移除模拟进度定时器）
    progress.value = Math.min(90, Math.floor((i / maxPolls) * 90))

    const resultResponse = await getTranscript(props.messageId)

    // 验证响应
    if (!resultResponse || resultResponse.code === undefined) {
      console.error('[VoiceToText] API 响应异常:', resultResponse)
      throw new TranscriptError('API 响应异常', 'INVALID_RESPONSE', resultResponse)
    }

    if (resultResponse.code !== 200) {
      throw new TranscriptError(
        resultResponse.msg || '获取转写结果失败',
        resultResponse.code,
        resultResponse
      )
    }

    // 验证数据
    if (!resultResponse.data) {
      console.error('[VoiceToText] API 返回空数据:', resultResponse)
      throw new TranscriptError('转写数据异常', 'EMPTY_DATA', resultResponse)
    }

    const { status, content, errorMessage, confidence } = resultResponse.data

    // 使用 switch-case 处理各种状态
    switch (status) {
      case 'completed':
        if (!content) {
          throw new TranscriptError('转写完成但内容为空', 'EMPTY_CONTENT', resultResponse.data)
        }
        return { text: content, confidence: confidence || 1 }

      case 'failed':
        throw new TranscriptError(
          errorMessage || '转写失败',
          'TRANSCRIPT_FAILED',
          resultResponse.data
        )

      case 'processing':
        // 继续等待
        await new Promise(resolve => setTimeout(resolve, pollInterval))
        break

      case 'pending':
      case 'queued':
        // 任务在队列中，继续等待
        await new Promise(resolve => setTimeout(resolve, pollInterval))
        break

      default:
        console.warn('[VoiceToText] 未知转写状态:', status, '继续等待...')
        await new Promise(resolve => setTimeout(resolve, pollInterval))
    }
  }

  throw new TranscriptError('转写超时，请稍后重试', 'TIMEOUT', null)
}

// 复制转写结果
const handleCopy = () => {
  copyToClipboard(transcript.value).then(() => {
    emit('copy', transcript.value)
  })
}

// 引用回复
const handleReply = () => {
  emit('reply', transcript.value)
}

// 删除转写结果
const handleDelete = () => {
  transcript.value = ''
  emit('delete', props.messageId)
}

// 组件卸载时清理
onUnmounted(() => {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
})

// 暴露方法
defineExpose({
  startTranscribe: handleStartTranscribe,
  clearTranscript: () => { transcript.value = '' }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.voice-to-text-container {
  margin-top: 8px;
}

// 转写按钮
.transcribe-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  .material-icons-outlined {
    font-size: 14px;
  }

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);

    .btn-text {
      color: var(--dt-brand-color);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// 转写中状态
.transcribing-state {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-md);
}

.wave-animation {
  display: flex;
  align-items: center;
  gap: 3px;
  width: 24px;
  height: 16px;
}

.wave-bar {
  width: 3px;
  background: var(--dt-brand-color);
  border-radius: var(--dt-radius-sm);
  animation: wave 1s ease-in-out infinite;

  &:nth-child(1) { animation-delay: 0s; height: 8px; }
  &:nth-child(2) { animation-delay: 0.1s; height: 12px; }
  &:nth-child(3) { animation-delay: 0.2s; height: 16px; }
  &:nth-child(4) { animation-delay: 0.3s; height: 10px; }
}

@keyframes wave {
  0%, 100% {
    transform: scaleY(0.5);
    opacity: 0.5;
  }
  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}

.state-text {
  flex: 1;
  font-size: 12px;
  color: var(--dt-brand-color);
}

.progress-text {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  font-variant-numeric: tabular-nums;
}

// 转写结果
.transcript-result {
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.transcript-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--dt-success-bg);
  border-bottom: 1px solid var(--dt-border-light);

  .icon {
    font-size: 16px;
    color: var(--dt-success-color);
  }

  .header-text {
    flex: 1;
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-success-color);
  }
}

.collapse-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-success-color);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: rgba(82, 196, 26, 0.1);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
}

.transcript-content {
  padding: 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--dt-text-primary);
  word-break: break-word;
}

.transcript-actions {
  display: flex;
  gap: 4px;
  padding: 6px 12px;
  background: var(--dt-bg-body);
  border-top: 1px solid var(--dt-border-light);
}

.action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-tertiary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
  }

  &.danger:hover {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// 错误状态
.error-state {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--dt-error-bg);
  border-radius: var(--dt-radius-md);
}

.error-icon {
  font-size: 18px;
  color: var(--dt-error-color);
}

.error-text {
  flex: 1;
  font-size: 12px;
  color: var(--dt-error-color);
}

.retry-btn {
  padding: 4px 10px;
  background: var(--dt-error-color);
  border: none;
  border-radius: var(--dt-radius-sm);
  font-size: 11px;
  color: #fff;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    opacity: 0.9;
  }

  &:active {
    transform: scale(0.98);
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .transcribe-btn {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: rgba(0, 137, 255, 0.15);
    }
  }

  .transcript-result {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);
  }

  .transcript-header {
    background: rgba(82, 196, 26, 0.15);
    border-color: var(--dt-border-dark);
  }

  .transcript-content {
    color: var(--dt-text-primary-dark);
  }

  .transcript-actions {
    background: var(--dt-bg-body-dark);
    border-color: var(--dt-border-dark);
  }

  .error-state {
    background: rgba(239, 68, 68, 0.15);
  }
}
</style>
