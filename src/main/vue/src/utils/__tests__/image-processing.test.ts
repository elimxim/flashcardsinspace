import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { decodeAndResize } from '@/utils/image-processing.ts'

function makeImageFile(name = 'test.jpg', type = 'image/jpeg', size = 100): File {
  const bytes = new Uint8Array(size).fill(0)
  return new File([bytes], name, { type })
}

// jsdom has no canvas backend, so stub document.createElement('canvas') with a fake whose
// getImageData echoes the requested dimensions.
function makeCanvas() {
  return {
    width: 0,
    height: 0,
    getContext: () => ({
      imageSmoothingQuality: 'low',
      drawImage: vi.fn(),
      getImageData: (_x: number, _y: number, gw: number, gh: number) => ({
        data: new Uint8ClampedArray(4),
        width: gw,
        height: gh,
      }),
    }),
  }
}

// loadImage loads the blob into an <img>; jsdom never fires onload, so stub Image to resolve
// synchronously with controllable natural dimensions (or to error).
let nextImageSize = { width: 800, height: 600 }

function stubImage(succeed = true) {
  vi.stubGlobal('URL', { createObjectURL: vi.fn(() => 'blob:mock'), revokeObjectURL: vi.fn() })
  vi.stubGlobal('Image', class {
    onload: (() => void) | null = null
    onerror: (() => void) | null = null
    naturalWidth = 0
    naturalHeight = 0
    set src(_value: string) {
      if (succeed) {
        this.naturalWidth = nextImageSize.width
        this.naturalHeight = nextImageSize.height
        setTimeout(() => this.onload?.(), 0)
      } else {
        setTimeout(() => this.onerror?.(), 0)
      }
    }
  })
}

beforeEach(() => {
  nextImageSize = { width: 800, height: 600 }
  stubImage()
  vi.stubGlobal('document', { createElement: vi.fn(() => makeCanvas()) })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('decodeAndResize', () => {
  it('keeps an image that already fits (800x600 within 1600)', async () => {
    const imageData = await decodeAndResize(makeImageFile())
    expect(imageData.width).toBe(800)
    expect(imageData.height).toBe(600)
  })

  it('downscales when the long edge exceeds maxDim (3200x2400 -> 1600x1200)', async () => {
    nextImageSize = { width: 3200, height: 2400 }

    const imageData = await decodeAndResize(makeImageFile(), 1600)
    expect(imageData.width).toBe(1600)
    expect(imageData.height).toBe(1200)
  })

  it('does not upscale a small image (200x200 stays 200x200)', async () => {
    nextImageSize = { width: 200, height: 200 }

    const imageData = await decodeAndResize(makeImageFile())
    expect(imageData.width).toBe(200)
    expect(imageData.height).toBe(200)
  })

  it('preserves aspect ratio (1920x1080 -> 1600x900)', async () => {
    nextImageSize = { width: 1920, height: 1080 }

    const imageData = await decodeAndResize(makeImageFile(), 1600)
    expect(imageData.width).toBe(1600)
    expect(imageData.height).toBe(900)
  })

  it('throws when the input cannot be decoded', async () => {
    stubImage(false)

    await expect(decodeAndResize(makeImageFile('data.bin', 'application/octet-stream')))
      .rejects.toThrow()
  })
})
