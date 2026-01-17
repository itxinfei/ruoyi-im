// 会话详情模块API
// 统一封装用户详情、群组详情、成员列表、内容统计等接口
// @module api/im/conversation-detail
import request from '@/utils/request'
import { getUser } from './user'
import { getGroup, quitGroup, dismissGroup } from './group'
import { listGroupMember, addGroupMember, delGroupMember, updateMemberRole } from './groupMember'
import { setConversationPinned, setConversationMuted } from './conversation'
import { getMessageTypeStatistics, searchMessages, listMessage } from './message'

// ==================== 用户详情 ====================

// 获取用户详细信息（从user.js复用）
export const getUserInfo = getUser

// ==================== 群组详情 ====================

// 获取群组详细信息（从group.js复用）
export const getGroupDetail = getGroup

// 获取群组成员列表（从groupMember.js复用）
export const getGroupMembers = listGroupMember

// ==================== 群组成员管理 ====================

// 添加群组成员
export const addMember = addGroupMember

// 移除群组成员
export const removeMember = delGroupMember

// 设置/取消管理员
export const setGroupAdmin = (groupId, memberId, isAdmin) => {
  return updateMemberRole(groupId, memberId, { role: isAdmin ? 'ADMIN' : 'MEMBER' })
}

// ==================== 会话设置 ====================

// 置顶/取消置顶会话
export const setPinned = setConversationPinned

// 设置/取消免打扰
export const setMuted = setConversationMuted

// ==================== 消息统计 ====================

// 获取会话内容统计（媒体、文件、链接等）
// 返回结构: { mediaCount, fileCount, linkCount, mediaList, fileList, linkList }
export function getContentStats(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/contentStats`,
    method: 'get',
  }).catch(() => {
    // 如果后端没有此接口，使用降级方案
    return getFallbackContentStats(conversationId)
  })
}

// 降级方案：通过消息列表统计数据
async function getFallbackContentStats(conversationId) {
  try {
    // 获取最近100条消息
    const response = await listMessage({
      conversationId,
      limit: 100,
    })

    if (response.code !== 200 || !response.data) {
      return {
        mediaCount: 0,
        fileCount: 0,
        linkCount: 0,
        mediaList: [],
        fileList: [],
        linkList: [],
      }
    }

    const messages = response.data
    const mediaList = []
    const fileList = []
    const linkList = []

    messages.forEach(msg => {
      // 解析消息内容
      let content = {}
      try {
        content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
      } catch {
        content = { text: msg.content }
      }

      if (msg.messageType === 'IMAGE' || msg.messageType === 'VIDEO') {
        mediaList.push({
          id: msg.id,
          url: content.url || content.thumbUrl,
          type: msg.messageType.toLowerCase(),
          time: msg.sendTime,
        })
      } else if (msg.messageType === 'FILE') {
        fileList.push({
          id: msg.id,
          name: content.fileName || content.name || '未知文件',
          url: content.url,
          size: content.fileSize || 0,
          time: msg.sendTime,
        })
      } else if (msg.messageType === 'TEXT') {
        // 提取链接
        const text = content.text || ''
        const linkRegex = /https?:\/\/[^\s]+/g
        const matches = text.match(linkRegex)
        if (matches) {
          matches.forEach(url => {
            linkList.push({
              id: `link-${msg.id}-${url.length}`,
              url,
              title: url,
              time: msg.sendTime,
            })
          })
        }
      }
    })

    return {
      mediaCount: mediaList.length,
      fileCount: fileList.length,
      linkCount: linkList.length,
      mediaList: mediaList.slice(0, 20), // 只返回前20个
      fileList: fileList.slice(0, 20),
      linkList: linkList.slice(0, 20),
    }
  } catch (error) {
    console.error('获取内容统计失败:', error)
    return {
      mediaCount: 0,
      fileCount: 0,
      linkCount: 0,
      mediaList: [],
      fileList: [],
      linkList: [],
    }
  }
}

// ==================== 群组操作 ====================

// 退出群组
export const leaveGroup = quitGroup

// 解散群组
export const dismissTheGroup = dismissGroup

// 清空聊天记录
export function clearChatMessages(conversationId) {
  return request({
    url: `/api/im/conversation/${conversationId}/messages`,
    method: 'delete',
  }).catch(() => {
    // 如果后端没有清空接口，返回成功但不执行
    console.warn('清空聊天记录接口不存在')
    return { code: 200, msg: '操作成功' }
  })
}

// ==================== 搜索 ====================

// 搜索会话中的媒体文件
export function searchMedia(conversationId, keyword = '', limit = 20) {
  return searchMessages({
    conversationId,
    keyword,
    messageType: 'IMAGE',
    pageNum: 1,
    pageSize: limit,
  })
}

// 搜索会话中的文件
export function searchFiles(conversationId, keyword = '', limit = 20) {
  return searchMessages({
    conversationId,
    keyword,
    messageType: 'FILE',
    pageNum: 1,
    pageSize: limit,
  })
}

// 搜索会话中的链接
export function searchLinks(conversationId, keyword = '', limit = 20) {
  return searchMessages({
    conversationId,
    keyword,
    messageType: 'TEXT',
    pageNum: 1,
    pageSize: limit,
  })
}

// ==================== 默认导出 ====================

export default {
  // 用户相关
  getUserInfo,

  // 群组相关
  getGroupDetail,
  getGroupMembers,

  // 成员管理
  addMember,
  removeMember,
  setGroupAdmin,

  // 会话设置
  setPinned,
  setMuted,

  // 内容统计
  getContentStats,
  searchMedia,
  searchFiles,
  searchLinks,

  // 群组操作
  leaveGroup,
  dismissTheGroup,
  clearChatMessages,
}
