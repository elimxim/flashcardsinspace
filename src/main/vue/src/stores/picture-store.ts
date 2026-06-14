import { defineStore } from 'pinia'
import { FlashcardPictureMetadata } from '@/model/flashcard.ts'
import { Log, LogTag } from '@/utils/logger.ts'

export interface PictureState {
  pictureIdMap: Map<string, number>
  loaded: boolean
}

export const usePictureStore = defineStore('picture', {
  state: (): PictureState => {
    return {
      pictureIdMap: new Map(),
      loaded: false,
    }
  },
  actions: {
    loadState(pictureMetadata: FlashcardPictureMetadata[]) {
      Log.log(LogTag.STORE, `picture.loadState: ${pictureMetadata.length} metadata size`)
      this.$reset()
      this.pictureIdMap = new Map(
        pictureMetadata.map(metadata => [
          pictureMapKey(metadata.flashcardId, metadata.flashcardSide),
          metadata.pictureId
        ])
      )
      this.loaded = true
    },
    checkStateLoaded() {
      if (!this.loaded) throw Error(`picture.checkStateLoaded: !loaded`)
    },
    setPictureId(flashcardId: number, side: string, pictureId: number) {
      Log.log(LogTag.STORE, `picture.setPictureId: Picture.id=${pictureId}, Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      this.checkStateLoaded()
      const key = pictureMapKey(flashcardId, side)
      this.pictureIdMap.set(key, pictureId)
    },
    getPictureId(flashcardId: number | undefined, side: string): number | undefined {
      Log.log(LogTag.STORE, `picture.getPictureId: Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      if (!flashcardId) return undefined
      this.checkStateLoaded()
      return this.pictureIdMap.get(pictureMapKey(flashcardId, side))
    },
    removePictureId(flashcardId: number, side: string) {
      Log.log(LogTag.STORE, `picture.removePictureId: Flashcard.id=${flashcardId}, Flashcard.side=${side}`)
      this.checkStateLoaded()
      const key = pictureMapKey(flashcardId, side)
      this.pictureIdMap.delete(key)
    },
  }
})

const pictureMapKey: (flashcardId: number, side: string) => string =
  (flashcardId, side) => `${flashcardId}-${side}`
