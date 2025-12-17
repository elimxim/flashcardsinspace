<template>
  <form @submit.prevent="handleForgotPassword">
    <input v-model="email" type="email" placeholder="Email" required />
    <button type="submit">Send Reset Email</button>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import authClient from '@/api/auth-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'

const email = ref('')

async function handleForgotPassword() {
  try {
    const response = await authClient.post('/password-reset', {
      email: email.value,
    })

    alert(response.data)
  } catch (error) {
    Log.error(LogTag.LOGIC, 'Failed to send reset email: ', error)
  }
}
</script>
