import { ErrorResponseBody } from '@/api/communication.ts'

export function errorResponseData(error: unknown): ErrorResponseBody | undefined {
  return (error as { response?: { data?: ErrorResponseBody } })?.response?.data
}

export function isCanceledError(error: unknown): boolean {
  return (error as { code?: string })?.code === 'ERR_CANCELED'
}
