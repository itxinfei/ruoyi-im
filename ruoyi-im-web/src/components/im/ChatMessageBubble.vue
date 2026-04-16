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
                  <template v-for="(part, index) in parsedTextParts" :key="index">
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

                <div v-else-if="messageType === 'VIDEO'" class="video-content" @click="openVideoPlayer">
                  <video :src="mediaUrl" class="content-video" />
                  <div class="video-overlay">
                    <el-icon class="play-icon">
                      <VideoPlay />
                    </el-icon>
                  </div>
                  <span v-if="videoDuration" class="video-duration">{{ formatVideoDuration(videoDuration) }}</span>
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

                <div v-else-if="messageType === 'CARD' && cardInfo" class="card-content" @click="handleCardClick">
                  <img :src="cardInfo.userAvatar" class="card-avatar" alt="card">
                  <div class="card-info">
                    <div class="card-name">
                      {{ cardInfo.userName }}
                    </div>
                    <div class="card-dept">
                      {{ cardInfo.department || '点击查看详情' }}
                    </div>
                  </div>
                  <div class="card-tag">
                    {{ cardInfo.cardType === 'group' ? '群聊名片' : '个人名片' }}
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
                  @click.stop="$emit('read-detail', message.messageId || message.id)"
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
        <button
          v-for="action in menuActions"
          :key="action.key"
          class="menu-item"
          type="button"
          @click.stop="runMenuAction(action.key)"
        >
          <el-icon><component :is="action.icon" /></el-icon>
          <span>{{ action.label }}</span>
        </button>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="js">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineRound,
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
  quotedMessage: { type: Object, default: null }
})

const emit = defineEmits(['reply', 'forward', 'recall', 'delete', 'favorite', 'read-detail', 'edit', 'reaction'])

const isHovered = ref(false)
const imageLoading = ref(true)
const downloadProgress = ref(0)
const swipeOffset = ref(0)
const menuVisible = ref(false)
const menuPosition = ref({ x: 0, y: 0 })
const gestureState = ref(null)
const quickReactions = ['👍', '❤️', '😂', '😮', '😢', '👏']

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
      emit('read-detail', props.message.messageId || props.message.id)
      break
  }
}

const emitDelete = () => {
  resetSwipe()
  emit('delete', props.message)
}

const handleTrackClick = () => {
  if (isSwipeOpen.value) {
    resetSwipe()
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
.message-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;

  &.is-grouped {
    margin-top: -6px;
  }
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 4px 0 2px;

  span {
    padding: 4px 12px;
    border-radius: var(--dt-radius-full);
    background: rgba(255, 255, 255, 0.78);
    border: 1px solid var(--dt-border-lighter);
    box-shadow: var(--dt-shadow-1);
    color: var(--dt-text-tertiary);
    font-size: var(--dt-font-size-sm);
    line-height: 1;
  }
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
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
  border-radius: var(--dt-radius-xl);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
  background: var(--dt-bg-card);
}

.message-column {
  min-width: 0;
  max-width: min(72%, 720px);
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
  font-size: var(--dt-font-size-sm);
  line-height: 1.2;
}

.swipe-shell {
  position: relative;
  width: 100%;
  overflow: hidden;
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
  }
}

.swipe-track {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 180ms ease;
  touch-action: pan-y;
}

