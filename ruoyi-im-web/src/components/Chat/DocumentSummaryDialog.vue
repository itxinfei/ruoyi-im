<template>
  <el-dialog
    v-model="visible"
    title="AI文档摘要"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="文档内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="8"
          placeholder="请输入需要生成摘要的文档内容..."
        />
      </el-form-item>

      <el-form-item label="摘要类型">
        <el-radio-group v-model="form.summaryType">
          <el-radio label="brief">简要摘要</el-radio>
          <el-radio label="normal">标准摘要</el-radio>
          <el-radio label="detailed">详细摘要</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="摘要语言">
        <el-select v-model="form.language" placeholder="请选择语言">
          <el-option label="中文" value="zh" />
          <el-option label="English" value="en" />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 摘要结果 -->
    <div v-if="summaryResult" class="summary-result">
      <el-divider>摘要结果</el-divider>
      <div class="summary-content">
        <p class="summary-text">{{ summaryResult.summary }}</p>
        <div v-if="summaryResult.keyPoints && summaryResult.keyPoints.length" class="key-points">
          <h4>关键要点：</h4>
          <ul>
            <li v-for="(point, index) in summaryResult.keyPoints" :key="index">
              {{ point }}
            </li>
          </ul>
        </div>
        <div class="summary-stats">
          <span>原文长度: {{ summaryResult.originalLength }} 字符</span>
          <span>摘要长度: {{ summaryResult.summaryLength }} 字符</span>
          <span>压缩率: {{ compressionRate }}%</span>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!form.content.trim()"
        @click="generateSummary"
      >
        {{ loading ? '生成中...' : '生成摘要' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { summarize } from '@/api/im'

const visible = ref(false)
const loading = ref(false)
const form = ref({
  content: '',
  summaryType: 'normal',
  language: 'zh'
})
const summaryResult = ref(null)

const compressionRate = computed(() => {
  if (!summaryResult.value) return 0
  return Math.round(
    (1 - summaryResult.value.summaryLength / summaryResult.value.originalLength) * 100
  )
})

const open = (content = '') => {
  visible.value = true
  if (content) {
    form.value.content = content
  }
  summaryResult.value = null
}

const handleClose = () => {
  visible.value = false
  form.value = {
    content: '',
    summaryType: 'normal',
    language: 'zh'
  }
  summaryResult.value = null
}

const generateSummary = async () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  loading.value = true
  try {
    const res = await summarize({
      content: form.value.content,
      summaryType: form.value.summaryType,
      language: form.value.language
    })

    if (res.code === 200 && res.data) {
      summaryResult.value = res.data
      ElMessage.success('摘要生成成功')
    } else {
      ElMessage.error(res.msg || '生成摘要失败')
    }
  } catch (error) {
    console.error('生成摘要失败:', error)
    ElMessage.error('生成摘要失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

defineExpose({
  open
})
</script>

<script>
export default {
  name: 'DocumentSummaryDialog'
}
</script>

<style scoped>
.summary-result {
  margin-top: 20px;
}

.summary-content {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.summary-text {
  line-height: 1.8;
  color: #303133;
  margin: 0 0 16px 0;
}

.key-points {
  margin-top: 16px;
}

.key-points h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.key-points ul {
  margin: 0;
  padding-left: 20px;
}

.key-points li {
  line-height: 1.6;
  color: #606266;
  margin-bottom: 4px;
}

.summary-stats {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
  color: #909399;
}
</style>
