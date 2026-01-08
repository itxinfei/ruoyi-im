<template>
  <div class="dt-emoji-picker">
    <!-- 分类标签 -->
    <div class="dt-emoji-picker__header">
      <div class="dt-emoji-picker__tabs">
        <div
          v-for="category in categories"
          :key="category.key"
          class="dt-emoji-picker__tab"
          :class="{ active: currentCategory === category.key }"
          @click="currentCategory = category.key"
        >
          {{ category.icon }}
        </div>
      </div>
    </div>

    <!-- 表情网格 -->
    <div class="dt-emoji-picker__body">
      <div
        v-for="emoji in currentEmojis"
        :key="emoji"
        class="dt-emoji-picker__emoji"
        @click="selectEmoji(emoji)"
      >
        {{ emoji }}
      </div>
    </div>

    <!-- 底部最近使用 -->
    <div class="dt-emoji-picker__footer">
      <span class="dt-emoji-picker__recent">最近使用</span>
      <div class="dt-emoji-picker__recent-list">
        <span
          v-for="emoji in recentEmojis.slice(0, 10)"
          :key="emoji"
          class="dt-emoji-picker__recent-emoji"
          @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </span>
        <span v-if="recentEmojis.length === 0" class="dt-emoji-picker__empty">
          暂无最近使用的表情
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const emit = defineEmits(['select'])

// 表情分类
const categories = [
  { key: 'smileys', icon: '😊', name: '表情' },
  { key: 'animals', icon: '🐱', name: '动物' },
  { key: 'food', icon: '🍎', name: '食物' },
  { key: 'activities', icon: '⚽', name: '活动' },
  { key: 'travel', icon: '🚗', name: '旅行' },
  { key: 'objects', icon: '💡', name: '物品' },
  { key: 'symbols', icon: '❤️', name: '符号' },
]

