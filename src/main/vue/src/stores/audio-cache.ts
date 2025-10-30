import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MAX_AUDIO_STORAGE_BYTES = 40 * 1024 * 1024 // 40 MB

export interface AudioCacheEntry {
  frontAudio: Blob | undefined
  backAudio: Blob | undefined
  lastAccessedAt: number
  size: number
}

export const useAudioCache = defineStore('audio-cache', () => {
  const audioMap = ref<Map<number, AudioCacheEntry>>(new Map())
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

  function cleanAccessOrder(id: number) {
    const index = accessOrder.value.indexOf(id)
    if (index > -1) {
      accessOrder.value.splice(index, 1)
    }
  }

  function calculateEntrySize(enty: AudioCacheEntry): number {
    return (enty.frontAudio?.size ?? 0) + (enty.backAudio?.size ?? 0)
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

  function addAudio(flashcardId: number, audioBlob: Blob, isFrontSide: boolean) {
    console.log(`Caching audio for flashcard ${flashcardId}, isFrontSide: ${isFrontSide}`)

    const now = performance.now()

    const entry: AudioCacheEntry | undefined = audioMap.value.get(flashcardId) ?? {
      frontAudio: undefined,
      backAudio: undefined,
      lastAccessedAt: now,
      size: 0,
    }

    const oldSize = entry?.size ?? 0

    if (isFrontSide) {
      entry.frontAudio = audioBlob
    } else {
      entry.backAudio = audioBlob
    }

    const newSize = calculateEntrySize(entry)
    const sizeDifference = newSize - oldSize

    evictIfNeeded(sizeDifference)
    audioMap.value.set(flashcardId, entry)
    totalSize.value -= oldSize
    totalSize.value += newSize
    cleanAccessOrder(flashcardId)
    updateAccessOrder(flashcardId, now)
  }

  function getAudio(flashcardId: number, isFrontSide: boolean): Blob | undefined {
    console.log(`Getting cached audio for flashcard ${flashcardId}, isFrontSide: ${isFrontSide}`)

    const entry = audioMap.value.get(flashcardId)
    if (!entry) return undefined

    const now = performance.now()

    // Only update if the access time is significantly different (avoid thrashing)
    if (now - entry.lastAccessedAt > 1000) {
      entry.lastAccessedAt = now
      const index = accessOrder.value.indexOf(flashcardId)
      if (index > -1) {
        accessOrder.value.splice(index, 1)
      }

      updateAccessOrder(flashcardId, now)
    }

    return isFrontSide ? entry.frontAudio : entry.backAudio
  }

  function deleteAudio(flashcardId: number, isFrontSide: boolean): boolean {
    console.log(`Deleting cached audio for flashcard ${flashcardId}, isFrontSide: ${isFrontSide}`)

    const entry = audioMap.value.get(flashcardId)
    if (!entry) return false

    const oldSize = calculateEntrySize(entry)

    if (isFrontSide) {
      entry.frontAudio = undefined
    } else {
      entry.backAudio = undefined
    }

    const newSize = calculateEntrySize(entry)

    if (newSize === 0) {
      totalSize.value -= oldSize
      audioMap.value.delete(flashcardId)
      cleanAccessOrder(flashcardId)
    } else {
      totalSize.value -= oldSize
      totalSize.value += newSize
      entry.size = newSize
    }

    return true
  }

  function resetState() {
    audioMap.value.clear()
    totalSize.value = 0
    accessOrder.value = []
  }

  return {
    storageSize,
    storagePercentage,
    addAudio,
    getAudio,
    deleteAudio,
    resetState,
  }
})
