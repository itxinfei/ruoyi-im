<template>
  <div class="groups-view">
    <header class="view-header">
      <button class="back-btn" @click="$emit('back')">
        <span class="material-icons-outlined">arrow_back</span>
      </button>
      <h1 class="view-title">我的群组</h1>
    </header>

    <div class="view-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="groups.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">groups</span>
        <p class="empty-text">暂无群组</p>
      </div>

      <div v-else class="groups-list">
        <div
          v-for="group in groups"
          :key="group.id"
          class="group-item"
          @click="handleSelectGroup(group)"
        >
          <div class="group-avatar">
            <img v-if="group.avatar" :src="group.avatar" :alt="`${group.name} 的群头像`" />
            <span v-else class="material-icons-outlined" aria-hidden="true">group</span>
          </div>
          <div class="group-info">
            <div class="group-name">{{ group.name }}</div>
            <div class="group-desc">
                <span v-if="group.memberCount">{{ group.memberCount }} 人</span> 
                <span v-if="group.description">{{ group.description }}</span>     
              </div>
          </div>
          <div class="group-action">
            <span class="material-icons-outlined">chevron_right</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getGroups } from '@/api/im/group'

const emit = defineEmits(['back', 'select-group'])

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
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSelectGroup = (group) => {
  emit('select-group', group)
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
.groups-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f4f7f9;
}

.view-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
}

.back-btn {
  background: none;
  border: none;
  padding: 4px;
  margin-right: 8px;
  cursor: pointer;
  color: #595959;
  display: flex;
  align-items: center;
}

.back-btn:hover {
  color: #1677ff;
}

.view-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.view-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #8c8c8c;
  gap: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
}

.group-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.group-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.group-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-avatar .material-icons-outlined {
  font-size: 24px;
}

.group-info {
  flex: 1;
  min-width: 0;
}

.group-name {
  font-size: 15px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.group-desc {
  font-size: 13px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-action {
  color: #bfbfbf;
  flex-shrink: 0;
}

/* 暗色模式 */
:deep(.dark) .view-header,
:deep(.dark) .group-item {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .view-title,
:deep(.dark) .group-name {
  color: #f1f5f9;
}

:deep(.dark) .group-desc {
  color: #94a3b8;
}

:deep(.dark) .group-action {
  color: #475569;
}
</style>
