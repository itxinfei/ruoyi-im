<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon active-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon group-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalGroups || 0 }}</div>
              <div class="stat-label">群组数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon message-icon">
              <el-icon><ChatLineSquare /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.todayMessages || 0 }}</div>
              <div class="stat-label">今日消息</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 消息统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>消息统计（近7天）</span>
              <el-button type="primary" link @click="loadMessageStats">刷新</el-button>
            </div>
          </template>
          <div v-loading="messageStatsLoading" class="chart-container">
            <div v-if="messageStats.totalMessages > 0" class="message-stats">
              <div class="stat-row">
                <span class="stat-label">总消息</span>
                <span class="stat-value">{{ messageStats.totalMessages }}</span>
              </div>
              <div class="message-bar-chart">
                <div class="bar-item">
                  <div class="bar-label">文本</div>
                  <div class="bar-track">
                    <div class="bar-fill text-bar" :style="{ width: getTextPercent + '%' }"></div>
                  </div>
                  <div class="bar-value">{{ messageStats.textMessages }}</div>
                </div>
                <div class="bar-item">
                  <div class="bar-label">图片</div>
                  <div class="bar-track">
                    <div class="bar-fill image-bar" :style="{ width: getImagePercent + '%' }"></div>
                  </div>
                  <div class="bar-value">{{ messageStats.imageMessages }}</div>
                </div>
                <div class="bar-item">
                  <div class="bar-label">文件</div>
                  <div class="bar-track">
                    <div class="bar-fill file-bar" :style="{ width: getFilePercent + '%' }"></div>
                  </div>
                  <div class="bar-value">{{ messageStats.fileMessages }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <!-- 用户角色分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户角色分布</span>
              <el-button type="primary" link @click="loadUserStats">刷新</el-button>
            </div>
          </template>
          <div v-loading="userStatsLoading" class="chart-container">
            <div v-if="userStats.total > 0" class="role-stats">
              <div class="pie-chart-wrapper">
                <svg class="pie-chart" viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#e5e7eb" stroke-width="20" />
                  <circle
                    cx="50" cy="50" r="40"
                    fill="none"
                    stroke="#f56c6c"
                    stroke-width="20"
                    :stroke-dasharray="superAdminCircumference + ' ' + (251.32 - superAdminCircumference)"
                    stroke-dashoffset="0"
                    transform="rotate(-90 50 50)"
                  />
                  <circle
                    cx="50" cy="50" r="40"
                    fill="none"
                    stroke="#e6a23c"
                    stroke-width="20"
                    :stroke-dasharray="adminCircumference + ' ' + (251.32 - adminCircumference)"
                    :stroke-dashoffset="(-superAdminCircumference) + 'px'"
                    transform="rotate(-90 50 50)"
                  />
                  <circle
                    cx="50" cy="50" r="40"
                    fill="none"
                    stroke="#909399"
                    stroke-width="20"
                    :stroke-dasharray="userCircumference + ' ' + (251.32 - userCircumference)"
                    :stroke-dashoffset="(-(superAdminCircumference + adminCircumference)) + 'px'"
                    transform="rotate(-90 50 50)"
                  />
                </svg>
              </div>
              <div class="legend">
                <div class="legend-item">
                  <div class="legend-dot super-admin"></div>
                  <span>超级管理员: {{ userStats.superAdminCount || 0 }}</span>
                </div>
                <div class="legend-item">
                  <div class="legend-dot admin"></div>
                  <span>管理员: {{ userStats.adminCount || 0 }}</span>
                </div>
                <div class="legend-item">
                  <div class="legend-dot user"></div>
                  <span>普通用户: {{ userStats.userCount || 0 }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Star, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'
import { getOverview, getUserStats, getMessageAdminStats } from '@/api/admin'

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
  if (messageStats.value.totalMessages === 0) return 0
  return Math.round((messageStats.value.textMessages / messageStats.value.totalMessages) * 100)
})

const getImagePercent = computed(() => {
  if (messageStats.value.totalMessages === 0) return 0
  return Math.round((messageStats.value.imageMessages / messageStats.value.totalMessages) * 100)
})

const getFilePercent = computed(() => {
  if (messageStats.value.totalMessages === 0) return 0
  return Math.round((messageStats.value.fileMessages / messageStats.value.totalMessages) * 100)
})

// 饼图周长计算 (2 * PI * r = 2 * 3.14159 * 40 ≈ 251.32)
const superAdminCircumference = computed(() => {
  if (userStats.value.total === 0) return 0
  return (userStats.value.superAdminCount / userStats.value.total) * 251.32
})

const adminCircumference = computed(() => {
  if (userStats.value.total === 0) return 0
  return (userStats.value.adminCount / userStats.value.total) * 251.32
})

const userCircumference = computed(() => {
  if (userStats.value.total === 0) return 0
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
    const res = await getMessageAdminStats({})
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
        superAdminCount: 0, // 需要从后端获取具体角色统计
        adminCount: 0,
        userCount: res.data.total || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载用户统计失败')
  } finally {
    userStatsLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadMessageStats(), loadUserStats()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.active-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.group-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.message-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  margin-left: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.chart-card {
  height: 320px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-stats {
  width: 100%;
  padding: 10px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.stat-row .stat-label {
  color: #666;
  font-size: 14px;
}

.stat-row .stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.message-bar-chart {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  width: 50px;
  font-size: 14px;
  color: #666;
}

.bar-track {
  flex: 1;
  height: 24px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.text-bar {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
}

.image-bar {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.file-bar {
  background: linear-gradient(90deg, #e6a23c 0%, #f0c78a 100%);
}

.bar-value {
  width: 50px;
  text-align: right;
  font-size: 14px;
  color: #666;
}

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
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.super-admin {
  background-color: #f56c6c;
}

.legend-dot.admin {
  background-color: #e6a23c;
}

.legend-dot.user {
  background-color: #909399;
}

.loading-container {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}
</style>
