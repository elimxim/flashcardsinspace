import axios from 'axios'
import { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { VerificationIntentResponse } from '@/api/communication.ts'
import { VerificationType } from '@/core-logic/user-logic.ts'

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

export async function sendUserGetRequest() {
  Log.log(LogTag.GET, '/users/me')
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}

export async function sendPasswordResetRequest(secret: string) {
  Log.log(LogTag.POST, '/password-reset')
  return await authClient.post<void>('/password-reset', {
    secret: secret,
  })
}

export async function sendVerificationRequestWithBody(email: string, intent: VerificationType) {
  Log.log(LogTag.POST, '/verification/request')
  return await authClient.post<void>('/verification/request', {
    email: email,
    type: intent,
  })
}

export async function sendVerificationRequest() {
  Log.log(LogTag.POST, '/verification/request')
  return await authClient.post<void>('/verification/request')
}

export async function sendVerificationCodeRequest(code: string) {
  Log.log(LogTag.POST, '/verification/code')
  return await authClient.post<VerificationIntentResponse>('/verification/code', {
    code: code,
  })
}

export async function sendVerificationContextRequest() {
  Log.log(LogTag.GET, '/verification/context')
  return await authClient.get<VerificationIntentResponse>('/verification/context')
}
