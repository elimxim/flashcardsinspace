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
        :style="{ transform: `translateY(${stageOffsets[index]}px)` }"
      >
        <div class="stage select-none">
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
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const gridRef = ref<HTMLElement | null>(null)
const stageOffsets = ref<number[]>(Array(7).fill(0))
const stageElements = ref<HTMLElement[]>([])
const resizeObserver = ref<ResizeObserver | null>(null)
const isHovering = ref(false)

const createFlashcardCountComputed = (stage: Stage) => {
  return computed(() => {
    return countFlashcards(flashcards.value, stage, currDay.value)
  })
}

const calculateStageOffsets = () => {
  if (!gridRef.value || stageElements.value.length === 0 || !isHovering.value) {
    stageOffsets.value = Array(7).fill(0)
    return
  }

  const gridHeight = gridRef.value.clientHeight

  const stageSizes: number[] = []
  for (let i = 0; i < 7; i++) {
    const el = stageElements.value[i]
    if (el) {
      stageSizes.push(el.offsetHeight)
    } else {
      stageSizes.push(0)
    }
  }

  // Cubic growth function: f(i) = (i/6)^3
  // Maps 0-6 to 0-1 with cubic growth
  const cubicFactor = (index: number) => {
    const normalized = index / 6
    return normalized * normalized * normalized
  }

  // Calculate cubic factors for all stages
  const factors = Array.from({ length: 7 }, (_, i) => cubicFactor(i))

  const offsets: number[] = []

  for (let i = 0; i < 7; i++) {
    const stageHeight = stageSizes[i] || 50
    const halfStageHeight = stageHeight / 2

    // Position based on cubic distribution
    const cubicPosition = factors[i] // 0 to 1
    const maxY = gridHeight / 2 - halfStageHeight // bottom
    const minY = -gridHeight / 2 + halfStageHeight // top
    const offset = maxY - cubicPosition * (maxY - minY)

    offsets.push(offset)
  }

  stageOffsets.value = offsets
}

watch(isHovering, () => {
  calculateStageOffsets()
})

onMounted(() => {
  calculateStageOffsets()

  window.addEventListener('resize', calculateStageOffsets)

  if (gridRef.value) {
    resizeObserver.value = new ResizeObserver(() => {
      calculateStageOffsets()
    })
    resizeObserver.value.observe(gridRef.value)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateStageOffsets)

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
  color: black;
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
  align-items: center;
  margin: 10px;
}

.stage-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.6s cubic-bezier(0.34, 1.4, 0.64, 1);
}

.stage {
  display: flex;
  flex-direction: column;
  border: 1px solid black;
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
}

.stage-name {
  font-size: clamp(0.55rem, 24cqw, 0.9rem);
  font-weight: 500;
  color: black;
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
  color: black;
  border: 1px solid black;
  border-radius: 3px;
  background: none;
  padding: 2px;
  width: 60%;
  min-width: 30px;
  max-width: 40px;
  text-align: center;
}
</style>
