<template>
  <ReviewSummary>
    <div class="review-result">
      <div class="empty-deck-message">
        {{ emptyMessage }}
      </div>
      <div class="review-result-buttons">
        <SmartButton
          v-if="onBack"
          text="Back"
          class="off-button review-result-button"
          :on-click="onBack"
          fit-content
          auto-blur
        />
        <SmartButton
          text="Finish Review"
          class="calm-button review-result-button"
          :on-click="goToControlPanel"
          fit-content
          auto-blur
        />
      </div>
    </div>
  </ReviewSummary>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'
import SmartButton from '@/components/common/SmartButton.vue'
import ReviewSummary from '@/components/review/summary/ReviewSummary.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

defineProps<{
  total: number
  elapsedTime: number
  onBack?: () => void
}>()

const router = useRouter()

const emptyMessage = ref(deckEmptyMessage())

function goToControlPanel() {
  router.push({ name: routeNames.controlPanel })
}
</script>

<style scoped>
.review-result {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: clamp(8px, 1.5cqw, 12px);
  padding: clamp(8px, 1.2cqw, 12px);
  container-type: inline-size;
}

.empty-deck-message {
  font-size: clamp(1.2rem, 3cqw, 1.5rem);
  font-weight: 500;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-align: center;
  text-wrap: balance;
  color: rgba(100, 116, 139, 0.9);
}

.review-result-buttons {
  display: flex;
  gap: 10px;
}

.review-result-button {
  --smart-button--padding: 10px;
}
</style>
