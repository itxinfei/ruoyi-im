<template>
  <div class="admin-page dashboard-page">
    <el-row :gutter="16" class="metrics-row">
      <el-col
        v-for="item in metrics"
        :key="item.key"
        :xs="24"
        :sm="12"
        :xl="6"
      >
        <el-card class="metric-card" shadow="never">
          <div class="metric-inner">
            <div class="metric-icon" :class="item.iconClass">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">
                {{ item.label }}
              </div>
              <div class="metric-value">
                {{ item.value }}
              </div>
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
              <el-button type="primary" link @click="loadMessageStats">
                刷新
              </el-button>
            </div>
          </template>

          <div v-loading="messageStatsLoading" class="panel-body">
            <template v-if="messageStats.totalMessages > 0">
              <div ref="messageChartRef" class="chart-container" />
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
              <el-button type="primary" link @click="loadUserStats">
                刷新
              </el-button>
            </div>
          </template>

          <div v-loading="userStatsLoading" class="panel-body">
            <template v-if="userStats.total > 0">
              <div ref="userChartRef" class="chart-container" />
            </template>
            <el-empty v-else description="暂无角色统计" :image-size="88" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Star, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'
import { getOverview, getUserStats, getMessageAdminStats } from '@/api/admin'
import * as echarts from 'echarts'
import { useTheme } from '@/composables/useTheme'

const { isDark } = useTheme()

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

const messageChartRef = ref(null)
const userChartRef = ref(null)
let messageChart = null
let userChart = null

const metrics = computed(() => ([
  { key: 'users', label: '总用户数', value: overview.value.totalUsers || 0, icon: User, iconClass: 'users' },
  { key: 'active', label: '活跃用户', value: overview.value.activeUsers || 0, icon: Star, iconClass: 'active' },
  { key: 'groups', label: '群组数', value: overview.value.totalGroups || 0, icon: ChatDotRound, iconClass: 'groups' },
  { key: 'today', label: '今日消息', value: overview.value.todayMessages || 0, icon: ChatLineSquare, iconClass: 'messages' }
]))

// 获取暗色模式下对应的颜色值
const getDarkModeColor = (lightVar, darkVar) => {
  return isDark.value ? darkVar : lightVar
}

const initMessageChart = () => {
  if (!messageChartRef.value) return
  if (messageChart) messageChart.dispose()
  messageChart = echarts.init(messageChartRef.value)
  updateMessageChart()
}

const updateMessageChart = () => {
  if (!messageChart) return
  const total = messageStats.value.totalMessages || 0
  const textColor = isDark.value ? '#f1f5f9' : '#5f6670'
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      textStyle: { color: textColor, fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: isDark.value ? '#1e293b' : '#ffffff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [
        { value: messageStats.value.textMessages || 0, name: '文本', itemStyle: { color: getDarkModeColor('#277EFB', '#4493FA') } },
        { value: messageStats.value.imageMessages || 0, name: '图片', itemStyle: { color: getDarkModeColor('#00B42A', '#22C55E') } },
        { value: messageStats.value.fileMessages || 0, name: '文件', itemStyle: { color: getDarkModeColor('#FF7D00', '#FB923C') } }
      ]
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: '38%',
      style: {
        text: total.toString(),
        textAlign: 'center',
        fill: isDark.value ? '#f1f5f9' : '#171a1d',
        fontSize: 24,
        fontWeight: 'bold'
      }
    }, {
      type: 'text',
      left: 'center',
      top: '52%',
      style: {
        text: '总消息数',
        textAlign: 'center',
        fill: isDark.value ? '#64748b' : '#8a9099',
        fontSize: 12
      }
    }]
  }
  messageChart.setOption(option)
}

const initUserChart = () => {
  if (!userChartRef.value) return
  if (userChart) userChart.dispose()
  userChart = echarts.init(userChartRef.value)
  updateUserChart()
}

const updateUserChart = () => {
  if (!userChart) return
  const textColor = isDark.value ? '#f1f5f9' : '#5f6670'
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      textStyle: { color: textColor, fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: isDark.value ? '#1e293b' : '#ffffff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [
        { value: userStats.value.superAdminCount || 0, name: '超级管理员', itemStyle: { color: getDarkModeColor('#F53F3F', '#F87171') } },
        { value: userStats.value.adminCount || 0, name: '管理员', itemStyle: { color: getDarkModeColor('#FF7D00', '#FB923C') } },
        { value: userStats.value.userCount || 0, name: '普通用户', itemStyle: { color: getDarkModeColor('#8a9099', '#64748b') } }
      ]
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: '38%',
      style: {
        text: (userStats.value.total || 0).toString(),
        textAlign: 'center',
        fill: isDark.value ? '#f1f5f9' : '#171a1d',
        fontSize: 24,
        fontWeight: 'bold'
      }
    }, {
      type: 'text',
      left: 'center',
      top: '52%',
      style: {
        text: '总用户数',
        textAlign: 'center',
        fill: isDark.value ? '#64748b' : '#8a9099',
        fontSize: 12
      }
    }]
  }
  userChart.setOption(option)
}

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
    await nextTick()
    updateMessageChart()
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
    await nextTick()
    updateUserChart()
  } catch (error) {
    ElMessage.error('加载用户统计失败')
  } finally {
    userStatsLoading.value = false
  }
}

// 监听暗色模式变化，更新图表
watch(isDark, async () => {
  await nextTick()
  updateMessageChart()
  updateUserChart()
})

onMounted(async () => {
  await Promise.all([loadOverview(), loadMessageStats(), loadUserStats()])
  await nextTick()
  initMessageChart()
  initUserChart()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-lg);
}

.metric-card,
.panel {
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
}

.metric-inner {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-lg);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-primary);
  font-size: 20px;
}

.metric-icon.users { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
.metric-icon.active { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
.metric-icon.groups { background: var(--dt-info-bg); color: var(--dt-info-color); }
.metric-icon.messages { background: var(--dt-success-bg); color: var(--dt-success-color); }

.metric-label {
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

.metric-value {
  margin-top: var(--dt-spacing-xs);
  color: var(--dt-text-primary);
  font-size: 28px;
  font-weight: var(--dt-font-weight-bold);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  color: var(--dt-text-primary);
  font-size: var(--dt-font-size-lg);
}

.panel-header p {
  margin: var(--dt-spacing-xs) 0 0;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
}

.panel-body {
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-container {
  width: 100%;
  height: 260px;
}
</style>
