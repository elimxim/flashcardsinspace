import { defineStore } from 'pinia'
import {
  type Chronoday
} from '@/model/chrono.ts'
import { asIsoDateStr } from '@/utils/date.ts'
import apiClient from '@/api/api-client.ts'
import type {
  ChronoSyncRequest,
  ChronoSyncResponse,
  ChronoBulkUpdateRequest, ChronodayId,
} from '@/api/communication.ts'
import type { FlashcardSet } from '@/model/flashcard.ts'
import {
  chronodayStatuses,
  isCompleteAvailable,
  isInProgressAvailable,
  selectConsecutiveDaysBeforeIncluding,
} from '@/core-logic/chrono-logic.ts'

export interface ChronoState {
  chronodays: Chronoday[]
  currDay: Chronoday
  loaded: boolean
}

export const useChronoStore = defineStore('chrono', {
  state: (): ChronoState => {
    return {
      chronodays: [],
      currDay: defaultCurrDay(),
      loaded: false,
    }
  },
  getters: {
    currDayStages(): Set<string> {
      return new Set(this.currDay.stages)
    },
  },
  actions: {
    loadState(chronodays: Chronoday[], currDay: Chronoday) {
      console.log(`Loading ${chronodays.length} chronodays with curr day ${currDay.chronodate}`)
      this.chronodays = chronodays
      this.currDay = currDay
      this.loaded = true
    },
    async loadChronodays(flashcardSet: FlashcardSet): Promise<void> {
      this.resetState()
      const chronodaysRequest: ChronoSyncRequest = {
        clientDatetime: new Date().toISOString()
      }

      await apiClient.post<ChronoSyncResponse>('/flashcard-sets/' + flashcardSet.id + '/chrono/sync', chronodaysRequest)
        .then(response => {
          this.chronodays = response.data.chronodays
          this.currDay = response.data.currDay
          this.loaded = true
        })
      // todo catch errors
    },
    canGoPrev(): boolean {
      this.checkStateLoaded()
      if (this.currDay.seqNumber === 0) {
        console.log('No prev day: current is initial')
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
        const idx = this.chronodays.findIndex(v => v.id === newDay.id)
        if (idx !== -1) {
          this.chronodays[idx] = newDay
        } else {
          console.error(`Couldn't find day ${newDay.id} in the store to update status to ${newDay.status}`)
        }
        if (this.currDay.id === newDay.id) {
          this.currDay = newDay
        }
      })
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`State check: chrono store isn't loaded`)
    },
    resetState() {
      this.chronodays = []
      this.currDay = defaultCurrDay()
      this.loaded = false
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
