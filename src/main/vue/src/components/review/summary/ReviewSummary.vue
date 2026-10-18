<template>
  <div class="review-summary">
    <slot />
  </div>
</template>

<style scoped>
.review-summary {
  width: clamp(280px, 92vw, 760px);
  height: auto;
  margin: auto;
  border-radius: 18px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}
</style>
