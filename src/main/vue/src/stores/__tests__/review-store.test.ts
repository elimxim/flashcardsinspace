import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia, getActivePinia } from 'pinia'
import { AxiosHeaders, AxiosResponse } from 'axios'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { MonoStageReviewQueue, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { learningStages } from '@/core-logic/stage-logic.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import {
  sendFlashcardAudioGetRequest,
  sendFlashcardPictureGetRequest,
} from '@/api/api-client.ts'
import { ref } from "vue"

vi.mock('@/api/api-client.ts', () => ({
  sendFlashcardAudioGetRequest: vi.fn(),
  sendFlashcardPictureGetRequest: vi.fn(),
}))

const audioGet = vi.mocked(sendFlashcardAudioGetRequest)
const pictureGet = vi.mocked(sendFlashcardPictureGetRequest)

const flashcardSet: FlashcardSet = {
  id: 40,
  name: 'set',
  status: 'ACTIVE',
  languageId: 1,
  createdAt: new Date(),
}

const flashcardSetRef = ref(flashcardSet)

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

function blobResponse(blob: Blob): AxiosResponse<Blob> {
  return {
    data: blob,
    status: 200,
    statusText: 'OK',
    headers: { 'x-audio-id': '1' },
    config: { headers: new AxiosHeaders() },
  }
}

function loadFrontAudioMetadata(flashcardIds: number[]) {
  useAudioStore().loadState(flashcardIds.map(id => ({
    audioId: id * 10,
    flashcardId: id,
    flashcardSide: flashcardSides.FRONT,
  })))
  usePictureStore().loadState([])
}

function reviewStore() {
  return useReviewStore(ReviewSessionType.QUIZ, flashcardSetRef)
}

describe('review-store media', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    pictureGet.mockResolvedValue(blobResponse(new Blob(['picture'])))
    audioGet.mockResolvedValue(blobResponse(new Blob(['audio'])))
  })

  it('should serve the first flashcard from work the window already started', async () => {
    // given:
    loadFrontAudioMetadata([1, 2, 3])
    const store = reviewStore()

    // when:
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2), flashcard(3)]))
    await store.nextFlashcard()

    // then: the flashcard on screen was fetched exactly once, by the window
    const currentId = store.currFlashcard?.id
    expect(audioGet.mock.calls.filter(call => call[1] === currentId).length).toBe(1)
    expect(store.flashcardFrontSideAudioBlob).toBeDefined()
  })

  it('should keep the window topped up as the review advances', async () => {
    // given:
    loadFrontAudioMetadata([1, 2, 3, 4])
    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2), flashcard(3), flashcard(4)]))

    // when:
    await store.nextFlashcard()
    await store.nextFlashcard()

    // then: the flashcard on screen plus the next two are all covered
    const wanted = [store.currFlashcard!.id, ...store.reviewQueue.lookahead(2).map(f => f.id)]
    await vi.waitFor(() => {
      const requested = audioGet.mock.calls.map(call => call[1])
      wanted.forEach(id => expect(requested).toContain(id))
    })
  })

  it('should never request the same flashcard twice', async () => {
    // given:
    loadFrontAudioMetadata([1, 2, 3])
    const store = reviewStore()

    // when:
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2), flashcard(3)]))
    await store.nextFlashcard()
    await store.nextFlashcard()
    await store.nextFlashcard()

    // then:
    const requested = audioGet.mock.calls.map(call => call[1])
    expect(requested.length).toBe(new Set(requested).size)
  })

  it('should not let a slow fetch paint its media onto a later flashcard', async () => {
    // given: the first flashcard's audio never arrives until we say so
    loadFrontAudioMetadata([1, 2])
    const secondBlob = new Blob(['second'])
    let resolveFirst: (response: AxiosResponse<Blob>) => void = () => {}
    let calls = 0
    audioGet.mockImplementation(() => {
      calls = calls + 1
      if (calls === 1) {
        return new Promise<AxiosResponse<Blob>>((resolve) => {
          resolveFirst = resolve
        })
      }
      return Promise.resolve(blobResponse(secondBlob))
    })

    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2)]))
    await vi.waitFor(() => expect(audioGet).toHaveBeenCalled())

    // when: the user advances twice before that audio lands
    const first = store.nextFlashcard()
    const slowId = store.currFlashcard?.id
    const second = store.nextFlashcard()
    resolveFirst(blobResponse(new Blob(['first'])))
    await Promise.all([first, second])

    // then:
    expect(store.currFlashcard?.id).not.toBe(slowId)
    expect(store.flashcardFrontSideAudioBlob).toBe(secondBlob)
  })

  it('should clear the media refs when the review runs out', async () => {
    // given:
    loadFrontAudioMetadata([1])
    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1)]))
    await store.nextFlashcard()

    // when:
    await store.nextFlashcard()

    // then:
    expect(store.currFlashcard).toBeUndefined()
    expect(store.flashcardFrontSideAudioBlob).toBeUndefined()
  })

  it('should leave nothing behind when destroyed', () => {
    // given:
    loadFrontAudioMetadata([1])
    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1)]))
    const pinia = getActivePinia()!

    // when:
    store.$reset()
    destroyReviewStore(store)

    // then: the state entry is released, and the instance is gone too - so the next
    // useReviewStore re-runs the setup and re-captures the flashcard set
    expect(store.$id in pinia.state.value).toBe(false)
    expect(useReviewStore(ReviewSessionType.QUIZ, flashcardSetRef)).not.toBe(store)
  })

  it('should not let a revisited flashcard overwrite the one now on screen', async () => {
    // given: three flashcards, each with its own audio
    loadFrontAudioMetadata([1, 2, 3])
    const blobs = new Map<number, Blob>([
      [1, new Blob(['one'])], [2, new Blob(['two'])], [3, new Blob(['three'])],
    ])
    audioGet.mockImplementation((_setId, flashcardId) =>
      Promise.resolve(blobResponse(blobs.get(flashcardId)!))
    )
    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2), flashcard(3)]))
    await store.nextFlashcard()
    await store.nextFlashcard()
    const onScreen = store.currFlashcard!.id

    // when: the user steps back and forward again before the step back has settled.
    // The flashcard behind the window was dropped, so revisiting it starts a fresh
    // fetch - which the window then drops and aborts as it slides forward again.
    const back = store.prevFlashcard()
    const revisited = store.currFlashcard!.id
    await store.nextFlashcard()
    await back

    // then: that abandoned fetch must not write anything, or it would wipe the
    // media of the flashcard now on screen
    expect(revisited).not.toBe(onScreen)
    expect(store.currFlashcard?.id).toBe(onScreen)
    expect(store.flashcardFrontSideAudioBlob).toBe(blobs.get(onScreen))
  })

  it('should abort outstanding work on reset', async () => {
    // given:
    loadFrontAudioMetadata([1, 2])
    const signals: AbortSignal[] = []
    audioGet.mockImplementation((_setId, _flashcardId, _side, signal) => {
      if (signal) signals.push(signal)
      return new Promise<AxiosResponse<Blob>>(() => {})
    })
    const store = reviewStore()
    store.loadState(new MonoStageReviewQueue([flashcard(1), flashcard(2)]))
    await vi.waitFor(() => expect(audioGet).toHaveBeenCalled())

    // when:
    store.$reset()

    // then:
    expect(signals.length).toBeGreaterThan(0)
    expect(signals.every(signal => signal.aborted)).toBe(true)
  })
})
