<template>
  <teleport to="body">
    <!-- 遮罩层 -->
    <transition name="fade">
      <div v-if="visible" class="ai-reply-overlay" @click="handleClose"></div>
    </transition>

    <transition name="reply-pop">
      <div
        v-if="visible"
        class="ai-reply-panel"
        :style="panelStyle"
        @click.stop
      >
        <!-- 面板头部 -->
        <div class="panel-header">
          <span class="material-icons-outlined header-icon">auto_awesome</span>
          <span class="panel-title">AI 灵动回复</span>
          <div class="header-actions">
            <button class="refresh-btn" @click="handleRefresh" :disabled="loading">
              <span class="material-icons-outlined" :class="{ rotating: loading }">
                refresh
              </span>
            </button>
            <button class="close-btn" @click="handleClose">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>
        </div>

        <!-- 回复建议区域 -->
        <div class="reply-suggestions" v-if="!loading">
          <div class="suggestion-category" v-for="category in categorizedSuggestions" :key="category.name">
            <div class="category-header">
              <span class="material-icons-outlined category-icon">{{ category.icon }}</span>
              <span class="category-name">{{ category.name }}</span>
            </div>
            <div class="suggestion-list">
              <button
                v-for="(suggestion, idx) in category.suggestions"
                :key="idx"
                class="suggestion-item"
                @click="handleSelectReply(suggestion)"
              >
                <span class="suggestion-text">{{ suggestion.text }}</span>
                <span class="suggestion-action">
                  <span class="material-icons-outlined">send</span>
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div class="loading-state" v-else>
          <div class="loading-spinner"></div>
          <span>AI 正在思考...</span>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions" v-if="!loading">
          <button class="quick-btn" @click="handleGenerateMore">
            <span class="material-icons-outlined">add_circle</span>
            <span>生成更多</span>
          </button>
          <button class="quick-btn" @click="handleCustomize">
            <span class="material-icons-outlined">tune</span>
            <span>自定义风格</span>
          </button>
        </div>
      </div>
    </transition>

    <!-- 自定义风格对话框 -->
    <AiStyleDialog
      v-model="showStyleDialog"
      @save="handleStyleSave"
    />
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import AiStyleDialog from './AiStyleDialog.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  triggerMessage: { type: Object, default: null },
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const emit = defineEmits(['select', 'close', 'update:visible'])

const loading = ref(false)
const suggestions = ref([])
const showStyleDialog = ref(false)

// 加载风格配置
const loadStyleConfig = () => {
  const { getAiReplyStyle } = require('@/utils/storage')
  const saved = getAiReplyStyle()
  return Object.keys(saved).length > 0 ? saved : { categories: ['confirm', 'polite', 'work'] }
}

const styleConfig = ref(loadStyleConfig())

