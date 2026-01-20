<template>
  <div class="documents-panel">
    <div class="docs-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">文档</h2>
        <el-button type="primary" class="upload-btn">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </div>

      <div class="sidebar-body">
        <div class="nav-section">
          <div
            v-for="nav in navItems"
            :key="nav.id"
            class="nav-item"
            :class="{ active: activeNav === nav.id }"
            @click="activeNav = nav.id"
          >
            <el-icon><component :is="nav.icon" /></el-icon>
            <span>{{ nav.label }}</span>
          </div>
        </div>

        <div class="folders-section">
          <div class="folders-header">
            <span class="folders-title">文件夹</span>
            <el-button text class="folder-add-btn">
              <el-icon><FolderAdd /></el-icon>
            </el-button>
          </div>
          <div class="folders-list">
            <div
              v-for="folder in folders"
              :key="folder.id"
              class="folder-item"
              @click="handleFolderClick(folder)"
            >
              <div class="folder-icon" :class="folder.colorClass">
                <el-icon><Folder /></el-icon>
              </div>
              <div class="folder-info">
                <p class="folder-name">{{ folder.name }}</p>
                <p class="folder-count">{{ folder.count }}个文件</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="docs-main">
      <div class="docs-header">
        <div class="header-left">
          <h3 class="header-title">我的文档</h3>
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <el-input
              v-model="searchQuery"
              placeholder="搜索文档"
              class="search-input"
              clearable
            />
          </div>
        </div>
        <div class="header-right">
          <el-button text class="more-btn">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="view-tabs">
        <div
          class="tab-item"
          :class="{ active: viewMode === 'list' }"
          @click="viewMode = 'list'"
        >
          列表视图
        </div>
        <div
          class="tab-item"
          :class="{ active: viewMode === 'grid' }"
          @click="viewMode = 'grid'"
        >
          网格视图
        </div>
      </div>

      <div class="docs-content">
        <div class="docs-table">
          <table class="table">
            <thead>
              <tr>
                <th>文件名</th>
                <th>大小</th>
                <th>更新时间</th>
                <th>更新人</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="doc in documents"
                :key="doc.id"
                class="doc-row"
                @click="handleDocClick(doc)"
              >
                <td>
                  <div class="doc-name-cell">
                    <div class="doc-icon" :class="getDocIconClass(doc.type)">
                      <el-icon><component :is="getDocIcon(doc.type)" /></el-icon>
                    </div>
                    <div class="doc-name-row">
                      <span class="doc-name">{{ doc.name }}</span>
                      <el-icon v-if="doc.isStarred" class="doc-star"><Star /></el-icon>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="doc-size">{{ doc.size }}</span>
                </td>
                <td>
                  <span class="doc-time">{{ doc.updatedAt }}</span>
                </td>
                <td>
                  <div class="doc-updater">
                    <div class="updater-avatar">{{ doc.updatedBy[0] }}</div>
                    <span class="updater-name">{{ doc.updatedBy }}</span>
                  </div>
                </td>
                <td>
                  <div class="doc-actions" @click.stop>
                    <el-button text class="action-btn">
                      <el-icon><Star /></el-icon>
                    </el-button>
                    <el-button text class="action-btn">
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  Clock,
  Document,
  User,
  Star,
  Upload,
  FolderAdd,
  Folder,
  Search,
  MoreFilled,
  Picture,
  Files
} from '@element-plus/icons-vue'

const activeNav = ref('my-docs')
const viewMode = ref('list')
const searchQuery = ref('')

const navItems = ref([
  { id: 'recent', label: '最近使用', icon: Clock },
  { id: 'my-docs', label: '我的文档', icon: Document },
  { id: 'shared', label: '共享文档', icon: User },
  { id: 'starred', label: '星标文档', icon: Star }
])

const folders = ref([
  { id: '1', name: '产品文档', count: 25, colorClass: 'folder-blue' },
  { id: '2', name: '设计资源', count: 48, colorClass: 'folder-purple' },
  { id: '3', name: '开发文档', count: 32, colorClass: 'folder-green' },
  { id: '4', name: '项目资料', count: 18, colorClass: 'folder-orange' }
])

