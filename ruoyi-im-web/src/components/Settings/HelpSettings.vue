<template>
  <div class="help-settings">
    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">常见问题</h3>
        <p class="section-desc">为您解答使用过程中的常见疑问</p>
      </div>
      
      <div class="setting-card faq-card">
        <div v-for="(faq, index) in faqs" :key="index" class="faq-item">
          <div class="faq-question" @click="faq.expanded = !faq.expanded">
            <span class="question-text">{{ faq.question }}</span>
            <el-icon class="expand-icon" :class="{ 'is-expanded': faq.expanded }"><ArrowRight /></el-icon>
          </div>
          <div v-show="faq.expanded" class="faq-answer">
            {{ faq.answer }}
          </div>
          <el-divider v-if="index < faqs.length - 1" />
        </div>
      </div>
    </div>

    <div class="setting-section">
      <div class="section-header">
        <h3 class="section-title">意见反馈</h3>
        <p class="section-desc">帮助我们改进产品，您的建议对我们很重要</p>
      </div>
      
      <div class="setting-card feedback-card">
        <el-form :model="feedbackForm" label-position="top">
          <el-form-item label="问题类型">
            <div class="type-selector">
              <div 
                class="type-option" 
                :class="{ active: feedbackForm.type === 'feature' }"
                @click="feedbackForm.type = 'feature'"
              >
                功能建议
              </div>
              <div 
                class="type-option" 
                :class="{ active: feedbackForm.type === 'bug' }"
                @click="feedbackForm.type = 'bug'"
              >
                问题反馈
              </div>
            </div>
          </el-form-item>
          <el-form-item label="详细描述">
            <el-input 
              v-model="feedbackForm.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请详细描述您遇到的问题或建议..." 
              resize="none"
            />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交反馈</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const faqs = reactive([
  { question: '如何修改密码？', answer: '在"账号"页面点击"修改密码"按钮，输入旧密码和新密码进行设置。', expanded: false },
  { question: '如何设置消息免打扰？', answer: '在会话列表中右键点击会话，选择"消息免打扰"，或在通知设置中关闭桌面通知。', expanded: false },
  { question: '聊天记录会保存多久？', answer: '默认永久保存，除非您手动清理缓存或在存储设置中关闭"退出保留数据"。', expanded: false },
  { question: '支持哪些快捷键？', answer: '我们支持 Ctrl+Enter 发送消息，Ctrl+K 搜索，Alt+A 截图等常用快捷键。', expanded: false }
])

const feedbackForm = reactive({ type: 'feature', description: '' })
const submitting = ref(false)

const handleSubmit = async () => {
  if (!feedbackForm.description.trim()) {
    ElMessage.warning('请输入描述内容')
    return
  }
  submitting.value = true
  await new Promise(r => setTimeout(r, 1000)) // Simulating API call
  ElMessage.success('感谢您的反馈！')
  feedbackForm.description = ''
  submitting.value = false
}
</script>

<style scoped lang="scss">
.help-settings {
  padding: 0;
  box-sizing: border-box;
}

.setting-section {
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  margin-bottom: 12px;
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary, #303133);
    margin: 0 0 4px 0;
  }
  
  .section-desc {
    font-size: 12px;
    color: var(--text-color-secondary, #909399);
    margin: 0;
  }
}

.setting-card {
  background: var(--bg-color-overlay, #fff);
  border: 1px solid var(--border-color-light, #e4e7ed);
  border-radius: 8px;
  overflow: hidden;
}

.faq-card {
  .faq-item {
    transition: background-color 0.2s;
  }
  
  .el-divider {
    margin: 0;
    border-color: var(--border-color-lighter, #ebeef5);
  }
}

.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  
  &:hover {
    background-color: var(--bg-color-hover, #f5f7fa);
  }
}

.question-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-primary, #303133);
}

.expand-icon {
  color: var(--text-color-secondary, #909399);
  transition: transform 0.3s;
  font-size: 14px;
  
  &.is-expanded {
    transform: rotate(90deg);
  }
}

.faq-answer {
  padding: 0 20px 20px;
  font-size: 13px;
  color: var(--text-color-secondary, #606266);
  line-height: 1.6;
  background-color: var(--bg-color-page, #f9f9f9);
}

.feedback-card {
  padding: 24px;
}

.type-selector {
  display: flex;
  gap: 12px;
}

.type-option {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid var(--border-color-light, #dcdfe6);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-color-regular, #606266);
  transition: all 0.2s;
  background-color: var(--bg-color-overlay, #fff);

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
  }

  &.active {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
