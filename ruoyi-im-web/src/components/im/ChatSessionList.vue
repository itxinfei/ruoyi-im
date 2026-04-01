<template>
  <div class="session-panel">
    <!-- 头部搜索与筛选 (对齐钉钉 8px 步进) -->
    <div class="panel-header">
      <div class="search-bar" :class="{ 'is-focused': isSearchFocused }">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索"
          @focus="isSearchFocused = true"
          @blur="isSearchFocused = false"
        />
      </div>
      <!-- 会话类型过滤 tabs -->
      <div class="session-tabs">
        <button
          v-for="tab in sessionTabs"
          :key="tab.value"
          class="session-tab"
          :class="{ active: filterType === tab.value }"
          @click="filterType = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 会话列表区 -->
    <div class="session-list" v-loading="loading">
      <div 
        v-for="session in filteredSessions" 
        :key="session.id"
        class="session-item"
        :class="{ 'is-active': currentSession && currentSession.id === session.id, 'is-pinned': session.isPinned }"
        @click="selectSession(session)"
      >
        <!-- 头像区 (对齐钉钉 4px 圆角) -->
        <div class="avatar-wrapper">
          <img :src="session.avatar || '/avatars/default.png'" class="avatar" alt="avatar" />
          <div v-if="session.unreadCount > 0" class="unread-badge" :class="getUnreadBadgeClass(session.unreadCount)">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </div>
        </div>

        <!-- 内容区 -->
        <div class="content-wrapper">
          <div class="content-top">
            <span class="session-name">{{ session.name }}</span>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="content-bottom">
            <span v-if="session.draftContent" class="draft-tag">[草稿]</span>
            <span class="last-message">{{ session.lastMessage }}</span>
            <el-icon v-if="session.isMuted" class="mute-icon" :size="14"><BellFilled /></el-icon>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredSessions.length === 0" class="empty-list">
        <el-icon :size="48" color="var(--dt-border-lighter)"><ChatLineRound /></el-icon>
        <p>暂无消息会话</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatSessionList.vue (对齐钉钉 250px 规范)
 * 修复：数据初始化加载、色彩对齐、图标修正
 */
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { Search, BellFilled, ChatLineRound } from '@element-plus/icons-vue';

const store = useStore();

// 内部状态
const isSearchFocused = ref(false);
const searchKeyword = ref('');
const filterType = ref('all');

// 会话类型过滤 tabs
const sessionTabs = [
  { label: '全部', value: 'all' },
  { label: '单聊', value: 'PRIVATE' },
  { label: '群聊', value: 'GROUP' }
];

// 数据绑定 (对齐 Store 路径 im/session)
const loading = computed(() => store.state.im?.session?.loading);
const currentSession = computed(() => store.state.im?.session?.currentSession);
const sessions = computed(() => store.getters['im/session/sortedSessions']);

const filteredSessions = computed(() => {
  let list = sessions.value || [];
  // 按类型过滤
  if (filterType.value !== 'all') {
    list = list.filter(s => s.type === filterType.value);
  }
  // 按关键词过滤
  if (searchKeyword.value) {
    list = list.filter(s => s.name?.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  }
  return list;
});

// 初始化加载
onMounted(() => {
  store.dispatch('im/session/loadSessions');
});

const selectSession = (session) => {
  store.dispatch('im/session/selectSession', session);
};

// 未读角标圆角：1-9 圆形，10+ 胶囊
const getUnreadBadgeClass = (count) => {
  if (count >= 10) {
    return 'unread-badge-capsule';
  }
  return 'unread-badge-circle';
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  
  // 如果是今天，显示时间；否则显示日期
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' + 
           date.getMinutes().toString().padStart(2, '0');
  }
  return (date.getMonth() + 1) + '/' + date.getDate();
};
</script>

<style scoped>
.session-panel {
  width: var(--dt-session-panel-width);
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-session-list);
  border-right: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.panel-header {
  padding: var(--dt-spacing-lg) var(--dt-spacing-lg) var(--dt-spacing-sm);
  background-color: var(--dt-bg-session-list);
}

.session-tabs {
  display: flex;
  gap: 2px;
  margin-top: 8px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
  padding: 2px;
}

.session-tab {
  flex: 1;
  height: 26px;
  border: none;
  background: transparent;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.session-tab:hover {
  color: var(--dt-text-primary);
}

.session-tab.active {
  background-color: var(--dt-bg-card);
  color: var(--dt-brand-color);
  font-weight: 600;
  box-shadow: var(--dt-shadow-1);
}

.search-bar {
  display: flex;
  align-items: center;
  height: 32px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
  padding: 0 8px;
  border: 1.5px solid transparent;
  transition: all var(--dt-transition-fast);
}

.search-bar.is-focused {
  background-color: var(--dt-bg-card);
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 2px var(--dt-brand-bg);
}

.search-icon {
  color: var(--dt-text-tertiary);
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  margin-left: 6px;
  width: 100%;
  font-size: 13px;
  color: var(--dt-text-primary);
}

.search-bar input::placeholder {
  color: var(--dt-text-tertiary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

/* 滚动条美化 */
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.05); border-radius: 2px; }

.session-item {
  display: flex;
  align-items: center;
  height: 64px; /* 对齐钉钉 Windows 客户端规范 */
  padding: 0 var(--dt-spacing-md); /* 钉钉规范：左右12px */
  cursor: pointer;
  position: relative;
  transition: all var(--dt-transition-fast);
}

.session-item:hover {
  background-color: var(--dt-bg-session-hover);
  transform: translateX(2px);
}

.session-item.is-active {
  background-color: var(--dt-bg-session-active);
}

/* 钉钉 3px 激活蓝条 */
.session-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: var(--dt-brand-color);
  border-radius: 0 2px 2px 0;
}

/* 置顶背景色 */
.session-item.is-pinned:not(.is-active) {
  background-color: var(--dt-bg-body);
}

.avatar-wrapper {
  position: relative;
  width: var(--dt-avatar-size-md); /* 36px */
  height: var(--dt-avatar-size-md); /* 36px */
  margin-right: var(--dt-spacing-lg);
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: var(--dt-radius-sm); /* 对齐钉钉方圆 */
  object-fit: cover;
  border: 1px solid var(--dt-border-lighter);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: var(--dt-unread-color);
  color: var(--dt-text-white);
  font-size: 10px;
  font-weight: var(--dt-font-weight-semibold);
  line-height: 16px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.3);
}

/* 钉钉规范：1-9 数字圆形，10+ 数字胶囊 */
.unread-badge-circle {
  border-radius: var(--dt-radius-full); /* 50% 当 width=height=16px 时为正圆 */
}

.unread-badge-capsule {
  border-radius: 8px; /* 高度 16px 的一半，圆角为 8px */
}

.content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.content-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-name {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  margin-left: 8px;
}

.content-bottom {
  display: flex;
  align-items: center;
  gap: 4px;
}

.last-message {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.draft-tag {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-error-color);
  flex-shrink: 0;
}

.mute-icon {
  color: var(--dt-text-quaternary);
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  font-size: 13px;
  gap: 12px;
}
</style>
