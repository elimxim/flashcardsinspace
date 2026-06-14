import { Log, LogTag } from '@/utils/logger.ts'

export interface ProcessedImage {
  blob: Blob
  width: number
  height: number
}

const DEFAULT_MAX_DIM = 1600
const DEFAULT_QUALITY = 0.95
const DEFAULT_TARGET_MAX_BYTES = 500 * 1024
const MIN_QUALITY = 0.60
const MAX_QUALITY_ATTEMPTS = 4
const DOWNSCALE_FACTORS = [1, 0.8, 0.64, 0.51, 0.41]

type AnyCanvas = OffscreenCanvas | HTMLCanvasElement

/**
 * Converts any image File to WebP, resizing so neither edge exceeds maxDim (never upscales),
 * then iteratively lowers quality - and finally downscales - until the result fits within
 * targetMaxBytes. Applies EXIF orientation and uses OffscreenCanvas when available, falling
 * back to HTMLCanvasElement.
 */
export async function processImageToWebp(
  file: File,
  maxDim: number = DEFAULT_MAX_DIM,
  quality: number = DEFAULT_QUALITY,
  targetMaxBytes: number = DEFAULT_TARGET_MAX_BYTES,
): Promise<ProcessedImage> {
  Log.log(LogTag.SYSTEM, `processImageToWebp: input "${file.name}" type=${file.type || 'unknown'} size=${kb(file.size)}`)

  const bitmap = await createImageBitmap(file, { imageOrientation: 'from-image' })
  Log.log(LogTag.SYSTEM, `processImageToWebp: decoded source ${bitmap.width}x${bitmap.height}`)

  try {
    let best: ProcessedImage | undefined

    for (const factor of DOWNSCALE_FACTORS) {
      const { w, h } = fitWithin(bitmap.width, bitmap.height, Math.round(maxDim * factor))
      const canvas = renderToCanvas(bitmap, w, h)

      let q = quality
      for (let attempt = 1; attempt <= MAX_QUALITY_ATTEMPTS; attempt++) {
        const blob = await encodeCanvas(canvas, q)
        Log.log(LogTag.SYSTEM, `processImageToWebp: ${w}x${h} q=${q.toFixed(2)} -> ${kb(blob.size)} (attempt ${attempt})`)

        const candidate: ProcessedImage = { blob, width: w, height: h }
        if (best === undefined || blob.size < best.blob.size) {
          best = candidate
        }
        if (blob.size <= targetMaxBytes) {
          Log.log(LogTag.SYSTEM, `processImageToWebp: accepted ${w}x${h} q=${q.toFixed(2)} at ${kb(blob.size)} (target ${kb(targetMaxBytes)})`)
          return candidate
        }
        if (q <= MIN_QUALITY) break
        q = Math.max(MIN_QUALITY, q * Math.sqrt(targetMaxBytes / blob.size))
      }
      Log.log(LogTag.SYSTEM, `processImageToWebp: still over target at ${w}x${h}, downscaling further`)
    }

    if (best === undefined) {
      throw new Error('processImageToWebp produced no output')
    }
    Log.log(LogTag.SYSTEM, `processImageToWebp: could not reach target, using best effort ${best.width}x${best.height} at ${kb(best.blob.size)}`)
    return best
  } finally {
    bitmap.close()
  }
}

function fitWithin(srcW: number, srcH: number, maxDim: number): { w: number, h: number } {
  const scale = Math.min(1, maxDim / Math.max(srcW, srcH))
  return { w: Math.round(srcW * scale), h: Math.round(srcH * scale) }
}

function renderToCanvas(bitmap: ImageBitmap, w: number, h: number): AnyCanvas {
  if (typeof OffscreenCanvas !== 'undefined') {
    const canvas = new OffscreenCanvas(w, h)
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new Error('Could not get 2d context from OffscreenCanvas')
    drawScaled(ctx, bitmap, w, h)
    return canvas
  }
  const canvas = document.createElement('canvas')
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d')
  if (!ctx) throw new Error('Could not get 2d context from HTMLCanvasElement')
  drawScaled(ctx, bitmap, w, h)
  return canvas
}

function drawScaled(
  ctx: OffscreenCanvasRenderingContext2D | CanvasRenderingContext2D,
  bitmap: ImageBitmap,
  w: number,
  h: number,
) {
  ctx.imageSmoothingEnabled = true
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(bitmap, 0, 0, w, h)
}

async function encodeCanvas(canvas: AnyCanvas, quality: number): Promise<Blob> {
  const blob = 'convertToBlob' in canvas
    ? await (canvas as OffscreenCanvas).convertToBlob({ type: 'image/webp', quality })
    : await new Promise<Blob>((resolve, reject) => {
      (canvas as HTMLCanvasElement).toBlob(
        (b) => b ? resolve(b) : reject(new Error('HTMLCanvasElement.toBlob returned null')),
        'image/webp',
        quality,
      )
    })

  if (blob.type !== 'image/webp') {
    throw new Error(`Canvas encoded "${blob.type || 'unknown'}" instead of WebP; this browser may not support WebP encoding`)
  }

  return blob
}

function kb(bytes: number): string {
  return `${(bytes / 1024).toFixed(1)} KB`
}
