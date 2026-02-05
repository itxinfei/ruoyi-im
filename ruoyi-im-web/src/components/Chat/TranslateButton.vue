<template>
  <el-dropdown
    trigger="click"
    @command="handleTranslate"
  >
    <span class="translate-trigger">
      <el-icon><ChatLineRound /></el-icon>
      <span class="translate-text">翻译</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="lang in commonLanguages"
          :key="lang.code"
          :command="lang.code"
        >
          {{ lang.name }}
        </el-dropdown-item>
        <el-dropdown-item
          divided
          @click="showMoreLanguages = true"
        >
          更多语言...
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <!-- 更多语言选择对话框 -->
  <el-dialog
    v-model="showMoreLanguages"
    title="选择翻译语言"
    width="400px"
    :modal="true"
  >
    <el-radio-group
      v-model="selectedLanguage"
      class="language-list"
    >
      <el-radio
        v-for="lang in allLanguages"
        :key="lang.code"
        :label="lang.code"
        border
      >
        {{ lang.name }} ({{ lang.englishName }})
      </el-radio>
    </el-radio-group>
    <template #footer>
      <el-button @click="showMoreLanguages = false">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="translateSelected"
      >
        翻译
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound } from '@element-plus/icons-vue'
import { translate, getSupportedLanguages } from '@/api/im'

const props = defineProps({
  text: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['translated'])

const showMoreLanguages = ref(false)
const selectedLanguage = ref('en')
const allLanguages = ref([])

// 常用语言
const commonLanguages = computed(() => {
  return allLanguages.value.filter(lang =>
    ['zh', 'en', 'ja', 'ko'].includes(lang.code)
  )
})

// 加载支持的语言列表
const loadLanguages = async () => {
  try {
    const res = await getSupportedLanguages()
    if (res.code === 200 && res.data) {
      allLanguages.value = res.data
    }
  } catch (error) {
    console.error('加载语言列表失败:', error)
    // 使用默认语言列表
    allLanguages.value = [
      { code: 'zh', name: '中文', englishName: 'Chinese' },
      { code: 'en', name: '英语', englishName: 'English' },
      { code: 'ja', name: '日语', englishName: 'Japanese' },
      { code: 'ko', name: '韩语', englishName: 'Korean' },
      { code: 'fr', name: '法语', englishName: 'French' },
      { code: 'de', name: '德语', englishName: 'German' },
      { code: 'es', name: '西班牙语', englishName: 'Spanish' }
    ]
  }
}

// 处理翻译
const handleTranslate = async targetLang => {
  try {
    ElMessage.info('正在翻译...')
    const res = await translate({
      text: props.text,
      from: 'auto',
      to: targetLang
    })
    if (res.code === 200 && res.data) {
      emit('translated', {
        originalText: res.data.originalText,
        translatedText: res.data.translatedText,
        fromLanguage: res.data.fromLanguage,
        toLanguage: res.data.toLanguage
      })
    }
  } catch (error) {
    console.error('翻译失败:', error)
    ElMessage.error('翻译失败，请稍后重试')
  }
}

// 翻译选中的语言
const translateSelected = () => {
  showMoreLanguages.value = false
  handleTranslate(selectedLanguage.value)
}

// 初始化
loadLanguages()
</script>

<script>
export default {
  name: 'TranslateButton'
}
</script>

<style scoped>
.translate-trigger {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: var(--dt-radius-sm);
  transition: background-color 0.2s;
}

.translate-trigger:hover {
  background-color: var(--el-fill-color-light);
}

.translate-text {
  font-size: 12px;
}

.language-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.language-list :deep(.el-radio) {
  margin-right: 0;
}
</style>
