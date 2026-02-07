import { defineStore } from 'pinia'
import { FlashcardAudioMetadata } from '@/model/flashcard.ts'
import { Log, LogTag } from '@/utils/logger.ts'

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
      Log.log(LogTag.STORE, `audio.loadState: ${audioMetadata.length} metadata size`)
      this.$reset()
      this.audioIdMap = new Map(
        audioMetadata.map(metadata => [
          audioMapKey(metadata.flashcardId, metadata.flashcardSide),
          metadata.audioId
        ])
      )
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`audio.checkStateLoaded: !loaded`)
    },
    setAudioId(flashcardId: number, side: string, audioId: number) {
      Log.log(LogTag.STORE, `audio.setAudioId: Audio.id=${audioId}, Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      this.checkStateLoaded()
      this.audioIdMap.set(audioMapKey(flashcardId, side), audioId)
    },
    getAudioId(flashcardId: number | undefined, side: string): number | undefined {
      Log.log(LogTag.STORE, `audio.getAudioId: Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      if (!flashcardId) return undefined
      this.checkStateLoaded()
      return this.audioIdMap.get(audioMapKey(flashcardId, side))
    },
    removeAudioId(flashcardId: number, side: string) {
      Log.log(LogTag.STORE, `audio.removeAudioId: Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      this.checkStateLoaded()
      this.audioIdMap.delete(audioMapKey(flashcardId, side))
    },
  }
})

const audioMapKey: (flashcardId: number, side: string) => string =
  (flashcardId, side) => `${flashcardId}-${side}`
