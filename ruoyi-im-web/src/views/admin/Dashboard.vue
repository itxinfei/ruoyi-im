<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <StatCard
          title="总用户数"
          :value="overview.totalUsers"
          :icon="User"
          icon-color="#409eff"
          :loading="loading"
          clickable
          @click="handleUsersClick"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          title="活跃用户"
          :value="overview.activeUsers"
          :icon="Star"
          icon-color="#67c23a"
          :loading="loading"
          clickable
          @click="handleUsersClick"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          title="群组数"
          :value="overview.totalGroups"
          :icon="ChatDotRound"
          icon-color="#e6a23c"
          :loading="loading"
          clickable
          @click="handleGroupsClick"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          title="今日消息"
          :value="overview.todayMessages"
          :icon="ChatLineSquare"
          icon-color="#f56c6c"
          :loading="loading"
          clickable
          @click="handleMessagesClick"
        />
      </el-col>
    </el-row>

    <!-- 数据图表区域 -->
    <el-row
      :gutter="16"
      class="chart-row"
    >
      <!-- 消息统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">消息统计（近7天）</span>
              <el-button
                type="primary"
                link
                @click="loadMessageStats"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div
            v-loading="messageStatsLoading"
            class="chart-container"
          >
            <div
              v-if="messageStats.totalMessages > 0"
              class="message-stats"
            >
              <div class="stat-row">
                <span class="stat-row-label">总消息</span>
                <span class="stat-row-value">{{ messageStats.totalMessages }}</span>
              </div>
              <div class="message-bar-chart">
                <div class="bar-item">
                  <div class="bar-label">
                    文本
                  </div>
                  <div class="bar-track">
                    <div
                      class="bar-fill bar-fill--primary"
                      :style="{ width: getTextPercent + '%' }"
                    />
                  </div>
                  <div class="bar-value">
                    {{ messageStats.textMessages }}
                  </div>
                </div>
                <div class="bar-item">
                  <div class="bar-label">
                    图片
                  </div>
                  <div class="bar-track">
                    <div
                      class="bar-fill bar-fill--success"
                      :style="{ width: getImagePercent + '%' }"
                    />
                  </div>
                  <div class="bar-value">
                    {{ messageStats.imageMessages }}
                  </div>
                </div>
                <div class="bar-item">
                  <div class="bar-label">
                    文件
                  </div>
                  <div class="bar-track">
                    <div
                      class="bar-fill bar-fill--warning"
                      :style="{ width: getFilePercent + '%' }"
                    />
                  </div>
                  <div class="bar-value">
                    {{ messageStats.fileMessages }}
                  </div>
                </div>
              </div>
            </div>
            <el-empty
              v-else
              description="暂无数据"
              :image-size="80"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 用户角色分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户角色分布</span>
              <el-button
                type="primary"
                link
                @click="loadUserStats"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div
            v-loading="userStatsLoading"
            class="chart-container"
          >
            <div
              v-if="userStats.total > 0"
              class="role-stats"
            >
              <div class="pie-chart-wrapper">
                <svg
                  class="pie-chart"
                  viewBox="0 0 100 100"
                >
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    fill="none"
                    stroke="var(--dt-border-lighter)"
                    stroke-width="20"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    fill="none"
                    stroke="var(--dt-error)"
                    stroke-width="20"
                    :stroke-dasharray="superAdminCircumference + ' ' + (251.32 - superAdminCircumference)"
                    stroke-dashoffset="0"
                    transform="rotate(-90 50 50)"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    fill="none"
                    stroke="var(--dt-warning)"
                    stroke-width="20"
                    :stroke-dasharray="adminCircumference + ' ' + (251.32 - adminCircumference)"
                    :stroke-dashoffset="(-superAdminCircumference) + 'px'"
                    transform="rotate(-90 50 50)"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    fill="none"
                    stroke="var(--dt-text-secondary)"
                    stroke-width="20"
                    :stroke-dasharray="userCircumference + ' ' + (251.32 - userCircumference)"
                    :stroke-dashoffset="(-(superAdminCircumference + adminCircumference)) + 'px'"
                    transform="rotate(-90 50 50)"
                  />
                </svg>
              </div>
              <div class="legend">
                <div class="legend-item">
                  <div class="legend-dot legend-dot--super" />
                  <span>超级管理员: {{ userStats.superAdminCount || 0 }}</span>
                </div>
                <div class="legend-item">
                  <div class="legend-dot legend-dot--admin" />
                  <span>管理员: {{ userStats.adminCount || 0 }}</span>
                </div>
                <div class="legend-item">
                  <div class="legend-dot legend-dot--user" />
                  <span>普通用户: {{ userStats.userCount || 0 }}</span>
                </div>
              </div>
            </div>
            <el-empty
              v-else
              description="暂无数据"
              :image-size="80"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 加载状态 -->
    <div
      v-if="loading"
      class="loading-container"
    >
      <el-skeleton
        :rows="5"
        animated
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, ChatDotRound, ChatLineSquare, Star, Refresh } from '@element-plus/icons-vue'
import { getOverview, getUserStats, getMessageAdminStats } from '@/api/admin'
import { useRouter } from 'vue-router'
import StatCard from '@/components/admin/StatCard.vue'

