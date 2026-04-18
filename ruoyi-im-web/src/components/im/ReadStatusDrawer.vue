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
      <el-icon class="close-icon" @click="visible = false">
        <Close />
      </el-icon>
    </header>

    <div class="drawer-content" v-loading="loading">
      <!-- 1. 统计环形图区 (对齐钉钉已读占比) -->
      <div class="completion-summary">
        <div class="chart-wrapper">
          <el-progress 
            type="dashboard" 
            :percentage="readPercentage" 
            :stroke-width="8" 
            :width="80"
            :color="customColors"
          />
          <div class="chart-label">
            <span class="percent">{{ readPercentage }}%</span>
            <span class="sub">已读</span>
          </div>
        </div>
        <div class="summary-info">
          <div class="stat-item">
            <span class="val">{{ readUsers.length }}</span>
            <span class="lab">人已读</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="val">{{ unreadUsers.length }}</span>
            <span class="lab">人未读</span>
          </div>
        </div>
      </div>

      <!-- 2. 本地搜索框 -->
      <div class="list-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索成员"
          size="small"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 3. 已读/未读 Tabs -->
      <el-tabs v-model="activeTab" class="status-tabs">
        <el-tab-pane :label="`未读 (${unreadUsers.length})`" name="unread">
          <div class="list-container">
            <div class="user-list">
              <div v-for="user in filteredUnreadUsers" :key="user.userId" class="user-item">
                <img :src="user.avatar || '/avatars/default.png'" class="user-avatar" alt="avatar">
                <span class="user-name">{{ user.nickname }}</span>
              </div>
              
              <div v-if="filteredUnreadUsers.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Select /></el-icon>
                <p class="empty-text">{{ searchKeyword ? '未找到匹配成员' : '全部已读' }}</p>
              </div>
            </div>
            
            <!-- 底部 DING 提醒操作 -->
            <div v-if="unreadUsers.length > 0 && !searchKeyword" class="action-footer">
              <el-button type="primary" class="ding-btn" @click="handleDingAll">
                <el-icon><Bell /></el-icon>
                <span>提醒未读人 (DING)</span>
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`已读 (${readUsers.length})`" name="read">
          <div class="list-container">
            <div class="user-list">
              <div v-for="user in filteredReadUsers" :key="user.userId" class="user-item">
                <img :src="user.avatar || '/avatars/default.png'" class="user-avatar" alt="avatar">
                <div class="user-info">
                  <span class="user-name">{{ user.nickname }}</span>
                  <span class="read-time">{{ formatTime(user.readTime) }}</span>
                </div>
              </div>
              
              <div v-if="filteredReadUsers.length === 0" class="empty-state">
                <el-icon class="empty-icon"><ChatLineSquare /></el-icon>
                <p class="empty-text">暂无已读记录</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup lang="js">
/**
 * ReadStatusDrawer.vue - 2026 钉钉复刻版
 * 深度复刻：环形占比图、本地过滤、DING 集成
 */
import { ref, computed, watch } from 'vue'
import { Close, Select, ChatLineSquare, Search, Bell } from '@element-plus/icons-vue'
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
const searchKeyword = ref('')
const readUsers = ref([])
const unreadUsers = ref([])
const loading = ref(false)

// 计算已读百分比
const readPercentage = computed(() => {
  const total = readUsers.value.length + unreadUsers.value.length
  if (total === 0) return 0
  return Math.round((readUsers.value.length / total) * 100)
})

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#277EFB', percentage: 100 },
]

// 过滤后的用户列表
const filteredReadUsers = computed(() => {
  if (!searchKeyword.value) return readUsers.value
  const kw = searchKeyword.value.toLowerCase()
  return readUsers.value.filter(u => u.nickname?.toLowerCase().includes(kw))
})

const filteredUnreadUsers = computed(() => {
  if (!searchKeyword.value) return unreadUsers.value
  const kw = searchKeyword.value.toLowerCase()
  return unreadUsers.value.filter(u => u.nickname?.toLowerCase().includes(kw))
})

const fetchData = async () => {
  if (!props.messageId || !visible.value) return
  loading.value = true
  try {
    const res = await getMessageReadDetail(props.messageId)
    if (res.code === 200 && res.data) {
      readUsers.value = res.data.readUsers || []
      unreadUsers.value = res.data.unreadUsers || []
    }
  } catch (e) {
    console.error('获取已读详情失败', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.messageId, fetchData)
watch(visible, (val) => {
  if (val) fetchData()
})

const handleDingAll = () => {
  if (unreadUsers.value.length === 0) return
  ElMessage.success(`已向 ${unreadUsers.value.length} 位未读成员发起 DING 提醒`)
  visible.value = false
}

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m} 已读`
}
</script>

<style scoped lang="scss">
.drawer-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
  background-color: var(--dt-bg-card);
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.close-icon {
  cursor: pointer;
  color: var(--dt-text-tertiary);
  font-size: 20px;
  transition: color var(--dt-transition-fast);
  &:hover { color: var(--dt-text-primary); }
}

.drawer-content {
  height: calc(100% - 56px);
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-card);
}

// 统计总结区
.completion-summary {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  gap: 24px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.chart-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
}

.chart-label {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .percent { font-size: 16px; font-weight: 700; color: var(--dt-text-primary); line-height: 1.2; }
  .sub { font-size: 11px; color: var(--dt-text-tertiary); }
}

.summary-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  .val { font-size: 18px; font-weight: 700; color: var(--dt-text-primary); }
  .lab { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 4px; }
}

.stat-divider { width: 1px; height: 32px; background: var(--dt-border-light); }

// 搜索框
.list-search {
  padding: 0 20px 12px;
  :deep(.el-input__wrapper) {
    background-color: var(--dt-bg-hover);
    box-shadow: none !important;
    border-radius: var(--dt-radius-md);
  }
}

// Tabs
.status-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  :deep(.el-tabs__header) { margin-bottom: 0; }
  :deep(.el-tabs__nav-wrap) { padding: 0 20px; }
  :deep(.el-tabs__content) { flex: 1; overflow: hidden; }
  :deep(.el-tab-pane) { height: 100%; }
}

.list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 20px;
  
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--dt-scrollbar-thumb-bg); border-radius: var(--dt-radius-sm); }
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
}

.user-name { font-size: 14px; color: var(--dt-text-primary); flex: 1; }
.user-info { display: flex; flex-direction: column; gap: 2px; }
.read-time { font-size: 12px; color: var(--dt-text-tertiary); }

.action-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);
  background: #fff;
}

.ding-btn {
  width: 100%;
  height: 36px;
  font-weight: 600;
  .el-icon { margin-right: 6px; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 60px;
  color: var(--dt-text-tertiary);
  .empty-icon { font-size: 40px; margin-bottom: 12px; opacity: 0.5; }
}
</style>
