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
          <div class="header-with-search">
            <div class="section-title">常见问题</div>
            <el-input
              v-model="faqSearchQuery"
              placeholder="搜索您遇到的问题..."
              prefix-icon="Search"
              clearable
              class="faq-search-input"
            />
          </div>
          <el-collapse v-model="activeCollapse" accordion class="faq-collapse">
            <el-collapse-item v-for="(faq, index) in filteredFaqs" :key="index" :title="faq.question" :name="index">
              <div class="faq-answer">{{ faq.answer }}</div>
            </el-collapse-item>
            <el-empty v-if="filteredFaqs.length === 0" description="未找到相关问题" />
          </el-collapse>
          
          <div class="support-footer">
            <span>没找到答案？</span>
            <el-button link type="primary" @click="handleContactSupport">联系在线客服</el-button>
          </div>
        </template>

        <!-- 功能介绍 -->
        <template v-else-if="activeMenu === 'features'">
          <div class="section-title">功能介绍</div>
          <div class="feature-list">
            <div class="feature-item card-hover">
              <div class="feature-icon icon-blue"><el-icon><ChatDotRound /></el-icon></div>
              <div class="feature-info">
                <h3>即时沟通</h3>
                <p>支持单聊、群聊，消息实时同步，沟通更高效。支持引用回复、消息已读回执。</p>
              </div>
            </div>
            <div class="feature-item card-hover">
              <div class="feature-icon icon-green"><el-icon><Files /></el-icon></div>
              <div class="feature-info">
                <h3>文件传输</h3>
                <p>支持图片、文件、音频、视频传输，重要资料随时保存。支持 100MB 大文件。</p>
              </div>
            </div>
            <div class="feature-item card-hover">
              <div class="feature-icon icon-purple"><el-icon><VideoCamera /></el-icon></div>
              <div class="feature-info">
                <h3>音视频通话</h3>
                <p>高清流畅的音视频通话体验，随时随地面对面沟通，支持桌面端屏幕共享。</p>
              </div>
            </div>
          </div>
        </template>

        <!-- 意见反馈 -->
        <template v-else-if="activeMenu === 'feedback'">
          <div class="section-title">意见反馈</div>
          <div class="feedback-container">
            <el-form :model="feedbackForm" label-position="top" :rules="feedbackRules" ref="feedbackFormRef">
              <el-form-item label="反馈类型" prop="type">
                <el-radio-group v-model="feedbackForm.type" size="small">
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
                  placeholder="请详细描述您遇到的问题或建议，我们会认真对待每一份反馈..." 
                />
              </el-form-item>
              <el-form-item label="图片证据 (选填)">
                <el-upload
                  action="#"
                  list-type="picture-card"
                  :auto-upload="false"
                  :limit="3"
                  v-model:file-list="fileList"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="联系方式 (选填)" prop="contact">
                <el-input v-model="feedbackForm.contact" placeholder="留下您的手机号或邮箱，方便我们回访" />
              </el-form-item>
              <div class="form-footer">
                <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitFeedback">提交反馈</el-button>
              </div>
            </el-form>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import { ChatDotRound, Files, VideoCamera, Search, Plus } from '@element-plus/icons-vue'
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
const faqSearchQuery = ref('')
const fileList = ref([])

const menuItems = [
  { id: 'faq', label: '常见问题', icon: 'help_outline' },
  { id: 'features', label: '功能介绍', icon: 'tips_and_updates' },
  { id: 'feedback', label: '意见反馈', icon: 'feedback' }
]

