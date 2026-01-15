<template>
  <div class="workbench-approval">
    <!-- 审批头部 -->
    <div class="approval-header">
      <div class="header-stats">
        <div class="stat-item pending">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审批</div>
        </div>
        <div class="stat-item processing">
          <div class="stat-value">{{ stats.processing }}</div>
          <div class="stat-label">审批中</div>
        </div>
        <div class="stat-item completed">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-item rejected">
          <div class="stat-value">{{ stats.rejected }}</div>
          <div class="stat-label">已驳回</div>
        </div>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate"> 发起审批 </el-button>
    </div>

    <!-- 审批分类标签 -->
    <div class="approval-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 审批列表 -->
    <div class="approval-list">
      <!-- 待我审批 -->
      <template v-if="activeTab === 'pending'">
        <div v-if="pendingList.length === 0" class="empty-state">
          <el-icon :size="64"><DocumentChecked /></el-icon>
          <p>暂无待审批事项</p>
        </div>
        <div
          v-for="item in pendingList"
          :key="item.id"
          class="approval-item"
          @click="handleView(item)"
        >
          <div class="item-icon" :class="item.type">
            <el-icon :size="24"><component :is="getTypeIcon(item.type)" /></el-icon>
          </div>
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ item.applicant }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ item.applyTime }}
              </span>
            </div>
            <div v-if="item.urgent" class="item-urgent">
              <el-tag type="danger" size="small">紧急</el-tag>
            </div>
          </div>
          <div class="item-actions">
            <el-button type="success" size="small" :icon="Check" @click.stop="handleApprove(item)">
              通过
            </el-button>
            <el-button type="danger" size="small" :icon="Close" @click.stop="handleReject(item)">
              驳回
            </el-button>
          </div>
        </div>
      </template>

      <!-- 我发起的 -->
      <template v-else-if="activeTab === 'my'">
        <div v-if="myList.length === 0" class="empty-state">
          <el-icon :size="64"><Document /></el-icon>
          <p>暂无发起的审批</p>
        </div>
        <div v-for="item in myList" :key="item.id" class="approval-item" @click="handleView(item)">
          <div class="item-icon" :class="item.type">
            <el-icon :size="24"><component :is="getTypeIcon(item.type)" /></el-icon>
          </div>
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ item.applyTime }}
              </span>
            </div>
            <div class="item-status">
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
              <span v-if="item.currentNode" class="current-node">
                当前节点: {{ item.currentNode }}
              </span>
            </div>
          </div>
          <div class="item-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </template>

      <!-- 抄送我的 -->
      <template v-else-if="activeTab === 'cc'">
        <div v-if="ccList.length === 0" class="empty-state">
          <el-icon :size="64"><Message /></el-icon>
          <p>暂无抄送事项</p>
        </div>
        <div v-for="item in ccList" :key="item.id" class="approval-item" @click="handleView(item)">
          <div class="item-icon cc">
            <el-icon :size="24"><Message /></el-icon>
          </div>
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ item.applicant }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ item.applyTime }}
              </span>
            </div>
          </div>
          <div class="item-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </template>
    </div>

    <!-- 发起审批对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="发起审批"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="审批类型" prop="type">
          <el-select v-model="createForm.type" placeholder="选择审批类型" style="width: 100%">
            <el-option label="请假申请" value="leave" />
            <el-option label="报销申请" value="expense" />
            <el-option label="出差申请" value="businessTrip" />
            <el-option label="采购申请" value="purchase" />
            <el-option label="用印申请" value="seal" />
            <el-option label="合同审批" value="contract" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入审批标题" />
        </el-form-item>

        <el-form-item label="审批内容" prop="content">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述审批事项..."
          />
        </el-form-item>

        <!-- 动态表单根据类型显示 -->
        <template v-if="createForm.type === 'leave'">
          <el-form-item label="请假类型">
            <el-select v-model="createForm.leaveType" placeholder="选择请假类型">
              <el-option label="事假" value="personal" />
              <el-option label="病假" value="sick" />
              <el-option label="年假" value="annual" />
              <el-option label="调休" value="compensatory" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="createForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="createForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="请假天数">
            <el-input-number v-model="createForm.days" :min="0.5" :step="0.5" />
          </el-form-item>
        </template>

        <template v-if="createForm.type === 'expense'">
          <el-form-item label="报销金额">
            <el-input-number v-model="createForm.amount" :min="0" :precision="2" />
            <span style="margin-left: 8px">元</span>
          </el-form-item>
          <el-form-item label="费用类型">
            <el-select v-model="createForm.expenseType" placeholder="选择费用类型">
              <el-option label="交通费" value="transport" />
              <el-option label="餐饮费" value="meal" />
              <el-option label="住宿费" value="accommodation" />
              <el-option label="办公费" value="office" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="发票上传">
            <el-upload action="#" list-type="picture-card" :auto-upload="false" :limit="5">
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </template>

        <el-form-item label="审批人" prop="approvers">
          <el-select
            v-model="createForm.approvers"
            multiple
            placeholder="选择审批人"
            style="width: 100%"
          >
            <el-option
              v-for="user in approverUsers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="抄送人">
          <el-select
            v-model="createForm.ccUsers"
            multiple
            placeholder="选择抄送人"
            style="width: 100%"
          >
            <el-option
              v-for="user in ccUserList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="附件">
          <el-upload action="#" :auto-upload="false" :limit="3">
            <el-button type="primary" :icon="Upload">上传附件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitCreate">
          提交审批
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="detailTitle" width="700px">
      <div v-if="currentItem" class="approval-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-row">
            <span class="label">审批类型:</span>
            <span class="value">{{ getTypeName(currentItem.type) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">申请人:</span>
            <span class="value">{{ currentItem.applicant }}</span>
          </div>
          <div class="detail-row">
            <span class="label">申请时间:</span>
            <span class="value">{{ currentItem.applyTime }}</span>
          </div>
          <div class="detail-row">
            <span class="label">当前状态:</span>
            <el-tag :type="getStatusType(currentItem.status)">
              {{ getStatusText(currentItem.status) }}
            </el-tag>
          </div>
        </div>

        <!-- 审批内容 -->
        <div class="detail-section">
          <h4>审批内容</h4>
          <div class="detail-content">{{ currentItem.content }}</div>
        </div>

        <!-- 审批流程 -->
        <div class="detail-section">
          <h4>审批流程</h4>
          <div class="flow-steps">
            <div
              v-for="(step, index) in currentItem.flowSteps"
              :key="index"
              class="flow-step"
              :class="{
                'is-active': step.status === 'active',
                'is-completed': step.status === 'completed',
                'is-rejected': step.status === 'rejected',
              }"
            >
              <div class="step-icon">
                <el-icon v-if="step.status === 'completed'"><Check /></el-icon>
                <el-icon v-else-if="step.status === 'rejected'"><Close /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="step-content">
                <div class="step-name">{{ step.name }}</div>
                <div class="step-user">{{ step.user }}</div>
                <div v-if="step.comment" class="step-comment">{{ step.comment }}</div>
                <div v-if="step.time" class="step-time">{{ step.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentItem?.canApprove"
          type="success"
          @click="handleApprove(currentItem)"
        >
          通过
        </el-button>
        <el-button v-if="currentItem?.canApprove" type="danger" @click="handleReject(currentItem)">
          驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批意见对话框 -->
    <el-dialog v-model="commentDialogVisible" :title="commentTitle" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="4"
            :placeholder="commentPlaceholder"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComment"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentChecked,
  Document,
  Message,
  Clock,
  User,
  Plus,
  Check,
  Close,
  ArrowRight,
  Upload,
  Calendar,
  Money,
  Ticket,
  ShoppingBag,
  Stamp,
  DocumentCopy,
} from '@element-plus/icons-vue'
import {
  getApprovalList,
  createApproval,
  approveApproval,
  getApprovalDetail,
} from '@/api/im/workbench'

// Props
const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
})

