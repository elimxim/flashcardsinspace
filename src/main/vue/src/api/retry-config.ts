import { AxiosError, AxiosInstance } from 'axios'
import axiosRetry, { exponentialDelay, isNetworkOrIdempotentRequestError } from 'axios-retry'
import { Log, LogTag } from '@/utils/logger.ts'

export const DEFAULT_TIMEOUT_MS = 4_000
export const LONG_TIMEOUT_MS = 30_000

const MAX_RETRIES = 3
const BASE_DELAY_MS = 300

/**
 * Configure transient-failure retries on an axios instance.
 *
 * Uses axios-retry's default condition (network errors + idempotent 4xx/5xx),
 * which never retries 401/403 — those belong to token-refresh.ts.
 */
export function configureRetry(axiosInstance: AxiosInstance) {
  axiosRetry(axiosInstance, {
    retries: MAX_RETRIES,
    shouldResetTimeout: true,
    retryCondition: isNetworkOrIdempotentRequestError,
    retryDelay: (retryCount, error) => exponentialDelay(retryCount, error, BASE_DELAY_MS),
    onRetry: (retryCount, error, config) => {
      Log.log(LogTag.API, `Retry ${retryCount}/${MAX_RETRIES} for ${config.method?.toUpperCase()} ${config.url} (${describeError(error)})`)
    },
  })
}

function describeError(error: AxiosError): string {
  if (error.response) return `status ${error.response.status}`
  return error.code ?? 'network error'
}
