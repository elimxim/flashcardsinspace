import axios from 'axios'
import { Language } from '@/model/language.ts'
import { configureRetry, DEFAULT_TIMEOUT_MS } from '@/api/retry-config.ts'
import { Log, LogTag } from '@/utils/logger.ts'

const publicApiClient = axios.create({
  baseURL: '/api-public',
  withCredentials: false,
  timeout: DEFAULT_TIMEOUT_MS,
})

configureRetry(publicApiClient)

export async function sendLanguagesGetRequest() {
  Log.log(LogTag.GET, '/languages')
  return await publicApiClient.get<Language[]>('/languages')
}
