<template>
  <div
    class="batch-operation-bar"
    :class="{ visible: visible, 'has-selection': selectedCount > 0 }"
  >
    <!-- 选择模式切换 -->
    <div class="bar-left">
      <el-checkbox
        v-model="selectAll"
        :indeterminate="isIndeterminate"
        @change="handleSelectAll"
      >
        全选
      </el-checkbox>
      <span class="selected-count">已选 {{ selectedCount }} 项</span>
    </div>

    <!-- 批量操作按钮 -->
    <div
      v-if="selectedCount > 0"
      class="bar-actions"
    >
      <!-- 好友操作 -->
      <template v-if="type === 'friend'">
        <el-button
          size="small"
          :icon="User"
          @click="$emit('move-group')"
        >
          移动到分组
        </el-button>
        <el-button
          size="small"
          :icon="Message"
          @click="$emit('send-message')"
        >
          发送消息
        </el-button>
        <el-button
          size="small"
          :icon="Delete"
          type="danger"
          plain
          @click="$emit('delete')"
        >
          删除
        </el-button>
      </template>

      <!-- 群组操作 -->
      <template v-else-if="type === 'group'">
        <el-button
          size="small"
          :icon="Setting"
          @click="$emit('config')"
        >
          群设置
        </el-button>
        <el-button
          size="small"
          :icon="Delete"
          type="danger"
          plain
          @click="$emit('delete')"
        >
          退出群组
        </el-button>
      </template>

      <!-- 推荐/同事操作 -->
      <template v-else>
        <el-button
          size="small"
          :icon="Plus"
          type="primary"
          @click="$emit('add-friends')"
        >
          添加好友
        </el-button>
      </template>
    </div>

    <!-- 关闭按钮 -->
    <el-button
      :icon="Close"
      circle
      size="small"
      class="close-btn"
      @click="$emit('cancel')"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User, Message, Delete, Setting, Plus, Close } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  selectedCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  },
  type: {
    type: String,
    default: 'friend' // friend | group | other
  }
})

const emit = defineEmits(['select-all', 'cancel', 'move-group', 'send-message', 'delete', 'config', 'add-friends'])

const selectAll = computed({
  get: () => props.selectedCount > 0 && props.selectedCount === props.totalCount,
  set: () => {}
})

const isIndeterminate = computed(() => {
  return props.selectedCount > 0 && props.selectedCount < props.totalCount
})

const handleSelectAll = val => {
  emit('select-all', val)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.batch-operation-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-color);
  box-shadow: var(--dt-shadow-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--dt-spacing-2xl);
  z-index: var(--dt-z-fixed);
  transform: translateY(100%);
  transition: transform var(--dt-transition-base);

  &.visible {
    transform: translateY(0);
  }

  &.has-selection {
    background: var(--dt-color-primary-light);
    border-top-color: var(--dt-color-primary-light);
  }
}

.bar-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-lg);

  .selected-count {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-color-text-regular);
  }
}

.bar-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
}

.close-btn {
  margin-left: var(--dt-spacing-lg);
}
</style>
