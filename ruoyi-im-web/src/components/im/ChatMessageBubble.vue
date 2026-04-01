<template>
  <div 
    :class="[
      'message-item', 
      isMe ? 'is-me' : 'is-other',
      { 'is-grouped': isGrouped }
    ]"
  >
    <!-- 时间分割线 -->
    <div v-if="showTime" class="system-notice">
      {{ formattedTime }}
    </div>

    <!-- 消息主容器 -->
    <div class="message-container">
      <!-- 头像 -->
      <div v-if="!isGrouped" class="avatar-wrapper">
        <img :src="message.senderAvatar || '/avatars/default.png'" class="avatar" alt="avatar" />
      </div>
      <div v-else class="avatar-placeholder"></div>

      <!-- 消息内容区 -->
      <div class="message-content-wrapper">
        <!-- 接收方昵称 -->
        <div v-if="!isMe && !isGrouped" class="sender-name">{{ message.senderName }}</div>

        <!-- 引用回复预览 -->
        <div v-if="message.replyToMessageId && quotedMessage" class="quoted-message-preview">
          <span class="quoted-author">{{ quotedMessage.senderName }}:</span>
          <span class="quoted-content">{{ quotedMessage.content }}</span>
        </div>

        <div class="bubble-and-actions" @mouseenter="isHovered = true" @mouseleave="isHovered = false">
          <!-- 气泡行：气泡 + 状态栏（底部对齐） -->
          <div class="bubble-row">
            <!-- 气泡本体 -->
            <div :class="['message-bubble', `type-${(message.type || 'text').toLowerCase()}`]">
              <!-- 文本（非纯URL） -->
              <div v-if="(!message.type || message.type === 'TEXT') && !isPureUrlMessage" class="text-content">
                <template v-for="(part, index) in parsedTextParts" :key="index">
                  <a v-if="part.isLink" :href="part.text" target="_blank" class="text-link" @click.stop>{{ part.text }}</a>
                  <span v-else>{{ part.text }}</span>
                </template>
              </div>
              <!-- 纯URL消息：渲染为链接卡片 -->
              <div v-else-if="isPureUrlMessage" class="link-card" @click="openLink(getPureUrl)">
                <div class="link-content">
                  <div class="link-title">{{ getPureUrl }}</div>
                  <div class="link-url">{{ formatDisplayUrl(getPureUrl) }}</div>
                </div>
              </div>
              <!-- 图片 -->
              <div v-else-if="message.type === 'IMAGE'" class="image-content">
                <el-image :src="message.fileUrl" :preview-src-list="[message.fileUrl]" fit="cover" class="content-img" />
              </div>
              <!-- 视频 -->
              <div v-else-if="message.type === 'VIDEO'" class="video-content" @click="openVideoPlayer">
                <video :src="message.fileUrl" class="content-video" />
                <div class="video-overlay">
                  <el-icon class="play-icon"><VideoPlay /></el-icon>
                </div>
                <span v-if="message.duration" class="video-duration">{{ formatVideoDuration(message.duration) }}</span>
              </div>
              <!-- 文件 -->
              <div v-else-if="message.type === 'FILE'" class="file-content" @click="handleFileClick">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ message.fileName || '文件' }}</span>
              </div>
              <!-- 语音 -->
              <VoiceMessageBubble v-else-if="message.type === 'VOICE'" :message="message" />
              <!-- 链接卡片（LINK类型，后端返回元数据） -->
              <div v-else-if="message.type === 'LINK' && linkInfo" class="link-card" @click="openLink(linkInfo.url)">
                <div v-if="linkInfo.imageUrl" class="link-image">
                  <img :src="linkInfo.imageUrl" alt="" @error="handleImageError">
                </div>
                <div class="link-content">
                  <div class="link-title">{{ linkInfo.title }}</div>
                  <div v-if="linkInfo.description" class="link-desc">{{ linkInfo.description }}</div>
                  <div class="link-url">{{ formatDisplayUrl(linkInfo.url) }}</div>
                </div>
              </div>
              <!-- 名片 -->
              <div v-else-if="message.type === 'CARD' && cardInfo" class="card-content" @click="handleCardClick">
                <img :src="cardInfo.userAvatar || '/avatars/default.png'" class="card-avatar" />
                <div class="card-info">
                  <div class="card-name">{{ cardInfo.userName }}</div>
                  <div class="card-dept">{{ cardInfo.department || '' }}</div>
                </div>
                <div class="card-tag">{{ cardInfo.cardType === 'group' ? '群聊名片' : '个人名片' }}</div>
              </div>
              <!-- 位置 -->
              <div v-else-if="message.type === 'LOCATION' && locationInfo" class="location-content" @click="openLocation">
                <div class="location-map">
                  <img :src="locationMapUrl" alt="位置" class="map-image" />
                  <div class="location-overlay">
                    <el-icon class="location-icon"><Location /></el-icon>
                  </div>
                </div>
                <div class="location-info">
                  <div class="location-name">{{ locationInfo.name }}</div>
                  <div v-if="locationInfo.address" class="location-address">{{ locationInfo.address }}</div>
                </div>
              </div>
            </div>

            <!-- 发送方：状态与已读 -->
            <div v-if="isMe" class="message-status-sidebar">
              <el-icon v-if="message.status === 'sending'" class="status-icon is-loading"><Loading /></el-icon>
              <el-icon v-else-if="message.status === 'failed'" class="status-icon is-failed" title="发送失败，点击重试"><WarningFilled /></el-icon>
              <span v-else-if="message.isEdited" class="edited-tag">已编辑</span>
              <span v-else-if="message.isRead !== undefined" :class="['read-status', message.isRead ? 'is-read' : 'is-unread']" @click="$emit('read-detail', message.messageId || message.id)">
                {{ message.isRead ? '已读' : '未读' }}
              </span>
            </div>
          </div>

          <!-- 悬停操作条 (Action Bar) -->
          <div v-if="isHovered" class="action-bar">
            <div class="action-item" title="回复" @click="$emit('reply', message)">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="action-item" title="复制" @click="copyText">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="action-item" title="转发" @click="$emit('forward', message)">
              <el-icon><Position /></el-icon>
            </div>
            <div v-if="isMe && canRecall" class="action-item" title="撤回" @click="$emit('recall', message)">
              <el-icon><RefreshLeft /></el-icon>
            </div>
            <div v-if="isMe && canEdit" class="action-item" title="编辑" @click="$emit('edit', message)">
              <el-icon><Edit /></el-icon>
            </div>
            <div v-if="isMe" class="action-item" title="删除" @click="$emit('delete', message)">
              <el-icon><Delete /></el-icon>
            </div>
            <div class="action-item" title="收藏" @click="$emit('favorite', message)">
              <el-icon><Star /></el-icon>
            </div>
            <el-dropdown trigger="click" @command="(emoji) => $emit('reaction', { message, emoji })">
              <div class="action-item" title="表情">
                <el-icon style="font-size: 18px;"><Star /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="👍">👍 点赞</el-dropdown-item>
                  <el-dropdown-item command="❤️">❤️ 爱心</el-dropdown-item>
                  <el-dropdown-item command="😂">😂 笑哭</el-dropdown-item>
                  <el-dropdown-item command="😮">😮 惊讶</el-dropdown-item>
                  <el-dropdown-item command="😢">😢 悲伤</el-dropdown-item>
                  <el-dropdown-item command="👏">👏 鼓掌</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

        </div>

        <!-- 撤回/系统提示 -->
        <div v-if="message.status === 'recalled'" class="system-notice">
          你撤回了一条消息 <span class="re-edit-link" @click="$emit('edit', message)">重新编辑</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
