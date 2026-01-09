<template>
  <div class="workbench-report">
    <!-- 头部 -->
    <div class="report-header">
      <h2>工作报告</h2>
      <el-button type="primary" :icon="Edit" @click="handleCreate">写报告</el-button>
    </div>

    <!-- 报告类型切换 -->
    <div class="report-tabs">
      <div
        v-for="tab in reportTabs"
        :key="tab.key"
        class="report-tab"
        :class="{ active: activeTab === tab.key }"
        @click="
          activeTab = tab.key
          fetchReports()
        "
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 报告列表 -->
    <div class="report-list">
      <div
        v-for="report in reports"
        :key="report.id"
        class="report-item"
        @click="handleViewReport(report)"
      >
        <div class="report-avatar">
          <el-avatar :size="40" :src="report.avatar">
            {{ report.userName?.charAt(0) || '?' }}
          </el-avatar>
        </div>
        <div class="report-content">
          <div class="report-header">
            <span class="report-title">{{ report.title }}</span>
            <span class="report-type" :type="report.type">{{ report.typeText }}</span>
          </div>
          <div class="report-summary">{{ report.summary }}</div>
          <div class="report-meta">
            <span class="report-author">{{ report.userName }}</span>
            <span class="report-time">{{ formatTime(report.createTime) }}</span>
            <div class="report-stats">
              <span
                ><el-icon><View /></el-icon> {{ report.viewCount }}</span
              >
              <span
                ><el-icon><ChatDotRound /></el-icon> {{ report.commentCount }}</span
              >
              <span v-if="report.likeCount > 0"
                ><el-icon><Star /></el-icon> {{ report.likeCount }}</span
              >
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="reports.length === 0" class="empty-state">
        <el-empty description="暂无报告，点击右上角创建" />
      </div>
    </div>

    <!-- 创建/编辑报告对话框 -->
    <el-dialog
      v-model="showReportDialog"
      :title="isEditMode ? '编辑报告' : '创建工作报告'"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportFormRules" label-width="80px">
        <el-form-item label="报告类型" prop="type">
          <el-select v-model="reportForm.type" placeholder="选择报告类型">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
          </el-select>
        </el-form-item>

        <el-form-item label="报告标题" prop="title">
          <el-input
            v-model="reportForm.title"
            placeholder="请输入报告标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="今日工作" prop="todayWork">
          <el-input
            v-model="reportForm.todayWork"
            type="textarea"
            :rows="4"
            placeholder="总结今天完成的工作..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="明日计划" prop="tomorrowPlan">
          <el-input
            v-model="reportForm.tomorrowPlan"
            type="textarea"
            :rows="3"
            placeholder="列出明天的工作计划..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题与风险" prop="issues">
          <el-input
            v-model="reportForm.issues"
            type="textarea"
            :rows="3"
            placeholder="工作中遇到的问题或风险..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            v-model:file-list="reportForm.attachments"
            action=""
            :auto-upload="false"
            multiple
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button :icon="Folder">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持上传附件，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="可见范围">
          <el-radio-group v-model="reportForm.visibility">
            <el-radio label="all">全部人</el-radio>
            <el-radio label="department">部门内</el-radio>
            <el-radio label="private">仅自己</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="reportForm.visibility !== 'private'" label="接收人">
          <el-select
            v-model="reportForm.recipients"
            multiple
            filterable
            placeholder="选择接收人"
            style="width: 100%"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showReportDialog = false">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 提交报告 </el-button>
      </template>
    </el-dialog>

    <!-- 查看报告对话框 -->
    <el-dialog
      v-model="showViewDialog"
      :title="currentReport?.title"
      width="700px"
      class="report-view-dialog"
    >
      <div v-if="currentReport" class="report-detail">
        <!-- 报告头部信息 -->
        <div class="detail-header">
          <div class="author-info">
            <el-avatar :size="48" :src="currentReport.avatar">
              {{ currentReport.userName?.charAt(0) || '?' }}
            </el-avatar>
            <div class="author-meta">
              <div class="author-name">{{ currentReport.userName }}</div>
              <div class="report-type-tag" :type="currentReport.type">
                {{ currentReport.typeText }}
              </div>
            </div>
          </div>
          <div class="report-date">
            {{ formatDateTime(currentReport.createTime) }}
          </div>
        </div>

        <!-- 报告内容 -->
        <div class="detail-content">
          <div class="content-section">
            <h4>今日工作</h4>
            <div class="content-text">{{ currentReport.todayWork || '无' }}</div>
          </div>
          <div class="content-section">
            <h4>明日计划</h4>
            <div class="content-text">{{ currentReport.tomorrowPlan || '无' }}</div>
          </div>
          <div class="content-section">
            <h4>问题与风险</h4>
            <div class="content-text">{{ currentReport.issues || '无' }}</div>
          </div>
          <div
            v-if="currentReport.attachments && currentReport.attachments.length > 0"
            class="content-section"
          >
            <h4>附件</h4>
            <div class="attachment-list">
              <div
                v-for="(file, index) in currentReport.attachments"
                :key="index"
                class="attachment-item"
              >
                <el-icon><Document /></el-icon>
                <span>{{ file.name }}</span>
                <el-button link type="primary" size="small">下载</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 互动区域 -->
        <div class="detail-actions">
          <div class="action-item" @click="handleLike">
            <el-icon :class="{ 'is-liked': currentReport.isLiked }"><Star /></el-icon>
            <span>{{ currentReport.likeCount || 0 }}</span>
          </div>
          <div class="action-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ currentReport.commentCount || 0 }}</span>
          </div>
          <div class="action-item">
            <el-icon><View /></el-icon>
            <span>{{ currentReport.viewCount || 0 }}</span>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="detail-comments">
          <h4>评论 ({{ comments.length }})</h4>
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="32" :src="comment.avatar">
                {{ comment.userName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.userName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
          </div>

          <!-- 评论输入 -->
          <div class="comment-input">
            <el-input v-model="newComment" placeholder="写评论..." @keyup.enter="handleSendComment">
              <template #append>
                <el-button @click="handleSendComment">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showViewDialog = false">关闭</el-button>
        <el-button v-if="canEdit" type="primary" @click="handleEdit">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, View, ChatDotRound, Star, Folder, Document } from '@element-plus/icons-vue'

// State
const activeTab = ref('received')
const showReportDialog = ref(false)
const showViewDialog = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)
const reportFormRef = ref(null)
const currentReport = ref(null)
const newComment = ref('')
const canEdit = computed(() => {
  return currentReport.value?.isAuthor
})

