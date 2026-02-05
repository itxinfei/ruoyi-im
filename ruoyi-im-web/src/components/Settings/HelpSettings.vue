<template>
  <div class="help-settings">
    <!-- 常见问题 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-blue">
          <span class="material-icons-outlined">help_outline</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            常见问题
          </h3>
          <p class="section-desc">
            查看常见问题的解答
          </p>
        </div>
      </div>
      
      <div class="setting-card">
        <div class="faq-list">
          <div 
            v-for="(faq, index) in faqs" 
            :key="index" 
            class="faq-item"
            :class="{ expanded: faq.expanded }"
          >
            <div
              class="faq-question"
              @click="faq.expanded = !faq.expanded"
            >
              <div class="question-content">
                <span class="question-number">{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="question-text">{{ faq.question }}</span>
              </div>
              <span class="material-icons-outlined expand-icon">expand_more</span>
            </div>
            <Transition name="expand">
              <div
                v-show="faq.expanded"
                class="faq-answer"
              >
                {{ faq.answer }}
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </section>

    <!-- 意见反馈 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-green">
          <span class="material-icons-outlined">feedback</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            意见反馈
          </h3>
          <p class="section-desc">
            帮助我们改进产品
          </p>
        </div>
      </div>
      
      <div class="setting-card feedback-card">
        <div class="feedback-types">
          <div 
            class="type-card"
            :class="{ active: feedbackForm.type === 'feature' }"
            @click="feedbackForm.type = 'feature'"
          >
            <div class="type-icon bg-blue-light">
              <span class="material-icons-outlined">lightbulb</span>
            </div>
            <span class="type-name">功能建议</span>
          </div>
          <div 
            class="type-card"
            :class="{ active: feedbackForm.type === 'bug' }"
            @click="feedbackForm.type = 'bug'"
          >
            <div class="type-icon bg-red-light">
              <span class="material-icons-outlined">bug_report</span>
            </div>
            <span class="type-name">问题反馈</span>
          </div>
        </div>

        <div class="feedback-form">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的问题或建议，我们会尽快处理..."
            resize="none"
          />
          <div class="form-actions">
            <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              <span
                class="material-icons-outlined"
                style="font-size: 16px; margin-right: 4px;"
              >send</span>
              提交反馈
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 联系方式 -->
    <section class="setting-section">
      <div class="section-header">
        <div class="section-icon-wrapper bg-gradient-purple">
          <span class="material-icons-outlined">support_agent</span>
        </div>
        <div class="section-title-group">
          <h3 class="section-title">
            联系客服
          </h3>
          <p class="section-desc">
            需要更多帮助？联系我们的客服团队
          </p>
        </div>
      </div>
      
      <div class="contact-cards">
        <div class="contact-card">
          <div class="contact-icon bg-blue-light">
            <span class="material-icons-outlined">email</span>
          </div>
          <div class="contact-info">
            <div class="contact-title">
              邮件支持
            </div>
            <div class="contact-value">
              support@ruoyi-im.com
            </div>
          </div>
        </div>
        <div class="contact-card">
          <div class="contact-icon bg-green-light">
            <span class="material-icons-outlined">chat</span>
          </div>
          <div class="contact-info">
            <div class="contact-title">
              在线客服
            </div>
            <div class="contact-value">
              工作日 9:00-18:00
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const faqs = reactive([
  { 
    question: '如何修改我的个人资料和头像？', 
    answer: '在"账号与安全"页面中，点击头像或昵称旁边的编辑按钮，即可修改您的个人资料。头像支持 JPG、PNG 格式，建议尺寸 200x200 像素。', 
    expanded: false 
  },
  { 
    question: '如何设置消息免打扰？', 
    answer: '在"消息通知"页面中，您可以开启"自动免打扰"功能，并设置生效时间段。在该时间段内，系统将自动关闭通知提醒。', 
    expanded: false 
  },
  { 
    question: '聊天记录会保存多久？', 
    answer: '聊天记录默认永久保存在云端。您也可以在"存储管理"中导出聊天记录到本地进行备份。如果开启了"退出时保留数据"，本地缓存也会被保留。', 
    expanded: false 
  },
  { 
    question: '支持哪些快捷键？', 
    answer: '常用快捷键包括：Ctrl+Enter 发送消息（可设置为 Enter）、Ctrl+K 打开全局搜索、Alt+A 截图、Ctrl+D 删除会话。您可以在"消息通知"页面设置发送快捷键。', 
    expanded: false 
  },
  { 
    question: '如何切换界面主题？', 
    answer: '在"通用设置"页面中，您可以选择浅色、深色或跟随系统的主题模式。在"主题外观"页面，还可以自定义主题颜色。', 
    expanded: false 
  }
])

const feedbackForm = reactive({ type: 'feature', content: '' })
const submitting = ref(false)

const handleSubmit = async () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  submitting.value = true
  await new Promise(r => setTimeout(r, 1000))
  ElMessage.success('感谢您的反馈！我们会尽快处理')
  feedbackForm.content = ''
  submitting.value = false
}
</script>

