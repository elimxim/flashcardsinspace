import '@/assets/css/index.css'

// @ts-ignore
import App from '@/App.vue'
import router from '@/router'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faGear } from '@fortawesome/free-solid-svg-icons'
import { faBox } from '@fortawesome/free-solid-svg-icons'
import { faRectangleList } from '@fortawesome/free-solid-svg-icons'
import { faCalendarDays } from '@fortawesome/free-solid-svg-icons'
import { faTriangleExclamation } from '@fortawesome/free-solid-svg-icons'
import { faCircleInfo } from '@fortawesome/free-solid-svg-icons'
import { faGlobe } from '@fortawesome/free-solid-svg-icons'
import { faXmark } from '@fortawesome/free-solid-svg-icons'
import { faPenToSquare } from '@fortawesome/free-solid-svg-icons'
import { faAngleLeft } from '@fortawesome/free-solid-svg-icons'
import { faAngleRight } from '@fortawesome/free-solid-svg-icons'
import { faCircleUser } from '@fortawesome/free-solid-svg-icons'
import { faEye } from '@fortawesome/free-solid-svg-icons'
import { faEyeSlash } from '@fortawesome/free-solid-svg-icons'
import { useAuthStore } from '@/stores/auth-store.ts'
import { sendWhoAmIRequest } from '@/api/auth-client.ts'

library.add(faGear)
library.add(faBox)
library.add(faRectangleList)
library.add(faCalendarDays)
library.add(faTriangleExclamation)
library.add(faCircleInfo)
library.add(faGlobe)
library.add(faXmark)
library.add(faPenToSquare)
library.add(faAngleLeft)
library.add(faAngleRight)
library.add(faCircleUser)
library.add(faEye)
library.add(faEyeSlash)

const app = createApp(App)
app.component('font-awesome-icon', FontAwesomeIcon)
app.use(createPinia())

const authStore = useAuthStore()

sendWhoAmIRequest().then(response => {
  if (response.status === 200) {
    console.log('Who Am I: ', response.data.user)
    authStore.setUser(response.data.user)
  } else {
    console.log('Who Am I: ', response.status)
    authStore.setUser(null)
  }
}).then(() => {
  app.use(router)
  app.mount('#app')
})
