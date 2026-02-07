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
            <span class="material-icons-outlined nav-icon">{{ nav.icon }}</span>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="storage-info">
          <div class="storage-header">
            <span class="storage-label">存储空间</span>
            <span class="storage-percent">{{ storageDisplay.percent }}%</span>
          </div>
          <div class="storage-bar">
            <div
              class="storage-fill"
              :style="{ width: storageDisplay.percent + '%' }"
            />
          </div>
          <div class="storage-text">
            {{ storageDisplay.used }}&nbsp;/&nbsp;{{ storageDisplay.total }}
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="docs-main">
      <header class="docs-header">
        <div class="header-left">
          <h2 class="header-title">
            {{ currentViewTitle }}
          </h2>
          <div class="header-divider" />
          <div class="search-box">
            <span class="material-icons-outlined search-icon">search</span>
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
          <el-dropdown
            trigger="click"
            @command="handleNewCommand"
          >
            <button class="new-btn">
              <span class="material-icons-outlined">add</span>
              新建
              <span class="material-icons-outlined arrow">expand_more</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="folder">
                  <span class="material-icons-outlined">folder</span>
                  新建文件夹
                </el-dropdown-item>
                <el-dropdown-item command="upload">
                  <span class="material-icons-outlined">upload</span>
                  上传文件
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div class="docs-content">
        <div
          v-if="loading"
          class="loading-state"
        >
          <el-icon
            class="is-loading"
            :size="32"
          >
            <ElIconLoading />
          </el-icon>
          <span>加载中...</span>
        </div>

        <div
          v-else-if="files.length === 0"
          class="empty-state"
        >
          <span class="material-icons-outlined empty-icon">cloud_off</span>
          <p class="empty-text">
            暂无文件
          </p>
        </div>

        <div
          v-else
          class="files-table-wrapper"
        >
          <table class="files-table">
            <thead>
              <tr>
                <th class="name-col">
                  名称
                </th>
                <th>所有者</th>
                <th>修改时间</th>
                <th class="actions-col">
                  操作
                </th>
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
                    <div
                      class="file-icon"
                      :class="file.iconClass"
                    >
                      <span class="material-icons-outlined">{{ file.icon }}</span>
                    </div>
                    <div>
                      <div class="file-name">
                        {{ file.name }}
                      </div>
                      <div class="file-meta">
                        {{ file.meta }}
                      </div>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="owner-info">
                    <div
                      class="owner-avatar"
                      :style="{ background: file.ownerColor }"
                    >
                      {{ file.owner.charAt(0) }}
                    </div>
                    <span>{{ file.owner }}</span>
                  </div>
                </td>
                <td>
                  <span class="file-time">{{ file.modifiedTime }}</span>
                </td>
                <td class="actions-col">
                  <button
                    class="action-btn"
                    @click.stop="handleFileMenu(file)"
                  >
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
            <button
              class="page-btn"
              disabled
            >
              上一页
            </button>
            <span class="page-divider">|</span>
            <button class="page-btn">
              下一页
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 新建文件夹对话框 -->
    <el-dialog
      v-model="showFolderDialog"
      title="新建文件夹"
      width="400px"
    >
      <el-input
        v-model="newFolderName"
        placeholder="请输入文件夹名称"
        maxlength="50"
        show-word-limit
        @keyup.enter="handleCreateFolder"
      />
      <template #footer>
        <el-button @click="showFolderDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleCreateFolder"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件上传对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传文件"
      width="500px"
    >
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="uploadFiles"
        :multiple="true"
      >
        <span class="material-icons-outlined upload-icon">cloud_upload</span>
        <div class="upload-text">
          将文件拖到此处，或点击上传
        </div>
        <div class="upload-hint">
          支持任意类型文件，单个文件不超过100MB
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="uploading"
          @click="handleUploadSubmit"
        >
          开始上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <FilePreviewDialog
      v-model="showPreviewDialog"
      :file="selectedFile"
      @download="handleDownload"
    />

    <!-- 文档协作编辑器 -->
    <DocumentEditor
      v-if="showDocumentEditor"
      :document-id="editingDocumentId"
      @close="handleCloseDocumentEditor"
      @save="handleDocumentSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { Loading as ElIconLoading } from '@element-plus/icons-vue'
