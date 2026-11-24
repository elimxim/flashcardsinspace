<template>
  <SideBar ref="sidebar" v-model:expanded="sidebarExpandedCookie" title="FLASHCARD SETS">
    <template #left>
      <AwesomeButton
        icon="fa-solid fa-plus"
        class="control-bar-button"
        tooltip="Create New Flashcard Set"
        tooltip-position="bottom-right"
        :on-click="toggleStore.toggleFlashcardSetCreation"
      />
    </template>
    <DeferredLoading :wait-for="flashcardSetListAwaitCond">
      <FlashcardSetList :on-flashcard-set-changed="onFlashcardSetChanged" />
      <template #fallback>
        <FlashcardSetListSkeleton />
      </template>
    </DeferredLoading>
  </SideBar>
</template>

<script setup lang="ts">
import SideBar from '@/components/common/SideBar.vue'
import DeferredLoading from '@/components/common/DeferredLoading.vue'
import FlashcardSetList from '@/components/control-panel/FlashcardSetList.vue'
import FlashcardSetListSkeleton from '@/components/control-panel/FlashcardSetListSkeleton.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { computed, ref } from 'vue'
import { sidebarExpandedCookie } from '@/utils/cookies-ref.ts'
import { waitUntilStoreLoaded } from '@/utils/store-loading.ts'

defineProps<{
  onFlashcardSetChanged: (setId: number) => Promise<boolean>
}>()

const toggleStore = useToggleStore()
const languageStore = useLanguageStore()
const flashcardSetStore = useFlashcardSetStore()

const sidebar = ref<InstanceType<typeof SideBar>>()
const isOverlay = computed(() => sidebar.value?.isOverlay ?? false)

const flashcardSetListAwaitCond = () =>
  Promise.all([waitUntilStoreLoaded(languageStore), waitUntilStoreLoaded(flashcardSetStore)])

function toggle() {
  sidebar.value?.toggle()
}

defineExpose({
  toggle,
  isOverlay,
})
</script>
