import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const loginAction = async (credentials) => {
    const res = await login(credentials)
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      nickname: res.data.nickname
    }
    localStorage.setItem('token', token.value)
    return res
  }

  // 注册
  const registerAction = async (data) => {
    return await register(data)
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    registerAction,
    fetchUserInfo,
    logout
  }
})


