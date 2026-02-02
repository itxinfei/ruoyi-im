# 通讯录布局优化方案

## 📋 当前状态分析

### 现有布局
- **三栏布局**：左侧导航栏(56px) + 中间列表栏 + 右侧详情栏(360px)
- **左侧导航**：图标居中排列，超窄设计
- **中间列表**：支持虚拟滚动、A-Z索引、分组显示
- **右侧详情**：卡片式布局，支持群组和个人详情

### 存在的问题
1. **列表项高度固定** - 60px 的高度在不同内容量下显示不够灵活
2. **索引栏位置** - A-Z 索引栏在列表右侧，占用空间且交互不够直观
3. **搜索面板** - 搜索结果与主列表切换不够流畅
4. **移动端适配** - 三栏布局在小屏幕上显示压力大
5. **分组头部** - 分组标题与列表项视觉层级不够清晰
6. **详情面板** - 右侧详情栏在移动端体验不佳

---

## 🎯 优化方案

### 方案 1：列表项高度优化

**目标**：根据内容动态调整列表项高度，提升信息展示效率

**改进点**：
- 标准项：60px（当前）
- 紧凑项：48px（仅显示名称）
- 展开项：72px（显示名称+部门+在线状态）
- 分组头：32px（保持不变）

**实现方式**：
```javascript
// ContactItem.vue 中添加 size prop
const props = defineProps({
  size: {
    type: String,
    enum: ['compact', 'normal', 'expanded'],
    default: 'normal'
  }
})

// 根据 size 动态计算高度
const itemHeight = computed(() => {
  const heights = { compact: 48, normal: 60, expanded: 72 }
  return heights[props.size]
})
```

**样式调整**：
```scss
.contact-item {
  &.compact {
    height: 48px;
    .item-desc { display: none; }
  }
  
  &.expanded {
    height: 72px;
    .item-info { gap: 4px; }
  }
}
```

---

### 方案 2：A-Z 索引栏重设计

**目标**：改进索引栏的可用性和视觉效果

**改进点**：
- 从右侧移到**左侧导航栏下方**（当组织架构展开时）
- 改为**竖向排列**，与导航菜单保持一致
- 添加**快速定位反馈**（高亮 + 滚动动画）
- 支持**长按快速滚动**

**实现方式**：
```vue
<!-- 在 ContactsPanel.vue 中重新组织 -->
<aside class="sidebar">
  <!-- 搜索 + 导航菜单 -->
  <div class="search-section">...</div>
  <nav class="nav-list">...</nav>
  
  <!-- A-Z 索引栏 - 仅在组织架构时显示 -->
  <div v-if="currentNav === 'org' && showIndexBar" class="index-bar-vertical">
    <div
      v-for="letter in indexLetters"
      :key="letter"
      class="index-item"
      :class="{ active: activeLetter === letter }"
      @click="scrollToLetter(letter)"
      @mousedown="startIndexScroll"
    >
      {{ letter }}
    </div>
  </div>
</aside>
```

**样式**：
```scss
.index-bar-vertical {
  padding: 8px 0;
  border-top: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;
  
  .index-item {
    width: 40px;
    height: 20px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: var(--dt-text-tertiary);
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.15s ease;
    
    &:hover, &.active {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
      font-weight: 600;
    }
    
    &.disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }
  }
}
```

---

### 方案 3：搜索面板流畅化

**目标**：改进搜索体验，减少上下文切换

**改进点**：
- 搜索结果**不再全屏覆盖**，改为**侧滑面板**
- 搜索历史显示在**搜索框下方**
- 支持**实时搜索预览**
- 搜索结果与主列表**并排显示**（PC端）

**实现方式**：
```vue
<!-- 改进的搜索面板布局 -->
<div class="search-panel" :class="{ 'panel-open': showSearchPanel }">
  <!-- 搜索输入框 -->
  <div class="search-header">
    <input v-model="searchQuery" placeholder="搜索联系人、群组" />
  </div>
  
  <!-- 搜索历史 + 结果 -->
  <div class="search-content">
    <div v-if="!searchQuery" class="search-history">
      <!-- 历史记录 -->
    </div>
    <div v-else class="search-results">
      <!-- 搜索结果 -->
    </div>
  </div>
</div>
```

**样式**：
```scss
.search-panel {
  position: absolute;
  top: 0;
  left: 56px;
  width: 300px;
  height: 100%;
  background: #fff;
  border-right: 1px solid var(--dt-border-color);
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  z-index: 15;
  
  &.panel-open {
    transform: translateX(0);
  }
}
```

---

### 方案 4：移动端布局优化

**目标**：改进移动端的三栏布局体验

**改进点**：
- 采用**标签页切换**而非侧滑
- 详情面板改为**底部抽屉**（Drawer）
- 列表项高度**自适应**
- 搜索框**固定在顶部**

