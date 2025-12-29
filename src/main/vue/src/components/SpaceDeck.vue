<template>
  <div
    ref="spaceDeckElement"
    class="space-deck"
  >
    <div class="flashcard-deck">
      <transition
        :name="cardTransition"
        @after-enter="onCardTransitionComplete"
      >
        <SpaceCard
          v-if="flashcard && deckReady"
          ref="spaceCard"
          :key="flashcard ? flashcard.id : 0"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :stage="flashcard?.stage"
          :front-side="flashcard?.frontSide"
          :back-side="flashcard?.backSide"
          :viewed-times="viewedTimes"
          :on-edit="toggleStore.toggleFlashcardEdit"
          :front-side-audio="flashcardFrontSideAudio"
          :back-side-audio="flashcardBackSideAudio"
          :style="swipeStyle"
        />
        <template v-else>
          <slot v-if="hasSlot && showSlot && deckReady"/>
          <SpaceCard
            v-else
            :front-side="deckEmptyMessage()"
            unflippable
            text-only
            transparent
          />
        </template>
      </transition>
    </div>
  </div>
  <FlashcardEditModal
    v-model:flashcard="flashcard"
    v-model:removed="flashcardWasRemoved"
    v-model:audio-changed="audioWasChanged"
  />
</template>

<script setup lang="ts">
import SpaceCard from '@/components/SpaceCard.vue'
import FlashcardEditModal from '@/modals/FlashcardEditModal.vue'
import { computed, onMounted, onUnmounted, ref, useSlots, watch } from 'vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'
import { useSwipe } from '@/utils/use-swipe.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const autoPlayVoice = defineModel<boolean>('autoPlayVoice', { default: false })
const autoRepeatVoice = defineModel<boolean>('autoRepeatVoice', { default: false })

const props = withDefaults(defineProps<{
  showSlot?: boolean
  flashcardFrontSideAudio?: Blob | undefined
  flashcardBackSideAudio?: Blob | undefined
  swipeEnabled?: boolean
  canSwipeLeft?: boolean
  canSwipeRight?: boolean
  onFlashcardRemoved?: () => void
  onAudioChanged?: () => void
  onSwipeLeft?: () => void
  onSwipeRight?: () => void
}>(), {
  showSlot: true,
  flashcardFrontSideAudio: undefined,
  flashcardBackSideAudio: undefined,
  swipeEnabled: false,
  canSwipeLeft: true,
  canSwipeRight: true,
  onFlashcardRemoved: () => {
  },
  onAudioChanged: () => {
  },
  onSwipeLeft: () => {
  },
  onSwipeRight: () => {
  },
})

const slots = useSlots()
const toggleStore = useToggleStore()

const deckReady = ref(false)
const flashcardWasRemoved = ref(false)
const audioWasChanged = ref(false)
const spaceCard = ref<InstanceType<typeof SpaceCard>>()
const spaceDeckElement = ref<HTMLElement>()
const cardTransition = ref('')

const hasSlot = computed(() => !!slots.default)
const viewedTimes = computed(() => (flashcard.value?.timesReviewed ?? 0) + 1)

const { swipeStyle } = useSwipe({
  element: spaceDeckElement,
  enabled: () => props.swipeEnabled,
  canSwipeLeft: () => props.canSwipeLeft,
  canSwipeRight: () => props.canSwipeRight,
  onSwipeLeft: () => props.onSwipeLeft(),
  onSwipeRight: () => props.onSwipeRight(),
})

function setDeckReady() {
  deckReady.value = true
  cardTransition.value = 'drop-down'
}

function willSlideToLeft() {
  prepareSlideTransition('slide-to-left')
}

function willSlideToRight() {
  prepareSlideTransition('slide-to-right')
}

function prepareSlideTransition(value: string) {
  if (!deckReady.value) {
    cardTransition.value = ''
    return
  }

  if (!flashcard.value) {
    cardTransition.value = 'slide-from-right'
  } else {
    cardTransition.value = value
  }
}

function onCardTransitionComplete() {
  spaceCard.value?.onCardAnimationComplete()
}

defineExpose({
  setDeckReady,
  willSlideToLeft,
  willSlideToRight,
})

watch(flashcardWasRemoved, (newVal) => {
  if (newVal) {
    spaceCard.value?.flipToFront()
    props.onFlashcardRemoved()
    flashcardWasRemoved.value = false
  }
})

watch(audioWasChanged, (newVal) => {
  if (newVal) {
    props.onAudioChanged()
    audioWasChanged.value = false
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (toggleStore.isAnyModalOpen()) return
  if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    event.stopPropagation()
    spaceCard.value?.flip()
  }
}

</script>

<style scoped>
.space-deck {
  display: grid;
  place-items: center;
}

.flashcard-deck {
  grid-area: 1 / 1;
  position: relative;
  width: clamp(200px, 90vw, 600px);
  height: clamp(290px, 50vh, 450px);
}

.slide-to-right-enter-active,
.slide-to-right-leave-active,
.slide-to-left-enter-active,
.slide-to-left-leave-active {
  transition: all 0.4s cubic-bezier(0.33, 0, 0.2, 1);
  position: absolute;
}

.slide-from-right-enter-active,
.slide-from-right-leave-active {
  transition: all 0.2s cubic-bezier(0.33, 0, 0.2, 1);
  position: absolute;
}

.slide-from-right-leave-active,
.slide-to-right-leave-active,
.slide-to-left-leave-active {
  z-index: 1;
}

.slide-to-right-enter-from,
.slide-to-left-enter-from {
  transform: scale(0.90);
}

.slide-to-right-leave-to {
  transform: translateX(200%) rotate(10deg);
}

.slide-to-left-leave-to {
  transform: translateX(-200%) rotate(-10deg);
}

.slide-from-right-enter-from {
  transform: translateX(200%) rotate(10deg);
}

.slide-from-right-leave-to {
  opacity: 0;
  transform: none;
}

.drop-down-enter-active {
  animation: drop-and-bounce 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: absolute;
}

@keyframes drop-and-bounce {
  0% {
    transform: scale(1.5);
    opacity: 0;
  }
  70% {
    transform: scale(1);
    opacity: 1;
  }
  80% {
    transform: scale(1.01);
  }
  90% {
    transform: scale(1);
  }
  100% {
    transform: scale(0.99);
  }
}

</style>