// 表情数据 - 精选常用表情
const emojiData = {
  smileys: [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
    '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
    '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜',
    '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',
    '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬',
    '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒',
    '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '😶‍🌫️', '🥴',
    '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕',
    '😟', '🙁', '😮', '😯', '😲', '😳', '🥺', '😦',
    '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖',
    '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡',
    '😠', '🤬', '👋', '🤚', '🖐️', '✋', '🖖', '👌',
    '🤌', '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈',
    '👉', '👆', '🖕', '👇', '☝️', '👍', '👎', '✊',
    '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝',
  ],
  animals: [
    '🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼',
    '🐨', '🐯', '🦁', '🐮', '🐷', '🐸', '🐵', '🙈',
    '🙉', '🙊', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥',
    '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄',
    '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦟', '🦗',
    '🕷️', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙',
    '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬',
    '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍',
    '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🐃', '🐂',
    '🐄', '🐎', '🐖', '🐏', '🐑', '🦙', '🐐', '🦌',
    '🐕', '🐩', '🦮', '🐕‍🦺', '🐈', '🐈‍⬛', '🐓', '🦃',
    '🦚', '🦜', '🦢', '🦩', '🕊️', '🐇', '🦝', '🦨',
    '🦡', '🦦', '🦥', '🐁', '🐀', '🐿️', '🦔', '🐾',
  ],
  food: [
    '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓',
    '🫐', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝',
    '🍅', '🥑', '🥦', '🥬', '🥒', '🌶️', '🫑', '🌽',
    '🥕', '🫒', '🧄', '🧅', '🥔', '🍠', '🥐', '🥯',
    '🍞', '🥖', '🥨', '🧀', '🥚', '🍳', '🧈', '🥞',
    '🧇', '🥓', '🥩', '🍗', '🍖', '🦴', '🌭', '🍔',
    '🍟', '🍕', '🥪', '🥙', '🧆', '🌮', '🌯', '🫔',
    '🥗', '🥘', '🫕', '🥫', '🍝', '🍜', '🍲', '🍛',
    '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍚', '🍘',
    '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦',
    '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫',
    '🍿', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '☕',
    '🫖', '🍵', '🧃', '🥤', '🧋', '🍶', '🍺', '🍷',
    '🥂', '🥃', '🍸', '🍹', '🧉', '🍾', '🥄', '🍴',
    '🍽️', '🥣', '🥡', '🥢', '🧂', '🥫', '🍱', '🥘',
  ],
  activities: [
    '⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉',
    '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🏑', '🥍',
    '🏏', '🥅', '⛳', '🪁', '🏹', '🎣', '🤿', '🥊',
    '🥋', '🎽', '🛹', '🛷', '⛸️', '🥌', '🎿', '⛷️',
    '🏂', '🪂', '🏋️', '🤼', '🤸', '⛹️', '🤺', '🤾',
    '🏊', '🚣', '🧗', '🚵', '🚴', '🏆', '🥇', '🥈',
    '🥉', '🏅', '🎖️', '🏵️', '🎗️', '🎫', '🎟️', '🎪',
    '🤹', '🎭', '🎨', '🎬', '🎤', '🎧', '🎼', '🎹',
    '🥁', '🎷', '🎺', '🎸', '🪕', '🎻', '🎲', '♟️',
    '🎯', '🎳', '🎮', '🎰', '🧩', '🎗️', '🎞️', '📽️',
  ],
  travel: [
    '🚗', '🚕', '🚙', '🚌', '🚎', '🏎️', '🚓', '🚑',
    '🚒', '🚐', '🚚', '🚛', '🚜', '🛴', '🚲', '🛵',
    '🏍️', '🚨', '🚔', '🚍', '🚘', '🚖', '🚡', '🚠',
    '🚟', '🚃', '🚋', '🚞', '🚝', '🚄', '🚅', '🚈',
    '🚂', '🚆', '🚇', '🚊', '🚉', '✈️', '🛫', '🛬',
    '🛩️', '💺', '🛰️', '🚀', '🛸', '🚁', '🛶', '⛵',
    '🚤', '🛥️', '🛳️', '⛴️', '🚢', '⚓', '⛽', '🚧',
    '🚦', '🚥', '🚏', '🗺️', '🗿', '🗽', '🗼', '🏰',
    '🏯', '🏟️', '🎡', '🎢', '🎠', '⛲', '⛱️', '🏖️',
    '🏝️', '🏜️', '🌋', '⛰️', '🏔️', '🗻', '🏕️', '⛺',
    '🏠', '🏡', '🏘️', '🏚️', '🏗️', '🏭', '🏢', '🏬',
    '🏣', '🏤', '🏥', '🏦', '🏨', '🏪', '🏫', '🏩',
    '💒', '🏛️', '⛪', '🕌', '🕍', '🛕', '🕋', '⛩️',
    '🗾', '🎑', '🏞️', '🌅', '🌄', '🌠', '🎇', '🎆',
    '🌇', '🌆', '🏙️', '🌃', '🌌', '🌉', '🌁', '⛲',
  ],
  objects: [
    '⌚', '📱', '📲', '💻', '⌨️', '🖥️', '🖨️', '🖱️',
    '🖲️', '🕹️', '🗜️', '💾', '💿', '📀', '📼', '📷',
    '📸', '📹', '🎥', '📽️', '🎞️', '📞', '☎️', '📟',
    '📠', '📺', '📻', '🎙️', '🎚️', '🎛️', '🧭', '⏱️',
    '⏲️', '⏰', '🕰️', '⌛', '⏳', '📡', '🔋', '🔌',
    '💡', '🔦', '🕯️', '🪔', '🧯', '🛢️', '💸', '💵',
    '💴', '💶', '💷', '💰', '💳', '💎', '⚖️', '🪜',
    '🧰', '🪛', '🔧', '🔨', '⚒️', '🛠️', '⛏️', '🪚',
    '🔩', '⚙️', '🪤', '🧱', '⛓️', '🧲', '🔫', '💣',
    '🧨', '🪓', '🔪', '🗡️', '⚔️', '🛡️', '🚬', '⚰️',
    '🪦', '⚱️', '🏺', '🔮', '📿', '🧿', '💈', '⚗️',
    '🔭', '🔬', '🕳️', '🩹', '🩺', '💊', '💉', '🩸',
    '🧬', '🦠', '🧫', '🧪', '🌡️', '🧹', '🪺', '🪻',
    '🚽', '🪠', '🚿', '🛁', '🛀', '🧼', '🪥', '🪒',
    '🧽', '🪣', '🧴', '🛎️', '🔑', '🗝️', '🚪', '🪑',
  ],
  symbols: [
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
    '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖',
    '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️',
    '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈',
    '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐',
    '♑', '♒', '♓', '🆔', '⚛️', '🉑', '☢️', '☣️',
    '📴', '📳', '🈶', '🈚', '🈸', '🈺', '🈷️', '✴️',
    '🆚', '💮', '🉐', '㊙️', '㊗️', '🈴', '🈵', '🈹',
    '🈲', '🅰️', '🅱️', '🆎', '🆑', '🅾️', '🆘', '❌',
    '⭕', '🛑', '⛔', '📛', '🚫', '💯', '💢', '♨️',
    '🚷', '🚯', '🚳', '🚱', '🔞', '📵', '🚭', '❗',
    '❕', '❓', '❔', '‼️', '⁉️', '🔅', '🔆', '〽️',
    '⚠️', '🚸', '🔱', '⚜️', '🔰', '♻️', '✅', '🈯',
    '💹', '❇️', '✳️', '❎', '🌐', '💠', 'Ⓜ️', '🌀',
    '💤', '🏧', '🚾', '♿', '🅿️', '🛗', '🛂', '🛃',
  ],
}

