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
  ChronodaysPostResponse,
  TimelinePostRequest,
  TimelineResponse
} from '@/api/communication.ts'
import type { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { chronodayStatuses, timelineStatuses } from '@/core-logic/calendar-logic.ts'
import { computed } from 'vue';

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
      currDay: {
        id: 0,
        chronodate: asIsoDateStr(new Date()),
        seqNumber: 0,
        stages: [],
        status: chronodayStatuses.INITIAL,
      },
    }
  },
  getters: {
    currLightDayStages(): Set<string> {
      return new Set(this.currDay.stages)
    },
  },
  actions: {
    async loadData(flashcardSet: FlashcardSet): Promise<void> {
      await this.loadTimeline(flashcardSet).then(() =>
        this.loadChronodays(flashcardSet)
      )
    },
    async startTimeline(flashcardSet: FlashcardSet) {
      await this.createTimeline(flashcardSet).then(() =>
        this.loadChronodays(flashcardSet)
      )
    },
    async loadTimeline(flashcardSet: FlashcardSet): Promise<void> {
      await apiClient.get<TimelineResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline')
        .then(response => {
          this.timeline = response.data.timeline
        })
        .catch(error => {
          if (error.response.status === 404) {
            // todo OK
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
    switchToNextDay() {
      if (this.currDay.seqNumber === this.chronodays.length - 1) return

      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet as FlashcardSet
      if (flashcardSet !== null) {
        const next = this.chronodays[this.currDay.seqNumber] as Chronoday | undefined
        if (next !== undefined) {
          apiClient.post<ChronodaysPostResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays').then(response => {
            this.currDay = response.data.chronoday
          }).then(() =>
            this.loadChronodays(flashcardSet)
          )
          // todo catch errors
        } else {
          // todo do smth
        }
      }
    },
    switchToPrevDay() {
      if (this.currDay.seqNumber === 0) return

      const flashcardSetStore = useFlashcardSetStore()
      const flashcardSet = flashcardSetStore.flashcardSet as FlashcardSet
      if (flashcardSet !== null) {
        apiClient.delete('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays/' + this.currDay.id).then(response => {
          const prev = this.chronodays[this.currDay.seqNumber - 1] as Chronoday | undefined
          if (prev !== undefined) {
            this.currDay = prev
          }
        }).then(() =>
          this.loadChronodays(flashcardSet)
        )
        // todo catch errors
      } else {
        // todo smth
      }
    },
  }
})
