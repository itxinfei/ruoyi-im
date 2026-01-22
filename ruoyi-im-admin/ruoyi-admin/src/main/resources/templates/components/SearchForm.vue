<template>
  <div class="search-container">
    <!-- 搜索表单 -->
    <el-form
      ref="searchForm"
      :model="searchModel"
      :inline="inline"
      :label-width="labelWidth"
      class="search-form"
    >
      <!-- 动态渲染搜索字段 -->
      <template v-for="field in searchFields" :key="field.prop">
        <el-form-item
          :label="field.label"
          :prop="field.prop"
          :class="getFieldClass(field)"
        >
          <!-- 文本输入框 -->
          <el-input
            v-if="field.type === 'input'"
            v-model="searchModel[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            @keyup.enter="handleSearch"
            @clear="handleClear(field.prop)"
          >
            <template v-if="field.prepend" #prepend>
              {{ field.prepend }}
            </template>
            <template v-if="field.append" #append>
              {{ field.append }}
            </template>
          </el-input>

          <!-- 数字输入框 -->
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="searchModel[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :precision="field.precision"
            :controls-position="field.controlsPosition"
            @change="handleNumberChange(field.prop, $event)"
          />

          <!-- 选择器 -->
          <el-select
            v-else-if="field.type === 'select'"
            v-model="searchModel[field.prop]"
            :placeholder="field.placeholder || `请选择${field.label}`"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            :multiple="field.multiple"
            :collapse-tags="field.collapseTags"
            :filterable="field.filterable"
            :remote="field.remote"
            :remote-method="field.remoteMethod"
            :loading="field.loading"
            @change="handleSelectChange(field.prop, $event)"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
              :disabled="option.disabled"
            >
              <template v-if="option.render">
                <component :is="option.render" :option="option" />
              </template>
            </el-option>
          </el-select>

          <!-- 日期选择器 -->
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="searchModel[field.prop]"
            :type="field.dateType || 'date'"
            :placeholder="field.placeholder || `请选择${field.label}`"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :format="field.format"
            :value-format="field.valueFormat"
            :picker-options="field.pickerOptions"
            @change="handleDateChange(field.prop, $event)"
          />

          <!-- 日期范围选择器 -->
          <el-date-picker
            v-else-if="field.type === 'daterange'"
            v-model="searchModel[field.prop]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :format="field.format"
            :value-format="field.valueFormat"
            :picker-options="field.pickerOptions"
            @change="handleDateRangeChange(field.prop, $event)"
          />

          <!-- 级联选择器 -->
          <el-cascader
            v-else-if="field.type === 'cascader'"
            v-model="searchModel[field.prop]"
            :options="field.options"
            :placeholder="field.placeholder || `请选择${field.label}`"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :filterable="field.filterable"
            :props="field.props"
            @change="handleCascaderChange(field.prop, $event)"
          />

          <!-- 自定义插槽 -->
          <slot
            v-else-if="field.type === 'slot'"
            :name="field.slotName || field.prop"
            :field="field"
            :model="searchModel"
            :value="searchModel[field.prop]"
            :on-change="(val) => handleCustomChange(field.prop, val)"
          />

          <!-- 默认文本输入框 -->
          <el-input
            v-else
            v-model="searchModel[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </template>

      <!-- 操作按钮 -->
      <el-form-item v-if="showButtons" class="search-buttons">
        <el-button
          type="primary"
          :icon="Search"
          :loading="searching"
          @click="handleSearch"
        >
          {{ searchButtonText }}
        </el-button>
        
        <el-button
          :icon="Refresh"
          @click="handleReset"
        >
          重置
        </el-button>

        <!-- 更多操作按钮 -->
        <el-dropdown v-if="moreActions && moreActions.length > 0" @command="handleMoreAction">
          <el-button type="info">
            更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="action in moreActions"
                :key="action.key"
                :command="action.key"
                :disabled="action.disabled"
                :divided="action.divided"
              >
                <el-icon v-if="action.icon"><component :is="action.icon" /></el-icon>
                {{ action.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-form-item>
    </el-form>

    <!-- 搜索历史 -->
    <div v-if="showHistory && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <span>搜索历史</span>
        <el-button type="text" size="small" @click="clearHistory">
          清空历史
        </el-button>
      </div>
      <div class="history-items">
        <el-tag
          v-for="(item, index) in searchHistory"
          :key="index"
          closable
          size="small"
          @close="removeHistoryItem(index)"
          @click="applyHistoryItem(item)"
        >
          {{ getHistoryDisplay(item) }}
        </el-tag>
      </div>
    </div>

    <!-- 高级搜索展开按钮 -->
    <div v-if="advancedFields && advancedFields.length > 0" class="advanced-search">
      <el-button
        type="text"
        :icon="advancedExpanded ? ArrowUp : ArrowDown"
        @click="toggleAdvanced"
      >
        {{ advancedExpanded ? '收起' : '展开' }}高级搜索
      </el-button>
    </div>

    <!-- 高级搜索表单 -->
    <el-form
      v-if="advancedExpanded && advancedFields && advancedFields.length > 0"
      ref="advancedForm"
      :model="advancedModel"
      :inline="inline"
      :label-width="labelWidth"
      class="advanced-form"
    >
      <template v-for="field in advancedFields" :key="`advanced-${field.prop}`">
        <el-form-item
          :label="field.label"
          :prop="field.prop"
          :class="getFieldClass(field)"
        >
          <!-- 复用相同的字段渲染逻辑 -->
          <component
            :is="getFieldComponent(field)"
            v-bind="getFieldProps(field)"
            v-model="advancedModel[field.prop]"
            @change="handleAdvancedChange(field.prop, $event)"
          />
        </el-form-item>
      </template>
    </el-form>

    <!-- 快速过滤标签 -->
    <div v-if="quickFilters && quickFilters.length > 0" class="quick-filters">
      <span class="filter-label">快速过滤:</span>
      <el-tag
        v-for="filter in quickFilters"
        :key="filter.key"
        :type="activeQuickFilter === filter.key ? 'primary' : ''"
        :effect="activeQuickFilter === filter.key ? 'dark' : 'plain'"
        clickable
        @click="handleQuickFilter(filter)"
      >
        {{ filter.label }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  ArrowDown,
  ArrowUp
} from '@element-plus/icons-vue'

// Props 定义
const props = defineProps({
  // 搜索字段配置
  searchFields: {
    type: Array,
    required: true,
    default: () => []
  },
  
  // 高级搜索字段配置
  advancedFields: {
    type: Array,
    default: () => []
  },
  
  // 快速过滤配置
  quickFilters: {
    type: Array,
    default: () => []
  },
  
  // 表单配置
  inline: {
    type: Boolean,
    default: true
  },
  
  labelWidth: {
    type: String,
    default: '80px'
  },
  
  showButtons: {
    type: Boolean,
    default: true
  },
  
  searchButtonText: {
    type: String,
    default: '搜索'
  },
  
  // 搜索功能配置
  showHistory: {
    type: Boolean,
    default: false
  },
  
  maxHistory: {
    type: Number,
    default: 10
  },
  
  // 更多操作配置
  moreActions: {
    type: Array,
    default: () => []
  },
  
  // 外部搜索状态
  searching: {
    type: Boolean,
    default: false
  }
})

// Emits 定义
const emit = defineEmits([
  'search',
  'reset',
  'change',
  'clear',
  'more-action',
  'quick-filter',
  'advanced-change'
])

// 响应式数据
const searchForm = ref()
const advancedForm = ref()

const searchModel = reactive({})
const advancedModel = reactive({})

const advancedExpanded = ref(false)
const searchHistory = ref([])
const activeQuickFilter = ref('')

// 计算属性
const hasSearchData = computed(() => {
  return Object.values(searchModel).some(value => {
    if (Array.isArray(value)) {
      return value.length > 0
    }
    return value !== null && value !== undefined && value !== ''
  })
})

// 监听搜索字段变化，初始化搜索模型
watch(() => props.searchFields, (fields) => {
  fields.forEach(field => {
    if (searchModel[field.prop] === undefined) {
      searchModel[field.prop] = field.defaultValue || (field.multiple ? [] : '')
    }
  })
}, { immediate: true, deep: true })

// 监听高级搜索字段变化，初始化高级搜索模型
watch(() => props.advancedFields, (fields) => {
  fields.forEach(field => {
    if (advancedModel[field.prop] === undefined) {
      advancedModel[field.prop] = field.defaultValue || (field.multiple ? [] : '')
    }
  })
}, { immediate: true, deep: true })

// 方法定义
const handleSearch = () => {
  const formData = {
    ...searchModel,
    ...(advancedExpanded.value ? advancedModel : {})
  }
  
  emit('search', formData)
  
  // 保存搜索历史
  if (props.showHistory && hasSearchData.value) {
    saveToHistory(formData)
  }
}

const handleReset = () => {
  // 重置基础搜索表单
  searchForm.value?.resetFields()
  props.searchFields.forEach(field => {
    searchModel[field.prop] = field.defaultValue || (field.multiple ? [] : '')
  })
  
  // 重置高级搜索表单
  advancedForm.value?.resetFields()
  props.advancedFields.forEach(field => {
    advancedModel[field.prop] = field.defaultValue || (field.multiple ? [] : '')
  })
  
  // 重置快速过滤
  activeQuickFilter.value = ''
  
  emit('reset')
}

const handleClear = (prop) => {
  searchModel[prop] = ''
  emit('clear', prop)
}

const handleCustomChange = (prop, value) => {
  searchModel[prop] = value
  emit('change', prop, value)
}

const handleNumberChange = (prop, value) => {
  emit('change', prop, value)
}

const handleSelectChange = (prop, value) => {
  emit('change', prop, value)
}

const handleDateChange = (prop, value) => {
  emit('change', prop, value)
}

const handleDateRangeChange = (prop, value) => {
  emit('change', prop, value)
}

const handleCascaderChange = (prop, value) => {
  emit('change', prop, value)
}

const handleAdvancedChange = (prop, value) => {
  emit('advanced-change', prop, value)
}

const handleMoreAction = (command) => {
  emit('more-action', command)
}

const handleQuickFilter = (filter) => {
  activeQuickFilter.value = filter.key
  emit('quick-filter', filter)
}

const toggleAdvanced = () => {
  advancedExpanded.value = !advancedExpanded.value
}

const getFieldClass = (field) => {
  const classes = []
  
  if (field.class) {
    classes.push(field.class)
  }
  
  if (field.span) {
    classes.push(`el-col-${field.span}`)
  }
  
  if (field.offset) {
    classes.push(`el-col-offset-${field.offset}`)
  }
  
  return classes.join(' ')
}

const getFieldComponent = (field) => {
  const componentMap = {
    input: 'el-input',
    number: 'el-input-number',
    select: 'el-select',
    date: 'el-date-picker',
    daterange: 'el-date-picker',
    cascader: 'el-cascader'
  }
  
  return componentMap[field.type] || 'el-input'
}

const getFieldProps = (field) => {
  const props = { ...field }
  
  // 移除不需要传递给组件的属性
  delete props.label
  delete props.prop
  delete props.defaultValue
  delete props.class
  
  return props
}

// 搜索历史相关方法
const saveToHistory = (data) => {
  // 过滤空值并创建历史记录
  const historyItem = {}
  Object.keys(data).forEach(key => {
    const value = data[key]
    if (value !== null && value !== undefined && value !== '' && 
        (!Array.isArray(value) || value.length > 0)) {
      historyItem[key] = value
    }
  })
  
  if (Object.keys(historyItem).length === 0) return
  
  // 检查是否已存在相同的历史记录
  const existingIndex = searchHistory.value.findIndex(item => 
    JSON.stringify(item) === JSON.stringify(historyItem)
  )
  
  if (existingIndex > -1) {
    // 移到最前面
    searchHistory.value.splice(existingIndex, 1)
  }
  
  // 添加到最前面
  searchHistory.value.unshift(historyItem)
  
  // 限制历史记录数量
  if (searchHistory.value.length > props.maxHistory) {
    searchHistory.value = searchHistory.value.slice(0, props.maxHistory)
  }
}

const applyHistoryItem = (item) => {
  // 应用历史记录到搜索模型
  Object.keys(item).forEach(key => {
    if (searchModel.hasOwnProperty(key)) {
      searchModel[key] = item[key]
    } else if (advancedModel.hasOwnProperty(key)) {
      advancedModel[key] = item[key]
      if (!advancedExpanded.value) {
        advancedExpanded.value = true
      }
    }
  })
  
  // 自动执行搜索
  nextTick(() => {
    handleSearch()
  })
}

const removeHistoryItem = (index) => {
  searchHistory.value.splice(index, 1)
}

const clearHistory = () => {
  searchHistory.value = []
  ElMessage.success('搜索历史已清空')
}

const getHistoryDisplay = (item) => {
  const labels = []
  
  Object.keys(item).forEach(key => {
    const field = [...props.searchFields, ...props.advancedFields]
      .find(f => f.prop === key)
    
    if (field) {
      const value = item[key]
      let displayValue = value
      
      // 处理不同类型的显示值
      if (field.type === 'select' && field.options) {
        const option = field.options.find(opt => opt.value === value)
        displayValue = option ? option.label : value
      } else if (Array.isArray(value)) {
        displayValue = value.length ? `${value.length}项` : ''
      } else if (field.type === 'daterange' && Array.isArray(value) && value.length === 2) {
        displayValue = `${value[0]} 至 ${value[1]}`
      } else if (typeof value === 'string' && value.length > 10) {
        displayValue = value.substring(0, 10) + '...'
      }
      
      if (displayValue) {
        labels.push(`${field.label}: ${displayValue}`)
      }
    }
  })
  
  return labels.join(', ') || '搜索条件'
}

// 暴露方法给父组件
defineExpose({
  getModel: () => ({ ...searchModel, ...(advancedExpanded.value ? advancedModel : {}) }),
  setModel: (model) => {
    Object.keys(model).forEach(key => {
      if (searchModel.hasOwnProperty(key)) {
        searchModel[key] = model[key]
      } else if (advancedModel.hasOwnProperty(key)) {
        advancedModel[key] = model[key]
      }
    })
  },
  reset: handleReset,
  search: handleSearch,
  clearHistory
})
</script>

<style scoped>
.search-container {
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 0;
}

.search-buttons {
  margin-left: auto;
}

.search-buttons .el-button {
  margin-left: 8px;
}

.search-history {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.history-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-items .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.history-items .el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.advanced-search {
  margin-top: 12px;
  text-align: center;
}

.advanced-form {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.quick-filters {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.quick-filters .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-filters .el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    padding: 12px;
  }
  
  .search-form :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  .search-buttons {
    margin-left: 0;
    display: flex;
    gap: 8px;
  }
  
  .quick-filters {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>