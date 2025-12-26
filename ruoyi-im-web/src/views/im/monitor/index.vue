<template>
  <div class="app-container">
    <!-- 系统状态卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div>
              <span>系统状态</span>
              <el-tag
                :type="health.status === 'UP' ? 'success' : 'danger'"
                size="small"
                class="float-right"
              >
                {{ health.status }}
              </el-tag>
            </div>
          </template>
          <div class="status-item">
            <span>CPU使用率</span>
            <el-progress :percentage="systemMetrics.cpu_usage" :color="getCpuColor"></el-progress>
          </div>
          <div class="status-item">
            <span>内存使用率</span>
            <el-progress
              :percentage="systemMetrics.memory_usage"
              :color="getMemoryColor"
            ></el-progress>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div>
              <span>JVM状态</span>
            </div>
          </template>
          <div class="status-item">
            <span>堆内存使用</span>
            <div>
              {{ formatBytes(jvmMetrics.heap_used) }} / {{ formatBytes(jvmMetrics.heap_max) }}
            </div>
          </div>
          <div class="status-item">
            <span>线程数</span>
            <div>{{ jvmMetrics.thread_count }} / {{ jvmMetrics.peak_thread_count }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div>
              <span>WebSocket状态</span>
            </div>
          </template>
          <div class="status-item">
            <span>活跃连接数</span>
            <count-to
              :start-val="0"
              :end-val="wsMetrics.active_connections"
              :duration="2000"
              class="card-panel-num"
            />
          </div>
          <div class="status-item">
            <span>已连接数</span>
            <count-to
              :start-val="0"
              :end-val="wsMetrics.connected_count"
              :duration="2000"
              class="card-panel-num"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div>
              <span>消息状态</span>
            </div>
          </template>
          <div class="status-item">
            <span>平均延迟</span>
            <div>{{ messageMetrics.average_delay.toFixed(2) }}ms</div>
          </div>
          <div class="status-item">
            <span>消息总量</span>
            <count-to
              :start-val="0"
              :end-val="messageMetrics.total_count || 0"
              :duration="2000"
              class="card-panel-num"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>
              <span>系统资源趋势</span>
              <el-radio-group v-model="timeRange" size="mini" class="float-right">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <line-chart :chart-data="systemChartData" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>
              <span>消息统计</span>
              <el-radio-group v-model="messageTimeRange" size="mini" class="float-right">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <bar-chart :chart-data="messageChartData" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警列表 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div>
          <span>最近告警</span>
          <el-button type="text" icon="el-icon-setting" class="float-right" @click="showAlertConfig"
            >告警设置</el-button
          >
        </div>
      </template>
      <el-table v-loading="alertLoading" :data="alertList">
        <el-table-column label="告警时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="告警级别" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="dict.type.monitor_alert_level" :value="scope.row.level" />
          </template>
        </el-table-column>
        <el-table-column label="告警类型" align="center" width="120">
          <template #default="scope">
            <dict-tag :options="dict.type.monitor_alert_type" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column label="告警内容" align="left" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ scope.row.content }}
          </template>
        </el-table-column>
        <el-table-column label="通知方式" align="center" width="180">
          <template #default="scope">
            <dict-tag
              v-for="type in scope.row.notifyTypes"
              :key="type"
              :options="dict.type.monitor_notify_type"
              :value="type"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 告警设置对话框 -->
    <el-dialog v-model:visible="alertConfigVisible" title="告警设置" width="600px" append-to-body>
      <el-form
        ref="alertConfigForm"
        :model="alertConfig"
        :rules="alertConfigRules"
        label-width="100px"
      >
        <el-form-item label="启用告警" prop="enabled">
          <el-switch v-model="alertConfig.enabled" />
        </el-form-item>
        <el-form-item label="告警级别" prop="levels">
          <el-checkbox-group v-model="alertConfig.levels">
            <el-checkbox label="info">信息</el-checkbox>
            <el-checkbox label="warning">警告</el-checkbox>
            <el-checkbox label="error">错误</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="通知方式" prop="notifyTypes">
          <el-checkbox-group v-model="alertConfig.notifyTypes">
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="dingtalk">钉钉</el-checkbox>
            <el-checkbox label="webhook">Webhook</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="告警间隔" prop="interval">
          <el-input-number
            v-model="alertConfig.interval"
            :min="1"
            :max="60"
            label="分钟"
          ></el-input-number>
          <span class="help-text">分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="saveAlertConfig">确 定</el-button>
          <el-button @click="alertConfigVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getAllMetrics, checkHealth } from '@/api/im/monitor'
import CountTo from 'vue-count-to'
import LineChart from './components/LineChart'
import BarChart from './components/BarChart'

