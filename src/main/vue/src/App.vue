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
import { loadLanguageStore } from '@/utils/stores.ts'

onMounted(() => {
  loadLanguageStore()
})

</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--app--bg, white);
}

</style>
