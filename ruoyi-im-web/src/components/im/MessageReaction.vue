<template>
  <div class="reaction-container" v-if="reactions && reactions.length > 0">
    <div 
      v-for="group in groupedReactions" 
      :key="group.emoji"
      :class="['reaction-item', { 'is-mine': group.isMine }]"
      @click="toggleReaction(group.emoji)"
    >
      <span class="emoji">{{ group.emoji }}</span>
      <span class="count">{{ group.count }}</span>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * MessageReaction.vue (Vuex 修复版)
 */
import { computed } from 'vue';
import { useStore } from 'vuex';

const props = defineProps({
  messageId: [String, Number],
  reactions: Array
});

const store = useStore();

const groupedReactions = computed(() => {
  if (!props.reactions || props.reactions.length === 0) return [];

  const groups = {};
  const currentUserId = store.state.im?.currentUser?.id;

  props.reactions.forEach(reaction => {
    const key = reaction.emoji;
    if (!groups[key]) {
      groups[key] = {
        emoji: key,
        count: 0,
        users: [],
        isMine: false
      };
    }
    groups[key].count++;
    if (reaction.userId === currentUserId) {
      groups[key].isMine = true;
    }
  });

  return Object.values(groups);
});

const toggleReaction = (emoji) => {
  store.dispatch('im-message/toggleReaction', {
    messageId: props.messageId,
    emoji: emoji
  });
};
</script>

<style scoped>
.reaction-container { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 4px; }
.reaction-item {
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--dt-bg-hover);
  padding: 2px 6px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--dt-transition-fast);
}
.reaction-item:hover {
  background: var(--dt-bg-session-hover);
  transform: scale(1.02);
}
.reaction-item:active {
  transform: scale(0.98);
}
.reaction-item.is-mine { background: var(--dt-brand-bg); border-color: var(--dt-brand-color); }
.reaction-item.is-mine:hover { background: var(--dt-brand-bg-dark); }
.emoji { font-size: 14px; }
.count { font-size: var(--dt-font-size-xs); color: var(--dt-text-secondary); }
</style>