// 回复模板分类
const replyTemplates = {
  // 确认类回复
  confirm: [
    { text: '好的，收到', icon: 'check_circle', category: '确认' },
    { text: '没问题', icon: 'check_circle', category: '确认' },
    { text: '明白，我知道了', icon: 'check_circle', category: '确认' },
    { text: '好的，我马上处理', icon: 'check_circle', category: '确认' },
    { text: '了解，稍后回复你', icon: 'schedule', category: '确认' },
    { text: '收到，谢谢', icon: 'favorite', category: '确认' }
  ],

  // 询问类回复
  question: [
    { text: '请问具体是什么情况？', icon: 'help', category: '询问' },
    { text: '能详细说明一下吗？', icon: 'help', category: '询问' },
    { text: '大概什么时候可以完成？', icon: 'schedule', category: '询问' },
    { text: '需要我提供什么帮助？', icon: 'support_agent', category: '询问' },
    { text: '在哪里可以找到相关资料？', icon: 'search', category: '询问' },
    { text: '还有其他需要注意的吗？', icon: 'priority_high', category: '询问' }
  ],

  // 礼貌类回复
  polite: [
    { text: '非常感谢！', icon: 'favorite', category: '感谢' },
    { text: '太感谢了，帮大忙了', icon: 'volunteer_activism', category: '感谢' },
    { text: '辛苦了，感谢！', icon: 'emoji_people', category: '感谢' },
    { text: '不好意思，麻烦你了', icon: 'waving_hand', category: '感谢' },
    { text: '感恩有你！', icon: 'favorite_border', category: '感谢' },
    { text: '十分感谢！', icon: 'stars', category: '感谢' }
  ],

  // 工作类回复
  work: [
    { text: '我已经完成了，请查收', icon: 'task_alt', category: '工作' },
    { text: '正在进行中，预计今天完成', icon: 'progress', category: '工作' },
    { text: '已添加到待办，会尽快处理', icon: 'event_note', category: '工作' },
    { text: '我正在处理这个问题', icon: 'build', category: '工作' },
    { text: '稍后给你发详细资料', icon: 'attach_file', category: '工作' },
    { text: '已安排，请放心', icon: 'verified', category: '工作' }
  ],

  // 轻松类回复
  casual: [
    { text: '哈哈，好的', icon: 'sentiment_very_satisfied', category: '轻松' },
    { text: 'OK ~', icon: 'thumb_up', category: '轻松' },
    { text: '👍', icon: 'thumb_up', category: '轻松' },
    { text: '得嘞~', icon: 'waving_hand', category: '轻松' },
    { text: '来啦来啦~', icon: 'running', category: '轻松' },
    { text: '收到收到~', icon: 'mail', category: '轻松' }
  ],

  // 结束类回复
  closing: [
    { text: '好的，改天聊', icon: 'logout', category: '结束' },
    { text: '先这样，我有事先忙', icon: 'login', category: '结束' },
    { text: '明天继续讨论', icon: 'today', category: '结束' },
    { text: '回头说', icon: 'arrow_back', category: '结束' },
    { text: '先去忙了', icon: 'work', category: '结束' },
    { text: '稍后联系', icon: 'phone_disabled', category: '结束' }
  ]
}

// 分类配置
const categoryConfigs = {
  '确认': { icon: 'check_circle', name: '确认回复' },
  '询问': { icon: 'help_outline', name: '询问详情' },
  '感谢': { icon: 'favorite_border', name: '表示感谢' },
  '工作': { icon: 'work_outline', name: '工作相关' },
  '轻松': { icon: 'sentiment_satisfied', name: '轻松随意' },
  '结束': { icon: 'logout', name: '结束对话' },
  '自定义': { icon: 'edit_note', name: '自定义' }
}

// 根据触发消息内容智能推荐回复
const generateSmartReplies = (message) => {
  if (!message || !message.content) {
    // 没有消息时返回默认混合建议
    return {
      '确认': replyTemplates.confirm.slice(0, 2),
      '工作': replyTemplates.work.slice(0, 2),
      '轻松': replyTemplates.casual.slice(0, 2)
    }
  }

  const content = message.content?.toLowerCase() || ''
  const result = {}

  // 根据消息内容分析场景
  if (content.includes('请') || content.includes('帮忙') || content.includes('协助')) {
    result['确认'] = replyTemplates.confirm.slice(0, 3)
    result['工作'] = replyTemplates.work.slice(0, 2)
  } else if (content.includes('谢') || content.includes('感谢') || content.includes('辛苦')) {
    result['感谢'] = replyTemplates.polite.slice(0, 3)
    result['轻松'] = replyTemplates.casual.slice(0, 2)
  } else if (content.includes('完成') || content.includes('好了') || content.includes('搞定')) {
    result['确认'] = replyTemplates.confirm.slice(2, 4)
    result['轻松'] = replyTemplates.casual.slice(0, 3)
  } else if (content.includes('问') || content.includes('怎么') || content.includes('如何')) {
    result['询问'] = replyTemplates.question.slice(0, 3)
    result['工作'] = replyTemplates.work.slice(2, 4)
  } else if (content.includes('会议') || content.includes('讨论') || content.includes('项目')) {
    result['工作'] = replyTemplates.work.slice(0, 3)
    result['确认'] = replyTemplates.confirm.slice(0, 2)
  } else {
    // 默认混合推荐
    result['确认'] = replyTemplates.confirm.slice(0, 2)
    result['轻松'] = replyTemplates.casual.slice(0, 2)
    result['工作'] = replyTemplates.work.slice(0, 2)
  }

  return result
}

