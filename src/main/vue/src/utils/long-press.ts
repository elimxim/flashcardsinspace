import { type MaybeRefOrGetter, onUnmounted, ref, toValue } from 'vue'

interface LongPressOptions {
  holdTime: MaybeRefOrGetter<number>
  minDuration?: number
  canStart: () => boolean
  onLongPress: () => void
}

/**
 * Fires `onLongPress` once a press has been held for `holdTime` seconds (0 turns it off).
 */
export function useLongPress(options: LongPressOptions) {
  const { holdTime, minDuration = 250, canStart, onLongPress } = options
  const holding = ref(false)
  let holdTimeout: ReturnType<typeof setTimeout> | null = null
  let pressStartTime: number | null = null

  const enabled = () => toValue(holdTime) > 0

  function start(event: PointerEvent) {
    pressStartTime = null
    if (!enabled() || event.button !== 0 || holdTimeout !== null) return
    if (!canStart()) return
    pressStartTime = performance.now()
    holding.value = true
    holdTimeout = setTimeout(
      () => {
        holdTimeout = null
        holding.value = false
        onLongPress()
      },
      toValue(holdTime) * 1000,
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
    const consumed = pressStartTime !== null && performance.now() - pressStartTime >= minDuration
    pressStartTime = null
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
