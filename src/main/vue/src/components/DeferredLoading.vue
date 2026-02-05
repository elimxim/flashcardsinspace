<template>
  <Transition name="fade" mode="out-in">
    <div v-if="resolvedLoading" key="fallback">
      <slot name="fallback"/>
    </div>
    <div v-else-if="!isDelaying" key="content">
      <slot name="default"/>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { onMounted } from 'vue'

const props = withDefaults(defineProps<{
  waitFor: () => Promise<unknown>
  delayEntry?: number
  minDuration?: number
}>(), {
  delayEntry: 200,
  minDuration: 800,
})

const {
  isDelaying,
  resolvedLoading,
  startLoading,
  stopLoading
} = useDeferredLoading({
  delayEntry: props.delayEntry,
  minDuration: props.minDuration
})

onMounted(async () => {
  startLoading()
  console.log('Waiting for promise...')
  await props.waitFor()
  console.log('Promise resolved')
  await stopLoading()
  console.log('Loading stopped')
})

</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
