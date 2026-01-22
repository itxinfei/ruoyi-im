<template>
  <div class="workbench-panel">
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.pendingTodos || 0 }}</div>
          <div class="stat-label">待办事项</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.pendingApprovals || 0 }}</div>
          <div class="stat-label">待审批</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.todayMeetings || 0 }}</div>
          <div class="stat-label">今日会议</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.unreadMessages || 0 }}</div>
          <div class="stat-label">未读消息</div>
        </div>
      </div>
    </div>

    <!-- 考勤打卡 -->
    <div class="attendance-section">
      <el-card class="attendance-card">
        <template #header>
          <div class="card-header">
            <span>考勤打卡</span>
            <el-tag v-if="todayAttendance.checkIn" type="success" size="small">
              已打卡
            </el-tag>
          </div>
        </template>
        <div class="attendance-content">
          <div class="attendance-time">
            <div class="current-time">{{ currentTime }}</div>
            <div class="current-date">{{ currentDate }}</div>
          </div>
          <div class="attendance-actions">
            <el-button
              v-if="!todayAttendance.checkIn"
              type="primary"
              size="large"
              :loading="checkingIn"
              @click="handleCheckIn"
            >
              上班打卡
            </el-button>
            <el-button
              v-else-if="!todayAttendance.checkOut"
              type="warning"
              size="large"
              :loading="checkingOut"
              @click="handleCheckOut"
            >
              下班打卡
            </el-button>
            <div v-else class="attendance-complete">
              <el-icon class="check-icon"><CircleCheck /></el-icon>
              <span>今日打卡已完成</span>
            </div>
          </div>
          <div v-if="todayAttendance.checkIn" class="attendance-records">
            <div class="record-item">
              <span class="record-label">上班:</span>
              <span class="record-time">{{ todayAttendance.checkIn }}</span>
            </div>
            <div v-if="todayAttendance.checkOut" class="record-item">
              <span class="record-label">下班:</span>
              <span class="record-time">{{ todayAttendance.checkOut }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 待办事项 -->
      <div class="content-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button text @click="showCreateTodoDialog = true">
                <el-icon><Plus /></el-icon>
                新建
              </el-button>
            </div>
          </template>
          <div v-loading="loadingTodos" class="todo-list">
            <div
              v-for="todo in todos"
              :key="todo.id"
              class="todo-item"
              :class="{ completed: todo.completed }"
            >
              <el-checkbox
                v-model="todo.completed"
                @change="handleTodoComplete(todo)"
              />
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-meta">
                  <el-tag v-if="todo.priority === 'HIGH'" type="danger" size="small">
                    高优先级
                  </el-tag>
                  <span class="todo-date">{{ formatDate(todo.dueDate) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!loadingTodos && todos.length === 0" description="暂无待办事项" />
          </div>
        </el-card>
      </div>

      <!-- 待审批 -->
      <div class="content-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="mr-10">审批中心</span>
                <el-radio-group v-model="approvalTab" size="small" @change="loadApprovals">
                  <el-radio-button label="pending">待处理</el-radio-button>
                  <el-radio-button label="my">我发起</el-radio-button>
                </el-radio-group>
              </div>
              <el-button type="primary" size="small" @click="showCreateApproval = true">
                <el-icon><Plus /></el-icon> 发起
              </el-button>
            </div>
          </template>
          <div v-loading="loadingApprovals" class="approval-list">
            <div
              v-for="approval in approvals"
              :key="approval.id"
              class="approval-item"
              @click="handleApprovalClick(approval)"
            >
              <el-avatar :size="40" :src="approval.applicantAvatar">
                {{ approval.applicantName?.charAt(0) }}
              </el-avatar>
              <div class="approval-content">
                <div class="approval-title">{{ approval.title }}</div>
                <div class="approval-meta">
                  <span class="approval-applicant">{{ approval.applicantName }}</span>
                  <span class="approval-time">{{ formatTime(approval.createTime) }}</span>
                </div>
              </div>
              <el-tag :type="getApprovalTypeTag(approval.type)" size="small">
                {{ approval.typeName }}
              </el-tag>
            </div>
            <el-empty v-if="!loadingApprovals && approvals.length === 0" description="暂无待审批" />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 应用中心 -->
    <div class="apps-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>应用中心</span>
            <el-button text>全部应用</el-button>
          </div>
        </template>
        <div v-loading="loadingApps" class="apps-grid">
          <div
            v-for="app in applications"
            :key="app.id"
            class="app-item"
            @click="handleAppClick(app)"
          >
            <div class="app-icon-wrap">
              <el-image v-if="app.icon" :src="app.icon" class="app-icon" />
              <div v-else class="app-icon-placeholder">
                {{ app.name?.charAt(0) }}
              </div>
            </div>
            <div class="app-name">{{ app.name }}</div>
          </div>
          <el-empty v-if="!loadingApps && applications.length === 0" description="暂无可用应用" />
        </div>
      </el-card>
    </div>

    <!-- 公告栏 -->
    <div class="announcements-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>公告通知</span>
          </div>
        </template>
        <div v-loading="loadingAnnouncements" class="announcement-list">
          <div
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-item"
          >
            <div class="announcement-header">
              <el-icon class="announcement-icon"><Bell /></el-icon>
              <span class="announcement-title">{{ announcement.title }}</span>
              <span class="announcement-time">{{ formatTime(announcement.createTime) }}</span>
            </div>
            <div class="announcement-content">{{ announcement.content }}</div>
          </div>
          <el-empty v-if="!loadingAnnouncements && announcements.length === 0" description="暂无公告" />
        </div>
      </el-card>
    </div>

    <!-- 创建待办对话框 -->
    <el-dialog
      v-model="showCreateTodoDialog"
      title="新建待办"
      width="500px"
    >
      <el-form :model="todoForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="todoForm.title" placeholder="请输入待办标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="todoForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入待办内容"
          />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="todoForm.dueDate"
            type="datetime"
            placeholder="选择截止日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="todoForm.priority">
            <el-radio label="LOW">低</el-radio>
            <el-radio label="MEDIUM">中</el-radio>
            <el-radio label="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTodoDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTodo">创建</el-button>
      </template>
    </el-dialog>

    <!-- 发起审批 -->
    <CreateApprovalDialog
      v-model="showCreateApproval"
      @success="handleApprovalSuccess"
    />

    <!-- 审批详情 -->
    <ApprovalDetailDialog
      v-model="showApprovalDetail"
      :approval-id="selectedApprovalId"
      @refresh="loadApprovals"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock,
  Document,
  Calendar,
  ChatDotRound,
  CircleCheck,
  Plus,
  Bell
} from '@element-plus/icons-vue'
import {
  getTodos,
  createTodo,
  completeTodo,
  getAnnouncements,
  getStatistics
} from '@/api/im/workbench'
import { getPendingApprovals, getMyApprovals } from '@/api/im/approval'
import { getTodayStatus, checkIn, checkOut } from '@/api/im/attendance'
import { getVisibleApplications } from '@/api/im/app'
import ApprovalDetailDialog from '@/components/Workplace/ApprovalDetailDialog.vue'
import CreateApprovalDialog from '@/components/Workplace/CreateApprovalDialog.vue'

const statistics = ref({
  pendingTodos: 0,
  pendingApprovals: 0,
  todayMeetings: 0,
  unreadMessages: 0
})

const currentTime = ref('')
const currentDate = ref('')
const todayAttendance = ref({
  checkIn: null,
  checkOut: null
})

const checkingIn = ref(false)
const checkingOut = ref(false)
const loadingTodos = ref(false)
const loadingApprovals = ref(false)
const loadingAnnouncements = ref(false)

const todos = ref([])
const approvals = ref([])
const announcements = ref([])
const applications = ref([])

const loadingApps = ref(false)

const showCreateTodoDialog = ref(false)
const showCreateApproval = ref(false)
const showApprovalDetail = ref(false)
const selectedApprovalId = ref(null)
const approvalTab = ref('pending') // pending | my

const todoForm = reactive({
  title: '',
  content: '',
  dueDate: null,
  priority: 'MEDIUM'
})

let timeInterval = null

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await getStatistics()
    if (response && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载待办列表
const loadTodos = async () => {
  loadingTodos.value = true
  try {
    const response = await getTodos({ pageNum: 1, pageSize: 10 })
    if (response && response.data) {
      todos.value = response.data.list || response.data
    }
  } catch (error) {
    console.error('加载待办列表失败:', error)
  } finally {
    loadingTodos.value = false
  }
}

// 加载审批列表
const loadApprovals = async () => {
  loadingApprovals.value = true
  try {
    const response = approvalTab.value === 'pending' 
      ? await getPendingApprovals() 
      : await getMyApprovals()
      
    if (response && response.code === 200) {
      approvals.value = response.data
    }
  } catch (error) {
    console.error('加载审批列表失败:', error)
  } finally {
    loadingApprovals.value = false
  }
}

const handleApprovalSuccess = () => {
  approvalTab.value = 'my'
  loadApprovals()
  loadStatistics()
}

// 加载公告列表
const loadAnnouncements = async () => {
  loadingAnnouncements.value = true
  try {
    const response = await getAnnouncements()
    if (response && response.data) {
      announcements.value = (response.data.list || response.data).slice(0, 5)
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
  } finally {
    loadingAnnouncements.value = false
  }
}

// 加载应用列表
const loadApplications = async () => {
  loadingApps.value = true
  try {
    const response = await getVisibleApplications()
    if (response && response.code === 200) {
      applications.value = response.data
    }
  } catch (error) {
    console.error('加载应用列表失败:', error)
  } finally {
    loadingApps.value = false
  }
}

// 处理应用点击
const handleAppClick = (app) => {
  if (app.appUrl) {
    if (app.appType === 'LINK') {
      window.open(app.appUrl, '_blank')
    } else {
      // 路由跳转
      // router.push(app.appUrl)
      ElMessage.info(`正在打开: ${app.name}`)
    }
  } else {
    ElMessage.info(`应用 ${app.name} 暂未配置地址`)
  }
}

// 加载考勤记录
const loadAttendance = async () => {
  try {
    const response = await getTodayStatus()
    if (response && response.data) {
      // Backend ImAttendance has id, checkInTime, checkOutTime, status
      // Mapping to todayAttendance format
      todayAttendance.value = {
        id: response.data.id,
        checkIn: response.data.checkInTime ? formatTimeOnly(response.data.checkInTime) : null,
        checkOut: response.data.checkOutTime ? formatTimeOnly(response.data.checkOutTime) : null
      }
    }
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  }
}

const formatTimeOnly = (fullTime) => {
  if (!fullTime) return null
  // assuming fullTime is "YYYY-MM-DD HH:mm:ss" or ISO
  const d = new Date(fullTime)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 上班打卡
const handleCheckIn = async () => {
  checkingIn.value = true
  try {
    await checkIn({
      location: JSON.stringify({ address: '办公室', latitude: 0, longitude: 0 }),
      deviceInfo: 'Web'
    })
    ElMessage.success('上班打卡成功')
    await loadAttendance()
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error('打卡失败')
  } finally {
    checkingIn.value = false
  }
}

// 下班打卡
const handleCheckOut = async () => {
  checkingOut.value = true
  try {
    await checkOut({
      location: JSON.stringify({ address: '办公室', latitude: 0, longitude: 0 }),
      deviceInfo: 'Web'
    })
    ElMessage.success('下班打卡成功')
    await loadAttendance()
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error('打卡失败')
  } finally {
    checkingOut.value = false
  }
}

// 创建待办
const handleCreateTodo = async () => {
  if (!todoForm.title) {
    ElMessage.warning('请输入待办标题')
    return
  }

  try {
    await createTodo(todoForm)
    ElMessage.success('待办创建成功')
    showCreateTodoDialog.value = false
    todoForm.title = ''
    todoForm.content = ''
    todoForm.dueDate = null
    todoForm.priority = 'MEDIUM'
    await loadTodos()
  } catch (error) {
    console.error('创建待办失败:', error)
    ElMessage.error('创建失败')
  }
}

// 完成待办
const handleTodoComplete = async (todo) => {
  try {
    await completeTodo(todo.id)
    ElMessage.success(todo.completed ? '待办已完成' : '待办已恢复')
  } catch (error) {
    console.error('操作失败:', error)
    todo.completed = !todo.completed
    ElMessage.error('操作失败')
  }
}

// 审批点击
const handleApprovalClick = (approval) => {
  selectedApprovalId.value = approval.id
  showApprovalDetail.value = true
}

// 获取审批类型标签
const getApprovalTypeTag = (type) => {
  const typeMap = {
    'LEAVE': 'warning',
    'EXPENSE': 'danger',
    'PURCHASE': 'primary',
    'OTHER': 'info'
  }
  return typeMap[type] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 组件挂载
onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  loadStatistics()
  loadTodos()
  loadApprovals()
  loadAnnouncements()
  loadAttendance()
  loadApplications()
})

// 组件卸载
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped lang="scss">
.workbench-panel {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100%;
  overflow-y: auto;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24px;
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }
}

.attendance-section {
  margin-bottom: 20px;

  .attendance-card {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .attendance-content {
    display: flex;
    align-items: center;
    gap: 24px;

    .attendance-time {
      flex: 1;

      .current-time {
        font-size: 48px;
        font-weight: 600;
        color: #262626;
        font-family: 'Courier New', monospace;
      }

      .current-date {
        font-size: 14px;
        color: #8c8c8c;
        margin-top: 8px;
      }
    }

    .attendance-actions {
      .attendance-complete {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #52c41a;
        font-size: 16px;

        .check-icon {
          font-size: 24px;
        }
      }
    }

    .attendance-records {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .record-item {
        display: flex;
        gap: 8px;
        font-size: 14px;

        .record-label {
          color: #8c8c8c;
        }

        .record-time {
          color: #262626;
          font-weight: 500;
        }
      }
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;

  .content-section {
    :deep(.el-card) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 500;
}

.todo-list {
  min-height: 200px;

  .todo-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    &.completed {
      .todo-title {
        text-decoration: line-through;
        color: #8c8c8c;
      }
    }

    .todo-content {
      flex: 1;

      .todo-title {
        font-size: 14px;
        color: #262626;
        margin-bottom: 8px;
      }

      .todo-meta {
        display: flex;
        align-items: center;
        gap: 8px;

        .todo-date {
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }
}

.approval-list {
  min-height: 200px;

  .approval-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    .approval-content {
      flex: 1;

      .approval-title {
        font-size: 14px;
        color: #262626;
        margin-bottom: 4px;
      }

      .approval-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.announcements-section {
  :deep(.el-card) {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .announcement-list {
    .announcement-item {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .announcement-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .announcement-icon {
          font-size: 16px;
          color: #0089ff;
        }

        .announcement-title {
          flex: 1;
          font-size: 14px;
          font-weight: 500;
          color: #262626;
        }

        .announcement-time {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      .announcement-content {
        font-size: 13px;
        color: #595959;
        line-height: 1.6;
        padding-left: 24px;
      }
    }
  }
}

.apps-section {
  margin-bottom: 20px;

  .apps-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 20px;
    padding: 10px;

    .app-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      padding: 12px;
      border-radius: 12px;

      &:hover {
        background-color: #f0f7ff;
        transform: translateY(-2px);
      }

      .app-icon-wrap {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .app-icon {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .app-icon-placeholder {
          width: 100%;
          height: 100%;
          background: linear-gradient(135deg, #409eff 0%, #1e80ff 100%);
          color: #fff;
          font-size: 20px;
          font-weight: 600;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .app-name {
        font-size: 13px;
        color: #262626;
        text-align: center;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 100%;
      }
    }
  }
}
</style>
