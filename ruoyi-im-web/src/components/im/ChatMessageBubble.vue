<template>
  <div
    :class="[
      'message-item',
      isMe ? 'is-me' : 'is-other',
      { 'is-grouped': isGrouped, 'is-swipe-open': isSwipeOpen }
    ]"
  >
    <div v-if="showTime" class="time-divider">
      <span>{{ timelineLabel }}</span>
    </div>

    <div class="message-row">
      <!-- 多选模式下的 Checkbox -->
      <div v-if="isSelectionMode" class="selection-checkbox-wrapper" @click.stop="toggleSelect">
        <div class="selection-checkbox" :class="{ 'is-checked': isSelected }">
          <el-icon v-if="isSelected"><Check /></el-icon>
        </div>
      </div>

      <div v-if="!isGrouped" class="avatar-shell">
        <img :src="avatarSrc" class="avatar" alt="avatar">
      </div>
      <div v-else class="avatar-placeholder" />

      <div class="message-column">
        <div v-if="showSenderName" class="sender-line">
          {{ senderLabel }}
        </div>

        <div
          class="swipe-shell"
          @mouseenter="isHovered = true"
          @mouseleave="isHovered = false"
        >
          <button
            class="swipe-delete"
            type="button"
            :class="{ 'is-visible': isSwipeOpen }"
            @click.stop="emitDelete"
          >
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </button>

          <div
            class="swipe-track"
            :style="swipeTrackStyle"
            @click="handleTrackClick"
            @contextmenu.prevent="openContextMenu"
            @pointerdown="handlePointerDown"
            @pointermove="handlePointerMove"
            @pointerup="handlePointerUp"
            @pointercancel="handlePointerCancel"
          >
            <div v-if="quotedPreview" class="quoted-message-preview">
              <span class="quoted-author">{{ quotedPreview.author }}</span>
              <span class="quoted-content">{{ quotedPreview.content }}</span>
            </div>

            <div class="bubble-cluster">
              <div
                :class="[
                  'message-bubble',
                  `type-${messageType.toLowerCase()}`,
                  {
                    'is-pending': message.status === 'pending' || message.status === 'sending',
                    'is-gesture-active': menuVisible || isSwipeOpen
                  }
                ]"
              >
                <div v-if="messageType === 'TEXT' && !isPureUrlMessage" class="text-content">
                  <template v-for="(part, index) in parsedTextParts" :key="`part-${index}`">
                    <a
                      v-if="part.isLink"
                      :href="part.text"
                      target="_blank"
                      rel="noreferrer"
                      class="text-link"
                      @click.stop
                    >{{ part.text }}</a>
                    <span v-else>{{ part.text }}</span>
                  </template>
                </div>

                <div v-else-if="isPureUrlMessage" class="link-card is-inline" @click="openLink(pureUrl)">
                  <div v-if="urlMetadataLoading" class="link-loading">
                    <el-icon class="is-loading">
                      <Loading />
                    </el-icon>
                  </div>
                  <template v-else-if="urlMetadata">
                    <img
                      v-if="urlMetadata.imageUrl"
                      :src="urlMetadata.imageUrl"
                      class="link-image"
                      alt=""
                    >
                    <div class="link-content">
                      <div class="link-title">
                        {{ urlMetadata.title || pureUrl }}
                      </div>
                      <div v-if="urlMetadata.description" class="link-desc">
                        {{ urlMetadata.description }}
                      </div>
                      <div class="link-url">
                        {{ formatDisplayUrl(pureUrl) }}
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <div class="link-content">
                      <div class="link-title">
                        {{ pureUrl }}
                      </div>
                      <div class="link-url">
                        {{ formatDisplayUrl(pureUrl) }}
                      </div>
                    </div>
                  </template>
                </div>

                <div v-else-if="messageType === 'IMAGE'" class="image-content">
                  <div v-if="imageLoading" class="image-placeholder">
                    <el-icon class="is-loading">
                      <Loading />
                    </el-icon>
                  </div>
                  <el-image
                    :src="mediaUrl"
                    :preview-src-list="mediaUrl ? [mediaUrl] : []"
                    fit="cover"
                    class="content-img"
                    @load="imageLoading = false"
                    @error="imageLoading = false"
                  />
                </div>

                <div v-else-if="messageType === 'VIDEO'" class="video-content" @click="handleVideoClick">
                  <!-- 视频预览：优先使用 poster 属性 -->
                  <video 
                    ref="videoPreviewRef"
                    :src="mediaUrl" 
                    :poster="videoPoster"
                    class="content-video"
                    preload="metadata"
                    @loadedmetadata="onVideoMetadataLoaded"
                  />
                  <div class="video-overlay">
                    <div class="play-icon-wrapper">
                      <el-icon class="play-icon">
                        <VideoPlay />
                      </el-icon>
                    </div>
                  </div>
                  <div v-if="videoDuration" class="video-duration-tag">
                    <el-icon><VideoPlay /></el-icon>
                    <span>{{ formatVideoDuration(videoDuration) }}</span>
                  </div>
                </div>

                <div v-else-if="messageType === 'FILE'" class="file-content" @click="handleFileClick">
                  <el-icon class="file-icon" :style="{ color: fileTypeColor }">
                    <Document />
                  </el-icon>
                  <div class="file-info">
                    <span class="file-name">{{ resolvedFileName }}</span>
                    <span v-if="resolvedFileSize" class="file-size">{{ formatReadableFileSize(resolvedFileSize) }}</span>
                  </div>
                  <div v-if="downloadProgress > 0 && downloadProgress < 100" class="file-download-progress">
                    <div class="file-download-fill" :style="{ width: `${downloadProgress}%` }" />
                  </div>
                </div>

                <VoiceMessageBubble v-else-if="messageType === 'VOICE' || messageType === 'AUDIO'" :message="message" />

                <div v-else-if="messageType === 'LINK' && linkInfo" class="link-card" @click="openLink(linkInfo.url)">
                  <div v-if="linkInfo.imageUrl" class="link-image">
                    <img :src="linkInfo.imageUrl" alt="" @error="handleImageError">
                  </div>
                  <div class="link-content">
                    <div class="link-title">
                      {{ linkInfo.title }}
                    </div>
                    <div v-if="linkInfo.description" class="link-desc">
                      {{ linkInfo.description }}
                    </div>
                    <div class="link-url">
                      {{ formatDisplayUrl(linkInfo.url) }}
                    </div>
                  </div>
                </div>

                <div v-else-if="messageType === 'CARD' && cardInfo" class="card-bubble-item" @click="handleCardClick">
                  <div class="card-body">
                    <img :src="cardInfo.userAvatar || '/avatars/default.png'" class="card-img" alt="card">
                    <div class="card-meta">
                      <div class="card-title">{{ cardInfo.userName }}</div>
                      <div class="card-subtitle">{{ cardInfo.department || '点击查看详情' }}</div>
                    </div>
                  </div>
                  <div class="card-footer">
                    <span class="card-type-text">{{ cardInfo.cardType === 'group' ? '群组名片' : '个人名片' }}</span>
                    <el-icon><ArrowRight /></el-icon>
                  </div>
                </div>

                <div v-else-if="messageType === 'LOCATION' && locationInfo" class="location-content" @click="openLocation">
                  <div class="location-map">
                    <img :src="locationMapUrl" alt="位置" class="map-image">
                    <div class="location-overlay">
                      <el-icon class="location-icon">
                        <Location />
                      </el-icon>
                    </div>
                  </div>
                  <div class="location-info">
                    <div class="location-name">
                      {{ locationInfo.name }}
                    </div>
                    <div v-if="locationInfo.address" class="location-address">
                      {{ locationInfo.address }}
                    </div>
                  </div>
                </div>

                <div v-else class="fallback-content">
                  {{ fallbackPreview }}
                </div>
              </div>

              <div class="meta-line">
                <span class="meta-time">{{ messageTime }}</span>
                <span v-if="message.isEdited" class="meta-tag">已编辑</span>
                <span v-if="message.status === 'sending'" class="meta-status">
                  <el-icon class="status-icon is-loading"><Loading /></el-icon>
                  发送中
                </span>
                <span v-else-if="message.status === 'failed'" class="meta-status is-failed">
                  <el-icon class="status-icon"><WarningFilled /></el-icon>
                  发送失败
                </span>
                <button
                  v-else-if="isMe && message.isRead !== undefined"
                  class="read-status"
                  :class="message.isRead ? 'is-read' : 'is-unread'"
                  type="button"
                  @click.stop="$emit('read-detail', message.messageId)"
                >
                  {{ message.isRead ? '已读' : '未读' }}
                </button>
              </div>
            </div>

            <div class="action-bar">
              <button
                v-for="action in hoverActions"
                :key="action.key"
                class="action-item"
                type="button"
                :title="action.label"
                @click.stop="runAction(action.key)"
              >
                <el-icon><component :is="action.icon" /></el-icon>
              </button>
              <div class="reaction-strip">
                <button
                  v-for="emoji in quickReactions"
                  :key="emoji"
                  class="emoji-action"
                  type="button"
                  @click.stop="handleQuickReaction(emoji)"
                >
                  {{ emoji }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="reactionGroups.length" class="reaction-groups">
          <button
            v-for="reaction in reactionGroups"
            :key="reaction.emoji"
            class="reaction-chip"
            type="button"
            @click.stop="handleQuickReaction(reaction.emoji)"
          >
            <span>{{ reaction.emoji }}</span>
            <span>{{ reaction.count }}</span>
          </button>
        </div>

        <div v-if="message.status === 'recalled'" class="recall-notice">
          你撤回了一条消息
          <span v-if="canEdit" class="re-edit-link" @click="$emit('edit', message)">重新编辑</span>
        </div>
      </div>
    </div>

    <teleport to="body">
      <div v-if="menuVisible" class="message-menu-mask" @click="closeContextMenu" />
      <div v-if="menuVisible" class="message-context-menu" :style="contextMenuStyle">
        <!-- ... 菜单项 ... -->
      </div>

      <!-- 全屏视频播放器 -->
      <transition name="fade">
        <div v-if="showVideoPlayer" class="video-player-overlay" @click="closeVideoPlayer">
          <div class="video-player-container" @click.stop>
            <video 
              :src="mediaUrl" 
              controls 
              autoplay 
              class="full-video"
            />
            <el-icon class="video-close" @click="closeVideoPlayer"><Close /></el-icon>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="js">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineRound,
  Check,
  Delete,
  Document,
  DocumentCopy,
  Edit,
  Loading,
  Location,
  Position,
  RefreshLeft,
  Star,
  VideoPlay,
  WarningFilled
} from '@element-plus/icons-vue'
import VoiceMessageBubble from '@/components/Chat/VoiceMessageBubble.vue'
import { formatFileSize, formatMessagePreviewFromObject, parseMessageContent } from '@/utils/message'
import { parseUrlMetadata } from '@/api/im/urlMetadata'

