import {
  computed,
  onUnmounted,
  ref,
  watch,
  type MaybeRefOrGetter,
  type Ref,
  toValue,
} from 'vue'

export interface TapeOptions {
  /** Element to attach swipe listeners to */
  element: Ref<HTMLElement | undefined>
  /** Minimum distance in pixels to trigger swipe */
  threshold?: number
  /** Minimum velocity in px/ms to trigger swipe */
  velocityThreshold?: number
  /** Snap-back animation duration in ms */
  snapDuration?: number
  /** Width of a single page (for calculating exit offset). If not provided, uses element width. */
  pageWidth?: MaybeRefOrGetter<number>
  /** Gap between pages in pixels */
  pageGap?: number
  /** Whether swipe is enabled */
  enabled?: MaybeRefOrGetter<boolean>
  /** Whether the left swipe is allowed */
  canSwipeLeft?: MaybeRefOrGetter<boolean>
  /** Whether the right swipe is allowed */
  canSwipeRight?: MaybeRefOrGetter<boolean>
  /** Callback when swiped left */
  onSwipeLeft?: () => void
  /** Callback when swiped right */
  onSwipeRight?: () => void
}

/**
 * Composable for tape/carousel-like navigation.
 * Instead of animating off-screen, snaps back after triggering callback.
 * Useful for carousel/tape-like navigation where content slides in from sides.
 */
