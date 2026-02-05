<template>
  <div
    :class="[
      'page',
      'flex-column',
      'padding-auto',
      'scrollbar-hidden',
      'website-guide-page',
    ]"
  >
    <div class="schedule-section">
      <LightspeedScheduleSection/>
    </div>
  </div>
</template>

<script setup lang="ts">
import LightspeedScheduleSection from '@/components/LightspeedScheduleSection.vue'

</script>

<style scoped>
.website-guide-page {
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
}

.schedule-section {
  flex: 1;
}

</style>