const LONG_PRESS_DURATION = 420
const SWIPE_ACTION_WIDTH = 84
const SWIPE_OPEN_THRESHOLD = 42

const props = defineProps({
  message: { type: Object, required: true },
  isMe: { type: Boolean, default: false },
  isGrouped: { type: Boolean, default: false },
  showTime: { type: Boolean, default: false },
  quotedMessage: { type: Object, default: null },
  isSelectionMode: { type: Boolean, default: false },
  isSelected: { type: Boolean, default: false }
})

const emit = defineEmits([
  'reply', 'forward', 'recall', 'delete', 'favorite', 'read-detail', 'edit', 'reaction',
  'select-message', 'toggle-selection'
])

const isHovered = ref(false)
const showVideoPlayer = ref(false)
const videoPreviewRef = ref(null)

const videoPoster = computed(() => {
  return parsedPayload.value.posterUrl || parsedPayload.value.thumbnail || ''
})

const handleVideoClick = () => {
  if (props.isSelectionMode) {
    toggleSelect()
    return
  }
  showVideoPlayer.value = true
}

const closeVideoPlayer = () => {
  showVideoPlayer.value = false
}

const onVideoMetadataLoaded = (e) => {
  // 如果没有时长且元数据已加载，可以动态获取（作为兜底）
}

