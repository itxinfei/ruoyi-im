<template>
  <el-dialog
    v-model="visible"
    title="发起群聊"
    width="600px"
    class="create-group-dialog"
    append-to-body
    @close="handleClose"
  >
    <div class="dialog-content">
      <!-- 群名称输入 -->
      <div class="form-group">
        <label class="form-label">群名称 <span class="required">*</span></label>
        <el-input
          v-model="groupForm.name"
          placeholder="请输入群名称"
          maxlength="50"
          show-word-limit
        />
      </div>

      <!-- 成员选择 -->
      <div class="form-group">
        <label class="form-label">选择成员 <span class="required">*</span></label>
        <div class="member-section">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索联系人"
              :prefix-icon="Search"
              clearable
              size="small"
            />
          </div>

          <!-- 已选成员 -->
          <div v-if="selectedMembers.length > 0" class="selected-members">
            <div
              v-for="member in selectedMembers"
              :key="member.id"
              class="selected-member"
            >
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.nickname || member.username"
                :user-id="member.userId || member.id"
                :size="32"
                shape="circle"
              />
              <span class="name">{{ member.nickname || member.username }}</span>
              <el-icon class="remove-btn" @click="removeMember(member)">
                <Close />
              </el-icon>
            </div>
          </div>

          <!-- 可选成员列表 -->
          <div v-loading="loading" class="member-list">
            <div
              v-for="member in filteredMembers"
              :key="member.id"
              class="member-item"
              :class="{ selected: isSelected(member) }"
              @click="toggleMember(member)"
            >
              <el-checkbox :model-value="isSelected(member)" />
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.nickname || member.username"
                :user-id="member.userId || member.id"
                :size="36"
                shape="circle"
              />
              <div class="member-info">
                <div class="name">{{ member.nickname || member.username }}</div>
                <div class="department">{{ member.department || '' }}</div>
              </div>
            </div>
            <el-empty v-if="!loading && filteredMembers.length === 0" description="未找到联系人" />
          </div>
        </div>
      </div>

      <!-- 群简介 -->
      <div class="form-group">
        <label class="form-label">群简介</label>
        <el-input
          v-model="groupForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入群简介（可选）"
          maxlength="200"
          show-word-limit
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getContacts } from '@/api/im/contact'
import { createGroup } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const emit = defineEmits(['update:modelValue', 'created'])

const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')
const members = ref([])
const selectedMembers = ref([])

const groupForm = ref({
  name: '',
  description: ''
})

const filteredMembers = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  if (!keyword) return members.value
  return members.value.filter(m => 
    (m.nickname && m.nickname.toLowerCase().includes(keyword)) ||
    (m.username && m.username.toLowerCase().includes(keyword)) ||
    (m.department && m.department.toLowerCase().includes(keyword))
  )
})

const canSubmit = computed(() => {
  return groupForm.value.name.trim() && selectedMembers.value.length >= 2
})

const isSelected = (member) => {
  return selectedMembers.value.some(m => m.id === member.id)
}

const toggleMember = (member) => {
  const index = selectedMembers.value.findIndex(m => m.id === member.id)
  if (index !== -1) {
    selectedMembers.value.splice(index, 1)
  } else {
    selectedMembers.value.push(member)
  }
}

const removeMember = (member) => {
  const index = selectedMembers.value.findIndex(m => m.id === member.id)
  if (index !== -1) {
    selectedMembers.value.splice(index, 1)
  }
}

const loadContacts = async () => {
  loading.value = true
  try {
    const res = await getContacts()
    if (res.code === 200) {
      members.value = res.data || []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
    ElMessage.error('加载联系人失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const memberIds = selectedMembers.value.map(m => m.userId || m.id)
    const res = await createGroup({
      name: groupForm.value.name.trim(),
      description: groupForm.value.description.trim(),
      memberIds
    })

    if (res.code === 200) {
      ElMessage.success('群聊创建成功')
      emit('created', res.data)
      handleClose()
    } else {
      throw new Error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error(error.message || '创建群聊失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  // 重置表单
  groupForm.value = {
    name: '',
    description: ''
  }
  selectedMembers.value = []
  searchKeyword.value = ''
}

const open = () => {
  visible.value = true
  loadContacts()
}

// 监听外部 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  if (newVal) {
    loadContacts()
  }
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.create-group-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);

  .required {
    color: var(--dt-error-color);
    margin-left: 2px;
  }
}

.member-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  padding: 12px;
  background: var(--dt-bg-body);
}

.search-box {
  width: 100%;
}

.selected-members {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 44px;
  padding: 8px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-sm);
}

.selected-member {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--dt-brand-lighter);
  border-radius: var(--dt-radius-full);

  .name {
    font-size: 13px;
    color: var(--dt-text-primary);
  }

  .remove-btn {
    cursor: pointer;
    color: var(--dt-text-tertiary);
    font-size: 14px;
    transition: color var(--dt-transition-fast);

    &:hover {
      color: var(--dt-error-color);
    }
  }
}

.member-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;

  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 12px;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    transition: background var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-card);
    }

    &.selected {
      background: var(--dt-brand-lighter);
    }

    .member-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .name {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
      }

      .department {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>