<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-row',
    ]"
  >
    <SideBar ref="sidebar" style="z-index: 200;"/>
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
        <WelcomeWidget v-if="!flashcardSet" class="control-welcome"/>
        <template v-else>
          <FlashcardSetInfoBar
            :hidden="!flashcardSet || (sidebarExpandedCookie && !isSidebarOverlay)"
          />
          <MainPanel/>
          <LearningStagesWidget :grow-multiplier="3"/>
          <div class="control-outer-space-panel">
            <OuterSpaceWidget/>
          </div>
        </template>
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
import FlashcardSetInfoBar from '@/components/control-panel/FlashcardSetInfoBar.vue'
import MainPanel from '@/components/control-panel/MainPanel.vue'
import LearningStagesWidget from '@/components/control-panel/LearningStagesWidget.vue'
import OuterSpaceWidget from '@/components/control-panel/OuterSpaceWidget.vue'
import WelcomeWidget from '@/components/control-panel/WelcomeWidget.vue'
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
import { computed, ref } from 'vue'
import { sidebarExpandedCookie } from '@/utils/cookies-ref.ts'

const flashcardStore = useFlashcardStore()
const toggleStore = useToggleStore()

const { flashcardSet } = storeToRefs(flashcardStore)

const sidebar = ref<InstanceType<typeof SideBar>>()
const isSidebarOverlay = computed(() => sidebar.value?.isOverlay ?? false)

const flashcardSetName = computed(() => flashcardSet.value?.name || '')

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    toggleStore.toggleFlashcardSetSettings()
  }
}

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

.control-outer-space-panel {
  flex: 0 0 14%;
  overflow: hidden;
}

.control-welcome {
  margin: auto;
}

</style>