// URL metadata for inline link cards
const urlMetadata = ref(null)
const urlMetadataLoading = ref(false)
const urlMetadataCache = new Map()

const fetchUrlMetadata = async (url) => {
  if (!url || urlMetadataCache.has(url)) {
    urlMetadata.value = urlMetadataCache.get(url) || null
    return
  }
  urlMetadataLoading.value = true
  try {
    const res = await parseUrlMetadata(url)
    if (res.code === 200 && res.data) {
      urlMetadata.value = res.data
      urlMetadataCache.set(url, res.data)
    } else {
      urlMetadata.value = null
      urlMetadataCache.set(url, null)
    }
  } catch {
    urlMetadata.value = null
    urlMetadataCache.set(url, null)
  } finally {
    urlMetadataLoading.value = false
  }
}

let longPressTimer = null

// Watch for pure URL messages and fetch metadata
watch(isPureUrlMessage, (isPure) => {
  if (isPure && pureUrl.value) {
    urlMetadata.value = urlMetadataCache.get(pureUrl.value) || null
    if (!urlMetadataCache.has(pureUrl.value)) {
      fetchUrlMetadata(pureUrl.value)
    }
  } else {
    urlMetadata.value = null
  }
})

const messageType = computed(() => {
  return String(props.message.type || props.message.messageType || 'TEXT').toUpperCase()
})

const parsedPayload = computed(() => {
  const parsed = parseMessageContent({ ...props.message, type: messageType.value })
  return typeof parsed === 'object' && parsed !== null ? parsed : {}
})

const avatarSrc = computed(() => props.message.senderAvatar || '/avatars/me.png')
const senderLabel = computed(() => props.message.senderName || (props.isMe ? '我' : '未知用户'))
const showSenderName = computed(() => !props.isMe && !props.isGrouped && !!senderLabel.value)
const mediaUrl = computed(() => {
  return props.message.fileUrl || parsedPayload.value.fileUrl || parsedPayload.value.url || parsedPayload.value.imageUrl || ''
})
const resolvedFileName = computed(() => {
  return props.message.fileName || parsedPayload.value.fileName || parsedPayload.value.name || '文件'
})
const resolvedFileSize = computed(() => {
  return props.message.fileSize || parsedPayload.value.fileSize || parsedPayload.value.size || 0
})
const videoDuration = computed(() => props.message.duration || parsedPayload.value.duration || 0)
const fallbackPreview = computed(() => formatMessagePreviewFromObject(props.message))

const linkInfo = computed(() => {
  if (messageType.value !== 'LINK') return null
  return {
    url: parsedPayload.value.url || props.message.content || '',
    title: parsedPayload.value.title || '链接预览',
    description: parsedPayload.value.description || '',
    imageUrl: parsedPayload.value.imageUrl || parsedPayload.value.thumbnail || ''
  }
})

const cardInfo = computed(() => {
  if (messageType.value !== 'CARD') return null
  return {
    cardType: parsedPayload.value.cardType || 'user',
    userId: parsedPayload.value.userId || parsedPayload.value.groupId,
    userName: parsedPayload.value.userName || parsedPayload.value.groupName || '未知联系人',
    userAvatar: parsedPayload.value.userAvatar || parsedPayload.value.groupAvatar || '/avatars/me.png',
    department: parsedPayload.value.department || (parsedPayload.value.memberCount ? `群成员 ${parsedPayload.value.memberCount} 人` : '')
  }
})

