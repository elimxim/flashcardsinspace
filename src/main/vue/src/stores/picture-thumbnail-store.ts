import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

export const usePictureThumbnailStore = defineStore('picture-thumbnail', () => {
  const thumbnails = ref(new Map<string, Blob | null>())
  let flashcardSetId: number | undefined

  function switchSet(setId: number) {
    if (flashcardSetId === setId) return
    Log.log(LogTag.STORE, `picture-thumbnail.switchSet: FlashcardSet.id=${setId}`)
    thumbnails.value = new Map()
    flashcardSetId = setId
  }

  function getThumbnail(flashcardId: number, side: string): Blob | null | undefined {
    return thumbnails.value.get(thumbnailKey(flashcardId, side))
  }

  function setThumbnail(flashcardId: number, side: string, thumbnail: Blob | null) {
    thumbnails.value.set(thumbnailKey(flashcardId, side), thumbnail)
  }

  function deleteThumbnail(flashcardId: number, side: string) {
    Log.log(
      LogTag.STORE,
      `picture-thumbnail.deleteThumbnail: Flashcard.id=${flashcardId}, side=${side}`,
    )
    thumbnails.value.delete(thumbnailKey(flashcardId, side))
  }

  function $reset() {
    thumbnails.value = new Map()
    flashcardSetId = undefined
  }

  return { switchSet, getThumbnail, setThumbnail, deleteThumbnail, $reset }
})

function thumbnailKey(flashcardId: number, side: string): string {
  return `${flashcardId}-${side}`
}
