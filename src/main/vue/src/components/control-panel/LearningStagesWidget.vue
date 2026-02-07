<template>
  <div
    class="stages-widget stages-widget--theme"
    :class="{ 'stages-widget--expanded': isExpanded }"
    :style="{
      '--stage-height-base': `${baseHeight}px`,
      '--stage-height-expanded': `${baseHeight * props.growMultiplier}px`
    }"
    @click="toggleExpand"
  >
    <div class="stages-title">
      Learning Stages
    </div>
    <div ref="gridRef" class="stage-grid">
      <div
        v-for="stageInfo in stageInfoData"
        :key="stageInfo.name"
        class="stage-col"
        :style="{
          '--stage-ratio': stageInfo.ratio,
        }"
      >
        <div class="stage-inner">
          <div class="stage-name">
            {{ isNarrowGrid ? stageInfo.order : stageInfo.displayName }}
          </div>
          <div class="stage-count-wrapper">
            <div class="stage-count">
              {{ stageInfo.flashcardCount }}
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
import { computed, ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  growMultiplier?: number
}>(), {
  growMultiplier: 2,
})

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const gridRef = ref<HTMLElement>()
const isExpanded = ref(false)
const isNarrowGrid = ref(false)
const resizeObserver = ref<ResizeObserver>()
const baseHeight = ref(80)

const NAME_SHORT_GRID_WIDTH_THRESHOLD = 520

interface StageInfo extends Stage {
   flashcardCount: number
  ratio?: number
}

const stageInfoData = computed<StageInfo[]>(() => {
  const infoItems: StageInfo[] = learningStageArray.map(stage => ({
    ...stage,
    flashcardCount: countFlashcards(flashcards.value, stage, currDay.value)
  }))

  const maxCount = Math.max(...infoItems.map(info => info.flashcardCount))

  return infoItems.map(info => {
    const ratio = maxCount === 0 ? 0 : (info.flashcardCount / maxCount)
    return { ...info, ratio } as StageInfo
  })
})

const handleResize = (entries: ResizeObserverEntry[]) => {
  const entry = entries[0]
  if (entry) {
    const width = entry.contentRect.width
    isNarrowGrid.value = width < NAME_SHORT_GRID_WIDTH_THRESHOLD
    if (width < NAME_SHORT_GRID_WIDTH_THRESHOLD) {
      baseHeight.value = 60
    } else {
      baseHeight.value = 80
    }
  }
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

defineExpose({
  expand: () => isExpanded.value = true
})

onMounted(() => {
  if (gridRef.value) {
    resizeObserver.value = new ResizeObserver(handleResize)
    resizeObserver.value.observe(gridRef.value)
  }
})

onUnmounted(() => {
  resizeObserver.value?.disconnect()
})
</script>

<style scoped>
.stages-widget--theme {
  --growth-space: calc(var(--stage-height-expanded) - var(--stage-height-base));
  --animation-duration: 0.4s;
  --animation-ease: cubic-bezier(0.25, 0.8, 0.25, 1);
}

.stages-widget {
  position: relative;
  padding: 6px;
  display: flex;
  flex-direction: column;
  container-type: inline-size;
  border: 1px solid var(--cp--border-color);
  background: var(--cp--widget--color);
  border-radius: 6px;
  gap: 10px;
  width: 100%;
  transition: background var(--animation-duration) var(--animation-ease);
}

.stage-grid {
  height: var(--stage-height-base);
  transition: height var(--animation-duration) var(--animation-ease);
  transition-delay: 0s;
}

.stage-col {
  height: var(--stage-height-base);
  transition: height var(--animation-duration) var(--animation-ease);
  transition-delay: 0s;
}

@media (hover: hover) {
  .stages-widget:hover .stage-grid {
    height: var(--stage-height-expanded);
    transition-delay: 0.5s;
  }

  .stages-widget:hover .stage-col {
    height: calc(var(--stage-height-base) + (var(--growth-space) * var(--stage-ratio)));
    transition-delay: 0.5s;
  }
}

.stages-widget--expanded .stage-grid {
  height: var(--stage-height-expanded);
  transition-delay: 0s;
}

.stages-widget--expanded .stage-col {
  height: calc(var(--stage-height-base) + (var(--growth-space) * var(--stage-ratio)));
  transition-delay: 0s;
}

.stages-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--cp--text--color);
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  padding: 2px 4px;
}

.stage-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  align-items: end;
  gap: clamp(1px, 2cqi, 24px);
  width: 100%;
  will-change: height;
}

.stage-col {
  width: 100%;
  display: flex;
  justify-content: center;
  will-change: height;
}

.stage-inner {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.22);
  border-radius: 6px;
  width: 100%;
  height: 100%;
  min-height: 48px;
  max-width: 132px;
  padding: 6px;
  gap: 2px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2), inset 0 -2px 2px rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.stage-name {
  font-size: clamp(0.7rem, 20cqi, 0.9rem);
  font-weight: 600;
  color: rgba(250, 249, 246, 0.7);
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  word-break: break-word;
}

.stage-count-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  flex: 1;
}

.stage-count {
  font-size: clamp(0.6rem, 20cqi, 0.85rem);
  font-weight: 600;
  color: var(--cp--count-box--color);
  background: var(--cp--count-box--bg);
  border-radius: 3px;
  padding: 2px;
  width: 70%;
  min-width: 26px;
  max-width: 40px;
  text-align: center;
}
</style>