// 数据
const reports = ref([])
const comments = ref([])
const availableUsers = ref([
  { id: '1', name: '张三' },
  { id: '2', name: '李四' },
  { id: '3', name: '王五' },
])

const reportTabs = computed(() => [
  { key: 'received', label: '收到的报告', count: reports.value.filter(r => r.received).length },
  { key: 'sent', label: '我发送的', count: reports.value.filter(r => r.sent).length },
  { key: 'draft', label: '草稿箱', count: reports.value.filter(r => r.status === 'draft').length },
])

// 表单数据
const reportForm = ref({
  id: null,
  type: 'daily',
  title: '',
  todayWork: '',
  tomorrowPlan: '',
  issues: '',
  attachments: [],
  visibility: 'department',
  recipients: [],
})

const reportFormRules = {
  type: [{ required: true, message: '请选择报告类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入报告标题', trigger: 'blur' }],
  todayWork: [{ required: true, message: '请填写今日工作', trigger: 'blur' }],
}

// 方法
const fetchReports = async () => {
  try {
    // TODO: 调用API获取报告列表
    // const response = await getReportList({ type: activeTab.value })
    // reports.value = response.data

    // 模拟数据
    reports.value = [
      {
        id: '1',
        type: 'daily',
        typeText: '日报',
        title: '前端开发进度汇报',
        summary: '完成了用户中心模块的开发，包括登录、注册、个人中心等功能。',
        todayWork: '1. 完成用户中心页面开发\n2. 修复样式问题\n3. 编写单元测试',
        tomorrowPlan: '1. 开始开发订单模块\n2. 继续完善用户中心功能',
        issues: 'API接口响应速度较慢，需要后端优化',
        userName: '张三',
        avatar: '',
        createTime: new Date(),
        viewCount: 12,
        commentCount: 3,
        likeCount: 5,
        isLiked: false,
        isAuthor: true,
        received: true,
        sent: false,
        status: 'submitted',
      },
      {
        id: '2',
        type: 'weekly',
        typeText: '周报',
        title: '本周工作总结',
        summary: '完成了IM系统核心功能的开发，包括消息发送、群组管理等功能。',
        userName: '李四',
        avatar: '',
        createTime: new Date(Date.now() - 86400000),
        viewCount: 8,
        commentCount: 1,
        likeCount: 2,
        isLiked: true,
        isAuthor: false,
        received: true,
        sent: false,
        status: 'submitted',
      },
    ]
  } catch (error) {
    console.error('获取报告列表失败:', error)
  }
}

const handleCreate = () => {
  isEditMode.value = false
  reportForm.value = {
    id: null,
    type: 'daily',
    title: '',
    todayWork: '',
    tomorrowPlan: '',
    issues: '',
    attachments: [],
    visibility: 'department',
    recipients: [],
  }
  showReportDialog.value = true
}

const handleViewReport = async report => {
  currentReport.value = report

  // 获取评论
  await fetchComments(report.id)

  showViewDialog.value = true
}

