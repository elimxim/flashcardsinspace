<template>
  <div class="voice-player">
    <AwesomeButton
      icon="fa-solid fa-volume-high"
      class="voice-player-button"
      :disabled="!audioUrl"
      :active="isPlaying"
      :fade="isPlaying"
      :on-click="isPlaying ? stop : play"
      click-ripple
      square
    />
    <audio
      ref="audioRef"
      :src="audioUrl"
      @play="isPlaying = true"
      @pause="isPlaying = false"
      @ended="handleAudioEnded"
    />
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { ref, onBeforeUnmount, watch, onMounted } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

const props = withDefaults(defineProps<{
  audioBlob?: Blob | undefined,
  loopPlay?: boolean,
}>(), {
  audioBlob: undefined,
  loopPlay: false,
})

const audioUrl = ref<string>()
const audioRef = ref<HTMLAudioElement>()
const isPlaying = ref(false)
const isLooping = ref(false)

function play() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) return

  if (props.loopPlay) {
    isLooping.value = true
  }

  audio.currentTime = 0
  audio.play()
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Audio play failed:', error)
    })
}

function stop() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) return
  isLooping.value = false
  audio.pause()
  audio.currentTime = 0
}

function handleAudioEnded() {
  isPlaying.value = false
  if (isLooping.value) {
    play()
  }
}

function updateAudioUrl(blob: Blob | undefined) {
  if (blob && blob.size > 0) {
    try {
      if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
      audioUrl.value = URL.createObjectURL(blob)
    } catch (error) {
      Log.error(LogTag.LOGIC, 'Failed to create object URL', error, blob)
      audioUrl.value = undefined
    }
  } else {
    if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
    audioUrl.value = undefined
    if (blob) {
      Log.error(LogTag.LOGIC, 'Invalid blob provided:', blob)
    }
  }
}

watch(() => props.audioBlob, (newVal) => {
  updateAudioUrl(newVal)
})

onMounted(() => {
  if (props.audioBlob) {
    updateAudioUrl(props.audioBlob)
  }
})

onBeforeUnmount(() => {
  isPlaying.value = false
  isLooping.value = false
  audioRef?.value?.pause()
  if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
})

defineExpose({
  play,
  stop,
})

</script>

<style scoped>
.voice-player {
  position: relative;
  width: fit-content;
  height: 100%;
  border-radius: 999px;
}

.voice-player-button {
  height: 100%;
}

</style>
