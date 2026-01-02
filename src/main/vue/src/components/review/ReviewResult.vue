<template>
  <div class="review-result">
    <div class="empty-deck-message">
      {{ emptyMessage }}
    </div>
    <SmartButton
      text="Exit Review"
      class="calm-button exit-review-button"
      :on-click="goToControlPanel"
      fit-content
      auto-blur
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'
import SmartButton from '@/components/SmartButton.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

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
  border-radius: 24px;
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

.exit-review-button {
  --smart-button--padding: 10px;
}

</style>
