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
              :disabled="uploading"
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
            <el-select v-model="localSettings.general.language" size="small" style="width: 100px" @change="handleSettingChange('language', $event)" :disabled="saving">
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
            <el-select v-model="localSettings.general.timeFormat" size="small" style="width: 100px" @change="handleSettingChange('timeFormat', $event)" :disabled="saving">
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
          <div class="row-value">
            <el-switch v-model="localSettings.general.autoStart" size="small" @change="handleSettingChange('autoStart', $event)" :disabled="saving" />
          </div>
        </div>
        <div class="setting-row">
          <span class="row-label">
            <el-icon class="row-icon"><Minus /></el-icon>
            关闭最小化到托盘
          </span>
          <div class="row-value">
            <el-switch v-model="localSettings.general.minimizeToTray" size="small" @change="handleSettingChange('minimizeToTray', $event)" :disabled="saving" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { Picture, ChatDotRound, Clock, SwitchButton, Minus } from '@element-plus/icons-vue'
import { getSettingsByType, updateSetting, batchUpdateSettings } from '@/api/im/userSettings'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const store = useStore()
const { setTheme: switchTheme } = useTheme()

// 本地设置状态
const localSettings = reactive({
  general: {
    theme: 'light',
    language: 'zh-CN',
    timeFormat: '24h',
    autoStart: false,
    minimizeToTray: true
  }
})

// 管理员状态
const isAdmin = computed(() => store.getters['user/isAdmin'])

// Logo 状态
const logoUrl = ref(null)
const uploading = ref(false)
const uploadRef = ref(null)

// 保存状态
const saving = ref(false)

// 防抖定时器（必须在 handleDebounceSave 之前声明）
const saveTimer = ref(null)

// 加载设置
onMounted(async () => {
  await loadSettings()
  if (isAdmin.value) {
    await loadLogo()
  }
})

// 加载设置
const loadSettings = async () => {
  try {
    const res = await getSettingsByType('general')
    if (res.code === 200 && res.data) {
      // 转换后端数据为本地格式
      const backendSettings = {}
      res.data.forEach(item => {
        backendSettings[item.settingKey] = item.settingValue
      })
      
      // 合并到本地状态
      Object.assign(localSettings.general, {
        theme: backendSettings.theme || 'light',
        language: backendSettings.language || 'zh-CN',
        timeFormat: backendSettings.timeFormat || '24h',
        autoStart: backendSettings.autoStart === 'true',
        minimizeToTray: backendSettings.minimizeToTray === 'true'
      })
    }
    }
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载设置失败')
  }
}

// 加载 Logo
const loadLogo = async () => {
  try {
    const res = await fetch('/api/admin/config/logo').then(r => r.json())
    if (res.code === 200 && res.data) {
      logoUrl.value = res.data
    }
  } catch (error) {
    console.error('获取 Logo 失败:', error)
  }
}

// 上传前验证
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

// 上传 Logo
const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  uploading.value = true
  try {
    const res = await fetch('/api/admin/config/logo', {
      method: 'POST',
      body: formData
    }).then(r => r.json())
    if (res.code === 200) {
      logoUrl.value = res.data
      ElMessage.success('Logo 上传成功')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 恢复默认 Logo
const handleReset = async () => {
  try {
    await fetch('/api/admin/config/update', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ key: 'system.logo.url', value: '' })
    })
    logoUrl.value = null
    ElMessage.success('已恢复默认 Logo')
  } catch (error) {
    console.error('恢复失败:', error)
    ElMessage.error('恢复失败: ' + (error.message || '未知错误'))
  }
}

// 处理主题变更
const handleThemeChange = (theme) => {
  localSettings.general.theme = theme
  switchTheme(theme)
  handleDebounceSave('theme', theme)
}

// 处理设置变更
const handleSettingChange = (key, value) => {
  localSettings.general[key] = value
  handleDebounceSave(key, value)
}

// 防抖保存
const handleDebounceSave = (key, value) => {
  if (saveTimer.value) clearTimeout(saveTimer.value)
  saveTimer.value = setTimeout(async () => {
    await saveSingleSetting(key, value)
  }, 500)
}

// 保存单个设置
const saveSingleSetting = async (key, value) => {
  try {
    saving.value = true
    const res = await updateSetting({ settingKey: key, settingValue: String(value) })
    if (res.code === 200) {
      ElMessage.success('设置已保存')
      emit('update:modelValue', JSON.parse(JSON.stringify(localSettings)))
      emit('change')
    } else {
      ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

// 监听 props 变化
watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.general-settings {
  padding: 0;
  max-width: 100%;
  box-sizing: border-box;
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
}

.logo-placeholder {
  font-size: 16px;
  color: #ccc;
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
  }

  .logo-placeholder {
    color: #555;
  }
}
</style>
