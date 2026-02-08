<template>
  <button
    ref="button"
    class="smart-button smart-button--theme appearance-none touch-callout-none select-none"
    :class="{
      'smart-button--loading': resolvedLoading,
      'smart-button--disabled': disabled,
      'smart-button--rounded': rounded,
      'smart-button--fill-width': fillWidth,
      'smart-button--fill-height': fillHeight,
      'smart-button--fit-content': fitContent,
      'smart-button--tapped': animatingOnTap,
    }"
    :disabled="disabled"
    :hidden="hidden"
    v-bind="$attrs"
    @mousedown="onMouseDown"
    @mouseup="onMouseUp"
    @mouseleave="onMouseLeave"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
    @click.stop="handleClick"
    @contextmenu.prevent
  >
    <span class="smart-button-progress" :style="{ width: progressPercentage }"/>
    <span class="smart-button-title-wrapper">
      <span v-if="resolvedLoading">
        <font-awesome-icon icon="fa-solid fa-spinner" spin-pulse class="smart-button-spinner"/>
      </span>
      <template v-else>
        <slot v-if="hasSlot"/>
        <span v-else class="smart-button-title">
          {{ text }}
        </span>
      </template>
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, useSlots } from 'vue'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'

const props = withDefaults(defineProps<{
  text?: string
  disabled?: boolean
  hidden?: boolean
  rounded?: boolean
  holdTime?: number
  autoBlur?: boolean
  fillWidth?: boolean
  fillHeight?: boolean
  fitContent?: boolean
  titleScale?: number
  animateTap?: boolean
  tapDuration?: number
  onClick?: () => void | Promise<void>
}>(), {
  text: '',
  disabled: false,
  hidden: false,
  rounded: false,
  holdTime: 0,
  autoBlur: false,
  fillWidth: false,
  fillHeight: false,
  fitContent: false,
  titleScale: 1,
  animateTap: true,
  tapDuration: 200,
  onClick: async () => {
  },
})

const { resolvedLoading, startLoading, stopLoading } = useDeferredLoading()

const slots = useSlots()
const hasSlot = computed(() => !!slots.default)

let holdTimeout: ReturnType<typeof setTimeout> | null = null
let animationFrame: number | null = null
let pressStartTime: number | null = null

const button = ref<HTMLButtonElement>()
const animatingOnTap = ref(false)
const progress = ref(0)
const progressPercentage = computed(() => `${progress.value * 100}%`)

async function handleClick() {
  if (props.disabled || resolvedLoading.value) return
  if (props.autoBlur) button.value?.blur()
  if (props.holdTime <= 0) {
    if (UXConfig().showAnimationOnTap && props.animateTap) {
      startTapAnimation()
    } else {
      try {
        startLoading()
        await props.onClick()
      } finally {
        await stopLoading()
      }
    }
  }
}

function startTapAnimation() {
  animatingOnTap.value = true
  setTimeout(async () => {
    animatingOnTap.value = false
    try {
      startLoading()
      await props.onClick()
    } finally {
      await stopLoading()
    }
  }, props.tapDuration)
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
  if (props.disabled || resolvedLoading.value || props.holdTime <= 0 || holdTimeout !== null) return
  if (event.cancelable) {
    event.preventDefault()
  }

  pressStartTime = performance.now()
  animationFrame = requestAnimationFrame(updateProgress)

  holdTimeout = setTimeout(async () => {
    if (pressStartTime) {
      try {
        startLoading()
        await props.onClick()
      } finally {
        await stopLoading()
      }
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
  click: handleClick,
  hold: startHold,
  release: cancelHold,
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
  --s-btn--border-color--hover: var(--smart-button--border-color--hover, transparent);
  --s-btn--border-radius: var(--smart-button--border-radius, 3px);
  --s-btn--border-width: var(--smart-button--border-width, 1px);
  --s-btn--spinner--color: var(--smart-button--spinner--color, #a3a3a3);
  --s-btn--spinner--size: var(--smart-button--spinner--size, 24px);
  --s-btn--width: var(--smart-button--width, 100px);
  --s-btn--height: var(--smart-button--height, 40px);
  --s-btn--padding: var(--smart-button--padding, 0);
  --s-btn--bg: var(--smart-button--bg, #323232);
  --s-btn--bg--hover: var(--smart-button--bg--hover, #515151);
  --s-btn--bg--progress: var(--smart-button--bg--progress, #515151);
  --s-btn--bg--disabled: var(--smart-button--bg--disabled, #C8C8C8FF);
  --s-btn--bg--loading: var(--smart-button--bg--loading, #C8C8C8FF);
}

.smart-button {
  color: var(--s-btn--title--color);
  padding: var(--s-btn--padding);
  border: none;
  border-radius: var(--s-btn--border-radius);
  background: var(--s-btn--bg);
  outline: var(--s-btn--border-width) solid var(--s-btn--border-color);
  position: relative;
  transition: background-color 0.2s ease-in-out,
  outline 0.2s ease-in-out,
  box-shadow 0.2s ease-in-out;
  width: var(--s-btn--width);
  height: var(--s-btn--height);
  cursor: pointer;
  overflow: hidden;
  isolation: isolate;
}

.smart-button-progress {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: var(--s-btn--bg--progress);
  border-radius: var(--s-btn--border-radius);
  transition: width 0.05s linear;
  z-index: 0;
}

.smart-button-title-wrapper {
  position: relative;
  display: inline-block;
  font-family: var(--s-btn--font-family);
  font-size: var(--s-btn--title--font-size);
  word-spacing: var(--s-btn--title--word-spacing);
  letter-spacing: var(--s-btn--title--letter-spacing);
  transition: transform 0.1s ease-in-out;
  z-index: 1;
}

.smart-button-title {
  font-weight: 600;
  text-transform: uppercase;
}

.smart-button:not(.smart-button--disabled):active {
  transform: translateY(1px);
}

@media (hover: hover) {
  .smart-button:not(.smart-button--disabled):not(.smart-button--loading):hover,
  .smart-button:not(.smart-button--disabled):not(.smart-button--loading):focus {
    outline: 1px solid var(--s-btn--border-color--hover);
    color: var(--s-btn--title--color--hover);
    background: var(--s-btn--bg--hover);
  }

  .smart-button:not(.smart-button--disabled):not(.smart-button--loading):hover .smart-button-title {
    transform: scale(v-bind(titleScale));
  }
}

.smart-button--tapped:not(.smart-button--disabled):not(.smart-button--loading) {
  outline: 1px solid var(--s-btn--border-color--hover);
  color: var(--s-btn--title--color--hover);
  background: var(--s-btn--bg--hover);
}

.smart-button:has(.smart-button--tapped:not(.smart-button--disabled):not(.smart-button--loading)) .smart-button-title {
  transform: scale(v-bind(titleScale));
}

.smart-button--disabled:not(.smart-button--loading) {
  color: var(--s-btn--title--color--disabled);
  background: var(--s-btn--bg--disabled);
  cursor: default;
}

.smart-button--loading {
  background: var(--s-btn--bg--loading);
  cursor: default;
}

.smart-button-spinner {
  color: var(--s-btn--spinner--color);
  font-size: var(--s-btn--spinner--size);
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
