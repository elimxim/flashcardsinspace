<template>
  <div
    class="stages-widget stages-widget--theme"
    @mouseenter="isHovering = true"
    @mouseleave="isHovering = false"
  >
    <div class="stages-title">
      Learning Stages
    </div>
    <div class="stage-grid" ref="gridRef">
      <div
        v-for="(stage, index) in mainStageArray"
        :key="stage.name"
        class="stage-wrapper"
        :ref="(el) => { if (el) stageElements[index] = el as HTMLElement }"
        :style="{
          transform: `translateY(${stageOffsets[index]}px)`,
        }"
      >
        <div
          class="stage select-none"
          :style="stageHeights[index] > 0 ? {
            height: `${stageHeights[index]}px`,
            maxHeight: `${Math.max(stageHeights[index], 500)}px`,
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
const originalStageHeight = ref<number>(50) // Store the original stage height

const createFlashcardCountComputed = (stage: Stage) => {
  return computed(() => {
    return countFlashcards(flashcards.value, stage, currDay.value)
  })
}

const captureOriginalHeight = () => {
  if (!gridRef.value || stageElements.value.length === 0) return

  // Temporarily ensure we're not hovering to get clean measurements
  const wasHovering = isHovering.value
  if (wasHovering) {
    isHovering.value = false
    // Reset any applied styles
    stageOffsets.value = Array(7).fill(0)
    stageHeights.value = Array(7).fill(0)
  }

  // Wait for next tick to ensure styles are applied
  nextTick(() => {
    const firstWrapper = stageElements.value[0]
    if (firstWrapper) {
      const stageEl = firstWrapper.querySelector('.stage') as HTMLElement
      if (stageEl) {
        originalStageHeight.value = stageEl.offsetHeight
      }
    }

    // Restore hovering state if it was active
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

    // Calculate height: stages with flashcards expand to fill space to bottom
    if (flashcardCounts[i] > 0) {
      // Height = original height + space from current position to bottom
      const expandedHeight = referenceHeight + (gridHeight - referenceHeight - offset)
      heights.push(expandedHeight)
    } else {
      // Stages with 0 flashcards keep original height
      heights.push(0) // 0 means use auto height
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
  // calculateStageOffsets will be called automatically after captureOriginalHeight
  // if we were hovering, or we can call it explicitly
  if (!isHovering.value) {
    calculateStageOffsets()
  }
}

onMounted(() => {
  // Wait for next tick to ensure elements are rendered
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

}

.stages-widget {
  position: relative;
  padding: 1px 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border: 1px solid black;
  border-radius: 6px;
  width: 100%;
  min-width: 360px;
  height: 100%;
}

.stages-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: rgba(13, 18, 74, 0.6);
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
  border: 1px solid rgba(128, 128, 128, 0.62);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.66) 0%, rgba(118, 75, 162, 0.68) 100%);
  border-radius: 6px;
  justify-content: center;
  align-items: center;
  width: 70%;
  min-width: 46px;
  max-width: 106px;
  height: auto;
  min-height: 40px;
  max-height: 106px;
  aspect-ratio: 1 / 1;
  padding: 4px;
  gap: 4px;
  container-type: size;
  transition: height 0.6s cubic-bezier(0.34, 1.2, 0.64, 1),
              max-height 0.6s cubic-bezier(0.34, 1.2, 0.64, 1);
 }

.stage-name {
  font-size: clamp(0.55rem, 24cqw, 0.9rem);
  font-weight: 600;
  color: rgba(13, 18, 74, 0.6);
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
  align-items: center;
  width: 100%;
}

.stage-count {
  font-size: clamp(0.6rem, 24cqw, 0.85rem);
  font-weight: 600;
  color: rgba(20, 27, 106, 0.82);
  background: rgba(255, 255, 255, 0.6);
  border-radius: 3px;
  padding: 2px;
  width: 60%;
  min-width: 30px;
  max-width: 40px;
  text-align: center;
}
</style>
