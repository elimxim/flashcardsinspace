import {
  computed,
  onMounted,
  onUnmounted,
  ref,
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
}

export function useSwipe(options: SwipeOptions) {
  const {
    element,
    threshold = 100,
    velocityThreshold = 0.3,
    maxRotation = 15,
    animationSpeed = 2.5,
    enabled = true,
    canSwipeLeft = true,
    canSwipeRight = true,
    onSwipeLeft = () => {},
    onSwipeRight = () => {},
  } = options

  function getExitOffset(): number {
    return window.innerWidth * 2
  }

  function getAnimationDuration(): number {
    return Math.round(getExitOffset() / animationSpeed)
  }

  let touchStartX = 0
  let touchStartY = 0
  let touchStartTime = 0
  let isSwiping = false

  const swipeOffset = ref(0)
  const isSwipeActive = ref(false)
  const isAnimatingOut = ref(false)

  function canSwipe() {
    return toValue(enabled) && (toValue(canSwipeLeft) || toValue(canSwipeRight))
  }

  const swipeStyle = computed(() => {
    if (swipeOffset.value === 0 && !isSwipeActive.value && !isAnimatingOut.value) return {}
    const rotation = maxRotation > 0 ? (swipeOffset.value / 200) * maxRotation : 0
    return {
      transform: `translateX(${swipeOffset.value}px)${rotation ? ` rotate(${rotation}deg)` : ''}`,
      transition: isSwipeActive.value ? 'none' : `transform ${getAnimationDuration()}ms cubic-bezier(0.33, 0, 0.2, 1)`,
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

    // If horizontal movement is greater than vertical, it's a swipe attempt
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
      isSwiping = true
      swipeOffset.value = deltaX
      event.preventDefault() // Prevent scrolling when swiping
    }
  }

  function onTouchEnd(event: TouchEvent) {
    isSwipeActive.value = false

    if (!canSwipe()) {
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
      // Animate card off-screen in the swipe direction, then trigger callback
      isAnimatingOut.value = true
      const exitOffset = getExitOffset()
      const animationDuration = getAnimationDuration()
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
        }, animationDuration)
      })
    } else if (isSwiping) {
      // Invalid swipe - snap back with animation
      isAnimatingOut.value = true
      const animationDuration = getAnimationDuration()
      requestAnimationFrame(() => {
        swipeOffset.value = 0
        setTimeout(() => {
          isAnimatingOut.value = false
        }, animationDuration)
      })
    } else {
      swipeOffset.value = 0
    }

    isSwiping = false
  }

  onMounted(() => {
    const el = element.value
    if (el) {
      el.addEventListener('touchstart', onTouchStart)
      el.addEventListener('touchmove', onTouchMove, { passive: false })
      el.addEventListener('touchend', onTouchEnd)
    }
  })

  onUnmounted(() => {
    const el = element.value
    if (el) {
      el.removeEventListener('touchstart', onTouchStart)
      el.removeEventListener('touchmove', onTouchMove)
      el.removeEventListener('touchend', onTouchEnd)
    }
  })

  return {
    swipeStyle,
    swipeOffset,
    isSwipeActive,
    isAnimatingOut,
  }
}

