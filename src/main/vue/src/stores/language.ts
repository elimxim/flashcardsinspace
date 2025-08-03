import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'
import apiClient from '@/api/api-client.ts'
import type { LanguageResponse } from '@/api/api-dto.ts'

export interface LanguageState {
  languages: Language[],
}

export const useLanguageStore = defineStore('language', {
  state: (): LanguageState => {
    return {
      languages: [] as Language[],
    }
  },
  actions: {
    loadData() {
      apiClient.get<LanguageResponse>('/public/languages')
        .then(response => {
          this.languages = response.data.languages
        })
      // todo deal with error
    },
  }
})

