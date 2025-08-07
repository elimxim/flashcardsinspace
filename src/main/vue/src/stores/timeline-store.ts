import { defineStore } from 'pinia'
import {
  type Chronoday,
  lightDayStatuses,
  type Timeline,
  timelineStatuses
} from '@/model/timeline.ts'
import { stages } from '@/core-logic/stage-logic.ts'
import { isoDateStr } from '@/utils/date.ts'
import apiClient from '@/api/api-client.ts'
import type {
  ChronodaysGetRequest,
  ChronodaysGetResponse, ChronodaysPostResponse,
  FlashcardTimelineResponse
} from '@/api/communication.ts'
import type { FlashcardSet } from '@/model/flashcard.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts';

export interface TimelineState {
  timeline: Timeline
  chronodays: Chronoday[]
  currDay: Chronoday
}

export const useTimelineStore = defineStore('timeline', {
  state: (): TimelineState => {
    return {
      timeline: {
        id: 0,
        startedAt: new Date().toISOString(),
        status: timelineStatuses.ACTIVE,
      },
      chronodays: [],
      currDay: {
        id: 0,
        chronodate: isoDateStr(new Date()),
        seqNumber: 0,
        stages: [],
        status: lightDayStatuses.INITIAL,
      },
    }
  },
  getters: {
    currLightDayStages(): Set<string> {
      return new Set(this.currDay.stages.map(v => v.name))
    },
  },
  actions: {
    async loadData(flashcardSet: FlashcardSet) {
      try {
        const timelineResponse = await apiClient.get<FlashcardTimelineResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline')
        this.timeline = timelineResponse.data.timeline

        const chronodaysRequest: ChronodaysGetRequest = {
          clientDatetime: new Date().toISOString()
        }

        const chronodaysResponse = await apiClient.get<ChronodaysGetResponse>('/flashcard-sets/' + flashcardSet.id + '/timeline/chronodays', {
          data: chronodaysRequest
        })

        this.chronodays = chronodaysResponse.data.chronodays
        this.currDay = this.chronodays[this.chronodays.length - 1]
      } catch (error) {
        // fixme if timelineResponse failed - it's OKAY = return 404
        // todo catch errors
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
          })
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
        })
        // todo catch errors
      }
    },
  }
})
