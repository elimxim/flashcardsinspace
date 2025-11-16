<template>
  <transition name="error-slide" mode="out-in">
    <div
      v-if="firstErrorText"
      class="error-text error-text--theme"
      :key="firstErrorText"
    >
      {{ firstErrorText }}
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface ErrorItem {
  when: boolean
  text: string
}

const props = withDefaults(defineProps<{
  when?: boolean
  text?: string
  errors?: ErrorItem[]
}>(), {
  when: false,
  text: undefined,
  errors: () => []
})

const firstErrorText = computed(() => {
  if (props.text && props.when) {
    return props.text
  }

  if (props.errors && props.errors.length > 0) {
    const visibleText = props.errors.find(item => item.when)
    return visibleText?.text
  }
})

</script>

<style scoped>
.error-text--theme {
  --e-text--font-family: var(--error-font-family);
  --e-text--color: var(--text--error--color, #c80f0f);
}

.error-text {
  display: inline-block;
  font-family: var(--e-text--font-family);
  font-size: clamp(0.75rem, 1.6vh, 0.9rem);
  color: var(--e-text--color);
  margin: 0;
  padding: 0;
}

.error-slide-enter-active,
.error-slide-leave-active {
  transition: all 0.3s ease-out;
}

.error-slide-enter-from {
  opacity: 0;
}

.error-slide-leave-to {
  opacity: 0;
}

.error-slide-enter-to,
.error-slide-leave-from {
  opacity: 1;
}
</style>
