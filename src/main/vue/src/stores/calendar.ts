import { defineStore } from 'pinia'
import { type LightDay, StudyStatus } from '@/model/calendar.ts'
import { stages } from '@/core-logic/stage-logic.ts'
import { asIsoDate } from '@/utils/date.ts'

export interface CalendarState {
  lightStartDate: string
  currLightDay: LightDay
  lightDays: LightDay[]
}

export const useCalendarStore = defineStore('calendar', {
  state: (): CalendarState => {
    const isoDate = asIsoDate(new Date())
    return {
      lightStartDate: isoDate,
      currLightDay: {
        isoDate: isoDate,
        seqNumber: 0,
        stages: [],
        status: StudyStatus.COMPLETED,
      },
      lightDays: [],
    }
  },
  getters: {
    currLightDayStages(): Set<string> {
      return new Set(this.currLightDay.stages.map(v => v.name))
    },
  },
  actions: {
    loadData() {
      const calendar = testData()
      this.lightStartDate = calendar.lightStartDate
      this.currLightDay = calendar.currLightDay
      this.lightDays = calendar.lightDays
    },
    initCalendar(startDate: Date) {

    },
    switchLightDay() {
      const next = this.lightDays[this.currLightDay.seqNumber] as LightDay | undefined
      if (next === undefined) {
        throw new Error(
          `Can't get next light day by index ${this.currLightDay.seqNumber}:
          total number of days is ${this.lightDays.length}`
        )
      }

      this.currLightDay = next
    },
  }
})

function testData(): CalendarState {
  const days: LightDay[] = []
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 19)
  for (let i = 1; i <= 60; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    let status = StudyStatus.COMPLETED
    if (i > 14 && i < 18) {
      status = StudyStatus.IN_PROGRESS
    } else if (i >= 18) {
      status = StudyStatus.NOT_STARTED
    }
    days.push({
      isoDate: date.toISOString().split('T')[0],
      seqNumber: i,
      stages: [stages.SEVENTH, stages.SECOND, stages.THIRD, stages.FIRST],
      status: status,
    })
  }

  return {
    lightStartDate: days[0].isoDate,
    currLightDay: days[18],
    lightDays: days,
  }
}
