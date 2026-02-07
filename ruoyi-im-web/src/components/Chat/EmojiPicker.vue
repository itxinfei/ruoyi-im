<template>
  <div
    ref="pickerRef"
    v-click-outside="close"
    class="emoji-picker"
    :style="positionStyle"
  >
    <!-- 分类标签 -->
    <div class="emoji-tabs">
      <button
        v-for="category in categories"
        :key="category.id"
        class="emoji-tab"
        :class="{ active: activeCategory === category.id }"
        :title="category.name"
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
        :title="emoji"
        @click="selectEmoji(emoji)"
      >
        {{ emoji }}
      </button>
    </div>

    <!-- 底部状态栏 (可选，WildFire IM通常有) -->
    <div
      v-if="recentEmojis.length > 0 && activeCategory !== 'recent'"
      class="emoji-footer"
    >
      <span class="footer-tip">最近使用: {{ recentEmojis.slice(0, 5).join(' ') }}</span>
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

// 分类数据 (添加 name 用于 title)
const categories = [
  { id: 'recent', icon: '🕐', name: '最近使用' },
  { id: 'smile', icon: '😊', name: '表情' },
  { id: 'hand', icon: '👋', name: '手势' },
  { id: 'heart', icon: '❤️', name: '爱心' },
  { id: 'face', icon: '😎', name: '人物' },
  { id: 'food', icon: '🍔', name: '食物' },
  { id: 'animal', icon: '🐱', name: '动物' },
  { id: 'activity', icon: '⚽', name: '活动' }
]

// 表情数据
const emojiData = {
  recent: [],
  smile: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '🥸', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾', '🤖'],
  hand: ['👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👍', '👎', '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍️', '💅', '🤳', '💪'],
  heart: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑', '♒', '♓', '🆔', '⚛️'],
  face: ['👶', '👧', '🧒', '👦', '👩', '🧑', '👨', '👵', '🧓', '👴', '👲', '👳', '🧕', '👮', '👷', '💂', '🕵', '👩‍⚕️', '👨‍⚕️', '👩‍🌾', '👨‍🌾', '👩‍🍳', '👨‍🍳', '👩‍🎓', '👨‍🎓', '👩‍🎤', '👨‍🎤', '👩‍🏫', '👨‍🏫', '👩‍🏭', '👨‍🏭', '👩‍💻', '👨‍💻', '👩‍💼', '👨‍💼', '👩‍🔧', '👨‍🔧', '👩‍🔬', '👨‍🔬', '👩‍🎨', '👨‍🎨', '👩‍🚒', '👨‍🚒', '👩‍✈️', '👨‍✈️', '👩‍🚀', '👨‍🚀', '👩‍⚖️', '👨‍⚖️', '👰', '🤵', '👸', '🤴', '🦸', '🦹', '🤶', '🎅', '🧙', '🧝', '🧛', '🧟', '🧞', '🧜', '🧚', '👼', '🤰', '🤱', '🙇', '💁', '🙅', '🙆', '🙋', '🧏', '🤦', '🤷', '🙎', '🙍', '💇', '💆', '🧖', '💅', '🤳', '💃', '🕺', '👯', '🕴', '🚶', '🏃', '🧍', '🧎', '👨‍🦯', '👨‍🦼', '👨‍🦽', '🛀', '🛌', '👭', '👫', '👬', '💏', '💑', '👪'],
  food: ['🍏', '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝', '🍅', '🍆', 'AVOCADO', '🥦', '🥬', '🥒', '🌶️', '🫑', '🌽', '🥕', '🫒', '🧄', '🧅', '🥔', '🍠', '🥐', '🥯', '🍞', '🥖', '🥨', '🧀', '🥚', '🍳', '🧈', '🥞', '🧇', '🥓', '🥩', '🍗', '🍖', '🦴', '🌭', '🍔', '🍟', '🍕', '🫓', '🥪', '🥙', '🧆', '🌮', '🌯', '🫔', '🥗', '🥘', '🫕', '🥫', '🍝', '🍜', '🍲', '🍛', '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍚', '🍘', '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫', '🍿', '🍩', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '☕', '🍵', '🧃', '🥤', '🧋', '🍶', '🍺', '🍻', '🥂', '🍷', '🥃', '🍸', '🍹', '🧉', '🍾', '🧊', '🥄', '🍴', '🍽️', '🥣', '🥡', '🥢', '🧂'],
  animal: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐻‍❄️', '🐨', '🐯', '🦁', '🐮', '🐷', '🐽', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🪱', '🐛', '🦋', '🐌', '🐞', '🐜', '🪰', '🪲', '🪳', '🦟', '🦗', '🕷️', '🕸️', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🦣', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🦘', '🦬', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🦙', '🐐', '🦌', '🐕', '🐩', '🦮', '🐕‍🦺', '🐈', '🐈‍⬛', '🐓', '🦃', '🦚', '🦜', '🦢', '🦩', '🕊️', '🐇', '🦝', '🦨', '🦡', '🦫', '🦦', '🦥', '🐁', '🐀', '🐿️', '🦔', '🐾', '🐉', '🐲', '🌵', '🎄', '🌲', '🌳', '🌴', '🌱', '🌿', '☘️', '🍀', '🎍', '🎋', '🍃', '🍂', '🍁', '🍄', '🐚', '🪨', '🪵', '💐', '🌷', '🌹', '🥀', '🌺', '🌸', '🌼', '🌻', '🌞', '🌝', '🌛', '🌜', '🌚', '🌕', '🌖', '🌗', '🌘', '🌑', '🌒', '🌓', '🌔', '🌙', '🌎', '🌍', '🌏', '🪐', '💫', '⭐', '🌟', '✨', '⚡', '☄️', '💥', '🔥', '🌪️', '🌈', '☀️', '🌤️', '⛅', '🌥️', '☁️', '🌦️', '🌧️', '⛈️', '🌩️', '🌨️', '❄️', '☃️', '⛄', '🌬️', '💨', '💧', '💦', '☔', '☂️', '🌊', '🌫️'],
  activity: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🥅', '⛳', '🪁', '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛼', '🛷', '⛸️', '🥌', '🎿', '⛷️', '🏂', '🪂', '🏋️', '🤼', '🤸', '⛹️', '🤺', '🤾', '🏌️', '🏇', '🧘', '🏄', '🏊', '🤽', '🚣', '🧗', '🚵', '🚴', '🏆', '🥇', '🥈', '🥉', '🏅', '🎖️', '🏵️', '🎗️', '🎫', '🎟️', '🎪', '🤹', '🎭', '🩰', '🎨', '🎬', '🎤', '🎧', '🎼', '🎹', '🥁', '🎷', '🎺', '🎸', '🪕', '🎻', '🎲', '♟️', '🎯', '🎳', '🎮', '🎰', '🧩']
}

