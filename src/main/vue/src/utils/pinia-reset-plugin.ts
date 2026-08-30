import { type Store } from 'pinia'
import { Log, LogTag } from '@/utils/logger.ts'

/** Stores holding static reference data, which must survive a reset. */
const KEPT_STORE_IDS = new Set<string>(['language'])

const activeStores = new Set<Store>()

export function PiniaResetPlugin({ store }: { store: Store }) {
  activeStores.add(store)

  // a disposed store must not stay referenced here: it could never be collected,
  // and it would be reset again after its effect scope was already stopped
  const dispose = store.$dispose
  store.$dispose = () => {
    activeStores.delete(store)
    dispose.call(store)
  }
}

export function resetAllPiniaStores() {
  activeStores.forEach((store) => {
    if (KEPT_STORE_IDS.has(store.$id)) return

    try {
      Log.log(LogTag.SYSTEM, `Resetting store ${store.$id}`)
      store.$reset()
    } catch (error) {
      Log.error(LogTag.SYSTEM, `Failed to reset store ${store.$id}`, error)
    }
  })
}
