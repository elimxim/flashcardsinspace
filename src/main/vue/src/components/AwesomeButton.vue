<template>
  <div
    v-if="!hidden"
    class="awesome-button-wrapper"
    :class="{
      'awesome-button-wrapper--square': square && !fillSpace,
      'awesome-button-wrapper--growing': fillSpace && !square,
      'awesome-button-wrapper--growing--square': fillSpace && square,
    }"
  >
    <Tooltip
      :text="tooltip"
      :delay="tooltipDelay"
      :position="tooltipPosition"
    >
      <div
        role="button"
        class="awesome-button awesome-button--theme select-none drag-none"
        :class="{
        'awesome-button--active': active,
        'awesome-button--disabled': disabled,
        'awesome-button--invisible': invisible,
        'awesome-button--click-ripple': clickRipple,
        'awesome-button--ripple-active': rippleActive,
        'awesome-button--tapped': animatingOnTap,
      }"
        :disabled="disabled"
        v-bind="$attrs"
        @click.stop="handleClick"
        @dblclick.stop="onDoubleClick"
        @mouseenter="onHover"
        @mouseleave="onHover"
      >
        <slot name="above"/>
        <div class="awesome-icon-wrapper">
          <font-awesome-icon
            v-if="pressed && spinnable"
            :icon="spinIcon || icon"
            class="awesome-icon awesome--icon--spinning"
          />
          <font-awesome-icon
            v-else-if="fade"
            :icon="icon"
            class="awesome-icon awesome--icon--fading"
          />
          <font-awesome-icon
            v-else
            :icon="icon"
            class="awesome-icon"
          />
        </div>
        <slot name="below"/>
      </div>
    </Tooltip>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/components/Tooltip.vue'
import { ref } from 'vue'
import { hoverSupported } from '@/utils/utils.ts'

const props = withDefaults(defineProps<{
  icon: string
  fade?: boolean
  scaleFactor?: number
  spinnable?: boolean
  spinIcon?: string
  disabled?: boolean
  active?: boolean
  hidden?: boolean
  invisible?: boolean
  square?: boolean
  fillSpace?: boolean
  clickRipple?: boolean
  animateTap?: boolean,
  tapDuration?: number
  tooltip?: string
  tooltipPosition?: 'top' | 'bottom' | 'left' | 'right' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right'
  tooltipDelay?: number
  onClick?: () => void
  onDoubleClick?: () => void
  onHover?: () => void
}>(), {
  fade: false,
  scaleFactor: 1.1,
  spinnable: false,
  spinIcon: undefined,
  disabled: false,
  active: false,
  hidden: false,
  invisible: false,
  square: false,
  fillSpace: false,
  clickRipple: false,
  animateTap: true,
  tapDuration: 300,
  tooltip: undefined,
  tooltipPosition: 'top',
  tooltipDelay: 1000,
  onClick: () => {
  },
  onDoubleClick: () => {
  },
  onHover: () => {
  },
})

const pressed = ref(false)
const rippleActive = ref(false)
const animatingOnTap = ref(false)

function press() {
  pressed.value = !pressed.value
  props.onClick()
}

function handleClick() {
  if (props.disabled) return
  if (props.clickRipple) {
    triggerRipple()
  } else if (!hoverSupported && props.animateTap) {
    startTapAnimation()
  } else {
    press()
  }
}

function startTapAnimation() {
  animatingOnTap.value = true
  setTimeout(() => {
    animatingOnTap.value = false
    press()
  }, props.tapDuration)
}

function triggerRipple() {
  rippleActive.value = true
  setTimeout(() => {
    rippleActive.value = false
    press()
  }, 300)
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
  --a-btn--icon--width: var(--awesome-button--icon--width, auto);
  --a-btn--icon--color: var(--awesome-button--icon--color, #818181);
  --a-btn--icon--color--hover: var(--awesome-button--icon--color--hover, #404040);
  --a-btn--icon--color--disabled: var(--awesome-button--icon--color--disabled, #cacaca);
  --a-btn--icon--color--active: var(--awesome-button--icon--color--active, #000000);
  --a-btn--bg: var(--awesome-button--bg, none);
  --a-btn--bg--hover: var(--awesome-button--bg--hover, none);
  --a-btn--bg--disabled: var(--awesome-button--bg--disabled, none);
  --a-btn--bg--active: var(--awesome-button--bg--active, none);
  --a-btn--border: var(--awesome-button--border, none);
  --a-btn--border--hover: var(--awesome-button--border--hover, none);
  --a-btn--border-radius: var(--awesome-button--border-radius, none);
  --a-btn--padding: var(--awesome-button--padding, 1px);
}

.awesome-button-wrapper {
  position: relative;
  display: grid;
  width: fit-content;
  height: fit-content;
}

.awesome-button-wrapper--square {
  aspect-ratio: 1 / 1;
}

.awesome-button-wrapper--growing {
  width: 100%;
  height: 100%;
}

.awesome-button-wrapper--growing--square {
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

@media (hover: hover) {
  .awesome-button:not(.awesome-button--disabled):not(.awesome-button--active):hover {
    color: var(--a-btn--icon--color--hover);
    background: var(--a-btn--bg--hover);
  }
}

.awesome-button--tapped:not(.awesome-button--disabled):not(.awesome-button--active) {
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

.awesome-button--active {
  color: var(--a-btn--icon--color--active);
  background: var(--a-btn--bg--active);
}

.awesome-button--invisible {
  visibility: hidden;
}

@media (hover: hover) {
  .awesome-button-wrapper:has(.awesome-button:not(.awesome-button--disabled):hover) .awesome-icon-wrapper {
    transform: scale(v-bind(scaleFactor));
  }
}

.awesome-button-wrapper:has(.awesome-button--tapped:not(.awesome-button--disabled)) .awesome-icon-wrapper {
  transform: scale(v-bind(scaleFactor));
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
  width: var(--a-btn--icon--width);
}

.awesome--icon--spinning {
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

.awesome--icon--fading {
  animation: fade 1.5s linear infinite;
}

@keyframes fade {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
  100% {
    opacity: 1;
  }
}

.awesome-button--click-ripple::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: var(--a-btn--bg--active);
  opacity: 0;
  transform: translate(-50%, -50%) scale(0);
  pointer-events: none;
  z-index: 1;
  transition: none;
}

.awesome-button--click-ripple.awesome-button--ripple-active::before {
  animation: growing-circle 0.5s ease-out;
}

@keyframes growing-circle {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1);
  }
}

</style>
