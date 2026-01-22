class StateManager {
  constructor() {
    this.states = new Map()
    this.globalLoading = ref(false)
    this.globalError = ref(null)
    this.listeners = new Map()
    this.loadingStack = []
  }

  createState(key, options = {}) {
    const state = reactive({
      loading: false,
      error: null,
      data: options.initialData || null,
      success: false,
      timestamp: null,
      retryCount: 0,
      maxRetries: options.maxRetries || 3,
      timeout: options.timeout || 30000
    })

    this.states.set(key, state)
    return state
  }

  getState(key) {
    return this.states.get(key)
  }

  setLoading(key, loading, message = '') {
    const state = this.getState(key) || this.createState(key)
    state.loading = loading
    state.timestamp = Date.now()
    
    if (loading) {
      this.addToLoadingStack(key, message)
    } else {
      this.removeFromLoadingStack(key)
    }

    this.notifyListeners(key, 'loading', { loading, message })
    return state
  }

  setError(key, error) {
    const state = this.getState(key) || this.createState(key)
    state.loading = false
    state.success = false
    state.error = this.formatError(error)
    state.timestamp = Date.now()
    
    this.removeFromLoadingStack(key)
    this.notifyListeners(key, 'error', { error: state.error })
    return state
  }

  setSuccess(key, data) {
    const state = this.getState(key) || this.createState(key)
    state.loading = false
    state.error = null
    state.success = true
    state.data = data
    state.timestamp = Date.now()
    state.retryCount = 0
    
    this.removeFromLoadingStack(key)
    this.notifyListeners(key, 'success', { data })
    return state
  }

  resetState(key) {
    const state = this.getState(key)
    if (state) {
      state.loading = false
      state.error = null
      state.success = false
      state.retryCount = 0
    }
    this.removeFromLoadingStack(key)
    this.notifyListeners(key, 'reset')
  }

  clearAllStates() {
    this.states.clear()
    this.loadingStack = []
    this.globalLoading.value = false
    this.globalError.value = null
  }

  async withLoading(key, operation, options = {}) {
    const {
      loadingMessage = '',
      successMessage = '',
      errorMessage = '操作失败',
      showSuccessMessage = false,
      showErrorMessage = true,
      retryOnError = true
    } = options

    const state = this.getState(key) || this.createState(key)
    
    try {
      this.setLoading(key, true, loadingMessage)
      
      const result = await Promise.race([
        operation(),
        this.createTimeout(state.timeout)
      ])

      this.setSuccess(key, result)
      
      if (showSuccessMessage) {
        ElMessage.success(successMessage || '操作成功')
      }
      
      return result
    } catch (error) {
      state.retryCount++
      
      if (retryOnError && state.retryCount <= state.maxRetries) {
        const delay = Math.pow(2, state.retryCount) * 1000
        await new Promise(resolve => setTimeout(resolve, delay))
        return this.withLoading(key, operation, options)
      }
      
      this.setError(key, error)
      
      if (showErrorMessage) {
        ElMessage.error(errorMessage + ': ' + this.formatError(error).message)
      }
      
      throw error
    }
  }

  createTimeout(timeout) {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error(`操作超时（${timeout}ms）`))
      }, timeout)
    })
  }

  addToLoadingStack(key, message = '') {
    this.loadingStack.push({ key, message, timestamp: Date.now() })
    this.globalLoading.value = true
  }

  removeFromLoadingStack(key) {
    this.loadingStack = this.loadingStack.filter(item => item.key !== key)
    this.globalLoading.value = this.loadingStack.length > 0
  }

  getCurrentLoadingState() {
    return {
      global: this.globalLoading.value,
      stack: this.loadingStack,
      count: this.loadingStack.length
    }
  }

  formatError(error) {
    if (typeof error === 'string') {
      return new Error(error)
    }
    
    if (error instanceof Error) {
      return error
    }
    
    if (error && error.message) {
      return new Error(error.message)
    }
    
    return new Error('未知错误')
  }

  addListener(key, event, callback) {
    const listenerKey = `${key}:${event}`
    if (!this.listeners.has(listenerKey)) {
      this.listeners.set(listenerKey, [])
    }
    this.listeners.get(listenerKey).push(callback)
  }

  removeListener(key, event, callback) {
    const listenerKey = `${key}:${event}`
    const callbacks = this.listeners.get(listenerKey)
    if (callbacks) {
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  notifyListeners(key, event, data = {}) {
    const listenerKey = `${key}:${event}`
    const callbacks = this.listeners.get(listenerKey)
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback({ key, event, ...data })
        } catch (error) {
          console.error('State listener error:', error)
        }
      })
    }
  }

  getStats() {
    const stats = {
      totalStates: this.states.size,
      loadingStates: 0,
      errorStates: 0,
      successStates: 0,
      globalLoading: this.globalLoading.value,
      loadingStack: this.loadingStack.length
    }

    this.states.forEach(state => {
      if (state.loading) stats.loadingStates++
      if (state.error) stats.errorStates++
      if (state.success) stats.successStates++
    })

    return stats
  }

  createUseStateHook(key, options = {}) {
    return () => {
      const state = this.getState(key) || this.createState(key, options)
      
      const setLoading = (loading, message) => {
        this.setLoading(key, loading, message)
      }
      
      const setError = (error) => {
        this.setError(key, error)
      }
      
      const setSuccess = (data) => {
        this.setSuccess(key, data)
      }
      
      const reset = () => {
        this.resetState(key)
      }
      
      const withLoading = (operation, opts = {}) => {
        return this.withLoading(key, operation, { ...options, ...opts })
      }
      
      return {
        state: readonly(state),
        setLoading,
        setError,
        setSuccess,
        reset,
        withLoading
      }
    }
  }

  async batchOperation(key, operations, options = {}) {
    const {
      concurrent = false,
      stopOnError = true,
      progressCallback = null
    } = options

    const state = this.getState(key) || this.createState(key)
    const results = []
    let completed = 0
    let errors = []

    try {
      this.setLoading(key, true, '批量操作中...')

      if (concurrent) {
        const promises = operations.map(async (operation, index) => {
          try {
            const result = await operation()
            results[index] = { success: true, data: result }
            completed++
            
            if (progressCallback) {
              progressCallback(completed, operations.length, { success: true })
            }
            
            return result
          } catch (error) {
            results[index] = { success: false, error }
            errors.push({ index, error })
            completed++
            
            if (progressCallback) {
              progressCallback(completed, operations.length, { success: false, error })
            }
            
            if (stopOnError) {
              throw error
            }
          }
        })

        await Promise.all(promises)
      } else {
        for (let i = 0; i < operations.length; i++) {
          try {
            const result = await operations[i]()
            results[i] = { success: true, data: result }
            completed++
            
            if (progressCallback) {
              progressCallback(completed, operations.length, { success: true })
            }
          } catch (error) {
            results[i] = { success: false, error }
            errors.push({ index: i, error })
            completed++
            
            if (progressCallback) {
              progressCallback(completed, operations.length, { success: false, error })
            }
            
            if (stopOnError) {
              throw error
            }
          }
        }
      }

      this.setSuccess(key, { results, errors })
      return { results, errors }
      
    } catch (error) {
      this.setError(key, error)
      throw error
    }
  }
}

const stateManager = new StateManager()

export const usePageState = (key, options = {}) => {
  return stateManager.createUseStateHook(key, options)()
}

export const useGlobalLoading = () => {
  return {
    loading: readonly(stateManager.globalLoading),
    error: readonly(stateManager.globalError),
    setLoading: (loading, message) => {
      stateManager.globalLoading.value = loading
      if (!loading) stateManager.globalError.value = null
    },
    setError: (error) => {
      stateManager.globalError.value = stateManager.formatError(error)
      stateManager.globalLoading.value = false
    },
    clearError: () => {
      stateManager.globalError.value = null
    }
  }
}

export const useAsyncOperation = () => {
  return {
    execute: (key, operation, options = {}) => {
      return stateManager.withLoading(key, operation, options)
    },
    batch: (key, operations, options = {}) => {
      return stateManager.batchOperation(key, operations, options)
    }
  }
}

export const createLoadingComposable = (key, options = {}) => {
  return stateManager.createUseStateHook(key, options)
}

export const getState = (key) => {
  return stateManager.getState(key)
}

export const setState = (key, updates) => {
  const state = stateManager.getState(key)
  if (state) {
    Object.assign(state, updates)
  }
}

export { stateManager }

export default stateManager