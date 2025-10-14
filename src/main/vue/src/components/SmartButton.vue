<template>
  <button
    ref="button"
    class="smart-button smart-button--theme transition--bg-color"
    :class="{
      'smart-button--disabled': disabled,
      'smart-button--rounded': rounded,
      'smart-button--fill-width': fillWidth,
    }"
    :disabled="disabled"
    :hidden="hidden"
    v-bind="$attrs"
    @mousedown="startHold"
    @mouseup="cancelHold"
    @mouseleave="cancelHold"
    @touchstart.prevent="startHold"
    @touchend="cancelHold"
    @click="click"
  >
    {{ text }}
  </button>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(defineProps<{
  text: string,
  disabled?: boolean
  hidden?: boolean
  rounded?: boolean
  holdTime?: number
  autoBlur?: boolean
  fillWidth?: boolean
  onClick?: () => void
}>(), {
  disabled: false,
  hidden: false,
  rounded: false,
  holdTime: 0,
  autoBlur: false,
  fillWidth: false,
  onClick: () => {
  },
})

let holdTimeout: ReturnType<typeof setTimeout> | null = null
let animationFrame: number | null = null
let pressStartTime: number | null = null

const button = ref<HTMLButtonElement>()
const progress = ref(0)
const progressPercentage = computed(() => `${progress.value * 100}%`)

function click() {
  if (props.disabled) return
  if (props.autoBlur) button.value?.blur()
  if (props.holdTime <= 0) props.onClick()
}

function startHold(event: Event) {
  if (props.disabled || props.holdTime <= 0) return
  event.preventDefault()

  pressStartTime = performance.now()
  animationFrame = requestAnimationFrame(updateProgress)

  holdTimeout = setTimeout(() => {
    if (pressStartTime) {
      props.onClick()
    }
    cancelHold()
  }, props.holdTime * 1000)
}

function cancelHold() {
  if (!pressStartTime) return

  if (holdTimeout) {
    clearTimeout(holdTimeout)
    holdTimeout = null
  }

  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
    animationFrame = null
  }

  pressStartTime = null

  if (button.value) {
    progress.value = 0
  }
}

function updateProgress() {
  if (!pressStartTime || !button.value) return

  const elapsedTime = performance.now() - pressStartTime
  progress.value = Math.min(elapsedTime / (props.holdTime * 1000), 1)

  if (progress.value < 1) {
    animationFrame = requestAnimationFrame(updateProgress)
  }
}

defineExpose({
  click
})

</script>

<style scoped>
.smart-button--theme {
  --btn--font-family: var(--smart-button--font-family);
  --btn--color: var(--smart-button--color, white);
  --btn--border-color: var(--smart-button--border-color, transparent);
  --btn--border-width: var(--smart-button--border-width, 0);
  --btn--border-radius: var(--smart-button--border-radius, 3px);
  --btn--width: var(--smart-button--width, 100px);
  --btn--bg-color: var(--smart-button--bg-color, #323232);
  --btn--bg-color--hover: var(--smart-button--bg-color--hover, #515151);
  --btn--color--disabled: var(--smart-button--color--disabled, #a3a3a3);
  --btn--bg-color--disabled: var(--smart-button--bg-color--disabled, #C8C8C8FF);
}

.smart-button {
  font-family: var(--btn--font-family);
  color: var(--btn--color);
  border-color: var(--btn--border-color);
  border-width: var(--btn--border-width);
  border-style: solid;
  border-radius: var(--btn--border-radius);
  background-color: var(--btn--bg-color);
  position: relative;
  font-size: 16px;
  width: var(--btn--width);
  height: 40px;
  cursor: pointer;
  z-index: 1;
}

.smart-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: v-bind(progressPercentage);
  height: 100%;
  background-color: var(--btn--bg-color);
  border-radius: var(--btn--border-radius);
  transition: width 0.05s linear;
  z-index: -1;
}

.smart-button:not(.smart-button--disabled):active {
  transform: translateY(1px);
}

.smart-button:not(.smart-button--disabled):hover,
.smart-button:not(.smart-button--disabled):focus {
  outline: none;
  background-color: var(--btn--bg-color--hover);
}

.smart-button--disabled {
  color: var(--btn--color--disabled);
  background-color: var(--btn--bg-color--disabled);
  cursor: default;
}

.smart-button--rounded {
  --smart-button--border-radius: 9999px;
}

.smart-button--fill-width {
  --smart-button--width: 100%;
}

</style>
