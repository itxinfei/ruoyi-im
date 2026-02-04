<template>
  <el-dialog
    title="发起审批"
    v-model="visible"
    width="600px"
    @close="handleClose"
  >
    <!-- 第一步：选择模板 -->
    <div v-if="step === 1" class="template-grid">
      <div 
        v-for="tpl in templates" 
        :key="tpl.id" 
        class="template-card"
        @click="selectTemplate(tpl)"
      >
        <div class="tpl-icon">
          <img v-if="tpl.icon" :src="tpl.icon" />
          <el-icon v-else><Document /></el-icon>
        </div>
        <div class="tpl-name">{{ tpl.name }}</div>
      </div>
      <el-empty v-if="templates.length === 0" description="暂无可用模板" />
    </div>

    <!-- 第二步：填写表单 -->
    <div v-if="step === 2" class="form-section">
      <div class="form-header">
        <el-button link :icon="ArrowLeft" @click="step = 1">返回重选</el-button>
        <span class="selected-name">{{ selectedTemplate?.name }}</span>
      </div>

      <el-form :model="formData" label-width="100px" class="mt-20">
        <el-form-item label="审批标题">
           <el-input v-model="title" placeholder="请输入标题 (如: 张三的请假申请)" />
        </el-form-item>

        <template v-for="field in formSchema" :key="field.key">
          <el-form-item :label="field.label">
            <el-input 
              v-if="field.type === 'text'" 
              v-model="formData[field.key]" 
              :placeholder="'请输入' + field.label" 
            />
            <el-input 
              v-else-if="field.type === 'textarea'" 
              v-model="formData[field.key]" 
              type="textarea" 
              :rows="3"
            />
            <el-input-number 
              v-else-if="field.type === 'number'" 
              v-model="formData[field.key]" 
              style="width: 100%"
            />
            <el-date-picker
              v-else-if="field.type === 'datetime'"
              v-model="formData[field.key]"
              type="datetime"
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="formData[field.key]"
              type="date"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </template>
      </el-form>
    </div>

    <template #footer>
      <div v-if="step === 2">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { getTemplates, createApproval } from '@/api/im/approval'
import { Document, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const step = ref(1)
const templates = ref([])
const selectedTemplate = ref(null)
const submitting = ref(false)

const title = ref('')
const formData = ref({})

const formSchema = computed(() => {
  if (!selectedTemplate.value || !selectedTemplate.value.formSchema) return []
  try {
    return JSON.parse(selectedTemplate.value.formSchema)
  } catch (e) {
    console.error('解析方案失败', e)
    return []
  }
})

const fetchTemplates = async () => {
  try {
    const res = await getTemplates()
    if (res.code === 200) {
      templates.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const selectTemplate = (tpl) => {
  selectedTemplate.value = tpl
  title.value = `我的${tpl.name}`
  formData.value = {}
  step.value = 2
}

const handleSubmit = async () => {
  if (!title.value) {
    ElMessage.warning('请输入审批标题')
    return
  }
  submitting.value = true
  try {
    const res = await createApproval(
      { templateId: selectedTemplate.value.id, title: title.value },
      formData.value
    )
    if (res.code === 200) {
      ElMessage.success('审批申请已提交')
      visible.value = false
      emit('success')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
  step.value = 1
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    fetchTemplates()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
.template-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 10px;
}

.template-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: var(--dt-radius-lg);
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #0089ff;
    background: #f0f7ff;
    transform: translateY(-2px);
  }

  .tpl-icon {
    width: 48px;
    height: 48px;
    background: #e6f7ff;
    border-radius: var(--dt-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #1890ff;
    font-size: 24px;
    img { width: 100%; height: 100%; border-radius: var(--dt-radius-lg); object-fit: cover; }
  }

  .tpl-name {
    font-size: 14px;
    color: #333;
    font-weight: 500;
  }
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
  .selected-name {
    font-size: 16px;
    font-weight: bold;
  }
}

.mt-20 { margin-top: 20px; }
</style>
