import { Log, LogTag } from '@/utils/logger.ts'

export interface ProcessedImage {
  blob: Blob
  width: number
  height: number
}

const DEFAULT_MAX_DIM = 1600
const DEFAULT_QUALITY = 85
const DEFAULT_TARGET_BYTES = 280 * 1024

/**
 * Converts any image File to WebP.
 */
export async function processImageToWebp(
  file: File,
  maxDim: number = DEFAULT_MAX_DIM,
  quality: number = DEFAULT_QUALITY,
  targetBytes: number = DEFAULT_TARGET_BYTES,
): Promise<ProcessedImage> {
  Log.log(LogTag.SYSTEM, `imgProc: input "${file.name}" type=${file.type || 'unknown'} size=${kb(file.size)}`)
  const img = await loadImage(file)
  const { imageData, width, height } = resizeToImageData(img, maxDim)
  const { encode } = await import('@jsquash/webp')
  const buffer = await encode(imageData, { quality, target_size: targetBytes, pass: 6 })
  const blob = new Blob([buffer], { type: 'image/webp' })
  Log.log(LogTag.SYSTEM, `imgProc: ${img.naturalWidth}x${img.naturalHeight} -> ${width}x${height}, ${kb(blob.size)} (target ${kb(targetBytes)})`)
  return { blob, width, height }
}

/**
 * Resizes the image so its long edge is at most maxDim (never upscales) and returns the pixels as
 * ImageData. Capping dimensions keeps quality high under the byte budget and avoids storing huge
 * images.
 */
function resizeToImageData(img: HTMLImageElement, maxDim: number): {
  imageData: ImageData,
  width: number,
  height: number
} {
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
  return { imageData: ctx.getImageData(0, 0, width, height), width, height }
}

/**
 * Decodes a Blob via a plain <img>. <img> handles every format the OS supports
 * (including HEIC on Apple devices) and applies EXIF orientation by default on modern browsers.
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

function kb(bytes: number): string {
  return `${(bytes / 1024).toFixed(1)} KB`
}
