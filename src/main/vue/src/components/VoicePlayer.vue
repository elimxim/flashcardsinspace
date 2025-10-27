<template>
  <div class="voice-player">
    <AwesomeButton
      icon="fa-solid fa-volume-high"
      class="voice-player-button"
      style="height: 100%;"
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
      @ended="isPlaying = false"
    />
  </div>
</template>

<script setup lang="ts" generic="T extends Blob = Blob">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { ref, onBeforeUnmount, watch } from 'vue'

const props = defineProps<{
  audioBlob?: Blob | undefined
}>()

const audioUrl = ref<string>()
const audioRef = ref<HTMLAudioElement>()
const isPlaying = ref(false)

function play() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) return
  audio.currentTime = 0
  audio.play().catch((error) => {
    console.warn('Audio play failed:', error)
  })
}

function stop() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) return
  audio.pause()
  audio.currentTime = 0
}

watch(() => props.audioBlob, (newVal) => {
  if (newVal) {
    if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
    audioUrl.value = URL.createObjectURL(newVal)
  } else {
    if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
    audioUrl.value = undefined
  }
})

onBeforeUnmount(() => {
  isPlaying.value = false
  audioRef?.value?.pause()
  if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
})

defineExpose({
  play
})

</script>

<style scoped>
.voice-player {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  width: fit-content;
  height: 30px;
  border-radius: 999px;
}

</style>
