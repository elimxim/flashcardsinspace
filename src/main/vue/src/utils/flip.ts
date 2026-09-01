import { nextTick, onBeforeUpdate, onMounted, onUpdated, type Ref } from 'vue'

export interface FlipOptions {
  /** Transform duration in seconds (default: 0.5) */
  duration?: number
  /** Don't animate the updates that settle the initial layout (default: true). */
  skipInitialLayout?: boolean
}

interface Move {
  element: HTMLElement
  deltaX: number
  deltaY: number
}

/**
 * FLIP (First, Last, Invert, Play) animation
 * Smoothly animates elements when they change position in the DOM
 */
export function useFlip(
  containerRef: Ref<HTMLElement | null | undefined>,
  options: FlipOptions = {},
) {
  const { duration = 0.5, skipInitialLayout = true } = options
  const positions = new Map<string, DOMRect>()
  const pendingCleanups = new WeakMap<HTMLElement, () => void>()
  /** Whether an update is allowed to animate at all */
  let armed = true

  function flipElements(): HTMLElement[] {
    if (!containerRef.value) return []
    return Array.from(containerRef.value.querySelectorAll<HTMLElement>('[flip-key]'))
  }

  /**
   * Drop the inline styles an animation leaves behind, so no element keeps a
   * stray transform (which would make it a containing block and a stacking
   * context) or a transition that other transforms would inherit
   */
  function clearInlineStyles(element: HTMLElement) {
    pendingCleanups.get(element)?.()
    element.style.transition = ''
    element.style.transform = ''
  }

  function cleanUpAfterTransition(element: HTMLElement) {
    const stopListening = () => {
      element.removeEventListener('transitionend', onTransitionEnd)
      pendingCleanups.delete(element)
    }

    const onTransitionEnd = (event: TransitionEvent) => {
      if (event.target !== element || event.propertyName !== 'transform') return
      stopListening()
      element.style.transition = ''
      element.style.transform = ''
    }

    pendingCleanups.set(element, stopListening)
    element.addEventListener('transitionend', onTransitionEnd)
  }

  /**
   * Capture the current positions of all children with the 'flip-key' attribute
   */
  function capturePositions() {
    positions.clear()

    for (const element of flipElements()) {
      const key = element.getAttribute('flip-key')
      if (key) {
        // Includes the transform of a still running animation, which is exactly
        // where the element is right now and where the next one should start
        positions.set(key, element.getBoundingClientRect())
      }
    }
  }

  /**
   * Animate elements from their old positions to new positions
   */
  function animateToNewPositions() {
    if (!armed) {
      positions.clear()
      return
    }

    const elements = flipElements()

    // Reads and writes are kept in separate passes: interleaving them makes the
    // browser re-layout once per element
    for (const element of elements) {
      // So that every element is measured at its new layout position, not
      // wherever a running animation happens to have left it
      clearInlineStyles(element)
    }

    const moves: Move[] = []

    for (const element of elements) {
      const key = element.getAttribute('flip-key')
      if (!key) continue

      const first = positions.get(key)
      if (!first) continue

      const last = element.getBoundingClientRect()
      const deltaX = first.left - last.left
      const deltaY = first.top - last.top

      // Only animate if the position actually changed
      if (Math.abs(deltaX) <= 0.5 && Math.abs(deltaY) <= 0.5) continue

      moves.push({ element, deltaX, deltaY })
    }

    positions.clear()
    if (moves.length === 0) return

    // Invert: Apply transforms to make the elements look like they are still in
    // their old positions
    for (const { element, deltaX, deltaY } of moves) {
      element.style.transition = 'none'
      element.style.transform = `translate(${deltaX}px, ${deltaY}px)`
    }

    // Force a single reflow so the inverted positions are committed before the
    // transitions are attached
    void containerRef.value?.offsetHeight

    // Play: Transition to the new positions
    requestAnimationFrame(() => {
      for (const { element } of moves) {
        element.style.transition = `transform ${duration}s cubic-bezier(0.25, 1, 0.5, 1)`
        element.style.transform = 'translate(0, 0)'
        cleanUpAfterTransition(element)
      }
    })
  }

  /**
   * Perform FLIP animation
   * Call this before a layout change that will move elements
   */
  async function flip() {
    capturePositions()
    await nextTick()
    animateToNewPositions()
  }

  /**
   * Set up automatic FLIP on updates
   * Use with onBeforeUpdate and onUpdated lifecycle hooks
   */
  function setupAuto() {
    if (skipInitialLayout) {
      armed = false
      onMounted(() => {
        // Resolves once the updates triggered from the mounted hooks - the
        // container measuring itself, and whatever follows from it - have been
        // flushed
        nextTick().then(() => {
          armed = true
        })
      })
    }

    onBeforeUpdate(() => {
      capturePositions()
    })

    onUpdated(() => {
      animateToNewPositions()
    })
  }

  return {
    capturePositions,
    animateToNewPositions,
    flip,
    setupAuto,
  }
}
