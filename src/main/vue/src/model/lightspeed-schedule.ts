import type { Stage } from '@/core-logic/stage-logic.ts';

export interface LightDay {
  date: string
  order: number
  stages: Stage[]
  status: string
}

export const lightDayStatuses = {
  COMPLETED: "COMPLETED",
  IN_PROGRESS: "IN_PROGRESS",
  NOT_STARTED: "NOT_STARTED",
}
