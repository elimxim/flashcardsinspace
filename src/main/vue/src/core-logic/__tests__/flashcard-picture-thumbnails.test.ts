import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { AxiosHeaders, AxiosResponse } from 'axios'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { requestFlashcardPictureThumbnails } from '@/core-logic/flashcard-picture-logic.ts'
import { usePictureThumbnailStore } from '@/stores/picture-thumbnail-store.ts'
import { sendFlashcardPictureThumbnailsRequest } from '@/api/api-client.ts'

vi.mock('@/api/api-client.ts', () => ({
  sendFlashcardPictureThumbnailsRequest: vi.fn(),
}))

const SET_ID = 40
const FRONT = flashcardSides.FRONT

const thumbnailsGet = vi.mocked(sendFlashcardPictureThumbnailsRequest)

async function multipartResponse(
  thumbnails: Record<number, number[]>,
): Promise<AxiosResponse<Blob>> {
  const form = new FormData()
  Object.entries(thumbnails).forEach(([flashcardId, bytes]) =>
    form.append(
      flashcardId,
      new Blob([new Uint8Array(bytes)], { type: 'image/webp' }),
      `${flashcardId}.webp`,
    ),
  )
  const encoded = new Response(form)
  return {
    data: await encoded.blob(),
    status: 200,
    statusText: 'OK',
    headers: { 'content-type': encoded.headers.get('content-type') },
    config: { headers: new AxiosHeaders() },
  }
}

function noContentResponse(): AxiosResponse<Blob> {
  return {
    data: new Blob([]),
    status: 204,
    statusText: 'No Content',
    headers: {},
    config: { headers: new AxiosHeaders() },
  }
}

describe('requestFlashcardPictureThumbnails', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    thumbnailsGet.mockReset()
  })

  it('requests the flashcards by id and stores their thumbnails', async () => {
    thumbnailsGet.mockResolvedValue(await multipartResponse({ 1: [82, 73, 70, 70], 2: [7] }))

    await requestFlashcardPictureThumbnails(SET_ID, [1, 2], FRONT)

    expect(thumbnailsGet).toHaveBeenCalledWith(SET_ID, [1, 2], FRONT, undefined)
    const store = usePictureThumbnailStore()
    const first = store.getThumbnail(1, FRONT)
    expect(first?.type).toBe('image/webp')
    expect([...new Uint8Array(await first!.arrayBuffer())]).toEqual([82, 73, 70, 70])
    expect(store.getThumbnail(2, FRONT)?.size).toBe(1)
  })

  it('stores null for a flashcard the server sent no thumbnail for', async () => {
    thumbnailsGet.mockResolvedValue(await multipartResponse({ 1: [1] }))

    await requestFlashcardPictureThumbnails(SET_ID, [1, 2], FRONT)

    const store = usePictureThumbnailStore()
    expect(store.getThumbnail(1, FRONT)).toBeInstanceOf(Blob)
    expect(store.getThumbnail(2, FRONT)).toBeNull()
    expect(store.getThumbnail(3, FRONT)).toBeUndefined()
  })

  it('stores null for every flashcard when the server has no thumbnails', async () => {
    thumbnailsGet.mockResolvedValue(noContentResponse())

    await requestFlashcardPictureThumbnails(SET_ID, [1, 2], FRONT)

    const store = usePictureThumbnailStore()
    expect(store.getThumbnail(1, FRONT)).toBeNull()
    expect(store.getThumbnail(2, FRONT)).toBeNull()
  })

  it("forgets the previous set's thumbnails when another set's arrive", async () => {
    thumbnailsGet.mockResolvedValue(await multipartResponse({ 1: [1] }))
    await requestFlashcardPictureThumbnails(SET_ID, [1], FRONT)
    thumbnailsGet.mockResolvedValue(await multipartResponse({ 5: [5] }))

    await requestFlashcardPictureThumbnails(SET_ID + 1, [5], FRONT)

    const store = usePictureThumbnailStore()
    expect(store.getThumbnail(1, FRONT)).toBeUndefined()
    expect(store.getThumbnail(5, FRONT)).toBeInstanceOf(Blob)
  })

  it('stores nothing when the request fails', async () => {
    thumbnailsGet.mockRejectedValue(new Error('network'))

    await expect(requestFlashcardPictureThumbnails(SET_ID, [1], FRONT)).rejects.toThrow('network')

    expect(usePictureThumbnailStore().getThumbnail(1, FRONT)).toBeUndefined()
  })
})
