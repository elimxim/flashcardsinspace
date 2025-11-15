import axios from 'axios'
import { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

const authClientWithCredentials = axios.create({
  baseURL: '/auth',
  withCredentials: true,
})

configureDateTransformers(authClient)
configureDateTransformers(authClientWithCredentials)

export default authClient

export async function sendSignupRequest(name: string, email: string, password: string, languageId: number | undefined) {
  console.log('[POST] request => sign up')
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
  return await authClient.post<User>('/signup', {
    email: email,
    secret: password,
    name: name,
    languageId: languageId,
    timezone: timezone,
  })
}

export async function sendLoginRequest(email: string, password: string) {
  console.log('[POST] request => login')
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
  return authClient.post<User>('/login', {
    email: email,
    secret: password,
    timezone: timezone,
  })
}

export async function sendLogoutRequest() {
  console.log('[POST] request => log out')
  return await authClientWithCredentials.post('/logout')
}

export async function sendRefreshTokenRequest() {
  console.log('[POST] request => refresh')
  return await authClientWithCredentials.post<User>('/refresh')
}

export async function sendWhoAmIRequest() {
  console.log('[GET] request => who am i')
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}
