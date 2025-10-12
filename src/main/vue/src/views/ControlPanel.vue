<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <!-- Left Sidebar -->
      <div class="control-panel-sidebar" :class="{ 'sidebar-collapsed': !sidebarExpanded }">
        <div class="sidebar-header">
          <div class="sidebar-title">Flashcard Sets</div>
          <AwesomeButton
            ref="toggleButton"
            icon="fa-solid fa-chevron-left"
            :on-click="toggleSidebar"
          />
        </div>
        <div class="sidebar-content" :class="{ 'scrollable': sidebarExpanded && !isSidebarAnimating }">
          <div
            v-for="set in flashcardSets"
            :key="set.id"
            class="sidebar-item"
            :class="{ 'sidebar-item-active': set.id === flashcardSet?.id }"
            @click="selectFlashcardSet(set.id)"
          >
            <div class="sidebar-item-name">{{ set.name }}</div>
            <div class="sidebar-item-count">{{ getFlashcardCount(set.id) }}</div>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="control-panel-main">
        <div class="control-panel-top-bar">
          <AwesomeButton
            ref="toggleSidebarTopButton"
            :on-click="toggleSidebar"
            :hidden="sidebarExpanded"
            :disabled="sidebarExpanded"
            icon="fa-solid fa-bars"
            auto-blur
          />
          <div class="flashcard-set-name">
            {{ flashcardSetName }}
          </div>
          <AwesomeButton
            ref="settingsButton"
            icon="fa-solid fa-gear"
            :on-click="openFlashcardSetSettings"
          />
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardAndChronoStores, loadFlashcardSetStore } from '@/shared/stores.ts'

const flashcardStore = useFlashcardStore()
const flashcardSetStore = useFlashcardSetStore()
const modalStore = useModalStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { flashcardSets } = storeToRefs(flashcardSetStore)

const sidebarExpanded = ref(true)

const flashcardSetName = computed(() => {
  return flashcardSet.value?.name || 'No Flashcard Set Selected'
})

let sidebarAnimTimeout: number | undefined
const isSidebarAnimating = ref(false)

function toggleSidebar() {
  if (sidebarAnimTimeout) {
    window.clearTimeout(sidebarAnimTimeout)
  }
  isSidebarAnimating.value = true
  sidebarExpanded.value = !sidebarExpanded.value
  sidebarAnimTimeout = window.setTimeout(() => {
    isSidebarAnimating.value = false
  }, 400)
}

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    modalStore.toggleFlashcardSetSettings()
  }
}

async function selectFlashcardSet(setId: number) {
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    await loadFlashcardAndChronoStores(set)
  }
}

function getFlashcardCount(setId: number): string {
  if (flashcardSet.value?.id === setId) {
    return flashcards.value.length.toString()
  }
  return '—'
}

onMounted(() => {
  loadFlashcardSetStore()
})
</script>

<style scoped>
.control-panel--theme {
  font-family: var(--main-font-family);
  --top-bar--bg-color: #f5f5f5;
  --top-bar--text-color: #333333;
  --top-bar--shadow-color: rgba(0, 0, 0, 0.1);
  --sidebar--bg-color: #fafafa;
  --sidebar--border-color: #e0e0e0;
  --sidebar--item-hover-bg: #eeeeee;
  --sidebar--item-active-bg: #e3f2fd;
  --sidebar--item-text-color: #333333;
  --sidebar--item-count-color: #666666;
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - var(--navbar-height));
}

.control-panel-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

.control-panel-sidebar {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100%;
  background-color: var(--sidebar--bg-color);
  border-right: 1px solid var(--sidebar--border-color);
  box-shadow: 2px 0 8px var(--top-bar--shadow-color);
  transition: width 0.35s ease;
  overflow: hidden;
}

.control-panel-sidebar.sidebar-collapsed {
  width: 0;
  box-shadow: none;
  border-right: none;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: var(--top-bar--bg-color);
  box-shadow: 0 2px 4px var(--top-bar--shadow-color);
  flex-shrink: 0;
}

.sidebar-title {
  font-size: clamp(0.9rem, 1.5vw, 1.1rem);
  color: var(--top-bar--text-color);
  font-weight: normal;
  white-space: nowrap;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  flex: 1;
  overflow: hidden;
}

.sidebar-content.scrollable {
  overflow-y: auto;
}

.sidebar-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  background-color: white;
  border: 1px solid var(--sidebar--border-color);
  border-radius: 6px;
  transition: all 0.1s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.sidebar-item:hover {
  background-color: var(--sidebar--item-hover-bg);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.sidebar-item-active {
  background-color: var(--sidebar--item-active-bg);
  border-color: #90caf9;
}

.sidebar-item-name {
  flex: 1;
  color: var(--sidebar--item-text-color);
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 0.5rem;
}

.sidebar-item-count {
  color: var(--sidebar--item-count-color);
  font-size: 0.85rem;
  font-weight: 500;
  min-width: 30px;
  text-align: right;
}

.control-panel-main {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.control-panel-top-bar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  padding: 10px;
  background-color: var(--top-bar--bg-color);
  box-shadow: 0 2px 4px var(--top-bar--shadow-color);
  width: 100%;
}

.flashcard-set-name {
  font-size: clamp(1rem, 1.8vw, 1.2rem);
  color: var(--top-bar--text-color);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
</style>

