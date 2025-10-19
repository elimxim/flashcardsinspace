<template>
  <div
    v-if="!hidden"
    class="lamp lamp--theme"
    :class="shapeClass"
    role="switch"
    aria-readonly="true"
  >
    <div v-if="on" class="light-base"/>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  on?: boolean
  /** visual size in px */
  size?: number
  /** lamp shape */
  shape?: 'circle' | 'square' | 'diamond' | 'hex'
  /** border width in px */
  borderWidth?: number
  /** spotlight strength 0..1 */
  intensity?: number
  /** core radius as % of size (10..100) */
  radius?: number
  /** light softness in px (blur) */
  softness?: number
  /** hide the lamp */
  hidden?: boolean
}>(), {
  on: false,
  size: 22,
  shape: 'circle',
  borderWidth: 2,
  intensity: 0.5,
  radius: 95,
  softness: 1,
  hidden: false,
})

const shapeClass = computed(() => props.shape ? `shape-${props.shape}` : 'shape-circle')
const sizePx = computed(() => `${props.size}px`)
const borderWidthPx = computed(() => `${props.borderWidth}px`)
const intensityNum = computed(() => Math.max(0, Math.min(1, props.intensity)))
const radiusPct = computed(() => `${Math.max(5, Math.min(100, props.radius))}%`)
const softnessPx = computed(() => `${Math.max(0, props.softness)}px`)
</script>

<style scoped>
.lamp--theme {
  --lamp--color: var(--glow-lamp--color, #5c0be8);
  --lamp--bg-color: var(--glow-lamp--bg-color, #ae9698);
  --lamp--border-color: var(--glow-lamp--border-color, rgba(216, 216, 216, 0.65));
}

.lamp {
  position: relative;
  width: v-bind(sizePx);
  height: v-bind(sizePx);
  border-style: solid;
  border-width: v-bind(borderWidthPx);
  border-color: var(--lamp--border-color);
  background-color: var(--lamp--bg-color);
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.shape-circle {
  border-radius: 999px;
}

.shape-square {
  border-radius: 0;
}

.shape-diamond {
  clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
}

.shape-hex {
  clip-path: polygon(25% 6%, 75% 6%, 100% 50%, 75% 94%, 25% 94%, 0% 50%);
}

.light-base {
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: inherit;
  filter: blur(v-bind(softnessPx));
  background: radial-gradient(circle at 50% 50%,
    color-mix(in srgb, var(--lamp--color) calc(v-bind(intensityNum) * 100%), transparent) 0%,
    color-mix(in srgb, var(--lamp--color) calc(v-bind(intensityNum) * 65%), transparent) calc(v-bind(radiusPct) * .6),
    color-mix(in srgb, var(--lamp--color) calc(v-bind(intensityNum) * 35%), transparent) v-bind(radiusPct),
    color-mix(in srgb, var(--lamp--color) calc(v-bind(intensityNum) * 15%), transparent) calc(v-bind(radiusPct) * 1.8),
    rgba(0, 0, 0, 0) calc(v-bind(radiusPct) * 2.6)
  );
}

</style>
