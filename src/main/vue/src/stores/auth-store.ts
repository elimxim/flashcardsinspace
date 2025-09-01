import { defineStore } from 'pinia'
import type { User } from '@/model/user.ts'
import type { UserGetResponse } from '@/api/communication.ts'
import apiClient from '@/api/api-client.ts'
import authClient from '@/api/auth-client.ts'

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
    async signup(name: string, email: string, password: string, languageId: number | undefined) {
      const response = await authClient.post<UserGetResponse>('/signup', {
        email: email,
        secret: password,
        name: name,
        languageId: languageId,
      })
      this.setUser(response.data.user)
    },
    async login(email: string, password: string) {
      const response = await authClient.post<UserGetResponse>('/login', {
        email: email,
        secret: password,
      })
      this.setUser(response.data.user)
    },
    async logout() {
      await authClient.post('/logout')
      this.setUser(null)
    },
    async updateJwt() {
      const response = await apiClient.get<UserGetResponse>('/users/me', {
        validateStatus: () => true,
      })

      if (response.status === 200) {
        this.setUser(response.data.user)
      } else {
        this.setUser(null)
      }
    },
  }
})
