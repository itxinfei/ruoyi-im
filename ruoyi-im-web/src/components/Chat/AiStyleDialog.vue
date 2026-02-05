<template>
  <el-dialog
    v-model="visible"
    title="自定义回复风格"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form label-width="80px">
      <!-- 语气偏好 -->
      <el-form-item label="语气偏好">
        <el-radio-group v-model="styleConfig.tone">
          <el-radio-button label="formal">
            正式
          </el-radio-button>
          <el-radio-button label="polite">
            礼貌
          </el-radio-button>
          <el-radio-button label="casual">
            随意
          </el-radio-button>
        </el-radio-group>
        <div class="style-hint">
          {{ toneHints[styleConfig.tone] }}
        </div>
      </el-form-item>

      <!-- 回复长度 -->
      <el-form-item label="回复长度">
        <el-radio-group v-model="styleConfig.length">
          <el-radio-button label="short">
            简短
          </el-radio-button>
          <el-radio-button label="medium">
            中等
          </el-radio-button>
          <el-radio-button label="long">
            详细
          </el-radio-button>
        </el-radio-group>
        <div class="style-hint">
          {{ lengthHints[styleConfig.length] }}
        </div>
      </el-form-item>

      <!-- 优先分类 -->
      <el-form-item label="优先分类">
        <el-checkbox-group v-model="styleConfig.categories">
          <el-checkbox-button label="confirm">
            确认
          </el-checkbox-button>
          <el-checkbox-button label="question">
            询问
          </el-checkbox-button>
          <el-checkbox-button label="polite">
            感谢
          </el-checkbox-button>
          <el-checkbox-button label="work">
            工作
          </el-checkbox-button>
          <el-checkbox-button label="casual">
            轻松
          </el-checkbox-button>
          <el-checkbox-button label="closing">
            结束
          </el-checkbox-button>
        </el-checkbox-group>
      </el-form-item>

      <!-- 添加自定义模板 -->
      <el-form-item label="自定义">
        <div class="custom-templates">
          <div
            v-for="(template, index) in styleConfig.customTemplates"
            :key="index"
            class="custom-item"
          >
            <el-input
              v-model="template.text"
              placeholder="输入自定义回复"
              size="small"
            >
              <template #append>
                <el-button
                  icon="Delete"
                  @click="removeCustomTemplate(index)"
                />
              </template>
            </el-input>
          </div>
          <el-button
            type="primary"
            link
            :disabled="styleConfig.customTemplates.length >= 5"
            @click="addCustomTemplate"
          >
            + 添加自定义回复 (最多5条)
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="handleSave"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiReplyStyle, setAiReplyStyle } from '@/utils/storage'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'save'])

const visible = ref(false)

// 从 localStorage 加载配置
const loadConfig = () => {

  const saved = getAiReplyStyle()
  return Object.keys(saved).length > 0 ? saved : null
}

const savedConfig = loadConfig()

const styleConfig = reactive({
  tone: savedConfig?.tone || 'polite',
  length: savedConfig?.length || 'medium',
  categories: savedConfig?.categories || ['confirm', 'polite', 'work'],
  customTemplates: savedConfig?.customTemplates || []
})

const toneHints = {
  formal: '使用正式、专业的商务用语',
  polite: '使用礼貌、谦逊的表达方式',
  casual: '使用轻松、随意的聊天风格'
}

const lengthHints = {
  short: '简洁明了，一句话回复',
  medium: '适中长度，包含必要信息',
  long: '详细完整，包含更多细节'
}

const addCustomTemplate = () => {
  if (styleConfig.customTemplates.length >= 5) {
    ElMessage.warning('最多只能添加5条自定义回复')
    return
  }
  styleConfig.customTemplates.push({ text: '', category: 'custom' })
}

const removeCustomTemplate = index => {
  styleConfig.customTemplates.splice(index, 1)
}

const handleSave = () => {
  // 过滤掉空的模板
  styleConfig.customTemplates = styleConfig.customTemplates.filter(t => t.text.trim())
  // 确保至少选择一个分类
  if (styleConfig.categories.length === 0) {
    styleConfig.categories = ['confirm', 'polite', 'work']
  }
  // 保存到 localStorage
  setAiReplyStyle({ ...styleConfig })
  ElMessage.success('风格设置已保存')
  emit('save', { ...styleConfig })
  handleClose()
}

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})

// 暴露获取配置的方法
const getConfig = () => ({ ...styleConfig })

defineExpose({ getConfig })
</script>

<style scoped lang="scss">
.style-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.custom-templates {
  width: 100%;

  .custom-item {
    margin-bottom: 8px;
  }
}

:deep(.el-checkbox-button) {
  margin-right: 8px;
  margin-bottom: 8px;
}
</style>
