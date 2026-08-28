<template>
  <div class="doughnut-chart--wrapper doughnut-chart--theme">
    <div class="doughnut-chart">
      <svg class="doughnut-circle" viewBox="0 0 100 100">
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
      </svg>
      <div class="doughnut-center">
        <div class="doughnut-total">
          {{ total }}
        </div>
        <div class="doughnut-total-label">
          Total
        </div>
      </div>
    </div>
    <div class="doughnut-legend">
      <div class="doughnut-legend-item">
        <span class="doughnut-legend-color right"></span>
        <span class="legend-text">
          {{ legendRight }}: {{ rightCount }} ({{ rightPercentage }}%)
        </span>
      </div>
      <div class="doughnut-legend-item">
        <span class="doughnut-legend-color left"></span>
        <span class="legend-text">
          {{ legendLeft }}: {{ leftCount }} ({{ leftPercentage }}%)
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// Fixed radius in viewBox coordinates (viewBox is 100x100)
// Radius of 42 leaves room for stroke width of ~16 (42 + 8 = 50, centered)
const RADIUS = 42
const CIRCUMFERENCE = 2 * Math.PI * RADIUS

const props = withDefaults(defineProps<{
  total: number
  left: number
  showLegend?: boolean
  legendLeft?: string
  legendRight?: string
}>(), {
  showLegend: true,
  legendLeft: 'Left',
  legendRight: 'Right',
})

const rightCount = computed(() => props.total - props.left)
const leftCount = computed(() => props.left)

const rightRatio = computed(() => {
  if (props.total === 0) return 0
  if (leftCount.value === 0) return 1
  return rightCount.value / props.total
})

const leftRatio = computed(() => {
  if (props.total === 0) return 0
  if (rightCount.value === 0) return 1
  return leftCount.value / props.total
})

const rightPercentage = computed(() => {
  return (rightRatio.value * 100).toFixed(0)
})

const leftPercentage = computed(() => {
  if (props.total === 0) return 0
  return (leftRatio.value * 100).toFixed(0)
})

const rightDashArray = computed(() => {
  const arcLength = rightRatio.value * CIRCUMFERENCE
  const gap = CIRCUMFERENCE - arcLength
  return `${arcLength} ${gap}`
})

const leftDashArray = computed(() => {
  const arcLength = leftRatio.value * CIRCUMFERENCE
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
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 100%;
}

.doughnut-chart {
  flex: 1;
  position: relative;
  aspect-ratio: 1;
  container-type: inline-size;
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
