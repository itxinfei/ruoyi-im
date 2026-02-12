<template>
  <div
    class="contact-item"
    :class="{
      active: isActive,
      'swiped-left': isSwipedLeft,
      [size]: true
    }"
    @click="$emit('click', item)"
    @contextmenu.prevent="$emit('contextmenu', $event, item)"
  >
    <!-- 主要内容 -->
    <div class="contact-content">
      <DingtalkAvatar
        :name="item.name || item.displayName"
        :size="36"
        :src="item.avatar"
        :shape="item.type === 'group' ? 'square' : 'circle'"
      />
      <div class="item-info">
        <div class="item-header">
          <span
            class="item-name"
            v-html="highlightName"
          />
          <span
            v-if="item.tag"
            class="item-tag"
          >{{ item.tag }}</span>
        </div>
        <div
          v-if="item.description || item.dept || item.position"
          class="item-desc"
        >
          {{ item.description || item.position || item.dept }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  },
  searchQuery: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    enum: ['compact', 'normal', 'expanded'],
    default: 'normal'
  }
})

const emit = defineEmits(['click', 'contextmenu', 'delete'])

// 高亮显示搜索关键词（转义特殊字符）
const highlightName = computed(() => {
  const name = props.item.name || props.item.displayName || ''
  if (!props.searchQuery) { return name }

  // 先转义 HTML 防止 XSS，再进行关键词高亮
  const escapedName = escapeHtml(name)
  const escapedQuery = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const reg = new RegExp(`(${escapedQuery})`, 'gi')
  return escapedName.replace(reg, '<span class="highlight">$1</span>')
})

// 防止 XSS 攻击：对用户输入进行转义
const escapeHtml = str => {
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-item {
  position: relative;
  height: 64px; /* DingTalk Standard Height */
  overflow: hidden;
  background: var(--dt-bg-card);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border-bottom: 1px solid var(--dt-border-lighter);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-bg-session-active);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 15%;
      height: 70%;
      width: 3px;
      background: var(--dt-brand-color);
      border-radius: 0 4px 4px 0;
    }

    .item-name {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }

  /* 紧凑模式 */
  &.compact {
    height: 52px;

    .item-desc {
      display: none;
    }

    .contact-content {
      padding: 0 16px;
    }
  }

  /* 展开模式 */
  &.expanded {
    height: 72px;

    .item-info {
      gap: 4px;
    }
  }
}

// 暗色模式适配
.dark .contact-item {
  background: var(--dt-bg-card-dark);
  border-bottom-color: var(--dt-border-dark);

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.active {
    background: var(--dt-bg-session-active-dark);
  }
}

.contact-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 16px;
  gap: 12px;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  :deep(.highlight) {
    color: var(--dt-brand-color);
    font-weight: 600;
    background: var(--dt-search-highlight-bg);
  }
}

.item-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  flex-shrink: 0;
  border: 1px solid var(--dt-brand-light);
}

.item-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}
</style>
