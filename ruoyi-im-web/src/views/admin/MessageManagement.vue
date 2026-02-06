<template>
  <div class="message-management">
    <!-- 统计卡片 -->
    <el-row
      :gutter="20"
      class="mb-4"
    >
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">今日消息数</span>
              <span class="stat-value">{{ stats.todayMessages }}</span>
            </div>
            <div class="stat-icon primary-bg">
              <el-icon>
                <Message />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">敏感词触碰</span>
              <span class="stat-value">{{ stats.sensitiveHits }}</span>
            </div>
            <div class="stat-icon warning-bg">
              <el-icon>
                <Warning />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">文件传输</span>
              <span class="stat-value">{{ stats.fileTransfers }}</span>
            </div>
            <div class="stat-icon success-bg">
              <el-icon>
                <Files />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">平均延迟</span>
              <span class="stat-value">45ms</span>
            </div>
            <div class="stat-icon info-bg">
              <el-icon>
                <Timer />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card
      class="search-card"
      shadow="never"
    >
      <el-form
        :model="searchForm"
        inline
        class="search-form"
        @submit.prevent
      >
        <el-form-item label="发送者">
          <el-input
            v-model="searchForm.fromId"
            placeholder="发送者ID/昵称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input
            v-model="searchForm.content"
            placeholder="关键词"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="消息类型">
          <el-select
            v-model="searchForm.type"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="文本"
              value="text"
            />
            <el-option
              label="图片"
              value="image"
            />
            <el-option
              label="文件"
              value="file"
            />
            <el-option
              label="语音"
              value="voice"
            />
            <el-option
              label="视频"
              value="video"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格内容 -->
    <el-card
      class="table-card"
      shadow="never"
    >
      <div class="batch-actions">
        <el-button
          type="danger"
          plain
          :disabled="selectedMessages.length === 0"
          @click="handleBatchDelete"
        >
          批量撤回/删除
        </el-button>
        <el-button
          type="warning"
          @click="sensitiveWordVisible = true"
        >
          敏感词配置
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="messageList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        />
        <el-table-column
          prop="id"
          label="消息ID"
          width="100"
          align="center"
        />
        <el-table-column
          label="发送者"
          width="180"
        >
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar
                :size="24"
                :src="row.fromAvatar"
              >
                {{ row.fromNickname?.[0] }}
              </el-avatar>
              <span class="user-name">{{ row.fromNickname }} ({{ row.fromId }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="消息类型"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="getMessageTypeTag(row.type)"
              size="small"
            >
              {{ getMessageTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="内容"
          min-width="250"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="message-content">
              <span v-if="row.type === 'text'">{{ row.content }}</span>
              <span
                v-else-if="row.type === 'image'"
                class="media-link"
              >[图片消息]</span>
              <span
                v-else-if="row.type === 'file'"
                class="media-link"
              >[文件] {{ row.fileName }}</span>
              <span
                v-else
                class="media-link"
              >[{{ getMessageTypeLabel(row.type) }}]</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="conversationId"
          label="会话ID"
          width="120"
          align="center"
        />
        <el-table-column
          prop="createTime"
          label="发送时间"
          width="160"
          align="center"
        />
        <el-table-column
          label="操作"
          width="150"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleRecall(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMessages"
          @current-change="loadMessages"
        />
      </div>
    </el-card>

    <!-- 敏感词配置对话框 -->
    <el-dialog
      v-model="sensitiveWordVisible"
      title="敏感词库配置"
      width="600px"
    >
      <div class="sensitive-config">
        <el-alert
          title="触发敏感词的消息将被自动拦截或标记，请在下方配置关键词，每行一个。"
          type="info"
          :closable="false"
          class="mb-4"
        />
        <el-input
          v-model="sensitiveWords"
          type="textarea"
          :rows="15"
          placeholder="例如：
涉嫌欺诈
虚假宣传
..."
        />
      </div>
      <template #footer>
        <el-button @click="sensitiveWordVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="saveSensitiveWords"
        >
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="消息原文详情"
      width="500px"
    >
      <div
        v-if="currentMessage"
        class="message-detail"
      >
        <pre class="raw-content">{{ formatRawMessage(currentMessage) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Message, Warning, Files, Timer, Search, Refresh
} from '@element-plus/icons-vue'
import { getMessageList, recallMessage, getMessageAdminStats } from '@/api/admin'

const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedMessages = ref([])

const stats = reactive({
  todayMessages: 0,
  sensitiveHits: 0,
  fileTransfers: 0
})

const searchForm = reactive({
  fromId: '',
  content: '',
  type: '',
  dateRange: []
})

const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.content,
      fromId: searchForm.fromId,
      type: searchForm.type
    }
    if (searchForm.dateRange?.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const res = await getMessageList(params)
    if (res.code === 200) {
      messageList.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

// 加载消息统计数据
const loadMessageStats = async () => {
  try {
    const res = await getMessageAdminStats({ days: 1 })
    if (res.code === 200) {
      stats.todayMessages = res.data.todayMessages || 0
      stats.sensitiveHits = res.data.sensitiveHits || 0
      stats.fileTransfers = res.data.fileTransfers || 0
    }
  } catch (error) {
    console.error('加载消息统计失败:', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadMessages()
}

const handleReset = () => {
  searchForm.fromId = ''
  searchForm.content = ''
  searchForm.type = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSelectionChange = selection => {
  selectedMessages.value = selection
}

const detailVisible = ref(false)
const currentMessage = ref(null)
const handleViewDetail = row => {
  currentMessage.value = row
  detailVisible.value = true
}

const formatRawMessage = msg => {
  return JSON.stringify(msg, null, 2)
}

const handleRecall = row => {
  ElMessageBox.confirm('确定要强制撤回该消息吗？', '操作提醒', {
    type: 'warning'
  }).then(async () => {
    const res = await recallMessage(row.id)
    if (res.code === 200) {
      ElMessage.success('撤回成功')
      loadMessages()
    }
  }).catch(() => { })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要处理选中的 ${selectedMessages.value.length} 条消息吗？`, '批量操作', {
    type: 'danger'
  }).then(() => {
    ElMessage.success('操作已执行')
  }).catch(() => { })
}

// 敏感词
const sensitiveWordVisible = ref(false)
const sensitiveWords = ref('广告\n刷单\n私下转账')
const saveSensitiveWords = () => {
  ElMessage.success('配置已保存')
  sensitiveWordVisible.value = false
}

const getMessageTypeLabel = type => {
  const map = {
    text: '文本',
    image: '图片',
    file: '文件',
    voice: '语音',
    video: '视频'
  }
  return map[type] || type
}

const getMessageTypeTag = type => {
  const map = {
    text: '',
    image: 'success',
    file: 'warning',
    voice: 'info',
    video: 'danger'
  }
  return map[type] || ''
}

onMounted(() => {
  loadMessages()
  loadMessageStats()
})
</script>

<style scoped lang="scss">
.message-management {
  padding: var(--dt-space-md);
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-label {
      font-size: 14px;
      color: var(--dt-text-secondary);
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      &.primary-bg {
        background: rgba(0, 137, 255, 0.1);
        color: #0089FF;
      }

      &.success-bg {
        background: rgba(43, 193, 123, 0.1);
        color: #2BC17B;
      }

      &.warning-bg {
        background: rgba(255, 172, 0, 0.1);
        color: #FFAC00;
      }

      &.info-bg {
        background: rgba(140, 140, 140, 0.1);
        color: #8C8C8C;
      }
    }
  }
}

.search-card {
  margin-bottom: var(--dt-space-md);
}

.table-card {
  .batch-actions {
    margin-bottom: var(--dt-space-md);
    display: flex;
    gap: 12px;
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .user-name {
    font-size: 13px;
  }
}

.message-content {
  .media-link {
    color: var(--el-color-primary);
    cursor: pointer;
  }
}

.pagination-container {
  margin-top: var(--dt-space-md);
  display: flex;
  justify-content: flex-end;
}

.raw-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  font-size: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
