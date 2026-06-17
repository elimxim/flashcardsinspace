<template>
  <div class="picture-uploader picture-uploader--theme">
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display: none;"
      @change="handleFileChange"
    />
    <transition name="picture-toggle-flip" mode="out-in">
      <AwesomeButton
        v-if="!hasPicture"
        key="pick"
        icon="fa-solid fa-image"
        class="picture-uploader-button picture-uploader-button--pick"
        style="height: 100%;"
        :on-click="toggleControls"
        :active="expanded"
        square
      />
      <PictureThumbButton
        v-else
        key="thumb"
        ref="thumbButton"
        v-model:url="previewUrl"
        class="picture-uploader-thumb-button"
        :on-click="toggleControls"
        :active="expanded"
      />
    </transition>

    <transition name="picture-controls-slide">
      <div v-if="expanded" class="picture-uploader-controls-wrapper">
        <div class="picture-uploader-controls">
          <AwesomeButton
            icon="fa-solid fa-file-arrow-up"
            class="picture-uploader-button"
            :disabled="processing"
            :on-click="openPicker"
          />
          <div
            class="picture-uploader-size"
            :class="{ 'picture-uploader-size--processing': processing }"
          >{{ sizeLabel }}</div>
          <AwesomeButton
            icon="fa-solid fa-eye"
            class="picture-uploader-button"
            :disabled="!pictureBlob"
            :on-click="openPreview"
          />
          <AwesomeButton
            icon="fa-solid fa-trash"
            class="picture-uploader-button"
            :disabled="!pictureBlob"
            :on-click="removePicture"
          />
        </div>
      </div>
    </transition>
    <Teleport to="body">
      <transition name="picture-preview-fade">
        <div
          v-if="previewOpen && previewUrl"
          class="picture-preview-overlay"
          @click="closePreview"
        >
          <img :src="previewUrl" class="picture-preview-image" alt="Picture preview"/>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script lang="ts" setup>
import AwesomeButton from '@/components/AwesomeButton.vue'
import PictureThumbButton from '@/components/PictureThumbButton.vue'
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { decodeAndResize, encodeToWebp } from '@/utils/image-processing.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { Log, LogTag } from '@/utils/logger.ts'

const MAX_RAW_SIZE_BYTES = 30 * 1024 * 1024 // 30 MB
const MAX_PROCESSED_SIZE_BYTES = 500 * 1024 // 500 KB

const pictureBlob = defineModel<Blob | undefined>('picture-blob', { required: true })
const expanded = defineModel<boolean>('expanded', { default: false })

const thumbButton = ref<InstanceType<typeof PictureThumbButton>>()
const fileInputRef = ref<HTMLInputElement>()
const previewUrl = ref<string | undefined>()
const previewOpen = ref(false)
const processing = ref(false)
const encodeController = ref<AbortController>()

const hasPicture = computed(() => !!pictureBlob.value)
const sizeLabel = computed(() => formatSize(pictureBlob.value?.size ?? 0))

function formatSize(bytes: number): string {
  if (bytes >= 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  return `${Math.round(bytes / 1024)} KB`
}

function toggleControls() {
  expanded.value = !expanded.value
}

function openPicker() {
  fileInputRef.value?.click()
}

function openPreview() {
  if (!previewUrl.value) return
  previewOpen.value = true
  // Capture phase so we intercept Escape before the surrounding Modal's window
  // listener and stop it there - otherwise Escape closes the modal too.
  window.addEventListener('keydown', onPreviewKeydown, { capture: true })
}

function closePreview() {
  previewOpen.value = false
  window.removeEventListener('keydown', onPreviewKeydown, { capture: true })
}

function onPreviewKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    closePreview()
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return

  Log.log(LogTag.LOGIC, `PictureUploader: selected "${file.name}" type=${file.type || 'unknown'} size=${(file.size / 1024).toFixed(1)} KB`)
  if (file.size > MAX_RAW_SIZE_BYTES) {
    useSpaceToaster().bakeError(userApiErrors.PICTURE__TOO_LARGE)
    Log.log(LogTag.LOGIC, `PictureUploader: rejected - raw file too large (${(file.size / 1024 / 1024).toFixed(1)} MB, max 4 MB)`)
    return
  }

  processing.value = true
  encodeController.value = new AbortController()
  try {
    const imageData = await decodeAndResize(file)
    const blob = await encodeToWebp(imageData, encodeController.value.signal)
    if (blob.size > MAX_PROCESSED_SIZE_BYTES) {
      useSpaceToaster().bakeError(userApiErrors.PICTURE__TOO_LARGE_AFTER_COMPRESSION)
      Log.log(LogTag.LOGIC, `PictureUploader: rejected - processed still too large (${(blob.size / 1024).toFixed(1)} KB, max 500 KB)`)
      return
    }
    pictureBlob.value = blob
    expanded.value = true
    Log.log(LogTag.LOGIC, `PictureUploader: accepted processed webp ${(blob.size / 1024).toFixed(1)} KB`)
  } catch (error) {
    if (encodeController.value.signal.aborted) {
      Log.log(LogTag.LOGIC, 'PictureUploader: processing aborted')
      return
    }
    useSpaceToaster().bakeError(userApiErrors.PICTURE__UPLOADING_FAILED)
    Log.error(LogTag.LOGIC, 'PictureUploader: failed to process image', error)
  } finally {
    encodeController.value = undefined
    processing.value = false
  }
}

function removePicture() {
  pictureBlob.value = undefined
  closePreview()
  Log.log(LogTag.LOGIC, 'PictureUploader: picture removed')
}

