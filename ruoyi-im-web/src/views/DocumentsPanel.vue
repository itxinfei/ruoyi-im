<template>
  <div class="documents-panel">
    <!-- 左侧边栏 -->
    <aside class="docs-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">
          <span class="material-icons-outlined title-icon">cloud</span>
          云文档
        </h1>
      </div>

      <div class="sidebar-content">
        <div class="nav-section">
          <div
            v-for="nav in mainNavItems"
            :key="nav.id"
            class="nav-item"
            :class="{ active: activeNav === nav.id }"
            @click="activeNav = nav.id"
          >
            <span class="material-icons-outlined nav-icon">{{ nav.icon }}</span>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>

        <div class="nav-divider"></div>

        <div class="nav-section-label">存储位置</div>

        <div class="nav-section">
          <div
            v-for="nav in storageNavItems"
            :key="nav.id"
            class="nav-item"
            :class="{ active: activeNav === nav.id }"
            @click="activeNav = nav.id"
          >
            <span class="material-icons-outlined nav-icon">{{ nav.icon }}</span>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="storage-info">
          <div class="storage-header">
            <span class="storage-label">存储空间</span>
            <span class="storage-percent">85%</span>
          </div>
          <div class="storage-bar">
            <div class="storage-fill" style="width: 85%"></div>
          </div>
          <div class="storage-text">85GB / 100GB</div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="docs-main">
      <header class="docs-header">
        <div class="header-left">
          <h2 class="header-title">{{ currentViewTitle }}</h2>
          <div class="header-divider"></div>
          <div class="search-box">
            <span class="material-icons-outlined search-icon">search</span>
            <input
              v-model="searchQuery"
              class="search-input"
              placeholder="搜索文件"
              type="text"
            />
          </div>
        </div>
        <div class="header-right">
          <div class="view-toggle">
            <button
              class="toggle-btn"
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
            >
              <span class="material-icons-outlined">list</span>
            </button>
            <button
              class="toggle-btn"
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
            >
              <span class="material-icons-outlined">grid_view</span>
            </button>
          </div>
          <button class="new-btn">
            <span class="material-icons-outlined">add</span>
            新建
          </button>
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
                <th class="actions-col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="file in files"
                :key="file.id"
                class="file-row"
                @click="handleFileClick(file)"
              >
                <td class="name-col">
                  <div class="file-info">
                    <div class="file-icon" :class="file.iconClass">
                      <span class="material-icons-outlined">{{ file.icon }}</span>
                    </div>
                    <div>
                      <div class="file-name">{{ file.name }}</div>
                      <div class="file-meta">{{ file.meta }}</div>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="owner-info">
                    <div class="owner-avatar" :style="{ background: file.ownerColor }">
                      {{ file.owner.charAt(0) }}
                    </div>
                    <span>{{ file.owner }}</span>
                  </div>
                </td>
                <td>
                  <span class="file-time">{{ file.modifiedTime }}</span>
                </td>
                <td class="actions-col">
                  <button class="action-btn" @click.stop="handleFileMenu(file)">
                    <span class="material-icons-outlined">more_vert</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <span>共 {{ files.length }} 个文件</span>
          <div class="page-buttons">
            <button class="page-btn" disabled>上一页</button>
            <span class="page-divider">|</span>
            <button class="page-btn">下一页</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

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
  {
    id: 1,
    name: '2023年度项目资料',
    icon: 'folder',
    iconClass: 'icon-folder',
    meta: '3 个文件',
    owner: '我',
    ownerColor: '#3b82f6',
    modifiedTime: '2023-11-01 14:20'
  },
  {
    id: 2,
    name: 'RuoYi-IM 产品需求规格说明书 v2.0.docx',
    icon: 'description',
    iconClass: 'icon-doc',
    meta: '2.4 MB',
    owner: '李明',
    ownerColor: '#6366f1',
    modifiedTime: '2023-10-28 09:15'
  },
  {
    id: 3,
    name: 'Q4 研发部预算表.xlsx',
    icon: 'table_view',
    iconClass: 'icon-sheet',
    meta: '1.1 MB',
    owner: '张伟',
    ownerColor: '#ec4899',
    modifiedTime: '2023-10-27 16:45'
  },
  {
    id: 4,
    name: '系统架构设计图_Final.pdf',
    icon: 'picture_as_pdf',
    iconClass: 'icon-pdf',
    meta: '5.8 MB',
    owner: '王芳',
    ownerColor: '#f97316',
    modifiedTime: '2023-10-25 11:30'
  },
  {
    id: 5,
    name: '登录页UI_v3.png',
    icon: 'image',
    iconClass: 'icon-image',
    meta: '856 KB',
    owner: '刘洋',
    ownerColor: '#06b6d4',
    modifiedTime: '2023-10-24 15:10'
  }
])

const currentViewTitle = computed(() => {
  const item = [...mainNavItems.value, ...storageNavItems.value]
    .find(item => item.id === activeNav.value)
  return item?.label || '最近使用'
})

