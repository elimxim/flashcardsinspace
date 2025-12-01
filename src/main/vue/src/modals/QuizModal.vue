<template>
  <Modal
    :visible="toggleStore.quizOpen"
    :on-press-exit="exit"
  >
    <div>
      Quiz
    </div>
  </Modal>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'

const toggleStore = useToggleStore()

function exit() {
  toggleStore.toggleQuiz()
}
</script>

<style scoped>

</style>
