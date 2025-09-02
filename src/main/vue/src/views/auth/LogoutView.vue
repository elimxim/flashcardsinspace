<template>
  <!-- todo more user friendly message -->
  <p>You've been logged out</p>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { routeNames } from '@/router/index.ts'
import { sendLogoutRequest } from '@/api/auth-client.ts'

const router = useRouter()
const authStore = useAuthStore()

sendLogoutRequest().then(() => {
  console.error('Successfully logged out')
  authStore.setUser(null)
  router.push({ name: routeNames.login })
}).catch(error => {
  console.error('Failed to log out: ', error)
  router.push({ name: routeNames.login })
})

</script>

<style scoped>
</style>
