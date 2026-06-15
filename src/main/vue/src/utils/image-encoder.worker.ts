import { encode } from '@jsquash/webp'

interface EncodeRequest {
  buffer: ArrayBuffer
  width: number
  height: number
  quality: number
  targetBytes: number
}

self.onmessage = async (event: MessageEvent<EncodeRequest>) => {
  const { buffer, width, height, quality, targetBytes } = event.data
  try {
    const imageData = new ImageData(new Uint8ClampedArray(buffer), width, height)
    const result = await encode(imageData, { quality, target_size: targetBytes, pass: 6 })
    self.postMessage({ buffer: result })
  } catch (error) {
    self.postMessage({ error: error instanceof Error ? error.message : String(error) })
  }
}
