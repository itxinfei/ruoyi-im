<template>
  <div class="message-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2>消息管理</h2>
        <p class="page-desc">管理系统消息内容、敏感词过滤和消息统计</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Setting" @click="handleOpenSensitiveConfig">敏感词配置</el-button>
        <el-button :icon="Download" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索消息内容"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="searchForm.messageType" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="文本" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文件" value="FILE" />
            <el-option label="语音" value="VOICE" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="会话类型">
          <el-select v-model="searchForm.conversationType" placeholder="全部" clearable style="width: 120px">
            <el-option label="单聊" value="PRIVATE" />
            <el-option label="群聊" value="GROUP" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送者">
          <el-input
            v-model="searchForm.sender"
            placeholder="输入用户ID/昵称"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" value="normal" />
            <el-option label="已撤回" value="revoked" />
            <el-option label="已删除" value="deleted" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <div v-if="selectedMessages.length > 0" class="batch-actions-bar">
      <div class="batch-info">
        <el-icon class="batch-icon"><Select /></el-icon>
        <span>已选择 <strong>{{ selectedMessages.length }}</strong> 条消息</span>
      </div>
      <div class="batch-buttons">
        <el-button type="danger" size="small" :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button size="small" @click="handleClearSelection">取消选择</el-button>
      </div>
    </div>

    <!-- 数据表格卡片 -->
    <el-card class="table-card" shadow="never">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="messageList"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" fixed="left" />
        <el-table-column prop="id" label="消息ID" width="90" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <div class="type-badge" :class="'type-badge--' + row.messageType.toLowerCase()">
              <el-icon>
                <ChatLineSquare v-if="row.messageType === 'TEXT'" />
                <Picture v-else-if="row.messageType === 'IMAGE'" />
                <Folder v-else-if="row.messageType === 'FILE'" />
                <Microphone v-else-if="row.messageType === 'VOICE'" />
                <VideoCamera v-else-if="row.messageType === 'VIDEO'" />
                <Bell v-else />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="消息内容" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="message-content-cell">
              <span v-if="row.messageType === 'TEXT'" class="text-content">{{ row.content }}</span>
              <span v-else-if="row.messageType === 'IMAGE'" class="media-content">
                <el-image
                  v-if="row.fileUrl"
                  :src="row.fileUrl"
                  :preview-src-list="[row.fileUrl]"
                  fit="cover"
                  class="message-thumb"
                />
                <span class="media-label">[图片]</span>
              </span>
              <span v-else-if="row.messageType === 'FILE'" class="file-content">
                <el-icon class="file-icon"><Document /></el-icon>
                <span>{{ row.fileName || '文件' }}</span>
              </span>
              <span v-else-if="row.messageType === 'VOICE'" class="voice-content">
                <el-icon><Microphone /></el-icon>
                <span>{{ row.duration ? row.duration + 's' : '[语音]' }}</span>
              </span>
              <span v-else-if="row.messageType === 'VIDEO'" class="video-content">
                <el-icon><VideoCamera /></el-icon>
                <span>[视频]</span>
              </span>
              <span v-else>[{{ row.messageType }}]</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="senderId" label="发送者" width="150">
          <template #default="{ row }">
            <div class="sender-cell">
              <el-avatar :size="28" :src="row.senderAvatar">
                {{ row.senderNickName ? row.senderNickName[0] : 'U' }}
              </el-avatar>
              <span class="sender-name">{{ row.senderNickName || row.senderId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="conversationType" label="会话类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.conversationType === 'PRIVATE'" size="small" type="info">单聊</el-tag>
            <el-tag v-else size="small" type="warning">群聊</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conversationId" label="会话ID" width="90" />
        <el-table-column prop="createTime" label="发送时间" width="160" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRevoked" size="small" type="info">已撤回</el-tag>
            <el-tag v-else-if="row.isDeleted" size="small" type="danger">已删除</el-tag>
            <el-tag v-else size="small" type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100, 200]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 消息详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="消息详情"
      size="450px"
      class="message-detail-drawer"
    >
      <div v-if="currentMessage" class="detail-content">
        <!-- 消息头部信息 -->
        <div class="detail-header">
          <el-avatar :size="56" :src="currentMessage.senderAvatar">
            {{ currentMessage.senderNickName ? currentMessage.senderNickName[0] : 'U' }}
          </el-avatar>
          <div class="header-info">
            <h3>{{ currentMessage.senderNickName || '未知用户' }}</h3>
            <p class="user-id">ID: {{ currentMessage.senderId }}</p>
          </div>
        </div>

        <!-- 消息内容展示 -->
        <div class="message-detail-section">
          <h4 class="section-title">消息内容</h4>
          <div v-if="currentMessage.messageType === 'TEXT'" class="text-message-detail">
            {{ currentMessage.content }}
          </div>
          <div v-else-if="currentMessage.messageType === 'IMAGE'" class="image-message-detail">
            <el-image
              v-if="currentMessage.fileUrl"
              :src="currentMessage.fileUrl"
              :preview-src-list="[currentMessage.fileUrl]"
              fit="contain"
              class="detail-image"
            />
            <p v-if="currentMessage.fileName" class="file-name">{{ currentMessage.fileName }}</p>
          </div>
          <div v-else-if="currentMessage.messageType === 'FILE'" class="file-message-detail">
            <div class="file-card">
              <el-icon class="file-card-icon" :size="40"><Document /></el-icon>
              <div class="file-card-info">
                <div class="file-card-name">{{ currentMessage.fileName || '未知文件' }}</div>
                <div class="file-card-size">{{ formatFileSize(currentMessage.fileSize) }}</div>
              </div>
              <el-button type="primary" link>下载</el-button>
            </div>
          </div>
          <div v-else-if="currentMessage.messageType === 'VOICE'" class="voice-message-detail">
            <el-icon class="voice-icon"><Microphone /></el-icon>
            <span>语音消息 {{ currentMessage.duration ? currentMessage.duration + '秒' : '' }}</span>
          </div>
          <div v-else-if="currentMessage.messageType === 'VIDEO'" class="video-message-detail">
            <el-icon class="video-icon"><VideoCamera /></el-icon>
            <span>视频消息</span>
          </div>
          <div v-else class="other-message-detail">
            {{ currentMessage.content || '[' + currentMessage.messageType + ']' }}
          </div>
        </div>

        <!-- 详细信息 -->
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="消息ID">{{ currentMessage.id }}</el-descriptions-item>
          <el-descriptions-item label="会话ID">{{ currentMessage.conversationId }}</el-descriptions-item>
          <el-descriptions-item label="会话类型">
            <el-tag v-if="currentMessage.conversationType === 'PRIVATE'" type="info" size="small">单聊</el-tag>
            <el-tag v-else type="warning" size="small">群聊</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息类型">
            <el-tag :type="getMessageTypeColor(currentMessage.messageType)" size="small">
              {{ getMessageTypeLabel(currentMessage.messageType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ currentMessage.createTime }}</el-descriptions-item>
          <el-descriptions-item label="消息状态">
            <el-tag v-if="currentMessage.isRevoked" type="info" size="small">已撤回</el-tag>
            <el-tag v-else-if="currentMessage.isDeleted" type="danger" size="small">已删除</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.isEdited" label="编辑状态">
            <el-tag type="warning" size="small">已编辑</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.replyToMessageId" label="回复消息ID">
            {{ currentMessage.replyToMessageId }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.forwardFromMessageId" label="转发来源ID">
            {{ currentMessage.forwardFromMessageId }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button type="danger" :icon="Delete" @click="handleDeleteFromDetail">删除消息</el-button>
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 敏感词配置对话框 -->
    <el-dialog
      v-model="sensitiveConfigVisible"
      title="敏感词配置"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="敏感词过滤"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        配置敏感词后，包含敏感词的消息将被拦截或替换。每行一个敏感词。
      </el-alert>

      <el-form :model="sensitiveForm" label-width="100px">
        <el-form-item label="过滤策略">
          <el-radio-group v-model="sensitiveForm.strategy">
            <el-radio label="reject">拦截消息</el-radio>
            <el-radio label="replace">替换为***</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="敏感词列表">
          <el-input
            v-model="sensitiveForm.words"
            type="textarea"
            :rows="8"
            placeholder="每行输入一个敏感词，支持中英文、数字"
          />
        </el-form-item>
        <el-form-item label="关键词数">
          <span class="word-count">{{ sensitiveWordCount }} 个敏感词</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="sensitiveConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSensitiveConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Setting,
  Download,
  Delete,
  Select,
  ChatLineSquare,
  Picture,
  Folder,
  Microphone,
  VideoCamera,
  Bell,
  Document
} from '@element-plus/icons-vue'
import {
  searchMessages,
  deleteMessage,
  batchDeleteMessages,
  getMessageDetail,
  getMessageAdminStats,
  getSensitiveWords,
  saveSensitiveWords
} from '@/api/admin'
import { formatFileSize } from '@/utils/format'

const loading = ref(false)
const messageList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedMessages = ref([])

// 搜索表单
const searchForm = ref({
  keyword: '',
  messageType: '',
  conversationType: '',
  sender: '',
  dateRange: [],
  status: ''
})

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentMessage = ref(null)

// 敏感词配置
const sensitiveConfigVisible = ref(false)
const sensitiveForm = ref({
  strategy: 'reject',
  words: ''
})

const tableRef = ref(null)

// 敏感词数量
const sensitiveWordCount = computed(() => {
  if (!sensitiveForm.value.words) return 0
  return sensitiveForm.value.words.split('\n').filter(w => w.trim()).length
})

// 加载消息列表
const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.value.keyword,
      messageType: searchForm.value.messageType,
      conversationType: searchForm.value.conversationType,
      sender: searchForm.value.sender,
      status: searchForm.value.status,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 添加时间范围
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startTime = searchForm.value.dateRange[0]
      params.endTime = searchForm.value.dateRange[1]
    }

    const res = await searchMessages(params)
    if (res.code === 200) {
      messageList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载消息列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadMessages()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    messageType: '',
    conversationType: '',
    sender: '',
    dateRange: [],
    status: ''
  }
  pageNum.value = 1
  loadMessages()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadMessages()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadMessages()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedMessages.value = selection
}

const handleClearSelection = () => {
  tableRef.value?.clearSelection()
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteMessage(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMessages.value.length} 条消息吗？删除后无法恢复。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const ids = selectedMessages.value.map(m => m.id)
    const res = await batchDeleteMessages(ids)
    if (res.code === 200) {
      ElMessage.success(`成功删除 ${res.data.successCount || ids.length} 条消息`)
      selectedMessages.value = []
      tableRef.value?.clearSelection()
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getMessageDetail(row.id)
    if (res.code === 200) {
      currentMessage.value = res.data
      detailDrawerVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载消息详情失败')
  }
}

// 从详情删除
const handleDeleteFromDetail = async () => {
  if (!currentMessage.value) return
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteMessage(currentMessage.value.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      detailDrawerVisible.value = false
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出数据
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 保存敏感词配置
const handleSaveSensitiveConfig = async () => {
  try {
    // 将敏感词按行分割并过滤空行
    const words = sensitiveForm.value.words
      .split('\n')
      .map(w => w.trim())
      .filter(w => w.length > 0)

    const res = await saveSensitiveWords({
      strategy: sensitiveForm.value.strategy,
      words: words
    })

    if (res.code === 200) {
      ElMessage.success('敏感词配置已保存')
      sensitiveConfigVisible.value = false
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 打开敏感词配置对话框
const handleOpenSensitiveConfig = async () => {
  try {
    const res = await getSensitiveWords()
    if (res.code === 200 && res.data) {
      sensitiveForm.value.strategy = res.data.strategy || 'reject'
      sensitiveForm.value.words = (res.data.words || []).join('\n')
    }
    sensitiveConfigVisible.value = true
  } catch (error) {
    // 加载失败也打开对话框，使用默认值
    sensitiveForm.value = { strategy: 'reject', words: '' }
    sensitiveConfigVisible.value = true
  }
}

const getMessageTypeLabel = (type) => {
  const labels = {
    TEXT: '文本',
    IMAGE: '图片',
    FILE: '文件',
    VOICE: '语音',
    VIDEO: '视频',
    SYSTEM: '系统'
  }
  return labels[type] || type
}

const getMessageTypeColor = (type) => {
  const colors = {
    TEXT: 'primary',
    IMAGE: 'success',
    FILE: 'warning',
    VOICE: 'info',
    VIDEO: 'danger',
    SYSTEM: ''
  }
  return colors[type] || ''
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
/* 引入主题变量 */
@import '@/styles/admin-theme.css';

/* ================================
   页面容器
   ================================ */
.message-management {
  padding: 0;
}

/* ================================
   页面头部
   ================================ */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--dt-space-md);
}

.page-title h2 {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

.page-actions {
  display: flex;
  gap: var(--dt-space-sm);
}

/* ================================
   搜索卡片
   ================================ */
.search-card {
  margin-bottom: var(--dt-space-md);
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.search-card :deep(.el-card__body) {
  padding: var(--dt-space-md);
}

.search-card :deep(.el-form-item) {
  margin-bottom: var(--dt-space-sm);
  margin-right: var(--dt-space-md);
}

.search-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.search-card :deep(.el-form-item__label) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  font-weight: var(--dt-font-weight-normal);
}

/* ================================
   批量操作栏
   ================================ */
.batch-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--dt-space-sm) var(--dt-space-md);
  margin-bottom: var(--dt-space-md);
  background: var(--dt-primary);
  border-radius: var(--dt-radius-sm);
  color: white;
}

.batch-info {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  font-size: var(--dt-font-size-base);
}

.batch-icon {
  font-size: 18px;
}

.batch-buttons {
  display: flex;
  gap: var(--dt-space-sm);
}

.batch-buttons .el-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.batch-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.batch-buttons .el-button--danger {
  background: var(--dt-error);
  border-color: var(--dt-error);
}

.batch-buttons .el-button--danger:hover {
  background: var(--dt-error-light);
}

/* ================================
   表格卡片
   ================================ */
.table-card {
  border-radius: var(--dt-card-radius);
  border: 1px solid var(--dt-card-border);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

/* 表格样式 */
.table-card :deep(.el-table) {
  border: none;
}

.table-card :deep(.el-table__header-wrapper) {
  background-color: var(--dt-table-header-bg);
}

.table-card :deep(.el-table th) {
  background-color: var(--dt-table-header-bg);
  color: var(--dt-table-header-text);
  font-weight: var(--dt-font-weight-medium);
  border-bottom: 1px solid var(--dt-table-border);
}

.table-card :deep(.el-table tr:hover > td) {
  background-color: var(--dt-table-row-hover);
}

.table-card :deep(.el-table td) {
  border-bottom: 1px solid var(--dt-border-lighter);
}

/* 消息类型徽章 */
.type-badge {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 16px;
  color: white;
}

.type-badge--text {
  background: linear-gradient(135deg, #0089FF 0%, #0066CC 100%);
}

.type-badge--image {
  background: linear-gradient(135deg, #00C853 0%, #009624 100%);
}

.type-badge--file {
  background: linear-gradient(135deg, #FFAB00 0%, #FF8F00 100%);
}

.type-badge--voice {
  background: linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%);
}

.type-badge--video {
  background: linear-gradient(135deg, #FF3D00 0%, #D50000 100%);
}

.type-badge--system {
  background: linear-gradient(135deg, #607D8B 0%, #455A64 100%);
}

/* 消息内容单元格 */
.message-content-cell {
  display: flex;
  align-items: center;
}

.text-content {
  color: var(--dt-text-regular);
  line-height: 1.5;
}

.media-content {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.message-thumb {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-sm);
}

.media-label {
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);
}

.file-content {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  color: var(--dt-text-regular);
}

.file-icon {
  color: var(--dt-warning);
  font-size: 18px;
}

.voice-content,
.video-content {
  display: flex;
  align-items: center;
  gap: var(--dt-space-xs);
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);
}

/* 发送者单元格 */
.sender-cell {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
}

.sender-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   详情抽屉
   ================================ */
.detail-content {
  padding: var(--dt-space-md);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-space-md);
}

.header-info h3 {
  font-size: var(--dt-font-size-md);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.user-id {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  margin: 0;
}

/* 消息详情区块 */
.message-detail-section {
  margin-bottom: var(--dt-space-md);
}

.section-title {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin: 0 0 var(--dt-space-sm) 0;
}

.text-message-detail {
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-regular);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.image-message-detail {
  text-align: center;
}

.detail-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: var(--dt-radius-md);
}

.file-name {
  margin-top: var(--dt-space-sm);
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);
}

.file-message-detail .file-card {
  display: flex;
  align-items: center;
  gap: var(--dt-space-md);
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
}

.file-card-icon {
  color: var(--dt-warning);
  flex-shrink: 0;
}

.file-card-info {
  flex: 1;
}

.file-card-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.file-card-size {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.voice-message-detail,
.video-message-detail {
  display: flex;
  align-items: center;
  gap: var(--dt-space-sm);
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-regular);
}

.voice-icon,
.video-icon {
  font-size: 24px;
  color: var(--dt-primary);
}

.other-message-detail {
  padding: var(--dt-space-md);
  background: var(--dt-bg-page);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
}

/* 详情描述列表 */
.detail-descriptions {
  margin-bottom: var(--dt-space-md);
}

.detail-descriptions :deep(.el-descriptions__label) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.detail-descriptions :deep(.el-descriptions__content) {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-regular);
}

/* 详情操作按钮 */
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--dt-space-sm);
  padding-top: var(--dt-space-md);
  border-top: 1px solid var(--dt-divider);
}

/* ================================
   敏感词配置
   ================================ */
.word-count {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

/* ================================
   Element Plus 样式覆盖
   ================================ */

/* 按钮 link 样式 */
:deep(.el-button--link.el-button--primary) {
  color: var(--dt-primary);
}

:deep(.el-button--link.el-button--primary:hover) {
  color: var(--dt-primary-light);
}

:deep(.el-button--link.el-button--danger) {
  color: var(--dt-error);
}

:deep(.el-button--link.el-button--danger:hover) {
  color: var(--dt-error-light);
}

/* Tag 样式优化 */
:deep(.el-tag) {
  border-radius: var(--dt-radius-sm);
}

/* Avatar 样式 */
:deep(.el-avatar) {
  background-color: var(--dt-primary);
  color: white;
  font-size: var(--dt-font-size-sm);
}

/* Input 图标颜色 */
:deep(.el-input__prefix) {
  color: var(--dt-text-placeholder);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--dt-space-sm);
  }

  .page-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .batch-actions-bar {
    flex-direction: column;
    gap: var(--dt-space-sm);
    align-items: flex-start;
  }

  .search-card :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }
}
</style>
