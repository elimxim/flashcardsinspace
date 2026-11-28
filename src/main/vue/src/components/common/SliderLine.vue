<template>
  <div
    class="slider-line slider-line--theme"
    :class="{ 'slider-line--dragging': dragging, 'slider-line--disabled': disabled }"
    role="slider"
    :tabindex="disabled ? -1 : 0"
    :aria-label="label"
    :aria-valuemin="min"
    :aria-valuemax="max"
    :aria-valuenow="value"
    :aria-disabled="disabled"
    @pointerdown="onPointerDown"
    @pointermove="onPointerMove"
    @pointerup="onPointerUp"
    @pointercancel="onPointerUp"
    @keydown="onKeydown"
  >
    <div ref="rail" class="slider-line-rail">
      <div class="slider-line-track">
        <div class="slider-line-fill" :style="{ width: position }" />
      </div>
      <div class="slider-line-thumb" :style="{ left: position }" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const value = defineModel<number>({ default: 0 })

const props = withDefaults(
  defineProps<{
    min?: number
    max?: number
    step?: number
    label?: string
    disabled?: boolean
  }>(),
  {
    min: 0,
    max: 1,
    step: 0.05,
    label: undefined,
    disabled: false,
  },
)

const rail = ref<HTMLElement>()
const dragging = ref(false)

const clamp = (value: number) => Math.min(props.max, Math.max(props.min, value))

const position = computed(() => {
  const range = props.max - props.min
  const ratio = range > 0 ? (clamp(value.value) - props.min) / range : 0
  return `${ratio * 100}%`
})

function moveTo(clientX: number) {
  const rect = rail.value?.getBoundingClientRect()
  if (!rect || rect.width <= 0) return
  const ratio = Math.min(1, Math.max(0, (clientX - rect.left) / rect.width))
  value.value = props.min + ratio * (props.max - props.min)
}

function onPointerDown(event: PointerEvent) {
  if (props.disabled || event.button !== 0) return
  const target = event.currentTarget as HTMLElement
  target.setPointerCapture(event.pointerId)
  target.focus({ preventScroll: true })
  dragging.value = true
  moveTo(event.clientX)
  event.preventDefault()
}

function onPointerMove(event: PointerEvent) {
  if (dragging.value) moveTo(event.clientX)
}

function onPointerUp() {
  dragging.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (props.disabled) return
  switch (event.key) {
    case 'ArrowLeft':
    case 'ArrowDown':
      value.value = clamp(value.value - props.step)
      break
    case 'ArrowRight':
    case 'ArrowUp':
      value.value = clamp(value.value + props.step)
      break
    case 'Home':
      value.value = props.min
      break
    case 'End':
      value.value = props.max
      break
    default:
      return
  }
  event.preventDefault()
}
</script>

<style scoped>
.slider-line--theme {
  --sl--thickness: var(--slider-line--thickness, 5px);
  --sl--thumb-size: var(--slider-line--thumb-size, 18px);
  --sl--color-track: var(--slider-line--color-track, #b8c4d6);
  --sl--color-fill: var(--slider-line--color-fill, #007bff);
  --sl--color-thumb: var(--slider-line--color-thumb, #ededed);
  --sl--color-thumb--hover: color-mix(in srgb, var(--sl--color-thumb) 85%, #fff);
}

.slider-line {
  position: relative;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  height: var(--sl--thumb-size);
  padding: 0 calc(var(--sl--thumb-size) / 2);
  cursor: pointer;
  touch-action: none;
  user-select: none;
  outline: none;
}

.slider-line-rail {
  position: relative;
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
}

.slider-line-track {
  position: relative;
  width: 100%;
  height: var(--sl--thickness);
  border-radius: var(--sl--thickness);
  background-color: var(--sl--color-track);
  overflow: hidden;
}

.slider-line-fill {
  height: 100%;
  background-color: var(--sl--color-fill);
}

.slider-line-thumb {
  position: absolute;
  top: 50%;
  width: var(--sl--thumb-size);
  height: var(--sl--thumb-size);
  border-radius: 50%;
  background-color: var(--sl--color-thumb);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.35);
  transform: translate(-50%, -50%);
  transition: transform 0.15s ease;
}

.slider-line--dragging {
  cursor: grabbing;
}

.slider-line--dragging .slider-line-thumb {
  transform: translate(-50%, -50%) scale(1.15);
}

@media (hover: hover) {
  .slider-line:not(.slider-line--disabled):hover .slider-line-thumb {
    background-color: var(--sl--color-thumb--hover);
  }
}

.slider-line:focus-visible .slider-line-thumb {
  outline: 2px solid var(--sl--color-fill);
  outline-offset: 2px;
}

.slider-line--disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
