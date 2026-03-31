<template>
  <el-drawer
    v-model="visible"
    size="320px"
    direction="rtl"
    :with-header="false"
    class="read-status-drawer"
  >
    <!-- Header区 -->
    <header class="drawer-header">
      <span class="title">消息接收详情</span>
      <el-icon class="close-icon" @click="visible = false"><Close /></el-icon>
    </header>

    <div class="drawer-content">
      <el-tabs v-model="activeTab" class="status-tabs">
        <el-tab-pane :label="`未读 (${unreadUsers.length})`" name="unread">
          <div class="user-list">
            <div v-for="user in unreadUsers" :key="user.userId" class="user-item">
              <img :src="user.avatar || '/avatars/default.png'" class="user-avatar" alt="avatar" />
              <span class="user-name">{{ user.nickname }}</span>
            </div>
            <div v-if="unreadUsers.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Select /></el-icon>
              <p class="empty-text">全部已读</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane :label="`已读 (${readUsers.length})`" name="read">
          <div class="user-list">
            <div v-for="user in readUsers" :key="user.userId" class="user-item">
              <img :src="user.avatar || '/avatars/default.png'" class="user-avatar" alt="avatar" />
              <div class="user-info">
                <span class="user-name">{{ user.nickname }}</span>
                <span class="read-time">{{ formatTime(user.readTime) }}</span>
              </div>
            </div>
            <div v-if="readUsers.length === 0" class="empty-state">
              <el-icon class="empty-icon"><ChatLineSquare /></el-icon>
              <p class="empty-text">暂无已读记录</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup lang="js">
/**
 * ReadStatusDrawer.vue (对齐钉钉 UI 规范)
 */
import { ref, computed, watch } from 'vue'
import { Close, Select, ChatLineSquare } from '@element-plus/icons-vue'
import { getMessageReadDetail } from '@/api/im/message'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  messageId: [String, Number]
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('unread')
const readUsers = ref([])
const unreadUsers = ref([])
const loading = ref(false)

const fetchData = async () => {
  if (!props.messageId) return
  loading.value = true
  try {
    const res = await getMessageReadDetail(props.messageId)
    if (res.code === 200 && res.data) {
      readUsers.value = res.data.readUsers || []
      unreadUsers.value = res.data.unreadUsers || []
    }
  } catch (e) {
    console.error('获取已读详情失败', e)
    ElMessage.error('获取已读详情失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.messageId, fetchData, { immediate: true })

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')} 已读`
}
</script>

<style scoped>
.drawer-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
  background-color: var(--dt-bg-card);
}

.title {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
}

.close-icon {
  cursor: pointer;
  color: var(--dt-text-tertiary);
  font-size: 18px;
  transition: color var(--dt-transition-fast);
}

.close-icon:hover {
  color: var(--dt-text-primary);
}

.drawer-content {
  height: calc(100% - 60px);
  background-color: var(--dt-bg-card);
}

/* Tabs 自定义样式对齐钉钉蓝 */
.status-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 20px;
}
.status-tabs :deep(.el-tabs__item.is-active) {
  color: var(--dt-brand-color);
}
.status-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--dt-brand-color);
}
.status-tabs :deep(.el-tabs__item:hover) {
  color: var(--dt-brand-color);
}

.user-list {
  padding: 8px 20px;
  height: 100%;
  overflow-y: auto;
}

.user-list::-webkit-scrollbar { width: 4px; }
.user-list::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.05); border-radius: 2px; }

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 56px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.user-item:last-child {
  border-bottom: none;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--dt-border-light);
}

.user-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  flex: 1;
  overflow: hidden;
}

.read-time {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 60px;
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: var(--dt-font-size-base);
}
</style>
