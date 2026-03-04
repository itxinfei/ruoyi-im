<template>
  <div class="documents-panel">
    <!-- 左侧边栏 (240px 黄金宽度) -->
    <aside class="docs-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">云文档</h1>
      </div>

      <div class="sidebar-content">
        <div class="nav-section">
          <div v-for="nav in mainNavItems" :key="nav.id" class="nav-item" :class="{ active: activeNav === nav.id }" @click="activeNav = nav.id">
            <span class="material-icons-outlined nav-icon">{{ nav.icon }}</span>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
        <div class="nav-divider"></div>
        <div class="nav-section-label">存储位置</div>
        <div class="nav-section">
          <div v-for="nav in storageNavItems" :key="nav.id" class="nav-item" :class="{ active: activeNav === nav.id }" @click="activeNav = nav.id">
            <span class="material-icons-outlined nav-icon">{{ nav.icon }}</span>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="storage-info">
          <div class="storage-header"><span class="storage-label">存储空间</span><span class="storage-percent">85%</span></div>
          <div class="storage-bar"><div class="storage-fill" style="width: 85%"></div></div>
          <div class="storage-text">85GB / 100GB</div>
        </div>
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="docs-main">
      <header class="docs-header">
        <div class="header-left">
          <h2 class="header-title">{{ currentViewTitle }}</h2>
          <div class="header-divider"></div>
          <div class="search-box">
            <span class="material-icons-outlined search-icon">search</span>
            <input v-model="searchQuery" class="search-input" placeholder="搜索文件" type="text" />
          </div>
        </div>
        <div class="header-right">
          <div class="view-toggle">
            <button class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'"><span class="material-icons-outlined">list</span></button>
            <button class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'"><span class="material-icons-outlined">grid_view</span></button>
          </div>
          <el-dropdown trigger="click" @command="handleNewCommand">
            <button class="new-btn"><span class="material-icons-outlined">add</span>新建</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="folder"><span class="material-icons-outlined">folder</span> 新建文件夹</el-dropdown-item>
                <el-dropdown-item command="upload"><span class="material-icons-outlined">upload</span> 上传文件</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div class="docs-content">
        <div class="files-table-wrapper">
          <table class="files-table">
            <thead>
              <tr>
                <th class="name-col">名称</th>
                <th>所有者</th>
                <th>修改时间</th>
                <th class="actions-col"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="file in files" :key="file.id" class="file-row" @click="handleFileClick(file)">
                <td class="name-col">
                  <div class="file-info">
                    <div class="file-icon" :class="file.iconClass"><span class="material-icons-outlined">{{ file.icon }}</span></div>
                    <div class="file-text">
                      <div class="file-name">{{ file.name }}</div>
                      <div class="file-meta">{{ file.meta }}</div>
                    </div>
                  </div>
                </td>
                <td><span class="owner-name">{{ file.owner }}</span></td>
                <td><span class="file-time">{{ file.modifiedTime }}</span></td>
                <td class="actions-col">
                  <button class="action-btn" @click.stop="handleFileMenu(file)"><span class="material-icons-outlined">more_horiz</span></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeNav = ref('recent')
const viewMode = ref('list')
const searchQuery = ref('')
const mainNavItems = ref([
  { id: 'recent', label: '最近使用', icon: 'schedule' },
  { id: 'my-created', label: '我创建的', icon: 'folder_special' },
  { id: 'shared', label: '分享给我', icon: 'share' },
  { id: 'starred', label: '收藏夹', icon: 'star_border' }
])
const storageNavItems = ref([
  { id: 'enterprise', label: '企业空间', icon: 'corporate_fare' },
  { id: 'personal', label: '个人空间', icon: 'person_outline' },
  { id: 'trash', label: '回收站', icon: 'delete_outline' }
])
const files = ref([
  { id: 1, name: '年度汇报PPT', icon: 'folder', iconClass: 'icon-folder', meta: '3 个文件', owner: '我', modifiedTime: '11-01 14:20' },
  { id: 2, name: '项目需求书.docx', icon: 'description', iconClass: 'icon-doc', meta: '2.4 MB', owner: '李明', modifiedTime: '10-28 09:15' },
  { id: 3, name: '财务预算.xlsx', icon: 'table_view', iconClass: 'icon-sheet', meta: '1.1 MB', owner: '张伟', modifiedTime: '10-27 16:45' }
])
const currentViewTitle = computed(() => {
  const item = [...mainNavItems.value, ...storageNavItems.value].find(i => i.id === activeNav.value)
  return item?.label || '最近使用'
})
const handleFileClick = (f) => ElMessage.info(`打开: ${f.name}`)
const handleNewCommand = (c) => ElMessage.info(`执行: ${c}`)
const handleFileMenu = (f) => ElMessage.info(`操作: ${f.name}`)
</script>

