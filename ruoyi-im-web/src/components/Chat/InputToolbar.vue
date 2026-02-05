<template>
  <div class="input-toolbar">
    <!-- 左侧工具组：媒体操作 -->
    <div class="toolbar-left">
      <el-tooltip content="表情" placement="top">
        <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="$emit('toggle-emoji')">
          <span class="material-icons-outlined">sentiment_satisfied_alt</span>
        </button>
      </el-tooltip>

      <el-tooltip content="图片" placement="top">
        <button class="toolbar-btn" @click="$emit('upload-image')">
          <span class="material-icons-outlined">image</span>
        </button>
      </el-tooltip>

      <el-tooltip content="文件" placement="top">
        <button class="toolbar-btn" @click="$emit('upload-file')">
          <span class="material-icons-outlined">insert_drive_file</span>
        </button>
      </el-tooltip>

      <el-tooltip content="截图" placement="top">
        <button class="toolbar-btn" @click="$emit('screenshot')">
          <span class="material-icons-outlined">screenshot</span>
        </button>
      </el-tooltip>

      <el-tooltip content="@成员" placement="top" v-if="showAtButton">
        <button class="toolbar-btn" @click="$emit('at-member')">
          <span class="material-icons-outlined">alternate_email</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 右侧工具组：通话+AI -->
    <div class="toolbar-right">
      <!-- 通话按钮组 -->
      <div class="call-buttons">
        <el-tooltip content="语音通话" placement="top">
          <button class="toolbar-btn call-btn voice-call" @click="$emit('voice-call')">
            <span class="material-icons-outlined">phone</span>
          </button>
        </el-tooltip>

        <el-tooltip content="视频通话" placement="top">
          <button class="toolbar-btn call-btn video-call" @click="$emit('video-call')">
            <span class="material-icons-outlined">videocam</span>
          </button>
        </el-tooltip>
      </div>

      <el-tooltip content="AI 灵动回复" placement="top">
        <button class="toolbar-btn ai-reply-btn" @click="$emit('smart-reply')">
          <span class="material-icons-outlined">auto_awesome</span>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
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
  'screenshot',
  'at-member',
  'smart-reply',
  'voice-call',
  'video-call'
])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  min-height: 50px;
  padding-bottom: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .toolbar-btn {
    position: relative;
    width: 32px;
    height: 32px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    color: #000b;
    border-radius: var(--dt-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--dt-transition-fast);

    .material-icons-outlined {
      font-size: 22px;
    }

    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: #3f64e4;
    }

    &:active {
      transform: scale(0.95);
    }

    &:focus-visible {
      outline: 2px solid var(--dt-brand-color);
      outline-offset: 2px;
    }

    &.active {
      color: var(--dt-brand-color);
      background: rgba(63, 100, 228, 0.1);
    }

    // 通话按钮特殊样式
    &.call-btn {
      width: 36px;
      height: 36px;
      border-radius: var(--dt-radius-md);

      &.voice-call {
        color: #22c55e;

        &:hover {
          background: rgba(34, 197, 94, 0.1);
        }
      }

      &.video-call {
        color: #3b82f6;

        &:hover {
          background: rgba(59, 130, 246, 0.1);
        }
      }
    }

    // AI回复按钮
    &.ai-reply-btn {
      color: #7c3aed;

      &:hover {
        background: rgba(124, 58, 237, 0.1);
      }
    }
  }

  .call-buttons {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

// 暗色模式
:global(.dark) {
  .input-toolbar {
    .toolbar-btn {
      color: var(--dt-text-secondary-dark);

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: var(--dt-brand-color);
      }

      &.active {
        background: rgba(0, 137, 255, 0.15);
        color: var(--dt-brand-color);
      }

      &.call-btn {
        &.voice-call {
          color: #4ade80;

          &:hover {
            background: rgba(34, 197, 94, 0.15);
          }
        }

        &.video-call {
          color: #60a5fa;

          &:hover {
            background: rgba(59, 130, 246, 0.15);
          }
        }
      }

      &.ai-reply-btn {
        color: #a78bfa;

        &:hover {
          background: rgba(124, 58, 237, 0.15);
        }
      }
    }
  }
}

// 响应式设计 - 小屏幕时调整按钮大小
@media (max-width: 768px) {
  .input-toolbar {
    .toolbar-left {
      gap: 12px;
    }

    .toolbar-right {
      gap: 8px;
    }

    .toolbar-btn {
      width: 30px;
      height: 30px;

      .material-icons-outlined {
        font-size: 20px;
      }

      &.call-btn {
        width: 34px;
        height: 34px;
      }
    }
  }
}
</style>
