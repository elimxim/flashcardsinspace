import { type Store } from 'pinia'
import { Log, LogTag } from '@/utils/logger.ts'

const activeStores: Store[] = []

export function PiniaResetPlugin({ store }: { store: Store }) {
  activeStores.push(store)
}

export function resetAllPiniaStores() {
  activeStores
    .filter((store) => {
      return store.$id !== 'language'
    })
    .forEach((store) => {
    if (typeof store.$reset === 'function') {
      Log.log(LogTag.SYSTEM, `Resetting store ${store.$id}`)
      store.$reset()
    }
  })
}
