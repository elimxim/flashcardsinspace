<template>
  <transition name="fade-slide">
    <div v-if="!hidden" class="flashcard-info-bar flashcard-info-bar--theme">
      <div class="language-info">
        <AwesomeContainer icon="fa-solid fa-globe">
          <div class="language-info-text">
            {{ language?.name }}
          </div>
        </AwesomeContainer>
      </div>
      <div class="flashcards-info">
        <div class="cp-count-box cp-count-box--big">
          {{ flashcardCount }}
        </div>
        <div class="flashcards-info-text">
          Flashcards
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'

withDefaults(defineProps<{
  hidden?: boolean
}>(), {
  hidden: false,
})

const flashcardStore = useFlashcardStore()
const { flashcards, language } = storeToRefs(flashcardStore)

const flashcardCount = computed(() => flashcards.value.length)
</script>

<style scoped>
.flashcard-info-bar {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  gap: 40px;
  height: fit-content;
  max-height: max-content;
}

.language-info {
  display: flex;
  --awesome-container--icon--size: 1.2rem;
  --awesome-container--icon--color: var(--fa-icon--color--globe);
  --awesome-container--gap: 8px;
}

.language-info-text {
  font-size: 1rem;
  color: rgba(43, 69, 142, 0.88);
  white-space: nowrap;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
}

.flashcards-info {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
}

.flashcards-info-text {
  font-size: 0.85rem;
  font-weight: 500;
  color: rgba(43, 69, 142, 0.88);
  word-spacing: 0.02rem;
  letter-spacing: 0.02rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: max-height 0.3s ease-out, opacity 0.3s ease-out;
}

.fade-slide-enter-from {
  max-height: 0;
  opacity: 0;
}

.fade-slide-enter-to {
  max-height: 100px;
  opacity: 0;
}

.fade-slide-leave-from {
  max-height: 100px;
  opacity: 1;
}

.fade-slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
