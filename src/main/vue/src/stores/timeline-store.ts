import { defineStore, storeToRefs } from 'pinia'
import {
  type Chronoday,
  type Timeline
} from '@/model/timeline.ts'
import { asIsoDateStr } from '@/utils/date.ts'
import apiClient from '@/api/api-client.ts'
import type {
  ChronodaysGetParams,
  ChronodaysGetResponse,
  ChronodaysResponse, ChronodaysPutRequest,
  TimelinePostRequest,
  TimelineResponse
} from '@/api/communication.ts'
import type { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import {
  chronodayStatuses,
  isCompleteAvailable, isInProgressAvailable,
} from '@/core-logic/calendar-logic.ts'

export interface TimelineState {
  timeline: Timeline | null
  chronodays: Chronoday[]
  currDay: Chronoday
}

export const useTimelineStore = defineStore('timeline', {
  state: (): TimelineState => {
    return {
      timeline: null,
      chronodays: [],
      currDay: defaultCurrDay(),
    }
  },
  getters: {
    currLightDayStages(): Set<string> {
      return new Set(this.currDay.stages)
    },
  },
  actions: {
    async loadData(flashcardSet: FlashcardSet): Promise<void> {
      this.resetState()
      await this.loadTimeline(flashcardSet).then(() =>
        this.loadChronodays(flashcardSet)
      )
    },
    async startTimeline(flashcardSet: FlashcardSet) {
      this.resetState()
      await this.createTimeline(flashcardSet).then(() =>
        this.loadChronodays(flashcardSet)
      )
    },
    resetState() {
      this.timeline = null
      this.chronodays = []
      this.currDay = defaultCurrDay()
    },
    async loadTimeline(flashcardSet: FlashcardSet): Promise<void> {
      await apiClient.get<TimelineResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline')
        .then(response => {
          this.timeline = response.data.timeline
        })
        .catch(error => {
          if (error.response.status === 404) {
            console.log('Timeline is not set yet for Flashcard Set ' + flashcardSet.id)
            this.timeline = null
          } else {
            // todo not OK
          }
        })
    },
    async loadChronodays(flashcardSet: FlashcardSet): Promise<void> {
      const chronodaysRequest: ChronodaysGetParams = {
        clientDatetime: new Date().toISOString()
      }

      await apiClient.get<ChronodaysGetResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays', {
        params: chronodaysRequest
      }).then(response => {
        this.chronodays = response.data.chronodays
        this.currDay = response.data.currDay
      })
      // todo catch errors
    },
    async createTimeline(flashcardSet: FlashcardSet): Promise<void> {
      if (this.timeline === null) {
        const request: TimelinePostRequest = {
          clientDatetime: new Date(),
        }

        await apiClient.post<TimelineResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline', request).then(response => {
          this.timeline = response.data.timeline
        })
      } else {
        // todo else
      }
    },
    switchToPrevDay() {
      if (this.currDay.seqNumber === 0) return // todo log
      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet
      if (flashcardSet !== null) {
        const prev = this.chronodays[this.currDay.seqNumber - 1] as Chronoday | undefined
        if (prev !== undefined) {
          apiClient.delete('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays/' + this.currDay.id)
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
          apiClient.post<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays')
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
        await apiClient.put<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays/' + this.currDay.id, request)
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
        await apiClient.put<ChronodaysResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays/' + this.currDay.id, request)
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
