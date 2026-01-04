<template>
  <div
    ref="spaceDeckElement"
    class="space-deck"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
  >
    <div class="flashcard-deck">
      <div
        v-if="isTouchDevice && swipeLabelText"
        class="swipe-indicator"
        :class="{
          'swipe-indicator--left': fingerOffset < 0,
          'swipe-indicator--right': fingerOffset > 0,
        }"
      >
        <div class="swipe-indicator-line" :style="swipeLineStyle"/>
        <span class="swipe-indicator-text" :style="swipeTextStyle">
          {{ swipeLabelText }}
        </span>
      </div>
      <SpaceCard
        v-if="canShowPullInCard"
        :key="lastFlashcard?.id"
        :stage="lastFlashcard?.stage"
        :front-side="lastFlashcard?.frontSide"
        :back-side="lastFlashcard?.backSide"
        :style="pullInCardStyle"
        class="pull-in-card"
      />
      <SpaceCard
        v-if="currFlashcard && deckReady"
        ref="spaceCard"
        :key="currFlashcard.id"
        v-model:auto-play-voice="autoPlayVoice"
        v-model:auto-repeat-voice="autoRepeatVoice"
        :stage="currFlashcard.stage"
        :front-side="currFlashcard.frontSide"
        :back-side="currFlashcard.backSide"
        :viewed-times="viewedTimes"
        :on-edit="toggleStore.toggleFlashcardEdit"
        :front-side-audio="flashcardFrontSideAudioBlob"
        :back-side-audio="flashcardBackSideAudioBlob"
        :class="cardAnimationClass"
        :style="cardStyle"
        @animationend="onEnterAnimationEnd"
      />
      <template v-else>
        <slot v-if="hasSlot && showSlot && deckReady"/>
        <SpaceCard
          v-else
          :front-side="emptyMessage"
          unflippable
          text-only
          transparent
        />
      </template>
    </div>
  </div>
  <FlashcardEditModal
    v-model:flashcard="currFlashcard"
    v-model:removed="flashcardWasRemoved"
    v-model:audio-changed="audioWasChanged"
  />
</template>

<script setup lang="ts">
import SpaceCard from '@/components/review/SpaceCard.vue'
import FlashcardEditModal from '@/modals/FlashcardEditModal.vue'
import { computed, onMounted, onUnmounted, ref, useSlots, watch } from 'vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { deckEmptyMessage, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { isTouchDevice } from '@/utils/utils.ts'
import { useReviewStore } from '@/stores/review-store.ts'
import { storeToRefs } from 'pinia'

const props = withDefaults(defineProps<{
  sessionType: ReviewSessionType
  showSlot?: boolean
  canSlideLeft?: boolean
  canSlideRight?: boolean
  swipeLeftText?: string
  swipeRightText?: string
  onFlashcardRemoved?: () => void
  onAudioChanged?: () => void
  onSlideLeft?: () => Promise<void> | void
  onSlideRight?: () => Promise<void> | void
}>(), {
  showSlot: true,
  canSlideLeft: true,
  canSlideRight: true,
  swipeLeftText: undefined,
  swipeRightText: undefined,
  onFlashcardRemoved: () => {
  },
  onAudioChanged: () => {
  },
  onSlideLeft: () => {
  },
  onSlideRight: () => {
  },
})

const slots = useSlots()
const toggleStore = useToggleStore()

const reviewStore = useReviewStore(props.sessionType)

const {
  currFlashcard,
  autoPlayVoice,
  autoRepeatVoice,
  flashcardFrontSideAudioBlob,
  flashcardBackSideAudioBlob,
} = storeToRefs(reviewStore)

const deckReady = ref(false)
const flashcardWasRemoved = ref(false)
const audioWasChanged = ref(false)
const spaceCard = ref<InstanceType<typeof SpaceCard>>()
const spaceDeckElement = ref<HTMLElement>()

const hasSlot = computed(() => !!slots.default)
const viewedTimes = computed(() => (currFlashcard.value?.timesReviewed ?? 0) + 1)
const emptyMessage = ref(deckEmptyMessage())

const SWIPE_THRESHOLD = 100
const SWIPE_VELOCITY_THRESHOLD = 0.3
const SWIPE_MAX_ROTATION = 5
const SWIPE_ANIMATION_DURATION = 500
const SWIPE_ANIMATION_DURATION_SLOW = 1000

