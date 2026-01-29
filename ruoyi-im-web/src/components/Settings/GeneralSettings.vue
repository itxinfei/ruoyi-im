<template>
  <div class="general-settings">
    <!-- 品牌标识 - 仅管理员可见 -->
    <section v-if="isAdmin" class="setting-section">
      <h3 class="section-title">品牌标识</h3>
      <div class="setting-card">
        <div class="logo-setting">
          <div class="logo-preview-wrapper">
            <div class="logo-preview">
              <img v-if="logoUrl" :src="logoUrl" class="logo-image" alt="Logo预览" />
              <div v-else class="logo-placeholder">
                <el-icon class="placeholder-icon"><Picture /></el-icon>
                <span class="placeholder-text">系统Logo</span>
              </div>
            </div>
          </div>
          <div class="logo-info">
            <p class="info-text">建议上传 200x200 像素的 PNG 或 JPG 图片</p>
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
                  <el-icon><Upload /></el-icon>
                  上传Logo
                </el-button>
              </el-upload>
              <el-button v-if="logoUrl" @click="handleReset" :disabled="uploading">
                <el-icon><RefreshLeft /></el-icon>
                恢复默认
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 外观主题 -->
    <section class="setting-section">
      <h3 class="section-title">外观</h3>
      <div class="setting-card">
        <div class="theme-selector">
          <div
            class="theme-option"
            :class="{ active: localSettings.general.theme === 'light' }"
            @click="selectTheme('light')"
          >
            <div class="theme-preview light-preview">
              <div class="preview-sidebar"></div>
              <div class="preview-main">
                <div class="preview-header"></div>
                <div class="preview-content">
                  <div class="preview-line"></div>
                  <div class="preview-line short"></div>
                </div>
              </div>
            </div>
            <div class="theme-info">
              <span class="theme-label">浅色</span>
              <el-icon v-if="localSettings.general.theme === 'light'" class="check-icon"><Check /></el-icon>
            </div>
          </div>

          <div
            class="theme-option"
            :class="{ active: localSettings.general.theme === 'dark' }"
            @click="selectTheme('dark')"
          >
            <div class="theme-preview dark-preview">
              <div class="preview-sidebar"></div>
              <div class="preview-main">
                <div class="preview-header"></div>
                <div class="preview-content">
                  <div class="preview-line"></div>
                  <div class="preview-line short"></div>
                </div>
              </div>
            </div>
            <div class="theme-info">
              <span class="theme-label">深色</span>
              <el-icon v-if="localSettings.general.theme === 'dark'" class="check-icon"><Check /></el-icon>
            </div>
          </div>

          <div
            class="theme-option"
            :class="{ active: localSettings.general.theme === 'auto' }"
            @click="selectTheme('auto')"
          >
            <div class="theme-preview auto-preview">
              <div class="preview-half light">
                <div class="mini-sidebar"></div>
              </div>
              <div class="preview-half dark">
                <div class="mini-sidebar"></div>
              </div>
            </div>
            <div class="theme-info">
              <span class="theme-label">跟随系统</span>
              <el-icon v-if="localSettings.general.theme === 'auto'" class="check-icon"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 语言设置 -->
    <section class="setting-section">
      <h3 class="section-title">语言与地区</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-purple">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">界面语言</div>
            <div class="item-desc">选择软件显示的语言</div>
          </div>
          <el-select
            v-model="localSettings.general.language"
            size="default"
            style="width: 140px"
            @change="handleChange"
          >
            <el-option label="简体中文" value="zh-CN">
              <span style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 16px;">🇨🇳</span>
                简体中文
              </span>
            </el-option>
            <el-option label="English" value="en-US">
              <span style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 16px;">🇺🇸</span>
                English
              </span>
            </el-option>
          </el-select>
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-cyan">
              <el-icon><Clock /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">时间格式</div>
            <div class="item-desc">选择时间显示格式</div>
          </div>
          <el-select
            v-model="localSettings.general.timeFormat"
            size="default"
            style="width: 140px"
            @change="handleChange"
          >
            <el-option label="24小时制" value="24h" />
            <el-option label="12小时制" value="12h" />
          </el-select>
        </div>
      </div>
    </section>

    <!-- 启动与行为 -->
    <section class="setting-section">
      <h3 class="section-title">启动与行为</h3>
      <div class="setting-list">
        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-blue">
              <el-icon><SwitchButton /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">开机自启动</div>
            <div class="item-desc">系统启动时自动运行应用</div>
          </div>
          <el-switch v-model="localSettings.general.autoStart" @change="handleChange" />
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-green">
              <el-icon><Minus /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">关闭时最小化到托盘</div>
            <div class="item-desc">点击关闭按钮时最小化到系统托盘</div>
          </div>
          <el-switch v-model="localSettings.general.minimizeToTray" @change="handleChange" />
        </div>

        <div class="setting-item">
          <div class="item-icon-wrapper">
            <div class="item-icon bg-orange">
              <el-icon><FullScreen /></el-icon>
            </div>
          </div>
          <div class="item-content">
            <div class="item-title">默认最大化窗口</div>
            <div class="item-desc">启动时自动最大化窗口</div>
          </div>
          <el-switch v-model="localSettings.general.startMaximized" @change="handleChange" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import {
  Check,
  Picture,
  Upload,
  RefreshLeft,
  ChatDotRound,
  Clock,
  SwitchButton,
  Minus,
  FullScreen
} from '@element-plus/icons-vue'
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
    startMaximized: false,
    ...props.modelValue.general
  }
})