// State
const activeTab = ref('pending')
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const submitting = ref(false)
const commentText = ref('')
const commentType = ref('') // 'approve' | 'reject'
const currentItem = ref(null)

const stats = ref({
  pending: 5,
  processing: 3,
  completed: 28,
  rejected: 1,
})

const tabs = ref([
  { key: 'pending', label: '待我审批', icon: Clock, count: 5 },
  { key: 'my', label: '我发起的', icon: Document, count: 0 },
  { key: 'cc', label: '抄送我的', icon: Message, count: 2 },
])

const createForm = ref({
  type: '',
  title: '',
  content: '',
  leaveType: '',
  startTime: null,
  endTime: null,
  days: 1,
  amount: 0,
  expenseType: '',
  approvers: [],
  ccUsers: [],
})

const createRules = {
  type: [{ required: true, message: '请选择审批类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入审批内容', trigger: 'blur' }],
  approvers: [{ required: true, message: '请选择审批人', trigger: 'change' }],
}

const approverUsers = ref([
  { id: 1, name: '张经理' },
  { id: 2, name: '李总监' },
  { id: 3, name: '王副总' },
])

const ccUserList = ref([
  { id: 4, name: '行政部' },
  { id: 5, name: '财务部' },
])

// 模拟数据
const pendingList = ref([
  {
    id: 1,
    type: 'leave',
    title: '请假申请 - 事假2天',
    applicant: '李四',
    applyTime: '2024-01-08 10:30',
    content: '因家中有事，申请请假2天',
    urgent: true,
    status: 'pending',
    canApprove: true,
    currentNode: '部门经理审批',
    flowSteps: [
      { name: '发起申请', user: '李四', status: 'completed', time: '2024-01-08 10:30' },
      { name: '部门经理审批', user: '张经理', status: 'active', comment: '', time: '' },
      { name: 'HR备案', user: '人事部', status: 'pending', comment: '', time: '' },
    ],
  },
  {
    id: 2,
    type: 'expense',
    title: '报销申请 - 差旅费3500元',
    applicant: '王五',
    applyTime: '2024-01-08 09:15',
    content: '北京出差交通及住宿费用报销',
    urgent: false,
    status: 'pending',
    canApprove: true,
    currentNode: '财务审批',
    flowSteps: [
      { name: '发起申请', user: '王五', status: 'completed', time: '2024-01-08 09:15' },
      {
        name: '部门经理审批',
        user: '张经理',
        status: 'completed',
        comment: '同意',
        time: '2024-01-08 09:30',
      },
      { name: '财务审批', user: '财务部', status: 'active', comment: '', time: '' },
    ],
  },
  {
    id: 3,
    type: 'businessTrip',
    title: '出差申请 - 上海3天',
    applicant: '赵六',
    applyTime: '2024-01-07 16:45',
    content: '前往上海客户处进行技术交流',
    urgent: false,
    status: 'pending',
    canApprove: true,
    currentNode: '部门经理审批',
    flowSteps: [
      { name: '发起申请', user: '赵六', status: 'completed', time: '2024-01-07 16:45' },
      { name: '部门经理审批', user: '张经理', status: 'active', comment: '', time: '' },
    ],
  },
  {
    id: 4,
    type: 'purchase',
    title: '采购申请 - 办公设备',
    applicant: '孙七',
    applyTime: '2024-01-07 14:20',
    content: '申请采购显示器2台，键盘鼠标套装5套',
    urgent: false,
    status: 'pending',
    canApprove: true,
    currentNode: '部门经理审批',
    flowSteps: [
      { name: '发起申请', user: '孙七', status: 'completed', time: '2024-01-07 14:20' },
      { name: '部门经理审批', user: '张经理', status: 'active', comment: '', time: '' },
      { name: '采购部审批', user: '采购部', status: 'pending', comment: '', time: '' },
    ],
  },
  {
    id: 5,
    type: 'contract',
    title: '合同审批 - 服务合同',
    applicant: '周八',
    applyTime: '2024-01-07 11:00',
    content: '与XX公司签订年度技术服务合同',
    urgent: true,
    status: 'pending',
    canApprove: true,
    currentNode: '法务审批',
    flowSteps: [
      { name: '发起申请', user: '周八', status: 'completed', time: '2024-01-07 11:00' },
      {
        name: '部门经理审批',
        user: '张经理',
        status: 'completed',
        comment: '同意',
        time: '2024-01-07 11:30',
      },
      { name: '法务审批', user: '法务部', status: 'active', comment: '', time: '' },
    ],
  },
])

