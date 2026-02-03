<template>
  <div class="admin-action-bar">
    <!-- 左侧：标题和描述 -->
    <div class="action-bar-left">
      <h2 class="action-title">{{ title }}</h2>
      <p v-if="description" class="action-desc">{{ description }}</p>
    </div>

    <!-- 右侧：操作按钮组 -->
    <div class="action-bar-right">
      <!-- 主要操作按钮 -->
      <el-button
        v-for="action in primaryActions"
        :key="action.key"
        :type="action.type || 'primary'"
        :icon="action.icon"
        :loading="action.loading"
        :disabled="action.disabled"
        @click="handleAction(action)"
      >
        {{ action.label }}
      </el-button>

      <!-- 次要操作按钮（text类型） -->
      <el-button
        v-for="action in secondaryActions"
        :key="action.key"
        text
        :icon="action.icon"
        :disabled="action.disabled"
        @click="handleAction(action)"
      >
        {{ action.label }}
      </el-button>

      <!-- 更多操作下拉菜单 -->
      <el-dropdown v-if="moreActions.length > 0" trigger="click" @command="handleMoreAction">
        <el-button text>
          更多
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="action in moreActions"
              :key="action.key"
              :command="action.key"
              :divided="action.divided"
              :disabled="action.disabled"
            >
              <el-icon v-if="action.icon">
                <component :is="action.icon" />
              </el-icon>
              {{ action.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>

  <!-- 统计信息栏（可选） -->
  <div v-if="stats" class="action-stats">
    <div class="stat-item" v-for="(stat, key) in stats" :key="key">
      <span class="stat-label">{{ stat.label }}:</span>
      <span class="stat-value" :class="stat.color">{{ stat.value }}</span>
    </div>
  </div>
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'

/**
 * AdminActionBar - 管理后台通用操作栏组件
 *
 * @param {String} title - 页面标题
 * @param {String} description - 页面描述（可选）
 * @param {Array} primaryActions - 主要操作按钮数组
 * @param {Array} secondaryActions - 次要操作按钮数组（text样式）
 * @param {Array} moreActions - 更多操作下拉菜单项
 * @param {Object} stats - 统计信息对象
 *
 * @example
 * <admin-action-bar
 *   title="用户管理"
 *   description="管理系统用户账号、角色和状态"
 *   :primary-actions="[
 *     { key: 'add', label: '新增用户', icon: Plus, type: 'primary' }
 *   ]"
 *   :secondary-actions="[
 *     { key: 'import', label: '批量导入', icon: Upload }
 *   ]"
 *   @action="handleAction"
 * />
 */

const props = defineProps({
  // 页面标题
  title: {
    type: String,
    required: true
  },
  // 页面描述
  description: {
    type: String,
    default: ''
  },
  // 主要操作按钮
  primaryActions: {
    type: Array,
    default: () => []
  },
  // 次要操作按钮
  secondaryActions: {
    type: Array,
    default: () => []
  },
  // 更多操作下拉菜单
  moreActions: {
    type: Array,
    default: () => []
  },
  // 统计信息
  stats: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['action'])

// 处理主要/次要操作
const handleAction = (action) => {
  emit('action', action.key, action)
}

// 处理更多操作
const handleMoreAction = (key) => {
  const action = props.moreActions.find(a => a.key === key)
  if (action) {
    emit('action', key, action)
  }
}
</script>

<style scoped lang="scss">
/* 引入主题变量 */
@import '@/styles/admin-theme.scss';

/* ================================
   操作栏容器
   ================================ */
.admin-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.action-bar-left {
  flex: 1;
}

.action-bar-right {
  display: flex;
  gap: var(--dt-space-sm);
  align-items: center;
  flex-shrink: 0;
}

/* ================================
   标题和描述
   ================================ */
.action-title {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.action-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

/* ================================
   统计信息栏
   ================================ */
.action-stats {
  display: flex;
  gap: var(--dt-space-lg);
  padding: var(--dt-space-sm) var(--dt-space-md);
  margin-bottom: var(--dt-space-md);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-sm);
  border: 1px solid var(--dt-border-light);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  font-size: var(--dt-font-size-sm);
}

.stat-label {
  color: var(--dt-text-secondary);
}

.stat-value {
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.stat-value.primary {
  color: var(--dt-primary);
}

.stat-value.success {
  color: var(--dt-success);
}

.stat-value.warning {
  color: var(--dt-warning);
}

.stat-value.danger {
  color: var(--dt-error);
}

.stat-value.info {
  color: var(--dt-info);
}

/* ================================
   Element Plus 下拉菜单样式覆盖
   ================================ */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* ================================
   响应式
   ================================ */
@media (max-width: 768px) {
  .admin-action-bar {
    flex-direction: column;
    gap: var(--dt-space-sm);
  }

  .action-bar-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .action-stats {
    flex-wrap: wrap;
    gap: var(--dt-space-md);
  }
}
</style>
