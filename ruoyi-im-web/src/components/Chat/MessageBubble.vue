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
  margin-bottom: 16px;
  max-width: 70%;
  align-items: flex-start;
  position: relative;
  animation: messageSlideIn 0.3s ease-out forwards;

  .message-avatar {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    border-radius: 50%;
    overflow: hidden;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.05);
    }

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
  }

  // ==================== 接收方消息样式（对方发送的消息）====================
  // 头像在左边，消息在右边
  &.other {
    flex-direction: row;

    .message-avatar {
      order: 1;
      margin-right: 12px;
      margin-left: 0;
    }

    .message-wrapper {
      order: 2;
      align-items: flex-start;
    }

    .message-header {
      margin-bottom: 6px;
      margin-left: 0;

      .sender-name {
        font-size: 13px;
        color: #8c8c8c;
        font-weight: 500;
      }
    }
  }

  // ==================== 发送方消息样式（我发送的消息）====================
  // 头像在右边，消息在左边
  &.self {
    flex-direction: row;
    align-self: flex-end;

    .message-avatar {
      order: 2;
      margin-left: 12px;
      margin-right: 0;
    }

    .message-wrapper {
      order: 1;
      align-items: flex-end;
    }
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 消息内容样式 ====================

.message-content {
  position: relative;

  // 文本消息气泡 - 默认（接收方）
  .text-message {
    position: relative;
    background-color: #f5f5f5;
    padding: 10px 14px;
    border-radius: 8px;
    display: inline-block;
    max-width: 100%;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

    .text-content {
      color: #1f1f1f;
      line-height: 1.6;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-size: 15px;
    }

    // 接收方小三角（左侧）
    &::before {
      content: '';
      position: absolute;
      top: 14px;
      left: -6px;
      width: 0;
      height: 0;
      border: 6px solid transparent;
      border-right-color: #f5f5f5;
    }
  }

  // 图片消息
  .image-message {
    .image-container {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

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
      background: #f8f9fa;
      border-radius: 8px;
      padding: 12px 14px;
      cursor: pointer;
      min-width: 240px;
      transition: all 0.2s ease;
      border: 1px solid #e8e8e8;

      &:hover {
        background: #f0f2f5;
        border-color: #d9d9d9;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      .file-icon {
        font-size: 28px;
        color: #1890ff;
        margin-right: 12px;
      }

      .file-info {
        flex: 1;
        min-width: 0;

        .file-name {
          font-size: 14px;
          color: #1f1f1f;
          margin-bottom: 2px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 500;
        }

        .file-size {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      .file-action {
        margin-left: 12px;
        font-size: 18px;
        color: #1890ff;
      }
    }
  }

  // 语音消息
  .voice-message {
    .voice-container {
      display: flex;
      align-items: center;
      background: #f8f9fa;
      border-radius: 8px;
      padding: 10px 14px;
      cursor: pointer;
      min-width: 150px;
      transition: all 0.2s ease;
      border: 1px solid #e8e8e8;

      &:hover {
        background: #f0f2f5;
        border-color: #d9d9d9;
      }

      .voice-icon {
        font-size: 20px;
        color: #1890ff;
        margin-right: 10px;
      }

      .voice-wave {
        flex: 1;
        display: flex;
        align-items: center;
        height: 20px;

        .wave-bar {
          width: 3px;
          height: 8px;
          background: #d9d9d9;
          margin-right: 2px;
          border-radius: 2px;
          transition: background 0.3s ease;

          &.active {
            background: #1890ff;
            animation: voiceWave 1s infinite ease-in-out;

            &:nth-child(1) { animation-delay: 0s; }
            &:nth-child(2) { animation-delay: 0.1s; }
            &:nth-child(3) { animation-delay: 0.2s; }
            &:nth-child(4) { animation-delay: 0.3s; }
            &:nth-child(5) { animation-delay: 0.4s; }
          }
        }
      }

      .voice-duration {
        font-size: 12px;
        color: #8c8c8c;
        margin-left: 10px;
      }
    }
  }

  // 视频消息
  .video-message {
    .video-container {
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

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
      background: #f8f9fa;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      max-width: 280px;
      transition: all 0.2s ease;
      border: 1px solid #e8e8e8;

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
          font-size: 14px;
          font-weight: 500;
          color: #1f1f1f;
          margin-bottom: 2px;
        }

        .location-address {
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }

  // 系统消息
  .system-message {
    .system-content {
      text-align: center;
      font-size: 12px;
      color: #8c8c8c;
      padding: 6px 12px;

      &::before,
      &::after {
        content: '';
        display: inline-block;
        width: 30%;
        height: 1px;
        background: #e8e8e8;
        vertical-align: middle;
        margin: 0 8px;
      }
    }
  }

  // 引用消息
  .quote-message {
    .quote-container {
      background: #f8f9fa;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      transition: all 0.2s ease;
      border: 1px solid #e8e8e8;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .quote-content {
        background: #f0f2f5;
        padding: 8px 12px;
        border-left: 3px solid #1890ff;
        margin-bottom: 6px;

        .quote-text {
          font-size: 13px;
          color: #595959;
          margin-bottom: 2px;
        }

        .quote-sender {
          font-size: 11px;
          color: #8c8c8c;
        }
      }

      .current-text {
        padding: 0 12px 8px;
        font-size: 15px;
        color: #1f1f1f;
      }
    }
  }

  // 投票消息
  .vote-message {
    .vote-container {
      background: #f8f9fa;
      border-radius: 8px;
      padding: 12px 14px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      transition: all 0.2s ease;
      border: 1px solid #e8e8e8;

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .vote-title {
        font-size: 15px;
        font-weight: 500;
        color: #1f1f1f;
        margin-bottom: 10px;
      }

      .vote-options {
        .vote-option {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 12px;
          border-radius: 6px;
          margin-bottom: 6px;
          cursor: pointer;
          transition: all 0.2s ease;
          border: 1px solid transparent;

          &:hover {
            background: #fff;
            border-color: #d9d9d9;
          }

          &.selected {
            background: rgba(0, 214, 185, 0.1);
            border-color: #1890ff;
          }

          .option-text {
            font-size: 14px;
            color: #1f1f1f;
          }

          .option-votes {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }
  }

  // 消息状态（气泡内，右下角）
  .message-status-inline {
    position: absolute;
    right: -20px;
    bottom: 4px;
    display: flex;
    align-items: center;

    .status-icon {
      font-size: 14px;
      transition: all 0.3s ease;

      &.sending {
        color: #1890ff;
        animation: pulse 1.5s ease-in-out infinite;
      }

      &.sent {
        color: #52c41a;
      }

      &.delivered {
        color: #52c41a;
      }

      &.read {
        color: #1890ff;
      }

      &.failed {
        color: #ff4d4f;
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

// ==================== 发送方消息特殊样式 ====================

.message-bubble.self {
  .message-content {
    // 文本消息 - 发送方使用钉钉风格蓝绿色
    .text-message {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      box-shadow: 0 2px 8px rgba(0, 214, 185, 0.2);

      .text-content {
        color: #fff;
      }

      // 发送方小三角（右侧）
      &::before {
        left: auto;
        right: -6px;
        border-right-color: transparent;
        border-left-color: #1890ff;
      }
    }

    // 文件、语音等其他消息类型
    .file-message .file-container,
    .voice-message .voice-container,
    .location-message .location-container,
    .quote-message .quote-container,
    .vote-message .vote-container {
      border-color: rgba(0, 214, 185, 0.3);
    }

    // 消息状态位置调整（左侧）
    .message-status-inline {
      right: auto;
      left: -20px;
    }
  }

  // 时间戳右对齐
  .message-time-footer {
    text-align: right;
  }
}

// ==================== 时间戳样式 ====================

.message-time-footer {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  padding: 0 4px;
  white-space: nowrap;
}

// ==================== 撤回消息样式 ====================

.recalled-message {
  padding: 8px 14px;
  background-color: #f0f2f5;
  border-radius: 8px;
  color: #8c8c8c;
  font-size: 13px;
  font-style: italic;
}

// ==================== 动画定义 ====================

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

@keyframes voiceWave {
  0%, 100% { height: 8px; }
  50% { height: 16px; }
}

// ==================== 消息操作按钮 ====================

.message-actions {
  position: absolute;
  top: -30px;
  right: 0;
  display: flex;
  gap: 4px;
  padding: 4px 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
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
    color: #595959;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s ease;

    &:hover {
      color: #1890ff;
      background-color: rgba(0, 214, 185, 0.1);
    }

    &.danger:hover {
      color: #ff4d4f;
      background-color: rgba(255, 77, 79, 0.1);
    }
  }
}

.message-bubble:hover .message-actions {
  opacity: 1;
  visibility: visible;
}

// ==================== 右键菜单 ====================

.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 120px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  animation: fadeIn 0.15s ease;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 14px;
    color: #1f1f1f;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f0f2f5;
    }

    &.danger {
      color: #ff4d4f;

      &:hover {
        background-color: rgba(255, 77, 79, 0.1);
      }
    }

    i {
      font-size: 14px;
    }
  }

  .menu-divider {
    height: 1px;
    background-color: #e8e8e8;
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

// ==================== 响应式设计 ====================

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
        min-width: 200px;
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
