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
          :on-click="toggleStore.toggleFlashcardSetCreation"
        />
      </template>
      <template #right>
        <AwesomeButton
          ref="toggleButton"
          icon="fa-solid fa-chevron-left"
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
          <div class="sidebar-item__language">{{ getLanguageName(set) }}</div>
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
  --bar--bg-color: var(--sidebar--bg-color, #f5f5f5);
  --bar--border-color: var(--sidebar--border-color, #dee2e6);
  --bar--item--bg-color: var(--sidebar--item--bg-color, #ffffff);
  --bar--item--bg-color--hover: var(--sidebar--item--bg-color--hover, #e8eef5);
  --bar--item--bg-color--active: var(--sidebar--item--bg-color--active, #e8eef5);
  --bar--item--border-color: var(--sidebar--item--border-color, #dee2e6);
  --bar--item--border-color--active: var(--sidebar--item--border-color--active, #dee2e6);
  --bar--item--text-color: var(--sidebar--item--text-color, #495057);
  --bar--item--shadow-color: var(--sidebar--item--shadow-color, rgba(0, 0, 0, 0.1));
  --bar--item--count--color: var(--sidebar--item--count-color, #6c757d);
  --bar--item--count--shadow-color: var(--sidebar--item--count--shadow-color, rgba(0, 0, 0, 0.1));
  --bar--language--text-color: var(--sidebar--language--text-color, #6c757d);
  --bar--count--bg-color: var(--sidebar--count--bg-color, #f8f9fa);
  --bar--count--border-color: var(--sidebar--count--border-color, #dee2e6);
  --bar--scrollbar--track-color: var(--sidebar--scrollbar--track-color, #f1f3f4);
  --bar--scrollbar--thumb-color: var(--sidebar--scrollbar--thumb-color, #dfdfdf);
  --bar--scrollbar--thumb-color--hover: var(--sidebar--scrollbar--thumb-hover-color, #cccccc);
  --bar--scrollbar--thumb-color--active: var(--sidebar--scrollbar--thumb-active-color, #cccccc);
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  width: clamp(200px, 40vw, 280px);
  height: 100%;
  background-color: var(--bar--bg-color);
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
  border-right: 1px solid var(--bar--border-color);
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
  background: linear-gradient(135deg,
  var(--bar--item--bg-color) 0%,
  color-mix(in srgb, var(--bar--item--bg-color) 95%, #000000) 50%,
  color-mix(in srgb, var(--bar--item--bg-color) 90%, #000000) 100%);
  border: 1px solid var(--bar--item--border-color);
  border-radius: 3px;
  box-shadow: 0 1px 2px var(--bar--item--shadow-color),
  inset 0 1px 0 color-mix(in srgb, var(--bar--item--bg-color) 80%, #ffffff),
  inset 0 -1px 0 color-mix(in srgb, var(--bar--item--bg-color) 80%, #000000);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.sidebar-item:hover {
  background: linear-gradient(135deg,
  var(--bar--item--bg-color--hover) 0%,
  color-mix(in srgb, var(--bar--item--bg-color--hover) 92%, #000000) 50%,
  color-mix(in srgb, var(--bar--item--bg-color--hover) 85%, #000000) 100%);
  box-shadow: 0 3px 6px var(--bar--item--shadow-color),
  inset 0 1px 0 color-mix(in srgb, var(--bar--item--bg-color--hover) 75%, #ffffff),
  inset 0 -1px 0 color-mix(in srgb, var(--bar--item--bg-color--hover) 75%, #000000);
  transform: translateX(-6px);
}

.sidebar-item--active {
  background: linear-gradient(135deg,
  var(--bar--item--bg-color--active) 0%,
  color-mix(in srgb, var(--bar--item--bg-color--active) 88%, #000000) 50%,
  color-mix(in srgb, var(--bar--item--bg-color--active) 80%, #000000) 100%);
  border-color: var(--bar--item--border-color--active);
  box-shadow: 0 2px 4px var(--bar--item--shadow-color),
  inset 0 1px 0 color-mix(in srgb, var(--bar--item--bg-color--active) 70%, #ffffff),
  inset 0 -1px 0 color-mix(in srgb, var(--bar--item--bg-color--active) 70%, #000000),
  inset 0 0 0 1px color-mix(in srgb, var(--bar--item--bg-color--active) 60%, #0066cc);
}

.sidebar-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
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
  border-radius: 2px;
  background: var(--bar--count--bg-color);
  border: 1px solid var(--bar--count--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1px;
  color: var(--bar--item--count--color);
  font-size: 0.7rem;
  margin-left: 0.75rem;
}
</style>
