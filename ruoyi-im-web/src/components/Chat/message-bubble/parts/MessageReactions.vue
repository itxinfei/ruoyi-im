<template>
  <div v-if="hasReactions" class="reaction-aggregate">
    <div
      v-for="reaction in reactions"
      :key="reaction.emoji"
      class="reaction-item"
      :class="{ 'is-active': reaction.hasOwnReaction }"
      @click.stop="$emit('toggle', reaction.emoji)"
    >
      <span class="reaction-emoji">{{ reaction.emoji }}</span>
      <span class="reaction-count">{{ reaction.count }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['toggle'])

const store = useStore()

// 按表情分组
const reactions = computed(() => {
  if (!props.message?.reactions) return []

  const currentUser = store.getters['user/currentUser']
  const grouped = {}

  props.message.reactions.forEach(r => {
    if (!grouped[r.emoji]) {
      grouped[r.emoji] = {
        emoji: r.emoji,
        count: 0,
        hasOwnReaction: false
      }
    }
    grouped[r.emoji].count++
    grouped[r.emoji].hasOwnReaction = r.userId === currentUser?.id
  })

  return Object.values(grouped)
})

const hasReactions = computed(() => reactions.value.length > 0)
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.reaction-aggregate {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
  margin-left: -2px;
}

.reaction-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(0, 137, 255, 0.08);
  border-radius: var(--dt-radius-lg);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--dt-transition-base);
  border: 1px solid transparent;

  .reaction-emoji {
    font-size: 14px;
  }

  .reaction-count {
    font-size: 11px;
    font-weight: 500;
    color: var(--dt-text-secondary);
    min-width: 12px;
  }

  &:hover {
    background: rgba(0, 137, 255, 0.15);

    .reaction-count {
      color: var(--dt-brand-color);
    }
  }

  &.is-active {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-color);

    .reaction-emoji {
      animation: bounce 0.3s var(--dt-ease-out);
    }

    .reaction-count {
      color: var(--dt-brand-color);
    }
  }
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

// 暗色模式
:global(.dark) {
  .reaction-item {
    background: rgba(0, 137, 255, 0.1);
    border-color: transparent;

    &:hover {
      background: rgba(0, 137, 255, 0.2);
      border-color: #0089FF;
    }

    &.is-active {
      background: rgba(0, 137, 255, 0.25);
      border-color: #0089FF;
    }

    .reaction-emoji {
      filter: brightness(1.1);
    }

    .reaction-count {
      color: var(--dt-text-secondary);
    }

    &.is-active .reaction-count {
      color: var(--dt-text-primary);
    }
  }
}
</style>
