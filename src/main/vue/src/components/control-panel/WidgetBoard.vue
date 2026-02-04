<template>
  <FlashcardSetInfoBar
    :hidden="showInfoBar"
  />
  <MainPanel/>
  <LearningStagesWidget ref="learningStagesWidget" :grow-multiplier="3"/>
  <div class="control-outer-space-panel">
    <OuterSpaceWidget/>
  </div>
</template>

<script setup lang="ts">
import FlashcardSetInfoBar from '@/components/control-panel/FlashcardSetInfoBar.vue'
import MainPanel from '@/components/control-panel/MainPanel.vue'
import LearningStagesWidget from '@/components/control-panel/LearningStagesWidget.vue'
import OuterSpaceWidget from '@/components/control-panel/OuterSpaceWidget.vue'
import { nextTick, onMounted, ref } from 'vue'
import { isTouchDevice } from '@/utils/utils.ts'

defineProps<{
  showInfoBar: boolean
}>()

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
.control-outer-space-panel {
  flex: 0 0 14%;
  overflow: hidden;
}
</style>
