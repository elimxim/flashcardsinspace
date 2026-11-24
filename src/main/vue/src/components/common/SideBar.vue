<template>
  <div class="sidebar-wrapper">
    <div
      class="sidebar-overlay"
      :class="{ 'sidebar-overlay--visible': expanded && isOverlay }"
      @click="toggle"
    />
    <div
      class="sidebar"
      :class="{
        'sidebar--collapsed': !expanded,
        'sidebar--overlay': isOverlay,
        'sidebar--no-transition': isTransitioning,
      }"
    >
      <ControlBar class="sidebar-control-bar" :title="title" center-title shadow>
        <template #left>
          <slot name="left" />
        </template>
        <template #right>
          <AwesomeButton
            icon="fa-solid fa-chevron-left"
            class="control-bar-button"
            :on-click="toggle"
          />
        </template>
      </ControlBar>
      <div class="sidebar-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import { onMounted, onUnmounted, ref } from 'vue'

defineProps<{
  title: string
}>()

const expanded = defineModel<boolean>('expanded', { required: true })

const OVERLAY_BREAKPOINT = 620

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
  expanded.value = !expanded.value
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

.sidebar-content {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.sidebar-content::-webkit-scrollbar {
  width: 8px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: #575e68;
  border-radius: 0;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: #808daa;
  border-radius: 0;
}

@media (hover: hover) {
  .sidebar-content::-webkit-scrollbar-thumb:hover {
    background: #98a9ca;
  }
}

.sidebar-content::-webkit-scrollbar-thumb:active {
  background: #98a9ca;
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
