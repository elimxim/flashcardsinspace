import axios from 'axios'
import { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { VerificationCodeResponse } from '@/api/communication.ts'

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: true,
})

configureDateTransformers(authClient)

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
  return await authClient.post('/logout')
}

export async function sendRefreshTokenRequest() {
  Log.log(LogTag.POST, '/refresh')
  return await authClient.post<User>('/refresh')
}

export async function sendWhoAmIRequest() {
  Log.log(LogTag.GET, '/users/me')
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}

export async function sendVerificationCodePostRequest(email: string, purpose: string) {
  Log.log(LogTag.POST, '/verification-code')
  return await authClient.post('/verification-code', {
    email: email,
    purpose: purpose,
  })
}

export async function sendVerificationCodePutRequest(code: string) {
  Log.log(LogTag.PUT, '/verification-code')
  return await authClient.put<VerificationCodeResponse>('/verification-code', {
    code: code,
  })
}

export async function sendVerificationCodeGetRequest() {
  Log.log(LogTag.GET, '/verification-code')
  return await authClient.get<VerificationCodeResponse>('/verification-code')
}
