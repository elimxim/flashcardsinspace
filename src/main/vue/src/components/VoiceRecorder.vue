<template>
  <div class="voice-recorder voice-recorder--theme">
    <AwesomeButton
      icon="fa-solid fa-microphone"
      class="voice-recorder-mic-button"
      style="height: 100%;"
      :on-click="toggleControls"
      :active="isControlsExpanded"
      click-ripple
      square
    />
    <transition name="voice-controls-slide">
      <template v-if="isControlsExpanded">
        <div v-if="!isSupported" class="voice-warning-text">
          Your browser doesn’t support voice recording
        </div>
        <div v-else class="voice-recorder-controls">
          <AwesomeButton
            v-if="!isRecording || isPaused"
            icon="fa-solid fa-play"
            class="voice-recorder-control-button"
            :on-click="!isRecording ? startRecording : isPaused ? resumeRecording : pauseRecording"
          />
          <AwesomeButton
            v-if="isRecording && !isPaused"
            icon="fa-solid fa-pause"
            class="voice-recorder-control-button"
            :on-click="pauseRecording"
          />
          <AwesomeButton
            icon="fa-solid fa-stop"
            class="voice-recorder-control-button"
            :disabled="!isRecording"
            :on-click="stopRecording"
          />
          <div class="voice-recorder-time">
            {{ recordingTime }}
          </div>
          <AwesomeButton
            icon="fa-solid fa-trash"
            class="voice-recorder-control-button"
            :disabled="!audioBlob"
            :on-click="removeRecording"
          />
        </div>
      </template>
    </transition>
    <VoicePlayer
      class="voice-recorder-play-button"
      :audio-blob="audioBlob"
    />
  </div>
</template>

<script lang="ts" setup>
import AwesomeButton from '@/components/AwesomeButton.vue'
import VoicePlayer from '@/components/VoicePlayer.vue'
import { ref, computed, onBeforeUnmount, watch } from 'vue'

const audioBlob = defineModel<Blob | undefined>()

const props = withDefaults(defineProps<{
  maxDuration?: number
}>(), {
  maxDuration: 20 * 1000
})

const isControlsExpanded = ref(false)
const isRecording = ref(false)
const isPaused = ref(false)

const mediaStream = ref<MediaStream>()
const mediaRecorder = ref<MediaRecorder>()
const blobChunks = ref<BlobPart[]>([])
const startTime = ref<number>(0)
const elapsedMs = ref<number>(0)
let timerIntervalId: number | undefined

const mimeCandidates: string[] = [
  'audio/webm;codecs=opus',
  'audio/ogg;codecs=opus',
  'audio/mp4',
  'audio/webm',
  'audio/ogg',
]

const isSupported = computed(() => {
  return typeof window !== 'undefined' && 'MediaRecorder' in window
    && typeof navigator !== 'undefined' && !!navigator.mediaDevices?.getUserMedia
})

