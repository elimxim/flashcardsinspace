import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePictureCache } from '@/stores/picture-cache.ts'

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
    cache.addPicture(1, blob, true)
    expect(cache.getPicture(1, true)).toBe(blob)
  })

  it('get returns undefined for unknown flashcardId', () => {
    const cache = usePictureCache()
    expect(cache.getPicture(999, true)).toBeUndefined()
  })

  it('get returns undefined for missing side', () => {
    const cache = usePictureCache()
    const blob = makeBlob(100)
    cache.addPicture(1, blob, true)
    expect(cache.getPicture(1, false)).toBeUndefined()
  })

  it('deletePicture removes a side', () => {
    const cache = usePictureCache()
    const blob = makeBlob(100)
    cache.addPicture(1, blob, true)
    cache.deletePicture(1, true)
    expect(cache.getPicture(1, true)).toBeUndefined()
  })

  it('deleting one side leaves other side intact', () => {
    const cache = usePictureCache()
    const front = makeBlob(50)
    const back = makeBlob(75)
    cache.addPicture(1, front, true)
    cache.addPicture(1, back, false)
    cache.deletePicture(1, true)
    expect(cache.getPicture(1, true)).toBeUndefined()
    expect(cache.getPicture(1, false)).toBe(back)
  })

  it('$reset clears all entries', () => {
    const cache = usePictureCache()
    cache.addPicture(1, makeBlob(100), true)
    cache.addPicture(2, makeBlob(200), false)
    cache.$reset()
    expect(cache.getPicture(1, true)).toBeUndefined()
    expect(cache.getPicture(2, false)).toBeUndefined()
  })

  it('evicts oldest entry when total size exceeds cap', () => {
    const cache = usePictureCache()

    // Add 3 large blobs to fill close to / past the 80 MB cap
    const THIRTY_MB = 30 * 1024 * 1024
    const blob1 = makeBlob(THIRTY_MB) // flashcard 1 front — 30 MB
    const blob2 = makeBlob(THIRTY_MB) // flashcard 2 front — 30 MB
    const blob3 = makeBlob(THIRTY_MB) // flashcard 3 front — would push total to 90 MB → evict oldest

    cache.addPicture(1, blob1, true)
    cache.addPicture(2, blob2, true)
    cache.addPicture(3, blob3, true) // should evict flashcard 1

    expect(cache.getPicture(1, true)).toBeUndefined()
    expect(cache.getPicture(2, true)).toBeDefined()
    expect(cache.getPicture(3, true)).toBeDefined()
  })

  it('evicts nothing when total size is within cap', () => {
    const cache = usePictureCache()
    const blob = makeBlob(1000)
    cache.addPicture(1, blob, true)
    cache.addPicture(2, blob, false)
    expect(cache.getPicture(1, true)).toBeDefined()
    expect(cache.getPicture(2, false)).toBeDefined()
  })
})
