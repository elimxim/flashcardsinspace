<template>
  <Transition name="modal-sheet" :duration="{ enter: 420, leave: 280 }">
    <div
      v-if="visible"
      class="modal-overlay"
      :class="{ 'modal-overlay--dragging': dragging }"
      :style="sheetPosition"
      role="dialog"
      aria-modal="true"
      tabindex="-1"
    >
      <div class="modal-backdrop" />
      <div ref="panel" class="modal-sheet touch-none" tabindex="-1">
        <div
          class="modal-sheet-grip"
          @pointerdown="onPointerDown"
          @pointermove="onPointerMove"
          @pointerup="onPointerUp"
          @pointercancel="onPointerUp"
        >
          <div class="modal-sheet-handle" />
          <slot name="header" />
        </div>
        <slot />
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    visible: boolean
    focusOn?: Pick<HTMLElement, 'focus'>
    onDismiss?: () => void
  }>(),
  {
    focusOn: undefined,
    onDismiss: () => {},
  },
)

const DRAG_START_PX = 6
const DISMISS_DISTANCE = 0.4 // of the sheet height
const FLICK_SPEED = 0.5 // px per ms
const FLICK_MAX_PAUSE_MS = 100
const DISMISS_REFUSED_MS = 1500

const panel = ref<HTMLDivElement>()
const offset = ref(0)
const dragging = ref(false)
const dismissed = ref(false)

const sheetPosition = computed(() => ({
  '--sheet-offset': dismissed.value ? '100%' : `${offset.value}px`,
  '--sheet-progress': dismissed.value ? 1 : offset.value / (panel.value?.offsetHeight || 1),
}))

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    offset.value = 0
    dismissed.value = false
  },
)

let drag:
  | {
      pointerId: number
      startY: number
      startOffset: number
      lastY: number
      lastTime: number
      speed: number
    }
  | undefined

function onPointerDown(event: PointerEvent) {
  if (dismissed.value || event.button !== 0) return
  drag = {
    pointerId: event.pointerId,
    startY: event.clientY,
    startOffset: offset.value,
    lastY: event.clientY,
    lastTime: event.timeStamp,
    speed: 0,
  }
}

function onPointerMove(event: PointerEvent) {
  if (!drag || event.pointerId !== drag.pointerId || !panel.value) return

  const delta = event.clientY - drag.startY
  if (!dragging.value) {
    // Below this it's still a tap, so the header buttons keep working.
    if (Math.abs(delta) < DRAG_START_PX) return
    dragging.value = true
    const grip = event.currentTarget as Element
    grip.setPointerCapture(event.pointerId)
  }

  const elapsed = event.timeStamp - drag.lastTime
  if (elapsed > 0) drag.speed = (event.clientY - drag.lastY) / elapsed
  drag.lastY = event.clientY
  drag.lastTime = event.timeStamp

  offset.value = Math.max(0, drag.startOffset + delta)
  if (offset.value >= panel.value.offsetHeight * DISMISS_DISTANCE) dismiss()
}

function onPointerUp(event: PointerEvent) {
  if (!drag || event.pointerId !== drag.pointerId) return
  // A finger that stopped before lifting isn't a flick.
  const flick = drag.speed > FLICK_SPEED && event.timeStamp - drag.lastTime < FLICK_MAX_PAUSE_MS
  drag = undefined
  dragging.value = false
  // Otherwise the sheet stays where it was let go.
  if (flick) dismiss()
}

function dismiss() {
  drag = undefined
  dragging.value = false
  // Slides down right away, even when the exit button closes the modal
  // only after its tap animation.
  dismissed.value = true
  props.onDismiss()

  // The modal may refuse to close; bring the sheet back instead of leaving it hidden.
  setTimeout(() => {
    if (props.visible && dismissed.value) {
      dismissed.value = false
      offset.value = 0
    }
  }, DISMISS_REFUSED_MS)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  /* clip, not hidden: focusing an input must not scroll the overlay. */
  overflow: clip;
  z-index: 900;
}

.modal-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  opacity: calc(1 - var(--sheet-progress, 0));
  transition: opacity 280ms;
}

.modal-sheet {
  position: absolute;
  top: var(--navbar-height);
  right: 0;
  bottom: 0;
  left: 0;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding: 0 8px calc(8px + env(safe-area-inset-bottom));
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.2);
  user-select: none;
  outline: none;
  transform: translateY(var(--sheet-offset, 0px));
  transition: transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}

/* The sheet follows the finger exactly while dragged. */
.modal-overlay--dragging .modal-sheet,
.modal-overlay--dragging .modal-backdrop {
  transition: none;
}

.modal-sheet-grip {
  cursor: grab;
}

.modal-sheet-handle {
  width: 40px;
  height: 5px;
  margin: 8px auto 4px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.2);
}

.modal-sheet-enter-active .modal-sheet {
  transition: transform 420ms cubic-bezier(0.22, 1, 0.36, 1);
}

.modal-sheet-leave-active .modal-sheet {
  transition: transform 280ms cubic-bezier(0.4, 0, 0.9, 0.4);
}

.modal-sheet-enter-from .modal-sheet,
.modal-sheet-leave-to .modal-sheet {
  transform: translateY(100%);
}

.modal-sheet-enter-from .modal-backdrop,
.modal-sheet-leave-to .modal-backdrop {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .modal-sheet-enter-active .modal-sheet,
  .modal-sheet-leave-active .modal-sheet {
    transition: opacity 160ms;
  }

  .modal-sheet-enter-from .modal-sheet,
  .modal-sheet-leave-to .modal-sheet {
    transform: translateY(var(--sheet-offset, 0px));
    opacity: 0;
  }
}
</style>
