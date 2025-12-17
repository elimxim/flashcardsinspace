import axios from 'axios'
import { Language } from '@/model/language.ts'
import { Log, LogTag } from '@/utils/logger.ts'

const publicApiClient = axios.create({
  baseURL: '/api-public',
  withCredentials: false,
})

export async function sendLanguagesGetRequest() {
  Log.log(LogTag.GET, '/languages')
  return await publicApiClient.get<Language[]>('/languages')
}
