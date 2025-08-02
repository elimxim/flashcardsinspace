<template>
  <div class="auth-container">
    <form @submit.prevent="signup">
      <input v-model="name" type="text" placeholder="Name" required/>
      <input v-model="email" type="email" placeholder="Email" required/>
      <input v-model="password" type="password" placeholder="Password" required/>
      <input v-model="confirmPassword" type="password" placeholder="Confirm Password" required/>
      <button type="submit">Sign Up</button>
    </form>
    <p class="auth-option">
      Already signed up?
      <router-link to="/login">Login</router-link>
    </p>
  </div>
</template>

<script setup>
import '@/assets/auth.css'
import apiClient from '@/api/api-client.ts';
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {useAuthStore} from "@/stores/auth-state.js";

const router = useRouter();
const authStore = useAuthStore();

const name = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');

async function signup() {
  try {
    const response = await apiClient.post('/auth/signup', {
      email: email.value,
      secret: password.value,
      name: name.value,
    });

    console.log('Successfully signed up: ', response.data.message);

    authStore.setUser({
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      registeredAt: response.data.registeredAt,
    })

    await router.push({name: 'flashcards'});
  } catch (error) {
    console.error('Failed to sign up: ', error);
  }
}
</script>

<style scoped>
</style>
