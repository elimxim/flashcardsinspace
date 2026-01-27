<template>
  <div
    class="void"
    :style="{
      width: containerSize + 'px',
      height: containerSize + 'px'
    }"
  >
    <svg width="0" height="0" style="position: absolute;">
      <defs>
        <filter id="liquid-fusion" x="-100%" y="-100%" width="300%" height="300%">
          <feGaussianBlur in="SourceGraphic" :stdDeviation="gooeyIntensity"/>
          <feColorMatrix type="matrix" :values="`1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 ${matrixAlpha}`"/>
        </filter>

        <filter id="ethereal-glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur in="SourceGraphic" :stdDeviation="glowAmount / 2" result="blur"/>
          <feMerge>
            <feMergeNode in="blur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>
    </svg>

    <div class="glow-layer" :style="{ filter: 'url(#ethereal-glow)' }">
      <div class="engine" :style="{ filter: 'url(#liquid-fusion)' }">
        <div class="body b1" :style="{
          width: bodySizes[0] + 'px', height: bodySizes[0] + 'px',
          backgroundColor: bodyColors[0],
          animationDuration: animationDuration(4.7),
          '--tx': physics.b1.limitX + 'px',
          '--ty': physics.b1.limitY * -1 + 'px',
          '--tx-mid': physics.b1.limitX * -1 + 'px',
          '--ty-mid': physics.b1.limitY + 'px'
        }"></div>

        <div class="body b2" :style="{
          width: bodySizes[1] + 'px', height: bodySizes[1] + 'px',
          backgroundColor: bodyColors[1],
          animationDuration: animationDuration(3.1),
          '--tx': physics.b2.limitX * -1 + 'px',
          '--ty': physics.b2.limitY * -1 + 'px',
          '--tx-mid': physics.b2.limitX + 'px',
          '--ty-mid': physics.b2.limitY + 'px'
        }"></div>

        <div class="body b3" :style="{
          width: bodySizes[2] + 'px', height: bodySizes[2] + 'px',
          backgroundColor: bodyColors[2],
          animationDuration: animationDuration(5.9),
          '--orbit': physics.b3.orbit + 'px'
        }"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  containerSize?: number
  bodySizes?: number[]
  bodyColors?: string[]
  speed?: number
  accentColor?: string
  gooeyIntensity?: number
  glowAmount?: number
}>(), {
  containerSize: 100,
  bodySizes: () => [14, 11, 12],
  bodyColors: () => ['#c63aed', '#5864ed', '#00d2ff'],
  speed: 1,
  gooeyIntensity: 2,
  glowAmount: 2
})

const animationDuration = (base: number) => `${base / props.speed}s`

const physics = computed(() => {
  const radius = props.containerSize / 2
  const buffer = props.gooeyIntensity + (props.glowAmount / 2) + 4

  const getLimit = (bodySize: number) => radius - (bodySize / 2) - buffer

  return {
    b1: {
      limitX: getLimit(props.bodySizes[0]) * 0.9,
      limitY: getLimit(props.bodySizes[0]) * 0.4,
    },
    b2: {
      limitX: getLimit(props.bodySizes[1]) * 0.5,
      limitY: getLimit(props.bodySizes[1]) * 0.9,
    },
    b3: {
      orbit: getLimit(props.bodySizes[2]) * 0.8,
    },
  }
})

const matrixAlpha = computed(() =>
  props.gooeyIntensity === 0 ? "0 0 0 1 0" : "0 0 0 18 -7"
)

</script>


<style scoped>
.void {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: transparent;
}

.glow-layer {
  width: 100%;
  height: 100%;
}

.engine {
  position: relative;
  width: 100%;
  height: 100%;
  isolation: isolate;
  animation: drift 20s infinite linear;
}

.body {
  position: absolute;
  top: 50%;
  left: 50%;
  border-radius: 50%;
  mix-blend-mode: screen;
  translate: -50% -50%;
  will-change: transform;
}

@keyframes chaos-1 {
  0%, 100% {
    transform: translate(var(--tx), var(--ty));
  }
  50% {
    transform: translate(var(--tx-mid), var(--ty-mid));
  }
}

@keyframes chaos-2 {
  0%, 100% {
    transform: translate(var(--tx), var(--ty));
  }
  50% {
    transform: translate(var(--tx-mid), var(--ty-mid));
  }
}

@keyframes chaos-3 {
  0% {
    transform: rotate(0deg) translateX(var(--orbit)) rotate(0deg);
  }
  100% {
    transform: rotate(360deg) translateX(var(--orbit)) rotate(-360deg);
  }
}

.b1 {
  animation: chaos-1 infinite ease-in-out;
}

.b2 {
  animation: chaos-2 infinite cubic-bezier(0.45, 0, 0.55, 1);
}

.b3 {
  animation: chaos-3 infinite linear;
}

@keyframes drift {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
