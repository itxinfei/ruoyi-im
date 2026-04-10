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
      <!-- 分类分组列表 -->
      <template v-for="group in groupedSessions" :key="group.type">
        <!-- 分类头部（可折叠） -->
        <div
          v-if="group.sessions.length > 0 || group.type === 'PINNED'"
          class="session-group"
          :class="{ 'is-collapsed': collapsedGroups[group.type] }"
        >
          <div class="group-header" @click="toggleGroup(group.type)">
            <el-icon class="group-arrow">
              <ArrowDown v-if="!collapsedGroups[group.type]" />
              <ArrowRight v-else />
            </el-icon>
            <span class="group-name">{{ group.label }}</span>
            <span v-if="group.unreadCount > 0" class="group-unread-badge">
              {{ group.unreadCount > 99 ? '99+' : group.unreadCount }}
            </span>
          </div>

          <!-- 分组内的会话项 -->
          <div v-show="!collapsedGroups[group.type]" class="group-sessions">
            <div
              v-for="session in group.sessions"
              :key="session.id"
              class="session-item"
              :class="{ 'is-active': currentSession && currentSession.id === session.id, 'is-pinned': session.isPinned }"
              @click="selectSession(session)"
            >
              <!-- 头像区 (对齐钉钉 4px 圆角) -->
              <div class="avatar-wrapper" @click.stop="handleAvatarClick(session)">
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
          </div>
        </div>
      </template>

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
import { ref, computed, onMounted, reactive } from 'vue';
import { useStore } from 'vuex';
import { Search, BellFilled, ChatLineRound, ArrowDown, ArrowRight } from '@element-plus/icons-vue';

const store = useStore();

// 内部状态
const isSearchFocused = ref(false);
const searchKeyword = ref('');
const filterType = ref('all');

// 分类折叠状态
const collapsedGroups = reactive({
  PINNED: false,
  PRIVATE: false,
  GROUP: false
});

// 分类定义
const sessionGroups = [
  { type: 'PINNED', label: '置顶' },
  { type: 'PRIVATE', label: '单聊' },
  { type: 'GROUP', label: '群聊' }
];

// 切换分组折叠状态
const toggleGroup = (type) => {
  collapsedGroups[type] = !collapsedGroups[type];
};

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

// 分组会话列表（钉钉分类设计：置顶 > 单聊 > 群聊）
const groupedSessions = computed(() => {
  const list = filteredSessions.value;

  return sessionGroups.map(group => {
    let groupSessions;

    if (filterType.value !== 'all') {
      // 单一类型过滤时，只显示该类型的非置顶会话
      groupSessions = list.filter(s => !s.isPinned && s.type === filterType.value);
    } else {
      // 全部类型时，按分组显示
      if (group.type === 'PINNED') {
        groupSessions = list.filter(s => s.isPinned);
      } else {
        groupSessions = list.filter(s => !s.isPinned && s.type === group.type);
      }
    }

    const unreadCount = groupSessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0);

    return {
      type: group.type,
      label: group.label,
      sessions: groupSessions,
      unreadCount
    };
  });
});

// 初始化加载
onMounted(() => {
  store.dispatch('im/session/loadSessions');
});

const selectSession = (session) => {
  store.dispatch('im/session/selectSession', session);
};

// 头像点击事件
const emit = defineEmits(['avatar-click', 'mention-user']);

const handleAvatarClick = (session) => {
  if (session.type === 'PRIVATE') {
    // 单聊：打开用户详情
    emit('avatar-click', session);
  } else {
    // 群聊：@该成员或显示成员信息
    emit('mention-user', session);
  }
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
  gap: 4px;
  margin-top: 10px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 4px;
}

.session-tab {
  flex: 1;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: var(--dt-font-size-sm);  /* 13px对齐到标准12px */
  font-weight: 500;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: color var(--dt-transition-fast), background-color var(--dt-transition-fast);
}

.session-tab:hover {
  color: var(--dt-text-primary);
  background: var(--dt-bg-card);
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
  height: 34px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 0 12px;
  border: 1.5px solid transparent;
  transition: background-color var(--dt-transition-fast);
}

.search-bar:hover {
  background-color: var(--dt-bg-card);
}

.search-bar.is-focused {
  background-color: var(--dt-bg-card);
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px var(--dt-brand-bg);
}

.search-icon {
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-lg);
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  margin-left: 10px;
  width: 100%;
  font-size: var(--dt-font-size-base);
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
.session-list::-webkit-scrollbar-thumb { background: var(--dt-scrollbar-thumb-bg); border-radius: var(--dt-radius-sm); }
.session-list::-webkit-scrollbar-thumb:hover { background: var(--dt-scrollbar-thumb-bg-hover); }
.session-list::-webkit-scrollbar-track { background: transparent; }

/* 分类分组样式 */
.session-group {
  margin-bottom: 4px;
}

.group-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
  transition: background-color var(--dt-transition-fast);
}

.group-header:hover {
  background-color: var(--dt-bg-session-hover);
}

.group-arrow {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-right: 8px;
  transition: transform var(--dt-transition-fast);
}

.session-group.is-collapsed .group-arrow {
  transform: rotate(0deg);
}

.group-name {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-text-secondary);
  flex: 1;
}

.group-unread-badge {
  background-color: var(--dt-error-color);
  color: var(--dt-text-white);
  font-size: 10px;
  font-weight: 600;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-sessions {
  /* 展开状态 */
}

.session-item {
  display: flex;
  align-items: center;
  height: var(--dt-session-item-height);  /* 钉钉标准 56px */
  padding: 0 var(--dt-spacing-lg);
  cursor: pointer;
  position: relative;
  transition: background-color var(--dt-transition-fast);
}

.session-item:hover {
  background-color: var(--dt-bg-session-hover);
}

.session-item.is-active {
  background-color: var(--dt-bg-session-active);
}

/* 钉钉 3px 激活蓝条 */
.session-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
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
  width: var(--dt-avatar-session);  /* 钉钉标准 40px */
  height: var(--dt-avatar-session);  /* 钉钉标准 40px */
  margin-right: var(--dt-spacing-md);
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: var(--dt-radius-sm); /* 钉钉规范：4px 圆角 */
  object-fit: cover;
  border: 1px solid var(--dt-border-lighter);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: var(--dt-unread-color);
  color: var(--dt-text-white);
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-semibold);
  line-height: 16px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.25);
}

/* 钉钉规范：1-9 数字圆形，10+ 数字胶囊 */
.unread-badge-circle {
  border-radius: var(--dt-radius-full); /* 50% 当 width=height=16px 时为正圆 */
}

.unread-badge-capsule {
  border-radius: var(--dt-radius-lg);
}

.content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 4px 0;
}

.content-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-size: var(--dt-font-size-md);
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin-left: 8px;
  flex-shrink: 0;
}

.content-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
}

.last-message {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  line-height: 1.4;
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
  font-size: var(--dt-font-size-sm);
  gap: 12px;
}

.empty-list .el-icon {
  opacity: 0.4;
}

.empty-list p {
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-base);
  margin-top: 4px;
}
</style>
