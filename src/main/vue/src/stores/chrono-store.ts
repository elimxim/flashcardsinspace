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
    addChronodays(chronodays: Chronoday[], currDay: Chronoday) {
      console.log(`Adding ${chronodays.length} chronodays with curr day ${currDay.chronodate}`)
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
      this.checkState()
      if (this.currDay.seqNumber === 0) {
        console.log('No prev day: current is initial')
        return false
      }

      const prevIdx = this.currDay.seqNumber - 1
      if (!this.chronodays[prevIdx]) {
        console.log(`No prev day: couldn't find prev ${prevIdx}`)
        return false
      }else {
        return true
      }
    },
    canGoNext(): boolean {
      this.checkState()
      return true
    },
    switchToPrevDay() {
      if (this.currDay.seqNumber === 0) {
        console.log("can't switch to prev day: no prev day")
        return
      }
      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet
      if (flashcardSet !== null) {
        const prev = this.chronodays[this.currDay.seqNumber - 1] as Chronoday | undefined
        if (prev !== undefined) {
          console.log(`Switching to prev day ${prev.chronodate}`)
          apiClient.delete('/flashcard-sets/' + flashcardSet.id + '/chrono/' + this.currDay.id)
            // todo log response
            .then(() =>
              this.loadChronodays(flashcardSet)
            )
          // todo catch errors
        } else {
          console.log("can't switch to prev day: no prev day")
        }
      } else {
        console.log("can't switch to prev day: flashcard set is null")
      }
    },
    switchToNextDay() {
      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet
      if (flashcardSet !== null) {
        const next = this.chronodays[this.currDay.seqNumber] as Chronoday | undefined
        if (next !== undefined) {
          console.log(`Switching to next day ${next.chronodate}`)
          apiClient.post<Chronoday>('/flashcard-sets/' + flashcardSet.id + '/chrono')
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
      const days = selectConsecutiveDaysBeforeIncluding(
        this.chronodays, this.currDay, daysFilter
      )

      if (days.length === 0) {
        console.error(`No days to mark as ${status}, flashcard set ID: ${flashcardSet.id}, current day: ${this.currDay.id}`)
        return
      }

      const request: ChronoBulkUpdateRequest = {
        ids: days.map((v): ChronodayId => ({ id: v.id })),
        status: status
      }

      await apiClient.put<Chronoday>('/flashcard-sets/' + flashcardSet.id + '/chrono/bulk', request)
        .then(() =>
          // todo log response
          this.loadChronodays(flashcardSet)
        )
      // todo handle errors
    },
    checkState() {
      if (!this.loaded) {
        throw Error(`State check: chrono store isn't loaded`)
      }
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
