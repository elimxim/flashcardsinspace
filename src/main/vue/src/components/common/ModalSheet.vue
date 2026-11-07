<template>
  <Transition :css="false" @enter="onEnter" @leave="onLeave">
    <div
      v-if="visible"
      ref="overlay"
      class="modal-overlay"
      role="dialog"
      aria-modal="true"
      tabindex="-1"
    >
      <div class="modal-backdrop" />
      <div class="modal-panel modal-sheet touch-none" tabindex="-1">
        <div
          class="modal-sheet-grip"
          @pointerdown="onGripPointerDown"
          @pointermove="onGripPointerMove"
          @pointerup="onGripPointerUp"
          @pointercancel="onGripPointerCancel"
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
import { ref, watch } from 'vue'
import {
  dragVelocity,
  isDismissFlick,
  reachedDismissDistance,
  remainingSlideDuration,
  sheetDragOffset,
  type MotionSample,
} from '@/utils/modal-motion.ts'
import {
  EASE_IN,
  EASE_OUT,
  fade,
  finish,
  freeze,
  modalParts,
  play,
  prefersReducedMotion,
  type ModalParts,
} from '@/utils/modal-animation.ts'

const props = withDefaults(
  defineProps<{
    visible: boolean
    focusOn?: HTMLElement
    onDismiss?: () => void
  }>(),
  {
    focusOn: undefined,
    onDismiss: () => {},
  },
)

const OPEN_MS = 420
const CLOSE_MS = 280
const SETTLE_MS = 260
const DRAG_START_PX = 6
// Long enough for exit buttons that close only after their tap animation.
const DISMISS_REFUSED_MS = 1500
const OFFSCREEN = 'translateY(100%)'

const overlay = ref<HTMLDivElement>()

let dismissedBySwipe = false
let restOffset = 0

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    dismissedBySwipe = false
    restOffset = 0
  },
)

function offsetOf(el: HTMLElement): number {
  const transform = getComputedStyle(el).transform
  return transform === 'none' ? 0 : new DOMMatrixReadOnly(transform).m42
}

/** Moves the sheet from wherever it is now; the backdrop dims or clears with it. */
function slide(
  { panel, backdrop }: ModalParts,
  transform: string,
  duration: number,
  easing: string,
): Animation[] {
  const panelNow = freeze(panel)
  const backdropNow = freeze(backdrop)
  const opacity = transform === OFFSCREEN ? 0 : 1
  return [
    play(panel, [{ transform: panelNow.transform }, { transform }], { duration, easing }),
    play(backdrop, [{ opacity: backdropNow.opacity }, { opacity }], { duration }),
  ]
}

function closeDuration(panel: HTMLElement): number {
  return remainingSlideDuration(offsetOf(panel), panel.offsetHeight, CLOSE_MS)
}

async function onEnter(el: Element, done: () => void) {
  const parts = modalParts(el)
  if (!parts) return done()

  const animations = prefersReducedMotion()
    ? fade(parts.overlay, true)
    : [
        play(parts.backdrop, [{ opacity: 0 }, { opacity: 1 }], { duration: OPEN_MS * 0.7 }),
        play(parts.panel, [{ transform: OFFSCREEN }, { transform: 'none' }], {
          duration: OPEN_MS,
          easing: EASE_OUT,
        }),
      ]
  if (!(await finish(animations))) return

  // Focus only now: focusing an input while the sheet is still below the
  // screen makes mobile browsers jump to it.
  const target = props.focusOn ?? parts.panel
  target.focus({ preventScroll: true })
  done()
}

async function onLeave(el: Element, done: () => void) {
  drag = undefined
  const parts = modalParts(el)
  if (parts) {
    const animations = prefersReducedMotion()
      ? fade(parts.overlay, false)
      : slide(parts, OFFSCREEN, closeDuration(parts.panel), dismissedBySwipe ? EASE_OUT : EASE_IN)
    await finish(animations)
  }
  done()
}

// --- Swipe down to close ---------------------------------------------------

interface Drag {
  pointerId: number
  startY: number
  baseOffset: number
  offset: number
  active: boolean
  samples: MotionSample[]
}

let drag: Drag | undefined

function onGripPointerDown(event: PointerEvent) {
  if (dismissedBySwipe || event.button !== 0) return
  drag = {
    pointerId: event.pointerId,
    startY: event.clientY,
    baseOffset: restOffset,
    offset: restOffset,
    active: false,
    samples: [{ y: event.clientY, time: event.timeStamp }],
  }
}

function onGripPointerMove(event: PointerEvent) {
  const parts = modalParts(overlay.value)
  if (!drag || event.pointerId !== drag.pointerId || !parts) return

  const delta = event.clientY - drag.startY
  if (!drag.active) {
    // Below the threshold it's still a tap, so the header buttons keep working.
    if (Math.abs(delta) < DRAG_START_PX) return
    drag.active = true
    freeze(parts.panel)
    const grip = event.currentTarget as Element
    try {
      grip.setPointerCapture(event.pointerId)
    } catch {
      // The pointer is already gone; the drag still works while it stays on the grip.
    }
  }

  drag.offset = sheetDragOffset(drag.baseOffset + delta)
  drag.samples.push({ y: event.clientY, time: event.timeStamp })
  if (drag.samples.length > 20) drag.samples.shift()

  place(parts, drag.offset)
  if (reachedDismissDistance(drag.offset, parts.panel.offsetHeight)) {
    drag = undefined
    dismiss()
  }
}

function onGripPointerUp(event: PointerEvent) {
  if (!drag || event.pointerId !== drag.pointerId) return
  const released = drag
  drag = undefined
  if (!released.active) return

  if (isDismissFlick(dragVelocity(released.samples))) {
    dismiss()
  } else {
    hold(released.offset)
  }
}

function onGripPointerCancel(event: PointerEvent) {
  if (!drag || event.pointerId !== drag.pointerId) return
  const released = drag
  drag = undefined
  if (released.active) hold(released.offset)
}

function place({ panel, backdrop }: ModalParts, offset: number) {
  panel.style.transform = `translateY(${offset}px)`
  const progress = Math.min(Math.max(offset / panel.offsetHeight, 0), 1)
  backdrop.style.opacity = String(1 - progress)
}

/** Leaves the sheet where the finger let go; only an upward overpull returns to the top. */
function hold(offset: number) {
  if (offset < 0) {
    settle()
  } else {
    restOffset = offset
  }
}

function settle() {
  const parts = modalParts(overlay.value)
  if (!parts) return
  restOffset = 0
  finish(slide(parts, 'none', SETTLE_MS, EASE_OUT))
}

function dismiss() {
  const parts = modalParts(overlay.value)
  if (!parts) return
  dismissedBySwipe = true

  // Start sliding right away: some exit buttons close the modal only after
  // their tap animation. The leave animation picks up from wherever this got to.
  slide(parts, OFFSCREEN, closeDuration(parts.panel), EASE_OUT)
  props.onDismiss()

  // The modal may refuse to close; bring the sheet back instead of leaving it hidden.
  setTimeout(() => {
    if (props.visible && dismissedBySwipe && overlay.value === parts.overlay) {
      dismissedBySwipe = false
      settle()
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
</style>