const locationInfo = computed(() => {
  if (messageType.value !== 'LOCATION') return null
  return {
    name: parsedPayload.value.name || '位置',
    address: parsedPayload.value.address || '',
    latitude: Number(parsedPayload.value.latitude || 0),
    longitude: Number(parsedPayload.value.longitude || 0)
  }
})

const locationMapUrl = computed(() => {
  if (!locationInfo.value || !locationInfo.value.latitude || !locationInfo.value.longitude) return ''
  const { latitude, longitude } = locationInfo.value
  return `https://static-maps.yandex.ru/1.x/?ll=${longitude},${latitude}&z=15&l=map&size=300,150`
})

const urlPattern = /(https?:\/\/[^\s<]+[^<.,:;"')\]\s])/g
const textContent = computed(() => String(props.message.content || ''))

const isPureUrlMessage = computed(() => {
  if (messageType.value !== 'TEXT') return false
  const content = textContent.value.trim()
  const matches = content.match(urlPattern)
  return !!(matches && matches.length === 1 && matches[0] === content)
})

const pureUrl = computed(() => {
  const content = textContent.value.trim()
  return content.match(urlPattern)?.[0] || ''
})

const parsedTextParts = computed(() => {
  if (!textContent.value) return []
  const globalPattern = new RegExp(urlPattern)
  const parts = []
  let lastIndex = 0
  let match

  while ((match = globalPattern.exec(textContent.value)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ text: textContent.value.slice(lastIndex, match.index), isLink: false })
    }

    const rawLink = match[0]
    const punctuation = rawLink.match(/[.,!?;:)\]]+$/)
    const linkText = punctuation ? rawLink.slice(0, rawLink.length - punctuation[0].length) : rawLink

    if (linkText) {
      parts.push({ text: linkText, isLink: true })
    }
    if (punctuation) {
      parts.push({ text: punctuation[0], isLink: false })
    }

    lastIndex = match.index + rawLink.length
  }

  if (lastIndex < textContent.value.length) {
    parts.push({ text: textContent.value.slice(lastIndex), isLink: false })
  }

  return parts
})

const quotedPreview = computed(() => {
  if (!props.message.replyToMessageId || !props.quotedMessage) return null
  return {
    author: props.quotedMessage.senderName || '引用消息',
    content: formatMessagePreviewFromObject(props.quotedMessage)
  }
})

const messageTime = computed(() => formatTime(props.message.sendTime, false))
const timelineLabel = computed(() => formatTime(props.message.sendTime, true))

const canRecall = computed(() => {
  if (!props.isMe || !props.message.sendTime || props.message.status === 'recalled') return false
  const diffMinutes = (Date.now() - new Date(props.message.sendTime).getTime()) / 60000
  return diffMinutes <= 2
})

const canEdit = computed(() => {
  if (!props.isMe || !props.message.sendTime || props.message.status === 'recalled') return false
  if (messageType.value !== 'TEXT') return false
  const diffMinutes = (Date.now() - new Date(props.message.sendTime).getTime()) / 60000
  return diffMinutes <= 5
})

const fileTypeColor = computed(() => {
  const ext = resolvedFileName.value.split('.').pop()?.toLowerCase()
  const colorMap = {
    pdf: 'var(--dt-file-pdf)',
    doc: 'var(--dt-file-word)',
    docx: 'var(--dt-file-word)',
    xls: 'var(--dt-file-excel)',
    xlsx: 'var(--dt-file-excel)',
    ppt: 'var(--dt-file-ppt)',
    pptx: 'var(--dt-file-ppt)'
  }
  return colorMap[ext] || 'var(--dt-file-other)'
})

const reactionGroups = computed(() => {
  const groupMap = new Map();
  (props.message.reactions || []).forEach((reaction) => {
    if (!reaction?.emoji) return
    const current = groupMap.get(reaction.emoji) || { emoji: reaction.emoji, count: 0 }
    current.count += 1
    groupMap.set(reaction.emoji, current)
  })
  return Array.from(groupMap.values())
})

const hoverActions = computed(() => {
  const actions = [
    { key: 'reply', label: '回复', icon: ChatLineRound },
    { key: 'forward', label: '转发', icon: Position },
    { key: 'favorite', label: props.message.isFavorited ? '取消收藏' : '收藏', icon: Star }
  ]

  if (messageType.value === 'TEXT' && textContent.value.trim()) {
    actions.splice(1, 0, { key: 'copy', label: '复制', icon: DocumentCopy })
  }

  if (props.isMe && canEdit.value) {
    actions.push({ key: 'edit', label: '编辑', icon: Edit })
  }
  if (props.isMe && canRecall.value) {
    actions.push({ key: 'recall', label: '撤回', icon: RefreshLeft })
  }
  if (props.isMe) {
    actions.push({ key: 'delete', label: '删除', icon: Delete })
  }

  return actions.slice(0, 5)
})

const menuActions = computed(() => {
  const actions = [...hoverActions.value]
  if (props.isMe && props.message.isRead !== undefined) {
    actions.push({ key: 'read-detail', label: '查看已读', icon: Star })
  }
  // 加入多选菜单项
  actions.push({ key: 'selection', label: '多选', icon: Check })
  return actions
})

