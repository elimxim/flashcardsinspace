<template>
  <div
    ref="spaceDeckElement"
    class="space-deck"
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
      <transition
        :name="cardTransition"
        @after-enter="onCardTransitionComplete"
      >
        <SpaceCard
          v-if="flashcard && deckReady"
          ref="spaceCard"
          :key="flashcard ? flashcard.id : 0"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :stage="flashcard?.stage"
          :front-side="flashcard?.frontSide"
          :back-side="flashcard?.backSide"
          :viewed-times="viewedTimes"
          :on-edit="toggleStore.toggleFlashcardEdit"
          :front-side-audio="flashcardFrontSideAudio"
          :back-side-audio="flashcardBackSideAudio"
          :style="swipeStyle"
        />
        <template v-else>
          <slot v-if="hasSlot && showSlot && deckReady"/>
          <SpaceCard
            v-else
            :front-side="deckEmptyMessage()"
            unflippable
            text-only
            transparent
          />
        </template>
      </transition>
    </div>
  </div>
  <FlashcardEditModal
    v-model:flashcard="flashcard"
    v-model:removed="flashcardWasRemoved"
    v-model:audio-changed="audioWasChanged"
  />
</template>

<script setup lang="ts">
import SpaceCard from '@/components/SpaceCard.vue'
import FlashcardEditModal from '@/modals/FlashcardEditModal.vue'
import { computed, onMounted, onUnmounted, ref, useSlots, watch } from 'vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'
import { isTouchDevice } from '@/utils/utils.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const autoPlayVoice = defineModel<boolean>('autoPlayVoice', { default: false })
const autoRepeatVoice = defineModel<boolean>('autoRepeatVoice', { default: false })

const props = withDefaults(defineProps<{
  showSlot?: boolean
  flashcardFrontSideAudio?: Blob | undefined
  flashcardBackSideAudio?: Blob | undefined
  canSlideLeft?: boolean
  canSlideRight?: boolean
  swipeLeftText?: string
  swipeRightText?: string
  onFlashcardRemoved?: () => void
  onAudioChanged?: () => void
  onSlideLeft?: () => void
  onSlideRight?: () => void
}>(), {
  showSlot: true,
  flashcardFrontSideAudio: undefined,
  flashcardBackSideAudio: undefined,
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

const deckReady = ref(false)
const flashcardWasRemoved = ref(false)
const audioWasChanged = ref(false)
const spaceCard = ref<InstanceType<typeof SpaceCard>>()
const spaceDeckElement = ref<HTMLElement>()
const cardTransition = ref('')

const hasSlot = computed(() => !!slots.default)
const viewedTimes = computed(() => (flashcard.value?.timesReviewed ?? 0) + 1)

const SWIPE_THRESHOLD = 100
const SWIPE_VELOCITY_THRESHOLD = 0.3
const SWIPE_MAX_ROTATION = 5
const SWIPE_ANIMATION_SPEED = 1.5

let touchStartX = 0
let touchStartY = 0
let touchStartTime = 0
let isSwiping = false

const swipeOffset = ref(0)
const fingerOffset = ref(0)
const isSwipeActive = ref(false)
const isAnimatingOut = ref(false)

function getExitOffset(): number {
  const el = spaceDeckElement.value
  const elementWidth = el?.offsetWidth ?? 0
  return window.innerWidth + elementWidth
}

function getAnimationDuration(): number {
  return Math.round(getExitOffset() / SWIPE_ANIMATION_SPEED)
}

function getInvisibleDuration(direction: 'left' | 'right'): number {
  const el = spaceDeckElement.value
  if (!el) return getAnimationDuration()

  const rect = el.getBoundingClientRect()
  const distance = direction === 'right'
    ? window.innerWidth - rect.left
    : rect.right

  return Math.round(distance / SWIPE_ANIMATION_SPEED)
}

function canSwipe() {
  return isTouchDevice && (props.canSlideLeft || props.canSlideRight)
}

const swipeStyle = computed(() => {
  const rotation = SWIPE_MAX_ROTATION > 0 ? (swipeOffset.value / 200) * SWIPE_MAX_ROTATION : 0
  const transitionDuration = getAnimationDuration()

  if (swipeOffset.value === 0 && !rotation && !isSwipeActive.value && !isAnimatingOut.value) return {}

  return {
    transform: `translateX(${swipeOffset.value}px)${rotation ? ` rotate(${rotation}deg)` : ''}`,
    transition: isSwipeActive.value ? 'none' : `transform ${transitionDuration}ms cubic-bezier(0.33, 0, 0.2, 1)`,
  }
})

const fingerProgress = computed(() => {
  const progress = fingerOffset.value / SWIPE_THRESHOLD
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

  if (isSwiping) {
    swipeOffset.value = deltaX
    fingerOffset.value = deltaX
    if (event.cancelable) {
      event.preventDefault()
    }
    return
  }

  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
    isSwiping = true
    swipeOffset.value = deltaX
    fingerOffset.value = deltaX
    if (event.cancelable) {
      event.preventDefault()
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

  const meetsThreshold = Math.abs(deltaX) > SWIPE_THRESHOLD || velocity > SWIPE_VELOCITY_THRESHOLD
  const isSwipeLeft = deltaX < 0
  const directionAllowed = isSwipeLeft ? props.canSlideLeft : props.canSlideRight
  const isValidSwipe = isSwiping && meetsThreshold && directionAllowed

  if (isValidSwipe) {
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
          slideRight()
        } else {
          slideLeft()
        }
      }, invisibleDuration)
    })
  } else if (isSwiping) {
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

  isSwiping = false
}

