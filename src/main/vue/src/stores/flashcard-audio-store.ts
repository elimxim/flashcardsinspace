import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MAX_AUDIO_STORAGE_BYTES = 40 * 1024 * 1024 // 40 MB

export interface BlobEntry {
  audio: BlobPair
  lastAccessedAt: number
  size: number
}

export interface BlobPair {
  frontSide: Blob | undefined
  frontSideMimeType: string | undefined
  backSide: Blob | undefined
  backSideMimeType: string | undefined
}

export const useFlashcardAudioStore = defineStore('flashcard-audio', () => {
  const audioMap = ref<Map<number, BlobEntry>>(new Map())
  const totalSize = ref(0)
  const accessOrder = ref<number[]>([]) // oldest first

  const storageSize = computed(() => totalSize.value)
  const storagePercentage = computed(() =>
    (totalSize.value / MAX_AUDIO_STORAGE_BYTES) * 100
  )

  function updateAccessOrder(id: number, accessTime: number) {
    // Binary search to find the insertion point
    let left = 0
    let right = accessOrder.value.length

    while (left < right) {
      const mid = Math.floor((left + right) / 2)
      const midId = accessOrder.value[mid]
      const midEntry = audioMap.value.get(midId)

      if (midEntry && midEntry.lastAccessedAt < accessTime) {
        left = mid + 1
      } else {
        right = mid
      }
    }

    accessOrder.value.splice(left, 0, id)
  }

  function calculateEntrySize(pair: BlobPair): number {
    return (pair.frontSide?.size ?? 0) + (pair.backSide?.size ?? 0)
  }

  function evictIfNeeded(requiredSize: number) {
    let availableSpace = MAX_AUDIO_STORAGE_BYTES - totalSize.value

    // Evict entries until we have enough space
    while (availableSpace < requiredSize && accessOrder.value.length > 0) {
      const orderNumber = accessOrder.value.shift()
      if (orderNumber !== undefined) {
        const entry = audioMap.value.get(orderNumber)
        if (entry) {
          totalSize.value -= entry.size
          availableSpace += entry.size
          audioMap.value.delete(orderNumber)
        }
      }
    }
  }

  function addAudio(id: number, audioBlob: Blob, mimeType: string | undefined, isFrontSide: boolean) {
    const now = performance.now()

    const blobEntry: BlobEntry | undefined = audioMap.value.get(id)
    const oldSize = blobEntry?.size ?? 0

    const blobPair: BlobPair = blobEntry?.audio ?? {
      frontSide: undefined,
      frontSideMimeType: undefined,
      backSide: undefined,
      backSideMimeType: undefined,
    }

    if (isFrontSide) {
      blobPair.frontSide = audioBlob
      blobPair.frontSideMimeType = mimeType
    } else {
      blobPair.backSide = audioBlob
      blobPair.backSideMimeType = mimeType
    }

    const newSize = calculateEntrySize(blobPair)
    const sizeDifference = newSize - oldSize

    if (blobEntry) {
      totalSize.value -= oldSize
      const index = accessOrder.value.indexOf(id)
      if (index > -1) {
        accessOrder.value.splice(index, 1)
      }
    }

    evictIfNeeded(sizeDifference)

    audioMap.value.set(id, {
      audio: blobPair,
      lastAccessedAt: now,
      size: newSize,
    })

    totalSize.value += newSize

    updateAccessOrder(id, now)
  }

  function getAudio(id: number, isFrontSide: boolean): Blob | undefined {
    const entry = audioMap.value.get(id)
    if (!entry) return undefined

    const now = performance.now()

    // Only update if the access time is significantly different (avoid thrashing)
    if (now - entry.lastAccessedAt > 1000) {
      entry.lastAccessedAt = now
      const index = accessOrder.value.indexOf(id)
      if (index > -1) {
        accessOrder.value.splice(index, 1)
      }

      updateAccessOrder(id, now)
    }

    return isFrontSide ? entry.audio.frontSide : entry.audio.backSide
  }

  function invalidateAll() {
    audioMap.value.clear()
    totalSize.value = 0
    accessOrder.value = []
  }

  return {
    storageSize,
    storagePercentage,
    addAudio,
    getAudio,
    invalidateAll,
  }
})
