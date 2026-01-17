import { createStore } from 'vuex'

export default createStore({
  state: {
    currentUser: {
      id: 1,
      name: '测试用户',
      avatar: 'https://via.placeholder.com/40'
    },
    sessions: [
      {
        id: 1,
        name: '张三',
        avatar: 'https://via.placeholder.com/40',
        lastMessage: '你好，在吗？',
        lastMessageTime: Date.now() - 3600000,
        unreadCount: 2
      },
      {
        id: 2,
        name: '项目群',
        avatar: 'https://via.placeholder.com/40',
        lastMessage: '明天开会',
        lastMessageTime: Date.now() - 7200000,
        unreadCount: 0,
        isGroup: true
      }
    ],
    currentSession: null,
    messages: {
      1: [
        {
          id: 1,
          content: '你好，在吗？',
          senderId: 2,
          senderName: '张三',
          senderAvatar: 'https://via.placeholder.com/40',
          timestamp: Date.now() - 3600000,
          isOwn: false
        }
      ],
      2: [
        {
          id: 1,
          content: '明天开会',
          senderId: 3,
          senderName: '李四',
          senderAvatar: 'https://via.placeholder.com/40',
          timestamp: Date.now() - 7200000,
          isOwn: false
        }
      ]
    }
  },
  mutations: {
    SET_CURRENT_SESSION(state, session) {
      state.currentSession = session
    },
    ADD_MESSAGE(state, { sessionId, message }) {
      if (!state.messages[sessionId]) {
        state.messages[sessionId] = []
      }
      state.messages[sessionId].push(message)
    },
    UPDATE_SESSION(state, session) {
      const index = state.sessions.findIndex(s => s.id === session.id)
      if (index !== -1) {
        state.sessions[index] = session
      }
    }
  },
  actions: {
    selectSession({ commit }, session) {
      commit('SET_CURRENT_SESSION', session)
    },
    sendMessage({ commit, state }, { sessionId, content }) {
      const message = {
        id: Date.now(),
        content,
        senderId: state.currentUser.id,
        senderName: state.currentUser.name,
        senderAvatar: state.currentUser.avatar,
        timestamp: Date.now(),
        isOwn: true
      }
      commit('ADD_MESSAGE', { sessionId, message })
    }
  }
})
