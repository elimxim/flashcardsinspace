<template>
  <div class="flashcard-set-list">
    <div
      v-for="set in flashcardSets"
      :key="set.id"
      class="flashcard-set"
      :class="{
        'flashcard-set--active': set.id === flashcardSet?.id,
        'flashcard-set--loading': set.id === flashcardSet?.id && resolvedLoading,
      }"
      @click="selectFlashcardSet(set.id)"
    >
      <div class="flashcard-set-content">
        <div class="flashcard-set-name">{{ set.name }}</div>
        <AwesomeContainer class="flashcard-set-language-container" icon="fa-solid fa-globe">
          <div class="flashcard-set-language">
            {{ getLanguageName(set) }}
          </div>
        </AwesomeContainer>
      </div>
      <div v-if="!extraLoaded" class="cp-count-box cp-count-box--pulsing">
        #
      </div>
      <div v-else class="cp-count-box">
        {{ getFlashcardsNumber(set) }}
      </div>
      <div v-if="set.id === flashcardSet?.id && resolvedLoading" class="flashcard-set-spinner-container">
        <KineticRingSpinner :ring-size="42" :track-size="6" :track-color="'rgba(241,245,249,0.2)'"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import KineticRingSpinner from '@/components/KineticRingSpinner.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { saveSelectedSetIdToCookies } from '@/utils/cookies.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import {
  loadFlashcardSetExtras,
  loadStoresForFlashcardSet,
  waitUntilStoreLoaded,
} from '@/utils/store-loading.ts'
import { onMounted } from 'vue'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'

const languageStore = useLanguageStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()

await waitUntilStoreLoaded(languageStore)
await waitUntilStoreLoaded(flashcardSetStore)

const {
  resolvedLoading,
  startLoading,
  stopLoading
} = useDeferredLoading()

const { flashcardSet } = storeToRefs(flashcardStore)
const { flashcardSets, extraLoaded } = storeToRefs(flashcardSetStore)

async function selectFlashcardSet(setId: number) {
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    startLoading()
    await loadStoresForFlashcardSet(set)
      .then((loaded) => {
        if (loaded) {
          saveSelectedSetIdToCookies(set.id)
        }
      })
    await stopLoading()
  }
}

function getLanguageName(set: FlashcardSet): string {
  const language = languageStore.getLanguage(set.languageId)
  return language ? language.name : 'Unknown'
}

function getFlashcardsNumber(set: FlashcardSet) {
  const extra = flashcardSetStore.getExtra(set.id)
  return extra?.flashcardsNumber ?? 0
}

onMounted(async () => {
  await waitUntilStoreLoaded(flashcardSetStore)
    .then(async () => {
      await loadFlashcardSetExtras()
    })
})

</script>

<style scoped>
.flashcard-set-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 1rem;
  border-right: 2px solid rgba(89, 78, 117, 0.85);
  overflow-x: hidden;
  overflow-y: auto;
}

.flashcard-set-list::-webkit-scrollbar {
  width: 8px;
}

.flashcard-set-list::-webkit-scrollbar-track {
  background: #575e68;
  border-radius: 0;
}

.flashcard-set-list::-webkit-scrollbar-thumb {
  background: #808daa;
  border-radius: 0;
}

@media (hover: hover) {
  .flashcard-set-list::-webkit-scrollbar-thumb:hover {
    background: #98a9ca;
  }
}

.flashcard-set-list::-webkit-scrollbar-thumb:active {
  background: #98a9ca;
}

.flashcard-set {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  background: var(--cp--widget--color);
  border: 1px solid var(--cp--border-color);
  border-radius: 6px;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.flashcard-set--loading {
  cursor: default;
  pointer-events: none;
}

@media (hover: hover) {
  .flashcard-set:hover {
    background: var(--cp--widget--color--active);
    backdrop-filter: none;
    transform: translateX(-6px);
  }

  .flashcard-set:hover .flashcard-set-name {
    color: var(--cp--text--color--active);
  }

  .flashcard-set:hover .flashcard-set-language {
    color: var(--cp--text--color--active);
  }
}

.flashcard-set--active {
  background: var(--cp--widget--color--active);
  border-color: rgba(255, 255, 255, 0.81);
  backdrop-filter: none;
  transform: translateX(-6px);
}

.flashcard-set--active .flashcard-set-name {
  color: var(--cp--text--color--active);
}

.flashcard-set--active .flashcard-set-language {
  color: var(--cp--text--color--active);
}

.flashcard-set-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.flashcard-set-name {
  color: var(--cp--text--color);
  font-size: 1.1rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease-in-out;
}

.flashcard-set-language-container {
  --awesome-container--icon--size: 0.8rem;
  --awesome-container--icon--color: var(--cp--text--color--active);
  --awesome-container--gap: 4px;
}

.flashcard-set-language {
  color: var(--cp--text--color);
  font-size: 0.75rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease-in-out;
}

.flashcard-set-spinner-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.4);
}

</style>