const myList = ref([
  {
    id: 101,
    type: 'leave',
    title: '请假申请 - 年假5天',
    applicant: '我',
    applyTime: '2024-01-05 09:00',
    content: '计划春节回家探亲',
    status: 'approved',
    currentNode: null,
    flowSteps: [
      { name: '发起申请', user: '我', status: 'completed', time: '2024-01-05 09:00' },
      {
        name: '部门经理审批',
        user: '张经理',
        status: 'completed',
        comment: '同意',
        time: '2024-01-05 09:30',
      },
      {
        name: 'HR备案',
        user: '人事部',
        status: 'completed',
        comment: '已备案',
        time: '2024-01-05 10:00',
      },
    ],
  },
  {
    id: 102,
    type: 'expense',
    title: '报销申请 - 招待费800元',
    applicant: '我',
    applyTime: '2024-01-06 15:20',
    content: '客户招待费用报销',
    status: 'processing',
    currentNode: '财务审批',
    flowSteps: [
      { name: '发起申请', user: '我', status: 'completed', time: '2024-01-06 15:20' },
      {
        name: '部门经理审批',
        user: '张经理',
        status: 'completed',
        comment: '同意',
        time: '2024-01-06 15:45',
      },
      { name: '财务审批', user: '财务部', status: 'active', comment: '', time: '' },
    ],
  },
  {
    id: 103,
    type: 'businessTrip',
    title: '出差申请 - 深圳2天',
    applicant: '我',
    applyTime: '2024-01-07 10:00',
    content: '前往深圳参加技术峰会',
    status: 'rejected',
    currentNode: null,
    flowSteps: [
      { name: '发起申请', user: '我', status: 'completed', time: '2024-01-07 10:00' },
      {
        name: '部门经理审批',
        user: '张经理',
        status: 'rejected',
        comment: '预算紧张，暂不同意',
        time: '2024-01-07 14:00',
      },
    ],
  },
])

