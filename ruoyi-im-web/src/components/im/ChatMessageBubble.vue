<template>
  <div
    class="slack-message-row"
    :class="{ 'is-consecutive': isGrouped, 'is-selected': isSelected }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 左侧：多选 Checkbox (悬浮或多选模式下显示) -->
    <div v-if="isSelectionMode || isSelected" class="slack-checkbox-zone" @click.stop="$emit('toggle-selection', message)">
      <div class="checkbox" :class="{ checked: isSelected }">
        <el-icon v-if="isSelected"><Check /></el-icon>
      </div>
    </div>

    <!-- 1. 头像区 (连续消息隐藏头像) -->
    <div class="slack-avatar-zone" v-else>
      <img v-if="!isGrouped" :src="avatarSrc" class="avatar" @click="$emit('show-user', message.senderId)">
      <div v-else class="time-hover">{{ hoverTime }}</div>
    </div>

    <!-- 2. 消息主干 -->
    <div class="slack-content-zone">
      <!-- 发送者与时间 (仅非连续消息显示) -->
      <div v-if="!isGrouped" class="slack-meta-row">
        <span class="sender-name">{{ senderLabel }}</span>
        <span class="timestamp">{{ timelineLabel }}</span>
      </div>

      <!-- 消息载体 (彻底废弃气泡，采用原生文档流) -->
      <div class="slack-body">
        <!-- 文本 -->
        <div v-if="messageType === 'TEXT'" class="rich-text-content">
          {{ message.content }}
        </div>

        <!-- 图片 -->
        <div v-else-if="messageType === 'IMAGE'" class="media-content">
          <el-image :src="mediaUrl" :preview-src-list="[mediaUrl]" fit="cover" class="media-img" lazy />
        </div>

        <!-- 视频 -->
        <div v-else-if="messageType === 'VIDEO'" class="media-content video" @click="handleVideoClick">
          <video :src="mediaUrl" class="media-video" />
          <div class="play-overlay"><el-icon><CaretRight /></el-icon></div>
        </div>

        <!-- 文件 (Slack风格卡片) -->
        <div v-else-if="messageType === 'FILE'" class="file-card" @click="handleFileClick">
          <div class="file-icon" :style="{ background: '#f0f4f8', color: fileTypeColor }"><el-icon><Document /></el-icon></div>
          <div class="file-info">
            <div class="f-name">{{ resolvedFileName }}</div>
            <div class="f-meta">{{ formatReadableFileSize(resolvedFileSize) }} · <span>点击下载</span></div>
          </div>
        </div>

        <!-- 名片 -->
        <div v-else-if="messageType === 'CARD'" class="contact-card" @click="handleCardClick">
          <img :src="cardInfo.userAvatar || '/avatars/default.png'" class="c-avatar">
          <div class="c-info">
            <div class="c-name">{{ cardInfo.userName }}</div>
            <div class="c-desc">{{ cardInfo.department || '名片' }}</div>
          </div>
          <el-button size="small" plain>发消息</el-button>
        </div>

        <!-- 位置 -->
        <div v-else-if="messageType === 'LOCATION'" class="location-card" @click="handleLocationClick">
          <div class="location-map">
            <img v-if="locationInfo.staticMapUrl" :src="locationInfo.staticMapUrl" class="map-img" />
            <div v-else class="map-placeholder">
              <el-icon><Location /></el-icon>
            </div>
          </div>
          <div class="location-info">
            <div class="l-title">{{ locationInfo.title || '位置' }}</div>
            <div class="l-address">{{ locationInfo.address || '' }}</div>
          </div>
        </div>
      </div>

      <!-- 底部：表态区 (Reactions) -->
      <div class="slack-reactions" v-if="reactions.length > 0">
        <div v-for="r in reactions" :key="r.emoji" class="reaction-pill" :class="{ 'i-reacted': r.hasMe }">
          <span class="emoji">{{ r.emoji }}</span>
          <span class="count">{{ r.count }}</span>
        </div>
      </div>
    </div>

    <!-- 3. 右上角悬浮工具栏 (Floating Action Bar) -->
    <transition name="fade-fast">
      <div v-if="isHovered && !isSelectionMode" class="slack-actions-bar">
        <button class="s-btn" title="添加回应"><el-icon><Smile /></el-icon></button>
        <button class="s-btn" title="在话题中回复"><el-icon><ChatDotSquare /></el-icon></button>
        <button class="s-btn" title="转发"><el-icon><Share /></el-icon></button>
        <button class="s-btn" title="更多"><el-icon><MoreFilled /></el-icon></button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import {
  Check, Document, CaretRight, CircleCheck, ChatDotSquare, Share, MoreFilled, Location
} from '@element-plus/icons-vue'

