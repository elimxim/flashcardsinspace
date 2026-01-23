<template>
  <div class="swipe-tape-wrapper">
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
      <div
        v-if="showProgress"
        class="tape-progress"
        :class="{
        'tape-progress--light': progressTheme === 'light',
        'tape-progress--dark': progressTheme === 'dark',
      }"
      >
        <p>{{ tapeProgress }}</p>
      </div>
      <div
        v-if="showNavigation"
        class="tape-navigation tape-navigation--left"
        :class="{
        'tape-navigation--light': navigationTheme === 'light',
        'tape-navigation--dark': navigationTheme === 'dark',
      }"
      >
        <div class="navigation-button" @click.stop="triggerSwipe('right')">
          <font-awesome-icon icon="fa-solid fa-chevron-left" class="navigation-icon"/>
        </div>
      </div>
      <div
        v-if="showNavigation"
        class="tape-navigation tape-navigation--right"
        :class="{
        'tape-navigation--light': navigationTheme === 'light',
        'tape-navigation--dark': navigationTheme === 'dark',
      }"
      >
        <div class="navigation-button" @click.stop="triggerSwipe('left')">
          <font-awesome-icon icon="fa-solid fa-chevron-right" class="navigation-icon"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T">
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = withDefaults(defineProps<{
  frames: T[]
  snapDuration?: number
  threshold?: number
  velocityThreshold?: number
  frameGap?: number
  onSwipeLeft?: () => void
  onSwipeRight?: () => void
  dynamicTape?: boolean
  showMiddleFrame?: boolean
  showProgress?: boolean
  progressTheme?: 'light' | 'dark'
  showNavigation?: boolean
  navigationTheme?: 'light' | 'dark'
  loopedTape?: boolean
}>(), {
  snapDuration: 200,
  threshold: 100,
  velocityThreshold: 0.3,
  frameGap: 10,
  onSwipeLeft: () => {
  },
  onSwipeRight: () => {
  },
  dynamicTape: false,
  showMiddleFrame: false,
  showProgress: false,
  progressTheme: 'light',
  showNavigation: false,
  navigationTheme: 'light',
  loopedTape: false,
})

const swipeTape = ref<HTMLElement>()
const trackRef = ref<HTMLElement>()
const resizeObserver = ref<ResizeObserver>()
const frameNumber = ref(1)
const totalFrames = computed(() => props.frames.length)
const tapeProgress = computed(() => `${frameNumber.value} / ${totalFrames.value}`)
const frameGapPx = computed(() => `${props.frameGap}px`)
const frameWidth = ref(swipeTape.value?.clientWidth ?? 0)

let touchStartX = 0
let touchStartY = 0
let touchStartTime = 0
let isSwiping = false

const swipeOffset = ref(0)
const isSwipeActive = ref(false)
const isAnimatingOut = ref(false)

const tapeStyle = computed(() => {
  let baseOffset = 0
  if (props.showMiddleFrame) {
    const halfFrames = Math.floor(totalFrames.value / 2)
    baseOffset -= frameWidth.value * halfFrames + props.frameGap * halfFrames
  }

  let framesOffset = 0
  if (!props.dynamicTape) {
    framesOffset -= (frameNumber.value - 1) * (frameWidth.value + props.frameGap)
  }

  const totalOffset = baseOffset + framesOffset + swipeOffset.value

  return {
    transform: `translateX(${totalOffset}px)`,
    transition: isSwipeActive.value ? 'none' : `transform ${props.snapDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
  }
})

function handleResize() {
  if (swipeTape.value) {
    frameWidth.value = swipeTape.value.offsetWidth
  }
}

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
          if (canSwipe('right')) {
            decFrameNumber()
            props.onSwipeRight?.()
          }
        } else {
          if (canSwipe('left')) {
            incFrameNumber()
            props.onSwipeLeft?.()
          }
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

function canSwipe(direction: 'left' | 'right') {
  if (props.dynamicTape) {
    return true
  }

  if (direction === 'left') {
    return frameNumber.value < totalFrames.value || props.loopedTape
  } else {
    return frameNumber.value >= 1 || props.loopedTape
  }
}

function incFrameNumber() {
  let nextNumber = frameNumber.value + 1
  if (nextNumber > totalFrames.value && props.loopedTape) {
    nextNumber = 1
  }

  frameNumber.value = nextNumber
}

function decFrameNumber() {
  let nextNumber = frameNumber.value - 1
  if (nextNumber <= 0 && props.loopedTape) {
    nextNumber = totalFrames.value
  }

  frameNumber.value = nextNumber
}

function triggerSwipe(direction: 'left' | 'right') {
  if (!canSwipe(direction)) return
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
        incFrameNumber()
        props.onSwipeLeft?.()
      } else {
        decFrameNumber()
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

onMounted(() => {
  handleResize()
  if (swipeTape.value) {
    resizeObserver.value = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.value.observe(swipeTape.value)
  }
})

onUnmounted(() => {
  resizeObserver.value?.disconnect()
})
</script>

<style scoped>
.swipe-tape-wrapper {
  overflow: hidden;
  padding: 6px;
  width: 100%;
  height: 100%;
}

.swipe-tape {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tape-track {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: v-bind(frameGapPx);
  width: calc(v-bind(totalFrames) * 100% + (v-bind(totalFrames) - 1) * v-bind(frameGapPx));
  height: 100%;
  will-change: transform;
}

.tape-frame {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.tape-progress {
  align-self: center;
  text-align: center;
  border-radius: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: fit-content;
  padding: 4px 10px;
}

.tape-progress p {
  font-size: 0.85rem;
  font-weight: 200;
  letter-spacing: 0.02rem;
  word-spacing: 0.04rem;
  color: #ffffff;
  margin: 0;
  padding: 0;
}

.tape-progress--light {
  background: rgba(0, 0, 0, 0.4);
}

.tape-progress--dark {
  background: rgba(255, 255, 255, 0.05);
}

.tape-navigation {
  position: absolute;
  top: 0;
  height: 100%;
  width: 10%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tape-navigation--left {
  left: -4px;
}

.tape-navigation--right {
  right: -4px;
}

.navigation-button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0.5;
  transition: all 0.4s ease-in-out;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navigation-icon {
  font-size: 40px;
  color: rgba(255, 255, 255, 0.5);
  will-change: opacity;
  transition: opacity 0.4s ease-in-out;
}

.tape-navigation:hover .navigation-button {
  opacity: 1;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.tape-navigation--light:hover .navigation-button {
  background: rgba(0, 0, 0, 0.2);
}

.tape-navigation--dark:hover .navigation-button {
  background: rgba(255, 255, 255, 0.1);
}

</style>

