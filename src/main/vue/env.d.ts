/// <reference types="vite/client" />

import type { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

declare module 'vue' {
  export interface GlobalComponents {
    FontAwesomeIcon: typeof FontAwesomeIcon
  }
}

export {}
