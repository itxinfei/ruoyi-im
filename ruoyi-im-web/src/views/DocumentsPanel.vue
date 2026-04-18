<template>
  <div class="documents-panel">
    <!-- 左侧边栏 (240px 黄金宽度) -->
    <aside class="docs-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">
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
            <el-icon class="nav-icon">
              <component :is="getNavIcon(nav.icon)" />
            </el-icon>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
        <div class="nav-divider" />
        <div class="nav-section-label">
          存储位置
        </div>
        <div class="nav-section">
          <div
            v-for="nav in storageNavItems"
            :key="nav.id"
            class="nav-item"
            :class="{ active: activeNav === nav.id }"
            @click="activeNav = nav.id"
          >
            <el-icon class="nav-icon">
              <component :is="getNavIcon(nav.icon)" />
            </el-icon>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="storage-info">
          <div class="storage-header">
            <span class="storage-label">存储空间</span><span class="storage-percent">{{ quota.usageRate }}%</span>
          </div>
          <div class="storage-bar">
            <div class="storage-fill" :style="{ width: Math.min(quota.usageRate, 100) + '%' }" />
          </div>
          <div class="storage-text">
            {{ quota.usedCapacityFormat }} / {{ quota.totalCapacityFormat }}
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="docs-main">
      <header class="docs-header">
        <div class="header-left">
          <!-- 返回上级按钮（仅我的文件显示） -->
          <button v-if="isInSubFolder && activeNav === 'my'" class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <!-- 面包屑导航（仅我的文件显示） -->
          <div v-if="activeNav === 'my'" class="breadcrumb-nav">
            <span class="breadcrumb-item" @click="navigateToFolder(null)">全部文件</span>
            <template v-for="(folder, idx) in folderPath" :key="folder.id">
              <span class="breadcrumb-sep">/</span>
              <span
                class="breadcrumb-item"
                :class="{ active: idx === folderPath.length - 1 }"
                @click="idx < folderPath.length - 1 && navigateToFolder(folder)"
              >{{ folder.name }}</span>
            </template>
          </div>
          <h2 v-else class="header-title">
            {{ currentViewTitle }}
          </h2>
          <div class="header-divider" />
          <div class="search-box">
            <el-icon class="search-icon">
              <Search />
            </el-icon>
            <input
              v-model="searchQuery"
              class="search-input"
              placeholder="搜索文件"
              type="text"
            >
          </div>
        </div>
        <div class="header-right">
          <div class="view-toggle">
            <button class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </button>
            <button class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </button>
          </div>
          <el-dropdown trigger="click" @command="handleNewCommand">
            <button class="new-btn">
              <el-icon><Plus /></el-icon>新建
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="folder">
                  <el-icon><Folder /></el-icon> 新建文件夹
                </el-dropdown-item>
                <el-dropdown-item command="upload">
                  <el-icon><Upload /></el-icon> 上传文件
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div class="docs-content" :class="{ 'is-grid': viewMode === 'grid' }">
        <!-- 加载与空状态 -->
        <div v-if="loading" class="loading-view"><el-icon class="is-loading"><Loading /></el-icon><p>正在获取文件...</p></div>
        <div v-else-if="files.length === 0" class="empty-view">
          <div class="empty-box">
            <el-icon class="empty-icon"><FolderOpened /></el-icon>
            <p>{{ searchQuery ? '未找到相关文件' : '文件夹空空如也' }}</p>
            <el-button v-if="!searchQuery" type="primary" size="small" @click="handleNewCommand('upload')">立即上传</el-button>
          </div>
        </div>

        <!-- 列表视图 (对齐钉钉 8.2) -->
        <div v-else-if="viewMode === 'list'" class="list-view">
          <div class="list-header">
            <div class="col-name">名称</div>
            <div class="col-owner">所有者</div>
            <div class="col-time">修改时间</div>
            <div class="col-actions"></div>
          </div>
          <div class="list-body">
            <div v-for="file in files" :key="file.id" class="list-row" @click="handleFileClick(file)" @dblclick="handleDoubleClick(file)">
              <div class="col-name">
                <div class="file-icon-wrapper" :class="file.iconClass">
                  <el-icon><component :is="getFileIconEl(file.icon)" /></el-icon>
                </div>
                <div class="file-info-main">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ file.meta }}</span>
                </div>
              </div>
              <div class="col-owner">{{ file.owner }}</div>
              <div class="col-time">{{ file.modifiedTime }}</div>
              <div class="col-actions">
                <el-dropdown trigger="click" @command="(cmd) => handleDropdownCommand(cmd, file)">
                  <el-icon class="more-btn" @click.stop><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view" :icon="View">查看</el-dropdown-item>
                      <el-dropdown-item command="rename" :icon="EditPen">重命名</el-dropdown-item>
                      <el-dropdown-item command="delete" :icon="Delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 网格视图 (对齐钉钉 8.2) -->
        <div v-else class="grid-view">
          <div v-for="file in files" :key="file.id" class="grid-item" @click="handleFileClick(file)" @dblclick="handleDoubleClick(file)">
            <div class="grid-thumb" :class="file.iconClass">
              <el-icon v-if="file.documentType !== 'IMAGE'"><component :is="getFileIconEl(file.icon)" /></el-icon>
              <el-image v-else :src="file.url" fit="cover" lazy />
            </div>
            <div class="grid-info">
              <span class="grid-name">{{ file.name }}</span>
              <span class="grid-sub">{{ file.meta }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 传输管理中心 (对齐钉钉右下角浮窗) -->
      <TransferManager ref="transferManagerRef" />
    </main>
    <!-- 文档编辑器弹窗 -->
    <DocumentEditorDialog
      v-model="showEditor"
      :document="selectedFile"
      @saved="handleFileSaved"
    />
    <!-- 分享文档弹窗 -->
    <el-dialog
      v-model="showShareDialog"
      title="分享文档"
      width="440px"
      append-to-body
    >
      <el-form label-position="top">
        <el-form-item label="分享给用户">
          <el-input v-model="shareForm.targetUserId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="权限">
          <el-select v-model="shareForm.permission" style="width: 100%">
            <el-option value="VIEW" label="只读" />
            <el-option value="EDIT" label="可编辑" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShareDialog = false">
          取消
        </el-button>
        <el-button type="primary" :loading="shareLoading" @click="handleShareConfirm">
          分享
        </el-button>
      </template>
    </el-dialog>
    <!-- 隐藏的文件上传输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      multiple
      style="display: none"
      @change="handleFileSelect"
    >
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocuments, searchDocuments, updateDocument, deleteDocument, permanentlyDeleteDocument, restoreDocument, shareDocument } from '@/api/im/document'
import { createFolder, getQuota, uploadFile, getFileList, getFolderList, renameFolder, deleteFolder, deleteFile, renameFile, toggleFileStar, toggleFolderStar } from '@/api/im/cloudDrive'
import { formatFileSize, formatTime } from '@/utils/format'
import DocumentEditorDialog from '@/components/Documents/DocumentEditorDialog.vue'
import { Folder, Delete, Search, List, Grid, Plus, FolderOpened, Star, MoreFilled, Document, Picture, VideoCamera, Microphone, View, Edit, Share, StarFilled, EditPen, Refresh, ArrowLeft } from '@element-plus/icons-vue'