import FilePreviewDialog from '@/components/FilePreviewDialog/index.vue'
import DocumentEditor from '@/components/Documents/DocumentEditor.vue'
import { formatFileSize } from '@/utils/format'
import { getFolderList, getFileList, createFolder, uploadToCloud, getStorageQuota } from '@/api/im/cloud'

const activeNav = ref('recent')
const viewMode = ref('list')
const searchQuery = ref('')
const currentFolderId = ref(null)

// 对话框状态
const showFolderDialog = ref(false)
const showUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const showDocumentEditor = ref(false)
const editingDocumentId = ref(null)
const newFolderName = ref('')
const uploadFiles = ref([])
const uploading = ref(false)
const selectedFile = ref(null)

// 数据加载状态
const loading = ref(false)

// 存储配额
const storageQuota = ref({ used: 0, total: 0, percent: 0 })

const mainNavItems = ref([
  { id: 'recent', label: '最近使用', icon: 'schedule' },
  { id: 'my-created', label: '我创建的', icon: 'folder_special' },
  { id: 'shared', label: '分享给我', icon: 'share' },
  { id: 'starred', label: '收藏夹', icon: 'star_border' }
])

const storageNavItems = ref([
  { id: 'enterprise', label: '企业空间', icon: 'corporate_fare', ownerType: 'COMPANY' },
  { id: 'personal', label: '个人空间', icon: 'person_outline', ownerType: 'PERSONAL' },
  { id: 'trash', label: '回收站', icon: 'delete_outline', ownerType: 'PERSONAL' }
])

// 文件列表
const files = ref([])

// 当前所有者类型
const currentOwnerType = computed(() => {
  const nav = [...storageNavItems.value].find(item => item.id === activeNav.value)
  return nav?.ownerType || 'PERSONAL'
})

const currentViewTitle = computed(() => {
  const item = [...mainNavItems.value, ...storageNavItems.value]
    .find(item => item.id === activeNav.value)
  return item?.label || '最近使用'
})

// 存储空间显示
const storageDisplay = computed(() => {
  const { used, total } = storageQuota.value
  const percent = total > 0 ? Math.round((used / total) * 100) : 0
  return {
    used: formatFileSize(used),
    total: formatFileSize(total),
    percent
  }
})

// 图标映射
const getIconForFile = fileName => {
  const ext = fileName.split('.').pop().toLowerCase()
  const iconMap = {
    doc: { icon: 'description', iconClass: 'icon-doc' },
    docx: { icon: 'description', iconClass: 'icon-doc' },
    xls: { icon: 'table_view', iconClass: 'icon-sheet' },
    xlsx: { icon: 'table_view', iconClass: 'icon-sheet' },
    pdf: { icon: 'picture_as_pdf', iconClass: 'icon-pdf' },
    png: { icon: 'image', iconClass: 'icon-image' },
    jpg: { icon: 'image', iconClass: 'icon-image' },
    jpeg: { icon: 'image', iconClass: 'icon-image' },
    gif: { icon: 'image', iconClass: 'icon-image' },
    txt: { icon: 'text_snippet', iconClass: 'icon-file' },
    zip: { icon: 'folder_zip', iconClass: 'icon-file' },
    rar: { icon: 'folder_zip', iconClass: 'icon-file' }
  }
  return iconMap[ext] || { icon: 'insert_drive_file', iconClass: 'icon-file' }
}