watch(pictureBlob, (newBlob) => {
  thumbButton.value?.revoke()
  if (newBlob) {
    try {
      previewUrl.value = URL.createObjectURL(newBlob)
    } catch (error) {
      Log.error(LogTag.LOGIC, 'PictureUploader: failed to create object URL for preview', error)
    }
  } else {
    previewOpen.value = false
  }
}, { immediate: true })

onBeforeUnmount(() => {
  encodeController.value?.abort()
  thumbButton.value?.revoke()
  window.removeEventListener('keydown', onPreviewKeydown, { capture: true })
})

</script>

<style scoped>
.picture-uploader--theme {
  --p-uploader--button--color: var(--picture-uploader--button--color, rgba(87, 87, 87, 0.86));
  --p-uploader--button--color--hover: var(--picture-uploader--button--color--hover, rgba(0, 0, 0, 0.9));
  --p-uploader--button--color--active: var(--picture-uploader--button--color--active, rgba(0, 0, 0, 0.9));
  --p-uploader--button--color--disabled: var(--picture-uploader--button--color--disabled, rgba(202, 202, 202, 0.9));
  --p-uploader--controls--bg: var(--picture-uploader--controls--bg, rgba(87, 87, 87, 0.15));
  --p-uploader--pick-button--bg--hover: var(--picture-uploader--mic-button--bg--hover, rgba(87, 87, 87, 0.12));
  --p-uploader--pick-button--bg--active: var(--picture-uploader--mic-button--bg--active, rgba(87, 87, 87, 0.18));
  --p-uploader--size--color: var(--picture-uploader--time--color, rgba(0, 0, 0, 0.9));
  --p-uploader--size--bg: var(--picture-uploader--time--bg, rgba(255, 255, 255, 0.52));
  --p-uploader--size--stripe: var(--picture-uploader--size--stripe, rgba(87, 87, 87, 0.35));
}

.picture-uploader {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  width: fit-content;
  height: 40px;
  border-radius: 999px;
  transition: width 0.3s ease-out;
  perspective: 600px;
}

.picture-toggle-flip-enter-active {
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;
  backface-visibility: hidden;
}

.picture-toggle-flip-leave-active {
  transition: transform 0.15s ease-in, opacity 0.15s ease-in;
  backface-visibility: hidden;
}

.picture-toggle-flip-enter-from {
  transform: rotateY(-90deg);
  opacity: 0;
}

.picture-toggle-flip-leave-to {
  transform: rotateY(90deg);
  opacity: 0;
}

.picture-uploader-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  background: var(--p-uploader--controls--bg);
  border-radius: 999px;
  padding: 4px 8px;
}

.picture-uploader-button {
  --awesome-button--icon--size: 22px;
  --awesome-button--icon--color: var(--p-uploader--button--color);
  --awesome-button--icon--color--hover: var(--p-uploader--button--color--hover);
  --awesome-button--icon--color--active: var(--p-uploader--button--color--active);
  --awesome-button--icon--color--disabled: var(--p-uploader--button--color--disabled);
  --awesome-button--border-radius: 999px;
  --awesome-button--padding: 4px;
}

.picture-uploader-button--pick {
  --awesome-button--bg--hover: var(--p-uploader--pick-button--bg--hover);
  --awesome-button--bg--active: var(--p-uploader--pick-button--bg--active);
}

.picture-uploader-thumb-button {
  --picture-thumb-button--size: 36px;
  --picture-thumb-button--ring-color: var(--p-uploader--button--color);
  --picture-thumb-button--ring-color--hover: var(--p-uploader--button--color--hover);
  --picture-thumb-button--ring-color--active: var(--p-uploader--button--color--active);
}

.picture-uploader-size {
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  color: var(--p-uploader--size--color);
  background: var(--p-uploader--size--bg);
  border-radius: 999px;
  text-align: center;
  text-wrap: nowrap;
  padding: 3px 12px;
}

.picture-uploader-size--processing {
  color: transparent;
  background-image: linear-gradient(
    45deg,
    var(--p-uploader--size--stripe) 25%,
    transparent 25%,
    transparent 50%,
    var(--p-uploader--size--stripe) 50%,
    var(--p-uploader--size--stripe) 75%,
    transparent 75%,
    transparent
  );
  background-size: 28px 28px;
  animation: picture-uploader-barber 0.8s linear infinite;
}

@keyframes picture-uploader-barber {
  from {
    background-position: 0 0;
  }
  to {
    background-position: 28px 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .picture-uploader-size--processing {
    animation: none;
  }
}

.picture-uploader-controls-wrapper {
  overflow: hidden;
  max-width: max-content;
}

.picture-controls-slide-enter-active,
.picture-controls-slide-leave-active {
  transition: max-width 0.4s ease-out, opacity 0.4s ease-out;
}

.picture-controls-slide-enter-from {
  max-width: 0;
  opacity: 0;
}

.picture-controls-slide-enter-to {
  max-width: 260px;
  opacity: 1;
}

.picture-controls-slide-leave-from {
  max-width: 260px;
  opacity: 1;
}

.picture-controls-slide-leave-to {
  max-width: 0;
  opacity: 0;
}

.picture-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: grid;
  place-items: center;
  padding: 4vmin;
  cursor: zoom-out;
  z-index: 1000;
}

.picture-preview-image {
  max-width: 92vw;
  max-height: 92vh;
  object-fit: contain;
  border-radius: 8px;
}

.picture-preview-fade-enter-active,
.picture-preview-fade-leave-active {
  transition: opacity 0.2s ease-in-out;
}

.picture-preview-fade-enter-from,
.picture-preview-fade-leave-to {
  opacity: 0;
}
</style>