<style scoped lang="scss">
.help-settings {
  max-width: 720px;
}

.setting-section {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.section-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
    color: white;
  }
  
  &.bg-gradient-blue {
    background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
  }
  
  &.bg-gradient-green {
    background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
  }
  
  &.bg-gradient-purple {
    background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
  }
}

.section-title-group {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0;
}

.setting-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
}

// FAQ 样式
.faq-list {
  padding: 8px;
}

.faq-item {
  border-bottom: 1px solid var(--dt-border-light);
  
  &:last-child {
    border-bottom: none;
  }
  
  &.expanded {
    .expand-icon {
      transform: rotate(180deg);
    }
    
    .faq-question {
      background: var(--dt-bg-hover);
    }
  }
}

.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  transition: all 0.2s ease;
  
  &:hover {
    background: var(--dt-bg-hover);
  }
}

.question-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.question-number {
  font-size: 12px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-brand-color);
  background: var(--dt-brand-bg);
  padding: 4px 8px;
  border-radius: var(--dt-radius-sm);
}

.question-text {
  font-size: 14px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

.expand-icon {
  font-size: 20px;
  color: var(--dt-text-tertiary);
  transition: transform 0.3s ease;
}

.faq-answer {
  padding: 0 16px 16px 52px;
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.8;
}

// 反馈卡片
.feedback-card {
  padding: 24px;
}

.feedback-types {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.type-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border: 2px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--dt-brand-color);
    background: var(--dt-bg-hover);
  }
  
  &.active {
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
    
    .type-name {
      color: var(--dt-brand-color);
      font-weight: var(--dt-font-weight-medium);
    }
  }
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
  }
  
  &.bg-blue-light {
    background: rgba(24, 144, 255, 0.1);
    color: #1890ff;
  }
  
  &.bg-red-light {
    background: rgba(245, 34, 45, 0.1);
    color: #f5222d;
  }
}

.type-name {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.feedback-form {
  .form-actions {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}

// 联系卡片
.contact-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.contact-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-lg);
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-sm);
  }
}

.contact-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  
  span {
    font-size: 24px;
  }
  
  &.bg-blue-light {
    background: rgba(24, 144, 255, 0.1);
    color: #1890ff;
  }
  
  &.bg-green-light {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }
}

.contact-info {
  flex: 1;
}

.contact-title {
  font-size: 14px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.contact-value {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

// 展开动画
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  opacity: 1;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  padding: 0 16px 0 52px;
}

// 暗黑模式适配
.dark {
  .setting-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
  
  .faq-item {
    border-color: var(--dt-border-dark);
  }
  
  .faq-question:hover,
  .faq-item.expanded .faq-question {
    background: var(--dt-bg-hover-dark);
  }
  
  .type-card {
    border-color: var(--dt-border-dark);
    
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
  
  .contact-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}
</style>
