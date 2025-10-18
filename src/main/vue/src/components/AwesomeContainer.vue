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
  --a-container--icon--font-size: var(--awesome-container--icon--font-size, 36px);
  --a-container--icon--color: var(--awesome-container--icon--color, #c3c3c3);
  --a-container--gap: var(--awesome-container--gap, 1rem);
}

.awesome-container {
  container-type: inline-size;
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: var(--a-container--gap);
}

.awesome-container__icon {
  font-size: var(--a-container--icon--font-size);
  color: var(--a-container--icon--color);
}
</style>
