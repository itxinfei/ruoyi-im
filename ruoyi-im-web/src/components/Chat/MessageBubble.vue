<template>
  <transition name="message-slide" appear>
    <div class="message-bubble" :class="messageClasses" @contextmenu.prevent="showContextMenu">
      <!-- 头像 -->
      <div class="message-avatar">
        <img
          :src="message.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
          :alt="message.senderName"
          class="avatar-image"
        />
      </div>

      <!-- 消息主体区域 -->
      <div class="message-wrapper">
        <!-- 消息头部信息（仅群聊显示发送者名称） -->
        <div v-if="showSenderInfo && !isMine" class="message-header">
          <span class="sender-name">{{ message.senderName }}</span>
        </div>

        <!-- 消息内容 -->
        <div class="message-content" @dblclick="handleDoubleClick">
          <!-- 撤回的消息 -->
          <div v-if="message.status === 'recalled' || message.revoked" class="recalled-message">
            <span>{{
              isMine ? '你撤回了一条消息' : `${message.senderName || '对方'}撤回了一条消息`
            }}</span>
          </div>

          <!-- 文本消息 -->
          <div v-else-if="message.type === 'text'" class="text-message">
            <div class="text-content" v-html="formatText(message.content)"></div>
          </div>

          <!-- 图片消息 -->
          <div v-else-if="message.type === 'image'" class="image-message">
            <div class="image-container" @click="previewImage(message)">
              <img
                :src="message.content"
                :alt="message.content"
                class="message-image"
                @load="onImageLoad"
              />
              <div class="image-overlay">
                <i class="el-icon-zoom-in"></i>
              </div>
            </div>
          </div>

          <!-- 文件消息 -->
          <div v-else-if="message.type === 'file'" class="file-message">
            <div class="file-container" @click="downloadFile(message.content)">
              <div class="file-icon">
                <i :class="getFileIcon(message.content.name)"></i>
              </div>
              <div class="file-info">
                <div class="file-name">{{ message.content.name }}</div>
                <div class="file-size">{{ formatFileSize(message.content.size) }}</div>
              </div>
              <div class="file-action">
                <i class="el-icon-download"></i>
              </div>
            </div>
          </div>

          <!-- 语音消息 -->
          <div v-else-if="message.type === 'voice'" class="voice-message">
            <div class="voice-container" @click="playVoice(message.content)">
              <div class="voice-icon">
                <i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
              </div>
              <div class="voice-wave">
                <div v-for="i in 5" :key="i" class="wave-bar" :class="{ active: isPlaying }"></div>
              </div>
              <div class="voice-duration">{{ formatDuration(message.content.duration) }}</div>
            </div>
          </div>

          <!-- 视频消息 -->
          <div v-else-if="message.type === 'video'" class="video-message">
            <div class="video-container">
              <video
                :src="message.content.url"
                :poster="message.content.poster"
                controls
                class="message-video"
              ></video>
            </div>
          </div>

          <!-- 位置消息 -->
          <div v-else-if="message.type === 'location'" class="location-message">
            <div class="location-container" @click="openLocation(message.content)">
              <div class="location-map">
                <img :src="getLocationMapUrl(message.content)" alt="位置" />
              </div>
              <div class="location-info">
                <div class="location-name">{{ message.content.name }}</div>
                <div class="location-address">{{ message.content.address }}</div>
              </div>
            </div>
          </div>

          <!-- 系统消息 -->
          <div v-else-if="message.type === 'system'" class="system-message">
            <div class="system-content">{{ message.content }}</div>
          </div>

          <!-- 引用消息 -->
          <div v-else-if="message.type === 'quote'" class="quote-message">
            <div class="quote-container">
              <div class="quote-content">
                <div class="quote-text">{{ message.content.quoteText }}</div>
                <div class="quote-sender">{{ message.content.quoteSender }}</div>
              </div>
              <div class="current-text">{{ message.content.currentText }}</div>
            </div>
          </div>

          <!-- 投票消息 -->
          <div v-else-if="message.type === 'vote'" class="vote-message">
            <div class="vote-container">
              <div class="vote-title">{{ message.content.title }}</div>
              <div class="vote-options">
                <div
                  v-for="(option, index) in message.content.options"
                  :key="index"
                  class="vote-option"
                  :class="{ selected: option.selected }"
                  @click="selectVoteOption(index)"
                >
                  <div class="option-text">{{ option.text }}</div>
                  <div class="option-votes">{{ option.votes }}票</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 消息状态（气泡内） -->
          <div v-if="isMine && !message.revoked" class="message-status-inline">
            <transition name="status-fade" mode="out-in">
              <i
                v-if="message.status === 'sending'"
                key="sending"
                class="el-icon-loading status-icon sending"
              ></i>
              <i
                v-else-if="message.status === 'sent'"
                key="sent"
                class="el-icon-circle-check status-icon sent"
              ></i>
              <i
                v-else-if="message.status === 'delivered'"
                key="delivered"
                class="el-icon-success status-icon delivered"
              ></i>
              <i
                v-else-if="message.status === 'read'"
                key="read"
                class="el-icon-view status-icon read"
              ></i>
              <i
                v-else-if="message.status === 'failed'"
                key="failed"
                class="el-icon-warning status-icon failed"
                title="点击重发"
                @click.stop="$emit('resend', message.id)"
              ></i>
            </transition>
          </div>
        </div>

        <!-- 时间戳（气泡下方） -->
        <div class="message-time-footer">
          {{ formatTime(message.timestamp) }}
        </div>
      </div>

      <!-- 消息操作按钮 -->
      <div
        v-if="!message.revoked && message.status !== 'recalled'"
        class="message-actions"
        :class="{ visible: showActions }"
      >
        <el-tooltip content="回复" placement="top">
          <i class="el-icon-chat-line-round action-icon" @click.stop="handleReply"></i>
        </el-tooltip>
        <el-tooltip content="转发" placement="top">
          <i class="el-icon-share action-icon" @click.stop="handleForward"></i>
        </el-tooltip>
        <el-tooltip v-if="isMine && canRecall" content="撤回" placement="top">
          <i class="el-icon-refresh-left action-icon" @click.stop="handleRecall"></i>
        </el-tooltip>
        <el-tooltip content="复制" placement="top">
          <i class="el-icon-copy-document action-icon" @click.stop="handleCopy"></i>
        </el-tooltip>
        <el-tooltip content="删除" placement="top">
          <i class="el-icon-delete action-icon danger" @click.stop="handleDelete"></i>
        </el-tooltip>
      </div>

      <!-- 右键菜单 -->
      <teleport to="body">
        <div
          v-if="contextMenuVisible"
          class="context-menu"
          :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
          @click.stop
        >
          <div class="menu-item" @click="handleReply">
            <i class="el-icon-chat-line-round"></i> 回复
          </div>
          <div class="menu-item" @click="handleForward"><i class="el-icon-share"></i> 转发</div>
          <div class="menu-item" @click="handleCopy">
            <i class="el-icon-copy-document"></i> 复制
          </div>
          <div v-if="isMine && canRecall" class="menu-item" @click="handleRecall">
            <i class="el-icon-refresh-left"></i> 撤回
          </div>
          <div class="menu-divider"></div>
          <div class="menu-item danger" @click="handleDelete">
            <i class="el-icon-delete"></i> 删除
          </div>
        </div>
      </teleport>
    </div>
  </transition>
