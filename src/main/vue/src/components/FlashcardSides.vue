<template>
  <div
    class="flashcard-sides"
    :class="{
      'front-active': activeSide === 'front',
      'back-active': activeSide === 'back',
    }"
    @mousedown="lastMouseDownInside = true"
    @focusout="onFocusout"
  >
    <div
      class="side-slot side-slot--front"
      @focusin="activeSide = 'front'"
    >
      <FlashcardSideInput
        ref="frontRef"
        v-model:text="frontText"
        v-model:audio="frontAudio"
        placeholder="Front side"
        side="front"
      />
    </div>
    <div
      class="side-slot side-slot--back"
      @focusin="activeSide = 'back'"
    >
      <FlashcardSideInput
        ref="backRef"
        v-model:text="backText"
        v-model:audio="backAudio"
        placeholder="Back side"
        side="back"
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

let lastMouseDownInside = false

const invalid = computed(() =>
  (frontRef.value?.invalid ?? false) || (backRef.value?.invalid ?? false)
)

function validate() {
  frontRef.value?.validate()
  backRef.value?.validate()
}

function resetState() {
  frontRef.value?.resetState()
  backRef.value?.resetState()
}

function onFocusout(e: FocusEvent) {
  if (lastMouseDownInside) {
    lastMouseDownInside = false
    return
  }

  const area = e.currentTarget as HTMLElement
  const next = e.relatedTarget as Element | null
  if (area.contains(next)) return
  if (next?.closest('button, [role="button"]')) return
  activeSide.value = null
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
  transition: flex-grow 300ms ease-in-out;
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
