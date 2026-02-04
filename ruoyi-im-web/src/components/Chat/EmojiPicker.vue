<template>
  <div
    class="emoji-picker"
    :style="positionStyle"
    ref="pickerRef"
    v-click-outside="close"
  >
    <!-- 分类标签 -->
    <div class="emoji-tabs">
      <button
        v-for="category in categories"
        :key="category.id"
        class="emoji-tab"
        :class="{ active: activeCategory === category.id }"
        @click="activeCategory = category.id"
      >
        <span class="tab-icon">{{ category.icon }}</span>
      </button>
    </div>

    <!-- 表情列表 -->
    <div class="emoji-list">
      <button
        v-for="emoji in currentEmojis"
        :key="emoji"
        class="emoji-item"
        @click="selectEmoji(emoji)"
        :title="emoji"
      >
        {{ emoji }}
      </button>
    </div>

    <!-- 最近使用 -->
    <div v-if="recentEmojis.length > 0" class="emoji-section">
      <div class="section-title">最近使用</div>
      <div class="emoji-list">
        <button
          v-for="emoji in recentEmojis"
          :key="emoji"
          class="emoji-item"
          @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { addRecentEmoji, getRecentEmoji } from '@/utils/storage'
import { ref, computed, onMounted, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  // 可以传入位置信息
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const emit = defineEmits(['select', 'close'])

const activeCategory = ref('recent')
const pickerRef = ref(null)
const STORAGE_KEY = 'im-recent-emojis'

// 分类数据
const categories = [
  { id: 'recent', icon: '🕐' },
  { id: 'smile', icon: '😊' },
  { id: 'hand', icon: '👋' },
  { id: 'heart', icon: '❤️' },
  { id: 'face', icon: '😎' },
  { id: 'food', icon: '🍔' },
  { id: 'animal', icon: '🐱' },
  { id: 'activity', icon: '⚽' }
]

// 表情数据
const emojiData = {
  recent: [],
  smile: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😔', '😪', '🤤', '😴', '😷', '🤒'],
  hand: ['👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤏', '✌️', '🤞', '🤝', '👊', '👏', '👆', '👇', '👈', '👉', '🤙', '💪', '🙏', '✍️', '💅', '🤳', '💪'],
  heart: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝'],
  face: ['😎', '🤓', '🧐', '🤠', '🥳', '🥸', '😈', '👿', '👹', '👺', '💀', '💩', '🤡', '👻', '👽', '🤖', '🎃', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '👶'],
  food: ['🍎', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🥭', '🍍�', '🍐', '🥝', '🍅', '🫒', '🥥', '🥑', '🍆', '🥔', '🥕', '🌽', '🥬', '🥦', '🧄', '🧅', '🍄', '🥜', '🌰', '🍞', '🥐', '🥖', '🥨', '🧀', '🥚', '🍳', '🧈', '🥞', '🧇', '🥓', '🥩', '🍗', '🍖', '🌭', '🍔', '🍟', '🍕', '🫓', '🥪', '🌮', '🌯', '🥙', '🧆', '🥚', '🍜', '🍝', '🍠', '🍲', '🍛', '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍘', '🥘'],
  animal: ['🐱', '🐶', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐸', '🐽', '🐵', '🙀', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🐞', '🦋', '🐌', '🐙', '🦎', '🦐', '🦑', '🦀'],
  activity: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🥊', '🥋', '🏏', '🏑', '🥍', '🏏�', '🥅', '⛳', '🪁', '🏹', '🎣', '🤿', '🎽', '🎿', '🛹', '🛼', '🛹', '🚴', '🚵', '🏇', '🏂', '🏋', '🏌', '🏇', '🏆', '🥇', '🥈', '🥉', '🏅', '🎖️', '🏵️', '🎗️', '🎟️', '🎫']
}

// 最近使用的表情
const recentEmojis = ref([])

// 加载最近使用的表情
const loadRecentEmojis = () => {
  recentEmojis.value = getRecentEmoji()
}

// 保存表情到最近使用
const saveRecentEmoji = (emoji) => {
  addRecentEmoji(emoji, 20)
  recentEmojis.value = getRecentEmoji()
}

// 当前分类的表情
const currentEmojis = computed(() => {
  if (activeCategory.value === 'recent') {
    return recentEmojis.value.length > 0 ? recentEmojis.value : emojiData.smile
  }
  return emojiData[activeCategory.value] || []
})

// 位置样式
const positionStyle = computed(() => {
  return {
    left: props.position.x + 'px',
    top: props.position.y + 'px'
  }
})

// 选择表情
const selectEmoji = (emoji) => {
  saveRecentEmoji(emoji)
  emit('select', emoji)
  close()
}

// 关闭
const close = () => {
  emit('close')
}

// 点击外部关闭
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}

// 初始化
onMounted(() => {
  loadRecentEmojis()
})

// 监听最近使用变化
watch(recentEmojis, (newVal) => {
  if (activeCategory.value === 'recent' && newVal.length === 0) {
    activeCategory.value = 'smile'
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.emoji-picker {
  position: fixed;
  width: 320px;
  max-height: 280px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-dialog);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
    box-shadow: var(--dt-shadow-3xl);
  }
}

.emoji-tabs {
  display: flex;
  padding: 8px;
  border-bottom: 1px solid var(--dt-border-light);
  gap: 4px;
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 0;
  }

  .dark & {
    border-color: var(--dt-border-dark);
  }
}

.emoji-tab {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.2s;

  .tab-icon {
    font-size: 16px;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .dark & {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
}

.emoji-section {
  .section-title {
    padding: 8px 12px 4px;
    font-size: 11px;
    color: var(--dt-text-tertiary);
    font-weight: 500;
  }
}

.emoji-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);  // 钉钉标准：8列网格
  padding: 8px;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: var(--dt-bg-hover);
    transform: scale(1.15);
  }

  &:active {
    transform: scale(1.05);
  }

  .dark &:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