</template>

<script>
import { formatTime, formatDuration } from '@/utils/format/time'
import { formatFileSize, getFileTypeIcon } from '@/utils/format/file'
import { formatText } from '@/utils/format/text'

// 撤回时间限制（2分钟）
const RECALL_TIME_LIMIT = 2 * 60 * 1000

export default {
  name: 'MessageBubble',
  props: {
    message: {
      type: Object,
      required: true,
    },
    isMine: {
      type: Boolean,
      default: false,
    },
    showSenderInfo: {
      type: Boolean,
      default: false,
    },
  },
  emits: [
    'resend',
    'image-load',
    'reply',
    'forward',
    'recall',
    'delete',
    'copy',
    'preview-image',
    'download-file',
    'play-voice',
    'open-location',
    'select-vote',
    'context-menu',
  ],
  data() {
    return {
      isPlaying: false,
      imageLoaded: false,
      showActions: false,
      contextMenuVisible: false,
      contextMenuX: 0,
      contextMenuY: 0,
    }
  },
  computed: {
    messageClasses() {
      return {
        self: this.isMine,
        other: !this.isMine,
        [`message-${this.message.type}`]: true,
        'image-loaded': this.imageLoaded,
      }
    },
    canRecall() {
      // 检查是否可以撤回（2分钟内）
      if (!this.message.timestamp && !this.message.createTime) return false
      const msgTime = new Date(this.message.timestamp || this.message.createTime).getTime()
      return Date.now() - msgTime < RECALL_TIME_LIMIT
    },
  },
  mounted() {
    // 点击其他地方关闭右键菜单
    document.addEventListener('click', this.hideContextMenu)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.hideContextMenu)
  },
  methods: {
    formatText,
    formatTime,
    formatFileSize,
    formatDuration,
    getFileIcon(filename) {
      return getFileTypeIcon(filename)
    },
    getLocationMapUrl() {
      return 'https://gw.alipayobjects.com/zos/rmsportal/WJyJcGifizzEbBbDBwQV.png'
    },
    previewImage(message) {
      this.$emit('preview-image', message)
    },

    // 图片加载完成
    onImageLoad(event) {
      this.imageLoaded = true
      if (event.target) {
        event.target.style.opacity = '1'
      }
      this.$emit('image-load', this.message)
    },

    // 复制消息内容
    handleCopy() {
      this.hideContextMenu()
      let content = ''
      if (this.message.type === 'text') {
        content = this.message.content
      } else if (this.message.type === 'file') {
        content = this.message.content.name
      } else if (this.message.type === 'location') {
        content = `${this.message.content.name} - ${this.message.content.address}`
      }

      if (content) {
        navigator.clipboard
          .writeText(content)
          .then(() => {
            this.$emit('copy', this.message)
          })
          .catch(err => {
            console.error('复制失败:', err)
          })
      }
    },

    // 删除消息
    handleDelete() {
      this.hideContextMenu()
      this.$emit('delete', this.message.id)
    },
  },
}
</script>

