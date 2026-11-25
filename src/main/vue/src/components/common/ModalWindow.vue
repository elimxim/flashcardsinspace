<template>
  <CenteredOverlay :visible="visible" :focus-on="focusOn ?? panel">
    <div ref="panel" class="modal-window touch-none" tabindex="-1">
      <div class="modal-window-content">
        <slot name="header" />
        <slot />
      </div>
    </div>
  </CenteredOverlay>
</template>

<script setup lang="ts">
import CenteredOverlay from '@/components/common/CenteredOverlay.vue'
import { ref } from 'vue'

defineProps<{
  visible: boolean
  focusOn?: Pick<HTMLElement, 'focus'>
}>()

const panel = ref<HTMLDivElement>()
</script>

<style scoped>
.modal-window {
  background: #fff;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  width: clamp(240px, 92vw, 540px);
  min-width: 240px;
  height: clamp(360px, 90vh, 480px);
  min-height: 360px;
  padding: 8px;
  user-select: none;
  resize: none;
  outline: none;
}

.modal-window-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>
