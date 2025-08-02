import type { Stage } from '@/core-logic/stage-logic.ts';

export interface LightspeedCalendar {
  lightStartDate: string
  currLightDay: LightDay
  lightDays: LightDay[]
}

export interface LightDay {
  isoDate: string
  seqNumber: number
  stages: Stage[]
  status: StudyStatus
}

export enum StudyStatus {
  COMPLETED = "COMPLETED",
  IN_PROGRESS = "IN_PROGRESS",
  NOT_STARTED = "NOT_STARTED",
}
