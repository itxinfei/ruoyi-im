<template>
  <div class="workbench-panel">
    <!-- 顶部欢迎区 -->
    <WorkbenchHeader
      :display-name="displayName"
      :refreshing="refreshing"
      @refresh="refreshAllData"
    />

    <!-- 内容区域 -->
    <el-scrollbar class="workbench-scroll-container">
      <div
        v-loading="initialLoading"
        class="workbench-content"
      >
        <!-- 统计概览 -->
        <section class="stats-overview-section">
          <div class="stats-grid">
            <WorkbenchStatCard
              type="primary"
              icon="task_alt"
              :value="overviewData.todoCount"
              label="待办事项"
              @click="navigateTo('todo')"
            />
            <WorkbenchStatCard
              type="success"
              icon="approval"
              :value="overviewData.approvalCount"
              label="待我审批"
              @click="navigateTo('approval')"
            />
            <WorkbenchStatCard
              type="warning"
              icon="mail"
              :value="overviewData.mailCount"
              label="未读邮件"
              @click="navigateTo('mail')"
            />
            <WorkbenchStatCard
              type="info"
              icon="people"
              :value="onlineUsersCount"
              label="在线同事"
              @click="navigateTo('contacts')"
            />
          </div>
        </section>

        <!-- 主内容网格 -->
        <div class="dashboard-grid">
          <!-- 左侧：核心业务 -->
          <div class="grid-column main-column">
            <TodoWidget
              :todos="todoList"
              :loading="loadingStates.todo"
              @toggle-complete="toggleTodoComplete"
              @more="navigateTo('todo')"
              @add="showTodoDialog = true"
            />
            <ApprovalWidget
              :approvals="approvalList"
              :loading="loadingStates.approval"
              @approve="handleApprove"
              @reject="handleReject"
              @more="navigateTo('approval')"
            />
          </div>

          <!-- 中间：协作与日程 -->
          <div class="grid-column center-column">
            <ScheduleWidget
              :schedules="scheduleList"
              :loading="loadingStates.schedule"
              @add="showScheduleDialog = true"
              @more="navigateTo('calendar')"
            />

            <!-- 快捷入口 -->
            <WorkbenchWidget
              title="常用入口"
              icon="grid_view"
            >
              <div class="quick-access-grid">
                <div
                  class="access-item"
                  @click="navigateTo('meeting')"
                >
                  <div class="access-icon bg-blue">
                    <span class="material-icons-outlined">videocam</span>
                  </div>
                  <span>视频会议</span>
                </div>
                <div
                  class="access-item"
                  @click="navigateTo('mail')"
                >
                  <div class="access-icon bg-orange">
                    <span class="material-icons-outlined">edit</span>
                  </div>
                  <span>写邮件</span>
                </div>
                <div
                  class="access-item"
                  @click="navigateTo('contacts')"
                >
                  <div class="access-icon bg-green">
                    <span class="material-icons-outlined">search</span>
                  </div>
                  <span>查找同事</span>
                </div>
                <div
                  class="access-item"
                  @click="navigateTo('documents')"
                >
                  <div class="access-icon bg-purple">
                    <span class="material-icons-outlined">folder_shared</span>
                  </div>
                  <span>知识库</span>
                </div>
              </div>
            </WorkbenchWidget>
          </div>

          <!-- 右侧：公告与资讯 -->
          <div class="grid-column side-column">
            <AnnouncementWidget
              :announcements="announcementList"
              :loading="loadingStates.announcement"
              @more="navigateTo('announcement')"
            />

            <!-- 未来会议 -->
            <WorkbenchWidget
              title="近期会议"
              icon="meeting_room"
              more-text="查看"
              @more="navigateTo('meeting')"
            >
              <div
                v-if="meetingList.length > 0"
                class="mini-meeting-list"
              >
                <div
                  v-for="m in meetingList.slice(0, 3)"
                  :key="m.id"
                  class="mini-meeting-item"
                >
                  <span class="m-time">{{ dayjs(m.scheduledStartTime).format('MM-DD HH:mm') }}</span>
                  <p class="m-title">
                    {{ m.title }}
                  </p>
                </div>
              </div>
              <div
                v-else
                class="widget-empty-mini"
              >
                无近期安排
              </div>
            </WorkbenchWidget>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- 弹窗 -->
    <el-dialog
      v-model="showTodoDialog"
      title="新建待办"
      width="460px"
      destroy-on-close
      class="workbench-dialog"
    >
      <el-form
        :model="newTodo"
        label-position="top"
      >
        <el-form-item label="待办事项">
          <el-input
            v-model="newTodo.title"
            placeholder="想做什么？"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="newTodo.priority">
            <el-radio label="high">
              紧急
            </el-radio>
            <el-radio label="medium">
              普通
            </el-radio>
            <el-radio label="low">
              低
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="newTodo.dueDate"
            type="datetime"
            placeholder="可选"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTodoDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="todoSubmitting"
          @click="createNewTodo"
        >
          创建
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showScheduleDialog"
      title="新建日程"
      width="460px"
      destroy-on-close
      class="workbench-dialog"
    >
      <el-form
        :model="newSchedule"
        label-position="top"
      >
        <el-form-item label="日程标题">
          <el-input
            v-model="newSchedule.title"
            placeholder="活动名称"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="scheduleTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input
            v-model="newSchedule.location"
            placeholder="会议室/线上链接"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScheduleDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="scheduleSubmitting"
          @click="createNewSchedule"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 基础组件
