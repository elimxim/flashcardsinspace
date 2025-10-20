<template>
  <div
    v-if="!hidden"
    class="awesome-button-wrapper"
    :class="{
      'awesome-button-wrapper--square': square,
      'awesome-button-wrapper--fill-space': fillSpace,
    }"
  >
    <div
      role="button"
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
      <slot name="above"/>
      <div class="awesome-icon-wrapper">
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
      </div>
      <slot name="below"/>
    </div>
  </div>
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
  square?: boolean
  fillSpace?: boolean
  onClick?: () => void
  onHover?: () => void
}>(), {
  spinnable: false,
  spinIcon: undefined,
  disabled: false,
  hidden: false,
  invisible: false,
  square: false,
  fillSpace: false,
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
  --a-btn--icon--size: var(--awesome-button--icon--size, 1.2rem);
  --a-btn--icon--color: var(--awesome-button--icon--color, #818181);
  --a-btn--icon--color--hover: var(--awesome-button--icon--color--hover, #404040);
  --a-btn--icon--color--disabled: var(--awesome-button--icon--color--disabled, #cacaca);
  --a-btn--bg: var(--awesome-button--bg, none);
  --a-btn--bg--hover: var(--awesome-button--bg--hover, none);
  --a-btn--bg--disabled: var(--awesome-button--bg--disabled, none);
  --a-btn--border: var(--awesome-button--border, none);
  --a-btn--border--hover: var(--awesome-button--border--hover, none);
  --a-btn--border-radius: var(--awesome-button--border-radius, none);
  --a-btn--padding: var(--awesome-button--padding, 1px);
}

.awesome-button-wrapper {
  display: grid;
  width: fit-content;
  height: fit-content;
}

.awesome-button-wrapper--square {
  width: fit-content;
  height: fit-content;
  aspect-ratio: 1 / 1;
}

.awesome-button-wrapper--fill-space {
  width: 100%;
  height: 100%;
}

.awesome-button-wrapper--square.awesome-button-wrapper--fill-space {
  width: auto;
  height: 100%;
  aspect-ratio: 1 / 1;
}

.awesome-button {
  position: relative;
  display: flex;
  flex-direction: column;
  place-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--a-btn--icon--color);
  background: var(--a-btn--bg);
  border: var(--a-btn--border);
  border-radius: var(--a-btn--border-radius);
  width: 100%;
  height: 100%;
  outline: none;
  cursor: pointer;
  margin: 0;
  padding: var(--a-btn--padding);
  transition: all 0.3s ease-in-out;
  overflow: hidden;
}

.awesome-button:not(.awesome-button--disabled):hover {
  color: var(--a-btn--icon--color--hover);
  background: var(--a-btn--bg--hover);
}

.awesome-button--disabled {
  color: var(--a-btn--icon--color--disabled);
  background: var(--a-btn--bg--disabled);
  cursor: default;
  box-shadow: none;
  transform: none;
}

.awesome-button--invisible {
  visibility: hidden;
}

.awesome-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 0;
  transition: transform 0.2s ease-in-out;
}

.awesome-icon {
  font-size: min(var(--a-btn--icon--size), 100cqw, 100cqh);
}

.awesome-spinning-icon {
  font-size: min(var(--a-btn--icon--size), 100cqw, 100cqh);
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

.awesome-button-wrapper:has(.awesome-button:not(.awesome-button--disabled):hover) .awesome-icon-wrapper {
  transform: scale(1.1);
}

</style>