<style scoped lang="scss">
@use '@/styles/dingtalk-theme.scss' as *;

.message-bubble {
  display: flex;
  margin-bottom: $spacing-sm;
  max-width: 80%;
  opacity: 0;
  transform: translateY(20px);
  align-items: flex-start;
  position: relative;

  .message-avatar {
    width: 36px;
    height: 36px;
    margin: 0 $spacing-sm;
    flex-shrink: 0;
    order: 1;

    .avatar-image {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
    }
  }

  .message-wrapper {
    display: flex;
    flex-direction: column;
    max-width: 100%;
    order: 2;
  }

  // 接收方消息样式
  &.other {
    align-self: flex-start;

    .message-avatar {
      margin-right: $spacing-sm;
      margin-left: 0;
    }

    .message-header {
      margin-bottom: 4px;
      margin-left: 2px;

      .sender-name {
        font-size: $font-size-xs;
        color: $text-secondary;
        font-weight: $font-weight-medium;
      }
    }
  }

  // 发送方消息样式
  &.self {
    align-self: flex-end;
    flex-direction: row-reverse;

    .message-avatar {
      order: 2;
      margin-left: $spacing-sm;
      margin-right: 0;
    }

    .message-wrapper {
      order: 1;
      align-items: flex-end;
    }
  }
}

