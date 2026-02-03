<!--
  群组二维码组件

  显示群组二维码，支持扫码入群
-->
<template>
  <div class="qrcode-container">
    <!-- 二维码展示区 -->
    <div class="qrcode-wrapper">
      <div ref="qrcodeRef" class="qrcode-canvas"></div>

      <!-- 加载状态 -->
      <div v-if="loading" class="qrcode-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>生成中...</span>
      </div>

      <!-- 二维码过期 -->
      <div v-if="isExpired" class="qrcode-expired">
        <el-icon :size="32"><Warning /></el-icon>
        <span>二维码已过期</span>
        <el-button size="small" type="primary" @click="refreshQRCode">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 群组信息 -->
    <div class="group-info">
      <div class="group-name">{{ groupInfo.name }}</div>
      <div class="group-desc">扫一扫二维码，加入群聊</div>
    </div>

    <!-- 有效期 -->
    <div class="qrcode-footer">
      <div class="expire-info">
        <el-icon><Clock /></el-icon>
        <span>{{ expireText }}</span>
      </div>
      <el-button link type="primary" @click="refreshQRCode">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </div>

    <!-- 操作按钮 -->
    <div class="qrcode-actions">
      <el-button @click="saveQRCode">
        <el-icon><Download /></el-icon>保存图片
      </el-button>
      <el-button @click="copyLink">
        <el-icon><Share /></el-icon>复制链接
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Loading, Warning, Clock, Refresh, Download, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { copyToClipboard } from '@/utils/format'

const props = defineProps({
  groupInfo: {
    type: Object,
    required: true
  }
})

const qrcodeRef = ref(null)
const loading = ref(false)
const isExpired = ref(false)
const expireTime = ref(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7天有效期
let expireTimer = null

// 有效期文本
const expireText = computed(() => {
  if (isExpired.value) return '已过期'
  const remaining = expireTime.value - Date.now()
  if (remaining <= 0) {
    isExpired.value = true
    return '已过期'
  }
  const days = Math.floor(remaining / (24 * 60 * 60 * 1000))
  if (days > 0) return `${days}天后过期`
  const hours = Math.floor(remaining / (60 * 60 * 1000))
  if (hours > 0) return `${hours}小时后过期`
  const minutes = Math.floor(remaining / (60 * 1000))
  return `${minutes}分钟后过期`
})

// 生成群组加入链接
const generateJoinLink = () => {
  // 这里使用群组 ID 生成链接，实际应该由后端提供
  const baseUrl = window.location.origin
  return `${baseUrl}/group/join?groupId=${props.groupInfo.id}`
}

// 生成二维码
const generateQRCode = async () => {
  if (!qrcodeRef.value) return

  loading.value = true
  isExpired.value = false

  try {
    const link = generateJoinLink()
    await QRCode.toCanvas(qrcodeRef.value, link, {
      width: 200,
      margin: 2,
      color: {
        dark: '#1890ff',
        light: '#ffffff'
      }
    })
    expireTime.value = Date.now() + 7 * 24 * 60 * 60 * 1000
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('二维码生成失败')
  } finally {
    loading.value = false
  }
}

// 刷新二维码
const refreshQRCode = () => {
  generateQRCode()
  ElMessage.success('二维码已刷新')
}

// 保存二维码图片
const saveQRCode = () => {
  const canvas = qrcodeRef.value
  if (!canvas) return

  try {
    const link = document.createElement('a')
    link.download = `群组二维码-${props.groupInfo.name}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('图片保存成功')
  } catch (error) {
    console.error('保存图片失败:', error)
    ElMessage.error('保存失败')
  }
}

// 复制群组链接
const copyLink = async () => {
  const link = generateJoinLink()
  await copyToClipboard(link, { successMsg: '链接已复制到剪贴板' })
}

// 检查二维码是否过期
const checkExpired = () => {
  if (expireTime.value && Date.now() > expireTime.value) {
    isExpired.value = true
  }
}

// 启动过期检查定时器
const startExpireCheck = () => {
  expireTimer = setInterval(checkExpired, 60 * 1000) // 每分钟检查一次
}

onMounted(() => {
  generateQRCode()
  startExpireCheck()
})

onUnmounted(() => {
  if (expireTimer) {
    clearInterval(expireTimer)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
}

.qrcode-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
  margin-bottom: 20px;
}

.qrcode-canvas {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: var(--dt-shadow-md);

  canvas {
    border-radius: 8px;
  }
}

.qrcode-loading,
.qrcode-expired {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  color: var(--dt-text-secondary);
  font-size: 14px;
}

.group-info {
  text-align: center;
  margin-bottom: 20px;
}

.group-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
}

.group-desc {
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.qrcode-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 280px;
  padding: 8px 12px;
  background: var(--dt-bg-secondary);
  border-radius: 8px;
  margin-bottom: 16px;
}

.expire-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: 14px;
  }
}

.qrcode-actions {
  display: flex;
  gap: 12px;
}
</style>
