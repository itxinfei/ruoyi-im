# 统一面板布局规范

## 设计目标
统一所有功能面板的布局结构，提升一致性和开发效率。

## 基础布局结构

### 标准面板布局
```vue
<template>
  <div class="panel-container">
    <!-- 面板头部（可选） -->
    <div v-if="$slots.header" class="panel-header">
      <slot name="header" />
    </div>
    
    <!-- 面板内容区 -->
    <div class="panel-content">
      <!-- 工具栏（可选） -->
      <div v-if="$slots.toolbar" class="panel-toolbar">
        <slot name="toolbar" />
      </div>
      
      <!-- 主要内容 -->
      <div class="panel-main">
        <slot />
      </div>
    </div>
  </div>
</template>
```

### 带侧边栏的面板布局
```vue
<template>
  <div class="panel-with-sidebar">
    <!-- 侧边栏 -->
    <aside class="panel-sidebar">
      <slot name="sidebar" />
    </aside>
    
    <!-- 主内容区 -->
    <main class="panel-main-content">
      <slot />
    </main>
  </div>
</template>
```

## 设计令牌（Design Tokens）

### 间距系统
```scss
// 基础间距单位（8px网格）
$spacing-xs: 4px;   // 0.5rem
$spacing-sm: 8px;   // 1rem
$spacing-md: 16px;  // 2rem
$spacing-lg: 24px;  // 3rem
$spacing-xl: 32px;  // 4rem
$spacing-xxl: 48px; // 6rem
```

### 尺寸规范
```scss
// 面板相关
$panel-header-height: 56px;
$panel-toolbar-height: 48px;
$panel-sidebar-width: 260px;
$panel-sidebar-collapsed: 64px;

// 组件高度
$input-height: 32px;
$button-height: 32px;
$select-height: 32px;
```

### 圆角和阴影
```scss
// 圆角
$border-radius-sm: 4px;
$border-radius-md: 8px;
$border-radius-lg: 12px;
$border-radius-xl: 16px;

// 阴影
$shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.03);
$shadow-md: 0 2px 8px rgba(0, 0, 0, 0.08);
$shadow-lg: 0 4px 16px rgba(0, 0, 0, 0.12);
$shadow-xl: 0 8px 32px rgba(0, 0, 0, 0.16);
```

## 响应式断点

```scss
// 断点定义
$breakpoints: (
  xs: 0,
  sm: 640px,
  md: 768px,
  lg: 1024px,
  xl: 1280px,
  xxl: 1536px
);

// Mixins
@mixin mobile {
  @media (max-width: 767px) { @content; }
}

@mixin tablet {
  @media (min-width: 768px) and (max-width: 1023px) { @content; }
}

@mixin desktop {
  @media (min-width: 1024px) { @content; }
}
```

## 组件规范

### 1. 面板头部 (PanelHeader)
```vue
<template>
  <div class="panel-header">
    <div class="header-left">
      <h2 class="header-title">{{ title }}</h2>
      <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
    </div>
    <div class="header-right">
      <slot name="actions" />
    </div>
  </div>
</template>

<style lang="scss">
.panel-header {
  height: $panel-header-height;
  padding: 0 $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
  
  .header-left {
    display: flex;
    align-items: baseline;
    gap: $spacing-sm;
  }
  
  .header-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }
  
  .header-subtitle {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }
}
</style>
```

### 2. 搜索框 (SearchBox)
```vue
<template>
  <div class="search-box">
    <el-input
      v-model="searchValue"
      :placeholder="placeholder"
      :prefix-icon="Search"
      clearable
      @input="handleSearch"
    />
  </div>
</template>

<style lang="scss">
.search-box {
  margin-bottom: $spacing-md;
  
  .el-input {
    .el-input__wrapper {
      border-radius: $border-radius-lg;
      box-shadow: $shadow-sm;
      transition: box-shadow 0.3s ease;
      
      &:hover {
        box-shadow: $shadow-md;
      }
      
      &.is-focus {
        box-shadow: 0 0 0 2px var(--el-color-primary);
      }
    }
  }
}
</style>
```

### 3. 加载状态 (LoadingState)
```vue
<template>
  <div class="loading-state">
    <el-skeleton animated>
      <template #template>
        <div class="skeleton-item">
          <el-skeleton-item variant="rect" style="width: 40px; height: 40px;" />
          <el-skeleton-item variant="text" style="width: 60%;" />
          <el-skeleton-item variant="text" style="width: 40%;" />
        </div>
      </template>
    </el-skeleton>
  </div>
</template>
```

### 4. 空状态 (EmptyState)
```vue
<template>
  <div class="empty-state">
    <el-empty :description="description">
      <template #image>
        <slot name="icon">
          <el-icon size="64"><Document /></el-icon>
        </slot>
      </template>
      <template #description>
        <slot name="description">{{ description }}</slot>
      </template>
      <template #default>
        <slot name="action" />
      </template>
    </el-empty>
  </div>
</template>
```

## 页面实施建议

### 1. 联系人页面优化
- 使用统一的 PanelHeader
- 搜索框使用 SearchBox 组件
- 添加统一的 EmptyState
- 响应式侧边栏：移动端折叠

### 2. 工作台页面优化
- 标准化 Bento Grid 间距
- 统一卡片阴影和圆角
- 优化拖拽交互的视觉反馈
- 添加骨架屏加载状态

### 3. 文档管理优化
- 统一文件视图切换样式
- 标准化操作按钮组
- 优化面包屑导航
- 改进批量操作工具栏

## 动画规范

### 过渡时长
```scss
$transition-duration-fast: 0.15s;
$transition-duration-normal: 0.3s;
$transition-duration-slow: 0.5s;
```

### 缓动函数
```scss
$ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
$ease-out: cubic-bezier(0, 0, 0.2, 1);
$ease-in: cubic-bezier(0.4, 0, 1, 1);
```

### 常用动画类
```scss
.fade-enter-active,
.fade-leave-active {
  transition: opacity $transition-duration-normal $ease-in-out;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform $transition-duration-normal $ease-in-out;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}
```

## 实施优先级

### P0 - 立即实施
1. 创建 BasePanel 基础组件
2. 定义设计令牌文件
3. 修复明显的不一致问题（如头部高度）

### P1 - 近期实施
1. 抽取 PanelHeader、SearchBox 等组件
2. 统一加载和空状态
3. 实施响应式标准

### P2 - 后续优化
1. 完善动画系统
2. 优化交互细节
3. 性能优化（虚拟滚动、懒加载）