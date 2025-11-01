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
      @ended="isPlaying = false"
    />
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { ref, onBeforeUnmount, watch, onMounted } from 'vue'

const props = defineProps<{
  audioBlob?: Blob | undefined
}>()

const audioUrl = ref<string>()
const audioRef = ref<HTMLAudioElement>()
const isPlaying = ref(false)

function play() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) {
    console.warn('Cannot play - audio element or URL missing', {
      hasAudio: !!audio,
      hasUrl: !!audioUrl.value
    })
    return
  }
  console.log('Playing audio from ', audioUrl.value)
  audio.currentTime = 0
  audio.play().catch((error) => {
    console.error('Audio play failed:', error)
  })
}

function stop() {
  const audio = audioRef.value as HTMLAudioElement | undefined
  if (!audio || !audioUrl.value) return
  audio.pause()
  audio.currentTime = 0
}

function updateAudioUrl(blob: Blob | undefined) {
  if (blob && blob.size > 0) {
    console.log('Updating audio URL')
    try {
      if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
      audioUrl.value = URL.createObjectURL(blob)
    } catch (error) {
      console.error('Failed to create object URL', error, blob)
      audioUrl.value = undefined
    }
  } else {
    console.log('Unsetting audio URL')
    if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
    audioUrl.value = undefined
    if (blob) {
      console.log('Invalid blob provided:', blob)
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
  width: fit-content;
  height: 100%;
  border-radius: 999px;
}

.voice-player-button {
  height: 100%;
}

</style>
