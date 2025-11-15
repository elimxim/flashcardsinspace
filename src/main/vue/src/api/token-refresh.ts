import {
  AxiosError,
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { User } from '@/model/user.ts'
import { sendRefreshTokenRequest } from '@/api/auth-client.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { redirectToLoginPage } from '@/router/index.ts'

export function configureTokenRefreshInterceptor(axiosInstance: AxiosInstance) {
  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => response,
    async (error: AxiosError) => {
      return handleUnauthorizedError(error, axiosInstance)
    }
  )
}

interface QueuedRequest {
  resolve: (value: AxiosResponse | Promise<AxiosResponse>) => void
  reject: (error: AxiosError) => void
  config: InternalAxiosRequestConfig
}

let isRefreshing = false
let refreshPromise: Promise<User> | null = null
let failedQueue: QueuedRequest[] = []

function rejectQueuedRequests(error: AxiosError) {
  failedQueue.forEach(({ reject, config }) => {
    console.log(`Rejecting request ${config.url}`)
    reject(error)
  })

  failedQueue = []
}

function processQueuedRequests(axiosInstance: AxiosInstance) {
  failedQueue.forEach(({ resolve, config }) => {
    console.log(`Replaying request ${config.url}`)
    resolve(axiosInstance.request(config))
  })

  failedQueue = []
}

async function attemptTokenRefresh(): Promise<User> {
  try {
    console.log('Refreshing token...')
    const response = await sendRefreshTokenRequest()
    console.log('Token refresh successful')
    return response.data
  } catch (error) {
    if (error instanceof AxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response?.status === 401 || axiosError.response?.status === 403) {
        console.info(`Refresh token expired: ${axiosError}`)
        throw new Error('Refresh token expired')
      }
    }
    console.error('Token refresh failed:', error)
    throw error
  }
}

async function refreshToken(): Promise<User> {
  if (isRefreshing && refreshPromise) {
    return refreshPromise
  }

  try {
    isRefreshing = true
    refreshPromise = attemptTokenRefresh()
    return await refreshPromise
  } finally {
    isRefreshing = false
    refreshPromise = null
  }
}

async function handleUnauthorizedError(error: AxiosError, axiosInstance: AxiosInstance): Promise<AxiosResponse> {
  if (error.response?.status === 401 || error.response?.status === 403) {
    console.log('Handling 401/403 error')
    const originalRequest = error.config as InternalAxiosRequestConfig
    return new Promise<AxiosResponse>((resolve, reject) => {
      console.log(`Request '${originalRequest.baseURL}' was queued`)
      failedQueue.push({ resolve, reject, config: originalRequest })
      if (!isRefreshing) {
        performTokenRefreshAndProcessQueue(error, axiosInstance)
      }
    })
  }

  return Promise.reject(error)
}

async function performTokenRefreshAndProcessQueue(error: AxiosError, axiosInstance: AxiosInstance) {
  const authStore = useAuthStore()

  try {
    const user = await refreshToken()
    authStore.setUser(user)
    processQueuedRequests(axiosInstance)
  } catch {
    rejectQueuedRequests(error)
    authStore.resetUser()
    await redirectToLoginPage()
  }
}