// 加载文件列表
const loadFiles = async () => {
  loading.value = true
  try {
    const res = await getFileList(currentFolderId.value)
    if (res.code === 200 && res.data) {
      files.value = res.data.map(item => ({
        id: item.id,
        name: item.fileName || item.name || item.folderName,
        icon: item.isFolder ? 'folder' : getIconForFile(item.fileName || item.name).icon,
        iconClass: item.isFolder ? 'icon-folder' : getIconForFile(item.fileName || item.name).iconClass,
        meta: item.isFolder ? `${item.fileCount || 0} 个文件` : formatFileSize(item.fileSize || 0),
        owner: item.ownerName || '我',
        ownerColor: '#3b82f6',
        modifiedTime: item.updateTime || item.createTime || '',
        isFolder: item.isFolder,
        fileUrl: item.fileUrl,
        folderId: item.folderId
      }))
    } else {
      files.value = []
    }
  } catch (error) {
    console.warn('加载文件列表失败:', error.message)
    // 失败时显示空列表
    files.value = []
  } finally {
    loading.value = false
  }
}

// 加载存储配额
const loadStorageQuota = async () => {
  try {
    const res = await getStorageQuota()
    if (res.code === 200 && res.data) {
      storageQuota.value = {
        used: res.data.used || 0,
        total: res.data.total || 0,
        percent: res.data.percent || 0
      }
    }
  } catch (error) {
    console.warn('获取存储配额失败:', error.message)
    // 默认值
    storageQuota.value = { used: 0, total: 100 * 1024 * 1024 * 1024, percent: 0 }
  }
}

const handleFileClick = file => {
  if (file.isFolder) {
    // 文件夹：进入文件夹
    currentFolderId.value = file.id
    loadFiles()
  } else {
    // 检查是否为文档类型
    const fileName = file.name?.toLowerCase() || ''
    const isDocument = fileName.endsWith('.doc') ||
      fileName.endsWith('.docx') ||
      fileName.endsWith('.txt') ||
      fileName.endsWith('.md') ||
      file.iconClass === 'icon-doc'

    if (isDocument) {
      // 打开文档编辑器
      editingDocumentId.value = file.id
      showDocumentEditor.value = true
    } else {
      // 其他文件：预览
      selectedFile.value = file
      showPreviewDialog.value = true
    }
  }
}

const handleFileMenu = file => {
  ElMessageBox({
    title: file.name,
    message: '请选择操作',
    showCancelButton: true,
    confirmButtonText: '下载',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true,
    showClose: false,
    closeOnClickModal: false,
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        handleDownload(file)
        done()
      } else if (action === 'cancel') {
        // 显示更多选项
        ElMessageBox({
          title: '文件操作',
          message: '',
          showCancelButton: true,
          confirmButtonText: '重命名',
          cancelButtonText: '删除',
          distinguishCancelAndClose: true
        }).then(() => {
          // 重命名
          ElMessageBox.prompt('请输入新名称', '重命名', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputValue: file.name
          }).then(({ value }) => {
            file.name = value
            ElMessage.success('重命名成功')
          }).catch(() => { })
        }).catch(() => {
          // 删除
          ElMessageBox.confirm('确定要删除这个文件吗？', '确认删除', {
            type: 'warning'
          }).then(() => {
            files.value = files.value.filter(f => f.id !== file.id)
            ElMessage.success('删除成功')
          }).catch(() => { })
        })
        done()
      } else {
        done()
      }
    }
  })
}

// 新建命令处理
const handleNewCommand = command => {
  if (command === 'folder') {
    newFolderName.value = ''
    showFolderDialog.value = true
  } else if (command === 'upload') {
    uploadFiles.value = []
    showUploadDialog.value = true
  }
}

// 创建文件夹
const handleCreateFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  try {
    const res = await createFolder({
      folderName: newFolderName.value,
      parentId: currentFolderId.value,
      ownerType: currentOwnerType.value
    })

    if (res.code === 200) {
      ElMessage.success('文件夹创建成功')
      showFolderDialog.value = false
      loadFiles()
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建文件夹失败:', error)
    ElMessage.error('创建失败，请稍后重试')
  }
}

