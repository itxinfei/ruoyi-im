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
  width: 24px;
  height: 24px;
  border: 2px solid #e5e6eb;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  .dark & {
    border-color: #3e3f42;
    border-top-color: #1890ff;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: 0.3;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

.dt-groups-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dt-group-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  cursor: pointer;
  transition: all 0.15s;

  .dark & {
    background: #2a2b2c;
    border-color: #3e3f42;
  }

  &:hover {
    background: #f5f5f5;

    .dark & {
      background: #3a3a3a;
    }
  }
}

.dt-group-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.dt-group-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  @include text-ellipsis;
}

.dt-group-desc {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dt-group-desc-text {
  @include text-ellipsis;
}

.dt-group-arrow {
  width: 16px;
  height: 16px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
}
</style>