/**
 * ChatMessageBubble.vue (对齐钉钉 8px 与色彩规范 + 引用回复 + 操作条完善 + URL预览)
 */
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading, WarningFilled, ChatLineRound, DocumentCopy, Position, MoreFilled, RefreshLeft, Delete, Star, Document, Microphone, Edit, VideoPlay, Location } from '@element-plus/icons-vue';
import VoiceMessageBubble from '@/components/Chat/VoiceMessageBubble.vue';

const props = defineProps({
  message: { type: Object, required: true },
  isMe: { type: Boolean, default: false },
  isGrouped: { type: Boolean, default: false },
  showTime: { type: Boolean, default: false },
  quotedMessage: { type: Object, default: null }
});

const emit = defineEmits(['reply', 'forward', 'recall', 'delete', 'favorite', 'read-detail', 'edit', 'reaction']);

const isHovered = ref(false);

// URL 正则
const urlPattern = /(https?:\/\/[^\s<]+[^<.,:;"')\]\s])/g;

// 检测纯URL消息
const isPureUrlMessage = computed(() => {
  if (!props.message || props.message.type !== 'TEXT') return false;
  const content = props.message.content?.trim() || '';
  if (!content) return false;
  const urlMatches = content.match(urlPattern);
  return urlMatches && urlMatches.length === 1 && urlMatches[0] === content;
});

// 获取纯URL消息的URL
const getPureUrl = computed(() => {
  if (!isPureUrlMessage.value) return '';
  const content = props.message.content?.trim() || '';
  const match = content.match(urlPattern);
  return match ? match[0] : '';
});

// 解析 LINK 类型消息
const linkInfo = computed(() => {
  if (props.message.type !== 'LINK') return null;
  try {
    const content = typeof props.message.content === 'string'
      ? JSON.parse(props.message.content)
      : props.message.content;
    return {
      url: content.url || props.message.content,
      title: content.title || '链接预览',
      description: content.description || '',
      imageUrl: content.imageUrl || content.thumbnail || ''
    };
  } catch (e) {
    return { url: props.message.content, title: '链接预览', description: '', imageUrl: '' };
  }
});

// 解析 CARD 类型消息
const cardInfo = computed(() => {
  if (props.message.type !== 'CARD') return null;
  try {
    const content = typeof props.message.content === 'string'
      ? JSON.parse(props.message.content)
      : props.message.content;
    return {
      cardType: content.cardType || 'user',
      userId: content.userId || content.groupId,
      userName: content.userName || content.groupName || '未知',
      userAvatar: content.userAvatar || content.groupAvatar || '/avatars/default.png',
      department: content.department || (content.memberCount ? `群成员 ${content.memberCount} 人` : '')
    };
  } catch (e) {
    return null;
  }
});

// 解析 LOCATION 类型消息
const locationInfo = computed(() => {
  if (props.message.type !== 'LOCATION') return null;
  try {
    const content = typeof props.message.content === 'string'
      ? JSON.parse(props.message.content)
      : props.message.content;
    return {
      name: content.name || '位置',
      address: content.address || '',
      latitude: content.latitude || 0,
      longitude: content.longitude || 0
    };
  } catch (e) {
    return null;
  }
});

// 位置地图 URL（使用 OpenStreetMap 静态图）
const locationMapUrl = computed(() => {
  if (!locationInfo.value) return '';
  const { latitude, longitude } = locationInfo.value;
  return `https://static-maps.yandex.ru/1.x/?ll=${longitude},${latitude}&z=15&l=map&size=300,150`;
});

// 点击位置消息
const openLocation = () => {
  if (locationInfo.value) {
    // 打开地图查看位置
    const { latitude, longitude } = locationInfo.value;
    window.open(`https://maps.google.com/maps?q=${latitude},${longitude}`, '_blank');
  }
};

// 点击名片消息
const handleCardClick = () => {
  if (cardInfo.value) {
    // 根据名片类型跳转：用户详情或群聊
    if (cardInfo.value.cardType === 'group') {
      // 跳转到群聊
      window.dispatchEvent(new CustomEvent('main:navigate-chat', {
        detail: { type: 'group', id: cardInfo.value.userId }
      }));
    } else {
      // 跳转到用户详情
      window.dispatchEvent(new CustomEvent('main:navigate-contact', {
        detail: { id: cardInfo.value.userId }
      }));
    }
  }
};

// 解析 TEXT 消息中的 URL 和纯文本
const parsedTextParts = computed(() => {
  const content = props.message?.content || '';
  if (!content) return [];

  const parts = [];
  let lastIndex = 0;
  let match;

  while ((match = urlPattern.exec(content)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ text: content.slice(lastIndex, match.index), isLink: false });
    }
    const link = match[0];
    let core = link;
    let tail = '';
    const punct = link.match(/[.,!?;:)\]]+$/);
    if (punct) {
      core = link.substring(0, link.length - punct[0].length);
      tail = punct[0];
    }
    if (core) parts.push({ text: core, isLink: true });
    if (tail) parts.push({ text: tail, isLink: false });
    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < content.length) {
    parts.push({ text: content.slice(lastIndex), isLink: false });
  }

  return parts;
});

