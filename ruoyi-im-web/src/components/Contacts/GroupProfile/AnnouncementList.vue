<!--
  群公告列表组件

  显示群公告列表，支持创建、编辑、删除
-->
<template>
  <div class="tab-content">
    <div class="announcement-header">
      <el-button
        v-if="canManage"
        type="primary"
        @click="$emit('create')"
      >
        <el-icon><Plus /></el-icon>发布公告
      </el-button>
    </div>

    <div
      v-if="announcements.length === 0"
      class="empty-state"
    >
      <el-icon
        :size="48"
        color="#dcdfe6"
      >
        <Bell />
      </el-icon>
      <p>暂无群公告</p>
      <p
        v-if="canManage"
        class="empty-hint"
      >
        点击上方按钮发布公告
      </p>
    </div>

    <div
      v-else
      class="announcement-list"
    >
      <div
        v-for="announcement in announcements"
        :key="announcement.id"
        class="announcement-item"
        :class="{ pinned: announcement.isPinned }"
      >
        <div class="announcement-header-row">
          <div class="announcement-title">
            <el-icon
              v-if="announcement.isPinned"
              class="pin-icon"
            >
              <Top />
            </el-icon>
            {{ announcement.title }}
          </div>
          <span class="announcement-time">{{ formatTime(announcement.createTime) }}</span>
        </div>
        <div class="announcement-content">
          {{ announcement.content }}
        </div>
        <div class="announcement-footer">
          <span class="publisher">{{ announcement.publisherName || '群主' }}</span>
          <div
            v-if="canManage"
            class="announcement-actions"
          >
            <el-button
              link
              type="primary"
              size="small"
              @click="$emit('edit', announcement)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(announcement)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Plus, Bell, Top } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { formatListItemTime } from '@/utils/format'

const props = defineProps({
  announcements: {
    type: Array,
    default: () => []
  },
  canManage: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['create', 'edit', 'delete'])

// 使用共享工具函数
const formatTime = formatListItemTime

const handleDelete = announcement => {
  ElMessageBox.confirm('确定要删除这条公告吗？', '删除公告', { type: 'warning' })
    .then(() => {
      emit('delete', announcement)
      ElMessage.success('公告已删除')
    }).catch(() => {})
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.announcement-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);

  p {
    margin: 8px 0;
  }

  .empty-hint {
    font-size: 13px;
    color: var(--dt-text-placeholder);
  }
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  padding: 14px 16px;
  border: 1px solid var(--dt-border-light);

  &.pinned {
    border-color: var(--dt-brand-color);
    background: rgba(0, 137, 255, 0.02);
  }
}

.announcement-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.announcement-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);

  .pin-icon {
    color: var(--dt-brand-color);
    font-size: 16px;
  }
}

.announcement-time {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.announcement-content {
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px dashed var(--dt-border-light);
}

.publisher {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.announcement-actions {
  display: flex;
  gap: 4px;
}
</style>