const documents = ref([
  {
    id: '1',
    name: '产品需求文档V2.0.docx',
    type: 'doc',
    size: '2.3 MB',
    updatedAt: '2小时前',
    updatedBy: '张伟',
    isStarred: true
  },
  {
    id: '2',
    name: 'UI设计规范.pdf',
    type: 'doc',
    size: '5.7 MB',
    updatedAt: '5小时前',
    updatedBy: '李明',
    isStarred: true
  },
  {
    id: '3',
    name: '项目进度表.xlsx',
    type: 'file',
    size: '1.2 MB',
    updatedAt: '昨天',
    updatedBy: '王芳',
    isStarred: false
  },
  {
    id: '4',
    name: '产品原型图.fig',
    type: 'image',
    size: '8.4 MB',
    updatedAt: '昨天',
    updatedBy: '李明',
    isStarred: false
  },
  {
    id: '5',
    name: 'Q1季度总结报告.pptx',
    type: 'doc',
    size: '12.5 MB',
    updatedAt: '2天前',
    updatedBy: '刘洋',
    isStarred: false
  },
  {
    id: '6',
    name: '技术架构图.png',
    type: 'image',
    size: '3.1 MB',
    updatedAt: '3天前',
    updatedBy: '刘洋',
    isStarred: false
  },
  {
    id: '7',
    name: '用户调研报告.docx',
    type: 'doc',
    size: '4.6 MB',
    updatedAt: '3天前',
    updatedBy: '陈晨',
    isStarred: false
  },
  {
    id: '8',
    name: '竞品分析.pdf',
    type: 'doc',
    size: '6.8 MB',
    updatedAt: '星期一',
    updatedBy: '张伟',
    isStarred: false
  }
])

const getDocIcon = (type) => {
  switch (type) {
    case 'doc':
      return Document
    case 'image':
      return Picture
    default:
      return Files
  }
}

const getDocIconClass = (type) => {
  switch (type) {
    case 'doc':
      return 'icon-blue'
    case 'image':
      return 'icon-purple'
    default:
      return 'icon-gray'
  }
}

const handleFolderClick = (folder) => {
  console.log('Folder clicked:', folder)
}

const handleDocClick = (doc) => {
  console.log('Doc clicked:', doc)
}
</script>

<style scoped>
.documents-panel {
  flex: 1;
  display: flex;
  background: #fff;
  overflow: hidden;
}

.docs-sidebar {
  width: 256px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  .sidebar-title {
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 12px 0;
  }

  .upload-btn {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
}

.nav-section {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-size: 14px;
  color: #595959;

  &:hover {
    background-color: #f5f5f5;
  }

  &.active {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  .el-icon {
    font-size: 16px;
  }
}

.folders-section {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.folders-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .folders-title {
    font-size: 14px;
    color: #595959;
  }

  .folder-add-btn {
    padding: 4px;

    .el-icon {
      font-size: 16px;
      color: #8c8c8c;
    }
  }
}

.folders-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  .folder-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 16px;
      color: #fff;
    }

    &.folder-blue {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
    }

    &.folder-purple {
      background: linear-gradient(135deg, #a855f7, #9333ea);
    }

    &.folder-green {
      background: linear-gradient(135deg, #22c55e, #16a34a);
    }

    &.folder-orange {
      background: linear-gradient(135deg, #f97316, #ea580c);
    }
  }

  .folder-info {
    flex: 1;
    min-width: 0;
  }

  .folder-name {
    font-size: 14px;
    color: #262626;
    margin: 0 0 2px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .folder-count {
    font-size: 12px;
    color: #8c8c8c;
    margin: 0;
  }
}

.docs-main {
  flex: 1;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.docs-header {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.search-box {
  position: relative;
  width: 256px;

  .search-icon {
    position: absolute;
    left: 10px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 16px;
    color: #8c8c8c;
  }

  .search-input {
    width: 100%;

    :deep(.el-input__wrapper) {
      background-color: #f5f5f5;
      border: none;
      padding-left: 36px;
      box-shadow: none;
    }
  }
}

.header-right {
  .more-btn {
    .el-icon {
      font-size: 16px;
    }
  }
}

.view-tabs {
  background: #fff;
  padding: 0 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 4px;
}

.tab-item {
  padding: 12px 16px;
  font-size: 14px;
  color: #595959;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    color: #1890ff;
  }

  &.active {
    color: #1890ff;
    border-bottom-color: #1890ff;
  }
}

.docs-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.docs-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.table {
  width: 100%;
  border-collapse: collapse;

  thead {
    background-color: #f5f5f5;

    th {
      text-align: left;
      padding: 12px 16px;
      font-size: 12px;
      font-weight: 500;
      color: #595959;
      border-bottom: 1px solid #f0f0f0;
    }
  }

  tbody {
    tr {
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: background-color 0.2s ease;

      &:hover {
        background-color: #f5f5f5;
      }

      &:last-child {
        border-bottom: none;
      }

      td {
        padding: 12px 16px;
      }
    }
  }
}

.doc-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }

  &.icon-blue {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.icon-purple {
    background-color: #f9f0ff;
    color: #722ed1;
  }

  &.icon-gray {
    background-color: #f5f5f5;
    color: #8c8c8c;
  }
}

.doc-name-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.doc-name {
  font-size: 14px;
  color: #262626;
}

.doc-star {
  font-size: 16px;
  color: #faad14;
}

.doc-size,
.doc-time {
  font-size: 14px;
  color: #595959;
}

.doc-updater {
  display: flex;
  align-items: center;
  gap: 8px;
}

.updater-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.updater-name {
  font-size: 14px;
  color: #595959;
}

.doc-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;

  .action-btn {
    .el-icon {
      font-size: 16px;
      color: #8c8c8c;
    }
  }
}
</style>
