<template>
  <div class="session-panel">
    <!-- 头部搜索与筛选 -->
    <div class="panel-header">
      <div class="search-bar" :class="{ 'is-focused': isSearchFocused }">
        <i class="el-icon-search"></i>
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索" 
          @focus="isSearchFocused = true"
          @blur="isSearchFocused = false"
        />
      </div>
    </div>

    <!-- 会话列表区 -->
    <div class="session-list">
      <div 
        v-for="session in filteredSessions" 
        :key="session.id"
        class="session-item"
        :class="{ 'is-active': currentSession && currentSession.id === session.id }"
        @click="selectSession(session)"
      >
        <!-- 头像区 -->
        <div class="avatar-wrapper">
          <img :src="session.avatar || '/avatars/default.png'" class="avatar" alt="avatar" />
          <div v-if="session.unreadCount > 0" class="unread-badge">
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
            <i v-if="session.isMuted" class="el-icon-mute"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatSessionList.vue (Vuex 路径修复版)
 */
import { ref, computed } from 'vue';
import { useStore } from 'vuex';

const store = useStore();

// 内部状态
const isSearchFocused = ref(false);
const searchKeyword = ref('');

// 修正：对齐 im/session 嵌套路径
const currentSession = computed(() => store.state.im?.session?.currentSession);
const sessions = computed(() => store.getters['im/session/sortedSessions']);

const filteredSessions = computed(() => {
  const list = sessions.value || [];
  if (!searchKeyword.value) return list;
  return list.filter(s => s.name?.includes(searchKeyword.value));
});

const selectSession = (session) => {
  store.dispatch('im/session/selectSession', session);
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.getHours().toString().padStart(2, '0') + ':' + 
         date.getMinutes().toString().padStart(2, '0');
};
</script>

<style scoped>
.session-panel {
  width: 240px; height: 100%; display: flex; flex-direction: column;
  background-color: var(--dt-bg-body); border-right: 1px solid var(--dt-border-color);
}
.panel-header { padding: 16px 12px; }
.search-bar {
  display: flex; align-items: center; height: 32px; background-color: var(--dt-bg-hover);
  border-radius: 4px; padding: 0 8px; border: 1px solid transparent; transition: all 0.2s;
}
.search-bar.is-focused { background-color: #fff; border-color: var(--dt-brand-color); }
.search-bar input { border: none; background: transparent; outline: none; margin-left: 6px; width: 100%; font-size: 12px; }
.session-list { flex: 1; overflow-y: auto; }
.session-item {
  display: flex; align-items: center; height: 64px; padding: 0 16px;
  cursor: pointer; position: relative; transition: background-color 0.1s;
}
.session-item:hover { background-color: var(--dt-bg-hover); }
.session-item.is-active { background-color: var(--dt-bg-selected); }
.session-item.is-active::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px; background-color: var(--dt-brand-color);
}
.avatar-wrapper { position: relative; width: 36px; height: 36px; margin-right: 12px; flex-shrink: 0; }
.avatar { width: 100%; height: 100%; border-radius: 4px; object-fit: cover; }
.unread-badge {
  position: absolute; top: -6px; right: -6px; background-color: #FF4D4F; color: #fff;
  font-size: 10px; line-height: 16px; min-width: 16px; padding: 0 4px; border-radius: 8px;
  text-align: center; font-weight: 600; box-shadow: 0 0 0 1px #fff;
}
.content-wrapper { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.content-top, .content-bottom { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.session-name { font-size: 14px; color: var(--dt-text-main); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-time { font-size: 12px; color: var(--dt-text-desc); margin-left: 8px; }
.content-bottom { font-size: 12px; color: var(--dt-text-desc); margin-top: 4px; }
.last-message { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.draft-tag { color: #FF4D4F; margin-right: 4px; flex-shrink: 0; }
</style>
