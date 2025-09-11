import axios from 'axios'
import { User } from '@/model/user.ts';
import apiClient from '@/api/api-client.ts';

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

export default authClient

export async function sendSignupRequest(name: string, email: string, password: string, languageId: number | undefined) {
  return await authClient.post<User>('/signup', {
    email: email,
    secret: password,
    name: name,
    languageId: languageId,
  })
}

export async function sendLoginRequest(email: string, password: string) {
  return await authClient.post<User>('/login', {
    email: email,
    secret: password,
  })
}

export async function sendLogoutRequest() {
  return await authClient.post('/logout')
}

export async function sendWhoAmIRequest() {
  return await apiClient.get<User>('/users/me', {
    validateStatus: () => true,
  })
}
