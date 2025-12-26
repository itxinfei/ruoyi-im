<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>在线用户</span>
              <el-tooltip content="当前在线的用户数量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper">
              <svg-icon icon-class="user" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">在线用户数</div>
              <count-to
                :start-val="0"
                :end-val="stats.onlineUsers"
                :duration="2000"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>消息统计</span>
              <el-tooltip content="今日消息总量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper" style="background: #36a3f7">
              <svg-icon icon-class="message" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">今日消息量</div>
              <count-to
                :start-val="0"
                :end-val="stats.todayMessages"
                :duration="2000"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>群组统计</span>
              <el-tooltip content="当前活跃的群组数量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper" style="background: #34bfa3">
              <svg-icon icon-class="peoples" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">活跃群组数</div>
              <count-to
                :start-val="0"
                :end-val="stats.activeGroups"
                :duration="2000"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>系统状态</span>
              <el-tooltip content="系统整体运行状态" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper" :style="{ background: getStatusColor() }">
              <svg-icon icon-class="monitor" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">系统状态</div>
              <div class="card-panel-status">{{ getStatusText() }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性能监控 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>消息吞吐量</span>
              <el-radio-group v-model="messageChartType" size="mini" style="float: right">
                <el-radio-button label="realtime">实时</el-radio-button>
                <el-radio-button label="hour">小时</el-radio-button>
                <el-radio-button label="day">天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <line-chart :chart-data="messageChartData"></line-chart>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>系统资源使用率</span>
              <el-radio-group v-model="resourceChartType" size="mini" style="float: right">
                <el-radio-button label="realtime">实时</el-radio-button>
                <el-radio-button label="hour">小时</el-radio-button>
                <el-radio-button label="day">天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <line-chart :chart-data="resourceChartData"></line-chart>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警和性能指标 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>最新告警</span>
              <el-button style="float: right; padding: 3px 0" type="text" @click="handleViewAlerts"
                >查看更多</el-button
              >
            </div>
          </template>
          <el-table :data="alertList" style="width: 100%" size="small">
            <el-table-column prop="alertTime" label="时间" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.alertTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="级别" width="100">
              <template #default="scope">
                <el-tag :type="getAlertLevelType(scope.row.level)">{{ scope.row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="告警内容" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>性能指标</span>
              <el-tooltip content="系统关键性能指标" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="metrics-panel">
            <div class="metric-item">
              <div class="metric-label">消息延迟</div>
              <div class="metric-value">{{ metrics.messageDelay }}ms</div>
              <el-progress
                :percentage="getDelayPercentage(metrics.messageDelay)"
                :status="getDelayStatus(metrics.messageDelay)"
              ></el-progress>
            </div>
            <div class="metric-item">
              <div class="metric-label">并发连接数</div>
              <div class="metric-value">{{ metrics.connections }}</div>
              <el-progress
                :percentage="getConnectionPercentage(metrics.connections)"
                :status="getConnectionStatus(metrics.connections)"
              ></el-progress>
            </div>
            <div class="metric-item">
              <div class="metric-label">消息队列积压</div>
              <div class="metric-value">{{ metrics.queueBacklog }}</div>
              <el-progress
                :percentage="getQueuePercentage(metrics.queueBacklog)"
                :status="getQueueStatus(metrics.queueBacklog)"
              ></el-progress>
            </div>
            <div class="metric-item">
              <div class="metric-label">系统负载</div>
              <div class="metric-value">{{ metrics.systemLoad }}</div>
              <el-progress
                :percentage="getLoadPercentage(metrics.systemLoad)"
                :status="getLoadStatus(metrics.systemLoad)"
              ></el-progress>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboardData, getPerformanceMetrics } from '@/api/im/dashboard'
import CountTo from 'vue-count-to'
import LineChart from './components/LineChart'

export default {
  name: 'ImDashboard',
  components: {
    CountTo,
    LineChart,
  },
  data() {
    return {
      // 统计数据
      stats: {
        onlineUsers: 0,
        todayMessages: 0,
        activeGroups: 0,
        systemStatus: 'normal',
      },
      // 图表类型
      messageChartType: 'realtime',
      resourceChartType: 'realtime',
      // 图表数据
      messageChartData: {},
      resourceChartData: {},
      // 告警列表
      alertList: [],
      // 性能指标
      metrics: {
        messageDelay: 0,
        connections: 0,
        queueBacklog: 0,
        systemLoad: 0,
      },
      // 定时器
      timer: null,
    }
  },
  created() {
    this.getStats()
    this.startAutoRefresh()
  },
  beforeUnmount() {
    this.stopAutoRefresh()
  },
  methods: {
    /** 获取统计数据 */
    getStats() {
      getDashboardData().then(response => {
        this.stats = response.data.stats
        this.messageChartData = response.data.messageChart
        this.resourceChartData = response.data.resourceChart
        this.alertList = response.data.alerts
      })

      getPerformanceMetrics().then(response => {
        this.metrics = response.data
      })
    },
    /** 获取状态颜色 */
    getStatusColor() {
      const map = {
        normal: '#34bfa3',
        warning: '#ffb822',
        error: '#f4516c',
      }
      return map[this.stats.systemStatus] || '#34bfa3'
    },
    /** 获取状态文本 */
    getStatusText() {
      const map = {
        normal: '正常',
        warning: '警告',
        error: '异常',
      }
      return map[this.stats.systemStatus] || '正常'
    },
    /** 获取告警级别样式 */
    getAlertLevelType(level) {
      const map = {
        critical: 'danger',
        warning: 'warning',
        info: 'info',
      }
      return map[level] || 'info'
    },
    /** 计算延迟百分比 */
    getDelayPercentage(delay) {
      return Math.min(Math.round(delay / 10), 100)
    },
    /** 获取延迟状态 */
    getDelayStatus(delay) {
      if (delay <= 500) return 'success'
      if (delay <= 1000) return 'warning'
      return 'exception'
    },
    /** 计算连接数百分比 */
    getConnectionPercentage(connections) {
      return Math.min(Math.round(connections / 100), 100)
    },
    /** 获取连接数状态 */
    getConnectionStatus(connections) {
      if (connections <= 5000) return 'success'
      if (connections <= 8000) return 'warning'
      return 'exception'
    },
    /** 计算队列积压百分比 */
    getQueuePercentage(backlog) {
      return Math.min(Math.round(backlog / 100), 100)
    },
    /** 获取队列积压状态 */
    getQueueStatus(backlog) {
      if (backlog <= 1000) return 'success'
      if (backlog <= 5000) return 'warning'
      return 'exception'
    },
    /** 计算系统负载百分比 */
    getLoadPercentage(load) {
      return Math.min(Math.round(load * 100), 100)
    },
    /** 获取系统负载状态 */
    getLoadStatus(load) {
      if (load <= 0.7) return 'success'
      if (load <= 0.9) return 'warning'
      return 'exception'
    },
    /** 查看更多告警 */
    handleViewAlerts() {
      this.$router.push({ path: '/im/monitor/alert' })
    },
    /** 启动自动刷新 */
    startAutoRefresh() {
      this.timer = setInterval(() => {
        this.getStats()
      }, 30000) // 每30秒刷新一次
    },
    /** 停止自动刷新 */
    stopAutoRefresh() {
      if (this.timer) {
        clearInterval(this.timer)
      }
    },
  },
}
</script>

<style scoped>
.card-panel {
  height: 108px;
  display: flex;
  align-items: center;
}
.card-panel-icon-wrapper {
  float: left;
  margin: 14px 0 0 14px;
  padding: 16px;
  transition: all 0.38s ease-out;
  border-radius: 6px;
  background: #409eff;
}
.card-panel-icon {
  float: left;
  font-size: 48px;
  color: #fff;
}
.card-panel-description {
  float: right;
  font-weight: bold;
  margin: 26px;
  margin-left: 0px;
}
.card-panel-text {
  line-height: 18px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
  margin-bottom: 12px;
}
.card-panel-num {
  font-size: 20px;
}
.card-panel-status {
  font-size: 20px;
  font-weight: bold;
}
.chart-wrapper {
  height: 340px;
}
.metrics-panel {
  padding: 20px;
}
.metric-item {
  margin-bottom: 20px;
}
.metric-label {
  margin-bottom: 5px;
  color: rgba(0, 0, 0, 0.45);
}
.metric-value {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}
</style>
