<template>
  <div class="todo-panel">
    <!-- 头部区域 -->
    <div class="panel-header">
      <div class="header-left">
        <div class="title-wrapper">
          <h2 class="panel-title">待办事项</h2>
          <div class="title-badge">
            <div class="badge-icon-wrapper">
              <span class="material-icons-outlined badge-icon">task_alt</span>
              <div class="icon-glow"></div>
            </div>
            <span class="badge-count">{{ todos.filter(t => !t.completed).length }}</span>
          </div>
        </div>
        <!-- 搜索框 -->
        <div class="search-wrapper" v-if="!batchMode">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchInput"
            type="text"
            class="search-input"
            placeholder="搜索待办..."
            @input="handleSearchInput"
          />
        </div>
      </div>
      <div class="header-actions">
        <!-- 批量操作按钮 -->
        <template v-if="batchMode">
          <span class="selected-count">已选 {{ selectedTodos.size }} 项</span>
          <button class="batch-btn batch-btn--complete" @click="batchComplete" :disabled="selectedTodos.size === 0">
            <span class="material-icons-outlined">check_circle</span>
            <span>完成</span>
          </button>
          <button class="batch-btn batch-btn--danger" @click="batchDelete" :disabled="selectedTodos.size === 0">
            <span class="material-icons-outlined">delete</span>
            <span>删除</span>
          </button>
          <button class="batch-btn batch-btn--cancel" @click="exitBatchMode">
            <span class="material-icons-outlined">close</span>
            <span>取消</span>
          </button>
        </template>
        <template v-else>
          <button class="icon-btn" @click="enterBatchMode" title="批量操作">
            <span class="material-icons-outlined">checklist</span>
            <span>批量</span>
          </button>
          <button class="add-btn" @click="showAddDialog = true">
            <span class="material-icons-outlined">add</span>
            <span>新建待办</span>
          </button>
        </template>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            list
          </span>
          <div class="icon-glow icon-glow--primary"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.length }}</span>
          <span class="stat-label">全部待办</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--pending">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            schedule
          </span>
          <div class="icon-glow icon-glow--warning"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => !t.completed).length }}</span>
          <span class="stat-label">进行中</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--high">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            priority_high
          </span>
          <div class="icon-glow icon-glow--danger"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.priority === 'high' && !t.completed).length }}</span>
          <span class="stat-label">紧急</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
      <div class="stat-card stat-card--done">
        <div class="stat-icon-wrapper">
          <span class="material-icons-outlined stat-icon">
            <div class="icon-bg"></div>
            check_circle
          </span>
          <div class="icon-glow icon-glow--success"></div>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ todos.filter(t => t.completed).length }}</span>
          <span class="stat-label">已完成</span>
        </div>
        <div class="stat-decoration"></div>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <button
        v-for="tab in filterTabs"
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeFilter === tab.key }"
        @click="activeFilter = tab.key"
      >
        <span class="filter-tab-icon material-icons-outlined">{{ tab.icon }}</span>
        <span class="filter-tab-label">{{ tab.label }}</span>
        <span v-if="getCount(tab.key) > 0" class="tab-count">{{ getCount(tab.key) }}</span>
        <div class="tab-indicator"></div>
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-dot"></div>
        </div>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-else-if="filteredTodos.length === 0" class="empty-state">
        <div class="empty-illustration">
          <div class="empty-icon-wrapper">
            <span class="material-icons-outlined empty-icon">{{ emptyIcon }}</span>
            <div class="icon-rings">
              <div class="ring"></div>
              <div class="ring"></div>
              <div class="ring"></div>
            </div>
          </div>
          <div class="empty-decoration"></div>
        </div>
        <h3 class="empty-title">{{ emptyTitle }}</h3>
        <p class="empty-text">{{ emptyText }}</p>
        <button v-if="activeFilter === 'all'" class="empty-action" @click="showAddDialog = true">
          <span class="material-icons-outlined">add</span>
          创建第一个待办
        </button>
      </div>

      <div v-else class="todo-list">
        <transition-group name="todo-item">
          <div
            v-for="todo in filteredTodos"
            :key="todo.id"
            class="todo-item"
            :class="{
              completed: todo.completed,
              overdue: isOverdue(todo.dueDate),
              [`priority-${todo.priority}`]: true,
              'batch-selected': selectedTodos.has(todo.id),
              'is-dragging': draggingTodo === todo.id
            }"
            draggable="true"
            @dragstart="handleDragStart($event, todo)"
            @dragend="handleDragEnd"
            @dragover="handleDragOver($event, todo)"
            @drop="handleDrop($event, todo)"
            @click="handleItemClick(todo)"
          >
            <!-- 优先级指示条 -->
            <div class="priority-indicator" :class="`priority-${todo.priority}`">
              <div class="indicator-glow"></div>
            </div>

            <!-- 批量选择复选框 -->
            <div v-if="batchMode" class="batch-checkbox" @click.stop="toggleSelectTodo(todo)">
              <div class="checkbox-inner" :class="{ checked: selectedTodos.has(todo.id) }">
                <span v-if="selectedTodos.has(todo.id)" class="material-icons-outlined check-icon">check</span>
              </div>
            </div>

            <!-- 完成复选框 -->
            <div v-else class="todo-checkbox" @click.stop="toggleComplete(todo)">
              <div class="checkbox-inner" :class="{ checked: todo.completed }">
                <span v-if="todo.completed" class="material-icons-outlined check-icon">check</span>
                <div class="checkbox-ripple"></div>
              </div>
            </div>

            <!-- 内容 -->
            <div class="todo-content">
              <div class="todo-header">
                <h4 class="todo-title">{{ todo.title }}</h4>
                <span class="todo-priority-badge" :class="`priority-${todo.priority}`">
                  {{ priorityText(todo.priority) }}
                </span>
              </div>
              <p v-if="todo.content" class="todo-desc">{{ todo.content }}</p>
              <div class="todo-meta">
                <span class="todo-date" :class="{ overdue: isOverdue(todo.dueDate) }">
                  <span class="material-icons-outlined date-icon">{{ dateIcon(todo.dueDate) }}</span>
                  {{ formatDate(todo.dueDate) }}
                </span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="todo-actions" @click.stop>
              <span v-if="!batchMode" class="drag-handle" title="拖拽排序">
                <span class="material-icons-outlined">drag_indicator</span>
              </span>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn" @click="handleEdit(todo)">
                  <span class="material-icons-outlined">edit</span>
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn action-btn--danger" @click="deleteTodo(todo)">
                  <span class="material-icons-outlined">delete</span>
                </button>
              </el-tooltip>
            </div>
          </div>
        </transition-group>
      </div>
    </div>

    <!-- 新建待办对话框 -->
    <CreateTodoDialog
      v-model="showAddDialog"
      :todo="editingTodo"
      @success="handleTodoCreated"
    />

    <!-- 待办详情对话框 -->
    <TodoDetailDialog
      v-model="showDetailDialog"
      :todo="selectedTodo"
      @edit="handleEditFromDetail"
      @delete="deleteTodo"
      @complete="toggleComplete"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { confirmDelete, deleteSuccess, messageError, messageSuccess } from '@/utils/ui'
import CreateTodoDialog from '@/components/CreateTodoDialog/index.vue'
import TodoDetailDialog from '@/components/TodoDetailDialog/index.vue'
import { getTodos, completeTodo, deleteTodo as deleteTodoApi, updateTodo } from '@/api/im/workbench'

// ============================================================================
// 工具函数：防抖
// ============================================================================
const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

const loading = ref(false)
const showAddDialog = ref(false)