export function useTape(options: TapeOptions) {
  const {
    element,
    threshold = 100,
    velocityThreshold = 0.3,
    snapDuration = 200,
    pageWidth,
    pageGap = 4,
    enabled = true,
    canSwipeLeft = true,
    canSwipeRight = true,
    onSwipeLeft = () => {},
    onSwipeRight = () => {},
  } = options

  function getPageWidth(): number {
    if (pageWidth !== undefined) {
      return toValue(pageWidth)
    }
    return element.value?.offsetWidth ?? 300
  }

  let touchStartX = 0
  let touchStartY = 0
  let touchStartTime = 0
  let isSwiping = false

  const swipeOffset = ref(0)
  const isSwipeActive = ref(false)
  const isAnimatingOut = ref(false)
  /** Direction of pending/active swipe: 'left', 'right', or null */
  const swipeDirection = ref<'left' | 'right' | null>(null)

  function canSwipe() {
    return toValue(enabled) && (toValue(canSwipeLeft) || toValue(canSwipeRight))
  }

  const tapeStyle = computed(() => {
    // Add base offset to show the middle page
    const baseOffset = -getPageWidth() - pageGap
    const totalOffset = baseOffset + swipeOffset.value

    if (totalOffset === 0 && !isSwipeActive.value && !isAnimatingOut.value) return {}

    return {
      transform: `translateX(${totalOffset}px)`,
      transition: isSwipeActive.value ? 'none' : `transform ${snapDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
    }
  })

  function onTouchStart(event: TouchEvent) {
    if (!canSwipe()) return
    const touch = event.touches[0]
    touchStartX = touch.clientX
    touchStartY = touch.clientY
    touchStartTime = Date.now()
    isSwiping = false
    isSwipeActive.value = true
    isAnimatingOut.value = false
    swipeOffset.value = 0
  }

  function onTouchMove(event: TouchEvent) {
    if (!canSwipe()) return
    const touch = event.touches[0]
    const deltaX = touch.clientX - touchStartX
    const deltaY = touch.clientY - touchStartY

    // Once swiping, always update the offset smoothly
    if (isSwiping) {
      swipeOffset.value = deltaX
      if (event.cancelable) {
        event.preventDefault()
      }
      return
    }

    // If horizontal movement is greater than vertical, it's a swipe attempt
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
      isSwiping = true
      swipeOffset.value = deltaX
      if (event.cancelable) {
        event.preventDefault() // Prevent scrolling when swiping
      }
    }
  }

  function onTouchEnd(event: TouchEvent) {
    if (!canSwipe()) {
      isSwipeActive.value = false
      swipeOffset.value = 0
      return
    }

    const touch = event.changedTouches[0]
    const deltaX = touch.clientX - touchStartX
    const deltaTime = Date.now() - touchStartTime
    const velocity = Math.abs(deltaX) / deltaTime

    const meetsThreshold = Math.abs(deltaX) > threshold || velocity > velocityThreshold
    const isSwipeLeft = deltaX < 0
    const directionAllowed = isSwipeLeft ? toValue(canSwipeLeft) : toValue(canSwipeRight)
    const isValidSwipe = isSwiping && meetsThreshold && directionAllowed

    if (isValidSwipe) {
      // Animate to show next/prev page, then call callback and reset
      const width = getPageWidth()
      // Animate to full page position
      const exitOffset = deltaX > 0 ? width + pageGap : -width - pageGap

      isAnimatingOut.value = true
      // Use RAF to ensure transition is applied before changing offset
      requestAnimationFrame(() => {
        isSwipeActive.value = false
        swipeOffset.value = exitOffset

        setTimeout(() => {
          // Reset offset with transitions disabled
          isSwipeActive.value = true
          swipeOffset.value = 0
          // Call callback - content will update but position stays centered
          if (deltaX > 0) {
            onSwipeRight()
          } else {
            onSwipeLeft()
          }
          // Wait for Vue to settle before re-enabling transitions
          setTimeout(() => {
            isSwipeActive.value = false
            isAnimatingOut.value = false
          }, 20)
        }, snapDuration)
      })
    } else if (isSwiping) {
      // Invalid swipe - snap back with animation
      isSwipeActive.value = false
      isAnimatingOut.value = true
      requestAnimationFrame(() => {
        swipeOffset.value = 0
        setTimeout(() => {
          isAnimatingOut.value = false
        }, snapDuration)
      })
    } else {
      isSwipeActive.value = false
      swipeOffset.value = 0
    }

    isSwiping = false
  }

  /**
   * Programmatically trigger a swipe animation (for button navigation).
   * Animates to show next/prev page and calls the callback.
   */
  function triggerSwipe(direction: 'left' | 'right') {
    if (isAnimatingOut.value) return

    const width = getPageWidth()
    const exitOffset = direction === 'left' ? -width - pageGap : width + pageGap

    swipeDirection.value = direction
    isAnimatingOut.value = true

    requestAnimationFrame(() => {
      isSwipeActive.value = false
      swipeOffset.value = exitOffset

      setTimeout(() => {
        // Reset offset with transitions disabled
        isSwipeActive.value = true
        swipeOffset.value = 0
        swipeDirection.value = null
        // Call callback
        if (direction === 'left') {
          onSwipeLeft()
        } else {
          onSwipeRight()
        }
        // Wait for Vue to settle before re-enabling transitions
        setTimeout(() => {
          isSwipeActive.value = false
          isAnimatingOut.value = false
        }, 20)
      }, snapDuration)
    })
  }

  function attachListeners(el: HTMLElement) {
    el.addEventListener('touchstart', onTouchStart)
    el.addEventListener('touchmove', onTouchMove, { passive: false })
    el.addEventListener('touchend', onTouchEnd)
  }

  function detachListeners(el: HTMLElement) {
    el.removeEventListener('touchstart', onTouchStart)
    el.removeEventListener('touchmove', onTouchMove)
    el.removeEventListener('touchend', onTouchEnd)
  }

  // Watch for element changes to attach/detach listeners
  watch(element, (newEl, oldEl) => {
    if (oldEl) {
      detachListeners(oldEl)
    }
    if (newEl) {
      attachListeners(newEl)
    }
  }, { immediate: true })

  onUnmounted(() => {
    const el = element.value
    if (el) {
      detachListeners(el)
    }
  })

  return {
    tapeStyle,
    swipeOffset,
    isSwipeActive,
    isAnimatingOut,
    swipeDirection,
    triggerSwipe,
  }
}
