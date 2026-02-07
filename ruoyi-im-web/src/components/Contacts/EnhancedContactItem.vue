<template>
  <div
    class="enhanced-contact-item"
    :class="{ active: isActive, 'batch-mode': batchMode }"
    @click="$emit('click', contact)"
  >
    <!-- 批量选择复选框 -->
    <el-checkbox
      v-if="batchMode"
      :model-value="selected"
      class="batch-checkbox"
      @change="$emit('toggle', contact.id, $event)"
      @click.stop
    />

    <!-- 头像区域 -->
    <div class="avatar-area">
      <DingtalkAvatar
        :src="contact.avatar || contact.groupAvatar"
        :name="contact.name || contact.groupName || contact.nickname"
        :size="44"
        :shape="contact.type === 'group' ? 'square' : 'circle'"
      />
      <!-- 在线状态指示 -->
      <div
        v-if="showOnlineStatus && !isGroup"
        class="online-indicator"
        :class="{ online: contact.online }"
      >
        <div class="indicator-dot" />
      </div>
    </div>

    <!-- 信息区域 -->
    <div class="info-area">
      <div class="info-main">
        <span
          class="contact-name"
          v-html="highlightName"
        />
        <span
          v-if="contact.role"
          class="role-badge"
          :class="contact.role.toLowerCase()"
        >
          {{ roleText }}
        </span>
      </div>
      <div class="info-sub">
        <span class="contact-desc">{{ description }}</span>
      </div>
      <div
        v-if="contact.tags && contact.tags.length > 0"
        class="info-tags"
      >
        <span
          v-for="(tag, idx) in contact.tags.slice(0, 2)"
          :key="idx"
          class="tag-pill"
          :style="{ backgroundColor: getTagColor(tag) }"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 操作区域 -->
    <div
      v-if="showActions"
      class="action-area"
    >
      <el-button
        :type="isActive ? 'primary' : ''"
        :icon="ChatDotRound"
        circle
        size="small"
        @click.stop="$emit('message', contact)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  contact: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  },
  batchMode: {
    type: Boolean,
    default: false
  },
  selected: {
    type: Boolean,
    default: false
  },
  searchQuery: {
    type: String,
    default: ''
  },
  showOnlineStatus: {
    type: Boolean,
    default: true
  },
  showActions: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'toggle', 'message'])

const isGroup = computed(() => props.contact.type === 'group' || props.contact.isGroup)

const roleText = computed(() => {
  const roleMap = {
    OWNER: '群主',
    ADMIN: '管理',
    MEMBER: '成员'
  }
  return roleMap[props.contact.role] || ''
})

const description = computed(() => {
  return (
    props.contact.signature ||
    props.contact.description ||
    props.contact.notice ||
    props.contact.dept ||
    '暂无介绍'
  )
})

// 高亮搜索关键词
const highlightName = computed(() => {
  const name = props.contact.name || props.contact.groupName || props.contact.nickname || ''
  if (!props.searchQuery) {return escapeHtml(name)}

  const escapedQuery = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const reg = new RegExp(`(${escapedQuery})`, 'gi')
  return escapeHtml(name).replace(reg, '<span class="highlight">$1</span>')
})

const escapeHtml = str => {
  if (!str) {return ''}
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

// 标签颜色生成
const getTagColor = tag => {
  const colors = [
    'var(--dt-brand-extra-light)',
    'var(--dt-success-05)',
    'var(--dt-warning-bg)',
    'var(--dt-error-bg)',
    'var(--dt-info-color)'
  ]
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.enhanced-contact-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 1px solid var(--el-border-color-lighter);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: var(--el-color-primary);
    transform: scaleY(0);
    transition: transform 0.25s ease;
  }

  &:hover {
    background: var(--el-fill-color-light);
    transform: translateX(4px);
  }

  &.active {
    background: var(--el-color-primary-light-9);

    &::before {
      transform: scaleY(1);
    }

    .contact-name {
      color: var(--el-color-primary);
      font-weight: 600;
    }
  }

  &.batch-mode {
    padding-left: 12px;
    transform: none !important;

    &:hover {
      transform: none;
    }
  }
}

.batch-checkbox {
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar-area {
  position: relative;
  flex-shrink: 0;
  margin-right: 12px;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;

  .indicator-dot {
    width: 100%;
    height: 100%;
    border-radius: var(--dt-radius-full);
    background-color: var(--el-color-info);
    border: 2px solid #fff;
    transition: all 0.3s;
  }

  &.online .indicator-dot {
    background-color: var(--el-color-success);
    box-shadow: 0 0 0 2px var(--dt-shadow-success-glow);
  }
}

.info-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  :deep(.highlight) {
    color: var(--el-color-primary);
    font-weight: 600;
    background: var(--el-color-primary-light-9);
    padding: 0 2px;
    border-radius: 2px;
  }
}

.role-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: var(--dt-radius-sm);
  flex-shrink: 0;
  font-weight: 500;

  &.owner {
    background: #fff7e6;
    color: #fa8c16;
  }

  &.admin {
    background: #e6f7ff;
    color: #1890ff;
  }
}

.info-sub {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-tags {
  display: flex;
  gap: 6px;
  margin-top: 2px;
}

.tag-pill {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  color: var(--el-text-color-regular);
}

.action-area {
  flex-shrink: 0;
  margin-left: 8px;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.25s ease;

  .enhanced-contact-item:hover & {
    opacity: 1;
    transform: translateX(0);
  }

  .enhanced-contact-item.active & {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
