<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="panel-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索"
          prefix-icon="el-icon-search"
          clearable
          class="search-input"
        />
      </div>
      <session-list
        :sessions-prop="sessions"
        :current-session-id-prop="currentSession?.id"
        @select="handleSessionSelect"
      />
    </div>

    <!-- 中间聊天区域 -->
    <div class="chat-main">
      <template v-if="currentSession">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="header-info">
            <span class="chat-title">{{ currentSession.name }}</span>
            <span v-if="currentSession.type === 'group'" class="chat-subtitle">
              ({{ currentSession.memberCount }}人)
            </span>
          </div>
          <div class="header-actions">
            <el-button
              v-if="currentSession.type === 'private'"
              :icon="VideoCamera"
              circle
              size="small"
              @click="startVideoCall"
            />
            <el-button
              v-if="currentSession.type === 'private'"
              :icon="Phone"
              circle
              size="small"
              @click="startVoiceCall"
            />
            <el-button :icon="Search" circle size="small" @click="showSearchMessage" />
            <el-button :icon="More" circle size="small" @click="toggleDetailPanel" />
          </div>
        </div>

        <!-- 消息列表 -->
        <message-list
          ref="messageListRef"
          :session-id="currentSession.id"
          :is-group="currentSession.type === 'group'"
          @load-more="loadMoreMessages"
        />

        <!-- 聊天输入 -->
        <chat-input
          :session-id="currentSession.id"
          :session-members="sessionMembers"
          @send-message="handleSendMessage"
          @send-file="handleSendFile"
          @send-image="handleSendImage"
          @send-voice="handleSendVoice"
          @send-location="handleSendLocation"
          @send-vote="handleSendVote"
          @send-code="handleSendCode"
          @start-call="handleStartCall"
        />
      </template>

      <!-- 空状态 -->
      <div v-else class="chat-empty">
        <el-empty description="选择一个会话开始聊天" />
      </div>
    </div>

    <!-- 右侧详情面板 -->
    <transition name="slide-right">
      <div v-if="showDetail && currentSession" class="detail-panel">
        <div class="panel-header">
          <span class="panel-title">{{
            currentSession.type === 'group' ? '群聊信息' : '联系人信息'
          }}</span>
          <el-button :icon="Close" circle size="small" @click="toggleDetailPanel" />
        </div>

        <div class="panel-content">
          <!-- 头像和名称 -->
          <div class="info-header">
            <el-avatar :size="64" :src="currentSession.avatar">
              {{ currentSession.name?.charAt(0) }}
            </el-avatar>
            <h3>{{ currentSession.name }}</h3>
            <p class="info-type">{{ currentSession.type === 'group' ? '群聊' : '联系人' }}</p>
          </div>

          <!-- 群组成员 -->
          <template v-if="currentSession.type === 'group'">
            <div class="info-section">
              <div class="section-header">
                <span>群组成员 ({{ currentSession.memberCount }})</span>
                <el-button
                  v-if="isGroupOwner || isGroupAdmin"
                  type="primary"
                  link
                  @click="showGroupMembers"
                >
                  管理
                </el-button>
              </div>
              <div class="member-grid">
                <div
                  v-for="member in groupMembers.slice(0, 8)"
                  :key="member.userId"
                  class="member-item"
                >
                  <el-avatar :size="36" :src="member.avatar">
                    {{ member.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="member-name">{{ member.nickname }}</span>
                </div>
                <div
                  v-if="groupMembers.length > 8"
                  class="member-item more"
                  @click="showGroupMembers"
                >
                  <div class="more-icon">+{{ groupMembers.length - 8 }}</div>
                </div>
              </div>
            </div>
          </template>

          <!-- 联系人信息 -->
          <template v-else>
            <div class="info-section">
              <div class="info-row">
                <span class="label">账号</span>
                <span class="value">{{ currentSession.account }}</span>
              </div>
              <div class="info-row">
                <span class="label">备注</span>
                <span class="value editable" @click="startEditRemark">
                  {{ currentSession.remark || '设置备注' }}
                </span>
              </div>
            </div>
          </template>

          <!-- 操作按钮 -->
          <div class="info-section">
            <div class="action-row" @click="searchMessage">
              <i class="el-icon-search"></i>
              <span>搜索聊天记录</span>
            </div>
            <div class="action-row" @click="exportHistory">
              <i class="el-icon-download"></i>
              <span>导出聊天记录</span>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 搜索消息对话框 -->
    <el-dialog v-model="searchDialogVisible" title="搜索聊天记录" width="600px">
      <div class="search-dialog">
        <el-input
          v-model="messageSearchKeyword"
          placeholder="搜索消息内容"
          prefix-icon="el-icon-search"
          clearable
          @input="handleMessageSearch"
        />
        <div v-loading="searching" class="search-results">
          <div
            v-for="message in searchResults"
            :key="message.id"
            class="search-item"
            @click="jumpToMessage(message)"
          >
            <div class="message-info">
              <span class="sender">{{ message.senderName }}</span>
              <span class="time">{{ formatTime(message.time) }}</span>
            </div>
            <div class="message-content" v-html="highlightKeyword(message.content)"></div>
          </div>
          <el-empty v-if="!searching && !searchResults.length" description="没有找到相关消息" />
        </div>
      </div>
    </el-dialog>

    <!-- 群成员管理对话框 -->
    <el-dialog v-model="memberDialogVisible" title="群成员管理" width="600px">
      <div class="member-dialog">
        <div class="dialog-actions">
          <el-button type="primary" size="small" @click="showAddMember">添加成员</el-button>
        </div>
        <el-table :data="groupMembers" style="width: 100%">
          <el-table-column label="成员" width="200">
            <template #default="scope">
              <div class="member-cell">
                <el-avatar :size="32" :src="scope.row.avatar">
                  {{ scope.row.nickname?.charAt(0) }}
                </el-avatar>
                <span>{{ scope.row.nickname }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template #default="scope">
              <el-tag
                :type="
                  scope.row.role === 'owner'
                    ? 'danger'
                    : scope.row.role === 'admin'
                      ? 'warning'
                      : ''
                "
                size="small"
              >
                {{
                  scope.row.role === 'owner'
                    ? '群主'
                    : scope.row.role === 'admin'
                      ? '管理员'
                      : '成员'
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button
                v-if="isGroupOwner && scope.row.role === 'member'"
                type="primary"
                link
                size="small"
                @click="setAdmin(scope.row)"
              >
                设为管理员
              </el-button>
              <el-button
                v-if="canManageMember(scope.row)"
                type="danger"
                link
                size="small"
                @click="removeMember(scope.row)"
              >
                移出群组
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Phone, Search, More, Close } from '@element-plus/icons-vue'
import SessionList from '@/components/Chat/SessionList.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import ChatInput from '@/components/Chat/ChatInput.vue'

const store = useStore()

// 状态
const searchKeyword = ref('')
const showDetail = ref(false)
const searchDialogVisible = ref(false)
const memberDialogVisible = ref(false)
const messageSearchKeyword = ref('')
const searching = ref(false)
const searchResults = ref([])
const messageListRef = ref(null)

// 计算属性
const currentSession = computed(() => store.state.im.currentSession)
const sessions = computed(() => store.state.im.sessions)
const groupMembers = computed(() => store.getters.groupMembers || [])
const isGroupOwner = computed(() => store.getters.isGroupOwner)
const isGroupAdmin = computed(() => store.getters.isGroupAdmin)

const sessionMembers = computed(() => {
  if (!currentSession.value) return []
  if (currentSession.value.type === 'group') {
    return groupMembers.value
  }
  return []
})

// 会话选择
const handleSessionSelect = session => {
  store.dispatch('im/switchSession', session)
}

// 切换详情面板
const toggleDetailPanel = () => {
  showDetail.value = !showDetail.value
}

// 发送消息
const handleSendMessage = async payload => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: payload.type || 'text',
      content: payload.content,
      replyTo: payload.replyTo,
    })
  } catch (error) {
    ElMessage.error('发送消息失败')
  }
}

