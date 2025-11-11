<template>
  <div class="control-panel control-panel--theme">
    <div class="control-sidebar">
      <SideBar ref="sidebar"/>
    </div>
    <div class="control-panel-layout">
      <ControlBar :title="flashcardSetName" shadow>
        <template #left>
          <AwesomeButton
            icon="fa-solid fa-bars"
            class="control-bar-button"
            :on-click="sidebar?.toggle"
            :hidden="isSidebarExpanded"
            :disabled="isSidebarExpanded"
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
      <div class="control-scrollable">
        <FlashcardInfoBar
          :hidden="!flashcardSet || isSidebarExpanded"
        />
        <MainPanel/>
        <div class="control-stages-panel">
          <LearningStagesWidget :grow-multiplier="3"/>
        </div>
        <div class="control-outer-space-panel">
          <OuterSpaceWidget/>
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
</template>

<script setup lang="ts">
import SideBar from '@/components/control-panel/SideBar.vue'
import ControlBar from '@/components/ControlBar.vue'
import FlashcardInfoBar from '@/components/control-panel/FlashcardInfoBar.vue'
import MainPanel from '@/components/control-panel/MainPanel.vue'
import LearningStagesWidget from '@/components/control-panel/LearningStagesWidget.vue'
import OuterSpaceWidget from '@/components/control-panel/OuterSpaceWidget.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
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
  --c-panel--bg-color: var(--control-panel--bg-color, #f5f5f5);
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: row;
  width: 100%;
  min-width: 420px;
  height: calc(100vh - var(--navbar-height));
  background-color: var(--c-panel--bg-color);
}

.control-sidebar {
  z-index: 1;
}

.control-panel-layout {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.control-scrollable {
  flex: 1;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 10px 10px 10px;
  overflow: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* Internet Explorer 10+ */
}

.control-scrollable::-webkit-scrollbar {
  display: none; /* Safari and Chrome */
}

.control-stages-panel {
  transition: all 0.2s ease-in-out;
}

.control-outer-space-panel {
  flex: 0 0 14%;
  overflow: hidden;
}

</style>

