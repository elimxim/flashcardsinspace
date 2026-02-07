<template>
  <div class="flashcard-set-list-skeleton">
    <div
      v-for="i in 4"
      :key="i"
      class="skeleton skeleton--dark flashcard-set-skeleton"
    >
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.flashcard-set-list-skeleton {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 1rem;
}

.flashcard-set-skeleton {
  width: 100%;
  height: 65px;
}
</style>
