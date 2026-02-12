<template>
  <div class="totem-scroll totem-scroll--theme">
    <div class="scroll-viewport" ref="viewport" @scroll="handleScroll">
      <div class="scroll-content" ref="contentRef">
        <slot/>
      </div>
    </div>

    <div
      class="scroll-track-wrapper"
      v-show="isScrollable"
      :style="{ width: `${trackWidth}px` }"
    >
      <button
        class="scroll-nav-btn top"
        :style="buttonStyle"
        @click.stop="scrollStep(-1)"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path
            d="M12 5.5c-0.8 0-1.5 0.5-2 1.5L4.5 16.5c-0.8 1.5 0.2 3.5 2 3.5h11c1.8 0 2.8-2 2-3.5L14 7c-0.5-1-1.2-1.5-2-1.5z"/>
        </svg>
      </button>
      <div class="scroll-track" :style="{ gap: `${spacing}px` }">
        <div
          v-for="(_, index) in dotCount"
          :key="index"
          class="scroll-track-dot"
          :class="{ 'is-active': isDotActive(index) }"
          :style="dotStyle"
          @click.stop="scrollToSection(index)"
        />
      </div>
      <button
        class="scroll-nav-btn bottom"
        :style="buttonStyle"
        @click.stop="scrollStep(1)"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path
            d="M12 18.5c0.8 0 1.5-0.5 2-1.5l5.5-9.5c0.8-1.5-0.2-3.5-2-3.5H6.5c-1.8 0-2.8 2-2 3.5l5.5 9.5c0.5 1 1.2 1.5 2 1.5z"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'

const props = withDefaults(defineProps<{
  dotCount?: number
  dotSize?: number
  trackWidth?: number
  buttonSize?: number
  spacing?: number
}>(), {
  dotCount: 4,
  dotSize: 14,
  trackWidth: 32,
  buttonSize: 22,
  spacing: 10,
})

const viewport = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)
const isScrollable = ref(false)
const thumbSize = ref(1)
const thumbStart = ref(0)
let resizeObserver: ResizeObserver | null = null

const dotStyle = computed(() => ({
  width: `${props.dotSize}px`,
  height: `${props.dotSize}px`
}))

const buttonStyle = computed(() => ({
  width: `${props.buttonSize}px`,
  height: `${props.buttonSize}px`
}))

/**
 * calculates:
 * - is the content overflowing? (show/hide scrollbar)
 * - how many dots should be filled? (thumb Size)
 * - which dots should be filled? (thumb Position)
 */
const updateMetrics = () => {
  if (!viewport.value || !contentRef.value) return

  const { scrollTop, clientHeight } = viewport.value
  const scrollHeight = contentRef.value.scrollHeight
  const maxScroll = scrollHeight - clientHeight

  const shouldBeScrollable = maxScroll > 1 // Tolerance of 1px
  if (isScrollable.value !== shouldBeScrollable) {
    isScrollable.value = shouldBeScrollable
    if (shouldBeScrollable) {
      nextTick(updateMetrics)
    }
  }

  if (!shouldBeScrollable) {
    thumbSize.value = props.dotCount
    thumbStart.value = 0
    return
  }

  const visibleRatio = clientHeight / scrollHeight
  let size = 1
  if (visibleRatio > 0.75) {
    size = 3
  } else if (visibleRatio > 0.50) {
    size = 2
  }

  thumbSize.value = Math.min(size, props.dotCount)

  const trackLength = props.dotCount - thumbSize.value
  if (trackLength <= 0) {
    thumbStart.value = 0
  } else {
    const scrollPercentage = maxScroll > 0 ? scrollTop / maxScroll : 0
    thumbStart.value = Math.round(scrollPercentage * trackLength)
  }
}

const handleScroll = () => {
  requestAnimationFrame(updateMetrics)
}

const isDotActive = (index: number) => {
  return index >= thumbStart.value && index < (thumbStart.value + thumbSize.value)
}

const scrollStep = (direction: number) => {
  if (!viewport.value) return
  const step = viewport.value.clientHeight * 0.5
  viewport.value.scrollBy({ top: direction * step, behavior: 'smooth' })
}

const scrollToSection = (index: number) => {
  if (!viewport.value) return
  const scrollHeight = contentRef.value?.scrollHeight || 0
  const maxScroll = scrollHeight - viewport.value.clientHeight
  const targetRatio = index / (props.dotCount - 1)
  viewport.value.scrollTo({ top: targetRatio * maxScroll, behavior: 'smooth' })
}

onMounted(() => {
  updateMetrics()
  if (contentRef.value) {
    resizeObserver = new ResizeObserver(() => {
      updateMetrics()
    })
    resizeObserver.observe(contentRef.value)
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})
</script>

<style scoped>
.totem-scroll--theme {
  --scroll--color--main: var(--totem-scroll--color--main, #9f9f9f);
  --scroll--color--active: var(--totem-scroll--color--active, #9f9f9f);
  --scroll--color--hover: var(--totem-scroll--color--hover, #686868);
}

.totem-scroll {
  position: relative;
  height: 100%;
  width: 100%;
  display: flex;
  overflow: hidden;
}

.scroll-viewport {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.scroll-viewport::-webkit-scrollbar {
  display: none;
}

.scroll-content {
  min-height: 100%;
  box-sizing: border-box;
}

.scroll-track-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: v-bind('spacing + "px"');
  padding: 10px 0;
  user-select: none;
  flex-shrink: 0;
  color: var(--scroll--color--main);
  transition: color 0.3s ease;
}

@media (hover: hover) {
  .scroll-track-wrapper:hover {
    --scroll--color--main: var(--scroll--color--hover);
    --scroll--color--active: var(--scroll--color--hover);
    color: var(--scroll--color--hover);
  }
}

.scroll-track {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: v-bind('spacing + "px"');
}

.scroll-track-dot {
  border-radius: 50%;
  border: 1px solid var(--scroll--color--main);
  box-sizing: border-box;
  background-color: transparent;
  cursor: pointer;
  transition: background-color 0.2s ease,
  border-color 0.2s ease,
  transform 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.scroll-track-dot.is-active {
  background-color: var(--scroll--color--active);
  border-color: var(--scroll--color--active);
}

.scroll-nav-btn {
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: inherit;
  transition: transform 0.2s ease, color 0.2s ease;
}

@media (hover: hover) {
  .scroll-track-dot:hover {
    transform: scale(1.25);
  }

  .scroll-nav-btn:hover {
    transform: scale(1.25);
  }
}

.scroll-nav-btn:active {
  transform: scale(0.95);
}

.scroll-nav-btn svg {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
