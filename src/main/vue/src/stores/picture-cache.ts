import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'

const MAX_PICTURE_STORAGE_BYTES = 80 * 1024 * 1024 // 80 MB

export interface PictureCacheEntry {
  frontPicture: Blob | undefined
  backPicture: Blob | undefined
  lastAccessedAt: number
  size: number
}

export const usePictureCache = defineStore('picture-cache', () => {
  const pictureMap = ref<Map<number, PictureCacheEntry>>(new Map())
  const totalSize = ref(0)
  const accessOrder = ref<number[]>([]) // oldest first

  const storageSize = computed(() => totalSize.value)
  const storagePercentage = computed(() =>
    (totalSize.value / MAX_PICTURE_STORAGE_BYTES) * 100
  )

  function updateAccessOrder(id: number, accessTime: number) {
    let left = 0
    let right = accessOrder.value.length

    while (left < right) {
      const mid = Math.floor((left + right) / 2)
      const midId = accessOrder.value[mid]
      const midEntry = pictureMap.value.get(midId)

      if (midEntry && midEntry.lastAccessedAt < accessTime) {
        left = mid + 1
      } else {
        right = mid
      }
    }

    accessOrder.value.splice(left, 0, id)
  }

  function cleanAccessOrder(id: number) {
    const index = accessOrder.value.indexOf(id)
    if (index > -1) {
      accessOrder.value.splice(index, 1)
    }
  }

  function calculateEntrySize(entry: PictureCacheEntry): number {
    return (entry.frontPicture?.size ?? 0) + (entry.backPicture?.size ?? 0)
  }

  function evictIfNeeded(requiredSize: number) {
    let availableSpace = MAX_PICTURE_STORAGE_BYTES - totalSize.value

    while (availableSpace < requiredSize && accessOrder.value.length > 0) {
      const orderNumber = accessOrder.value.shift()
      if (orderNumber !== undefined) {
        const entry = pictureMap.value.get(orderNumber)
        if (entry) {
          totalSize.value -= entry.size
          availableSpace += entry.size
          pictureMap.value.delete(orderNumber)
        }
      }
    }
  }

  function printCacheSize() {
    const sizeInKB = (totalSize.value / 1024).toFixed(1)
    Log.log(LogTag.STORE, `picture-cache.cacheSize: ${sizeInKB} KB`)
  }

  function sizeInKB(blob: Blob | undefined): string {
    return (blob?.size ?? 0 / 1024).toFixed(1)
  }

  function addPicture(flashcardId: number, pictureBlob: Blob, side: string) {
    Log.log(LogTag.STORE, `picture-cache.addPicture: Flashcard.id=${flashcardId}, side=${side}, Picture.size=${sizeInKB(pictureBlob)}`)

    const now = performance.now()

    const entry: PictureCacheEntry = pictureMap.value.get(flashcardId) ?? {
      frontPicture: undefined,
      backPicture: undefined,
      lastAccessedAt: now,
      size: 0,
    }

    const oldSize = entry.size

    if (side === flashcardSides.FRONT) {
      entry.frontPicture = pictureBlob
    } else {
      entry.backPicture = pictureBlob
    }

    const newSize = calculateEntrySize(entry)
    const sizeDifference = newSize - oldSize

    evictIfNeeded(sizeDifference)
    entry.size = newSize
    pictureMap.value.set(flashcardId, entry)
    totalSize.value -= oldSize
    totalSize.value += newSize
    cleanAccessOrder(flashcardId)
    updateAccessOrder(flashcardId, now)
    printCacheSize()
  }

  function getPicture(flashcardId: number, side: string): Blob | undefined {
    Log.log(LogTag.STORE, `picture-cache.getPicture: Flashcard.id=${flashcardId}, side=${side}`)

    const entry = pictureMap.value.get(flashcardId)
    if (!entry) return undefined

    const now = performance.now()

    if (now - entry.lastAccessedAt > 1000) {
      entry.lastAccessedAt = now
      const index = accessOrder.value.indexOf(flashcardId)
      if (index > -1) {
        accessOrder.value.splice(index, 1)
      }
      updateAccessOrder(flashcardId, now)
    }
    printCacheSize()

    return side === flashcardSides.FRONT ? entry.frontPicture : entry.backPicture
  }

  function deletePicture(flashcardId: number, side: string): boolean {
    Log.log(LogTag.STORE, `picture-cache.deletePicture: Flashcard.id=${flashcardId}, side=${side}`)

    const entry = pictureMap.value.get(flashcardId)
    if (!entry) return false

    const oldSize = calculateEntrySize(entry)

    if (side === flashcardSides.FRONT) {
      entry.frontPicture = undefined
    } else {
      entry.backPicture = undefined
    }

    const newSize = calculateEntrySize(entry)

    if (newSize === 0) {
      totalSize.value -= oldSize
      pictureMap.value.delete(flashcardId)
      cleanAccessOrder(flashcardId)
    } else {
      totalSize.value -= oldSize
      totalSize.value += newSize
      entry.size = newSize
    }
    printCacheSize()

    return true
  }

  function $reset() {
    pictureMap.value.clear()
    totalSize.value = 0
    accessOrder.value = []
  }

  return {
    storageSize,
    storagePercentage,
    addPicture,
    getPicture,
    deletePicture,
    $reset,
  }
})
