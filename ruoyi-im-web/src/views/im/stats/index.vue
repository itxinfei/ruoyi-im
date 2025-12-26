<template>
  <div class="app-container">
    <!-- 时间范围选择 -->
    <el-form :inline="true" :model="queryParams" class="demo-form-inline">
      <el-form-item label="统计时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateRangeChange"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
        <el-button type="success" @click="exportReport">导出报表</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="card-row">
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>活跃用户数</span>
              <el-tooltip content="当前时间范围内的活跃用户数量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <count-to
              :start-val="0"
              :end-val="stats.activeUsers"
              :duration="2000"
              class="card-panel-num"
            />
            <div class="card-panel-trend">
              较上期
              <span :class="stats.userTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.userTrend) }}%
                <i :class="stats.userTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>消息总量</span>
              <el-tooltip content="当前时间范围内的消息总数" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <count-to
              :start-val="0"
              :end-val="stats.totalMessages"
              :duration="2000"
              class="card-panel-num"
            />
            <div class="card-panel-trend">
              较上期
              <span :class="stats.messageTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.messageTrend) }}%
                <i :class="stats.messageTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>活跃群组</span>
              <el-tooltip content="当前时间范围内的活跃群组数量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <count-to
              :start-val="0"
              :end-val="stats.activeGroups"
              :duration="2000"
              class="card-panel-num"
            />
            <div class="card-panel-trend">
              较上期
              <span :class="stats.groupTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.groupTrend) }}%
                <i :class="stats.groupTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>文件传输量</span>
              <el-tooltip content="当前时间范围内的文件传输总量" placement="top">
                <i class="el-icon-info" style="margin-left: 5px"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="card-panel">
            <count-to
              :start-val="0"
              :end-val="stats.fileTransfer"
              :duration="2000"
              class="card-panel-num"
            />
            <div class="card-panel-text">MB</div>
            <div class="card-panel-trend">
              较上期
              <span :class="stats.fileTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.fileTrend) }}%
                <i :class="stats.fileTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>用户活跃度趋势</span>
              <el-radio-group v-model="userChartType" size="mini" style="float: right">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <line-chart :chart-data="userActivityData"></line-chart>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>消息类型分布</span>
              <el-radio-group v-model="messageChartType" size="mini" style="float: right">
                <el-radio-button label="pie">饼图</el-radio-button>
                <el-radio-button label="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <component :is="messageChartType + '-chart'" :chart-data="messageTypeData"></component>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜 -->
    <el-row :gutter="20" class="rank-row">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>活跃用户排行</span>
            </div>
          </template>
          <el-table :data="userRankList" size="small" style="width: 100%">
            <el-table-column type="index" label="排名" width="50" align="center" />
            <el-table-column prop="userName" label="用户名" />
            <el-table-column prop="messageCount" label="消息数" align="right" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>活跃群组排行</span>
            </div>
          </template>
          <el-table :data="groupRankList" size="small" style="width: 100%">
            <el-table-column type="index" label="排名" width="50" align="center" />
            <el-table-column prop="groupName" label="群组名" />
            <el-table-column prop="messageCount" label="消息数" align="right" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>系统性能指标</span>
            </div>
          </template>
          <div class="performance-list">
            <div v-for="(item, index) in performanceList" :key="index" class="performance-item">
              <span class="label">{{ item.label }}</span>
              <el-progress
                :percentage="item.value"
                :color="item.color"
                :format="format"
                :stroke-width="15"
              ></el-progress>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {
  getStats,
  getUserActivity,
  getMessageTypes,
  getRankings,
  getPerformance,
} from '@/api/im/stats'
import CountTo from 'vue-count-to'
import LineChart from './components/LineChart'
import PieChart from './components/PieChart'
import BarChart from './components/BarChart'

export default {
  name: 'ImStats',
  components: {
    CountTo,
    LineChart,
    PieChart,
    BarChart,
  },
  data() {
    return {
      // 查询参数
      queryParams: {
        startDate: undefined,
        endDate: undefined,
      },
      // 日期范围
      dateRange: [],
      // 统计数据
      stats: {
        activeUsers: 0,
        totalMessages: 0,
        activeGroups: 0,
        fileTransfer: 0,
        userTrend: 0,
        messageTrend: 0,
        groupTrend: 0,
        fileTrend: 0,
      },
      // 图表类型
      userChartType: 'day',
      messageChartType: 'pie',
      // 图表数据
      userActivityData: {},
      messageTypeData: {},
      // 排行榜数据
      userRankList: [],
      groupRankList: [],
      // 性能指标
      performanceList: [
        { label: 'CPU使用率', value: 0, color: '#409EFF' },
        { label: '内存使用率', value: 0, color: '#67C23A' },
        { label: '消息队列负载', value: 0, color: '#E6A23C' },
        { label: '系统负载', value: 0, color: '#F56C6C' },
      ],
    }
  },
  created() {
    this.initData()
  },
  methods: {
    /** 初始化数据 */
    initData() {
      // 设置默认时间范围为最近7天
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      this.dateRange = [start, end]
      this.handleDateRangeChange(this.dateRange)
      this.refreshData()
    },
    /** 处理日期范围变化 */
    handleDateRangeChange(dates) {
      if (dates) {
        this.queryParams.startDate = dates[0]
        this.queryParams.endDate = dates[1]
      } else {
        this.queryParams.startDate = undefined
        this.queryParams.endDate = undefined
      }
    },
    /** 刷新数据 */
    refreshData() {
      this.getStatsData()
      this.getUserActivityData()
      this.getMessageTypeData()
      this.getRankingData()
      this.getPerformanceData()
    },
    /** 获取统计数据 */
    getStatsData() {
      getStats(this.queryParams).then(response => {
        this.stats = response.data
      })
    },
    /** 获取用户活跃度数据 */
    getUserActivityData() {
      getUserActivity({
        ...this.queryParams,
        type: this.userChartType,
      }).then(response => {
        this.userActivityData = response.data
      })
    },
    /** 获取消息类型数据 */
    getMessageTypeData() {
      getMessageTypes(this.queryParams).then(response => {
        this.messageTypeData = response.data
      })
    },
    /** 获取排行榜数据 */
    getRankingData() {
      getRankings(this.queryParams).then(response => {
        this.userRankList = response.data.userRank
        this.groupRankList = response.data.groupRank
      })
    },
    /** 获取性能数据 */
    getPerformanceData() {
      getPerformance().then(response => {
        this.performanceList = response.data
      })
    },
    /** 导出报表 */
    exportReport() {
      this.download(
        'im/stats/export',
        {
          ...this.queryParams,
        },
        `统计报表_${new Date().getTime()}.xlsx`
      )
    },
    /** 格式化百分比 */
    format(percentage) {
      return percentage + '%'
    },
  },
}
</script>

<style scoped>
.card-row {
  margin-bottom: 20px;
}
.chart-row {
  margin-bottom: 20px;
}
.rank-row {
  margin-bottom: 20px;
}
.card-panel {
  text-align: center;
}
.card-panel-num {
  font-size: 24px;
  font-weight: bold;
}
.card-panel-text {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}
.card-panel-trend {
  margin-top: 10px;
  font-size: 14px;
}
.up {
  color: #67c23a;
}
.down {
  color: #f56c6c;
}
.chart-wrapper {
  height: 340px;
}
.performance-list {
  padding: 10px;
}
.performance-item {
  margin-bottom: 20px;
}
.performance-item .label {
  display: block;
  margin-bottom: 5px;
}
</style>