// 文件选择变化
const handleFileChange = (file, fileList) => {
  uploadFiles.value = fileList
}

// 执行上传
const handleUploadSubmit = async () => {
  if (uploadFiles.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    const uploadPromises = uploadFiles.value.map(file => {
      return uploadToCloud(currentFolderId.value || null, file.raw)
    })

    const results = await Promise.all(uploadPromises)
    const successCount = results.filter(r => r.code === 200).length

    if (successCount > 0) {
      ElMessage.success(`成功上传 ${successCount} 个文件`)
      showUploadDialog.value = false
      uploadFiles.value = []
      loadFiles()
      loadStorageQuota()
    } else {
      ElMessage.error('上传失败，请稍后重试')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

// 下载文件
const handleDownload = file => {
  if (file.fileUrl) {
    const link = document.createElement('a')
    link.href = file.fileUrl
    link.download = file.name
    link.click()
    ElMessage.success(`开始下载: ${file.name}`)
  } else {
    ElMessage.warning('暂无下载链接')
  }
}

// 关闭文档编辑器
const handleCloseDocumentEditor = () => {
  showDocumentEditor.value = false
  editingDocumentId.value = null
}

// 文档保存回调
const handleDocumentSave = ({ title, content }) => {
  // 更新本地文件列表中的文档信息
  const file = files.value.find(f => f.id === editingDocumentId.value)
  if (file) {
    file.name = title
    // 可以更新其他属性
  }
}

// 监听导航变化
watch(activeNav, () => {
  currentFolderId.value = null
  loadFiles()
})

// 初始加载
onMounted(() => {
  loadFiles()
  loadStorageQuota()
})
</script>

<style scoped lang="scss">
.documents-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

/* 左侧边栏 */
.docs-sidebar {
  width: 220px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 0 0 20px var(--dt-black-02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.5px;
}

.title-icon {
  color: var(--dt-brand-color);
  font-size: 20px;
  font-weight: 600;
}

.sidebar-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--dt-scrollbar-thumb-bg);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background-color: var(--dt-scrollbar-thumb-hover-bg);
    }
  }
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 12px;
  border-radius: var(--dt-radius-sm);
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);
  transform: translateX(2px);
}

.nav-item.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-weight: 500;
  box-shadow: 0 2px 8px var(--dt-brand-extra-light);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background-color: var(--dt-brand-color);
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 20px;
  text-align: center;
}

.nav-label {
  font-size: 14px;
  flex: 1;
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-light);
  margin: 12px 0;
  opacity: 0.6;
}

.nav-section-label {
  padding: 0 12px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
}

.storage-info {
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  padding: 12px;
  box-shadow: 0 1px 3px var(--dt-black-04);
}

.storage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.storage-label {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: 500;
}

.storage-percent {
  font-size: 12px;
  color: var(--dt-brand-color);
  font-weight: 500;
}

.storage-bar {
  width: 100%;
  height: 6px;
  background: var(--dt-border-color);
  border-radius: var(--dt-radius-sm);
  overflow: hidden;
  box-shadow: inset 0 1px 3px var(--dt-black-10);
}

.storage-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--dt-brand-color), var(--dt-brand-light));
  border-radius: var(--dt-radius-sm);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px var(--dt-brand-30);
}

.storage-text {
  margin-top: 8px;
  font-size: 11px;
  color: var(--dt-text-quaternary);
  text-align: right;
  font-weight: 400;
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
  padding: 0 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  box-shadow: 0 2px 8px var(--dt-black-02);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.header-divider {
  width: 1px;
  height: 24px;
  background: var(--dt-border-light);
  margin: 0 8px;
  opacity: 0.6;
}

.search-box {
  position: relative;
  flex-shrink: 0;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--dt-text-quaternary);
  font-size: 16px;
  transition: color 0.2s ease;
}