import WorkbenchHeader from '@/components/Workbench/WorkbenchHeader.vue'
import WorkbenchStatCard from '@/components/Workbench/WorkbenchStatCard.vue'
import WorkbenchWidget from '@/components/Workbench/WorkbenchWidget.vue'

// 业务挂件
import TodoWidget from '@/components/Workbench/TodoWidget.vue'
import ApprovalWidget from '@/components/Workbench/ApprovalWidget.vue'
import ScheduleWidget from '@/components/Workbench/ScheduleWidget.vue'
import AnnouncementWidget from '@/components/Workbench/AnnouncementWidget.vue'

// 导入 API
import { getTodoList, getTodoCount, completeTodo as completeTodoApi } from '@/api/im/todo'
import { getPendingApprovals, handleApproval as handleApprovalApi } from '@/api/im/approval'
import { getLatestAnnouncements } from '@/api/im/announcement'
import { getUnreadCount } from '@/api/im/mail'
import { getEventsByTimeRange, createEvent } from '@/api/im/schedule'
import { getMeetingList } from '@/api/im/meeting'
import { getOverview, createTodo as createTodoApi } from '@/api/im/workbench'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const store = useStore()

// ============================================================================
// 状态管理
// ============================================================================
const initialLoading = ref(true)
const refreshing = ref(false)
const refreshTimer = ref(null)

// 弹窗状态 (后续可以进一步重构为独立组件)
const showTodoDialog = ref(false)
const showScheduleDialog = ref(false)
const todoSubmitting = ref(false)
const scheduleSubmitting = ref(false)

// 新建表单
const newTodo = ref({ title: '', priority: 'medium', dueDate: null })
const newSchedule = ref({ title: '', location: '' })
const scheduleTimeRange = ref([])

// ============================================================================
// 数据状态
// ============================================================================
const loadingStates = ref({
  todo: false,
  approval: false,
  announcement: false,
  schedule: false,
  meeting: false
})

const overviewData = ref({
  todoCount: 0,
  approvalCount: 0,
  mailCount: 0,
  onlineCount: 0
})

const todoList = ref([])
const approvalList = ref([])
const announcementList = ref([])
const scheduleList = ref([])
const meetingList = ref([])

// ============================================================================
// 计算属性
// ============================================================================
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const displayName = computed(() => currentUser.value.nickname || currentUser.value.username || '用户')
const onlineUsersCount = computed(() => (store.getters['contact/onlineUsers'] || []).length)

// ============================================================================
// 数据加载逻辑
// ============================================================================

const loadOverviewData = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200 && res.data) {
      overviewData.value = {
        todoCount: res.data.todoCount || 0,
        approvalCount: res.data.approvalCount || 0,
        mailCount: res.data.mailCount || 0,
        onlineCount: res.data.onlineCount || 0
      }
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
}

const loadTodoData = async () => {
  loadingStates.value.todo = true
  try {
    const res = await getTodoList('TODO')
    if (res.code === 200 && res.data) {
      todoList.value = (Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || []))
        .map(item => ({
          ...item,
          priority: item.priority || 'medium',
          completed: item.isCompleted === true || item.status === 'COMPLETED'
        }))
    }
  } catch (error) {
    console.error('加载待办失败:', error)
  } finally {
    loadingStates.value.todo = false
  }
}

const loadApprovalData = async () => {
  loadingStates.value.approval = true
  try {
    const res = await getPendingApprovals()
    if (res.code === 200 && res.data) {
      approvalList.value = (Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || []))
      overviewData.value.approvalCount = approvalList.value.length
    }
  } catch (error) {
    console.error('加载审批失败:', error)
  } finally {
    loadingStates.value.approval = false
  }
}

