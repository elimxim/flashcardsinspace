import { nextTick, onBeforeUpdate, onMounted, onUpdated, type Ref } from 'vue'

export interface FlipOptions {
  /** Animation duration in seconds (default: 0.45) */
  duration?: number
  /** Don't animate the updates that settle the initial layout (default: true). */
  skipInitialLayout?: boolean
}

const EASING = 'cubic-bezier(0.22, 1, 0.36, 1)'
const THRESHOLD_PX = 0.5

const prefersReducedMotion = () =>
  typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

/**
 * FLIP (First, Last, Invert, Play) animation
 * Smoothly animates elements with a 'flip-key' attribute when they change
 * position or size in the DOM. Sizes are animated as real width/height, not as
 * a scale, so the content reflows instead of being stretched. The elements are
 * expected to fill their slot (e.g. grid cells), as sizes animate towards 100%.
 */
export function useFlip(
  containerRef: Ref<HTMLElement | null | undefined>,
  options: FlipOptions = {},
) {
  const { duration = 0.45, skipInitialLayout = true } = options
  const positions = new Map<string, DOMRect>()
  const running = new Map<HTMLElement, Animation>()
  /** Whether an update is allowed to animate at all */
  let armed = true

  function flipElements(): HTMLElement[] {
    if (!containerRef.value) return []
    return Array.from(containerRef.value.querySelectorAll<HTMLElement>('[flip-key]'))
  }

  /**
   * Capture the current positions of all children with the 'flip-key' attribute
   */
  function capturePositions() {
    positions.clear()

    for (const element of flipElements()) {
      const key = element.getAttribute('flip-key')
      if (key) {
        // Includes a still running animation, which is exactly where the
        // element is right now and where the next one should start
        positions.set(key, element.getBoundingClientRect())
      }
    }
  }

  /**
   * Animate elements from their old positions and sizes to the new ones
   */
  function animateToNewPositions() {
    if (!armed || prefersReducedMotion()) {
      positions.clear()
      for (const animation of running.values()) animation.cancel()
      return
    }

    const elements = flipElements()

    // So that every element is measured at its new layout, not wherever a
    // running animation happens to have left it
    for (const element of elements) {
      running.get(element)?.cancel()
    }

    // All reads before any write: interleaving them makes the browser
    // re-layout once per element
    const moves: { element: HTMLElement; keyframes: Keyframe[] }[] = []

    for (const element of elements) {
      const key = element.getAttribute('flip-key')
      if (!key) continue

      const first = positions.get(key)
      if (!first) continue

      const last = element.getBoundingClientRect()
      const deltaX = first.left - last.left
      const deltaY = first.top - last.top
      const moved = Math.abs(deltaX) > THRESHOLD_PX || Math.abs(deltaY) > THRESHOLD_PX
      const resized =
        Math.abs(first.width - last.width) > THRESHOLD_PX ||
        Math.abs(first.height - last.height) > THRESHOLD_PX

      if (!moved && !resized) continue

      const from: Keyframe = { transform: `translate(${deltaX}px, ${deltaY}px)` }
      const to: Keyframe = { transform: 'translate(0, 0)' }
      if (resized) {
        from.width = `${first.width}px`
        from.height = `${first.height}px`
        // Not the measured px: while the container keeps resizing, the slot
        // keeps changing too, and a px target would leave the element too big
        // or too small (eating the gaps) until the animation ends
        to.width = '100%'
        to.height = '100%'
      }
      moves.push({ element, keyframes: [from, to] })
    }

    positions.clear()

    for (const { element, keyframes } of moves) {
      const animation = element.animate(keyframes, {
        duration: duration * 1000,
        easing: EASING,
      })
      running.set(element, animation)
      const forget = () => {
        if (running.get(element) === animation) running.delete(element)
      }
      animation.addEventListener('finish', forget)
      animation.addEventListener('cancel', forget)
    }
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
