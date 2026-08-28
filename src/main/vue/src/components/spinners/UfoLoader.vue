<template>
  <div class="ufo-loader" role="status" aria-label="Loading next flashcard">
    <svg class="ufo-svg" viewBox="0 0 220 132" aria-hidden="true">
      <ellipse class="shadow" cx="110" cy="118" rx="64" ry="9" fill="#3f4368" opacity="0.12"/>
      <g class="craft">
        <ellipse cx="110" cy="76" rx="95" ry="24" fill="#3f4368"/>
        <path d="M 72 60 A 38 38 0 0 1 148 60 Z" fill="#8aa9e0"/>
        <g class="lights">
          <circle
            v-for="(light, index) in lights"
            :key="index"
            class="light"
            :cx="light.cx"
            :cy="light.cy"
            r="5.5"
            :fill="light.color"
            :style="{ animationDelay: light.delay }"
          />
        </g>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
const CYCLE_SECONDS = 1.7
const cycleDuration = `${CYCLE_SECONDS}s`

const lightDefs = [
  { cx: 66, cy: 82, color: '#48dcea' },
  { cx: 88, cy: 82, color: '#5aa6f0' },
  { cx: 110, cy: 82, color: '#9b86f0' },
  { cx: 132, cy: 82, color: '#e58fd0' },
  { cx: 154, cy: 82, color: '#48dcea' },
]

const lights = lightDefs.map((light, index) => ({
  ...light,
  delay: `${(CYCLE_SECONDS / lightDefs.length) * index}s`,
}))
</script>

<style scoped>
.ufo-loader {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ufo-svg {
  width: 200px;
  height: auto;
}

.craft {
  animation: ufo-bob 2.8s ease-in-out infinite;
}

.light {
  opacity: 0.3;
  animation-name: ufo-light-chase;
  animation-duration: v-bind(cycleDuration);
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}

@keyframes ufo-bob {
  0%, 100% {
    transform: translateY(-3px);
  }
  50% {
    transform: translateY(3px);
  }
}

@keyframes ufo-light-chase {
  0%, 100% {
    opacity: 0.3;
  }
  9% {
    opacity: 1;
  }
  24% {
    opacity: 0.3;
  }
}

@keyframes ufo-light-soft {
  0%, 100% {
    opacity: 0.35;
  }
  50% {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .craft {
    animation: none;
  }

  .light {
    animation-name: ufo-light-soft;
    animation-delay: 0s !important;
  }
}
</style>
