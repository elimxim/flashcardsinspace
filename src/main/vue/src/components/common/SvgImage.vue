<template>
  <span class="svg-image-container" :style="containerStyle">
    <img
      :src="imagePath"
      :alt="alt"
      :width="toCssSize(width)"
      :height="toCssSize(height)"
      :loading="loading"
      :decoding="decoding"
      :fetchpriority="fetchPriority"
      :draggable="!nonInteractive"
      :class="{ 'non-interactive': nonInteractive }"
      class="svg-image"
      @selectstart="onSelectStart"
      @dragstart="onDragStart"
      @contextmenu="onContextMenu"
    />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  /** Path to the SVG file, e.g. '/assets/' */
  path: string
  /** SVG file name, e.g. 'Melvin.svg' */
  name: string
  /** Alt text for <img> */
  alt?: string
  /** Rendered width */
  width?: number | string
  /** Rendered height */
  height?: number | string
  /** Native lazy/eager loading hint */
  loading?: 'lazy' | 'eager'
  /** Native decoding hint */
  decoding?: 'sync' | 'async' | 'auto'
  /** Fetch priority hint */
  fetchPriority?: 'high' | 'low' | 'auto'
  /** Makes <img> non-selectable, non-draggable, non-context-interactive */
  nonInteractive?: boolean
}>(), {
  alt: 'No image',
  width: undefined,
  height: undefined,
  loading: 'lazy',
  decoding: 'async',
  fetchPriority: 'auto',
  nonInteractive: false,
})

const imagePath = computed(() => `${props.path}/${props.name}`)

const toCssSize = (v: number | string | undefined) =>
  typeof v === 'number' ? `${v}px` : v

const containerStyle = computed(() => {
  const style: Record<string, string> = {}
  const w = toCssSize(props.width)
  const h = toCssSize(props.height)
  if (w !== undefined) style.width = w
  if (h !== undefined) style.height = h
  return style
})

const onSelectStart = (e: Event) => {
  if (props.nonInteractive) e.preventDefault()
}

const onDragStart = (e: DragEvent) => {
  if (props.nonInteractive) e.preventDefault()
}

const onContextMenu = (e: MouseEvent) => {
  if (props.nonInteractive) e.preventDefault()
}
</script>

<style scoped>
.svg-image-container {
  position: relative;
  display: block;
}

.svg-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.non-interactive {
  user-select: none;         /* Standard */
  -webkit-user-select: none; /* Safari, Chrome */
  -webkit-user-drag: none;   /* Safari, Chrome */
  -moz-user-select: none;    /* Firefox */
  -ms-user-select: none;     /* Edge */
  -webkit-touch-callout: none;
}

/* Reduces tap delay & some gesture defaults without blocking clicks */
:where(img) {
  touch-action: manipulation;
}
</style>
