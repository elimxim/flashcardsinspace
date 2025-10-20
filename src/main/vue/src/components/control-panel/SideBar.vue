<template>
  <div
    class="sidebar sidebar--theme"
    :class="{ 'sidebar--collapsed': !isSidebarExpanded }"
  >
    <ControlBar
      class="sidebar-control-bar"
      title="Flashcard Sets"
      center-title
    >
      <template #left>
        <AwesomeButton
          ref="createButton"
          icon="fa-solid fa-plus"
          class="control-bar-button"
          :on-click="toggleStore.toggleFlashcardSetCreation"
        />
      </template>
      <template #right>
        <AwesomeButton
          ref="toggleButton"
          icon="fa-solid fa-chevron-left"
          class="control-bar-button"
          :on-click="toggle"
        />
      </template>
    </ControlBar>
    <div class="sidebar-content sidebar-content--scrollable">
      <div
        v-for="set in flashcardSets"
        :key="set.id"
        class="sidebar-item"
        :class="{ 'sidebar-item--active': set.id === flashcardSet?.id }"
        @click="selectFlashcardSet(set.id)"
      >
        <div class="sidebar-item__content">
          <div class="sidebar-item__name">{{ set.name }}</div>
          <AwesomeContainer class="sidebar-item__language-container" icon="fa-solid fa-globe">
            <div class="sidebar-item__language">
              {{ getLanguageName(set) }}
            </div>
          </AwesomeContainer>
        </div>
        <div class="sidebar-item__count-container">
          <div class="sidebar-item__count">{{ getFlashcardsNumber(set) }}</div>
        </div>
      </div>
    </div>
  </div>
  <SpaceToast/>
  <FlashcardSetCreationModal/>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import FlashcardSetCreationModal from '@/views/modal/FlashcardSetCreationModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { onMounted, ref } from 'vue'
import {
  loadFlashcardAndChronoStores,
  loadFlashcardSetStore,
  reloadFlashcardAndChronoStores,
} from '@/shared/stores.ts'
import { saveSelectedSetId } from '@/shared/cookies.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import { useControlStore } from '@/stores/control-store.ts'

const flashcardStore = useFlashcardStore()
const flashcardSetStore = useFlashcardSetStore()
const languageStore = useLanguageStore()
const toggleStore = useToggleStore()
const controlStore = useControlStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { flashcardSets } = storeToRefs(flashcardSetStore)
const { isSidebarExpanded } = storeToRefs(controlStore)

let animationTimeout: number | undefined
const animating = ref(false)

function toggle() {
  if (animationTimeout) {
    window.clearTimeout(animationTimeout)
  }
  animating.value = true
  controlStore.toggleSidebar()
  animationTimeout = window.setTimeout(() => {
    animating.value = false
  }, 400)
}

async function selectFlashcardSet(setId: number) {
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    await loadFlashcardAndChronoStores(set)
      .then((loaded) => {
        if (loaded) {
          saveSelectedSetId(set.id)
        }
      })
  }
}

function getFlashcardsNumber(set: FlashcardSet): number {
  return flashcardSetStore.findExtra(set.id)?.flashcardsNumber ?? 0
}

function getLanguageName(set: FlashcardSet): string {
  const language = languageStore.getLanguage(set.languageId)
  return language ? language.name : 'Unknown'
}

defineExpose({
  toggle,
})

onMounted(() => {
  loadFlashcardSetStore()
    .then(async (loaded) => {
      if (loaded) {
        return reloadFlashcardAndChronoStores()
      }
    })
})
</script>


