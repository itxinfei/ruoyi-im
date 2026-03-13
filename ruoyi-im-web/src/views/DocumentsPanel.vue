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
        <!-- 空状态 -->
        <div v-if="!loading && files.length === 0" class="empty-state">
          <span class="material-icons-outlined empty-icon">folder_open</span>
          <p class="empty-text">{{ searchQuery ? '没有找到匹配的文档' : '暂无文档' }}</p>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <!-- 文件列表 -->
        <div v-else class="files-table-wrapper">
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
                    <div class="file-icon" :class="file.iconClass">
                      <span class="material-icons-outlined">{{ file.icon }}</span>
                    </div>
                    <div class="file-text">
                      <div class="file-name">
                        {{ file.name }}
                        <span v-if="file.isStarred" class="star-icon">
                          <span class="material-icons-outlined">star</span>
                        </span>
                      </div>
                      <div class="file-meta">{{ file.meta }}</div>
                    </div>
                  </div>
                </td>
                <td><span class="owner-name">{{ file.owner }}</span></td>
                <td><span class="file-time">{{ file.modifiedTime }}</span></td>
                <td class="actions-col">
                  <button class="action-btn" @click.stop="handleFileMenu(file)">
                    <span class="material-icons-outlined">more_horiz</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
    <!-- 文档编辑器弹窗 -->
    <DocumentEditorDialog
      v-model="showEditor"
      :document="selectedFile"
      @saved="handleFileSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocuments, searchDocuments, deleteDocument, toggleStar, restoreDocument, permanentlyDeleteDocument } from '@/api/im/document'
import { formatFileSize, formatTime } from '@/utils/format'
import DocumentEditorDialog from '@/components/Documents/DocumentEditorDialog.vue'

const activeNav = ref('recent')
const viewMode = ref('list')
const searchQuery = ref('')
const loading = ref(false)
const files = ref([])
const selectedFile = ref(null)
const showEditor = ref(false)

const mainNavItems = ref([
  { id: 'my', label: '我的文件', icon: 'folder' },
  { id: 'shared', label: '共享文件', icon: 'people' },
  { id: 'recent', label: '最近访问', icon: 'schedule' },
  { id: 'trash', label: '回收站', icon: 'delete_outline' }
])
const storageNavItems = ref([])

const currentViewTitle = computed(() => {
  const item = [...mainNavItems.value, ...storageNavItems.value].find(i => i.id === activeNav.value)
  return item?.label || '最近使用'
})

// 映射导航到API的type参数
const getApiType = () => {
  const map = {
    'my': 'my',
    'shared': 'shared',
    'recent': 'recent',
    'trash': 'trash'
  }
  return map[activeNav.value] || 'my'
}

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await getDocuments(getApiType())
    if (res.code === 200) {
      files.value = res.data.map(doc => ({
        id: doc.id,
        name: doc.title,
        icon: getFileIcon(doc.type),
        iconClass: getIconClass(doc.type),
        meta: formatFileSize(doc.content?.length || 0),
        owner: doc.creatorName || '我',
        modifiedTime: formatTime(doc.updateTime),
        isStarred: doc.isStarred,
        documentType: doc.type
      }))
    }
  } catch (error) {
    console.error('加载文档失败:', error)
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

// 搜索文档
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    loadDocuments()
    return
  }
  loading.value = true
  try {
    const res = await searchDocuments(searchQuery.value)
    if (res.code === 200) {
      files.value = res.data.map(doc => ({
        id: doc.id,
        name: doc.title,
        icon: getFileIcon(doc.type),
        iconClass: getIconClass(doc.type),
        meta: formatFileSize(doc.content?.length || 0),
        owner: doc.creatorName || '我',
        modifiedTime: formatTime(doc.updateTime),
        isStarred: doc.isStarred,
        documentType: doc.type
      }))
    }
  } catch (error) {
    console.error('搜索文档失败:', error)
    ElMessage.error('搜索文档失败')
  } finally {
    loading.value = false
  }
}

// 获取文件图标
const getFileIcon = (type) => {
  const icons = {
    'TEXT': 'description',
    'FILE': 'description',
    'IMAGE': 'image',
    'VIDEO': 'video_library',
    'VOICE': 'mic',
    'FOLDER': 'folder'
  }
  return icons[type] || 'description'
}

