<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-row',
      'control-panel',
      'control-panel--theme',
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
            :hidden="isSidebarExpanded && !isSidebarOverlay"
            :disabled="isSidebarExpanded && !isSidebarOverlay"
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
        <FlashcardInfoBar
          :hidden="!flashcardSet || (isSidebarExpanded && !isSidebarOverlay)"
        />
        <MainPanel/>
        <LearningStagesWidget :grow-multiplier="3"/>
        <div class="control-outer-space-panel">
          <OuterSpaceWidget/>
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
  <FlashcardSetCreationModal/>
  <FlashcardCreationModal/>
  <CalendarModal/>
</template>

<script setup lang="ts">
import SideBar from '@/components/control-panel/SideBar.vue'
import ControlBar from '@/components/ControlBar.vue'
import FlashcardInfoBar from '@/components/control-panel/FlashcardInfoBar.vue'
import MainPanel from '@/components/control-panel/MainPanel.vue'
import LearningStagesWidget from '@/components/control-panel/LearningStagesWidget.vue'
import OuterSpaceWidget from '@/components/control-panel/OuterSpaceWidget.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/modals/FlashcardSetSettingsModal.vue'
import FlashcardSetCreationModal from '@/modals/FlashcardSetCreationModal.vue'
import FlashcardCreationModal from '@/modals/FlashcardCreationModal.vue'
import CalendarModal from '@/modals/CalendarModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import { useControlStore } from '@/stores/control-store.ts'

const flashcardStore = useFlashcardStore()
const toggleStore = useToggleStore()
const controlStore = useControlStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { isSidebarExpanded } = storeToRefs(controlStore)

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
.control-panel--theme {
  --c-panel--text-color: #333333;
}

.control-panel {
  min-width: 420px;
}

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
  overflow: scroll;
}

.control-outer-space-panel {
  flex: 0 0 14%;
  overflow: hidden;
}

</style>

