<template>
  <el-dialog
    v-model="visible"
    title="群二维码"
    width="380px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="qrcode-dialog"
  >
    <div class="qrcode-container">
      <!-- 群信息 -->
      <div class="group-info">
        <DingtalkAvatar :src="groupInfo.avatar" :name="groupInfo.name" :size="56" />
        <div class="info-text">
          <div class="group-name">{{ groupInfo.name }}</div>
          <div class="group-tip">扫一扫加入群聊</div>
        </div>
      </div>

      <!-- 二维码区域 -->
      <div class="qrcode-area">
        <div v-if="loading" class="qrcode-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>生成中...</span>
        </div>
        <div v-else-if="qrcodeUrl" class="qrcode-image">
          <img :src="qrcodeUrl" alt="群二维码" />
        </div>
        <div v-else class="qrcode-error">
          <span class="material-icons-outlined">qr_code_2</span>
          <span>二维码生成失败</span>
        </div>
      </div>

      <!-- 有效期提示 -->
      <div class="qrcode-expire">
        <span class="material-icons-outlined expire-icon">schedule</span>
        <span class="expire-text">二维码有效期 7 天</span>
      </div>

      <!-- 操作按钮 -->
      <div class="qrcode-actions">
        <el-button @click="handleRefreshQrcode" :loading="loading">
          <span class="material-icons-outlined">refresh</span>
          刷新二维码
        </el-button>
        <el-button @click="handleDownloadQrcode" :disabled="!qrcodeUrl">
          <span class="material-icons-outlined">download</span>
          保存图片
        </el-button>
      </div>

      <!-- 使用说明 -->
      <div class="qrcode-instruction">
        <div class="instruction-title">如何加入：</div>
        <div class="instruction-steps">
          <div class="step">
            <span class="step-num">1</span>
            <span class="step-text">截图保存二维码或点击"保存图片"</span>
          </div>
          <div class="step">
            <span class="step-num">2</span>
            <span class="step-text">使用扫描功能扫描二维码</span>
          </div>
          <div class="step">
            <span class="step-num">3</span>
            <span class="step-text">确认加入群聊</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getGroupQrcode } from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  group: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const groupInfo = computed(() => props.group || {})
const qrcodeUrl = ref('')
const loading = ref(false)

// 生成二维码
const generateQrcode = async () => {
  if (!props.group?.id) return

  loading.value = true
  try {
    const res = await getGroupQrcode(props.group.id)
    if (res.code === 200) {
      // 后端返回二维码图片URL或Base64数据
      qrcodeUrl.value = res.data.qrcodeUrl || res.data.url || res.data
    }
  } catch (error) {
    console.error('生成群二维码失败:', error)
    ElMessage.error('生成二维码失败')
  } finally {
    loading.value = false
  }
}

// 刷新二维码
const handleRefreshQrcode = () => {
  qrcodeUrl.value = ''
  generateQrcode()
}

// 下载二维码
const handleDownloadQrcode = () => {
  if (!qrcodeUrl.value) return

  try {
    const link = document.createElement('a')
    link.href = qrcodeUrl.value
    link.download = `群二维码-${groupInfo.value.name}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('已保存到下载目录')
  } catch (error) {
    console.error('下载二维码失败:', error)
    ElMessage.error('保存失败，请右键另存图片')
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    generateQrcode()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.qrcode-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  .info-text {
    .group-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin-bottom: 4px;
    }

    .group-tip {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }
  }
}

.qrcode-area {
  width: 240px;
  height: 240px;
  border-radius: 16px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;

  .qrcode-loading,
  .qrcode-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: var(--dt-text-tertiary);

    .el-icon,
    .material-icons-outlined {
      font-size: 36px;
    }

    span {
      font-size: 14px;
    }
  }

  .qrcode-image {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;

    img {
      max-width: 100%;
      max-height: 100%;
      border-radius: 8px;
    }
  }
}

.qrcode-expire {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;

  .expire-icon {
    font-size: 16px;
    color: var(--dt-text-tertiary);
  }

  .expire-text {
    font-size: 13px;
    color: var(--dt-text-secondary);
  }
}

.qrcode-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  .el-button {
    flex: 1;

    .material-icons-outlined {
      font-size: 16px;
      margin-right: 4px;
    }
  }
}

.qrcode-instruction {
  width: 100%;
  padding: 16px;
  background: var(--dt-bg-body);
  border-radius: 12px;

  .instruction-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 12px;
  }

  .instruction-steps {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .step {
      display: flex;
      align-items: center;
      gap: 10px;

      .step-num {
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--dt-brand-color);
        color: #fff;
        border-radius: 50%;
        font-size: 11px;
        font-weight: 600;
        flex-shrink: 0;
      }

      .step-text {
        font-size: 13px;
        color: var(--dt-text-secondary);
      }
    }
  }
}
</style>
