<template>
  <div class="input-toolbar">
    <!-- 左侧工具组 -->
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

      <el-tooltip content="位置" placement="top">
        <button class="toolbar-btn" @click="$emit('send-location')">
          <span class="material-icons-outlined">location_on</span>
        </button>
      </el-tooltip>

      <el-tooltip content="定时发送" placement="top">
        <button class="toolbar-btn" @click="$emit('schedule-send')">
          <span class="material-icons-outlined">schedule</span>
        </button>
      </el-tooltip>

      <el-tooltip content="待办" placement="top">
        <button class="toolbar-btn" @click="$emit('create-todo')">
          <span class="material-icons-outlined">check_circle</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 右侧工具组 -->
    <div class="toolbar-right">
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
  'upload-video',
  'screenshot',
  'at-member',
  'smart-reply',
  'send-location',
  'schedule-send',
  'create-todo'
])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

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
    gap: 6px;
    flex-wrap: wrap;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .toolbar-btn {
    position: relative;
    width: 32px;
    height: 32px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--dt-transition-fast);

    .material-icons-outlined {
      font-size: 20px;
    }

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: var(--dt-brand-color);
      background: var(--dt-brand-bg);

      &::after {
        content: '';
        position: absolute;
        bottom: 2px;
        left: 50%;
        transform: translateX(-50%);
        width: 12px;
        height: 2px;
        background: var(--dt-brand-color);
        border-radius: 1px;
      }
    }

    // AI回复按钮
    &.ai-reply-btn {
      background: var(--dt-brand-bg);
      color: #7c3aed;

      .material-icons-outlined {
        font-size: 20px;
      }

      &:hover {
        background: var(--dt-brand-light);
      }
    }
  }
}

// 暗色模式
:global(.dark) {
  .input-toolbar {
    .toolbar-btn {
      color: var(--dt-text-tertiary);

      &:hover {
        background: var(--dt-bg-hover-dark);
        color: var(--dt-brand-color);
      }

      &.active {
        background: var(--dt-brand-light);
        color: var(--dt-brand-color);
      }

      &.ai-reply-btn {
        background: var(--dt-brand-light);
        color: #a78bfa;

        &:hover {
          background: rgba(124, 58, 237, 0.25);
        }
      }
    }
  }
}

// 响应式设计 - 小屏幕时调整按钮大小
@media (max-width: 768px) {
  .input-toolbar {
    .toolbar-btn {
      width: 30px;
      height: 30px;

      .material-icons-outlined {
        font-size: 18px;
      }
    }
  }
}
</style>
