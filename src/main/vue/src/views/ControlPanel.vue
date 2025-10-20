<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <SideBar ref="sidebar"/>
      <div class="control-panel-content">
        <ControlBar :title="flashcardSetName">
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
              :on-click="openFlashcardSetSettings"
              :disabled="!flashcardSet"
            />
          </template>
        </ControlBar>
        <FlashcardInfoBar :hidden="!flashcardSet || isSidebarExpanded" />
        <div class="main-panel">
          <FlashcardWidget/>
          <CalendarWidget/>
          <LaunchPanel/>
          <DayStreakWidget/>
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
import FlashcardWidget from '@/components/control-panel/FlashcardWidget.vue'
import CalendarWidget from '@/components/control-panel/CalendarWidget.vue'
import DayStreakWidget from '@/components/control-panel/DayStreakWidget.vue'
import LaunchPanel from '@/components/control-panel/LaunchPanel.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'
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

onMounted(() => {
  loadFlashcardSetStore()
})
</script>

<style scoped>
.control-panel--theme {
  --c-panel--text-color: #333333;
  --c-panel--bg-color: var(--control-panel--bg-color, #f5f5f5);
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - var(--navbar-height));
  background-color: var(--c-panel--bg-color);
}

.control-panel-layout {
  position: relative;
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
}

.control-panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-panel {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  padding: 6px;
  gap: 16px;
  border-radius: 4px;
  height: 120px;
  flex-wrap: wrap;
}

</style>

