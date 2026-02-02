# 通讯录布局优化 - 实施指南

## 📦 已创建的文件

### 1. 优化方案文档
- **文件**：`docs/contacts-layout-optimization.md`
- **内容**：6个优化方案的详细设计和对比

### 2. 改进的组件

#### ContactItem.vue（已更新）
- ✅ 添加 `size` prop（compact/normal/expanded）
- ✅ 动态高度支持（48px/60px/72px）
- ✅ 响应式样式

#### ListGroupHeader.vue（新建）
- ✅ 粘性定位分组头
- ✅ 显示分组成员数
- ✅ 支持折叠/展开功能
- ✅ 暗色模式支持

### 3. 样式文件

#### contacts-mobile-optimization.scss（新建）
- ✅ 移动端布局优化（标签页 + 列表 + 抽屉）
- ✅ 平板端适配
- ✅ 大屏优化
- ✅ 暗色模式支持
- ✅ 触摸设备优化

---

## 🚀 实施步骤

### 第一步：导入新样式文件

在 `ruoyi-im-web/src/main.js` 中添加：

```javascript
import '@/styles/contacts-mobile-optimization.scss'
```

或在 `ruoyi-im-web/src/views/ContactsPanel.vue` 的 `<style>` 块中导入：

```scss
@import '@/styles/contacts-mobile-optimization.scss';
```

### 第二步：更新 ContactsPanel.vue

#### 2.1 导入新组件

```javascript
import ListGroupHeader from '@/components/Contacts/ListGroupHeader.vue'
```

#### 2.2 注册组件

```javascript
// 在 setup 中或全局注册
components: {
  ListGroupHeader
}
```

#### 2.3 更新虚拟列表渲染

将原来的分组头渲染改为使用新组件：

```vue
<!-- 原来的代码 -->
<div v-if="item.type === 'header'" class="list-group-header">
  {{ item.title }}
</div>

<!-- 改为 -->
<ListGroupHeader
  v-if="item.type === 'header'"
  :title="item.title"
  :count="getGroupCount(item.title)"
  :is-collapsed="collapsedGroups.has(item.title)"
  :collapsible="true"
  @toggle="toggleGroup(item.title)"
/>
```

#### 2.4 添加分组折叠逻辑

在 `<script setup>` 中添加：

```javascript
// 分组折叠状态
const collapsedGroups = ref(new Set())

// 切换分组折叠
const toggleGroup = (groupName) => {
  if (collapsedGroups.value.has(groupName)) {
    collapsedGroups.value.delete(groupName)
  } else {
    collapsedGroups.value.add(groupName)
  }
}

// 获取分组成员数
const getGroupCount = (groupName) => {
  if (currentNav.value === 'friends') {
    const group = friendGroups.value.find(g => g.name === groupName)
    return group?.friends?.length || 0
  }
  if (currentNav.value === 'org') {
    return letterCounts.value[groupName] || 0
  }
  return 0
}

// 过滤虚拟列表数据（支持折叠）
const filteredVirtualListData = computed(() => {
  return virtualListData.value.filter(item => {
    if (item.type === 'header') {
      return true
    }
    // 如果分组被折叠，隐藏该分组下的项
    const groupName = findItemGroupName(item)
    return !collapsedGroups.value.has(groupName)
  })
})

// 辅助函数：找到项所属的分组
const findItemGroupName = (item) => {
  // 根据当前导航查找分组
  if (currentNav.value === 'friends') {
    for (const group of friendGroups.value) {
      if (group.friends?.some(f => f.id === item.id)) {
        return group.name
      }
    }
  }
  if (currentNav.value === 'org') {
    return item.name?.[0]?.toUpperCase() || '#'
  }
  return ''
}
```

#### 2.5 更新虚拟列表绑定

```vue
<VirtualList
  v-else
  ref="virtualListRef"
  class="virtual-scroll-container"
  :items="filteredVirtualListData"  <!-- 改为过滤后的数据 -->
  :item-size="getItemSize"
  @scroll="handleListScroll"
>
  <!-- ... -->
</VirtualList>
```

### 第三步：在 ContactItem 中使用 size prop

根据不同场景传递 size 参数：

```vue
<!-- 紧凑模式 - 搜索结果 -->
<ContactItem
  :item="item"
  :is-active="selectedItemId === item.id"
  size="compact"
  @click="selectItem(item)"
/>

<!-- 标准模式 - 好友列表 -->
<ContactItem
  :item="item"
  :is-active="selectedItemId === item.id"
  size="normal"
  @click="selectItem(item)"
/>

<!-- 展开模式 - 组织架构 -->
<ContactItem
  :item="item"
  :is-active="selectedItemId === item.id"
  size="expanded"
  @click="selectItem(item)"
/>
```

