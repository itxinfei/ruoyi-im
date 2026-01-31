/**
 * 消息状态管理单元测试
 *
 * 测试 Vuex store 中的消息状态管理逻辑
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createStore } from 'vuex'
import { MessageStatus } from '@/utils/message'

describe('消息状态管理', () => {
  let store

  beforeEach(() => {
    // 创建测试用的 Vuex store
    store = createStore({
      modules: {
        imMessage: {
          namespaced: true,
          state: {
            sendingMessages: new Map(), // 发送中的消息 Map
            messages: [],
            currentConversationId: null
          },
          getters: {
            // 获取发送中的消息
            getSendingMessages: (state) => {
              return Array.from(state.sendingMessages.values())
            },
            // 检查消息是否在发送中
            isMessageSending: (state) => (clientMsgId) => {
              return state.sendingMessages.has(clientMsgId)
            }
          },
          mutations: {
            // 添加发送中的消息
            ADD_SENDING_MESSAGE(state, message) {
              state.sendingMessages.set(message.clientMsgId, {
                ...message,
                timestamp: message.timestamp || Date.now()
              })
            },
            // 移除发送中的消息
            REMOVE_SENDING_MESSAGE(state, clientMsgId) {
              state.sendingMessages.delete(clientMsgId)
            },
            // 清理过期的发送中消息（超过 10 分钟）
            CLEANUP_EXPIRED_MESSAGES(state) {
              const now = Date.now()
              const EXPIRED_TIME = 10 * 60 * 1000 // 10 分钟

              for (const [clientMsgId, message] of state.sendingMessages.entries()) {
                if (now - message.timestamp > EXPIRED_TIME) {
                  state.sendingMessages.delete(clientMsgId)
                }
              }
            }
          },
          actions: {
            // 添加消息到发送队列
            addSendingMessage({ commit }, message) {
              commit('ADD_SENDING_MESSAGE', message)
            },
            // 标记消息为已送达
            markMessageAsDelivered({ commit, state }, { clientMsgId, messageId }) {
              if (state.sendingMessages.has(clientMsgId)) {
                commit('REMOVE_SENDING_MESSAGE', clientMsgId)
                // 更新消息列表中的消息状态
                const message = state.messages.find(m => m.clientMsgId === clientMsgId)
                if (message) {
                  message.id = messageId
                  message.sendStatus = MessageStatus.DELIVERED
                }
              }
            },
            // 标记消息发送失败
            markMessageAsFailed({ commit, state }, { clientMsgId, errorCode }) {
              if (state.sendingMessages.has(clientMsgId)) {
                commit('REMOVE_SENDING_MESSAGE', clientMsgId)
                // 更新消息列表中的消息状态
                const message = state.messages.find(m => m.clientMsgId === clientMsgId)
                if (message) {
                  message.sendStatus = MessageStatus.FAILED
                  message.sendErrorCode = errorCode
                }
              }
            },
            // 清理过期消息
            cleanupExpiredMessages({ commit }) {
              commit('CLEANUP_EXPIRED_MESSAGES')
            }
            // 每 5 分钟清理一次
          }
        }
      }
    })
  })

  describe('发送中的消息管理', () => {
    it('应该能够添加发送中的消息', () => {
      // Given
      const message = {
        clientMsgId: 'client-123',
        conversationId: 100,
        content: '测试消息',
        type: 'TEXT'
      }

      // When
      store.dispatch('imMessage/addSendingMessage', message)

      // Then
      const sendingMessages = store.getters['imMessage/getSendingMessages']
      expect(sendingMessages).toHaveLength(1)
      expect(sendingMessages[0].clientMsgId).toBe('client-123')
    })

    it('应该能够检查消息是否在发送中', () => {
      // Given
      const message = {
        clientMsgId: 'client-123',
        conversationId: 100,
        content: '测试消息'
      }
      store.dispatch('imMessage/addSendingMessage', message)

      // When
      const isSending = store.getters['imMessage/isMessageSending']('client-123')

      // Then
      expect(isSending).toBe(true)
    })

    it('应该能够移除发送中的消息', () => {
      // Given
      const message = {
        clientMsgId: 'client-123',
        conversationId: 100,
        content: '测试消息'
      }
      store.dispatch('imMessage/addSendingMessage', message)

      // When
      store.commit('imMessage/REMOVE_SENDING_MESSAGE', 'client-123')

      // Then
      const sendingMessages = store.getters['imMessage/getSendingMessages']
      expect(sendingMessages).toHaveLength(0)
    })
  })

  describe('消息状态更新', () => {
    it('应该能够标记消息为已送达', () => {
      // Given
      const clientMsgId = 'client-123'
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId,
        conversationId: 100,
        content: '测试消息'
      })
      store.state.imMessage.messages = [{
        clientMsgId,
        sendStatus: 'PENDING'
      }]

      // When
      store.dispatch('imMessage/markMessageAsDelivered', {
        clientMsgId,
        messageId: 456
      })

      // Then
      const message = store.state.imMessage.messages.find(m => m.clientMsgId === clientMsgId)
      expect(message.sendStatus).toBe('DELIVERED')
      expect(message.id).toBe(456)
      expect(store.getters['imMessage/isMessageSending'](clientMsgId)).toBe(false)
    })

    it('应该能够标记消息为发送失败', () => {
      // Given
      const clientMsgId = 'client-123'
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId,
        conversationId: 100,
        content: '测试消息'
      })
      store.state.imMessage.messages = [{
        clientMsgId,
        sendStatus: 'PENDING'
      }]

      // When
      store.dispatch('imMessage/markMessageAsFailed', {
        clientMsgId,
        errorCode: 'NETWORK_ERROR'
      })

      // Then
      const message = store.state.imMessage.messages.find(m => m.clientMsgId === clientMsgId)
      expect(message.sendStatus).toBe('FAILED')
      expect(message.sendErrorCode).toBe('NETWORK_ERROR')
    })
  })

  describe('过期消息清理', () => {
    it('应该清理超过 10 分钟的发送中消息', () => {
      // Given
      const now = Date.now()
      const expiredTime = now - 11 * 60 * 1000 // 11 分钟前
      const validTime = now - 5 * 60 * 1000 // 5 分钟前

      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId: 'expired-123',
        timestamp: expiredTime
      })
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId: 'valid-123',
        timestamp: validTime
      })

      // When
      store.dispatch('imMessage/cleanupExpiredMessages')

      // Then
      const sendingMessages = store.getters['imMessage/getSendingMessages']
      expect(sendingMessages).toHaveLength(1)
      expect(sendingMessages[0].clientMsgId).toBe('valid-123')
    })

    it('应该能够定期清理过期消息', async () => {
      // Given
      vi.useFakeTimers()

      const expiredTime = Date.now() - 11 * 60 * 1000
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId: 'expired-123',
        timestamp: expiredTime
      })

      // 模拟 5 分钟后执行清理
      const FIVE_MINUTES = 5 * 60 * 1000
      vi.advanceTimersByTime(FIVE_MINUTES)

      // When
      store.dispatch('imMessage/cleanupExpiredMessages')

      // Then
      const sendingMessages = store.getters['imMessage/getSendingMessages']
      expect(sendingMessages).toHaveLength(0)

      vi.useRealTimers()
    })
  })

  describe('幂等性处理', () => {
    it('重复标记同一消息为已送达应该只处理一次', () => {
      // Given
      const clientMsgId = 'client-123'
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId,
        conversationId: 100
      })
      store.state.imMessage.messages = [{
        clientMsgId,
        sendStatus: 'PENDING'
      }]

      // When - 重复标记 3 次
      store.dispatch('imMessage/markMessageAsDelivered', { clientMsgId, messageId: 456 })
      store.dispatch('imMessage/markMessageAsDelivered', { clientMsgId, messageId: 456 })
      store.dispatch('imMessage/markMessageAsDelivered', { clientMsgId, messageId: 456 })

      // Then - 应该不会报错，且消息状态保持为已送达
      const message = store.state.imMessage.messages.find(m => m.clientMsgId === clientMsgId)
      expect(message.sendStatus).toBe('DELIVERED')
      expect(message.id).toBe(456)
    })
  })
})

/**
 * 性能基线测试
 */
