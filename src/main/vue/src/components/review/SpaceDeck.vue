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
const SWIPE_ANIMATION_SPEED = 2
const SWIPE_ANIMATION_SPEED_SLOW = 0.8

const swipeOffset = ref(0)
const fingerOffset = ref(0)
const swipeStartX = ref(0)
const swipeStartY = ref(0)
const swipeStartTime = ref(0)
const isSwipeActive = ref(false)
const isSwiping = ref(false)
const isAnimating = ref(false)
const isAnimatingOut = ref(false)
const isSlowAnimation = ref(false)
const enterAnimation = ref<'drop-down' | 'zoom-in' | ''>('')
const isPullingIn = ref(false)
const lastFlashcard = ref<Flashcard | undefined>(undefined)

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

function getAnimationSpeed(): number {
  return isSlowAnimation.value ? SWIPE_ANIMATION_SPEED_SLOW : SWIPE_ANIMATION_SPEED
}

function getAnimationDuration(): number {
  return Math.round(getExitOffset() / getAnimationSpeed())
}

function getInvisibleDuration(direction: 'left' | 'right'): number {
  const el = spaceDeckElement.value
  if (!el) return getAnimationDuration()

  const rect = el.getBoundingClientRect()
  const distance = direction === 'right'
    ? window.innerWidth - rect.left
    : rect.right

  return Math.round(distance / getAnimationSpeed())
}

function canSwipe() {
  return isTouchDevice && (props.canSlideLeft || props.canSlideRight) && !isAnimatingOut.value && !isAnimating.value
}

const cardStyle = computed(() => {
  const rotation = SWIPE_MAX_ROTATION > 0 ? (swipeOffset.value / 200) * SWIPE_MAX_ROTATION : 0
  const transitionDuration = getAnimationDuration()

  if (swipeOffset.value === 0 && !rotation && !isSwipeActive.value && !isAnimatingOut.value) return {}

  return {
    transform: `translateX(${swipeOffset.value}px)${rotation ? ` rotate(${rotation}deg)` : ''}`,
    transition: isSwipeActive.value ? 'none' : `transform ${transitionDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
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
  const touch = event.touches[0]
  swipeStartX.value = touch.clientX
  swipeStartY.value = touch.clientY
  swipeStartTime.value = Date.now()
  isSwiping.value = false
  isSwipeActive.value = true
  isAnimatingOut.value = false
  isPullingIn.value = false
  swipeOffset.value = 0
  fingerOffset.value = 0
}

function onTouchMove(event: TouchEvent) {
  if (!canSwipe()) return
  const touch = event.touches[0]
  const deltaX = touch.clientX - swipeStartX.value
  const deltaY = touch.clientY - swipeStartY.value

  if (isSwiping.value) {
    if (isPullingIn.value) {
      swipeOffset.value = getPullInStartOffset() + deltaX
    } else {
      swipeOffset.value = deltaX
    }
    fingerOffset.value = deltaX
    if (event.cancelable) {
      event.preventDefault()
    }
    return
  }

  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
    isSwiping.value = true
    if (isDeckEmpty() && deltaX < 0 && props.canSlideLeft && lastFlashcard.value) {
      isPullingIn.value = true
      enterAnimation.value = ''
      const pullInStart = getPullInStartOffset()
      swipeOffset.value = pullInStart + deltaX
    } else if (!isPullingIn.value) {
      swipeOffset.value = deltaX
    } else {
      const pullInStart = getPullInStartOffset()
      swipeOffset.value = pullInStart + deltaX
    }

    fingerOffset.value = deltaX
  }
}

function onTouchEnd(event: TouchEvent) {
  fingerOffset.value = 0
  if (!canSwipe()) {
    isSwipeActive.value = false
    isPullingIn.value = false
    swipeOffset.value = 0
    return
  }

  const touch = event.changedTouches[0]
  const deltaX = touch.clientX - swipeStartX.value
  const deltaTime = Date.now() - swipeStartTime.value
  const velocity = Math.abs(deltaX) / deltaTime

  const meetsThreshold = Math.abs(deltaX) > SWIPE_THRESHOLD || velocity > SWIPE_VELOCITY_THRESHOLD
  const isSwipeLeft = deltaX < 0
  const directionAllowed = isSwipeLeft ? props.canSlideLeft : props.canSlideRight
  const isValidSwipe = isSwiping.value && meetsThreshold && directionAllowed

  if (isPullingIn.value) {
    isSwipeActive.value = false

    const pullInValid = meetsThreshold && props.canSlideLeft
    if (pullInValid) {
      isAnimatingOut.value = true
      requestAnimationFrame(() => {
        swipeOffset.value = 0
        setTimeout(() => {
          isAnimatingOut.value = false
          isPullingIn.value = false
          props.onSlideLeft()
        }, getAnimationDuration())
      })
    } else {
      isAnimatingOut.value = true
      requestAnimationFrame(() => {
        swipeOffset.value = getPullInStartOffset()
        setTimeout(() => {
          isAnimatingOut.value = false
          isPullingIn.value = false
          swipeOffset.value = 0
        }, getAnimationDuration())
      })
    }

    isSwiping.value = false
    return
  }

  if (isValidSwipe) {
    isSwipeActive.value = false
    isAnimatingOut.value = true
    const direction = deltaX > 0 ? 'right' : 'left'
    const exitOffset = getExitOffset()
    const invisibleDuration = getInvisibleDuration(direction)
    const offset = deltaX > 0 ? exitOffset : -exitOffset
    requestAnimationFrame(() => {
      swipeOffset.value = offset
      setTimeout(async () => {
        if (deltaX > 0) {
          await props.onSlideRight()
        } else {
          await props.onSlideLeft()
        }
        swipeOffset.value = 0
        isAnimatingOut.value = false
        enterAnimation.value = 'zoom-in'
      }, invisibleDuration)
    })
  } else if (isSwiping.value) {
    isSwipeActive.value = false
    isAnimatingOut.value = true
    const snapDuration = getAnimationDuration()
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
    if (isAnimating.value) {
      resolve(false)
      return
    }
    isAnimating.value = true
    isAnimatingOut.value = true
    isSlowAnimation.value = slow
    enterAnimation.value = '' // Stop any zoom-in animation so swipe transform can take effect

    const exitOffset = getExitOffset()
    const invisibleDuration = getInvisibleDuration(direction)
    const offset = direction === 'right' ? exitOffset : -exitOffset

    requestAnimationFrame(() => {
      swipeOffset.value = offset
      setTimeout(async () => {
        await onCompleteCallback()
        swipeOffset.value = 0
        isAnimatingOut.value = false
        isAnimating.value = false
        isSlowAnimation.value = false
        resolve(true)
      }, invisibleDuration)
    })
  })
}

function animatePullIn(onCompleteCallback: () => void): boolean {
  if (isAnimating.value) {
    return false
  }
  isAnimating.value = true
  isPullingIn.value = true
  enterAnimation.value = ''
  isSwipeActive.value = true

  // Start from off-screen right
  swipeOffset.value = getPullInStartOffset()

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      isSwipeActive.value = false
      isAnimatingOut.value = true
      swipeOffset.value = 0
      setTimeout(() => {
        isAnimatingOut.value = false
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
