<template>
  <el-dialog
    v-model="dialogVisible"
    title="反馈建议"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="feedback-dialog"
    @close="handleClose"
  >
    <div class="feedback-content">
      <!-- 反馈类型选择 -->
      <div class="feedback-type-section">
        <div class="section-label">
          <span class="required">*</span>
          <span>反馈类型</span>
        </div>
        <div class="type-options">
          <div
            v-for="type in feedbackTypes"
            :key="type.value"
            class="type-option"
            :class="{ active: formData.type === type.value }"
            @click="selectType(type.value)"
          >
            <div class="type-icon" :class="`type-${type.value}`">
              <el-icon :size="24">
                <component :is="type.icon" />
              </el-icon>
            </div>
            <span class="type-name">{{ type.label }}</span>
          </div>
        </div>
      </div>

      <!-- 反馈表单 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="feedback-form"
      >
        <!-- 标题 -->
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请简要描述您的问题或建议"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <!-- 详细描述 -->
        <el-form-item label="详细描述" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您遇到的问题或建议，帮助我们更好地改进产品&#10;&#10;• 问题描述：请描述具体操作步骤&#10;• 功能建议：请描述您期望的功能&#10;• 其他问题：请尽可能详细说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 联系方式 -->
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="formData.contact" placeholder="邮箱或手机号（选填，便于我们联系您）">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 图片附件 -->
        <el-form-item label="截图">
          <div class="upload-section">
            <el-upload
              ref="uploadRef"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :on-remove="handleRemove"
              :file-list="fileList"
              list-type="picture-card"
              :limit="3"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              <span>最多上传3张截图，每张不超过2MB</span>
            </div>
          </div>
        </el-form-item>

        <!-- 问题分类（仅bug类型显示） -->
        <el-form-item v-if="formData.type === 'bug'" label="严重程度" prop="severity">
          <el-radio-group v-model="formData.severity">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
            <el-radio :label="4">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- 快捷问题 -->
      <div v-if="formData.type === 'bug'" class="quick-issues">
        <div class="section-label">常见问题</div>
        <div class="quick-tags">
          <el-tag v-for="issue in commonIssues" :key="issue" @click="addCommonIssue(issue)">
            {{ issue }}
          </el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-checkbox v-model="formData.anonymous">匿名反馈</el-checkbox>
        <div class="footer-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交反馈
          </el-button>
        </div>
      </div>
    </template>

    <!-- 提交成功提示 -->
    <el-dialog
      v-model="showSuccess"
      title="反馈提交成功"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
      class="success-dialog"
    >
      <div class="success-content">
        <el-icon class="success-icon" :size="64" color="#67c23a">
          <CircleCheck />
        </el-icon>
        <p class="success-title">感谢您的反馈！</p>
        <p class="success-desc">我们会认真对待您的每一个建议</p>
        <div class="success-info">
          <div class="info-item">
            <el-icon><Tickets /></el-icon>
            <span>反馈编号：{{ feedbackId }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>预计处理时间：1-3个工作日</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleSuccessClose">知道了</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Message,
  CircleCheck,
  Tickets,
  Clock,
  Warning,
  MagicStick,
  ChatLineSquare,
  QuestionFilled,
} from '@element-plus/icons-vue'
import { submitFeedback, uploadFeedbackImage } from '@/api/im/feedback'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val),
})

const formRef = ref(null)
const uploadRef = ref(null)
const submitting = ref(false)
const showSuccess = ref(false)
const feedbackId = ref('')
const fileList = ref([])

// 反馈类型
const feedbackTypes = [
  { value: 'bug', label: '问题反馈', icon: Warning },
  { value: 'feature', label: '功能建议', icon: MagicStick },
  { value: 'improvement', label: '体验优化', icon: ChatLineSquare },
  { value: 'other', label: '其他', icon: QuestionFilled },
]

// 常见问题
const commonIssues = [
  '消息发送失败',
  '无法接收消息',
  '登录异常',
  '文件无法上传',
  '界面显示异常',
  '功能无法使用',
  '闪退/卡顿',
  '通知不提醒',
]

// 表单数据
const formData = reactive({
  type: 'bug',
  title: '',
  content: '',
  contact: '',
  severity: 2,
  anonymous: false,
  attachments: [],
})

// 表单验证规则
const formRules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' },
  ],
  contact: [
    {
      pattern: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+|1[3-9]\d{9}$/,
      message: '请输入正确的邮箱或手机号',
      trigger: 'blur',
    },
  ],
}

// 上传配置
const uploadUrl = computed(() => {
  return process.env.VUE_APP_BASE_API + '/common/upload'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + getToken(),
  }
})