// 发送文件
const handleSendFile = async file => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'file',
      content: file,
    })
  } catch (e) {
    ElMessage.error('文件发送失败')
  }
}

// 发送图片
const handleSendImage = async image => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'image',
      content: image,
    })
  } catch (e) {
    ElMessage.error('图片发送失败')
  }
}

// 发送语音
const handleSendVoice = async voice => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'voice',
      content: voice,
    })
  } catch (e) {
    ElMessage.error('语音发送失败')
  }
}

// 发送位置
const handleSendLocation = async location => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'location',
      content: location,
    })
  } catch (e) {
    ElMessage.error('位置发送失败')
  }
}

// 发送投票
const handleSendVote = async vote => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'vote',
      content: vote,
    })
  } catch (e) {
    ElMessage.error('投票发送失败')
  }
}

// 发送代码
const handleSendCode = async code => {
  if (!currentSession.value) return
  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      sessionType: currentSession.value.type,
      type: 'code',
      content: code,
    })
  } catch (e) {
    ElMessage.error('代码片段发送失败')
  }
}

// 开始通话
const handleStartCall = ({ type }) => {
  if (type === 'video') startVideoCall()
  if (type === 'voice') startVoiceCall()
}

// 加载更多消息
const loadMoreMessages = () => {
  if (!currentSession.value) return
  store.dispatch('im/loadMoreMessages', currentSession.value.id)
}