const isSwipeOpen = computed(() => swipeOffset.value <= -SWIPE_OPEN_THRESHOLD)
const swipeTrackStyle = computed(() => ({
  transform: `translateX(${swipeOffset.value}px)`
}))

const contextMenuStyle = computed(() => ({
  left: `${menuPosition.value.x}px`,
  top: `${menuPosition.value.y}px`
}))

const handleQuickReaction = (emoji) => {
  emit('reaction', { message: props.message, emoji })
}

const runMenuAction = (key) => {
  closeContextMenu()
  runAction(key)
}

const runAction = (key) => {
  switch (key) {
    case 'selection':
      emit('toggle-selection')
      break
    case 'reply':
      emit('reply', props.message)
      break
    case 'copy':
      copyText()
      break
    case 'forward':
      emit('forward', props.message)
      break
    case 'favorite':
      emit('favorite', props.message)
      break
    case 'recall':
      emit('recall', props.message)
      break
    case 'edit':
      emit('edit', props.message)
      break
    case 'delete':
      emitDelete()
      break
    case 'read-detail':
      emit('read-detail', props.message.messageId)
      break
  }
}

const emitDelete = () => {
  resetSwipe()
  emit('delete', props.message)
}

const handleTrackClick = () => {
  if (props.isSelectionMode) {
    toggleSelect()
    return
  }
  if (isSwipeOpen.value) {
    resetSwipe()
  }
}

const toggleSelect = () => {
  if (props.isSelectionMode) {
    emit('select-message', props.message)
  }
}

const openContextMenu = (event) => {
  const clientX = event.clientX || 0
  const clientY = event.clientY || 0
  positionContextMenu(clientX, clientY)
  menuVisible.value = true
  resetSwipe()
}

const closeContextMenu = () => {
  menuVisible.value = false
}

const positionContextMenu = (clientX, clientY) => {
  const estimatedWidth = 164
  const estimatedHeight = Math.max(48, menuActions.value.length * 40 + 12)
  const maxX = Math.max(12, window.innerWidth - estimatedWidth - 12)
  const maxY = Math.max(12, window.innerHeight - estimatedHeight - 12)
  menuPosition.value = {
    x: Math.min(Math.max(12, clientX), maxX),
    y: Math.min(Math.max(12, clientY), maxY)
  }
}

