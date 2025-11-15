import {
  AxiosError,
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
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
let refreshPromise: Promise<boolean> | null = null
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

export async function attemptTokenRefresh(): Promise<boolean> {
  const authStore = useAuthStore()
  try {
    console.log('Refreshing token...')
    const response = await sendRefreshTokenRequest()
    console.log('Token refresh successful')
    authStore.setUser(response.data)
    return true
  } catch (error) {
    authStore.resetUser()
    if (error instanceof AxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response?.status === 401 || axiosError.response?.status === 403) {
        console.info(`Refresh token expired: ${axiosError}`)
      }
    }
    console.error('Token refresh failed:', error)
    return false
  }
}

async function refreshToken(): Promise<boolean> {
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
        refreshToken().then(async (success) => {
          if (success) {
            processQueuedRequests(axiosInstance)
          } else {
            rejectQueuedRequests(error)
            await redirectToLoginPage()
          }
        })
      }
    })
  }

  return Promise.reject(error)
}
