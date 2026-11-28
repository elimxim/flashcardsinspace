<template>
  <button
    ref="tile"
    type="button"
    class="grid-tile"
    :class="{
      'grid-tile--selecting': selecting,
      'grid-tile--selected': selected,
    }"
    @click="onClick(flashcard)"
  >
    <img
      v-if="pictureUrl"
      :src="pictureUrl"
      class="grid-tile-picture"
      alt=""
      draggable="false"
      decoding="async"
    />
    <span v-else class="grid-tile-text">{{ flashcard.frontSide }}</span>
    <font-awesome-icon v-if="selected" icon="fa-solid fa-circle-check" class="grid-tile-check" />
  </button>
</template>

<script setup lang="ts">
import { type Flashcard } from '@/model/flashcard.ts'
import { galleryGridTileTrackerKey } from '@/utils/gallery-grid-tile-tracker.ts'
import { inject, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    flashcard: Flashcard
    selecting?: boolean
    selected?: boolean
    picture?: Blob | undefined
    onClick?: (flashcard: Flashcard) => void
  }>(),
  {
    selecting: false,
    selected: false,
    picture: undefined,
    onClick: () => {},
  },
)

const tile = ref<HTMLElement>()
const tracker = inject(galleryGridTileTrackerKey, undefined)
const pictureUrl = ref<string>()

function revokePictureUrl() {
  if (pictureUrl.value) URL.revokeObjectURL(pictureUrl.value)
  pictureUrl.value = undefined
}

watch(
  () => props.picture,
  (picture) => {
    revokePictureUrl()
    if (picture) pictureUrl.value = URL.createObjectURL(picture)
  },
  { immediate: true },
)

onMounted(() => {
  if (tile.value) tracker?.track(tile.value, props.flashcard.id)
})

onBeforeUnmount(() => {
  if (tile.value) tracker?.untrack(tile.value)
  revokePictureUrl()
})
</script>

<style scoped>
.grid-tile {
  position: relative;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: none;
  border-radius: 10px;
  background: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  container-type: inline-size;
  outline: none;
  cursor: zoom-in;
  transition: box-shadow 0.2s ease-in-out;
}

@media (hover: hover) {
  .grid-tile:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }
}

.grid-tile--selecting {
  cursor: pointer;
}

.grid-tile--selected {
  background: #f5f5f8;
}

.grid-tile--selected .grid-tile-text,
.grid-tile--selected .grid-tile-picture {
  opacity: 0.7;
}

.grid-tile-picture {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  user-select: none;
}

.grid-tile-check {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: clamp(14px, 12cqi, 24px);
  color: #007bff;
  background: white;
  border-radius: 50%;
}

.grid-tile-text {
  max-height: 100%;
  overflow: hidden;
  color: #2f3340;
  font-size: clamp(0.5rem, 11cqi, 1.3rem);
  line-height: 1.25;
  text-align: center;
  overflow-wrap: anywhere;
}
</style>