const swipeOffset = ref(0)
const fingerOffset = ref(0)
const swipeStartX = ref(0)
const swipeStartY = ref(0)
const swipeStartTime = ref(0)
const swipeStartOffset = ref(0) // Track the offset when touch started
const isTouching = ref(false) // User has finger on screen
const isSwiping = ref(false) // User is actively swiping horizontally
const isAnimating = ref(false) // Programmatic animation in progress
const isSlowAnimation = ref(false)
const enterAnimation = ref<'drop-down' | 'zoom-in' | ''>('')
const isPullingIn = ref(false)
const lastFlashcard = ref<Flashcard | undefined>(undefined)
let animationTimeoutId: ReturnType<typeof setTimeout> | null = null

const canShowPullInCard = computed(() => {
  return isDeckEmpty() && !!lastFlashcard.value && deckReady.value
})

const pullInCardStyle = computed(() => {
  if (isPullingIn.value) {
    return cardStyle.value
  } else {
    return {
      transform: `translateX(${getPullInStartOffset()}px)`,
      visibility: 'hidden',
    }
  }
})

function isDeckEmpty() {
  return !currFlashcard.value
}

watch(currFlashcard, (newVal) => {
  if (newVal) {
    lastFlashcard.value = newVal
  }
}, { immediate: true })

function getExitOffset(): number {
  const el = spaceDeckElement.value
  const elementWidth = el?.offsetWidth ?? 0
  return window.innerWidth + elementWidth
}

function getPullInStartOffset(): number {
  const el = spaceDeckElement.value
  const elementWidth = el?.offsetWidth ?? 300
  return elementWidth + 50
}

function getAnimationDuration(): number {
  return isSlowAnimation.value ? SWIPE_ANIMATION_DURATION_SLOW : SWIPE_ANIMATION_DURATION
}

function getInvisibleDuration(direction: 'left' | 'right'): number {
  const el = spaceDeckElement.value
  if (!el) return getAnimationDuration()

  const rect = el.getBoundingClientRect()
  const totalDistance = getExitOffset()
  const distanceToEdge = direction === 'right'
    ? window.innerWidth - rect.left
    : rect.right

  // Calculate duration proportionally based on distance ratio
  const ratio = distanceToEdge / totalDistance
  return Math.round(getAnimationDuration() * ratio)
}

function canSwipe() {
  return isTouchDevice && (props.canSlideLeft || props.canSlideRight)
}

function cancelAnimation() {
  if (animationTimeoutId) {
    clearTimeout(animationTimeoutId)
    animationTimeoutId = null
  }
  isAnimating.value = false
}

