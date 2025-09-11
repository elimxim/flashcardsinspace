import '@/assets/css/index.css'

// @ts-ignore
import App from '@/App.vue'
import router from '@/router'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth-store.ts'
import { sendWhoAmIRequest } from '@/api/auth-client.ts'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { applyFaIcons } from '@/fa.ts'

applyFaIcons()

const app = createApp(App)
app.component('font-awesome-icon', FontAwesomeIcon)
app.use(createPinia())

const authStore = useAuthStore()

sendWhoAmIRequest().then(response => {
  if (response.status === 200) {
    console.log('Who Am I: ', response.data)
    authStore.setUser(response.data)
  } else {
    console.log('Who Am I: ', response.status)
    authStore.setUser(null)
  }
}).then(() => {
  app.use(router)
  app.mount('#app')
})