const loadAnnouncementData = async () => {
  loadingStates.value.announcement = true
  try {
    const res = await getLatestAnnouncements(5)
    if (res.code === 200 && res.data) {
      announcementList.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  } finally {
    loadingStates.value.announcement = false
  }
}

const loadScheduleData = async () => {
  loadingStates.value.schedule = true
  try {
    const today = dayjs().startOf('day')
    const res = await getEventsByTimeRange(today.format('YYYY-MM-DD HH:mm:ss'), today.add(1, 'day').format('YYYY-MM-DD HH:mm:ss'))
    if (res.code === 200 && res.data) {
      scheduleList.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
    }
  } catch (error) {
    console.error('加载日程失败:', error)
  } finally {
    loadingStates.value.schedule = false
  }
}

const loadMeetingData = async () => {
  try {
    const res = await getMeetingList('SCHEDULED')
    if (res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
      const now = dayjs()
      meetingList.value = list.filter(m => dayjs(m.scheduledStartTime || m.startTime).isBefore(now.add(7, 'day')))
    }
  } catch (error) {
    console.error('加载会议失败:', error)
  }
}

const loadAllData = async () => {
  await Promise.all([
    loadOverviewData(),
    loadTodoData(),
    loadApprovalData(),
    loadAnnouncementData(),
    loadScheduleData(),
    loadMeetingData()
  ])
}

const refreshAllData = async () => {
  refreshing.value = true
  try {
    await loadAllData()
    ElMessage.success('数据已刷新')
  } finally {
    refreshing.value = false
  }
}

// ============================================================================
// 事件处理
// ============================================================================

const toggleTodoComplete = async todo => {
  try {
    await completeTodoApi(todo.id)
    ElMessage.success(todo.completed ? '待办已重启' : '待办已完成')
    await loadTodoData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleApprove = async approval => {
  try {
    await ElMessageBox.confirm('确认通过该审批吗？', '系统提示')
    await handleApprovalApi({ approvalId: approval.id, action: 'APPROVE', comment: '同意' })
    ElMessage.success('审批已通过')
    await loadApprovalData()
  } catch (e) { if (e !== 'cancel') {ElMessage.error('操作失败')} }
}

const handleReject = async approval => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回理由', '审批驳回')
    await handleApprovalApi({ approvalId: approval.id, action: 'REJECT', comment: value })
    ElMessage.success('审批已驳回')
    await loadApprovalData()
  } catch (e) { if (e !== 'cancel') {ElMessage.error('操作失败')} }
}

const createNewTodo = async () => {
  if (!newTodo.value.title) {return ElMessage.warning('请输入标题')}
  todoSubmitting.value = true

  // 优先级映射: high -> 3, medium -> 2, low -> 1
  const priorityMap = { high: 3, medium: 2, low: 1 }

  try {
    await createTodoApi({
      title: newTodo.value.title,
      priority: priorityMap[newTodo.value.priority] || 2,
      description: '' // 后端支持 description
    })
    ElMessage.success('待办创建成功')
    showTodoDialog.value = false
    newTodo.value = { title: '', priority: 'medium', dueDate: null } // 重置表单
    await loadTodoData()
  } finally { todoSubmitting.value = false }
}

const createNewSchedule = async () => {
  if (!newSchedule.value.title) {return ElMessage.warning('请输入标题')}
  scheduleSubmitting.value = true
  try {
    await createEvent({
      title: newSchedule.value.title,
      startTime: dayjs(scheduleTimeRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(scheduleTimeRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
      location: newSchedule.value.location
    })
    ElMessage.success('日程创建成功')
    showScheduleDialog.value = false
    await loadScheduleData()
  } finally { scheduleSubmitting.value = false }
}

const navigateTo = tab => window.dispatchEvent(new CustomEvent('switch-tab', { detail: tab }))

onMounted(async () => {
  await loadAllData()
  initialLoading.value = false
  refreshTimer.value = setInterval(loadAllData, 5 * 60 * 1000)
})

onUnmounted(() => refreshTimer.value && clearInterval(refreshTimer.value))
</script>

<style scoped lang="scss">
.workbench-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
  overflow: hidden;
}

.workbench-scroll-container {
  flex: 1;
}

.workbench-content {
  padding: 32px 60px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 340px 300px;
  gap: 24px;
  align-items: start;
}

.grid-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 快捷入口网格
.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.access-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 10px;
  background: var(--dt-bg-subtle);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-lighter);
    transform: translateY(-2px);

    .access-icon {
      transform: scale(1.1);
    }
  }

  span {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-secondary);
  }
}

.access-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);

  .material-icons-outlined {
    font-size: 22px;
    color: #fff;
  }

  &.bg-blue {
    background: linear-gradient(135deg, #60a5fa, #2563eb);
  }

  &.bg-orange {
    background: linear-gradient(135deg, #fbbf24, #d97706);
  }

  &.bg-green {
    background: linear-gradient(135deg, #34d399, #059669);
  }

  &.bg-purple {
    background: linear-gradient(135deg, #a78bfa, #7c3aed);
  }
}

// 会议简项
.mini-meeting-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-meeting-item {
  padding: 8px 12px;
  background: var(--dt-bg-subtle-hover);
  border-radius: var(--dt-radius-md);

  .m-time {
    font-size: 11px;
    color: var(--dt-brand-color);
    font-weight: 600;
  }

  .m-title {
    font-size: 13px;
    color: var(--dt-text-primary);
    margin: 4px 0 0 0;
    font-weight: 500;
  }
}

.widget-empty-mini {
  padding: 20px;
  text-align: center;
  font-size: 12px;
  color: var(--dt-text-quaternary);
}

// 对话框样式适配
.workbench-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);

    .el-dialog__header {
      padding: 20px 24px;
      border-bottom: 1px solid var(--dt-border-light);
    }

    .el-dialog__body {
      padding: 24px;
    }
  }
}

@media (max-width: 1400px) {
  .dashboard-grid {
    grid-template-columns: 1fr 300px;
  }

  .side-column {
    grid-column: span 2;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .side-column {
    grid-template-columns: 1fr;
  }

  .workbench-content {
    padding: 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
```
