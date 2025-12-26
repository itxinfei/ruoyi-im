<template>
  <transition name="message-slide" appear>
    <div class="message-bubble" :class="messageClasses">
      <!-- 消息头部信息 -->
      <div v-if="showSenderInfo" class="message-header">
        <span class="sender-name">{{ message.senderName }}</span>
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
      </div>

      <!-- 消息内容 -->
      <div class="message-content">
        <!-- 文本消息 -->
        <div v-if="message.type === 'text'" class="text-message">
          <div class="text-content" v-html="formatText(message.content)"></div>
        </div>

        <!-- 图片消息 -->
        <div v-else-if="message.type === 'image'" class="image-message">
          <div class="image-container" @click="previewImage(message.content)">
            <img :src="message.content" :alt="message.content" class="message-image" @load="onImageLoad" />
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
            <div class="vote-info">
              <span>{{ message.content.totalVotes }}人参与</span>
              <span v-if="!message.content.expired"
                >截止时间: {{ formatTime(message.content.deadline) }}</span
              >
            </div>
          </div>
        </div>
      </div>

      <!-- 消息状态 -->
      <div v-if="isSelf" class="message-status">
        <transition name="status-fade" mode="out-in">
          <i v-if="message.status === 'sending'" key="sending" class="el-icon-loading status-icon sending"></i>
          <i v-else-if="message.status === 'sent'" key="sent" class="el-icon-circle-check status-icon sent"></i>
          <i v-else-if="message.status === 'delivered'" key="delivered" class="el-icon-success status-icon delivered"></i>
          <i v-else-if="message.status === 'read'" key="read" class="el-icon-view status-icon read"></i>
          <i
            v-else-if="message.status === 'failed'"
            key="failed"
            class="el-icon-warning status-icon failed"
            @click.stop="$emit('resend', message.id)"
            title="点击重发"
          ></i>
        </transition>
      </div>
    </div>
  </transition>
</template>

<script>
import { formatTime, formatDuration } from '@/utils/format/time'
import { formatFileSize, getFileTypeIcon } from '@/utils/format/file'
import { formatText } from '@/utils/format/text'

