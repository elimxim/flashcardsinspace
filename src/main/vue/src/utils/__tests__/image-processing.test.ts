import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// @jsquash/webp is a WebAssembly codec; mock its encode so the resize/orchestration logic can be
// exercised in jsdom without loading any wasm.
const encodeMock = vi.fn((): Promise<ArrayBuffer> => Promise.resolve(new ArrayBuffer(64)))
vi.mock('@jsquash/webp', () => ({ encode: () => encodeMock() }))

import { processImageToWebp } from '@/utils/image-processing.ts'

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
  encodeMock.mockClear()
  nextImageSize = { width: 800, height: 600 }
  stubImage()
  vi.stubGlobal('document', { createElement: vi.fn(() => makeCanvas()) })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('processImageToWebp', () => {
  it('returns a blob with type image/webp', async () => {
    const result = await processImageToWebp(makeImageFile())
    expect(result.blob.type).toBe('image/webp')
    expect(encodeMock).toHaveBeenCalled()
  })

  it('returns dimensions reflecting the resized canvas (800x600 fits in 1600)', async () => {
    const result = await processImageToWebp(makeImageFile())
    expect(result.width).toBe(800)
    expect(result.height).toBe(600)
  })

  it('downscales when long edge exceeds maxDim', async () => {
    nextImageSize = { width: 3200, height: 2400 }

    const result = await processImageToWebp(makeImageFile(), 1600)
    // Scale = 1600/3200 = 0.5 → 1600x1200
    expect(result.width).toBe(1600)
    expect(result.height).toBe(1200)
  })

  it('does not upscale a small image (200x200 stays 200x200)', async () => {
    nextImageSize = { width: 200, height: 200 }

    const result = await processImageToWebp(makeImageFile())
    expect(result.width).toBe(200)
    expect(result.height).toBe(200)
  })

  it('aspect ratio preserved: 1920x1080 → 1600x900', async () => {
    nextImageSize = { width: 1920, height: 1080 }

    const result = await processImageToWebp(makeImageFile(), 1600)
    expect(result.width).toBe(1600)
    expect(result.height).toBe(900)
  })

  it('throws when the input cannot be decoded', async () => {
    stubImage(false)

    await expect(processImageToWebp(makeImageFile('data.bin', 'application/octet-stream')))
      .rejects.toThrow()
  })
})
