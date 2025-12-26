<template>
  <div class="call-history">
    <el-card>
      <template #header>
        <div class="header">
          <span>通话记录</span>
          <div class="header-actions">
            <el-radio-group v-model="filter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="audio">语音</el-radio-button>
              <el-radio-button label="video">视频</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- 通话记录列表 -->
      <div v-if="groupedRecords.length" class="call-list">
        <div v-for="group in groupedRecords" :key="group.date" class="call-group">
          <div class="group-date">{{ formatGroupDate(group.date) }}</div>

          <div v-for="record in group.records" :key="record.id" class="call-item">
            <div class="call-avatar">
              <el-avatar :size="40" :src="record.userInfo.avatar">
                {{ record.userInfo.name.substring(0, 1) }}
              </el-avatar>
            </div>

            <div class="call-info">
              <div class="call-name">{{ record.userInfo.name }}</div>
              <div class="call-detail">
                <i :class="getCallIcon(record)" :style="{ color: getCallColor(record) }"></i>
                <span>{{ getCallStatus(record) }}</span>
                <span class="call-time">{{ formatTime(record.startTime) }}</span>
                <span v-if="record.duration" class="call-duration">
                  {{ formatDuration(record.duration) }}
                </span>
              </div>
            </div>

            <div class="call-actions">
              <el-tooltip
                :content="record.type === 'video' ? '视频通话' : '语音通话'"
                placement="top"
              >
                <el-button
                  :type="record.type === 'video' ? 'primary' : 'success'"
                  circle
                  size="small"
                  :icon="record.type === 'video' ? 'el-icon-video-camera' : 'el-icon-phone'"
                  @click="makeCall(record)"
                />
              </el-tooltip>
              <el-dropdown trigger="click" @command="handleCommand($event, record)">
                <el-button type="text" icon="el-icon-more"></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除记录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <i class="el-icon-phone"></i>
        <p>暂无通话记录</p>
      </div>
    </el-card>

    <!-- 通话详情对话框 -->
    <el-dialog v-if="selectedRecord" v-model:visible="detailVisible" title="通话详情" width="400px">
      <div class="call-detail-dialog">
        <div class="detail-item">
          <span class="label">联系人</span>
          <span class="value">{{ selectedRecord.userInfo.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">通话类型</span>
          <span class="value">{{ selectedRecord.type === 'video' ? '视频通话' : '语音通话' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">通话方向</span>
          <span class="value">{{ selectedRecord.direction === 'outgoing' ? '呼出' : '呼入' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">开始时间</span>
          <span class="value">{{ formatDetailTime(selectedRecord.startTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">结束时间</span>
          <span class="value">{{ formatDetailTime(selectedRecord.endTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">通话时长</span>
          <span class="value">{{ formatDuration(selectedRecord.duration) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">通话状态</span>
          <span class="value">{{ getDetailStatus(selectedRecord) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import dayjs from 'dayjs'

export default {
  name: 'CallHistory',
  data() {
    return {
      filter: 'all',
      detailVisible: false,
      selectedRecord: null,
    }
  },
  computed: {
    ...mapState('call', ['callRecords']),

    // 过滤并分组后的记录
    groupedRecords() {
      // 首先过滤记录
      const filteredRecords = this.callRecords.filter(record => {
        if (this.filter === 'all') return true
        return record.type === this.filter
      })

      // 按日期分组
      const groups = filteredRecords.reduce((acc, record) => {
        const date = dayjs(record.startTime).format('YYYY-MM-DD')
        if (!acc[date]) {
          acc[date] = []
        }
        acc[date].push(record)
        return acc
      }, {})

      // 转换为数组并排序
      return Object.entries(groups)
        .map(([date, records]) => ({
          date,
          records: records.sort((a, b) => new Date(b.startTime) - new Date(a.startTime)),
        }))
        .sort((a, b) => new Date(b.date) - new Date(a.date))
    },
  },
  methods: {
    // 获取通话图标
    getCallIcon(record) {
      const icons = {
        outgoing: 'el-icon-top-right',
        incoming: 'el-icon-bottom-left',
        missed: 'el-icon-close',
      }
      return icons[this.getCallDirection(record)]
    },

    // 获取通话颜色
    getCallColor(record) {
      const colors = {
        outgoing: '#67C23A',
        incoming: '#409EFF',
        missed: '#F56C6C',
      }
      return colors[this.getCallDirection(record)]
    },

    // 获取通话方向
    getCallDirection(record) {
      if (record.endReason === 'rejected' || record.endReason === 'timeout') {
        return 'missed'
      }
      return record.direction
    },

    // 获取通话状态
    getCallStatus(record) {
      const status = {
        outgoing: '已拨出',
        incoming: '已接听',
        missed: record.direction === 'outgoing' ? '无人接听' : '未接来电',
      }
      return status[this.getCallDirection(record)]
    },

    // 获取详细状态
    getDetailStatus(record) {
      const status = {
        ended: '正常结束',
        rejected: '已拒绝',
        cancelled: '已取消',
        timeout: '无人接听',
        busy: '忙线中',
        failed: '连接失败',
      }
      return status[record.endReason] || '未知状态'
    },

    // 格式化分组日期
    formatGroupDate(date) {
      const today = dayjs()
      const recordDate = dayjs(date)

      if (recordDate.isSame(today, 'day')) {
        return '今天'
      }
      if (recordDate.isSame(today.subtract(1, 'day'), 'day')) {
        return '昨天'
      }
      if (recordDate.isSame(today, 'year')) {
        return recordDate.format('M月D日')
      }
      return recordDate.format('YYYY年M月D日')
    },

    // 格式化时间
    formatTime(time) {
      return dayjs(time).format('HH:mm')
    },

    // 格式化详细时间
    formatDetailTime(time) {
      return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    },

    // 格式化通话时长
    formatDuration(seconds) {
      if (!seconds) return '0秒'

      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const remainingSeconds = seconds % 60

      let result = ''
      if (hours > 0) {
        result += `${hours}小时`
      }
      if (minutes > 0) {
        result += `${minutes}分钟`
      }
      if (remainingSeconds > 0 || !result) {
        result += `${remainingSeconds}秒`
      }
      return result
    },

    // 发起通话
    makeCall(record) {
      this.$store.dispatch('call/makeCall', {
        userId: record.userId,
        isVideo: record.type === 'video',
      })
    },

    // 处理更多操作
    handleCommand(command, record) {
      switch (command) {
        case 'detail':
          this.showDetail(record)
          break
        case 'delete':
          this.deleteRecord(record)
          break
      }
    },

    // 显示详情
    showDetail(record) {
      this.selectedRecord = record
      this.detailVisible = true
    },

    // 删除记录
    deleteRecord(record) {
      this.$confirm('确定要删除这条通话记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          this.$store.dispatch('call/deleteCallRecord', record.id)
          this.$message.success('删除成功')
        })
        .catch(() => {})
    },
  },
}
</script>

<style lang="scss" scoped>
.call-history {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .call-list {
    .call-group {
      margin-bottom: 20px;

      .group-date {
        padding: 8px 0;
        color: #909399;
        font-size: 14px;
      }

      .call-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border-radius: 4px;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f5f7fa;
        }

        .call-avatar {
          margin-right: 12px;
        }

        .call-info {
          flex: 1;
          min-width: 0;

          .call-name {
            font-weight: 500;
            margin-bottom: 4px;
          }

          .call-detail {
            color: #909399;
            font-size: 13px;

            i {
              margin-right: 4px;
            }

            .call-time {
              margin: 0 8px;
            }

            .call-duration {
              color: #67c23a;
            }
          }
        }

        .call-actions {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 40px 0;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 16px;
    }

    p {
      margin: 0;
    }
  }
}

.call-detail-dialog {
  .detail-item {
    display: flex;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .label {
      width: 80px;
      color: #909399;
    }

    .value {
      flex: 1;
      color: #303133;
    }
  }
}
</style>
