<template>
  <div class="app-container">
    <div class="menu-area">
      <MainMenu/>
    </div>

    <div class="main-area" v-if="!started">
      <ReviewFormStarter/>
    </div>

    <div class="main-area" v-if="started">
      <ReviewForm/>
    </div>
  </div>
</template>

<script setup lang="ts">
import MainMenu from '@/components/MainMenu.vue'
import ReviewFormStarter from '@/components/ReviewFormStarter.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { storeToRefs } from 'pinia'

const reviewStateStore = useReviewStateStore()
const { started } = storeToRefs(reviewStateStore)

onBeforeRouteLeave((to, from, next) => {
  reviewStateStore.finishReview()
  next()
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: row;
}

.menu-area {
  flex: 1;
  background-color: #f0f0f0;
}

.main-area {
  flex: 10;
  background: none;
}

</style>
