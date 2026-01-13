export enum VerificationCodeResult {
  SUCCESS = 'SUCCESS',
  CONTEXT = 'CONTEXT',
  INVALID = 'INVALID',
  NOT_FOUND = 'NOT_FOUND',
  LOCKED = 'LOCKED',
  EXPIRED = 'EXPIRED',
  LIMITED = 'LIMITED',
  USED = 'USED',
}

export const verificationCodePurposes = {
  EMAIL_VERIFICATION: 'EMAIL_VERIFICATION',
}

export function parseVerificationCodeResult(result: string): VerificationCodeResult | undefined {
  return Object.values(VerificationCodeResult).find(v => v === result)
}
