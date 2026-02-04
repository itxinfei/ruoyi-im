<template>
  <div class="general-settings">
    <!-- 品牌标识 - 仅管理员可见 -->
    <section v-if="isAdmin" class="setting-section">
      <div class="section-header">
        <h3 class="section-title">品牌标识</h3>
        <p class="section-desc">自定义系统 Logo 和品牌形象</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">系统 Logo</span>
            <span class="item-desc">建议上传透明背景的 PNG 图片</span>
          </div>
          <div class="item-action">
            <div class="logo-wrapper">
              <div class="logo-preview">
                <img v-if="logoUrl" :src="logoUrl" alt="Logo" />
                <el-icon v-else class="logo-placeholder"><Picture /></el-icon>
              </div>
              <div class="logo-actions">
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
                <el-button v-if="logoUrl" link type="danger" size="small" @click="handleReset" :disabled="uploading">恢复默认</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 外观设置 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">外观设置</h3>
        <p class="section-desc">自定义界面主题和显示效果</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-content">
            <span class="item-label">主题模式</span>
          </div>
          <div class="item-action">
            <el-radio-group v-model="localSettings.general.theme" size="default" @change="handleThemeChange">
              <el-radio-button label="light">
                <el-icon><Sunny /></el-icon> 浅色
              </el-radio-button>
              <el-radio-button label="dark">
                <el-icon><Moon /></el-icon> 深色
              </el-radio-button>
              <el-radio-button label="auto">
                <el-icon><Monitor /></el-icon> 跟随系统
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
    </section>

    <!-- 语言与地区 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">语言与地区</h3>
        <p class="section-desc">设置界面语言和时间格式</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><ChatDotRound /></el-icon>
              <span class="item-label">界面语言</span>
            </div>
          </div>
          <div class="item-action">
            <el-select v-model="localSettings.general.language" size="default" style="width: 120px" @change="handleSettingChange('language', $event)" :disabled="saving">
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </div>
        </div>

        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><Clock /></el-icon>
              <span class="item-label">时间格式</span>
            </div>
          </div>
          <div class="item-action">
            <el-select v-model="localSettings.general.timeFormat" size="default" style="width: 120px" @change="handleSettingChange('timeFormat', $event)" :disabled="saving">
              <el-option label="24小时制" value="24h" />
              <el-option label="12小时制" value="12h" />
            </el-select>
          </div>
        </div>
      </div>
    </section>

    <!-- 启动与行为 -->
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">启动与行为</h3>
        <p class="section-desc">配置应用程序的启动和关闭行为</p>
      </div>
      
      <div class="setting-card">
        <div class="setting-item">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><SwitchButton /></el-icon>
              <span class="item-label">开机自启动</span>
            </div>
            <span class="item-desc">系统启动时自动运行应用程序</span>
          </div>
          <div class="item-action">
            <el-switch v-model="localSettings.general.autoStart" @change="handleSettingChange('autoStart', $event)" :disabled="saving" />
          </div>
        </div>

        <el-divider class="card-divider" />
        
        <div class="setting-item">
          <div class="item-content">
            <div class="label-with-icon">
              <el-icon class="item-icon"><Minus /></el-icon>
              <span class="item-label">关闭时最小化到托盘</span>
            </div>
            <span class="item-desc">点击关闭按钮时，程序将隐藏到系统托盘而不是退出</span>
          </div>
          <div class="item-action">
            <el-switch v-model="localSettings.general.minimizeToTray" @change="handleSettingChange('minimizeToTray', $event)" :disabled="saving" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { Picture, ChatDotRound, Clock, SwitchButton, Minus, Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { getSettingsByType, updateSetting } from '@/api/im/userSettings'
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
  } catch (error) {
    console.error('加载设置失败:', error)
    // ElMessage.error('加载设置失败')
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
      // ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    // ElMessage.error('保存设置失败')
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
  max-width: 800px;
  margin: 0 auto;
}

.setting-section {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  margin-bottom: 16px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin: 0 0 4px 0;
  }
  
  .section-desc {
    font-size: 13px;
    color: var(--text-color-secondary);
    margin: 0;
  }
}

.setting-card {
  background: var(--bg-color-overlay);
  border: 1px solid var(--border-color-light);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
}

.card-divider {
  margin: 0;
  border-color: var(--border-color-lighter);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  min-height: 24px;
  transition: background 0.2s;
  
  .item-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    
    .item-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-color-primary);
    }
    
    .item-desc {
      font-size: 12px;
      color: var(--text-color-secondary);
    }
    
    .label-with-icon {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .item-icon {
        font-size: 16px;
        color: var(--text-color-secondary);
      }
    }
  }

  .item-action {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .logo-preview {
    width: 48px;
    height: 48px;
    border: 1px solid var(--border-color-light);
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-color);
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      padding: 4px;
    }
  }

  .logo-placeholder {
    font-size: 24px;
    color: var(--text-color-placeholder);
  }
  
  .logo-actions {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

// 深色模式适配
.dark {
  .setting-card {
    background: #252525;
    border-color: #333;
  }
  
  .card-divider {
    border-color: #333;
  }

  .logo-wrapper .logo-preview {
    background: #1a1a1a;
    border-color: #333;
  }
}
</style>