const props = defineProps({
  message: Object, isMe: Boolean, isGrouped: Boolean, showTime: Boolean, isSelectionMode: Boolean, isSelected: Boolean
})

const emit = defineEmits(['reply', 'forward', 'delete', 'reaction', 'read-detail', 'select-message', 'toggle-selection', 'show-user'])

const isHovered = ref(false)
const messageType = computed(() => (props.message.messageType || 'TEXT').toUpperCase())
const mediaUrl = computed(() => props.message.content || '')

const timelineLabel = computed(() => {
  if (!props.message.sendTime) return ''
  const d = new Date(props.message.sendTime)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})
const hoverTime = computed(() => timelineLabel.value)

const avatarSrc = computed(() => props.message.senderAvatar || '/avatars/default.png')
const senderLabel = computed(() => props.message.senderName || '成员')

const cardInfo = computed(() => props.message.payload || {})
const locationInfo = computed(() => {
  const payload = props.message.payload || {}
  return {
    title: payload.title || '位置',
    address: payload.address || '',
    latitude: payload.latitude,
    longitude: payload.longitude,
    staticMapUrl: payload.staticMapUrl || ''
  }
})
const resolvedFileName = computed(() => props.message.fileName || '未知文件')
const resolvedFileSize = computed(() => props.message.fileSize || 0)
const formatReadableFileSize = (s) => (s / 1024 / 1024).toFixed(1) + ' MB'
const fileTypeColor = computed(() => '#277efb') // 可根据扩展名动态变化

// 模拟 Reactions 数据
const reactions = ref([])

// 位置点击处理（可扩展为打开地图）
const handleLocationClick = () => {
  const { latitude, longitude } = locationInfo.value
  if (latitude && longitude) {
    const mapUrl = `https://maps.google.com/?q=${latitude},${longitude}`
    window.open(mapUrl, '_blank')
  }
}

// 名片点击处理
const handleCardClick = () => {
  const { userId } = cardInfo.value
  if (userId) {
    // 打开用户资料对话框
  }
}

// 文件消息点击处理
const handleFileClick = () => {
  const fileUrl = mediaUrl.value
  if (fileUrl) {
    window.open(fileUrl, '_blank')
  }
}

// 视频消息点击处理
const handleVideoClick = () => {
  const videoUrl = mediaUrl.value
  if (videoUrl) {
    window.open(videoUrl, '_blank')
  }
}
</script>

<style scoped lang="scss">
/* 核心架构：Slack/飞书 统一左对齐流式布局 */
.slack-message-row {
  display: flex;
  align-items: flex-start;
  padding: 6px 20px;
  position: relative;
  transition: background-color 0.15s;
  width: 100%;
  
  &:hover {
    background-color: #f8f9fa; // 极淡的灰底色，标志着视线聚焦
    
    .time-hover { opacity: 1; }
  }

  &.is-selected {
    background-color: #f0f5ff;
  }
}

/* 连续消息的挤压（隐藏头像，只显示时间） */
.slack-message-row.is-consecutive {
  padding-top: 2px;
  padding-bottom: 2px;
}

/* 1. 头像与复选区 (严格对齐轴线) */
.slack-checkbox-zone, .slack-avatar-zone {
  width: 44px; // 容纳 36px 头像 + 8px 间距
  flex-shrink: 0;
  display: flex;
  justify-content: flex-start;
  margin-top: 2px;
}

