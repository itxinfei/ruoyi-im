<template>
  <el-dialog
    v-model="visible"
    :title="'消息标记'"
    width="480px"
    class="message-mark-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="mark-header">
      <h3>标记消息</h3>
      <el-button circle text @click="visible = false">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div class="mark-content">
      <!-- 消息预览 -->
      <div class="message-preview" v-if="message">
        <div class="preview-header">
          <DingtalkAvatar
            :src="message.senderAvatar"
            :name="message.senderName"
            :size="32"
            shape="circle"
          />
          <div class="preview-info">
            <div class="preview-sender">{{ message.senderName }}</div>
            <div class="preview-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
        <div class="preview-content" v-html="formatContent(message.content)"></div>
      </div>

      <!-- 标记类型选择 -->
      <div class="mark-type-section">
        <div class="section-title">标记类型</div>
        <el-radio-group v-model="markType" size="large">
          <el-radio-button label="FLAG">
            <el-icon><Flag /></el-icon>
            <span>标记旗标</span>
          </el-radio-button>
          <el-radio-button label="IMPORTANT">
            <el-icon><Star /></el-icon>
            <span>重要</span>
          </el-radio-button>
          <el-radio-button label="TODO">
            <el-icon><DocumentChecked /></el-icon>
            <span>待办事项</span>
          </el-radio-button>
        </el-radio-group>

        <!-- 颜色选择 -->
        <div v-if="markType !== 'FLAG'" class="color-picker-section">
          <div class="section-title">标记颜色</div>
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color.value"
              class="color-option"
              :class="{ active: markColor === color.value }"
              @click="markColor = color.value"
            >
              <div class="color-preview" :style="{ backgroundColor: color.value }">
                <el-icon v-if="markColor === color.value"><Check /></el-icon>
              </div>
              <span>{{ color.label }}</span>
            </div>
          </div>
        </div>

        <!-- 提醒时间 -->
        <div v-if="markType === 'TODO'" class="reminder-section">
          <div class="section-title">提醒时间</div>
          <div class="reminder-picker">
            <el-time-picker
              v-model="remindTime"
              format="HH:mm"
              placeholder="选择提醒时间"
            />
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div class="remark-section">
        <div class="section-title">备注</div>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="3"
          placeholder="添加备注信息..."
          maxlength="500"
          show-word-limit
        />
      </div>
    </div>

    <div class="mark-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleMark" :disabled="!canMark">
        <el-icon><Flag /></el-icon>
        标记
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Flag, Star, DocumentChecked, Close } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: Boolean,
  message: Object
})

const emit = defineEmits(['update:modelValue', 'mark'])

const visible = ref(false)
const markType = ref('FLAG')
const markColor = ref('#ff4d4f')
const remark = ref('')
const remindTime = ref('')

// 颜色选项
const colorOptions = [
  { value: '#ff4d4f', label: '红色' },
  { value: '#52c41a', label: '绿色' },
  { value: '#faad14', label: '橙色' },
  { value: '#1890ff', label: '蓝色' },
  { value: '#722ed1', label: '紫色' },
  { value: '#13c2c2', label: '灰色' }
]

const canMark = computed(() => {
  return props.message && !props.message.isRevoked && !props.message.isDeleted
})

// 格式化内容
const formatContent = (content) => {
  if (!content) return ''
  return content
    // 处理表情
    .replace(/\[(\w+)\]/g, '<img class="emoji" src="/emoji/$1.png" alt="$1">')
    // 处理@提及
    .replace(/@(\S+)/g, '<span class="at-mention">@$1</span>')
    // 处理链接
    .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" class="message-link">$1</a>')
}

// 处理标记
const handleMark = () => {
  emit('mark', {
    messageId: props.message.id,
    markType: markType.value,
    markColor: markColor.value,
    remark: remark.value,
    remindTime: remindTime.value
  })
  
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style lang="scss" scoped>
.message-mark-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
    
    .el-dialog__header {
      padding: 16px 24px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .el-dialog__title {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }
    
    .el-dialog__body {
      padding: 0;
    }
  }
  
  .mark-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: var(--el-bg-color);
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }
  
  .mark-content {
    padding: 24px;
    
    .message-preview {
      margin-bottom: 16px;
      
      .preview-header {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: var(--el-fill-color-light);
        border-radius: 8px;
      }
      
      .preview-info {
        flex: 1;
        
        .preview-sender {
          font-weight: 500;
          color: var(--el-text-color-primary);
        }
        
        .preview-time {
          font-size: 12px;
          color: var(--el-text-color-placeholder);
          margin-left: auto;
        }
      }
      
      .preview-content {
        line-height: 1.5;
        color: var(--el-text-color-primary);
        
        :deep(.emoji) {
          width: 20px;
          vertical-align: middle;
        }
        
        :deep(.at-mention) {
          color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
          padding: 2px 4px;
          border-radius: 4px;
        }
        
        :deep(.message-link) {
          color: var(--el-color-primary);
          text-decoration: none;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
  
  .mark-type-section {
    margin-bottom: 20px;
    
    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      margin-bottom: 8px;
    }
    
    .el-radio-group {
      display: flex;
      gap: 8px;
      
      :deep(.el-radio-button__inner) {
        padding: 8px 16px;
      }
    }
  }
  
  .color-picker {
    margin-top: 12px;
    
    .color-options {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .color-option {
        cursor: pointer;
        
        .color-preview {
          width: 24px;
          height: 24px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 12px;
        }
        
        &.active {
          .color-preview {
            box-shadow: 0 0 0 2px currentColor;
          }
        }
      }
    }
  }
  
  .reminder-section {
    margin-top: 16px;
    
    .reminder-picker {
      margin-top: 8px;
    }
  }
  
  .remark-section {
    margin-top: 16px;
    
    .el-textarea {
      .el-input__inner {
        background: var(--el-bg-color);
        border-color: var(--el-border-color);
        border-radius: 6px;
      }
    }
  }
  
  .mark-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid var(--el-border-color-lighter);
    background: var(--el-bg-color);
    border-radius: 0 0 12px 12px;
    
    .el-button {
      min-width: 100px;
    }
  }
}

/* 动画 */
.mark-dialog-enter-active,
.mark-dialog-leave-active {
  transition: all 0.3s ease;
}

.mark-dialog-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}

.mark-dialog-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

/* 深色模式适配 */
.dark .mark-dialog {
  :deep(.el-dialog__header) {
    background: var(--el-bg-color-dark);
    border-bottom-color: var(--el-border-color-dark);
  }
  
  .mark-content .message-preview {
    background: var(--el-fill-color-dark);
    }
  
  .mark-type-section .section-title {
    color: var(--el-text-color-primary);
  }
}
</style>