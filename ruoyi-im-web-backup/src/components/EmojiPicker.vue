<template>
  <div class="emoji-picker" v-if="visible">
    <div class="emoji-header">
      <div class="emoji-tabs">
        <div 
          v-for="category in categories" 
          :key="category.key"
          class="emoji-tab"
          :class="{ active: activeCategory === category.key }"
          @click="setActiveCategory(category.key)"
        >
          {{ category.emoji }}
        </div>
      </div>
    </div>
    
    <div class="emoji-content">
      <div class="emoji-grid">
        <div 
          v-for="emoji in currentEmojis" 
          :key="emoji"
          class="emoji-item"
          @click="selectEmoji(emoji)"
          @mouseenter="hoverEmoji = emoji"
          @mouseleave="hoverEmoji = null"
        >
          {{ emoji }}
        </div>
      </div>
    </div>
    
    <div class="emoji-footer">
      <div class="recent-emojis" v-if="recentEmojis.length > 0">
        <div class="recent-label">最近使用</div>
        <div class="recent-emoji-list">
          <div 
            v-for="emoji in recentEmojis" 
            :key="emoji"
            class="recent-emoji"
            @click="selectEmoji(emoji)"
          >
            {{ emoji }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'close'])

const activeCategory = ref('smile')
const hoverEmoji = ref(null)
const recentEmojis = ref([])

const categories = [
  { key: 'smile', emoji: '😊', name: '笑脸' },
  { key: 'gesture', emoji: '👍', name: '手势' },
  { key: 'animal', emoji: '🐱', name: '动物' },
  { key: 'food', emoji: '🍎', name: '食物' },
  { key: 'activity', emoji: '⚽', name: '活动' },
  { key: 'travel', emoji: '🚗', name: '旅行' },
  { key: 'object', emoji: '💡', name: '物品' },
  { key: 'symbol', emoji: '❤️', name: '符号' },
  { key: 'flag', emoji: '🏳️', name: '旗帜' }
]

