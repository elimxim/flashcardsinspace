<template>
  <div class="sidebar-wrapper">
    <div
      class="sidebar-overlay"
      :class="{ 'sidebar-overlay--visible': sidebarExpandedCookie && isOverlay }"
      @click="toggle"
    />
    <div
      class="sidebar"
      :class="{
        'sidebar--collapsed': !sidebarExpandedCookie,
        'sidebar--overlay': isOverlay,
        'sidebar--no-transition': isTransitioning
      }"
    >
      <ControlBar
        class="sidebar-control-bar"
        title="FLASHCARD SETS"
        center-title
        shadow
      >
        <template #left>
          <AwesomeButton
            ref="createButton"
            icon="fa-solid fa-plus"
            class="control-bar-button"
            tooltip="Create New Flashcard Set"
            tooltip-position="bottom-right"
            :on-click="toggleStore.toggleFlashcardSetCreation"
          />
        </template>
        <template #right>
          <AwesomeButton
            ref="toggleButton"
            icon="fa-solid fa-chevron-left"
            class="control-bar-button"
            :on-click="toggle"
          />
        </template>
      </ControlBar>
      <Suspense>
        <template #default>
          <FlashcardSetList :on-flashcard-set-changed="onFlashcardSetChanged"/>
        </template>
        <template #fallback>
          <FlashcardSetListSkeleton/>
        </template>
      </Suspense>
    </div>
  </div>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import FlashcardSetList from '@/components/control-panel/FlashcardSetList.vue'
import FlashcardSetListSkeleton from '@/components/control-panel/FlashcardSetListSkeleton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { onMounted, onUnmounted, ref } from 'vue'
import { sidebarExpandedCookie } from '@/utils/cookies-ref.ts'

defineProps<{
  onFlashcardSetChanged: (setId: number) => Promise<boolean>
}>()

const OVERLAY_BREAKPOINT = 620

const toggleStore = useToggleStore()

const isOverlay = ref(window.innerWidth <= OVERLAY_BREAKPOINT)
const isTransitioning = ref(false)

function updateOverlayMode() {
  const wasOverlay = isOverlay.value
  const nowOverlay = window.innerWidth <= OVERLAY_BREAKPOINT

  if (wasOverlay !== nowOverlay) {
    isTransitioning.value = true
    isOverlay.value = nowOverlay
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        isTransitioning.value = false
      })
    })
  }
}

function toggle() {
  sidebarExpandedCookie.value = !sidebarExpandedCookie.value
}

defineExpose({
  toggle,
  isOverlay,
})

onMounted(() => {
  window.addEventListener('resize', updateOverlayMode)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateOverlayMode)
})
</script>

<style scoped>
.sidebar-overlay {
  display: none;
  position: fixed;
  top: var(--navbar-height);
  left: 0;
  width: 100vw;
  height: calc(100dvh - var(--navbar-height));
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.35s ease-in-out;
}

.sidebar-overlay--visible {
  display: block;
  opacity: 1;
  pointer-events: auto;
}

.sidebar-wrapper {
  position: relative;
  align-self: stretch;
  flex-shrink: 0;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 260px;
  height: 100%;
  background: #32334a;
  overflow: hidden;
  transition: margin-left 0.35s ease-in-out;
}

.sidebar--collapsed {
  margin-left: calc(-1 * 260px);
}

.sidebar--overlay {
  position: absolute;
  left: 0;
  top: 0;
  width: 260px;
  margin-left: 0;
  transition: transform 0.35s ease-in-out;
}

.sidebar--overlay.sidebar--collapsed {
  transform: translateX(-100%);
}

.sidebar--no-transition {
  transition: none !important;
}

.sidebar-control-bar {
  --control-bar--title--font-size: clamp(0.9rem, 1.5vw, 1.1rem);
}

</style>
