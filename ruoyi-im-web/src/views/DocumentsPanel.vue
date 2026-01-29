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
          <el-dropdown trigger="click" @command="handleNewCommand">
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

    <!-- 新建文件夹对话框 -->
    <el-dialog v-model="showFolderDialog" title="新建文件夹" width="400px">
      <el-input
        v-model="newFolderName"
        placeholder="请输入文件夹名称"
        maxlength="50"
        show-word-limit
        @keyup.enter="createFolder"
      />
      <template #footer>
        <el-button @click="showFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="createFolder">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文件" width="500px">
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
        <div class="upload-text">将文件拖到此处，或点击上传</div>
        <div class="upload-hint">支持任意类型文件，单个文件不超过100MB</div>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUploadSubmit">开始上传</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <FilePreviewDialog
      v-model="showPreviewDialog"
      :file="selectedFile"
      @download="handleDownload"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilePreviewDialog from '@/components/FilePreviewDialog/index.vue'

const activeNav = ref('recent')
const viewMode = ref('list')
const searchQuery = ref('')

// 对话框状态
const showFolderDialog = ref(false)
const showUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const newFolderName = ref('')
const uploadFiles = ref([])
const uploading = ref(false)
const selectedFile = ref(null)

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
  if (file.icon === 'folder') {
    // 文件夹：进入文件夹
    ElMessage.info(`进入文件夹: ${file.name}`)
  } else {
    // 文件：预览
    selectedFile.value = file
    showPreviewDialog.value = true
  }
}

const handleFileMenu = (file) => {
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
            }).catch(() => {})
          }).catch(() => {
            // 删除
            ElMessageBox.confirm('确定要删除这个文件吗？', '确认删除', {
              type: 'warning'
            }).then(() => {
              files.value = files.value.filter(f => f.id !== file.id)
              ElMessage.success('删除成功')
            }).catch(() => {})
          })
        done()
      } else {
        done()
      }
    }
  })
}

// 新建命令处理
const handleNewCommand = (command) => {
  if (command === 'folder') {
    newFolderName.value = ''
    showFolderDialog.value = true
  } else if (command === 'upload') {
    uploadFiles.value = []
    showUploadDialog.value = true
  }
}

// 创建文件夹
const createFolder = () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  const newFolder = {
    id: Date.now(),
    name: newFolderName.value,
    icon: 'folder',
    iconClass: 'icon-folder',
    meta: '0 个文件',
    owner: '我',
    ownerColor: '#3b82f6',
    modifiedTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
  }

  files.value.unshift(newFolder)
  ElMessage.success('文件夹创建成功')
  showFolderDialog.value = false
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
    // 模拟上传
    for (const file of uploadFiles.value) {
      const fileExt = file.name.split('.').pop().toLowerCase()
      let icon, iconClass, color

      const iconMap = {
        doc: { icon: 'description', iconClass: 'icon-doc' },
        docx: { icon: 'description', iconClass: 'icon-doc' },
        xls: { icon: 'table_view', iconClass: 'icon-sheet' },
        xlsx: { icon: 'table_view', iconClass: 'icon-sheet' },
        pdf: { icon: 'picture_as_pdf', iconClass: 'icon-pdf' },
        png: { icon: 'image', iconClass: 'icon-image' },
        jpg: { icon: 'image', iconClass: 'icon-image' },
        jpeg: { icon: 'image', iconClass: 'icon-image' },
        gif: { icon: 'image', iconClass: 'icon-image' }
      }

      const mapped = iconMap[fileExt] || { icon: 'insert_drive_file', iconClass: 'icon-file' }

      files.value.unshift({
        id: Date.now() + Math.random(),
        name: file.name,
        icon: mapped.icon,
        iconClass: mapped.iconClass,
        meta: formatFileSize(file.size),
        owner: '我',
        ownerColor: '#3b82f6',
        modifiedTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
      })
    }

    ElMessage.success(`成功上传 ${uploadFiles.value.length} 个文件`)
    showUploadDialog.value = false
    uploadFiles.value = []
  } catch (error) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