.search-input {
  width: 280px;
  background: var(--dt-bg-body);
  border: 1.5px solid var(--dt-border-color);
  border-radius: var(--dt-radius-2xl);
  padding: 10px 16px 10px 36px;
  font-size: 14px;
  color: var(--dt-text-primary);
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px var(--dt-black-04);
}

.search-input:focus {
  background: var(--dt-bg-card);
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px var(--dt-brand-lighter), 0 2px 8px var(--dt-brand-extra-light);
  width: 320px;
}

.search-input:focus+.search-icon {
  color: var(--dt-brand-color);
}

.search-input::placeholder {
  color: var(--dt-text-quaternary);
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-toggle {
  display: flex;
  align-items: center;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-xl);
  padding: 2px;
  box-shadow: inset 0 1px 3px var(--dt-black-04);
}

.toggle-btn {
  padding: 8px 12px;
  color: var(--dt-text-quaternary);
  background: none;
  border: none;
  border-radius: var(--dt-radius-xl);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
}

.toggle-btn:hover {
  color: var(--dt-text-secondary);
  background: var(--dt-black-02);
}

.toggle-btn.active {
  background: var(--dt-bg-card);
  color: var(--dt-brand-color);
  box-shadow: 0 2px 6px var(--dt-black-10);
  transform: translateY(-1px);
}

.new-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--dt-brand-color), var(--dt-brand-light));
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-2xl);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px var(--dt-brand-30);
  position: relative;
  overflow: hidden;
}

.new-btn:hover {
  background: linear-gradient(135deg, var(--dt-brand-hover), var(--dt-brand-color));
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--dt-brand-30);
}

.new-btn:active {
  transform: translateY(0);
}

.new-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: var(--dt-white-20);
  border-radius: var(--dt-radius-full);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.new-btn:hover::before {
  width: 300px;
  height: 300px;
}

.new-btn .arrow {
  font-size: 16px;
  margin-left: -2px;
  transition: transform 0.2s ease;
}

.new-btn:hover .arrow {
  transform: rotate(180deg);
}

/* 上传区域 */
.upload-area {
  padding: 48px 24px;
  border: 2px dashed var(--dt-border-color);
  border-radius: var(--dt-radius-xl);
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--dt-bg-body);
  position: relative;
  overflow: hidden;
}

.upload-area:hover {
  border-color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  box-shadow: 0 4px 16px var(--dt-brand-extra-light);
  transform: translateY(-2px);
}

.upload-icon {
  font-size: 56px;
  color: var(--dt-brand-color);
  margin-bottom: 16px;
  transition: transform 0.3s ease;
}

.upload-area:hover .upload-icon {
  transform: scale(1.1) rotate(5deg);
}

.upload-text {
  font-size: 16px;
  color: var(--dt-text-primary);
  margin-bottom: 6px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.upload-hint {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  line-height: 1.4;
  max-width: 320px;
  margin: 0 auto;
}

.docs-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-sm);
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background-color: var(--dt-text-quaternary);
    }
  }
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: var(--dt-border-color);
}

.empty-text {
  font-size: 14px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.files-table-wrapper {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: 0 4px 16px var(--dt-black-04);
  border: 1px solid var(--dt-border-color);
  overflow: hidden;
  transition: box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.files-table-wrapper:hover {
  box-shadow: 0 6px 24px var(--dt-black-08);
}

.files-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.files-table thead {
  background: var(--dt-bg-body);
  border-bottom: 1px solid var(--dt-border-color);
  box-shadow: 0 1px 3px var(--dt-black-02);
}

.files-table th {
  padding: 16px 24px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: relative;
}

.files-table th::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--dt-brand-lighter), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.files-table th:hover::after {
  opacity: 1;
}

.files-table th.actions-col {
  text-align: right;
}

.files-table tbody tr {
  border-bottom: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.files-table tbody tr:hover {
  background: var(--dt-bg-hover);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px var(--dt-black-04);
}

.files-table tbody tr:last-child {
  border-bottom: none;
}

.files-table td {
  padding: 16px 24px;
  transition: all 0.2s ease;
}

.files-table tbody tr:hover td {
  color: var(--dt-text-primary);
}

.name-col {
  width: 50%;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px var(--dt-black-08);
  position: relative;
  overflow: hidden;
}

.file-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--dt-white-20), transparent);
  transition: left 0.6s ease;
}

