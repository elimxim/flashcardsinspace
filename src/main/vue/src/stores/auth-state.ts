import { defineStore } from 'pinia'
import type { AuthState } from '@/model/state.ts'
import type { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { email } from '@vuelidate/validators';
import type { UserResponse } from '@/api/api-dto.ts';

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
    async signup(name: string, email: string, password: string) {
      const response = await apiClient.post<UserResponse>('/auth/signup', {
        email: email,
        secret: password,
        name: name,
      })
      this.setUser(response.data.user)
    },
    async login(email: string, password: string) {
      const response = await apiClient.post<UserResponse>('/auth/login', {
        email: email,
        secret: password,
      })
      this.setUser(response.data.user)
    },
    async logout() {
      await apiClient.post('/auth/logout')
      this.setUser(null)
    },
    async updateJwt() {
      const response = await apiClient.get<UserResponse>('/users/me', {
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
