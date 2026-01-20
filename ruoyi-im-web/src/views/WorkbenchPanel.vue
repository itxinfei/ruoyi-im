<template>
  <div class="workbench-panel">
    <div class="workbench-content">
      <div class="panel-header">
        <div class="header-left">
          <h1 class="panel-title">工作台</h1>
          <p class="panel-subtitle">欢迎回来，开始高效工作</p>
        </div>
        <div class="header-right">
          <p class="date-label">今天是</p>
          <p class="date-text">{{ currentDate }}</p>
        </div>
      </div>

      <div class="quick-actions">
        <div
          v-for="action in quickActions"
          :key="action.id"
          class="action-card"
          @click="handleActionClick(action.id)"
        >
          <div class="action-icon" :class="action.colorClass">
            <el-icon class="icon">
              <component :is="action.icon" />
            </el-icon>
          </div>
          <span class="action-label">{{ action.label }}</span>
        </div>
      </div>

      <div class="content-grid">
        <div class="grid-item">
          <div class="card">
            <div class="card-header">
              <div class="card-title">
                <el-icon><Check /></el-icon>
                我的待办
              </div>
              <el-badge :value="todos.length" class="todo-badge" />
            </div>
            <div class="card-body">
              <div
                v-for="todo in todos"
                :key="todo.id"
                class="todo-item"
                @click="handleTodoClick(todo)"
              >
                <div class="todo-checkbox"></div>
                <div class="todo-content">
                  <p class="todo-title">{{ todo.title }}</p>
                  <div class="todo-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ todo.time }}</span>
                  </div>
                </div>
              </div>
              <el-button text class="view-all-btn">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div class="grid-item">
          <div class="card">
            <div class="card-header">
              <div class="card-title">
                <el-icon><Bell /></el-icon>
                公告通知
              </div>
            </div>
            <div class="card-body">
              <div
                v-for="notice in notices"
                :key="notice.id"
                class="notice-item"
                @click="handleNoticeClick(notice)"
              >
                <div v-if="notice.unread" class="unread-dot"></div>
                <div v-else class="unread-placeholder"></div>
                <div class="notice-content">
                  <p class="notice-title" :class="{ unread: notice.unread }">{{ notice.title }}</p>
                  <span class="notice-time">{{ notice.time }}</span>
                </div>
              </div>
              <el-button text class="view-all-btn">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div class="grid-item">
          <div class="card">
            <div class="card-header">
              <div class="card-title">
                <el-icon><TrendCharts /></el-icon>
                团队数据
              </div>
            </div>
            <div class="card-body">
              <div class="stat-item stat-blue">
                <div class="stat-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <p class="stat-label">团队成员</p>
                  <p class="stat-value">24人</p>
                </div>
                <el-icon class="stat-trend"><TrendCharts /></el-icon>
              </div>
              <div class="stat-item stat-green">
                <div class="stat-icon">
                  <el-icon><Check /></el-icon>
                </div>
                <div class="stat-info">
                  <p class="stat-label">本周完成任务</p>
                  <p class="stat-value">38个</p>
                </div>
                <el-icon class="stat-trend"><TrendCharts /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="grid-item">
          <div class="card">
            <div class="card-header">
              <div class="card-title">
                <el-icon><Document /></el-icon>
                最近文档
              </div>
            </div>
            <div class="card-body">
              <div
                v-for="doc in recentDocs"
                :key="doc.id"
                class="doc-item"
                @click="handleDocClick(doc)"
              >
                <div class="doc-icon" :class="doc.iconClass">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="doc-content">
                  <p class="doc-title">{{ doc.title }}</p>
                  <span class="doc-time">{{ doc.time }}</span>
                </div>
              </div>
              <el-button text class="view-all-btn">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Calendar,
  Clock,
  Check,
  User,
  TrendCharts,
  Document,
  Bell,
  ArrowRight
} from '@element-plus/icons-vue'

const quickActions = ref([
  {
    id: 'calendar',
    icon: Calendar,
    label: '日程',
    colorClass: 'action-blue'
  },
  {
    id: 'attendance',
    icon: Clock,
    label: '考勤打卡',
    colorClass: 'action-green'
  },
  {
    id: 'approval',
    icon: Check,
    label: '审批',
    colorClass: 'action-orange'
  },
  {
    id: 'report',
    icon: Document,
    label: '日报',
    colorClass: 'action-purple'
  }
])