### 第四步：测试各种场景

#### 4.1 桌面端测试
- [ ] 列表项高度切换（compact/normal/expanded）
- [ ] 分组头粘性定位
- [ ] 分组折叠/展开
- [ ] A-Z 索引栏功能
- [ ] 搜索面板

#### 4.2 平板端测试（768px - 1024px）
- [ ] 布局响应式调整
- [ ] 列表项高度适配
- [ ] 详情面板宽度调整

#### 4.3 移动端测试（< 768px）
- [ ] 顶部搜索栏显示
- [ ] 标签页导航
- [ ] 列表全屏显示
- [ ] 底部详情抽屉
- [ ] 触摸交互

#### 4.4 暗色模式测试
- [ ] 所有新组件的暗色样式
- [ ] 颜色对比度检查

---

## 📋 检查清单

### 代码质量
- [ ] 无 TypeScript 错误
- [ ] 无 ESLint 警告
- [ ] 无控制台错误
- [ ] 虚拟滚动性能正常

### 功能完整性
- [ ] 列表项高度动态调整
- [ ] 分组头显示和交互
- [ ] 分组折叠/展开
- [ ] 移动端布局切换
- [ ] 搜索功能正常

### 用户体验
- [ ] 动画流畅
- [ ] 响应速度快
- [ ] 触摸目标足够大（移动端）
- [ ] 无障碍访问支持

### 浏览器兼容性
- [ ] Chrome 最新版
- [ ] Firefox 最新版
- [ ] Safari 最新版
- [ ] Edge 最新版

---

## 🔧 常见问题

### Q1: 虚拟列表高度计算不准确？

**解决方案**：确保 `getItemSize` 函数返回的高度与实际 CSS 高度一致。

```javascript
const getItemSize = (item) => {
  if (item.type === 'header') return 32
  if (item.size === 'compact') return 48
  if (item.size === 'expanded') return 72
  return 60 // normal
}
```

### Q2: 分组折叠后虚拟列表不更新？

**解决方案**：确保使用 `computed` 而非 `ref` 来计算过滤后的数据，这样会自动触发更新。

```javascript
const filteredVirtualListData = computed(() => {
  // 过滤逻辑
})
```

### Q3: 移动端详情抽屉显示不正确？

**解决方案**：检查 z-index 值，确保抽屉在最上层。

```scss
.detail-panel {
  &.detail-open {
    z-index: 100; // 确保足够高
  }
}
```

### Q4: 暗色模式颜色不对？

**解决方案**：使用 CSS 变量而非硬编码颜色，并在 `.dark` 类下定义暗色值。

```scss
.dark {
  .list-group-header {
    background: var(--dt-bg-card-dark);
    color: var(--dt-text-primary-dark);
  }
}
```

---

## 📊 性能优化建议

### 1. 虚拟滚动优化
```javascript
// 增加缓冲区，减少闪烁
<VirtualList
  :buffer="5"
  :item-size="getItemSize"
/>
```

### 2. 搜索防抖
```javascript
const handleSearch = useDebounceFn(async () => {
  // 搜索逻辑
}, 300) // 300ms 防抖
```

### 3. 列表项 memo 化
```javascript
// 使用 v-memo 避免不必要的重新渲染
<ContactItem
  v-memo="[item.id, isActive, searchQuery]"
  :item="item"
  :is-active="isActive"
  :search-query="searchQuery"
/>
```

### 4. 图片懒加载
```javascript
// 在 ContactItem 中使用 v-lazy
<img v-lazy="item.avatar" />
```

---

## 🎨 设计令牌更新

如需添加新的设计令牌，在 `design-tokens.scss` 中添加：

```scss
// 列表项高度
--dt-contact-item-height-compact: 48px;
--dt-contact-item-height-normal: 60px;
--dt-contact-item-height-expanded: 72px;

// 分组头
--dt-group-header-height: 32px;
--dt-group-header-height-mobile: 28px;

// 详情面板
--dt-detail-panel-height-mobile: 60vh;
```

---

## 📚 相关文档

- [通讯录布局优化方案](./contacts-layout-optimization.md)
- [设计令牌文档](../ruoyi-im-web/src/styles/design-tokens.scss)
- [组件库文档](../ruoyi-im-web/src/components/Contacts/)

---

## 🤝 反馈和改进

如有任何问题或改进建议，请：

1. 检查上述检查清单
2. 查看常见问题部分
3. 参考性能优化建议
4. 联系设计团队讨论

