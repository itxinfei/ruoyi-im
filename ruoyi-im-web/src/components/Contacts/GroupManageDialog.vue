<template>
  <el-dialog
    v-model="dialogVisible"
    title="分组管理"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="group-manage">
      <!-- 分组列表 -->
      <div class="group-list">
        <div
          v-for="group in groups"
          :key="group.name"
          class="group-item"
          :class="{ active: selectedGroup === group.name }"
        >
          <span class="group-name">{{ group.name }}</span>
          <span class="group-count">({{ group.count }})</span>
          <div class="group-actions">
            <el-icon class="action-icon" @click="handleRename(group)">
              <Edit />
            </el-icon>
            <el-icon
              v-if="group.name !== '默认分组'"
              class="action-icon danger"
              @click="handleDelete(group)"
            >
              <Delete />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 新建分组按钮 -->
      <el-button
        type="primary"
        :icon="Plus"
        class="add-group-btn"
        @click="showCreateDialog = true"
      >
        新建分组
      </el-button>
    </div>

    <!-- 新建分组对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新建分组"
      width="320px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form :model="createForm" label-width="60px">
        <el-form-item label="分组名">
          <el-input
            v-model="createForm.groupName"
            placeholder="请输入分组名称"
            maxlength="20"
            show-word-limit
            @keyup.enter="handleCreate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名分组对话框 -->
    <el-dialog
      v-model="showRenameDialog"
      title="重命名分组"
      width="320px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form :model="renameForm" label-width="60px">
        <el-form-item label="分组名">
          <el-input
            v-model="renameForm.newName"
            placeholder="请输入新的分组名称"
            maxlength="20"
            show-word-limit
            @keyup.enter="handleRenameConfirm"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRenameConfirm">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Plus } from '@element-plus/icons-vue'
import { renameGroup, deleteGroup, createGroup } from '@/api/im/contact'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  friendGroups: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const dialogVisible = ref(false)
const showCreateDialog = ref(false)
const showRenameDialog = ref(false)
const selectedGroup = ref('')

// 新建分组表单
const createForm = ref({
  groupName: ''
})

// 重命名表单
const renameForm = ref({
  oldName: '',
  newName: ''
})

// 分组列表
const groups = computed(() => {
  return props.friendGroups.map(g => ({
    name: g.name || g.groupName,
    count: g.friends?.length || 0
  }))
})

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 新建分组
const handleCreate = async () => {
  const groupName = createForm.value.groupName.trim()
  if (!groupName) {
    ElMessage.warning('请输入分组名称')
    return
  }

  // 检查是否已存在
  if (groups.value.some(g => g.name === groupName)) {
    ElMessage.warning('分组名称已存在')
    return
  }

  try {
    await createGroup({ groupName })
    ElMessage.success('分组创建成功')
    showCreateDialog.value = false
    createForm.value.groupName = ''
    emit('refresh')
  } catch (error) {
    ElMessage.error('分组创建失败')
  }
}

// 重命名分组
const handleRename = (group) => {
  renameForm.value.oldName = group.name
  renameForm.value.newName = group.name
  showRenameDialog.value = true
}

// 确认重命名
const handleRenameConfirm = async () => {
  const newName = renameForm.value.newName.trim()
  if (!newName) {
    ElMessage.warning('请输入分组名称')
    return
  }

  if (newName === renameForm.value.oldName) {
    showRenameDialog.value = false
    return
  }

  // 检查是否已存在
  if (groups.value.some(g => g.name === newName && g.name !== renameForm.value.oldName)) {
    ElMessage.warning('分组名称已存在')
    return
  }

  try {
    await renameGroup(renameForm.value.oldName, newName)
    ElMessage.success('分组重命名成功')
    showRenameDialog.value = false
    emit('refresh')
  } catch (error) {
    ElMessage.error('分组重命名失败')
  }
}

// 删除分组
const handleDelete = async (group) => {
  try {
    await ElMessageBox.confirm(
      `删除"${group.name}"分组后，该分组下的联系人将移至默认分组。确定删除吗？`,
      '删除分组',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteGroup(group.name)
    ElMessage.success('分组删除成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('分组删除失败')
    }
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-list {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);

  @extend .scrollbar-sm;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid var(--dt-border-lighter);
  transition: background var(--dt-transition-fast);
  gap: 8px;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-hover);

    .group-actions {
      opacity: 1;
    }
  }

  .group-name {
    flex: 1;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
  }

  .group-count {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
    margin-right: 8px;
  }

  .group-actions {
    display: flex;
    gap: 8px;
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }

  .action-icon {
    font-size: 16px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    padding: 4px;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);
    }

    &.danger:hover {
      background: rgba(255, 77, 79, 0.1);
      color: var(--dt-error-color);
    }
  }
}

.add-group-btn {
  width: 100%;
}
</style>
