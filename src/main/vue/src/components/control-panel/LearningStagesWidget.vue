<template>
  <div
    class="stages-widget stages-widget--theme"
    :class="{
      'stages-widget--expanded': isHovering,
      'hex-grid': isHovering,
    }"
    @mouseenter="isHovering = true"
    @mouseleave="isHovering = false"
  >
    <div class="stages-title">
      Learning Stages
    </div>
    <div ref="gridRef" class="stage-grid">
      <div
        v-for="(stage, index) in mainStageArray"
        :key="stage.name"
        :ref="(el) => { if (el) stageElements[index] = el as HTMLElement }"
        class="stage-wrapper"
        :style="{
          transform: `translateY(${stageOffsets[index]}px)`,
        }"
      >
        <div
          class="stage select-none"
          :class="{
            'stage--current-day': isStageInCurrentDay(stage)
          }"
          :style="stageHeights[index] > 0 ? {
            height: `${stageHeights[index]}px`,
            maxHeight: `${stageHeights[index]}px`,
            aspectRatio: 'auto',
          } : {}"
        >
          <div class="stage-name">
            {{ stage.displayName }}
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
import { mainStageArray, Stage } from '@/core-logic/stage-logic.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const gridRef = ref<HTMLElement | null>(null)
const stageOffsets = ref<number[]>(Array(7).fill(0))
const stageHeights = ref<number[]>(Array(7).fill(0))
const stageElements = ref<HTMLElement[]>([])
const resizeObserver = ref<ResizeObserver | null>(null)
const isHovering = ref(false)
const originalStageHeight = ref<number>(0)

const createFlashcardCountComputed = (stage: Stage) => {
  return computed(() => {
    return countFlashcards(flashcards.value, stage, currDay.value)
  })
}

const isStageInCurrentDay = (stage: Stage) => {
  return currDay.value?.stages?.includes(stage.name) ?? false
}

const captureOriginalHeight = () => {
  if (!gridRef.value || stageElements.value.length === 0) return

  const wasHovering = isHovering.value
  if (wasHovering) {
    isHovering.value = false
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
  }

  nextTick(() => {
    const firstWrapper = stageElements.value[0]
    if (firstWrapper) {
      const stageEl = firstWrapper.querySelector('.stage') as HTMLElement
      if (stageEl) {
        originalStageHeight.value = stageEl.offsetHeight
      }
    }

    if (wasHovering) {
      isHovering.value = true
      calculateStageOffsets()
    }
  })
}

const calculateStageOffsets = () => {
  if (!gridRef.value || stageElements.value.length === 0) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
    return
  }

  if (!isHovering.value) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
    return
  }

  const gridHeight = gridRef.value.clientHeight
  const referenceHeight = originalStageHeight.value

  const flashcardCounts = mainStageArray.map(stage =>
    countFlashcards(flashcards.value, stage, currDay.value)
  )

  const maxCount = Math.max(...flashcardCounts)

  if (maxCount === 0) {
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
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

    // Calculate height: stages with flashcards expand to fill space to the bottom
    if (flashcardCounts[i] > 0) {
      // Height = original height plus space from current position to bottom
      const expandedHeight = referenceHeight + (gridHeight - referenceHeight - offset)
      heights.push(expandedHeight)
    } else {
      // Stages with 0 flashcards keep the original height
      heights.push(0)
    }
  }

  stageOffsets.value = offsets
  stageHeights.value = heights
}

watch(isHovering, () => {
  calculateStageOffsets()
})

const handleResize = () => {
  captureOriginalHeight()
  if (!isHovering.value) {
    calculateStageOffsets()
  }
}

