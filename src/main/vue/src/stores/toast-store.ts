import { defineStore } from 'pinia'

export interface ToastState {
  message: string,
  show: boolean,
  timeout: number | undefined,
}

export const useToastStore = defineStore('toast', {
  state: (): ToastState => {
    return {
      message: '',
      show: false,
      timeout: undefined,
    }
  },
  actions: {
    showToast(newMessage: string) {
      this.message = newMessage
      this.show = true
      clearTimeout(this.timeout)
      this.timeout = window.setTimeout(() => {
        this.hideToast()
      }, 10000)
    },
    hideToast() {
      this.show = false
      this.message = ''
      clearTimeout(this.timeout)
    },
  }
})
