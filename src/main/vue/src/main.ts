import '@/assets/css/index.css'

import App from '@/App.vue'
import router from '@/router'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { applyFaSolidIcons } from '@/fa-solid.ts'
import { applyFaRegularIcons } from '@/fa-regular.ts'
import { whoAmI } from '@/core-logic/user-logic.ts'
import { PiniaResetPlugin } from '@/utils/pinia-reset-plugin.ts'
import { Log, LogTag } from '@/utils/logger.ts'

declare global {
  interface Window {
    loaderTimerId?: number
    loaderAppearedAt?: number
  }
}

async function bootstrap() {
  const loader = document.getElementById('initial-loader')
  const loaderAppearedAt = window.loaderAppearedAt

  applyFaRegularIcons()
  applyFaSolidIcons()

  const app = createApp(App)
  app.component('FontAwesomeIcon', FontAwesomeIcon)

  const pinia = createPinia()
  pinia.use(PiniaResetPlugin)
  app.use(pinia)

  try {
    await whoAmI()
    app.use(router)
  } catch (error) {
    Log.info(LogTag.SYSTEM, 'Could not determine user status', error)
  }

  if (loader && loaderAppearedAt) {
    clearTimeout(window.loaderTimerId)
    const loaderDuration = performance.now() - loaderAppearedAt
    const remainingTime = Math.max(0, 1000 - loaderDuration)
    // wait for at least 200ms (transition) + 800ms to avoid layout blink
    await new Promise(resolve => setTimeout(resolve, remainingTime))
    loader.classList.remove('visible')
    // wait for 200ms (transition) before mounting the app
    await new Promise(resolve => setTimeout(resolve, 200))
  }

  app.mount('#app')
  Log.log(LogTag.SYSTEM, "App mounted to #app")
  loader?.remove()
}

await bootstrap()