const handleEdit = () => {
  isEditMode.value = true
  reportForm.value = { ...currentReport.value }
  showViewDialog.value = false
  showReportDialog.value = true
}

const handleDialogClose = () => {
  reportFormRef.value?.resetFields()
}

const handleFileChange = (file, fileList) => {
  // 文件选择处理
}

const handleFileRemove = (file, fileList) => {
  // 文件移除处理
}

const handleSubmit = async () => {
  const valid = await reportFormRef.value?.validate()
  if (!valid) return

  submitting.value = true

  try {
    const reportData = {
      ...reportForm.value,
      status: 'submitted',
    }

    // TODO: 调用API提交报告
    ElMessage.success(isEditMode.value ? '报告已更新' : '报告已提交')

    showReportDialog.value = false
    await fetchReports()
  } catch (error) {
    console.error('提交报告失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleSaveDraft = async () => {
  submitting.value = true

  try {
    // TODO: 调用API保存草稿
    ElMessage.success('草稿已保存')
    showReportDialog.value = false
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handleLike = () => {
  if (!currentReport.value) return

  currentReport.value.isLiked = !currentReport.value.isLiked
  currentReport.value.likeCount += currentReport.value.isLiked ? 1 : -1

  // TODO: 调用API点赞/取消点赞
}

const fetchComments = async reportId => {
  try {
    // TODO: 调用API获取评论
    comments.value = [
      {
        id: '1',
        content: '工作进展很好，继续保持！',
        userName: '李四',
        avatar: '',
        createTime: new Date(Date.now() - 3600000),
      },
      {
        id: '2',
        content: '收到，辛苦了',
        userName: '王五',
        avatar: '',
        createTime: new Date(Date.now() - 7200000),
      },
    ]
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleSendComment = async () => {
  if (!newComment.value.trim()) {
    return
  }

  try {
    // TODO: 调用API发送评论
    comments.value.push({
      id: Date.now().toString(),
      content: newComment.value,
      userName: '我',
      avatar: '',
      createTime: new Date(),
    })

    currentReport.value.commentCount++
    newComment.value = ''
  } catch (error) {
    console.error('发送评论失败:', error)
    ElMessage.error('发送失败')
  }
}

const formatTime = date => {
  if (!date) return ''
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  return `${Math.floor(minutes / 1440)}天前`
}

const formatDateTime = date => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchReports()
})
</script>

<style lang="scss" scoped>
.workbench-report {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);

  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
  }
}

.report-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.report-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 500;
  }

  .tab-count {
    padding: 2px 8px;
    background: var(--el-fill-color);
    border-radius: 10px;
    font-size: 12px;
  }
}

.report-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.report-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
}

.report-avatar {
  flex-shrink: 0;
}

.report-content {
  flex: 1;
  min-width: 0;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.report-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.report-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;

  &[type='daily'] {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
  }

  &[type='weekly'] {
    background: var(--el-color-success-light-9);
    color: var(--el-color-success);
  }

  &[type='monthly'] {
    background: var(--el-color-warning-light-9);
    color: var(--el-color-warning);
  }
}

.report-summary {
  font-size: 13px;
  color: var(--el-text-regular);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.report-stats {
  display: flex;
  gap: 12px;
  margin-left: auto;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

// 报告详情
.report-detail {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-light);
    margin-bottom: 20px;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .author-meta {
    .author-name {
      font-size: 15px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .report-type-tag {
      font-size: 12px;
    }
  }

  .report-date {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .detail-content {
    padding: 0 16px 20px;
  }

  .content-section {
    margin-bottom: 20px;

    h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .content-text {
      font-size: 14px;
      line-height: 1.6;
      color: var(--el-text-regular);
      white-space: pre-wrap;
    }
  }

  .attachment-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .attachment-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-radius: 6px;
  }

  .detail-actions {
    display: flex;
    gap: 24px;
    padding: 16px;
    border-top: 1px solid var(--el-border-color-light);

    .action-item {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      color: var(--el-text-color-secondary);
      transition: all 0.2s;

      &:hover {
        color: var(--el-color-primary);
      }

      .is-liked {
        color: var(--el-color-warning);
      }
    }
  }

  .detail-comments {
    padding: 0 16px 16px;

    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      font-weight: 500;
    }

    .comment-list {
      max-height: 300px;
      overflow-y: auto;
      margin-bottom: 16px;
    }

    .comment-item {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .comment-content {
      flex: 1;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }

    .comment-author {
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .comment-time {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }

    .comment-text {
      font-size: 13px;
      color: var(--el-text-regular);
      line-height: 1.5;
    }

    .comment-input {
      margin-top: 12px;
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .report-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .el-button {
      width: 100%;
    }
  }

  .report-item {
    flex-direction: column;
  }

  .report-stats {
    margin-left: 0;
  }

  .detail-actions {
    justify-content: center;
  }
}
</style>
