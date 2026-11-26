<template>
  <div class="gallery-widget">
    <AwesomeButton
      icon="fa-solid fa-film"
      class="cp-widget"
      :disabled="!flashcardSet"
      :on-click="openGallery"
      await-tap-animation
      fill-space
    >
      <template #below>
        <div class="gallery-button-slot">
          <div class="cp-text">Gallery</div>
        </div>
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const router = useRouter()
const { flashcardSet } = storeToRefs(useFlashcardStore())

async function openGallery() {
  await router.push({ name: routeNames.flashcardsGallery })
}
</script>

<style scoped>
.gallery-widget {
  position: relative;
  width: 100%;
  height: 100%;
}

.gallery-button-slot {
  margin-top: 6px;
}
</style>
