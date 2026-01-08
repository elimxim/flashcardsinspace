export enum CodeVerificationResult {
  SUCCESS = 'SUCCESS',
  TESTED = 'TESTED',
  INVALID = 'INVALID',
  NOT_FOUND = 'NOT_FOUND',
  LOCKED = 'LOCKED',
  EXPIRED = 'EXPIRED',
  LIMITED = 'LIMITED',
}

export const confirmationCodePurposes = {
  EMAIL_VERIFICATION: 'EMAIL_VERIFICATION',
}

export function parseCodeVerificationResult(result: string): CodeVerificationResult | undefined {
  return Object.values(CodeVerificationResult).find(v => v === result)
}
