<template>
  <div class="files-list">
    <div ref="filesContainer" class="files-items">
      <transition-group name="file-list" tag="div" class="files-list-inner">
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="file-item"
          :class="{ active: activeFileId === file.id, collapsed: isCollapsed }"
          @click="onSelect(file)"
          @mouseenter="hoveredFileId = file.id"
          @mouseleave="hoveredFileId = null"
        >
          <div class="file-icon">
            <el-icon :size="isCollapsed ? 28 : 32">
              <component :is="getFileIcon(file.type)" />
            </el-icon>
          </div>

          <div class="file-info" :class="{ hidden: isCollapsed }">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
            </div>
          </div>

          <div class="file-actions" :class="{ hidden: isCollapsed }" @click.stop>
            <el-button
              :icon="Download"
              circle
              size="small"
              text
              @click="handleDownload(file)"
              title="下载"
            />
            <el-dropdown trigger="click" @command="cmd => handleFileAction(cmd, file)">
              <el-button :icon="MoreFilled" circle size="small" text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="preview">
                    <el-icon><View /></el-icon>
                    预览
                  </el-dropdown-item>
                  <el-dropdown-item command="share">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </transition-group>

      <transition name="empty-state">
        <div v-if="filteredFiles.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="empty-text">暂无文件</div>
          <el-button type="primary" size="small" @click="handleUpload">
            上传文件
          </el-button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Download,
  MoreFilled,
  View,
  Share,
  Delete,
  Document,
  Picture,
  VideoPlay,
  DocumentCopy,
  Folder,
} from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/format/file'
import { formatRelativeTime } from '@/utils/format/time'

const props = defineProps({
  files: { type: Array, default: () => [] },
  isCollapsed: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const store = useStore()

const hoveredFileId = ref(null)
const activeFileId = ref(null)
const filesContainer = ref(null)

const filteredFiles = computed(() => {
  return Array.isArray(props.files) ? props.files : []
})

const getFileIcon = fileType => {
  const type = fileType?.toLowerCase() || 'document'
  const iconMap = {
    image: Picture,
    video: VideoPlay,
    audio: DocumentCopy,
    zip: Folder,
    rar: Folder,
    '7z': ZipFile,
    pdf: Document,
    doc: Document,
    docx: Document,
    xls: Document,
    xlsx: Document,
    ppt: Document,
    pptx: Document,
  }
  return iconMap[type] || Document
}

// 使用工具函数格式化时间
const formatTime = formatRelativeTime

const onSelect = file => {
  activeFileId.value = file.id
  emit('select', file)
}

const handleDownload = file => {
  if (file.url) {
    const link = document.createElement('a')
    link.href = file.url
    link.download = file.name || 'file'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('下载开始')
  }
}

const handleUpload = () => {
  ElMessage.info('上传文件功能开发中...')
  // TODO: 实现文件上传逻辑
}

const handleFileAction = (command, file) => {
  switch (command) {
    case 'preview':
      window.open(file.url, '_blank')
      break
    case 'share':
      ElMessage.info(`分享文件: ${file.name}`)
      // TODO: 实现分享逻辑
      break
    case 'delete':
      store.dispatch('im/deleteFile', file.id)
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.files-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $bg-white;

  .files-items {
    flex: 1;
    overflow-y: auto;
    position: relative;
    @include custom-scrollbar(6px, $border-dark);

    .files-list-inner {
      min-height: 100%;
    }

    .file-item {
      display: flex;
      align-items: center;
      padding: $spacing-md;
      cursor: pointer;
      position: relative;
      transition: all $transition-base $ease-out;
      border-left: 3px solid transparent;

      &:hover {
        background-color: $bg-hover;
        transform: translateX(2px);

        .file-actions {
          opacity: 1;
          transform: translateX(0);
        }
      }

      &.active {
        background-color: $primary-color-light;
        border-left-color: $primary-color;

        .file-name {
          color: $primary-color;
        }
      }

      &.collapsed {
        justify-content: center;
        padding: $spacing-md;

        .file-icon {
          margin-right: 0;
        }
      }

      .file-icon {
        position: relative;
        margin-right: $spacing-md;
        flex-shrink: 0;
        transition: margin-right $transition-base $ease-base;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: $border-radius-sm;
        background: linear-gradient(135deg, $primary-color-light 0%, $primary-color-lighter 100%);
        color: $primary-color;
        transition: all $transition-base $ease-base;

        &:hover {
          transform: scale(1.1);
          background: linear-gradient(135deg, $primary-color 0%, $primary-color-light 100%);
          color: #fff;
        }
      }

      .file-info {
        flex: 1;
        min-width: 0;
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .file-name {
          font-size: 14px;
          font-weight: 600;
          color: $text-primary;
          @include text-ellipsis;
          margin-bottom: 4px;
          transition: color $transition-base $ease-base;
        }

        .file-meta {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          font-size: 12px;
          color: $text-tertiary;

          .file-size {
            padding-right: $spacing-xs;
            border-right: 1px solid $border-light;
          }
        }
      }

      .file-actions {
        display: flex;
        gap: 4px;
        margin-left: $spacing-sm;
        opacity: 0;
        transform: translateX(10px);
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        :deep(.el-button) {
          color: $text-tertiary;
          transition: all $transition-base $ease-base;

          &:hover {
            color: $primary-color;
            background-color: $bg-hover;
          }
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px $spacing-xl;
      color: $text-tertiary;

      .empty-icon {
        font-size: 64px;
        color: $border-dark;
        margin-bottom: $spacing-lg;
      }

      .empty-text {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: $spacing-md;
      }

      :deep(.el-button) {
        margin-top: $spacing-md;
      }
    }
  }
}

.file-list-enter-active,
.file-list-leave-active {
  transition: all 0.3s ease;
}

.file-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.file-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.file-list-move {
  transition: transform 0.3s ease;
}

.empty-state-enter-active,
.empty-state-leave-active {
  transition: all 0.3s ease;
}

.empty-state-enter-from,
.empty-state-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: $breakpoint-md) {
  .files-list {
    .files-items {
      .file-item {
        padding: $spacing-md $spacing-sm;

        .file-icon {
          width: 36px;
          height: 36px;
        }

        .file-info {
          .file-name {
            font-size: 13px;
          }

          .file-meta {
            font-size: 11px;
          }
        }

        .file-actions {
          :deep(.el-button) {
            width: 24px;
            height: 24px;
          }
        }
      }
    }
  }
}
</style>
