<template>
  <div class="admin-page message-management">
    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <h3>消息管理</h3>
            <p>按类型、时间与关键字检索并治理消息</p>
          </div>
          <el-space>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="loadMessages">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="12" class="toolbar-row">
        <el-col :xs="24" :sm="10" :md="8" :lg="6">
          <el-input v-model="searchKeyword" placeholder="搜索消息内容" clearable @keyup.enter="handleSearch" @clear="handleSearch">
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6" :lg="4">
          <el-select v-model="searchMessageType" placeholder="消息类型" clearable style="width: 100%" @change="handleSearch">
            <el-option label="文本" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文件" value="FILE" />
            <el-option label="语音" value="VOICE" />
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="24" :md="10" :lg="8">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @change="handleSearch"
          />
        </el-col>
      </el-row>

      <div v-if="selectedMessages.length" class="batch-actions">
        <span>已选择 {{ selectedMessages.length }} 条消息</span>
        <el-space>
          <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
          <el-button v-if="failedItems.length" size="small" @click="failedDialogVisible = true">查看失败项</el-button>
        </el-space>
      </div>

      <el-table :data="messageList" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="消息ID" width="88" />
        <el-table-column prop="senderId" label="发送者ID" width="98" />
        <el-table-column prop="messageType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.messageType)">{{ getTypeLabel(row.messageType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="消息内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">{{ renderPreview(row) }}</template>
        </el-table-column>
        <el-table-column prop="conversationId" label="会话ID" width="98" />
        <el-table-column prop="createTime" label="发送时间" width="180" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isRevoked === 1" type="info">已撤回</el-tag>
            <el-tag v-else-if="row.isDeleted === 1" type="danger">已删除</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="130">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.isRevoked !== 1 && row.isDeleted !== 1"
              size="small"
              type="danger"
              link
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="消息详情" width="640px">
      <el-descriptions :column="2" border v-if="currentMessage">
        <el-descriptions-item label="消息ID">{{ currentMessage.id }}</el-descriptions-item>
        <el-descriptions-item label="会话ID">{{ currentMessage.conversationId }}</el-descriptions-item>
        <el-descriptions-item label="发送者ID">{{ currentMessage.senderId }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">
          <el-tag :type="getTypeTag(currentMessage.messageType)">{{ getTypeLabel(currentMessage.messageType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间" :span="2">{{ currentMessage.createTime }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">
          <div class="message-content">{{ currentMessage.content || renderPreview(currentMessage) }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="failedDialogVisible" title="批量失败明细" width="520px">
      <el-table :data="failedItems" border>
        <el-table-column prop="id" label="消息ID" width="120" />
        <el-table-column prop="reason" label="失败原因" min-width="260" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { searchMessages, deleteMessage, batchDeleteMessages, getMessageDetail } from '@/api/admin'

const loading = ref(false)
const messageList = ref([])
const searchKeyword = ref('')
const searchMessageType = ref('')
const dateRange = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedMessages = ref([])
const detailDialogVisible = ref(false)
const currentMessage = ref(null)
const failedDialogVisible = ref(false)
const failedItems = ref([])

const getTypeLabel = (type) => {
  const map = {
    TEXT: '文本', IMAGE: '图片', FILE: '文件', VOICE: '语音', VIDEO: '视频'
  }
  return map[type] || type || '未知'
}

const getTypeTag = (type) => {
  const map = {
    TEXT: 'primary', IMAGE: 'success', FILE: 'warning', VOICE: 'info', VIDEO: 'danger'
  }
  return map[type] || 'info'
}

const renderPreview = (row) => {
  if (!row) return ''
  if (row.messageType === 'IMAGE') return `[图片] ${row.fileName || ''}`
  if (row.messageType === 'FILE') return `[文件] ${row.fileName || ''}`
  if (row.messageType === 'VOICE') return '[语音消息]'
  if (row.messageType === 'VIDEO') return `[视频] ${row.fileName || ''}`
  return row.content || ''
}

const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchKeyword.value,
      messageType: searchMessageType.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await searchMessages(params)
    if (res.code === 200) {
      messageList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadMessages()
}

const handleReset = () => {
  searchKeyword.value = ''
  searchMessageType.value = ''
  dateRange.value = []
  pageNum.value = 1
  loadMessages()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadMessages()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadMessages()
}

const handleSelectionChange = (selection) => {
  selectedMessages.value = selection
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteMessage(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (!selectedMessages.value.length) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedMessages.value.map(m => m.id)
    const res = await batchDeleteMessages(ids)
    if (res.code === 200) {
      const successCount = res.data?.successCount ?? ids.length
      const failedCount = res.data?.failedCount ?? 0
      failedItems.value = (res.data?.failedItems || []).map(item => ({
        id: item.id || item.messageId || '未知',
        reason: item.reason || item.msg || '未知原因'
      }))
      ElMessage.success(`批量删除完成：成功 ${successCount}，失败 ${failedCount}`)
      if (failedCount > 0) failedDialogVisible.value = true
      selectedMessages.value = []
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败')
  }
}

const handleViewDetail = async (row) => {
  try {
    const res = await getMessageDetail(row.id)
    if (res.code === 200) {
      currentMessage.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载消息详情失败')
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e6ebf3;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.toolbar-row {
  margin-bottom: 12px;
}

.batch-actions {
  margin-bottom: 12px;
  background: #f8fafc;
  border: 1px solid #e6ebf3;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 220px;
  overflow-y: auto;
}
</style>