const activeNav = ref('recent')
const viewMode = ref('list')
const searchQuery = ref('')
const loading = ref(false)
const files = ref([])
const selectedFile = ref(null)
const showEditor = ref(false)
const currentFolderId = ref(0) // 0表示根目录
const folderPath = ref([]) // 文件夹导航路径 [{id, name}]
const quota = ref({ usedCapacityFormat: '0B', totalCapacityFormat: '100GB', usageRate: 0 })
const showShareDialog = ref(false)
const shareForm = ref({ targetUserId: '', permission: 'VIEW' })
const shareLoading = ref(false)
const sharingFile = ref(null)

const mainNavItems = ref([
  { id: 'my', label: '我的文件', icon: 'Folder' },
  { id: 'shared', label: '共享文件', icon: 'UserFilled' },
  { id: 'recent', label: '最近访问', icon: 'Clock' },
  { id: 'trash', label: '回收站', icon: 'Delete' }
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

// 是否在子文件夹中
const isInSubFolder = computed(() => folderPath.value.length > 0)

// 导航到指定文件夹
const navigateToFolder = (targetFolder) => {
  if (!targetFolder) {
    // 返回根目录
    currentFolderId.value = 0
    folderPath.value = []
  } else {
    const idx = folderPath.value.findIndex(p => p.id === targetFolder.id)
    if (idx >= 0) {
      // 点击面包屑中的某一级
      folderPath.value = folderPath.value.slice(0, idx + 1)
    } else {
      // 进入子文件夹
      folderPath.value.push(targetFolder)
    }
    currentFolderId.value = targetFolder.id
  }
  loadDocuments()
}

// 返回上级文件夹
const goBack = () => {
  if (folderPath.value.length > 0) {
    folderPath.value.pop()
    currentFolderId.value = folderPath.value.length > 0 ? folderPath.value[folderPath.value.length - 1].id : 0
    loadDocuments()
  }
}

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    if (activeNav.value === 'my') {
      // 我的文件：获取文件夹和文件列表
      const [folderRes, fileRes] = await Promise.all([
        getFolderList({ parentId: currentFolderId.value, ownerType: 'USER' }),
        getFileList(currentFolderId.value)
      ])

      // 转换文件夹
      const folders = (folderRes.data || []).map(folder => ({
        id: `folder_${folder.id}`,
        name: folder.folderName,
        icon: 'Folder',
        iconClass: 'icon-folder',
        meta: folder.fileCount != null ? `${folder.fileCount} 个文件` : '',
        owner: folder.ownerName || '我',
        modifiedTime: formatTime(folder.updateTime),
        isStarred: false,
        documentType: 'FOLDER',
        cloudFileId: folder.id,
        isFolder: true
      }))

      // 转换文件
      const filesList = (fileRes.data || []).map(doc => ({
        id: doc.id,
        name: doc.fileName || doc.name || doc.title,
        icon: getFileIcon(doc.fileType),
        iconClass: getIconClass(doc.fileType),
        meta: formatFileSize(doc.fileSize || 0),
        owner: doc.uploaderName || '我',
        modifiedTime: formatTime(doc.updateTime),
        isStarred: doc.isStarred,
        documentType: doc.fileType,
        cloudFileId: doc.id,
        isFolder: false
      }))

      // 文件夹在前，文件在后
      files.value = [...folders, ...filesList]
    } else {
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
          documentType: doc.type,
          isFolder: false
        }))
      }
    }
  } catch (error) {
    console.error('加载文档失败:', error)
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

// 加载存储配额
const loadQuota = async () => {
  try {
    const res = await getQuota()
    if (res.code === 200 && res.data) {
      quota.value = {
        usedCapacityFormat: res.data.usedCapacityFormat || '0B',
        totalCapacityFormat: res.data.totalCapacityFormat || '100GB',
        usageRate: res.data.usageRate || 0
      }
    }
  } catch (error) {
    // 静默失败，使用默认值
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
    'TEXT': 'Document',
    'FILE': 'Document',
    'IMAGE': 'Picture',
    'VIDEO': 'VideoCamera',
    'VOICE': 'Microphone',
    'FOLDER': 'Folder'
  }
  return icons[type] || 'Document'
}

// Element Plus 图标名（用于模板动态组件）
const getNavIcon = (icon) => {
  const map = {
    'folder': 'Folder',
    'people': 'UserFilled',
    'schedule': 'Clock',
    'delete_outline': 'Delete'
  }
  return map[icon] || 'Folder'
}

// 文件图标 Element Plus 组件名
const getFileIconEl = (icon) => {
  const map = {
    Document,
    Picture,
    VideoCamera,
    Microphone,
    Folder
  }
  return map[icon] || Document
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
  if (file.documentType === 'FOLDER') {
    // 文件夹：进入子文件夹
    navigateToFolder({ id: file.cloudFileId || file.id, name: file.name })
    return
  }
  selectedFile.value = file
  showEditor.value = true
}

// 双击处理：文件夹进入，文件重命名
const handleDoubleClick = async (file) => {
  if (activeNav.value === 'trash') return
  if (file.documentType === 'FOLDER') {
    // 文件夹：进入子文件夹
    navigateToFolder({ id: file.cloudFileId || file.id, name: file.name })
    return
  }
  // 文件：重命名
  const { value: newName } = await ElMessageBox.prompt('请输入新名称', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: file.name,
    inputValidator: (val) => val && val.trim() !== '' ? true : '名称不能为空'
  })
  if (newName) {
    try {
      const res = await updateDocument({ id: file.id, title: newName.trim() })
      if (res.code === 200) {
        ElMessage.success('重命名成功')
        loadDocuments()
      }
    } catch (error) {
      ElMessage.error('重命名失败')
    }
  }
}