export default {
  name: 'Monitor',
  components: {
    CountTo,
    LineChart,
    BarChart,
  },
  dicts: ['monitor_alert_level', 'monitor_alert_type', 'monitor_notify_type'],
  data() {
    return {
      // 系统指标数据
      systemMetrics: {
        cpu_usage: 0,
        memory_usage: 0,
      },
      // JVM指标数据
      jvmMetrics: {
        heap_used: 0,
        heap_max: 0,
        thread_count: 0,
        peak_thread_count: 0,
      },
      // WebSocket指标数据
      wsMetrics: {
        active_connections: 0,
        connected_count: 0,
      },
      // 消息指标数据
      messageMetrics: {
        average_delay: 0,
        total_count: 0,
      },
      // 健康状态
      health: {
        status: 'UP',
      },
      // 图表时间范围
      timeRange: '1h',
      messageTimeRange: '1h',
      // 系统资源图表数据
      systemChartData: {
        labels: [],
        datasets: [],
      },
      // 消息统计图表数据
      messageChartData: {
        labels: [],
        datasets: [],
      },
      // 告警列表
      alertList: [],
      alertLoading: false,
      // 告警设置
      alertConfigVisible: false,
      alertConfig: {
        enabled: true,
        levels: ['warning', 'error'],
        notifyTypes: ['email'],
        interval: 5,
      },
      alertConfigRules: {
        levels: [{ type: 'array', required: true, message: '请选择告警级别', trigger: 'change' }],
        notifyTypes: [
          { type: 'array', required: true, message: '请选择通知方式', trigger: 'change' },
        ],
        interval: [{ required: true, message: '请输入告警间隔', trigger: 'blur' }],
      },
      // 定时刷新
      timer: null,
    }
  },
  computed: {
    getCpuColor() {
      return percentage => {
        if (percentage < 60) return '#67C23A'
        if (percentage < 80) return '#E6A23C'
        return '#F56C6C'
      }
    },
    getMemoryColor() {
      return percentage => {
        if (percentage < 60) return '#67C23A'
        if (percentage < 80) return '#E6A23C'
        return '#F56C6C'
      }
    },
  },
  created() {
    this.getMetrics()
    this.startTimer()
  },
  beforeUnmount() {
    this.stopTimer()
  },
  methods: {
    // 获取监控指标
    async getMetrics() {
      try {
        const [metricsRes, healthRes] = await Promise.all([getAllMetrics(), checkHealth()])

        this.systemMetrics = metricsRes.data.system
        this.jvmMetrics = metricsRes.data.jvm
        this.wsMetrics = metricsRes.data.websocket
        this.messageMetrics = metricsRes.data.message
        this.health = healthRes.data

        this.updateCharts()
      } catch (error) {
        console.error('Failed to get metrics:', error)
      }
    },

    // 更新图表数据
    updateCharts() {
      // 更新系统资源图表
      this.systemChartData = {
        labels: this.getLast24Hours(),
        datasets: [
          {
            label: 'CPU使用率',
            data: [
              /* CPU数据 */
            ],
            borderColor: '#409EFF',
            fill: false,
          },
          {
            label: '内存使用率',
            data: [
              /* 内存数据 */
            ],
            borderColor: '#67C23A',
            fill: false,
          },
        ],
      }

      // 更新消息统计图表
      this.messageChartData = {
        labels: this.getLast24Hours(),
        datasets: [
          {
            label: '消息数量',
            data: [
              /* 消息数据 */
            ],
            backgroundColor: '#409EFF',
          },
        ],
      }
    },

    // 获取最近24小时的时间标签
    getLast24Hours() {
      const hours = []
      for (let i = 23; i >= 0; i--) {
        const date = new Date()
        date.setHours(date.getHours() - i)
        hours.push(date.getHours() + ':00')
      }
      return hours
    },

    // 格式化字节大小
    formatBytes(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    // 显示告警设置
    showAlertConfig() {
      this.alertConfigVisible = true
    },

    // 保存告警设置
    saveAlertConfig() {
      this.$refs.alertConfigForm.validate(async valid => {
        if (valid) {
          try {
            // TODO: 调用保存告警设置的API
            this.$modal.msgSuccess('保存成功')
            this.alertConfigVisible = false
          } catch (error) {
            this.$modal.msgError('保存失败')
          }
        }
      })
    },

    // 开始定时刷新
    startTimer() {
      this.timer = setInterval(() => {
        this.getMetrics()
      }, 60000) // 每分钟刷新一次
    },

    // 停止定时刷新
    stopTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
  },
}
</script>

<style lang="scss" scoped>
.status-card {
  .status-item {
    margin-bottom: 15px;

    &:last-child {
      margin-bottom: 0;
    }

    span {
      display: block;
      margin-bottom: 5px;
      color: #606266;
    }
  }

  .card-panel-num {
    font-size: 20px;
    font-weight: bold;
    color: #303133;
  }
}

.chart-wrapper {
  height: 340px;
}

.float-right {
  float: right;
}

.help-text {
  margin-left: 10px;
  color: #909399;
}
</style>
