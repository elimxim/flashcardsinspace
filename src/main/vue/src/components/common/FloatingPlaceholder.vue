<template>
  <span
    class="floating-placeholder floating-placeholder--theme"
    :class="{ 'floating-placeholder--floating': floating }"
  >
    {{ text }}
  </span>
</template>

<script setup lang="ts">
defineProps<{
  text: string
  floating: boolean
}>()
</script>

<style scoped>
.floating-placeholder--theme {
  --flt-plchldr--top: var(--floating-placeholder--top, 0);
  --flt-plchldr--top--floating: var(--floating-placeholder--top--floating, 0);
  --flt-plchldr--left: var(--floating-placeholder--left, 0);
  --flt-plchldr--font-size: var(--floating-placeholder--font-size, 1rem);
  --flt-plchldr--color: var(--floating-placeholder--color, #b8c4d6);
}

.floating-placeholder {
  position: absolute;
  top: var(--flt-plchldr--top);
  left: var(--flt-plchldr--left);
  font-family: var(--main--font-family);
  font-size: var(--flt-plchldr--font-size);
  line-height: normal;
  color: var(--flt-plchldr--color);
  white-space: nowrap;
  pointer-events: none;
  transform-origin: left top;
  transition: transform 0.2s ease-in-out;
}

.floating-placeholder--floating {
  transform: translateY(calc(var(--flt-plchldr--top--floating) - var(--flt-plchldr--top))) scale(0.7);
}
</style>
