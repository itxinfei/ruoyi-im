<template>
  <el-dialog
    v-model="visible"
    title="帮助与反馈"
    width="640px"
    class="help-feedback-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="help-container">
      <!-- 侧边导航 -->
      <div class="help-aside">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          class="menu-item"
          :class="{ active: activeMenu === item.id }"
          @click="activeMenu = item.id"
        >
          <span class="material-icons-outlined menu-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="help-main scrollbar-thin">
        <!-- 常见问题 -->
        <template v-if="activeMenu === 'faq'">
          <div class="section-title">常见问题</div>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item v-for="(faq, index) in faqs" :key="index" :title="faq.question" :name="index">
              <div class="faq-answer">{{ faq.answer }}</div>
            </el-collapse-item>
          </el-collapse>
        </template>

        <!-- 功能介绍 -->
        <template v-else-if="activeMenu === 'features'">
          <div class="section-title">功能介绍</div>
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon icon-blue"><el-icon><ChatDotRound /></el-icon></div>
              <div class="feature-info">
                <h3>即时沟通</h3>
                <p>支持单聊、群聊，消息实时同步，沟通更高效。</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon icon-green"><el-icon><Files /></el-icon></div>
              <div class="feature-info">
                <h3>文件传输</h3>
                <p>支持图片、文件断点续传，重要资料随时保存。</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon icon-purple"><el-icon><VideoCamera /></el-icon></div>
              <div class="feature-info">
                <h3>音视频通话</h3>
                <p>高清流畅的音视频通话体验，随时随地面对面。</p>
              </div>
            </div>
          </div>
        </template>

        <!-- 意见反馈 -->
        <template v-else-if="activeMenu === 'feedback'">
          <div class="section-title">意见反馈</div>
          <el-form :model="feedbackForm" label-position="top" :rules="feedbackRules" ref="feedbackFormRef">
            <el-form-item label="反馈类型" prop="type">
              <el-radio-group v-model="feedbackForm.type">
                <el-radio-button label="bug">功能异常</el-radio-button>
                <el-radio-button label="suggestion">产品建议</el-radio-button>
                <el-radio-button label="other">其他</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="问题描述" prop="content">
              <el-input 
                v-model="feedbackForm.content" 
                type="textarea" 
                :rows="4" 
                placeholder="请详细描述您遇到的问题或建议..." 
              />
            </el-form-item>
            <el-form-item label="联系方式 (选填)" prop="contact">
              <el-input v-model="feedbackForm.contact" placeholder="手机号/邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="submitFeedback">提交反馈</el-button>
            </el-form-item>
          </el-form>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { ChatDotRound, Files, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const activeMenu = ref('faq')
const activeCollapse = ref('')
const submitting = ref(false)
const feedbackFormRef = ref(null)

const menuItems = [
  { id: 'faq', label: '常见问题', icon: 'help_outline' },
  { id: 'features', label: '功能介绍', icon: 'tips_and_updates' },
  { id: 'feedback', label: '意见反馈', icon: 'feedback' }
]

const faqs = [
  {
    question: '如何修改登录密码？',
    answer: '请在“设置 > 账号与安全”中修改您的登录密码。如果忘记密码，请联系管理员重置。'
  },
  {
    question: '消息为什么显示发送失败？',
    answer: '请检查您的网络连接是否正常。如果网络正常，可能是服务器暂时繁忙，请稍后重试。'
  },
  {
    question: '如何创建群聊？',
    answer: '在消息列表顶部点击“+”号按钮，选择“创建群组”，然后选择联系人即可创建群聊。'
  },
  {
    question: '文件传输有大小限制吗？',
    answer: '目前支持最大 100MB 的文件传输。如果需要传输超大文件，建议使用云盘功能。'
  }
]

const feedbackForm = reactive({
  type: 'bug',
  content: '',
  contact: ''
})

const feedbackRules = {
  content: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 5, message: '描述太短了，请多写一点吧', trigger: 'blur' }
  ]
}

const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  await feedbackFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      // 模拟 API 请求
      setTimeout(() => {
        submitting.value = false
        ElMessage.success('感谢您的反馈，我们会尽快处理！')
        feedbackForm.content = ''
        feedbackForm.contact = ''
        // 可选：重置类型
        // feedbackForm.type = 'bug'
      }, 1000)
    }
  })
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.help-feedback-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 500px;
}

.help-container {
  display: flex;
  height: 100%;
}

.help-aside {
  width: 160px;
  background: #f8fafc;
  border-right: 1px solid #f2f3f5;
  padding: 12px 0;
  
  .dark & { background: #0f172a; border-right-color: #334155; }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 20px;
    font-size: 14px;
    color: #1f2329;
    cursor: pointer;
    transition: all 0.2s;
    
    .dark & { color: #cbd5e1; }

    .menu-icon { font-size: 20px; color: #646a73; }
    
    &:hover { background: #f2f3f5; .dark & { background: #1e293b; } }
    
    &.active {
      background: #e1f0ff;
      color: #0089ff;
      font-weight: 500;
      .menu-icon { color: #0089ff; }
      .dark & { background: rgba(0, 137, 255, 0.15); }
    }
  }
}

.help-main {
  flex: 1;
  padding: 24px;
  background: #fff;
  overflow-y: auto;
  
  .dark & { background: #1e293b; color: #f1f5f9; }

  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #1f2329;
    .dark & { color: #f1f5f9; }
  }
}

.faq-answer {
  color: #646a73;
  line-height: 1.6;
  font-size: 14px;
  .dark & { color: #94a3b8; }
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  .dark & { background: #0f172a; }

  .feature-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
    
    &.icon-blue { background: #e6f7ff; color: #1890ff; .dark & { background: rgba(24, 144, 255, 0.2); } }
    &.icon-green { background: #f6ffed; color: #52c41a; .dark & { background: rgba(82, 196, 26, 0.2); } }
    &.icon-purple { background: #f9f0ff; color: #722ed1; .dark & { background: rgba(114, 46, 209, 0.2); } }
  }

  .feature-info {
    h3 { font-size: 16px; font-weight: 600; margin: 0 0 4px 0; color: #1f2329; .dark & { color: #f1f5f9; } }
    p { font-size: 13px; color: #646a73; margin: 0; line-height: 1.5; .dark & { color: #94a3b8; } }
  }
}

:deep(.el-collapse-item__header) {
  font-size: 15px;
  .dark & { background: transparent; color: #e2e8f0; border-bottom-color: #334155; }
}
:deep(.el-collapse-item__wrap) {
  .dark & { background: transparent; border-bottom-color: #334155; }
}
:deep(.el-form-item__label) {
  .dark & { color: #e2e8f0; }
}

.scrollbar-thin::-webkit-scrollbar { width: 4px; }
.scrollbar-thin::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 4px; }
.dark .scrollbar-thin::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); }
</style>