onMounted(() => {
  nextTick(() => {
    captureOriginalHeight()
    calculateStageOffsets()
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
})

</script>

<style scoped>
.stages-widget--theme {
  --stages--widget--border-color: var(--stages-widget--border-color, none);
  --stages--widget--bg: var(--stages-widget--bg, linear-gradient(180deg,
  rgb(163, 175, 232) 0%,
  rgb(183, 191, 240) 30%,
  rgb(202, 206, 248) 60%,
  rgb(227, 229, 251) 85%,
  rgb(242, 242, 255) 100%
  ));
  --stages--title--color: var(--stages-title--color, rgba(13, 18, 74, 0.6));
  --stages--stage--border-color: var(--stages-stage--border-color, rgba(0, 255, 255, 0.6));
  --stages--stage--bg: var(--stages-stage--bg, linear-gradient(175deg, rgba(102, 102, 232, 0.65) 0%, rgba(11, 46, 117, 0.65) 100%));
  --stages--stage-name--color: var(--stages-stage-name--color, rgba(13, 18, 74, 0.6));
  --stages--stage-count--color: var(--stages-stage-count--color, rgba(13, 18, 74, 0.6));
  --stages--stage-count--bg: var(--stages-stage-count--bg, rgba(255, 255, 255, 0.6));
  --stages--current-day--glow-color: var(--stages-current-day--glow-color, rgba(255, 215, 0, 0.4));
}

.stages-widget {
  position: relative;
  padding: 1px 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border: 1px solid var(--stages--widget--border-color);
  background: var(--stages--widget--bg);
  border-radius: 6px;
  width: 100%;
  min-width: 360px;
  flex-grow: 0;
  transition: flex-grow 0.1s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              background 0.3s ease-in-out;
}

.stages-widget--expanded {
  flex-grow: 1;
}

.stages-widget--expanded.hex-grid {
  background:
    repeating-linear-gradient(0deg, transparent 0px, transparent 24px, rgba(0, 255, 255, 0.15) 24px, rgba(0, 255, 255, 0.15) 25px),
    repeating-linear-gradient(60deg, transparent 0px, transparent 24px, rgba(0, 255, 255, 0.15) 24px, rgba(0, 255, 255, 0.15) 25px),
    repeating-linear-gradient(120deg, transparent 0px, transparent 24px, rgba(0, 255, 255, 0.15) 24px, rgba(0, 255, 255, 0.15) 25px),
    linear-gradient(180deg, rgb(163, 175, 232) 0%, rgb(183, 191, 240) 30%, rgb(202, 206, 248) 60%, rgb(227, 229, 251) 85%, rgb(252, 252, 255) 100%);
}

.stages-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--stages--title--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  align-self: start;
  text-align: left;
  white-space: nowrap;
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
}

.stage-wrapper {
  width: 100%;
  height: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.6s cubic-bezier(0.34, 1.2, 0.64, 1);
  position: relative;
}

.stage {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--stages--stage--border-color);
  background: var(--stages--stage--bg);
  border-radius: 6px;
  justify-content: center;
  align-items: center;
  width: 80%;
  min-width: 50px;
  height: auto;
  min-height: clamp(56px, 6cqw, 80px);
  max-height: 80px;
  padding: 4px;
  gap: 4px;
  container-type: size;
  box-shadow: 0 0 6px var(--stages--stage--border-color),
              0 2px 6px rgba(0, 0, 0, 0.15),
              0 1px 3px rgba(0, 0, 0, 0.1);
  transition: height 0.3s ease-in-out,
              max-height 0.3s ease-in-out
 }

.stage-name {
  font-size: clamp(0.55rem, 24cqw, 0.9rem);
  font-weight: 600;
  color: var(--stages--stage-name--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: normal;
}

.stage-count-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: end;
  width: 100%;
}

.stage-count {
  font-size: clamp(0.6rem, 24cqw, 0.85rem);
  font-weight: 600;
  color: var(--stages--stage-count--color);
  background: var(--stages--stage-count--bg);
  border-radius: 3px;
  padding: 2px;
  width: 60%;
  min-width: 30px;
  max-width: 40px;
  text-align: center;
}
</style>