export default {
  name: 'MessageBubble',
  props: {
    message: {
      type: Object,
      required: true,
    },
    isSelf: {
      type: Boolean,
      default: false,
    },
    showSenderInfo: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['resend', 'image-load'],
  data() {
    return {
      isPlaying: false,
      imageLoaded: false,
    }
  },
  computed: {
    messageClasses() {
      return {
        self: this.isSelf,
        other: !this.isSelf,
        [`message-${this.message.type}`]: true,
        'image-loaded': this.imageLoaded,
      }
    },
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
    previewImage(url) {
      this.$emit('preview-image', url)
    },
    downloadFile(file) {
      this.$emit('download-file', file)
    },
    playVoice(voice) {
      this.isPlaying = !this.isPlaying
      this.$emit('play-voice', voice)
    },
    openLocation(location) {
      this.$emit('open-location', location)
    },
    selectVoteOption(index) {
      this.$emit('select-vote', index)
    },
    onImageLoad() {
      this.imageLoaded = true
      this.$emit('image-load', this.message.id)
    },
  },
}
</script>

<style scoped lang="scss">
.message-bubble {
  display: flex;
  margin-bottom: 16px;
  max-width: 80%;
  opacity: 0;
  transform: translateY(20px);

  &.self {
    align-self: flex-end;
    flex-direction: row-reverse;

    .message-content {
      .text-message {
        background-color: #1890ff;
        color: #fff;

        .text-content {
          color: #fff;
        }
      }

      .image-message {
        .image-container {
          .image-overlay {
            background: rgba(0, 0, 0, 0.5);
          }
        }
      }
    }

    .message-status {
      margin-right: 8px;
      display: flex;
      align-items: flex-end;

      .status-icon {
        font-size: 14px;
        transition: all 0.3s ease;

        &.sending {
          color: #1890ff;
          animation: pulse 1.5s ease-in-out infinite;
        }

        &.sent {
          color: #b8e986;
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

          &:active {
            transform: scale(1.1);
          }
        }
      }
    }
  }

  &.other {
    align-self: flex-start;

    .message-header {
      margin-bottom: 4px;

      .sender-name {
        font-size: 12px;
        color: #1890ff;
        margin-right: 8px;
        font-weight: 500;
      }

      .message-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .message-content {
    .text-message {
      background-color: #fff;
      padding: 12px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      transition: transform 0.2s ease, box-shadow 0.2s ease;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .text-content {
        color: #333;
        line-height: 1.5;
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    }

    .image-message {
      .image-container {
        position: relative;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: transform 0.2s ease, box-shadow 0.2s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &:active {
          transform: translateY(0);
        }

        .message-image {
          max-width: 300px;
          max-height: 300px;
          display: block;
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &.image-loaded .message-image {
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

    .file-message {
      .file-container {
        display: flex;
        align-items: center;
        background: #fff;
        border-radius: 8px;
        padding: 12px;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        min-width: 240px;
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
          background: #fafafa;
        }

        &:active {
          transform: translateY(0);
        }

        .file-icon {
          font-size: 32px;
          color: #1890ff;
          margin-right: 12px;
          transition: transform 0.2s ease;
        }

        .file-info {
          flex: 1;
          min-width: 0;

          .file-name {
            font-size: 14px;
            color: #333;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .file-size {
            font-size: 12px;
            color: #999;
          }
        }

        .file-action {
          margin-left: 12px;
          font-size: 20px;
          color: #1890ff;
          transition: transform 0.2s ease, color 0.2s ease;
        }

        &:hover {
          .file-icon,
          .file-action {
            transform: scale(1.1);
          }
        }
      }
    }

    .voice-message {
      .voice-container {
        display: flex;
        align-items: center;
        background: #fff;
        border-radius: 8px;
        padding: 12px;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        min-width: 200px;
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
          background: #fafafa;
        }

        &:active {
          transform: translateY(0);
        }

        .voice-icon {
          font-size: 24px;
          color: #1890ff;
          margin-right: 12px;
          transition: transform 0.2s ease;
        }

        .voice-wave {
          flex: 1;
          display: flex;
          align-items: center;
          height: 30px;

          .wave-bar {
            width: 3px;
            height: 10px;
            background: #e8e8e8;
            margin-right: 3px;
            border-radius: 2px;
            transition: background 0.3s ease;

            &.active {
              background: #1890ff;
              animation: wave 1s infinite ease-in-out;

              &:nth-child(1) {
                animation-delay: 0s;
              }
              &:nth-child(2) {
                animation-delay: 0.1s;
              }
              &:nth-child(3) {
                animation-delay: 0.2s;
              }
              &:nth-child(4) {
                animation-delay: 0.3s;
              }
              &:nth-child(5) {
                animation-delay: 0.4s;
              }
            }
          }
        }

        .voice-duration {
          font-size: 12px;
          color: #999;
          margin-left: 12px;
        }
      }

      &:hover {
        .voice-icon {
          transform: scale(1.1);
        }
      }
    }

    .video-message {
      .video-container {
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        transition: transform 0.2s ease, box-shadow 0.2s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .message-video {
          max-width: 300px;
          max-height: 300px;
          display: block;
        }
      }
    }

    .location-message {
      .location-container {
        background: #fff;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        max-width: 300px;
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .location-map {
          img {
            width: 100%;
            height: 120px;
            object-fit: cover;
            transition: transform 0.3s ease;
          }
        }

        &:hover {
          .location-map img {
            transform: scale(1.05);
          }
        }

        .location-info {
          padding: 12px;

          .location-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }

          .location-address {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }

    .system-message {
      .system-content {
        text-align: center;
        font-size: 12px;
        color: #999;
        padding: 8px 12px;

        &::before,
        &::after {
          content: '';
          display: inline-block;
          width: 30%;
          height: 1px;
          background: #e8e8e8;
          vertical-align: middle;
          margin: 0 10px;
        }
      }
    }

    .quote-message {
      .quote-container {
        background: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .quote-content {
          background: #f5f5f5;
          padding: 8px 12px;
          border-left: 4px solid #1890ff;
          margin-bottom: 8px;

          .quote-text {
            font-size: 14px;
            color: #333;
            margin-bottom: 4px;
          }

          .quote-sender {
            font-size: 12px;
            color: #999;
          }
        }

        .current-text {
          padding: 0 12px 12px;
          font-size: 14px;
          color: #333;
        }
      }
    }

    .vote-message {
      .vote-container {
        background: #fff;
        border-radius: 8px;
        padding: 12px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .vote-title {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 12px;
        }

        .vote-options {
          margin-bottom: 12px;

          .vote-option {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px 12px;
            border-radius: 4px;
            margin-bottom: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
            border: 1px solid transparent;

            &:hover {
              background: #f5f5f5;
            }

            &.selected {
              background: #e6f7ff;
              border-color: #1890ff;
            }

            .option-text {
              font-size: 14px;
              color: #333;
            }

            .option-votes {
              font-size: 12px;
              color: #999;
            }
          }
        }

        .vote-info {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

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
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes wave {
  0%,
  100% {
    height: 10px;
  }
  50% {
    height: 20px;
  }
}

@media (max-width: 768px) {
  .message-bubble {
    max-width: 85%;

    .message-content {
      .image-message .image-container .message-image {
        max-width: 250px;
        max-height: 250px;
      }

      .video-message .video-container .message-video {
        max-width: 250px;
        max-height: 250px;
      }

      .location-message .location-container {
        max-width: 250px;
      }

      .file-message .file-container {
        min-width: 200px;
      }

      .voice-message .voice-container {
        min-width: 180px;
      }
    }
  }
}

@media (max-width: 480px) {
  .message-bubble {
    max-width: 90%;
    margin-bottom: 12px;

    .message-content {
      .image-message .image-container .message-image {
        max-width: 200px;
        max-height: 200px;
      }

      .video-message .video-container .message-video {
        max-width: 200px;
        max-height: 200px;
      }

      .location-message .location-container {
        max-width: 200px;
      }

      .file-message .file-container {
        min-width: 180px;
        padding: 10px;

        .file-icon {
          font-size: 28px;
        }
      }

      .voice-message .voice-container {
        min-width: 160px;
        padding: 10px;

        .voice-icon {
          font-size: 20px;
        }
      }
    }
  }
}
</style>
