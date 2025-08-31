<template>
  <div class="awesome-container">
    <font-awesome-icon :icon="icon" class="awesome-icon"/>
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

.awesome-icon {
  font-size: var(--icon-size, 12cqi);
  color: var(--icon-color, #c3c3c3);
}
</style>