const ccList = ref([
  {
    id: 201,
    type: 'leave',
    title: '请假申请 - 病假3天',
    applicant: '李四',
    applyTime: '2024-01-06 09:30',
    content: '身体不适，需休养',
  },
  {
    id: 202,
    type: 'contract',
    title: '合同审批 - 采购合同',
    applicant: '采购部',
    applyTime: '2024-01-07 16:00',
    content: '与供应商签订年度采购框架协议',
  },
])

// 计算属性
const detailTitle = computed(() => {
  if (!currentItem.value) return '审批详情'
  return `${getTypeName(currentItem.value.type)} - 审批详情`
})

const commentTitle = computed(() => {
  return commentType.value === 'approve' ? '审批通过' : '审批驳回'
})

const commentPlaceholder = computed(() => {
  return commentType.value === 'approve' ? '请输入审批意见（可选）' : '请输入驳回原因（必填）'
})

// 方法
const getTypeIcon = type => {
  const icons = {
    leave: Ticket,
    expense: Money,
    businessTrip: Calendar,
    purchase: ShoppingBag,
    seal: Stamp,
    contract: DocumentCopy,
  }
  return icons[type] || Document
}

const getTypeName = type => {
  const names = {
    leave: '请假申请',
    expense: '报销申请',
    businessTrip: '出差申请',
    purchase: '采购申请',
    seal: '用印申请',
    contract: '合同审批',
  }
  return names[type] || '审批申请'
}

