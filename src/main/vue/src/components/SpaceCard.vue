<template>
  <div
    class="space-card space-card--theme"
    :class="{ 'space-card--flipped': flipped }"
    @click="flip"
  >
    <div class="space-card-flipper">
      <div
        class="space-card-side space-card-side--front"
        :class="{
          'space-card-side--front--style': !transparent,
          'space-card-side--transparent': transparent,
        }"
      >
        <SpaceCardSide
          ref="frontSpaceCardSide"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :stage="stage"
          :text="frontSide"
          :audio="frontSideAudio"
          :picture="frontSidePicture"
          :text-only="textOnly"
          :viewed-times="viewedTimes"
          :on-edit="handleEdit"
          :on-copy-text-to-clipboard="resetClipboardButton"
        />
      </div>
      <div
        class="space-card-side space-card-side--back"
        :class="{
          'space-card-side--back--style': !transparent,
          'space-card-side--transparent': transparent,
        }"
      >
        <SpaceCardSide
          ref="backSpaceCardSide"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :stage="stage"
          :text="backSide"
          :audio="backSideAudio"
          :picture="backSidePicture"
          :text-only="textOnly"
          :viewed-times="viewedTimes"
          :on-edit="handleEdit"
          :on-copy-text-to-clipboard="resetClipboardButton"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, computed } from 'vue'
import SpaceCardSide from '@/components/SpaceCardSide.vue'

const autoPlayVoice = defineModel<boolean>('autoPlayVoice', { default: false })
const autoRepeatVoice = defineModel<boolean>('autoRepeatVoice', { default: false })

const props = withDefaults(
  defineProps<{
    stage?: string
    frontSide?: string | undefined
    frontSideAudio?: Blob | undefined
    frontSidePicture?: Blob | undefined
    backSide?: string | undefined
    backSideAudio?: Blob | undefined
    backSidePicture?: Blob | undefined
    viewedTimes?: number
    textOnly?: boolean
    unflippable?: boolean
    transparent?: boolean
    onEdit?: () => void
  }>(),
  {
    stage: undefined,
    frontSide: undefined,
    frontSideAudio: undefined,
    frontSidePicture: undefined,
    backSide: undefined,
    backSideAudio: undefined,
    backSidePicture: undefined,
    viewedTimes: undefined,
    textOnly: false,
    unflippable: false,
    transparent: false,
    onEdit: () => {},
  },
)

const FLIP_ANIMATION_DURATION_MS = 500

const flipped = ref(false)
const currentFlipDuration = ref(FLIP_ANIMATION_DURATION_MS)
const flipAnimationDurationMs = computed(() => `${currentFlipDuration.value}ms`)
const isAnimating = ref(false)
const cardAnimationCompleted = ref(false)

let flipDurationTimeout: ReturnType<typeof setTimeout> | null = null

const frontSpaceCardSide = ref<InstanceType<typeof SpaceCardSide>>()
const backSpaceCardSide = ref<InstanceType<typeof SpaceCardSide>>()

function stopAllVoices() {
  frontSpaceCardSide.value?.stopVoice()
  backSpaceCardSide.value?.stopVoice()
}

function playCurrentSideVoice() {
  if (!flipped.value) {
    frontSpaceCardSide.value?.playVoice()
  } else {
    backSpaceCardSide.value?.playVoice()
  }
}

function resetClipboardButton() {
  if (flipped.value) {
    frontSpaceCardSide.value?.resetClipboardButton()
  } else {
    backSpaceCardSide.value?.resetClipboardButton()
  }
}

function flip() {
  if (!props.unflippable && !isAnimating.value) {
    if (flipDurationTimeout) {
      clearTimeout(flipDurationTimeout)
      flipDurationTimeout = null
    }

    isAnimating.value = true
    stopAllVoices()
    flipped.value = !flipped.value

    setTimeout(() => {
      isAnimating.value = false
      if (autoPlayVoice.value && cardAnimationCompleted.value) {
        playCurrentSideVoice()
      }
    }, FLIP_ANIMATION_DURATION_MS)
  }
}

function flipToFront() {
  if (!props.unflippable && flipped.value) {
    flipped.value = false
  }
}

async function flipToFrontAndWait(): Promise<void> {
  return new Promise((resolve) => {
    if (flipped.value && !props.unflippable) {
      flipped.value = false
      setTimeout(() => resolve(void 0), FLIP_ANIMATION_DURATION_MS)
    } else {
      resolve(void 0)
    }
  })
}

function handleEdit() {
  stopAllVoices()
  props.onEdit()
}

function onCardAnimationComplete() {
  cardAnimationCompleted.value = true
  if (autoPlayVoice.value) {
    nextTick(() => {
      playCurrentSideVoice()
    })
  }
}

defineExpose({
  flip,
  flipToFront,
  flipToFrontAndWait,
  onCardAnimationComplete,
})
</script>

<style scoped>
.space-card--theme {
  --card--border-color: var(--space-card--border-color, none);
  --card--box-shadow: var(--space-card--box-shadow, 0 8px 12px rgba(0, 0, 0, 0.15));
  --card--box-shadow--hover: var(--flashcard--box-shadow--hover, 0 12px 16px rgba(0, 0, 0, 0.2));
  --card--front--bg-color: var(--space-card--front--bg-color, white);
  --card--front--bg-image: var(--space-card--front--bg-image, none);
  --card--front--bg-size: var(--space-card--front--bg-size, none);
  --card--back--bg-color: var(--space-card--back--bg-color, white);
}

.space-card {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  will-change: transform;
  transform-style: preserve-3d;
  -webkit-transform-style: preserve-3d;
  perspective: 1000px;
  z-index: 10;
}

.space-card--flipped .space-card-flipper {
  transform: rotateY(180deg);
}

.space-card:not(.space-card--flipped) .space-card-side--back {
  pointer-events: none;
}

.space-card--flipped .space-card-side--front {
  pointer-events: none;
}

.space-card-flipper {
  flex: 1;
  transition: transform v-bind(flipAnimationDurationMs) cubic-bezier(0.25, 1, 0.5, 1);
  transform-style: preserve-3d;
  -webkit-transform-style: preserve-3d;
  position: relative;
  will-change: transform;
}

.space-card-side {
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0.4rem;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  will-change: transform;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  overflow-wrap: break-word;
  border-color: var(--card--border-color);
  border-style: solid;
  border-width: 1px;
}

.space-card-side--front {
  transform: rotateY(0deg);
  --paper-color: var(--card--front--bg-color);
}

.space-card-side--front--style {
  background-color: var(--card--front--bg-color);
  background-image: var(--card--front--bg-image);
  background-size: var(--card--front--bg-size);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card-side--back {
  transform: rotateY(180deg);
  --paper-color: var(--card--back--bg-color);
}

.space-card-side--back--style {
  background-color: var(--card--back--bg-color);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card:hover .space-card-side--front--style,
.space-card:hover .space-card-side--back--style {
  box-shadow: var(--card--box-shadow--hover);
}

.space-card-side--transparent {
  background: none;
  border: none;
  cursor: default;
  perspective: none;
}
</style>
