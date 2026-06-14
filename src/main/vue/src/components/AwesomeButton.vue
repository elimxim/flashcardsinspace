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
          'awesome-button--tapped': animatingOnTap,
        }"
        :disabled="disabled"
        v-bind="$attrs"
        @click.stop="handleClick"
        @dblclick.stop="handleDoubleClick"
        @mouseenter="handleHover"
        @mouseleave="handleHover"
      >
        <div v-if="resolvedLoading" class="awesome-icon-wrapper">
          <font-awesome-icon
            icon="fa-solid fa-spinner"
            class="awesome-icon"
            spin-pulse
          />
        </div>
        <template v-else>
          <slot name="above"/>
          <div class="awesome-icon-wrapper">
            <font-awesome-icon
              v-if="pressed && flipIcon"
              :icon="flipIcon"
              class="awesome-icon"
              :class="{ 'awesome--icon--spinning': spinWhenFlipped }"
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
        </template>
      </div>
    </Tooltip>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/components/Tooltip.vue'
import { ref } from 'vue'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'

const props = withDefaults(defineProps<{
  icon: string
  fade?: boolean
  flipIcon?: string
  spinWhenFlipped?: boolean
  disabled?: boolean
  active?: boolean
  hidden?: boolean
  invisible?: boolean
  square?: boolean
  fillSpace?: boolean
  animateTap?: boolean,
  tapDuration?: number
  tooltip?: string
  tooltipPosition?: 'top' | 'bottom' | 'left' | 'right' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right'
  tooltipDelay?: number
  onClick?: () => void | Promise<void>
  onDoubleClick?: () => void | Promise<void>
  onHover?: () => void | Promise<void>
}>(), {
  fade: false,
  flipIcon: undefined,
  spinWhenFlipped: false,
  disabled: false,
  active: false,
  hidden: false,
  invisible: false,
  square: false,
  fillSpace: false,
  animateTap: true,
  tapDuration: 300,
  tooltip: undefined,
  tooltipPosition: 'top',
  tooltipDelay: 1000,
  onClick: async () => {
  },
  onDoubleClick: async () => {
  },
  onHover: async () => {
  },
})

const {
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const pressed = ref(false)
const animatingOnTap = ref(false)

async function press() {
  pressed.value = !pressed.value
  try {
    startLoading()
    await props.onClick()
  } finally {
    await stopLoading()
  }
}

function handleClick() {
  if (props.disabled || resolvedLoading.value) return
  if (UXConfig().showAnimationOnTap && props.animateTap) {
    startTapAnimation()
  } else {
    press()
  }
}

async function handleDoubleClick() {
  if (props.disabled || resolvedLoading.value) return
  try {
    startLoading()
    await props.onDoubleClick()
  } finally {
    await stopLoading()
  }
}

async function handleHover() {
  if (props.disabled || resolvedLoading.value) return
  await props.onHover()
}

function startTapAnimation() {
  animatingOnTap.value = true
  setTimeout(() => {
    animatingOnTap.value = false
    press()
  }, props.tapDuration)
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
  --a-btn--scale-factor--on-hover: var(--awesome-button--scale-factor--on-hover, 1.1);
  --a-btn--scale-factor--on-active: var(--awesome-button--scale-factor--on-active, 0.9);
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
  transition: all 0.2s ease-in-out;
  overflow: visible;
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
    transform: scale(var(--a-btn--scale-factor--on-hover));
  }
}

.awesome-button-wrapper:has(.awesome-button--tapped:not(.awesome-button--disabled)) .awesome-icon-wrapper {
  transform: scale(var(--a-btn--scale-factor--on-hover));
}

.awesome-button-wrapper:has(.awesome-button:not(.awesome-button--disabled):active) .awesome-icon-wrapper {
  transform: scale(var(--a-btn--scale-factor--on-active));
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
