<template>
  <div class="kinetic-ring-spinner">
    <div
      class="kinetic-ring"
      :style="{
        '--ring-size': ringSizePx,
        '--ring-track--size': trackSizePx,
        '--ring-track--color': trackColor
      }"
    >
      <div class="ring-track"/>
      <div class="ring-beam"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  ringSize?: number
  trackSize?: number
  trackColor?: string
}>(), {
  ringSize: 160,
  trackSize: 12,
  trackColor: '#f1f5f9',
})

const ringSizePx = computed(() => `${props.ringSize}px`)
const trackSizePx = computed(() => `${props.trackSize}px`)

</script>

<style scoped>
.kinetic-ring-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
}

.kinetic-ring {
  position: relative;
  width: var(--ring-size);
  height: var(--ring-size);
  display: flex;
  justify-content: center;
  align-items: center;
}

.ring-track {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: var(--ring-track--size) solid var(--ring-track--color);
}

.ring-beam {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: var(--ring-track--size) solid transparent;
  background: conic-gradient(
    from 0deg,
    transparent 0%,
    #7f42b8 15%,
    #a855f7 25%,
    transparent 35%
  ) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask-composite: exclude;
  filter: drop-shadow(0 0 8px rgba(127, 66, 184, 0.6));
  animation: rotate 1.2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