const clearLongPressTimer = () => {
  if (longPressTimer) {
    window.clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

const handlePointerDown = (event) => {
  if (event.pointerType === 'mouse' && event.button !== 0) return

  gestureState.value = {
    pointerId: event.pointerId,
    pointerType: event.pointerType || 'mouse',
    startX: event.clientX,
    startY: event.clientY,
    isSwiping: false
  }

  if (gestureState.value.pointerType !== 'mouse') {
    clearLongPressTimer()
    longPressTimer = window.setTimeout(() => {
      positionContextMenu(event.clientX, event.clientY)
      menuVisible.value = true
      gestureState.value = null
      resetSwipe()
    }, LONG_PRESS_DURATION)
  }
}

const handlePointerMove = (event) => {
  const currentGesture = gestureState.value
  if (!currentGesture || currentGesture.pointerId !== event.pointerId) return

  const deltaX = event.clientX - currentGesture.startX
  const deltaY = event.clientY - currentGesture.startY

  if (Math.abs(deltaY) > 10 && Math.abs(deltaY) > Math.abs(deltaX)) {
    clearLongPressTimer()
    return
  }

  if (currentGesture.pointerType !== 'mouse' && Math.abs(deltaX) > 10 && Math.abs(deltaX) > Math.abs(deltaY)) {
    clearLongPressTimer()
    currentGesture.isSwiping = true
    if (event.cancelable) {
      event.preventDefault()
    }

    if (deltaX <= 0) {
      swipeOffset.value = Math.max(deltaX, -SWIPE_ACTION_WIDTH)
    } else if (isSwipeOpen.value) {
      swipeOffset.value = Math.min(0, -SWIPE_ACTION_WIDTH + deltaX)
    }
  }
}

const finishSwipe = () => {
  swipeOffset.value = swipeOffset.value <= -SWIPE_OPEN_THRESHOLD ? -SWIPE_ACTION_WIDTH : 0
}

const handlePointerUp = (event) => {
  const currentGesture = gestureState.value
  if (!currentGesture || currentGesture.pointerId !== event.pointerId) return
  clearLongPressTimer()
  if (currentGesture.isSwiping) {
    finishSwipe()
  }
  gestureState.value = null
}

const handlePointerCancel = () => {
  clearLongPressTimer()
  if (gestureState.value?.isSwiping) {
    finishSwipe()
  }
  gestureState.value = null
}

const resetSwipe = () => {
  swipeOffset.value = 0
}

const copyText = async () => {
  if (!textContent.value) return
  try {
    await navigator.clipboard.writeText(textContent.value)
    ElMessage.success('已复制')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
}

const openLink = (url) => {
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

const handleFileClick = () => {
  if (!mediaUrl.value) {
    ElMessage.info('文件地址无效')
    return
  }

  const xhr = new XMLHttpRequest()
  xhr.open('GET', mediaUrl.value, true)
  xhr.responseType = 'blob'
  xhr.onprogress = (event) => {
    if (event.lengthComputable) {
      downloadProgress.value = Math.round((event.loaded / event.total) * 100)
    }
  }
  xhr.onload = () => {
    if (xhr.status !== 200) {
      downloadProgress.value = 0
      ElMessage.error('文件下载失败')
      return
    }
    downloadProgress.value = 100
    const blobUrl = window.URL.createObjectURL(xhr.response)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = resolvedFileName.value
    link.click()
    window.URL.revokeObjectURL(blobUrl)
    window.setTimeout(() => {
      downloadProgress.value = 0
    }, 900)
  }
  xhr.onerror = () => {
    downloadProgress.value = 0
    ElMessage.error('文件下载失败')
  }
  xhr.send()
}

const openVideoPlayer = () => {
  if (!mediaUrl.value) {
    ElMessage.info('视频地址无效')
    return
  }
  window.open(mediaUrl.value, '_blank', 'noopener,noreferrer')
}

const openLocation = () => {
  if (!locationInfo.value?.latitude || !locationInfo.value?.longitude) {
    ElMessage.info('位置信息无效')
    return
  }
  window.open(`https://maps.google.com/maps?q=${locationInfo.value.latitude},${locationInfo.value.longitude}`, '_blank', 'noopener,noreferrer')
}

const handleCardClick = () => {
  if (!cardInfo.value) return
  if (cardInfo.value.cardType === 'group') {
    window.dispatchEvent(new CustomEvent('main:navigate-chat', {
      detail: { type: 'group', id: cardInfo.value.userId }
    }))
    return
  }
  window.dispatchEvent(new CustomEvent('main:navigate-contact', {
    detail: { id: cardInfo.value.userId }
  }))
}

const formatDisplayUrl = (url) => {
  try {
    const urlObject = new URL(url)
    const shortPath = urlObject.pathname.length > 18 ? `${urlObject.pathname.slice(0, 18)}...` : urlObject.pathname
    return `${urlObject.host}${shortPath}`
  } catch {
    return url.slice(0, 28)
  }
}

const formatVideoDuration = (seconds) => {
  const total = Number(seconds || 0)
  const minutes = Math.floor(total / 60)
  const remainder = total % 60
  return `${minutes}:${String(remainder).padStart(2, '0')}`
}

const formatReadableFileSize = (bytes) => formatFileSize(Number(bytes || 0))

const formatTime = (value, showDate) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  if (!showDate) {
    return `${hours}:${minutes}`
  }

  const now = new Date()
  const sameYear = now.getFullYear() === date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return sameYear ? `${month}-${day} ${hours}:${minutes}` : `${date.getFullYear()}-${month}-${day} ${hours}:${minutes}`
}

onBeforeUnmount(() => {
  clearLongPressTimer()
})
</script>

<style scoped lang="scss">
// 消息进场动画：轻微上滑 + 缩放，增加灵动感
@keyframes message-appear {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  animation: message-appear 0.25s cubic-bezier(0.4, 0, 0.2, 1); // 全局消息入场动效

  &.is-grouped {
    margin-top: -6px;
    animation: none; // 连续消息不重复触发动画
  }
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 8px 0 4px;

  span {
    padding: 4px 12px;
    border-radius: var(--dt-radius-full);
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-lighter);
    color: var(--dt-text-tertiary);
    font-size: var(--dt-font-size-xs);
    line-height: 1;
    letter-spacing: 0.2px;
  }
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

// 多选 Checkbox 样式
.selection-checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 40px; // 与头像/气泡高度对齐
  margin-right: 4px;
  cursor: pointer;
  flex-shrink: 0;
}

.selection-checkbox {
  width: 18px;
  height: 18px;
  border: 1.5px solid var(--dt-border-light);
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-card);
  transition: all var(--dt-transition-fast);
  color: transparent;

  &.is-checked {
    background-color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    color: var(--dt-text-white);
  }

  .el-icon {
    font-size: 12px;
    font-weight: bold;
  }
}

.is-me .message-row {
  flex-direction: row-reverse;
}

.avatar-shell,
.avatar-placeholder {
  width: 36px;
  flex: 0 0 36px;
}

.avatar {
  width: 36px;
  height: 36px;
  display: block;
  object-fit: cover;
  border-radius: var(--dt-radius-md); // 钉钉风格：稍微圆润的直角
  background: var(--dt-bg-card);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: transform var(--dt-transition-fast);

  &:hover {
    transform: scale(1.05);
  }
}

.message-column {
  min-width: 0;
  max-width: min(70%, 720px);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.is-me .message-column {
  align-items: flex-end;
}

.sender-line {
  margin: 0 2px 6px;
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-xs);
  line-height: 1.2;
}

.swipe-shell {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: var(--dt-radius-xl); // 增加外层切圆角，防止滑动溢出
}

.swipe-delete {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  width: 84px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: none;
  color: var(--dt-text-white);
  background: var(--dt-error-color);
  opacity: 0;
  transition: opacity var(--dt-transition-fast);

  &.is-visible {
    opacity: 1;
  }

  .el-icon {
    font-size: 18px;
  }

  span {
    font-size: var(--dt-font-size-sm);
    font-weight: 500;
  }
}

.swipe-track {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.2s cubic-bezier(0.18, 0.89, 0.32, 1.28); // 增加回弹效果
  touch-action: pan-y;
}

.bubble-cluster {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-bubble {
  position: relative;
  max-width: fit-content;
  min-height: 40px;
  padding: var(--dt-bubble-padding-v) var(--dt-bubble-padding-h);
  border-radius: var(--dt-radius-lg);
  line-height: 1.5;
  font-size: 14px;
  word-break: break-word;
  white-space: pre-wrap;
  transition: transform var(--dt-transition-fast), background-color var(--dt-transition-fast), box-shadow var(--dt-transition-fast);
  cursor: pointer;

  // 物理触感反馈：模拟按压感
  &:active {
    transform: scale(0.985);
  }

  &.is-pending {
    opacity: 0.72;
    filter: grayscale(0.2);
  }

  &.is-gesture-active {
    transform: scale(0.98);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
  }
}

// 接收方气泡：简洁风格
.is-other .message-bubble {
  color: var(--dt-text-primary);
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-bubble-radius-received);
  box-shadow: var(--dt-shadow-1);

  &:hover {
    background: var(--dt-bg-card);
    box-shadow: var(--dt-shadow-2);
  }
}

// 发送方气泡：品牌渐变 + 内发光
.is-me .message-bubble {
  color: var(--dt-text-white);
  background: var(--dt-brand-gradient);
  border-radius: var(--dt-bubble-radius-sent);
  box-shadow: 0 2px 6px rgba(39, 126, 251, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.12); // 内发光增强质感

  &:hover {
    filter: brightness(1.05);
    box-shadow: 0 4px 12px rgba(39, 126, 251, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.15);
  }
}

.quoted-message-preview {
  max-width: 100%;
  padding: 8px 12px;
  border-left: 3px solid var(--dt-brand-color);
  border-radius: var(--dt-radius-md);
  background: rgba(39, 126, 251, 0.05);
  color: var(--dt-text-secondary);
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 4px;
}

.quoted-author {
  margin-right: 6px;
  color: var(--dt-text-primary);
  font-weight: 600;
}

.quoted-content {
  color: var(--dt-text-secondary);
}

.text-content {
  color: inherit;
  font-family: var(--dt-font-family);
  letter-spacing: 0.1px;
}

.text-link {
  color: inherit;
  font-weight: 600;
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 2px;
  text-decoration-color: rgba(255, 255, 255, 0.4);

  .is-other & {
    color: var(--dt-brand-color);
    text-decoration-color: rgba(39, 126, 251, 0.2);
    
    &:hover {
      text-decoration-color: var(--dt-brand-color);
    }
  }
}

.content-img {
  min-width: 120px;
  max-width: min(320px, 100%);
  max-height: 400px;
  border-radius: var(--dt-radius-lg);
  display: block;
  object-fit: cover;
  transition: transform var(--dt-transition-base);

  &:hover {
    transform: scale(1.01);
  }
}

.image-content {
  position: relative;
  line-height: 0; // 消除图片下方间隙
}

.image-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-placeholder);
}