const faqs = [
  {
    question: '如何修改登录密码？',
    answer: '请在“侧边栏设置 > 账号与安全”中修改您的登录密码。如果忘记密码，请联系您的系统管理员进行重置。'
  },
  {
    question: '消息为什么显示发送失败？',
    answer: '通常由网络不稳定引起。请检查您的网络连接。若连接正常但仍失败，请检查是否处于内网环境，并尝试重新登录。'
  },
  {
    question: '如何创建群聊？',
    answer: '在消息列表顶部点击“+”号按钮，选择“创建群组”，然后从联系人列表中选择成员即可一键开群。'
  },
  {
    question: '文件传输有大小限制吗？',
    answer: '本系统目前支持最大 100MB 的单个文件传输。超大文件推荐通过“云盘”模块共享，支持分片上传。'
  },
  {
    question: '如何切换深色模式？',
    answer: '点击左下角侧边栏的主题图标，或在“设置 > 通用设置”中进行外观主题切换。'
  }
]

const filteredFaqs = computed(() => {
  if (!faqSearchQuery.value) return faqs
  const query = faqSearchQuery.value.toLowerCase()
  return faqs.filter(f => 
    f.question.toLowerCase().includes(query) || 
    f.answer.toLowerCase().includes(query)
  )
})

const handleContactSupport = () => {
  ElMessage.info('正在连接在线客服，请稍候...')
}

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
      // 模拟反馈提交 API 请求
      setTimeout(() => {
        submitting.value = false
        ElMessage.success('感谢您的反馈，我们的团队会认真研读！')
        feedbackForm.content = ''
        feedbackForm.contact = ''
        fileList.value = []
        visible.value = false
      }, 1500)
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
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .dark & { background: #0f172a; border-right-color: #334155; }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    font-size: 14px;
    color: #4b5563;
    cursor: pointer;
    transition: all 0.2s ease;
    border-radius: 8px;
    
    .dark & { color: #94a3b8; }

    .menu-icon { font-size: 20px; color: #64748b; transition: all 0.2s; }
    
    &:hover { 
      background: rgba(0,0,0,0.04); 
      color: #1f2329;
      .dark & { background: rgba(255,255,255,0.06); color: #f1f5f9; }
    }
    
    &.active {
      background: #eff6ff;
      color: #0089ff;
      font-weight: 600;
      .menu-icon { color: #0089ff; }
      .dark & { background: rgba(0, 137, 255, 0.15); color: #38bdf8; .menu-icon { color: #38bdf8; } }
    }
  }
}

.help-main {
  flex: 1;
  padding: 32px;
  background: #fff;
  overflow-y: auto;
  
  .dark & { background: #1e293b; color: #f1f5f9; }

  .header-with-search {
    margin-bottom: 24px;
    .section-title { margin-bottom: 12px; font-size: 20px; font-weight: 700; }
  }

  .faq-search-input {
    :deep(.el-input__wrapper) {
      border-radius: 20px;
      padding-left: 16px;
    }
  }

  .section-title {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 24px;
    color: #1f2329;
    .dark & { color: #f1f5f9; }
  }
}

.faq-collapse {
  border: none;
  :deep(.el-collapse-item) {
    background: #f8fafc;
    border-radius: 12px;
    margin-bottom: 12px;
    padding: 0 16px;
    border: 1px solid #f1f5f9;
    .dark & { background: #0f172a; border-color: #334155; }
    
    .el-collapse-item__header {
      background: transparent; border-bottom: none; height: 54px; font-weight: 500;
    }
    .el-collapse-item__wrap {
      background: transparent; border-bottom: none;
    }
  }
}

.support-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 14px;
  color: #8f959e;
  padding: 20px;
  border-top: 1px dashed #f0f0f0;
  .dark & { border-top-color: #334155; }
}

.card-hover {
  transition: all 0.3s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
}

.feedback-container {
  background: #f8fafc;
  padding: 24px;
  border-radius: 16px;
  .dark & { background: #0f172a; }
  
  :deep(.el-upload--picture-card) {
    width: 80px; height: 80px; border-radius: 8px; line-height: 90px;
  }
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 80px; height: 80px; border-radius: 8px;
  }
}

.form-footer {
  margin-top: 24px;
  .submit-btn { width: 100%; height: 44px; border-radius: 8px; font-weight: 600; font-size: 16px; }
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