const getStatusType = status => {
  const types = {
    pending: 'info',
    processing: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'success',
  }
  return types[status] || 'info'
}

const getStatusText = status => {
  const texts = {
    pending: '待审批',
    processing: '审批中',
    approved: '已通过',
    rejected: '已驳回',
    completed: '已完成',
  }
  return texts[status] || status
}

const handleCreate = () => {
  createForm.value = {
    type: '',
    title: '',
    content: '',
    leaveType: '',
    startTime: null,
    endTime: null,
    days: 1,
    amount: 0,
    expenseType: '',
    approvers: [],
    ccUsers: [],
  }
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  // 表单验证
  if (!createForm.value.type) {
    ElMessage.warning('请选择审批类型')
    return
  }
  if (!createForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!createForm.value.content) {
    ElMessage.warning('请输入审批内容')
    return
  }

  submitting.value = true

  try {
    // 调用API提交审批
    const formData = {
      type: createForm.value.type,
      title: createForm.value.title,
      content: createForm.value.content,
      leaveType: createForm.value.leaveType,
      startTime: createForm.value.startTime,
      endTime: createForm.value.endTime,
      days: createForm.value.days,
      amount: createForm.value.amount,
      expenseType: createForm.value.expenseType,
      approvers: createForm.value.approvers,
      ccUsers: createForm.value.ccUsers,
    }

    // 使用默认模板ID创建审批
    await createApproval({
      templateId: 1,
      title: createForm.value.title,
      formData,
    })

    ElMessage.success('审批提交成功')
    createDialogVisible.value = false

    // 刷新列表
    await fetchData()
  } catch (error) {
    console.error('提交审批失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleView = item => {
  currentItem.value = item
  detailDialogVisible.value = true
}

const handleApprove = item => {
  currentItem.value = item
  commentType.value = 'approve'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleReject = item => {
  currentItem.value = item
  commentType.value = 'reject'
  commentText.value = ''
  commentDialogVisible.value = true
}

const handleSubmitComment = async () => {
  if (commentType.value === 'reject' && !commentText.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  try {
    const action = commentType.value === 'approve' ? '通过' : '驳回'
    const apiMethod = commentType.value === 'approve' ? 'approve' : 'reject'

    // 调用API提交审批意见
    await approveApproval(currentItem.value.id, {
      action: apiMethod,
      comment: commentText.value,
    })

    ElMessage.success(`审批${action}成功`)

    // 更新列表
    if (commentType.value === 'approve') {
      const index = pendingList.value.findIndex(i => i.id === currentItem.value.id)
      if (index > -1) {
        pendingList.value.splice(index, 1)
        stats.value.pending--
      }
    } else {
      const index = pendingList.value.findIndex(i => i.id === currentItem.value.id)
      if (index > -1) {
        pendingList.value.splice(index, 1)
        stats.value.pending--
        stats.value.rejected++
      }
    }

    commentDialogVisible.value = false
    detailDialogVisible.value = false
  } catch (error) {
    console.error('提交审批意见失败:', error)
    ElMessage.error('提交失败，请重试')
  }
}

const fetchData = async () => {
  try {
    // 调用API获取审批数据
    const [pendingRes, myRes] = await Promise.all([
      getApprovalList({ type: 'pending' }),
      getApprovalList({ type: 'my' }),
    ])

    if (pendingRes.data.code === 200) {
      const pendingData = pendingRes.data.data || []
      pendingList.value = pendingData.map(item => ({
        id: item.id,
        type: item.type || 'leave',
        title: item.title,
        applicant: item.applicantName || '申请人',
        applyTime: formatDateTime(item.createTime),
        content: item.content || '',
        urgent: item.urgent || false,
        status: item.status,
        canApprove: true,
        currentNode: item.currentNode,
        flowSteps: item.flowSteps || [],
      }))
      tabs.value[0].count = pendingList.value.length
      stats.value.pending = pendingList.value.length
    }

    if (myRes.data.code === 200) {
      const myData = myRes.data.data || []
      myList.value = myData.map(item => ({
        id: item.id,
        type: item.type || 'leave',
        title: item.title,
        applicant: '我',
        applyTime: formatDateTime(item.createTime),
        content: item.content || '',
        status: item.status,
        currentNode: item.currentNode,
        flowSteps: item.flowSteps || [],
      }))
      tabs.value[1].count = myList.value.length

      // 统计各状态数量
      stats.value.processing = myList.value.filter(i => i.status === 'processing').length
      stats.value.completed = myList.value.filter(
        i => i.status === 'approved' || i.status === 'completed'
      ).length
      stats.value.rejected = myList.value.filter(i => i.status === 'rejected').length
    }
  } catch (error) {
    console.error('获取审批数据失败:', error)
    // 保持模拟数据作为备用
  }
}

// 格式化日期时间
const formatDateTime = dateStr => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.workbench-approval {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

// 审批头部
.approval-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  min-width: 100px;

  &.pending {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  &.processing {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }

  &.completed {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
  }

  &.rejected {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    color: white;
  }
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
}

// 审批分类标签
.approval-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding: 0 4px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--el-border-color-light);
  font-size: 14px;
  color: var(--el-text-color-secondary);

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary);
    color: white;
    border-color: var(--el-color-primary);

    .tab-count {
      background: white;
      color: var(--el-color-primary);
    }
  }
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: var(--el-color-danger);
  color: white;
  border-radius: 9px;
  font-size: 12px;
}

// 审批列表
.approval-list {
  min-height: 400px;
}

.approval-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
}