.bubble-cluster {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-bubble {
  position: relative;
  max-width: 100%;
  min-height: 40px;
  padding: 10px 14px;
  border-radius: var(--dt-radius-2xl);
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
  white-space: pre-wrap;
  transition: transform var(--dt-transition-fast), box-shadow var(--dt-transition-fast), background-color var(--dt-transition-fast);

  &.is-pending {
    opacity: 0.72;
  }

  &.is-gesture-active {
    box-shadow: var(--dt-shadow-2);
  }
}

.is-other .message-bubble {
  color: var(--dt-text-primary);
  background: var(--dt-bubble-left-bg);
  border: 1px solid rgba(23, 26, 29, 0.06);
  border-top-left-radius: 8px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}

.is-me .message-bubble {
  color: var(--dt-text-white);
  background: var(--dt-brand-color);
  border-top-right-radius: 8px;
}

.quoted-message-preview {
  max-width: 100%;
  padding: 8px 12px;
  border-left: 3px solid var(--dt-brand-color);
  border-radius: var(--dt-radius-xl);
  background: rgba(39, 126, 251, 0.08);
  color: var(--dt-text-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.quoted-author {
  margin-right: 6px;
  color: var(--dt-text-primary);
  font-weight: 500;
}

.quoted-content {
  color: var(--dt-text-secondary);
}

.text-content {
  color: inherit;
}

.text-link {
  color: inherit;
  font-weight: 600;
  text-decoration: underline;
  text-decoration-color: rgba(255, 255, 255, 0.4);

  .is-other & {
    color: var(--dt-brand-color);
    text-decoration-color: rgba(39, 126, 251, 0.26);
  }
}

.content-img {
  min-width: 120px;
  max-width: min(320px, 100%);
  max-height: 400px;
  border-radius: var(--dt-radius-2xl);
  display: block;
  object-fit: cover;
}

.image-content {
  position: relative;
}

.image-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-2xl);
  color: var(--dt-text-tertiary);
  background: var(--dt-bg-placeholder);
}

.video-content,
.location-content,
.link-card,
.file-content,
.card-content {
  overflow: hidden;
  border-radius: var(--dt-radius-2xl);
}

.video-content {
  position: relative;
  min-width: 220px;
  max-width: 320px;
  cursor: pointer;
}

.content-video {
  width: 100%;
  display: block;
  max-height: 240px;
  background: rgba(23, 26, 29, 0.12);
}

.video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.16);
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.video-content:hover .video-overlay {
  opacity: 1;
}

.play-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-full);
  background: rgba(255, 255, 255, 0.88);
  color: var(--dt-text-primary);
}

.video-duration {
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 3px 7px;
  border-radius: var(--dt-radius-full);
  color: var(--dt-text-white);
  background: var(--dt-overlay-bg);
  font-size: 12px;
}

.file-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
  max-width: 320px;
  padding: 12px 14px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.08);
}

.is-other .file-content,
.is-other .link-card,
.is-other .card-content,
.is-other .location-content {
  background: #f8fafc;
  border: 1px solid rgba(23, 26, 29, 0.06);
}

.is-me .file-content,
.is-me .link-card,
.is-me .card-content,
.is-me .location-content {
  background: rgba(255, 255, 255, 0.14);
}

.file-icon {
  font-size: 34px;
  flex-shrink: 0;
}

.file-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
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
  font-size: 12px;
  color: var(--dt-text-secondary);
}

.file-download-progress {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: rgba(23, 26, 29, 0.08);
}

.file-download-fill {
  height: 100%;
  background: var(--dt-brand-color);
}

.link-card {
  width: min(280px, 100%);
  cursor: pointer;

  &.is-inline {
    width: auto;
  }
}

.link-image {
  width: 100%;
  height: 120px;
  background: var(--dt-border-lighter);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.link-content {
  padding: 12px 14px;
}

.link-desc {
  font-size: var(--dt-font-size-sm);
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
  min-height: 80px;
  color: var(--dt-text-tertiary);
}

.link-title,
.card-name,
.location-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.link-desc,
.card-dept,
.location-address {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--dt-text-secondary);
}

.link-url {
  margin-top: 8px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 220px;
  max-width: 280px;
  padding: 12px 14px;
  cursor: pointer;
}

.card-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-xl);
  object-fit: cover;
  flex-shrink: 0;
}

.card-info {
  min-width: 0;
  flex: 1;
}