const emojiData = {
  smile: ['😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐'],
  gesture: ['👋', '🤚', '🖐', '✋', '🖖', '👌', '🤌', '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '👇', '☝️', '👍', '👎', '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🙏', '🤝', '💪', '🦾', '🦵', '🦿', '💄', '💋', '👄', '🦷', '👅', '👂', '🦻', '👃', '👶', '👦', '👧', '👩', '👱', '👨', '👩', '👴', '👵'],
  animal: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐽', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦟', '🦗', '🕷', '🕸', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈'],
  food: ['🍎', '🍏', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝', '🍅', '🍆', '🥑', '🥦', '🥬', '🥒', '🌶', '🌽', '🥕', '🥔', '🍠', '🥐', '🍞', '🥖', '🥨', '🧀', '🥚', '🍳', '🥞', '🥓', '🥩', '🍗', '🍖', '🌭', '🍔', '🍟', '🍕', '🥪', '🥙', '🌮', '🌯', '🥗', '🥘', '🥫', '🍝', '🍜', '🍲', '🍛', '🍣', '🍱', '🥟', '🍤', '🍙', '🍚', '🍘', '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫', '🍿', '🍩', '🍪'],
  activity: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '⛳', '🏹', '🎣', '🤿', '🥊', '🥋', '🥌', '🎽', '⛸', '🥌', '🛷', '⛷', '🏂', '🏋️', '🤼', '🤸', '🤺', '🤾', '🏌️', '🏇', '🧘', '🏄', '🏊', '🤽', '🚣', '🧗', '🚵', '🚴', '🏆', '🥇', '🥈', '🥉', '🏅', '🎖', '🏵', '🎗', '🎫', '🎟', '🎯', '🎲', '🎰', '🎮', '🕹', '🎳', '🎯', '🎱', '🎸', '🎺', '🎷', '🥁', '🎹', '🎼', '🎧', '🎤', '🎙', '📻'],
  travel: ['🚗', '🚕', '🚙', '🚌', '🚎', '🏎', '🚓', '🚑', '🚒', '🚐', '🛻', '🚚', '🚛', '🚜', '🏍', '🛵', '🚲', '🛴', '🛹', '🛼', '🚁', '🛸', '🚂', '🚆', '🚄', '🚅', '🚈', '🚝', '🚞', '🚋', '🚃', '🚎', '🚐', '🚑', '🚒', '🚚', '🚛', '🚜', '🏎', '🚓', '🚔', '🚍', '🛵', '🚲', '🛴', '🛹', '🛼', '🚁', '🛸', '🚂', '🚆', '🚄', '🚅', '🚈', '🚝', '🚞', '🚋', '🚃', '🚎', '🚐', '🚑', '🚒', '🚚', '🚛', '🚜', '🏎', '🚓', '🚔'],
  object: ['⌚', '📱', '📲', '💻', '⌨️', '🖥', '🖨', '🖱', '🖲', '🕹', '🗜', '💽', '💾', '💿', '📀', '📼', '📷', '📸', '📹', '🎥', '📽', '🎞', '📞', '☎️', '📟', '📠', '📺', '📻', '🎙', '🎚', '🎛', '🧭', '⏱', '⏲', '⏰', '🕰', '⌛', '⏳', '📡', '🔋', '🔌', '💡', '🔦', '🕯', '🪔', '🧯', '🛢', '💸', '💵', '💴', '💶', '💷', '💰', '💳', '💎', '⚖️', '🧰', '🔧', '🔨', '⚒', '🛠', '⛏', '🔩', '⚙', '🧱', '⛓', '🧲', '🔫', '💣', '🧨', '🪓', '🔪', '🗡', '⚔️', '🛡', '🚬', '⚰️', '⚱️', '🏺'],
  symbol: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑', '♒', '♓', '🆔', '⚛️', '🉑', '☢️', '☣️', '📴', '📳', '🈶', '🈚', '🈯', '🈳', '🈲', '🅰️', '🅱️', '🆎', '🆑', '🅾️', '🆘', '❌', '⭕', '🛐', '⚛️', '🉑', '💮', '🉐', '㊙️', '㊗️', '🈴', '🈵', '🈹', '🈲', '🈚', '🈯', '🉑'],
  flag: ['🏁', '🚩', '🎌', '🏴', '🏳️', '🏳️‍🌈', '🏳️‍⚧️', '🏴‍☠️', '🇦🇨', '🇦🇩', '🇦🇪', '🇦🇫', '🇦🇬', '🇦🇮', '🇦🇱', '🇦🇲', '🇦🇴', '🇦🇶', '🇦🇷', '🇦🇸', '🇦🇹', '🇦🇺', '🇦🇼', '🇦🇽', '🇦🇿', '🇧🇦', '🇧🇧', '🇧🇩', '🇧🇪', '🇧🇫', '🇧🇬', '🇧🇭', '🇧🇮', '🇧🇯', '🇧🇰', '🇧🇱', '🇧🇲', '🇧🇳', '🇧🇴', '🇧🇶', '🇧🇷', '🇧🇸', '🇧🇹', '🇧🇻', '🇧🇼', '🇧🇾', '🇧🇿', '🇨🇦', '🇨🇨', '🇨🇩', '🇨🇫', '🇨🇬', '🇨🇭', '🇨🇮', '🇨🇰', '🇨🇱', '🇨🇲', '🇨🇳', '🇨🇴', '🇨🇵', '🇨🇷', '🇨🇺', '🇨🇻', '🇨🇼', '🇨🇽', '🇨🇾', '🇨🇿', '🇩🇪', '🇩🇯', '🇩🇰', '🇩🇲', '🇩🇴', '🇩🇿']
}

const currentEmojis = computed(() => {
  return emojiData[activeCategory.value] || []
})

function setActiveCategory(category) {
  activeCategory.value = category
}

function selectEmoji(emoji) {
  emit('select', emoji)
  addToRecent(emoji)
  emit('close')
}

function addToRecent(emoji) {
  const recent = [...recentEmojis.value]
  const index = recent.indexOf(emoji)
  
  if (index > -1) {
    recent.splice(index, 1)
  }
  
  recent.unshift(emoji)
  recentEmojis.value = recent.slice(0, 20) // 只保留最近20个
  
  // 保存到本地存储
  localStorage.setItem('recent-emojis', JSON.stringify(recentEmojis.value))
}

function loadRecentEmojis() {
  try {
    const saved = localStorage.getItem('recent-emojis')
    if (saved) {
      recentEmojis.value = JSON.parse(saved)
    }
  } catch (error) {
    console.warn('Failed to load recent emojis:', error)
  }
}

onMounted(() => {
  loadRecentEmojis()
})
</script>

<style scoped>
.emoji-picker {
  position: absolute;
  bottom: 60px;
  right: 16px;
  width: 320px;
  max-height: 400px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8e8e8;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.emoji-header {
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.emoji-tabs {
  display: flex;
  gap: 4px;
}

.emoji-tab {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.emoji-tab:hover {
  background: #f5f5f5;
}

.emoji-tab.active {
  background: #e6f7ff;
  color: #1677ff;
}

.emoji-content {
  flex: 1;
  overflow-y: auto;
  max-height: 240px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 2px;
  padding: 8px;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  font-size: 20px;
  transition: all 0.2s;
}

.emoji-item:hover {
  background: #f5f5f5;
  transform: scale(1.2);
}

.emoji-footer {
  padding: 8px;
  border-top: 1px solid #f0f0f0;
}

.recent-emojis {
  margin-top: 8px;
}

.recent-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.recent-emoji-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.recent-emoji {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.recent-emoji:hover {
  background: #f5f5f5;
  transform: scale(1.2);
}

/* 滚动条样式 */
.emoji-content::-webkit-scrollbar {
  width: 4px;
}

.emoji-content::-webkit-scrollbar-track {
  background: transparent;
}

.emoji-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

.emoji-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>