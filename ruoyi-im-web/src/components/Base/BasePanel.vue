<template>
  <div class="base-panel" :class="[panelClass, { 'has-sidebar': hasSidebar }]">
    <!-- 面板头部 -->
    <div v-if="$slots.header" class="panel-header">
      <slot name="header" />
    </div>
    
    <!-- 带侧边栏的布局 -->
    <template v-if="hasSidebar">
      <aside class="panel-sidebar">
        <slot name="sidebar" />
      </aside>
      <main class="panel-main-content">
        <div v-if="$slots.toolbar" class="panel-toolbar">
          <slot name="toolbar" />
        </div>
        <div class="panel-main">
          <slot />
        </div>
      </main>
    </template>
    
    <!-- 标准布局 -->
    <template v-else>
      <div class="panel-content">
        <div v-if="$slots.toolbar" class="panel-toolbar">
          <slot name="toolbar" />
        </div>
        <div class="panel-main">
          <slot />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
const props = defineProps({
  // 面板类型：default, with-sidebar
  type: {
    type: String,
    default: 'default'
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

const hasSidebar = computed(() => props.type === 'with-sidebar')
const panelClass = computed(() => [`panel-${props.type}`, props.customClass])
</script>

<style lang="scss" scoped>
// 导入设计令牌
@use '@/styles/design-tokens' as *;

.base-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color);
  border-radius: $border-radius-lg;
  overflow: hidden;
  
  // 面板头部
  .panel-header {
    height: $panel-header-height;
    flex-shrink: 0;
    padding: 0 $spacing-lg;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--el-border-color-light);
    background: var(--el-bg-color-page);
    z-index: 10;
  }
  
  // 带侧边栏的布局
  &.panel-with-sidebar {
    .panel-sidebar {
      width: $panel-sidebar-width;
      flex-shrink: 0;
      border-right: 1px solid var(--el-border-color-light);
      background: var(--el-bg-color-page);
      overflow-y: auto;
      
      // 响应式折叠
      @include mobile {
        width: $panel-sidebar-collapsed;
        border-right: none;
        border-bottom: 1px solid var(--el-border-color-light);
      }
    }
    
    .panel-main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
      overflow: hidden;
    }
  }
  
  // 标准布局
  &.panel-default {
    .panel-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
  }
  
  // 工具栏
  .panel-toolbar {
    height: $panel-toolbar-height;
    flex-shrink: 0;
    padding: 0 $spacing-lg;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: var(--el-bg-color);
  }
  
  // 主内容区
  .panel-main {
    flex: 1;
    padding: $spacing-lg;
    overflow-y: auto;
    
    // 优化滚动
    -webkit-overflow-scrolling: touch;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: var(--dt-radius-sm);
      
      &:hover {
        background: var(--el-border-color-darker);
      }
    }
    
    // 移动端适配
    @include mobile {
      padding: $spacing-md;
    }
  }
}

// 过渡动画
.panel-enter-active,
.panel-leave-active {
  transition: all $transition-duration-normal $ease-in-out;
}

.panel-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.panel-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>