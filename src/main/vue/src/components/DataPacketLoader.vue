<template>
  <div ref="packetLoader" class="packet-loader" :style="{ height: sizePx }">
    <div
      v-for="i in count"
      :key="i"
      class="data-packet"
      :class="{ 'is-first': i === 1, 'is-last': i === count }"
      :style="{
        backgroundColor: color,
        width: sizePx,
        height: sizePx,
        left: `calc(${(i - 1)} * (${sizePx} + ${gapPx}))`,
        '--packet-distance': calculatedDistancePx,
        animationDuration: durationSec
      }"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue"

const props = withDefaults(defineProps<{
  gap?: number
  count?: number
  size?: number
  duration?: number
  color?: string
  distance?: number
}>(), {
  gap: 0,
  count: 4,
  size: 20,
  duration: 2.5,
  color: 'rgba(255, 255, 255, 0.1)',
  distance: 100,
})

const packetLoader = ref<HTMLElement>()

const gapPx = computed(() => `${props.gap}px`)
const sizePx = computed(() => `${props.size}px`)
const durationSec = computed(() => `${props.duration}s`)
const calculatedDistancePx = computed(() => `${props.distance}px`)

</script>

<style scoped>
.packet-loader {
  position: relative;
  width: 100%;
}

.data-packet {
  position: absolute;
  top: 0;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-sizing: border-box;
  /* Removed animation-name from here so nth-child can set it */
  animation-iteration-count: infinite;
  animation-timing-function: cubic-bezier(0.1, 0, 0.8, 1);
}

.is-first {
  border-radius: 4px 0 0 4px;
}

.is-last {
  border-radius: 0 4px 4px 0;
}

.data-packet:nth-child(1) {
  animation-name: move-1;
}

.data-packet:nth-child(2) {
  animation-name: move-2;
}

.data-packet:nth-child(3) {
  animation-name: move-3;
}

.data-packet:nth-child(4) {
  animation-name: move-4;
}

/* Square 1: Moves LAST out, FIRST back */
@keyframes move-1 {
  0%, 25% {
    transform: translateX(0);
  }
  45%, 55% {
    transform: translateX(var(--packet-distance));
  }
  75%, 100% {
    transform: translateX(0);
  }
}

/* Square 2 */
@keyframes move-2 {
  0%, 18% {
    transform: translateX(0);
  }
  38%, 62% {
    transform: translateX(var(--packet-distance));
  }
  82%, 100% {
    transform: translateX(0);
  }
}

/* Square 3 */
@keyframes move-3 {
  0%, 11% {
    transform: translateX(0);
  }
  31%, 69% {
    transform: translateX(var(--packet-distance));
  }
  89%, 100% {
    transform: translateX(0);
  }
}

/* Square 4: Moves FIRST out, LAST back */
@keyframes move-4 {
  0%, 4% {
    transform: translateX(0);
  }
  24%, 76% {
    transform: translateX(var(--packet-distance));
  }
  96%, 100% {
    transform: translateX(0);
  }
}
</style>
