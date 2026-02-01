<template>
  <div class="input-toolbar">
    <div class="toolbar-left">
      <!-- 基础工具组 -->
      <div class="toolbar-group">
        <el-tooltip content="表情" placement="top">
          <button class="toolbar-btn" :class="{ active: showEmojiPicker }" @click.stop="$emit('toggle-emoji')">
            <span class="material-icons-outlined">sentiment_satisfied_alt</span>
          </button>
        </el-tooltip>

        <el-tooltip content="@成员" placement="top" v-if="showAtButton">
          <button class="toolbar-btn" @click="$emit('at-member')">
            <span class="material-icons-outlined">alternate_email</span>
          </button>
        </el-tooltip>
      </div>

      <!-- 分隔线 -->
      <div class="toolbar-divider" v-if="showAtButton"></div>

      <!-- 媒体发送组 -->
      <div class="toolbar-group">
        <el-tooltip content="图片" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-image')">
            <span class="material-icons-outlined">image</span>
          </button>
        </el-tooltip>

        <el-tooltip content="视频" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-video')">
            <span class="material-icons-outlined">videocam</span>
          </button>
        </el-tooltip>

        <el-tooltip content="文件" placement="top">
          <button class="toolbar-btn" @click="$emit('upload-file')">
            <span class="material-icons-outlined">attach_file</span>
          </button>
        </el-tooltip>
      </div>

      <!-- 分隔线 -->
      <div class="toolbar-divider"></div>

      <!-- 更多工具组 -->
      <div class="toolbar-group">
        <el-tooltip content="截图" placement="top">
          <button class="toolbar-btn" @click="$emit('screenshot')">
            <span class="material-icons-outlined">screenshot</span>
          </button>
        </el-tooltip>

        <el-tooltip content="位置" placement="top">
          <button class="toolbar-btn" @click="$emit('send-location')">
            <span class="material-icons-outlined">location_on</span>
          </button>
        </el-tooltip>

        <el-tooltip content="定时发送" placement="top">
          <button class="toolbar-btn schedule-btn" @click="$emit('schedule-send')">
            <span class="material-icons-outlined">schedule_send</span>
          </button>
        </el-tooltip>

        <el-tooltip content="任务" placement="top">
          <button class="toolbar-btn" @click="$emit('create-todo')">
            <span class="material-icons-outlined">check_circle</span>
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
    gap: 8px;

    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      background: transparent;
      border-radius: 8px;
      transition: all var(--dt-transition-fast);
    }
  }

  // 分隔线
  .toolbar-divider {
    width: 1px;
    height: 20px;
    background: var(--dt-border-light);
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
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    .material-icons-outlined {
      font-size: 20px;
      transition: transform 0.2s var(--dt-ease-out);
    }

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);

      .material-icons-outlined {
        transform: scale(1.1);
      }
    }

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: var(--dt-brand-color);
      background: var(--dt-brand-bg);

      &::before {
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

    &.ai-reply-btn {
      background: linear-gradient(135deg, rgba(147, 51, 234, 0.08) 0%, rgba(147, 51, 234, 0.12) 100%);
      color: #9333ea;

      .material-icons-outlined {
        background: linear-gradient(135deg, #9333ea, #7c3aed);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        font-size: 22px;
      }

      &:hover {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(147, 51, 234, 0.2) 100%);
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(147, 51, 234, 0.2);
      }

      &:active {
        transform: translateY(0) scale(0.95);
      }
    }

    &.schedule-btn {
      color: var(--dt-warning-color);

      .material-icons-outlined {
        font-size: 22px;
      }

      &:hover {
        background: var(--dt-warning-bg);
        color: var(--dt-warning-color);
      }
    }
  }
}

// 暗色模式
:global(.dark) {
  .input-toolbar {
    .toolbar-divider {
      background: var(--dt-border-dark);
    }

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
        }
      }
    }
  }
}
</style>
