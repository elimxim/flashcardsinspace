import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, defineStore, setActivePinia } from 'pinia'
import { createApp, ref } from 'vue'
import { PiniaResetPlugin, resetAllPiniaStores } from '@/utils/pinia-reset-plugin.ts'

// a setup store that never returns its own $reset, so it keeps Pinia's throwing stub
const useStubbedStore = defineStore('stubbed', () => {
  const value = ref(1)
  return { value }
})

const useCountingStore = defineStore('counting', () => {
  const resets = ref(0)
  function $reset() {
    resets.value = resets.value + 1
  }
  return { resets, $reset }
})

const useLanguageStore = defineStore('language', () => {
  const resets = ref(0)
  function $reset() {
    resets.value = resets.value + 1
  }
  return { resets, $reset }
})

describe('pinia-reset-plugin', () => {
  beforeEach(() => {
    const pinia = createPinia()
    pinia.use(PiniaResetPlugin)
    createApp({}).use(pinia)
    setActivePinia(pinia)
  })

  it('should reset every store even when one of them fails', () => {
    // the stubbed store is registered first, so it fails first
    useStubbedStore()
    const counting = useCountingStore()

    resetAllPiniaStores()

    expect(counting.resets).toBe(1)
  })

  it('should keep reference data', () => {
    const language = useLanguageStore()

    resetAllPiniaStores()

    expect(language.resets).toBe(0)
  })

  it('should stop tracking a store once it is disposed', () => {
    const counting = useCountingStore()
    const reset = vi.spyOn(counting, '$reset')

    counting.$dispose()
    resetAllPiniaStores()

    expect(reset).not.toHaveBeenCalled()
  })
})