// 格式化时间
const formattedTime = computed(() => {
  if (!props.message.sendTime) return '';
  const d = new Date(props.message.sendTime);
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
});

// 检查是否可以撤回（发送后2分钟内）
const canRecall = computed(() => {
  if (!props.message.sendTime || !props.isMe) return false;
  const sendTime = new Date(props.message.sendTime).getTime();
  const now = Date.now();
  const diffMinutes = (now - sendTime) / (1000 * 60);
  return diffMinutes <= 2 && props.message.status !== 'recalled';
});

// 检查是否可以编辑（发送后5分钟内，仅文本消息）
const canEdit = computed(() => {
  if (!props.message.sendTime || !props.isMe) return false;
  if (props.message.type && props.message.type !== 'TEXT') return false;
  const sendTime = new Date(props.message.sendTime).getTime();
  const now = Date.now();
  const diffMinutes = (now - sendTime) / (1000 * 60);
  return diffMinutes <= 5 && props.message.status !== 'recalled';
});

// 复制文本
const copyText = () => {
  if (props.message.content) {
    navigator.clipboard.writeText(props.message.content);
    ElMessage.success('已复制');
  }
};

// 辅助方法
const handleImageError = (e) => {
  e.target.style.display = 'none';
};

const openLink = (url) => {
  if (url) window.open(url, '_blank');
};

