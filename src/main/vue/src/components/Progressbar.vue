<template>
  <div class="progress">
    <div class="progress__track">
      <div v-if="!indeterminate" class="progress__bar"/>
      <div v-else class="progress__bar progress__bar--indeterminate"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  /** Progress in [0,1] */
  progress?: number | null
  /** Total duration (ms) */
  duration?: number
  /** Start automatically on mount */
  autoStart?: boolean
  /** Pause timer */
  paused?: boolean
  /** Height */
  height?: string
  /** Rounds the ends of the track */
  trackRounded?: boolean
  /** Rounds the ends of the bar */
  barRounded?: boolean
  /** Right-to-left fill */
  reverse?: boolean
  /** Indeterminate visual */
  indeterminate?: boolean
  /** Optional delay before starting (ms) */
  delay?: number
}>(), {
  progress: null,
  duration: 4000,
  autoStart: true,
  paused: false,
  height: '8px',
  trackRounded: false,
  barRounded: false,
  reverse: false,
  indeterminate: false,
  delay: 0,
})

const emit = defineEmits<{ (e: 'done'): void }>()

const isControlled = computed(() => props.progress != null)
const localProgress = ref(0)
let animationFrame: number | null = null
let delayTimeout: number | null = null
let startAt = 0
let carried = 0

function start() {
  cancel()
  if (!props.duration || props.duration <= 0) {
    localProgress.value = 1
    emit('done')
  } else if (props.delay && props.delay > 0) {
    delayTimeout = window.setTimeout(() => {
      delayTimeout && window.clearTimeout(delayTimeout);
      delayTimeout = null;
      begin()
    }, props.delay)
  } else {
    begin()
  }
}

function begin() {
  startAt = performance.now()
  tick(startAt)
}

function tick(now: number) {
  if (props.paused) {
    animationFrame = requestAnimationFrame(tick)
    return
  }
  const elapsed = now - startAt + carried
  const progress = Math.min(1, elapsed / (props.duration || 1))

  localProgress.value = progress

  if (progress >= 1) {
    cancel()
    emit('done')
    return
  }

  animationFrame = requestAnimationFrame(tick)
}

function cancel() {
  if (animationFrame != null) {
    cancelAnimationFrame(animationFrame);
    animationFrame = null
  }
  if (delayTimeout != null) {
    window.clearTimeout(delayTimeout);
    delayTimeout = null
  }
}

watch(() => props.paused, (newValue) => {
  if (isControlled.value) return
  const now = performance.now()
  if (newValue) {
    carried += now - startAt
  } else {
    startAt = now
  }
})

watch(() => props.duration, () => {
  if (!isControlled.value) start()
})

onMounted(() => {
  if (!isControlled.value && props.autoStart) start()
})

onBeforeUnmount(cancel)

const barWidthPercent = computed(() => {
  if (isControlled.value) {
    return Math.max(0, Math.min(1, Number(props.progress)))
  } else {
    return localProgress.value
  }
})

const trackHeight = computed(() => props.height)
const trackBorderRadius = computed(() => props.trackRounded ? `${props.height}` : '0')
const barBorderRadius = computed(() => props.barRounded ? `${props.height}` : '0')
const barWidth = computed(() => `${Math.round(barWidthPercent.value * 10000) / 100}%`)
const barLeft = computed(() => props.reverse ? 'auto' : '0')
const barRight = computed(() => props.reverse ? '0' : 'auto')

</script>

<style scoped>
.progress {
  position: relative;
  pointer-events: none;
}

.progress__track {
  position: relative;
  width: 100%;
  height: v-bind(trackHeight);
  border-radius: v-bind(trackBorderRadius);
  background-color: var(--progressbar--bg-color, rgba(255, 255, 255, 0.10));
  overflow: hidden;
}

.progress__bar {
  position: absolute;
  top: 0;
  bottom: 0;
  width: v-bind(barWidth);
  left: v-bind(barLeft);
  right: v-bind(barRight);
  border-radius: v-bind(barBorderRadius);
  background-image: linear-gradient(
    90deg,
    var(--progressbar--from, #9f9f9f),
    var(--progressbar--via, #c1c1c1),
    var(--progressbar--to, #6e6e6e)
  );
}

.progress__bar--indeterminate {
  left: 0;
  right: 0;
  width: 30%;
  min-width: 80px;
  animation: indeterminate 1.2s infinite linear;
}

@keyframes indeterminate {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(400%);
  }
}
</style>