// 处理文件保存
const handleFileSaved = () => {
  loadDocuments()
}

// 确认分享
const handleShareConfirm = async () => {
  if (!sharingFile.value) return
  if (!shareForm.value.targetUserId.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }
  shareLoading.value = true
  try {
    const res = await shareDocument({
      documentId: sharingFile.value.id,
      targetUserId: Number(shareForm.value.targetUserId),
      permission: shareForm.value.permission
    })
    if (res.code === 200) {
      ElMessage.success('分享成功')
      showShareDialog.value = false
    } else {
      ElMessage.error(res.msg || '分享失败')
    }
  } catch (error) {
    ElMessage.error('分享失败')
  } finally {
    shareLoading.value = false
  }
}

// 文件上传输入框引用
const fileInputRef = ref(null)

// 处理新建命令
const handleNewCommand = async (command) => {
  if (command === 'folder') {
    const { value: folderName } = await ElMessageBox.prompt('请输入文件夹名称', '新建文件夹', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '新建文件夹',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '文件夹名称不能为空'
        }
        return true
      }
    })
    if (folderName) {
      try {
        const res = await createFolder({
          folderName: folderName.trim(),
          parentId: currentFolderId.value,
          ownerType: 'USER',
          accessPermission: 'PRIVATE'
        })
        if (res.code === 200) {
          ElMessage.success('文件夹创建成功')
          loadDocuments()
          loadQuota()
        }
      } catch (error) {
        ElMessage.error('创建文件夹失败')
      }
    }
  } else if (command === 'upload') {
    fileInputRef.value?.click()
  }
}

