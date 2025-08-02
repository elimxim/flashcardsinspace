<template>
  <form @submit.prevent="handleForgotPassword">
    <input v-model="email" type="email" placeholder="Email" required />
    <button type="submit">Send Reset Email</button>
  </form>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';

const email = ref('');

async function handleForgotPassword() {
  try {
    const response = await axios.post('/auth/forgot-password', {
      email: email.value,
    });

    alert(response.data);
  } catch (error) {
    alert(error.response.data || 'Error sending reset email.');
  }
}
</script>
