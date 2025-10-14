<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <FlashcardSetSideBar ref="sidebar"/>
      <div class="control-panel-main">
        <div class="control-panel-top-bar">
          <AwesomeButton
            :on-click="sidebar?.toggle"
            :invisible="sidebar?.isExpanded()"
            :disabled="sidebar?.isExpanded()"
            icon="fa-solid fa-bars"
          />
          <div class="top-bar-text">
            {{ flashcardSetName }}
          </div>
          <AwesomeButton
            icon="fa-solid fa-gear"
            :on-click="openFlashcardSetSettings"
            :disabled="!flashcardSet"
          />
        </div>
        <LaunchButton
          label="Start Review"
          :on-click="onLaunch"
          :disabled="false"
        />
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
</template>

<script setup lang="ts">
import FlashcardSetSideBar from '@/components/control-panel/FlashcardSetSideBar.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import LaunchButton from '@/components/LaunchButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'

const flashcardStore = useFlashcardStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)

const sidebar = ref<InstanceType<typeof FlashcardSetSideBar>>()

const flashcardSetName = computed(() => {
  return flashcardSet.value?.name || 'No Flashcard Set Selected'
})

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
  --top-bar--bg-color: #f5f5f5;
  --top-bar--text-color: #333333;
  --top-bar--shadow-color: rgba(0, 0, 0, 0.4);
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
  box-shadow: -4px 0 4px 0 var(--top-bar--shadow-color);
  width: 100%;
}

.top-bar-text {
  font-size: clamp(1rem, 1.8vw, 1.2rem);
  color: var(--top-bar--text-color);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
</style>

