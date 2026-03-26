<template>
  <div class="todo-panel">
    <!-- 1. 顶部操作区 -->
    <header class="panel-header">
      <div class="header-left">
        <h2 class="title">待办事项</h2>
        <span class="count">({{ pendingCount }} 条待处理)</span>
      </div>
      <div class="header-right">
        <el-button type="primary" size="small" @click="openCreateDialog">
          <i class="icon-plus"></i> 新建待办
        </el-button>
      </div>
    </header>

    <!-- 2. 状态切换 Tab (钉钉风格) -->
    <nav class="status-tabs">
      <span 
        v-for="tab in ['进行中', '已完成']" 
        :key="tab"
        :class="['tab-item', { active: activeTab === tab }]"
        @click="activeTab = tab"
      >
        {{ tab }}
      </span>
    </nav>

    <!-- 3. 待办列表 (高密度布局) -->
    <div class="todo-list" v-loading="loading">
      <div v-if="todoList.length === 0" class="empty-state">
        <img src="/images/empty-todo.svg" alt="empty" class="empty-image" />
        <p>干净利落，没有待办</p>
      </div>

      <div 
        v-for="todo in sortedTodoList" 
        :key="todo.id"
        class="todo-item"
        :class="{ 'is-done': todo.status === 'DONE' }"
      >
        <!-- 复选框 -->
        <div class="checkbox-wrapper" @click="processToggle(todo)">
          <div class="custom-checkbox">
            <i v-if="todo.status === 'DONE'" class="icon-check"></i>
          </div>
        </div>

        <div class="todo-content">
          <div class="todo-title">{{ todo.content }}</div>
          <div class="todo-meta">
            <span v-if="todo.deadline" class="deadline" :class="{ 'is-overdue': isOverdue(todo.deadline) }">
              <i class="icon-time"></i> {{ formatDeadline(todo.deadline) }}
            </span>
            <span v-if="todo.source" class="source">
              来自: {{ todo.source }}
            </span>
          </div>
        </div>

        <div class="todo-actions">
          <i class="icon-delete" @click="processDelete(todo.id)"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * TodoPanel.vue
 * 严格对齐 Doc-21 协同布局与 Doc-35 视觉契约
 */
import { ref, computed, onMounted } from 'vue';
import { todoApi } from '@/api/todo';
import { ElMessage, ElMessageBox } from 'element-plus';

// 1. 核心状态
const activeTab = ref('进行中');
const loading = ref(false);
const todoList = ref([]);

// 2. 数据计算
const pendingCount = computed(() => {
  return todoList.value.filter(t => t.status === 'PENDING').length;
});

const sortedTodoList = computed(() => {
  const targetStatus = activeTab.value === '进行中' ? 'PENDING' : 'DONE';
  return todoList.value.filter(t => t.status === targetStatus)
    .sort((a, b) => b.createTime - a.createTime);
});

// 3. 业务逻辑
const fetchTodos = async () => {
  loading.value = true;
  try {
    const res = await todoApi.getList();
    todoList.value = res.responseBody || [];
  } finally {
    loading.value = false;
  }
};

const processToggle = async (todo) => {
  // 乐观更新
  const originalStatus = todo.status;
  todo.status = todo.status === 'DONE' ? 'PENDING' : 'DONE';
  
  try {
    await todoApi.toggleStatus(todo.id);
    ElMessage.success(todo.status === 'DONE' ? '标记完成' : '已恢复');
  } catch (e) {
    todo.status = originalStatus; // 回滚
  }
};

const processDelete = (id) => {
  ElMessageBox.confirm('确定删除该待办吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await todoApi.delete(id);
    todoList.value = todoList.value.filter(t => t.id !== id);
    ElMessage.success('已删除');
  });
};

// 工具
const isOverdue = (deadline) => {
  return deadline && deadline < new Date().getTime();
};

const formatDeadline = (ts) => {
  const d = new Date(ts);
  return `${d.getMonth() + 1}月${d.getDate()}日`;
};

const openCreateDialog = () => {
  // 逻辑由父组件 Dialog 承载
};

onMounted(fetchTodos);
</script>

<style scoped>
.todo-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--dt-bg-body);
}

.panel-header {
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title { font-size: 18px; font-weight: 600; color: var(--dt-text-main); }
.count { font-size: 13px; color: var(--dt-text-desc); margin-left: 8px; }

.status-tabs {
  padding: 0 24px;
  display: flex;
  gap: 24px;
  border-bottom: 1px solid var(--dt-border-color);
}

.tab-item {
  height: 40px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--dt-text-main);
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: var(--dt-brand-color);
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: var(--dt-brand-color);
}

.todo-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 24px;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.todo-item:hover {
  background-color: var(--dt-bg-hover);
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

/* 钉钉风格复选框 */
.custom-checkbox {
  width: 18px;
  height: 18px;
  border: 1px solid var(--dt-border-color);
  border-radius: 4px;
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.is-done .custom-checkbox {
  background-color: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
}

.icon-check { color: var(--dt-text-white); font-size: 12px; }

.todo-content { flex: 1; min-width: 0; }

.todo-title {
  font-size: 14px;
  color: var(--dt-text-main);
  line-height: 1.5;
  word-break: break-all;
}

.is-done .todo-title {
  color: var(--dt-text-desc);
  text-decoration: line-through;
}

.todo-meta {
  margin-top: 4px;
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.deadline { color: var(--dt-text-desc); display: flex; align-items: center; gap: 4px; }
.deadline.is-overdue { color: var(--dt-error-color); }

.source { color: var(--dt-brand-color); background: var(--dt-brand-bg); padding: 0 4px; border-radius: 2px; }

.todo-actions {
  opacity: 0;
  transition: opacity 0.2s;
  color: var(--dt-text-desc);
}

.todo-actions i:hover { color: var(--dt-error-color); }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 80px;
  color: var(--dt-text-desc);
}
.empty-state img { width: 120px; opacity: 0.5; margin-bottom: 16px; }
</style>