.video-content,
.location-content,
.link-card,
.file-content,
.card-content {
  overflow: hidden;
  border-radius: var(--dt-radius-lg);
}

.video-content {
  position: relative;
  min-width: 200px;
  max-width: 320px;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  cursor: pointer;
  background: #000;
  line-height: 0;

  &:hover {
    .play-icon-wrapper {
      transform: scale(1.1);
      background: rgba(255, 255, 255, 1);
    }
    .video-overlay {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

.content-video {
  width: 100%;
  max-height: 240px;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.1);
  transition: background var(--dt-transition-fast);
}

.play-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all var(--dt-transition-fast);

  .play-icon {
    font-size: 24px;
    color: var(--dt-brand-color);
  }
}

.video-duration-tag {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 2px 8px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: var(--dt-radius-full);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon { font-size: 10px; }
}

// 全屏播放器样式
.video-player-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player-container {
  position: relative;
  width: 90%;
  height: 85%;
  max-width: 1200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.full-video {
  max-width: 100%;
  max-height: 100%;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  border-radius: var(--dt-radius-lg);
}

.video-close {
  position: absolute;
  top: -40px;
  right: -40px;
  font-size: 32px;
  color: #fff;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.2s;

  &:hover { opacity: 1; }
}

@media (max-width: 768px) {
  .video-close {
    top: 10px;
    right: 10px;
  }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.file-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
  max-width: 320px;
  padding: 12px 14px;
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.is-other .file-content,
.is-other .link-card,
.is-other .card-content,
.is-other .location-content {
  background: #fdfdfe;
  border: 1px solid var(--dt-border-light);
  
  &:hover {
    background: #f8fbff;
    border-color: var(--dt-brand-light);
  }
}

.is-me .file-content,
.is-me .link-card,
.is-me .card-content,
.is-me .location-content {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  
  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.file-icon {
  font-size: 38px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.05));
}

.file-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  @include text-ellipsis;
  font-size: 14px;
  font-weight: 500;
}

.is-me .file-name,
.is-me .file-size,
.is-me .link-title,
.is-me .link-desc,
.is-me .link-url,
.is-me .card-name,
.is-me .card-dept,
.is-me .location-name,
.is-me .location-address {
  color: var(--dt-text-white);
}

.file-size {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  .is-me & { opacity: 0.8; }
}

.file-download-progress {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: rgba(0, 0, 0, 0.05);
}

.file-download-fill {
  height: 100%;
  background: var(--dt-brand-color);
  .is-me & { background: var(--dt-text-white); }
}

.link-card {
  width: min(280px, 100%);
  cursor: pointer;
  box-shadow: var(--dt-shadow-1);

  &.is-inline {
    width: auto;
  }
}

.link-image {
  width: 100%;
  height: 120px;
  background: var(--dt-bg-placeholder);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.link-content {
  padding: 10px 12px;
}

.link-desc {
  font-size: 12px;
  color: var(--dt-text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.link-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60px;
  color: var(--dt-text-tertiary);
}

.link-title,
.card-name,
.location-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.link-url {
  margin-top: 6px;
  font-size: 11px;
  color: var(--dt-text-tertiary);
  opacity: 0.8;
}

.card-bubble-item {
  width: 240px;
  background: #ffffff;
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  box-shadow: var(--dt-shadow-1);

  &:hover {
    box-shadow: var(--dt-shadow-2);
    border-color: var(--dt-brand-light);
  }

  &:active {
    transform: scale(0.98);
  }
}

.card-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
}

.card-img {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-md);
  object-fit: cover;
  border: 1px solid var(--dt-border-lighter);
}

.card-meta {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  @include text-ellipsis;
}

.card-subtitle {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-top: 2px;
  @include text-ellipsis;
}

.card-footer {
  height: 28px;
  padding: 0 12px;
  background: #fdfdfe;
  border-top: 1px solid var(--dt-border-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .card-type-text {
    font-size: 10px;
    color: var(--dt-text-quaternary);
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }
  
  .el-icon {
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }
}

.is-me .card-bubble-item {
  // 发送方的名片也要保持白色背景，钉钉逻辑
  color: var(--dt-text-primary);
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
  width: 32px;
  height: 32px;
  @include flex-center;
  border-radius: var(--dt-radius-full);
  background: var(--dt-brand-color);
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 8px rgba(39, 126, 251, 0.3);
}

.location-icon {
  font-size: 18px;
  color: var(--dt-text-white);
}

.location-info {
  padding: 10px 12px;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 16px;
  padding: 0 4px;
  color: var(--dt-text-tertiary);
  font-size: 11px;
  margin-top: 2px;
}

.is-me .meta-line {
  justify-content: flex-end;
}

.meta-time {
  opacity: 0.8;
}

.meta-tag {
  background: var(--dt-bg-placeholder);
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 10px;
}

.meta-status {
  display: inline-flex;
  align-items: center;
  gap: 3px;

  &.is-failed {
    color: var(--dt-error-color);
  }
}

.read-status {
  @include button-reset;
  color: var(--dt-text-tertiary);
  transition: color var(--dt-transition-fast);

  &.is-unread {
    color: var(--dt-brand-color);
    font-weight: 600;
  }

  &:hover {
    color: var(--dt-brand-hover);
  }
}

.action-bar {
  position: absolute;
  top: -42px;
  display: none;
  align-items: center;
  gap: 4px;
  padding: 4px 6px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  box-shadow: var(--dt-shadow-3);
  z-index: 10;
}

.is-me .action-bar { right: 0; }
.is-other .action-bar { left: 0; }

.action-item {
  width: 28px;
  height: 28px;
  @include flex-center;
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }
}

.reaction-strip {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding-left: 4px;
  margin-left: 4px;
  border-left: 1px solid var(--dt-border-lighter);
}

.emoji-action {
  width: 26px;
  height: 26px;
  @include flex-center;
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  transition: all var(--dt-transition-fast);

  &:hover {
    transform: scale(1.2) translateY(-2px);
    background: var(--dt-brand-bg);
  }
}

.reaction-groups {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.reaction-chip {
  @include button-reset;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: var(--dt-radius-full);
  color: var(--dt-text-secondary);
  background: #ffffff;
  border: 1px solid var(--dt-border-lighter);
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-bg);
    border-color: var(--dt-brand-light);
  }

  span:last-child {
    font-size: 11px;
    font-weight: 600;
  }
}

.recall-notice {
  margin-top: 6px;
  padding: 4px 12px;
  color: var(--dt-text-tertiary);
  font-size: 12px;
  text-align: center;
  width: 100%;
}

.re-edit-link {
  margin-left: 6px;
  color: var(--dt-brand-color);
  font-weight: 500;
  cursor: pointer;
  &:hover { text-decoration: underline; }
}

.message-context-menu {
  position: fixed;
  min-width: 160px;
  padding: 4px;
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  box-shadow: var(--dt-shadow-4);
  z-index: 2000;
}

.menu-item {
  @include button-reset;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-primary);
  font-size: 13px;
  transition: all var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }

  .el-icon { font-size: 16px; opacity: 0.7; }
}

// 触摸优化：移动端/平板禁用 hover 动画
@media (hover: none) {
  .message-bubble:active {
    transform: scale(0.96);
    background-color: var(--dt-bg-hover);
  }
}
</style>
