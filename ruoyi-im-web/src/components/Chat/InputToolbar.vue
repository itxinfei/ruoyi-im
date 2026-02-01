<template>
  <div class="input-toolbar">
    <div class="toolbar-left">
      <!-- 核心工具组 - 常用功能 -->
      <div class="toolbar-group">
        <el-tooltip content="表情" placement="top">
          <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="$emit('toggle-emoji')">
            <span class="material-icons-outlined">sentiment_satisfied_alt</span>
          </button>
        </el-tooltip>

        <el-tooltip content="附件" placement="top">
          <button class="toolbar-btn attachment-btn" @click="toggleAttachmentMenu">
            <span class="material-icons-outlined">add_circle</span>
          </button>
        </el-tooltip>

        <!-- 附件下拉菜单 -->
        <div v-if="showAttachmentMenu" class="attachment-menu">
          <div class="attachment-item" @click="$emit('upload-image')">
            <span class="material-icons-outlined">image</span>
            <span>图片</span>
          </div>
          <div class="attachment-item" @click="$emit('upload-file')">
            <span class="material-icons-outlined">insert_drive_file</span>
            <span>文件</span>
          </div>
          <div class="attachment-item" @click="$emit('screenshot')">
            <span class="material-icons-outlined">screenshot</span>
            <span>截图</span>
          </div>
          <div class="attachment-divider"></div>
          <div class="attachment-item" @click="$emit('send-location')">
            <span class="material-icons-outlined">location_on</span>
            <span>位置</span>
          </div>
          <div class="attachment-item" @click="$emit('schedule-send')">
            <span class="material-icons-outlined">schedule</span>
            <span>定时发送</span>
          </div>
          <div class="attachment-item" @click="$emit('create-todo')">
            <span class="material-icons-outlined">check_circle</span>
            <span>待办</span>
          </div>
        </div>
      </div>

      <!-- @成员按钮（仅群聊显示） -->
      <el-tooltip content="@成员" placement="top" v-if="showAtButton">
        <button class="toolbar-btn" @click="$emit('at-member')">
          <span class="material-icons-outlined">alternate_email</span>
        </button>
      </el-tooltip>
    </div>

    <div class="toolbar-right">
      <!-- AI 灵动回复 -->
      <el-tooltip content="AI 灵动回复" placement="top">
        <button class="toolbar-btn ai-reply-btn" @click="$emit('smart-reply')">
          <span class="material-icons-outlined">auto_awesome</span>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  showEmojiPicker: {
    type: Boolean,
    default: false
  },
  showAtButton: {
    type: Boolean,
    default: false
  }
})

defineEmits([
  'toggle-emoji',
  'upload-image',
  'upload-file',
  'upload-video',
  'screenshot',
  'at-member',
  'smart-reply',
  'send-location',
  'schedule-send',
  'create-todo'
])

// 附件菜单状态
const showAttachmentMenu = ref(false)

const toggleAttachmentMenu = () => {
  showAttachmentMenu.value = !showAttachmentMenu.value
}

const closeAttachmentMenu = () => {
  showAttachmentMenu.value = false
}

// 点击外部关闭菜单
const handleClickOutside = (e) => {
  if (!e.target.closest('.attachment-btn') && !e.target.closest('.attachment-menu')) {
    closeAttachmentMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 40px;
  padding-bottom: 0;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 4px;
      position: relative;
    }
  }

  // 分隔线
  .toolbar-divider {
    width: 1px;
    height: 20px;
    background: #e8e8e8;
  }

  .toolbar-btn {
    position: relative;
    width: 32px;
    height: 32px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    color: #666;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease-out;

    .material-icons-outlined {
      font-size: 20px;
    }

    &:hover {
      background: #f5f5f5;
      color: #1890ff;
    }

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: #1890ff;
      background: #e6f7ff;

      &::after {
        content: '';
        position: absolute;
        bottom: 2px;
        left: 50%;
        transform: translateX(-50%);
        width: 12px;
        height: 2px;
        background: #1890ff;
        border-radius: 1px;
      }
    }

    // 附件按钮特殊样式
    &.attachment-btn {
      color: #1890ff;

      .material-icons-outlined {
        font-size: 24px;
      }

      &:hover {
        background: #e6f7ff;
      }
    }

    // AI回复按钮
    &.ai-reply-btn {
      background: #f5f5ff;
      color: #7c3aed;

      .material-icons-outlined {
        font-size: 20px;
      }

      &:hover {
        background: #ede9fe;
      }
    }
  }

  // 附件菜单
  .attachment-menu {
    position: absolute;
    bottom: 100%;
    left: 0;
    margin-bottom: 8px;
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    padding: 8px 0;
    min-width: 160px;
    z-index: 100;
    animation: fadeIn 0.15s ease-out;

    .attachment-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      cursor: pointer;
      transition: background 0.1s ease;

      .material-icons-outlined {
        font-size: 18px;
        color: #666;
      }

      span:last-child {
        font-size: 14px;
        color: #333;
      }

      &:hover {
        background: #f5f5f5;

        .material-icons-outlined {
          color: #1890ff;
        }
      }
    }

    .attachment-divider {
      height: 1px;
      background: #e8e8e8;
      margin: 4px 0;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 暗色模式
:global(.dark) {
  .input-toolbar {
    .toolbar-divider {
      background: rgba(255, 255, 255, 0.1);
    }

    .toolbar-btn {
      color: #999;

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #1890ff;
      }

      &.active {
        background: rgba(24, 144, 255, 0.15);
        color: #1890ff;
      }

      &.attachment-btn {
        color: #1890ff;
        background: rgba(24, 144, 255, 0.1);

        &:hover {
          background: rgba(24, 144, 255, 0.2);
        }
      }

      &.ai-reply-btn {
        background: rgba(124, 58, 237, 0.15);
        color: #a78bfa;

        &:hover {
          background: rgba(124, 58, 237, 0.25);
        }
      }
    }

    .attachment-menu {
      background: #2a2a2a;
      border-color: rgba(255, 255, 255, 0.1);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

      .attachment-item {
        span:last-child {
          color: #e8e8e8;
        }

        &:hover {
          background: rgba(255, 255, 255, 0.08);

          .material-icons-outlined {
            color: #1890ff;
          }
        }
      }

      .attachment-divider {
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }
}
</style>
