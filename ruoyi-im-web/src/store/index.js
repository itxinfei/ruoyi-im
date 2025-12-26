import { createStore } from 'vuex'
import chat from './chat'
import getters from './getters'
import app from './modules/app'
import user from './modules/user'
import im from './modules/im'
import { createPersistedState } from '@/utils/persist'

export default createStore({
  modules: {
    app,
    user,
    chat,
    im,
  },
  getters,
  plugins: [
    createPersistedState({
      key: 'vuex',
      paths: ['app.sidebarOpened', 'app.device', 'user.token', 'user.name', 'user.avatar'],
    }),
  ],
})
