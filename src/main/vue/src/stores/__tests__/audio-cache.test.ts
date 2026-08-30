import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'

function makeBlob(sizeBytes: number): Blob {
  return new Blob([new Uint8Array(sizeBytes)])
}

describe('audio-cache', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('add then get returns the same blob', () => {
    const cache = useAudioCache()
    const blob = makeBlob(100)

    cache.addAudio(1, blob, flashcardSides.FRONT)

    expect(cache.getAudio(1, flashcardSides.FRONT)).toBe(blob)
  })

  it('get returns undefined for unknown flashcardId', () => {
    const cache = useAudioCache()

    expect(cache.getAudio(999, flashcardSides.FRONT)).toBeUndefined()
  })

  it('get returns undefined for missing side', () => {
    const cache = useAudioCache()

    cache.addAudio(1, makeBlob(100), flashcardSides.FRONT)

    expect(cache.getAudio(1, flashcardSides.BACK)).toBeUndefined()
  })

  it('deleting one side leaves other side intact', () => {
    const cache = useAudioCache()
    const front = makeBlob(50)
    const back = makeBlob(75)

    cache.addAudio(1, front, flashcardSides.FRONT)
    cache.addAudio(1, back, flashcardSides.BACK)
    cache.deleteAudio(1, flashcardSides.FRONT)

    expect(cache.getAudio(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getAudio(1, flashcardSides.BACK)).toBe(back)
  })

  it('$reset clears all entries', () => {
    const cache = useAudioCache()

    cache.addAudio(1, makeBlob(100), flashcardSides.FRONT)
    cache.addAudio(2, makeBlob(200), flashcardSides.BACK)
    cache.$reset()

    expect(cache.getAudio(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getAudio(2, flashcardSides.BACK)).toBeUndefined()
  })

  it('counts both sides of a flashcard once', () => {
    const cache = useAudioCache()

    cache.addAudio(1, makeBlob(100), flashcardSides.FRONT)
    cache.addAudio(1, makeBlob(200), flashcardSides.BACK)

    expect(cache.storageSize).toBe(300)
  })

  it('evicts oldest entry when total size exceeds cap', () => {
    const cache = useAudioCache()
    // 3 blobs of 15 MB against the 40 MB cap: the third one evicts the oldest
    const FIFTEEN_MB = 15 * 1024 * 1024

    cache.addAudio(1, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)
    cache.addAudio(2, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)
    cache.addAudio(3, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)

    expect(cache.getAudio(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getAudio(2, flashcardSides.FRONT)).toBeDefined()
    expect(cache.getAudio(3, flashcardSides.FRONT)).toBeDefined()
  })

  it('reclaims accounted space when evicting', () => {
    const cache = useAudioCache()
    // 3 blobs of 15 MB against the 40 MB cap: the third one evicts the oldest
    const FIFTEEN_MB = 15 * 1024 * 1024

    cache.addAudio(1, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)
    cache.addAudio(2, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)
    cache.addAudio(3, makeBlob(FIFTEEN_MB), flashcardSides.FRONT)

    // one entry was evicted, so exactly two remain accounted for
    expect(cache.storageSize).toBe(2 * FIFTEEN_MB)
  })

  it('evicts nothing when total size is within cap', () => {
    const cache = useAudioCache()
    const blob = makeBlob(1000)

    cache.addAudio(1, blob, flashcardSides.FRONT)
    cache.addAudio(2, blob, flashcardSides.BACK)

    expect(cache.getAudio(1, flashcardSides.FRONT)).toBeDefined()
    expect(cache.getAudio(2, flashcardSides.BACK)).toBeDefined()
  })
})