const todos = ref([
  {
    id: '1',
    title: '完成项目设计稿',
    time: '今天 15:00',
    status: 'pending'
  },
  {
    id: '2',
    title: '参加团队会议',
    time: '今天 16:30',
    status: 'pending'
  },
  {
    id: '3',
    title: '审批报销单',
    time: '明天 10:00',
    status: 'pending'
  }
])

const notices = ref([
  {
    id: '1',
    title: '关于调整工作时间的通知',
    time: '2小时前',
    unread: true
  },
  {
    id: '2',
    title: '本月绩效考核开始',
    time: '昨天',
    unread: true
  },
  {
    id: '3',
    title: '公司年会活动通知',
    time: '2天前',
    unread: false
  }
])

const recentDocs = ref([
  {
    id: '1',
    title: '产品需求文档V2.0',
    time: '2小时前',
    iconClass: 'doc-blue'
  },
  {
    id: '2',
    title: 'Q1季度总结报告',
    time: '昨天',
    iconClass: 'doc-green'
  }
])

const currentDate = ref('')

const formatDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const day = days[now.getDay()]
  currentDate.value = `${year}年${month}月${date}日 ${day}`
}

const handleActionClick = (actionId) => {
  console.log('Action clicked:', actionId)
}

const handleTodoClick = (todo) => {
  console.log('Todo clicked:', todo)
}

const handleNoticeClick = (notice) => {
  console.log('Notice clicked:', notice)
}

const handleDocClick = (doc) => {
  console.log('Doc clicked:', doc)
}

onMounted(() => {
  formatDate()
})
</script>

<style scoped>
.workbench-panel {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.workbench-content {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-left {
  .panel-title {
    font-size: 24px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 4px 0;
  }

  .panel-subtitle {
    font-size: 14px;
    color: #8c8c8c;
    margin: 0;
  }
}

.header-right {
  text-align: right;

  .date-label {
    font-size: 14px;
    color: #8c8c8c;
    margin: 0 0 4px 0;
  }

  .date-text {
    font-size: 18px;
    font-weight: 500;
    color: #262626;
    margin: 0;
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.action-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .icon {
    font-size: 28px;
    color: #fff;
  }

  &.action-blue {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
  }

  &.action-green {
    background: linear-gradient(135deg, #22c55e, #16a34a);
  }

  &.action-orange {
    background: linear-gradient(135deg, #f97316, #ea580c);
  }

  &.action-purple {
    background: linear-gradient(135deg, #a855f7, #9333ea);
  }
}

.action-label {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.grid-item {
  .card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    overflow: hidden;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
    color: #262626;
    margin: 0;

    .el-icon {
      font-size: 16px;
      color: #595959;
    }
  }

  .todo-badge {
    background-color: #e6f7ff;
    color: #1890ff;
  }
}

.card-body {
  padding: 12px 20px;
}

.todo-item,
.notice-item,
.doc-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }
}

.todo-checkbox {
  width: 16px;
  height: 16px;
  border: 2px solid #1890ff;
  border-radius: 4px;
  flex-shrink: 0;
  margin-top: 2px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #e6f7ff;
  }
}

.todo-content,
.notice-content,
.doc-content {
  flex: 1;
  min-width: 0;
}

.todo-title,
.notice-title,
.doc-title {
  font-size: 14px;
  color: #262626;
  margin: 0 0 4px 0;
  line-height: 1.4;

  &.unread {
    font-weight: 500;
  }
}

.todo-time,
.notice-time,
.doc-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8c8c8c;

  .el-icon {
    font-size: 12px;
  }
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  flex-shrink: 0;
  margin-top: 6px;
}

.unread-placeholder {
  width: 8px;
  height: 8px;
  flex-shrink: 0;
  margin-top: 6px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;

  &.stat-blue {
    background-color: #e6f7ff;
  }

  &.stat-green {
    background-color: #f6ffed;
  }
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 20px;
  }

  .stat-blue & {
    background-color: #bae7ff;
    color: #1890ff;
  }

  .stat-green & {
    background-color: #d9f7be;
    color: #52c41a;
  }
}

.stat-info {
  flex: 1;

  .stat-label {
    font-size: 12px;
    color: #595959;
    margin: 0 0 4px 0;
  }

  .stat-value {
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin: 0;
  }
}

.stat-trend {
  font-size: 20px;
  color: #52c41a;
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }

  &.doc-blue {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.doc-green {
    background-color: #f6ffed;
    color: #52c41a;
  }
}

.view-all-btn {
  width: 100%;
  color: #1890ff;
  font-size: 14px;
  margin-top: 8px;
  padding: 8px 0;

  &:hover {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  .el-icon {
    margin-left: 4px;
  }
}
</style>
