<template>
  <div class="kinetic-ring-spinner kinetic-ring-spinner--theme">
    <div class="kinetic-ring">
      <div class="ring-track"/>
      <div
        class="ring-beam"
        :class="{
          'ring-beam--frozen': freeze,
        }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  ringSize?: number
  trackSize?: number
  beamSpeed?: number
  freeze?: boolean
}>(), {
  ringSize: 160,
  trackSize: 12,
  beamSpeed: 1.2,
  freeze: false,
})

const ringSizePx = computed(() => `${props.ringSize}px`)
const trackSizePx = computed(() => `${props.trackSize}px`)
const beamSpeedSeconds = computed(() => `${props.beamSpeed}s`)

</script>

<style scoped>
.kinetic-ring-spinner--theme {
  --ring-size: v-bind(ringSizePx);
  --ring-track--size: v-bind(trackSizePx);
  --ring-track--color: #f1f5f9;
  --beam--speed: v-bind(beamSpeedSeconds);
  --beam--color-1: var(--kinetic-ring-spinner--beam--color-1, #7f42b8);
  --beam--color-2: var(--kinetic-ring-spinner--beam--color-2, #a855f7);
  --beam--color-frozen: var(--kinetic-ring-spinner--beam--color-freeze, transparent);
}

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
    var(--beam--color-1) 15%,
    var(--beam--color-2) 25%,
    transparent 35%
  ) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask-composite: exclude;
  animation: rotate var(--beam--speed) linear infinite;
}

.ring-beam--frozen {
  animation-play-state: paused;
  background: var(--beam--color-frozen) border-box;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
