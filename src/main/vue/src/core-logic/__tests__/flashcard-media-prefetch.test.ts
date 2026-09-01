import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { AxiosHeaders, AxiosResponse } from 'axios'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { Flashcard } from '@/model/flashcard.ts'
import { learningStages } from '@/core-logic/stage-logic.ts'
import {
  createMediaPrefetcher,
  FlashcardMediaPrefetcher,
} from '@/core-logic/flashcard-media-prefetch.ts'
import { sendFlashcardAudioGetRequest, sendFlashcardPictureGetRequest } from '@/api/api-client.ts'

vi.mock('@/api/api-client.ts', () => ({
  sendFlashcardAudioGetRequest: vi.fn(),
  sendFlashcardPictureGetRequest: vi.fn(),
}))

const SET_ID = 40

const audioGet = vi.mocked(sendFlashcardAudioGetRequest)
const pictureGet = vi.mocked(sendFlashcardPictureGetRequest)

function blobResponse(blob: Blob = new Blob(['media'])): AxiosResponse<Blob> {
  return {
    data: blob,
    status: 200,
    statusText: 'OK',
    headers: { 'x-audio-id': '1' },
    config: { headers: new AxiosHeaders() },
  }
}

function flashcard(id: number): Flashcard {
  return {
    id: id,
    frontSide: undefined,
    backSide: undefined,
    stage: learningStages.S1.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: new Date().toString(),
  }
}

function loadMediaMetadata(flashcardIds: number[]) {
  useAudioStore().loadState(
    flashcardIds.map((id) => ({
      audioId: id * 10,
      flashcardId: id,
      flashcardSide: flashcardSides.FRONT,
    })),
  )
  usePictureStore().loadState(
    flashcardIds.map((id) => ({
      pictureId: id * 100,
      flashcardId: id,
      flashcardSide: flashcardSides.BACK,
      width: 10,
      height: 10,
    })),
  )
}