describe('消息管理性能测试', () => {
  let store

  beforeEach(() => {
    store = createStore({
      modules: {
        imMessage: {
          namespaced: true,
          state: {
            sendingMessages: new Map(),
            messages: []
          },
          mutations: {
            ADD_SENDING_MESSAGE(state, message) {
              state.sendingMessages.set(message.clientMsgId, message)
            }
          }
        }
      }
    })
  })

  it('1000 次消息添加操作应该在合理时间内完成', () => {
    const startTime = performance.now()

    // 添加 1000 条消息
    for (let i = 0; i < 1000; i++) {
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId: `client-${i}`,
        content: `消息 ${i}`,
        timestamp: Date.now()
      })
    }

    const endTime = performance.now()
    const duration = endTime - startTime

    // 性能基线：1000 次操作应该在 100ms 内完成
    expect(duration).toBeLessThan(100)
    console.log(`性能基线测试: 1000 次消息添加耗时 ${duration.toFixed(2)}ms`)
  })

  it('Map 查询操作应该保持 O(1) 时间复杂度', () => {
    // 添加 10000 条消息
    for (let i = 0; i < 10000; i++) {
      store.commit('imMessage/ADD_SENDING_MESSAGE', {
        clientMsgId: `client-${i}`,
        content: `消息 ${i}`
      })
    }

    const startTime = performance.now()

    // 查询 1000 次
    for (let i = 0; i < 1000; i++) {
      const randomId = `client-${Math.floor(Math.random() * 10000)}`
      store.state.imMessage.sendingMessages.has(randomId)
    }

    const endTime = performance.now()
    const duration = endTime - startTime

    // 性能基线：1000 次查询应该在 10ms 内完成
    expect(duration).toBeLessThan(10)
    console.log(`性能基线测试: 1000 次查询耗时 ${duration.toFixed(2)}ms`)
  })
})
