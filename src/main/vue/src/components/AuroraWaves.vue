<template>
  <div class="aurora-waves-container">
    <svg class="waves-svg" preserveAspectRatio="none" viewBox="0 0 100 100">
      <defs>
        <linearGradient
          v-for="(color, index) in colors"
          :id="`grad-${index}`"
          :key="`grad-${index}`"
          x1="0%" y1="0%"
          x2="100%" y2="0%"
        >
          <stop offset="0%" :stop-color="color" stop-opacity="0"/>
          <stop offset="50%" :stop-color="color" stop-opacity="1"/>
          <stop offset="100%" :stop-color="color" stop-opacity="0"/>
        </linearGradient>
      </defs>

      <path
        v-for="line in lines"
        :key="line.id"
        class="wave-path"
        :d="line.pathShape"
        :stroke="`url(#grad-${line.colorIndex})`"
        :style="{
          '--wave--duration': line.duration + 's',
          '--wave--y-pos': line.yPos,
          '--wave--y-scale': line.amplitude,
          '--wave--line-length': line.length,
          '--wave--gap-length': '1000',
          '--wave--base-thick': line.thickness,
          '--wave--peak-thick': (line.thickness * pulsePeak),
          '--wave--blur': blurAmount + 'px'
        }"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  colors?: string[]
  minLength?: number
  maxLength?: number
  minThickness?: number
  maxThickness?: number
  pulsePeak?: number
  minDuration?: number
  maxDuration?: number
  minAmplitude?: number
  maxAmplitude?: number
  spawnInterval?: number
  maxLines?: number
  blurAmount?: number
}>(), {
  colors: () => ['#00ffb3', '#c63aed', '#3a64ed', '#00d2ff'],
  minLength: 0.3,
  maxLength: 0.6,
  minThickness: 1,
  maxThickness: 3,
  pulsePeak: 1.2,
  minDuration: 15,
  maxDuration: 30,
  minAmplitude: 0.5,
  maxAmplitude: 1.5,
  spawnInterval: 500,
  maxLines: 20,
  blurAmount: 10,
})

interface WaveLine {
  id: number
  colorIndex: number
  pathShape: string
  yPos: number
  duration: number
  length: number
  thickness: number
  amplitude: number
}

const lines = ref<WaveLine[]>([])
let lineIdCounter = 0
let intervalId: ReturnType<typeof setInterval> | null = null
let rapidSpawnId: ReturnType<typeof setInterval> | null = null

// Normalized paths (0-100 Coordinates)
// Start X: -20 (20% off-screen left)
// End X: 120 (20% off-screen right)
// Center Y: 50 (Middle)
const pathShapes = [
  // Gentle Sine
  "M -20,50 C 20,30 50,70 80,50 S 110,30 140,50",
  // Deep Valley
  "M -20,50 C 20,80 50,90 80,50 S 110,20 140,50",
  // High Arch
  "M -20,50 C 20,20 50,10 80,50 S 110,80 140,50",
  // Diagonal Flow
  "M -20,70 C 20,70 50,30 80,30 S 110,70 140,30"
]

// Normalizes inputs to 100-unit scale
const scaleUnit = (val: number) => val * 100

const random = (min: number, max: number) => Math.random() * (max - min) + min

const addLine = () => {
  const id = lineIdCounter++
  const duration = random(props.minDuration, props.maxDuration)

  const newLine: WaveLine = {
    id,
    colorIndex: Math.floor(random(0, props.colors.length)),
    pathShape: pathShapes[Math.floor(random(0, pathShapes.length))],
    yPos: random(-20, 40),
    duration: duration,
    length: random(scaleUnit(props.minLength), scaleUnit(props.maxLength)),
    thickness: random(props.minThickness, props.maxThickness),
    amplitude: random(props.minAmplitude, props.maxAmplitude)
  }

  lines.value.push(newLine)

  setTimeout(() => {
    lines.value = lines.value.filter(l => l.id !== id)
  }, duration * 1000 + 1000)
}

onMounted(() => {
  rapidSpawnId = setInterval(() => {
    if (lines.value.length < props.maxLines) addLine()
  }, 200)

  setTimeout(() => {
    if (rapidSpawnId) {
      clearInterval(rapidSpawnId)
    }
    intervalId = setInterval(() => {
      if (document.hidden) return
      if (lines.value.length < props.maxLines) {
        addLine()
      }
    }, props.spawnInterval)
  }, 3000)
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
  if (rapidSpawnId) {
    clearInterval(rapidSpawnId)
  }
})
</script>

<style scoped>
.aurora-waves-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.waves-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.wave-path {
  fill: none;
  stroke-linecap: round;
  filter: blur(var(--wave--blur));
  mix-blend-mode: screen;
  stroke-width: var(--wave--base-thick);
  stroke-dasharray: var(--wave--line-length) var(--wave--gap-length);
  stroke-dashoffset: var(--wave--line-length);
  transform-origin: center;
  will-change: transform, stroke-dashoffset;
  animation: snakeTravel var(--wave--duration) linear forwards,
  pulseAlive 3s ease-in-out infinite alternate;
}

@keyframes snakeTravel {
  0% {
    stroke-dashoffset: var(--wave--line-length);
    transform: translateY(calc(var(--wave--y-pos) * 1px)) translateX(0) scaleY(var(--wave--y-scale));
    opacity: 0;
  }
  5% {
    opacity: 1;
  }
  100% {
    stroke-dashoffset: -300;
    transform: translateY(calc(var(--wave--y-pos) * 1px)) translateX(0) scaleY(var(--wave--y-scale));
    opacity: 1;
  }
}

@keyframes pulseAlive {
  0% {
    stroke-width: var(--wave--base-thick);
  }
  100% {
    stroke-width: var(--wave--peak-thick);
  }
}
</style>