describe('flashcard-media-prefetch', () => {
  let prefetcher: FlashcardMediaPrefetcher
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    audioGet.mockResolvedValue(blobResponse())
    pictureGet.mockResolvedValue(blobResponse())
    prefetcher = createMediaPrefetcher(SET_ID)
  })

  afterEach(() => {
    prefetcher.dispose()
  })

  it('should not touch the network for a flashcard without media', async () => {
    loadMediaMetadata([])

    const media = await prefetcher.media(1)

    expect(audioGet).not.toHaveBeenCalled()
    expect(pictureGet).not.toHaveBeenCalled()
    expect(media.frontAudio).toBeUndefined()
    expect(media.error).toBeUndefined()
  })

  it('should request only the sides a flashcard actually has', async () => {
    loadMediaMetadata([1])

    const media = await prefetcher.media(1)

    expect(audioGet).toHaveBeenCalledTimes(1)
    expect(audioGet).toHaveBeenCalledWith(SET_ID, 1, flashcardSides.FRONT, expect.anything())
    expect(pictureGet).toHaveBeenCalledTimes(1)
    expect(pictureGet).toHaveBeenCalledWith(SET_ID, 1, flashcardSides.BACK, expect.anything())
    expect(media.frontAudio).toBeDefined()
    expect(media.backPicture).toBeDefined()
    expect(media.backAudio).toBeUndefined()
    expect(media.frontPicture).toBeUndefined()
  })

  it('should fetch a flashcard exactly once however often it is asked for', async () => {
    loadMediaMetadata([1])

    const first = prefetcher.media(1)
    const second = prefetcher.media(1)
    await Promise.all([first, second, prefetcher.media(1)])

    // there is nothing to de-duplicate, one owner means one request
    expect(first).toBe(second)
    expect(audioGet).toHaveBeenCalledTimes(1)
  })

  it('should serve the flashcard on screen from work the window already started', async () => {
    loadMediaMetadata([1, 2])
    let resolveAudio: (response: AxiosResponse<Blob>) => void = () => {}
    audioGet.mockReturnValue(
      new Promise((resolve) => {
        resolveAudio = resolve
      }),
    )

    // the window starts flashcard 1, then the user reaches it before it lands
    prefetcher.slide(undefined, [flashcard(1), flashcard(2)], [])
    const foreground = prefetcher.media(1)
    resolveAudio(blobResponse())

    expect((await foreground).frontAudio).toBeDefined()
    expect(audioGet.mock.calls.filter((call) => call[1] === 1).length).toBe(1)
  })

  it('should fetch one flashcard at a time', async () => {
    loadMediaMetadata([1, 2, 3])
    const inFlight = new Set<number>()
    let maxInFlight = 0
    const track = async (flashcardId: number) => {
      inFlight.add(flashcardId)
      maxInFlight = Math.max(maxInFlight, inFlight.size)
      return new Promise<AxiosResponse<Blob>>((resolve) =>
        setTimeout(resolve, 0, blobResponse()),
      ).finally(() => inFlight.delete(flashcardId))
    }
    audioGet.mockImplementation((_setId, flashcardId) => track(flashcardId))
    pictureGet.mockImplementation((_setId, flashcardId) => track(flashcardId))

    prefetcher.slide(undefined, [flashcard(1), flashcard(2), flashcard(3)], [])
    await prefetcher.media(3)

    // a deeper window lengthens the lookahead, never the concurrency
    expect(maxInFlight).toBe(1)
  })

  it('should keep only the current flashcard and the window', async () => {
    loadMediaMetadata([1, 2, 3, 4])

    prefetcher.slide(flashcard(1), [flashcard(2), flashcard(3)], [])
    await prefetcher.media(1)
    prefetcher.slide(flashcard(2), [flashcard(3), flashcard(4)], [])
    await prefetcher.media(2)

    // the map cannot grow with the length of the session
    expect(prefetcher.size()).toBe(3)
  })

  it('should pick up edited media after a flashcard is forgotten', async () => {
    loadMediaMetadata([1])
    const before = await prefetcher.media(1)

    // an upload replaces the blob in the cache, as uploadFlashcardAudioBlob does
    const edited = new Blob(['edited'])
    useAudioCache().addAudio(1, edited, flashcardSides.FRONT)
    prefetcher.forget(1)

    expect(before.frontAudio).not.toBe(edited)
    expect((await prefetcher.media(1)).frontAudio).toBe(edited)
  })

  it('should keep serving the stale blob until a flashcard is forgotten', async () => {
    loadMediaMetadata([1])
    const before = await prefetcher.media(1)

    useAudioCache().addAudio(1, new Blob(['edited']), flashcardSides.FRONT)

    expect((await prefetcher.media(1)).frontAudio).toBe(before.frontAudio)
  })

  it('should report a failure without throwing', async () => {
    loadMediaMetadata([1])
    const error = new Error('boom')
    audioGet.mockRejectedValue(error)

    const media = await prefetcher.media(1)

    expect(media.error).toBe(error)
    expect(media.frontAudio).toBeUndefined()
  })

  it('should keep the queue moving after a failure', async () => {
    loadMediaMetadata([1, 2])
    audioGet.mockImplementation((_setId, flashcardId) =>
      flashcardId === 1 ? Promise.reject(new Error('boom')) : Promise.resolve(blobResponse()),
    )

    prefetcher.slide(undefined, [flashcard(1), flashcard(2)], [])

    // a failing link must not break the chain
    expect((await prefetcher.media(1)).error).toBeDefined()
    expect((await prefetcher.media(2)).frontAudio).toBeDefined()
  })

  it('should hold nothing once disposed', async () => {
    loadMediaMetadata([1])
    prefetcher.slide(undefined, [flashcard(1)], [])

    prefetcher.dispose()
    const media = await prefetcher.media(1)

    expect(prefetcher.size()).toBe(0)
    expect(media.frontAudio).toBeUndefined()
  })

  it('should fetch on demand when the window is switched off', async () => {
    loadMediaMetadata([1, 2])
    const disabled = createMediaPrefetcher(SET_ID, 0)

    disabled.slide(flashcard(1), [], [])

    // nothing runs ahead, but the flashcard on screen still gets its media
    expect(disabled.size()).toBe(0)
    expect((await disabled.media(1)).frontAudio).toBeDefined()
    expect(audioGet.mock.calls.map((call) => call[1])).toEqual([1])

    disabled.dispose()
  })

  it('should keep flashcards behind the current one warm', async () => {
    // the window has moved past flashcard 1 onto flashcard 2
    loadMediaMetadata([1, 2, 3])
    prefetcher.slide(undefined, [flashcard(1)], [])
    await prefetcher.media(1)
    prefetcher.slide(flashcard(2), [flashcard(3)], [flashcard(1)])

    // the chain is then blocked by a fetch that never lands
    audioGet.mockReturnValue(new Promise<AxiosResponse<Blob>>(() => {}))
    pictureGet.mockReturnValue(new Promise<AxiosResponse<Blob>>(() => {}))

    // stepping back finds a settled entry rather than queueing behind it
    const settled = Symbol('settled')
    const race = await Promise.race([
      prefetcher.media(1).then(() => settled),
      new Promise((resolve) => setTimeout(resolve, 50, 'still waiting')),
    ])
    expect(race).toBe(settled)
  })

  it('should drop a flashcard that falls out of both windows', async () => {
    loadMediaMetadata([1, 2, 3])
    prefetcher.slide(undefined, [flashcard(1)], [])
    await prefetcher.media(1)

    // flashcard 1 is neither current, ahead, nor behind
    prefetcher.slide(flashcard(2), [flashcard(3)], [])

    // only what is ahead is left - the current flashcard gets an entry
    // when it is asked for, not when the window slides
    expect(prefetcher.size()).toBe(1)
  })

  it('should contain a failure that throws outside the request itself', async () => {
    // the store lookups throw when Pinia is gone, which is what a fetch
    // still queued after teardown looks like
    loadMediaMetadata([1, 2])
    const first = prefetcher.media(1)
    setActivePinia(undefined)
    const failed = await first
    setActivePinia(pinia)

    // it is reported, not thrown, and nothing queued behind it is stalled
    expect(failed.error).toBeDefined()
    expect(failed.frontAudio).toBeUndefined()
    expect((await prefetcher.media(2)).frontAudio).toBeDefined()
  })

  it('should not share state with another session', async () => {
    loadMediaMetadata([1, 2])
    const other = createMediaPrefetcher(SET_ID)

    // one session tears down while the other is still working
    other.slide(undefined, [flashcard(1)], [])
    prefetcher.slide(undefined, [flashcard(2)], [])
    other.dispose()

    expect((await prefetcher.media(2)).frontAudio).toBeDefined()
  })
})
