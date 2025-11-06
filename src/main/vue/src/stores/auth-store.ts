import { defineStore } from 'pinia'
import type { User } from '@/model/user.ts'

export interface AuthState {
  user: User | undefined,
}

export enum UserRole {
  ASTRONAUT = 'ASTRONAUT',
  COMMANDER = 'COMMANDER',
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => {
    return {
      user: undefined,
    }
  },
  getters: {
    isAuthenticated(): boolean {
      return this.user !== undefined
    },
  },
  actions: {
    setUser(user: User | undefined) {
      this.user = user
    },
    resetUser() {
      this.user = undefined
    },
    hasAccess(role: UserRole): boolean {
      return this.user?.roles.includes(role.toString()) ?? false
    },
  }
})