.message-content {
  position: relative;

  // 文本消息气泡
  .text-message {
    position: relative;
    background-color: $bg-white;
    padding: 8px 12px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    transition: all 0.2s ease;
    display: inline-block;
    max-width: 100%;

    .text-content {
      color: $text-primary;
      line-height: 1.5;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-size: $font-size-base;
    }

    // 接收方小三角（左侧）
    &::before {
      content: '';
      position: absolute;
      top: 12px;
      left: -10px;
      width: 0;
      height: 0;
      border: 5px solid transparent;
      border-right-color: $bg-white;
    }
  }

  // 图片消息
  .image-message {
    .image-container {
      position: relative;
      border-radius: 4px 12px 12px 12px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .message-image {
        max-width: 280px;
        max-height: 280px;
        display: block;
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      .image-loaded .message-image {
        opacity: 1;
      }

      .image-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s ease;

        i {
          font-size: 32px;
          color: #fff;
          transform: scale(0.8);
          transition: transform 0.3s ease;
        }
      }

      &:hover {
        .image-overlay {
          opacity: 1;

          i {
            transform: scale(1);
          }
        }
      }
    }
  }

  // 文件消息
  .file-message {
    .file-container {
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 4px 12px 12px 12px;
      padding: 10px 12px;
      cursor: pointer;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      min-width: 220px;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .file-icon {
        font-size: 28px;
        color: $primary-color;
        margin-right: 10px;
      }

      .file-info {
        flex: 1;
        min-width: 0;

        .file-name {
          font-size: $font-size-base;
          color: $text-primary;
          margin-bottom: 2px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .file-size {
          font-size: $font-size-xs;
          color: $text-tertiary;
        }
      }

      .file-action {
        margin-left: 10px;
        font-size: 18px;
        color: $primary-color;
      }
    }
  }

  // 语音消息
  .voice-message {
    .voice-container {
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 4px 12px 12px 12px;
      padding: 8px 12px;
      cursor: pointer;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      min-width: 140px;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .voice-icon {
        font-size: 20px;
        color: $primary-color;
        margin-right: 8px;
      }

      .voice-wave {
        flex: 1;
        display: flex;
        align-items: center;
        height: 20px;

        .wave-bar {
          width: 3px;
          height: 8px;
          background: #e8e8e8;
          margin-right: 2px;
          border-radius: 2px;
          transition: background 0.3s ease;

          &.active {
            background: $primary-color;
            animation: wave 1s infinite ease-in-out;

            &:nth-child(1) { animation-delay: 0s; }
            &:nth-child(2) { animation-delay: 0.1s; }
            &:nth-child(3) { animation-delay: 0.2s; }
            &:nth-child(4) { animation-delay: 0.3s; }
            &:nth-child(5) { animation-delay: 0.4s; }
          }
        }
      }

      .voice-duration {
        font-size: $font-size-xs;
        color: $text-tertiary;
        margin-left: 8px;
      }
    }
  }

  // 视频消息
  .video-message {
    .video-container {
      border-radius: 4px 12px 12px 12px;
      overflow: hidden;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

      .message-video {
        max-width: 280px;
        max-height: 280px;
        display: block;
      }
    }
  }

  // 位置消息
  .location-message {
    .location-container {
      background: #fff;
      border-radius: 4px 12px 12px 12px;
      overflow: hidden;
      cursor: pointer;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      max-width: 280px;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .location-map {
        img {
          width: 100%;
          height: 120px;
          object-fit: cover;
        }
      }

      .location-info {
        padding: 10px 12px;

        .location-name {
          font-size: $font-size-base;
          font-weight: $font-weight-medium;
          color: $text-primary;
          margin-bottom: 2px;
        }

        .location-address {
          font-size: $font-size-xs;
          color: $text-tertiary;
        }
      }
    }
  }

  // 系统消息
  .system-message {
    .system-content {
      text-align: center;
      font-size: $font-size-xs;
      color: $text-tertiary;
      padding: 6px 12px;

      &::before,
      &::after {
        content: '';
        display: inline-block;
        width: 30%;
        height: 1px;
        background: $border-light;
        vertical-align: middle;
        margin: 0 8px;
      }
    }
  }

  // 引用消息
  .quote-message {
    .quote-container {
      background: #fff;
      border-radius: 4px 12px 12px 12px;
      overflow: hidden;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .quote-content {
        background: $bg-light;
        padding: 6px 10px;
        border-left: 3px solid $primary-color;
        margin-bottom: 6px;

        .quote-text {
          font-size: $font-size-sm;
          color: $text-secondary;
          margin-bottom: 2px;
        }

        .quote-sender {
          font-size: $font-size-xs;
          color: $text-tertiary;
        }
      }

      .current-text {
        padding: 0 10px 8px;
        font-size: $font-size-base;
        color: $text-primary;
      }
    }
  }

  // 投票消息
  .vote-message {
    .vote-container {
      background: $bg-white;
      border-radius: 4px 12px 12px 12px;
      padding: 10px 12px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .vote-title {
        font-size: $font-size-base;
        font-weight: $font-weight-medium;
        color: $text-primary;
        margin-bottom: 8px;
      }

      .vote-options {
        .vote-option {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 6px 10px;
          border-radius: $border-radius-sm;
          margin-bottom: 6px;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid transparent;

          &:hover {
            background: $bg-hover;
          }

          &.selected {
            background: $primary-color-lighter;
            border-color: $primary-color;
          }

          .option-text {
            font-size: $font-size-base;
            color: $text-primary;
          }

          .option-votes {
            font-size: $font-size-sm;
            color: $text-tertiary;
          }
        }
      }
    }
  }

  // 消息状态（气泡内，右下角）
  .message-status-inline {
    position: absolute;
    right: -18px;
    bottom: 4px;
    display: flex;
    align-items: center;

    .status-icon {
      font-size: 12px;
      transition: all 0.3s ease;

      &.sending {
        color: $primary-color;
        animation: pulse 1.5s ease-in-out infinite;
      }

      &.sent {
        color: $success-color;
      }

      &.delivered {
        color: $success-color;
      }

      &.read {
        color: $primary-color;
      }

      &.failed {
        color: $error-color;
        cursor: pointer;
        transition: transform 0.2s ease, color 0.2s ease;

        &:hover {
          transform: scale(1.2);
          color: #ff7875;
        }
      }
    }
  }
}

// 发送方消息特殊样式
.message-bubble.self {
  .message-content {
    // 文本消息 - 发送方
    .text-message {
      background-color: $primary-color;
      border-radius: 12px 4px 12px 12px;

      .text-content {
        color: #fff;
      }

      // 发送方小三角（右侧）
      &::before {
        left: auto;
        right: -10px;
        border-right-color: transparent;
        border-left-color: $primary-color;
      }
    }

    // 其他消息类型也调整圆角
    .image-message .image-container,
    .file-message .file-container,
    .voice-message .voice-container,
    .video-message .video-container,
    .location-message .location-container,
    .quote-message .quote-container,
    .vote-message .vote-container {
      border-radius: 12px 4px 12px 12px;
    }

    // 消息状态位置调整
    .message-status-inline {
      right: auto;
      left: -18px;
    }
  }
}

// 时间戳（气泡下方）
.message-time-footer {
  font-size: 11px;
  color: $text-tertiary;
  margin-top: 4px;
  padding: 0 2px;
  white-space: nowrap;
}

.message-bubble.self .message-time-footer {
  text-align: right;
}

// 撤回消息样式
.recalled-message {
  padding: 6px 12px;
  background-color: $bg-light;
  border-radius: 4px 12px 12px 12px;
  color: $text-tertiary;
  font-size: $font-size-sm;
  font-style: italic;
}

// 动画
.message-slide-enter-active {
  animation: slideIn 0.3s ease-out forwards;
}

.message-slide-leave-active {
  animation: slideOut 0.3s ease-in forwards;
}

.status-fade-enter-active,
.status-fade-leave-active {
  transition: all 0.3s ease;
}

.status-fade-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.status-fade-leave-to {
  opacity: 0;
  transform: scale(1.2);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-20px);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes wave {
  0%, 100% { height: 8px; }
  50% { height: 16px; }
}

// 消息操作按钮
.message-actions {
  position: absolute;
  top: -30px;
  right: 0;
  display: flex;
  gap: 4px;
  padding: 4px 8px;
  background: $bg-white;
  border-radius: $border-radius-xl;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s ease;

  &.visible {
    opacity: 1;
    visibility: visible;
  }

  .action-icon {
    padding: 4px;
    font-size: 14px;
    color: $text-secondary;
    cursor: pointer;
    border-radius: 50%;
    transition: all 0.2s ease;

    &:hover {
      color: $primary-color;
      background-color: $primary-color-lighter;
    }

    &.danger:hover {
      color: $error-color;
      background-color: #fff1f0;
    }
  }
}

.message-bubble:hover .message-actions {
  opacity: 1;
  visibility: visible;
}

// 右键菜单
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 120px;
  background: $bg-white;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-lg;
  padding: 4px 0;
  animation: fadeIn 0.15s ease;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: $font-size-sm;
    color: $text-primary;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: $bg-hover;
    }

    &.danger {
      color: $error-color;

      &:hover {
        background-color: #fff1f0;
      }
    }

    i {
      font-size: 14px;
    }
  }

  .menu-divider {
    height: 1px;
    background-color: $border-light;
    margin: 4px 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 响应式
@media (max-width: 768px) {
  .message-bubble {
    max-width: 85%;

    .message-content {
      .image-message .image-container .message-image,
      .video-message .video-container .message-video {
        max-width: 240px;
        max-height: 240px;
      }

      .location-message .location-container {
        max-width: 240px;
      }

      .file-message .file-container {
        min-width: 180px;
      }

      .voice-message .voice-container {
        min-width: 140px;
      }
    }
  }
}

@media (max-width: 480px) {
  .message-bubble {
    max-width: 90%;

    .message-content {
      .image-message .image-container .message-image,
      .video-message .video-container .message-video {
        max-width: 200px;
        max-height: 200px;
      }

      .location-message .location-container {
        max-width: 200px;
      }
    }
  }
}
</style>
