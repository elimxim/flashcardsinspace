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
                :stroke-dashoffset="-segmentGap / 2"
                :transform="rotation"
              />
            </mask>
          </defs>
          <g :mask="segmented ? `url(#${segmentMaskId})` : undefined">
            <circle
              v-for="(arc, index) in arcs"
              :key="index"
              class="doughnut-arc"
              cx="50"
              cy="50"
              :r="RADIUS"
              fill="none"
              :style="{ stroke: arc.color }"
              :stroke-dasharray="arc.dashArray"
              :stroke-dashoffset="arc.dashOffset"
              :transform="rotation"
            />
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
      <div v-for="(item, index) in legendItems" :key="index" class="doughnut-legend-item">
        <span class="doughnut-legend-color" :style="{ background: item.color }"></span>
        <span class="legend-text">
          {{ item.legend }}: {{ item.count }} ({{ item.percentage }}%)
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export interface DoughnutPiece {
  value: number
  legend: string
  // Any CSS color, including var(--...), so callers can keep theming through custom properties
  color: string
}
</script>

<script setup lang="ts">
import { computed, onUnmounted, ref, useId, watch } from 'vue'

// Fixed radius in viewBox coordinates (viewBox is 100x100)
// Radius of 42 leaves room for stroke width of ~16 (42 + 8 = 50, centered)
const RADIUS = 42
const CIRCUMFERENCE = 2 * Math.PI * RADIUS
const ANIMATION_DURATION = 1200

// Pieces go clockwise from startAngle in array order
const props = withDefaults(
  defineProps<{
    pieces: DoughnutPiece[]
    startAngle?: number
    showLegend?: boolean
    // Piece indices in the order the legend lists them; defaults to the drawing order
    legendOrder?: number[]
    animationDelay?: number
    segmented?: boolean
    segmentGap?: number
    segmentCount?: number
  }>(),
  {
    startAngle: 0,
    showLegend: true,
    legendOrder: undefined,
    animationDelay: 200,
    segmented: false,
    segmentGap: 0.7,
    segmentCount: 40,
  },
)

const segmentMaskId = `doughnut-segments-${useId()}`
const segmentDashArray = `${CIRCUMFERENCE / props.segmentCount - props.segmentGap} ${props.segmentGap}`

// SVG circles start at 3 o'clock, so -90 moves the start to the top
const rotation = computed(() => `rotate(${props.startAngle - 90}, 50, 50)`)

interface ChartValues {
  total: number
  counts: number[]
  ratios: number[]
  arcs: number[]
}

function sum(values: number[]): number {
  return values.reduce((acc, v) => acc + v, 0)
}

// Segmented arcs end on whole segments, so one segment never shows two colors
function toSegments(ratio: number): number {
  if (ratio <= 0) return 0
  return Math.max(1, Math.round(ratio * props.segmentCount))
}

// Rounding each piece on its own can over- or undershoot the ring,
// so the difference goes to the pieces whose rounding was furthest off
function segmentArcs(ratios: number[]): number[] {
  const count = props.segmentCount
  const segments = ratios.map(toSegments)
  const error = (i: number) => ratios[i] * count - segments[i]
  let diff = segments.some((s) => s > 0) ? count - sum(segments) : 0

  while (diff !== 0) {
    let pick = -1
    for (let i = 0; i < segments.length; i++) {
      if (diff > 0 ? ratios[i] <= 0 : segments[i] <= 1) continue
      if (pick === -1 || (diff > 0 ? error(i) > error(pick) : error(i) < error(pick))) pick = i
    }
    if (pick === -1) break
    segments[pick] += Math.sign(diff)
    diff -= Math.sign(diff)
  }
  return segments.map((s) => s / count)
}

const targetValues = computed<ChartValues>(() => {
  const counts = props.pieces.map((p) => Math.max(0, p.value))
  const total = sum(counts)
  const ratios = counts.map((c) => (total === 0 ? 0 : c / total))
  // Only the target is snapped to segments; the sweep towards it stays smooth
  const arcs = props.segmented ? segmentArcs(ratios) : ratios
  return { total, counts, ratios, arcs }
})

const shown = ref<ChartValues>({ total: 0, counts: [], ratios: [], arcs: [] })
let animationFrameId: number | null = null
let firstSweep = true

function easeOutCubic(t: number): number {
  return 1 - Math.pow(1 - t, 3)
}

// Each value moves from its old to its new number by its own progress (0..1)
function lerpAll(from: number[], to: number[], progress: number[]): number[] {
  return to.map((v, i) => {
    const start = from[i] ?? 0
    return start + (v - start) * progress[i]
  })
}

// The ring fills like one stroke going clockwise: a piece starts growing
// only once the stroke reaches it, i.e. after the previous piece is complete
function sweepProgress(arcs: number[], k: number): number[] {
  let start = 0
  return arcs.map((arc) => {
    const progress = arc > 0 ? Math.min(1, Math.max(0, (k - start) / arc)) : k >= start ? 1 : 0
    start += arc
    return progress
  })
}

function animateTo(target: ChartValues) {
  if (animationFrameId !== null) cancelAnimationFrame(animationFrameId)
  const from = shown.value
  // Growing one by one only works from an empty ring. When a filled ring changes,
  // all pieces move together, so the ring stays whole instead of gapping or overlapping
  const fromEmpty = sum(from.arcs) === 0
  // The delay holds back only the first sweep, so later value updates follow right away
  const delay = firstSweep ? props.animationDelay : 0
  firstSweep = false
  const startTime = performance.now() + delay

  const step = (now: number) => {
    const t = Math.min(1, Math.max(0, now - startTime) / ANIMATION_DURATION)
    const k = easeOutCubic(t)
    const progress = fromEmpty ? sweepProgress(target.arcs, k) : target.arcs.map(() => k)
    shown.value = {
      total: from.total + (target.total - from.total) * k,
      counts: lerpAll(from.counts, target.counts, progress),
      ratios: lerpAll(from.ratios, target.ratios, progress),
      arcs: lerpAll(from.arcs, target.arcs, progress),
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

const arcs = computed(() => {
  let offset = 0
  return props.pieces.map((piece, i) => {
    const length = (shown.value.arcs[i] ?? 0) * CIRCUMFERENCE
    const arc = {
      color: piece.color,
      dashArray: `${length} ${CIRCUMFERENCE - length}`,
      dashOffset: -offset,
    }
    offset += length
    return arc
  })
})

const legendItems = computed(() => {
  const order = props.legendOrder ?? props.pieces.map((_, i) => i)
  return order.map((i) => ({
    legend: props.pieces[i].legend,
    color: props.pieces[i].color,
    count: Math.round(shown.value.counts[i] ?? 0),
    percentage: ((shown.value.ratios[i] ?? 0) * 100).toFixed(0),
  }))
})
</script>

<style scoped>
.doughnut-chart--theme {
  --d-chart--arc--thickness: var(--doughnut-chart--arc--thickness, 16);
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

.doughnut-arc {
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

.legend-text {
  font-size: 0.85rem;
  color: var(--d-chart--text--color);
}
</style>
