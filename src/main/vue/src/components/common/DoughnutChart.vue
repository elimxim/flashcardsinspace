<template>
  <div class="doughnut-chart--wrapper doughnut-chart--theme">
    <div class="doughnut-chart">
      <div class="doughnut-body">
        <svg class="doughnut-circle" viewBox="0 0 100 100">
          <defs v-if="segmented">
            <mask
              :id="segmentMaskId"
              maskUnits="userSpaceOnUse"
              x="0"
              y="0"
              width="100"
              height="100"
            >
              <circle
                class="segment-mask"
                cx="50"
                cy="50"
                :r="RADIUS"
                fill="none"
                :stroke-dasharray="segmentDashArray"
                :stroke-dashoffset="segmentGap / 2"
                transform="rotate(-90, 50, 50)"
              />
            </mask>
          </defs>
          <g :mask="segmented ? `url(#${segmentMaskId})` : undefined">
            <circle
              class="arc-right"
              cx="50"
              cy="50"
              :r="RADIUS"
              fill="none"
              :stroke-dasharray="rightDashArray"
              transform="rotate(-90, 50, 50)"
            />
            <g transform="translate(100, 0) scale(-1, 1)">
              <circle
                class="arc-left"
                cx="50"
                cy="50"
                :r="RADIUS"
                fill="none"
                :stroke-dasharray="leftDashArray"
                transform="rotate(-90, 50, 50)"
              />
            </g>
          </g>
        </svg>
        <div class="doughnut-center">
          <div class="doughnut-total">
            {{ shownTotal }}
          </div>
          <div class="doughnut-total-label">Total</div>
        </div>
      </div>
    </div>
    <div v-if="showLegend" class="doughnut-legend">
      <div class="doughnut-legend-item">
        <span class="doughnut-legend-color right"></span>
        <span class="legend-text">
          {{ legendRight }}: {{ shownRightCount }} ({{ rightPercentage }}%)
        </span>
      </div>
      <div class="doughnut-legend-item">
        <span class="doughnut-legend-color left"></span>
        <span class="legend-text">
          {{ legendLeft }}: {{ shownLeftCount }} ({{ leftPercentage }}%)
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, useId, watch } from 'vue'

// Fixed radius in viewBox coordinates (viewBox is 100x100)
// Radius of 42 leaves room for stroke width of ~16 (42 + 8 = 50, centered)
const RADIUS = 42
const CIRCUMFERENCE = 2 * Math.PI * RADIUS
const ANIMATION_DURATION = 1200

const props = withDefaults(
  defineProps<{
    left: number
    right: number
    showLegend?: boolean
    legendLeft?: string
    legendRight?: string
    animationDelay?: number
    segmented?: boolean
    segmentGap?: number
    segmentCount?: number
  }>(),
  {
    showLegend: true,
    legendLeft: 'Left',
    legendRight: 'Right',
    animationDelay: 200,
    segmented: false,
    segmentGap: 0.7,
    segmentCount: 40,
  },
)

const segmentMaskId = `doughnut-segments-${useId()}`
const segmentDashArray = `${CIRCUMFERENCE / props.segmentCount - props.segmentGap} ${props.segmentGap}`

const rightCount = computed(() => props.right)
const leftCount = computed(() => props.left)

const total = computed(() => props.left + props.right)

const rightRatio = computed(() => {
  if (total.value === 0) return 0
  return rightCount.value / total.value
})

const leftRatio = computed(() => {
  if (total.value === 0) return 0
  return leftCount.value / total.value
})

interface ChartValues {
  total: number
  rightCount: number
  leftCount: number
  rightRatio: number
  leftRatio: number
}

const zeroValues: ChartValues = {
  total: 0,
  rightCount: 0,
  leftCount: 0,
  rightRatio: 0,
  leftRatio: 0,
}

const targetValues = computed<ChartValues>(() => ({
  total: total.value,
  rightCount: rightCount.value,
  leftCount: leftCount.value,
  rightRatio: rightRatio.value,
  leftRatio: leftRatio.value,
}))

