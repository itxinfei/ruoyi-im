<template>
  <el-dialog
    v-model="visible"
    title="设置标签"
    width="420px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="tags-dialog-content">
      <!-- 当前标签选择 -->
      <div class="tags-section">
        <div class="section-label">选择标签</div>
        <div class="tags-container">
          <el-select
            v-model="selectedTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或创建标签"
            class="tags-select"
          >
            <el-option
              v-for="tag in allTags"
              :key="tag"
              :label="tag"
              :value="tag"
            >
              <span class="tag-option">
                <span class="tag-color-dot" :style="{ background: getTagColor(tag) }"></span>
                {{ tag }}
              </span>
            </el-option>
          </el-select>
        </div>
      </div>

      <!-- 常用标签 -->
      <div class="tags-section">
        <div class="section-label">常用标签</div>
        <div class="common-tags">
          <div
            v-for="tag in commonTags"
            :key="tag"
            class="common-tag"
            :class="{ selected: selectedTags.includes(tag) }"
            :style="{ borderColor: selectedTags.includes(tag) ? getTagColor(tag) : '' }"
            @click="toggleTag(tag)"
          >
            <span class="tag-dot" :style="{ background: getTagColor(tag) }"></span>
            <span class="tag-text">{{ tag }}</span>
          </div>
        </div>
      </div>

      <!-- 新建标签 -->
      <div class="tags-section">
        <div class="section-label">新建标签</div>
        <div class="new-tag-input">
          <el-input
            v-model="newTagInput"
            placeholder="输入新标签名称"
            maxlength="10"
            show-word-limit
            @keyup.enter="createNewTag"
          >
            <template #append>
              <el-button @click="createNewTag" :disabled="!newTagInput.trim()">
                添加
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUserTags } from '@/api/im/contact'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  userId: { type: [String, Number], default: null },
  currentTags: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const selectedTags = ref([])
const newTagInput = ref('')
const saving = ref(false)

// 常用标签列表
const commonTags = ref(['重要', '同事', '家人', '朋友', '客户', '领导'])

// 所有可用标签（包括用户自定义的）
const allTags = ref([...commonTags.value])

// 标签颜色映射（为每个标签分配一个固定颜色）
const tagColors = ref({
  '重要': '#ff4d4f',
  '同事': '#1890ff',
  '家人': '#52c41a',
  '朋友': '#722ed1',
  '客户': '#fa8c16',
  '领导': '#eb2f96'
})

// 获取标签颜色
const getTagColor = (tag) => {
  if (tagColors.value[tag]) {
    return tagColors.value[tag]
  }
  // 为新标签生成颜色（基于标签名hash）
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#13c2c2', '#fadb14']
  return colors[Math.abs(hash) % colors.length]
}

// 切换标签选中状态
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

// 创建新标签
const createNewTag = () => {
  const tag = newTagInput.value.trim()
  if (!tag) return

  if (selectedTags.value.includes(tag)) {
    ElMessage.warning('标签已存在')
    return
  }

  // 添加到选中列表和常用标签列表
  selectedTags.value.push(tag)
  if (!allTags.value.includes(tag)) {
    allTags.value.push(tag)
  }
  if (!commonTags.value.includes(tag)) {
    commonTags.value.push(tag)
  }

  // 保存颜色映射
  tagColors.value[tag] = getTagColor(tag)

  newTagInput.value = ''
  ElMessage.success('标签已添加')
}

// 保存标签
const handleSave = async () => {
  try {
    saving.value = true
    await updateUserTags(props.userId, selectedTags.value)
    ElMessage.success('标签保存成功')
    emit('saved', selectedTags.value)
    handleClose()
  } catch (error) {
    console.error('保存标签失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  selectedTags.value = []
  newTagInput.value = ''
  visible.value = false
}

// 监听对话框打开，初始化选中标签
watch(() => props.modelValue, (val) => {
  if (val && props.currentTags) {
    selectedTags.value = [...props.currentTags]
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.tags-dialog-content {
  .tags-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin-bottom: 12px;
    }
  }

  .tags-container {
    .tags-select {
      width: 100%;

      :deep(.el-select__tags) {
        flex-wrap: wrap;
      }
    }

    .tag-option {
      display: flex;
      align-items: center;
      gap: 8px;

      .tag-color-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
      }
    }
  }

  .common-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .common-tag {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 16px;
      border: 1px solid var(--dt-border-light);
      background: var(--dt-bg-body);
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: var(--dt-border);
      }

      &.selected {
        background: var(--dt-brand-bg);
        border-width: 2px;
      }

      .tag-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
      }

      .tag-text {
        font-size: 13px;
        color: var(--dt-text-primary);
      }
    }
  }

  .new-tag-input {
    :deep(.el-input-group__append) {
      .el-button {
        border-left: none;
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
