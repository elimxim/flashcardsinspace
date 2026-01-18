<template>
  <div
    :class="[
      'page',
      'flex-column',
      'padding-auto',
      'scrollbar-hidden',
      'website-support-page',
    ]"
  >
    <LightspeedScheduleSection/>
  </div>
</template>

<script setup lang="ts">
import LightspeedScheduleSection from '@/pages/support/LightspeedScheduleSection.vue'

</script>

<style scoped>
.website-support-page {
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
}

</style>
