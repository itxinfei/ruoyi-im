<template>
  <div class="module-workspace">
    <!-- 模块头部 -->
    <div class="module-header">
      <h2>{{ moduleTitle }}</h2>
      <el-button :icon="Plus" type="primary" size="small">
        {{ moduleActionText }}
      </el-button>
    </div>

    <!-- 模块内容 -->
    <div class="module-content">
      <!-- 不同模块的占位内容 -->
      <div v-if="module === 'approval'" class="approval-content">
        <div class="approval-tabs">
          <div
            v-for="tab in approvalTabs"
            :key="tab.key"
            class="approval-tab"
            :class="{ active: activeApprovalTab === tab.key }"
            @click="activeApprovalTab = tab.key"
          >
            {{ tab.label }}
          </div>
        </div>
        <div class="approval-list">
          <div class="empty-state">
            <el-icon :size="48" color="#ddd">
              <Tickets />
            </el-icon>
            <p>暂无审批记录</p>
          </div>
        </div>
      </div>

      <div v-else-if="module === 'email'" class="email-content">
        <div class="email-sidebar">
          <div
            v-for="folder in emailFolders"
            :key="folder.key"
            class="email-folder"
            :class="{ active: activeEmailFolder === folder.key }"
            @click="activeEmailFolder = folder.key"
          >
            <el-icon><component :is="folder.icon" /></el-icon>
            <span>{{ folder.label }}</span>
            <span class="folder-count">{{ folder.count }}</span>
          </div>
        </div>
        <div class="email-main">
          <div class="empty-state">
            <el-icon :size="48" color="#ddd">
              <ChatLineSquare />
            </el-icon>
            <p>选择邮件查看内容</p>
          </div>
        </div>
      </div>

      <div v-else class="placeholder-content">
        <div class="empty-state">
          <el-icon :size="64" color="#ddd">
            <Box />
          </el-icon>
          <h3>{{ moduleTitle }}功能开发中...</h3>
          <p>敬请期待更多精彩功能</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Box, Tickets, ChatLineSquare, Document, InBox, Star, Message } from '@element-plus/icons-vue'

const props = defineProps({
  module: {
    type: String,
    default: ''
  },
  collapsed: {
    type: Boolean,
    default: false
  },
  navWidth: {
    type: Number,
    default: 60
  }
})

// 模块标题
const moduleTitle = computed(() => {
  const titleMap = {
    'approval': '审批中心',
    'email': '邮箱',
    'ding': '钉钉',
    'app-center': '应用中心',
    'document': '文档管理',
  }
  return titleMap[props.module] || '未知模块'
})

// 模块操作按钮文本
const moduleActionText = computed(() => {
  const actionTextMap = {
    'approval': '新建审批',
    'email': '写邮件',
    'ding': '发起DING',
    'app-center': '添加应用',
    'document': '新建文档',
  }
  return actionTextMap[props.module] || '新建'
})

// 审批标签页
const activeApprovalTab = ref('pending')
const approvalTabs = [
  { key: 'pending', label: '待我审批' },
  { key: 'processing', label: '我发起的' },
  { key: 'completed', label: '已完成' },
]

// 邮箱文件夹
const activeEmailFolder = ref('inbox')
const emailFolders = [
  { key: 'inbox', label: '收件箱', icon: InBox, count: 5 },
  { key: 'sent', label: '已发送', icon: Message, count: 2 },
  { key: 'starred', label: '星标邮件', icon: Star, count: 1 },
  { key: 'draft', label: '草稿箱', icon: Document, count: 0 },
]
</script>

<style scoped lang="scss">
.module-workspace {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;

  .module-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #e8e8e8;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #262626;
    }
  }

  .module-content {
    flex: 1;
    overflow: hidden;

    .approval-content {
      .approval-tabs {
        display: flex;
        border-bottom: 1px solid #e8e8e8;

        .approval-tab {
          padding: 12px 20px;
          cursor: pointer;
          font-size: 14px;
          color: #595959;
          border-bottom: 2px solid transparent;
          transition: all 0.2s ease;

          &:hover {
            color: #1677ff;
          }

          &.active {
            color: #1677ff;
            border-bottom-color: #1677ff;
            font-weight: 500;
          }
        }
      }

      .approval-list {
        height: calc(100% - 49px);
        padding: 20px;
      }
    }

    .email-content {
      display: flex;
      height: 100%;

      .email-sidebar {
        width: 200px;
        border-right: 1px solid #e8e8e8;
        padding: 16px 0;

        .email-folder {
          display: flex;
          align-items: center;
          padding: 10px 16px;
          cursor: pointer;
          font-size: 14px;
          color: #595959;
          transition: all 0.2s ease;

          &:hover {
            background: rgba(0, 0, 0, 0.04);
          }

          &.active {
            background: rgba(22, 119, 255, 0.1);
            color: #1677ff;
          }

          .el-icon {
            margin-right: 12px;
            font-size: 16px;
          }

          span {
            flex: 1;
          }

          .folder-count {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }

      .email-main {
        flex: 1;
        padding: 20px;
      }
    }

    .placeholder-content {
      height: 100%;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #b8b8b8;

    h3 {
      margin: 16px 0 8px 0;
      font-size: 16px;
      font-weight: 500;
      color: #595959;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }
}
</style>
