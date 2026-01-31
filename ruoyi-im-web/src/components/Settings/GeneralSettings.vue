<template>
  <div class="general-settings">
    <!-- 品牌标识 - 仅管理员可见 -->
    <section v-if="isAdmin" class="setting-group">
      <h3 class="group-title">品牌标识</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">系统Logo</span>
          <div class="row-value">
            <div class="logo-preview">
              <img v-if="logoUrl" :src="logoUrl" alt="Logo" />
              <el-icon v-else class="logo-placeholder"><Picture /></el-icon>
            </div>
            <el-upload
              ref="uploadRef"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
              :limit="1"
            >
              <el-button type="primary" size="small" :loading="uploading">上传</el-button>
            </el-upload>
            <el-button v-if="logoUrl" size="small" @click="handleReset" :disabled="uploading">恢复</el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 外观设置 -->
    <section class="setting-group">
      <h3 class="group-title">外观设置</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">主题模式</span>
          <div class="row-value">
            <el-radio-group v-model="localSettings.general.theme" size="small" @change="handleThemeChange">
              <el-radio-button label="light">浅色</el-radio-button>
              <el-radio-button label="dark">深色</el-radio-button>
              <el-radio-button label="auto">跟随系统</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
    </section>

    <!-- 语言设置 -->
    <section class="setting-group">
      <h3 class="group-title">语言与地区</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><ChatDotRound /></el-icon>
            界面语言
          </span>
          <div class="row-value">
            <el-select v-model="localSettings.general.language" size="small" style="width: 100px" @change="handleChange">
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </div>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Clock /></el-icon>
            时间格式
          </span>
          <div class="row-value">
            <el-select v-model="localSettings.general.timeFormat" size="small" style="width: 100px" @change="handleChange">
              <el-option label="24小时制" value="24h" />
              <el-option label="12小时制" value="12h" />
            </el-select>
          </div>
        </div>
      </div>
    </section>

    <!-- 启动与行为 -->
    <section class="setting-group">
      <h3 class="group-title">启动与行为</h3>
      <div class="setting-list">
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><SwitchButton /></el-icon>
            开机自启动
          </span>
          <el-switch v-model="localSettings.general.autoStart" size="small" @change="handleChange" />
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Minus /></el-icon>
            关闭最小化到托盘
          </span>
          <el-switch v-model="localSettings.general.minimizeToTray" size="small" @change="handleChange" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { Picture, ChatDotRound, Clock, SwitchButton, Minus } from '@element-plus/icons-vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const store = useStore()
const localSettings = reactive({
  general: {
    theme: 'light',
    language: 'zh-CN',
    timeFormat: '24h',
    autoStart: false,
    minimizeToTray: true,
    ...props.modelValue.general
  }
})

import { useTheme } from '@/composables/useTheme'
const { setTheme: switchTheme } = useTheme()

const handleThemeChange = (theme) => {
  localSettings.general.theme = theme
  switchTheme(theme)
  handleChange()
}

const isAdmin = computed(() => store.getters['user/isAdmin'])
const logoUrl = ref(null)
const uploading = ref(false)

onMounted(async () => {
  if (isAdmin.value) {
    try {
      const res = await request.get('/api/admin/config/logo')
      if (res.code === 200 && res.data) {
        logoUrl.value = res.data
      }
    } catch (error) {
      console.error('获取系统Logo失败:', error)
    }
  }
})

const beforeUpload = (file) => {
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

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  uploading.value = true
  try {
    const res = await request.post('/api/admin/config/logo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      logoUrl.value = res.data
      ElMessage.success('Logo上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const handleReset = async () => {
  try {
    await request.put('/api/admin/config/update', {
      key: 'system.logo.url',
      value: ''
    })
    logoUrl.value = null
    ElMessage.success('已恢复默认Logo')
  } catch (error) {
    ElMessage.error('恢复失败: ' + (error.message || '未知错误'))
  }
}

watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

const handleChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
  emit('change')
}
</script>

<style scoped lang="scss">
.general-settings {
  padding: 0;
}

.setting-group {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.setting-list {
  background: #fafafa;
  border: 1px solid #e8e8e8;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  min-height: 40px;
  border-bottom: 1px solid #e8e8e8;

  &:last-child {
    border-bottom: none;
  }
}

.row-label {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;

  .row-icon {
    font-size: 14px;
    color: #999;
  }
}

.row-value {
  font-size: 13px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-preview {
  width: 32px;
  height: 32px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    padding: 2px;
  }

  .logo-placeholder {
    font-size: 16px;
    color: #ccc;
  }
}

// 深色模式
.dark {
  .group-title {
    color: #ccc;
    border-color: #333;
  }

  .setting-list {
    background: #252525;
    border-color: #333;
  }

  .setting-row {
    border-color: #333;
  }

  .row-label {
    color: #999;

    .row-icon {
      color: #666;
    }
  }

  .row-value {
    color: #ccc;
  }

  .logo-preview {
    border-color: #444;
    background: #1a1a1a;

    .logo-placeholder {
      color: #555;
    }
  }
}
</style>