const handleFileClick = (file) => {
  ElMessage.info(`打开文件: ${file.name}`)
}

const handleFileMenu = (file) => {
  ElMessage.info(`文件操作: ${file.name}`)
}
</script>

<style scoped>
.documents-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

/* 左侧边栏 */
.docs-sidebar {
  width: 256px;
  background: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: #262626;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #1677ff;
}

.sidebar-content {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #595959;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f5f5f5;
}

.nav-item.active {
  background: #e6f7ff;
  color: #1677ff;
  font-weight: 500;
}

.nav-icon {
  font-size: 20px;
}

.nav-label {
  font-size: 14px;
}

.nav-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 16px 0;
}

.nav-section-label {
  padding: 0 12px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #8c8c8c;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.storage-info {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.storage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.storage-label {
  font-size: 12px;
  color: #8c8c8c;
}

.storage-percent {
  font-size: 12px;
  color: #1677ff;
  font-weight: 500;
}

.storage-bar {
  width: 100%;
  height: 6px;
  background: #e6e6e6;
  border-radius: 3px;
  overflow: hidden;
}

.storage-fill {
  height: 100%;
  background: #1677ff;
  border-radius: 3px;
}

.storage-text {
  margin-top: 8px;
  font-size: 10px;
  color: #bfbfbf;
  text-align: right;
}

/* 主内容区 */
.docs-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.docs-header {
  height: 64px;
  padding: 0 32px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.header-divider {
  width: 1px;
  height: 16px;
  background: #d9d9d9;
  margin: 0 8px;
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #bfbfbf;
  font-size: 18px;
}

.search-input {
  width: 256px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  padding: 8px 12px 8px 34px;
  font-size: 14px;
  color: #262626;
  outline: none;
  transition: all 0.2s;
}

.search-input:focus {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
}

.search-input::placeholder {
  color: #bfbfbf;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-toggle {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 2px;
}

.toggle-btn {
  padding: 6px;
  color: #bfbfbf;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn:hover {
  color: #595959;
}

.toggle-btn.active {
  background: #fff;
  color: #595959;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.new-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.new-btn:hover {
  background: #4096ff;
}

.new-btn:active {
  transform: scale(0.98);
}

.docs-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.files-table-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e6e6e6;
  overflow: hidden;
}

.files-table {
  width: 100%;
  border-collapse: collapse;
}

.files-table thead {
  background: #fafafa;
  border-bottom: 1px solid #e6e6e6;
}

.files-table th {
  padding: 16px 24px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #8c8c8c;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.files-table th.actions-col {
  text-align: right;
}

.files-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.files-table tbody tr:hover {
  background: #fafafa;
}

.files-table tbody tr:last-child {
  border-bottom: none;
}

.files-table td {
  padding: 16px 24px;
}

.name-col {
  width: 50%;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-folder { background: #fef3c7; color: #f59e0b; }
.icon-doc { background: #dbeafe; color: #2563eb; }
.icon-sheet { background: #d1fae5; color: #059669; }
.icon-pdf { background: #fee2e2; color: #ef4444; }
.icon-image { background: #f3e8ff; color: #a855f7; }

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.file-meta {
  font-size: 10px;
  color: #bfbfbf;
  margin-top: 2px;
}

.owner-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #595959;
}

.owner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 500;
}

.file-time {
  font-size: 14px;
  color: #8c8c8c;
}

.actions-col {
  text-align: right;
}

.action-btn {
  opacity: 0;
  background: none;
  border: none;
  color: #bfbfbf;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.file-row:hover .action-btn {
  opacity: 1;
}

.action-btn:hover {
  color: #1677ff;
}

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  background: none;
  border: none;
  color: #8c8c8c;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 12px;
  transition: color 0.2s;
}

.page-btn:hover:not([disabled]) {
  color: #1677ff;
}

.page-btn[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-divider {
  color: #d9d9d9;
}

/* 暗色模式 */
:deep(.dark) .docs-sidebar,
:deep(.dark) .docs-header,
:deep(.dark) .files-table-wrapper {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .sidebar-title,
:deep(.dark) .header-title,
:deep(.dark) .file-name {
  color: #f1f5f9;
}

:deep(.dark) .nav-item {
  color: #94a3b8;
}

:deep(.dark) .nav-item:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .nav-item.active {
  background: rgba(30, 58, 138, 0.3);
  color: #60a5fa;
}

:deep(.dark) .search-input {
  background: #0f172a;
  color: #cbd5e1;
}

:deep(.dark) .storage-info {
  background: rgba(30, 41, 59, 0.5);
}

:deep(.dark) .files-table thead {
  background: rgba(15, 23, 42, 0.5);
  border-color: #334155;
}

:deep(.dark) .files-table th {
  color: #64748b;
}

:deep(.dark) .files-table tbody tr:hover {
  background: rgba(51, 65, 85, 0.3);
}

:deep(.dark) .file-time {
  color: #64748b;
}

:deep(.dark) .owner-info {
  color: #94a3b8;
}
</style>