<style scoped>
.sidebar--theme {
  --bar--bg: var(--sidebar--bg, linear-gradient(135deg, rgba(9, 7, 30, 0.86) 0%, rgba(14, 13, 37, 0.82) 100%));
  --bar--border-color: var(--sidebar--border-color, rgba(89, 78, 117, 0.85));
  --bar--item--bg: var(--sidebar--item--bg, linear-gradient(135deg, rgba(154, 190, 197, 0.31) 0%, rgba(156, 166, 200, 0.31) 100%));
  --bar--item--bg--hover: var(--sidebar--item--bg--hover, linear-gradient(135deg, rgba(73, 139, 154, 0.78) 0%, rgba(147, 56, 177, 0.67) 100%));
  --bar--item--bg--active: var(--sidebar--item--bg--active, linear-gradient(135deg, rgba(73, 139, 154, 0.78) 0%, rgba(147, 56, 177, 0.67) 100%));
  --bar--item--border-color: var(--sidebar--item--border-color, rgba(63, 13, 83, 0.81));
  --bar--item--border-color--active: var(--sidebar--item--border-color--active, rgba(63, 13, 83, 0.81));
  --bar--item--text-color: var(--sidebar--item--text-color, rgba(245, 254, 255, 0.95));
  --bar--item--shadow-color: var(--sidebar--item--shadow-color, transparent);
  --bar--item--count--color: var(--sidebar--item--count-color, #081233);
  --bar--item--count--bg: var(--sidebar--item--count--bg, rgba(255, 255, 255, 0.87));
  --bar--item--count--border-color: var(--sidebar--item--count--border-color, rgba(103, 126, 149, 0.83));
  --bar--language--text-color: var(--sidebar--language--text-color, #efe9ef);
  --bar--language--icon-color: var(--sidebar--language--icon-color, rgba(172, 252, 255, 0.85));
  --bar--scrollbar--track-color: var(--sidebar--scrollbar--track-color, #575e68);
  --bar--scrollbar--thumb-color: var(--sidebar--scrollbar--thumb-color, #808daa);
  --bar--scrollbar--thumb-color--hover: var(--sidebar--scrollbar--thumb-hover-color, #98a9ca);
  --bar--scrollbar--thumb-color--active: var(--sidebar--scrollbar--thumb-active-color, #98a9ca);
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  width: clamp(200px, 40vw, 280px);
  height: 100%;
  background: var(--bar--bg);
  transition: margin-left 0.35s ease-in-out;
  overflow: hidden;
}

.sidebar--collapsed {
  margin-left: clamp(-280px, -40vw, -200px);
}

.sidebar-control-bar {
  --control-bar--title--font-size: clamp(0.9rem, 1.5vw, 1.1rem);
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 1rem;
  flex: 1;
  overflow: hidden;
  border-right: 2px solid var(--bar--border-color);
}

.sidebar-content--scrollable {
  overflow-y: auto;
}

.sidebar-content--scrollable::-webkit-scrollbar {
  width: 8px;
}

.sidebar-content--scrollable::-webkit-scrollbar-track {
  background: var(--bar--scrollbar--track-color);
  border-radius: 0;
}

.sidebar-content--scrollable::-webkit-scrollbar-thumb {
  background: var(--bar--scrollbar--thumb-color);
  border-radius: 0;
}

.sidebar-content--scrollable::-webkit-scrollbar-thumb:hover {
  background: var(--bar--scrollbar--thumb-color--hover);
}

.sidebar-content--scrollable::-webkit-scrollbar-thumb:active {
  background: var(--bar--scrollbar--thumb-color--active);
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  background: var(--bar--item--bg);
  border: 1px solid var(--bar--item--border-color);
  border-radius: 6px;
  box-shadow: 2px 2px 3px var(--bar--item--shadow-color);
  backdrop-filter: blur(4px);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.sidebar-item:hover {
  background: var(--bar--item--bg--hover);
  box-shadow: 8px 3px 3px var(--bar--item--shadow-color);
  backdrop-filter: none;
  transform: translateX(-6px);
}

.sidebar-item--active {
  background: var(--bar--item--bg--active);
  border-color: var(--bar--item--border-color--active);
  box-shadow: 8px 3px 3px var(--bar--item--shadow-color);
  backdrop-filter: none;
  transform: translateX(-6px);
}

.sidebar-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.sidebar-item__name {
  color: var(--bar--item--text-color);
  font-size: 0.9rem;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item__language-container {
  --awesome-container--icon--font-size: 0.8rem;
  --awesome-container--icon--color: var(--bar--language--icon-color);
  --awesome-container--gap: 4px;
}

.sidebar-item__language {
  color: var(--bar--language--text-color);
  font-size: 0.65rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item__count {
  width: 32px;
  height: 24px;
  border-radius: 4px;
  background: var(--bar--item--count--bg);
  border: 1px solid var(--bar--item--count--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1px;
  color: var(--bar--item--count--color);
  font-size: 0.7rem;
  margin-left: 0.75rem;
}
</style>
