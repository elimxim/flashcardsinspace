<template>
  <form @submit.prevent="handleForgotPassword">
    <input v-model="email" type="email" placeholder="Email" required />
    <button type="submit">Send Reset Email</button>
  </form>
</template>

<script setup lang="ts">
import apicClient from '@/api/api-client.ts'
import { ref } from 'vue'

const email = ref('')

async function handleForgotPassword() {
  try {
    const response = await apicClient.post('/auth/password-reset', {
      email: email.value,
    })

    alert(response.data);
  } catch (error) {
    console.log('Failed to send reset email: ', error);
  }
}
</script>
