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
  height: 60px;
  /* 默认标准高度 */
  overflow: hidden;
  background: #ffffff;
  cursor: pointer;
  transition: background var(--dt-transition-fast), height var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-brand-bg);

    .item-name {
      color: var(--dt-brand-color);
    }
  }

  /* 紧凑模式 - 48px */
  &.compact {
    height: 48px;

    .item-desc {
      display: none;
    }

    .contact-content {
      padding: 0 12px;
    }
  }

  /* 展开模式 - 72px */
  &.expanded {
    height: 72px;

    .item-info {
      gap: 6px;
    }

    .item-desc {
      font-size: var(--dt-font-size-xs);
      line-height: 1.3;
    }
  }
}

// 暗色模式适配
.dark .contact-item {
  background: var(--dt-bg-card-dark);

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.active {
    background: var(--dt-brand-bg-dark);
  }
}

.contact-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 12px;
  /* 钉钉标准内边距 */
  gap: 12px;
  /* 头像和内容之间的间距 */
  background: inherit;
  transition: transform var(--dt-transition-base);
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .dark & {
    color: var(--dt-text-primary-dark);
  }

  :deep(.highlight) {
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-semibold);
  }
}

.item-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-weight: var(--dt-font-weight-medium);
  flex-shrink: 0;
}

.item-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;

  .dark & {
    color: var(--dt-text-secondary-dark);
  }
}
</style>