// 处理文件选择上传
const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files || [])
  if (!files.length) return

  try {
    for (const file of files) {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('folderId', String(currentFolderId.value))
      await uploadFile(formData)
    }
    ElMessage.success(`${files.length} 个文件上传成功`)
    loadDocuments()
    loadQuota()
  } catch (error) {
    ElMessage.error('文件上传失败')
  } finally {
    // 清空 input 以便重复选择同一文件
    event.target.value = ''
  }
}

// 处理文件操作菜单
const handleDropdownCommand = async (command, file) => {
  switch (command) {
    case 'view':
      handleFileClick(file)
      break
    case 'edit':
      handleFileClick(file)
      break
    case 'share':
      sharingFile.value = file
      shareForm.value = { targetUserId: '', permission: 'VIEW' }
      showShareDialog.value = true
      break
    case 'star':
    case 'unstar': {
      try {
        const isStar = command === 'star'
        let res
        if (file.documentType === 'FOLDER') {
          res = await toggleFolderStar(file.cloudFileId || file.id, isStar)
        } else if (file.cloudFileId) {
          // 云盘文件
          res = await toggleFileStar(file.cloudFileId, isStar)
        } else {
          // 文档系统文件 - 使用 document API
          const { toggleStar } = await import('@/api/im/document')
          res = await toggleStar(file.id, isStar)
        }
        if (res.code === 200) {
          ElMessage.success(isStar ? '已收藏' : '已取消收藏')
          loadDocuments()
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
      break
    }
    case 'rename': {
      const { value: newName } = await ElMessageBox.prompt('请输入新名称', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: file.name,
        inputValidator: (val) => val && val.trim() !== '' ? true : '名称不能为空'
      })
      if (newName) {
        try {
          let res
          if (file.documentType === 'FOLDER') {
            res = await renameFolder(file.cloudFileId || file.id, newName.trim())
          } else if (file.cloudFileId) {
            // 云盘文件
            res = await renameFile(file.cloudFileId, newName.trim())
          } else {
            // 文档系统文件
            res = await updateDocument({ id: file.id, title: newName.trim() })
          }
          if (res.code === 200) {
            ElMessage.success('重命名成功')
            loadDocuments()
          }
        } catch (error) {
          ElMessage.error('重命名失败')
        }
      }
      break
    }
    case 'delete': {
      try {
        await ElMessageBox.confirm(`确定要删除 "${file.name}" 吗？`, '删除文档', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let res
        if (file.documentType === 'FOLDER') {
          res = await deleteFolder(file.cloudFileId || file.id)
        } else if (file.cloudFileId) {
          // 云盘文件
          res = await deleteFile(file.cloudFileId)
        } else {
          // 文档系统文件
          res = await deleteDocument(file.id)
        }
        if (res.code === 200) {
          ElMessage.success('已删除')
          loadDocuments()
          loadQuota()
        }
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败')
      }
      break
    }
    case 'restore': {
      try {
        const res = await restoreDocument(file.id)
        if (res.code === 200) {
          ElMessage.success('已恢复')
          loadDocuments()
        }
      } catch (error) {
        ElMessage.error('恢复失败')
      }
      break
    }
    case 'permanent': {
      try {
        await ElMessageBox.confirm(`确定要永久删除 "${file.name}" 吗？此操作不可恢复！`, '永久删除', {
          confirmButtonText: '永久删除',
          cancelButtonText: '取消',
          type: 'error'
        })
        const res = await permanentlyDeleteDocument(file.id)
        if (res.code === 200) {
          ElMessage.success('已永久删除')
          loadDocuments()
          loadQuota()
        }
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('永久删除失败')
      }
      break
    }
  }
}

// 监听导航变化
watch(activeNav, () => {
  currentFolderId.value = 0
  folderPath.value = []
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

// 组件卸载时清理定时器
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
})

// 初始化加载
onMounted(() => {
  loadDocuments()
  loadQuota()
})
</script>

<style scoped lang="scss">
.documents-panel {
  display: flex; height: 100%; background: #fff;
}

// 1. 侧边栏
.docs-sidebar {
  width: 240px; background: #f8fbff; border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
}

.sidebar-header {
  height: 56px; padding: 0 20px; display: flex; align-items: center;
  .sidebar-title { font-size: 16px; font-weight: 600; color: var(--dt-text-primary); }
}

.sidebar-content {
  flex: 1; padding: 12px 8px;
  .nav-item {
    height: 40px; display: flex; align-items: center; gap: 12px; padding: 0 12px;
    border-radius: 8px; cursor: pointer; color: var(--dt-text-secondary); margin-bottom: 2px;
    &:hover { background: #eff4fc; }
    &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
  }
  .nav-divider { height: 1px; background: rgba(0,0,0,0.04); margin: 12px; }
  .nav-section-label { font-size: 11px; color: var(--dt-text-quaternary); padding: 8px 12px; }
}

// 2. 主区头部
.docs-header {
  height: 56px; padding: 0 24px; border-bottom: 1px solid var(--dt-border-light);
  display: flex; align-items: center; justify-content: space-between;
}

.header-left {
  display: flex; align-items: center; gap: 16px;
  .header-divider { width: 1px; height: 16px; background: var(--dt-border-light); }
}

.search-box {
  width: 240px; height: 32px; background: var(--dt-bg-hover); border-radius: 16px;
  display: flex; align-items: center; padding: 0 12px;
  input { flex: 1; border: none; background: transparent; outline: none; font-size: 13px; }
  .search-icon { color: var(--dt-text-tertiary); margin-right: 8px; }
}

.new-btn {
  height: 32px; padding: 0 16px; background: var(--dt-brand-color); color: #fff;
  border: none; border-radius: 6px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; gap: 6px;
  &:hover { background: var(--dt-brand-hover); }
}

// 3. 内容区
.docs-content {
  flex: 1; overflow-y: auto; padding: 12px 24px;
}

// 列表视图
.list-view {
  .list-header {
    height: 40px; display: flex; align-items: center; border-bottom: 1px solid var(--dt-border-light);
    color: var(--dt-text-tertiary); font-size: 12px; padding: 0 12px;
    div { flex: 1; } .col-name { flex: 2; } .col-actions { width: 40px; flex: none; }
  }
  .list-row {
    height: 52px; display: flex; align-items: center; padding: 0 12px; border-radius: 8px;
    cursor: pointer; transition: 0.2s;
    &:hover { background: var(--dt-bg-hover); .more-btn { opacity: 1; } }
    div { flex: 1; font-size: 13px; color: var(--dt-text-secondary); }
    .col-name { flex: 2; display: flex; align-items: center; gap: 12px;
      .file-name { color: var(--dt-text-primary); font-weight: 500; display: block; }
      .file-size { font-size: 11px; color: var(--dt-text-quaternary); }
    }
    .more-btn { opacity: 0; color: var(--dt-text-tertiary); cursor: pointer; }
  }
}

// 网格视图
.grid-view {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 20px; padding: 12px 0;
  .grid-item {
    padding: 12px; border-radius: 12px; border: 1px solid transparent; transition: 0.2s;
    text-align: center; cursor: pointer;
    &:hover { background: #fdfdfe; border-color: var(--dt-brand-light); box-shadow: var(--dt-shadow-1); }
    .grid-thumb {
      height: 100px; background: #f8f9fb; border-radius: 8px; @include flex-center; margin-bottom: 10px;
      font-size: 40px; overflow: hidden;
      &.icon-doc { color: #2196f3; } &.icon-pdf { color: #f56c6c; } &.icon-sheet { color: #67c23a; }
    }
    .grid-name { font-size: 13px; color: var(--dt-text-primary); @include text-ellipsis; display: block; }
    .grid-sub { font-size: 11px; color: var(--dt-text-quaternary); }
  }
}

// 文件图标基础
.file-icon-wrapper {
  width: 32px; height: 32px; border-radius: 6px; @include flex-center; font-size: 16px;
  &.icon-folder { background: #fff8e1; color: #ff9800; }
  &.icon-doc { background: #eef5fe; color: #2196f3; }
  &.icon-image { background: #f3e5f5; color: #9c27b0; }
}

.empty-view {
  height: 80%; @include flex-center; color: var(--dt-text-tertiary);
  .empty-box { text-align: center; .empty-icon { font-size: 64px; opacity: 0.2; margin-bottom: 16px; } }
}
</style>