.slack-checkbox-zone {
  cursor: pointer;
  .checkbox {
    width: 18px; height: 18px; border: 1.5px solid #dcdfe6; border-radius: 4px;
    @include flex-center; color: transparent; transition: 0.2s;
    &.checked { background: var(--dt-brand-color); border-color: var(--dt-brand-color); color: #fff; }
  }
}

.avatar {
  width: 36px; height: 36px; border-radius: 4px; object-fit: cover; cursor: pointer;
  border: 1px solid rgba(0,0,0,0.05); // 防止纯白头像融入背景
}

.time-hover {
  font-size: 10px; color: #aaa; opacity: 0; transition: opacity 0.2s;
  width: 36px; text-align: center; line-height: 20px; user-select: none;
}

/* 2. 消息主干 (无气泡束缚) */
.slack-content-zone {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.slack-meta-row {
  display: flex; align-items: baseline; gap: 8px; margin-bottom: 2px; line-height: 1.2;
  .sender-name { font-size: 15px; font-weight: 700; color: #1d1d1f; }
  .timestamp { font-size: 12px; color: #86868b; font-weight: 400; }
}

.slack-body {
  font-size: 15px;
  line-height: 1.6;
  color: #333; // 极度舒适的阅读黑
  word-break: break-word;
  white-space: pre-wrap;
}

/* 富文本排版 (Markdown支持预留) */
.rich-text-content {
  margin-top: 2px;
}

/* 多媒体块 */
.media-content {
  margin-top: 6px; border-radius: 8px; overflow: hidden; max-width: 360px;
  border: 1px solid rgba(0,0,0,0.06); background: #f8f9fa;
  .media-img { display: block; width: 100%; }
}

.media-content.video {
  position: relative; cursor: pointer;
  .media-video { width: 100%; display: block; }
  .play-overlay { position: absolute; inset: 0; @include flex-center; background: rgba(0,0,0,0.2); transition: 0.2s;
    .el-icon { font-size: 40px; color: #fff; opacity: 0.9; }
    &:hover { background: rgba(0,0,0,0.3); .el-icon { transform: scale(1.1); } }
  }
}

/* 极简文件卡片 */
.file-card {
  display: flex; align-items: center; gap: 12px; padding: 10px 14px; margin-top: 6px;
  background: #fff; border: 1px solid #e3e5e8; border-radius: 8px; max-width: 320px; cursor: pointer;
  transition: 0.2s; &:hover { border-color: #bac2cd; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
  .file-icon { width: 36px; height: 36px; border-radius: 6px; @include flex-center; font-size: 20px; }
  .file-info { flex: 1; min-width: 0; .f-name { font-size: 14px; font-weight: 600; @include text-ellipsis; color: #1d1d1f; } .f-meta { font-size: 12px; color: #86868b; margin-top: 2px; span { color: var(--dt-brand-color); } } }
}

/* 联系人名片 */
.contact-card {
  display: flex; align-items: center; gap: 12px; padding: 12px; margin-top: 6px;
  background: #fff; border: 1px solid #e3e5e8; border-radius: 8px; max-width: 280px;
  .c-avatar { width: 40px; height: 40px; border-radius: 6px; }
  .c-info { flex: 1; min-width: 0; .c-name { font-size: 14px; font-weight: 600; color: #1d1d1f; } .c-desc { font-size: 12px; color: #86868b; } }
}

/* 位置消息卡片 */
.location-card {
  display: flex; flex-direction: column; gap: 0; margin-top: 6px;
  background: #fff; border: 1px solid #e3e5e8; border-radius: 8px; max-width: 280px; overflow: hidden;
  cursor: pointer;
  transition: 0.2s;
  &:hover { border-color: #bac2cd; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
  .location-map {
    width: 100%; height: 120px; background: #f0f4f8;
    .map-img { width: 100%; height: 100%; object-fit: cover; }
    .map-placeholder { width: 100%; height: 100%; @include flex-center; font-size: 32px; color: #86868b; }
  }
  .location-info {
    padding: 10px 12px;
    .l-title { font-size: 14px; font-weight: 600; color: #1d1d1f; }
    .l-address { font-size: 12px; color: #86868b; margin-top: 2px; @include text-ellipsis; }
  }
}

/* 3. 悬浮操作栏 (Slack/飞书绝对定位右上角) */
.slack-actions-bar {
  position: absolute; top: -16px; right: 20px;
  display: flex; align-items: center; padding: 2px;
  background: #fff; border: 1px solid #e3e5e8; border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08); z-index: 10;

  .s-btn {
    width: 28px; height: 28px; @include flex-center; border: none; background: transparent;
    color: #5f6368; border-radius: 4px; cursor: pointer; font-size: 16px; transition: 0.1s;
    &:hover { background: #f0f4f8; color: #1d1d1f; }
  }
}

.fade-fast-enter-active, .fade-fast-leave-active { transition: opacity 0.1s, transform 0.1s; }
.fade-fast-enter-from, .fade-fast-leave-to { opacity: 0; transform: translateY(4px); }
</style>
