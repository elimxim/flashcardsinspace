<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <SideBar ref="sidebar"/>
      <div class="control-panel-content">
        <ControlBar :title="flashcardSetName">
          <template #left>
            <AwesomeButton
              :on-click="sidebar?.toggle"
              :hidden="sidebar?.isExpanded()"
              :disabled="sidebar?.isExpanded()"
              icon="fa-solid fa-bars"
            />
          </template>
          <template #right>
            <AwesomeButton
              icon="fa-solid fa-gear"
              :on-click="openFlashcardSetSettings"
              :disabled="!flashcardSet"
            />
          </template>
        </ControlBar>
        <div class="main-panel">
          <FlashcardInfoPanel/>
          <CalendarPanel/>
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
</template>

<script setup lang="ts">
import SideBar from '@/components/control-panel/SideBar.vue'
import ControlBar from '@/components/ControlBar.vue'
import CalendarPanel from '@/components/control-panel/CalendarPanel.vue'
import FlashcardInfoPanel from '@/components/control-panel/FlashcardInfoPanel.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'

const flashcardStore = useFlashcardStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)

const sidebar = ref<InstanceType<typeof SideBar>>()
const flashcardSetName = computed(() => flashcardSet.value?.name || '')

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    modalStore.toggleFlashcardSetSettings()
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
  width: 100%;
  height: 100%;
}

.control-panel-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.main-panel {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 6px;
  gap: clamp(4px, 1vw, 10px);
  border-radius: 4px;
  flex-wrap: wrap;
}

</style>

