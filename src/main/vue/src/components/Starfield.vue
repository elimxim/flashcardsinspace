<template>
  <div class="starfield" aria-hidden="true">
    <span v-for="s in stars" :key="s.id" class="starfield__star" :style="starStyle(s)"/>
    <slot/>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mulberry32 } from '@/utils/utils.ts'

const props = withDefaults(defineProps<{
  /** How many stars */
  density?: number
  /** Average star size */
  starSize?: number
  /** Enable twinkle animation */
  twinkle?: boolean
  /** Vertical drift amplitude; 0 disables drift */
  verticalDrift?: string
  /** Seed for deterministic field */
  seed?: number | null
}>(), {
  density: 24,
  starSize: 1.4,
  twinkle: false,
  verticalDrift: "0px",
  seed: null,
})

interface Star {
  id: number;
  top: number;
  left: number;
  size: number;
  twinkleDuration: number;
  verticalDriftDuration: number
}

const stars = computed<Star[]>(() => {
  const rand = mulberry32(props.seed ?? Math.floor(Math.random() * 1e9))
  const starNumber = Math.max(1, Math.round(props.density))
  const result: Star[] = []
  for (let i = 0; i < starNumber; i++) {
    const starSize = props.starSize * (0.6 + rand() * 1.1)
    const twinkleDuration = props.twinkle ? 2 + rand() * 3 : 0
    const verticalDriftDuration = parseInt(props.verticalDrift) > 0 ? 6 + rand() * 4 : 0
    result.push({
      id: i,
      top: rand() * 100,
      left: rand() * 100,
      size: starSize,
      twinkleDuration:  twinkleDuration,
      verticalDriftDuration: verticalDriftDuration,
    })
  }
  return result
})

function starStyle(s: Star) {
  return {
    top: s.top + '%',
    left: s.left + '%',
    width: s.size + 'px',
    height: s.size + 'px',
    animationDuration: `${s.twinkleDuration + 's'}, ${s.verticalDriftDuration + 's'}`,
  }
}
</script>

<style scoped>
.starfield {
  position: absolute;
  overflow: hidden;
  pointer-events: none;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.starfield__star {
  position: absolute;
  opacity: 1;
  border-radius: 9999px;
  background-color: var(--starfield__star-color, rgba(255, 255, 255, 0.9));
  will-change: opacity, transform;
  animation-name: twinkle, verticalDrift;
  animation-timing-function: ease-in-out, ease-in-out;
  animation-iteration-count: infinite, infinite;
}

@keyframes twinkle {
  0% {
    opacity: .35
  }
  50% {
    opacity: 1
  }
  100% {
    opacity: .35
  }
}

@keyframes verticalDrift {
  0% {
    transform: translateY(0)
  }
  50% {
    transform: translateY(v-bind(verticalDrift))
  }
  100% {
    transform: translateY(0)
  }
}
</style>
