<template>
  <div class="awesome-container awesome-container--theme">
    <div class="awesome-container-icon-wrapper">
      <font-awesome-icon :icon="icon" class="awesome-container-icon"/>
    </div>
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
  --a-container--icon--size: var(--awesome-container--icon--size, 36px);
  --a-container--icon--color: var(--awesome-container--icon--color, #c3c3c3);
  --a-container--gap: var(--awesome-container--gap, 1rem);
}

.awesome-container {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: var(--a-container--gap);
}

.awesome-container-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: calc(var(--a-container--icon--size) + 4px);
}

.awesome-container-icon {
  font-size: var(--a-container--icon--size);
  color: var(--a-container--icon--color);
}
</style>
