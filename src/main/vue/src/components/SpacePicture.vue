<template>
  <div class="space-picture">
    <img
      v-if="imageUrl"
      :src="imageUrl"
      class="space-picture-img"
      alt="Image"
      aria-hidden="true"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

const props = withDefaults(defineProps<{
  pictureBlob?: Blob | undefined
}>(), {
  pictureBlob: undefined,
})

const imageUrl = ref<string | undefined>()

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

watch(() => props.pictureBlob, (newBlob) => {
  applyBlob(newBlob)
})

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

.space-picture-img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  display: block;
  border-radius: 12px;
}

</style>
