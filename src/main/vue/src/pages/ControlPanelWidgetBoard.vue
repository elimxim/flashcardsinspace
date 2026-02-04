<template>
  <WelcomeWidget v-if="flashcardStoreLoaded && !flashcardSet" class="control-welcome"/>
  <template v-else-if="flashcardStoreLoaded">
    <FlashcardSetInfoBar
      :hidden="showInfoBar"
    />
    <MainPanel/>
    <LearningStagesWidget ref="learningStagesWidget" :grow-multiplier="3"/>
    <div class="control-outer-space-panel">
      <OuterSpaceWidget/>
    </div>
  </template>
</template>

<script setup lang="ts">
import FlashcardSetInfoBar from '@/components/control-panel/FlashcardSetInfoBar.vue'
import MainPanel from '@/components/control-panel/MainPanel.vue'
import LearningStagesWidget from '@/components/control-panel/LearningStagesWidget.vue'
import OuterSpaceWidget from '@/components/control-panel/OuterSpaceWidget.vue'
import WelcomeWidget from '@/components/control-panel/WelcomeWidget.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { nextTick, onMounted, ref } from 'vue'
import { isTouchDevice } from '@/utils/utils.ts'

defineProps<{
  showInfoBar: boolean
}>()

const flashcardStore = useFlashcardStore()

const { flashcardSet, loaded: flashcardStoreLoaded } = storeToRefs(flashcardStore)

const learningStagesWidget = ref<InstanceType<typeof LearningStagesWidget>>()

onMounted(() => {
  if (isTouchDevice) {
    nextTick().then(() =>
      setTimeout(() => {
        learningStagesWidget.value?.expand()
      }, 1000)
    )
  }
})
</script>

<style scoped>

</style>
