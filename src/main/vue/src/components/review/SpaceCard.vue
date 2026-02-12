<template>
  <div
    class="space-card space-card--theme"
    :class="{ 'space-card--flipped': flipped }"
    @click="flip"
  >
    <div class="space-card-flipper">
      <div
        class="space-card-face space-card-face--front"
        :class="{
          'space-card-face--front--style': !transparent,
          'space-card-face--transparent': transparent,
        }"
      >
        <div class="space-card-strip select-none">
          <span v-if="!textOnly" class="space-card-strip-text">
            <Tooltip text="Learning Stage" position="bottom-right">
              {{ stageDisplayName }}
            </Tooltip>
          </span>
          <AwesomeButton
            v-if="!textOnly"
            icon="fa-solid fa-pen-to-square"
            class="space-card-button"
            tooltip="Edit flashcard"
            tooltip-position="bottom-left"
            :on-click="handleEdit"
          />
        </div>
        <TotemScroll>
          <div class="space-card-content">
            <p>{{ frontSide }}</p>
          </div>
        </TotemScroll>
        <div class="space-card-strip select-none">
          <span v-if="!textOnly">
            <Tooltip text="Viewed Times" position="top-right">
              <font-awesome-icon icon="fa-regular fa-eye"/>
            </Tooltip>
            {{ viewedTimes }}
          </span>
          <div v-if="!textOnly" class="space-card-strip-group">
            <AwesomeButton
              icon="fa-solid fa-repeat"
              class="space-card-button"
              tooltip="Repeat voice"
              tooltip-position="top-left"
              :active="autoRepeatVoice"
              :on-click="toggleAutoRepeatVoice"
            />
            <AwesomeButton
              v-if="!UXConfig().hasStrictAudio"
              icon="fa-solid fa-a"
              class="space-card-button"
              tooltip="Auto play voice"
              tooltip-position="top-left"
              :active="autoPlayVoice"
              :on-click="toggleAutoPlayVoice"
            />
            <VoicePlayer
              ref="frontVoicePlayer"
              class="space-card-button"
              :audio-blob="frontSideAudio"
              :loop-play="autoRepeatVoice"
            />
          </div>
        </div>
      </div>
      <div
        class="space-card-face space-card-face--back"
        :class="{
          'space-card-face--back--style': !transparent,
          'space-card-face--transparent': transparent,
        }"
      >
        <div class="space-card-strip select-none">
          <span v-if="!textOnly" class="space-card-strip-text">
            <Tooltip text="Learning Stage" position="bottom-right">
              {{ stageDisplayName }}
            </Tooltip>
          </span>
          <AwesomeButton
            v-if="!textOnly"
            icon="fa-solid fa-pen-to-square"
            class="space-card-button"
            tooltip="Edit flashcard"
            tooltip-position="bottom-left"
            :on-click="handleEdit"
          />
        </div>
        <TotemScroll>
          <div class="space-card-content">
            <p>{{ backSide }}</p>
          </div>
        </TotemScroll>
        <div class="space-card-strip select-none">
          <span v-if="!textOnly">
            <Tooltip text="Viewed Times" position="top-right">
              <font-awesome-icon icon="fa-regular fa-eye"/>
            </Tooltip>
            {{ viewedTimes }}
          </span>
          <div v-if="!textOnly" class="space-card-strip-group">
            <AwesomeButton
              icon="fa-solid fa-repeat"
              class="space-card-button"
              tooltip="Repeat voice"
              tooltip-position="top-left"
              :active="autoRepeatVoice"
              :on-click="toggleAutoRepeatVoice"
            />
            <AwesomeButton
              v-if="!UXConfig().hasStrictAudio"
              icon="fa-solid fa-a"
              class="space-card-button"
              tooltip="Auto play voice"
              tooltip-position="top-left"
              :active="autoPlayVoice"
              :on-click="toggleAutoPlayVoice"
            />
            <VoicePlayer
              ref="backVoicePlayer"
              class="space-card-button"
              :audio-blob="backSideAudio"
              :loop-play="autoRepeatVoice"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import TotemScroll from '@/components/TotemScroll.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import VoicePlayer from '@/components/VoicePlayer.vue'
import Tooltip from '@/components/Tooltip.vue'
import { ref, nextTick, computed } from 'vue'
import { stageNameMap } from '@/core-logic/stage-logic.ts'
import { UXConfig } from '@/utils/device-utils.ts'

const autoPlayVoice = defineModel<boolean>('autoPlayVoice', { default: false })
const autoRepeatVoice = defineModel<boolean>('autoRepeatVoice', { default: false })

