<template>
  <el-dialog
    v-model="visible"
    title=""
    width="800px"
    :close-on-click-modal="true"
    class="help-feedback-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="help-container">
      <!-- 顶部标签导航 -->
      <div class="tabs-header">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          class="tab-item"
          :class="{ active: activeMenu === item.id }"
          @click="activeMenu = item.id"
        >
          <span class="material-icons-outlined tab-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area scrollbar-thin">
        <!-- 常见问题 -->
        <template v-if="activeMenu === 'faq'">
          <div class="faq-section">
            <div class="section-header">
              <h2 class="section-title">
                常见问题
              </h2>
              <el-input
                v-model="faqSearchQuery"
                placeholder="搜索您遇到的问题..."
                prefix-icon="Search"
                clearable
                class="faq-search-input"
              />
            </div>
            <div class="faq-list">
              <div 
                v-for="(faq, index) in filteredFaqs" 
                :key="index" 
                class="faq-item"
                @click="toggleFaq(index)"
              >
                <div class="faq-question">
                  <span class="question-text">{{ faq.question }}</span>
                  <el-icon
                    class="arrow-icon"
                    :class="{ expanded: activeCollapse === index }"
                  >
                    <ArrowDown />
                  </el-icon>
                </div>
                <div
                  class="faq-answer"
                  :class="{ show: activeCollapse === index }"
                >
                  {{ faq.answer }}
                </div>
              </div>
              <el-empty
                v-if="filteredFaqs.length === 0"
                description="未找到相关问题"
                :image-size="100"
              />
            </div>
            <div class="support-footer">
              <span>没找到答案？</span>
              <el-button
                type="primary"
                link
                @click="handleContactSupport"
              >
                <el-icon><Service /></el-icon>
                联系在线客服
              </el-button>
            </div>
          </div>
        </template>

        <!-- 功能介绍 -->
        <template v-else-if="activeMenu === 'features'">
          <div class="features-section">
            <h2 class="section-title">
              功能介绍
            </h2>
            <div class="feature-grid">
              <div class="feature-card">
                <div class="feature-icon-wrapper chat-bg">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>即时沟通</h3>
                  <p>支持单聊、群聊，消息实时同步，沟通更高效。支持引用回复、消息已读回执。</p>
                </div>
              </div>
              <div class="feature-card">
                <div class="feature-icon-wrapper file-bg">
                  <el-icon><Files /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>文件传输</h3>
                  <p>支持图片、文件、音频、视频传输，重要资料随时保存。支持 100MB 大文件。</p>
                </div>
              </div>
              <div class="feature-card">
                <div class="feature-icon-wrapper video-bg">
                  <el-icon><Camera /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>音视频通话</h3>
                  <p>高清流畅的音视频通话体验，随时随地面对面沟通，支持桌面端屏幕共享。</p>
                </div>
              </div>
              <div class="feature-card">
                <div class="feature-icon-wrapper screen-bg">
                  <el-icon><Monitor /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>屏幕共享</h3>
                  <p>一键共享屏幕，方便远程协作和演示，提升工作效率。</p>
                </div>
              </div>
              <div class="feature-card">
                <div class="feature-icon-wrapper group-bg">
                  <el-icon><User /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>群组管理</h3>
                  <p>创建和管理群组，支持设置群公告、群文件、群成员管理等丰富功能。</p>
                </div>
              </div>
              <div class="feature-card">
                <div class="feature-icon-wrapper cloud-bg">
                  <el-icon><Folder /></el-icon>
                </div>
                <div class="feature-content">
                  <h3>云盘存储</h3>
                  <p>个人云盘空间，文件随时存储、分享，支持在线预览和下载。</p>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 意见反馈 -->
        <template v-else-if="activeMenu === 'feedback'">
          <div class="feedback-section">
            <h2 class="section-title">
              意见反馈
            </h2>
            <div class="feedback-card">
              <el-form
                ref="feedbackFormRef"
                :model="feedbackForm"
                label-position="top"
                :rules="feedbackRules"
              >
                <el-form-item
                  label="反馈类型"
                  prop="type"
                >
                  <el-radio-group
                    v-model="feedbackForm.type"
                    size="large"
                  >
                    <el-radio-button label="bug">
                      <el-icon><Warning /></el-icon>
                      功能异常
                    </el-radio-button>
                    <el-radio-button label="suggestion">
                      <el-icon><Lightbulb /></el-icon>
                      产品建议
                    </el-radio-button>
                    <el-radio-button label="other">
                      <el-icon><ChatDotRound /></el-icon>
                      其他
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item
                  label="问题描述"
                  prop="content"
                >
                  <el-input 
                    v-model="feedbackForm.content" 
                    type="textarea" 
                    :rows="5" 
                    placeholder="请详细描述您遇到的问题或建议，我们会认真对待每一份反馈..." 
                    show-word-limit
                    maxlength="500"
                  />
                </el-form-item>
                <el-form-item label="图片证据 (选填)">
                  <el-upload
                    v-model:file-list="fileList"
                    action="#"
                    list-type="picture-card"
                    :auto-upload="false"
                    :limit="3"
                    class="feedback-upload"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div class="upload-tip">
                    最多上传 3 张图片，每张不超过 5MB
                  </div>
                </el-form-item>
                <el-form-item
                  label="联系方式 (选填)"
                  prop="contact"
                >
                  <el-input 
                    v-model="feedbackForm.contact" 
                    placeholder="留下您的手机号或邮箱，方便我们回访" 
                    prefix-icon="Message"
                  />
                </el-form-item>
                <div class="form-footer">
                  <el-button
                    type="primary"
                    class="submit-btn"
                    :loading="submitting"
                    @click="submitFeedback"
                  >
                    <el-icon v-if="!submitting">
                      <Position />
                    </el-icon>
                    {{ submitting ? '提交中...' : '提交反馈' }}
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import {
  ChatDotRound,
  Files,
  Camera,
  Search,
  Plus,
  ArrowDown,
  Service,
  Monitor,
  User,
  Folder,
  Warning,
  Message,
  Position
} from '@element-plus/icons-vue'
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
  if (!faqSearchQuery.value) {return faqs}
  const query = faqSearchQuery.value.toLowerCase()
  return faqs.filter(f => 
    f.question.toLowerCase().includes(query) || 
    f.answer.toLowerCase().includes(query)
  )
})

