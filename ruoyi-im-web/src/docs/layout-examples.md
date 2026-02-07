# 统一布局组件使用示例

## 1. 基础面板使用

### 标准面板布局
```vue
<template>
  <BasePanel>
    <template #header>
      <PanelHeader 
        title="待办事项"
        subtitle="共 12 项待办"
        :searchable="true"
        @search="handleSearch"
      >
        <template #actions>
          <el-button type="primary" @click="addTodo">
            <el-icon><Plus /></el-icon>
            新建
          </el-button>
        </template>
      </PanelHeader>
    </template>
    
    <!-- 主内容 -->
    <div class="todo-list">
      <div v-for="item in todos" :key="item.id" class="todo-item">
        <!-- 内容 -->
      </div>
    </div>
  </BasePanel>
</template>
```

### 带侧边栏的面板
```vue
<template>
  <BasePanel type="with-sidebar" class="contacts-panel">
    <template #header>
      <PanelHeader 
        title="通讯录"
        :searchable="true"
        @search="handleSearch"
      />
    </template>
    
    <template #sidebar>
      <div class="sidebar-nav">
        <div 
          v-for="tab in tabs"
          :key="tab.key"
          :class="['nav-item', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <span v-if="tab.count" class="badge">{{ tab.count }}</span>
        </div>
      </div>
    </template>
    
    <template #toolbar>
      <div class="toolbar-actions">
        <el-button-group>
          <el-button :type="viewType === 'list' ? 'primary' : 'default'">
            <el-icon><List /></el-icon>
          </el-button>
          <el-button :type="viewType === 'grid' ? 'primary' : 'default'">
            <el-icon><Grid /></el-icon>
          </el-button>
        </el-button-group>
        <el-divider direction="vertical" />
        <el-dropdown trigger="click">
          <el-button>
            筛选 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>全部</el-dropdown-item>
              <el-dropdown-item>在线</el-dropdown-item>
              <el-dropdown-item>离线</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>
    
    <div class="contacts-content">
      <!-- 动态内容区 -->
    </div>
  </BasePanel>
</template>
```

## 2. 搜索框组件使用

### 基础搜索
```vue
<template>
  <SearchBox
    placeholder="搜索联系人、群组..."
    @search="handleSearch"
  />
</template>
```

### 带建议的搜索
```vue
<template>
  <SearchBox
    v-model="searchQuery"
    placeholder="搜索消息记录..."
    :show-suggestions="true"
    :suggestions="suggestions"
    @search="handleSearch"
    @select="handleSelect"
  >
    <template #prefix>
      <el-icon><Search /></el-icon>
    </template>
  </SearchBox>
</template>

<script setup>
const searchQuery = ref('')
const suggestions = computed(() => [
  { text: '最近的对话', type: '分类' },
  { text: '重要文件', type: '分类' },
  { text: '项目文档', type: '文件' },
  { text: '会议记录', type: '消息' }
])

const handleSearch = (query) => {
  console.log('搜索:', query)
}

const handleSelect = (item) => {
  console.log('选择:', item)
}
</script>
```

### 带快捷键的搜索
```vue
<template>
  <div class="search-container">
    <SearchBox
      ref="searchBoxRef"
      placeholder="按 Ctrl+K 快速搜索..."
      @focus="showShortcuts = true"
      @blur="hideShortcuts"
    />
    
    <div v-if="showShortcuts" class="search-shortcuts">
      <div class="shortcut-item">
        <kbd>↑</kbd><kbd>↓</kbd>
        <span>导航</span>
      </div>
      <div class="shortcut-item">
        <kbd>Enter</kbd>
        <span>选择</span>
      </div>
      <div class="shortcut-item">
        <kbd>Esc</kbd>
        <span>关闭</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const searchBoxRef = ref(null)
const showShortcuts = ref(false)

// 监听全局快捷键
onMounted(() => {
  const handleKeydown = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault()
      searchBoxRef.value?.focus()
    }
  }
  document.addEventListener('keydown', handleKeydown)
  
  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown)
  })
})

const hideShortcuts = () => {
  setTimeout(() => {
    showShortcuts.value = false
  }, 200)
}
</script>
```

