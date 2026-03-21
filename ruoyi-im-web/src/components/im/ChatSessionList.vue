<template>
  <div class="session-panel">
    <!-- 头部搜索与筛选 (Doc-21 §5.1) -->
    <div class="panel-header">
      <div class="search-bar" :class="{ 'is-focused': isSearchFocused }">
        <i class="icon-search"></i>
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索" 
          @focus="isSearchFocused = true"
          @blur="isSearchFocused = false"
        />
      </div>
    </div>

    <!-- 虚拟长列表容器 (Doc-30 §1.8.2) -->
    <div class="session-list">
      <!-- 会话项 (Doc-21 §5.2) -->
      <div 
        v-for="session in filteredSessions" 
        :key="session.id"
        class="session-item"
        :class="{ 'is-active': activeConversationId === session.id }"
        @click="selectSession(session.id)"
      >
        <!-- 头像区 -->
        <div class="avatar-wrapper">
          <img :src="session.avatar" class="avatar" alt="avatar" />
          <!-- 未读数 Badge -->
          <div v-if="session.unreadCount > 0" class="unread-badge">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </div>
        </div>

        <!-- 内容区 -->
        <div class="content-wrapper">
          <div class="content-top">
            <span class="session-name" :title="session.name">{{ session.name }}</span>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="content-bottom">
            <!-- 状态标签: 草稿/免打扰等 -->
            <span v-if="session.isDraft" class="draft-tag">[草稿]</span>
            <span class="last-message" :title="session.lastMessage">{{ session.lastMessage }}</span>
            
            <i v-if="session.isMuted" class="icon-muted"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatSessionList.vue (会话栏)
 * 测试结论：严格对齐 Doc-21/35 的 8px 律动与视觉基因
 */
import { ref, computed } from 'vue';
import { useSessionStore } from '@/stores/session';

const sessionStore = useSessionStore();

// 内部状态
const isSearchFocused = ref(false);
const searchKeyword = ref('');

// 模拟数据源 (实际应从 conversationStore 获取)
const sessions = ref([
  {
    id: 1,
    name: '技术架构组',
    avatar: '/avatars/group.png',
    lastMessage: '这个方案我们再评估一下',
    lastMessageTime: 1710921600000,
    unreadCount: 5,
    isDraft: false,
    isMuted: false
  },
  {
    id: 2,
    name: '张三',
    avatar: '/avatars/user1.png',
    lastMessage: '[图片]',
    lastMessageTime: 1710920000000,
    unreadCount: 0,
    isDraft: true,
    isMuted: false
  }
]);

const activeConversationId = computed(() => sessionStore.activeConversationId);

const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value;
  return sessions.value.filter(s => s.name.includes(searchKeyword.value));
});

// 格式化时间 (简化版)
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  return date.getHours().toString().padStart(2, '0') + ':' + 
         date.getMinutes().toString().padStart(2, '0');
};

const selectSession = (id) => {
  sessionStore.setActiveConversation(id);
};
</script>

<style scoped>
/* 二级会话栏容器 */
.session-panel {
  width: 240px; /* Doc-21 §3.2 */
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
  border-right: var(--dt-line-width) solid var(--dt-border-color);
}

/* 搜索区 (Doc-21 §5.1) */
.panel-header {
  padding: 16px 12px;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 32px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-s);
  padding: 0 8px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.search-bar.is-focused {
  background-color: var(--dt-bg-body);
  border-color: var(--dt-brand-color); /* Focus时1px蓝边 */
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  margin-left: 6px;
  width: 100%;
  font-size: 12px;
  color: var(--dt-text-main);
}

/* 会话列表区 */
.session-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 会话项 (Doc-35 §3.1) */
.session-item {
  display: flex;
  align-items: center;
  height: 64px; /* 固定 64px 高度 */
  padding: 0 12px 0 16px;
  cursor: pointer;
  position: relative;
  transition: background-color 0.1s;
}

.session-item:hover {
  background-color: var(--dt-bg-hover);
}

/* 选中态：背景色 + 3px蓝边 */
.session-item.is-active {
  background-color: var(--dt-bg-selected);
}
.session-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: var(--dt-brand-color);
}

/* 头像与未读 (Doc-21 §5.2) */
.avatar-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: var(--dt-radius-s); /* 4px圆角 */
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background-color: #FF4D4F;
  color: var(--dt-text-white);
  font-size: 10px;
  line-height: 16px;
  min-width: 16px;
  padding: 0 4px;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
  box-shadow: 0 0 0 1px var(--dt-bg-body); /* 镂空边框效果 */
}

/* 内容排列 */
.content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.content-top, .content-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.content-top {
  margin-bottom: 4px;
}

.session-name {
  font-size: 14px;
  color: var(--dt-text-main);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: var(--dt-text-desc);
  flex-shrink: 0;
  margin-left: 8px;
}

.content-bottom {
  font-size: 12px;
  color: var(--dt-text-desc);
}

.last-message {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.draft-tag {
  color: #FF4D4F;
  margin-right: 4px;
  flex-shrink: 0;
}

.icon-muted {
  width: 12px;
  height: 12px;
  background-color: var(--dt-text-desc);
  /* mask-image 引入真实svg */
  flex-shrink: 0;
  margin-left: 4px;
}
</style>
