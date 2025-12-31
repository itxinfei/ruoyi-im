<template>
  <el-dialog
    v-model="visible"
    title="发起投票"
    width="520px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    class="vote-dialog"
    @close="handleClose"
  >
    <!-- 投票表单 -->
    <el-form
      ref="formRef"
      :model="voteForm"
      :rules="formRules"
      label-position="top"
      class="vote-form"
    >
      <!-- 投票标题 -->
      <el-form-item label="投票标题" prop="title">
        <el-input
          v-model="voteForm.title"
          placeholder="请输入投票标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 投票选项 -->
      <el-form-item label="投票选项" prop="options" class="options-form-item">
        <div class="options-list">
          <div
            v-for="(option, index) in voteForm.options"
            :key="index"
            class="option-item"
          >
            <div class="option-index">{{ index + 1 }}</div>
            <el-input
              v-model="option.text"
              :placeholder="`选项 ${index + 1}`"
              maxlength="50"
            />
            <el-button
              v-if="voteForm.options.length > 2"
              :icon="Delete"
              circle
              type="danger"
              plain
              size="small"
              class="delete-btn"
              @click="removeOption(index)"
            />
          </div>
        </div>

        <!-- 添加选项按钮 -->
        <el-button
          v-if="voteForm.options.length < maxOptions"
          type="primary"
          link
          :icon="Plus"
          class="add-option-btn"
          @click="addOption"
        >
          添加选项（最多{{ maxOptions }}项）
        </el-button>
      </el-form-item>

      <!-- 投票设置 -->
      <el-form-item label="投票设置" class="settings-form-item">
        <div class="settings-grid">
          <!-- 单选/多选 -->
          <div class="setting-item">
            <span class="setting-label">投票类型</span>
            <el-radio-group v-model="voteForm.type" size="small">
              <el-radio-button value="single">单选</el-radio-button>
              <el-radio-button value="multiple">多选</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 多选时的最大选择数 -->
          <div v-if="voteForm.type === 'multiple'" class="setting-item">
            <span class="setting-label">最多可选</span>
            <el-input-number
              v-model="voteForm.maxChoices"
              :min="2"
              :max="voteForm.options.length"
              size="small"
            />
          </div>

          <!-- 是否匿名 -->
          <div class="setting-item">
            <span class="setting-label">匿名投票</span>
            <el-switch v-model="voteForm.anonymous" />
          </div>

          <!-- 是否允许查看结果 -->
          <div class="setting-item">
            <span class="setting-label">投票前查看结果</span>
            <el-switch v-model="voteForm.showResultBeforeVote" />
          </div>

          <!-- 截止时间 -->
          <div class="setting-item full-width">
            <span class="setting-label">截止时间</span>
            <el-date-picker
              v-model="voteForm.deadline"
              type="datetime"
              placeholder="选择截止时间（可选）"
              :disabled-date="disabledDate"
              :shortcuts="dateShortcuts"
              value-format="YYYY-MM-DD HH:mm:ss"
              size="small"
              style="width: 100%"
            />
          </div>
        </div>
      </el-form-item>

      <!-- 备注说明 -->
      <el-form-item label="补充说明（可选）">
        <el-input
          v-model="voteForm.description"
          type="textarea"
          :rows="2"
          placeholder="添加投票说明..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 预览区域 -->
    <div v-if="showPreview" class="vote-preview">
      <div class="preview-header">
        <el-icon><View /></el-icon>
        <span>投票预览</span>
      </div>
      <div class="preview-content">
        <div class="preview-title">{{ voteForm.title || '投票标题' }}</div>
        <div class="preview-options">
          <div
            v-for="(option, index) in validOptions"
            :key="index"
            class="preview-option"
          >
            <el-radio v-if="voteForm.type === 'single'" :value="index" disabled>
              {{ option.text || `选项 ${index + 1}` }}
            </el-radio>
            <el-checkbox v-else :value="index" disabled>
              {{ option.text || `选项 ${index + 1}` }}
            </el-checkbox>
          </div>
        </div>
        <div class="preview-meta">
          <span v-if="voteForm.anonymous">
            <el-icon><Hide /></el-icon> 匿名投票
          </span>
          <span v-if="voteForm.deadline">
            <el-icon><Clock /></el-icon> 截止: {{ formatDeadline(voteForm.deadline) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button link @click="togglePreview">
          {{ showPreview ? '隐藏预览' : '显示预览' }}
        </el-button>
        <div class="footer-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            发起投票
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * @file VoteDialog.vue
 * @description 投票创建对话框组件 - 用于创建和发送投票消息
 * @author IM System
 * @version 1.0.0
 */

import { ref, computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Delete,
  View,
  Hide,
  Clock,
} from '@element-plus/icons-vue'

// ==================== Props 定义 ====================
const props = defineProps({
  /**
   * 控制对话框显示状态
   */
  modelValue: {
    type: Boolean,
    default: false,
  },
  /**
   * 会话ID
   */
  sessionId: {
    type: String,
    default: '',
  },
})

// ==================== Emits 定义 ====================
const emit = defineEmits([
  'update:modelValue',
  'confirm',  // 确认创建投票时触发
])

// ==================== 常量定义 ====================

/** 最大选项数 */
const maxOptions = 10

/** 日期快捷选项 */
const dateShortcuts = [
  {
    text: '1小时后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000)
      return date
    },
  },
  {
    text: '6小时后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 6)
      return date
    },
  },
  {
    text: '1天后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    },
  },
  {
    text: '3天后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 3)
      return date
    },
  },
  {
    text: '1周后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
      return date
    },
  },
]

