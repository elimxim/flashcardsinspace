<template>
  <Transition
    name="modal-window"
    :duration="{ enter: 340, leave: 240 }"
    @enter="onEnter"
    @leave="setOrigin"
    @after-leave="returnFocus"
  >
    <div v-if="visible" class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1">
      <div class="modal-backdrop" />
      <div ref="panel" class="modal-window touch-none" tabindex="-1">
        <div class="modal-window-content">
          <slot name="header" />
          <slot />
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  focusOn?: Pick<HTMLElement, 'focus'>
}>()

const ORIGIN_MAX_AGE_MS = 1500

const panel = ref<HTMLDivElement>()

let lastPressed: { el: Element; time: number } | undefined

let origin: Element | undefined

watch(
  () => props.visible,
  (visible) => {
    if (visible) origin = takeOrigin()
  },
)

// Capture phase: AwesomeButton stops click propagation.
onMounted(() => document.addEventListener('pointerdown', rememberPressed, true))
onUnmounted(() => document.removeEventListener('pointerdown', rememberPressed, true))

function rememberPressed(event: PointerEvent) {
  if (!(event.target instanceof Element)) return
  const el = event.target.closest('button, [role="button"], a') ?? event.target
  lastPressed = { el, time: performance.now() }
}

/** The control that just opened the window; falls back to the focused one for keyboard users. */
function takeOrigin(): Element | undefined {
  const pressed = lastPressed
  lastPressed = undefined
  if (pressed && performance.now() - pressed.time < ORIGIN_MAX_AGE_MS) return pressed.el
  const focused = document.activeElement
  return focused && focused !== document.body ? focused : undefined
}

function onEnter(el: Element) {
  nextTick(focusInitial)

  const box = setOrigin(el)
  if (!box) return
  // Child components may measure themselves on mount, so the browser can have
  // styled the window before --modal-origin was set. Restart its transition so
  // it really starts from the button.
  box.style.transition = 'none'
  void box.offsetWidth
  box.style.transition = ''
}

/** Tells the CSS where the window grows from and shrinks back to. */
function setOrigin(el: Element): HTMLElement | null {
  const overlay = el as HTMLElement
  const box = overlay.querySelector<HTMLElement>('.modal-window')
  const button = origin?.isConnected ? origin.getBoundingClientRect() : undefined
  let transform = 'scale(0.92)'
  if (box && button?.width) {
    // offset* ignore transforms, so this is where the window sits when fully open.
    const dx = button.left + button.width / 2 - (box.offsetLeft + box.offsetWidth / 2)
    const dy = button.top + button.height / 2 - (box.offsetTop + box.offsetHeight / 2)
    const sx = button.width / box.offsetWidth
    const sy = button.height / box.offsetHeight
    transform = `translate(${dx}px, ${dy}px) scale(${sx}, ${sy})`
  }
  overlay.style.setProperty('--modal-origin', transform)
  return box
}

function focusInitial() {
  const target = props.focusOn ?? panel.value
  target?.focus({ preventScroll: true })
}

function returnFocus() {
  if (document.activeElement === document.body && origin instanceof HTMLElement) {
    origin.focus({ preventScroll: true })
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  /* clip, not hidden: focusing an input must not scroll the overlay. */
  overflow: clip;
  z-index: 900;
}

.modal-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-window {
  position: relative;
  background: #fff;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  width: clamp(240px, 92vw, 540px);
  min-width: 240px;
  height: clamp(360px, 90vh, 480px);
  min-height: 360px;
  padding: 8px;
  margin: 10px 10px clamp(10px, 10vh, 100px);
  user-select: none;
  resize: none;
  outline: none;
}

.modal-window-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* Open: grow out of the button with a slight overshoot. */
.modal-window-enter-active .modal-window {
  transition:
    transform 340ms cubic-bezier(0.2, 1.12, 0.36, 1),
    opacity 100ms linear;
}

/* Close: shrink back into the button, fading out at the very end. */
.modal-window-leave-active .modal-window {
  transition:
    transform 240ms cubic-bezier(0.45, 0, 0.55, 1),
    opacity 100ms linear 140ms;
}

.modal-window-enter-from .modal-window,
.modal-window-leave-to .modal-window {
  transform: var(--modal-origin);
  opacity: 0;
}

.modal-window-leave-active .modal-window-content {
  transition: opacity 80ms ease-in;
}

.modal-window-enter-from .modal-window-content,
.modal-window-leave-to .modal-window-content {
  opacity: 0;
}

.modal-window-enter-active .modal-backdrop,
.modal-window-leave-active .modal-backdrop {
  transition: opacity 240ms;
}

.modal-window-enter-from .modal-backdrop,
.modal-window-leave-to .modal-backdrop {
  opacity: 0;
}

.modal-window-enter-active .modal-window,
.modal-window-leave-active .modal-window {
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .modal-window-enter-from .modal-window,
  .modal-window-leave-to .modal-window {
    transform: none;
  }
}
</style>
