import { defineStore } from 'pinia'
import type { AuthState } from '@/models/state.ts'
import type { User } from '@/models/user.ts'
import apiClient from '@/api/api-client.ts'

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => {
    return {
      user: null,
    }
  },
  getters: {
    isAuthenticated(): boolean {
      return this.user !== null
    }
  },
  actions: {
    setUser(user: User) {
      this.user = user
    },
    async login() {
      try {
        const response = await apiClient.get<User>('/users/me')
        this.user = response.data
      } catch (error) {
        console.error('Failed to log in: ', error)
        this.user = null
      }
    },
    async logout() {
      try {
        await apiClient.post('/auth/logout')
      } catch (error) {
        console.error('Failed to log out: ', error)
      }
      this.user = null
    },
  }
})
