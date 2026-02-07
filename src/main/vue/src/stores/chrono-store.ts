import { defineStore } from 'pinia'
import { type Chronoday } from '@/model/chrono.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'
import { asIsoDateStr } from '@/utils/utils.ts'
import { Log, LogTag } from '@/utils/logger.ts'

export interface ChronoState {
  chronodays: Chronoday[]
  currDay: Chronoday
  dayStreak: number
  loaded: boolean
}

export const useChronoStore = defineStore('chrono', {
  state: (): ChronoState => {
    return {
      chronodays: [],
      currDay: defaultCurrDay(),
      dayStreak: 0,
      loaded: false,
    }
  },
  getters: {
    isDayOff(): boolean {
      return this.currDay.status === chronodayStatuses.OFF
    },
    isInitialDay(): boolean {
      return this.currDay.status === chronodayStatuses.INITIAL
    },
  },
  actions: {
    loadState(chronodays: Chronoday[], currDay: Chronoday, dayStreak: number) {
      Log.log(LogTag.STORE, `chrono.loadState: currDay.chronodate=${currDay.chronodate} + ${chronodays.length} chronodays, dayStreak=${dayStreak} `)
      this.$reset()
      this.chronodays = chronodays
      this.currDay = currDay
      this.dayStreak = dayStreak
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`chrono.checkStateLoaded: !loaded`)
    },
    canGoPrev(): boolean {
      this.checkStateLoaded()
      if (this.currDay.seqNumber === undefined || this.currDay.seqNumber === 0) {
        Log.log(LogTag.STORE, `chrono.canGoPrev: no prev day, currDay.status=${this.currDay.status}`)
        return false
      }

      const prevIdx = this.currDay.seqNumber - 1
      if (!this.chronodays[prevIdx]) {
        Log.log(LogTag.STORE, `chrono.canGoPrev: no prev day, chronoday.idx=${prevIdx} not found`)
        return false
      } else {
        return true
      }
    },
    canGoNext(): boolean {
      this.checkStateLoaded()
      return true
    },
    updateDays(updatedDays: Chronoday[]) {
      updatedDays.forEach(newDay => {
        if (this.currDay.id === newDay.id) {
          this.currDay = newDay
        }

        const idx = this.chronodays.findIndex(v => v.id === newDay.id)
        if (idx !== -1) {
          this.chronodays[idx] = newDay
        } else {
          Log.error(LogTag.STORE, `chrono.updateDays: chronoday.id=${newDay.id} not found`)
        }
      })
    },
    updateDayStreak(streak: number) {
      this.dayStreak = streak
    },
  }
})

const defaultCurrDay = (): Chronoday => {
  return {
    id: 0,
    chronodate: asIsoDateStr(new Date()),
    seqNumber: 0,
    stages: [],
    status: chronodayStatuses.INITIAL,
  }
}
