<template>
  <div class="sidebar-wrapper">
    <div
      class="sidebar-overlay"
      :class="{ 'sidebar-overlay--visible': sidebarExpandedCookie && isOverlay }"
      @click="toggle"
    />
    <div
      class="sidebar"
      :class="{
        'sidebar--collapsed': !sidebarExpandedCookie,
        'sidebar--overlay': isOverlay,
        'sidebar--no-transition': isTransitioning
      }"
    >
      <ControlBar
        class="sidebar-control-bar"
        title="FLASHCARD SETS"
        center-title
        shadow
      >
        <template #left>
          <AwesomeButton
            ref="createButton"
            icon="fa-solid fa-plus"
            class="control-bar-button"
            tooltip="Create New Flashcard Set"
            tooltip-position="bottom-right"
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
          <div class="sidebar-item-content">
            <div class="sidebar-item-name">{{ set.name }}</div>
            <AwesomeContainer class="sidebar-item-language-container" icon="fa-solid fa-globe">
              <div class="sidebar-item-language">
                {{ getLanguageName(set) }}
              </div>
            </AwesomeContainer>
          </div>
          <div class="cp-count-box">
            {{ getFlashcardsNumber(set) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { onMounted, onUnmounted, ref } from 'vue'
import {
  loadFlashcardRelatedStores,
  loadFlashcardSetStore,
  reloadFlashcardRelatedStores,
} from '@/utils/stores.ts'
import { saveSelectedSetIdToCookies } from '@/utils/cookies.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import { sidebarExpandedCookie } from '@/utils/cookies-ref.ts'

const OVERLAY_BREAKPOINT = 620

const flashcardStore = useFlashcardStore()
const flashcardSetStore = useFlashcardSetStore()
const languageStore = useLanguageStore()
const toggleStore = useToggleStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { flashcardSets } = storeToRefs(flashcardSetStore)

const isOverlay = ref(window.innerWidth <= OVERLAY_BREAKPOINT)
const isTransitioning = ref(false)

function updateOverlayMode() {
  const wasOverlay = isOverlay.value
  const nowOverlay = window.innerWidth <= OVERLAY_BREAKPOINT

  if (wasOverlay !== nowOverlay) {
    isTransitioning.value = true
    isOverlay.value = nowOverlay
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        isTransitioning.value = false
      })
    })
  }
}

function toggle() {
  sidebarExpandedCookie.value = !sidebarExpandedCookie.value
}

async function selectFlashcardSet(setId: number) {
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    await loadFlashcardRelatedStores(set)
      .then((loaded) => {
        if (loaded) {
          saveSelectedSetIdToCookies(set.id)
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
  isOverlay,
})

onMounted(() => {
  window.addEventListener('resize', updateOverlayMode)
  loadFlashcardSetStore()
    .then(async (loaded) => {
      if (loaded) {
        return reloadFlashcardRelatedStores()
      }
    })
})

onUnmounted(() => {
  window.removeEventListener('resize', updateOverlayMode)
})
</script>


<style scoped>
.sidebar-overlay {
  display: none;
  position: fixed;
  top: var(--navbar-height);
  left: 0;
  width: 100vw;
  height: calc(100dvh - var(--navbar-height));
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.35s ease-in-out;
}

.sidebar-overlay--visible {
  display: block;
  opacity: 1;
  pointer-events: auto;
}

.sidebar-wrapper {
  position: relative;
  align-self: stretch;
  flex-shrink: 0;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 260px;
  height: 100%;
  background: #32334a;
  overflow: hidden;
  transition: margin-left 0.35s ease-in-out;
}

.sidebar--collapsed {
  margin-left: calc(-1 * 260px);
}

.sidebar--overlay {
  position: absolute;
  left: 0;
  top: 0;
  width: 260px;
  margin-left: 0;
  transition: transform 0.35s ease-in-out;
}

.sidebar--overlay.sidebar--collapsed {
  transform: translateX(-100%);
}

.sidebar--no-transition {
  transition: none !important;
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
  border-right: 2px solid rgba(89, 78, 117, 0.85);
}

.sidebar-content--scrollable {
  overflow-y: auto;
}

.sidebar-content--scrollable::-webkit-scrollbar {
  width: 8px;
}

.sidebar-content--scrollable::-webkit-scrollbar-track {
  background: #575e68;
  border-radius: 0;
}

.sidebar-content--scrollable::-webkit-scrollbar-thumb {
  background: #808daa;
  border-radius: 0;
}

@media (hover: hover) {
  .sidebar-content--scrollable::-webkit-scrollbar-thumb:hover {
    background: #98a9ca;
  }
}

.sidebar-content--scrollable::-webkit-scrollbar-thumb:active {
  background: #98a9ca;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  background: var(--cp--widget--color);
  border: 1px solid var(--cp--border-color);
  border-radius: 6px;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

@media (hover: hover) {
  .sidebar-item:hover {
    background: var(--cp--widget--color--active);
    backdrop-filter: none;
    transform: translateX(-6px);
  }

  .sidebar-item:hover .sidebar-item-name {
    color: var(--cp--text--color--active);
  }

  .sidebar-item:hover .sidebar-item-language {
    color: var(--cp--text--color--active);
  }
}

.sidebar-item--active {
  background: var(--cp--widget--color--active);
  border-color: rgba(255, 255, 255, 0.81);
  backdrop-filter: none;
  transform: translateX(-6px);
}

.sidebar-item--active .sidebar-item-name {
  color: var(--cp--text--color--active);
}

.sidebar-item--active .sidebar-item-language {
  color: var(--cp--text--color--active);
}

.sidebar-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.sidebar-item-name {
  color: var(--cp--text--color);
  font-size: 1.1rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease-in-out;
}

.sidebar-item-language-container {
  --awesome-container--icon--size: 0.8rem;
  --awesome-container--icon--color: var(--cp--text--color--active);
  --awesome-container--gap: 4px;
}

.sidebar-item-language {
  color: var(--cp--text--color);
  font-size: 0.75rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease-in-out;
}

</style>
