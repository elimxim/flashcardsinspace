<template>
  <picture
    :class="{ 'non-interactive': nonInteractive }"
    @selectstart.prevent="onSelectStart"
    @dragstart.prevent="onDragStart"
    @contextmenu.prevent="onContextMenu"
  >
    <!-- Modern formats first -->
    <source
      v-for="format in formats"
      :key="format"
      :type="typeFor(format)"
      :srcset="srcsetFor(format)"
      :sizes="!dpr ? imgSizes : undefined"
    />
    <!-- Fallback image -->
    <img
      :src="fallbackImgSrc"
      :srcset="fallbackImgSrcset"
      :sizes="!dpr ? imgSizes : undefined"
      :alt="alt"
      :width="dimensions?.width"
      :height="dimensions?.height"
      :loading="imgLoading"
      :decoding="imgDecoding"
      :fetchpriority="imgFetchPriority"
      v-bind="$attrs"
      :draggable="!nonInteractive"
      :class="{ 'non-interactive': nonInteractive }"
    />
  </picture>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Format = 'avif' | 'webp' | 'jpg' | 'jpeg' | 'png'

const props = withDefaults(defineProps<{
  /** Image base path, e.g. '/img/hero' */
  base: string
  /** Alt text for <img> */
  alt?: string
  /** Image formats order (without fallbackExt) */
  formats?: Format[]
  /** Fallback extension for <img> */
  fallbackExt?: Format
  /** widths for width-based srcset (ignored in dpr mode) */
  widths?: number[]
  /** Intrinsic dimensions to prevent Cumulative Layout Shift (CLS) */
  dimensions?: { width: number; height: number }
  /** Overrides fetchPriority; defaults high if hero */
  fetchPriority?: 'high' | 'low' | 'auto'
  /** Automatic sizes builder inputs */
  breakpoints?: Array<{ max: number; vw: number }>
  /** Final absolute px width after the last breakpoint */
  containerMaxPx?: number
  /** DPR (Device Pixel Ratio) mode for avatars */
  dpr?: boolean
  /** LCP (Largest Contentful Paint) hero mode */
  hero?: boolean
  /** Makes <img> non-selectable, non-draggable, non-context-interactive */
  nonInteractive?: boolean
}>(), {
  alt: 'No image available',
  fallbackExt: 'jpg',
  widths: () => [640, 960, 1280, 1920],
  dimensions: undefined,
  formats: () => ['avif', 'webp'],
  fetchPriority: undefined,
  breakpoints: () => ([
    { max: 640, vw: 96 },
    { max: 1200, vw: 90 },
  ]),
  containerMaxPx: 1200,
  dpr: false,
  hero: false,
  nonInteractive: false,
})

const imgLoading = computed(() => props.hero ? 'eager' : 'lazy')
const imgDecoding = computed(() => (props.hero ? 'sync' : 'async'))
const imgFetchPriority = computed<'high' | 'low' | 'auto'>(() => {
  if (props.fetchPriority) return props.fetchPriority
  return props.hero ? 'high' : 'auto'
})

const imgSizes = computed(() => {
  const parts = [...props.breakpoints]
    .sort((a, b) => a.max - b.max)
    .map(b => `(max-width: ${b.max}px) ${Math.min(Math.max(b.vw, 1), 100)}vw`)
  parts.push(`${props.containerMaxPx}px`)
  return parts.join(', ')
})

function typeFor(fmt: Format) {
  return `image/${fmt === 'jpg' ? 'jpeg' : fmt}`
}

function srcsetFor(format: Format) {
  if (props.dpr) {
    const x1 = `${props.base}@1x.${format}`
    const x2 = `${props.base}@2x.${format}`
    const x4 = `${props.base}@4x.${format}`
    return `${x1} 1x, ${x2} 2x, ${x4} 4x`
  }
  return props.widths.map(w => `${props.base}-${w}.${format} ${w}w`).join(', ')
}

const fallbackImgSrcset = computed(() => srcsetFor(props.fallbackExt))
const fallbackImgSrc = computed(() => {
  if (props.dpr) return `${props.base}.${props.fallbackExt}`
  // choosing a reasonable default (2nd width) to avoid huge initial fetch
  const index = Math.min(1, props.widths.length - 1)
  return `${props.base}-${props.widths[index]}.${props.fallbackExt}`
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
.non-interactive,
.non-interactive * {
  user-select: none;         /* Standard */
  -webkit-user-select: none; /* Safari, Chrome */
  -webkit-user-drag: none;   /* Safari, Chrome */
  -moz-user-select: none;    /* Firefox */
  -ms-user-select: none;     /* Edge */
  -webkit-touch-callout: none;
}

/* Reduces 300ms tap delay & some gesture defaults without blocking clicks */
:where(picture, img) {
  touch-action: manipulation;
}
</style>