.file-icon:hover::before {
  left: 100%;
}

.icon-folder {
  background: linear-gradient(135deg, var(--dt-warning-bg), #fef3c7);
  color: var(--dt-warning-color);
}

.icon-doc {
  background: linear-gradient(135deg, var(--dt-brand-bg), #dbeafe);
  color: var(--dt-brand-color);
}

.icon-sheet {
  background: linear-gradient(135deg, var(--dt-success-bg), #d1fae5);
  color: var(--dt-success-color);
}

.icon-pdf {
  background: linear-gradient(135deg, var(--dt-error-bg), #fee2e2);
  color: var(--dt-error-color);
}

.icon-image {
  background: linear-gradient(135deg, var(--dt-purple-gradient-start), var(--dt-purple-gradient-end));
  color: var(--dt-purple-color);
}

.file-icon:hover {
  transform: scale(1.05) rotate(2deg);
  box-shadow: 0 4px 12px var(--dt-black-12);
}

.file-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  transition: all 0.2s ease;
  letter-spacing: 0.5px;
}

.file-meta {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  margin-top: 4px;
  line-height: 1.3;
  transition: color 0.2s ease;
}

.file-row:hover .file-name {
  color: var(--dt-brand-color);
}

.file-row:hover .file-meta {
  color: var(--dt-text-tertiary);
}

.owner-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--dt-text-secondary);
  transition: all 0.2s ease;
}

.owner-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--dt-radius-full);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px var(--dt-black-10);
  position: relative;
  overflow: hidden;
}

.owner-avatar::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--dt-white-20), transparent);
  transition: left 0.6s ease;
}

.owner-avatar:hover::before {
  left: 100%;
}

.file-row:hover .owner-avatar {
  transform: scale(1.1);
  box-shadow: 0 4px 8px var(--dt-black-15);
}

.file-time {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  transition: all 0.2s ease;
  font-weight: 400;
}

.file-row:hover .file-time {
  color: var(--dt-text-secondary);
}

.actions-col {
  text-align: right;
  position: relative;
}

.action-btn {
  opacity: 0;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  color: var(--dt-text-quaternary);
  cursor: pointer;
  padding: 6px 8px;
  border-radius: var(--dt-radius-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px var(--dt-black-04);
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
}

.file-row:hover .action-btn {
  opacity: 1;
  transform: translateY(-2px);
}

.action-btn:hover {
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  border-color: var(--dt-brand-color);
  box-shadow: 0 2px 8px var(--dt-brand-extra-light);
}

.pagination {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  font-size: 13px;
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: 0 2px 8px var(--dt-black-04);
  border: 1px solid var(--dt-border-color);
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-color);
  color: var(--dt-text-tertiary);
  cursor: pointer;
  padding: 6px 12px;
  font-size: 13px;
  border-radius: var(--dt-radius-sm);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px var(--dt-black-04);
}

.page-btn:hover:not([disabled]) {
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  border-color: var(--dt-brand-color);
  box-shadow: 0 2px 6px var(--dt-brand-extra-light);
  transform: translateY(-1px);
}

