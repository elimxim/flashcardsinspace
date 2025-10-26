<template>
  <div class="voice-recorder voice-recorder--theme">
    <AwesomeButton
      icon="fa-solid fa-microphone"
      class="voice-recorder-button voice-recorder-mic-button"
      :on-click="toggleControls"
      :active="isControlsExpanded"
      square
    />
    <template v-if="isControlsExpanded">
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
          icon="fa-solid fa-trash"
          class="voice-recorder-button"
          :disabled="!audioURL"
          :on-click="discardRecording"
        />
      </div>
    </template>
    <VoicePlayer :audio-url="audioURL"/>
  </div>
</template>

<script lang="ts" setup>
import AwesomeButton from '@/components/AwesomeButton.vue'
import VoicePlayer from '@/components/VoicePlayer.vue'
import { ref, computed, onBeforeUnmount } from 'vue'
import axios, { AxiosError, CancelTokenSource } from 'axios'

const isControlsExpanded = ref(false)
const isRecording = ref(false)
const isPaused = ref(false)

const mediaStream = ref<MediaStream>()
const mediaRecorder = ref<MediaRecorder>()
const audioBlob = ref<Blob>()
const audioURL = ref<string>()
const blobChunks = ref<BlobPart[]>([])
const currentMime = ref<string>('')
const startTime = ref<number>(0)
const elapsedMs = ref<number>(0)
const isUploading = ref(false)
const uploadPct = ref<number>(0)
let timerIntervalId: number | undefined
let cancelSrc: CancelTokenSource | undefined

const audioMimeCandidates: string[] = [
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

const niceSize = computed(() =>
  audioBlob.value ? (audioBlob.value.size / 1024).toFixed(1) : '0.0'
)

function pickAudioMimeType(): string | undefined {
  for (const mime of audioMimeCandidates) {
    try {
      if ((window as any).MediaRecorder.isTypeSupported?.(mime)) {
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
  startTime.value = performance.now() - elapsedMs.value
  stopTimer()
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
  currentMime.value = mimeType || ''
  mediaRecorder.value = new MediaRecorder(stream,
    mimeType ? { mimeType: mimeType } : undefined
  )

  mediaRecorder.value.ondataavailable = (e: BlobEvent) => {
    if (e.data && e.data.size > 0) blobChunks.value.push(e.data)
  }

  mediaRecorder.value.onstop = async () => {
    const finalMime = mediaRecorder.value?.mimeType || currentMime.value || 'audio/webm'
    currentMime.value = finalMime
    audioBlob.value = new Blob(blobChunks.value, { type: finalMime })
    audioURL.value = URL.createObjectURL(audioBlob.value)
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
}

function discardRecording() {
  if (audioURL.value) URL.revokeObjectURL(audioURL.value)
  audioURL.value = undefined
  audioBlob.value = undefined
  blobChunks.value = []
  elapsedMs.value = 0
}

// ---- Axios upload ----
type UploadResponse = {
  ok: boolean;
  path: string;
  type: string;
  size: number;
};

async function uploadRecording() {
  if (!audioBlob.value) return;

  const ext = currentMime.value.includes('ogg') ? 'ogg'
    : currentMime.value.includes('mp4') ? 'm4a'
      : 'webm';

  const file = new File([audioBlob.value], `voice-${Date.now()}.${ext}`, {
    type: audioBlob.value.type || currentMime.value || 'application/octet-stream',
  });

  const form = new FormData();
  form.append('file', file);

  try {
    isUploading.value = true;
    uploadPct.value = 0;
    cancelSrc = axios.CancelToken.source();

    const { data } = await axios.post<UploadResponse>('/api/upload-audio', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total) uploadPct.value = Math.round((e.loaded / e.total) * 100);
      },
      cancelToken: cancelSrc.token,
      // optional: timeout: 60_000,
    });

    if (!data?.ok) throw new Error('Upload failed');
    alert(`Uploaded ✓ (${Math.round(data.size / 1024)} KB)`);
  } catch (err) {
    if (axios.isCancel(err)) {
      alert('Upload canceled.');
    } else {
      const msg = (err as AxiosError)?.message ?? 'Upload failed';
      alert(msg);
    }
  } finally {
    isUploading.value = false;
    uploadPct.value = 0;
    cancelSrc = undefined;
  }
}

function cancelUpload() {
  cancelSrc?.cancel('User canceled')
}

onBeforeUnmount(() => {
  stopTimer()
  if (audioURL.value) URL.revokeObjectURL(audioURL.value)
  mediaStream.value?.getTracks().forEach((t) => t.stop())
  mediaStream.value = undefined
});
</script>

<style scoped>
.voice-recorder--theme {

}

.voice-recorder {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  width: fit-content;
  height: 30px;
  border-radius: 999px;
}

.voice-warning-text {
  font-size: 0.9rem;
  color: #404040;
}

.voice-recorder-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  background: rgba(87, 87, 87, 0.15);
  border-radius: 999px;
  padding: 3px 16px;
}

.voice-recorder-button {
  --awesome-button--icon--size: 18px;
  --awesome-button--icon--color: rgba(87, 87, 87, 0.86);
}

.voice-recorder-mic-button {
  --awesome-button--icon--color--active: rgba(87, 87, 87, 0.86);
  --awesome-button--bg--hover: rgba(87, 87, 87, 0.15);
  --awesome-button--bg--active: rgba(87, 87, 87, 0.15);
  --awesome-button--border-radius: 999px;
  height: 30px;
}

.voice-recorder-time {
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  color: rgba(0, 0, 0, 0.9);
  background: rgba(255, 255, 255, 0.52);
  border-radius: 999px;
  text-align: center;
  text-wrap: nowrap;
  padding: 3px 16px;
}

</style>
