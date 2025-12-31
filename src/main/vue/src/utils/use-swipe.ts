import {
  computed,
  onUnmounted,
  ref,
  watch,
  type MaybeRefOrGetter,
  type Ref,
  toValue,
} from 'vue'

export interface SwipeOptions {
  /** Element to attach swipe listeners to */
  element: Ref<HTMLElement | undefined>
  /** Minimum distance in pixels to trigger swipe */
  threshold?: number
  /** Minimum velocity in px/ms to trigger swipe */
  velocityThreshold?: number
  /** Maximum rotation in degrees during swipe, 0 disables rotation */
  maxRotation?: number
  /** Animation speed in pixels per millisecond */
  animationSpeed?: number
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
  /**
   * Tape mode: instead of animating off-screen, snaps back after triggering callback.
   * Useful for carousel/tape-like navigation where content slides in from sides.
   */
  tapeMode?: boolean
  /** Snap-back animation duration in ms for tape mode */
  tapeSnapDuration?: number


}

export function useSwipe(options: SwipeOptions) {
  const {
    element,
    threshold = 100,
    velocityThreshold = 0.3,
    maxRotation = 5,
    animationSpeed = 1.5,
    enabled = true,
    canSwipeLeft = true,
    canSwipeRight = true,
    onSwipeLeft = () => {},
    onSwipeRight = () => {},
    tapeMode = false,
    tapeSnapDuration = 200,
  } = options

  function getExitOffset(): number {
    const el = element.value
    const elementWidth = el?.offsetWidth ?? 0
    return window.innerWidth + elementWidth
  }

  function getAnimationDuration(): number {
    return Math.round(getExitOffset() / animationSpeed)
  }

  /** Time until the element is no longer visible (exits viewport) */
  function getInvisibleDuration(direction: 'left' | 'right'): number {
    const el = element.value
    if (!el) return getAnimationDuration()

    const rect = el.getBoundingClientRect()
    // Distance until the element is fully off-screen
    const distance = direction === 'right'
      ? window.innerWidth - rect.left
      : rect.right

    return Math.round(distance / animationSpeed)
  }

  let touchStartX = 0
  let touchStartY = 0
  let touchStartTime = 0
  let isSwiping = false

  const swipeOffset = ref(0)
  const fingerOffset = ref(0)
  const isSwipeActive = ref(false)
  const isAnimatingOut = ref(false)
  /** Direction of pending/active swipe: 'left', 'right', or null */
  const swipeDirection = ref<'left' | 'right' | null>(null)

  function canSwipe() {
    return toValue(enabled) && (toValue(canSwipeLeft) || toValue(canSwipeRight))
  }

  const swipeStyle = computed(() => {
    if (swipeOffset.value === 0 && !isSwipeActive.value && !isAnimatingOut.value) return {}
    const rotation = maxRotation > 0 ? (swipeOffset.value / 200) * maxRotation : 0
    const transitionDuration = tapeMode
      ? tapeSnapDuration
      : getAnimationDuration()

    return {
      transform: `translateX(${swipeOffset.value}px)${rotation ? ` rotate(${rotation}deg)` : ''}`,
      transition: isSwipeActive.value ? 'none' : `transform ${transitionDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
    }
  })

  const fingerProgress = computed(() => {
    const progress = fingerOffset.value / threshold
    return Math.max(-1, Math.min(1, progress))
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
    fingerOffset.value = 0
  }

  function onTouchMove(event: TouchEvent) {
    if (!canSwipe()) return
    const touch = event.touches[0]
    const deltaX = touch.clientX - touchStartX
    const deltaY = touch.clientY - touchStartY

    // Once swiping, always update the offset smoothly
    if (isSwiping) {
      swipeOffset.value = deltaX
      fingerOffset.value = deltaX
      if (event.cancelable) {
        event.preventDefault()
      }
      return
    }

    // If horizontal movement is greater than vertical, it's a swipe attempt
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
      isSwiping = true
      swipeOffset.value = deltaX
      fingerOffset.value = deltaX
      if (event.cancelable) {
        event.preventDefault() // Prevent scrolling when swiping
      }
    }
  }

  function onTouchEnd(event: TouchEvent) {
    fingerOffset.value = 0

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
      if (tapeMode) {
        // Tape mode: animate current content out, then call callback
        const el = element.value
        const width = el?.offsetWidth ?? 300
        const gap = 4
        // Animate to full exit position
        const exitOffset = deltaX > 0 ? width + gap : -width - gap

        isSwipeActive.value = false
        isAnimatingOut.value = true
        swipeOffset.value = exitOffset

        setTimeout(() => {
          // After animation, call callback and reset
          if (deltaX > 0) {
            onSwipeRight()
          } else {
            onSwipeLeft()
          }
          // Snap back to center without animation
          isSwipeActive.value = true
          swipeOffset.value = 0
          requestAnimationFrame(() => {
            isSwipeActive.value = false
            isAnimatingOut.value = false
          })
        }, tapeSnapDuration)
      } else {
        // Card mode: animate off-screen, then trigger callback
        isSwipeActive.value = false
        isAnimatingOut.value = true
        const direction = deltaX > 0 ? 'right' : 'left'
        const exitOffset = getExitOffset()
        const invisibleDuration = getInvisibleDuration(direction)
        const offset = deltaX > 0 ? exitOffset : -exitOffset
        requestAnimationFrame(() => {
          swipeOffset.value = offset
          setTimeout(() => {
            swipeOffset.value = 0
            isAnimatingOut.value = false
            if (deltaX > 0) {
              onSwipeRight()
            } else {
              onSwipeLeft()
            }
          }, invisibleDuration)
        })
      }
    } else if (isSwiping) {
      // Invalid swipe - snap back with animation
      isSwipeActive.value = false
      isAnimatingOut.value = true
      const snapDuration = tapeMode ? tapeSnapDuration : getAnimationDuration()
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
   * In tape mode, animates from the opposite edge and calls the callback.
   */
  function triggerSwipe(direction: 'left' | 'right') {
    if (isAnimatingOut.value) return

    const el = element.value
    if (!el) {
      // No element, just call callback
      if (direction === 'left') {
        onSwipeLeft()
      } else {
        onSwipeRight()
      }
      return
    }

    const width = el.offsetWidth
    const exitOffset = direction === 'left' ? -width : width

    // Set direction BEFORE animation so the adjacent month can position correctly
    swipeDirection.value = direction
    isSwipeActive.value = false
    isAnimatingOut.value = true

    // Use RAF to ensure the direction is set before offset changes
    requestAnimationFrame(() => {
      swipeOffset.value = exitOffset

      setTimeout(() => {
        // After animation, call callback and reset
        if (direction === 'left') {
          onSwipeLeft()
        } else {
          onSwipeRight()
        }
        // Snap back to center without animation
        isSwipeActive.value = true
        swipeOffset.value = 0
        swipeDirection.value = null
        requestAnimationFrame(() => {
          isSwipeActive.value = false
          isAnimatingOut.value = false
        })
      }, tapeSnapDuration)
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
    swipeStyle,
    swipeOffset,
    fingerOffset,
    fingerProgress,
    isSwipeActive,
    isAnimatingOut,
    swipeDirection,
    triggerSwipe,
  }
}

