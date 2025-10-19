<template>
  <div class="launch-panel">
    <LaunchButton
      :on-click="startReview"
      :disabled="isDisabled"
    />
  </div>
</template>

<script setup lang="ts">
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'

const router = useRouter()
const flashcardStore = useFlashcardStore()

const { isEmpty, isSuspended } = storeToRefs(flashcardStore)

const isDisabled = computed(() => isEmpty.value || isSuspended.value)

function startReview() {
  router.push({ name: routeNames.review })
}
</script>

<style scoped>
.launch-panel {
  position: relative;
  display: flex;
  align-items: center;
  padding: 1px;
  height: 100%;
  width: 160px;
}
</style>
