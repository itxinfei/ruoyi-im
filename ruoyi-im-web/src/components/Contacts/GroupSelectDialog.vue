<template>
  <el-dialog
    v-model="visible"
    title="选择分组"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="group-select-content">
      <!-- 搜索框 -->
      <el-input
        v-model="searchQuery"
        placeholder="搜索分组..."
        :prefix-icon="Search"
        clearable
        class="search-input"
      />

      <!-- 分组列表 -->
      <div class="group-list scrollbar-custom">
        <div
          v-for="group in filteredGroups"
          :key="group"
          class="group-item"
          :class="{ selected: selectedGroup === group }"
          @click="selectGroup(group)"
        >
          <div class="group-icon">
            <span class="group-initial">{{ group.charAt(0) }}</span>
          </div>
          <span class="group-name">{{ group }}</span>
          <el-icon
            v-if="selectedGroup === group"
            class="check-icon"
            color="var(--el-color-primary)"
          >
            <Check />
          </el-icon>
        </div>

        <!-- 创建新分组入口 -->
        <div
          v-if="searchQuery && !filteredGroups.length"
          class="group-item create-new"
          @click="createNewGroup"
        >
          <div class="group-icon add">
            <el-icon><Plus /></el-icon>
          </div>
          <span class="group-name">创建"{{ searchQuery }}"分组</span>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!searchQuery && !groups.length"
          description="暂无分组"
          :image-size="80"
        />
      </div>

      <!-- 快速添加新分组 -->
      <div class="add-group-section">
        <el-input
          v-model="newGroupName"
          placeholder="新分组名称"
          maxlength="20"
          show-word-limit
          size="small"
          @keyup.enter="quickAddGroup"
        >
          <template #append>
            <el-button
              :icon="Plus"
              @click="quickAddGroup"
            >
              添加
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="!selectedGroup"
          @click="handleConfirm"
        >
          确定 ({{ selectedCount }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Plus, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getGroupList, createGroup } from '@/api/im/contact'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  selectedCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
const groups = ref([])
const searchQuery = ref('')
const selectedGroup = ref('')
const newGroupName = ref('')

// 过滤后的分组列表
const filteredGroups = computed(() => {
  if (!searchQuery.value) {return groups.value}
  const query = searchQuery.value.toLowerCase()
  return groups.value.filter(g => g.toLowerCase().includes(query))
})

// 同步v-model
watch(() => props.modelValue, val => {
  visible.value = val
  if (val) {
    loadGroups()
  }
})

watch(visible, val => {
  emit('update:modelValue', val)
})

// 加载分组列表
const loadGroups = async () => {
  try {
    const res = await getGroupList()
    if (res.code === 200) {
      groups.value = res.data || []
      // 添加默认分组
      if (!groups.value.includes('未分组')) {
        groups.value.unshift('未分组')
      }
    }
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

// 选择分组
const selectGroup = group => {
  selectedGroup.value = group
}

// 创建新分组
const createNewGroup = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }

  try {
    const res = await createGroup({ groupName: searchQuery.value.trim() })
    if (res.code === 200) {
      ElMessage.success('分组创建成功')
      await loadGroups()
      selectedGroup.value = searchQuery.value.trim()
      searchQuery.value = ''
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 快速添加分组
const quickAddGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }

  try {
    const res = await createGroup({ groupName: newGroupName.value.trim() })
    if (res.code === 200) {
      ElMessage.success('分组创建成功')
      await loadGroups()
      selectedGroup.value = newGroupName.value.trim()
      newGroupName.value = ''
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  selectedGroup.value = ''
  searchQuery.value = ''
  newGroupName.value = ''
}

// 确认选择
const handleConfirm = () => {
  if (!selectedGroup.value) {
    ElMessage.warning('请选择分组')
    return
  }
  emit('confirm', selectedGroup.value)
  handleClose()
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-select-content {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-lg);
}

.search-input {
  flex-shrink: 0;
}

.group-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px var(--dt-spacing-md);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.selected {
    background: var(--dt-color-primary-light);

    .group-name {
      color: var(--dt-color-primary);
      font-weight: 500;
    }
  }

  &.create-new {
    color: var(--dt-color-primary);

    .group-icon.add {
      background: var(--dt-color-primary-light);
      color: var(--dt-color-primary);
    }
  }
}

.group-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-md);
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.add {
    background: var(--dt-color-primary-light);
    color: var(--dt-color-primary);
  }

  .group-initial {
    font-size: var(--dt-font-size-sm);
    font-weight: 600;
    color: #666;
  }
}

.group-name {
  flex: 1;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-color-text-primary);
}

.check-icon {
  flex-shrink: 0;
}

.add-group-section {
  flex-shrink: 0;
  padding-top: var(--dt-spacing-md);
  border-top: 1px solid var(--dt-border-color-light);
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
