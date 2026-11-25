<template>
  <div class="gallery">
    <GalleryFilterSideBar
      ref="sidebar"
      v-model:filter="filter"
      v-model:expanded="gallerySidebarExpandedCookie"
      style="z-index: 200"
      :counts="counts"
      :selected-count="selectedIds.length"
    />
    <div class="gallery-main">
      <ControlBar :title="title" shadow>
        <template #left>
          <AwesomeButton
            icon="fa-solid fa-sliders"
            class="control-bar-button"
            tooltip="Filters"
            tooltip-position="bottom-right"
            :on-click="sidebar?.toggle"
            :hidden="gallerySidebarExpandedCookie && !isSidebarOverlay"
            :disabled="gallerySidebarExpandedCookie && !isSidebarOverlay"
          />
        </template>
        <template #right>
          <AwesomeButton
            icon="fa-solid fa-circle-xmark"
            class="control-bar-button"
            tooltip="Leave the gallery"
            tooltip-position="bottom-left"
            :on-click="exit"
          />
        </template>
      </ControlBar>
      <FlashcardsViewer v-model:filter="filter" v-model:selected-ids="selectedIds" />
    </div>
  </div>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import GalleryFilterSideBar from '@/components/gallery/GalleryFilterSideBar.vue'
import FlashcardsViewer, { type GalleryFilter } from '@/components/gallery/FlashcardsViewer.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { gallerySidebarExpandedCookie } from '@/utils/cookies-ref.ts'
import { routeNames } from '@/router'
import { flashcardStageOn } from '@/core-logic/review-logic.ts'

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const filter = ref<GalleryFilter>({
  selected: false,
  unknown: false,
  attempted: false,
  stages: [],
  outerSpace: false,
})

const selectedIds = ref<number[]>([])

// the Selected toggle disappears with the selection, so it mustn't stay on unseen
watch(selectedIds, (ids) => {
  if (ids.length === 0 && filter.value.selected) {
    filter.value = { ...filter.value, selected: false }
  }
})

const sidebar = ref<InstanceType<typeof GalleryFilterSideBar>>()
const isSidebarOverlay = computed(() => sidebar.value?.isOverlay ?? false)

const title = computed(() => flashcardSet.value?.name ?? '')

const counts = computed(() => {
  const counts = new Map<string, number>()
  flashcards.value.forEach((flashcard) => {
    const name = flashcardStageOn(flashcard, currDay.value).name
    counts.set(name, (counts.get(name) ?? 0) + 1)
  })
  return counts
})

async function exit() {
  await router.push({ name: routeNames.controlPanel })
}
</script>

<style scoped>
.gallery {
  flex: 1;
  align-self: stretch;
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
}

.gallery-main {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}
</style>
