<template>
  <teleport to="body">
    <transition name="emoji-pop">
      <div
        v-if="visible"
        class="ai-emoji-panel"
        :style="panelStyle"
        @click.stop
      >
        <!-- 面板头部 -->
        <div class="panel-header">
          <span class="panel-title">AI 推荐表情</span>
          <button class="close-btn" @click="handleClose">
            <span class="material-icons-outlined">close</span>
          </button>
        </div>

        <!-- 快捷表情区域 -->
        <div class="quick-emoji-section">
          <div class="section-title">常用表情</div>
          <div class="emoji-grid">
            <button
              v-for="emoji in quickEmojis"
              :key="emoji.emoji"
              class="emoji-btn"
              :class="{ 'is-recommended': emoji.recommended }"
              @click="handleSelectEmoji(emoji.emoji)"
            >
              <span class="emoji">{{ emoji.emoji }}</span>
              <span v-if="emoji.recommended" class="recommend-badge">
                <span class="material-icons-outlined">auto_awesome</span>
              </span>
              <span class="emoji-label">{{ emoji.label }}</span>
            </button>
          </div>
        </div>

        <!-- 场景表情区域 -->
        <div class="scene-emoji-section">
          <div class="section-title">场景表达</div>
          <div class="scene-tabs">
            <button
              v-for="scene in emojiScenes"
              :key="scene.key"
              class="scene-tab"
              :class="{ active: activeScene === scene.key }"
              @click="activeScene = scene.key"
            >
              <span class="material-icons-outlined">{{ scene.icon }}</span>
              {{ scene.label }}
            </button>
          </div>
          <div class="scene-emoji-grid">
            <button
              v-for="emoji in currentSceneEmojis"
              :key="emoji.emoji"
              class="scene-emoji-btn"
              @click="handleSelectEmoji(emoji.emoji)"
            >
              <span class="emoji">{{ emoji.emoji }}</span>
              <span class="emoji-label">{{ emoji.label }}</span>
            </button>
          </div>
        </div>

        <!-- 消息上下文分析提示 -->
        <div class="context-hint" v-if="contextHint">
          <span class="material-icons-outlined hint-icon">lightbulb</span>
          {{ contextHint }}
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  message: { type: Object, default: null },
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const emit = defineEmits(['select', 'close'])

const activeScene = ref('agree')

// 根据消息内容分析推荐表情
const contextHint = computed(() => {
  if (!props.message) return ''

  const content = props.message.content?.toLowerCase() || ''

  if (content.includes('谢谢') || content.includes('感谢')) {
    return '检测到感谢语气，推荐表示礼貌的表情'
  }
  if (content.includes('好的') || content.includes('可以') || content.includes('没问题')) {
    return '检测到同意语气，推荐表示认可的表情'
  }
  if (content.includes('恭喜') || content.includes('厉害')) {
    return '检测到夸奖语气，推荐表示赞许的表情'
  }
  if (content.includes('哈哈') || content.includes('😄')) {
    return '检测到轻松语气，推荐表示开心的表情'
  }
  if (content.includes('加油') || content.includes('努力')) {
    return '检测到鼓励语气，推荐表示支持的表情'
  }

  return '根据消息内容智能推荐'
})

// 常用表情（带AI推荐标记）
const quickEmojis = [
  { emoji: '👍', label: '赞', recommended: true },
  { emoji: '❤️', label: '喜欢', recommended: true },
  { emoji: '😄', label: '开心', recommended: false },
  { emoji: '🎉', label: '庆祝', recommended: false },
  { emoji: '🤝', label: '握手', recommended: true },
  { emoji: '👏', label: '鼓掌', recommended: false },
  { emoji: '💪', label: '加油', recommended: true },
  { emoji: '🙏', label: '感谢', recommended: false }
]

// 场景表情分类
const emojiScenes = [
  { key: 'agree', label: '认同', icon: 'thumb_up' },
  { key: 'happy', label: '开心', icon: 'sentiment_very_satisfied' },
  { key: 'surprise', label: '惊讶', icon: 'sentiment_shocked' },
  { key: 'thinking', label: '思考', icon: 'psychology' },
  { key: 'love', label: '喜爱', icon: 'favorite' }
]

// 各场景的表情列表
const sceneEmojis = {
  agree: [
    { emoji: '👍', label: '赞同' },
    { emoji: '👌', label: 'OK' },
    { emoji: '✅', label: '确认' },
    { emoji: '💯', label: '满分' },
    { emoji: '🤝', label: '合作愉快' },
    { emoji: '🆗', label: '搞定' }
  ],
  happy: [
    { emoji: '😄', label: '开心' },
    { emoji: '😁', label: '笑嘻嘻' },
    { emoji: '🥰', label: '喜爱' },
    { emoji: '😍', label: '眼冒星' },
    { emoji: '🤩', label: '崇拜' },
    { emoji: '🎉', label: '庆祝' }
  ],
  surprise: [
    { emoji: '😮', label: '惊讶' },
    { emoji: '😲', label: '震惊' },
    { emoji: '🤯', label: '炸裂' },
    { emoji: '😯', label: '无语' },
    { emoji: '🤔', label: '嗯...' },
    { emoji: '🙃', label: '倒脸' }
  ],
  thinking: [
    { emoji: '🤔', label: '思考' },
    { emoji: '💭', label: '想法' },
    { emoji: '🧐', label: '观察' },
    { emoji: '🤨', label: '怀疑' },
    { emoji: '😏', label: '意味深长' },
    { emoji: '🙃', label: '无奈' }
  ],
  love: [
    { emoji: '❤️', label: '爱心' },
    { emoji: '💕', label: '双心' },
    { emoji: '💖', label: '闪亮心' },
    { emoji: '💗', label: ' growing' },
    { emoji: '💓', label: '跳动心' },
    { emoji: '💘', label: '心箭' }
  ]
}

