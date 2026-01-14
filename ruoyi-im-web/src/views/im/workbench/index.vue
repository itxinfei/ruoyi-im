<template>
  <div class="workbench-page">
    <!-- 工作台头部导航 -->
    <div class="workbench-nav">
      <div
        v-for="tab in workbenchTabs"
        :key="tab.key"
        class="nav-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span class="nav-label">{{ tab.label }}</span>
        <span v-if="tab.badge > 0" class="nav-badge">{{ tab.badge }}</span>
      </div>
    </div>

    <!-- 工作台内容区 -->
    <div class="workbench-body">
      <!-- 首页概览 -->
      <div v-show="activeTab === 'overview'" class="tab-content">
        <!-- 概览卡片 -->
        <div class="overview-cards">
          <div
            v-for="card in overviewCards"
            :key="card.key"
            class="overview-card"
            :class="card.className"
            @click="handleCardClick(card.key)"
          >
            <div class="card-icon" :style="{ background: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
            </div>
            <div class="card-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="todo-section">
          <div class="section-header">
            <h3>待办事项</h3>
            <el-button link type="primary" :icon="Plus" @click="showAddTodoDialog">新建待办</el-button>
          </div>
          <div class="todo-list">
            <div
              v-for="todo in todoList"
              :key="todo.id"
              class="todo-item"
              :class="{ completed: todo.isCompleted }"
            >
              <el-checkbox :model-value="todo.isCompleted" @change="handleTodoComplete(todo)" />
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div v-if="todo.description" class="todo-desc">{{ todo.description }}</div>
              </div>
              <el-tag :type="getPriorityType(todo.priority)" size="small">
                {{ getPriorityText(todo.priority) }}
              </el-tag>
              <el-button link type="danger" :icon="Delete" @click="handleTodoDelete(todo)" />
            </div>
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" :image-size="60" />
          </div>
        </div>

        <!-- 快捷应用 -->
        <div class="quick-apps-section">
          <div class="section-header">
            <h3>快捷应用</h3>
          </div>
          <div class="quick-apps-grid">
            <div
              v-for="app in quickApps"
              :key="app.key"
              class="quick-app-item"
              @click="handleAppClick(app)"
            >
              <div class="app-icon" :style="{ backgroundColor: app.color }">
                <span class="app-char">{{ app.name.charAt(0) }}</span>
              </div>
              <span class="app-name">{{ app.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 考勤打卡 -->
      <div v-show="activeTab === 'attendance'" class="tab-content">
        <Attendance />
      </div>

      <!-- 审批中心 -->
      <div v-show="activeTab === 'approval'" class="tab-content">
        <Approval />
      </div>

      <!-- 日程管理 -->
      <div v-show="activeTab === 'schedule'" class="tab-content">
        <Schedule />
      </div>

      <!-- 工作报告 -->
      <div v-show="activeTab === 'report'" class="tab-content">
        <Report />
      </div>
    </div>

    <!-- 新建待办对话框 -->
    <el-dialog v-model="addTodoVisible" title="新建待办" width="500px">
      <el-form :model="todoForm" label-width="80px">
        <el-form-item label="待办标题">
          <el-input v-model="todoForm.title" placeholder="请输入待办标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="todoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="todoForm.priority">
            <el-radio label="HIGH">高</el-radio>
            <el-radio label="NORMAL">普通</el-radio>
            <el-radio label="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTodoVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddTodo">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Grid,
  Timer,
  Document,
  Calendar,
  ChatDotRound,
  Bell,
  Plus,
  Delete,
  ArrowRight,
  OfficeBuilding,
  Ticket,
  Edit
} from '@element-plus/icons-vue'
import {
  getWorkbenchOverview,
  getTodos,
  createTodo,
  completeTodo,
  deleteTodo,
} from '@/api/im/workbench'
import Attendance from './Attendance.vue'
import Approval from './Approval.vue'
import Schedule from './Schedule.vue'
import Report from './Report.vue'

const router = useRouter()

// State
const activeTab = ref('overview')
const addTodoVisible = ref(false)
const todoForm = ref({
  title: '',
  description: '',
  priority: 'NORMAL',
})

// 工作台标签
const workbenchTabs = ref([
  { key: 'overview', label: '首页', icon: Grid, badge: 0 },
  { key: 'attendance', label: '考勤', icon: OfficeBuilding, badge: 0 },
  { key: 'approval', label: '审批', icon: Document, badge: 5 },
  { key: 'schedule', label: '日程', icon: Calendar, badge: 0 },
  { key: 'report', label: '汇报', icon: Edit, badge: 0 },
])

// 概览卡片
const overviewCards = ref([
  {
    key: 'todo',
    label: '待办事项',
    value: 0,
    icon: Timer,
    color: '#1890FF',
    className: 'card-todo',
  },
  {
    key: 'message',
    label: '未读消息',
    value: 0,
    icon: ChatDotRound,
    color: '#52c41a',
    className: 'card-message',
  },
  {
    key: 'approval',
    label: '待审批',
    value: 0,
    icon: Document,
    color: '#faad14',
    className: 'card-approval',
  },
  {
    key: 'notice',
    label: '系统通知',
    value: 0,
    icon: Bell,
    color: '#ff4d4f',
    className: 'card-notice',
  },
])

// 待办列表
const todoList = ref([])

// 快捷应用
const quickApps = ref([
  { key: 'approval', name: '审批中心', color: '#1890FF', tab: 'approval' },
  { key: 'attendance', name: '考勤打卡', color: '#52c41a', tab: 'attendance' },
  { key: 'schedule', name: '日程管理', color: '#faad14', tab: 'schedule' },
  { key: 'report', name: '工作报告', color: '#722ed1', tab: 'report' },
  { key: 'contacts', name: '联系人', color: '#eb2f96', route: '/im/contacts' },
  { key: 'group', name: '群组', color: '#13c2c2', route: '/im/group' },
])

// 方法
const switchTab = tab => {
  activeTab.value = tab
}

const handleCardClick = key => {
  const tabMap = {
    todo: 'overview',
    message: 'overview',
    approval: 'approval',
    notice: 'overview',
  }
  if (key === 'approval') {
    switchTab('approval')
  }
}

const handleAppClick = app => {
  if (app.tab) {
    switchTab(app.tab)
  } else if (app.route) {
    router.push(app.route)
  }
}

const showAddTodoDialog = () => {
  todoForm.value = {
    title: '',
    description: '',
    priority: 'NORMAL',
  }
  addTodoVisible.value = true
}

const handleAddTodo = async () => {
  if (!todoForm.value.title) {
    ElMessage.warning('请输入待办标题')
    return
  }
  try {
    await createTodo(todoForm.value)
    ElMessage.success('创建成功')
    addTodoVisible.value = false
    loadTodos()
    loadOverview()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleTodoComplete = async todo => {
  if (todo.isCompleted) return
  try {
    await completeTodo(todo.id)
    ElMessage.success('已完成')
    loadTodos()
    loadOverview()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleTodoDelete = async todo => {
  try {
    await deleteTodo(todo.id)
    ElMessage.success('删除成功')
    loadTodos()
    loadOverview()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const getPriorityType = priority => {
  // 后端返回整数：1=低, 2=中, 3=高
  const map = { 1: 'success', 2: 'info', 3: 'danger' }
  return map[priority] || 'info'
}

const getPriorityText = priority => {
  // 后端返回整数：1=低, 2=中, 3=高
  const map = { 1: '低', 2: '普通', 3: '高' }
  return map[priority] || '普通'
}

const loadOverview = async () => {
  try {
    const response = await getWorkbenchOverview()
    if (response.code === 200 && response.data) {
      overviewCards.value[0].value = response.data.todoCount || 0
      overviewCards.value[1].value = response.data.unreadMessageCount || 0
      overviewCards.value[2].value = response.data.approvalCount || 0
      overviewCards.value[3].value = response.data.noticeCount || 0

      // 更新审批徽章
      workbenchTabs.value[2].badge = response.data.approvalCount || 0
    }
  } catch (error) {
    console.error('获取概览失败:', error)
  }
}

const loadTodos = async () => {
  try {
    const response = await getTodos()
    if (response.code === 200) {
      // 后端返回的数据格式转换
      todoList.value = (response.data || []).map(todo => ({
        ...todo,
        isCompleted: todo.status === 'COMPLETED',
      }))
    }
  } catch (error) {
    console.error('获取待办列表失败:', error)
  }
}

onMounted(() => {
  loadOverview()
  loadTodos()
})
</script>

<style lang="scss" scoped>
.workbench-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #F5F7FA;
}

// 工作台导航
.workbench-nav {
  display: flex;
  gap: 8px;
  padding: 12px 16px; // 修改：20px -> 16px（符合4的倍数）
  background: white;
  border-bottom: 1px solid #E8E8E8;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px; // 修改：6px -> 8px（符合4的倍数）
  padding: 8px 16px;
  border-radius: 4px; // 修改：6px -> 4px（符合规范）
  cursor: pointer;
  color: #666666; // 修改：#666 -> 规范值
  transition: all 0.2s;
  font-size: 14px;

  &:hover {
    background: #F5F7FA;
  }

  &.active {
    background: #E6F7FF;
    color: #1890FF; // 修改：#1677ff -> #1890FF（钉钉规范）
    font-weight: 500;
  }
}

.nav-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #FF4D4F;
  color: white;
  border-radius: 8px;
  font-size: 11px;
}

// 内容区
.workbench-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.tab-content {
  min-height: 100%;
}

// 概览卡片
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px; // 修改：10px -> 8px（符合规范）
  color: white;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: 500; // 修改：600 -> 500（符合规范）
  color: #262626;
  line-height: 1.2;
}

.card-label {
  font-size: 12px; // 修改：13px -> 12px（辅助文字规范）
  color: #999999; // 修改：#8c8c8c -> #999999（辅助文字规范）
  margin-top: 4px; // 修改：2px -> 4px（符合4的倍数）
}

.card-arrow {
  color: #CCCCCC;
}

// 待办事项
.todo-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  h3 {
    margin: 0;
    font-size: 16px; // 修改：15px -> 16px（小标题规范）
    font-weight: 500;
    color: #262626;
  }
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px; // 修改：10px -> 12px（符合4的倍数）
  border-radius: 4px; // 修改：6px -> 4px（符合规范）
  margin-bottom: 8px; // 修改：6px -> 8px（符合4的倍数）
  transition: background 0.2s;

  &:hover {
    background: #F5F7FA;
  }

  &.completed {
    opacity: 0.6;

    .todo-title {
      text-decoration: line-through;
    }
  }
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  color: #262626;
}

.todo-desc {
  font-size: 12px;
  color: #999999; // 修改：#8c8c8c -> #999999（辅助文字规范）
  margin-top: 4px; // 修改：2px -> 4px（符合4的倍数）
}

// 快捷应用
.quick-apps-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
}

.quick-apps-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.quick-app-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F5F7FA;
  }
}

.app-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px; // 修改：10px -> 8px（符合规范）
  margin-bottom: 8px;
  color: white;
  font-size: 18px;
  font-weight: 500; // 修改：600 -> 500（符合规范）
}

.app-name {
  font-size: 12px; // 修改：13px -> 12px（辅助文字规范）
  color: #666666; // 修改：#595959 -> #666666（次要文字规范）
}

@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-apps-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .workbench-nav {
    overflow-x: auto;
    white-space: nowrap;
  }

  .overview-cards {
    grid-template-columns: 1fr;
  }

  .quick-apps-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
