<template>
  <div id="app" class="app">
    <Navbar/>
    <router-view/>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { onMounted } from 'vue'
import { loadLanguageStore } from '@/shared/stores.ts'

onMounted(() => {
  loadLanguageStore()
})

</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(90deg, #0a0e1a 0%, #1a1f2e 50%, #2a2f3e 100%);
}

</style>
