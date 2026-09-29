import { type MaybeRefOrGetter, onUnmounted, ref, toValue } from 'vue'

/**
 * Fires `onLongPress` once a press has been held for `holdTime` seconds (0 turns it off).
 */
export function useLongPress(options: {
  holdTime: MaybeRefOrGetter<number>
  canStart: () => boolean
  onLongPress: () => void
}) {
  const holding = ref(false)
  let holdTimeout: ReturnType<typeof setTimeout> | null = null
  let longPressed = false

  const enabled = () => toValue(options.holdTime) > 0

  function start(event: PointerEvent) {
    longPressed = false
    if (!enabled() || event.button !== 0 || holdTimeout !== null) return
    if (!options.canStart()) return
    holding.value = true
    holdTimeout = setTimeout(
      () => {
        holdTimeout = null
        holding.value = false
        longPressed = true
        options.onLongPress()
      },
      toValue(options.holdTime) * 1000,
    )
  }

  function cancel() {
    if (holdTimeout) {
      clearTimeout(holdTimeout)
      holdTimeout = null
    }
    holding.value = false
  }

  function consumeClick(): boolean {
    const consumed = longPressed
    longPressed = false
    return consumed
  }

  onUnmounted(cancel)

  return {
    holding,
    consumeClick,
    listeners: {
      pointerdown: start,
      pointerup: cancel,
      pointerleave: cancel,
      pointercancel: cancel,
      contextmenu: (event: Event) => {
        if (enabled()) event.preventDefault()
      },
    },
  }
}
