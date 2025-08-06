import { defineStore } from 'pinia'
import { type LightDay, lightDayStatuses } from '@/model/lightspeed-schedule.ts'
import { stages } from '@/core-logic/stage-logic.ts'
import { asIsoDate } from '@/utils/date.ts'

export interface LightspeedScheduleState {
  days: LightDay[]
  startDate: string
  currDay: LightDay
}

export const useLightspeedScheduleStore = defineStore('lightspeed-schedule', {
  state: (): LightspeedScheduleState => {
    const isoDate = asIsoDate(new Date())
    return {
      startDate: isoDate,
      currDay: {
        date: isoDate,
        order: 0,
        stages: [],
        status: '',
      },
      days: [],
    }
  },
  getters: {
    currLightDayStages(): Set<string> {
      return new Set(this.currDay.stages.map(v => v.name))
    },
  },
  actions: {
    loadData() {
      const calendar = testData()
      this.startDate = calendar.startDate
      this.currDay = calendar.currDay
      this.days = calendar.days
    },
    initCalendar(startDate: Date) {

    },
    switchLightDay() {
      const next = this.days[this.currDay.order] as LightDay | undefined
      if (next === undefined) {
        throw new Error(
          `Can't get next light day by index ${this.currDay.order}:
          total number of days is ${this.days.length}`
        )
      }

      this.currDay = next
    },
  }
})

function testData(): LightspeedScheduleState {
  const days: LightDay[] = []
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 19)
  for (let i = 1; i <= 60; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    let status = lightDayStatuses.COMPLETED
    if (i > 14 && i < 18) {
      status = lightDayStatuses.IN_PROGRESS
    } else if (i >= 18) {
      status = lightDayStatuses.NOT_STARTED
    }
    days.push({
      date: date.toISOString().split('T')[0],
      order: i,
      stages: [stages.S7, stages.S2, stages.S3, stages.S1],
      status: status,
    })
  }

  return {
    startDate: days[0].date,
    currDay: days[18],
    days: days,
  }
}