const router = useRouter()
const loading = ref(false)
const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalGroups: 0,
  totalMessages: 0,
  todayMessages: 0
})

// 消息统计
const messageStatsLoading = ref(false)
const messageStats = ref({
  totalMessages: 0,
  textMessages: 0,
  imageMessages: 0,
  fileMessages: 0
})

// 用户统计
const userStatsLoading = ref(false)
const userStats = ref({
  total: 0,
  superAdminCount: 0,
  adminCount: 0,
  userCount: 0
})

const getTextPercent = computed(() => {
  if (messageStats.value.totalMessages === 0) {return 0}
  return Math.round((messageStats.value.textMessages / messageStats.value.totalMessages) * 100)
})

const getImagePercent = computed(() => {
  if (messageStats.value.totalMessages === 0) {return 0}
  return Math.round((messageStats.value.imageMessages / messageStats.value.totalMessages) * 100)
})

const getFilePercent = computed(() => {
  if (messageStats.value.totalMessages === 0) {return 0}
  return Math.round((messageStats.value.fileMessages / messageStats.value.totalMessages) * 100)
})

// 饼图周长计算 (2 * PI * r = 2 * 3.14159 * 40 ≈ 251.32)
const superAdminCircumference = computed(() => {
  if (userStats.value.total === 0) {return 0}
  return (userStats.value.superAdminCount / userStats.value.total) * 251.32
})

const adminCircumference = computed(() => {
  if (userStats.value.total === 0) {return 0}
  return (userStats.value.adminCount / userStats.value.total) * 251.32
})

const userCircumference = computed(() => {
  if (userStats.value.total === 0) {return 0}
  return (userStats.value.userCount / userStats.value.total) * 251.32
})

const loadOverview = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
}

const loadMessageStats = async () => {
  messageStatsLoading.value = true
  try {
    // 获取近7天的消息统计
    const res = await getMessageAdminStats({ days: 7 })
    if (res.code === 200) {
      messageStats.value = res.data
    }
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
        userCount: res.data.userCount || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载用户统计失败')
  } finally {
    userStatsLoading.value = false
  }
}

// 统计卡片点击跳转
const navigateTo = path => {
  router.push(path)
}

const handleUsersClick = () => navigateTo('/admin/users')
const handleGroupsClick = () => navigateTo('/admin/groups')
const handleMessagesClick = () => navigateTo('/admin/messages')

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadMessageStats(), loadUserStats()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">


/* ================================
   页面容器
   ================================ */
.dashboard {
  padding: 0;
}

.chart-row {
  margin-top: var(--dt-space-md);
}

/* ================================
   图表卡片
   ================================ */
.chart-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  height: 320px;
}

.chart-card :deep(.el-card__header) {
  padding: var(--dt-space-sm) var(--dt-space-md);
  border-bottom: 1px solid var(--dt-divider);
}

.chart-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.chart-container {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ================================
   消息统计
   ================================ */
.message-stats {
  width: 100%;
  padding: var(--dt-space-sm);
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--dt-space-md);
  padding-bottom: var(--dt-space-sm);
  border-bottom: 1px solid var(--dt-divider);
}

.stat-row-label {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-regular);
}

.stat-row-value {
  font-size: 24px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-primary);
}

.message-bar-chart {
  display: flex;
  flex-direction: column;
  gap: var(--dt-space-sm);
}

.bar-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.bar-label {
  width: 40px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

.bar-track {
  flex: 1;
  height: 24px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: var(--dt-radius-sm);
  transition: width var(--dt-transition-slow) var(--dt-ease-out);
}

.bar-fill--primary {
  background: var(--dt-primary-gradient);
}

.bar-fill--success {
  background: linear-gradient(90deg, var(--dt-success) 0%, var(--dt-success-light) 100%);
}

.bar-fill--warning {
  background: linear-gradient(90deg, var(--dt-warning) 0%, var(--dt-warning-light) 100%);
}

.bar-value {
  width: 50px;
  text-align: right;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-medium);
}

/* ================================
   角色分布饼图
   ================================ */
.role-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  width: 100%;
}

.pie-chart-wrapper {
  position: relative;
}

.pie-chart {
  width: 150px;
  height: 150px;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: var(--dt-space-sm);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-regular);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: var(--dt-radius-round);
  flex-shrink: 0;
}

.legend-dot--super {
  background-color: var(--dt-error);
}

.legend-dot--admin {
  background-color: var(--dt-warning);
}

.legend-dot--user {
  background-color: var(--dt-text-secondary);
}

/* ================================
   加载容器
   ================================ */
.loading-container {
  margin-top: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-card);
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

/* ================================
   Element Plus 样式覆盖
   ================================ */

/* 按钮 link 样式 */
:deep(.el-button--link.el-button--primary) {
  color: var(--dt-primary);
}

:deep(.el-button--link.el-button--primary:hover) {
  color: var(--dt-primary-light);
}

/* 空状态样式 */
:deep(.el-empty) {
  --el-empty-description-color: var(--dt-text-placeholder);
}

/* Loading 样式 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.9);
}

[data-theme='dark'] :deep(.el-loading-mask) {
  background-color: rgba(42, 42, 42, 0.9);
}
</style>
