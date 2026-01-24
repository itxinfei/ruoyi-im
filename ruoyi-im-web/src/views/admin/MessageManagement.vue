<template>
  <div class="message-management">
    <el-card>
      <!-- 搜索栏 -->
      <el-row :gutter="20" class="search-bar">
        <el-col :span="5">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索消息内容"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchMessageType" placeholder="消息类型" clearable @change="handleSearch">
            <el-option label="文本" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文件" value="FILE" />
            <el-option label="语音" value="VOICE" />
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="4" :offset="6">
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>

      <!-- 消息列表 -->
      <el-table
        :data="messageList"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="消息ID" width="80" />
        <el-table-column prop="senderId" label="发送者ID" width="100" />
        <el-table-column prop="messageType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.messageType === 'TEXT'" type="primary">文本</el-tag>
            <el-tag v-else-if="row.messageType === 'IMAGE'" type="success">图片</el-tag>
            <el-tag v-else-if="row.messageType === 'FILE'" type="warning">文件</el-tag>
            <el-tag v-else-if="row.messageType === 'VOICE'" type="info">语音</el-tag>
            <el-tag v-else-if="row.messageType === 'VIDEO'" type="danger">视频</el-tag>
            <el-tag v-else type="info">{{ row.messageType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="消息内容" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.messageType === 'TEXT'">{{ row.content }}</span>
            <span v-else-if="row.messageType === 'IMAGE'">[图片] {{ row.fileName }}</span>
            <span v-else-if="row.messageType === 'FILE'">[文件] {{ row.fileName }}</span>
            <span v-else-if="row.messageType === 'VOICE'">[语音] {{ row.content }}</span>
            <span v-else-if="row.messageType === 'VIDEO'">[视频] {{ row.fileName }}</span>
            <span v-else>{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversationId" label="会话ID" width="100" />
        <el-table-column prop="createTime" label="发送时间" width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRevoked === 1" type="info">已撤回</el-tag>
            <el-tag v-else-if="row.isDeleted === 1" type="danger">已删除</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.isRevoked !== 1 && row.isDeleted !== 1"
              size="small"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedMessages.length > 0" class="batch-actions">
        <span class="selected-info">已选择 {{ selectedMessages.length }} 条消息</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="消息详情" width="600px">
      <el-descriptions :column="2" border v-if="currentMessage">
        <el-descriptions-item label="消息ID">{{ currentMessage.id }}</el-descriptions-item>
        <el-descriptions-item label="会话ID">{{ currentMessage.conversationId }}</el-descriptions-item>
        <el-descriptions-item label="发送者ID">{{ currentMessage.senderId }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">
          <el-tag v-if="currentMessage.messageType === 'TEXT'" type="primary">文本</el-tag>
          <el-tag v-else-if="currentMessage.messageType === 'IMAGE'" type="success">图片</el-tag>
          <el-tag v-else-if="currentMessage.messageType === 'FILE'" type="warning">文件</el-tag>
          <el-tag v-else-if="currentMessage.messageType === 'VOICE'" type="info">语音</el-tag>
          <el-tag v-else-if="currentMessage.messageType === 'VIDEO'" type="danger">视频</el-tag>
          <el-tag v-else type="info">{{ currentMessage.messageType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间" :span="2">{{ currentMessage.createTime }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">
          <div v-if="currentMessage.messageType === 'TEXT'" class="message-content">{{ currentMessage.content }}</div>
          <div v-else-if="currentMessage.messageType === 'IMAGE'">
            [图片] {{ currentMessage.fileName }}
            <el-image
              v-if="currentMessage.fileUrl"
              :src="currentMessage.fileUrl"
              style="width: 100px; height: 100px; margin-top: 10px"
              fit="cover"
              :preview-src-list="[currentMessage.fileUrl]"
            />
          </div>
          <div v-else-if="currentMessage.messageType === 'FILE'">
            [文件] {{ currentMessage.fileName }} ({{ formatFileSize(currentMessage.fileSize) }})
            <el-button v-if="currentMessage.fileUrl" type="primary" link size="small" style="margin-left: 10px">下载</el-button>
          </div>
          <div v-else>{{ currentMessage.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="是否撤回">
          <el-tag :type="currentMessage.isRevoked === 1 ? 'info' : 'success'">
            {{ currentMessage.isRevoked === 1 ? '已撤回' : '未撤回' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否删除">
          <el-tag :type="currentMessage.isDeleted === 1 ? 'danger' : 'success'">
            {{ currentMessage.isDeleted === 1 ? '已删除' : '正常' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  searchMessages,
  deleteMessage,
  batchDeleteMessages,
  getMessageDetail
} from '@/api/admin'

const loading = ref(false)
const messageList = ref([])
const searchKeyword = ref('')
const searchMessageType = ref('')
const dateRange = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedMessages = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentMessage = ref(null)

const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchKeyword.value,
      messageType: searchMessageType.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 添加时间范围
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
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadMessages()
}

const handleRefresh = () => {
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
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedMessages.value.map(m => m.id)
    const res = await batchDeleteMessages(ids)
    if (res.code === 200) {
      ElMessage.success(`成功删除 ${res.data.successCount} 条消息`)
      selectedMessages.value = []
      loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
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

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.selected-info {
  color: #606266;
  font-size: 14px;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
