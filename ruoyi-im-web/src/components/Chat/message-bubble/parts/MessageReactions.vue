<template>
  <div
    v-if="hasReactions"
    class="reaction-aggregate"
  >
    <div
      v-for="reaction in reactions"
      :key="reaction.emoji"
      class="reaction-item"
      :class="{ 'is-active': reaction.hasOwnReaction }"
      :title="reaction.usersTitle"
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
  if (!props.message?.reactions) { return [] }

  const currentUser = store.getters['user/currentUser']
  const grouped = {}

  props.message.reactions.forEach(r => {
    if (!grouped[r.emoji]) {
      grouped[r.emoji] = {
        emoji: r.emoji,
        count: 0,
        hasOwnReaction: false,
        users: []
      }
    }
    grouped[r.emoji].count++
    grouped[r.emoji].users.push(r.userName || r.userId)
    grouped[r.emoji].hasOwnReaction = r.userId === currentUser?.id
  })

  return Object.values(grouped).map(r => ({
    ...r,
    usersTitle: r.users.join('、')
  }))
})

const hasReactions = computed(() => reactions.value.length > 0)
</script>

<style scoped lang="scss">
// 表情回应组件 - 野火IM风格

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
  background: var(--dt-brand-light);
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;

  .reaction-emoji {
    font-size: 14px;
  }

  .reaction-count {
    font-size: 11px;
    font-weight: 500;
    color: var(--dt-text-medium);
    min-width: 12px;
  }

  &:hover {
    background: var(--dt-brand-extra-light);

    .reaction-count {
      color: var(--dt-brand-color);
    }
  }

  &.is-active {
    background: var(--dt-brand-extra-light);
    border-color: var(--dt-brand-color);

    .reaction-emoji {
      animation: bounce 0.3s ease-out;
    }

    .reaction-count {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }
}

@keyframes bounce {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.3);
  }
}

// 暗色模式
:global(.dark) {
  .reaction-item {
    background: var(--dt-brand-bg-dark);
    border-color: transparent;

    .reaction-count {
      color: var(--dt-text-secondary-dark);
    }

    &:hover {
      background: var(--dt-brand-bg-hover);
      border-color: var(--dt-brand-color);
    }

    &.is-active {
      background: var(--dt-brand-bg-dark);
      border-color: var(--dt-brand-color);
    }

    .reaction-emoji {
      filter: brightness(1.1);
    }

    &.is-active .reaction-count {
      color: var(--dt-brand-color-light);
    }
  }
}
</style>
