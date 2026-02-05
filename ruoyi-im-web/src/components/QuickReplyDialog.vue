<template>
  <el-dialog
    v-model="visible"
    title="快捷回复管理"
    width="700px"
    class="quick-reply-dialog"
    append-to-body
    @open="loadReplies"
  >
    <div class="quick-reply-content">
      <!-- 分类标签 -->
      <div class="category-tabs">
        <div
          v-for="category in categories"
          :key="category.key"
          :class="['category-tab', { active: activeCategory === category.key }]"
          @click="activeCategory = category.key"
        >
          <el-icon><component :is="category.icon" /></el-icon>
          <span>{{ category.label }}</span>
          <span
            v-if="getCategoryCount(category.key) > 0"
            class="count"
          >
            {{ getCategoryCount(category.key) }}
          </span>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索快捷回复内容"
          size="small"
          clearable
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          size="small"
          :icon="Plus"
          @click="handleAdd"
        >
          添加快捷回复
        </el-button>
      </div>

      <!-- 快捷回复列表 -->
      <div
        v-loading="loading"
        class="reply-list"
      >
        <div
          v-if="!loading && filteredReplies.length === 0"
          class="empty-state"
        >
          <el-icon><EditPen /></el-icon>
          <span>暂无快捷回复，点击上方按钮添加</span>
        </div>

        <div
          v-else
          class="reply-items"
        >
          <div
            v-for="reply in filteredReplies"
            :key="reply.id"
            class="reply-item"
            :class="{ 'is-editing': editingId === reply.id }"
            draggable="true"
            @dragstart="handleDragStart($event, reply)"
            @dragover.prevent
            @drop="handleDrop($event, reply)"
          >
            <div
              v-if="editingId === reply.id"
              class="edit-form"
            >
              <el-input
                v-model="editForm.content"
                type="textarea"
                :rows="3"
                placeholder="请输入快捷回复内容"
                maxlength="200"
                show-word-limit
              />
              <div class="edit-actions">
                <el-select
                  v-model="editForm.category"
                  size="small"
                  style="width: 120px; margin-right: 8px"
                >
                  <el-option
                    v-for="cat in categories.filter(c => c.key !== 'all')"
                    :key="cat.key"
                    :label="cat.label"
                    :value="cat.key"
                  />
                </el-select>
                <el-button
                  size="small"
                  @click="cancelEdit"
                >
                  取消
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click="saveEdit"
                >
                  保存
                </el-button>
              </div>
            </div>
            <template v-else>
              <div class="reply-drag-handle">
                <el-icon><Rank /></el-icon>
              </div>
              <div class="reply-content">
                <div class="reply-text">
                  {{ reply.content }}
                </div>
                <div class="reply-meta">
                  <el-tag
                    size="small"
                    :type="getCategoryType(reply.category)"
                  >
                    {{ getCategoryLabel(reply.category) }}
                  </el-tag>
                  <span class="use-count">使用 {{ reply.useCount || 0 }} 次</span>
                </div>
              </div>
              <div class="reply-actions">
                <el-button
                  size="small"
                  text
                  :icon="Edit"
                  @click="startEdit(reply)"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  text
                  type="danger"
                  :icon="Delete"
                  @click="handleDelete(reply)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加快捷回复"
      width="500px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="addForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item
          label="分类"
          prop="category"
        >
          <el-select
            v-model="addForm.category"
            placeholder="请选择分类"
          >
            <el-option
              v-for="cat in categories.filter(c => c.key !== 'all')"
              :key="cat.key"
              :label="cat.label"
              :value="cat.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="内容"
          prop="content"
        >
          <el-input
            v-model="addForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入快捷回复内容"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitAdd"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <template #footer>
      <el-button @click="visible = false">
        关闭
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Search,
  Plus,
  EditPen,
  Edit,
  Delete,
  Rank,
  ChatDotRound,
  Flag,
  Star,
  Folder
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQuickReplyList,
  createQuickReply,
  updateQuickReply,
  deleteQuickReply,
  updateSortOrder
} from '@/api/im/quickReply'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

// 分类定义
const categories = [
  { key: 'all', label: '全部', icon: 'Folder' },
  { key: 'greeting', label: '问候语', icon: 'ChatDotRound' },
  { key: 'ending', label: '结束语', icon: 'Flag' },
  { key: 'common', label: '常用语', icon: 'Star' },
  { key: 'custom', label: '自定义', icon: 'EditPen' }
]

const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const replies = ref([])
const editingId = ref(null)
const editForm = ref({ content: '', category: 'custom' })
const showAddDialog = ref(false)
const submitting = ref(false)
const addForm = ref({ content: '', category: 'custom' })
const formRef = ref(null)

const formRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [
    { required: true, message: '请输入快捷回复内容', trigger: 'blur' },
    { min: 1, max: 200, message: '内容长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

// 获取分类数量
const getCategoryCount = category => {
  if (category === 'all') {return replies.value.length}
  return replies.value.filter(r => r.category === category).length
}

// 获取分类标签
const getCategoryLabel = category => {
  const cat = categories.find(c => c.key === category)
  return cat ? cat.label : '未知'
}

// 获取分类标签颜色
const getCategoryType = category => {
  const typeMap = {
    greeting: 'success',
    ending: 'warning',
    common: 'info',
    custom: ''
  }
  return typeMap[category] || ''
}

// 过滤后的回复列表
const filteredReplies = computed(() => {
  let result = replies.value

  // 分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(r => r.category === activeCategory.value)
  }

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => r.content.toLowerCase().includes(keyword))
  }

  return result
})

// 加载快捷回复列表
const loadReplies = async () => {
  loading.value = true
  try {
    const res = await getQuickReplyList()
    if (res.code === 200) {
      replies.value = res.data || []
    }
  } catch (error) {
    console.error('加载快捷回复失败', error)
  } finally {
    loading.value = false
  }
}

// 添加快捷回复
const handleAdd = () => {
  addForm.value = { content: '', category: activeCategory.value === 'all' ? 'custom' : activeCategory.value }
  showAddDialog.value = true
}

// 提交添加
const submitAdd = async () => {
  await formRef.value?.validate()

  submitting.value = true
  try {
    const res = await createQuickReply(addForm.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddDialog.value = false
      addForm.value = { content: '', category: 'custom' }
      loadReplies()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加快捷回复失败', error)
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

// 开始编辑
const startEdit = reply => {
  editingId.value = reply.id
  editForm.value = {
    content: reply.content,
    category: reply.category
  }
}

// 取消编辑
const cancelEdit = () => {
  editingId.value = null
  editForm.value = { content: '', category: 'custom' }
}

// 保存编辑
const saveEdit = async () => {
  if (!editForm.value.content.trim()) {
    ElMessage.warning('请输入快捷回复内容')
    return
  }

  try {
    const res = await updateQuickReply({
      id: editingId.value,
      content: editForm.value.content,
      category: editForm.value.category
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editingId.value = null
      editForm.value = { content: '', category: 'custom' }
      loadReplies()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存快捷回复失败', error)
    ElMessage.error('保存失败')
  }
}

// 删除快捷回复
const handleDelete = async reply => {
  try {
    await ElMessageBox.confirm(`确定要删除快捷回复"${reply.content}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteQuickReply(reply.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadReplies()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除快捷回复失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 拖拽相关
let draggedItem = null

const handleDragStart = (event, reply) => {
  draggedItem = reply
  event.dataTransfer.effectAllowed = 'move'
}

const handleDrop = async (event, targetReply) => {
  event.preventDefault()
  if (!draggedItem || draggedItem.id === targetReply.id) {return}

  // 重新排序
  const newOrder = [...filteredReplies.value]
  const draggedIndex = newOrder.findIndex(r => r.id === draggedItem.id)
  const targetIndex = newOrder.findIndex(r => r.id === targetReply.id)

  if (draggedIndex > -1 && targetIndex > -1) {
    newOrder.splice(draggedIndex, 1)
    newOrder.splice(targetIndex, 0, draggedItem)

    // 更新排序
    const idList = newOrder.map(r => r.id)
    try {
      await updateSortOrder(idList)
      loadReplies()
    } catch (error) {
      console.error('更新排序失败', error)
    }
  }

  draggedItem = null
}
</script>

<style scoped lang="scss">
.quick-reply-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
    max-height: 600px;
    overflow-y: auto;
  }
}

.quick-reply-content {
  .category-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .category-tab {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: var(--dt-radius-md);
      cursor: pointer;
      transition: all 0.2s;
      color: var(--el-text-color-secondary);

      &:hover {
        background: var(--el-fill-color-light);
      }

      &.active {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      .count {
        font-size: 11px;
        background: var(--el-fill-color);
        padding: 2px 6px;
        border-radius: var(--dt-radius-lg);
      }
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .reply-list {
    min-height: 300px;

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      gap: 12px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 48px;
        opacity: 0.5;
      }
    }

    .reply-items {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .reply-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 12px;
        background: var(--el-fill-color-light);
        border-radius: var(--dt-radius-md);
        transition: all 0.2s;

        &:hover {
          background: var(--el-fill-color);
        }

        &.is-editing {
          background: var(--el-color-primary-light-9);
          flex-direction: column;

          .edit-form {
            width: 100%;

            .edit-actions {
              display: flex;
              justify-content: flex-end;
              margin-top: 12px;
            }
          }
        }

        .reply-drag-handle {
          cursor: grab;
          color: var(--el-text-color-placeholder);
          margin-top: 2px;

          &:active {
            cursor: grabbing;
          }
        }

        .reply-content {
          flex: 1;
          min-width: 0;

          .reply-text {
            font-size: 14px;
            color: var(--el-text-color-primary);
            margin-bottom: 8px;
            line-height: 1.5;
          }

          .reply-meta {
            display: flex;
            align-items: center;
            gap: 12px;

            .use-count {
              font-size: 12px;
              color: var(--el-text-color-secondary);
            }
          }
        }

        .reply-actions {
          display: flex;
          gap: 4px;
          flex-shrink: 0;
        }
      }
    }
  }
}
</style>
