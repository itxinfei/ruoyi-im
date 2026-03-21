<template>
  <div :class="['dingtalk-app', isDark ? 'dark' : '']">
    <div class="layout-wrapper">
      <!-- 1. 左侧一级导航 (固定 68px) -->
      <ImSideNavNew
        :active-module="activeModule"
        @switch-module="handleSwitchModule"
      />

      <!-- 2. 中间二级列表与右侧主区 (Flex 1) -->
      <main class="content-container">
        <!-- 消息模块专用：双栏布局 -->
        <template v-if="activeModule === 'chat'">
          <SessionPanel
            :current-session="currentSession"
            @select-session="handleSelectSession"
            @show-user="handleShowUser"
          />
          <ChatPanel
            v-if="currentSession"
            :session="currentSession"
            @show-user="handleShowUser"
            @create-group="handleCreateGroupFromChat"
            @toggle-detail="showDetailPanel = !showDetailPanel"
          />
          <aside v-if="currentSession && showDetailPanel" class="chat-side-panel">
            <div class="side-header">
              <span>详情</span>
              <button class="side-close-btn" @click="showDetailPanel = false">×</button>
            </div>
            <div class="side-body">
              <template v-if="currentSession">
                <div class="side-section side-hero">
                  <div class="side-title">{{ currentSession.name }}</div>
                  <div class="side-sub">
                    {{ currentSession.type === 'GROUP' ? `${currentSession.memberCount || 0} 人` : '单聊' }}
                  </div>
                  <div class="side-quick-row">
                    <button class="side-chip" @click="handleQuickPin">
                      {{ currentSession.isPinned ? '取消置顶' : '置顶' }}
                    </button>
                    <button class="side-chip" @click="handleQuickMute">
                      {{ currentSession.isMuted ? '取消静音' : '静音' }}
                    </button>
                    <button class="side-chip light" @click="handleSideGo('records')">聊天记录</button>
                  </div>
                </div>

                <div class="side-section">
                  <div class="side-row">
                    <span class="side-label">会话概览</span>
                  </div>
                  <div class="side-kpis">
                    <div class="kpi">
                      <div class="kpi-value">{{ currentSession.unreadCount || 0 }}</div>
                      <div class="kpi-label">未读</div>
                    </div>
                    <div class="kpi">
                      <div class="kpi-value">{{ currentSession.isPinned ? '是' : '否' }}</div>
                      <div class="kpi-label">置顶</div>
                    </div>
                    <div class="kpi">
                      <div class="kpi-value">{{ currentSession.isMuted ? '是' : '否' }}</div>
                      <div class="kpi-label">静音</div>
                    </div>
                  </div>
                </div>

                <div v-if="currentSession.type === 'GROUP'" class="side-section">
                  <div class="side-row">
                    <span class="side-label">成员</span>
                    <span class="side-count">{{ sideMembers.length }}</span>
                  </div>
                  <div class="side-members">
                    <div v-for="m in sideMembers" :key="m.userId" class="side-member">
                      <DingtalkAvatar :src="m.avatar" :name="m.nickname" :user-id="m.userId" :size="28" shape="square" @click="handleShowUser(m.userId)" />
                      <span class="member-name">{{ m.nickname }}</span>
                    </div>
                  </div>
                  <div class="side-actions-inline">
                    <button class="side-link" @click="handleSideGo('members')">查看全部成员</button>
                    <button class="side-link ghost" @click="handleSideGo('members')">邀请成员</button>
                  </div>
                </div>

                <div v-if="currentSession.type === 'GROUP'" class="side-section">
                  <div class="side-row">
                    <span class="side-label">文件</span>
                    <span class="side-count">{{ sideFileStats?.total || sideFiles.length || 0 }}</span>
                  </div>
                  <div class="side-mini">
                    <span>总数</span>
                    <span>{{ sideFileStats?.total || sideFiles.length || 0 }}</span>
                  </div>
                  <div v-if="sideFileStats?.totalSize" class="side-mini">
                    <span>总大小</span>
                    <span>{{ sideFileStats.totalSize }}</span>
                  </div>
                  <div class="side-files">
                    <div v-for="f in sideFiles" :key="f.id || f.fileId" class="file-row">
                      <span class="file-name">{{ f.fileName || f.name }}</span>
                      <span class="file-meta">{{ f.uploaderName || '' }}</span>
                    </div>
                  </div>
                  <div v-if="sideFileCategories.length" class="side-cates">
                    <div v-for="c in sideFileCategories.slice(0, 4)" :key="c.name || c.category" class="cate-row">
                      <span class="cate-name">{{ c.name || c.category }}</span>
                      <span class="cate-count">{{ c.count || c.total || 0 }}</span>
                    </div>
                  </div>
                  <div class="side-actions-inline">
                    <button class="side-link" @click="handleSideGo('files')">查看全部文件</button>
                    <button class="side-link ghost" @click="handleSideGo('files')">文件分类</button>
                  </div>
                </div>

                <div v-if="currentSession.type === 'PRIVATE' && sideUser" class="side-section">
                  <div class="side-row">
                    <span class="side-label">个人信息</span>
                  </div>
                  <div class="side-item"><span>部门</span><span class="side-value">{{ sideUser.departmentName || sideUser.department || '—' }}</span></div>
                  <div class="side-item"><span>职位</span><span class="side-value">{{ sideUser.position || '—' }}</span></div>
                  <div class="side-item"><span>手机</span><span class="side-value">{{ sideUser.mobile || '—' }}</span></div>
                </div>

                <div class="side-section">
                  <div class="side-row">
                    <span class="side-label">会话内搜索</span>
                  </div>
                  <div class="side-search">
                    <input
                      v-model="sideSearch"
                      class="side-search-input"
                      placeholder="输入关键词"
                      @keyup.enter="handleSideSearch"
                    >
                    <button class="side-search-btn" @click="handleSideSearch">搜索</button>
                  </div>
                  <div class="side-actions">
                    <button class="side-chip" @click="handleSideGo('search')">会话内搜索</button>
                    <button class="side-chip light" @click="handleSideGo('records')">聊天记录</button>
                  </div>
                </div>
              </template>
              <div v-else class="side-empty">
                选择会话以查看详情
              </div>
            </div>
          </aside>
        </template>

        <!-- 其他模块：单面板布局 -->
        <template v-else>
          <div class="module-panel">
            <WorkbenchPanel v-if="activeModule === 'workbench'" />
            <ContactsPanel v-if="activeModule === 'contacts'" @show-user="handleShowUser" />
            <DocumentsPanel v-if="activeModule === 'drive'" />
            <CalendarPanel v-if="activeModule === 'calendar'" />
            <TodoPanel v-if="activeModule === 'todo'" />
            <ApprovalPanel v-if="activeModule === 'approval'" />
            <MailPanel v-if="activeModule === 'mail'" />
            <AssistantPanel v-if="activeModule === 'assistant'" />
            <AdminLayout v-if="activeModule === 'admin'" />
            <SearchPanel
              v-if="activeModule === 'search'"
              @show-user="handleShowUser"
              @go-to-session="handleGoToSession"
              @go-to-group="handleGoToGroup"
            />
            <SettingsPanel v-if="activeModule === 'settings'" />
            <ProfilePanel v-if="activeModule === 'profile'" />
          </div>
        </template>
      </main>

      <!-- 全局交互层 -->
      <UserProfileDialog
        v-model="showUserDetail"
        :user-id="detailUserId"
        @start-call="handleStartCall"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useStore } from 'vuex'
