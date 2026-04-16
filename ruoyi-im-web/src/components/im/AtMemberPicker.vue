<template>
  <div v-if="visible" class="at-member-picker" :style="pickerStyle">
    <!-- 搜索框 -->
    <div class="picker-search">
      <el-input
        ref="searchInputRef"
        v-model="keyword"
        placeholder="搜索成员"
        size="small"
        clearable
        @keydown.enter="selectFirstMember"
        @keydown.esc="close"
        @keydown.up.prevent="navigateUp"
        @keydown.down.prevent="navigateDown"
      />
    </div>

    <!-- 成员列表 -->
    <div ref="listRef" class="picker-list">
      <div
        v-for="(member, index) in filteredMembers"
        :key="member.userId"
        :class="['picker-item', { 'is-active': activeIndex === index }]"
        @click="selectMember(member)"
        @mouseenter="activeIndex = index"
      >
        <img
          :src="member.avatar || '/avatars/default.png'"
          class="member-avatar"
          alt="avatar"
        >
        <div class="member-info">
          <span class="member-name">{{ member.nickname || member.name }}</span>
          <span v-if="member.role && member.role !== 'MEMBER'" class="member-role">
            {{ getRoleLabel(member.role) }}
          </span>
        </div>
        <el-icon v-if="activeIndex === index" class="select-icon">
          <Check />
        </el-icon>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredMembers.length === 0" class="picker-empty">
        <el-icon><User /></el-icon>
        <span>未找到成员</span>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * AtMemberPicker.vue - @成员选择器
 * 对齐钉钉设计规范，悬浮在输入框上方
 */
import { ref, computed, watch, nextTick } from 'vue'
import { Check, User } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  members: {
    type: Array,
    default: () => []
  },
  position: {
    type: Object,
    default: () => ({ top: 0, left: 0 })
  }
})

const emit = defineEmits(['select', 'close'])

// 状态
const keyword = ref('')
const activeIndex = ref(0)
const searchInputRef = ref(null)
const listRef = ref(null)

// 计算样式
const pickerStyle = computed(() => ({
  top: `${props.position.top}px`,
  left: `${props.position.left}px`
}))

// 过滤成员
const filteredMembers = computed(() => {
  if (!keyword.value) return props.members.slice(0, 10)
  const kw = keyword.value.toLowerCase()
  return props.members
    .filter(m =>
      (m.nickname || m.name || '').toLowerCase().includes(kw) ||
      (m.userId || '').toString().includes(kw)
    )
    .slice(0, 10)
})

// 角色标签
const getRoleLabel = (role) => {
  const roleMap = {
    OWNER: '群主',
    ADMIN: '管理员'
  }
  return roleMap[role] || ''
}

// 选择成员
const selectMember = (member) => {
  emit('select', member)
  close()
}

// 关闭
const close = () => {
  keyword.value = ''
  activeIndex.value = 0
  emit('close')
}

// 选中第一个
const selectFirstMember = () => {
  if (filteredMembers.value.length > 0) {
    selectMember(filteredMembers.value[activeIndex.value])
  }
}

// 向上导航
const navigateUp = () => {
  if (activeIndex.value > 0) {
    activeIndex.value--
    scrollActiveIntoView()
  }
}

// 向下导航
const navigateDown = () => {
  if (activeIndex.value < filteredMembers.value.length - 1) {
    activeIndex.value++
    scrollActiveIntoView()
  }
}

// 滚动激活项到可视区域
const scrollActiveIntoView = () => {
  nextTick(() => {
    if (listRef.value) {
      const activeEl = listRef.value.querySelector('.is-active')
      if (activeEl) {
        activeEl.scrollIntoView({ block: 'nearest' })
      }
    }
  })
}

// 监听 visible 变化，自动聚焦
watch(() => props.visible, (val) => {
  if (val) {
    nextTick(() => {
      searchInputRef.value?.focus()
    })
  }
})

// 重置激活索引
watch(filteredMembers, () => {
  activeIndex.value = 0
})
</script>

<style scoped lang="scss">
.at-member-picker {
  position: absolute;
  z-index: var(--dt-z-popover);
  width: 240px;
  max-height: 300px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-float);
  border: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.picker-search {
  padding: var(--dt-spacing-sm);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.picker-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-xs) 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-bg);
    border-radius: var(--dt-radius-sm);
  }
}

.picker-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);

  &:hover,
  &.is-active {
    background-color: var(--dt-bg-hover);
  }

  &.is-active {
    background-color: var(--dt-brand-bg);
  }
}

.member-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--dt-border-lighter);
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  min-width: 0;
}

.member-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-role {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  padding: 1px 4px;
  border-radius: var(--dt-radius-xs);
  flex-shrink: 0;
}

.select-icon {
  color: var(--dt-brand-color);
  font-size: var(--dt-font-size-base);
  flex-shrink: 0;
}

.picker-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--dt-spacing-xl);
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
  gap: var(--dt-spacing-sm);

  .el-icon {
    font-size: var(--dt-font-size-2xl);
  }
}
</style>