const cardStyle = computed(() => {
  const rotation = SWIPE_MAX_ROTATION > 0 ? (swipeOffset.value / 200) * SWIPE_MAX_ROTATION : 0
  const transitionDuration = getAnimationDuration()

  if (swipeOffset.value === 0 && !rotation && !isTouching.value && !isAnimating.value) return {}

  return {
    transform: `translateX(${swipeOffset.value}px)${rotation ? ` rotate(${rotation}deg)` : ''}`,
    transition: isTouching.value ? 'none' : `transform ${transitionDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
  }
})

const cardAnimationClass = computed(() => {
  return enterAnimation.value ? `card-enter-${enterAnimation.value}` : ''
})

const fingerProgress = computed(() => {
  const progress = fingerOffset.value / SWIPE_THRESHOLD
  return Math.max(-1, Math.min(1, progress))
})

function onTouchStart(event: TouchEvent) {
  if (!canSwipe()) return

  // Cancel any ongoing animation and capture current position
  cancelAnimation()
  const currentOffset = swipeOffset.value

  const touch = event.touches[0]
  swipeStartX.value = touch.clientX
  swipeStartY.value = touch.clientY
  swipeStartTime.value = performance.now()
  swipeStartOffset.value = currentOffset // Remember where we started from
  isTouching.value = true
  isSwiping.value = false
  isPullingIn.value = false
  fingerOffset.value = 0
  // Keep swipeOffset at current value - don't reset to 0
}

function onTouchMove(event: TouchEvent) {
  if (!isTouching.value || !canSwipe()) return

  const touch = event.touches[0]
  const deltaX = touch.clientX - swipeStartX.value
  const deltaY = touch.clientY - swipeStartY.value

  if (isSwiping.value) {
    // Already swiping - update position
    if (isPullingIn.value) {
      swipeOffset.value = getPullInStartOffset() + deltaX
    } else {
      swipeOffset.value = swipeStartOffset.value + deltaX
    }
    fingerOffset.value = deltaX
    if (event.cancelable) {
      event.preventDefault()
    }
    return
  }

  // Determine if this is a horizontal swipe
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
    isSwiping.value = true
    fingerOffset.value = deltaX

    // Check if we should start a pull-in gesture
    if (isDeckEmpty() && deltaX < 0 && props.canSlideLeft && lastFlashcard.value) {
      isPullingIn.value = true
      enterAnimation.value = ''
      swipeOffset.value = getPullInStartOffset() + deltaX
    } else {
      swipeOffset.value = swipeStartOffset.value + deltaX
    }
  }
}

function onTouchEnd(event: TouchEvent) {
  if (!isTouching.value) return

  fingerOffset.value = 0
  isTouching.value = false

  if (!canSwipe() || !isSwiping.value) {
    // No swipe happened - snap back if needed
    if (swipeOffset.value !== 0) {
      isAnimating.value = true
      swipeOffset.value = 0
      animationTimeoutId = setTimeout(() => {
        isAnimating.value = false
      }, getAnimationDuration())
    }
    isSwiping.value = false
    isPullingIn.value = false
    return
  }

  const touch = event.changedTouches[0]
  const deltaX = touch.clientX - swipeStartX.value
  const deltaTime = performance.now() - swipeStartTime.value
  const velocity = Math.abs(deltaX) / deltaTime

  const meetsThreshold = Math.abs(deltaX) > SWIPE_THRESHOLD || velocity > SWIPE_VELOCITY_THRESHOLD
  const isSwipeLeft = deltaX < 0
  const directionAllowed = isSwipeLeft ? props.canSlideLeft : props.canSlideRight
  const isValidSwipe = meetsThreshold && directionAllowed

  if (isPullingIn.value) {
    // Handle pull-in gesture completion
    isAnimating.value = true
    if (isValidSwipe) {
      // Complete the pull-in
      swipeOffset.value = 0
      animationTimeoutId = setTimeout(() => {
        isAnimating.value = false
        isPullingIn.value = false
        props.onSlideLeft()
      }, getAnimationDuration())
    } else {
      // Cancel pull-in - slide back off the screen
      swipeOffset.value = getPullInStartOffset()
      animationTimeoutId = setTimeout(() => {
        isAnimating.value = false
        isPullingIn.value = false
        swipeOffset.value = 0
      }, getAnimationDuration())
    }
    isSwiping.value = false
    return
  }

  if (isValidSwipe) {
    // Complete the swipe
    isAnimating.value = true
    const direction = deltaX > 0 ? 'right' : 'left'
    const exitOffset = getExitOffset()
    const invisibleDuration = getInvisibleDuration(direction)
    const offset = deltaX > 0 ? exitOffset : -exitOffset

    swipeOffset.value = offset
    animationTimeoutId = setTimeout(async () => {
      if (deltaX > 0) {
        await props.onSlideRight()
      } else {
        await props.onSlideLeft()
      }
      swipeOffset.value = 0
      isAnimating.value = false
      enterAnimation.value = 'zoom-in'
    }, invisibleDuration)
  } else {
    // Snap back to center
    isAnimating.value = true
    swipeOffset.value = 0
    animationTimeoutId = setTimeout(() => {
      isAnimating.value = false
    }, getAnimationDuration())
  }

  isSwiping.value = false
}

const swipeLabelText = computed(() => {
  if (fingerOffset.value < 0 && props.swipeLeftText) return props.swipeLeftText
  if (fingerOffset.value > 0 && props.swipeRightText) return props.swipeRightText
  return ''
})

const swipeLineStyle = computed(() => {
  const progress = Math.abs(fingerProgress.value)
  const width = Math.abs(fingerOffset.value) * 0.5
  return {
    width: `${width}px`,
    opacity: Math.min(1, progress),
  }
})

const swipeTextStyle = computed(() => {
  const progress = Math.abs(fingerProgress.value)
  return {
    opacity: progress,
  }
})

function setDeckReady() {
  deckReady.value = true
  enterAnimation.value = 'drop-down'
}

async function triggerSlideLeft() {
  if (!props.canSlideLeft) return
  await props.onSlideLeft()
  enterAnimation.value = 'zoom-in'
}

async function triggerSlideRight() {
  if (!props.canSlideRight) return
  await props.onSlideRight()
  enterAnimation.value = 'zoom-in'
}

function animateSwipe(direction: 'left' | 'right', onCompleteCallback: () => Promise<void> | void, slow = false): Promise<boolean> {
  return new Promise((resolve) => {
    if (isAnimating.value || isTouching.value) {
      resolve(false)
      return
    }
    isAnimating.value = true
    isSlowAnimation.value = slow
    enterAnimation.value = '' // Stop any zoom-in animation so swipe transform can take effect

    const exitOffset = getExitOffset()
    const invisibleDuration = getInvisibleDuration(direction)
    const offset = direction === 'right' ? exitOffset : -exitOffset

    requestAnimationFrame(() => {
      swipeOffset.value = offset
      animationTimeoutId = setTimeout(async () => {
        await onCompleteCallback()
        swipeOffset.value = 0
        isAnimating.value = false
        isSlowAnimation.value = false
        resolve(true)
      }, invisibleDuration)
    })
  })
}

function animatePullIn(onCompleteCallback: () => void): boolean {
  if (isAnimating.value || isTouching.value) {
    return false
  }
  isAnimating.value = true
  isPullingIn.value = true
  enterAnimation.value = ''

  // Start from off-screen right (no transition)
  isTouching.value = true // Temporarily disable transition
  swipeOffset.value = getPullInStartOffset()

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      isTouching.value = false // Re-enable transition
      swipeOffset.value = 0
      animationTimeoutId = setTimeout(() => {
        isPullingIn.value = false
        isAnimating.value = false
        onCompleteCallback()
      }, getAnimationDuration())
    })
  })
  return true
}

async function slideLeft() {
  if (!props.canSlideLeft) return
  if (isDeckEmpty() && lastFlashcard.value) {
    animatePullIn(() => props.onSlideLeft())
  } else {
    await animateSwipe('left', triggerSlideLeft)
  }
}

async function slideRight() {
  if (!props.canSlideRight) return
  await animateSwipe('right', triggerSlideRight)
}

function animateOutLeft(slow = false): Promise<boolean> {
  return animateSwipe('left', () => {
    enterAnimation.value = 'zoom-in'
  }, slow)
}

function animateOutRight(slow = false): Promise<boolean> {
  return animateSwipe('right', () => {
    enterAnimation.value = 'zoom-in'
  }, slow)
}

function onEnterAnimationEnd() {
  enterAnimation.value = ''
  spaceCard.value?.onCardAnimationComplete()
}

defineExpose({
  setDeckReady,
  slideLeft,
  slideRight,
  animateOutLeft,
  animateOutRight,
})

watch(flashcardWasRemoved, (newVal) => {
  if (newVal) {
    spaceCard.value?.flipToFront()
    props.onFlashcardRemoved()
    flashcardWasRemoved.value = false
  }
})

watch(audioWasChanged, (newVal) => {
  if (newVal) {
    props.onAudioChanged()
    audioWasChanged.value = false
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (toggleStore.isAnyModalOpen()) return
  if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    event.stopPropagation()
    spaceCard.value?.flip()
  }
}

</script>

<style scoped>
.space-deck {
  display: grid;
  place-items: center;
}

.flashcard-deck {
  grid-area: 1 / 1;
  position: relative;
  width: clamp(200px, 90vw, 600px);
  height: clamp(290px, 50vh, 450px);
}

.swipe-indicator {
  position: absolute;
  top: -3rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  pointer-events: none;
  z-index: 20;
}

.swipe-indicator--left {
  flex-direction: row-reverse;
}

.swipe-indicator--right {
  flex-direction: row;
}

.swipe-indicator-line {
  height: 1px;
  width: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(148, 163, 184, 0.6) 50%,
    rgba(148, 163, 184, 0.8) 100%
  );
  transition: opacity 0.3s ease-out;
}

.swipe-indicator--left .swipe-indicator-line {
  background: linear-gradient(
    270deg,
    transparent 0%,
    rgba(148, 163, 184, 0.6) 50%,
    rgba(148, 163, 184, 0.8) 100%
  );
}

.swipe-indicator-text {
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  white-space: nowrap;
  color: rgba(100, 116, 139, 0.9);
  transition: opacity 0.3s ease-out;
}

.card-enter-drop-down {
  animation: drop-and-bounce 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes drop-and-bounce {
  0% {
    transform: scale(1.5);
    opacity: 0;
  }
  70% {
    transform: scale(1);
    opacity: 1;
  }
  80% {
    transform: scale(1.01);
  }
  90% {
    transform: scale(1);
  }
  100% {
    transform: scale(0.99);
  }
}

.card-enter-zoom-in {
  animation: zoom-in 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.1);
}

@keyframes zoom-in {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.pull-in-card {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 20;
}

</style>
