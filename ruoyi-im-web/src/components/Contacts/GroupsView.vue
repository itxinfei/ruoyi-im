<template>
  <div class="dingtalk-groups-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="dt-loading">
      <div class="dt-spinner"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="groups.length === 0" class="dt-empty">
      <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke-linecap="round"/>
        <circle cx="9" cy="7" r="3.5"/>
        <path d="M23 21v-2a4 4 0 00-3-3.87" stroke-linecap="round"/>
        <path d="M16 3.13a4 4 0 010 7.75" stroke-linecap="round"/>
      </svg>
      <p>暂无群组</p>
    </div>

    <!-- 群组列表 -->
    <div v-else class="dt-groups-list">
      <div
        v-for="group in groups"
        :key="group.id"
        class="dt-group-item"
        @click="handleSelectGroup(group)"
      >
        <DingtalkAvatar
          :name="group.name"
          :size="44"
          :src="group.avatar"
          shape="square"
        />
        <div class="dt-group-info">
          <div class="dt-group-name">{{ group.name }}</div>
          <div class="dt-group-desc">
            <span v-if="group.memberCount">{{ group.memberCount }}人</span>
            <span v-if="group.description" class="dt-group-desc-text">{{ group.description }}</span>
          </div>
        </div>
        <svg class="dt-group-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M9 18l6-6-6-6" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGroups } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const emit = defineEmits(['select-group'])

const loading = ref(false)
const groups = ref([])

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroups()
    if (res.code === 200) {
      groups.value = res.data || []
    }
  } catch (e) {
    console.error('加载群组失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSelectGroup = (group) => {
  emit('select-group', group)
}

defineExpose({ loadGroups })

onMounted(() => {
  loadGroups()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.dingtalk-groups-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dt-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.dt-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--dt-border-light);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  .dark & {
    border-color: var(--dt-border-dark);
    border-top-color: var(--dt-brand-color);
  }
}

.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);

  svg {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    opacity: 0.2;
  }

  p {
    font-size: var(--dt-font-size-base);
    margin: 0;
    color: var(--dt-text-tertiary);
  }
}

.dt-groups-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
}

.dt-group-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #ffffff;
  border-radius: var(--dt-radius-md);
  border: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  &:hover {
    border-color: var(--dt-border-color);
    box-shadow: var(--dt-shadow-1);
    transform: translateX(2px);
  }

  &:active {
    transform: translateX(0);
  }
}

.dt-group-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.dt-group-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: 4px;

  @include text-ellipsis;
}

.dt-group-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dt-group-desc-text {
  @include text-ellipsis;

  &::before {
    content: '•';
    margin-right: 4px;
  }
}

.dt-group-arrow {
  width: 18px;
  height: 18px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
  transition: transform var(--dt-transition-fast);

  .dt-group-item:hover & {
    color: var(--dt-brand-color);
    transform: translateX(2px);
  }
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 768px) {
  .dt-groups-list {
    padding: 4px;
    gap: 4px;
  }

  .dt-group-item {
    padding: 10px;
  }
}
</style>
