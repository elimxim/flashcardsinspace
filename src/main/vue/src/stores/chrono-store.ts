import { defineStore } from 'pinia'
import { type Chronoday } from '@/model/chrono.ts'
import { asIsoDateStr } from '@/utils/date.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'

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
      console.log(`Loading ${chronodays.length} chronodays with curr day ${currDay.chronodate}`)
      this.resetState()
      this.chronodays = chronodays
      this.currDay = currDay
      this.dayStreak = dayStreak
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`State check: chrono store isn't loaded`)
    },
    resetState() {
      this.chronodays = []
      this.currDay = defaultCurrDay()
      this.dayStreak = 0
      this.loaded = false
    },
    canGoPrev(): boolean {
      this.checkStateLoaded()
      if (this.currDay.seqNumber === undefined || this.currDay.seqNumber === 0) {
        console.log('No prev day: current is initial or OFF')
        return false
      }

      const prevIdx = this.currDay.seqNumber - 1
      if (!this.chronodays[prevIdx]) {
        console.log(`No prev day: couldn't find prev ${prevIdx}`)
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
          console.error(`Couldn't find day ${newDay.id} in the store to update status to ${newDay.status}`)
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
