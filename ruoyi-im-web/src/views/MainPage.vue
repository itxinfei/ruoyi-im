<template>
  <div class="dingtalk-app">
    <!-- 1. 左侧一级导航 (固定 68px) -->
    <ImSideNavNew
      :active-module="activeModule"
      @switch-module="processSwitchModule"
    />

    <!-- 2. 动态内容区 -->
    <main class="content-container">
      <!-- 消息模块专用布局 -->
      <template v-if="activeModule === 'chat'">
        <div class="chat-module-wrapper">
          <ChatSessionList />
          <div class="chat-main-view">
            <ChatWindow v-if="currentSession" />
            <div v-else class="empty-view">
              <div class="empty-icon">💬</div>
              <p>开启高效办公的一天</p>
            </div>
          </div>
        </div>
      </template>

      <!-- 协同办公/管理单面板布局 (防御式渲染) -->
      <template v-else>
        <div class="single-panel-wrapper">
          <TodoPanel v-if="activeModule === 'todo'" />
          <div v-else class="placeholder-view">
            <div class="empty-icon">📦</div>
            <p>{{ getModuleName(activeModule) }} 模块正在接入中...</p>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup lang="js">
/**
 * MainPage.vue (Vuex 路径修复版)
 * 严格对齐嵌套 Store 路径: im.session
 */
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import ImSideNavNew from '@/components/ImSideNavNew/index.vue';
import ChatSessionList from '@/components/im/ChatSessionList.vue';
import ChatWindow from '@/components/im/ChatWindow.vue';
import TodoPanel from './im/TodoPanel.vue';

const store = useStore();
const activeModule = ref('chat');

// 修正：嵌套访问路径 im -> session (增加可选链防护)
const currentSession = computed(() => store.state.im?.session?.currentSession);

const processSwitchModule = (moduleId) => {
  activeModule.value = moduleId;
};

const getModuleName = (id) => {
  const map = {
    'contacts': '通讯录',
    'workbench': '工作台',
    'drive': '云盘',
    'calendar': '日历',
    'approval': '审批',
    'admin': '管理后台'
  };
  return map[id] || id;
};
</script>
<style scoped>
/* 钉钉 Windows 7.0+ 官方视觉契约 - 强对比/高清晰度版 */
.dingtalk-app {
  /* 1. 品牌色 - 钉钉标准蓝 */
  --dt-brand-color: #007FFF;
  --dt-brand-hover: #3399FF;
  --dt-brand-bg: #E6F4FF;

  /* 2. 侧边栏专属 (深色区必须高对比) */
  --dt-bg-sidebar: #2B2D30;        /* 侧边栏背景 */
  --dt-sb-icon: #ADB1B8;           /* 未选中图标：亮灰 */
  --dt-sb-icon-active: #FFFFFF;    /* 选中图标：纯白 */
  --dt-sb-item-hover: rgba(255, 255, 255, 0.1);

  /* 3. 列表与内容区 (浅色区) */
  --dt-bg-list: #F7F7F7;           /* 二级列表背景 */
  --dt-bg-chat: #FFFFFF;           /* 聊天区背景：纯白 */
  --dt-bg-hover: #E8EAEF;          /* 列表 Hover 态：明显灰 */
  --dt-bg-selected: #E2E5EB;       /* 列表选中态 */

  /* 4. 文字分层 (确保 100% 可见) */
  --dt-text-main: #1F1F1F;         /* 重要文字：接近纯黑 */
  --dt-text-desc: #858E99;         /* 辅助文字：标准灰 */
  --dt-text-white: #FFFFFF;        /* 深色背景上的文字 */

  /* 5. 边框与线条 */
  --dt-border-color: #E5E7EB;      /* 分割线 */
  --dt-line-width: 1px;

  /* 6. 圆角 */
  --dt-radius-s: 4px;
  --dt-radius-m: 8px;

  width: 100%; height: 100vh; overflow: hidden; display: flex;
}


.content-container { flex: 1; height: 100%; min-width: 0; overflow: hidden; }
.chat-module-wrapper { display: flex; width: 100%; height: 100%; }
.chat-main-view { flex: 1; height: 100%; min-width: 0; background-color: var(--dt-bg-chat); }
.empty-view, .placeholder-view { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--dt-text-desc); }
.empty-icon { font-size: 64px; margin-bottom: 24px; opacity: 0.5; }
.single-panel-wrapper { width: 100%; height: 100%; }
</style>