<style scoped lang="scss">
.documents-panel { display: flex; height: 100%; flex: 1; background: #fff; }
.docs-sidebar { width: 240px; border-right: 1px solid #f2f3f5; display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: 16px 20px; .sidebar-title { font-size: 16px; font-weight: 600; color: #1f2329; } }
.sidebar-content { flex: 1; padding: 8px 12px;
  .nav-item { display: flex; align-items: center; gap: 10px; padding: 8px 12px; margin: 2px 0; border-radius: 6px; cursor: pointer; color: #1f2329; transition: all 0.2s;
    &:hover { background: #f5f6f7; }
    &.active { background: #e8f4ff; color: #1677ff; font-weight: 600; }
    .nav-icon { font-size: 18px; }
    .nav-label { font-size: 13px; }
  }
  .nav-divider { height: 1px; background: #f2f3f5; margin: 12px 8px; }
  .nav-section-label { padding: 8px 12px; font-size: 11px; color: #8f959e; text-transform: uppercase; }
}
.sidebar-footer { padding: 16px; border-top: 1px solid #f2f3f5;
  .storage-info { background: #f8fafc; padding: 12px; border-radius: 8px;
    .storage-header { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; color: #8f959e; }
    .storage-bar { height: 4px; background: #e5e6eb; border-radius: 2px; overflow: hidden; .storage-fill { height: 100%; background: #1677ff; } }
    .storage-text { font-size: 10px; color: #c9cdd4; margin-top: 6px; text-align: right; }
  }
}
.docs-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.docs-header { height: 56px; padding: 0 24px; border-bottom: 1px solid #f2f3f5; display: flex; align-items: center; justify-content: space-between;
  .header-left { display: flex; align-items: center; gap: 12px; .header-title { font-size: 16px; font-weight: 600; margin: 0; }
    .header-divider { width: 1px; height: 14px; background: #f2f3f5; }
    .search-box { position: relative; .search-icon { position: absolute; left: 8px; top: 50%; transform: translateY(-50%); font-size: 16px; color: #8f959e; }
      .search-input { width: 200px; height: 32px; background: #f3f3f3; border: none; border-radius: 6px; padding: 0 32px; font-size: 13px; outline: none; &:focus { background: #fff; box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1); } }
    }
  }
  .header-right { display: flex; align-items: center; gap: 12px;
    .view-toggle { display: flex; background: #f3f3f3; padding: 2px; border-radius: 4px;
      .toggle-btn { padding: 4px; border: none; background: transparent; cursor: pointer; color: #8f959e; &.active { background: #fff; color: #1677ff; border-radius: 3px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); } }
    }
    .new-btn { padding: 6px 16px; background: #1677ff; color: #fff; border: none; border-radius: 4px; font-size: 13px; font-weight: 500; cursor: pointer; }
  }
}
.docs-content { flex: 1; padding: 20px; overflow-y: auto; }
.files-table { width: 100%; border-collapse: collapse; 
  th { text-align: left; padding: 12px 16px; font-size: 12px; color: #8f959e; border-bottom: 1px solid #f2f3f5; font-weight: 400; }
  td { padding: 10px 16px; border-bottom: 1px solid #f2f3f5; vertical-align: middle; }
  .file-row { cursor: pointer; &:hover { background: #f8fafc; .action-btn { opacity: 1; } } }
  .file-info { display: flex; align-items: center; gap: 12px;
    .file-icon { width: 32px; height: 32px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 18px;
      &.icon-folder { background: #fff7e6; color: #faad14; }
      &.icon-doc { background: #e6f4ff; color: #1677ff; }
      &.icon-sheet { background: #f6ffed; color: #52c41a; }
    }
    .file-name { font-size: 13px; font-weight: 500; color: #1f2329; }
    .file-meta { font-size: 11px; color: #8f959e; margin-top: 1px; }
  }
  .owner-name, .file-time { font-size: 13px; color: #646a73; }
  .actions-col { text-align: right; .action-btn { opacity: 0; background: transparent; border: none; color: #8f959e; cursor: pointer; } }
}
</style>