// 处理文件点击（下载/预览）
const handleFileClick = () => {
  const fileUrl = props.message.fileUrl
  if (!fileUrl) {
    ElMessage.info('文件地址无效')
    return
  }
  window.open(fileUrl, '_blank')
};

// 打开视频播放器
const openVideoPlayer = () => {
  const fileUrl = props.message.fileUrl
  if (!fileUrl) {
    ElMessage.info('视频地址无效')
    return
  }
  window.open(fileUrl, '_blank')
};

// 格式化视频时长
const formatVideoDuration = (seconds) => {
  if (!seconds) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
};

const formatDisplayUrl = (url) => {
  try {
    const urlObj = new URL(url);
    return urlObj.host + (urlObj.pathname.length > 20 ? urlObj.pathname.substring(0, 20) + '...' : urlObj.pathname);
  } catch {
    return url.substring(0, 30);
  }
};
</script>

<style scoped>
.message-item {
  display: flex;
  flex-direction: column;
  padding: 0 var(--dt-spacing-xl);
  margin-bottom: var(--dt-spacing-md);
}

/* 连续消息压缩间距（同一人1分钟内：4px） */
.message-item.is-grouped {
  margin-top: -4px;
}

.system-notice {
  align-self: center;
  margin: var(--dt-spacing-lg) 0;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
}

.re-edit-link {
  color: var(--dt-brand-color);
  cursor: pointer;
  margin-left: var(--dt-spacing-xs);
  transition: color var(--dt-transition-fast);
}

.re-edit-link:hover {
  color: var(--dt-brand-hover);
}

.message-container {
  display: flex;
  align-items: flex-end;
  width: 100%;
}

.is-me .message-container {
  flex-direction: row-reverse;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: var(--dt-avatar-size-md);
  height: var(--dt-avatar-size-md);
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
}

