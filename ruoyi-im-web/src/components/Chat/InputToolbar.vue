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
  height: 50px;
  min-height: 50px;
  padding-bottom: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px; // 从20px调整为12px,更紧凑
  }
}

.toolbar-btn {
  width: 32px; // 野火IM:32px按钮
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #666666; // 野火IM:灰色图标
  cursor: pointer;
  border-radius: 4px;
  transition: all var(--dt-transition-fast);
  padding: 0;

  .material-icons-outlined {
    font-size: 20px; // 野火IM:20px图标
  }

  &:hover {
    background: rgba(0, 0, 0, 0.05);
    color: #4168e0; // hover时变为野火IM蓝色
  }

  &:active {
    transform: scale(0.95);
  }

  .dark & {
    color: #999999;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #4168e0;
    }
  }

  &:focus-visible {
    outline: 2px solid var(--dt-brand-color);
    outline-offset: 2px;
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  // 更多按钮
  &.more-btn {
    &:hover {
      background: var(--dt-bg-hover);
    }
  }

}

// 更多菜单样式
:global(.toolbar-more-menu) {
  min-width: 160px;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    font-size: 14px;

    .material-icons-outlined {
      font-size: 18px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      .material-icons-outlined {
        color: var(--dt-brand-color);
      }
    }
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