// 获取图标样式
const getIconClass = (type) => {
  const classes = {
    'TEXT': 'icon-doc',
    'FILE': 'icon-doc',
    'IMAGE': 'icon-image',
    'VIDEO': 'icon-video',
    'VOICE': 'icon-voice',
    'FOLDER': 'icon-folder'
  }
  return classes[type] || 'icon-doc'
}

// 处理文件点击
const handleFileClick = (file) => {
  selectedFile.value = file
  showEditor.value = true
}

// 处理菜单关闭
const handleEditorClose = () => {
  showEditor.value = false
  selectedFile.value = null
}

// 处理文件保存
const handleFileSaved = () => {
  loadDocuments()
}

// 处理新建命令
const handleNewCommand = (command) => {
  if (command === 'folder') {
    ElMessage.info('创建文件夹功能开发中')
  } else if (command === 'upload') {
    ElMessage.info('上传文件功能开发中')
  }
}

// 处理文件操作菜单
const handleFileMenu = async (file, event) => {
  // 阻止事件冒泡
  if (event) {
    event.stopPropagation()
  }

  const isTrash = activeNav.value === 'trash'

  // 定义菜单项
  const menuItems = [
    { label: '查看', value: 'view', icon: 'visibility' },
    { label: '编辑', value: 'edit', icon: 'edit', hide: file.documentType !== 'TEXT' || isTrash },
    { label: '分享', value: 'share', icon: 'share', hide: isTrash },
    { label: file.isStarred ? '取消收藏' : '收藏', value: 'star', icon: file.isStarred ? 'star' : 'star_border', hide: isTrash },
    { label: '删除', value: 'delete', icon: 'delete', hide: isTrash },
    { label: '恢复', value: 'restore', icon: 'restore', hide: !isTrash },
    { label: '永久删除', value: 'permanent', icon: 'delete_forever', hide: !isTrash }
  ].filter(item => !item.hide)

  // 使用 Element Plus 的 ElMessageBox 或自定义菜单
  // 这里简化处理，使用 ElMessageBox.prompt 风格的菜单
  const { value: action } = await ElMessageBox.prompt(
    '',
    `操作: ${file.name}`,
    {
      confirmButtonText: '关闭',
      showCancelButton: false,
      showInput: false,
      distinguishCancelAndClose: true,
      message: menuItems.map((item, index) => `${index + 1}. ${item.label}`).join('\n'),
      beforeClose: (action, instance, done) => {
        done()
      }
    }
  ).catch(() => {})
}

