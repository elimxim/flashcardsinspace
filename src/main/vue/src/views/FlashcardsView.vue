<template>
  <div class="container">
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
.container {
  display: flex;
  flex-direction: row;
  height: 100%;
}

.menu {
  flex: 1;
  padding: 4px;
  background-color: #f0f0f0;
}

.review-form {
  flex: 10;
  padding: 4px;
  //background-color: #dadada;
}

</style>