// 响应式数据
const currentCategory = ref('smileys')
const recentEmojis = ref([])

// 计算属性
const currentEmojis = computed(() => {
  return emojiData[currentCategory.value] || []
})

// 方法
const selectEmoji = (emoji) => {
  emit('select', emoji)
  addToRecent(emoji)
}

const addToRecent = (emoji) => {
  const index = recentEmojis.value.indexOf(emoji)
  if (index > -1) {
    recentEmojis.value.splice(index, 1)
  }
  recentEmojis.value.unshift(emoji)

  // 保持最近使用的表情数量在20个以内
  if (recentEmojis.value.length > 20) {
    recentEmojis.value = recentEmojis.value.slice(0, 20)
  }

  // 保存到本地存储
  localStorage.setItem('dt-recent-emojis', JSON.stringify(recentEmojis.value))
}

// 生命周期
onMounted(() => {
  // 从本地存储加载最近使用的表情
  const saved = localStorage.getItem('dt-recent-emojis')
  if (saved) {
    try {
      recentEmojis.value = JSON.parse(saved)
    } catch (error) {
      console.error('Failed to load recent emojis:', error)
    }
  }
})
</script>

<style lang="scss" scoped>
.dt-emoji-picker {
  // 钉钉6.5精确尺寸：360px宽
  width: 360px;
  background: var(--dt-bg-component);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-4);
  overflow: hidden;
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  z-index: var(--dt-z-dropdown);

  // 头部 - 56px高
  &__header {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    border-bottom: 1px solid var(--dt-border-lighter);
    background: var(--dt-bg-container);
    height: 56px;
  }

  &__tabs {
    display: flex;
    gap: 4px;
  }

  &__tab {
    // 36px x 36px
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    cursor: pointer;
    border-radius: var(--dt-radius-base);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-elevated);
    }

    &.active {
      background: rgba(22, 119, 255, 0.1);
    }
  }

  // 表情网格 - 240px高，8列
  &__body {
    height: 240px;
    padding: 12px;
    overflow-y: auto;
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;
    align-content: start;
  }

  &__emoji {
    // 36px x 36px 单元格
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    cursor: pointer;
    border-radius: var(--dt-radius-base);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-container);
      transform: scale(1.15);
    }
  }

  // 底部最近使用 - 48px高
  &__footer {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border-top: 1px solid var(--dt-border-lighter);
    height: 48px;
    background: var(--dt-bg-container);
  }

  &__recent {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    margin-right: 12px;
    white-space: nowrap;
  }

  &__recent-list {
    display: flex;
    gap: 4px;
    flex: 1;
    overflow-x: auto;
  }

  &__recent-emoji {
    font-size: 18px;
    cursor: pointer;
    padding: 2px;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);
    flex-shrink: 0;

    &:hover {
      background: var(--dt-bg-elevated);
      transform: scale(1.1);
    }
  }

  &__empty {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-placeholder);
  }
}

// 滚动条样式
.dt-emoji-picker__body::-webkit-scrollbar {
  width: 6px;
}

.dt-emoji-picker__body::-webkit-scrollbar-track {
  background: transparent;
}

.dt-emoji-picker__body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;

  &:hover {
    background: rgba(0, 0, 0, 0.2);
  }
}

.dt-emoji-picker__recent-list::-webkit-scrollbar {
  height: 4px;
}

.dt-emoji-picker__recent-list::-webkit-scrollbar-track {
  background: transparent;
}

.dt-emoji-picker__recent-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;

  &:hover {
    background: rgba(0, 0, 0, 0.2);
  }
}

// 动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--dt-transition-base) var(--dt-easing-ease-out);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
