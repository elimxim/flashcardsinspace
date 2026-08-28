<template>
  <div
    class="toggle toggle--theme"
    :class="{
      'toggle--overhang': overhang,
      'toggle--vertical': position === 'vertical',
      'toggle--reversed': reverse,
    }"
    :style="{
      '--tgl--speed': `${speed}ms`,
      '--tgl--track-length-factor': trackLengthFactor,
      '--tgl--track-size': trackSize,
      '--tgl--thickness': thickness,
    }"
  >
    <label class="toggle-track">
      <input
        v-model="model"
        type="checkbox"
        role="switch"
        class="toggle-input"
      />
      <span class="toggle-switcher" aria-hidden="true"/>
    </label>
    <div v-if="$slots.default" class="toggle-label-container">
      <slot/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const model = defineModel<boolean>({ default: false })

const props = withDefaults(defineProps<{
  position?: 'horizontal' | 'vertical'
  size?: 'S' | 'M' | 'L'
  overhang?: boolean
  trackLength?: 'S' | 'M' | 'L'
  speed?: number
  reverse?: boolean
}>(), {
  position: 'horizontal',
  size: 'M',
  overhang: false,
  trackLength: 'S',
  speed: 200,
  reverse: false,
})

const trackLengthFactor = computed(() => {
  switch (props.trackLength) {
    case 'S': return 1
    case 'M': return 1.5
    case 'L': return 2
    default: return 1
  }
})

const trackSize = computed(() => {
  switch (props.size) {
    case 'S': return '30px'
    case 'M': return '40px'
    case 'L': return '60px'
    default: return '40px'
  }
})

const thickness = computed(() => {
  switch (props.size) {
    case 'S': return '16px'
    case 'M': return '24px'
    case 'L': return '30px'
    default: return '24px'
  }
})

</script>

<style scoped>
.toggle--theme {
  --tgl--color-on: var(--toggle--color-on, #007bff);
  --tgl--color-off: var(--toggle--color-off, #b8c4d6);
  --tgl--switcher-color: var(--toggle--switcher-color, #ededed);
  --tgl--color-on--hover: color-mix(in srgb, var(--tgl--color-on) 85%, #fff);
  --tgl--color-off--hover: color-mix(in srgb, var(--tgl--color-off) 85%, #fff);
  --tgl--border-color-on: var(--toggle--color-border-on, #007bff);
  --tgl--border-color-off: var(--toggle--color-border-off, #b8c4d6);
  --tgl--border-color-on--hover: color-mix(in srgb, var(--tgl--border-color-on) 85%, #fff);
  --tgl--border-color-off--hover: color-mix(in srgb, var(--tgl--border-color-off) 85%, #fff);
  --tgl--border-width: var(--toggle--border-width, 3px);
  /* calculated variables */
  --tgl--track-length: calc(var(--tgl--track-size) * var(--tgl--track-length-factor));
  --tgl--inset: var(--tgl--border-width);
  --tgl--switcher-size: calc(var(--tgl--thickness) - 2 * var(--tgl--inset));
  --tgl--switcher-offset: calc((var(--tgl--thickness) - var(--tgl--switcher-size)) / 2 - var(--tgl--inset));
  --tgl--travel: calc(var(--tgl--track-length) - var(--tgl--thickness));
}

.toggle--overhang {
  --tgl--switcher-size: calc(var(--tgl--thickness) + 2 * var(--tgl--inset));
}

.toggle {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  user-select: none;
}

.toggle--reversed {
  flex-direction: row-reverse;
}

.toggle--vertical {
  flex-direction: column;
  gap: 2px;
}

.toggle--vertical.toggle--reversed {
  flex-direction: column-reverse;
}

.toggle-input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
  outline: none;
}

.toggle-track {
  position: relative;
  flex-shrink: 0;
  box-sizing: border-box;
  width: var(--tgl--track-length);
  height: var(--tgl--thickness);
  border: var(--tgl--border-width) solid var(--tgl--border-color-off);
  border-radius: var(--tgl--thickness);
  background-color: var(--tgl--color-off);
  transition:
    background-color var(--tgl--speed) ease,
    border-color var(--tgl--speed) ease;
  cursor: pointer;
}

.toggle--vertical .toggle-track {
  width: var(--tgl--thickness);
  height: var(--tgl--track-length);
}

.toggle-switcher {
  position: absolute;
  left: var(--tgl--switcher-offset);
  top: 50%;
  width: var(--tgl--switcher-size);
  height: var(--tgl--switcher-size);
  border-radius: 50%;
  background-color: var(--tgl--switcher-color);
  transform: translateY(-50%);
  transition: transform var(--tgl--speed) ease;
}

.toggle--vertical .toggle-switcher {
  top: auto;
  bottom: var(--tgl--switcher-offset);
  left: 50%;
  transform: translateX(-50%);
}

.toggle-track:has(.toggle-input:checked) {
  background-color: var(--tgl--color-on);
  border-color: var(--tgl--border-color-on);
}

.toggle-track:has(.toggle-input:checked) .toggle-switcher {
  transform: translateY(-50%) translateX(var(--tgl--travel));
}

.toggle--vertical .toggle-track:has(.toggle-input:checked) .toggle-switcher {
  transform: translateX(-50%) translateY(calc(-1 * var(--tgl--travel)));
}

@media (hover: hover) {
  .toggle-track:hover:has(.toggle-input:not(:disabled)) {
    background-color: var(--tgl--color-off--hover);
    border-color: var(--tgl--border-color-off--hover);
  }

  .toggle-track:hover:has(.toggle-input:checked:not(:disabled)) {
    background-color: var(--tgl--color-on--hover);
    border-color: var(--tgl--border-color-on--hover);
  }
}

.toggle-track:has(.toggle-input:focus-visible) {
  outline: 2px solid var(--tgl--color-on);
  outline-offset: 2px;
}

.toggle:has(.toggle-input:disabled) {
  opacity: 0.55;
}

.toggle-track:has(.toggle-input:disabled) {
  cursor: not-allowed;
}

.toggle-label-container {
  position: relative;
  display: flex;
  align-items: center;
}

</style>
