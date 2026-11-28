import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { loadFlashcardSetStore, loadStoresForCurrFlashcardSet } from '@/utils/store-loading.ts'

export const useFlashcardSetLoading = defineStore('flashcard-set-loading', () => {
  const { loadingStarted, resolvedLoading, startLoading, stopLoading, resetLoading } =
    useDeferredLoading()

  const loaded = ref(false)

  async function loadCurrFlashcardSet() {
    try {
      startLoading()
      if (await loadFlashcardSetStore()) {
        await loadStoresForCurrFlashcardSet()
      }
    } finally {
      await stopLoading()
      loaded.value = true
    }
  }

  function $reset() {
    resetLoading()
    loaded.value = false
  }

  return {
    loadingStarted,
    resolvedLoading,
    loaded,
    loadCurrFlashcardSet,
    $reset,
  }
})
