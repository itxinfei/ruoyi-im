<template>
  <div class="input-toolbar">
    <!-- 左侧工具组:核心功能 -->
    <div class="toolbar-left">
      <el-tooltip
        content="表情"
        placement="top"
      >
        <button
          class="toolbar-btn"
          :class="{ active: showEmojiPicker }"
          @click.stop="$emit('toggle-emoji')"
        >
          <span class="material-icons-outlined">sentiment_satisfied_alt</span>
        </button>
      </el-tooltip>

      <el-tooltip
        content="图片"
        placement="top"
      >
        <button
          class="toolbar-btn"
          @click="$emit('upload-image')"
        >
          <span class="material-icons-outlined">image</span>
        </button>
      </el-tooltip>

      <el-tooltip
        content="文件"
        placement="top"
      >
        <button
          class="toolbar-btn"
          @click="$emit('upload-file')"
        >
          <span class="material-icons-outlined">insert_drive_file</span>
        </button>
      </el-tooltip>

      <el-tooltip
        v-if="showAtButton"
        content="@成员"
        placement="top"
      >
        <button
          class="toolbar-btn"
          @click="$emit('at-member')"
        >
          <span class="material-icons-outlined">alternate_email</span>
        </button>
      </el-tooltip>

      <!-- 更多功能下拉菜单 -->
      <el-dropdown
        trigger="click"
        @command="handleMoreCommand"
      >
        <el-tooltip
          content="更多"
          placement="top"
        >
          <button class="toolbar-btn more-btn">
            <span class="material-icons-outlined">more_horiz</span>
          </button>
        </el-tooltip>
        <template #dropdown>
          <el-dropdown-menu class="toolbar-more-menu">
            <el-dropdown-item command="screenshot">
              <span class="material-icons-outlined">screenshot</span>
              <span>截图</span>
            </el-dropdown-item>
            <el-dropdown-item command="smart-reply">
              <span class="material-icons-outlined">auto_awesome</span>
              <span>智能回复</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
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

const emit = defineEmits([
  'toggle-emoji',
  'upload-image',
  'upload-file',
  'screenshot',
  'at-member',
  'smart-reply'
])

// 处理更多菜单命令
const handleMoreCommand = command => {
  emit(command)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 42px;
  min-height: 42px;
  padding-bottom: 0;
  margin-bottom: 4px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.toolbar-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #666666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.15s ease;
  padding: 0;

  .material-icons-outlined {
    font-size: 18px;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.05);
    color: #0089ff;
  }

  &:active {
    transform: scale(0.95);
  }

  .dark & {
    color: #999999;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #0089ff;
    }
  }

  &:focus-visible {
    outline: 2px solid #0089ff;
    outline-offset: 2px;
  }

  &.active {
    background: #e6f4ff;
    color: #0089ff;
  }

  // 更多按钮
  &.more-btn {
    &:hover {
      background: rgba(0, 0, 0, 0.05);
    }
  }

}

// 更多菜单样式
:global(.toolbar-more-menu) {
  min-width: 140px;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 13px;

    .material-icons-outlined {
      font-size: 16px;
      color: #5f6672;
    }

    &:hover {
      .material-icons-outlined {
        color: #0089ff;
      }
    }
  }
}

// 暗色模式
:global(.dark) {
  .input-toolbar {
    .toolbar-btn {
      color: #a0a8b8;

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #0089ff;
      }

      &.active {
        background: rgba(0, 137, 255, 0.15);
        color: #0089ff;
      }
    }
  }

  .toolbar-more-menu {
    .el-dropdown-menu__item {
      .material-icons-outlined {
        color: var(--dt-text-secondary-dark);
      }

      &:hover {
        .material-icons-outlined {
          color: var(--dt-brand-color);
        }
      }
    }
  }
}
</style>