// 当前场景的表情列表
const currentSceneEmojis = computed(() => {
  return sceneEmojis[activeScene.value] || sceneEmojis.agree
})

// 面板位置样式
const panelStyle = computed(() => {
  const x = Math.min(props.position.x, window.innerWidth - 320)
  const y = Math.min(props.position.y, window.innerHeight - 400)
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

// 选择表情
const handleSelectEmoji = (emoji) => {
  emit('select', emoji)
  emit('close')
}

// 关闭面板
const handleClose = () => {
  emit('close')
}

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 面板打开时，根据消息内容智能选择默认场景
    if (props.message) {
      const content = props.message.content?.toLowerCase() || ''
      if (content.includes('谢谢') || content.includes('感谢') || content.includes('好的')) {
        activeScene.value = 'agree'
      } else if (content.includes('哈哈') || content.includes('高兴')) {
        activeScene.value = 'happy'
      } else if (content.includes('什么') || content.includes('真的')) {
        activeScene.value = 'surprise'
      } else if (content.includes('觉得') || content.includes('认为')) {
        activeScene.value = 'thinking'
      } else if (content.includes('喜欢') || content.includes('爱')) {
        activeScene.value = 'love'
      }
    }
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// AI表情面板
// ============================================================================
.ai-emoji-panel {
  position: fixed;
  width: 300px;
  max-height: 420px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: var(--dt-shadow-3xl);
  overflow: hidden;
  z-index: 9999;
  animation: emojiPanelIn 0.3s var(--dt-ease-out);
  display: flex;
  flex-direction: column;
}

// 面板弹出动画
.emoji-pop-enter-active {
  transition: all 0.25s var(--dt-ease-out);
}

.emoji-pop-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-10px);
}

@keyframes emojiPanelIn {
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
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  background: linear-gradient(135deg, var(--dt-brand-bg) 0%, rgba(0, 137, 255, 0.05) 100%);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-brand-color);

  &::before {
    content: 'auto_awesome';
    font-family: 'Material Icons Outlined';
    font-size: 16px;
  }
}

.close-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-quaternary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// 快捷表情区域
.quick-emoji-section {
  padding: 12px;
  border-bottom: 1px solid var(--dt-border-light);
}

.section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--dt-text-quaternary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.emoji-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 6px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-color);
    transform: translateY(-2px);

    .emoji {
      transform: scale(1.2);
    }
  }

  &.is-recommended {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);

    .emoji {
      animation: bounce 0.5s ease-in-out infinite alternate;
    }
  }

  .emoji {
    font-size: 24px;
    line-height: 1;
    transition: transform var(--dt-transition-base);
  }

  .emoji-label {
    font-size: 10px;
    color: var(--dt-text-tertiary);
    text-align: center;
    white-space: nowrap;
  }
}

@keyframes bounce {
  from { transform: scale(1) rotate(-3deg); }
  to { transform: scale(1.1) rotate(3deg); }
}

.recommend-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-radius: var(--dt-radius-full);
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.3);

  .material-icons-outlined {
    font-size: 10px;
    color: #fff;
  }
}

// 场景表情区域
.scene-emoji-section {
  padding: 12px;
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.scene-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.scene-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-full);
  font-size: 11px;
  color: var(--dt-text-secondary);
  white-space: nowrap;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-color);
    color: #fff;
    border-color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 14px;
  }
}

.scene-emoji-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.scene-emoji-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: var(--dt-bg-input);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-color);

    .emoji {
      transform: scale(1.15);
    }
  }

  .emoji {
    font-size: 20px;
    line-height: 1;
    transition: transform var(--dt-transition-fast);
  }

  .emoji-label {
    font-size: 10px;
    color: var(--dt-text-tertiary);
    text-align: center;
  }
}

// 上下文提示
.context-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-top: 8px;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border-radius: var(--dt-radius-md);
  font-size: 11px;
  color: #d46b08;

  .hint-icon {
    font-size: 14px;
    color: #fa8c16;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
:global(.dark) {
  .ai-emoji-panel {
    background: var(--dt-bg-card-dark);
    box-shadow: var(--dt-shadow-3xl);
  }

  .panel-header {
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.15) 0%, rgba(0, 137, 255, 0.05) 100%);
    border-color: var(--dt-border-dark);
  }

  .emoji-btn,
  .scene-emoji-btn {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
      border-color: var(--dt-brand-color);
    }
  }

  .emoji-btn.is-recommended {
    background: rgba(0, 137, 255, 0.2);
    border-color: var(--dt-brand-color);
  }

  .scene-tab {
    background: var(--dt-bg-input-dark);
    border-color: var(--dt-border-dark);

    &.active {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
    }
  }

  .context-hint {
    background: linear-gradient(135deg, rgba(250, 140, 22, 0.2) 0%, rgba(250, 140, 22, 0.1) 100%);
    color: #fdba74;
  }
}
</style>
