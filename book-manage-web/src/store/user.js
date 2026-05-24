import { defineStore } from 'pinia'
import { login, register, logout, getUserInfo } from '@/api/auth'
import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    token: localStorage.getItem('token') || ''
  }),
  
  actions: {
    async login(userData) {
      const res = await login(userData)
      this.token = res.data
      setToken(res.data)
      await this.getUserInfo()
      return res
    },
    
    async getUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return res
    },
    
    async logout() {
      await logout()
      this.token = ''
      this.userInfo = {}
      removeToken()
      localStorage.removeItem('userInfo')
    }
  }
})