// 下载文件
const handleDownload = (file) => {
  ElMessage.success(`开始下载: ${file.name}`)
  // 实际项目中这里应该创建一个下载链接
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}
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
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.02);
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
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.2);
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
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
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
  border-radius: 0 2px 2px 0;
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
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
  border-radius: 3px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.storage-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--dt-brand-color), var(--dt-brand-light));
  border-radius: 3px;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px rgba(22, 119, 255, 0.3);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
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
  border-radius: 20px;
  padding: 10px 16px 10px 36px;
  font-size: 14px;
  color: var(--dt-text-primary);
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  background: var(--dt-bg-card);
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px var(--dt-brand-lighter), 0 2px 8px rgba(22, 119, 255, 0.15);
  width: 320px;
}

.search-input:focus + .search-icon {
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
  border-radius: 16px;
  padding: 2px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.toggle-btn {
  padding: 8px 12px;
  color: var(--dt-text-quaternary);
  background: none;
  border: none;
  border-radius: 14px;
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
  background: rgba(0, 0, 0, 0.02);
}

.toggle-btn.active {
  background: var(--dt-bg-card);
  color: var(--dt-brand-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
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
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.new-btn:hover {
  background: linear-gradient(135deg, var(--dt-brand-hover), var(--dt-brand-color));
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.3);
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
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
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
  padding: 40px 20px;
  border: 2px dashed var(--dt-border-color);
  border-radius: var(--dt-radius-xl);
  text-align: center;
  transition: all var(--dt-transition-fast);
}

.upload-area:hover {
  border-color: var(--dt-brand-color);
}

.upload-icon {
  font-size: 48px;
  color: var(--dt-brand-color);
  margin-bottom: 12px;
}

.upload-text {
  font-size: 14px;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.docs-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.files-table-wrapper {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: var(--dt-shadow-1);
  border: 1px solid var(--dt-border-color);
  overflow: hidden;
}

.files-table {
  width: 100%;
  border-collapse: collapse;
}

.files-table thead {
  background: var(--dt-bg-body);
  border-bottom: 1px solid var(--dt-border-color);
}

.files-table th {
  padding: 16px 24px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.files-table th.actions-col {
  text-align: right;
}

.files-table tbody tr {
  border-bottom: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: background var(--dt-transition-fast);
}

.files-table tbody tr:hover {
  background: var(--dt-bg-hover);
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
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-folder { background: var(--dt-warning-bg); color: var(--dt-warning-color); }
.icon-doc { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
.icon-sheet { background: var(--dt-success-bg); color: var(--dt-success-color); }
.icon-pdf { background: var(--dt-error-bg); color: var(--dt-error-color); }
.icon-image { background: #f3e8ff; color: #a855f7; }

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.file-meta {
  font-size: 10px;
  color: var(--dt-text-quaternary);
  margin-top: 2px;
}

.owner-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--dt-text-secondary);
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
  color: var(--dt-text-tertiary);
}

.actions-col {
  text-align: right;
}

.action-btn {
  opacity: 0;
  background: none;
  border: none;
  color: var(--dt-text-quaternary);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--dt-radius-sm);
  transition: all var(--dt-transition-fast);
}

.file-row:hover .action-btn {
  opacity: 1;
}

.action-btn:hover {
  color: var(--dt-brand-color);
  background: var(--dt-bg-hover);
}

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  background: none;
  border: none;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  padding: 4px 8px;
  font-size: 12px;
  transition: color var(--dt-transition-fast);
}

.page-btn:hover:not([disabled]) {
  color: var(--dt-brand-color);
}

.page-btn[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-divider {
  color: var(--dt-border-color);
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
}

.dark .toggle-btn.active {
  background: var(--dt-bg-card-dark);
  color: var(--dt-text-primary-dark);
}
</style>
