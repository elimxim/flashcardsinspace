<template>
  <div class="voice-recorder voice-recorder--theme">
    <AwesomeButton
      icon="fa-solid fa-microphone"
      class="voice-recorder-button voice-recorder-button--mic"
      style="height: 100%;"
      :on-click="toggleControls"
      :active="isControlsExpanded"
      :click-ripple="!expanded"
      square
    />
    <transition name="voice-controls-slide">
      <div v-if="isControlsExpanded" class="voice-recorder-controls-wrapper">
        <div v-if="!isSupported" class="voice-warning-text">
          Your browser doesn’t support voice recording
        </div>
        <div v-else class="voice-recorder-controls">
          <AwesomeButton
            v-if="!isRecording || isPaused"
            icon="fa-solid fa-play"
            class="voice-recorder-button"
            :on-click="!isRecording ? startRecording : isPaused ? resumeRecording : pauseRecording"
          />
          <AwesomeButton
            v-if="isRecording && !isPaused"
            icon="fa-solid fa-pause"
            class="voice-recorder-button"
            :on-click="pauseRecording"
          />
          <AwesomeButton
            icon="fa-solid fa-stop"
            class="voice-recorder-button"
            :disabled="!isRecording"
            :on-click="stopRecording"
          />
          <div class="voice-recorder-time">
            {{ recordingTime }}
          </div>
          <AwesomeButton
            v-if="!noTrash"
            icon="fa-solid fa-trash"
            class="voice-recorder-button"
            :disabled="!audioBlob"
            :on-click="removeRecording"
          />
        </div>
      </div>
    </transition>
    <VoicePlayer
      class="voice-recorder-button voice-recorder-button--play"
      :audio-blob="audioBlob"
    />
  </div>
</template>

<script lang="ts" setup>
import AwesomeButton from '@/components/AwesomeButton.vue'
import VoicePlayer from '@/components/VoicePlayer.vue'
import { ref, computed, onBeforeUnmount, watch } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

const audioBlob = defineModel<Blob | undefined>()

const props = withDefaults(defineProps<{
  maxDuration?: number
  expanded?: boolean
  noTrash?: boolean
}>(), {
  maxDuration: 20 * 1000,
  expanded: false,
  noTrash: false,
})

const isControlsExpanded = ref(props.expanded)
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
  if (props.expanded) return
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
  Log.log(LogTag.LOGIC, `Audio recorded: ${audioSize()} KB`)
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
  --v-recorder--time--bg: var(--voice-recorder--time--bg, rgba(255, 255, 255, 0.52));
  --v-recorder--controls--bg: var(--voice-recorder--controls--bg, rgba(87, 87, 87, 0.15));
  --v-recorder--button--color: var(--voice-recorder--button--color, rgba(87, 87, 87, 0.86));
  --v-recorder--button--color--hover: var(--voice-recorder--button--color--hover, rgba(0, 0, 0, 0.9));
  --v-recorder--button--color--active: var(--voice-recorder--button--color--active, rgba(0, 0, 0, 0.9));
  --v-recorder--button--color--disabled: var(--voice-recorder--button--color--disabled, rgba(202, 202, 202, 0.9));
  --v-recorder--mic-button--bg--hover: var(--voice-recorder--mic-button--bg--hover, rgba(87, 87, 87, 0.12));
  --v-recorder--mic-button--bg--active: var(--voice-recorder--mic-button--bg--active, rgba(87, 87, 87, 0.18));
  --v-recorder--play-button--bg--hover: var(--voice-recorder--play-button--bg--hover, rgba(87, 87, 87, 0.12));
  --v-recorder--play-button--bg--active: var(--voice-recorder--play-button--bg--active, rgba(87, 87, 87, 0.18));
  --v-recorder--warning-text--color: var(--voice-recorder--warning-text--color, #404040);
}

.voice-recorder {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  width: fit-content;
  height: 32px;
  border-radius: 999px;
  transition: width 0.3s ease-out;
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
  padding: 4px 8px;
}

.voice-recorder-button {
  --awesome-button--icon--size: 18px;
  --awesome-button--icon--color: var(--v-recorder--button--color);
  --awesome-button--icon--color--hover: var(--v-recorder--button--color--hover);
  --awesome-button--icon--color--active: var(--v-recorder--button--color--active);
  --awesome-button--icon--color--disabled: var(--v-recorder--button--color--disabled);
  --awesome-button--border-radius: 999px;
  --awesome-button--padding: 2px;
}

.voice-recorder-button--mic {
  --awesome-button--bg--hover: var(--v-recorder--mic-button--bg--hover);
  --awesome-button--bg--active: var(--v-recorder--mic-button--bg--active);
}

.voice-recorder-button--play {
  --awesome-button--bg--hover: var(--v-recorder--play-button--bg--hover);
  --awesome-button--bg--active: var(--v-recorder--play-button--bg--active);
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

.voice-recorder-controls-wrapper {
  overflow: hidden;
  max-width: max-content;
}

.voice-controls-slide-enter-active,
.voice-controls-slide-leave-active {
  transition: max-width 0.3s ease-out, opacity 0.3s ease-out;
}

.voice-controls-slide-enter-from {
  max-width: 0;
  opacity: 0;
}

.voice-controls-slide-enter-to {
  max-width: 200px;
  opacity: 1;
}

.voice-controls-slide-leave-from {
  max-width: 200px;
  opacity: 1;
}

.voice-controls-slide-leave-to {
  max-width: 0;
  opacity: 0;
}

</style>