function attachSwipeListeners(el: HTMLElement) {
  el.addEventListener('touchstart', onTouchStart)
  el.addEventListener('touchmove', onTouchMove, { passive: false })
  el.addEventListener('touchend', onTouchEnd)
}

function detachSwipeListeners(el: HTMLElement) {
  el.removeEventListener('touchstart', onTouchStart)
  el.removeEventListener('touchmove', onTouchMove)
  el.removeEventListener('touchend', onTouchEnd)
}

watch(spaceDeckElement, (newEl, oldEl) => {
  if (oldEl) {
    detachSwipeListeners(oldEl)
  }
  if (newEl) {
    attachSwipeListeners(newEl)
  }
}, { immediate: true })

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
  cardTransition.value = 'drop-down'
}

function willSlideLeft() {
  prepareSlideTransition('slide-to-left')
}

function willSlideRight() {
  prepareSlideTransition('slide-to-right')
}

function slideLeft() {
  if (!props.canSlideLeft) return
  willSlideLeft()
  props.onSlideLeft()
}

function slideRight() {
  if (!props.canSlideRight) return
  willSlideRight()
  props.onSlideRight()
}

function prepareSlideTransition(value: string) {
  if (!deckReady.value) {
    cardTransition.value = ''
    return
  }

  if (!flashcard.value) {
    cardTransition.value = 'slide-from-right'
  } else {
    cardTransition.value = value
  }
}

function onCardTransitionComplete() {
  spaceCard.value?.onCardAnimationComplete()
}

defineExpose({
  setDeckReady,
  willSlideLeft,
  willSlideRight,
  slideLeft,
  slideRight,
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
  const el = spaceDeckElement.value
  if (el) {
    detachSwipeListeners(el)
  }
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
  top: -2rem;
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

.slide-to-right-enter-active,
.slide-to-right-leave-active,
.slide-to-left-enter-active,
.slide-to-left-leave-active {
  transition: all 0.4s cubic-bezier(0.33, 0, 0.2, 1);
  position: absolute;
}

.slide-from-right-enter-active,
.slide-from-right-leave-active {
  transition: all 0.2s cubic-bezier(0.33, 0, 0.2, 1);
  position: absolute;
}

.slide-from-right-leave-active,
.slide-to-right-leave-active,
.slide-to-left-leave-active {
  z-index: 1;
}

.slide-to-right-enter-from,
.slide-to-left-enter-from {
  transform: scale(0.90);
}

.slide-to-right-leave-to {
  transform: translateX(200%) rotate(10deg);
}

.slide-to-left-leave-to {
  transform: translateX(-200%) rotate(-10deg);
}

.slide-from-right-enter-from {
  transform: translateX(200%) rotate(10deg);
}

.slide-from-right-leave-to {
  opacity: 0;
  transform: none;
}

.drop-down-enter-active {
  animation: drop-and-bounce 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: absolute;
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

</style>
