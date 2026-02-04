<template>
  <div class="user-info-skeleton">
    <div class="skeleton user-info-row-skeleton"/>
    <div class="skeleton user-info-row-skeleton"/>
    <div class="skeleton user-info-row-skeleton"/>
    <div class="skeleton user-info-row-skeleton--thin"/>
    <div class="skeleton user-info-row-skeleton--thin user-info-row-skeleton--short"/>
  </div>
  <div class="skeleton user-controls-skeleton"/>
</template>

<script setup lang="ts">
</script>

<style scoped>
.user-info-skeleton {
  flex: 0 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(6, auto);
  grid-auto-flow: column;
  gap: 10px;
  max-width: 600px;
}

.user-info-row-skeleton {
  height: 46px;
  width: 100%;
}

.user-info-row-skeleton--thin {
  height: 16px;
}

.user-info-row-skeleton--short {
  width: 50%;
}

.user-controls-skeleton {
  margin-top: 20px;
  width: 100%;
  height: 40px;
  max-width: 600px;
}

</style>
