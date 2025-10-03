<template>
  <div class="awesome-container awesome-container--theme">
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
.awesome-container--theme {
  --icon--font-size: var(--awesome-container--icon--font-size, 36px);
  --icon--color: var(--awesome-container--icon--color, #c3c3c3);
}

.awesome-container {
  container-type: inline-size;
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 1rem;
}

.awesome-container__icon {
  font-size: var(--icon--font-size);
  color: var(--icon--color);
}
</style>
