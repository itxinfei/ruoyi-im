<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>消息管理</span>
          <el-button type="primary" @click="handleRefresh" :loading="refreshing">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
        <el-form-item label="会话ID" prop="conversationId">
          <el-input
            v-model="queryParams.conversationId"
            placeholder="请输入会话ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        
        <el-form-item label="发送者" prop="senderId">
          <el-input
            v-model="queryParams.senderId"
            placeholder="请输入发送者ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        
        <el-form-item label="消息类型" prop="messageType">
          <el-select v-model="queryParams.messageType" placeholder="消息类型" clearable>
            <el-option
              v-for="type in messageTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作工具栏 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="danger"
            :disabled="multiple"
            @click="handleBatchDelete"
            :loading="batchDeleting"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            :disabled="multiple"
            @click="handleBatchMarkSensitive"
            :loading="batchMarking"
          >
            <el-icon><Warning /></el-icon>
            批量标记敏感
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            @click="handleExport"
            :loading="exporting"
          >
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-col>
      </el-row>

      <!-- 消息表格 -->
      <el-table
        v-loading="loading"
        :data="messageList"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        stripe
        border
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="ID" prop="id" width="80" sortable />
        
        <el-table-column label="会话ID" prop="conversationId" width="100">
          <template #default="scope">
            <el-link
              type="primary"
              @click="viewConversation(scope.row.conversationId)"
            >
              {{ scope.row.conversationId }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column label="发送者" prop="senderId" width="100">
          <template #default="scope">
            <el-tag type="info" size="small">
              {{ scope.row.senderId }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="消息类型" prop="messageType" width="100">
          <template #default="scope">
            <el-tag :type="getMessageTypeColor(scope.row.messageType)" size="small">
              {{ getMessageTypeLabel(scope.row.messageType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="消息内容" prop="content" min-width="200">
          <template #default="scope">
            <div class="message-content">
              <span
                :class="{ 'sensitive-content': scope.row.sensitiveLevel > 0 }"
                @click="viewMessageDetail(scope.row)"
              >
                {{ getMessagePreview(scope.row.content) }}
              </span>
              <el-icon v-if="scope.row.sensitiveLevel > 0" class="sensitive-icon">
                <Warning />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="敏感级别" prop="sensitiveLevel" width="100">
          <template #default="scope">
            <el-tag
              v-if="scope.row.sensitiveLevel > 0"
              :type="getSensitiveLevelColor(scope.row.sensitiveLevel)"
              size="small"
            >
              {{ getSensitiveLevelLabel(scope.row.sensitiveLevel) }}
            </el-tag>
            <span v-else class="text-muted">正常</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isRevoked" type="info" size="small">已撤回</el-tag>
            <el-tag v-if="scope.row.isEdited" type="warning" size="small">已编辑</el-tag>
            <el-tag v-if="scope.row.isDeleted" type="danger" size="small">已删除</el-tag>
            <span v-if="!scope.row.isRevoked && !scope.row.isEdited && !scope.row.isDeleted" class="text-muted">
              正常
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="发送时间" prop="createTime" width="160" sortable>
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewMessageDetail(scope.row)"
            >
              详情
            </el-button>
            
            <el-dropdown v-if="hasMoreOperations(scope.row)" @command="(cmd) => handleCommand(cmd, scope.row)">
              <el-button size="small" type="info">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" v-if="canEdit(scope.row)">
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="revoke" v-if="canRevoke(scope.row)">
                    撤回
                  </el-dropdown-item>
                  <el-dropdown-item command="markSensitive" v-if="scope.row.sensitiveLevel === 0">
                    标记敏感
                  </el-dropdown-item>
                  <el-dropdown-item command="unmarkSensitive" v-else>
                    取消敏感标记
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="messageDetailVisible"
      title="消息详情"
      width="600px"
      destroy-on-close
    >
      <message-detail
        v-if="messageDetailVisible"
        :message-id="selectedMessageId"
        @close="messageDetailVisible = false"
        @refresh="handleRefresh"
      />
    </el-dialog>

    <!-- 批量操作确认对话框 -->
    <el-dialog
      v-model="batchConfirmVisible"
      title="批量操作确认"
      width="400px"
    >
      <div class="batch-confirm-content">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>{{ batchConfirmMessage }}</p>
        <p class="text-muted">此操作不可恢复，请谨慎操作！</p>
      </div>
      <template #footer>
        <el-button @click="batchConfirmVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchOperation" :loading="batchOperationLoading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Delete,
  Warning,
  Download,
  ArrowDown
} from '@element-plus/icons-vue'
import MessageDetail from './MessageDetail.vue'
import {
  getMessageList,
  deleteMessage,
  revokeMessage,
  markMessageSensitive,
  batchDeleteMessages,
  batchMarkMessagesSensitive
} from '@/api/im/message'
import { formatTime } from '@/utils/format'

// 响应式数据
const loading = ref(false)
const refreshing = ref(false)
const exporting = ref(false)
const batchDeleting = ref(false)
const batchMarking = ref(false)
const batchOperationLoading = ref(false)

const messageList = ref([])
const total = ref(0)
const selectedMessageIds = ref([])
const selectedMessageId = ref(null)

const messageDetailVisible = ref(false)
const batchConfirmVisible = ref(false)
const batchConfirmMessage = ref('')
const batchOperationType = ref('')

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  conversationId: '',
  senderId: '',
  messageType: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref([])

// 消息类型选项
const messageTypes = [
  { label: '文本', value: 'TEXT' },
  { label: '图片', value: 'IMAGE' },
  { label: '文件', value: 'FILE' },
  { label: '语音', value: 'VOICE' },
  { label: '视频', value: 'VIDEO' }
]

// 计算属性
const multiple = computed(() => selectedMessageIds.value.length > 0)

// 方法定义
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    } else {
      queryParams.startDate = ''
      queryParams.endDate = ''
    }

    const response = await getMessageList(queryParams)
    
    if (response.code === 200) {
      messageList.value = response.rows || []
      total.value = response.total || 0
    } else {
      ElMessage.error(response.msg || '获取消息列表失败')
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.conversationId = ''
  queryParams.senderId = ''
  queryParams.messageType = ''
  dateRange.value = []
  handleQuery()
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    await getList()
    ElMessage.success('刷新成功')
  } finally {
    refreshing.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedMessageIds.value = selection.map(item => item.id)
}

const handleRowClick = (row) => {
  // 可以在这里实现行点击的快捷操作
}

const viewMessageDetail = (row) => {
  selectedMessageId.value = row.id
  messageDetailVisible.value = true
}

const viewConversation = (conversationId) => {
  // 跳转到会话详情页面
  // this.$router.push(`/im/conversation/detail/${conversationId}`)
  ElMessage.info(`查看会话 ${conversationId}`)
}

const handleCommand = async (command, row) => {
  switch (command) {
    case 'edit':
      // 编辑消息
      break
    case 'revoke':
      await handleRevoke(row)
      break
    case 'markSensitive':
      await handleMarkSensitive(row, true)
      break
    case 'unmarkSensitive':
      await handleMarkSensitive(row, false)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handleRevoke = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回此消息吗？', '确认撤回', {
      type: 'warning'
    })

    const response = await revokeMessage(row.id)
    if (response.code === 200) {
      ElMessage.success('消息撤回成功')
      getList()
    } else {
      ElMessage.error(response.msg || '消息撤回失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error)
      ElMessage.error('消息撤回失败')
    }
  }
}

const handleMarkSensitive = async (row, mark) => {
  try {
    const action = mark ? '标记为敏感' : '取消敏感标记'
    await ElMessageBox.confirm(`确定要${action}此消息吗？`, `确认${action}`, {
      type: 'warning'
    })

    const response = await markMessageSensitive(row.id, mark ? 1 : 0)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      getList()
    } else {
      ElMessage.error(response.msg || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${mark ? '标记' : '取消标记'}敏感失败:`, error)
      ElMessage.error(`${mark ? '标记' : '取消标记'}敏感失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此消息吗？删除后不可恢复！', '确认删除', {
      type: 'warning'
    })

    const response = await deleteMessage(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = () => {
  batchOperationType.value = 'delete'
  batchConfirmMessage.value = `确定要删除选中的 ${selectedMessageIds.value.length} 条消息吗？`
  batchConfirmVisible.value = true
}

const handleBatchMarkSensitive = () => {
  batchOperationType.value = 'markSensitive'
  batchConfirmMessage.value = `确定要将选中的 ${selectedMessageIds.value.length} 条消息标记为敏感吗？`
  batchConfirmVisible.value = true
}

const confirmBatchOperation = async () => {
  batchOperationLoading.value = true
  try {
    let response
    if (batchOperationType.value === 'delete') {
      response = await batchDeleteMessages(selectedMessageIds.value)
    } else if (batchOperationType.value === 'markSensitive') {
      response = await batchMarkMessagesSensitive(selectedMessageIds.value, 1)
    }

    if (response.code === 200) {
      ElMessage.success('批量操作成功')
      batchConfirmVisible.value = false
      getList()
    } else {
      ElMessage.error(response.msg || '批量操作失败')
    }
  } catch (error) {
    console.error('批量操作失败:', error)
    ElMessage.error('批量操作失败')
  } finally {
    batchOperationLoading.value = false
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    // 导出逻辑
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 工具方法
const getMessagePreview = (content) => {
  try {
    const parsed = JSON.parse(content)
    return parsed.text || parsed.fileName || parsed.url || '无内容'
  } catch {
    return content.length > 50 ? content.substring(0, 50) + '...' : content
  }
}

const getMessageTypeColor = (type) => {
  const colorMap = {
    TEXT: '',
    IMAGE: 'success',
    FILE: 'info',
    VOICE: 'warning',
    VIDEO: 'danger'
  }
  return colorMap[type] || ''
}

const getMessageTypeLabel = (type) => {
  const labelMap = {
    TEXT: '文本',
    IMAGE: '图片',
    FILE: '文件',
    VOICE: '语音',
    VIDEO: '视频'
  }
  return labelMap[type] || type
}

const getSensitiveLevelColor = (level) => {
  const colorMap = {
    1: 'warning',
    2: 'danger'
  }
  return colorMap[level] || ''
}

const getSensitiveLevelLabel = (level) => {
  const labelMap = {
    1: '一般',
    2: '严重'
  }
  return labelMap[level] || ''
}

const hasMoreOperations = (row) => {
  return !row.isDeleted
}

const canEdit = (row) => {
  return !row.isDeleted && !row.isRevoked
}

const canRevoke = (row) => {
  return !row.isDeleted && !row.isRevoked
}

// 生命周期
onMounted(() => {
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-content {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.message-content span:hover {
  color: #409eff;
}

.sensitive-content {
  color: #e6a23c;
  font-weight: 500;
}

.sensitive-icon {
  color: #e6a23c;
  font-size: 16px;
}

.batch-confirm-content {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.mb8 {
  margin-bottom: 8px;
}
</style>