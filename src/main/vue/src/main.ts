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

applyFaRegularIcons()
applyFaSolidIcons()

const app = createApp(App)
app.component('FontAwesomeIcon', FontAwesomeIcon)

const pinia = createPinia()
pinia.use(PiniaResetPlugin)
app.use(pinia)

whoAmI().then(() => {
  app.use(router)
  app.mount('#app')
})
