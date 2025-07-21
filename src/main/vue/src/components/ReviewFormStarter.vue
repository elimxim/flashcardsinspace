<template>
  <div class="review-form-starter" v-if="!started">
    <button class="review-button review-start-button"
            @click="startReview">
      Start review
    </button>
  </div>
</template>

<script setup lang="ts">
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { storeToRefs } from 'pinia'
import { onMounted, onUnmounted } from 'vue'

const globalStateStore = useGlobalStateStore()
const reviewStateStore = useReviewStateStore()
const { started } = storeToRefs(reviewStateStore)

function startReview() {
  reviewStateStore.startReview('Flashcards')
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (globalStateStore.isAnyModalFormOpen()) {
    return
  }

  if (event.key == 'Enter' && !started.value) {
    startReview()
  }
}
</script>

<style scoped>
.review-form-starter {
  height: 100%;
  width: 100%;
  padding: 0;
  margin: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.review-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  margin-bottom: 8%;
}

.review-start-button {
  font-size: 1.5em;
  background-color: #6db37c;
  color: white;
}

.review-start-button:hover {
  background-color: #519c53;
}
</style>
