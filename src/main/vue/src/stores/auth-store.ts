import { defineStore } from 'pinia'
import type { User } from '@/model/user.ts'

export interface AuthState {
  user: User | null,
}

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
    setUser(user: User | null) {
      this.user = user
    },
  }
})
