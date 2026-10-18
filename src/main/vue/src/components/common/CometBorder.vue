<template>
  <div class="comet-border comet-border--theme" aria-hidden="true">
    <div class="comet-border-spot" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    lapDuration?: number
    fadeDuration?: number
  }>(),
  {
    lapDuration: 6000,
    fadeDuration: 3000,
  },
)

const lapDurationMs = computed(() => `${props.lapDuration}ms`)
const fadeDurationMs = computed(() => `${props.fadeDuration}ms`)
</script>

<style scoped>
@property --comet-border--angle {
  syntax: '<angle>';
  initial-value: 0deg;
  inherits: false;
}

.comet-border--theme {
  --c-border--spot: var(--comet-border--spot, #ede9fe);
  --c-border--tail: var(--comet-border--tail, rgba(167, 139, 250, 0.6));
  --c-border--glow: var(--comet-border--glow, rgba(139, 92, 246, 0.9));
}

.comet-border {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  filter: drop-shadow(0 0 4px var(--c-border--glow)) drop-shadow(0 0 10px var(--c-border--glow));
  animation: comet-border-fade v-bind(fadeDurationMs) ease-in-out infinite;
}

.comet-border-spot {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 2px;
  background: conic-gradient(
    from var(--comet-border--angle),
    transparent 0%,
    transparent 82%,
    var(--c-border--tail) 93%,
    var(--c-border--spot) 97%,
    transparent 100%
  );
  mask:
    linear-gradient(#000 0 0) content-box exclude,
    linear-gradient(#000 0 0);
  animation: comet-border-travel v-bind(lapDurationMs) linear infinite;
}

@keyframes comet-border-travel {
  to {
    --comet-border--angle: 360deg;
  }
}

@keyframes comet-border-fade {
  0%,
  100% {
    opacity: 0;
  }
  40%,
  60% {
    opacity: 1;
  }
}
</style>
