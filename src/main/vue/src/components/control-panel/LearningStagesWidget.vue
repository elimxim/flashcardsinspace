<template>
  <div
    class="stages-widget stages-widget--theme"
    :class="{
      'stages-widget--hex-grid': isExpanded,
    }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @click="handleClick"
  >
    <div class="stages-title">
      Learning Stages
    </div>
    <div ref="gridRef" class="stage-grid">
      <div
        v-for="(stage, index) in learningStageArray"
        :key="stage.name"
        class="stage-wrapper select-none"
      >
        <div
          :ref="(el) => { if (el) stageElements[index] = el as HTMLElement }"
          class="stage"
          :class="{
            'stage--current-day': isStageInCurrentDay(stage)
          }"
          :style="{
            transform: `translateY(${stageOffsets[index]}px)`,
            height: `${stageHeights[index]}px`
          }"
        >
          <div class="stage-name">
            {{ isNarrowGrid ? stage.order : stage.displayName }}
          </div>
          <div class="stage-count-wrapper">
            <div class="stage-count">
              {{ createFlashcardCountComputed(stage) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { learningStageArray, Stage } from '@/core-logic/stage-logic.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { isHoverSupported } from '@/utils/utils.ts'

const props = withDefaults(defineProps<{
  growMultiplier?: number
}>(), {
  growMultiplier: 2,
})

const NAME_SHORT_GRID_WIDTH_THRESHOLD = 600

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const gridRef = ref<HTMLElement>()
const stageOffsets = ref<number[]>(Array(7).fill(0))
const stageHeights = ref<number[]>(Array(7).fill(0))
const stageElements = ref<HTMLElement[]>([])
const resizeObserver = ref<ResizeObserver>()
const isExpanded = ref(false)
const isNarrowGrid = ref(false)
const hoverDelayTimeout = ref<number>()
const originalStageHeight = ref<number>(0)
const originalGridHeight = ref<number>(0)
const captureHeightTimeout = ref<number>()
const isTransitioning = ref(false)
const transitionTimeout = ref<number>()
const transitionDurationMs = 300
const transitionDuration = `${transitionDurationMs / 1000.0}s`
const transitionDelayMs = transitionDurationMs + 50
const stageWidthPercentage = ref(80)
const stageWidthPercentageCss = computed(() => `${stageWidthPercentage.value}%`)

const createFlashcardCountComputed = (stage: Stage) => {
  return computed(() => {
    return countFlashcards(flashcards.value, stage, currDay.value)
  })
}

const isStageInCurrentDay = (stage: Stage) => {
  return currDay.value?.stages?.includes(stage.name) ?? false
}

function captureOriginalHeights(force: boolean = false) {
  if (!gridRef.value || stageElements.value.length === 0) return
  if ((isExpanded.value || isTransitioning.value) && !force) return

  if (captureHeightTimeout.value) {
    clearTimeout(captureHeightTimeout.value)
    captureHeightTimeout.value = undefined
  }

  captureHeightTimeout.value = window.setTimeout(() => {
    if ((!isExpanded.value && !isTransitioning.value) || force) {
      const stageEl = stageElements.value[0]
      if (stageEl) {
        originalStageHeight.value = stageEl.offsetHeight
      }

      if (gridRef.value) {
        originalGridHeight.value = gridRef.value.clientHeight
      }
    }

    captureHeightTimeout.value = undefined
  }, transitionDelayMs)
}

const calculateStageOffsets = () => {
  if (!gridRef.value || stageElements.value.length === 0) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
    return
  }

  const gridHeight = originalGridHeight.value * props.growMultiplier
  const referenceHeight = originalStageHeight.value

  if (!isExpanded.value) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(referenceHeight)
    return
  }

  const flashcardCounts = learningStageArray.map(stage =>
    countFlashcards(flashcards.value, stage, currDay.value)
  )

  const maxCount = Math.max(...flashcardCounts)

  if (maxCount === 0) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(referenceHeight)
    return
  }

  // Calculate normalized factors based on flashcard counts
  // Stage with max count gets factor 1, others get proportional factors
  const factors = flashcardCounts.map(count => count / maxCount)

  const offsets: number[] = []
  const heights: number[] = []

  for (let i = 0; i < 7; i++) {
    const countFactor = factors[i]
    const maxY = gridHeight - referenceHeight
    const offset = maxY - countFactor * maxY

    offsets.push(offset)

    if (flashcardCounts[i] > 0) {
      const expandedHeight = referenceHeight + (gridHeight - referenceHeight - offset)
      heights.push(expandedHeight)
    } else {
      heights.push(referenceHeight)
    }
  }

  stageOffsets.value = offsets
  stageHeights.value = heights
}

watch(isExpanded, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    isTransitioning.value = true

    // Clear any existing transition timeout
    if (transitionTimeout.value) {
      clearTimeout(transitionTimeout.value)
    }

    // Set a timeout to clear transitioning flag after animation completes
    transitionTimeout.value = window.setTimeout(() => {
      isTransitioning.value = false
      transitionTimeout.value = undefined
    }, transitionDelayMs)
  }
  calculateStageOffsets()
})

function handleMouseEnter() {
  if (!isHoverSupported) return
  hoverDelayTimeout.value = window.setTimeout(() => {
    isExpanded.value = true
  }, 150)
}

