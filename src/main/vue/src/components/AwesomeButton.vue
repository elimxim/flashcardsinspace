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
    @mouseenter="onHover"
    @mouseleave="onHover"
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
  onHover?: () => void
}>(), {
  spinnable: false,
  spinIcon: undefined,
  disabled: false,
  hidden: false,
  invisible: false,
  onClick: () => {
  },
  onHover: () => {
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
  --a-btn--fontsize: var(--awesome-button--font-size, 1.2rem);
  --a-btn--width: var(--awesome-button--width, fit-content);
  --a-btn--height: var(--awesome-button--height, fit-content);
  --a-btn--color: var(--awesome-button--color, #818181);
  --a-btn--color--hover: var(--awesome-button--color--hover, #404040);
  --a-btn--color--disabled: var(--awesome-button--color--disabled, #cacaca);
  --a-btn--bg: var(--awesome-button--bg, none);
  --a-btn--bg--hover: var(--awesome-button--bg--hover, none);
  --a-btn--bg--disabled: var(--awesome-button--bg--disabled, none);
  --a-btn--border: var(--awesome-button--border, none);
  --a-btn--border--hover: var(--awesome-button--border--hover, none);
  --a-btn--border-radius: var(--awesome-button--border-radius, none);
  --a-btn--padding: var(--awesome-button--padding, 1px);
}

.awesome-button {
  position: relative;
  display: flex;
  align-items: center;
  font-size: var(--a-btn--fontsize);
  color: var(--a-btn--color);
  background: var(--a-btn--bg);
  border: var(--a-btn--border);
  border-radius: var(--a-btn--border-radius);
  width: var(--a-btn--width);
  height: var(--a-btn--height);
  outline: none;
  cursor: pointer;
  margin: 0;
  padding: var(--a-btn--padding);
  transition: all 0.3s ease-in-out;
  overflow: hidden;
}

.awesome-button:not(.awesome-button--disabled):hover {
  color: var(--a-btn--color--hover);
  background: var(--a-btn--bg--hover);
}

.awesome-button--disabled {
  color: var(--a-btn--color--disabled);
  background: var(--a-btn--bg--disabled);
  cursor: default;
  box-shadow: none;
  transform: none;
}

.awesome-button--invisible {
  visibility: hidden;
}

.awesome-icon {
  flex: 1;
  transition: transform 0.2s ease-in-out;
}

.awesome-spinning-icon {
  flex: 1;
  color: var(--a-btn--color--hover);
  background: var(--a-btn--bg--hover);
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
