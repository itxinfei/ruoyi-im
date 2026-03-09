<template>
  <div class="admin-page dashboard-page">
    <el-row :gutter="16" class="metrics-row">
      <el-col :xs="24" :sm="12" :xl="6" v-for="item in metrics" :key="item.key">
        <el-card class="metric-card" shadow="never">
          <div class="metric-inner">
            <div class="metric-icon" :class="item.iconClass">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">{{ item.label }}</div>
              <div class="metric-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :xl="12">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>消息统计（近7天）</h3>
                <p>按消息类型查看结构占比</p>
              </div>
              <el-button type="primary" link @click="loadMessageStats">刷新</el-button>
            </div>
          </template>

          <div v-loading="messageStatsLoading" class="panel-body">
            <template v-if="messageStats.totalMessages > 0">
              <div class="total-line">
                <span>总消息数</span>
                <strong>{{ messageStats.totalMessages }}</strong>
              </div>
              <div class="bars">
                <div class="bar-item" v-for="item in messageBars" :key="item.key">
                  <span class="bar-name">{{ item.label }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :class="item.className" :style="{ width: item.percent + '%' }"></div>
                  </div>
                  <span class="bar-value">{{ item.value }}</span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无消息统计" :image-size="88" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="12">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <h3>角色分布</h3>
                <p>超级管理员 / 管理员 / 普通用户</p>
              </div>
              <el-button type="primary" link @click="loadUserStats">刷新</el-button>
            </div>
          </template>

          <div v-loading="userStatsLoading" class="panel-body">
            <template v-if="userStats.total > 0">
              <div class="legend-list">
                <div class="legend-item">
                  <span class="dot super"></span>
                  <span>超级管理员</span>
                  <strong>{{ userStats.superAdminCount || 0 }}</strong>
                </div>
                <div class="legend-item">
                  <span class="dot admin"></span>
                  <span>管理员</span>
                  <strong>{{ userStats.adminCount || 0 }}</strong>
                </div>
                <div class="legend-item">
                  <span class="dot user"></span>
                  <span>普通用户</span>
                  <strong>{{ userStats.userCount || 0 }}</strong>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无角色统计" :image-size="88" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Star, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'
import { getOverview, getUserStats, getMessageAdminStats } from '@/api/admin'

const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalGroups: 0,
  todayMessages: 0
})

const messageStatsLoading = ref(false)
const messageStats = ref({
  totalMessages: 0,
  textMessages: 0,
  imageMessages: 0,
  fileMessages: 0
})

const userStatsLoading = ref(false)
const userStats = ref({
  total: 0,
  superAdminCount: 0,
  adminCount: 0,
  userCount: 0
})

const metrics = computed(() => ([
  { key: 'users', label: '总用户数', value: overview.value.totalUsers || 0, icon: User, iconClass: 'users' },
  { key: 'active', label: '活跃用户', value: overview.value.activeUsers || 0, icon: Star, iconClass: 'active' },
  { key: 'groups', label: '群组数', value: overview.value.totalGroups || 0, icon: ChatDotRound, iconClass: 'groups' },
  { key: 'today', label: '今日消息', value: overview.value.todayMessages || 0, icon: ChatLineSquare, iconClass: 'messages' }
]))

const messageBars = computed(() => {
  const total = messageStats.value.totalMessages || 0
  const make = (key, label, value, className) => ({
    key,
    label,
    value,
    className,
    percent: total > 0 ? Math.round((value / total) * 100) : 0
  })
  return [
    make('text', '文本', messageStats.value.textMessages || 0, 'text'),
    make('image', '图片', messageStats.value.imageMessages || 0, 'image'),
    make('file', '文件', messageStats.value.fileMessages || 0, 'file')
  ]
})

const loadOverview = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200) overview.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载概览数据失败')
  }
}

const loadMessageStats = async () => {
  messageStatsLoading.value = true
  try {
    const res = await getMessageAdminStats({})
    if (res.code === 200) messageStats.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载消息统计失败')
  } finally {
    messageStatsLoading.value = false
  }
}

const loadUserStats = async () => {
  userStatsLoading.value = true
  try {
    const res = await getUserStats()
    if (res.code === 200) {
      userStats.value = {
        total: res.data.total || 0,
        superAdminCount: res.data.superAdminCount || 0,
        adminCount: res.data.adminCount || 0,
        userCount: res.data.userCount || (res.data.total || 0)
      }
    }
  } catch (error) {
    ElMessage.error('加载用户统计失败')
  } finally {
    userStatsLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadMessageStats(), loadUserStats()])
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-card,
.panel {
  border-radius: 12px;
  border: 1px solid var(--dt-border-light);
}

.metric-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-primary);
  font-size: 18px;
}

.metric-icon.users { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
.metric-icon.active { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
.metric-icon.groups { background: var(--dt-info-bg); color: var(--dt-info-color); }
.metric-icon.messages { background: var(--dt-success-bg); color: var(--dt-success-color); }

.metric-label {
  color: var(--dt-text-tertiary);
  font-size: 12px;
}

.metric-value {
  margin-top: 4px;
  color: var(--dt-text-primary);
  font-size: 26px;
  font-weight: 700;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  color: var(--dt-text-primary);
  font-size: 16px;
}

.panel-header p {
  margin: 4px 0 0;
  color: var(--dt-text-tertiary);
  font-size: 12px;
}

.panel-body {
  min-height: 220px;
}

.total-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--dt-border-light);
  color: var(--dt-text-secondary);
}

.total-line strong {
  color: var(--dt-text-primary);
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-item {
  display: grid;
  grid-template-columns: 48px 1fr 56px;
  align-items: center;
  gap: 10px;
}

.bar-track {
  height: 12px;
  border-radius: 99px;
  background: var(--dt-bg-body);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 99px;
}

.bar-fill.text { background: var(--dt-brand-color); }
.bar-fill.image { background: var(--dt-success-color); }
.bar-fill.file { background: var(--dt-warning-color); }

.bar-name,
.bar-value {
  color: var(--dt-text-tertiary);
  font-size: 13px;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.super { background: var(--dt-error-color); }
.dot.admin { background: var(--dt-warning-color); }
.dot.user { background: var(--dt-text-tertiary); }

.legend-item span {
  color: var(--dt-text-secondary);
}

.legend-item strong {
  color: var(--dt-text-primary);
}
</style>
