import { sendUserGetRequest } from '@/api/auth-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { attemptTokenRefresh } from '@/api/token-refresh.ts'
import { useAuthStore } from '@/stores/auth-store.ts'

export enum VerificationResult {
  SUCCESS = 'SUCCESS',
  FOUND = 'FOUND',
  NOT_FOUND = 'NOT_FOUND',
  SESSION_EXPIRED = 'SESSION_EXPIRED',
  EXPIRED = 'EXPIRED',
  INVALID = 'INVALID',
  USED = 'USED',
  LOCKED = 'LOCKED',
  LIMITED = 'LIMITED',
}

export enum VerificationIntent {
  EMAIL_VERIFICATION = 'EMAIL_VERIFICATION',
}

export function toVerificationCodeResult(result: string): VerificationResult | undefined {
  return Object.values(VerificationResult).find(v => v === result)
}

export function parseVerificationIntent(value?: unknown): VerificationIntent | undefined {
  if (value === undefined || value === null || typeof value !== 'string') return undefined
  return Object.values(VerificationIntent).find(v => v === value)
}

export async function whoAmI() {
  return sendUserGetRequest()
    .then(async (response) => {
      if (response.status === 200) {
        Log.log(LogTag.API, `Who Am I: ${response.data}`)
        useAuthStore().setUser(response.data)
        return true
      } else {
        Log.log(LogTag.API, `Who Am I: ${response.status}`)
        return attemptTokenRefresh()
      }
    })
}
