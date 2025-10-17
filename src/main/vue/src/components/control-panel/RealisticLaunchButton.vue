<template>
  <button
    ref="launchButton"
    v-bind="$attrs"
    class="launch-button launch-button--theme"
    type="button"
    :class="{ disabled }"
    :disabled="disabled"
    @click="onPress"
  >
    <span class="label-glow" aria-hidden="true">{{ label }}</span>
    <span class="glass-pane" aria-hidden="true"></span>
    <span class="label">{{ label }}</span>

    <span
      v-for="r in ripples"
      :key="r.id"
      class="ripple"
      :style="{ left: r.x + 'px', top: r.y + 'px' }"
      aria-hidden="true"
    />
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  onClick: () => void
  label?: string
  disabled?: boolean
  accent?: string
  glow?: string
}>(), {
  label: 'Launch',
  disabled: false,
  accent: '#9be7ff',
  glow: '#6be4ff',
})

const launchButton = ref<HTMLButtonElement>()

interface Ripple {
  id: number
  x: number
  y: number
}

const ripples = ref<Ripple[]>([])
let rippleId = 0

function onPress(event: MouseEvent) {
  if (props.disabled) return
  createRipple(event)
  props.onClick()
}

function createRipple(event: MouseEvent) {
  if (!launchButton.value) return
  const rect = launchButton.value.getBoundingClientRect()
  const ripple: Ripple = {
    id: ++rippleId,
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
  }
  ripples.value.push(ripple)
  setTimeout(() => {
    ripples.value = ripples.value.filter(r => r.id !== ripple.id)
  }, 500)
}
</script>

<style scoped>
.launch-button--theme {
  --launch--size: var(--realistic-launch-button--size, 100px);
  --launch--border-radius: var(--realistic-launch-button--border-radius, 3px);
  --launch--label-color: var(--realistic-launch-button--text-color, #e6f7ff);
  --launch--lable-color--glowing: var(--realistic-launch-button--text-color--glowing, #6be4ff);
  --launch--shadow-color: var(--realistic-launch-button--shadow-color, rgba(0, 0, 0, 0.18));
  --launch--shadow-color--hover: var(--realistic-launch-button--shadow-color, rgba(0, 0, 0, 0.3));
  --laubch--riple--bg-color: var(--realistic-launch-button--ripple--bg-color, #fff3);
  --launch--bg-color--from: var(--realistic-launch-button--bg-color--from, #3f4d68);
  --launch--bg-color--via: var(--realistic-launch-button--bg-color--via, #616f8a);
}

.launch-button {
  position: relative;
  display: grid;
  place-items: center;
  width: var(--launch--size);
  height: var(--launch--size);
  aspect-ratio: 1 / 1;
  border: none;
  cursor: pointer;
  overflow: hidden;
  user-select: none;
  text-transform: uppercase;
  word-break: break-word;
  border-radius: var(--launch--border-radius);
  background: radial-gradient(80% 60% at 50% 40%, var(--launch--bg-color--from), var(--launch--bg-color--via) 90%);
  box-shadow: 0 2px 4px var(--launch--shadow-color);
  transition: transform .12s ease, filter .25s ease, box-shadow .25s ease;
}

.launch-button:not(.disabled):active {
  transform: translateY(1px);
}

.launch-button.disabled {
  cursor: default;
  opacity: .65;
}

.glass-pane {
  position: absolute;
  inset: 4px;
  border-radius: var(--launch--border-radius);
  background:
    linear-gradient(180deg, #ffffff2a, #ffffff10 40%, #00000055 80%, #0000),
    radial-gradient(60% 40% at 50% 0%, #ffffff14, #0000 60%);
  backdrop-filter: blur(8px) saturate(1.3);
  -webkit-backdrop-filter: blur(8px) saturate(1.3);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, .25),
    inset 0 8px 24px rgba(255, 255, 255, .12),
    inset 0 -12px 24px rgba(0, 0, 0, .35),
    0 8px 22px rgba(0, 0, 0, .35);
}

.label {
  position: relative;
  z-index: 3;
  font-size: clamp(10px, 1.2vw, 14px);
  letter-spacing: .18em;
  color: var(--launch--label-color);
}

.label-glow {
  position: absolute;
  z-index: 1;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: clamp(10px, 1.2vw, 14px);
  letter-spacing: .18em;
  color: var(--launch--lable-color--glowing);
  filter: blur(6px) saturate(1.3);
  opacity: .9;
}

.launch-button:not(.disabled):hover {
  filter: saturate(1.2) brightness(1.2);
  box-shadow: 0 2px 4px var(--launch--shadow-color--hover)
}

.launch-button:not(.disabled):hover .label-glow {
  opacity: 1;
  filter: blur(6px) saturate(2)
}

.ripple {
  position: absolute;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  background: var(--laubch--riple--bg-color);
  animation: ripple .65s ease-out forwards;
  z-index: 4
}

@keyframes ripple {
  from {
    width: 0;
    height: 0;
    opacity: .45
  }
  to {
    width: 360px;
    height: 360px;
    opacity: 0
  }
}

</style>
