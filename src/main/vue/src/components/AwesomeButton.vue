<template>
  <button
    v-if="!hidden"
    class="awesome-button awesome-button--theme select-none drag-none"
    :class="{
      'awesome-button--disabled': disabled,
      'awesome-button--invisible': invisible,
    }"
    :disabled="disabled"
    v-bind="$attrs"
    @click.stop="press"
  >
    <font-awesome-icon
      v-if="pressed && spinnable"
      :icon="spinIcon || icon"
      class="awesome-spinning-icon"
    />
    <font-awesome-icon
      v-else
      :icon="icon"
      class="awesome-icon"
    />
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  icon: string
  spinnable?: boolean
  spinIcon?: string
  disabled?: boolean
  hidden?: boolean
  invisible?: boolean
  onClick?: () => void
}>(), {
  spinnable: false,
  spinIcon: undefined,
  disabled: false,
  hidden: false,
  invisible: false,
  onClick: () => {
  },
})

const pressed = ref(false)

function press() {
  pressed.value = !pressed.value
  props.onClick()
}

function isPressed(): boolean {
  return pressed.value
}

defineExpose({
  isPressed,
  press
})

</script>

<style scoped>
.awesome-button--theme {
  --btn--fontsize: var(--awesome-button--font-size, 1.2rem);
  --btn--color: var(--awesome-button--color, #818181);
  --btn--color--hover: var(--awesome-button--color--hover, #404040);
  --btn--color--disabled: var(--awesome-button--color--disabled, #cacaca);
  --btn--bg: var(--awesome-button--bg, none);
  --btn--bg--hover: var(--awesome-button--bg--hover, none);
  --btn--bg--disabled: var(--awesome-button--bg--disabled, none);
  --btn--border: var(--awesome-button--border, none);
  --btn--border--hover: var(--awesome-button--border--hover, none);
  --btn--border-radius: var(--awesome-button--border-radius, none);
  --btn--padding: var(--awesome-button--padding, 1px);
}

.awesome-button {
  position: relative;
  display: flex;
  align-items: center;
  font-size: var(--btn--fontsize);
  color: var(--btn--color);
  background: var(--btn--bg);
  border: var(--btn--border);
  border-radius: var(--btn--border-radius);
  outline: none;
  cursor: pointer;
  margin: 0;
  padding: var(--btn--padding);
  transition: all 0.3s ease-in-out;
  overflow: hidden;
}

.awesome-button:not(.awesome-button--disabled):hover {
  color: var(--btn--color--hover);
  background: var(--btn--bg--hover);
}

.awesome-button--disabled {
  color: var(--btn--color--disabled);
  background: var(--btn--bg--disabled);
  cursor: default;
  box-shadow: none;
  transform: none;
}

.awesome-button--invisible {
  visibility: hidden;
}

.awesome-icon {
  flex: 1;
  transition: transform 0.2s ease;
}

.awesome-spinning-icon {
  flex: 1;
  color: var(--btn--color--hover);
  background: var(--btn--bg--hover);
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.awesome-button:not(.awesome-button--disabled):hover .awesome-icon {
  transform: scale(1.1);
}
</style>