## 3. 响应式布局示例

### 移动端自适应
```vue
<template>
  <BasePanel type="with-sidebar" class="responsive-panel">
    <template #header>
      <PanelHeader
        :show-back="isMobile"
        title="文档管理"
        @back="handleBack"
        :searchable="!isMobile"
        @search="handleSearch"
      >
        <template #actions>
          <el-button v-if="!isMobile" type="primary">
            <el-icon><Upload /></el-icon>
            上传
          </el-button>
          <el-button v-else circle>
            <el-icon><Plus /></el-icon>
          </el-button>
        </template>
      </PanelHeader>
    </template>
    
    <template #sidebar>
      <!-- 移动端抽屉式侧边栏 -->
      <div v-if="!isMobile" class="desktop-sidebar">
        <FolderTree :folders="folders" />
      </div>
      
      <!-- 移动端：侧边栏内容移至抽屉 -->
      <el-drawer
        v-else
        v-model="showMobileDrawer"
        title="文件夹"
        direction="ltr"
        size="80%"
      >
        <FolderTree :folders="folders" />
      </el-drawer>
    </template>
    
    <template #toolbar>
      <div class="breadcrumb" v-if="currentPath">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
            v-for="item in breadcrumbItems"
            :key="item.path"
            :to="item.path"
          >
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="view-controls">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </template>
    
    <!-- 文件内容区 -->
    <FileList
      :files="files"
      :view-mode="viewMode"
      @file-click="handleFileClick"
    />
  </BasePanel>
</template>

<script setup>
import { useResponsive } from '@/composables/useResponsive'

const { isMobile, isTablet } = useResponsive()
const showMobileDrawer = ref(false)
const viewMode = ref('grid')

// 移动端显示抽屉按钮
watch(isMobile, (val) => {
  if (val) {
    // 移动端逻辑
  } else {
    // 桌面端逻辑
  }
})
</script>

<style lang="scss" scoped>
.responsive-panel {
  .breadcrumb {
    flex: 1;
  }
  
  .view-controls {
    @include mobile {
      margin-left: auto;
    }
  }
  
  .mobile-drawer-trigger {
    position: fixed;
    bottom: 80px;
    left: 20px;
    z-index: 100;
    display: none;
    
    @include mobile {
      display: block;
    }
  }
}
</style>
```

## 4. 实际应用示例：优化后的联系人面板

