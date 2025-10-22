import { nextTick, onBeforeUpdate, onUpdated, type Ref } from 'vue'

/**
 * FLIP (First, Last, Invert, Play) animation
 * Smoothly animates elements when they change position in the DOM
 */
export function useFlip(containerRef: Ref<HTMLElement | null | undefined>) {
  const positions = new Map<string, DOMRect>()
  let isAnimating = false

  /**
   * Capture the current positions of all children with the 'flip-key' attribute
   */
  function capturePositions() {
    if (!containerRef.value) return

    positions.clear()
    const elements = containerRef.value.querySelectorAll('[flip-key]') as NodeListOf<HTMLElement>

    for (const element of elements) {
      const key = element.getAttribute('flip-key')
      if (key) {
        positions.set(key, element.getBoundingClientRect())
      }
    }
  }

  /**
   * Animate elements from their old positions to new positions
   */
  function animateToNewPositions() {
    if (!containerRef.value || isAnimating) return

    const elements = containerRef.value.querySelectorAll('[flip-key]') as NodeListOf<HTMLElement>
    let hasAnimations = false

    for (const element of elements) {
      const key = element.getAttribute('flip-key')
      if (!key) continue

      const first = positions.get(key)
      const last = element.getBoundingClientRect()

      if (first) {
        const deltaX = first.left - last.left
        const deltaY = first.top - last.top

        // Only animate if the position actually changed
        if (Math.abs(deltaX) > 0.5 || Math.abs(deltaY) > 0.5) {
          hasAnimations = true

          // Invert: Apply transform to make it look like it's still in the old position
          element.style.transform = `translate(${deltaX}px, ${deltaY}px)`
          element.style.transition = 'none'

          // Force reflow
          element.offsetHeight

          // Play: Transition to the new position
          requestAnimationFrame(() => {
            element.style.transition = 'transform 0.5s cubic-bezier(0.25, 1, 0.5, 1)'
            element.style.transform = 'translate(0, 0)'
          })
        }
      }
    }

    if (hasAnimations) {
      isAnimating = true
      setTimeout(() => {
        isAnimating = false
      }, 500) // Match transition duration
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
    onBeforeUpdate(() => {
      capturePositions()
    })

    onUpdated(() => {
      nextTick().then(() => {
        animateToNewPositions()
      })
    })
  }

  return {
    capturePositions,
    animateToNewPositions,
    flip,
    setupAuto,
  }
}

