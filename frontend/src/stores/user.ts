import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'

/**
 * 检查 JWT token 是否已过期（解析 payload 中的 exp 字段）
 */
function isTokenExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1]
    if (!payload) return true
    // base64url → base64 → JSON
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const { exp } = JSON.parse(decoded)
    if (!exp) return false // 没有 exp 字段，认为不过期
    // exp 是秒级时间戳
    return Date.now() >= exp * 1000
  } catch {
    return true // 解析失败视为过期
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('cyber_token') || '')
  const user = ref<User | null>(JSON.parse(localStorage.getItem('cyber_user') || 'null'))

  // isLoggedIn 不仅检查 token 是否存在，还检查是否过期
  const isLoggedIn = computed(() => {
    if (!token.value) return false
    if (isTokenExpired(token.value)) {
      // 自动清理过期的 token
      localStorage.removeItem('cyber_token')
      localStorage.removeItem('cyber_user')
      return false
    }
    return true
  })

  function setAuth(t: string, u: User) {
    token.value = t
    user.value = u
    localStorage.setItem('cyber_token', t)
    localStorage.setItem('cyber_user', JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('cyber_token')
    localStorage.removeItem('cyber_user')
  }

  return { token, user, isLoggedIn, setAuth, logout }
})