```vue
<template>
  <BasePanel type="with-sidebar" class="contacts-panel">
    <template #header>
      <PanelHeader
        title="通讯录"
        subtitle="共 {{ totalContacts }} 位联系人"
        :searchable="true"
        search-placeholder="搜索联系人、群组..."
        @search="handleSearch"
      >
        <template #actions>
          <el-button type="primary" @click="showAddContact = true">
            <el-icon><UserFilled /></el-icon>
            添加联系人
          </el-button>
          <el-button @click="showCreateGroup = true">
            <el-icon><User /></el-icon>
            创建群组
          </el-button>
        </template>
        <template #more>
          <el-dropdown-menu>
            <el-dropdown-item @click="importContacts">
              <el-icon><Upload /></el-icon>
              导入联系人
            </el-dropdown-item>
            <el-dropdown-item @click="exportContacts">
              <el-icon><Download /></el-icon>
              导出联系人
            </el-dropdown-item>
            <el-dropdown-item divided @click="showSettings = true">
              <el-icon><Setting /></el-icon>
              设置
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </PanelHeader>
    </template>
    
    <template #sidebar>
      <div class="sidebar-content">
        <!-- 导航标签 -->
        <div class="nav-tabs">
          <div
            v-for="tab in navTabs"
            :key="tab.key"
            :class="['nav-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            <el-icon><component :is="tab.icon" /></el-icon>
            <span>{{ tab.label }}</span>
            <el-badge v-if="tab.count" :value="tab.count" :max="99" />
          </div>
        </div>
        
        <!-- 动态内容区 -->
        <div class="tab-content">
          <!-- 好友列表 -->
          <ContactList
            v-if="activeTab === 'friends'"
            :contacts="filteredFriends"
            :loading="loading"
            @contact-click="handleContactClick"
          />
          
          <!-- 群组列表 -->
          <GroupList
            v-else-if="activeTab === 'groups'"
            :groups="filteredGroups"
            :loading="loading"
            @group-click="handleGroupClick"
          />
          
          <!-- 组织架构 -->
          <OrganizationTree
            v-else-if="activeTab === 'org'"
            :tree-data="orgTree"
            :loading="loading"
            @node-click="handleOrgNodeClick"
          />
        </div>
      </div>
    </template>
    
    <!-- 主内容区 - 联系人详情 -->
    <div v-if="selectedContact" class="contact-detail">
      <ContactDetailCard
        :contact="selectedContact"
        @edit="editContact"
        @delete="deleteContact"
        @start-chat="startChat"
      />
    </div>
    
    <!-- 空状态 -->
    <EmptyState
      v-else-if="!loading && !hasResults"
      icon="User"
      description="暂无联系人"
    >
      <el-button type="primary" @click="showAddContact = true">
        添加第一个联系人
      </el-button>
    </EmptyState>
  </BasePanel>
</template>

<script setup>
import { BasePanel } from '@/components/Base'
import { PanelHeader, SearchBox, EmptyState } from '@/components/Base'
import { ContactList, GroupList, OrganizationTree, ContactDetailCard } from '@/components/Contacts'

// 状态管理
const activeTab = ref('friends')
const searchQuery = ref('')
const loading = ref(false)
const selectedContact = ref(null)

// 导航标签
const navTabs = computed(() => [
  { key: 'friends', label: '好友', icon: 'User', count: friends.value.length },
  { key: 'groups', label: '群组', icon: 'UserFilled', count: groups.value.length },
  { key: 'org', label: '组织架构', icon: 'OfficeBuilding' }
])

// 处理搜索
const handleSearch = (query) => {
  searchQuery.value = query
  // 实现搜索逻辑
}

// 切换标签
const switchTab = (key) => {
  activeTab.value = key
  selectedContact.value = null
}

// 响应式处理
const { isMobile } = useResponsive()

// 移动端点击联系人后进入详情页
const handleContactClick = (contact) => {
  if (isMobile.value) {
    router.push(`/contacts/${contact.id}`)
  } else {
    selectedContact.value = contact
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/design-tokens' as *;

.contacts-panel {
  .sidebar-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .nav-tabs {
      flex-shrink: 0;
      padding: $spacing-sm;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .nav-tab {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        padding: $spacing-sm $spacing-md;
        cursor: pointer;
        border-radius: $border-radius-md;
        transition: all $transition-duration-fast $ease-in-out;
        
        &:hover {
          background: var(--el-fill-color-light);
        }
        
        &.active {
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }
        
        span {
          flex: 1;
        }
      }
    }
    
    .tab-content {
      flex: 1;
      overflow: hidden;
    }
  }
  
  .contact-detail {
    height: 100%;
    overflow-y: auto;
  }
}
</style>
```

## 5. 最佳实践建议

### 组件复用
1. 使用 `BasePanel` 作为所有面板的容器
2. 使用 `PanelHeader` 统一头部的交互和样式
3. 使用 `SearchBox` 替代分散的搜索实现

### 响应式设计
1. 使用 `useResponsive` composable 处理断点
2. 移动端优先设计，逐步增强
3. 保持触摸友好的交互区域（最小44px）

### 性能优化
1. 大列表使用虚拟滚动
2. 图片懒加载
3. 合理使用 `v-show` 和 `v-if`
4. 避免不必要的响应式数据

### 用户体验
1. 统一的加载状态（skeleton）
2. 统一的空状态设计
3. 一致的动画过渡
4. 键盘快捷键支持
