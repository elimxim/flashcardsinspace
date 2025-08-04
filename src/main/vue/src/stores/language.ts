import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'
import type { LanguageGetResponse } from '@/api/communication.ts'

export interface LanguageState {
  languageMap: Map<number, Language>,
}

export const useLanguageStore = defineStore('language', {
  state: (): LanguageState => {
    return {
      languageMap: new Map()
    }
  },
  getters: {
    languages(): Language[] {
      return [...this.languageMap.values()]
    }
  },
  actions: {
    loadData() {
      apiClient.get<LanguageGetResponse>('/public/languages')
        .then(response => {
          this.languageMap = new Map(response.data.languages.map(v => [v.id, v]))
        })
      // todo deal with error
    },
    getLanguage(id: number): Language | null {
      return this.languageMap.get(id) ?? null
    },
  }
})