// 判断是否为管理员
const isAdmin = computed(() => store.getters['user/isAdmin'])

const logoUrl = ref(null)
const uploadRef = ref(null)
const uploading = ref(false)

onMounted(async () => {
  // 只对管理员请求自定义logo
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
  max-width: 680px;
}

// 分区标题
.setting-section {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
  padding-left: 4px;
}

// 设置卡片
.setting-card {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 24px;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

// Logo设置
.logo-setting {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.logo-preview-wrapper {
  flex-shrink: 0;
}

.logo-preview {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-body);
  overflow: hidden;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 8px;
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

.logo-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-text {
  margin: 0;
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.logo-actions {
  display: flex;
  gap: 12px;
}

// 主题选择器
.theme-selector {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.theme-option {
  position: relative;
  cursor: pointer;
  width: 140px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &:hover .theme-preview {
    border-color: var(--dt-brand-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &.active .theme-preview {
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
  }
}

.theme-preview {
  width: 100%;
  height: 90px;
  border-radius: 8px;
  border: 2px solid var(--dt-border-light);
  overflow: hidden;
  position: relative;
  transition: all 0.2s;

  .dark & {
    border-color: var(--dt-border-dark);
  }
}

.light-preview {
  background: #f5f7fa;
  display: flex;

  .preview-sidebar {
    width: 28px;
    height: 100%;
    background: #fff;
    border-right: 1px solid #e4e7ed;
  }

  .preview-main {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .preview-header {
    height: 24px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
  }

  .preview-content {
    flex: 1;
    padding: 12px;
  }

  .preview-line {
    height: 6px;
    background: #dcdfe6;
    margin-bottom: 8px;
    border-radius: 3px;

    &.short {
      width: 60%;
    }
  }
}

.dark-preview {
  background: #1a1a1a;
  display: flex;

  .preview-sidebar {
    width: 28px;
    height: 100%;
    background: #2c2c2c;
    border-right: 1px solid #3c3c3c;
  }

  .preview-main {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .preview-header {
    height: 24px;
    background: #2c2c2c;
    border-bottom: 1px solid #3c3c3c;
  }

  .preview-content {
    flex: 1;
    padding: 12px;
  }

  .preview-line {
    height: 6px;
    background: #4c4c4c;
    margin-bottom: 8px;
    border-radius: 3px;

    &.short {
      width: 60%;
    }
  }
}

.auto-preview {
  display: flex;

  .preview-half {
    flex: 1;
    height: 100%;
    position: relative;

    &.light {
      background: #f5f7fa;

      .mini-sidebar {
        position: absolute;
        left: 0;
        top: 0;
        width: 14px;
        height: 100%;
        background: #fff;
        border-right: 1px solid #e4e7ed;
      }
    }

    &.dark {
      background: #1a1a1a;

      .mini-sidebar {
        position: absolute;
        left: 0;
        top: 0;
        width: 14px;
        height: 100%;
        background: #2c2c2c;
        border-right: 1px solid #3c3c3c;
      }
    }
  }
}

.theme-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.theme-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.check-icon {
  color: var(--dt-brand-color);
  font-size: 18px;
  font-weight: bold;
}

// 设置列表
.setting-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 16px;
  border-bottom: 1px solid var(--dt-border-light);
  transition: background-color 0.2s;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

// 图标样式
.item-icon-wrapper {
  flex-shrink: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.bg-blue {
    background: rgba(22, 119, 255, 0.1);
    color: #1677ff;
  }

  &.bg-green {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }

  &.bg-orange {
    background: rgba(250, 140, 22, 0.1);
    color: #fa8c16;
  }

  &.bg-purple {
    background: rgba(114, 46, 209, 0.1);
    color: #722ed1;
  }

  &.bg-cyan {
    background: rgba(19, 194, 194, 0.1);
    color: #13c2c2;
  }
}

// 内容区域
.item-content {
  flex: 1;
  min-width: 0;
}

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

// 响应式适配
@media (max-width: 640px) {
  .logo-setting {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .logo-actions {
    justify-content: center;
  }

  .theme-selector {
    justify-content: center;
  }

  .theme-option {
    width: 120px;
  }

  .setting-item {
    padding: 14px 16px;
  }
}
</style>
