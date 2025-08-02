<template>
  <!-- todo show something more here -->
  <div style="font-size: 10em; text-align: center;">
    <font-awesome-icon icon="fa-solid fa-circle-user"/>
  </div>
</template>


<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth-state.ts'

const router = useRouter()
const authStore = useAuthStore()

// todo logout button
</script>

<style scoped>
</style>
