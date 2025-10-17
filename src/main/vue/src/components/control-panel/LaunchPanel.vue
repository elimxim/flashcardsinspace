<template>
  <div class="launch-panel launch-panel--theme">
    <div class="launch-panel-content">
      <div class="launch-panel-label">
        Review
      </div>
      <div class="launch-panel-layout">
        <LaunchButton
          :on-click="startReview"
        />
      </div>
    </div>
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
.launch-panel--theme {
  font-family: var(--main-font-family);
  --l-panel--label-color: var(--launch-panel--label-color, #555555);
  --l-panel--border-color: var(--launch-panel--border-color, rgba(128, 128, 128, 0.62));
}

.launch-panel {
  position: relative;
  display: flex;
  flex-direction: row;
}

.launch-panel-content {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 1px;
}

.launch-panel-label {
  font-size: clamp(0.5rem, 1.8vw, 0.6rem);
  color: var(--l-panel--label-color);
  letter-spacing: 0.1rem;
  word-spacing: 0.1rem;
  text-transform: uppercase;
  padding: 0 0 0 6px;
}

.launch-panel-layout {
  display: grid;
  align-items: center;
  padding: 4px;
  border: 1px solid var(--l-panel--border-color);
  border-radius: 6px;
  width: fit-content;
  height: clamp(110px, 20vw, 120px);
}
</style>