// 选择反馈类型
const selectType = type => {
  formData.type = type
  // 切换类型时清除验证
  formRef.value?.clearValidate('type')
}

// 添加常见问题
const addCommonIssue = issue => {
  if (formData.content) {
    formData.content += '\n\n问题：' + issue
  } else {
    formData.content = '问题：' + issue
  }
}

// 上传前校验
const beforeUpload = file => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    formData.attachments.push(response.url)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    // 从列表中移除失败的文件
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 移除附件
const handleRemove = file => {
  const index = formData.attachments.indexOf(file.url)
  if (index > -1) {
    formData.attachments.splice(index, 1)
  }
}

// 提交反馈
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true

    const submitData = {
      type: formData.type,
      title: formData.title,
      content: formData.content,
      contact: formData.contact,
      anonymous: formData.anonymous,
      attachments: formData.attachments,
      severity: formData.severity,
    }

    const response = await submitFeedback(submitData)

    if (response.code === 200) {
      feedbackId.value = response.data.feedbackId || generateFeedbackId()
      showSuccess.value = true
      emit('success', submitData)
    } else {
      ElMessage.error(response.msg || '提交失败')
    }
  } catch (error) {
    if (error !== false) {
      // 表单验证失败时为false
      console.error('提交反馈失败:', error)
      ElMessage.error('提交失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 生成反馈编号
const generateFeedbackId = () => {
  return 'FB' + Date.now().toString().slice(-8)
}

// 关闭成功提示
const handleSuccessClose = () => {
  showSuccess.value = false
  handleClose()
}

// 关闭对话框
const handleClose = () => {
  resetForm()
  emit('update:visible', false)
}

// 重置表单
const resetForm = () => {
  formData.type = 'bug'
  formData.title = ''
  formData.content = ''
  formData.contact = ''
  formData.severity = 2
  formData.anonymous = false
  formData.attachments = []
  fileList.value = []
  formRef.value?.clearValidate()
}

// 监听对话框打开
watch(
  () => props.visible,
  val => {
    if (val) {
      // 从localStorage加载用户联系方式
      const savedContact = localStorage.getItem('user_feedback_contact')
      if (savedContact && !formData.contact) {
        formData.contact = savedContact
      }
    }
  }
)
</script>

<style lang="scss" scoped>
.feedback-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 20px 10px;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  :deep(.el-dialog__footer) {
    padding: 15px 20px 20px;
  }
}

.feedback-content {
  .feedback-type-section {
    margin-bottom: 20px;

    .section-label {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 12px;

      .required {
        color: #f56c6c;
        margin-right: 4px;
      }
    }

    .type-options {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;

      .type-option {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px 8px;
        border: 2px solid #e5e6eb;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #c9cdd4;
          background: #f3f4f6;
        }

        &.active {
          border-color: #1890ff;
          background: #e6f7ff;

          .type-icon {
            background: #1890ff;
            color: #fff;
          }
        }

        .type-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 8px;
          background: #f3f4f6;
          color: #4e5969;
          transition: all 0.2s;

          &.type-bug {
            background: #fff7e8;
            color: #ff7d00;
          }

          &.type-feature {
            background: #e8ffea;
            color: #00b42a;
          }

          &.type-improvement {
            background: #f0f9ff;
            color: #165dff;
          }

          &.type-other {
            background: #f5f5f5;
            color: #8b95a1;
          }
        }

        .type-name {
          font-size: 13px;
          color: #4e5969;
        }
      }
    }
  }

  .feedback-form {
    margin-bottom: 20px;

    :deep(.el-form-item__label) {
      font-weight: 500;
    }

    .upload-section {
      .upload-tip {
        font-size: 12px;
        color: #8b95a1;
        margin-top: 8px;
      }
    }
  }

  .quick-issues {
    padding: 16px;
    background: #f7f8fa;
    border-radius: 8px;

    .section-label {
      font-size: 13px;
      font-weight: 500;
      color: #4e5969;
      margin-bottom: 10px;
    }

    .quick-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .el-tag {
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #1890ff;
          color: #fff;
          border-color: #1890ff;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .footer-actions {
    display: flex;
    gap: 12px;
  }
}

.success-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  .success-content {
    text-align: center;
    padding: 20px 0;

    .success-icon {
      margin-bottom: 16px;
    }

    .success-title {
      font-size: 18px;
      font-weight: 500;
      color: #303133;
      margin: 0 0 8px;
    }

    .success-desc {
      font-size: 14px;
      color: #8b95a1;
      margin: 0 0 24px;
    }

    .success-info {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding: 16px;
      background: #f7f8fa;
      border-radius: 8px;

      .info-item {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        font-size: 13px;
        color: #4e5969;
      }
    }
  }
}
</style>
