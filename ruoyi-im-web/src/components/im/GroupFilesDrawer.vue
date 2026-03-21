<template>
  <el-drawer
    v-model="visible"
    title="群文件"
    size="360px"
    :with-header="false"
    class="group-files-drawer"
  >
    <!-- Header区 (Doc-21 规范) -->
    <header class="drawer-header">
      <div class="header-left">
        <i class="icon-back" @click="closeDrawer" />
        <span class="title">群文件</span>
      </div>
      <div class="header-right">
        <!-- 仅支持简单文件上传组件 -->
        <el-upload
          action="#"
          :show-file-list="false"
          :http-request="processUpload"
          :disabled="isUploading"
        >
          <i v-if="!isUploading" class="icon-upload" title="上传文件" />
          <i v-else class="el-icon-loading" title="上传中" />
        </el-upload>
      </div>
    </header>

    <div class="drawer-content">
      <!-- 搜索区 (Doc-21 §5.1 同构) -->
      <div class="search-section">
        <div class="search-bar" :class="{ 'is-focused': isSearchFocused }">
          <i class="icon-search" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索文件"
            @focus="isSearchFocused = true"
            @blur="isSearchFocused = false"
            @keyup.enter="processSearch"
          >
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <span class="stats-text">共 {{ fileStats.totalCount || 0 }} 个文件，已使用 {{ formatSize(fileStats.totalSize || 0) }}</span>
      </div>

      <!-- 分类 Tab -->
      <div class="tab-section">
        <span
          v-for="tab in ['全部', '文档', '图片', '媒体', '其他']"
          :key="tab"
          class="tab-item"
          :class="{ 'active': activeTab === tab }"
          @click="switchTab(tab)"
        >
          {{ tab }}
        </span>
      </div>

      <!-- 文件列表区 -->
      <div v-loading="isLoading" class="file-list">
        <div v-if="fileList.length === 0" class="empty-state">
          暂无文件
        </div>

        <div
          v-for="file in fileList"
          :key="file.id"
          class="file-item"
        >
          <div class="file-icon">
            <i class="icon-file-generic" />
          </div>
          <div class="file-info">
            <div class="file-name" :title="file.fileName">
              {{ file.fileName }}
            </div>
            <div class="file-meta">
              <span class="file-size">{{ formatSize(file.fileSize) }}</span>
              <span class="file-uploader">{{ file.uploaderName }}</span>
              <span class="file-time">{{ formatDate(file.createTime) }}</span>
            </div>
          </div>
          <div class="file-actions">
            <i class="icon-download" title="下载" @click="processDownload(file)" />
            <i class="icon-delete" title="删除" @click="processDelete(file)" />
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="js">
/**
 * GroupFilesDrawer.vue
 * 严格对齐 Doc-21 业务抽屉规范与 8px 视觉律动
 */
import { ref, computed, watch } from 'vue'
import { groupFileApi } from '@/api/groupFile'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const closeDrawer = () => {
  visible.value = false
}

// 状态定义 (Doc-02: 禁止 data/info 命名)
const isSearchFocused = ref(false)
const searchKeyword = ref('')
const activeTab = ref('全部')
const isLoading = ref(false)
const isUploading = ref(false)
const fileList = ref([])
const fileStats = ref({ totalCount: 0, totalSize: 0 })

// 模拟加载逻辑
const fetchFileList = async () => {
  if (!props.groupId) return
  isLoading.value = true
  try {
    const payload = {
      groupId: props.groupId,
      category: activeTab.value === '全部' ? null : activeTab.value,
      keyword: searchKeyword.value,
      pageNum: 1,
      pageSize: 50
    }
    const res = await groupFileApi.getList(payload)
    fileList.value = res.data?.records || res.data || []
  } catch (e) {
    console.error('获取群文件列表失败:', e)
    fileList.value = []
  } finally {
    isLoading.value = false
  }
}

