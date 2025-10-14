<template>
  <div
    class="sidebar sidebar--theme"
    :class="{ 'sidebar--collapsed': !expanded }"
  >
    <div class="sidebar-header">
      <AwesomeButton
        ref="createButton"
        icon="fa-solid fa-plus"
        :on-click="modalStore.toggleFlashcardSetCreation"
      />
      <div class="sidebar-header__title">
        Flashcard Sets
      </div>
      <AwesomeButton
        ref="toggleButton"
        icon="fa-solid fa-chevron-left"
        :on-click="toggle"
      />
    </div>
    <div class="sidebar-content sidebar__content--scrollable">
      <div
        v-for="set in flashcardSets"
        :key="set.id"
        class="sidebar-item"
        :class="{ 'sidebar-item--active': set.id === flashcardSet?.id }"
        @click="selectFlashcardSet(set.id)"
      >
        <div class="sidebar-item__content">
          <div class="sidebar-item__name">{{ set.name }}</div>
          <div class="sidebar-item__language">{{ getLanguageName(set.languageId) }}</div>
        </div>
        <div class="sidebar-item__count-container">
          <div class="sidebar-item__count">{{ getFlashcardCount(set.id) }}</div>
        </div>
      </div>
    </div>
  </div>
  <SpaceToast/>
  <FlashcardSetCreationModal/>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import FlashcardSetCreationModal from '@/views/modal/FlashcardSetCreationModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import {
  loadFlashcardAndChronoStores,
  loadFlashcardSetStore,
  reloadFlashcardAndChronoStores,
} from '@/shared/stores.ts'
import { saveSelectedSetId } from '@/shared/cookies.ts'

const flashcardStore = useFlashcardStore()
const flashcardSetStore = useFlashcardSetStore()
const languageStore = useLanguageStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { flashcardSets } = storeToRefs(flashcardSetStore)

let animationTimeout: number | undefined
const animating = ref(false)
const expanded = ref(true)

function toggle() {
  if (animationTimeout) {
    window.clearTimeout(animationTimeout)
  }
  animating.value = true
  expanded.value = !expanded.value
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

function getFlashcardCount(setId: number): number {
  return 0
}

function getLanguageName(languageId: number): string {
  const language = languageStore.getLanguage(languageId)
  return language ? language.name : 'Unknown'
}

defineExpose({
  toggle,
  expanded: computed(() => expanded.value),
  animating: computed(() => animating.value),
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
  font-family: var(--main-font-family);
  --bar--bg-color: var(--sidebar--bg-color, #f5f5f5);
  --bar--border-color: var(--sidebar--border-color, #dee2e6);
  --bar--text-color: var(--sidebar--text-color, #495057);
  --bar--shadow-color: var(--sidebar--shadow-color, rgba(0, 0, 0, 0.4));
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
  width: 280px;
  height: 100%;
  background-color: var(--bar--bg-color);
  transition: margin-left 0.35s ease-in-out;
  overflow: hidden;
}

.sidebar--collapsed {
  margin-left: -280px;
}

.sidebar-header {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: var(--bar--bg-color);
  box-shadow: 4px 0 4px 0 var(--bar--shadow-color);
  flex-shrink: 0;
}

.sidebar-header__title {
  font-size: clamp(0.9rem, 1.5vw, 1.1rem);
  color: var(--bar--text-color);
  font-weight: normal;
  white-space: nowrap;
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

.sidebar__content--scrollable {
  overflow-y: auto;
}

.sidebar__content--scrollable::-webkit-scrollbar {
  width: 8px;
}

.sidebar__content--scrollable::-webkit-scrollbar-track {
  background: var(--bar--scrollbar--track-color);
  border-radius: 0;
}

.sidebar__content--scrollable::-webkit-scrollbar-thumb {
  background: var(--bar--scrollbar--thumb-color);
  border-radius: 0;
}

.sidebar__content--scrollable::-webkit-scrollbar-thumb:hover {
  background: var(--bar--scrollbar--thumb-color--hover);
}

.sidebar__content--scrollable::-webkit-scrollbar-thumb:active {
  background: var(--bar--scrollbar--thumb-color--active);
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 6px 10px;
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

.sidebar-item__lamp-container {
  margin-right: 0.75rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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
  background: linear-gradient(135deg,
  var(--bar--count--bg-color) 0%,
  color-mix(in srgb, var(--bar--count--bg-color) 90%, #000000) 50%,
  color-mix(in srgb, var(--bar--count--bg-color) 85%, #000000) 100%);
  border: 1px solid var(--bar--count--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--bar--item--count--color);
  font-size: 0.7rem;
  box-shadow: inset 0 1px 2px var(--bar--item--count--shadow-color),
  inset 0 1px 0 color-mix(in srgb, var(--bar--count--bg-color) 80%, #ffffff),
  inset 0 -1px 0 color-mix(in srgb, var(--bar--count--bg-color) 80%, #000000),
  0 1px 2px var(--bar--item--count--shadow-color);
  margin-left: 0.75rem;
}
</style>
