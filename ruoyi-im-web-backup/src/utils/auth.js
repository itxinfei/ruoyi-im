// 使用localStorage替代cookies存储token
const TokenKey = 'token'

export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TokenKey, token)
  } else {
    localStorage.removeItem(TokenKey)
  }
}

export function removeToken() {
  localStorage.removeItem(TokenKey)
}
