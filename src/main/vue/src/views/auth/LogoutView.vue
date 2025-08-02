<template>
  <!-- todo more user friendly message -->
  <p>You've been logged out</p>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth-state.ts'
import { routeNames } from '@/router/index.ts'

const router = useRouter()
const authStore = useAuthStore()

try {
  authStore.logout()
} catch (error) {
  console.error('Failed to log out: ', error)
}

// router.push({ name: routeNames.login })
</script>

<style scoped>
</style>
