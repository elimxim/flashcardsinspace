<template>
  <div
    :class="[
      'page',
      'flex-row',
      'padding-auto',
      'flex-center',
      'home-page',
      'scroll-none',
      'touch-none'
    ]"
  >
    <div class="hud-wrapper">
      <CodeConfirmationDevice
        v-model:attempts="attempts"
        :verify="verify"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import CodeConfirmationDevice from '@/components/CodeConfirmationDevice.vue'
import { ref } from 'vue'

const attempts = ref(3)

function verify(code: string) {

}

</script>

<style scoped>
.hud-wrapper {
  height: 80vh;
  width: 90vw;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
