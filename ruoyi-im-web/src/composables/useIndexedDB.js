/**
 * IndexedDB 本地存储 Composable
 *
 * 功能：
 * - 消息本地缓存
 * - 会话列表本地缓存
 * - 优先从本地读取，后台同步
 * - 离线可用
 *
 * 数据库结构：
 * - messages: 存储消息列表
 * - conversations: 存储会话列表
 * - users: 存储用户信息
 */

import { ref, computed } from 'vue'

// 数据库名称
const DB_NAME = 'ImCacheDB'
const DB_VERSION = 1

// 存储对象名称
const STORE_MESSAGES = 'messages'
const STORE_CONVERSATIONS = 'conversations'
const STORE_USERS = 'users'

// 数据库连接
let db = null

/**
 * 初始化 IndexedDB
 */
async function initDB() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onerror = () => {
      console.error('IndexedDB 打开失败:', request.error)
      reject(request.error)
    }

    request.onsuccess = () => {
      db = request.result
      resolve(db)
    }

    request.onupgradeneeded = (event) => {
      const database = event.target.result

      // 创建消息存储
      if (!database.objectStoreNames.contains(STORE_MESSAGES)) {
        const messageStore = database.createObjectStore(STORE_MESSAGES, { keyPath: 'id', autoIncrement: true })
        messageStore.createIndex('conversationId', 'conversationId', { unique: false })
        messageStore.createIndex('createTime', 'createTime', { unique: false })
      }

      // 创建会话存储
      if (!database.objectStoreNames.contains(STORE_CONVERSATIONS)) {
        const conversationStore = database.createObjectStore(STORE_CONVERSATIONS, { keyPath: 'id', autoIncrement: true })
        conversationStore.createIndex('lastMessageTime', 'lastMessageTime', { unique: false })
      }

      // 创建用户存储
      if (!database.objectStoreNames.contains(STORE_USERS)) {
        const userStore = database.createObjectStore(STORE_USERS, { keyPath: 'userId', autoIncrement: false })
      }
    }
  })
}

/**
 * 获取数据库连接
 */
async function getDB() {
  if (!db) {
    await initDB()
  }
  return db
}

/**
 * 添加或更新消息
 */
async function putMessage(message) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_MESSAGES], 'readwrite')
    const store = transaction.objectStore(STORE_MESSAGES)
    const request = store.put(message)

    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

/**
 * 批量添加消息
 */
async function putMessages(messages) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_MESSAGES], 'readwrite')
    const store = transaction.objectStore(STORE_MESSAGES)

    // 批量添加
    messages.forEach(message => {
      store.put(message)
    })

    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

/**
 * 获取会话的所有消息
 */
async function getMessagesByConversation(conversationId, limit = 100) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_MESSAGES], 'readonly')
    const store = transaction.objectStore(STORE_MESSAGES)
    const index = store.index('conversationId')
    const request = index.getAll(conversationId, limit)

    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

/**
 * 清空会话的本地消息缓存
 */
async function clearConversationMessages(conversationId) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_MESSAGES], 'readwrite')
    const store = transaction.objectStore(STORE_MESSAGES)
    const index = store.index('conversationId')
    const request = index.openCursor(IDBKeyRange.only(conversationId))

    const deletePromises = []

    request.onsuccess = (event) => {
      const cursor = event.target.result
      if (cursor) {
        deletePromises.push(cursor.delete())
        cursor.continue()
      } else {
        Promise.all(deletePromises).then(() => resolve())
      }
    }

    request.onerror = () => reject(request.error)
  })
}

/**
 * 添加或更新会话
 */
async function putConversation(conversation) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_CONVERSATIONS], 'readwrite')
    const store = transaction.objectStore(STORE_CONVERSATIONS)
    const request = store.put(conversation)

    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

/**
 * 批量添加会话
 */
async function putConversations(conversations) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_CONVERSATIONS], 'readwrite')
    const store = transaction.objectStore(STORE_CONVERSATIONS)

    conversations.forEach(conversation => {
      store.put(conversation)
    })

    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

/**
 * 获取所有会话
 */
async function getAllConversations() {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_CONVERSATIONS], 'readonly')
    const store = transaction.objectStore(STORE_CONVERSATIONS)
    const request = store.getAll()

    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

/**
 * 删除会话
 */
async function deleteConversation(conversationId) {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction([STORE_CONVERSATIONS], 'readwrite')
    const store = transaction.objectStore(STORE_CONVERSATIONS)
    const request = store.delete(conversationId)

    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}

