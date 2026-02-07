<template>
  <el-dialog
    v-model="visible"
    title="添加群成员"
    width="420px"
    class="add-members-dialog"
    :close-on-click-modal="false"
    destroy-on-close
    @open="loadContacts"
  >
    <!-- 搜索框 -->
    <div class="search-section">
      <el-input
        v-model="searchText"
        placeholder="搜索联系人"
        clearable
        prefix-icon="Search"
      />
    </div>

    <!-- 联系人列表 -->
    <div
      v-loading="loading"
      class="contacts-list"
    >
      <!-- 已选择的成员 -->
      <div
        v-if="selectedMembers.length > 0"
        class="selected-section"
      >
        <div class="section-header">
          <span class="section-title">已选择 ({{ selectedMembers.length }})</span>
          <el-button
            link
            type="danger"
            size="small"
            @click="clearSelection"
          >
            清空
          </el-button>
        </div>
        <div class="selected-members">
          <div
            v-for="member in selectedMembers"
            :key="member.id"
            class="selected-member-chip"
          >
            <DingtalkAvatar
              :src="member.avatar"
              :name="member.name"
              :size="28"
            />
            <span class="member-name">{{ member.name }}</span>
            <el-icon
              class="remove-icon"
              @click="toggleSelection(member)"
            >
              <Close />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 可选择的联系人 -->
      <div class="selectable-section">
        <div
          v-if="selectedMembers.length > 0"
          class="section-header"
        >
          <span class="section-title">选择联系人</span>
        </div>
        <div class="contact-list">
          <div
            v-for="contact in filteredContacts"
            :key="contact.id"
            class="contact-item"
            :class="{ selected: isSelected(contact) }"
            @click="toggleSelection(contact)"
          >
            <el-checkbox
              :model-value="isSelected(contact)"
              @change="toggleSelection(contact)"
            />
            <DingtalkAvatar
              :src="contact.avatar"
              :name="contact.name"
              :size="40"
            />
            <div class="contact-info">
              <div class="contact-name">
                {{ contact.name }}
              </div>
              <div class="contact-dept">
                {{ contact.dept || contact.department || '无部门' }}
              </div>
            </div>
            <el-icon
              v-if="isSelected(contact)"
              class="check-icon"
            >
              <Check />
            </el-icon>
          </div>
        </div>
        <el-empty
          v-if="filteredContacts.length === 0 && !loading"
          description="未找到联系人"
        />
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="dialog-footer">
        <span class="selected-count">已选 {{ selectedMembers.length }} 人</span>
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="selectedMembers.length === 0"
          :loading="submitting"
          @click="handleConfirm"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Check } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getContacts } from '@/api/im/contact'
import { addGroupMembers } from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [String, Number], required: true },
  existingMembers: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const contacts = ref([])
const selectedMembers = ref([])
const loading = ref(false)
const submitting = ref(false)
const searchText = ref('')

// 已存在的成员ID集合
const existingMemberIds = computed(() => {
  return new Set(props.existingMembers.map(m => m.id || m.userId || m))
})

// 过滤后的联系人列表
const filteredContacts = computed(() => {
  let result = contacts.value

  // 排除已存在的成员
  result = result.filter(c => !existingMemberIds.value.has(c.id))

  // 搜索过滤
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(c =>
      c.name?.toLowerCase().includes(keyword) ||
      c.nickname?.toLowerCase().includes(keyword) ||
      c.username?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 判断是否已选中
const isSelected = contact => {
  return selectedMembers.value.some(m => m.id === contact.id)
}

// 切换选中状态
const toggleSelection = contact => {
  const index = selectedMembers.value.findIndex(m => m.id === contact.id)
  if (index > -1) {
    selectedMembers.value.splice(index, 1)
  } else {
    selectedMembers.value.push(contact)
  }
}

// 清空选择
const clearSelection = () => {
  selectedMembers.value = []
}

// 加载联系人列表
const loadContacts = async () => {
  loading.value = true
  try {
    const res = await getContacts()
    if (res.code === 200) {
      contacts.value = res.data || []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  } finally {
    loading.value = false
  }
}

// 确认添加成员
const handleConfirm = async () => {
  if (selectedMembers.value.length === 0) {return}

  submitting.value = true
  try {
    const userIds = selectedMembers.value.map(m => m.id)
    const res = await addGroupMembers(props.groupId, userIds)
    if (res.code === 200) {
      ElMessage.success(`已添加 ${selectedMembers.value.length} 位成员`)
      selectedMembers.value = []
      emit('success', res.data)
      visible.value = false
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error('添加失败，请重试')
  } finally {
    submitting.value = false
  }
}

watch(() => props.modelValue, val => {
  if (val) {
    searchText.value = ''
    selectedMembers.value = []
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.add-members-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }

  .search-section {
    margin-bottom: 16px;
  }
}

.contacts-list {
  max-height: 400px;
  overflow-y: auto;
  @extend .scrollbar-sm;
}

.selected-section {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dt-border-light);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--dt-text-secondary);
    }
  }

  .selected-members {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .selected-member-chip {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 10px 6px 6px;
      background: var(--dt-brand-color);
      color: #fff;
      border-radius: var(--dt-radius-2xl);
      font-size: 13px;

      .member-name {
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .remove-icon {
        cursor: pointer;
        opacity: 0.7;
        transition: opacity 0.2s;

        &:hover {
          opacity: 1;
        }
      }
    }
  }
}

.selectable-section {
  .section-header {
    margin-bottom: 12px;

    .section-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--dt-text-tertiary);
    }
  }
}

.contact-list {
  .contact-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: var(--dt-radius-lg);
    cursor: pointer;
    transition: all 0.2s;
    position: relative;

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.selected {
      background: var(--dt-brand-light);

      .contact-name {
        color: var(--dt-brand-color);
      }
    }

    .contact-info {
      flex: 1;
      min-width: 0;

      .contact-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .contact-dept {
        font-size: 12px;
        color: var(--dt-text-tertiary);
        margin-top: 2px;
      }
    }

    .check-icon {
      color: var(--dt-brand-color);
      font-size: 20px;
    }
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .selected-count {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }

  .el-button {
    margin-left: 12px;
  }
}
</style>