// 分类后的建议
const categorizedSuggestions = computed(() => {
  const replies = generateSmartReplies(props.triggerMessage)

  const result = Object.entries(replies).map(([categoryName, items]) => ({
    name: categoryConfigs[categoryName]?.name || categoryName,
    icon: categoryConfigs[categoryName]?.icon || 'chat_bubble',
    suggestions: items
  }))

  // 添加自定义模板（如果有）
  if (styleConfig.value.customTemplates && styleConfig.value.customTemplates.length > 0) {
    const validTemplates = styleConfig.value.customTemplates.filter(t => t.text && t.text.trim())
    if (validTemplates.length > 0) {
      result.push({
        name: categoryConfigs['自定义'].name,
        icon: categoryConfigs['自定义'].icon,
        suggestions: validTemplates
      })
    }
  }

  return result
})

// 面板位置样式
const panelStyle = computed(() => {
  const x = Math.min(props.position.x, window.innerWidth - 360)
  const y = Math.min(props.position.y, window.innerHeight - 400)
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

// 选择回复
const handleSelectReply = (suggestion) => {
  emit('select', suggestion.text)
  emit('close')
  emit('update:visible', false)
}

// 关闭面板
const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

// 刷新建议
const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 生成更多
const handleGenerateMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 800)
}

// 自定义风格
const handleCustomize = () => {
  showStyleDialog.value = true
}

// 保存风格配置
const handleStyleSave = (config) => {
  styleConfig.value = config
  // 重新生成建议
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 300)
}

// 监听显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 面板打开时生成建议
    loading.value = true
    setTimeout(() => {
      loading.value = false
    }, 300)
  }
})

// ESC 键处理
const handleKeydown = (e) => {
  if (e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

// 添加和移除 ESC 键监听
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 遮罩层
// ============================================================================
.ai-reply-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
  z-index: 9998;
  cursor: default;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ============================================================================
// AI灵动回复面板
// ============================================================================
.ai-reply-panel {
  position: fixed;
  width: 340px;
  max-height: 480px;
  background: var(--dt-bg-card);
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  z-index: 9999;
  animation: replyPanelIn 0.3s var(--dt-ease-out);
  display: flex;
  flex-direction: column;
}

// 面板弹出动画
.reply-pop-enter-active {
  transition: all 0.25s var(--dt-ease-out);
}

.reply-pop-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-10px);
}

@keyframes replyPanelIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 面板头部
.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  background: linear-gradient(135deg, var(--dt-brand-bg) 0%, rgba(0, 137, 255, 0.05) 100%);
}

.header-icon {
  font-size: 18px;
  color: var(--dt-brand-color);
}

.panel-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-brand-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.refresh-btn,
.close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .material-icons-outlined {
    font-size: 18px;
    transition: transform var(--dt-transition-base);
  }

  .rotating {
    animation: rotate 1s linear infinite;
  }
}

.close-btn:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

// 回复建议区域
.reply-suggestions {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 2px;
  }
}

.suggestion-category {
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.category-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  padding: 4px 8px;
}

.category-icon {
  font-size: 14px;
  color: var(--dt-brand-color);
}

.category-name {
  font-size: 11px;
  font-weight: 600;
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-color);
    transform: translateX(4px);

    .suggestion-action {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &:active {
    transform: translateX(2px) scale(0.98);
  }
}

.suggestion-text {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-primary);
  text-align: left;
}

.suggestion-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--dt-brand-color);
  border-radius: 50%;
  opacity: 0;
  transform: translateX(-8px);
  transition: all var(--dt-transition-fast);

  .material-icons-outlined {
    font-size: 12px;
    color: #fff;
  }
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 12px;
  color: var(--dt-text-secondary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--dt-border-color);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
}

.quick-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .ai-reply-panel {
    background: var(--dt-bg-card-dark);
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
  }

  .panel-header {
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.15) 0%, rgba(0, 137, 255, 0.05) 100%);
    border-color: var(--dt-border-dark);
  }

  .suggestion-item {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
      border-color: var(--dt-brand-color);
    }
  }

  .quick-actions {
    background: rgba(255, 255, 255, 0.03);
    border-color: var(--dt-border-dark);
  }

  .quick-btn {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
}
</style>
