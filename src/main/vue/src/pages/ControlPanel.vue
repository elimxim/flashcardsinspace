<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-row',
    ]"
  >
    <SideBar
      ref="sidebar"
      style="z-index: 200;"
      :on-flashcard-set-changed="onFlashcardSetChanged"
    />
    <div class="control-panel-layout">
      <ControlBar :title="flashcardSetName" shadow>
        <template #left>
          <AwesomeButton
            icon="fa-solid fa-bars"
            class="control-bar-button"
            :on-click="sidebar?.toggle"
            :hidden="sidebarExpandedCookie && !isSidebarOverlay"
            :disabled="sidebarExpandedCookie && !isSidebarOverlay"
          />
        </template>
        <template #right>
          <AwesomeButton
            icon="fa-solid fa-gear"
            class="control-bar-button"
            tooltip="Edit Flashcard Set"
            tooltip-position="bottom-left"
            :on-click="openFlashcardSetSettings"
            :disabled="!flashcardSet"
          />
        </template>
      </ControlBar>
      <div class="control-panel-content scrollbar-hidden">
        <KineticRingSpinner v-if="resolvedLoading"/>
        <WelcomeWidget v-else-if="!loadingStarted && !flashcardSet" class="control-welcome"/>
        <WidgetBoard
          v-else-if="!loadingStarted"
          :show-info-bar="sidebarExpandedCookie && !isSidebarOverlay"
        />
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
  <FlashcardSetCreationModal/>
  <FlashcardCreationModal/>
  <CalendarModal/>
  <QuizModal/>
  <SpaceToast/>
</template>

<script setup lang="ts">
import SideBar from '@/components/control-panel/SideBar.vue'
import ControlBar from '@/components/ControlBar.vue'
import KineticRingSpinner from '@/components/KineticRingSpinner.vue'
import WelcomeWidget from '@/components/control-panel/WelcomeWidget.vue'
import WidgetBoard from '@/components/control-panel/WidgetBoard.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/modals/FlashcardSetSettingsModal.vue'
import FlashcardSetCreationModal from '@/modals/FlashcardSetCreationModal.vue'
import FlashcardCreationModal from '@/modals/FlashcardCreationModal.vue'
import CalendarModal from '@/modals/CalendarModal.vue'
import QuizModal from '@/modals/QuizModal.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { sidebarExpandedCookie } from '@/utils/cookies-ref.ts'
import {
  loadFlashcardSetStore,
  loadStoresForCurrFlashcardSet,
  loadStoresForFlashcardSet,
} from '@/utils/store-loading.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'

const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const toggleStore = useToggleStore()

const { flashcardSet } = storeToRefs(flashcardStore)

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
  resetLoading,
} = useDeferredLoading()

const flashcardSetName = ref<string>()

const sidebar = ref<InstanceType<typeof SideBar>>()
const isSidebarOverlay = computed(() => sidebar.value?.isOverlay ?? false)

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    toggleStore.toggleFlashcardSetSettings()
  }
}

async function onFlashcardSetChanged(setId: number): Promise<boolean> {
  Log.log(LogTag.DEBUG, `onFlashcardSetChanged: ${setId}`)
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    try {
      flashcardSetName.value = undefined
      resetLoading()
      startLoading()
      return await loadStoresForFlashcardSet(set)
    } finally {
      flashcardSetName.value = flashcardSet.value?.name ?? ''
      await stopLoading()
    }
  }
  return false
}

onMounted(async () => {
  try {
    startLoading()
    await loadFlashcardSetStore()
      .then((loaded) => {
        if (loaded) {
          return loadStoresForCurrFlashcardSet()
        }
      })
  } finally {
    flashcardSetName.value = flashcardSet.value?.name ?? ''
    await stopLoading()
  }
})

</script>

<style scoped>
.control-panel-layout {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.control-panel-content {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 10px 10px 10px;
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
}

.control-welcome {
  margin: auto;
}

</style>

