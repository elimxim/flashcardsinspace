<template>
  <div class="space-deck">
    <div v-if="flashcard" class="flashcard-deck">
      <transition :name="cardTransition">
        <SpaceCard
          v-if="deckReady"
          ref="spaceCard"
          :key="flashcard ? flashcard.id : 0"
          :stage="flashcard?.stage"
          :front-side="flashcard?.frontSide"
          :back-side="flashcard?.backSide"
          :viewed-times="viewedTimes"
          :on-edit="() => flashcardWasEdited = true"
        />
      </transition>
    </div>
    <div v-else class="flashcard-deck">
      <SpaceCard
        front-side="No more flashcards for review"
        unflippable
        text-only
        transparent
      />
    </div>
  </div>
  <FlashcardModificationModal
    v-model:flashcard="flashcard"
    v-model:removed="flashcardWasRemoved"
    edit-mode
  />
</template>

<script setup lang="ts">
import SpaceCard from '@/components/SpaceCard.vue'
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useModalStore } from '@/stores/modal-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'

const flashcard = defineModel<Flashcard | null>('flashcard', { default: null })

const props = withDefaults(defineProps<{
  onFlashcardRemoved?: () => void
  onFlashcardEdited?: () => void
}>(), {
  flashcard: null,
  onFlashcardRemoved: () => {
  },
  onFlashcardEdited: () => {
  },
})

const modalStore = useModalStore()

const deckReady = ref(false)
const flashcardWasRemoved = ref(false)
const flashcardWasEdited = ref(false)
const spaceCard = ref<InstanceType<typeof SpaceCard>>()
const cardTransition = ref('slide-next')

const viewedTimes = computed(() => (flashcard.value?.reviewCount ?? 0) + 1)

function setDeckReady() {
  console.log(`FlashcardDeck.ready`)
  deckReady.value = true
  if (deckReady.value) {
    cardTransition.value = 'drop-down'
  }
}

async function preparePrev() {
  await spaceCard.value?.flipToFrontAndWait()
  if (!deckReady.value || !flashcard.value) {
    cardTransition.value = ''
  } else {
    cardTransition.value = 'slide-prev'
  }
}

async function prepareNext() {
  await spaceCard.value?.flipToFrontAndWait()
  if (!deckReady.value || !flashcard.value) {
    cardTransition.value = ''
  } else {
    cardTransition.value = 'slide-next'
  }
}

defineExpose({
  setDeckReady,
  preparePrev,
  prepareNext,
})

watch(flashcardWasRemoved, (newVal) => {
  if (newVal) {
    spaceCard.value?.flipToFront()
    props.onFlashcardRemoved()
    flashcardWasRemoved.value = false
  }
})

watch(flashcardWasEdited, (newVal) => {
  if (newVal) {
    modalStore.toggleFlashcardEdit()
    props.onFlashcardEdited()
    flashcardWasEdited.value = false
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (modalStore.isAnyModalOpen()) return
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
  height: clamp(250px, 50vh, 450px);
  background-color: transparent;
}

.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition: all 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
}

.slide-next-leave-active,
.slide-prev-leave-active {
  z-index: 1;
}

.slide-next-enter-from,
.slide-prev-enter-from {
  transform: scale(0.90);
}

.slide-next-leave-to {
  transform: translateX(200%) rotate(10deg);
}

.slide-prev-leave-to {
  transform: translateX(-200%) rotate(-10deg);
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
