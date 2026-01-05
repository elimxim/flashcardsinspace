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
        ref="ccd"
        v-model:attempts="attempts"
        :verify-code="verifyCode"
        :resend-code="resendCode"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import CodeConfirmationDevice from '@/components/CodeConfirmationDevice.vue'
import { ref } from 'vue'

const attempts = ref(3)
const ccd = ref<InstanceType<typeof CodeConfirmationDevice>>()

function verifyCode(code: string) {
  ccd.value?.triggerFailure()
}

function resendCode() {
  attempts.value = 3
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