.item-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;

  &.leave {
    background: var(--el-color-warning-light-9);
    color: var(--el-color-warning);
  }

  &.expense {
    background: var(--el-color-success-light-9);
    color: var(--el-color-success);
  }

  &.businessTrip {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
  }

  &.purchase {
    background: var(--el-color-info-light-9);
    color: var(--el-color-info);
  }

  &.seal {
    background: var(--el-color-danger-light-9);
    color: var(--el-color-danger);
  }

  &.contract {
    background: var(--el-color-purple-light-9);
    color: var(--el-color-purple);
  }

  &.cc {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.item-urgent {
  margin-top: 6px;
}

.item-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;

  .current-node {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.item-arrow {
  color: var(--el-text-color-placeholder);
  flex-shrink: 0;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--el-text-color-placeholder);

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

// 审批详情
.approval-detail {
  .detail-section {
    margin-bottom: 24px;

    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}

.detail-row {
  display: flex;
  margin-bottom: 12px;

  .label {
    width: 100px;
    color: var(--el-text-color-secondary);
  }

  .value {
    flex: 1;
    color: var(--el-text-color-primary);
  }
}

.detail-content {
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  line-height: 1.8;
  color: var(--el-text-color-primary);
}

.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow-step {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border-left: 3px solid var(--el-border-color);

  &.is-active {
    border-left-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  &.is-completed {
    border-left-color: var(--el-color-success);
  }

  &.is-rejected {
    border-left-color: var(--el-color-danger);
  }
}

.step-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: white;
  border: 2px solid var(--el-border-color);
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-secondary);

  .is-active & {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
  }

  .is-completed & {
    border-color: var(--el-color-success);
    color: var(--el-color-success);
  }

  .is-rejected & {
    border-color: var(--el-color-danger);
    color: var(--el-color-danger);
  }
}

.step-content {
  flex: 1;
}

.step-name {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.step-user {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.step-comment {
  font-size: 13px;
  color: var(--el-color-info);
  margin-bottom: 4px;
}

.step-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .header-stats {
    flex-wrap: wrap;
  }

  .stat-item {
    min-width: 80px;
    padding: 12px 16px;
  }

  .stat-value {
    font-size: 22px;
  }

  .approval-tabs {
    flex-wrap: wrap;
  }

  .item-actions {
    flex-direction: column;
  }

  .item-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
