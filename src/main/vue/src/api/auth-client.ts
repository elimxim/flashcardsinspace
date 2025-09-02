import axios from 'axios'
import { User } from '@/model/user.ts';
import apiClient from '@/api/api-client.ts';
import type { UserGetResponse } from '@/api/communication.ts';

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

export default authClient

// communication>

export interface SignupResponse {
  user: User
}

export interface LoginResponse {
  user: User
}

// <communication

export async function sendSignupRequest(name: string, email: string, password: string, languageId: number | undefined) {
  return await authClient.post<SignupResponse>('/signup', {
    email: email,
    secret: password,
    name: name,
    languageId: languageId,
  })
}

export async function sendLoginRequest(email: string, password: string) {
  return await authClient.post<LoginResponse>('/login', {
    email: email,
    secret: password,
  })
}

export async function sendLogoutRequest() {
  return await authClient.post('/logout')
}

export async function sendWhoAmIRequest() {
  return await apiClient.get<UserGetResponse>('/users/me', {
    validateStatus: () => true,
  })
}
