<template>
  <Teleport to="body">
    <CenteredOverlay :visible="open" :z-index="zIndex" :on-backdrop-click="close">
      <slot />
    </CenteredOverlay>
  </Teleport>
</template>

<script setup lang="ts">
import CenteredOverlay from '@/components/common/CenteredOverlay.vue'
import { onBeforeUnmount, watch } from 'vue'

const open = defineModel<boolean>('open', { required: true })

const props = withDefaults(
  defineProps<{
    zIndex?: number
    closeOnEscape?: boolean
  }>(),
  {
    zIndex: 1000,
    closeOnEscape: true,
  },
)

function close() {
  open.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.closeOnEscape) {
    event.preventDefault()
    event.stopPropagation()
    close()
  }
}

// Capture phase so we intercept Escape before a surrounding Modal's window
// listener and stop it there - otherwise Escape closes the modal too.
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
