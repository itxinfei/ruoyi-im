<template>
  <div class="friend-requests-panel">
    <template v-if="inline">
      <div v-loading="loading" class="requests-list">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="收到的申请" name="received">
            <div class="request-items">
              <div
                v-for="request in receivedRequests"
                :key="request.id"
                class="request-item"
              >
                <el-avatar :size="48" :src="addTokenToUrl(request.avatar)">
                  {{ request.name?.charAt(0) }}
                </el-avatar>
                <div class="request-info">
                  <div class="request-name">{{ request.name }}</div>
                  <div class="request-message">{{ request.message || '请求添加你为好友' }}</div>
                  <div class="request-time">{{ formatTime(request.createTime) }}</div>
                </div>
                <div class="request-actions">
                  <el-button
                    v-if="request.status === 'PENDING'"
                    type="primary"
                    size="small"
                    @click="handleAccept(request)"
                  >
                    接受
                  </el-button>
                  <el-button
                    v-if="request.status === 'PENDING'"
                    size="small"
                    @click="handleReject(request)"
                  >
                    拒绝
                  </el-button>
                  <el-tag v-if="request.status === 'ACCEPTED'" type="success" size="small">
                    已接受
                  </el-tag>
                  <el-tag v-if="request.status === 'REJECTED'" type="info" size="small">
                    已拒绝
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="receivedRequests.length === 0" description="暂无好友申请" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="发出的申请" name="sent">
            <div class="request-items">
              <div
                v-for="request in sentRequests"
                :key="request.id"
                class="request-item"
              >
                <el-avatar :size="48" :src="addTokenToUrl(request.avatar)">
                  {{ request.name?.charAt(0) }}
                </el-avatar>
                <div class="request-info">
                  <div class="request-name">{{ request.name }}</div>
                  <div class="request-message">{{ request.message || '等待对方确认' }}</div>
                  <div class="request-time">{{ formatTime(request.createTime) }}</div>
                </div>
                <div class="request-status">
                  <el-tag v-if="request.status === 'PENDING'" type="warning" size="small">
                    等待确认
                  </el-tag>
                  <el-tag v-if="request.status === 'ACCEPTED'" type="success" size="small">
                    已同意
                  </el-tag>
                  <el-tag v-if="request.status === 'REJECTED'" type="danger" size="small">
                    已拒绝
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="sentRequests.length === 0" description="暂无发出的申请" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
    <el-dialog
      v-else
      v-model="visible"
      title="好友申请"
      width="600px"
      :close-on-click-modal="true"
      @close="handleClose"
    >
      <div v-loading="loading" class="requests-list">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="收到的申请" name="received">
            <div class="request-items">
              <div
                v-for="request in receivedRequests"
                :key="request.id"
                class="request-item"
              >
                <el-avatar :size="48" :src="addTokenToUrl(request.avatar)">
                  {{ request.name?.charAt(0) }}
                </el-avatar>
                <div class="request-info">
                  <div class="request-name">{{ request.name }}</div>
                  <div class="request-message">{{ request.message || '请求添加你为好友' }}</div>
                  <div class="request-time">{{ formatTime(request.createTime) }}</div>
                </div>
                <div class="request-actions">
                  <el-button
                    v-if="request.status === 'PENDING'"
                    type="primary"
                    size="small"
                    @click="handleAccept(request)"
                  >
                    接受
                  </el-button>
                  <el-button
                    v-if="request.status === 'PENDING'"
                    size="small"
                    @click="handleReject(request)"
                  >
                    拒绝
                  </el-button>
                  <el-tag v-if="request.status === 'ACCEPTED'" type="success" size="small">
                    已接受
                  </el-tag>
                  <el-tag v-if="request.status === 'REJECTED'" type="info" size="small">
                    已拒绝
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="receivedRequests.length === 0" description="暂无好友申请" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="发出的申请" name="sent">
            <div class="request-items">
              <div
                v-for="request in sentRequests"
                :key="request.id"
                class="request-item"
              >
                <el-avatar :size="48" :src="request.avatar">
                  {{ request.name?.charAt(0) }}
                </el-avatar>
                <div class="request-info">
                  <div class="request-name">{{ request.name }}</div>
                  <div class="request-message">{{ request.message || '等待对方确认' }}</div>
                  <div class="request-time">{{ formatTime(request.createTime) }}</div>
                </div>
                <div class="request-status">
                  <el-tag v-if="request.status === 'PENDING'" type="warning" size="small">
                    等待确认
                  </el-tag>
                  <el-tag v-if="request.status === 'ACCEPTED'" type="success" size="small">
                    已同意
                  </el-tag>
                  <el-tag v-if="request.status === 'REJECTED'" type="danger" size="small">
                    已拒绝
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="sentRequests.length === 0" description="暂无发出的申请" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFriendRequests, handleFriendRequest } from '@/api/im/contact'
import { addTokenToUrl } from '@/utils/file'
import { formatRelativeTime } from '@/utils/format'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  inline: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const loading = ref(false)
const activeTab = ref('received')
const receivedRequests = ref([])
const sentRequests = ref([])

// 加载好友申请
const loadRequests = async () => {
  loading.value = true
  try {
    const response = await getFriendRequests()
    if (response && response.data) {
      const requests = response.data
      receivedRequests.value = requests.filter(r => r.direction === 'RECEIVED')
      sentRequests.value = requests.filter(r => r.direction === 'SENT')
    }
  } catch (error) {
    console.error('加载好友申请失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 接受申请
const handleAccept = async (request) => {
  try {
    await handleFriendRequest(request.id, true)
    request.status = 'ACCEPTED'
    ElMessage.success('已接受好友申请')
    emit('refresh')
  } catch (error) {
    console.error('接受申请失败:', error)
    ElMessage.error('操作失败')
  }
}

// 拒绝申请
const handleReject = async (request) => {
  try {
    await handleFriendRequest(request.id, false)
    request.status = 'REJECTED'
    ElMessage.success('已拒绝好友申请')
  } catch (error) {
    console.error('拒绝申请失败:', error)
    ElMessage.error('操作失败')
  }
}

// 格式化时间
const formatTime = formatRelativeTime

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadRequests()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.requests-list {
  min-height: 400px;
}

.request-items {
  .request-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-radius: var(--dt-radius-md);
    margin-bottom: 12px;
    background: #f5f5f5;
    transition: background-color 0.2s;

    &:hover {
      background: #e8e8e8;
    }

    .request-info {
      flex: 1;

      .request-name {
        font-size: 15px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 4px;
      }

      .request-message {
        font-size: 13px;
        color: #595959;
        margin-bottom: 4px;
      }

      .request-time {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .request-actions {
      display: flex;
      gap: 8px;
    }

    .request-status {
      flex-shrink: 0;
    }
  }
}
</style>