function handleMouseLeave() {
  if (!isHoverSupported) return
  if (hoverDelayTimeout.value) {
    clearTimeout(hoverDelayTimeout.value)
    hoverDelayTimeout.value = undefined
  }
  isExpanded.value = false
}

// fallback for touch devices
function handleClick() {
  if (isHoverSupported) return
  isExpanded.value = !isExpanded.value
}

const handleResize = () => {
  if (gridRef.value) {
    isNarrowGrid.value = gridRef.value.clientWidth < NAME_SHORT_GRID_WIDTH_THRESHOLD
  }
  if (!isExpanded.value && !isTransitioning.value) {
    captureOriginalHeights()
  }
}

watch(isNarrowGrid, (newVal) => {
  if (newVal) {
    stageWidthPercentage.value = 80
  } else {
    stageWidthPercentage.value = 100
  }
})

onMounted(() => {
  nextTick().then(() => {
    captureOriginalHeights(true)
    if (gridRef.value) {
      isNarrowGrid.value = gridRef.value.clientWidth < NAME_SHORT_GRID_WIDTH_THRESHOLD
    }
  })

  window.addEventListener('resize', handleResize)

  if (gridRef.value) {
    resizeObserver.value = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.value.observe(gridRef.value)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)

  if (resizeObserver.value) {
    resizeObserver.value.disconnect()
  }

  if (captureHeightTimeout.value) {
    clearTimeout(captureHeightTimeout.value)
    captureHeightTimeout.value = undefined
  }

  if (transitionTimeout.value) {
    clearTimeout(transitionTimeout.value)
    transitionTimeout.value = undefined
  }

  if (hoverDelayTimeout.value) {
    clearTimeout(hoverDelayTimeout.value)
    hoverDelayTimeout.value = undefined
  }

  isExpanded.value = false
  isTransitioning.value = false
})

</script>

<style scoped>
.stages-widget--theme {
  --l-widget--border-color: var(--stages-widget--border-color, none);
  --l-widget--bg: var(--stages-widget--bg, linear-gradient(135deg, rgb(154, 170, 241) 0%, rgb(162, 133, 192) 100%));
  --l-widget--hex-line--color: var(--stages-widget--hex-line--color, rgba(0, 255, 255, 0.05));
  --l-widget--title--color: var(--stages-widget--title--color, rgba(13, 18, 74, 0.6));
  --l-widget--stage--bg: var(--stages-widget--stage--bg, rgba(0, 0, 0, 0.22));
  --l-widget--stage--name--color: var(--stages-widget--stage--name--color, rgba(250, 249, 246, 0.7));
  --l-widget--stage--count--color: var(--stages-widget--stage--count--color, rgba(13, 18, 74, 0.6));
  --l-widget--stage--count--bg: var(--stages-widget--stage--count--bg, rgba(255, 255, 255, 0.6));
}

.stages-widget {
  position: relative;
  padding: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border: 1px solid var(--l-widget--border-color);
  background: var(--l-widget--bg);
  border-radius: 6px;
  width: 100%;
  flex-grow: 0;
  transition: background 0.3s ease-in-out;
}

.stages-widget--hex-grid {
  background: repeating-linear-gradient(0deg, transparent 0px, transparent 24px, var(--l-widget--hex-line--color) 24px, var(--l-widget--hex-line--color) 25px),
  repeating-linear-gradient(60deg, transparent 0px, transparent 24px, var(--l-widget--hex-line--color) 24px, var(--l-widget--hex-line--color) 25px),
  repeating-linear-gradient(120deg, transparent 0px, transparent 24px, var(--l-widget--hex-line--color) 24px, var(--l-widget--hex-line--color) 25px),
  var(--l-widget--bg);
}

.stages-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--l-widget--title--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  align-self: start;
  text-align: left;
  white-space: nowrap;
  padding: 2px 10px;
}

.stage-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  width: 100%;
  position: relative;
  align-items: start;
  margin: 10px;
  container-type: inline-size;
  container-name: stage-grid;
}

.stage-wrapper {
  width: 100%;
  height: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.stage {
  display: flex;
  flex-direction: column;
  background: var(--l-widget--stage--bg);
  border-radius: 6px;
  justify-content: center;
  align-items: center;
  width: v-bind(stageWidthPercentageCss);
  min-width: 20px;
  height: auto;
  min-height: clamp(52px, 8cqw, 80px);
  padding: clamp(2px, 20cqi, 4px);
  gap: 4px;
  container-type: size;
  transition: transform v-bind(transitionDuration) ease-in-out, height v-bind(transitionDuration) ease-in-out;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2), inset 0 -2px 2px rgba(255, 255, 255, 0.1);
}

.stage-name {
  font-size: clamp(0.7rem, 20cqi, 0.9rem);
  font-weight: 600;
  color: var(--l-widget--stage--name--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: normal;
  overflow-wrap: break-word;
  word-break: break-word;
}

.stage-count-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: end;
  width: 100%;
}

.stage-count {
  font-size: clamp(0.6rem, 20cqi, 0.85rem);
  font-weight: 600;
  color: var(--l-widget--stage--count--color);
  background: var(--l-widget--stage--count--bg);
  border-radius: 3px;
  padding: 2px;
  width: 60%;
  min-width: 26px;
  max-width: 40px;
  text-align: center;
}
</style>
