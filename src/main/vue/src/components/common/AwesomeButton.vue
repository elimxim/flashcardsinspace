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
    <Tooltip :text="tooltip" :delay="tooltipDelay" :position="tooltipPosition">
      <div
        role="button"
        class="awesome-button awesome-button--theme select-none drag-none"
        :class="{
          'awesome-button--active': active,
          'awesome-button--disabled': disabled,
          'awesome-button--invisible': invisible,
          'awesome-button--tapped': animatingOnTap,
          'awesome-button--holdable': holdTime > 0,
          'awesome-button--held': held,
          'awesome-button--holding': holding,
          'awesome-button--switched': switched,
        }"
        :disabled="disabled"
        v-bind="$attrs"
        @click.stop="handleClick"
        @dblclick.stop="handleDoubleClick"
        @mouseenter="handleHover"
        @mouseleave="handleHover"
        v-on="longPressListeners"
      >
        <div v-if="resolvedLoading" class="awesome-icon-wrapper">
          <font-awesome-icon icon="fa-solid fa-spinner" class="awesome-icon" spin-pulse />
        </div>
        <template v-else>
          <slot name="above" />
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
            <font-awesome-icon v-else :icon="icon" class="awesome-icon" />
          </div>
          <slot name="below" />
        </template>
      </div>
    </Tooltip>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/components/common/Tooltip.vue'
import { computed, ref, watch } from 'vue'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'
import { useLongPress } from '@/utils/long-press.ts'

const props = withDefaults(
  defineProps<{
    icon: string
    fade?: boolean
    flipIcon?: string
    spinWhenFlipped?: boolean
    disabled?: boolean
    disabledOnPress?: boolean
    active?: boolean
    hidden?: boolean
    invisible?: boolean
    square?: boolean
    fillSpace?: boolean
    animateTap?: boolean
    animationDuration?: number
    tooltip?: string
    tooltipPosition?:
      | 'top'
      | 'bottom'
      | 'left'
      | 'right'
      | 'top-left'
      | 'top-right'
      | 'bottom-left'
      | 'bottom-right'
    tooltipDelay?: number
    holdTime?: number
    held?: boolean
    onClick?: () => void | Promise<void>
    onDoubleClick?: () => void | Promise<void>
    onHover?: () => void | Promise<void>
    onHold?: () => void
  }>(),
  {
    fade: false,
    flipIcon: undefined,
    spinWhenFlipped: false,
    disabled: false,
    disabledOnPress: false,
    active: false,
    hidden: false,
    invisible: false,
    square: false,
    fillSpace: false,
    animateTap: true,
    animationDuration: 300,
    tooltip: undefined,
    tooltipPosition: 'top',
    tooltipDelay: 1000,
    holdTime: 0,
    held: false,
    onClick: async () => {},
    onDoubleClick: async () => {},
    onHover: async () => {},
    onHold: () => {},
  },
)

const animationDurationSeconds = computed(() => `${(props.animationDuration / 1000).toFixed(1)}s`)

const { resolvedLoading, startLoading, stopLoading } = useDeferredLoading()

const pressed = ref(false)
const animatingOnTap = ref(false)
const disabled = ref(props.disabled)

const SWITCH_ANIMATION_DURATION = 350

const switched = ref(false)

const switchAnimationMillis = ref(`${SWITCH_ANIMATION_DURATION}ms`)
const holdAnimationSeconds = computed(() => `${props.holdTime}s`)

const {
  holding,
  listeners: longPressListeners,
  consumeClick,
} = useLongPress({
  holdTime: () => props.holdTime,
  canStart: () => !disabled.value && !resolvedLoading.value,
  onLongPress: () => {
    switched.value = true
    setTimeout(() => (switched.value = false), SWITCH_ANIMATION_DURATION)
    props.onHold()
  },
})

async function press() {
  pressed.value = !pressed.value
  try {
    startLoading()
    await props.onClick()
    if (pressed.value && props.disabledOnPress) {
      disabled.value = true
    }
  } finally {
    await stopLoading()
  }
}

function handleClick() {
  if (consumeClick()) return
  if (disabled.value || resolvedLoading.value) return
  if (UXConfig().showAnimationOnTap && props.animateTap) {
    startTapAnimation()
  } else {
    press()
  }
}

