<template>
  <div class="launch-panel">
    <LaunchButton
      :on-click="startReview"
    />
  </div>
</template>

<script setup lang="ts">
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const router = useRouter()

function startReview() {
  router.push({ name: routeNames.review })
}
</script>

<style scoped>
.launch-panel {
  position: relative;
  display: flex;
  align-items: center;
  padding: 1px;
  height: 100%;
  width: fit-content;
}
</style>
