<template>
  <div class="app-container">
    <div class="menu">
      <FlashcardMenu/>
    </div>

    <div class="review-form">
      <FlashcardReviewForm/>
    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardMenu from '@/components/FlashcardMenu.vue'
import FlashcardReviewForm from '@/components/FlashcardReviewForm.vue';


</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: row;
}

.menu {
  padding: 4px;
  background-color: #f0f0f0;
}

.review-form {
  flex: 10;
  padding: 4px;
  background: none;
}

</style>