const props = withDefaults(defineProps<{
  stage?: string
  frontSide?: string
  frontSideAudio?: Blob | undefined
  backSide?: string
  backSideAudio?: Blob | undefined
  viewedTimes?: number
  textOnly?: boolean
  unflippable?: boolean
  transparent?: boolean
  onEdit?: () => void
}>(), {
  stage: undefined,
  frontSide: undefined,
  frontSideAudio: undefined,
  backSide: undefined,
  backSideAudio: undefined,
  viewedTimes: undefined,
  textOnly: false,
  unflippable: false,
  transparent: false,
  onEdit: () => {
  },
})

const FLIP_ANIMATION_DURATION_MS = 500

const flipped = ref(false)
const currentFlipDuration = ref(FLIP_ANIMATION_DURATION_MS)
const flipAnimationDurationMs = computed(() => `${currentFlipDuration.value}ms`)
const isAnimating = ref(false)
const cardAnimationCompleted = ref(false)
let flipDurationTimeout: ReturnType<typeof setTimeout> | null = null
const frontVoicePlayer = ref<InstanceType<typeof VoicePlayer>>()
const backVoicePlayer = ref<InstanceType<typeof VoicePlayer>>()

const stageDisplayName = computed(() => {
  if (props.stage === undefined || props.stage === 'OUTER_SPACE') {
    return ''
  } else {
    return stageNameMap.get(props.stage)?.displayName ?? props.stage
  }
})

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

function toggleAutoPlayVoice() {
  autoPlayVoice.value = !autoPlayVoice.value
}

function toggleAutoRepeatVoice() {
  autoRepeatVoice.value = !autoRepeatVoice.value
}

function stopAllVoices() {
  frontVoicePlayer.value?.stop()
  backVoicePlayer.value?.stop()
}

function playCurrentSideVoice() {
  if (!flipped.value) {
    frontVoicePlayer.value?.play()
  } else {
    backVoicePlayer.value?.play()
  }
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
  --card--color: var(--space-card--color, #686868);
  --card--color--strip: var(--space-card--color--strip, #9f9f9f);
  --card--color--strip--hover: var(--space-card--color--strip--hover, #686868);
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
  perspective: 1000px;
  z-index: 10;
}

.space-card--flipped .space-card-flipper {
  transform: rotateY(180deg);
}

.space-card:not(.space-card--flipped) .space-card-face--back {
  pointer-events: none;
}

.space-card--flipped .space-card-face--front {
  pointer-events: none;
}

.space-card-flipper {
  flex: 1;
  transition: transform v-bind(flipAnimationDurationMs) cubic-bezier(0.25, 1, 0.5, 1);
  transform-style: preserve-3d;
  position: relative;
  will-change: transform;
}

.space-card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0.4rem;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  overflow-wrap: break-word;
  border-color: var(--card--border-color);
  border-style: solid;
  border-width: 1px;
}

.space-card-face--front {
  transform: rotateY(0deg);
  --paper-color: var(--card--front--bg-color);
}

.space-card-face--back {
  transform: rotateY(180deg);
  --paper-color: var(--card--back--bg-color);
}

.space-card-face--front--style {
  background-color: var(--card--front--bg-color);
  background-image: var(--card--front--bg-image);
  background-size: var(--card--front--bg-size);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card-face--back--style {
  background-color: var(--card--back--bg-color);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card:hover .space-card-face--front--style,
.space-card:hover .space-card-face--back--style {
  box-shadow: var(--card--box-shadow--hover);
}

.space-card-face--transparent {
  background: none;
  border: none;
  cursor: default;
  perspective: none;
}

.space-card-strip {
  height: 28px;
  font-size: clamp(1.1rem, 2vw, 1.2rem);
  color: var(--card--color--strip);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 4px;
  padding-right: 4px;
  gap: 10px;
}

.space-card-strip-group {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.space-card-content {
  height: 100%;
  display: block;
  width: 100%;
  min-height: 0;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.space-card-content p {
  margin: 0;
  min-height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  padding: 10px;
  color: var(--card--color);
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  text-align: center;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.space-card-strip-text {
  background: none;
  font-size: inherit;
  color: inherit;
}

.space-card-button {
  --awesome-button--icon--size: clamp(1.2rem, 2vw, 1.3rem);
  --awesome-button--icon--color: var(--card--color--strip);
  --awesome-button--icon--color--hover: var(--card--color--strip--hover);
  --awesome-button--icon--color--active: var(--card--color--strip--hover);
}

</style>