const recordingTime = computed(() => {
  const s = Math.floor(elapsedMs.value / 1000)
  const mm = String(Math.floor(s / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${mm}:${ss}`
})

function pickAudioMimeType(): string | undefined {
  for (const mime of mimeCandidates) {
    try {
      if (window.MediaRecorder.isTypeSupported?.(mime)) {
        return mime
      }
    } catch {
      console.info(`${mime} is not supported`)
    }
  }
}

async function ensureStream(): Promise<MediaStream> {
  if (mediaStream.value) return mediaStream.value
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
  mediaStream.value = stream
  return stream
}

function startTimer() {
  stopTimer()
  startTime.value = performance.now() - elapsedMs.value
  timerIntervalId = window.setInterval(() => {
    elapsedMs.value = performance.now() - startTime.value
  }, 250)
}

function stopTimer() {
  if (timerIntervalId) {
    clearInterval(timerIntervalId)
    timerIntervalId = undefined
  }
}

function toggleControls() {
  if (isRecording.value) pauseRecording()
  isControlsExpanded.value = !isControlsExpanded.value
}

async function startRecording() {
  if (!isSupported.value) return

  const stream = await ensureStream()
  const mimeType = pickAudioMimeType()

  blobChunks.value = []
  mediaRecorder.value = new MediaRecorder(stream,
    mimeType ? { mimeType: mimeType } : undefined
  )

  mediaRecorder.value.ondataavailable = (e: BlobEvent) => {
    if (e.data && e.data.size > 0) blobChunks.value.push(e.data)
  }

  mediaRecorder.value.onstop = () => {
    audioBlob.value = new Blob(blobChunks.value, { type: mediaRecorder.value?.mimeType })
  }

  mediaRecorder.value.start()
  isRecording.value = true
  isPaused.value = false
  elapsedMs.value = 0
  startTimer()
}

function pauseRecording() {
  if (!mediaRecorder.value) return
  mediaRecorder.value.pause()
  isPaused.value = true
  stopTimer()
}

function resumeRecording() {
  if (!mediaRecorder.value) return
  mediaRecorder.value.resume()
  isPaused.value = false
  startTimer()
}

function stopRecording() {
  if (!mediaRecorder.value) return
  mediaRecorder.value.stop()
  isRecording.value = false
  isPaused.value = false
  stopTimer()
  console.log(`Audio recorded: ${audioSize()} KB`)
}

function removeRecording() {
  audioBlob.value = undefined
  blobChunks.value = []
  elapsedMs.value = 0
}

function audioSize() {
  return audioBlob.value ? (audioBlob.value.size / 1024).toFixed(1) : '0.0'
}

function resetState() {
  mediaStream.value?.getTracks().forEach((t) => t.stop())
  mediaStream.value = undefined
  blobChunks.value = []
  elapsedMs.value = 0
  mediaRecorder.value?.stop()
  isRecording.value = false
  isPaused.value = false
  stopTimer()
}

watch(elapsedMs, (newVal) => {
  if (isRecording.value && newVal >= props.maxDuration) {
    stopRecording()
  }
})

watch(audioBlob, (newVal) => {
  if (!newVal && !isRecording.value) {
    resetState()
  }
})

onBeforeUnmount(() => {
  resetState()
})

</script>

<style scoped>
.voice-recorder--theme {
  --v-recorder--time--color: var(--voice-recorder--time--color, rgba(0, 0, 0, 0.9));
  --v-recorder--time--bg: var(--voice-recorder--controls--bg, rgba(255, 255, 255, 0.52));
  --v-recorder--controls--bg: var(--voice-recorder--controls--bg, rgba(87, 87, 87, 0.15));
  --v-recorder--warning-text--color: var(--voice-recorder--warning-text--color, #404040);
}

.voice-recorder {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  width: fit-content;
  height: 30px;
  border-radius: 999px;
  transition: all 0.3s;
}

.voice-warning-text {
  font-size: 0.9rem;
  color: var(--v-recorder--warning-text--color);
}

.voice-recorder-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  background: var(--v-recorder--controls--bg);
  border-radius: 999px;
  padding: 3px 16px;
}

.voice-recorder-time {
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  color: var(--v-recorder--time--color);
  background: var(--v-recorder--time--bg);
  border-radius: 999px;
  text-align: center;
  text-wrap: nowrap;
  padding: 3px 16px;
}

.voice-controls-slide-enter-active {
  animation: slideIn 0.3s ease-out;
}

.voice-controls-slide-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  0% {
    opacity: 1;
    max-width: 0;
    margin: 0;
    padding: 0;
  }
  100% {
    opacity: 1;
    max-width: 500px;
    padding: 3px 16px;
  }
}

@keyframes slideOut {
  0% {
    opacity: 1;
    max-width: 500px;
    padding: 3px 16px;
  }
  100% {
    opacity: 0;
    max-width: 0;
    margin: 0;
    padding: 0;
  }
}

</style>