.avatar-placeholder {
  width: var(--dt-avatar-size-md);
  flex-shrink: 0;
}

.message-content-wrapper {
  max-width: var(--dt-bubble-max-width, 70%);
  margin: 0 var(--dt-spacing-lg);
  display: flex;
  flex-direction: column;
}

.is-me .message-content-wrapper {
  align-items: flex-end;
}

.sender-name {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  margin-bottom: 4px;
  margin-left: 2px;
}

/* 引用回复预览 */
.quoted-message-preview {
  background-color: var(--dt-bg-hover);
  border-left: 2px solid var(--dt-brand-color);
  padding: 4px 8px;
  margin-bottom: 4px;
  border-radius: 0 var(--dt-radius-sm) var(--dt-radius-sm) 0;
  font-size: var(--dt-font-size-sm);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quoted-author {
  color: var(--dt-text-secondary);
  margin-right: 4px;
}

.quoted-content {
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
}

.bubble-and-actions {
  /* 外层定位容器：承载气泡行和绝对定位的操作条 */
  position: relative;
  width: 100%;
}

.bubble-row {
  display: flex;
  align-items: flex-end;
}

.is-me .bubble-row {
  flex-direction: row-reverse;
}

/* 核心气泡规范 */
.message-bubble {
  padding: var(--dt-bubble-padding-v) var(--dt-bubble-padding-h);
  font-size: var(--dt-font-size-base);
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  transition: box-shadow var(--dt-transition-fast), transform var(--dt-transition-fast);
}

/* 接收方 (左侧) - 白色气泡，钉钉风格：左上小尖，底右圆角 */
.is-other .message-bubble {
  background-color: var(--dt-bubble-left-bg);
  color: var(--dt-text-primary);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-bubble-radius-received);
  box-shadow: var(--dt-shadow-1);
  position: relative;
}
/* 左侧气泡左上小三角伪元素 */
.is-other .message-bubble::before {
  content: '';
  position: absolute;
  left: -5px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-right: 5px solid var(--dt-border-light);
}
.is-other .message-bubble::after {
  content: '';
  position: absolute;
  left: -4px;
  top: 11px;
  width: 0;
  height: 0;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  border-right: 4px solid var(--dt-bubble-left-bg);
}
.is-other .message-bubble:hover {
  box-shadow: var(--dt-shadow-2);
}

/* 发送方 (右侧) - 蓝色气泡，钉钉风格：右上小尖，底左圆角 */
.is-me .message-bubble {
  background-color: var(--dt-bubble-right-bg);
  color: var(--dt-text-white);
  border-radius: var(--dt-bubble-radius-sent);
  box-shadow: var(--dt-shadow-brand);
  position: relative;
}
/* 右侧气泡右上小三角伪元素 */
.is-me .message-bubble::before {
  content: '';
  position: absolute;
  right: -5px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid rgba(0, 0, 0, 0.1);
}
.is-me .message-bubble::after {
  content: '';
  position: absolute;
  right: -4px;
  top: 11px;
  width: 0;
  height: 0;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  border-left: 4px solid var(--dt-bubble-right-bg);
}
.is-me .message-bubble:hover {
  background-color: var(--dt-bubble-right-bg-hover);
}

.content-img {
  max-width: 280px;  /* 钉钉规范：图片最大280px */
  border-radius: var(--dt-radius-sm);
  display: block;
}

/* 视频消息 */
.video-content {
  position: relative;
  max-width: 320px;  /* 钉钉规范：视频最大320px */
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  cursor: pointer;
}

.content-video {
  width: 100%;
  max-height: 200px;
  display: block;
  background-color: var(--dt-bg-card);
}

.video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--dt-overlay-bg);
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.video-content:hover .video-overlay {
  opacity: 1;
}

.play-icon {
  font-size: var(--dt-font-size-2xl);
  color: var(--dt-text-white);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  font-size: 12px;
  color: var(--dt-text-white);
  background-color: var(--dt-overlay-bg);
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);
}

