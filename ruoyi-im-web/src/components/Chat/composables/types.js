/**
 * Chat 模块类型定义
 * 提供 JSDoc 类型提示，增强开发体验
 */

/**
 * @typedef {Object} Message
 * @property {String|Number} id - 消息 ID
 * @property {String} type - 消息类型 (TEXT|IMAGE|FILE|VIDEO|VOICE|AUDIO|LOCATION|SYSTEM|RECALLED|NUDGE|COMBINE)
 * @property {String} content - 消息内容
 * @property {String|Number} senderId - 发送者 ID
 * @property {String} senderName - 发送者名称
 * @property {String} senderAvatar - 发送者头像
 * @property {Boolean} isOwn - 是否为自己发送的消息
 * @property {Number} timestamp - 消息时间戳
 * @property {Boolean} [isMerged] - 是否与上一条消息合并显示
 * @property {Number} [readCount] - 已读人数
 * @property {Boolean} [isRead] - 是否已读
 * @property {Number} [groupMemberCount] - 群成员数量
 * @property {Boolean} [isPinned] - 是否置顶
 * @property {Array} [reactions] - 表情回应列表
 * @property {Object} [linkPreview] - 链接预览数据
 */

/**
 * @typedef {Object} Reaction
 * @property {String} emoji - 表情符号
 * @property {String|Number} userId - 用户 ID
 * @property {String} userName - 用户名称
 */

/**
 * @typedef {Object} MessageDivider
 * @property {String} id - 分隔符 ID (time-{messageId})
 * @property {Boolean} isTimeDivider - 标识为时间分隔符
 * @property {String} timeText - 时间文本
 */

/**
 * @typedef {Object} SessionProps
 * @property {Array<Message>} messages - 消息列表
 * @property {String} sessionType - 会话类型 (PRIVATE|GROUP)
 * @property {String|Number} sessionId - 会话 ID
 */

/**
 * @typedef {Object} VirtualScrollState
 * @property {import('vue').Ref<number>} scrollTop - 滚动位置
 * @property {import('vue').Ref<number>} clientHeight - 客户端高度
 * @property {import('vue').Ref<Map>} messageHeightCache - 消息高度缓存
 * @property {import('vue').ComputedRef<Boolean>} isLargeGroup - 是否大群
 * @property {import('vue').ComputedRef<Boolean>} isLazyLoadingEnabled - 是否启用懒加载
 * @property {import('vue').ComputedRef<number>} topSpacerHeight - 顶部占位高度
 * @property {import('vue').ComputedRef<number>} bottomSpacerHeight - 底部占位高度
 * @property {import('vue').ComputedRef<Array>} visibleMessages - 可见消息列表
 */

/**
 * @typedef {Object} ReadUsersState
 * @property {import('vue').Ref<Object>} readUsersMap - 已读用户映射
 * @property {import('vue').Ref<Object>} loadingReadUsers - 加载状态映射
 * @property {Function} fetchReadUsers - 获取已读用户
 * @property {Function} prefetchReadUsers - 预加载已读用户
 * @property {Function} clearCache - 清空缓存
 */

/**
 * @typedef {Object} ScrollState
 * @property {import('vue').Ref<Boolean>} showScrollToBottom - 显示滚动到底部按钮
 * @property {Function} scrollToBottom - 滚动到底部
 * @property {Function} scrollToMsg - 滚动到指定消息
 * @property {Function} handleScroll - 处理滚动事件
 */

/**
 * @typedef {Object} BufferZone
 * @property {number} ABOVE - 上方缓冲区像素
 * @property {number} BELOW - 下方缓冲区像素
 * @property {number} MESSAGE_COUNT - 最少渲染消息数
 */

/**
 * @typedef {Object} VisibleRange
 * @property {number} startIndex - 开始索引
 * @property {number} endIndex - 结束索引
 */

/**
 * @typedef {Object} FileUploadResult
 * @property {String} url - 文件 URL
 * @property {String} name - 文件名
 * @property {Number} size - 文件大小
 * @property {String} type - 文件类型
 */

/**
 * @typedef {Object} LinkPreview
 * @property {String} url - 链接 URL
 * @property {String} title - 标题
 * @property {String} description - 描述
 * @property {String} image - 预览图 URL
 */

/**
 * @typedef {Object} UserInfo
 * @property {String|Number} id - 用户 ID
 * @property {String} nickname - 昵称
 * @property {String} avatar - 头像 URL
 * @property {String} [status] - 在线状态
 */

/**
 * @typedef {Object} GroupInfo
 * @property {String|Number} id - 群组 ID
 * @property {String} name - 群名称
 * @property {String} avatar - 群头像
 * @property {Number} memberCount - 成员数量
 * @property {String} [notice] - 群公告
 */

/**
 * @typedef {Object} ConversationItem
 * @property {String|Number} id - 会话 ID
 * @property {String} type - 会话类型 (PRIVATE|GROUP)
 * @property {UserInfo|GroupInfo} target - 会话对象
 * @property {Message} [lastMessage] - 最后一条消息
 * @property {Number} unreadCount - 未读数
 * @property {Boolean} [isPinned] - 是否置顶
 * @property {Boolean} [isMuted] - 是否免打扰
 */

/**
 * @typedef {Object} CommandOptions
 * @property {String} command - 命令类型
 * @property {Message} message - 关联消息
 * @property {Object} [data] - 附加数据
 */

/**
 * 消息类型枚举
 * @enum {String}
 */
export const MessageType = {
  TEXT: 'TEXT',
  IMAGE: 'IMAGE',
  FILE: 'FILE',
  VIDEO: 'VIDEO',
  VOICE: 'VOICE',
  AUDIO: 'AUDIO',
  LOCATION: 'LOCATION',
  SYSTEM: 'SYSTEM',
  RECALLED: 'RECALLED',
  NUDGE: 'NUDGE',
  COMBINE: 'COMBINE',
  COMBINE_FORWARD: 'COMBINE_FORWARD'
}

/**
 * 会话类型枚举
 * @enum {String}
 */
export const SessionType = {
  PRIVATE: 'PRIVATE',
  GROUP: 'GROUP'
}

/**
 * 发送状态枚举
 * @enum {String}
 */
export const SendStatus = {
  SENDING: 'sending',
  SENT: 'sent',
  FAILED: 'failed',
  RECALLED: 'recalled'
}

export default {
  MessageType,
  SessionType,
  SendStatus
}