**实现方式**：
```vue
<!-- 移动端布局 -->
<div class="contacts-panel mobile-view">
  <!-- 顶部搜索栏 -->
  <div class="mobile-search-bar">
    <input placeholder="搜索" />
  </div>
  
  <!-- 标签页导航 -->
  <div class="mobile-tabs">
    <button v-for="tab in tabs" :key="tab" :class="{ active: currentNav === tab }">
      {{ tab }}
    </button>
  </div>
  
  <!-- 列表内容 -->
  <div class="mobile-list">
    <!-- 虚拟列表 -->
  </div>
  
  <!-- 底部详情抽屉 -->
  <el-drawer v-model="showDetailDrawer" direction="btt">
    <!-- 详情内容 -->
  </el-drawer>
</div>
```

---

### 方案 5：分组头部视觉优化

**目标**：提升分组标题的视觉层级和可读性

**改进点**：
- 分组头**粘性定位**（Sticky）
- 添加**背景色区分**
- 显示**分组内成员数**
- 支持**分组折叠/展开**

**实现方式**：
```vue
<!-- 改进的分组头 -->
<div class="list-group-header" :class="{ sticky: isSticky }">
  <div class="group-title">
    <span class="group-name">{{ item.title }}</span>
    <span class="group-count">({{ getGroupCount(item.title) }})</span>
  </div>
  <button class="group-toggle" @click="toggleGroup(item.title)">
    <svg :class="{ rotated: expandedGroups.has(item.title) }">...</svg>
  </button>
</div>
```

**样式**：
```scss
.list-group-header {
  position: sticky;
  top: 0;
  height: 32px;
  padding: 8px 12px;
  background: var(--dt-bg-card-hover);
  border-bottom: 1px solid var(--dt-border-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 10;
  
  .group-title {
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .group-count {
    margin-left: 4px;
    color: var(--dt-text-tertiary);
    font-weight: 400;
  }
}
```

---

### 方案 6：详情面板响应式优化

**目标**：改进详情面板在不同屏幕尺寸下的表现

**改进点**：
- **PC端**：保持右侧固定面板（360px）
- **平板端**：改为可折叠侧面板
- **移动端**：改为底部抽屉或全屏模态
- 详情内容**卡片化**，支持**滚动**

**实现方式**：
```scss
// 响应式断点
@media (max-width: 1024px) {
  .detail-panel {
    position: absolute;
    right: 0;
    width: 320px;
    transform: translateX(100%);
    transition: transform 0.3s ease;
    
    &.detail-open {
      transform: translateX(0);
    }
  }
}

@media (max-width: 768px) {
  .detail-panel {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    height: 60vh;
    border-radius: 12px 12px 0 0;
    transform: translateY(100%);
    
    &.detail-open {
      transform: translateY(0);
    }
  }
}
```

---

## 📊 优化对比表

| 方面 | 当前 | 优化后 | 收益 |
|------|------|--------|------|
| 列表项高度 | 固定 60px | 动态 48-72px | 信息展示更灵活 |
| A-Z 索引 | 右侧浮动 | 左侧导航栏 | 节省空间，交互更直观 |
| 搜索体验 | 全屏覆盖 | 侧滑面板 | 减少上下文切换 |
| 移动端 | 三栏压缩 | 标签页+抽屉 | 提升小屏体验 |
| 分组头 | 普通文本 | 粘性+可折叠 | 提升可读性 |
| 详情面板 | 固定宽度 | 响应式 | 适配各种屏幕 |

---

## 🚀 实施优先级

### 第一阶段（高优先级）
1. ✅ 列表项高度优化（方案 1）
2. ✅ 分组头部视觉优化（方案 5）
3. ✅ 移动端布局优化（方案 4）

### 第二阶段（中优先级）
4. ⏳ A-Z 索引栏重设计（方案 2）
5. ⏳ 搜索面板流畅化（方案 3）

### 第三阶段（低优先级）
6. ⏳ 详情面板响应式优化（方案 6）

---

## 📝 实施步骤

### 步骤 1：修改 ContactItem 组件
- 添加 `size` prop
- 根据 size 调整样式和高度

### 步骤 2：修改 ContactsPanel 组件
- 实现分组头粘性定位
- 优化移动端布局
- 改进搜索面板

### 步骤 3：更新设计令牌
- 添加新的高度变量
- 更新间距和颜色

### 步骤 4：测试和调整
- 各种屏幕尺寸测试
- 性能优化（虚拟滚动）
- 无障碍访问检查

---

## 💡 额外建议

1. **虚拟滚动优化** - 确保大列表性能
2. **搜索性能** - 添加防抖和节流
3. **暗色模式** - 确保所有新样式支持暗色主题
4. **动画过渡** - 使用 CSS 过渡而非 JS 动画
5. **键盘导航** - 支持 Tab、Enter、Esc 等快捷键

