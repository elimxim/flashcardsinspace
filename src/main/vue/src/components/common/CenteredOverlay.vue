<template>
  <Transition
    name="centered-overlay"
    :duration="{ enter: 340, leave: 240 }"
    @enter="onEnter"
    @leave="setOrigin"
    @after-leave="returnFocus"
  >
    <div
      v-if="visible"
      class="centered-overlay"
      :style="{ zIndex }"
      role="dialog"
      aria-modal="true"
      tabindex="-1"
    >
      <div
        class="centered-overlay-backdrop"
        :class="{ 'centered-overlay-backdrop--dismissible': !!onBackdropClick }"
        @click="onBackdropClick?.()"
      />
      <div ref="panel" class="centered-overlay-panel" tabindex="-1">
        <slot />
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

/**
 * A backdrop with its content centered on the screen. The content grows out of the
 * control that opened it and shrinks back into it; focus returns there afterwards.
 */
const props = withDefaults(
  defineProps<{
    visible: boolean
    focusOn?: Pick<HTMLElement, 'focus'>
    zIndex?: number
    onBackdropClick?: () => void
  }>(),
  {
    focusOn: undefined,
    zIndex: 900,
    onBackdropClick: undefined,
  },
)

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

onMounted(() => document.addEventListener('pointerdown', rememberPressed, true))
onUnmounted(() => document.removeEventListener('pointerdown', rememberPressed, true))

function rememberPressed(event: PointerEvent) {
  if (!(event.target instanceof Element)) return
  const el = event.target.closest('button, [role="button"], a') ?? event.target
  lastPressed = { el, time: performance.now() }
}

/** The control that just opened the overlay; falls back to the focused one for keyboard users. */
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
  // styled the panel before --overlay-origin was set. Restart its transition so
  // it really starts from the button.
  box.style.transition = 'none'
  void box.offsetWidth
  box.style.transition = ''
}

/** Tells the CSS where the panel grows from and shrinks back to. */
function setOrigin(el: Element): HTMLElement | null {
  const overlay = el as HTMLElement
  const box = overlay.querySelector<HTMLElement>('.centered-overlay-panel')
  const button = origin?.isConnected ? origin.getBoundingClientRect() : undefined
  let transform = 'scale(0.92)'
  if (box && button?.width) {
    // offset* ignore transforms, so this is where the panel sits when fully open.
    const dx = button.left + button.width / 2 - (box.offsetLeft + box.offsetWidth / 2)
    const dy = button.top + button.height / 2 - (box.offsetTop + box.offsetHeight / 2)
    const sx = button.width / box.offsetWidth
    const sy = button.height / box.offsetHeight
    transform = `translate(${dx}px, ${dy}px) scale(${sx}, ${sy})`
  }
  overlay.style.setProperty('--overlay-origin', transform)
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
.centered-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 4vmin;
  /* clip, not hidden: focusing an input must not scroll the overlay. */
  overflow: clip;
}

.centered-overlay-backdrop {
  position: absolute;
  inset: 0;
  background: var(--overlay--backdrop--color);
}

.centered-overlay-backdrop--dismissible {
  cursor: zoom-out;
}

.centered-overlay-panel {
  position: relative;
  display: flex;
  outline: none;
}

/* Open: grow out of the button with a slight overshoot. */
.centered-overlay-enter-active .centered-overlay-panel {
  transition:
    transform 340ms cubic-bezier(0.2, 1.12, 0.36, 1),
    opacity 100ms linear;
}

/* Close: shrink back into the button, fading out at the very end. */
.centered-overlay-leave-active .centered-overlay-panel {
  transition:
    transform 240ms cubic-bezier(0.45, 0, 0.55, 1),
    opacity 100ms linear 140ms;
}

.centered-overlay-enter-from .centered-overlay-panel,
.centered-overlay-leave-to .centered-overlay-panel {
  transform: var(--overlay-origin);
  opacity: 0;
}

.centered-overlay-enter-active .centered-overlay-backdrop,
.centered-overlay-leave-active .centered-overlay-backdrop {
  transition: opacity 240ms;
}

.centered-overlay-enter-from .centered-overlay-backdrop,
.centered-overlay-leave-to .centered-overlay-backdrop {
  opacity: 0;
}

.centered-overlay-enter-active .centered-overlay-panel,
.centered-overlay-leave-active .centered-overlay-panel {
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .centered-overlay-enter-from .centered-overlay-panel,
  .centered-overlay-leave-to .centered-overlay-panel {
    transform: none;
  }
}
</style>
