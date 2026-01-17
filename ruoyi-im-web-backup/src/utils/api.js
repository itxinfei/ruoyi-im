import request from './request'

const cache = new Map()

const defaultRetryConfig = {
  retries: 3,
  retryDelay: 1000,
  retryCondition: error => {
    return (
      error.code === 'ECONNABORTED' ||
      error.code === 'ETIMEDOUT' ||
      error.message.includes('timeout')
    )
  },
}

export async function requestWithRetry(config, retryConfig = defaultRetryConfig) {
  const { retries, retryDelay, retryCondition } = { ...defaultRetryConfig, ...retryConfig }

  try {
    return await request(config)
  } catch (error) {
    if (retries > 0 && retryCondition(error)) {
      await new Promise(resolve => setTimeout(resolve, retryDelay))
      return requestWithRetry(config, { ...retryConfig, retries: retries - 1 })
    }
    throw error
  }
}

export async function requestWithCache(config, cacheConfig = {}) {
  const { cacheKey, cacheTime = 5 * 60 * 1000 } = cacheConfig

  if (!cacheKey) {
    return await request(config)
  }

  const cached = cache.get(cacheKey)
  if (cached && Date.now() - cached.timestamp < cacheTime) {
    return cached.data
  }

  const response = await request(config)
  cache.set(cacheKey, {
    data: response,
    timestamp: Date.now(),
  })

  return response
}

export function clearCache(key) {
  if (key) {
    cache.delete(key)
  } else {
    cache.clear()
  }
}

export async function batchRequest(requests, concurrency = 3) {
  const results = []
  const executing = []

  for (const requestFn of requests) {
    const promise = requestFn().then(result => {
      executing.splice(executing.indexOf(promise), 1)
      return result
    })

    results.push(promise)
    executing.push(promise)

    if (executing.length >= concurrency) {
      await Promise.race(executing)
    }
  }

  return Promise.all(results)
}

export function createApiModule(apiConfig) {
  const api = {}

  for (const [name, config] of Object.entries(apiConfig)) {
    api[name] = data => {
      const requestConfig = {
        url: config.url,
        method: config.method || 'get',
        ...config,
      }

      if (config.method === 'get') {
        requestConfig.params = data
      } else {
        requestConfig.data = data
      }

      return request(requestConfig)
    }
  }

  return api
}

export function debounceRequest(fn, delay = 300) {
  let timer = null
  let lastPromise = null

  return function (...args) {
    return new Promise((resolve, reject) => {
      if (timer) {
        clearTimeout(timer)
        if (lastPromise) {
          lastPromise.cancel?.()
        }
      }

      timer = setTimeout(() => {
        lastPromise = fn(...args)
        lastPromise.then(resolve).catch(reject)
      }, delay)
    })
  }
}

export function throttleRequest(fn, interval = 500) {
  let lastTime = 0
  let timer = null

  return function (...args) {
    const now = Date.now()

    return new Promise((resolve, reject) => {
      if (now - lastTime >= interval) {
        lastTime = now
        fn(...args)
          .then(resolve)
          .catch(reject)
      } else {
        if (timer) {
          clearTimeout(timer)
        }
        timer = setTimeout(
          () => {
            lastTime = Date.now()
            fn(...args)
              .then(resolve)
              .catch(reject)
          },
          interval - (now - lastTime)
        )
      }
    })
  }
}
