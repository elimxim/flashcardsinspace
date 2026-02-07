import { defineStore } from 'pinia'
import { ErrorResponseBody } from '@/api/communication.ts'
import { UserApiError } from '@/api/user-api-error.ts'

export enum ToastType {
  SUCCESS = 'success',
  ERROR = 'error',
  INFO = 'info',
  WARNING = 'warning',
}

export interface Toast {
  type: ToastType
  title: string
  message?: string
  footer?: string
  duration: number
  persistent?: boolean
}

export interface ToastState {
  toast: Toast | null,
  show: boolean,
  paused: boolean,
  timeout: number | null,
  remaining: number,
  startedAt: number,
}

export const useSpaceToaster = defineStore('space-toaster', {
  state: (): ToastState => {
    return {
      toast: null,
      show: false,
      paused: false,
      timeout: null,
      remaining: 0,
      startedAt: 0,
    }
  },
  actions: {
    bakeError(apiError: UserApiError, error?: ErrorResponseBody) {
      const toast: Toast = {
        type: ToastType.ERROR,
        title: apiError.title,
        message: apiError.message,
        footer: error?.errorCode,
        duration: 4000,
      }
      this.bake(toast)
    },
    bakeSuccess(title: string, message: string, delay?: number) {
      const toast: Toast = {
        type: ToastType.SUCCESS,
        title: title,
        message: message,
        duration: 2000,
      }
      if (delay) {
        this.bakeWithDelay(toast, delay)
      } else {
        this.bake(toast)
      }
    },
    bakeWithDelay(toast: Toast, delay: number) {
      setTimeout(() => {
        this.bake(toast)
      }, delay)
    },
    bake(toast: Toast) {
      const wasShowing = this.show
      this.$reset()
      const showNow = () => {
        this.toast = toast
        this.show = true
        this.start()
      }
      if (wasShowing) {
        requestAnimationFrame(showNow)
      } else {
        showNow()
      }
    },
    start() {
      if (this.toast !== null) {
        if (this.toast.persistent) return
        this.remaining = this.toast.duration
        this.startedAt = performance.now()
        this.timeout = window.setTimeout(() => this.show = false, this.remaining)
      }
    },
    pause() {
      if (this.toast !== null) {
        if (this.toast.persistent || this.timeout === null) return
        this.paused = true
        this.remaining -= performance.now() - this.startedAt
        window.clearTimeout(this.timeout)
        this.timeout = null
      }
    },
    resume() {
      if (this.toast !== null) {
        if (this.toast.persistent || this.timeout !== null) return
        this.paused = false
        this.startedAt = performance.now()
        this.timeout = window.setTimeout(() => this.show = false, this.remaining)
      }
    },
    dismiss() {
      this.$reset()
    },
    $reset() {
      if (this.timeout != null) {
        window.clearTimeout(this.timeout)
      }

      this.$patch((state: ToastState) => {
        state.toast = null
        state.show = false
        state.paused = false
        state.timeout = null
        state.remaining = 0
        state.startedAt = 0
      })
    },
  }
})
