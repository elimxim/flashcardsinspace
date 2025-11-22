<template>
  <div
    :class="[
      'page',
      'flex-column',
      'flex-center',
      'padding-auto',
      'support-page',
      'support-page--theme'
    ]"
  >
    <Starfield
      :density="80"
      :star-size="2"
      twinkle
      vertical-drift="4px"
    />
    <div class="support-content">
      <div class="support-header">
        <h1 class="support-title">
          <font-awesome-icon icon="fa-solid fa-tools" style="font-size: 6rem;"/>
          <br/>
          Page is under construction
        </h1>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
</script>

<style scoped>
.support-page--theme {
  --support--title--color: rgba(255, 255, 255, 0.95);
}

.support-page {
  align-items: center;
  justify-content: center;
}

.support-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  max-width: 800px;
  width: 100%;
}

.support-header {
  text-align: center;
}

.support-title {
  display: flex;
  flex-direction: column;
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 700;
  color: var(--support--title--color);
  margin: 0 0 0.5rem 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

</style>
