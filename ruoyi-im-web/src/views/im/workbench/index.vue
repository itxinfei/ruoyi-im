<template>
  <div class="workbench-container">
    <!-- 顶部概览卡片 -->
    <div class="overview-section">
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in overviewStats" :key="item.key">
          <div class="stat-card" :class="item.className">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主内容区 -->
    <el-row :gutter="16" class="content-section">
      <!-- 左侧：待办事项 -->
      <el-col :span="14">
        <div class="content-card">
          <div class="card-header">
            <span class="card-title">待办事项</span>
            <el-button link type="primary" :icon="Plus" @click="showAddTodoDialog">新建待办</el-button>
          </div>
          <div class="todo-list">
            <div
              v-for="todo in todoList"
              :key="todo.id"
              class="todo-item"
              :class="{ completed: todo.isCompleted }"
            >
              <div class="todo-checkbox">
                <el-checkbox
                  :model-value="todo.isCompleted"
                  @change="handleTodoComplete(todo)"
                />
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-desc" v-if="todo.description">{{ todo.description }}</div>
              </div>
              <div class="todo-actions">
                <el-tag :type="getPriorityType(todo.priority)" size="small">
                  {{ getPriorityText(todo.priority) }}
                </el-tag>
                <el-button link type="danger" :icon="Delete" @click="handleTodoDelete(todo)" />
              </div>
            </div>
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" :image-size="80" />
          </div>
        </div>
      </el-col>

      <!-- 右侧：快捷应用 -->
      <el-col :span="10">
        <div class="content-card">
          <div class="card-header">
            <span class="card-title">快捷应用</span>
          </div>
          <div class="app-grid">
            <div
              v-for="app in quickApps"
              :key="app.id"
              class="app-item"
              @click="handleAppClick(app)"
            >
              <div class="app-icon" :style="{ backgroundColor: app.color }">
                <span class="app-icon-text">{{ app.name.charAt(0) }}</span>
              </div>
              <div class="app-name">{{ app.name }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  ChatDotRound,
  Timer,
  Bell,
  Plus,
  Delete,
} from '@element-plus/icons-vue'
import { getWorkbenchOverview, getTodos, createTodo, completeTodo, deleteTodo } from '@/api/im/workbench'

const overviewStats = ref([
  { key: 'todo', label: '待办事项', value: 0, icon: Timer, color: '#1677ff', className: 'stat-todo' },
  { key: 'message', label: '未读消息', value: 0, icon: ChatDotRound, color: '#52c41a', className: 'stat-message' },
  { key: 'approval', label: '待审批', value: 0, icon: Document, color: '#faad14', className: 'stat-approval' },
  { key: 'notice', label: '系统通知', value: 0, icon: Bell, color: '#ff4d4f', className: 'stat-notice' },
])

const todoList = ref([])

const quickApps = ref([
  { id: 1, name: '审批中心', code: 'approval', color: '#1677ff' },
  { id: 2, name: '应用中心', code: 'app-center', color: '#52c41a' },
  { id: 3, name: '群组', code: 'group', color: '#faad14' },
  { id: 4, name: '文件', code: 'file', color: '#722ed1' },
  { id: 5, name: '联系人', code: 'contacts', color: '#eb2f96' },
  { id: 6, name: '设置', code: 'settings', color: '#8c8c8c' },
])

const addTodoVisible = ref(false)
const todoForm = reactive({
  title: '',
  description: '',
  priority: 'NORMAL',
})

const loadOverview = async () => {
  try {
    const response = await getWorkbenchOverview()
    if (response.code === 200 && response.data) {
      overviewStats.value[0].value = response.data.todoCount || 0
      overviewStats.value[1].value = response.data.messageCount || 0
      overviewStats.value[2].value = response.data.approvalCount || 0
      overviewStats.value[3].value = response.data.noticeCount || 0
    }
  } catch (error) {
    console.error('获取概览失败:', error)
  }
}

const loadTodos = async () => {
  try {
    const response = await getTodos()
    if (response.code === 200) {
      todoList.value = response.data || []
    }
  } catch (error) {
    console.error('获取待办列表失败:', error)
  }
}

const showAddTodoDialog = () => {
  todoForm.title = ''
  todoForm.description = ''
  todoForm.priority = 'NORMAL'
  addTodoVisible.value = true
}

const handleAddTodo = async () => {
  if (!todoForm.title) {
    ElMessage.warning('请输入待办标题')
    return
  }
  try {
    await createTodo(todoForm)
    ElMessage.success('创建成功')
    addTodoVisible.value = false
    loadTodos()
    loadOverview()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleTodoComplete = async (todo) => {
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

const handleTodoDelete = async (todo) => {
  try {
    await deleteTodo(todo.id)
    ElMessage.success('删除成功')
    loadTodos()
    loadOverview()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleAppClick = (app) => {
  // 跳转到对应页面
  console.log('点击应用:', app)
}

const getPriorityType = (priority) => {
  const map = { HIGH: 'danger', NORMAL: 'info', LOW: 'success' }
  return map[priority] || 'info'
}

const getPriorityText = (priority) => {
  const map = { HIGH: '高', NORMAL: '普通', LOW: '低' }
  return map[priority] || '普通'
}

onMounted(() => {
  loadOverview()
  loadTodos()
})
</script>

<style lang="scss" scoped>
.workbench-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.overview-section {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.content-section {
  margin-top: 16px;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.todo-list {
  max-height: 400px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s;
  margin-bottom: 8px;

  &:hover {
    background: #f5f5f5;
  }

  &.completed {
    opacity: 0.6;

    .todo-title {
      text-decoration: line-through;
    }
  }
}

.todo-checkbox {
  margin-right: 12px;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #333;
}

.todo-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.app-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
  }
}

.app-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.app-name {
  font-size: 13px;
  color: #666;
}
</style>
