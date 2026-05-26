<template>
  <div
    ref="containerRef"
    class="flashcard-sides"
    :class="{
      'front-active': activeSide === 'front',
      'back-active': activeSide === 'back',
    }"
  >
    <div class="side-slot side-slot--front">
      <FlashcardSideInput
        ref="frontRef"
        v-model:text="frontText"
        v-model:audio="frontAudio"
        v-model:expanded-down="frontExpandedDown"
        placeholder="Front side"
      />
    </div>
    <div class="side-slot side-slot--back">
      <FlashcardSideInput
        ref="backRef"
        v-model:text="backText"
        v-model:audio="backAudio"
        v-model:expanded-down="backExpandedDown"
        placeholder="Back side"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardSideInput from '@/components/FlashcardSideInput.vue'
import { ref, computed, watch } from 'vue'

const frontText = defineModel<string>('frontText', { required: true })
const frontAudio = defineModel<Blob | undefined>('frontAudio')
const backText = defineModel<string>('backText', { required: true })
const backAudio = defineModel<Blob | undefined>('backAudio')

const containerRef = ref<HTMLDivElement>()
const frontRef = ref<InstanceType<typeof FlashcardSideInput>>()
const backRef = ref<InstanceType<typeof FlashcardSideInput>>()

const frontExpandedDown = ref<boolean>(false)
const backExpandedDown = ref<boolean>(false)

const activeSide = ref<'front' | 'back' | null>(null)

watch([frontExpandedDown, backExpandedDown], ([isFrontExpanded, isBackExpanded]) => {
  switch (activeSide.value) {
    case null:
      activeSide.value = isFrontExpanded ? 'front' : isBackExpanded ? 'back' : null
      triggerSide(activeSide.value)
      break
    case 'front':
      if (!isFrontExpanded && !isBackExpanded) {
        activeSide.value = null
        containerRef.value?.scrollIntoView()
        frontExpandedDown.value = false
      } else if (isBackExpanded) {
        activeSide.value = 'back'
        triggerSide(activeSide.value)
        frontExpandedDown.value = false
      }
      break
    case 'back':
      if (!isBackExpanded && !isFrontExpanded) {
        activeSide.value = null
        containerRef.value?.scrollIntoView()
        backExpandedDown.value = false
      } else if (isFrontExpanded) {
        activeSide.value = 'front'
        triggerSide(activeSide.value)
        backExpandedDown.value = false
      }
      break
  }
})

function triggerSide(side: 'front' | 'back' | null) {
  const ref = side === 'front' ? frontRef?.value : side === 'back' ? backRef?.value : undefined
  if (ref) setTimeout(() => ref.scrollIntoView(), EXPANSION_TRANSITION_MS)
}

const invalid = computed(() =>
  (frontRef.value?.invalid ?? false) || (backRef.value?.invalid ?? false)
)

const EXPANSION_TRANSITION_MS = 300
const expansionTransition = `${EXPANSION_TRANSITION_MS}ms`

function validate() {
  frontRef.value?.validate()
  backRef.value?.validate()
}

function resetState() {
  frontRef.value?.resetState()
  backRef.value?.resetState()
}

defineExpose({ validate, resetState, invalid })
</script>

<style scoped>
.flashcard-sides {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  min-height: 0;
}

.side-slot {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  transition: flex-grow v-bind(expansionTransition) ease-in-out;
  min-height: 62px;
  overflow: hidden;
}

.front-active .side-slot--front {
  flex-grow: 1;
}

.front-active .side-slot--back {
  flex-grow: 0;
}

.back-active .side-slot--front {
  flex-grow: 0;
}

.back-active .side-slot--back {
  flex-grow: 1;
}
</style>