// 切换收藏状态
const handleToggleStar = async (file) => {
  try {
    await toggleStar(file.id, !file.isStarred)
    file.isStarred = !file.isStarred
    ElMessage.success(file.isStarred ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除文档
const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${file.name}"吗？`, '确认删除', {
      type: 'warning'
    })
    await deleteDocument(file.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 恢复文档
const handleRestore = async (file) => {
  try {
    await restoreDocument(file.id)
    ElMessage.success('恢复成功')
    loadDocuments()
  } catch (error) {
    console.error('恢复失败:', error)
    ElMessage.error('恢复失败')
  }
}

// 永久删除
const handlePermanentDelete = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要永久删除"${file.name}"吗？此操作无法撤销。`, '永久删除', {
      type: 'error'
    })
    await permanentlyDeleteDocument(file.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 监听导航变化
watch(activeNav, () => {
  loadDocuments()
})

// 监听搜索输入
let searchTimer
watch(searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300)
})

// 初始化加载
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped lang="scss">
.documents-panel { display: flex; height: 100%; flex: 1; background: var(--dt-bg-card); }
.docs-sidebar { width: 200px; border-right: 1px solid var(--dt-border-light); display: flex; flex-direction: column; flex-shrink: 0; background: var(--dt-bg-card); }
.sidebar-header { padding: var(--dt-spacing-lg) var(--dt-spacing-md); border-bottom: 1px solid var(--dt-border-lighter);
  .sidebar-title { font-size: 16px; font-weight: 600; color: var(--dt-text-primary); margin: 0; }
}
.sidebar-content { flex: 1; padding: var(--dt-spacing-md) 0; overflow-y: auto;
  .nav-item { display: flex; align-items: center; gap: var(--dt-spacing-md); padding: var(--dt-spacing-md) var(--dt-spacing-lg); margin: 2px 0; border-radius: var(--dt-radius-sm); cursor: pointer; color: var(--dt-text-primary); transition: all var(--dt-transition-fast); position: relative;
    &:hover { background: var(--dt-bg-session-hover); }
    &.active { background: var(--dt-brand-lighter); color: var(--dt-brand-color); font-weight: 500;
      &::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px; background: var(--dt-brand-color); }
    }
    .nav-icon { font-size: 18px; flex-shrink: 0; }
    .nav-label { font-size: 14px; flex: 1; }
  }
  .nav-divider { height: 1px; background: var(--dt-border-light); margin: var(--dt-spacing-md) var(--dt-spacing-md); }
  .nav-section-label { padding: var(--dt-spacing-sm) var(--dt-spacing-lg); font-size: 11px; color: var(--dt-text-tertiary); text-transform: uppercase; font-weight: 500; }
}
.sidebar-footer { padding: 16px; border-top: 1px solid var(--dt-border-light);
  .storage-info { background: var(--dt-bg-body); padding: 12px; border-radius: 8px;
    .storage-header { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; color: var(--dt-text-tertiary); }
    .storage-bar { height: 4px; background: var(--dt-border-color); border-radius: 2px; overflow: hidden; .storage-fill { height: 100%; background: var(--dt-brand-color); } }
    .storage-text { font-size: 10px; color: var(--dt-text-quaternary); margin-top: 6px; text-align: right; }
  }
}
.docs-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.docs-header { height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light); display: flex; align-items: center; justify-content: space-between;
  .header-left { display: flex; align-items: center; gap: 12px; .header-title { font-size: 16px; font-weight: 600; margin: 0; }
    .header-divider { width: 1px; height: 14px; background: var(--dt-border-light); }
    .search-box { position: relative; .search-icon { position: absolute; left: 8px; top: 50%; transform: translateY(-50%); font-size: 16px; color: var(--dt-text-tertiary); }
      .search-input { width: 200px; height: 32px; background: var(--dt-bg-body); border: none; border-radius: 6px; padding: 0 32px; font-size: 13px; outline: none; &:focus { background: var(--dt-bg-card); box-shadow: 0 0 0 2px var(--dt-brand-lighter); } }
    }
  }
  .header-right { display: flex; align-items: center; gap: 12px;
    .view-toggle { display: flex; background: var(--dt-bg-body); padding: 2px; border-radius: 4px;
      .toggle-btn { padding: 4px; border: none; background: transparent; cursor: pointer; color: var(--dt-text-tertiary); &.active { background: var(--dt-bg-card); color: var(--dt-brand-color); border-radius: 3px; box-shadow: var(--dt-shadow-1); } }
    }
    .new-btn { padding: 6px 16px; background: var(--dt-brand-color); color: var(--dt-text-primary); border: none; border-radius: 4px; font-size: 13px; font-weight: 500; cursor: pointer; }
  }
}
.docs-content { flex: 1; padding: 20px; overflow-y: auto; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; color: var(--dt-text-tertiary);
  .empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; color: var(--dt-border-color); }
  .empty-text { margin: 0; font-size: 14px; }
}
.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; color: var(--dt-text-tertiary);
  .is-loading { font-size: 32px; margin-bottom: 16px; }
  p { margin: 0; font-size: 14px; }
}
.files-table { width: 100%; border-collapse: collapse;
  th { text-align: left; padding: 12px 16px; font-size: 12px; color: var(--dt-text-tertiary); border-bottom: 1px solid var(--dt-border-light); font-weight: 400; }
  td { padding: 10px 16px; border-bottom: 1px solid var(--dt-border-light); vertical-align: middle; }
  .file-row { cursor: pointer; &:hover { background: var(--dt-bg-body); .action-btn { opacity: 1; } } }
  .file-info { display: flex; align-items: center; gap: 12px;
    .file-icon { width: 32px; height: 32px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 18px;
      &.icon-folder { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
      &.icon-doc { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.icon-sheet { background: var(--dt-success-bg); color: var(--dt-success-color); }
      &.icon-image { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
      &.icon-video { background: var(--dt-error-bg); color: var(--dt-error-color); }
      &.icon-voice { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    }
    .file-name { font-size: 13px; font-weight: 500; color: var(--dt-text-primary); display: flex; align-items: center; gap: 4px;
      .star-icon { color: var(--dt-warning-color); font-size: 14px; }
    }
    .file-meta { font-size: 11px; color: var(--dt-text-tertiary); margin-top: 1px; }
  }
  .owner-name, .file-time { font-size: 13px; color: var(--dt-text-secondary); }
  .actions-col { text-align: right; .action-btn { opacity: 0; background: transparent; border: none; color: var(--dt-text-tertiary); cursor: pointer; } }
}
</style>
