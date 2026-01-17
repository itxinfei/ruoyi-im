<template>
  <div class="drive-workspace">
    <!-- 文件管理头部 -->
    <div class="drive-header">
      <div class="header-left">
        <h3>文件</h3>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbs"
            :key="index"
            :to="item.path"
          >
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-button :icon="Upload" size="small">上传</el-button>
        <el-button :icon="FolderAdd" size="small">新建文件夹</el-button>
      </div>
    </div>

    <!-- 文件内容区 -->
    <div class="drive-content">
      <!-- 文件列表 -->
      <div class="file-list">
        <div class="file-header">
          <el-checkbox v-model="selectAll" @change="toggleSelectAll" />
          <span class="header-label">文件名</span>
          <span class="header-label">大小</span>
          <span class="header-label">修改时间</span>
        </div>

        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="file-item"
          :class="{ selected: file.selected }"
          @click="handleFileClick(file)"
          @contextmenu.prevent="showFileMenu($event, file)"
        >
          <el-checkbox v-model="file.selected" @click.stop />
          <div class="file-info">
            <el-icon class="file-icon">
              <component :is="getFileIcon(file.type)" />
            </el-icon>
            <span class="file-name">{{ file.name }}</span>
          </div>
          <span class="file-size">{{ formatFileSize(file.size) }}</span>
          <span class="file-time">{{ formatDate(file.updateTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 文件预览对话框 -->
    <FilePreviewDialog
      v-model:visible="previewDialogVisible"
      :file="previewingFile"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Upload, FolderAdd, Document, Folder, Picture, VideoPlay } from '@element-plus/icons-vue'
import FilePreviewDialog from '@/components/File/FilePreviewDialog.vue'

const searchKeyword = ref('')
const selectAll = ref(false)
const previewDialogVisible = ref(false)
const previewingFile = ref(null)

// 面包屑导航
const breadcrumbs = ref([
  { name: '我的文件', path: '/' }
])

// 模拟文件数据
const files = ref([
  { id: 1, name: '文档.docx', type: 'document', size: 1024 * 50, updateTime: Date.now(), selected: false },
  { id: 2, name: '图片.jpg', type: 'image', size: 1024 * 1024 * 2, updateTime: Date.now(), selected: false },
  { id: 3, name: '视频.mp4', type: 'video', size: 1024 * 1024 * 50, updateTime: Date.now(), selected: false },
  { id: 4, name: '资料', type: 'folder', size: 0, updateTime: Date.now(), selected: false },
])

// 过滤后的文件
const filteredFiles = computed(() => {
  if (!searchKeyword.value) return files.value
  return files.value.filter(file =>
    file.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 获取文件图标
const getFileIcon = (type) => {
  const iconMap = {
    document: Document,
    image: Picture,
    video: VideoPlay,
    folder: Folder,
  }
  return iconMap[type] || Document
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 处理文件点击
const handleFileClick = (file) => {
  if (file.type === 'folder') {
    // 进入文件夹
    breadcrumbs.value.push({ name: file.name, path: file.name })
  } else {
    // 预览文件
    previewingFile.value = file
    previewDialogVisible.value = true
  }
}

// 显示文件右键菜单
const showFileMenu = (event, file) => {
  // 显示文件操作菜单
  event.preventDefault()
  console.log('显示菜单:', file)
}

// 全选/取消全选
const toggleSelectAll = () => {
  files.value.forEach(file => {
    file.selected = selectAll.value
  })
}
</script>

<style scoped lang="scss">
.drive-workspace {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;

  .drive-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e8e8e8;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .drive-content {
    flex: 1;
    overflow: hidden;
    padding: 20px;

    .file-list {
      .file-header {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background: #fafafa;
        border-bottom: 1px solid #e8e8e8;
        font-size: 13px;
        color: #8c8c8c;

        .header-label {
          flex: 1;
          padding-left: 8px;
        }
      }

      .file-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #f5f5f5;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background: rgba(0, 0, 0, 0.02);
        }

        &.selected {
          background: rgba(22, 119, 255, 0.1);
        }

        .file-info {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 12px;
          padding-left: 8px;

          .file-icon {
            font-size: 20px;
            color: #8c8c8c;
          }

          .file-name {
            font-size: 14px;
            color: #262626;
          }
        }

        .file-size,
        .file-time {
          width: 120px;
          font-size: 13px;
          color: #8c8c8c;
          text-align: right;
        }
      }
    }
  }
}
</style>
