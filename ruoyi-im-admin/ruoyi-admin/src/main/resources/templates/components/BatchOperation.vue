<template>
  <div class="batch-operation-container">
    <!-- 批量操作选择工具栏 -->
    <div class="batch-toolbar">
      <div class="selection-info">
        <el-checkbox
          v-model="selectAll"
          :indeterminate="isIndeterminate"
          @change="handleSelectAll"
          :disabled="disabled"
        >
          全选
        </el-checkbox>
        
        <span v-if="selectedCount > 0" class="selection-count">
          已选择 {{ selectedCount }} 项
          <el-button
            type="text"
            size="small"
            @click="clearSelection"
          >
            清空选择
          </el-button>
        </span>
      </div>

      <div class="batch-actions">
        <template v-for="action in availableActions" :key="action.key">
          <el-button
            :type="action.type || 'default'"
            :size="action.size || 'small'"
            :disabled="action.disabled || selectedCount === 0"
            :loading="action.loading"
            :icon="action.icon"
            @click="handleAction(action)"
            v-show="!action.hidden"
          >
            {{ action.label }}
            <el-badge
              v-if="action.showBadge && selectedCount > 0"
              :value="selectedCount"
              class="action-badge"
            />
          </el-button>
        </template>
      </div>
    </div>

    <!-- 批量操作进度条 -->
    <div v-if="batchProcessing" class="batch-progress">
      <div class="progress-info">
        <span>{{ progressText }}</span>
        <el-button
          type="text"
          size="small"
          @click="cancelBatchOperation"
          :disabled="!cancellable"
        >
          取消
        </el-button>
      </div>
      <el-progress
        :percentage="progressPercentage"
        :status="progressStatus"
        :stroke-width="6"
      />
      <div class="progress-details">
        <span>成功: {{ successCount }}</span>
        <span>失败: {{ failCount }}</span>
        <span v-if="skippedCount > 0">跳过: {{ skippedCount }}</span>
      </div>
    </div>

    <!-- 快速选择工具 -->
    <div class="quick-selection">
      <el-dropdown @command="handleQuickSelect" trigger="click">
        <el-button type="text" size="small">
          快速选择<el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="option in quickSelectOptions"
              :key="option.key"
              :command="option.key"
              :disabled="option.disabled"
            >
              {{ option.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 高级批量操作对话框 -->
    <el-dialog
      v-model="advancedDialogVisible"
      :title="currentAction?.label"
      width="600px"
      destroy-on-close
    >
      <div class="advanced-operation">
        <!-- 操作确认信息 -->
        <div class="operation-confirmation">
          <el-icon class="warning-icon"><Warning /></el-icon>
          <div class="confirmation-text">
            <h4>{{ currentAction?.confirmText || `确定要${currentAction?.label}吗？` }}</h4>
            <p>此操作将影响 {{ selectedCount }} 个项目，请谨慎操作！</p>
          </div>
        </div>

        <!-- 操作参数配置 -->
        <el-form
          v-if="currentAction?.formFields"
          ref="operationForm"
          :model="operationData"
          :rules="operationRules"
          label-width="100px"
        >
          <el-form-item
            v-for="field in currentAction.formFields"
            :key="field.prop"
            :label="field.label"
            :prop="field.prop"
          >
            <!-- 文本输入 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="operationData[field.prop]"
              :placeholder="field.placeholder"
              :type="field.inputType || 'text'"
            />
            
            <!-- 选择器 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="operationData[field.prop]"
              :placeholder="field.placeholder"
              :multiple="field.multiple"
              :options="field.options"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            
            <!-- 开关 -->
            <el-switch
              v-else-if="field.type === 'switch'"
              v-model="operationData[field.prop]"
              :active-text="field.activeText"
              :inactive-text="field.inactiveText"
            />
            
            <!-- 数量输入 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="operationData[field.prop]"
              :min="field.min"
              :max="field.max"
              :step="field.step"
            />
          </el-form-item>
        </el-form>

        <!-- 预览选中项目 -->
        <div v-if="showPreview" class="preview-section">
          <h5>预览选中项目</h5>
          <el-table
            :data="selectedItemsPreview"
            size="small"
            max-height="200"
            border
          >
            <el-table-column
              v-for="column in previewColumns"
              :key="column.prop"
              :prop="column.prop"
              :label="column.label"
              :width="column.width"
            />
          </el-table>
          <div v-if="selectedCount > previewLimit" class="preview-more">
            ...还有 {{ selectedCount - previewLimit }} 项未显示
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="advancedDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAdvancedOperation"
          :loading="processing"
        >
          {{ currentAction?.confirmButtonText || '确认执行' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量操作结果详情 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="批量操作结果"
      width="800px"
    >
      <div class="operation-result">
        <div class="result-summary">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总数" :value="selectedCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="成功" :value="successCount" suffix="项" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="失败" :value="failCount" suffix="项" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="成功率" :value="successRate" suffix="%" />
            </el-col>
          </el-row>
        </div>

        <div v-if="failCount > 0" class="failure-details">
          <h5>失败详情</h5>
          <el-table
            :data="failureItems"
            size="small"
            max-height="300"
            border
          >
            <el-table-column prop="item" label="项目" width="200" />
            <el-table-column prop="error" label="错误信息" />
          </el-table>
        </div>

        <div class="result-actions">
          <el-button @click="resultDialogVisible = false">关闭</el-button>
          <el-button
            v-if="failCount > 0 && canRetry"
            type="warning"
            @click="retryFailedItems"
          >
            重试失败项
          </el-button>
          <el-button
            v-if="hasExport"
            type="success"
            @click="exportResult"
          >
            导出结果
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Warning } from '@element-plus/icons-vue'

// Props 定义
const props = defineProps({
  // 数据项列表
  items: {
    type: Array,
    required: true,
    default: () => []
  },
  
  // 选中的项
  selectedItems: {
    type: Array,
    default: () => []
  },
  
  // 批量操作配置
  actions: {
    type: Array,
    default: () => []
  },
  
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  
  // 标识字段名
  idKey: {
    type: String,
    default: 'id'
  },
  
  // 是否显示预览
  showPreview: {
    type: Boolean,
    default: true
  },
  
  // 预览限制数量
  previewLimit: {
    type: Number,
    default: 10
  },
  
  // 预览列配置
  previewColumns: {
    type: Array,
    default: () => [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '名称' }
    ]
  },
  
  // 是否支持重试
  canRetry: {
    type: Boolean,
    default: true
  },
  
  // 是否支持导出结果
  hasExport: {
    type: Boolean,
    default: true
  }
})

// Emits 定义
const emit = defineEmits([
  'selection-change',
  'batch-operation',
  'select-all',
  'quick-select',
  'retry-failed',
  'export-result'
])

// 响应式数据
const selectAll = ref(false)
const isIndeterminate = ref(false)
const batchProcessing = ref(false)
const processing = ref(false)
const cancellable = ref(true)

const advancedDialogVisible = ref(false)
const resultDialogVisible = ref(false)

const currentAction = ref(null)
const operationData = reactive({})
const operationRules = reactive({})
const operationForm = ref()

// 进度相关
const progressPercentage = ref(0)
const progressStatus = ref('')
const progressText = ref('')
const successCount = ref(0)
const failCount = ref(0)
const skippedCount = ref(0)
const failureItems = ref([])

// 计算属性
const selectedCount = computed(() => props.selectedItems.length)

const availableActions = computed(() => {
  return props.actions.map(action => ({
    ...action,
    disabled: action.disabled || (action.requireSelection && selectedCount.value === 0)
  }))
})

const successRate = computed(() => {
  if (selectedCount.value === 0) return 0
  return Math.round((successCount.value / selectedCount.value) * 100)
})

const selectedItemsPreview = computed(() => {
  return props.selectedItems.slice(0, props.previewLimit)
})

const quickSelectOptions = computed(() => {
  const options = []
  
  // 根据数据类型提供快速选择选项
  if (props.items.length > 0) {
    options.push({ key: 'all', label: '选择全部' })
    options.push({ key: 'none', label: '清空选择' })
    options.push({ key: 'reverse', label: '反向选择' })
    
    // 可以根据具体业务添加更多选项
    // options.push({ key: 'first-10', label: '选择前10项' })
    // options.push({ key: 'last-10', label: '选择后10项' })
  }
  
  return options
})

// 监听选中项变化
watch(() => props.selectedItems, (newSelection) => {
  const itemCount = props.items.length
  const selectedCount = newSelection.length
  
  if (selectedCount === 0) {
    selectAll.value = false
    isIndeterminate.value = false
  } else if (selectedCount === itemCount) {
    selectAll.value = true
    isIndeterminate.value = false
  } else {
    selectAll.value = false
    isIndeterminate.value = true
  }
  
  emit('selection-change', newSelection)
}, { deep: true })

// 方法定义
const handleSelectAll = (checked) => {
  if (checked) {
    // 全选
    const allItems = props.items.map(item => item[props.idKey])
    emit('select-all', allItems)
  } else {
    // 取消全选
    emit('select-all', [])
  }
}

const clearSelection = () => {
  emit('select-all', [])
}

const handleAction = async (action) => {
  currentAction.value = action
  
  // 检查是否需要显示高级对话框
  if (action.needsConfirmation || action.formFields) {
    // 初始化操作数据
    Object.keys(operationData).forEach(key => {
      delete operationData[key]
    })
    
    if (action.formFields) {
      action.formFields.forEach(field => {
        operationData[field.prop] = field.defaultValue || (field.multiple ? [] : '')
      })
    }
    
    // 设置验证规则
    Object.keys(operationRules).forEach(key => {
      delete operationRules[key]
    })
    
    if (action.formRules) {
      Object.assign(operationRules, action.formRules)
    }
    
    advancedDialogVisible.value = true
  } else {
    // 直接执行操作
    await executeBatchOperation(action)
  }
}

const confirmAdvancedOperation = async () => {
  // 表单验证
  if (currentAction.value.formFields) {
    try {
      await operationForm.value.validate()
    } catch (error) {
      return
    }
  }
  
  advancedDialogVisible.value = false
  await executeBatchOperation(currentAction.value, operationData)
}

const executeBatchOperation = async (action, params = {}) => {
  batchProcessing.value = true
  progressPercentage.value = 0
  progressStatus.value = ''
  progressText.value = `正在执行${action.label}...`
  successCount.value = 0
  failCount.value = 0
  skippedCount.value = 0
  failureItems.value = []
  cancellable.value = true
  
  try {
    // 发送批量操作请求
    const result = await emit('batch-operation', {
      action: action.key,
      items: props.selectedItems,
      params,
      onProgress: (progress) => {
        progressPercentage.value = progress.percentage
        progressText.value = progress.text || `${action.label}中...`
        successCount.value = progress.success || 0
        failCount.value = progress.fail || 0
        skippedCount.value = progress.skipped || 0
        
        if (progress.failureItems) {
          failureItems.value = progress.failureItems
        }
      }
    })
    
    if (result && result.success) {
      progressPercentage.value = 100
      progressStatus.value = 'success'
      progressText.value = `${action.label}完成！`
      
      ElMessage.success(`${action.label}成功！`)
      
      // 显示结果详情
      if (failCount.value > 0 || action.showResult) {
        showResultDialog()
      }
    } else {
      throw new Error(result?.message || '操作失败')
    }
  } catch (error) {
    progressStatus.value = 'exception'
    progressText.value = `${action.label}失败: ${error.message}`
    ElMessage.error(`${action.label}失败: ${error.message}`)
  } finally {
    batchProcessing.value = false
  }
}

const cancelBatchOperation = () => {
  if (!cancellable.value) return
  
  cancellable.value = false
  progressStatus.value = 'warning'
  progressText.value = '正在取消操作...'
  
  // 触发取消事件
  emit('cancel-batch-operation')
  
  setTimeout(() => {
    batchProcessing.value = false
    ElMessage.info('操作已取消')
  }, 1000)
}

const handleQuickSelect = (command) => {
  switch (command) {
    case 'all':
      emit('select-all', props.items.map(item => item[props.idKey]))
      break
    case 'none':
      emit('select-all', [])
      break
    case 'reverse':
      const currentSelection = new Set(props.selectedItems)
      const reversedSelection = props.items
        .map(item => item[props.idKey])
        .filter(id => !currentSelection.has(id))
      emit('select-all', reversedSelection)
      break
    default:
      // 其他自定义快速选择
      emit('quick-select', command)
  }
}

const showResultDialog = () => {
  resultDialogVisible.value = true
}

const retryFailedItems = () => {
  if (failCount.value === 0) return
  
  const failedIds = failureItems.value.map(item => item.itemId)
  emit('retry-failed', failedIds)
  resultDialogVisible.value = false
}

const exportResult = () => {
  const result = {
    total: selectedCount.value,
    success: successCount.value,
    fail: failCount.value,
    skipped: skippedCount.value,
    failureItems: failureItems.value
  }
  
  emit('export-result', result)
}

// 暴露方法给父组件
defineExpose({
  clearSelection,
  executeBatchOperation,
  cancelBatchOperation,
  showResultDialog
})
</script>

<style scoped>
.batch-operation-container {
  background-color: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selection-count {
  color: #606266;
  font-size: 14px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-badge {
  margin-left: 4px;
}

.batch-progress {
  padding: 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-details {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}

.quick-selection {
  padding: 8px 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.advanced-operation {
  padding: 0;
}

.operation-confirmation {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fdf6ec;
  border-radius: 4px;
}

.warning-icon {
  font-size: 24px;
  color: #e6a23c;
  margin-top: 2px;
}

.confirmation-text h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.confirmation-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.preview-section {
  margin-top: 20px;
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.preview-section h5 {
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 500;
}

.preview-more {
  text-align: center;
  padding: 8px;
  color: #909399;
  font-size: 12px;
  background-color: #f8f9fa;
}

.operation-result {
  padding: 0;
}

.result-summary {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.failure-details {
  margin-bottom: 20px;
}

.failure-details h5 {
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 500;
}

.result-actions {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.result-actions .el-button {
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .batch-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .selection-info,
  .batch-actions {
    justify-content: center;
  }
  
  .batch-actions {
    flex-wrap: wrap;
  }
  
  .progress-details {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
  
  .operation-confirmation {
    flex-direction: column;
    text-align: center;
  }
  
  .result-summary {
    padding: 16px;
  }
}
</style>