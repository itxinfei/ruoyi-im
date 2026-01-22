import { createStore } from 'vuex'
import im from './modules/im'
import user from './modules/user'

export default createStore({
  modules: {
    im,
    user
  }
})