const handleContactSupport = () => {
  ElMessage.info('正在连接在线客服，请稍候...')
}

const toggleFaq = index => {
  if (activeCollapse.value === index) {
    activeCollapse.value = ''
  } else {
    activeCollapse.value = index
  }
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
  if (!feedbackFormRef.value) {return}
  
  await feedbackFormRef.value.validate(valid => {
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

watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.help-feedback-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
    height: 600px;
    overflow: hidden;
  }
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-2xl);
  }
}

.help-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-card);
}

.tabs-header {
  display: flex;
  gap: 0;
  padding: 0 24px;
  border-bottom: 2px solid var(--dt-border-light);

  .tab-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 18px 20px;
    font-size: 15px;
    font-weight: 500;
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    position: relative;

    .tab-icon {
      font-size: 20px;
      color: var(--dt-text-tertiary);
      transition: all var(--dt-transition-fast);
    }

    &:hover {
      color: var(--dt-text-primary);

      .tab-icon {
        color: var(--dt-brand-color);
      }
    }

    &.active {
      color: var(--dt-brand-color);
      font-weight: 600;

      .tab-icon {
        color: var(--dt-brand-color);
      }

      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        right: 0;
        height: 2px;
        background: var(--dt-brand-color);
        border-radius: var(--dt-radius-sm);
      }
    }
  }
}

.content-area {
  flex: 1;
  padding: 32px 40px;
  overflow-y: auto;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--dt-text-primary);
}

.faq-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    gap: 16px;

    .section-title {
      margin-bottom: 0;
      flex-shrink: 0;
    }

    .faq-search-input {
      flex: 1;
      max-width: 300px;

      :deep(.el-input__wrapper) {
        border-radius: var(--dt-radius-full);
        padding-left: 16px;
        box-shadow: var(--dt-shadow-2);
        transition: all var(--dt-transition-fast);

        &:hover {
          box-shadow: var(--dt-shadow-3);
          border-color: var(--dt-brand-color);
        }

        &.is-focus {
          box-shadow: 0 0 0 3px var(--dt-brand-lighter);
        }
      }
    }
  }

  .faq-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .faq-item {
    background: var(--dt-bg-body);
    border: 1.5px solid var(--dt-border-light);
    border-radius: var(--dt-radius-lg);
    overflow: hidden;
    transition: all var(--dt-transition-fast);
    cursor: pointer;

    &:hover {
      border-color: var(--dt-brand-color);
      box-shadow: var(--dt-shadow-3);
    }

    .faq-question {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      font-size: 15px;
      font-weight: 500;
      color: var(--dt-text-primary);

      .question-text {
        flex: 1;
        padding-right: 16px;
      }

      .arrow-icon {
        font-size: 16px;
        color: var(--dt-text-tertiary);
        transition: transform var(--dt-transition-base);

        &.expanded {
          transform: rotate(180deg);
        }
      }
    }

    .faq-answer {
      max-height: 0;
      overflow: hidden;
      padding: 0 20px;
      color: var(--dt-text-secondary);
      line-height: 1.6;
      font-size: 14px;
      transition: all var(--dt-transition-base);

      &.show {
        max-height: 200px;
        padding: 16px 20px;
        border-top: 1px solid var(--dt-border-light);
      }
    }
  }

  .support-footer {
    margin-top: 32px;
    text-align: center;
    font-size: 14px;
    color: var(--dt-text-tertiary);
    padding: 24px;
    border-top: 1px dashed var(--dt-border-light);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-lg);
  }
}