// 最近使用的表情
const recentEmojis = ref([])

// 加载最近使用的表情
const loadRecentEmojis = () => {
  recentEmojis.value = getRecentEmoji()
}

// 保存表情到最近使用
const saveRecentEmoji = emoji => {
  addRecentEmoji(emoji, 20)
  recentEmojis.value = getRecentEmoji()
}

// 当前分类的表情
const currentEmojis = computed(() => {
  if (activeCategory.value === 'recent') {
    // 如果没有最近使用的表情，默认显示笑脸分类
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
const selectEmoji = emoji => {
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
    el.clickOutsideEvent = event => {
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
watch(recentEmojis, newVal => {
  if (activeCategory.value === 'recent' && newVal.length === 0) {
    activeCategory.value = 'smile'
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.emoji-picker {
  position: fixed;
  width: 360px; // 野火IM风格：360px宽度，更宽敞
  height: 280px;
  background: #ffffff; // 野火IM:纯白背景
  border: 1px solid #e0e0e0; // 野火IM:灰色边框
  border-radius: 8px; // 野火IM:8px圆角
  box-shadow: var(--dt-shadow-lg); // 野火IM:柔和阴影
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .dark & {
    background: #1e1e1e;
    border-color: #374151;
  }
}

.emoji-tabs {
  display: flex;
  padding: 0 8px;
  background: #f5f5f5; // 野火IM:浅灰背景头部
  border-bottom: 1px solid #e0e0e0;
  overflow-x: auto;
  flex-shrink: 0;

  &::-webkit-scrollbar {
    height: 0;
    display: none;
  }

  .dark & {
    background: #2d2d2d;
    border-color: #374151;
  }
}

.emoji-tab {
  flex-shrink: 0;
  width: 40px; // 加宽触控区域
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  .tab-icon {
    font-size: 18px;
    filter: grayscale(100%); // 默认灰色
    opacity: 0.7;
    transition: all 0.2s;
  }

  &:hover {
    .tab-icon {
      filter: grayscale(0%);
      opacity: 1;
      transform: scale(1.1);
    }
  }

  &.active {
    .tab-icon {
      filter: grayscale(0%);
      opacity: 1;
    }

    // 底部指示条
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 5px;
      right: 5px;
      height: 2px;
      background: #4168e0; // 野火IM蓝
      border-radius: 2px 2px 0 0;
    }
  }
}

.emoji-list {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(9, 1fr); // 增加列数
  padding: 12px;
  gap: 4px;
  overflow-y: auto;
  align-content: start;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb);
    border-radius: 3px;

    &:hover {
      background: var(--dt-scrollbar-thumb-hover);
    }
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
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;

  &:hover {
    background: #f0f0f0;
    transform: scale(1.2);
    font-size: 24px;
    z-index: 1;
  }

  &:active {
    transform: scale(0.95);
  }

  .dark &:hover {
    background: var(--dt-bg-hover-dark);
  }
}

.emoji-footer {
  padding: 6px 12px;
  font-size: 12px;
  color: #999;
  background: #f9f9f9;
  border-top: 1px solid #eee;
  flex-shrink: 0;

  .dark & {
    background: #252525;
    border-color: #374151;
    color: #666;
  }
}
</style>
