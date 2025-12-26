<template>
  <div class="call-stats">
    <el-row :gutter="20">
      <!-- 通话总览 -->
      <el-col :span="24">
        <el-card class="overview-card">
          <div class="stats-grid">
            <div class="stats-item">
              <div class="stats-value">{{ stats.totalCalls }}</div>
              <div class="stats-label">总通话次数</div>
            </div>
            <div class="stats-item">
              <div class="stats-value">{{ formatDuration(stats.totalDuration) }}</div>
              <div class="stats-label">总通话时长</div>
            </div>
            <div class="stats-item">
              <div class="stats-value">{{ stats.missedCalls }}</div>
              <div class="stats-label">未接来电</div>
            </div>
            <div class="stats-item">
              <div class="stats-value">{{ stats.videoCalls }}</div>
              <div class="stats-label">视频通话</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 通话类型分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div>
              <span>通话类型分布</span>
            </div>
          </template>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 通话方向分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div>
              <span>通话方向分布</span>
            </div>
          </template>
          <div ref="directionChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import * as echarts from 'echarts'

export default {
  name: 'CallStats',
  computed: {
    ...mapGetters('call', ['callStatistics']),
    stats() {
      return this.callStatistics
    },
  },
  mounted() {
    this.initCharts()
  },
  methods: {
    // 初始化图表
    initCharts() {
      this.initTypeChart()
      this.initDirectionChart()
    },

    // 初始化通话类型图表
    initTypeChart() {
      const chart = echarts.init(this.$refs.typeChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
        },
        legend: {
          orient: 'vertical',
          left: 'left',
        },
        series: [
          {
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center',
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '20',
                fontWeight: 'bold',
              },
            },
            labelLine: {
              show: false,
            },
            data: [
              { value: this.stats.videoCalls, name: '视频通话' },
              { value: this.stats.audioCalls, name: '语音通话' },
            ],
          },
        ],
      }
      chart.setOption(option)
    },

    // 初始化通话方向图表
    initDirectionChart() {
      const chart = echarts.init(this.$refs.directionChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
        },
        legend: {
          orient: 'vertical',
          left: 'left',
        },
        series: [
          {
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center',
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '20',
                fontWeight: 'bold',
              },
            },
            labelLine: {
              show: false,
            },
            data: [
              { value: this.stats.outgoingCalls, name: '呼出' },
              { value: this.stats.incomingCalls, name: '呼入' },
              { value: this.stats.missedCalls, name: '未接' },
            ],
          },
        ],
      }
      chart.setOption(option)
    },

    // 格式化通话时长
    formatDuration(seconds) {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)

      if (hours > 0) {
        return `${hours}小时${minutes}分钟`
      }
      return `${minutes}分钟`
    },
  },
}
</script>

<style lang="scss" scoped>
.call-stats {
  padding: 20px;

  .overview-card {
    margin-bottom: 20px;

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;

      .stats-item {
        text-align: center;
        padding: 20px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .stats-value {
          font-size: 24px;
          font-weight: bold;
          color: #409eff;
          margin-bottom: 8px;
        }

        .stats-label {
          color: #909399;
        }
      }
    }
  }

  .chart-card {
    .chart-container {
      height: 300px;
    }
  }
}
</style>
