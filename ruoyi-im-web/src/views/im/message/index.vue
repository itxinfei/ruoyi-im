<template>
  <div class="app-container">
    <!-- 搜索工具栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="发送者" prop="senderId">
        <el-input
          v-model="queryParams.senderId"
          placeholder="请输入发送者ID"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收者" prop="receiverId">
        <el-input
          v-model="queryParams.receiverId"
          placeholder="请输入接收者ID"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="消息类型" prop="msgType">
        <el-select v-model="queryParams.msgType" placeholder="选择类型" clearable style="width: 120px">
          <el-option label="文本" value="text" />
          <el-option label="图片" value="image" />
          <el-option label="文件" value="file" />
          <el-option label="语音" value="voice" />
          <el-option label="视频" value="video" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Download" @click="handleExport">导出</el-button>
      </el-col>
    </el-row>

    <!-- 消息列表 -->
    <el-table
      v-loading="loading"
      :data="messageList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="消息ID" align="center" prop="msgId" width="100" />
      <el-table-column label="发送者" align="center" width="120">
        <template #default="scope">
          <div class="user-cell">
            <el-avatar :size="24" :src="scope.row.senderAvatar">
              {{ scope.row.senderName?.charAt(0) }}
            </el-avatar>
            <span>{{ scope.row.senderName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="接收者" align="center" width="120">
        <template #default="scope">
          <div class="user-cell">
            <el-avatar :size="24" :src="scope.row.receiverAvatar">
              {{ scope.row.receiverName?.charAt(0) }}
            </el-avatar>
            <span>{{ scope.row.receiverName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="消息类型" align="center" prop="msgType" width="100">
        <template #default="scope">
          <el-tag :type="getMsgTypeTag(scope.row.msgType)">
            {{ getMsgTypeLabel(scope.row.msgType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息内容" align="left" prop="content" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.msgType === 'text'">{{ scope.row.content }}</span>
          <span v-else-if="scope.row.msgType === 'image'" class="media-hint">[图片消息]</span>
          <span v-else-if="scope.row.msgType === 'file'" class="media-hint">[文件: {{ scope.row.fileName }}]</span>
          <span v-else-if="scope.row.msgType === 'voice'" class="media-hint">[语音消息]</span>
          <span v-else-if="scope.row.msgType === 'video'" class="media-hint">[视频消息]</span>
          <span v-else>{{ scope.row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" align="center" prop="sendTime" width="180" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'read' ? 'success' : 'info'">
            {{ scope.row.status === 'read' ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template #default="scope">
          <el-button link type="primary" :icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="消息详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="消息ID">{{ currentMessage.msgId }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{ getMsgTypeLabel(currentMessage.msgType) }}</el-descriptions-item>
        <el-descriptions-item label="发送者">{{ currentMessage.senderName }}</el-descriptions-item>
        <el-descriptions-item label="接收者">{{ currentMessage.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="发送时间" :span="2">{{ currentMessage.sendTime }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">
          <div v-if="currentMessage.msgType === 'text'">{{ currentMessage.content }}</div>
          <div v-else-if="currentMessage.msgType === 'image'">
            <el-image :src="currentMessage.mediaUrl" style="max-width: 300px" />
          </div>
          <div v-else>{{ currentMessage.content || currentMessage.fileName }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, Download, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式状态
const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dateRange = ref([])
const detailDialogVisible = ref(false)
const currentMessage = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  senderId: '',
  receiverId: '',
  msgType: '',
  startTime: '',
  endTime: '',
})

// 获取消息类型标签
const getMsgTypeLabel = (type) => {
  const labels = {
    text: '文本',
    image: '图片',
    file: '文件',
    voice: '语音',
    video: '视频',
  }
  return labels[type] || type
}

// 获取消息类型标签颜色
const getMsgTypeTag = (type) => {
  const tags = {
    text: '',
    image: 'success',
    file: 'warning',
    voice: 'info',
    video: 'danger',
  }
  return tags[type] || ''
}

// 获取消息列表
const getList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await request({
      url: '/api/im/message/list',
      method: 'get',
      params,
    })

    if (response.code === 200) {
      messageList.value = response.rows || []
      total.value = response.total || 0
    } else {
      // 模拟数据
      messageList.value = [
        {
          msgId: '1',
          senderId: '101',
          senderName: '张三',
          senderAvatar: '',
          receiverId: '102',
          receiverName: '李四',
          receiverAvatar: '',
          msgType: 'text',
          content: '你好，在吗？',
          sendTime: '2024-01-15 10:30:00',
          status: 'read',
        },
        {
          msgId: '2',
          senderId: '102',
          senderName: '李四',
          receiverId: '101',
          receiverName: '张三',
          msgType: 'image',
          content: '',
          mediaUrl: '',
          sendTime: '2024-01-15 10:31:00',
          status: 'unread',
        },
      ]
      total.value = 2
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    messageList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.senderId = ''
  queryParams.receiverId = ''
  queryParams.msgType = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  handleQuery()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

// 页码变化
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  getList()
}

// 多选框选中
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.msgId)
}

// 查看消息详情
const handleView = (row) => {
  currentMessage.value = row
  detailDialogVisible.value = true
}

// 删除消息
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('是否确认删除该消息？', '警告', { type: 'warning' })
    await request({
      url: `/api/im/message/${row.msgId}`,
      method: 'delete',
    })
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的消息')
    return
  }
  try {
    await ElMessageBox.confirm('是否确认删除选中的消息？', '警告', { type: 'warning' })
    await request({
      url: '/api/im/message/batch',
      method: 'delete',
      data: { ids: selectedIds.value },
    })
    ElMessage.success('批量删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 导出
const handleExport = async () => {
  try {
    const response = await request({
      url: '/api/im/message/export',
      method: 'get',
      params: queryParams,
      responseType: 'blob',
    })
    const blob = new Blob([response], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `消息记录_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .search-form {
    margin-bottom: 16px;
  }

  .mb8 {
    margin-bottom: 8px;
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .user-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .media-hint {
    color: #909399;
    font-style: italic;
  }
}
</style>
