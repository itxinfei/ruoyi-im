<template>
  <div class="voice-to-text-container">
    <!-- 转写按钮 -->
    <button
      v-if="!isTranscribing && !transcript"
      class="transcribe-btn"
      @click="handleStartTranscribe"
      :title="`转成文字 (${duration}秒)`"
    >
      <span class="material-icons-outlined">text_format</span>
      <span class="btn-text">转文字</span>
    </button>

    <!-- 转写中状态 -->
    <div v-else-if="isTranscribing" class="transcribing-state">
      <div class="wave-animation">
        <span class="wave-bar"></span>
        <span class="wave-bar"></span>
        <span class="wave-bar"></span>
        <span class="wave-bar"></span>
      </div>
      <span class="state-text">正在转写...</span>
      <span class="progress-text">{{ progress }}%</span>
    </div>

    <!-- 转写结果 -->
    <div v-else-if="transcript" class="transcript-result">
      <div class="transcript-header">
        <span class="material-icons-outlined icon">check_circle</span>
        <span class="header-text">转写结果</span>
        <button class="collapse-btn" @click="isExpanded = !isExpanded">
          <span class="material-icons-outlined">
            {{ isExpanded ? 'expand_less' : 'expand_more' }}
          </span>
        </button>
      </div>
      <transition name="expand">
        <div v-show="isExpanded" class="transcript-content">
          {{ transcript }}
        </div>
      </transition>
      <div class="transcript-actions">
        <button class="action-btn" @click="handleCopy" title="复制">
          <span class="material-icons-outlined">content_copy</span>
        </button>
        <button class="action-btn" @click="handleReply" title="引用回复">
          <span class="material-icons-outlined">reply</span>
        </button>
        <button class="action-btn danger" @click="handleDelete" title="删除">
          <span class="material-icons-outlined">delete</span>
        </button>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <span class="material-icons-outlined error-icon">error_outline</span>
      <span class="error-text">{{ error }}</span>
      <button class="retry-btn" @click="handleStartTranscribe">重试</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { copyToClipboard } from '@/utils/format'

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

// 开始转写
const handleStartTranscribe = async () => {
  if (!props.voiceUrl) {
    error.value = '语音文件不存在'
    return
  }

  isTranscribing.value = true
  error.value = ''
  progress.value = 0

  // 模拟转写进度
  const progressInterval = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 15
    }
  }, 300)

  try {
    // 调用转写接口
    const result = await callTranscribeAPI()

    clearInterval(progressInterval)
    progress.value = 100

    // 模拟转写延迟
    await new Promise(resolve => setTimeout(resolve, 500))

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
    clearInterval(progressInterval)
    isTranscribing.value = false
    error.value = err.message || '转写失败，请重试'
  }
}

// 调用转写 API
const callTranscribeAPI = async () => {
  // 这里应该调用真实的后端 API
  // 目前返回模拟数据

  // 模拟网络请求延迟
  await new Promise(resolve => setTimeout(resolve, 2000))

  // 根据语言返回不同的模拟结果
  const mockResults = {
    'zh-CN': {
      text: '这是一条模拟的语音转文字结果。在实际使用中，这里会显示真实的语音识别内容。支持中文、英文等多种语言的语音识别。',
      confidence: 0.95
    },
    'en-US': {
      text: 'This is a simulated voice-to-text result. In actual use, this will display real voice recognition content. Supports multiple languages.',
      confidence: 0.92
    }
  }

  const lang = props.language.startsWith('zh') ? 'zh-CN' : 'en-US'
  return mockResults[lang] || mockResults['zh-CN']
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
  border-radius: 2px;
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