const shown = ref<ChartValues>({ ...zeroValues })
let animationFrameId: number | null = null
let firstSweep = true

function easeOutCubic(t: number): number {
  return 1 - Math.pow(1 - t, 3)
}

function animateTo(target: ChartValues) {
  if (animationFrameId !== null) cancelAnimationFrame(animationFrameId)
  const from = { ...shown.value }
  // The delay holds back only the first sweep, so later value updates follow right away
  const delay = firstSweep ? props.animationDelay : 0
  firstSweep = false
  const startTime = performance.now() + delay

  const step = (now: number) => {
    const t = Math.min(1, Math.max(0, now - startTime) / ANIMATION_DURATION)
    const k = easeOutCubic(t)
    shown.value = {
      total: from.total + (target.total - from.total) * k,
      rightCount: from.rightCount + (target.rightCount - from.rightCount) * k,
      leftCount: from.leftCount + (target.leftCount - from.leftCount) * k,
      rightRatio: from.rightRatio + (target.rightRatio - from.rightRatio) * k,
      leftRatio: from.leftRatio + (target.leftRatio - from.leftRatio) * k,
    }
    animationFrameId = t < 1 ? requestAnimationFrame(step) : null
  }
  animationFrameId = requestAnimationFrame(step)
}

watch(targetValues, animateTo, { immediate: true })

onUnmounted(() => {
  if (animationFrameId !== null) cancelAnimationFrame(animationFrameId)
})

const shownTotal = computed(() => Math.round(shown.value.total))
const shownRightCount = computed(() => Math.round(shown.value.rightCount))
const shownLeftCount = computed(() => Math.round(shown.value.leftCount))

const rightPercentage = computed(() => {
  return (shown.value.rightRatio * 100).toFixed(0)
})

const leftPercentage = computed(() => {
  return (shown.value.leftRatio * 100).toFixed(0)
})

const rightDashArray = computed(() => {
  const arcLength = shown.value.rightRatio * CIRCUMFERENCE
  const gap = CIRCUMFERENCE - arcLength
  return `${arcLength} ${gap}`
})

const leftDashArray = computed(() => {
  const arcLength = shown.value.leftRatio * CIRCUMFERENCE
  const gap = CIRCUMFERENCE - arcLength
  return `${arcLength} ${gap}`
})
</script>

<style scoped>
.doughnut-chart--theme {
  --d-chart--arc--thickness: var(--doughnut-chart--arc--thickness, 16);
  --d-chart--arc--right--color: var(--doughnut-chart--arc--right--color, #4caf50);
  --d-chart--arc--left--color: var(--doughnut-chart--arc--left--color, #f44336);
  --d-chart--text--color: var(--doughnut-chart--text--color, #6a6a6a);
  --d-chart--total--color: var(--doughnut-chart--total--color, #353535);
}

.doughnut-chart--wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  height: 100%;
}

.doughnut-chart {
  flex: 1;
  min-height: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  container-type: size;
}

.doughnut-body {
  position: relative;
  width: min(100cqw, 100cqh);
  height: min(100cqw, 100cqh);
  padding: 2px;
}

.doughnut-circle {
  width: 100%;
  height: 100%;
}

.arc-right {
  stroke: var(--d-chart--arc--right--color);
  stroke-width: var(--d-chart--arc--thickness);
  stroke-linecap: butt;
}

.arc-left {
  stroke: var(--d-chart--arc--left--color);
  stroke-width: var(--d-chart--arc--thickness);
  stroke-linecap: butt;
}

.segment-mask {
  stroke: white;
  stroke-width: calc(var(--d-chart--arc--thickness) + 2);
}

.doughnut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.doughnut-total {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--d-chart--total--color);
}

.doughnut-total-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  color: var(--d-chart--text--color);
}

.doughnut-legend {
  flex: none;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 4px;
}

.doughnut-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.doughnut-legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.doughnut-legend-color.right {
  background: var(--d-chart--arc--right--color);
}

.doughnut-legend-color.left {
  background: var(--d-chart--arc--left--color);
}

.legend-text {
  font-size: 0.85rem;
  color: var(--d-chart--text--color);
}
</style>
