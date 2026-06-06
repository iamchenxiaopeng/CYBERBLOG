import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('cyber_token') || '')
  const user = ref<User | null>(JSON.parse(localStorage.getItem('cyber_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

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
