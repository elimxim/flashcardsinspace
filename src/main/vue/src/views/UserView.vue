<template>
  <!-- todo show something more here -->
  <div class="user-container">
    <font-awesome-icon icon="fa-solid fa-circle-user"/>
  </div>
</template>


<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.ts'

const router = useRouter()
const authStore = useAuthStore()

// todo logout button
</script>

<style scoped>
.user-container {
  height: 92vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 20em;
  color: #d6d6d6;
}

</style>
