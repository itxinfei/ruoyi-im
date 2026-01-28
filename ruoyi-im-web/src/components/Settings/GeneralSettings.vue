<template>
  <div class="general-settings">
    <div class="setting-group">
      <h3 class="group-title">品牌标识</h3>
      <div class="logo-uploader">
        <div class="logo-preview">
          <img v-if="logoUrl" :src="logoUrl" class="logo-image" alt="Logo预览" />
          <div v-else class="logo-placeholder">
            <el-icon class="placeholder-icon"><Picture /></el-icon>
            <span class="placeholder-text">暂无Logo</span>
          </div>
        </div>
        <div class="logo-actions">
          <el-upload
            ref="uploadRef"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="image/*"
            :limit="1"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon><Upload /></el-icon> 上传Logo
            </el-button>
          </el-upload>
          <el-button v-if="logoUrl" @click="handleReset" :disabled="uploading">
            <el-icon><RefreshLeft /></el-icon> 恢复默认
          </el-button>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">外观</h3>
      <div class="theme-selector">
        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'light' }"
          @click="selectTheme('light')"
        >
          <div class="theme-preview light-preview">
            <div class="preview-nav"></div>
            <div class="preview-content">
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
          </div>
          <span class="theme-label">浅色</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'light'">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        
        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'dark' }"
          @click="selectTheme('dark')"
        >
          <div class="theme-preview dark-preview">
            <div class="preview-nav"></div>
            <div class="preview-content">
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
          </div>
          <span class="theme-label">深色</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'dark'">
            <el-icon><Check /></el-icon>
          </div>
        </div>

        <div 
          class="theme-option" 
          :class="{ active: localSettings.general.theme === 'auto' }"
          @click="selectTheme('auto')"
        >
          <div class="theme-preview auto-preview">
            <div class="preview-half light"></div>
            <div class="preview-half dark"></div>
          </div>
          <span class="theme-label">跟随系统</span>
          <div class="active-indicator" v-if="localSettings.general.theme === 'auto'">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <h3 class="group-title">语言</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-content">
            <div class="item-title">界面语言</div>
            <div class="item-desc">选择软件显示的语言</div>
          </div>
          <el-select v-model="localSettings.general.language" size="default" style="width: 140px" @change="handleChange">
            <el-option label="简体中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
          </el-select>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted } from 'vue'
import { Check, Picture, Upload, RefreshLeft } from '@element-plus/icons-vue'
import { request } from '@/api/request'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const localSettings = reactive(JSON.parse(JSON.stringify(props.modelValue)))

const logoUrl = ref(null)
const uploadRef = ref(null)
const uploading = ref(false)

onMounted(async () => {
  try {
    const res = await request.get('/api/admin/config/logo')
    if (res.code === 200 && res.data) {
      logoUrl.value = res.data
    }
  } catch (error) {
    console.error('获取系统Logo失败:', error)
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

const selectTheme = (theme) => {
  localSettings.general.theme = theme
  handleChange()
}

const handleChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
  emit('change')
}
</script>

<style scoped lang="scss">
.general-settings {
  padding-bottom: 20px;
}

.setting-group {
  margin-bottom: 32px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
  padding-left: 4px;
}

.logo-uploader {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.logo-preview {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  border: 2px dashed var(--dt-border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-card);
  overflow: hidden;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--dt-text-tertiary);

  .placeholder-icon {
    font-size: 32px;
  }

  .placeholder-text {
    font-size: 12px;
  }
}

.logo-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.theme-selector {
  display: flex;
  gap: 20px;
}

.theme-option {
  position: relative;
  cursor: pointer;
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  &:hover .theme-preview {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.active .theme-preview {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-color-light);
  }
}

.theme-preview {
  width: 100%;
  height: 80px;
  border-radius: 8px;
  border: 1px solid var(--dt-border-light);
  overflow: hidden;
  position: relative;
  transition: all 0.2s;
  background: #fff;
  
  .dark & {
    border-color: var(--dt-border-dark);
  }
}

.light-preview {
  background: #f5f5f5;
  .preview-nav {
    width: 20px;
    height: 100%;
    background: #fff;
    position: absolute;
    left: 0;
    top: 0;
    border-right: 1px solid #eee;
  }
  .preview-content {
    margin-left: 20px;
    padding: 10px;
  }
  .preview-line {
    height: 4px;
    background: #e0e0e0;
    margin-bottom: 6px;
    border-radius: 2px;
    &.short { width: 60%; }
  }
}

.dark-preview {
  background: #1e1e1e;
  .preview-nav {
    width: 20px;
    height: 100%;
    background: #2d2d2d;
    position: absolute;
    left: 0;
    top: 0;
    border-right: 1px solid #333;
  }
  .preview-content {
    margin-left: 20px;
    padding: 10px;
  }
  .preview-line {
    height: 4px;
    background: #444;
    margin-bottom: 6px;
    border-radius: 2px;
    &.short { width: 60%; }
  }
}

.auto-preview {
  display: flex;
  .preview-half {
    flex: 1;
    height: 100%;
    &.light { background: #f5f5f5; }
    &.dark { background: #1e1e1e; }
  }
}

.theme-label {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.active-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--dt-brand-color);
  color: #fff;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  overflow: hidden;
  
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  
  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 4px;
  }

  .item-desc {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}
</style>