// 视频通话
const startVideoCall = () => {
  if (!currentSession.value || currentSession.value.type !== 'private') return
  store.dispatch('im/startVideoCall', currentSession.value.id)
}

// 语音通话
const startVoiceCall = () => {
  if (!currentSession.value || currentSession.value.type !== 'private') return
  store.dispatch('im/startVoiceCall', currentSession.value.id)
}

// 显示消息搜索
const showSearchMessage = () => {
  searchDialogVisible.value = true
  messageSearchKeyword.value = ''
  searchResults.value = []
}

// 搜索消息
const handleMessageSearch = async () => {
  if (!messageSearchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searching.value = true
  try {
    const results = await store.dispatch('im/searchMessages', {
      sessionId: currentSession.value.id,
      keyword: messageSearchKeyword.value,
    })
    searchResults.value = results
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 跳转到消息
const jumpToMessage = message => {
  searchDialogVisible.value = false
  messageListRef.value?.scrollToMessage(message.id)
}

// HTML 实体转义，防止 XSS
const escapeHtml = text => {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  }
  return String(text).replace(/[&<>"']/g, m => map[m])
}

// 高亮关键词（先转义 HTML，再添加高亮）
const highlightKeyword = content => {
  if (!content) return ''
  const escaped = escapeHtml(content)
  if (!messageSearchKeyword.value) return escaped
  const keyword = escapeHtml(messageSearchKeyword.value)
  const reg = new RegExp(keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi')
  return escaped.replace(reg, match => `<span class="highlight">${match}</span>`)
}

// 搜索聊天记录
const searchMessage = () => {
  showSearchMessage()
}

// 导出聊天记录
const exportHistory = async () => {
  try {
    await store.dispatch('im/exportChatHistory', currentSession.value.id)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 显示群成员管理
const showGroupMembers = () => {
  memberDialogVisible.value = true
}

// 显示添加成员
const showAddMember = () => {
  ElMessage.info('添加成员功能开发中...')
}

// 是否可以管理成员
const canManageMember = member => {
  if (isGroupOwner.value) return member.role !== 'owner'
  if (isGroupAdmin.value) return member.role === 'member'
  return false
}

// 设置管理员
const setAdmin = async member => {
  try {
    await store.dispatch('im/setGroupAdmin', {
      groupId: currentSession.value.id,
      userId: member.userId,
    })
    ElMessage.success('设置成功')
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

// 移除成员
const removeMember = async member => {
  try {
    await ElMessageBox.confirm(`确定要将 ${member.nickname} 移出群组吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await store.dispatch('im/removeGroupMember', {
      groupId: currentSession.value.id,
      userId: member.userId,
    })
    ElMessage.success('移除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 编辑备注
const startEditRemark = () => {
  ElMessage.info('编辑备注功能开发中...')
}

// 格式化时间
const formatTime = time => {
  return new Date(time).toLocaleString()
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.chat-container {
  height: 100%;
  display: flex;
  background-color: #f5f5f5;
}

// 左侧会话列表面板
.session-panel {
  width: $list-panel-width;
  min-width: $list-panel-width;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 12px;
    border-bottom: 1px solid $border-color-lighter;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        background-color: #f5f5f5;
      }
    }
  }
}

// 中间聊天主区域
.chat-main {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;

  .chat-header {
    height: 60px;
    min-height: 60px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid $border-color-lighter;
    background-color: #fff;

    .header-info {
      display: flex;
      align-items: baseline;
      gap: 8px;

      .chat-title {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
      }

      .chat-subtitle {
        font-size: 12px;
        color: $text-secondary;
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .chat-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

// 右侧详情面板
.detail-panel {
  width: $detail-panel-width;
  min-width: $detail-panel-width;
  height: 100%;
  background-color: #fff;
  border-left: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;

  .panel-header {
    height: 60px;
    min-height: 60px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid $border-color-lighter;

    .panel-title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    .info-header {
      text-align: center;
      padding-bottom: 16px;
      border-bottom: 1px solid $border-color-lighter;
      margin-bottom: 16px;

      h3 {
        margin: 12px 0 4px;
        font-size: 16px;
      }

      .info-type {
        color: $text-secondary;
        font-size: 12px;
        margin: 0;
      }
    }

    .info-section {
      margin-bottom: 20px;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        font-weight: 500;
      }

      .member-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 12px;

        .member-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;

          .member-name {
            font-size: 12px;
            color: $text-secondary;
            max-width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          &.more {
            cursor: pointer;

            .more-icon {
              width: 36px;
              height: 36px;
              border-radius: 50%;
              background-color: #f5f5f5;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 12px;
              color: $text-secondary;
            }
          }
        }
      }

      .info-row {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .label {
          width: 60px;
          color: $text-secondary;
          font-size: 14px;
        }

        .value {
          flex: 1;
          font-size: 14px;

          &.editable {
            color: $primary-color;
            cursor: pointer;

            &:hover {
              text-decoration: underline;
            }
          }
        }
      }

      .action-row {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 0;
        cursor: pointer;
        color: $text-regular;

        &:hover {
          color: $primary-color;
        }

        i {
          font-size: 16px;
        }
      }
    }
  }
}

// 搜索对话框
.search-dialog {
  .search-results {
    max-height: 400px;
    overflow-y: auto;
    margin-top: 16px;

    .search-item {
      padding: 12px;
      cursor: pointer;
      border-radius: 4px;

      &:hover {
        background-color: #f5f5f5;
      }

      .message-info {
        margin-bottom: 4px;

        .sender {
          font-weight: 500;
        }

        .time {
          margin-left: 8px;
          font-size: 12px;
          color: $text-secondary;
        }
      }

      .message-content {
        color: $text-regular;

        :deep(.highlight) {
          background-color: #ffd04b;
          padding: 0 2px;
        }
      }
    }
  }
}

// 成员对话框
.member-dialog {
  .dialog-actions {
    margin-bottom: 16px;
  }

  .member-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

// 过渡动画
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

// 响应式
@media screen and (max-width: 768px) {
  .session-panel {
    width: 100%;
    position: absolute;
    z-index: 10;
  }

  .detail-panel {
    width: 100%;
    position: absolute;
    z-index: 10;
    right: 0;
  }
}
</style>
