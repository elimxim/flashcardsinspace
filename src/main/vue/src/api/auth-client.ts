import axios from 'axios'
import { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'
import { Log, LogTag } from '@/utils/logger.ts'

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
  Log.log(LogTag.POST, '/signup')
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
  Log.log(LogTag.POST, '/login')
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
  return authClient.post<User>('/login', {
    email: email,
    secret: password,
    timezone: timezone,
  })
}

export async function sendLogoutRequest() {
  Log.log(LogTag.POST, '/logout')
  return await authClientWithCredentials.post('/logout')
}

export async function sendRefreshTokenRequest() {
  Log.log(LogTag.POST, '/refresh')
  return await authClientWithCredentials.post<User>('/refresh')
}

export async function sendWhoAmIRequest() {
  Log.log(LogTag.GET, '/users/me')
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}