const fetchStats = async () => {
  if (!props.groupId) return
  try {
    const res = await groupFileApi.getStatistics(props.groupId)
    fileStats.value = res.data || { totalCount: 0, totalSize: 0 }
  } catch (e) {
    console.error('获取群文件统计失败:', e)
    fileStats.value = { totalCount: 0, totalSize: 0 }
  }
}

watch(visible, (newVal) => {
  if (newVal) {
    fetchFileList()
    fetchStats()
  }
})

const processSearch = () => {
  fetchFileList()
}

const switchTab = (tab) => {
  activeTab.value = tab
  fetchFileList()
}

const processUpload = async (options) => {
  isUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('groupId', props.groupId)
    formData.append('category', '文档') // 简化处理

    await groupFileApi.upload(formData)

    ElMessage.success('文件上传成功')
    fetchFileList()
    fetchStats()
  } catch (e) {
    ElMessage.error('上传失败: ' + (e.message || '未知错误'))
  } finally {
    isUploading.value = false
  }
}

const processDownload = async (file) => {
  try {
    const res = await groupFileApi.download(file.id)
    const fileUrl = res.data?.fileUrl || res.data
    if (fileUrl) {
      window.open(fileUrl, '_blank')
    }
    ElMessage.success(`开始下载: ${file.fileName}`)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

const processDelete = (file) => {
  ElMessageBox.confirm(`确定删除文件 "${file.fileName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await groupFileApi.delete(file.id)
      ElMessage.success('文件已删除')
      fetchFileList()
      fetchStats()
    } catch (e) {
      ElMessage.error('删除失败: ' + (e.message || '未知错误'))
    }
  })
}

// 工具函数
const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (timestamp) => {
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}-${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.drawer-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: var(--dt-line-width) solid var(--dt-border-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-back {
  cursor: pointer;
  color: var(--dt-text-desc);
  font-size: 16px;
}

.icon-back:hover {
  color: var(--dt-brand-color);
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-main);
}

.header-right .icon-upload {
  cursor: pointer;
  color: var(--dt-brand-color);
  font-size: 18px;
}

.drawer-content {
  height: calc(100% - 60px);
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
}

/* 搜索区 */
.search-section {
  padding: 16px 20px 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 32px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-s);
  padding: 0 8px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.search-bar.is-focused {
  background-color: var(--dt-bg-body);
  border-color: var(--dt-brand-color);
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  margin-left: 6px;
  width: 100%;
  font-size: 12px;
  color: var(--dt-text-main);
}

/* 统计与 Tab */
.stats-section {
  padding: 0 20px 8px;
  font-size: 12px;
  color: var(--dt-text-desc);
}

.tab-section {
  display: flex;
  padding: 0 20px;
  border-bottom: var(--dt-line-width) solid var(--dt-border-color);
}

.tab-item {
  font-size: 14px;
  color: var(--dt-text-main);
  padding: 8px 12px;
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: var(--dt-brand-color);
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 12px;
  right: 12px;
  height: 2px;
  background-color: var(--dt-brand-color);
}

/* 列表区 */
.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.empty-state {
  text-align: center;
  color: var(--dt-text-desc);
  font-size: 14px;
  margin-top: 40px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.file-item:hover {
  background-color: var(--dt-bg-hover);
}

.file-item:hover .file-actions {
  opacity: 1;
}

.file-icon {
  width: 40px;
  height: 40px;
  background-color: #F2F3F5;
  border-radius: var(--dt-radius-s);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: var(--dt-text-desc);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.file-name {
  font-size: 14px;
  color: var(--dt-text-main);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 12px;
  color: var(--dt-text-desc);
  display: flex;
  gap: 12px;
}

.file-actions {
  display: flex;
  gap: 12px;
  opacity: 0; /* Hover 显示 */
  transition: opacity 0.2s;
}

.file-actions i {
  color: var(--dt-text-desc);
  cursor: pointer;
  font-size: 16px;
}

.file-actions i:hover {
  color: var(--dt-brand-color);
}
</style>
