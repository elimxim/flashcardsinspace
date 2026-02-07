import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'

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
      this.$reset()
      this.languageMap = new Map(languages.map(v => [v.id, v]))
      this.loaded = true
    },
    getLanguage(id: number): Language | undefined {
      return this.languageMap.get(id)
    },
  }
})

