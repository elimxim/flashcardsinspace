<template>
  <button
    ref="button"
    class="smart-button smart-button--theme appearance-none"
    :class="{
      'smart-button--disabled': disabled,
      'smart-button--rounded': rounded,
      'smart-button--fill-width': fillWidth,
      'smart-button--fill-height': fillHeight,
      'smart-button--fit-content': fitContent,
    }"
    :disabled="disabled"
    :hidden="hidden"
    v-bind="$attrs"
    @mousedown="onMouseDown"
    @mouseup="onMouseUp"
    @mouseleave="onMouseLeave"
    @touchstart.prevent="onTouchStart"
    @touchend="onTouchEnd"
    @click="click"
  >
    <span class="smart-button-title">
      {{ text }}
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = withDefaults(defineProps<{
  text: string
  disabled?: boolean
  hidden?: boolean
  rounded?: boolean
  holdTime?: number
  autoBlur?: boolean
  fillWidth?: boolean
  fillHeight?: boolean
  fitContent?: boolean
  titleScale?: number
  onClick?: () => void
}>(), {
  disabled: false,
  hidden: false,
  rounded: false,
  holdTime: 0,
  autoBlur: false,
  fillWidth: false,
  fillHeight: false,
  fitContent: false,
  titleScale: 1,
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

function onMouseDown(event: Event) {
  startHold(event)
}

function onMouseUp() {
  cancelHold()
}

function onMouseLeave() {
  cancelHold()
}

function onTouchStart(event: Event) {
  startHold(event)
}

function onTouchEnd() {
  cancelHold()
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

onMounted(() => {
  document.addEventListener('mouseup', handleGlobalMouseUp)
})

onUnmounted(() => {
  document.removeEventListener('mouseup', handleGlobalMouseUp)
})

function handleGlobalMouseUp() {
  if (button.value && document.activeElement === button.value) {
    button.value.blur()
  }
}

</script>

<style scoped>
.smart-button--theme {
  --s-btn--font-family: var(--button--font-family);
  --s-btn--title--font-size: var(--smart-button--title--font-size, 16px);
  --s-btn--title--color: var(--smart-button--title--color, #FAF9F6);
  --s-btn--title--color--hover: var(--smart-button--title--color--hover, #FAF9F6);
  --s-btn--title--color--disabled: var(--smart-button--title--color--disabled, #a3a3a3);
  --s-btn--title--word-spacing: var(--smart-button--title--word-spacing, 0.05rem);
  --s-btn--title--letter-spacing: var(--smart-button--title--letter-spacing, 0.05rem);
  --s-btn--border-color: var(--smart-button--border-color, transparent);
  --s-btn--border-radius: var(--smart-button--border-radius, 3px);
  --s-btn--width: var(--smart-button--width, 100px);
  --s-btn--height: var(--smart-button--height, 40px);
  --s-btn--padding: var(--smart-button--padding, 0);
  --s-btn--bg: var(--smart-button--bg, #323232);
  --s-btn--bg--hover: var(--smart-button--bg--hover, #515151);
  --s-btn--bg--disabled: var(--smart-button--bg--disabled, #C8C8C8FF);
}

.smart-button {
  color: var(--s-btn--title--color);
  padding: var(--s-btn--padding);
  border: none;
  border-radius: var(--s-btn--border-radius);
  background: var(--s-btn--bg);
  outline: 1px solid var(--s-btn--border-color);
  position: relative;
  transition:
    background-color 0.2s ease-in-out,
    outline 0.2s ease-in-out,
    box-shadow 0.2s ease-in-out;
  width: var(--s-btn--width);
  height: var(--s-btn--height);
  cursor: pointer;
  overflow: hidden;
  z-index: 1;
}

.smart-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: v-bind(progressPercentage);
  height: 100%;
  background-color: var(--s-btn--bg);
  border-radius: var(--s-btn--border-radius);
  transition: width 0.05s linear;
  z-index: -1;
  overflow: hidden;
}

.smart-button-title {
  display: inline-block;
  font-family: var(--s-btn--font-family);
  font-weight: 600;
  font-size: var(--s-btn--title--font-size);
  word-spacing: var(--s-btn--title--word-spacing);
  letter-spacing: var(--s-btn--title--letter-spacing);
  text-transform: uppercase;
  transition: transform 0.1s ease-in-out;
}

.smart-button:not(.smart-button--disabled):active {
  transform: translateY(1px);
}

.smart-button:not(.smart-button--disabled):hover,
.smart-button:not(.smart-button--disabled):focus {
  outline: none;
  color: var(--s-btn--title--color--hover);
  background: var(--s-btn--bg--hover);
}

.smart-button:not(.smart-button--disabled):hover .smart-button-title {
  transform: scale(v-bind(titleScale));
}

.smart-button--disabled {
  color: var(--s-btn--title--color--disabled);
  background: var(--s-btn--bg--disabled);
  cursor: default;
}

.smart-button--rounded {
  --smart-button--border-radius: 9999px;
}

.smart-button--fill-width {
  --smart-button--width: 100%;
}

.smart-button--fill-height {
  --smart-button--height: 100%;
}

.smart-button--fit-content {
  --smart-button--width: fit-content;
  --smart-button--height: fit-content;
}

</style>