/**
 * 清空所有数据
 */
async function clearAll() {
  const database = await getDB()
  return new Promise((resolve, reject) => {
    const transaction = database.transaction(
      [STORE_MESSAGES, STORE_CONVERSATIONS, STORE_USERS],
      'readwrite'
    )

    transaction.objectStore(STORE_MESSAGES).clear()
    transaction.objectStore(STORE_CONVERSATIONS).clear()
    transaction.objectStore(STORE_USERS).clear()

    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

/**
 * IndexedDB Composable
 */
export function useIndexedDB() {
  const isReady = ref(false)
  const error = ref(null)

  // 初始化
  initDB().then(() => {
    isReady.value = true
  }).catch((err) => {
    error.value = err
    console.error('IndexedDB 初始化失败:', err)
  })

  // 获取会话消息（优先从本地）
  const getConversationMessages = async (conversationId, limit = 100) => {
    try {
      // 先从本地获取
      const localMessages = await getMessagesByConversation(conversationId, limit)

      if (localMessages.length > 0) {
        console.log(`[IndexedDB] 从本地加载 ${localMessages.length} 条消息`)
        return {
          messages: localMessages,
          fromLocal: true
        }
      }

      return { messages: [], fromLocal: false }
    } catch (err) {
      console.error('从 IndexedDB 读取消息失败:', err)
      return { messages: [], fromLocal: false }
    }
  }

  // 缓存会话消息
  const cacheMessages = async (messages) => {
    try {
      await putMessages(messages)
      console.log(`[IndexedDB] 缓存 ${messages.length} 条消息`)
    } catch (err) {
      console.error('缓存消息到 IndexedDB 失败:', err)
    }
  }

  // 缓存单条消息
  const cacheSingleMessage = async (message) => {
    try {
      await putMessage(message)
    } catch (err) {
      console.error('缓存单条消息失败:', err)
    }
  }

  // 获取会话列表（优先从本地）
  const getConversationList = async () => {
    try {
      const localConversations = await getAllConversations()

      if (localConversations.length > 0) {
        console.log(`[IndexedDB] 从本地加载 ${localConversations.length} 个会话`)
        return {
          conversations: localConversations,
          fromLocal: true
        }
      }

      return { conversations: [], fromLocal: false }
    } catch (err) {
      console.error('从 IndexedDB 读取会话失败:', err)
      return { conversations: [], fromLocal: false }
    }
  }

  // 缓存会话列表
  const cacheConversations = async (conversations) => {
    try {
      await putConversations(conversations)
      console.log(`[IndexedDB] 缓存 ${conversations.length} 个会话`)
    } catch (err) {
      console.error('缓存会话到 IndexedDB 失败:', err)
    }
  }

  // 缓存单个会话
  const cacheSingleConversation = async (conversation) => {
    try {
      await putConversation(conversation)
    } catch (err) {
      console.error('缓存单个会话失败:', err)
    }
  }

  // 删除会话
  const removeConversation = async (conversationId) => {
    try {
      await deleteConversation(conversationId)
      await clearConversationMessages(conversationId)
      console.log(`[IndexedDB] 删除会话 ${conversationId}`)
    } catch (err) {
      console.error('删除会话失败:', err)
    }
  }

  // 清空所有缓存
  const clearCache = async () => {
    try {
      await clearAll()
      console.log('[IndexedDB] 清空所有缓存')
    } catch (err) {
      console.error('清空缓存失败:', err)
    }
  }

  // 获取缓存大小
  const getCacheSize = async () => {
    if (navigator.storage && navigator.storage.estimate) {
      try {
        const estimate = await navigator.storage.estimate()
        const usage = estimate.usage || 0
        const quota = estimate.quota || 0
        return {
          used: Math.round(usage / 1024 / 1024), // MB
          quota: Math.round(quota / 1024 / 1024),  // MB
          percentage: Math.round((usage / quota) * 100)
        }
      } catch (err) {
        console.error('获取缓存大小失败:', err)
      }
    }
    return { used: 0, quota: 0, percentage: 0 }
  }

  return {
    isReady,
    error,
    getConversationMessages,
    cacheMessages,
    cacheSingleMessage,
    getConversationList,
    cacheConversations,
    cacheSingleConversation,
    removeConversation,
    clearCache,
    getCacheSize
  }
}