.page-btn[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.page-divider {
  color: var(--dt-border-color);
  margin: 0 4px;
  font-size: 11px;
  opacity: 0.6;
}

/* 暗色模式 */
.dark .docs-sidebar,
.dark .docs-header,
.dark .files-table-wrapper {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .sidebar-title,
.dark .header-title,
.dark .file-name {
  color: var(--dt-text-primary-dark);
}

.dark .nav-item {
  color: var(--dt-text-secondary-dark);
}

.dark .nav-item:hover {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-primary-dark);
}

.dark .nav-item.active {
  background: var(--dt-brand-bg-dark);
  color: var(--dt-brand-color);
}

.dark .search-input {
  background: var(--dt-bg-input-dark);
  color: var(--dt-text-primary-dark);
  border-color: var(--dt-border-dark);
}

.dark .search-input:focus {
  background: var(--dt-bg-card-dark);
  box-shadow: 0 0 0 3px var(--dt-brand-light), 0 2px 8px var(--dt-brand-extra-light);
}

.dark .storage-info {
  background: var(--dt-bg-hover-dark);
}

.dark .files-table thead {
  background: var(--dt-bg-hover-dark);
  border-color: var(--dt-border-dark);
}

.dark .files-table th {
  color: var(--dt-text-tertiary-dark);
}

.dark .files-table th::after {
  background: linear-gradient(90deg, transparent, var(--dt-brand-30), transparent);
}

.dark .files-table tbody tr:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .files-table tbody tr {
  border-color: var(--dt-border-dark);
}

.dark .file-time {
  color: var(--dt-text-tertiary-dark);
}

.dark .owner-info {
  color: var(--dt-text-secondary-dark);
}

.dark .toggle-btn {
  color: var(--dt-text-quaternary-dark);
  background: var(--dt-white-05);
}

.dark .toggle-btn:hover {
  background: var(--dt-white-10);
}

.dark .toggle-btn.active {
  background: var(--dt-bg-card-dark);
  color: var(--dt-brand-color);
  box-shadow: 0 2px 6px var(--dt-black-20);
}

.dark .action-btn {
  background: var(--dt-white-05);
  border-color: var(--dt-border-dark);
  color: var(--dt-text-quaternary-dark);
}

.dark .action-btn:hover {
  background: var(--dt-brand-bg-dark);
  border-color: var(--dt-brand-color);
  color: var(--dt-brand-color);
}

.dark .pagination {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
  box-shadow: 0 2px 8px var(--dt-black-10);
}

.dark .page-btn {
  background: var(--dt-white-05);
  border-color: var(--dt-border-dark);
  color: var(--dt-text-tertiary-dark);
}

.dark .page-btn:hover:not([disabled]) {
  background: var(--dt-brand-bg-dark);
  border-color: var(--dt-brand-color);
  color: var(--dt-brand-color);
}

.dark .upload-area {
  background: var(--dt-white-03);
  border-color: var(--dt-border-dark);
}

.dark .upload-area:hover {
  background: var(--dt-brand-bg-dark);
  border-color: var(--dt-brand-color);
  box-shadow: 0 4px 16px var(--dt-brand-extra-light);
}

/* 应用动画效果 - 使用全局动画 */
.docs-sidebar {
  animation: slideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.docs-header {
  animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.1s both;
}

.files-table-wrapper {
  animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

.pagination {
  animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.3s both;
}

/* 微交互优化 */
.nav-item,
.toggle-btn,
.new-btn,
.action-btn,
.page-btn {
  user-select: none;
}

/* 焦点样式优化 */
*:focus-visible {
  outline: 2px solid var(--dt-brand-color);
  outline-offset: 2px;
  border-radius: var(--dt-radius-sm);
}

.dark *:focus-visible {
  outline: 2px solid var(--dt-brand-color);
  outline-offset: 2px;
  border-radius: var(--dt-radius-sm);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .docs-sidebar {
    width: 200px;
  }

  .sidebar-title {
    font-size: 16px;
    padding: 16px;
  }

  .nav-item {
    padding: 12px 16px;
  }

  .storage-info {
    padding: 16px;
    margin: 16px;
  }

  .name-col {
    width: 45%;
  }

  .file-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}
</style>
