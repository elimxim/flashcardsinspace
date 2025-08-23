import { defineStore } from 'pinia'
import {
  type Chronoday
} from '@/model/chrono.ts'
import { asIsoDateStr } from '@/utils/date.ts'
import apiClient from '@/api/api-client.ts'
import type {
  ChronodaysGetParams,
  ChronodaysGetResponse,
  ChronodaysResponse,
  ChronodaysPutRequest,
} from '@/api/communication.ts'
import type { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  chronodayStatuses,
  isCompleteAvailable, isInProgressAvailable,
} from '@/core-logic/calendar-logic.ts'

export interface ChronoState {
  chronodays: Chronoday[]
  currDay: Chronoday
}

export const useChronoStore = defineStore('chrono', {
  state: (): ChronoState => {
    return {
      chronodays: [],
      currDay: defaultCurrDay(),
    }
  },
  getters: {
    currDayStages(): Set<string> {
      return new Set(this.currDay.stages)
    },
    isInitialized(): boolean {
      return this.chronodays.length === 0
    }
  },
  actions: {
    async loadData(flashcardSet: FlashcardSet): Promise<void> {
      this.resetState()
      await this.loadChronodays(flashcardSet)
    },
    resetState() {
      this.chronodays = []
      this.currDay = defaultCurrDay()
    },
    async loadChronodays(flashcardSet: FlashcardSet): Promise<void> {
      const chronodaysRequest: ChronodaysGetParams = {
        clientDatetime: new Date().toISOString()
      }

      await apiClient.put<ChronodaysGetResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays', chronodaysRequest)
        .then(response => {
          this.chronodays = response.data.chronodays
          this.currDay = response.data.currDay
        })
      // todo catch errors
    },
    async addInitialChronoday(flashcardSet: FlashcardSet): Promise<boolean> {
      if (!this.isInitialized) {
        console.log(`Adding initial chronoday for flashcard set ${flashcardSet.id}`)
        await apiClient.post<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays?initial=true')
          .then(response => {
            console.log(`Added initial chronoday ${response.data.chronoday.chronodate} for flashcard set ${flashcardSet.id}`)
          })
          // todo catch errors
          .then(async () => {
            await this.loadData(flashcardSet)
            console.log(`Reloaded chrono data for flashcard set ${flashcardSet.id}`)
          })
        return true
      } else {
        return false
      }
    },
    switchToPrevDay() {
      if (this.currDay.seqNumber === 0) return // todo log
      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet
      if (flashcardSet !== null) {
        const prev = this.chronodays[this.currDay.seqNumber - 1] as Chronoday | undefined
        if (prev !== undefined) {
          apiClient.delete('/flashcard-sets/' + flashcardSet.id + '/chronodays/' + this.currDay.id)
            // todo log response
            .then(() =>
              this.loadChronodays(flashcardSet)
            )
          // todo catch errors
        } else {
          // todo smth
        }
      } else {
        // todo smth
      }
    },
    switchToNextDay() {
      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet
      if (flashcardSet !== null) {
        const next = this.chronodays[this.currDay.seqNumber] as Chronoday | undefined
        if (next !== undefined) {
          apiClient.post<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays')
            // todo log response
            .then(() =>
              this.loadChronodays(flashcardSet)
            )
          // todo catch errors
        } else {
          // todo do smth
        }
      } else {
        // todo smth
      }
    },
    async markCurrDayAsInProgress(flashcardSet: FlashcardSet) {
      if (isInProgressAvailable(this.currDay)) {
        const request: ChronodaysPutRequest = {
          chronodayStatus: chronodayStatuses.IN_PROGRESS
        }
        await apiClient.put<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays/' + this.currDay.id, request)
          // todo log response
          .then(() =>
            this.loadChronodays(flashcardSet)
          )
      }
    },
    async markCurrDayAsCompleted(flashcardSet: FlashcardSet) {
      if (isCompleteAvailable(this.currDay)) {
        const request: ChronodaysPutRequest = {
          chronodayStatus: chronodayStatuses.COMPLETED
        }
        await apiClient.put<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays/' + this.currDay.id, request)
          // todo log response
          .then(() =>
            this.loadChronodays(flashcardSet)
          )
        // todo handle errors
      }
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
