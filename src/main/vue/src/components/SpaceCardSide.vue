<template>
  <div class="space-card-strip select-none">
    <span v-if="!textOnly" class="space-card-strip-text">
      <Tooltip text="Learning Stage" position="bottom-right">
        {{ stageDisplayName }}
      </Tooltip>
    </span>
    <div v-if="!textOnly" class="space-card-strip-group">
      <AwesomeButton
        ref="clipboardButton"
        icon="fa-regular fa-clone"
        flip-icon="fa-solid fa-check"
        class="space-card-button"
        tooltip="Copy text"
        tooltip-position="bottom-left"
        :on-click="copyTextToClipboard"
        disabled-on-press
      />
      <AwesomeButton
        icon="fa-solid fa-pen-to-square"
        class="space-card-button"
        tooltip="Edit flashcard"
        tooltip-position="bottom-left"
        :on-click="onEdit"
      />
    </div>
  </div>
  <TotemScroll v-if="hasPicture">
    <div class="space-card-content space-card-content--picture">
      <div class="space-card-picture">
        <SpacePicture :picture-blob="picture" />
      </div>
      <p v-if="text" class="space-card-text">{{ text }}</p>
    </div>
  </TotemScroll>
  <TotemScroll v-else>
    <div class="space-card-content space-card-content--text">
      <p class="space-card-text">{{ text }}</p>
    </div>
  </TotemScroll>
  <div class="space-card-strip select-none">
    <span v-if="!textOnly">
      <Tooltip text="Viewed Times" position="top-right">
        <font-awesome-icon icon="fa-regular fa-eye" />
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
        ref="voicePlayer"
        class="space-card-button"
        :audio-blob="audio"
        :loop-play="autoRepeatVoice"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import Tooltip from '@/components/common/Tooltip.vue'
import VoicePlayer from '@/components/VoicePlayer.vue'
import SpacePicture from '@/components/SpacePicture.vue'
import TotemScroll from '@/components/common/TotemScroll.vue'
import { computed, ref } from 'vue'
import { specialStages, stageNameMap } from '@/core-logic/stage-logic.ts'
import { UXConfig } from '@/utils/device-utils.ts'

const autoPlayVoice = defineModel<boolean>('autoPlayVoice', { default: false })
const autoRepeatVoice = defineModel<boolean>('autoRepeatVoice', { default: false })

const props = withDefaults(
  defineProps<{
    stage?: string
    text?: string
    audio?: Blob | undefined
    picture?: Blob | undefined
    textOnly?: boolean
    viewedTimes?: number
    onEdit?: () => void
    onCopyTextToClipboard?: () => void
  }>(),
  {
    stage: undefined,
    text: undefined,
    audio: undefined,
    picture: undefined,
    textOnly: false,
    viewedTimes: undefined,
    onEdit: () => {},
    onCopyTextToClipboard: () => {},
  },
)

const clipboardButton = ref<InstanceType<typeof AwesomeButton>>()
const voicePlayer = ref<InstanceType<typeof VoicePlayer>>()

const stageDisplayName = computed(() => {
  if (props.stage === undefined || props.stage === specialStages.OUTER_SPACE.name) {
    return ''
  } else {
    return stageNameMap.get(props.stage)?.displayName ?? props.stage
  }
})

const hasPicture = computed(() => !!props.picture)

function toggleAutoPlayVoice() {
  autoPlayVoice.value = !autoPlayVoice.value
}

function toggleAutoRepeatVoice() {
  autoRepeatVoice.value = !autoRepeatVoice.value
}

async function copyTextToClipboard() {
  if (props.text) {
    await navigator.clipboard.writeText(props.text)
    props.onCopyTextToClipboard()
  }
}

function resetClipboardButton() {
  clipboardButton.value?.reset()
}

function playVoice() {
  voicePlayer.value?.play()
}

function stopVoice() {
  voicePlayer.value?.stop()
}

defineExpose({
  resetClipboardButton,
  playVoice,
  stopVoice,
})
</script>

<style scoped>
.space-card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 10px;
  box-sizing: border-box;
  align-items: center;
}

.space-card-content--text {
  height: auto;
  min-height: 100%;
  justify-content: center;
}

.space-card-content--picture {
  width: 100%;
  gap: 6px;
}

.space-card-picture {
  flex: 1 1 0;
  min-height: 76%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.space-card-strip {
  height: 28px;
  font-size: clamp(1.1rem, 2vw, 1.2rem);
  color: var(--space-card--color--strip);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px;
  gap: 10px;
}

.space-card-strip-group {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.space-card-strip-text {
  background: none;
  font-size: inherit;
  color: inherit;
}

.space-card-text {
  margin: 0;
  width: 100%;
  color: var(--space-card--color);
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  text-align: center;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.space-card-content--picture .space-card-text {
  flex: 0 0 auto;
}

.space-card-button {
  --awesome-button--icon--size: clamp(1.2rem, 2vw, 1.3rem);
  --awesome-button--icon--color: var(--space-card--color--strip);
  --awesome-button--icon--color--hover: var(--space-card--color--strip--hover);
  --awesome-button--icon--color--active: var(--space-card--color--strip--hover);
}
</style>