// ==================== 响应式状态 ====================

/** 对话框显示状态 */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

/** 表单引用 */
const formRef = ref(null)

/** 是否显示预览 */
const showPreview = ref(false)

/** 提交中状态 */
const submitting = ref(false)

/** 投票表单数据 */
const voteForm = reactive({
  title: '',
  options: [
    { text: '' },
    { text: '' },
  ],
  type: 'single',         // 'single' | 'multiple'
  maxChoices: 2,          // 多选时最大选择数
  anonymous: false,       // 是否匿名
  showResultBeforeVote: false,  // 投票前是否可查看结果
  deadline: null,         // 截止时间
  description: '',        // 补充说明
})

/** 表单验证规则 */
const formRules = {
  title: [
    { required: true, message: '请输入投票标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  options: [
    {
      validator: (rule, value, callback) => {
        const validCount = value.filter(opt => opt.text.trim()).length
        if (validCount < 2) {
          callback(new Error('至少需要2个有效选项'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

// ==================== 计算属性 ====================

/**
 * 有效的选项列表（过滤空选项）
 */
const validOptions = computed(() => {
  return voteForm.options.filter(opt => opt.text.trim())
})

// ==================== 方法定义 ====================

/**
 * 添加选项
 */
const addOption = () => {
  if (voteForm.options.length >= maxOptions) {
    ElMessage.warning(`最多只能添加${maxOptions}个选项`)
    return
  }
  voteForm.options.push({ text: '' })
}

/**
 * 移除选项
 * @param {number} index - 选项索引
 */
const removeOption = (index) => {
  if (voteForm.options.length <= 2) {
    ElMessage.warning('至少需要保留2个选项')
    return
  }
  voteForm.options.splice(index, 1)

  // 调整多选最大选择数
  if (voteForm.maxChoices > voteForm.options.length) {
    voteForm.maxChoices = voteForm.options.length
  }
}

/**
 * 切换预览显示
 */
const togglePreview = () => {
  showPreview.value = !showPreview.value
}

/**
 * 禁用过去的日期
 * @param {Date} date - 日期对象
 * @returns {boolean} 是否禁用
 */
const disabledDate = (date) => {
  return date.getTime() < Date.now() - 8.64e7
}

/**
 * 格式化截止时间显示
 * @param {string} deadline - 截止时间字符串
 * @returns {string} 格式化后的时间
 */
const formatDeadline = (deadline) => {
  if (!deadline) return ''
  const date = new Date(deadline)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

/**
 * 提交投票
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    // 检查有效选项数量
    if (validOptions.value.length < 2) {
      ElMessage.warning('至少需要2个有效选项')
      return
    }

    submitting.value = true

    // 构建投票消息数据
    const voteData = {
      type: 'vote',
      title: voteForm.title.trim(),
      options: validOptions.value.map((opt, index) => ({
        id: `opt_${index}`,
        text: opt.text.trim(),
        votes: 0,
        voters: [],
      })),
      voteType: voteForm.type,
      maxChoices: voteForm.type === 'multiple' ? voteForm.maxChoices : 1,
      anonymous: voteForm.anonymous,
      showResultBeforeVote: voteForm.showResultBeforeVote,
      deadline: voteForm.deadline,
      description: voteForm.description.trim(),
      createdAt: new Date().toISOString(),
      totalVotes: 0,
      status: 'active',  // 'active' | 'ended'
    }

    // 触发确认事件
    emit('confirm', voteData)

    ElMessage.success('投票创建成功')
    handleClose()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  resetForm()
}

/**
 * 重置表单
 */
const resetForm = () => {
  voteForm.title = ''
  voteForm.options = [{ text: '' }, { text: '' }]
  voteForm.type = 'single'
  voteForm.maxChoices = 2
  voteForm.anonymous = false
  voteForm.showResultBeforeVote = false
  voteForm.deadline = null
  voteForm.description = ''
  showPreview.value = false

  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// ==================== 监听器 ====================

/**
 * 监听对话框关闭时重置表单
 */
watch(visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.vote-dialog {
  :deep(.el-dialog__body) {
    padding: $spacing-lg $spacing-xl;
    max-height: 60vh;
    overflow-y: auto;
  }
}

// 投票表单
.vote-form {
  .options-form-item {
    :deep(.el-form-item__content) {
      display: block;
    }
  }

  .options-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .option-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      .option-index {
        width: 24px;
        height: 24px;
        @include flex-center;
        background-color: $bg-base;
        border-radius: $border-radius-round;
        font-size: 12px;
        color: $text-tertiary;
        flex-shrink: 0;
      }

      :deep(.el-input) {
        flex: 1;
      }

      .delete-btn {
        flex-shrink: 0;
        opacity: 0;
        transition: opacity $transition-fast $ease-base;
      }

      &:hover .delete-btn {
        opacity: 1;
      }
    }
  }

  .add-option-btn {
    padding: 0;
    height: auto;
    font-size: 13px;
  }

  .settings-form-item {
    :deep(.el-form-item__content) {
      display: block;
    }
  }

  .settings-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md $spacing-xl;

    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;

      &.full-width {
        grid-column: 1 / -1;
        flex-direction: column;
        align-items: flex-start;
        gap: $spacing-sm;
      }

      .setting-label {
        font-size: 13px;
        color: $text-secondary;
      }
    }
  }
}

// 投票预览
.vote-preview {
  margin-top: $spacing-lg;
  border: 1px solid $border-light;
  border-radius: $border-radius-base;
  overflow: hidden;

  .preview-header {
    @include flex-center;
    gap: $spacing-xs;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-base;
    font-size: 13px;
    color: $text-secondary;
    border-bottom: 1px solid $border-light;
  }

  .preview-content {
    padding: $spacing-lg;

    .preview-title {
      font-size: 15px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: $spacing-md;
    }

    .preview-options {
      display: flex;
      flex-direction: column;
      gap: $spacing-sm;

      .preview-option {
        padding: $spacing-sm $spacing-md;
        background-color: $bg-light;
        border-radius: $border-radius-sm;

        :deep(.el-radio),
        :deep(.el-checkbox) {
          width: 100%;

          .el-radio__label,
          .el-checkbox__label {
            color: $text-primary;
          }
        }
      }
    }

    .preview-meta {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-md;
      margin-top: $spacing-md;
      font-size: 12px;
      color: $text-tertiary;

      span {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
      }
    }
  }
}

// 底部操作
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .footer-actions {
    display: flex;
    gap: $spacing-sm;
  }
}

// 响应式适配
@media (max-width: $breakpoint-md) {
  .vote-dialog {
    :deep(.el-dialog) {
      width: 90% !important;
      margin: 5vh auto !important;
    }
  }

  .vote-form {
    .settings-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
