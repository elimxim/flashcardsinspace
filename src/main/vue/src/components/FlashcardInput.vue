<template>
  <div
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
        placeholder="Front side"
        @input-click="toggleSide('front')"
      />
    </div>
    <div class="side-slot side-slot--back">
      <FlashcardSideInput
        ref="backRef"
        v-model:text="backText"
        v-model:audio="backAudio"
        placeholder="Back side"
        @input-click="toggleSide('back')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardSideInput from '@/components/FlashcardSideInput.vue'
import { ref, computed } from 'vue'

const frontText = defineModel<string>('frontText', { required: true })
const frontAudio = defineModel<Blob | undefined>('frontAudio')
const backText = defineModel<string>('backText', { required: true })
const backAudio = defineModel<Blob | undefined>('backAudio')

const activeSide = ref<'front' | 'back' | null>(null)

const frontRef = ref<InstanceType<typeof FlashcardSideInput>>()
const backRef = ref<InstanceType<typeof FlashcardSideInput>>()

const invalid = computed(() =>
  (frontRef.value?.invalid ?? false) || (backRef.value?.invalid ?? false)
)

const EXPANSION_TRANSITION_MS = 300
const expansionTransition = `${EXPANSION_TRANSITION_MS}ms`

function toggleSide(side: 'front' | 'back') {
  activeSide.value = activeSide.value === side ? null : side
  if (activeSide.value === side) {
    const ref = side === 'front' ? frontRef : backRef
    setTimeout(() => ref.value?.scrollIntoView(), EXPANSION_TRANSITION_MS)
  }
}

function validate() {
  frontRef.value?.validate()
  backRef.value?.validate()
}

function resetState() {
  activeSide.value = null
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
  min-height: 62px;
}

.side-slot {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  transition: flex-grow v-bind(expansionTransition) ease-in-out;
  min-height: fit-content;
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
