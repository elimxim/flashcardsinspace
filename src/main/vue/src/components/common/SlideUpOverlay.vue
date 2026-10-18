<template>
  <div class="slide-up-overlay">
    <div
      class="overlay-base"
      :class="{ 'overlay-base--covered': visible }"
      :inert="visible || undefined"
    >
      <slot />
    </div>
    <Transition name="overlay-slide" appear>
      <div v-if="visible" class="overlay-panel">
        <slot name="overlay" />
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    visible: boolean
    duration?: number
    overlayDelay?: number
    baseDelay?: number
  }>(),
  {
    duration: 500,
    overlayDelay: 0,
    baseDelay: 200,
  },
)

const durationMs = computed(() => `${props.duration}ms`)
const overlayDelayMs = computed(() => `${props.overlayDelay}ms`)
const baseDelayMs = computed(() => `${props.baseDelay}ms`)
const baseHideDelayMs = computed(() => `${props.overlayDelay + props.duration}ms`)
</script>

<style scoped>
.slide-up-overlay {
  flex: 1;
  position: relative;
  width: 100%;
  min-height: 0;
  display: flex;
}

.overlay-base {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    opacity v-bind(durationMs) ease v-bind(baseDelayMs),
    transform v-bind(durationMs) ease v-bind(baseDelayMs),
    visibility 0s linear v-bind(baseDelayMs);
}

.overlay-base--covered {
  opacity: 0;
  transform: scale(0.94);
  pointer-events: none;
  visibility: hidden;
  transition:
    opacity v-bind(durationMs) ease v-bind(overlayDelayMs),
    transform v-bind(durationMs) ease v-bind(overlayDelayMs),
    visibility 0s linear v-bind(baseHideDelayMs);
}

.overlay-panel {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
  z-index: 5;
}

.overlay-slide-enter-active {
  transition:
    transform v-bind(durationMs) cubic-bezier(0.22, 1, 0.36, 1) v-bind(overlayDelayMs),
    opacity v-bind(durationMs) ease-out v-bind(overlayDelayMs);
}

.overlay-slide-leave-active {
  transition:
    transform calc(v-bind(durationMs) * 0.8) cubic-bezier(0.64, 0, 0.78, 0),
    opacity calc(v-bind(durationMs) * 0.8) ease-in;
}

.overlay-slide-enter-from,
.overlay-slide-leave-to {
  transform: translateY(100vh);
  opacity: 0;
}
</style>
