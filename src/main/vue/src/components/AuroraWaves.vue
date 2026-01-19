<template>
  <div class="aurora-waves-container">
    <svg class="waves-svg" preserveAspectRatio="none" viewBox="0 0 100 100">
      <defs>
        <linearGradient
          v-for="(color, index) in colors"
          :key="`grad-${index}`"
          :id="`grad-${index}`"
          x1="0%" y1="0%" x2="100%" y2="0%"
        >
          <stop offset="0%" :stop-color="color" stop-opacity="0" />
          <stop offset="50%" :stop-color="color" stop-opacity="1" />
          <stop offset="100%" :stop-color="color" stop-opacity="0" />
        </linearGradient>
      </defs>

      <path
        v-for="line in lines"
        :key="line.id"
        class="wave-path"
        :d="line.pathShape"
        :stroke="`url(#grad-${line.colorIndex})`"
        :style="{
          '--duration': line.duration + 's',
          '--y-pos': line.yPos, /* Unitless: interpreted as translation in SVG space */
          '--scale-y': line.amplitude,
          /* Remove 'px'. These are now raw SVG units (0-100 scale) */
          '--line-len': line.length,
          '--gap-len': '1000', /* Giant gap relative to 100-unit width */
          '--base-thick': line.thickness,
          '--peak-thick': (line.thickness * pulsePeak),
          '--blur-val': blurAmount + 'px' /* Blur still needs px usually */
        }"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const props = withDefaults(defineProps<{
  colors?: string[],

  // NEW: Inputs are roughly 0.0 to 1.0 (Percentage of screen width)
  minLength?: number, // e.g., 0.3 = 30% of screen width
  maxLength?: number, // e.g., 0.8 = 80% of screen width

  // NEW: Thickness relative to SVG coords (approx 0.1 to 2.0)
  minThickness?: number,
  maxThickness?: number,
  pulsePeak?: number,
  minDuration?: number,
  maxDuration?: number,
  minAmplitude?: number,
  maxAmplitude?: number,
  spawnInterval?: number,
  maxLines?: number,
  blurAmount?: number,
}>(), {
  colors: () => ['#00ffb3', '#c63aed', '#3a64ed', '#00d2ff'],
  // Default: Length is 30% to 60% of screen width
  minLength: 0.3,
  maxLength: 0.6,
  // Default: Thickness is delicate
  minThickness: 1,
  maxThickness: 3,
  pulsePeak: 1.2,
  minDuration: 15,
  maxDuration: 30,
  minAmplitude: 0.5,
  maxAmplitude: 1.5,
  spawnInterval: 500,
  maxLines: 20,
  blurAmount: 10
});

interface WaveLine {
  id: number;
  colorIndex: number;
  pathShape: string;
  yPos: number;
  duration: number;
  length: number;    // SVG Units (0-100)
  thickness: number; // SVG Units
  amplitude: number;
}

const lines = ref<WaveLine[]>([]);
let lineIdCounter = 0;
let intervalId: any = null;
let rapidSpawnId: any = null;

// NORMALIZED PATHS (0-100 Coordinates)
// Start X: -20 (20% off-screen left)
// End X: 120 (20% off-screen right)
// Y Center: 50 (Middle)
const pathShapes = [
  // Gentle Sine
  "M -20,50 C 20,30 50,70 80,50 S 110,30 140,50",
  // Deep Valley
  "M -20,50 C 20,80 50,90 80,50 S 110,20 140,50",
  // High Arch
  "M -20,50 C 20,20 50,10 80,50 S 110,80 140,50",
  // Diagonal Flow
  "M -20,70 C 20,70 50,30 80,30 S 110,70 140,30"
];

// Helper: Normalize inputs to 100-unit scale
// If user passes 0.5, we treat it as 50 SVG units.
const scaleUnit = (val: number) => val * 100;

const random = (min: number, max: number) => Math.random() * (max - min) + min;

const addLine = () => {
  const id = lineIdCounter++;
  const duration = random(props.minDuration, props.maxDuration);

  const newLine: WaveLine = {
    id,
    colorIndex: Math.floor(random(0, props.colors.length)),
    pathShape: pathShapes[Math.floor(random(0, pathShapes.length))],

    // Y-Pos: -10 to 110 (allows vertically centering the wave)
    yPos: random(-10, 110),

    duration: duration,
    // Convert 0.3 -> 30 SVG units
    length: random(scaleUnit(props.minLength), scaleUnit(props.maxLength)),
    thickness: random(props.minThickness, props.maxThickness),
    amplitude: random(props.minAmplitude, props.maxAmplitude)
  };

  lines.value.push(newLine);

  setTimeout(() => {
    lines.value = lines.value.filter(l => l.id !== id);
  }, duration * 1000 + 1000);
};

onMounted(() => {
  // Rapid start
  rapidSpawnId = setInterval(() => {
    if (lines.value.length < props.maxLines) addLine();
  }, 200);

  setTimeout(() => {
    clearInterval(rapidSpawnId);
    intervalId = setInterval(() => {
      if (document.hidden) return;
      if (lines.value.length < props.maxLines) {
        addLine();
      }
    }, props.spawnInterval);
  }, 3000);
});

onUnmounted(() => {
  clearInterval(intervalId);
  clearInterval(rapidSpawnId);
});
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
  /* No overflow hidden on SVG allows blurs to spill out */
  overflow: visible;
}

.wave-path {
  fill: none;
  stroke-linecap: round;

  /* Use CSS blur (px units are fine here as they are screen-relative) */
  filter: blur(var(--blur-val));
  mix-blend-mode: screen;

  /* UNITLESS VALUES (Relative to 100 viewBox) */
  stroke-width: var(--base-thick);
  stroke-dasharray: var(--line-len) var(--gap-len);
  stroke-dashoffset: var(--line-len);

  /* We interpret y-pos as a percentage translation relative to the viewbox.
     Since viewbox is 100 units high, 'translateY(50)' moves it to the middle.
  */
  transform-origin: center;
  will-change: transform, stroke-dashoffset;

  animation:
    snakeTravel var(--duration) linear forwards,
    pulseAlive 3s ease-in-out infinite alternate;
}

@keyframes snakeTravel {
  0% {
    stroke-dashoffset: var(--line-len);
    /* Start X: 0 (which maps to -20 in SVG space) */
    transform: translateY(calc(var(--y-pos) * 1px)) translateX(0) scaleY(var(--scale-y));
    opacity: 0;
  }
  5% { opacity: 1; }
  100% {
    /* End Offset: -300 units.
       Since width is 100 units, -300 ensures it fully clears any screen 3x over.
    */
    stroke-dashoffset: -300;
    transform: translateY(calc(var(--y-pos) * 1px)) translateX(0) scaleY(var(--scale-y));
    opacity: 1;
  }
}

@keyframes pulseAlive {
  0% { stroke-width: var(--base-thick); }
  100% { stroke-width: var(--peak-thick); }
}
</style>