.card-tag {
  flex-shrink: 0;
  padding: 4px 8px;
  border-radius: var(--dt-radius-full);
  font-size: 11px;
  color: var(--dt-brand-color);
  background: rgba(39, 126, 251, 0.12);
}

.location-map {
  position: relative;
  width: 100%;
  height: 132px;
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
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-full);
  background: var(--dt-brand-color);
  transform: translate(-50%, -50%);
}

.location-icon {
  font-size: 20px;
  color: var(--dt-text-white);
}

.location-info {
  padding: 12px 14px;
}

.fallback-content {
  min-width: 120px;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 16px;
  padding: 0 4px;
  color: var(--dt-text-tertiary);
  font-size: 11px;
}

.is-me .meta-line {
  justify-content: flex-end;
}

.is-other .meta-line {
  justify-content: flex-start;
}

.meta-time,
.meta-tag,
.meta-status,
.read-status {
  line-height: 1;
}

.meta-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &.is-failed {
    color: var(--dt-error-color);
  }
}

.status-icon {
  font-size: 12px;
}

.read-status {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  color: var(--dt-text-tertiary);

  &.is-unread {
    color: var(--dt-brand-color);
    font-weight: 600;
  }
}

.action-bar {
  position: absolute;
  top: -44px;
  display: none;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  box-shadow: var(--dt-shadow-2);
  z-index: 3;
}

.is-me .action-bar {
  right: 0;
}

.is-other .action-bar {
  left: 0;
}

.action-item,
.emoji-action,
.reaction-chip,
.menu-item {
  border: none;
  background: transparent;
  cursor: pointer;
  font: inherit;
}

.action-item {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-secondary);
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
    background: rgba(39, 126, 251, 0.08);
  }
}

.reaction-strip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding-left: 4px;
  border-left: 1px solid var(--dt-border-light);
}

.emoji-action {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  transition: background-color var(--dt-transition-fast), transform var(--dt-transition-fast);

  &:hover {
    transform: translateY(-1px);
    background: rgba(39, 126, 251, 0.08);
  }
}

.reaction-groups {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
  padding: 0 2px;
}

.reaction-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--dt-radius-full);
  color: var(--dt-text-secondary);
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(23, 26, 29, 0.06);
  box-shadow: var(--dt-shadow-1);
}

.recall-notice {
  margin-top: 4px;
  color: var(--dt-text-tertiary);
  font-size: 12px;
}

.re-edit-link {
  margin-left: 6px;
  color: var(--dt-brand-color);
  cursor: pointer;
}

.message-menu-mask {
  position: fixed;
  inset: 0;
  z-index: 1998;
  background: transparent;
}

.message-context-menu {
  position: fixed;
  min-width: 164px;
  padding: 6px;
  border-radius: var(--dt-radius-2xl);
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  box-shadow: var(--dt-shadow-2);
  z-index: 1999;
}

.menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-primary);
  text-align: left;
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-color);
    background: rgba(39, 126, 251, 0.08);
  }
}

@media (hover: hover) and (pointer: fine) {
  .swipe-shell:hover .action-bar {
    display: inline-flex;
  }
}

@media (max-width: 1280px) {
  .message-column {
    max-width: min(78%, 640px);
  }
}

@media (max-width: 768px) {
  .message-row {
    gap: 10px;
  }

  .message-column {
    max-width: calc(100% - 46px);
  }

  .message-bubble {
    padding: 10px 12px;
    border-radius: var(--dt-radius-2xl);
  }

  .content-img,
  .video-content {
    max-width: min(260px, 100%);
  }

  .file-content,
  .card-content,
  .link-card {
    min-width: 0;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .avatar-shell,
  .avatar-placeholder {
    width: 32px;
    flex-basis: 32px;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-md);
  }

  .message-row {
    gap: 8px;
  }

  .message-column {
    max-width: calc(100% - 40px);
  }

  .time-divider span {
    font-size: 11px;
  }

  .action-bar {
    display: none !important;
  }
}
</style>
