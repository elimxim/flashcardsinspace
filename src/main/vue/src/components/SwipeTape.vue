<template>
  <div ref="swipeTape" class="swipe-tape">
    <div
      ref="trackRef"
      class="tape-track"
      :style="tapeStyle"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <div
        v-for="(frame, index) in frames"
        :key="index"
        class="tape-frame"
      >
        <slot :frame="frame" :index="index"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T">
import { computed, ref } from 'vue'

const props = withDefaults(defineProps<{
  frames: T[]
  snapDuration?: number
  threshold?: number
  velocityThreshold?: number
  frameGap?: number
  frameKey?: (frame: T, index: number) => string
  onSwipeLeft?: () => void
  onSwipeRight?: () => void
}>(), {
  snapDuration: 200,
  threshold: 100,
  velocityThreshold: 0.3,
  frameGap: 10,
  frameKey: (_: T, index: number) => `${index}`,
  onSwipeLeft: () => {
  },
  onSwipeRight: () => {
  },
})

const swipeTape = ref<HTMLElement>()
const trackRef = ref<HTMLElement>()
const frameGapPx = computed(() => `${props.frameGap}px`)
const doubleFrameGapPx = computed(() => `${props.frameGap * 2}px`)

const frameWidth = computed(() => swipeTape.value?.offsetWidth ?? 300)

let touchStartX = 0
let touchStartY = 0
let touchStartTime = 0
let isSwiping = false

const swipeOffset = ref(0)
const isSwipeActive = ref(false)
const isAnimatingOut = ref(false)

const tapeStyle = computed(() => {
  // Add base offset to show the middle frame
  const baseOffset = -frameWidth.value - props.frameGap
  const totalOffset = baseOffset + swipeOffset.value

  if (totalOffset === 0 && !isSwipeActive.value && !isAnimatingOut.value) return {}

  return {
    transform: `translateX(${totalOffset}px)`,
    transition: isSwipeActive.value ? 'none' : `transform ${props.snapDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
  }
})

function onTouchStart(event: TouchEvent) {
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
      event.preventDefault()
    }
  }
}

function onTouchEnd(event: TouchEvent) {
  const touch = event.changedTouches[0]
  const deltaX = touch.clientX - touchStartX
  const deltaTime = Date.now() - touchStartTime
  const velocity = Math.abs(deltaX) / deltaTime

  const meetsThreshold = Math.abs(deltaX) > props.threshold || velocity > props.velocityThreshold
  const isValidSwipe = isSwiping && meetsThreshold

  if (isValidSwipe) {
    // Animate to show the next or prev frame, then call callback and reset
    const width = frameWidth.value
    const exitOffset = deltaX > 0 ? width + props.frameGap : -width - props.frameGap

    isAnimatingOut.value = true
    requestAnimationFrame(() => {
      isSwipeActive.value = false
      swipeOffset.value = exitOffset

      setTimeout(() => {
        // Reset offset with transitions disabled
        isSwipeActive.value = true
        swipeOffset.value = 0
        // Call callback
        if (deltaX > 0) {
          props.onSwipeRight?.()
        } else {
          props.onSwipeLeft?.()
        }
        // Wait for Vue to settle before re-enabling transitions
        setTimeout(() => {
          isSwipeActive.value = false
          isAnimatingOut.value = false
        }, 20)
      }, props.snapDuration)
    })
  } else if (isSwiping) {
    // Invalid swipe - snap back with animation
    isSwipeActive.value = false
    isAnimatingOut.value = true
    requestAnimationFrame(() => {
      swipeOffset.value = 0
      setTimeout(() => {
        isAnimatingOut.value = false
      }, props.snapDuration)
    })
  } else {
    isSwipeActive.value = false
    swipeOffset.value = 0
  }

  isSwiping = false
}

function triggerSwipe(direction: 'left' | 'right') {
  if (isAnimatingOut.value) return

  const width = frameWidth.value
  const exitOffset = direction === 'left'
    ? -width - props.frameGap
    : width + props.frameGap

  isAnimatingOut.value = true

  requestAnimationFrame(() => {
    isSwipeActive.value = false
    swipeOffset.value = exitOffset

    setTimeout(() => {
      // Reset offset with transitions disabled
      isSwipeActive.value = true
      swipeOffset.value = 0
      if (direction === 'left') {
        props.onSwipeLeft?.()
      } else {
        props.onSwipeRight?.()
      }
      // Wait for Vue to settle before re-enabling transitions
      setTimeout(() => {
        isSwipeActive.value = false
        isAnimatingOut.value = false
      }, 100)
    }, props.snapDuration)
  })
}

defineExpose({
  triggerSwipe,
})
</script>

<style scoped>
.swipe-tape {
  flex: 1;
  position: relative;
  min-height: 0;
}

.tape-track {
  position: absolute;
  inset: 0;
  display: flex;
  gap: v-bind(frameGapPx);
  width: calc(300% + v-bind(doubleFrameGapPx));
  will-change: transform;
}

.tape-frame {
  flex: 1;
  min-width: 0;
}

</style>

