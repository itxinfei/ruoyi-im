const STORAGE_KEY_PREFIX = 'vuex_'

export function createPersistedState(options = {}) {
  const {
    key = 'vuex',
    paths = [],
    storage = localStorage,
    filter = () => true,
    reducer = state => state,
  } = options

  return store => {
    const savedState = loadState(key, storage, reducer)

    if (savedState) {
      store.replaceState(Object.assign({}, store.state, savedState))
    }

    store.subscribe((mutation, state) => {
      const stateToSave =
        paths.length === 0
          ? state
          : paths.reduce((acc, path) => {
              const value = path.split('.').reduce((obj, key) => obj && obj[key], state)
              if (value !== undefined) {
                path.split('.').reduce((obj, key, index, arr) => {
                  if (index === arr.length - 1) {
                    obj[key] = value
                  } else {
                    obj[key] = obj[key] || {}
                  }
                  return obj[key]
                }, acc)
              }
              return acc
            }, {})

      if (filter(mutation, stateToSave)) {
        saveState(key, stateToSave, storage)
      }
    })
  }
}

function loadState(key, storage, reducer) {
  try {
    const serializedState = storage.getItem(STORAGE_KEY_PREFIX + key)
    if (serializedState === null) {
      return undefined
    }
    return reducer(JSON.parse(serializedState))
  } catch (err) {
    console.error('Failed to load state:', err)
    return undefined
  }
}

function saveState(key, state, storage) {
  try {
    const serializedState = JSON.stringify(state)
    storage.setItem(STORAGE_KEY_PREFIX + key, serializedState)
  } catch (err) {
    console.error('Failed to save state:', err)
  }
}

export function resetPersistedState(key, storage = localStorage) {
  storage.removeItem(STORAGE_KEY_PREFIX + key)
}
