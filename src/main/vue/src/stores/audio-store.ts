import { defineStore } from 'pinia'
import { FlashcardAudioMetadata } from '@/model/flashcard.ts'

export interface AudioState {
  audioIdMap: Map<string, number>
  loaded: boolean
}

export const useAudioStore = defineStore('audio', {
  state: (): AudioState => {
    return {
      audioIdMap: new Map(),
      loaded: false,
    }
  },
  actions: {
    loadState(audioMetadata: FlashcardAudioMetadata[]) {
      this.resetState()
      this.audioIdMap = new Map(
        audioMetadata.map(metadata => [
          audioMapKey(metadata.flashcardId, metadata.flashcardSide),
          metadata.audioId
        ])
      )
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`State check: audio store isn't loaded`)
    },
    resetState() {
      this.audioIdMap.clear()
      this.loaded = false
    },
    setAudioId(flashcardId: number, side: string, audioId: number) {
      console.log(`Setting audio id ${audioId} for flashcard ${flashcardId} side ${side}`)
      this.checkStateLoaded()
      this.audioIdMap.set(audioMapKey(flashcardId, side), audioId)
    },
    getAudioId(flashcardId: number | undefined, side: string): number | undefined {
      console.log(`Getting audio id for flashcard ${flashcardId} side ${side}`)
      if (!flashcardId) return undefined
      this.checkStateLoaded()
      return this.audioIdMap.get(audioMapKey(flashcardId, side))
    },
    removeAudioId(flashcardId: number, side: string) {
      console.log(`Removing audio id for flashcard ${flashcardId} side ${side}`)
      this.checkStateLoaded()
      this.audioIdMap.delete(audioMapKey(flashcardId, side))
    },
  }
})

const audioMapKey: (flashcardId: number, side: string) => string =
  (flashcardId, side) => `${flashcardId}-${side}`
