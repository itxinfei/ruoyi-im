<template>
  <div class="input-toolbar">
    <div class="toolbar-left">
      <!-- 媒体发送组 -->
      <div class="toolbar-group">
        <el-tooltip content="表情" placement="top">
          <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="$emit('toggle-emoji')">
            <el-icon><ChatDotRound /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="图片" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-image')">
            <el-icon><Picture /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="文件" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-file')">
            <el-icon><FolderOpened /></el-icon>
          </button>
        </el-tooltip>

        <el-tooltip content="视频" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-video')">
            <span class="material-icons-outlined">videocam</span>
          </button>
        </el-tooltip>

        <el-tooltip content="截图 (Ctrl+Alt+A)" placement="top">
          <button class="toolbar-btn" @click="$emit('screenshot')">
            <span class="material-icons-outlined">content_cut</span>
          </button>
        </el-tooltip>

        <el-tooltip v-if="showAtButton" content="@成员" placement="top">
          <button class="toolbar-btn" @click="$emit('at-member')">
            <span class="material-icons-outlined">alternate_email</span>
          </button>
        </el-tooltip>
      </div>
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
import { ChatDotRound, Picture, FolderOpened } from '@element-plus/icons-vue'

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
  'smart-reply'
])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 4px;

    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 4px;
      background: var(--dt-bg-input);
      border-radius: var(--dt-radius-lg);
      transition: all var(--dt-transition-fast);
    }
  }

  .toolbar-btn {
    position: relative;
    width: 28px;
    height: 28px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    .el-icon,
    .material-icons-outlined {
      font-size: 16px;
      transition: transform 0.2s var(--dt-ease-out);
    }

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
      transform: translateY(-1px);

      .el-icon,
      .material-icons-outlined {
        transform: scale(1.1);
      }
    }

    &:active {
      transform: translateY(0) scale(0.95);
    }

    &.active {
      color: var(--dt-brand-color);
      background: var(--dt-brand-bg);
      box-shadow: inset 0 1px 2px rgba(22, 119, 255, 0.2);

      &::before {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 16px;
        height: 2px;
        background: var(--dt-brand-color);
        border-radius: 1px;
      }
    }

    &.ai-reply-btn {
      background: linear-gradient(135deg, rgba(147, 51, 234, 0.1) 0%, rgba(147, 51, 234, 0.15) 100%);
      color: #9333ea;

      .material-icons-outlined {
        background: linear-gradient(135deg, #9333ea, #7c3aed);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      &:hover {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.2) 0%, rgba(147, 51, 234, 0.25) 100%);
        transform: translateY(-2px) scale(1.05);
        box-shadow: 0 4px 12px rgba(147, 51, 234, 0.25);
      }

      animation: aiPulse 3s ease-in-out infinite;
    }
  }
}

@keyframes aiPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(147, 51, 234, 0); }
  50% { box-shadow: 0 0 0 4px rgba(147, 51, 234, 0.2); }
}

// 暗色模式
:global(.dark) {
  .input-toolbar .toolbar-left .toolbar-group {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--dt-border-dark);
  }

  .input-toolbar .toolbar-btn {
    color: var(--dt-text-secondary-dark);

    &:hover {
      background: rgba(22, 119, 255, 0.15);
      color: var(--dt-brand-color);
    }

    &.active {
      background: rgba(22, 119, 255, 0.15);
      color: var(--dt-brand-color);
    }

    &.ai-reply-btn {
      background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(147, 51, 234, 0.1) 100%);
      color: #c084fc;

      .material-icons-outlined {
        background: linear-gradient(135deg, #c084fc, #a78bfa);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      &:hover {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.25) 0%, rgba(147, 51, 234, 0.2) 100%);
        color: #d8b4fe;
        box-shadow: 0 4px 12px rgba(147, 51, 234, 0.3);
      }
    }
  }
}
</style>
