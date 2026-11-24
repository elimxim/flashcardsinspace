<template>
  <Teleport to="body">
    <transition name="zoom-overlay">
      <div v-if="open" class="zoom-overlay" @click.self="close">
        <div class="zoom-overlay-content">
          <slot />
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue'

const open = defineModel<boolean>('open', { required: true })

function close() {
  open.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    close()
  }
}

watch(
  open,
  (isOpen) => {
    if (isOpen) {
      window.addEventListener('keydown', onKeydown, { capture: true })
    } else {
      window.removeEventListener('keydown', onKeydown, { capture: true })
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown, { capture: true })
})
</script>

<style scoped>
.zoom-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: grid;
  place-items: center;
  padding: 4vmin;
  cursor: zoom-out;
  z-index: 1000;
}

.zoom-overlay-content {
  display: flex;
  cursor: default;
}

.zoom-overlay-enter-active,
.zoom-overlay-leave-active {
  transition: opacity 0.2s ease-in-out;
}

.zoom-overlay-enter-active .zoom-overlay-content,
.zoom-overlay-leave-active .zoom-overlay-content {
  transition: transform 0.2s ease-in-out;
}

.zoom-overlay-enter-from,
.zoom-overlay-leave-to {
  opacity: 0;
}

.zoom-overlay-enter-from .zoom-overlay-content,
.zoom-overlay-leave-to .zoom-overlay-content {
  transform: scale(0.85);
}
</style>
