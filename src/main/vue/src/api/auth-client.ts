import axios from 'axios'
import { User } from '@/model/user.ts';

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

export default authClient

// communication>

export interface SignupResponse {
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
