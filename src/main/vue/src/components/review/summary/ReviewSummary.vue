<template>
  <div class="review-summary">
    <div v-if="title" class="review-summary-title">{{ title }}</div>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
  }>(),
  {
    title: undefined,
  },
)
</script>

<style scoped>
.review-summary {
  width: clamp(280px, 92vw, 760px);
  height: auto;
  margin: auto;
  display: flex;
  flex-direction: column;
  border-radius: 18px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  container-type: inline-size;
}

.review-summary-title {
  padding: clamp(8px, 1.2cqw, 12px) clamp(8px, 1.2cqw, 12px) 0;
  font-size: clamp(1rem, 3cqw, 1.4rem);
  font-weight: 500;
  letter-spacing: 0.02rem;
  text-align: center;
  text-wrap: balance;
  color: #353535;
}
</style>
