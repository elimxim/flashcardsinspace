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
  isCompleteAvailable,
  isInProgressAvailable,
  selectConsecutiveDaysBeforeIncluding,
} from '@/core-logic/chrono-logic.ts'

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
    async addInitialChronoday(flashcardSet: FlashcardSet): Promise<void> {
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
    async markLastDaysAsInProgress(flashcardSet: FlashcardSet) {
      if (isInProgressAvailable(this.currDay)) {
        await this.markDaysAs(flashcardSet, chronodayStatuses.IN_PROGRESS, isInProgressAvailable)
      }
    },
    async markLastDaysAsCompleted(flashcardSet: FlashcardSet) {
      if (isCompleteAvailable(this.currDay)) {
        await this.markDaysAs(flashcardSet, chronodayStatuses.COMPLETED, isCompleteAvailable)
      }
    },
    async markDaysAs(
      flashcardSet: FlashcardSet,
      status: string,
      daysFilter: (chronoday: Chronoday) => boolean,
    ) {
      const request: ChronodaysPutRequest = {
        status: status
      }
      const days = selectConsecutiveDaysBeforeIncluding(
        this.chronodays, this.currDay, daysFilter
      )

      if (days.length === 0) {
        console.error(`No days to mark as ${status}, flashcard set ID: ${flashcardSet.id}, current day: ${this.currDay.id}`)
        return
      }

      const idsStr = days.map(v => v.id).join(',')
      await apiClient.put<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/chronodays/bulk',
        request,
        {
          params: {
            ids: idsStr,
          },
        })
        // todo log response
        .then(() =>
          this.loadChronodays(flashcardSet)
        )
      // todo handle errors
    }
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
