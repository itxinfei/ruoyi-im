<template>
  <el-dialog
    v-model="visible"
    title="消息统计"
    :width="800"
    :modal="true"
    :close-on-click-modal="false"
    class="message-stats-dialog"
  >
    <div v-loading="loading" class="stats-container">
      <!-- 统计概览 -->
      <div class="stats-overview">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <span class="material-icons-outlined">chat</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.totalMessages) }}</div>
            <div class="stat-label">总消息数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <span class="material-icons-outlined">image</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.imageCount) }}</div>
            <div class="stat-label">图片消息</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <span class="material-icons-outlined">attach_file</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.fileCount) }}</div>
            <div class="stat-label">文件消息</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <span class="material-icons-outlined">mic</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.voiceCount) }}</div>
            <div class="stat-label">语音消息</div>
          </div>
        </div>
      </div>

      <!-- 时间范围选择 -->
      <div class="time-range-selector">
        <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="quarter">近三月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
          <el-radio-button label="all">全部</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 图表区域 -->
      <el-tabs v-model="activeTab" class="stats-tabs">
        <!-- 消息趋势 -->
        <el-tab-pane label="消息趋势" name="trend">
          <div class="chart-container">
            <div class="chart-header">
              <h4>每日消息数量</h4>
            </div>
            <div class="trend-chart">
              <div
                v-for="(item, index) in trendData"
                :key="index"
                class="trend-bar"
                :style="{ height: `${(item.count / maxTrendCount) * 100}%` }"
              >
                <el-tooltip :content="`${item.date}: ${item.count} 条消息`" placement="top">
                  <div class="bar-fill"></div>
                </el-tooltip>
                <div class="bar-label">{{ formatTrendLabel(item.date) }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 活跃时段 -->
        <el-tab-pane label="活跃时段" name="hourly">
          <div class="chart-container">
            <div class="chart-header">
              <h4>24小时活跃分布</h4>
            </div>
            <div class="hourly-chart">
              <div
                v-for="(item, hour) in hourlyData"
                :key="hour"
                class="hour-bar"
                :style="{ height: `${(item / maxHourlyCount) * 100}%` }"
              >
                <el-tooltip :content="`${hour}:00 - ${item} 条消息`" placement="top">
                  <div class="bar-fill"></div>
                </el-tooltip>
                <div class="bar-label">{{ hour }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 成员排行 -->
        <el-tab-pane label="成员排行" name="ranking" v-if="isGroup">
          <div class="ranking-container">
            <div class="ranking-list">
              <div
                v-for="(member, index) in rankingData"
                :key="member.id"
                class="ranking-item"
                :class="{ 'top-three': index < 3 }"
              >
                <div class="ranking-number" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
                <DingtalkAvatar :src="member.avatar" :name="member.name" :size="40" />
                <div class="member-info">
                  <div class="member-name">{{ member.name }}</div>
                  <div class="member-stats">
                    <span>{{ formatNumber(member.messageCount) }} 条消息</span>
                    <span v-if="member.percentage" class="percentage">{{ member.percentage }}%</span>
                  </div>
                  <div class="member-bar">
                    <div class="bar-fill" :style="{ width: `${(member.messageCount / rankingData[0].messageCount) * 100}%` }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 消息类型分布 -->
        <el-tab-pane label="类型分布" name="type">
          <div class="type-distribution">
            <div
              v-for="item in typeDistribution"
              :key="item.type"
              class="type-item"
            >
              <div class="type-header">
                <div class="type-icon" :class="`type-${item.type}`">
                  <span class="material-icons-outlined">{{ getTypeIcon(item.type) }}</span>
                </div>
                <div class="type-info">
                  <span class="type-name">{{ item.name }}</span>
                  <span class="type-count">{{ formatNumber(item.count) }}</span>
                </div>
              </div>
              <div class="type-bar">
                <div class="bar-fill" :style="{ width: `${item.percentage}%` }"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleExport">导出统计</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: Boolean,
  conversationId: [String, Number],
  isGroup: {
    type: Boolean,
    default: false
  },
  messages: {
    type: Array,
    default: () => []
  },
  members: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const activeTab = ref('trend')
const timeRange = ref('month')

// 统计数据
const stats = ref({
  totalMessages: 0,
  imageCount: 0,
  fileCount: 0,
  voiceCount: 0,
  videoCount: 0
})

// 趋势数据
const trendData = ref([])

// 时段数据
const hourlyData = ref(new Array(24).fill(0))

// 排行数据
const rankingData = ref([])

// 类型分布
const typeDistribution = ref([])

// 计算属性
const maxTrendCount = computed(() => {
  const max = Math.max(...trendData.value.map(d => d.count))
  return max || 1
})

const maxHourlyCount = computed(() => {
  const max = Math.max(...hourlyData.value)
  return max || 1
})

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化趋势标签
const formatTrendLabel = (date) => {
  const d = dayjs(date)
  const today = dayjs()
  if (d.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return '今天'
  } else if (d.format('YYYY-MM-DD') === today.subtract(1, 'day').format('YYYY-MM-DD')) {
    return '昨天'
  } else {
    return d.format('MM-DD')
  }
}

// 获取类型图标
const getTypeIcon = (type) => {
  const icons = {
    TEXT: 'text_fields',
    IMAGE: 'image',
    FILE: 'description',
    VOICE: 'mic',
    VIDEO: 'videocam',
    LINK: 'link',
    REPLY: 'reply',
    RECALL: 'undo',
    SYSTEM: 'info'
  }
  return icons[type] || 'chat_bubble'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    TEXT: '文本消息',
    IMAGE: '图片消息',
    FILE: '文件消息',
    VOICE: '语音消息',
    VIDEO: '视频消息',
    LINK: '链接消息',
    REPLY: '回复消息',
    RECALL: '撤回消息',
    SYSTEM: '系统消息'
  }
  return names[type] || type
}

// 计算统计数据
const calculateStats = () => {
  if (!props.messages || props.messages.length === 0) {
    resetStats()
    return
  }

  const now = dayjs()
  let filteredMessages = props.messages

  // 根据时间范围筛选
  switch (timeRange.value) {
    case 'week':
      filteredMessages = props.messages.filter(m =>
        dayjs(m.sendTime || m.createTime).isAfter(now.subtract(7, 'day'))
      )
      break
    case 'month':
      filteredMessages = props.messages.filter(m =>
        dayjs(m.sendTime || m.createTime).isAfter(now.subtract(30, 'day'))
      )
      break
    case 'quarter':
      filteredMessages = props.messages.filter(m =>
        dayjs(m.sendTime || m.createTime).isAfter(now.subtract(90, 'day'))
      )
      break
    case 'year':
      filteredMessages = props.messages.filter(m =>
        dayjs(m.sendTime || m.createTime).isAfter(now.subtract(365, 'day'))
      )
      break
  }

  // 基础统计
  stats.value = {
    totalMessages: filteredMessages.length,
    imageCount: filteredMessages.filter(m => m.messageType === 'IMAGE').length,
    fileCount: filteredMessages.filter(m => m.messageType === 'FILE').length,
    voiceCount: filteredMessages.filter(m => m.messageType === 'VOICE').length,
    videoCount: filteredMessages.filter(m => m.messageType === 'VIDEO').length
  }

  // 计算趋势数据（按天）
  const trendMap = new Map()
  filteredMessages.forEach(m => {
    const date = dayjs(m.sendTime || m.createTime).format('YYYY-MM-DD')
    trendMap.set(date, (trendMap.get(date) || 0) + 1)
  })

  // 生成最近30天的数据（包括没有消息的日期）
  const days = timeRange.value === 'all' ? 30 : getDaysCount()
  trendData.value = []
  for (let i = days - 1; i >= 0; i--) {
    const date = now.subtract(i, 'day').format('YYYY-MM-DD')
    trendData.value.push({
      date,
      count: trendMap.get(date) || 0
    })
  }

  // 计算时段数据（按小时）
  hourlyData.value = new Array(24).fill(0)
  filteredMessages.forEach(m => {
    const hour = dayjs(m.sendTime || m.createTime).hour()
    hourlyData.value[hour]++
  })

  // 计算成员排行
  if (props.isGroup && props.members.length > 0) {
    const memberMap = new Map()
    filteredMessages.forEach(m => {
      const senderId = m.senderId
      memberMap.set(senderId, (memberMap.get(senderId) || 0) + 1)
    })

    const memberStats = Array.from(memberMap.entries())
      .map(([id, count]) => {
        const member = props.members.find(m => m.id == id || m.userId == id)
        return {
          id,
          name: member?.name || member?.userName || '未知用户',
          avatar: member?.avatar || '',
          messageCount: count,
          percentage: ((count / filteredMessages.length) * 100).toFixed(1)
        }
      })
      .sort((a, b) => b.messageCount - a.messageCount)
      .slice(0, 10)

    rankingData.value = memberStats
  }

  // 计算类型分布
  const typeMap = new Map()
  filteredMessages.forEach(m => {
    const type = m.messageType
    typeMap.set(type, (typeMap.get(type) || 0) + 1)
  })

  typeDistribution.value = Array.from(typeMap.entries())
    .map(([type, count]) => ({
      type,
      name: getTypeName(type),
      count,
      percentage: ((count / filteredMessages.length) * 100).toFixed(1)
    }))
    .sort((a, b) => b.count - a.count)
}

const getDaysCount = () => {
  const counts = { week: 7, month: 30, quarter: 90, year: 365, all: 30 }
  return counts[timeRange.value] || 30
}

const resetStats = () => {
  stats.value = {
    totalMessages: 0,
    imageCount: 0,
    fileCount: 0,
    voiceCount: 0,
    videoCount: 0
  }
  trendData.value = []
  hourlyData.value = new Array(24).fill(0)
  rankingData.value = []
  typeDistribution.value = []
}

const handleTimeRangeChange = () => {
  calculateStats()
}

const handleExport = () => {
  ElMessage.success('统计数据导出功能开发中')
}

// 监听消息变化
watch(() => props.messages, () => {
  calculateStats()
}, { deep: true, immediate: true })

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    calculateStats()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.message-stats-dialog {
  .stats-container {
    padding: 8px 0;
  }

  .stats-overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: var(--dt-bg-tertiary);
      border-radius: var(--dt-radius-lg);

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: var(--dt-radius-md);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        .material-icons-outlined {
          font-size: 24px;
          color: #fff;
        }
      }

      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: var(--dt-text-primary);
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: var(--dt-text-tertiary);
          margin-top: 4px;
        }
      }
    }
  }

  .time-range-selector {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }

  .stats-tabs {
    .chart-container {
      padding: 16px 0;

      .chart-header {
        margin-bottom: 20px;

        h4 {
          font-size: 15px;
          font-weight: 600;
          color: var(--dt-text-primary);
          margin: 0;
        }
      }
    }

    .trend-chart,
    .hourly-chart {
      display: flex;
      align-items: flex-end;
      gap: 4px;
      height: 200px;
      padding: 0 8px;

      .trend-bar,
      .hour-bar {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        position: relative;
        min-height: 4px;

        .bar-fill {
          width: 100%;
          height: 100%;
          min-height: 4px;
          background: linear-gradient(180deg, #1677ff 0%, #69b1ff 100%);
          border-radius: 4px 4px 0 0;
          cursor: pointer;
          transition: opacity var(--dt-transition-base);

          &:hover {
            opacity: 0.8;
          }
        }

        .bar-label {
          font-size: 11px;
          color: var(--dt-text-tertiary);
          margin-top: 6px;
          white-space: nowrap;
        }
      }
    }
  }

  .ranking-container {
    .ranking-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .ranking-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 12px 16px;
        background: var(--dt-bg-tertiary);
        border-radius: var(--dt-radius-md);
        transition: all var(--dt-transition-base);

        &.top-three {
          background: linear-gradient(135deg, rgba(22, 119, 255, 0.08) 0%, rgba(22, 119, 255, 0.02) 100%);
        }

        .ranking-number {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 14px;
          background: var(--dt-bg-hover);
          color: var(--dt-text-tertiary);

          &.rank-1 {
            background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
            color: #fff;
          }

          &.rank-2 {
            background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
            color: #fff;
          }

          &.rank-3 {
            background: linear-gradient(135deg, #cd7f32 0%, #e5a158 100%);
            color: #fff;
          }
        }

        .member-info {
          flex: 1;

          .member-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--dt-text-primary);
            margin-bottom: 4px;
          }

          .member-stats {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;
            color: var(--dt-text-tertiary);
            margin-bottom: 6px;

            .percentage {
              color: var(--dt-brand-color);
            }
          }

          .member-bar {
            height: 4px;
            background: var(--dt-bg-hover);
            border-radius: 2px;
            overflow: hidden;

            .bar-fill {
              height: 100%;
              background: linear-gradient(90deg, #1677ff 0%, #69b1ff 100%);
              border-radius: 2px;
              transition: width 0.5s ease-out;
            }
          }
        }
      }
    }
  }

  .type-distribution {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .type-item {
      .type-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;

        .type-icon {
          width: 36px;
          height: 36px;
          border-radius: var(--dt-radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;

          &.type-TEXT {
            background: rgba(22, 119, 255, 0.1);
            color: #1677ff;
          }

          &.type-IMAGE {
            background: rgba(82, 196, 26, 0.1);
            color: #52c41a;
          }

          &.type-FILE {
            background: rgba(250, 173, 20, 0.1);
            color: #faad14;
          }

          &.type-VOICE {
            background: rgba(22, 119, 255, 0.1);
            color: #1677ff;
          }

          &.type-VIDEO {
            background: rgba(255, 77, 79, 0.1);
            color: #ff4d4f;
          }

          .material-icons-outlined {
            font-size: 20px;
          }
        }

        .type-info {
          flex: 1;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .type-name {
            font-size: 14px;
            color: var(--dt-text-primary);
          }

          .type-count {
            font-size: 14px;
            font-weight: 600;
            color: var(--dt-brand-color);
          }
        }
      }

      .type-bar {
        height: 8px;
        background: var(--dt-bg-hover);
        border-radius: 4px;
        overflow: hidden;

        .bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #1677ff 0%, #69b1ff 100%);
          border-radius: 4px;
          transition: width 0.5s ease-out;
        }
      }
    }
  }
}

// 暗色模式
.dark .message-stats-dialog {
  .stat-card,
  .ranking-item {
    background: var(--dt-bg-tertiary-dark);
  }

  .type-bar,
  .member-bar {
    background: var(--dt-bg-hover-dark);
  }
}

// 响应式
@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .trend-chart,
  .hourly-chart {
    gap: 2px;

    .bar-label {
      font-size: 10px;
    }
  }
}
</style>
