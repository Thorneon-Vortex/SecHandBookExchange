import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))

  const login = (tokenValue, adminInfoValue) => {
    token.value = tokenValue
    adminInfo.value = adminInfoValue
    localStorage.setItem('admin_token', tokenValue)
    localStorage.setItem('admin_info', JSON.stringify(adminInfoValue))
  }

  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  const isLoggedIn = computed(() => !!token.value)

  return {
    token,
    adminInfo,
    login,
    logout,
    isLoggedIn
  }
})