async function handleDoubleClick() {
  if (disabled.value || resolvedLoading.value) return
  try {
    startLoading()
    await props.onDoubleClick()
  } finally {
    await stopLoading()
  }
}

async function handleHover() {
  if (disabled.value || resolvedLoading.value) return
  await props.onHover()
}

function startTapAnimation() {
  animatingOnTap.value = true
  setTimeout(() => {
    animatingOnTap.value = false
    press()
  }, props.animationDuration)
}

function isPressed(): boolean {
  return pressed.value
}

function reset() {
  pressed.value = false
  disabled.value = false
}

watch(
  () => props.disabled,
  (newVal) => {
    disabled.value = newVal
  },
)

defineExpose({
  isPressed,
  press,
  reset,
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
  --a-btn--bg--held: var(--awesome-button--bg--held, none);
  --a-btn--bg--held--hover: var(--awesome-button--bg--held--hover, none);
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
  transition: all v-bind(animationDurationSeconds) ease-in-out;
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

.awesome-button--holdable {
  -webkit-touch-callout: none;
}

.awesome-button--holding {
  animation: rumble v-bind(holdAnimationSeconds) linear;
}

.awesome-button--switched {
  animation: switch-pop v-bind(switchAnimationMillis) ease-out;
}

.awesome-button--held:not(.awesome-button--disabled) {
  background: var(--a-btn--bg--held);
}

@media (hover: hover) {
  .awesome-button.awesome-button--held:not(.awesome-button--disabled):not(
      .awesome-button--active
    ):hover {
    background: var(--a-btn--bg--held--hover);
  }
}

.awesome-button--held.awesome-button--tapped:not(.awesome-button--disabled):not(
    .awesome-button--active
  ) {
  background: var(--a-btn--bg--held--hover);
}

.awesome-button--invisible {
  visibility: hidden;
}

@media (hover: hover) {
  .awesome-button-wrapper:has(.awesome-button:not(.awesome-button--disabled):hover)
    .awesome-icon-wrapper {
    transform: scale(var(--a-btn--scale-factor--on-hover));
  }
}

.awesome-button-wrapper:has(.awesome-button--tapped:not(.awesome-button--disabled))
  .awesome-icon-wrapper {
  transform: scale(var(--a-btn--scale-factor--on-hover));
}

.awesome-button-wrapper:has(.awesome-button:not(.awesome-button--disabled):active)
  .awesome-icon-wrapper {
  transform: scale(var(--a-btn--scale-factor--on-active));
}

.awesome-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 0;
  transition: transform 0.2s ease-in-out;
  /* Standing compositing layer: avoids the promote/demote around the tap
     scale that re-snaps sub-pixel positions page-wide on iOS WebKit. */
  will-change: transform;
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

@keyframes rumble {
  0% {
    transform: translate(0, 0) rotate(0);
  }
  10% {
    transform: translate(-0.3px, 0.3px) rotate(-0.2deg);
  }
  20% {
    transform: translate(0.4px, -0.4px) rotate(0.3deg);
  }
  30% {
    transform: translate(-0.7px, 0.5px) rotate(-0.5deg);
  }
  40% {
    transform: translate(0.9px, -0.7px) rotate(0.6deg);
  }
  50% {
    transform: translate(-1.1px, 0.9px) rotate(-0.8deg);
  }
  60% {
    transform: translate(1.4px, -1px) rotate(0.9deg);
  }
  70% {
    transform: translate(-1.6px, 1.2px) rotate(-1.1deg);
  }
  80% {
    transform: translate(1.9px, -1.4px) rotate(1.2deg);
  }
  90% {
    transform: translate(-2.1px, 1.6px) rotate(-1.4deg);
  }
  100% {
    transform: translate(0, 0) rotate(0);
  }
}

@keyframes switch-pop {
  0% {
    transform: scale(1);
    filter: brightness(1);
  }
  40% {
    transform: scale(1.07);
    filter: brightness(1.25);
  }
  100% {
    transform: scale(1);
    filter: brightness(1);
  }
}
</style>
