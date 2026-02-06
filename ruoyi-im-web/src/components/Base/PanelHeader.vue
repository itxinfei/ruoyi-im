<template>
  <div class="panel-header">
    <div class="header-left">
      <!-- 返回按钮（移动端） -->
      <el-button
        v-if="showBack"
        class="back-btn"
        text
        @click="$emit('back')"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      
      <!-- 标题区域 -->
      <div class="title-section">
        <h2 class="header-title">
          {{ title }}
        </h2>
        <p
          v-if="subtitle"
          class="header-subtitle"
        >
          {{ subtitle }}
        </p>
      </div>
    </div>
    
    <div class="header-right">
      <!-- 搜索框 -->
      <div
        v-if="searchable"
        class="header-search"
      >
        <el-input
          v-model="searchValue"
          :placeholder="searchPlaceholder"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>
      
      <!-- 操作按钮区 -->
      <div class="header-actions">
        <slot name="actions" />
        
        <!-- 更多按钮 -->
        <el-dropdown
          v-if="$slots.more"
          trigger="click"
        >
          <el-button text>
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <slot name="more" />
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { ArrowLeft, Search, MoreFilled } from '@element-plus/icons-vue'

const props = defineProps({
  // 标题
  title: {
    type: String,
    required: true
  },
  // 副标题
  subtitle: {
    type: String,
    default: ''
  },
  // 是否显示返回按钮
  showBack: {
    type: Boolean,
    default: false
  },
  // 是否显示搜索
  searchable: {
    type: Boolean,
    default: false
  },
  // 搜索框占位符
  searchPlaceholder: {
    type: String,
    default: '搜索...'
  },
  // 搜索防抖延迟（毫秒）
  searchDebounce: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['back', 'search'])

const searchValue = ref('')
let searchTimer = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    emit('search', searchValue.value)
  }, props.searchDebounce)
}

// 清理定时器
onBeforeUnmount(() => {
  clearTimeout(searchTimer)
})
</script>

<style lang="scss" scoped>
@use '@/styles/design-tokens' as *;

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: $panel-header-height;
  padding: 0 $spacing-lg;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  
  // 左侧区域
  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    
    .back-btn {
      margin-right: $spacing-sm;
      
      @include desktop {
        display: none;
      }
    }
    
    .title-section {
      min-width: 0;
      flex: 1;
    }
    
    .header-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .header-subtitle {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin: 2px 0 0 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  // 右侧区域
  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex-shrink: 0;
    
    .header-search {
      width: 240px;
      
      @include mobile {
        display: none;
      }
      
      :deep(.el-input__wrapper) {
        border-radius: $border-radius-lg;
        background: var(--el-fill-color-blank);
        box-shadow: none;
        border: 1px solid var(--el-border-color-lighter);
        
        &:hover {
          border-color: var(--el-border-color);
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
        }
      }
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      
      .el-button {
        color: var(--el-text-color-regular);
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
  
  // 移动端适配
  @include mobile {
    padding: 0 $spacing-md;
    
    .header-right {
      .header-actions {
        .el-button:not(.back-btn) {
          padding: 8px;
        }
      }
    }
  }
}
</style>