import type { Stage } from '@/core-logic/stage-logic.ts';

export interface Timeline {
  id: number
  startedAt: string
  status: string
}

export interface Chronoday {
  id: number
  chronodate: string
  seqNumber: number
  status: string
  stages: Stage[]
}

export const timelineStatuses = {
  ACTIVE: "ACTIVE",
  SUSPENDED: "SUSPENDED",
}

export const lightDayStatuses = {
  INITIAL: "INITIAL",
  COMPLETED: "COMPLETED",
  IN_PROGRESS: "IN_PROGRESS",
  NOT_STARTED: "NOT_STARTED",
  OFF: "OFF",
}
