import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'
import { waitUntilLoaded } from '@/utils/stores.ts'

export interface LanguageState {
  languageMap: Map<number, Language>
  loaded: boolean
}

export const useLanguageStore = defineStore('language', {
  state: (): LanguageState => {
    return {
      languageMap: new Map(),
      loaded: false,
    }
  },
  getters: {
    languages(): Language[] {
      return [...this.languageMap.values()]
    }
  },
  actions: {
    loadState(languages: Language[]) {
      this.resetState()
      this.languageMap = new Map(languages.map(v => [v.id, v]))
      this.loaded = true
    },
    getLanguage(id: number): Language | undefined {
      return this.languageMap.get(id)
    },
    async getLanguageAsync(id: number): Promise<Language | undefined> {
      await waitUntilLoaded(this)
      return this.getLanguage(id)
    },
    resetState() {
      this.languageMap = new Map()
      this.loaded = false
    }
  }
})