/* 文件消息 */
.file-content {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
}

.file-content .file-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 语音消息 */
.voice-content {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.voice-duration {
  font-size: 12px;
}

/* 名片消息 */
.card-content {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  min-width: 180px;
  max-width: 240px;
  padding: 8px 12px;
  background-color: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
}

.card-content:hover {
  background-color: var(--dt-bg-hover);
}

.card-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  overflow: hidden;
}

.card-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-dept {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 2px;
}

.card-tag {
  font-size: 10px;
  color: var(--dt-brand-color);
  background-color: var(--dt-brand-bg);
  padding: 2px 6px;
  border-radius: 2px;
  flex-shrink: 0;
}

/* 位置消息 */
.location-content {
  display: flex;
  flex-direction: column;
  min-width: 200px;
  max-width: 280px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  cursor: pointer;
}

.location-content:hover {
  background: var(--dt-bg-hover);
}

.location-map {
  position: relative;
  width: 100%;
  height: 120px;
  background: var(--dt-bg-body);
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.location-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  background: var(--dt-brand-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.location-icon {
  font-size: 20px;
  color: var(--dt-text-white);
}

.location-info {
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
}

.location-name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-address {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 链接卡片 */
.link-card {
  display: flex;
  flex-direction: column;
  max-width: 280px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--dt-border-light);
}

.link-card:hover {
  background: var(--dt-bg-hover);
}

.link-image {
  width: 100%;
  height: 120px;
  overflow: hidden;
  background: var(--dt-border-lighter);
}

.link-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.link-content {
  padding: 12px;
}

.link-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.link-url {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 文本中的链接 */
.text-link {
  color: var(--dt-brand-color);
  text-decoration: none;
  word-break: break-all;
}

.text-link:hover {
  text-decoration: underline;
}

/* 状态与已读侧边栏 - 钉钉规范：距气泡边缘4px */
.message-status-sidebar {
  display: flex;
  align-items: flex-end;
  margin-right: 4px;  /* 钉钉规范：4px间距 */
  padding-bottom: 2px;
  flex-shrink: 0;
}

.status-icon {
  font-size: 14px;
}

.status-icon.is-loading {
  color: var(--dt-text-tertiary);
  animation: rotate 2s linear infinite;
}

.status-icon.is-failed {
  color: var(--dt-error-color);
  cursor: pointer;
}

.read-status {
  font-size: 12px;
  white-space: nowrap;
}

.read-status.is-read {
  color: var(--dt-text-tertiary);
}

.read-status.is-unread {
  color: var(--dt-brand-color);
}

.edited-tag {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  white-space: nowrap;
}

/* 操作悬浮条 (Action Bar) - 钉钉规范：16x16图标，间距8px，距离气泡8px */
.action-bar {
  display: flex;
  align-items: center;
  background-color: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-float);
  padding: 4px;
  gap: 8px;  /* 钉钉规范：图标间距8px */
  position: absolute;
  top: -8px;  /* 钉钉规范：距气泡顶部8px */
  z-index: 10;
  opacity: 0;
  transform: translateY(4px) scale(0.95);
  transition: opacity var(--dt-transition-fast), transform var(--dt-transition-fast), box-shadow var(--dt-transition-fast);
  pointer-events: none;
}

.bubble-and-actions:hover .action-bar {
  opacity: 1;
  transform: translateY(0) scale(1);
  pointer-events: auto;
}

.is-me .action-bar {
  right: 0;
}

.is-other .action-bar {
  left: 0;
}

.action-item {
  width: 24px;  /* 钉钉规范：16x16图标，24px容器 */
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-sm);  /* 钉钉规范：4px圆角 */
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-fast);
}

.action-item:hover {
  background-color: var(--dt-bg-hover);
  color: var(--dt-text-primary);  /* 钉钉规范：悬停变深色 */
}

.action-item:active {
  transform: scale(0.95);
  background-color: var(--dt-brand-bg-dark);
}

@keyframes rotate {
  100% { transform: rotate(360deg); }
}
</style>
