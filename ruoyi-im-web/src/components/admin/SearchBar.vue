<template>
  <el-card class="admin-search-bar" shadow="never">
    <el-form :model="internalForm" inline>
      <template v-for="item in items" :key="item.prop">
        <!-- 输入框类型 -->
        <el-form-item v-if="item.type === 'input'" :label="item.label">
          <el-input
            v-model="internalForm[item.prop]"
            :placeholder="item.placeholder || `请输入${item.label}`"
            :clearable="item.clearable !== false"
            :style="{ width: item.width || '200px' }"
            @keyup.enter="handleSearch"
          >
            <template v-if="item.prefix" #prefix>
              <el-icon><component :is="item.prefix" /></el-icon>
            </template>
            <template v-if="item.suffix" #suffix>
              <el-icon><component :is="item.suffix" /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 选择器类型 -->
        <el-form-item v-else-if="item.type === 'select'" :label="item.label">
          <el-select
            v-model="internalForm[item.prop]"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :clearable="item.clearable !== false"
            :multiple="item.multiple"
            :style="{ width: item.width || '120px' }"
          >
            <el-option
              v-for="option in item.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <!-- 日期选择器类型 -->
        <el-form-item v-else-if="item.type === 'date'" :label="item.label">
          <el-date-picker
            v-model="internalForm[item.prop]"
            :type="item.dateType || 'date'"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :clearable="item.clearable !== false"
            :style="{ width: item.width || '200px' }"
            :format="item.format"
            :value-format="item.valueFormat"
          />
        </el-form-item>

        <!-- 日期范围选择器 -->
        <el-form-item v-else-if="item.type === 'daterange'" :label="item.label">
          <el-date-picker
            v-model="internalForm[item.prop]"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :format="item.format || 'YYYY-MM-DD HH:mm'"
            :value-format="item.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
            :style="{ width: item.width || '320px' }"
          />
        </el-form-item>

        <!-- 级联选择器 -->
        <el-form-item v-else-if="item.type === 'cascader'" :label="item.label">
          <el-cascader
            v-model="internalForm[item.prop]"
            :options="item.options"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :clearable="item.clearable !== false"
            :style="{ width: item.width || '200px' }"
          />
        </el-form-item>

        <!-- 开关 -->
        <el-form-item v-else-if="item.type === 'switch'" :label="item.label">
          <el-switch
            v-model="internalForm[item.prop]"
            :active-text="item.activeText"
            :inactive-text="item.inactiveText"
          />
        </el-form-item>
      </template>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button v-if="showExpand" text link @click="expandVisible = !expandVisible">
          {{ expandVisible ? '收起' : '展开' }}
          <el-icon>
            <ArrowUp v-if="expandVisible" />
            <ArrowDown v-else />
          </el-icon>
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 展开的额外搜索项 -->
    <el-collapse-transition>
      <div v-show="expandVisible">
        <el-divider />
        <el-form :model="internalForm" inline>
          <template v-for="item in expandItems" :key="item.prop">
            <el-form-item :label="item.label">
              <!-- 复用上面的搜索项类型逻辑 -->
              <el-input
                v-if="item.type === 'input'"
                v-model="internalForm[item.prop]"
                :placeholder="item.placeholder"
                :style="{ width: item.width || '200px' }"
                @keyup.enter="handleSearch"
              />
              <el-select
                v-else-if="item.type === 'select'"
                v-model="internalForm[item.prop]"
                :placeholder="item.placeholder"
                :clearable="item.clearable !== false"
                :style="{ width: item.width || '120px' }"
              >
                <el-option
                  v-for="option in item.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </template>
        </el-form>
      </div>
    </el-collapse-transition>
  </el-card>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Search, Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

/**
 * AdminSearchBar - 管理后台通用搜索栏组件
 *
 * @param {Array} items - 搜索项配置数组
 * @param {Array} expandItems - 展开区域额外搜索项（可选）
 * @param {Object} model - 表单数据模型（v-model）
 * @param {Boolean} showExpand - 是否显示展开按钮
 *
 * @example
 * <admin-search-bar
 *   v-model="searchForm"
 *   :items="[
 *     { prop: 'keyword', label: '关键词', type: 'input', prefix: Search },
 *     { prop: 'status', label: '状态', type: 'select', options: [...] }
 *   ]"
 *   @search="handleSearch"
 *   @reset="handleReset"
 * />
 */

const props = defineProps({
  // 搜索项配置
  items: {
    type: Array,
    required: true,
    default: () => []
  },
  // 展开的额外搜索项
  expandItems: {
    type: Array,
    default: () => []
  },
  // 表单数据
  model: {
    type: Object,
    default: () => ({})
  },
  // 是否显示展开按钮
  showExpand: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:model', 'search', 'reset'])

// 内部表单数据
const internalForm = ref({ ...props.model })
const expandVisible = ref(false)

// 监听外部 model 变化
watch(() => props.model, (newVal) => {
  internalForm.value = { ...newVal }
}, { deep: true })

// 监听内部表单变化，同步到外部
watch(internalForm, (newVal) => {
  emit('update:model', { ...newVal })
}, { deep: true })

// 搜索
const handleSearch = () => {
  emit('search', internalForm.value)
}

// 重置
const handleReset = () => {
  // 重置为初始值
  items.value.forEach(item => {
    if (item.type === 'daterange') {
      internalForm.value[item.prop] = []
    } else {
      internalForm.value[item.prop] = item.defaultValue || (item.type === 'select' ? '' : '')
    }
  })
  expandItems.value.forEach(item => {
    if (item.type === 'daterange') {
      internalForm.value[item.prop] = []
    } else {
      internalForm.value[item.prop] = item.defaultValue || ''
    }
  })
  emit('reset')
}

// 初始化默认值
props.items.forEach(item => {
  if (item.defaultValue !== undefined) {
    internalForm.value[item.prop] = item.defaultValue
  }
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

.admin-search-bar {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
  margin-bottom: var(--dt-space-md);
}

.admin-search-bar :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.admin-search-bar :deep(.el-form-item) {
  margin-bottom: var(--dt-space-sm);
  margin-right: var(--dt-space-md);
}

.admin-search-bar :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.admin-search-bar :deep(.el-form-item__label) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-normal);
}

.admin-search-bar :deep(.el-divider) {
  margin: var(--dt-space-sm) 0;
}

/* Input 图标颜色 */
.admin-search-bar :deep(.el-input__prefix),
.admin-search-bar :deep(.el-input__suffix) {
  color: var(--dt-text-placeholder);
}
</style>
