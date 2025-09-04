<template>
  <div class="awesome-container">
    <font-awesome-icon :icon="icon" class="awesome-container__icon"/>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon: string,
}>()
</script>

<style scoped>
.awesome-container {
  container-type: inline-size;
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 1rem;
}

.awesome-container__icon {
  font-size: var(--awesome-container__icon--font-size, 12cqi);
  color: var(--awesome-container__icon--color, #c3c3c3);
}
</style>
