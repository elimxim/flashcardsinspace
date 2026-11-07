<template>
  <Transition :css="false" @enter="onEnter" @leave="onLeave" @after-leave="onAfterLeave">
    <div v-if="visible" class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1">
      <div class="modal-backdrop" />
      <div ref="panel" class="modal-panel modal-window touch-none" tabindex="-1">
        <slot name="header" />
        <slot />
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { originTransform } from '@/utils/modal-motion.ts'
import { takeModalOrigin, trackModalOrigins, visibleBox } from '@/utils/modal-origin.ts'
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

const props = defineProps<{
  visible: boolean
  focusOn?: HTMLElement
}>()

const EASE_OUT_SOFT_OVERSHOOT = 'cubic-bezier(0.2, 1.12, 0.36, 1)'
const EASE_IN_OUT = 'cubic-bezier(0.45, 0, 0.55, 1)'

const GROW_MS = 340
const SHRINK_MS = 240
const POP_MS = 220

const panel = ref<HTMLDivElement>()

let origin: Element | undefined

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    origin = takeModalOrigin()
    nextTick().then(() => {
      const target = props.focusOn ?? panel.value
      target?.focus({ preventScroll: true })
    })
  },
)

onMounted(trackModalOrigins)

function contentOf(panel: HTMLElement): HTMLElement[] {
  return Array.from(panel.children) as HTMLElement[]
}

function enterAnimations({ overlay, backdrop, panel }: ModalParts): Animation[] {
  if (prefersReducedMotion()) return fade(overlay, true)

  const from = visibleBox(origin)
  const growing = from
    ? play(
        panel,
        [
          { transform: originTransform(from, panel.getBoundingClientRect()), opacity: 0 },
          { opacity: 1, offset: 0.3 },
          { transform: 'none', opacity: 1 },
        ],
        { duration: GROW_MS, easing: EASE_OUT_SOFT_OVERSHOOT },
      )
    : play(
        panel,
        [
          { transform: 'scale(0.92)', opacity: 0 },
          { transform: 'none', opacity: 1 },
        ],
        { duration: POP_MS, easing: EASE_OUT },
      )

  return [
    play(backdrop, [{ opacity: 0 }, { opacity: 1 }], { duration: GROW_MS * 0.8 }),
    growing,
    // Content shows up once the window is big enough not to look squashed.
    ...contentOf(panel).map((el) =>
      play(el, [{ opacity: 0 }, { opacity: 1 }], {
        duration: GROW_MS * 0.55,
        delay: GROW_MS * 0.3,
        easing: 'ease-out',
      }),
    ),
  ]
}

function leaveAnimations({ overlay, backdrop, panel }: ModalParts): Animation[] {
  const backdropNow = freeze(backdrop)
  const panelNow = freeze(panel)
  const content = contentOf(panel)
  const contentNow = content.map(freeze)

  if (prefersReducedMotion()) return fade(overlay, false)

  const to = visibleBox(origin)
  const shrinking = to
    ? play(
        panel,
        [
          { transform: panelNow.transform, opacity: panelNow.opacity },
          { opacity: panelNow.opacity, offset: 0.55 },
          { transform: originTransform(to, panel.getBoundingClientRect()), opacity: 0 },
        ],
        { duration: SHRINK_MS, easing: EASE_IN_OUT },
      )
    : play(
        panel,
        [
          { transform: panelNow.transform, opacity: panelNow.opacity },
          { transform: 'scale(0.94)', opacity: 0 },
        ],
        { duration: POP_MS * 0.8, easing: EASE_IN },
      )

  return [
    play(backdrop, [{ opacity: backdropNow.opacity }, { opacity: 0 }], { duration: SHRINK_MS }),
    shrinking,
    ...content.map((el, i) =>
      play(el, [{ opacity: contentNow[i]?.opacity ?? 1 }, { opacity: 0 }], {
        duration: SHRINK_MS * 0.35,
        easing: 'ease-in',
      }),
    ),
  ]
}

async function onEnter(el: Element, done: () => void) {
  const parts = modalParts(el)
  if (!parts || !(await finish(enterAnimations(parts)))) return
  done()
}

async function onLeave(el: Element, done: () => void) {
  const parts = modalParts(el)
  if (parts) await finish(leaveAnimations(parts))
  done()
}

function onAfterLeave() {
  // Give focus back to whatever opened the modal, if it can take it.
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
</style>
