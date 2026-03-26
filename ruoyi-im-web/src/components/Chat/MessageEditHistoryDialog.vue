<template>
  <el-dialog
    v-model="visible"
    title="消息编辑历史"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="edit-history-container">
      <!-- 消息预览 -->
      <div class="message-preview">
        <div class="preview-label">
          当前内容
        </div>
        <div class="preview-content">
          {{ currentContent }}
        </div>
        <div class="preview-meta">
          <span>编辑次数：{{ editCount }} 次</span>
        </div>
      </div>

      <!-- 编辑历史列表 -->
      <div class="history-list">
        <div class="history-header">
          <span class="material-icons-outlined">history</span>
          <span>编辑记录</span>
        </div>

        <div v-if="historyList.length > 0" class="history-items">
          <div
            v-for="(item, index) in historyList"
            :key="item.id"
            class="history-item"
            :class="{ 'first': index === 0 }"
          >
            <div class="history-info">
              <img :src="item.editorAvatar || '/default-avatar.png'" class="editor-avatar" @error="handleAvatarError">
              <div class="editor-detail">
                <div class="editor-name">
                  {{ item.editorName }}
                </div>
                <div class="edit-time">
                  {{ formatEditTime(item.editTime) }}
                </div>
              </div>
            </div>

            <div class="content-compare">
              <div class="content-before">
                <div class="content-label">
                  编辑前
                </div>
                <div class="content-text">
                  {{ item.oldContent }}
                </div>
              </div>
              <div class="content-arrow">
                <span class="material-icons-outlined">arrow_downward</span>
              </div>
              <div class="content-after">
                <div class="content-label">
                  编辑后
                </div>
                <div class="content-text">
                  {{ item.newContent }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无编辑历史" :image-size="80" />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">
        关闭
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getMessageEditHistory } from '../../api/im/message'

const props = defineProps({
  modelValue: Boolean,
  messageId: Number,
  currentContent: String,
  editCount: Number
})

const emit = defineEmits(['update:modelValue', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const historyList = ref([])

// 加载编辑历史
const loadEditHistory = async () => {
  if (!props.messageId) return

  try {
    const res = await getMessageEditHistory(props.messageId)
    if (res.code === 200 && res.data) {
      historyList.value = res.data || []
    }
  } catch (e) {
    console.error('获取编辑历史失败:', e)
    ElMessage.error('获取编辑历史失败')
  }
}

// 格式化编辑时间
const formatEditTime = (time) => {
  if (!time) return ''
  const t = dayjs(time)
  return t.format('YYYY-MM-DD HH:mm:ss')
}

// 头像加载错误处理
const handleAvatarError = (e) => {
  e.target.src = '/default-avatar.png'
}

// 监听打开
watch(() => visible.value, (val) => {
  if (val && props.messageId) {
    loadEditHistory()
  }
})

// 监听 messageId 变化
watch(() => props.messageId, (val) => {
  if (visible.value && val) {
    loadEditHistory()
  }
})
</script>

<style scoped lang="scss">
.edit-history-container {
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.message-preview {
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-lg);
  padding: 12px 16px;
  margin-bottom: 20px;

  .preview-label {
    font-size: 12px;
    color: var(--dt-text-secondary);
    margin-bottom: 6px;
  }

  .preview-content {
    font-size: 14px;
    color: var(--dt-text-primary);
    line-height: 1.6;
    padding: 10px;
    background: var(--dt-bg-card);
    border-radius: var(--dt-radius-md);
    margin-bottom: 8px;
    max-height: 100px;
    overflow-y: auto;
  }

  .preview-meta {
    font-size: 12px;
    color: var(--dt-text-secondary);
  }
}

.history-list {
  .history-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 16px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--dt-border-light);

    .material-icons-outlined {
      font-size: 18px;
      color: var(--dt-brand-color);
    }
  }

  .history-items {
    .history-item {
      padding: 16px;
      border: 1px solid var(--dt-border-light);
      border-radius: var(--dt-radius-lg);
      margin-bottom: 12px;
      background: var(--dt-bg-card);

      &.first {
        border-color: var(--dt-brand-color);
        background: rgba(22, 119, 255, 0.02);
      }

      .history-info {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 12px;

        .editor-avatar {
          width: 32px;
          height: 32px;
          border-radius: var(--dt-radius-round);
          object-fit: cover;
        }

        .editor-detail {
          .editor-name {
            font-size: 13px;
            font-weight: 500;
            color: var(--dt-text-primary);
          }

          .edit-time {
            font-size: 11px;
            color: var(--dt-text-secondary);
            margin-top: 2px;
          }
        }
      }

      .content-compare {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .content-before, .content-after {
          padding: 10px 12px;
          border-radius: var(--dt-radius-md);
          font-size: 13px;
          line-height: 1.5;

          .content-label {
            font-size: 11px;
            color: var(--dt-text-secondary);
            margin-bottom: 4px;
          }

          .content-text {
            color: var(--dt-text-primary);
            word-break: break-word;
          }
        }

        .content-before {
          background: #fef0f0;
          border: 1px solid #fde2e2;

          .content-text {
            color: #f56c6c;
            text-decoration: line-through;
          }
        }

        .content-after {
          background: #f0f9ff;
          border: 1px solid #e1f3fd;

          .content-text {
            color: var(--dt-brand-color);
          }
        }

        .content-arrow {
          display: flex;
          justify-content: center;
          align-items: center;
          color: var(--dt-text-secondary);

          .material-icons-outlined {
            font-size: 20px;
          }
        }
      }
    }
  }
}
</style>