.features-section {
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .feature-card {
    display: flex;
    gap: 16px;
    padding: 24px;
    background: var(--dt-bg-body);
    border: 1.5px solid var(--dt-border-light);
    border-radius: var(--dt-radius-xl);
    transition: all var(--dt-transition-fast);

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--dt-shadow-float);
      border-color: var(--dt-brand-color);
    }

    .feature-icon-wrapper {
      width: 56px;
      height: 56px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      flex-shrink: 0;
      color: #fff;
      box-shadow: var(--dt-shadow-3);

      &.chat-bg {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.file-bg {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.video-bg {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.screen-bg {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      &.group-bg {
        background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
      }

      &.cloud-bg {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      }
    }

    .feature-content {
      flex: 1;

      h3 {
        font-size: 17px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: var(--dt-text-primary);
      }

      p {
        font-size: 13px;
        color: var(--dt-text-secondary);
        margin: 0;
        line-height: 1.6;
      }
    }
  }
}

.feedback-section {
  .feedback-card {
    background: var(--dt-bg-body);
    padding: 32px;
    border-radius: var(--dt-radius-xl);
    border: 1.5px solid var(--dt-border-light);

    :deep(.el-form-item__label) {
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    :deep(.el-radio-group) {
      width: 100%;

      .el-radio-button {
        flex: 1;

        .el-radio-button__inner {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          padding: 12px 16px;
          font-size: 14px;
          border-radius: var(--dt-radius-md);
          transition: all var(--dt-transition-fast);

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }

    :deep(.el-textarea__inner) {
      border-radius: var(--dt-radius-lg);
      padding: 12px 16px;
      line-height: 1.6;
      font-size: 14px;
      transition: all var(--dt-transition-fast);
      border: 1.5px solid var(--dt-border-color);

      &:focus {
        border-color: var(--dt-brand-color);
        box-shadow: 0 0 0 3px var(--dt-brand-lighter);
      }
    }

    :deep(.el-input__wrapper) {
      border-radius: var(--dt-radius-md);
      transition: all var(--dt-transition-fast);
      border: 1.5px solid var(--dt-border-color);

      &:hover {
        border-color: var(--dt-brand-color);
      }

      &.is-focus {
        border-color: var(--dt-brand-color);
        box-shadow: 0 0 0 3px var(--dt-brand-lighter);
      }
    }

    .feedback-upload {
      :deep(.el-upload--picture-card) {
        width: 100px;
        height: 100px;
        border-radius: var(--dt-radius-lg);
        border: 2px dashed var(--dt-border-color);
        background: var(--dt-bg-card);
        transition: all var(--dt-transition-fast);

        &:hover {
          border-color: var(--dt-brand-color);
          background: var(--dt-brand-bg);
        }
      }

      :deep(.el-upload-list--picture-card .el-upload-list__item) {
        width: 100px;
        height: 100px;
        border-radius: var(--dt-radius-lg);
      }
    }

    .upload-tip {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-top: 8px;
    }
  }

  .form-footer {
    margin-top: 24px;

    .submit-btn {
      width: 100%;
      height: 48px;
      border-radius: var(--dt-radius-lg);
      font-weight: 600;
      font-size: 16px;
      background: var(--dt-brand-color);
      border: none;
      box-shadow: var(--dt-shadow-brand);
      transition: all var(--dt-transition-fast);

      &:hover {
        transform: translateY(-2px);
        box-shadow: var(--dt-shadow-brand-strong); // 保持 hover 特效
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: var(--dt-black-15);
  border-radius: var(--dt-radius-sm);
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}

// 暗色模式
.dark .help-feedback-dialog {
  .help-container {
    background: var(--dt-bg-card-dark);
  }

  .tabs-header {
    border-bottom-color: var(--dt-border-dark);

    .tab-item {
      color: var(--dt-text-secondary-dark);

      .tab-icon {
        color: var(--dt-text-tertiary-dark);
      }

      &:hover {
        color: var(--dt-text-primary-dark);
      }
    }
  }

  .section-title {
    color: var(--dt-text-primary-dark);
  }

  .faq-item {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      border-color: var(--dt-brand-color);
    }

    .faq-question {
      color: var(--dt-text-primary-dark);
    }

    .faq-answer {
      color: var(--dt-text-secondary-dark);

      &.show {
        border-top-color: var(--dt-border-dark);
      }
    }
  }

  .support-footer {
    border-top-color: var(--dt-border-dark);
    background: var(--dt-bg-hover-dark);
  }

  .feature-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    .feature-content h3 {
      color: var(--dt-text-primary-dark);
    }

    .feature-content p {
      color: var(--dt-text-secondary-dark);
    }
  }

  .feedback-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    :deep(.el-form-item__label) {
      color: var(--dt-text-primary-dark);
    }

    :deep(.el-textarea__inner) {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-dark);
      color: var(--dt-text-primary-dark);
    }

    :deep(.el-input__wrapper) {
      background: var(--dt-bg-input-dark);
      border-color: var(--dt-border-dark);
    }

    .feedback-upload :deep(.el-upload--picture-card) {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    .upload-tip {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .scrollbar-thin::-webkit-scrollbar-thumb {
    background: var(--dt-white-15);
  }
}
</style>
