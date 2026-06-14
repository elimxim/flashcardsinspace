import { describe, it, expect, beforeAll, vi } from 'vitest'
import { processImageToWebp } from '@/utils/image-processing.ts'

// OffscreenCanvas and createImageBitmap are not available in jsdom/node.
// We mock them to exercise the logic paths.

const MOCK_WEBP_BLOB = new Blob(['mock'], { type: 'image/webp' })

function makeImageFile(name = 'test.jpg', type = 'image/jpeg', size = 100): File {
  const bytes = new Uint8Array(size).fill(0)
  return new File([bytes], name, { type })
}

function makeOffscreenCanvas(w: number, h: number) {
  return {
    width: w,
    height: h,
    getContext: () => ({ drawImage: vi.fn() }),
    convertToBlob: () => Promise.resolve(new Blob(['webp'], { type: 'image/webp' })),
  }
}

beforeAll(() => {
  vi.stubGlobal('createImageBitmap', vi.fn(() =>
    Promise.resolve({
      width: 800,
      height: 600,
      close: vi.fn(),
    })
  ))

  vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(makeOffscreenCanvas))
})

describe('processImageToWebp', () => {
  it('returns a blob with type image/webp', async () => {
    const file = makeImageFile()
    const result = await processImageToWebp(file)
    expect(result.blob.type).toBe('image/webp')
  })

  it('returns dimensions reflecting the resized canvas (800x600 fits in 1600)', async () => {
    const file = makeImageFile()
    const result = await processImageToWebp(file)
    // 800x600 fits within 1600 — no scaling needed
    expect(result.width).toBe(800)
    expect(result.height).toBe(600)
  })

  it('downscales when long edge exceeds maxDim', async () => {
    // Simulate a 3200x2400 bitmap (long edge 3200)
    vi.mocked(createImageBitmap).mockResolvedValueOnce({
      width: 3200,
      height: 2400,
      close: vi.fn(),
    } as unknown as ImageBitmap)

    vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(makeOffscreenCanvas))

    const file = makeImageFile()
    const result = await processImageToWebp(file, 1600)

    // Scale = 1600/3200 = 0.5 → 1600x1200
    expect(result.width).toBe(1600)
    expect(result.height).toBe(1200)
  })

  it('does not upscale a small image (200x200 stays 200x200)', async () => {
    vi.mocked(createImageBitmap).mockResolvedValueOnce({
      width: 200,
      height: 200,
      close: vi.fn(),
    } as unknown as ImageBitmap)

    vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(makeOffscreenCanvas))

    const file = makeImageFile()
    const result = await processImageToWebp(file)
    expect(result.width).toBe(200)
    expect(result.height).toBe(200)
  })

  it('throws on non-image input (bitmap creation rejects)', async () => {
    vi.mocked(createImageBitmap).mockRejectedValueOnce(new Error('not an image'))
    const file = makeImageFile('data.bin', 'application/octet-stream')
    await expect(processImageToWebp(file)).rejects.toThrow()
  })

  it('falls back to HTMLCanvasElement when OffscreenCanvas is unavailable', async () => {
    vi.stubGlobal('OffscreenCanvas', undefined)

    const canvasEl = {
      width: 0,
      height: 0,
      getContext: () => ({ drawImage: vi.fn() }),
      toBlob: (cb: (b: Blob | null) => void) => {
        cb(new Blob(['canvas-fallback'], { type: 'image/webp' }))
      },
    }
    vi.stubGlobal('document', {
      createElement: vi.fn(() => canvasEl),
    })

    vi.mocked(createImageBitmap).mockResolvedValueOnce({
      width: 400,
      height: 300,
      close: vi.fn(),
    } as unknown as ImageBitmap)

    const file = makeImageFile()
    const result = await processImageToWebp(file)
    expect(result.blob.type).toBe('image/webp')
    expect(result.width).toBe(400)
    expect(result.height).toBe(300)

    // Restore for subsequent tests
    vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(makeOffscreenCanvas))
    vi.stubGlobal('document', undefined)
  })

  it('aspect ratio preserved: 1920x1080 → 1600x900', async () => {
    vi.mocked(createImageBitmap).mockResolvedValueOnce({
      width: 1920,
      height: 1080,
      close: vi.fn(),
    } as unknown as ImageBitmap)

    vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(makeOffscreenCanvas))

    const file = makeImageFile()
    const result = await processImageToWebp(file, 1600)

    // scale = 1600/1920 ≈ 0.8333 → 1600 x 900
    expect(result.width).toBe(1600)
    expect(result.height).toBe(900)

    // Verify aspect ratio within 1px
    const sourceRatio = 1920 / 1080
    const resultRatio = result.width / result.height
    expect(Math.abs(sourceRatio - resultRatio)).toBeLessThan(0.02)
  })

  // Restore stable OffscreenCanvas mock after the last test that tampers with it
  it('uses MOCK_WEBP_BLOB type in stable mock (sanity)', async () => {
    vi.stubGlobal('OffscreenCanvas', vi.fn().mockImplementation(() => ({
      width: 100,
      height: 100,
      getContext: () => ({ drawImage: vi.fn() }),
      convertToBlob: () => Promise.resolve(MOCK_WEBP_BLOB),
    })))

    vi.mocked(createImageBitmap).mockResolvedValueOnce({
      width: 100,
      height: 100,
      close: vi.fn(),
    } as unknown as ImageBitmap)

    const result = await processImageToWebp(makeImageFile())
    expect(result.blob.type).toBe('image/webp')
  })
})
