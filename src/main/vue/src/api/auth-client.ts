import axios from 'axios'
import { User } from '@/model/user.ts'
import apiClient from '@/api/api-client.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

configureDateTransformers(authClient)

export default authClient

export async function sendSignupRequest(name: string, email: string, password: string, languageId: number | undefined) {
  console.log('[POST] request => sign up')
  return await authClient.post<User>('/signup', {
    email: email,
    secret: password,
    name: name,
    languageId: languageId,
  })
}

export async function sendLoginRequest(email: string, password: string) {
  console.log('[POST] request => login')
  return authClient.post<User>('/login', {
    email: email,
    secret: password,
  })
}

export async function sendLogoutRequest() {
  console.log('[POST] request => log out')
  return await authClient.post('/logout')
}

export async function sendWhoAmIRequest() {
  console.log('[GET] request => who am i')
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}
