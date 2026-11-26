<template>
  <div class="space-picture" :class="{ 'space-picture--fill': fill }">
    <img
      v-if="imageUrl"
      :src="imageUrl"
      class="space-picture-img"
      :style="fillStyle"
      alt="Image"
      aria-hidden="true"
      @load="onImageLoad"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

const props = withDefaults(
  defineProps<{
    pictureBlob?: Blob | undefined
    fill?: boolean
  }>(),
  {
    pictureBlob: undefined,
    fill: false,
  },
)

const imageUrl = ref<string | undefined>()
const aspectRatio = ref<number>()

function onImageLoad(event: Event) {
  const image = event.target as HTMLImageElement
  if (image.naturalHeight > 0) {
    aspectRatio.value = image.naturalWidth / image.naturalHeight
  }
}

const fillStyle = computed(() =>
  props.fill && aspectRatio.value
    ? { width: `min(100cqw, ${aspectRatio.value * 100}cqh)`, maxWidth: 'none', maxHeight: 'none' }
    : undefined,
)

function revokeImage() {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
    imageUrl.value = undefined
  }
}

function applyBlob(blob: Blob | undefined) {
  revokeImage()
  if (blob && blob.size > 0) {
    try {
      imageUrl.value = URL.createObjectURL(blob)
    } catch (error) {
      Log.error(LogTag.LOGIC, 'SpacePicture: failed to create object URL', error)
    }
  }
}

watch(
  () => props.pictureBlob,
  (newBlob) => {
    applyBlob(newBlob)
  },
)

onMounted(() => {
  if (props.pictureBlob) {
    applyBlob(props.pictureBlob)
  }
})

onBeforeUnmount(() => {
  revokeImage()
})
</script>

<style scoped>
.space-picture {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.space-picture--fill {
  container-type: size;
}

.space-picture-img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  display: block;
  border-radius: 12px;
}
</style>
