/**
 * ChatPanel - 弹窗状态管理 Composable
 *
 * 职责：
 * - 各种弹窗的显示/隐藏状态管理
 * - 弹窗之间的联动逻辑
 */

import { ref, computed } from 'vue'

export function useChatDialogs() {
  // 语音/视频通话
  const showVoiceCall = ref(false)
  const showVideoCall = ref(false)
  const isIncomingCall = ref(false)

  // 搜索面板
  const showChatSearch = ref(false)

  // 文件面板
  const showFilesPanel = ref(false)

  // 聊天历史
  const showChatHistory = ref(false)

  // 群公告
  const showAnnouncementDialog = ref(false)

  // 导出聊天
  const showExportDialog = ref(false)

  // 群文件
  const showGroupFilesPanel = ref(false)

  // 合并转发详情
  const showCombineDetail = ref(false)
  const combineMessages = ref([])
  const combineConversationTitle = ref('')

  // 图片预览
  const showImagePreview = ref(false)
  const imagePreviewIndex = ref(0)

  // 当前联系人信息
  const showContactDetail = ref(false)
  const currentContactUser = ref(null)

  /**
   * 显示语音通话
   */
  const openVoiceCall = (incoming = false) => {
    isIncomingCall.value = incoming
    showVoiceCall.value = true
    showVideoCall.value = false
  }

  /**
   * 显示视频通话
   */
  const openVideoCall = (incoming = false) => {
    isIncomingCall.value = incoming
    showVideoCall.value = true
    showVoiceCall.value = false
  }

  /**
   * 关闭所有通话弹窗
   */
  const closeAllCalls = () => {
    showVoiceCall.value = false
    showVideoCall.value = false
    isIncomingCall.value = false
  }

  /**
   * 显示联系人详情
   */
  const openContactDetail = (user) => {
    currentContactUser.value = user
    showContactDetail.value = true
  }

  /**
   * 显示图片预览
   */
  const openImagePreview = (index, images = []) => {
    imagePreviewIndex.value = index
    if (images.length > 0) {
      // 可以在这里处理图片列表
    }
    showImagePreview.value = true
  }

  /**
   * 显示合并转发详情
   */
  const openCombineDetail = (messages, title) => {
    combineMessages.value = messages
    combineConversationTitle.value = title
    showCombineDetail.value = true
  }

  /**
   * 关闭所有弹窗
   */
  const closeAllDialogs = () => {
    closeAllCalls()
    showChatSearch.value = false
    showFilesPanel.value = false
    showChatHistory.value = false
    showAnnouncementDialog.value = false
    showExportDialog.value = false
    showGroupFilesPanel.value = false
    showCombineDetail.value = false
    showContactDetail.value = false
    showImagePreview.value = false
  }

  // 当前打开的弹窗数量
  const openDialogsCount = computed(() => {
    let count = 0
    if (showVoiceCall.value) count++
    if (showVideoCall.value) count++
    if (showChatSearch.value) count++
    if (showFilesPanel.value) count++
    if (showChatHistory.value) count++
    if (showAnnouncementDialog.value) count++
    if (showExportDialog.value) count++
    if (showGroupFilesPanel.value) count++
    if (showCombineDetail.value) count++
    if (showContactDetail.value) count++
    if (showImagePreview.value) count++
    return count
  })

  return {
    // 通话
    showVoiceCall,
    showVideoCall,
    isIncomingCall,
    openVoiceCall,
    openVideoCall,
    closeAllCalls,

    // 搜索
    showChatSearch,

    // 文件
    showFilesPanel,

    // 历史
    showChatHistory,

    // 公告
    showAnnouncementDialog,

    // 导出
    showExportDialog,

    // 群文件
    showGroupFilesPanel,

    // 合并转发
    showCombineDetail,
    combineMessages,
    combineConversationTitle,
    openCombineDetail,

    // 联系人详情
    showContactDetail,
    currentContactUser,
    openContactDetail,

    // 图片预览
    showImagePreview,
    imagePreviewIndex,
    openImagePreview,

    // 通用
    closeAllDialogs,
    openDialogsCount
  }
}
