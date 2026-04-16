/**
 * 搜索历史管理 - 统一 localStorage 数据源
 */
import { ref } from 'vue'

const HISTORY_KEY = 'im_search_history'
const SCOPE_KEY = 'im_search_scope_session'
const KEYWORD_KEY = 'im_search_keyword'

// 模块级历史记录（多组件共享同一份数据）
const historyRef = ref([])

function loadHistory() {
  try {
    const saved = localStorage.getItem(HISTORY_KEY)
    historyRef.value = saved ? JSON.parse(saved) : []
  } catch {
    historyRef.value = []
  }
  return historyRef.value
}

export function useSearchHistory() {
  // 初始化加载
  if (historyRef.value.length === 0) {
    loadHistory()
  }

  const saveHistory = (history) => {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(0, 10)))
  }

  const addToHistory = (keyword) => {
    const trimmed = keyword.trim()
    if (!trimmed) return
    const filtered = historyRef.value.filter(k => k !== trimmed)
    historyRef.value = [trimmed, ...filtered].slice(0, 10)
    saveHistory(historyRef.value)
  }

  const clearHistory = () => {
    historyRef.value = []
    localStorage.removeItem(HISTORY_KEY)
  }

  // 搜索范围会话
  const loadScopeSession = () => {
    try {
      const raw = localStorage.getItem(SCOPE_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  const saveScopeSession = (session) => {
    localStorage.setItem(SCOPE_KEY, JSON.stringify(session))
  }

  // 搜索关键词
  const loadKeyword = () => {
    return localStorage.getItem(KEYWORD_KEY) || ''
  }

  const saveKeyword = (keyword) => {
    if (keyword) {
      localStorage.setItem(KEYWORD_KEY, keyword)
    } else {
      localStorage.removeItem(KEYWORD_KEY)
    }
  }

  return {
    history: historyRef,
    addToHistory,
    clearHistory,
    loadScopeSession,
    saveScopeSession,
    loadKeyword,
    saveKeyword
  }
}
