<template>
  <div class="review-form-starter">
    <button
      class="review-button review-start-button"
      :class="{ 'review-start-button-disabled': isEmpty }"
      :disabled="isEmpty"
      @click="startReview">
      Start review
    </button>
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const router = useRouter()
const flashcardSetStore = useFlashcardSetStore()

const { isEmpty } = storeToRefs(flashcardSetStore)

function startReview() {
  router.push({ name: routeNames.review })
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

.review-start-button:not(.review-start-button-disabled):hover {
  background-color: #519c53;
}

.review-start-button-disabled {
  background-color: #ededed;
  color: #cacaca;
  cursor: default;
}
</style>
