<template>
  <div class="admin-table">
    <!-- 搜索栏 -->
    <div
      v-if="showSearch"
      class="admin-table__search"
    >
      <slot name="search">
        <el-form
          :inline="true"
          :model="searchForm"
          class="search-form"
        >
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              :placeholder="searchPlaceholder"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :icon="Search"
              @click="handleSearch"
            >
              搜索
            </el-button>
            <el-button
              :icon="Refresh"
              @click="handleReset"
            >
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </slot>
    </div>

    <!-- 操作栏 -->
    <div
      v-if="$slots.actions || showExport"
      class="admin-table__actions"
    >
      <slot name="actions" />
      <el-button
        v-if="showExport"
        :icon="Download"
        @click="handleExport"
      >
        导出数据
      </el-button>
    </div>

    <!-- 批量操作栏 -->
    <div
      v-if="showBatch && selectedRows.length > 0"
      class="admin-table__batch"
    >
      <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      <slot
        name="batch"
        :selected-rows="selectedRows"
      />
      <el-button
        size="small"
        @click="clearSelection"
      >
        取消选择
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :stripe="stripe"
      :border="border"
      :empty-text="emptyText"
      :height="height"
      :max-height="maxHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <!-- 复选框列 -->
      <el-table-column
        v-if="showBatch"
        type="selection"
        width="55"
        :reserve-selection="true"
      />
      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        :index="indexMethod"
      />
      <!-- 自定义列 -->
      <slot />
    </el-table>

    <!-- 分页 -->
    <div
      v-if="showPagination"
      class="admin-table__pagination"
    >
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { debounce } from '@/utils/debounce'

/**
 * 管理后台表格组件
 * 集成搜索、分页、批量操作等常用功能
 */
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 总数
  total: {
    type: Number,
    default: 0
  },
  // 当前页码
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页数量
  pageSize: {
    type: Number,
    default: 20
  },
  // 分页大小选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 行唯一标识
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 是否显示搜索栏
  showSearch: {
    type: Boolean,
    default: true
  },
  // 搜索占位符
  searchPlaceholder: {
    type: String,
    default: '请输入关键词搜索'
  },
  // 是否显示导出按钮
  showExport: {
    type: Boolean,
    default: false
  },
  // 是否显示批量操作
  showBatch: {
    type: Boolean,
    default: false
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 是否显示序号
  showIndex: {
    type: Boolean,
    default: false
  },
  // 是否斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 是否边框
  border: {
    type: Boolean,
    default: false
  },
  // 空数据提示
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 表格高度
  height: {
    type: [String, Number],
    default: null
  },
  // 最大高度
  maxHeight: {
    type: [String, Number],
    default: null
  },
  // 搜索防抖延迟（毫秒）
  searchDebounce: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits([
  'search',
  'reset',
  'export',
  'selection-change',
  'sort-change',
  'page-change',
  'size-change',
  'update:currentPage',
  'update:pageSize'
])

const tableRef = ref(null)
const searchForm = ref({ keyword: '' })
const selectedRows = ref([])

// 序号计算
const indexMethod = index => {
  return (props.currentPage - 1) * props.pageSize + index + 1
}

// 搜索处理（带防抖）
const handleSearch = debounce(() => {
  emit('search', searchForm.value)
  emit('update:currentPage', 1)
}, props.searchDebounce)

// 重置搜索
const handleReset = () => {
  searchForm.value = { keyword: '' }
  emit('reset')
  emit('update:currentPage', 1)
}

// 导出
const handleExport = () => {
  emit('export')
}

// 选择变化
const handleSelectionChange = selection => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// 排序变化
const handleSortChange = sortInfo => {
  emit('sort-change', sortInfo)
}

// 清空选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
}

// 页码变化
const handlePageChange = page => {
  emit('update:currentPage', page)
  emit('page-change', page)
}

// 每页数量变化
const handleSizeChange = size => {
  emit('update:pageSize', size)
  emit('update:currentPage', 1)
  emit('size-change', size)
}

// 暴露方法
defineExpose({
  clearSelection,
  tableRef
})
</script>

<style scoped lang="scss">
.admin-table {
  &__search {
    margin-bottom: 16px;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  &__actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__batch {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    margin-bottom: 16px;
    background: var(--el-fill-color-light);
    border-radius: var(--dt-radius-sm);

    .selected-count {
      font-size: 14px;
      color: var(--el-text-color-primary);
    }
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
