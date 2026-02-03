import { watch } from 'vue'

/**
 * Waits for a specific property on a Pinia store to become truthy.
 * @param store The Pinia store instance
 * @param property The boolean property to watch (defaults to 'loaded')
 */
export function waitUntilStoreLoaded<T>(store: T, property: keyof T = 'loaded' as keyof T): Promise<void> {
  if (store[property]) {
    return Promise.resolve()
  }

  return new Promise((resolve) => {
    const unwatch = watch(
      () => store[property],
      (value) => {
        if (value) {
          unwatch()
          resolve()
        }
      }
    )
  })
}
