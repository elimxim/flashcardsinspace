import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePictureCache } from '@/stores/picture-cache.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'

function makeBlob(sizeBytes: number): Blob {
  return new Blob([new Uint8Array(sizeBytes)])
}

describe('picture-cache', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('add then get returns the same blob', () => {
    const cache = usePictureCache()
    const blob = makeBlob(100)
    cache.addPicture(1, blob, flashcardSides.FRONT)
    expect(cache.getPicture(1, flashcardSides.FRONT)).toBe(blob)
  })

  it('get returns undefined for unknown flashcardId', () => {
    const cache = usePictureCache()
    expect(cache.getPicture(999, flashcardSides.FRONT)).toBeUndefined()
  })

  it('get returns undefined for missing side', () => {
    const cache = usePictureCache()
    const blob = makeBlob(100)
    cache.addPicture(1, blob, flashcardSides.FRONT)
    expect(cache.getPicture(1, flashcardSides.BACK)).toBeUndefined()
  })

  it('deletePicture removes a side', () => {
    const cache = usePictureCache()
    const blob = makeBlob(100)
    cache.addPicture(1, blob, flashcardSides.FRONT)
    cache.deletePicture(1, flashcardSides.FRONT)
    expect(cache.getPicture(1, flashcardSides.FRONT)).toBeUndefined()
  })

  it('deleting one side leaves other side intact', () => {
    const cache = usePictureCache()
    const front = makeBlob(50)
    const back = makeBlob(75)
    cache.addPicture(1, front, flashcardSides.FRONT)
    cache.addPicture(1, back, flashcardSides.BACK)
    cache.deletePicture(1, flashcardSides.FRONT)
    expect(cache.getPicture(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getPicture(1, flashcardSides.BACK)).toBe(back)
  })

  it('$reset clears all entries', () => {
    const cache = usePictureCache()
    cache.addPicture(1, makeBlob(100), flashcardSides.FRONT)
    cache.addPicture(2, makeBlob(200), flashcardSides.BACK)
    cache.$reset()
    expect(cache.getPicture(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getPicture(2, flashcardSides.BACK)).toBeUndefined()
  })

  it('evicts oldest entry when total size exceeds cap', () => {
    const cache = usePictureCache()

    // Add 3 large blobs to fill close to / past the 80 MB cap
    const THIRTY_MB = 30 * 1024 * 1024
    const blob1 = makeBlob(THIRTY_MB) // flashcard 1 front — 30 MB
    const blob2 = makeBlob(THIRTY_MB) // flashcard 2 front — 30 MB
    const blob3 = makeBlob(THIRTY_MB) // flashcard 3 front — would push total to 90 MB → evict oldest

    cache.addPicture(1, blob1, flashcardSides.FRONT)
    cache.addPicture(2, blob2, flashcardSides.FRONT)
    cache.addPicture(3, blob3, flashcardSides.FRONT) // should evict flashcard 1

    expect(cache.getPicture(1, flashcardSides.FRONT)).toBeUndefined()
    expect(cache.getPicture(2, flashcardSides.FRONT)).toBeDefined()
    expect(cache.getPicture(3, flashcardSides.FRONT)).toBeDefined()
  })

  it('evicts nothing when total size is within cap', () => {
    const cache = usePictureCache()
    const blob = makeBlob(1000)
    cache.addPicture(1, blob, flashcardSides.FRONT)
    cache.addPicture(2, blob, flashcardSides.BACK)
    expect(cache.getPicture(1, flashcardSides.FRONT)).toBeDefined()
    expect(cache.getPicture(2, flashcardSides.BACK)).toBeDefined()
  })
})
