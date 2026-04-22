<template>
  <div class="mail-v2">
    <!-- 1. 左侧文件夹侧栏 (180px) -->
    <aside class="mail-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="compose-btn" @click="showComposeDialog = true">
          <el-icon><Edit /></el-icon>
          <span>写邮件</span>
        </el-button>
      </div>
      <nav class="folder-list">
        <div 
          v-for="f in folders" 
          :key="f.key" 
          class="folder-item"
          :class="{ active: activeFolder === f.key }"
          @click="activeFolder = f.key"
        >
          <el-icon><component :is="f.iconEl" /></el-icon>
          <span class="label">{{ f.label }}</span>
          <span v-if="f.count > 0" class="badge">{{ f.count }}</span>
        </div>
      </nav>
    </aside>

    <!-- 2. 中间邮件摘要列表 (320px) -->
    <section class="mail-list-panel">
      <header class="list-header">
        <div class="search-box">
          <el-icon><Search /></el-icon>
          <input v-model="searchKeyword" placeholder="搜索邮件..." @keyup.enter="handleSearch" />
        </div>
      </header>
      <div class="list-body custom-scrollbar" v-loading="loading">
        <div v-for="mail in displayEmails" :key="mail.id" class="mail-item" :class="{ unread: !mail.read, active: selectedEmail?.id === mail.id }" @click="handleViewEmail(mail)">
          <div class="item-top">
            <span class="sender">{{ mail.sender }}</span>
            <span class="time">{{ mail.time }}</span>
          </div>
          <div class="item-subject">{{ mail.subject }}</div>
          <div class="item-preview">{{ mail.preview }}</div>
          <div v-if="!mail.read" class="unread-indicator"></div>
        </div>
        <div v-if="displayEmails.length === 0" class="empty-view">暂无邮件</div>
      </div>
    </section>

    <!-- 3. 右侧读信区 -->
    <main class="mail-reader">
      <div v-if="selectedEmail" class="reader-container">
        <header class="reader-header">
          <h1 class="mail-subject-full">{{ selectedEmail.subject }}</h1>
          <div class="mail-meta-full">
            <DingtalkAvatar :name="selectedEmail.sender" :size="32" />
            <div class="meta-info">
              <span class="sender-name">{{ selectedEmail.sender }}</span>
              <span class="time-full">{{ selectedEmail.time }}</span>
            </div>
            <div class="header-actions">
              <el-button-group>
                <el-button size="small" :icon="Back" @click="handleReply">回复</el-button>
                <el-button size="small" :icon="Right" @click="handleForward">转发</el-button>
                <el-button size="small" :icon="Delete" @click="handleDeleteEmail">删除</el-button>
              </el-button-group>
            </div>
          </div>
        </header>
        <article class="mail-body markdown-body" v-html="selectedEmail.content || selectedEmail.preview"></article>
      </div>
      <div v-else class="reader-empty">
        <el-icon class="empty-icon"><Message /></el-icon>
        <p>选择一封邮件开始阅读</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { Edit, Box, Promotion, Document, Star, Delete, Search, Message, Back, Right } from '@element-plus/icons-vue'
import { getMailList } from '@/api/im/mail'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const activeFolder = ref('inbox')
const searchKeyword = ref('')
const loading = ref(false)
const emails = ref([])
const selectedEmail = ref(null)

const folders = [
  { key: 'inbox', label: '收件箱', iconEl: Box, count: 5 },
  { key: 'sent', label: '已发送', iconEl: Promotion, count: 0 },
  { key: 'draft', label: '草稿箱', iconEl: Document, count: 2 },
  { key: 'trash', label: '已删除', iconEl: Delete, count: 0 }
]

const displayEmails = computed(() => emails.value)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMailList(activeFolder.value)
    if (res.code === 200) emails.value = res.data
  } finally { loading.value = false }
}

const handleViewEmail = (m) => { selectedEmail.value = m; m.read = true }

onMounted(loadData)
</script>

<style scoped lang="scss">
.mail-v2 { display: flex; height: 100%; background: var(--dt-bg-card); overflow: hidden; }

.mail-sidebar {
  width: 180px; background: var(--dt-bg-body); border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
  .sidebar-header { padding: 20px 16px; .compose-btn { width: 100%; height: 36px; font-weight: 600; } }
  .folder-list {
    flex: 1; padding: 0 8px;
    .folder-item {
      height: 38px; display: flex; align-items: center; gap: 10px; padding: 0 12px;
      border-radius: var(--dt-radius-md); cursor: pointer; color: var(--dt-text-secondary); font-size: 13px;
      &:hover { background: var(--dt-bg-hover); }
      &.active { background: var(--dt-brand-bg); color: var(--dt-brand-color); font-weight: 600; }
      .badge { margin-left: auto; font-size: 11px; opacity: 0.7; }
    }
  }
}

.mail-list-panel {
  width: 320px; border-right: 1px solid var(--dt-border-light); display: flex; flex-direction: column;
  .list-header { height: 56px; padding: 0 16px; border-bottom: 1px solid var(--dt-border-light); @include flex-center;
    .search-box { width: 100%; height: 32px; background: var(--dt-bg-hover); border-radius: 16px; display: flex; align-items: center; padding: 0 12px; 
      input { border: none; background: transparent; outline: none; font-size: 12px; margin-left: 8px; flex: 1; }
    }
  }
  .list-body { flex: 1; overflow-y: auto; }
}

.mail-item {
  padding: 16px; border-bottom: 1px solid var(--dt-border-lighter); cursor: pointer; position: relative; transition: var(--dt-transition-fast);
  &:hover { background: var(--dt-bg-hover); }
  &.active { background: var(--dt-brand-bg); }
  &.unread { .item-subject { font-weight: 700; color: var(--dt-text-primary); } }
  
  .item-top { display: flex; justify-content: space-between; font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 6px; }
  .item-subject { font-size: 14px; color: var(--dt-text-secondary); @include text-ellipsis; margin-bottom: 4px; }
  .item-preview { font-size: 12px; color: var(--dt-text-quaternary); @include text-ellipsis-multiline(2); }
  .unread-indicator { position: absolute; left: 6px; top: 22px; width: 6px; height: 6px; background: var(--dt-brand-color); border-radius: 50%; }
}

.mail-reader { flex: 1; background: var(--dt-bg-card); position: relative; }
.reader-container { height: 100%; display: flex; flex-direction: column;
  .reader-header { padding: 32px 40px; border-bottom: 1px solid var(--dt-border-light);
    .mail-subject-full { font-size: 22px; font-weight: 700; margin: 0 0 20px; color: var(--dt-text-primary); }
    .mail-meta-full { display: flex; align-items: center; gap: 12px;
      .meta-info { flex: 1; display: flex; flex-direction: column; .sender-name { font-weight: 600; font-size: 14px; } .time-full { font-size: 12px; color: var(--dt-text-tertiary); } }
    }
  }
  .mail-body { flex: 1; overflow-y: auto; padding: 40px; font-size: 15px; line-height: 1.8; color: var(--dt-text-primary); }
}

.reader-empty { height: 100%; @include flex-center; flex-direction: column; color: var(--dt-text-tertiary); opacity: 0.3; .empty-icon { font-size: 80px; margin-bottom: 20px; } }
</style>
