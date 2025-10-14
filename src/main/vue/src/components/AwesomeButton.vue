<template>
  <button
    class="awesome-button awesome-button--theme select-none drag-none"
    :class="{
      'awesome-button--disabled': disabled,
      'awesome-button--invisible': invisible,
    }"
    :disabled="disabled"
    v-bind="$attrs"
    @click.stop="press"
  >
    <font-awesome-icon v-if="pressed && spinnable" :icon="spinIcon || icon" spin/>
    <font-awesome-icon v-else :icon="icon"/>
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  icon: string
  spinnable?: boolean
  spinIcon?: string
  disabled?: boolean
  invisible?: boolean
  onClick?: () => void
}>(), {
  spinnable: false,
  spinIcon: undefined,
  disabled: false,
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
}

.awesome-button {
  font-size: var(--btn--fontsize);
  color: var(--btn--color);
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  margin: 0;
  padding: 0;
}

.awesome-button:not(.awesome-button--disabled):hover {
  color: var(--btn--color--hover);
}

.awesome-button--disabled {
  color: var(--btn--color--disabled);
  cursor: default;
}

.awesome-button--invisible {
  visibility: hidden;
}
</style>
