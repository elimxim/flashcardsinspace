<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <FlashcardSetSideBar ref="sidebar"/>
      <div class="main-panel">
        <div class="topbar">
          <AwesomeButton
            :on-click="sidebar?.toggle"
            :hidden="sidebar?.isExpanded()"
            :disabled="sidebar?.isExpanded()"
            icon="fa-solid fa-bars"
          />
          <div class="topbar-text">
            {{ flashcardSetName }}
          </div>
          <AwesomeButton
            icon="fa-solid fa-gear"
            :on-click="openFlashcardSetSettings"
            :disabled="!flashcardSet"
          />
        </div>
        <div class="launch-panel">
          <CalendarPanel/>
          <LaunchButton
            label="Start Review"
            :on-click="onLaunch"
            :disabled="false"
          />
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
  <FlashcardSetCreationModal/>
  <FlashcardModificationModal/>
</template>

<script setup lang="ts">
import FlashcardSetSideBar from '@/components/control-panel/FlashcardSetSideBar.vue'
import CalendarPanel from '@/components/control-panel/CalendarPanel.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import FlashcardSetCreationModal from '@/views/modal/FlashcardSetCreationModal.vue'
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'

const flashcardStore = useFlashcardStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)

const sidebar = ref<InstanceType<typeof FlashcardSetSideBar>>()
const flashcardSetName = computed(() => flashcardSet.value?.name || '')

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    modalStore.toggleFlashcardSetSettings()
  }
}

const onLaunch = () => console.log('🚀 launch!')

onMounted(() => {
  loadFlashcardSetStore()
})
</script>

<style scoped>
.control-panel--theme {
  font-family: var(--main-font-family);
  --panel--bg-color: var(--control-panel--bg-color, #f5f5f5);
  --panel--topbar--bg-color: #f5f5f5;
  --panel--topbar--text-color: #333333;
  --panel--topbar--shadow-color: rgba(0, 0, 0, 0.4);
  --panel--text-color: #333333;
  --panel--border-color: rgba(0, 0, 0, 0.4);
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - var(--navbar-height));
  background-color: var(--panel--bg-color);
}

.control-panel-layout {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
}

.main-panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  padding: 10px;
  background-color: var(--panel--topbar--bg-color);
  box-shadow: -4px 0 4px 0 var(--panel--topbar--shadow-color);
  width: 100%;
  height: 40px;
}

.topbar-text {
  font-size: clamp(1rem, 1.8vw, 1.2rem);
  color: var(--panel--topbar--text-color);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.launch-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px;
  border: 2px solid #0d124a;
  border-radius: 4px;
  margin: 4px;
  flex-wrap: wrap;
}


</style>

