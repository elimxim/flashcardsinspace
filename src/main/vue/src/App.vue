<template>
  <div id="app" class="app-container">
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
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

</style>
