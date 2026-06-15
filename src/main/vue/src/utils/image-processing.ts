import { Log, LogTag } from '@/utils/logger.ts'

const DEFAULT_MAX_DIM = 1600
const DEFAULT_QUALITY = 85
const DEFAULT_TARGET_BYTES = 280 * 1024

/**
 * Decodes an image File with an <img> and resizes it so neither edge exceeds maxDim (never
 * upscales), returning the raw pixels. Runs on the main thread because it needs the DOM (<img>,
 * canvas); pass the result to encodeToWebp to compress it off the main thread.
 */
export async function decodeAndResize(file: File, maxDim: number = DEFAULT_MAX_DIM): Promise<ImageData> {
  Log.log(LogTag.SYSTEM, `imgProc: input "${file.name}" type=${file.type || 'unknown'} size=${(file.size / 1024).toFixed(1)} KB`)
  const img = await loadImage(file)
  const imageData = resizeToImageData(img, maxDim)
  Log.log(LogTag.SYSTEM, `imgProc: ${img.naturalWidth}x${img.naturalHeight} -> ${imageData.width}x${imageData.height}`)
  return imageData
}

/**
 * Decodes a Blob via a plain <img>. <img> handles every format the OS supports
 * (including HEIC on Apple devices) and applies EXIF orientation
 * by default on modern browsers.
 */
function loadImage(blob: Blob): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(blob)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('Could not decode image'))
    }
    img.src = url
  })
}

/**
 * Resizes the image so its long edge is at most maxDim (never upscales)
 * and reads back the pixels.
 * */
function resizeToImageData(img: HTMLImageElement, maxDim: number): ImageData {
  const scale = Math.min(1, maxDim / Math.max(img.naturalWidth, img.naturalHeight))
  const width = Math.round(img.naturalWidth * scale)
  const height = Math.round(img.naturalHeight * scale)

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) throw new Error('Could not get 2d canvas context')
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(img, 0, 0, width, height)
  return ctx.getImageData(0, 0, width, height)
}

/**
 * Encodes ImageData to a WebP Blob in a Web Worker. The pixel buffer
 * is transferred into the worker (zero-copy). Aborting the signal
 * terminates the worker and rejects with an AbortError.
 */
export function encodeToWebp(
  imageData: ImageData,
  signal?: AbortSignal,
  quality: number = DEFAULT_QUALITY,
  targetBytes: number = DEFAULT_TARGET_BYTES
): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const worker = new Worker(new URL('./image-encoder.worker.ts', import.meta.url), { type: 'module' })

    const onAbort = () => {
      worker.terminate()
      reject(new DOMException('Image encoding aborted', 'AbortError'))
    }

    worker.onmessage = ({ data }: MessageEvent<{ buffer?: ArrayBuffer, error?: string }>) => {
      worker.terminate()
      signal?.removeEventListener('abort', onAbort)
      if (data.buffer) {
        resolve(new Blob([data.buffer], { type: 'image/webp' }))
      } else {
        reject(new Error(data.error ?? 'WebP encoder worker returned no data'))
      }
    }
    worker.onerror = (event) => {
      worker.terminate()
      signal?.removeEventListener('abort', onAbort)
      reject(new Error(event.message || 'WebP encoder worker failed'))
    }

    if (signal?.aborted) {
      onAbort()
      return
    }
    signal?.addEventListener('abort', onAbort, { once: true })

    const buffer = imageData.data.buffer as ArrayBuffer
    worker.postMessage(
      {
        buffer,
        width: imageData.width,
        height: imageData.height,
        quality, targetBytes
      },
      [buffer]
    )
  })
}
