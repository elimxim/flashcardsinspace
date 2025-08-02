<template>
  <div class="auth-container">
    <form @submit.prevent="handleLogin">
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button type="submit">Log In</button>
    </form>
  </div>
</template>

<script setup>
import '@/assets/auth.css'
import apiClient from '@/api/api-client.ts';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {useAuthStore} from "@/stores/auth-state.js";

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const password = ref('');

async function handleLogin() {
  try {
    const response = await apiClient.post('/auth/login', {
      email: email.value,
      secret: password.value,
    });

    console.log('Successfully logged in: ', response.data.message);

    authStore.setUser({
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      registeredAt: response.data.registeredAt,
    })

    console.log('Login successful:', response.data.message);
    await router.push({ name: 'flashcards' });
  } catch (error) {
    alert(error.response?.data?.message || 'Error logging in.'); // todo remove alert
  }
}
</script>
