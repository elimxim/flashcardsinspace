<template>
  <p>You've been logged out</p>
</template>


<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth-state.ts'

const router = useRouter()
const authStore = useAuthStore()

authStore.logout()
router.push({ name: 'login' })
</script>

<style scoped>
</style>
