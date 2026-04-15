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
            <el-icon class="nav-icon"><component :is="getNavIcon(nav.icon)" /></el-icon>
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
            <el-icon class="nav-icon"><component :is="getNavIcon(nav.icon)" /></el-icon>
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
            <el-icon class="search-icon"><Search /></el-icon>
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

      <div class="docs-content">
        <!-- 空状态 -->
        <div v-if="!loading && files.length === 0" class="empty-state">
          <el-icon class="empty-icon"><FolderOpened /></el-icon>
          <p class="empty-text">
            {{ searchQuery ? '没有找到匹配的文档' : '暂无文档' }}
          </p>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading">
            <loading />
          </el-icon>
          <p>加载中...</p>
        </div>

        <!-- 文件列表 -->
        <div v-else class="files-table-wrapper">
          <table class="files-table">
            <thead>
              <tr>
                <th class="name-col">
                  名称
                </th>
                <th>所有者</th>
                <th>修改时间</th>
                <th class="actions-col" />
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="file in files"
                :key="file.id"
                class="file-row"
                @click="handleFileClick(file)"
                @dblclick="handleDoubleClick(file)"
              >
                <td class="name-col">
                  <div class="file-info">
                    <div class="file-icon" :class="file.iconClass">
                      <el-icon>{{ getFileIconEl(file.icon) }}</el-icon>
                    </div>
                    <div class="file-text">
                      <div class="file-name">
                        {{ file.name }}
                        <span v-if="file.isStarred" class="star-icon">
                          <el-icon><Star /></el-icon>
                        </span>
                      </div>
                      <div class="file-meta">
                        {{ file.meta }}
                      </div>
                    </div>
                  </div>
                </td>
                <td><span class="owner-name">{{ file.owner }}</span></td>
                <td><span class="file-time">{{ file.modifiedTime }}</span></td>
                <td class="actions-col">
                  <el-dropdown trigger="click" @command="(cmd) => handleDropdownCommand(cmd, file)">
                    <button class="action-btn" @click.stop>
                      <el-icon><MoreFilled /></el-icon>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="view" :icon="View">查看</el-dropdown-item>
                        <el-dropdown-item
                          v-if="file.documentType === 'TEXT' && activeNav !== 'trash'"
                          command="edit"
                          :icon="Edit"
                        >编辑</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav !== 'trash' && !file.cloudFileId"
                          command="share"
                          :icon="Share"
                        >分享</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav !== 'trash' && file.documentType !== 'FOLDER' && !file.cloudFileId"
                          :command="file.isStarred ? 'unstar' : 'star'"
                          :icon="file.isStarred ? StarFilled : Star"
                        >{{ file.isStarred ? '取消收藏' : '收藏' }}</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav !== 'trash'"
                          command="rename"
                          :icon="EditPen"
                        >重命名</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav !== 'trash'"
                          command="delete"
                          :icon="Delete"
                          divided
                        >删除</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav === 'trash'"
                          command="restore"
                          :icon="Refresh"
                        >恢复</el-dropdown-item>
                        <el-dropdown-item
                          v-if="activeNav === 'trash'"
                          command="permanent"
                          :icon="Delete"
                          divided
                        >永久删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
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
    <!-- 分享文档弹窗 -->
    <el-dialog v-model="showShareDialog" title="分享文档" width="440px" append-to-body>
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
        <el-button @click="showShareDialog = false">取消</el-button>
        <el-button type="primary" :loading="shareLoading" @click="handleShareConfirm">分享</el-button>
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
import { getDocuments, searchDocuments, toggleStar, updateDocument, deleteDocument, permanentlyDeleteDocument, restoreDocument, shareDocument } from '@/api/im/document'
import { createFolder, getQuota, uploadFile, getFileList, getFolderList, renameFolder, deleteFolder, deleteFile, renameFile, toggleFileStar, toggleFolderStar } from '@/api/im/cloudDrive'
import { formatFileSize, formatTime } from '@/utils/format'
import DocumentEditorDialog from '@/components/Documents/DocumentEditorDialog.vue'
import { Folder, UserFilled, Clock, Delete, Search, List, Grid, Plus, FolderOpened, Star, MoreFilled, Document, Picture, VideoCamera, Microphone, View, Edit, Share, StarFilled, EditPen, Refresh, ArrowLeft } from '@element-plus/icons-vue'

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
    'Document': Document,
    'Picture': Picture,
    'VideoCamera': VideoCamera,
    'Microphone': Microphone,
    'Folder': Folder
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
.documents-panel { display: flex; height: 100%; flex: 1; background: var(--dt-bg-body); }
.docs-sidebar { width: var(--dt-contact-panel-width, 220px); background: var(--dt-bg-card); border-right: 1px solid var(--dt-border-light); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: var(--dt-spacing-lg) var(--dt-spacing-md); border-bottom: 1px solid var(--dt-border-lighter);
  .sidebar-title { font-size: var(--dt-font-size-base); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); margin: 0; }
}
.sidebar-content { flex: 1; padding: var(--dt-spacing-md) 0; overflow-y: auto;
  .nav-item { display: flex; flex-direction: row; align-items: center; gap: var(--dt-spacing-sm); padding: var(--dt-spacing-sm) var(--dt-spacing-md); height: 44px; box-sizing: border-box; margin: 2px var(--dt-spacing-xs); border-radius: var(--dt-radius-md); cursor: pointer; color: var(--dt-text-primary); transition: background-color var(--dt-transition-base); position: relative;
    &:hover { background: var(--dt-bg-session-hover); }
    &.active {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
      font-weight: var(--dt-font-weight-semibold);
      &::before { content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 3px; height: 20px; background: var(--dt-brand-color); border-radius: 0 2px 2px 0; }
    }
    .nav-icon { font-size: var(--dt-icon-size-lg); flex-shrink: 0; }
    .nav-label { font-size: var(--dt-font-size-base); flex: 1; }
  }
  .nav-divider { height: 1px; background: var(--dt-border-light); margin: var(--dt-spacing-md) var(--dt-spacing-md); }
  .nav-section-label { padding: var(--dt-spacing-sm) var(--dt-spacing-lg); font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); text-transform: uppercase; font-weight: var(--dt-font-weight-medium); }
}
.sidebar-footer { padding: var(--dt-spacing-lg); border-top: 1px solid var(--dt-border-light);
  .storage-info { background: var(--dt-bg-body); padding: var(--dt-spacing-md); border-radius: var(--dt-radius-md);
    .storage-header { display: flex; justify-content: space-between; font-size: var(--dt-font-size-xs); margin-bottom: var(--dt-spacing-sm); color: var(--dt-text-tertiary); }
    .storage-bar { height: var(--dt-spacing-xs); background: var(--dt-border-color); border-radius: var(--dt-radius-full); overflow: hidden; .storage-fill { height: 100%; background: var(--dt-brand-color); } }
    .storage-text { font-size: var(--dt-font-size-xs); color: var(--dt-text-quaternary); margin-top: var(--dt-spacing-sm); text-align: right; }
  }
}
.docs-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.docs-header { height: var(--dt-header-height, 56px); padding: 0 var(--dt-spacing-xl); border-bottom: 1px solid var(--dt-border-light); display: flex; align-items: center; justify-content: space-between;
  .header-left { display: flex; align-items: center; gap: var(--dt-spacing-md);
    .back-btn { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border: none; background: var(--dt-bg-body); border-radius: var(--dt-radius-sm); cursor: pointer; color: var(--dt-text-secondary); &:hover { background: var(--dt-bg-session-hover); color: var(--dt-text-primary); } }
    .breadcrumb-nav { display: flex; align-items: center; gap: 4px; font-size: var(--dt-font-size-sm);
      .breadcrumb-sep { color: var(--dt-text-quaternary); }
      .breadcrumb-item { color: var(--dt-text-secondary); cursor: pointer; padding: 2px 4px; border-radius: var(--dt-radius-xs); &:hover { color: var(--dt-brand-color); background: var(--dt-brand-lighter); }
        &.active { color: var(--dt-text-primary); font-weight: var(--dt-font-weight-medium); cursor: default; &:hover { background: transparent; color: var(--dt-text-primary); } }
      }
    }
    .header-divider { width: 1px; height: var(--dt-spacing-lg); background: var(--dt-border-light); }
    .search-box { position: relative; .search-icon { position: absolute; left: var(--dt-spacing-sm); top: 50%; transform: translateY(-50%); font-size: var(--dt-icon-size-md); color: var(--dt-text-tertiary); }
      .search-input { width: var(--dt-search-width-md, 200px); height: var(--dt-btn-height-sm); background: var(--dt-bg-body); border: none; border-radius: var(--dt-radius-sm); padding: 0 var(--dt-spacing-2xl); font-size: var(--dt-font-size-sm); outline: none; &:focus { background: var(--dt-bg-card); box-shadow: 0 0 0 2px var(--dt-brand-lighter); } }
    }
  }
  .header-right { display: flex; align-items: center; gap: var(--dt-spacing-md);
    .view-toggle { display: flex; background: var(--dt-bg-body); padding: var(--dt-spacing-xs); border-radius: var(--dt-radius-sm);
      .toggle-btn { padding: var(--dt-spacing-xs); border: none; background: transparent; cursor: pointer; color: var(--dt-text-tertiary); &.active { background: var(--dt-bg-card); color: var(--dt-brand-color); border-radius: var(--dt-radius-xs); box-shadow: var(--dt-shadow-1); } }
    }
    .new-btn { padding: var(--dt-spacing-xs) var(--dt-spacing-md); background: var(--dt-brand-color); color: var(--dt-text-primary); border: none; border-radius: var(--dt-radius-sm); font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); cursor: pointer; }
  }
}
.docs-content { flex: 1; padding: var(--dt-spacing-lg); overflow-y: auto; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: var(--dt-empty-state-height, 200px); color: var(--dt-text-tertiary);
  .empty-icon { font-size: var(--dt-icon-size-xl, 64px); margin-bottom: var(--dt-spacing-lg); opacity: 0.5; color: var(--dt-border-color); }
  .empty-text { margin: 0; font-size: var(--dt-font-size-sm); }
}
.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: var(--dt-empty-state-height, 200px); color: var(--dt-text-tertiary);
  .is-loading { font-size: var(--dt-icon-size-2xl); margin-bottom: var(--dt-spacing-lg); }
  p { margin: 0; font-size: var(--dt-font-size-sm); }
}
.files-table { width: 100%; border-collapse: collapse;
  th { text-align: left; padding: var(--dt-spacing-sm) var(--dt-spacing-md); font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); border-bottom: 1px solid var(--dt-border-light); font-weight: var(--dt-font-weight-normal); }
  td { padding: var(--dt-spacing-sm) var(--dt-spacing-md); border-bottom: 1px solid var(--dt-border-light); vertical-align: middle; }
  .file-row { cursor: pointer; transition: background var(--dt-transition-fast), box-shadow var(--dt-transition-fast); border-radius: var(--dt-radius-md);
    &:hover { background: var(--dt-bg-session-hover); box-shadow: var(--dt-shadow-1); .action-btn { opacity: 1; } }
  }
  .file-info { display: flex; align-items: center; gap: var(--dt-spacing-sm);
    .file-icon { width: var(--dt-avatar-size-md); height: var(--dt-avatar-size-md); border-radius: var(--dt-radius-sm); display: flex; align-items: center; justify-content: center; font-size: var(--dt-icon-size-lg);
      .el-icon { font-size: var(--dt-icon-size-lg); }
      &.icon-folder { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
      &.icon-doc { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
      &.icon-sheet { background: var(--dt-success-bg); color: var(--dt-success-color); }
      &.icon-image { background: var(--dt-brand-lighter); color: var(--dt-brand-color); }
      &.icon-video { background: var(--dt-error-bg); color: var(--dt-error-color); }
      &.icon-voice { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
    }
    .file-name { font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-medium); color: var(--dt-text-primary); display: flex; align-items: center; gap: var(--dt-spacing-xs);
      .star-icon { color: var(--dt-warning-color); font-size: var(--dt-font-size-sm); }
    }
    .file-meta { font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); margin-top: var(--dt-spacing-xs); }
  }
  .owner-name, .file-time { font-size: var(--dt-font-size-sm); color: var(--dt-text-secondary); }
  .actions-col { text-align: right; .action-btn { opacity: 0; background: transparent; border: none; color: var(--dt-text-tertiary); cursor: pointer; transition: color var(--dt-transition-fast); &:hover { color: var(--dt-brand-color); } } }
}
</style>
