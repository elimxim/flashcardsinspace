<template>
  <div id="app" class="app-container">
    <Navbar/>
    <router-view/>
  </div>
</template>

<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import { useLanguageStore } from '@/stores/language-store.ts'
import { onMounted } from 'vue'
import { loadLanguages } from '@/api/public-api-client.ts'

const languageStore = useLanguageStore()

onMounted(() => {
  loadLanguages().then(response => {
    languageStore.loadState(response.data)
  }).catch(error => {
    console.error('Error loading languages:', error)
  })
})

</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

</style>
