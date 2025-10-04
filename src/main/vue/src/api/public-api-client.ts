import axios from 'axios'
import { Language } from '@/model/language.ts'

const publicApiClient = axios.create({
  baseURL: '/api-public',
  withCredentials: false,
})

export async function loadLanguages() {
  console.log('[GET] request => languages')
  return await publicApiClient.get<Language[]>('/languages')
}
