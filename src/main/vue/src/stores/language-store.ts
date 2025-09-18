import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'
import type { LanguageGetResponse } from '@/api/communication.ts'
import publicApiClient from '@/api/public-api-client.ts'

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
    loadLanguages() {
      publicApiClient.get<LanguageGetResponse>('/languages')
        .then(response => {
          this.resetState()
          this.languageMap = new Map(response.data.languages.map(v => [v.id, v]))
          this.loaded = true
        })
      // todo deal with error
    },
    getLanguage(id: number): Language | null {
      return this.languageMap.get(id) ?? null
    },
    resetState() {
      this.languageMap = new Map()
      this.loaded = false
    }
  }
})