import { getGroup } from '@/api/im/group'
import { getUserInfo } from '@/api/im/user'
import { getGroupFiles, getGroupFileStatistics, getGroupFileCategories } from '@/api/im/groupFile'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useTheme } from '@/composables/useTheme'
import ImSideNavNew from '@/components/ImSideNavNew/index.vue'
import SessionPanel from './SessionPanel.vue'
import ChatPanel from './ChatPanel.vue'
import WorkbenchPanel from './WorkbenchPanel.vue'
import ContactsPanel from './ContactsPanel.vue'
import DocumentsPanel from './DocumentsPanel.vue'
import CalendarPanel from './CalendarPanel.vue'
import TodoPanel from './TodoPanel.vue'
import ApprovalPanel from './ApprovalPanel.vue'
import MailPanel from './MailPanel.vue'
import AssistantPanel from './AssistantPanel.vue'
import SearchPanel from './SearchPanel.vue'
import SettingsPanel from './SettingsPanel.vue'
import ProfilePanel from './ProfilePanel.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import AdminLayout from './admin/AdminLayout.vue'
import ChatDetailDrawer from '@/components/Chat/ChatDetailDrawer.vue'

const store = useStore()
const activeModule = ref('chat')
const showUserDetail = ref(false)
const detailUserId = ref(null)

const { connect, disconnect, onMessage, onRead, onOnline, onOffline } = useImWebSocket()
const theme = useTheme()

const currentSession = computed(() => store.state.im?.session?.currentSession || null)
const isDark = computed(() => theme.isDark.value)

const showDetailPanel = ref(false)

const handleSwitchModule = (m) => {
  activeModule.value = m
}
const handleSelectSession = (s) => { store.commit('im/session/SET_CURRENT_SESSION', s) }

const initWebSocket = () => {
  const token = store.state.user?.token
  if (!token) return

  connect(token)

  // 监听新消息
  onMessage((message) => {
    store.dispatch('im/message/receiveMessage', message)
  })

  // 监听已读状态（含多端同步）
  onRead((payload) => {
    // 如果是自己其他端发送的已读，同步更新本地未读数
    if (payload.userId === store.state.user?.currentUser?.id) {
      store.commit('im/session/UPDATE_SESSION', {
        id: payload.conversationId,
        unreadCount: 0
      })
    }
  })

  // 监听在线状态
  onOnline((payload) => {
    store.commit('im/contact/SET_USER_STATUS', { userId: payload.userId, status: 'online' })
  })

  onOffline((payload) => {
    store.commit('im/contact/SET_USER_STATUS', { userId: payload.userId, status: 'offline' })
  })
}

onMounted(() => {
  if (store.state.user?.token) {
    store.dispatch('im/session/loadSessions').catch(err => {
      console.warn('会话加载失败', err)
    })
    initWebSocket()
  }
})

onUnmounted(() => {
  disconnect()
})
</script>

<style lang="scss" scoped>
.dingtalk-app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--dt-bg-body);
  display: flex;
  min-width: 1200px;
}

.layout-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
}

.content-container {
  flex: 1;
  display: flex;
  height: 100%;
  min-width: 0; 
  background: var(--dt-bg-card);
  position: relative;
  overflow: hidden;
}

.module-panel {
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
}

// 统一过度动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-base);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
